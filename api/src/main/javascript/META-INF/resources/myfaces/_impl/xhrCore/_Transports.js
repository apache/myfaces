/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
/**
 * The xhr core adapter
 * which provides the transport mechanisms to the calling
 * objects, and controls the queue behavior, the error handling
 * and partial page submit functionality among other things
 *
 * The idea behind this is to make the ajax request object as barebones
 * as possible and shift the extra functionality like queuing
 * parameter handling etc... to this class so that our transports become more easily
 * pluggable. This should keep the path open to iframe submits and other transport layers
 *
 * the call to the corresponding transport just should be a
 * transport.xhrQueuedPost
 *
 * or transport.xhrPost,transport.xhrGet  etc... in the future
 */
/** @namespace myfaces._impl.xhrCore._Transports */
myfaces._impl.core._Runtime.extendClass("myfaces._impl.xhrCore._Transports"
        , Object, {

    _PAR_ERRORLEVEL:"errorlevel",
    _PAR_QUEUESIZE:"queuesize",
    _PAR_PPS:"pps",
    _PAR_TIMEOUT:"timeout",
    _PAR_DELAY:"delay",

    /**
     * a singleton queue
     * note the structure of our inheritance
     * is that that _queue is attached to prototype
     * and hence the pointer to the request qeue
     * is shared over all instances
     *
     * if you need to have it per instance for complex objects
     * you have to initialize in the constructor
     *
     * (This is the same limitation dojo class inheritance
     * where our inheritance pattern is derived from has)
     */
    _q: new myfaces._impl.xhrCore._AjaxRequestQueue(),

    _threshold: "ERROR",

    _Lang :  myfaces._impl._util._Lang,


    /**
     * mapped options already have the exec and view properly in place
     * myfaces specifics can be found under mappedOptions.myFaces
     * @param {Node} source the source of this call
     * @param {Node} sourceForm the html form which is the source of this call
     * @param {Object} context (Map) the internal pass through context
     * @param {Object} passThrgh (Map) values to be passed through
     **/
    xhrQueuedPost : function(source, sourceForm, context, passThrgh) {
        this._q.enqueue(
                new myfaces._impl.xhrCore._AjaxRequest(this._getArguments(source, sourceForm, context, passThrgh)));
    },

    /**
     * Spec. 13.3.3
     * Examining the response markup and updating the DOM tree
     * @param {XmlHttpRequest} request - the ajax request
     * @param {XmlHttpRequest} context - the ajax context
     */
    response : function(request, context) {
        this._q._curReq._response.processResponse(request, context);
    },

    /**
     * creates the arguments map and
     * fetches the config params in a proper way in to
     * deal with them in a flat way (from the nested context way)
     *
     * @param source the source of the request
     * @param sourceForm the sourceform
     * @param context   the context holding all values
     * @param passThrgh the passThrough values to be blended into the response
     */
    _getArguments: function(source, sourceForm, context, passThrgh) {
        var _RT = myfaces._impl.core._Runtime;
        var _getConfig = _RT.getLocalOrGlobalConfig;
        var _Lang = myfaces._impl._util._Lang;

        var ret = {
            "source": source,
            "sourceForm": sourceForm,
            "context": context,
            "passThrough": passThrgh,
            "xhrQueue": this._q,

            //standard done callback
            "onDone": this._Lang.hitch(this, this._stdOnDone),
            //standard success callback
            "onSuccess": this._Lang.hitch(this, this._stdOnSuccess),
            //standard server side error callback
            "onError": this._Lang.hitch(this, this._stdOnError),

            //standard timeout callback
            "onTimeout": this._Lang.hitch(this, this._stdOnTimeout),
            //standard exception handling callback
            "onException": this._Lang.hitch(this, this._stdErrorHandler),
            //standard warning handling callback
            "onWarn": this._Lang.hitch(this, this._stdErrorHandler)
        };

        //we now mix in the config settings which might either be set globally
        //or pushed in under the context myfaces.<contextValue> into the current request 
        this._applyConfig(ret, "alarmThreshold", this._PAR_ERRORLEVEL);
        this._applyConfig(ret, "queueSize", this._PAR_QUEUESIZE);
        this._applyConfig(ret, "timeout", this._PAR_TIMEOUT);
        this._applyConfig(ret, "delay", this._PAR_DELAY);

        //now partial page submit needs a different treatment
        //since pps == execute strings
        if (_getConfig(context, this._PAR_PPS, null) != null
                && _Lang.exists(passThrgh, myfaces._impl.core.Impl.P_EXECUTE)
                && passThrgh[myfaces._impl.core.Impl.P_EXECUTE].length > 0) {
            ret['partialIdsArray'] = passThrgh[myfaces._impl.core.Impl.P_EXECUTE].split(" ");
        }
        return ret;
    },

    /**
     * helper method to apply a config setting to our varargs param list
     *
     * @param destination the destination map to receive the setting
     * @param destParm the destination param of the destination map
     * @param srcParm the source param which is the key to our config setting
     */
    _applyConfig: function(destination, destParm, srcParm) {
        var _RT = myfaces._impl.core._Runtime;
        var _getConfig = _RT.getLocalOrGlobalConfig;
        if (_getConfig(this._context, srcParm, null) != null) {
            destination[destParm] = _getConfig(this._context, srcParm, null);
        }
    },

    /**
     * standard on done handler which routes to the
     * event sending specified by the spec
     *
     * @param request the xhr request object
     * @param context the context holding all values for further processing
     */
    _stdOnDone: function(request, context) {
        this._loadImpl();

        this._Impl.sendEvent(request, context, this._Impl.COMPLETE);
    },

    /**
     * standard spec compliant success handler
     *
     * @param request the xhr request object
     * @param context the context holding all values for further processing
     */
    _stdOnSuccess: function(request, context) {
        //_onSuccess
        this._loadImpl();

        this._Impl.response(request, context);
        this._Impl.sendEvent(request, context, this._Impl.SUCCESS);
        this._q.processQueue();

    },

    /**
     * now to the error case handlers which by spec should
     * route to our standard error queue
     *
     * @param request the xhr request object
     * @param context the context holding all values for further processing
     */
    _stdOnError: function(request, context) {
        this._loadImpl();

        //_onError
        var errorText;
        try {
            errorText = "Request failed";
            if (request.status) {
                errorText += "with status " + request.status;
                if (request.statusText) {
                    errorText += " and reason " + this._xhr.statusText;
                }
            }
        } catch (e) {
            errorText = "Request failed with unknown status";
        }
        //_onError
        this._Impl.sendError(request, context, this._Impl.HTTPERROR,
                this._Impl.HTTPERROR, errorText);
    },



    /**
     * standard timeout handler
     */
    _stdOnTimeout: function() {
        this._q._curReq.abort();
    },

    /**
     * Client error handlers which also in the long run route into our error queue
     * but also are able to deliver more meaningful messages
     *
     *
     * @param request the xhr request object
     * @param context the context holding all values for further processing
     * @param sourceClass (String) the issuing class for a more meaningful message
     * @param func the issuing function
     * @param exception the embedded exception
     */
    _stdErrorHandler: function(request, context, sourceClass, func, exception) {
        this._loadImpl();
        
        if (this._threshold == "ERROR" && !exception._processed) {
            this._Impl.sendError(request, context, this._Impl.CLIENT_ERROR, exception.name,
                    "MyFaces ERROR:" + this._Lang.createErrorMsg(sourceClass, func, exception));
        }
        this._q.cleanup();
        //we forward the exception, just in case so that the client
        //will receive it in any way
        exception._processed = true;
        throw exception;
    },

    /**
     * Standard non blocking warnings handler
     *
     * @param request the xhr request object
     * @param context the context holding all values for further processing
     * @param sourceClass (String) the issuing class for a more meaningful message
     * @param func the issuing function
     * @param exception the embedded exception
     */
    _stdWarningsHandler: function(request, context, sourceClass, func, exception) {
        this._loadImpl();

        if (this._threshold == "WARNING" || this._threshold == "ERROR") {
            this._Impl.sendError(request, context, this._Impl.CLIENT_ERROR, exception.name,
                    "MyFaces WARNING:" + this._Lang.createErrorMsg(sourceClass, func, exception));
        }
        this.destroy();
    },

    _loadImpl: function() {
        if(!this._Impl) {
            this._Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
        }
        return this._Impl;
    }

});
