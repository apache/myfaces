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
 *
 * Abstract base request encapuslating all methods and variables
 * shared over all different request objects
 *
 * Author: Werner Punz (latest modification by $Author: ganeshpuri $)
 * Version: $Revision: 1.4 $ $Date: 2009/05/31 09:16:44 $
 */
/** @namespace myfaces._impl.xhrCore._AjaxRequest */
myfaces._impl.core._Runtime.extendClass("myfaces._impl.xhrCore._BaseRequest", myfaces._impl.xhrCore._FinalizeableObj, {

    _Dom: null,
    _Lang: null,
    _RT: myfaces._impl.core._Runtime,

    _contentType: "application/x-www-form-urlencoded",
    _source: null,
    _xhr: null,
    _encoding:null ,

    _context:null,
    _ajaxUtil: null,
    _sourceForm: null,
    _passThrough: null,
    _requestParameters: null,
    _exception: null,
    _timeout: null,
    _delay:null,
    _queueSize:-1,
    _xhrQueue: null,

    _partialIdsArray : null,
    //callbacks for onDone... done issues
    //onSuccess everything has passed through
    //onError server side error

    //onException exception thrown by the client
    //onWarning warning issued by the client
    //onException exception thrown by the client
    //onWarning warning issued by the client
    _onDone : null,
    _onSuccess: null,
    _onError: null,
    _onException: null,
    _onWarning: null,
    _onTimeout:null,

    /*response object which is exposed to the queue*/
    _response: null,

    _timeoutId: null,

    /*all instance vars can be set from the outside
     * via a parameter map*/
    _ajaxType: "POST",

    /*
     * constants used internally
     */
    _CONTENT_TYPE:"Content-Type",
    _HEAD_FACES_REQ:"Faces-Request",

    _READY_STATE_DONE: 4,
    _STATUS_OK_MINOR: 200,
    _STATUS_OK_MAJOR: 300,

    _VAL_AJAX: "partial/ajax",




    //abstract methods which have to be implemented
    //since we do not have abstract methods we simulate them
    //by using empty ones
    constructor_: function() {
        this._callSuper("constructor");
        this._initDefaultFinalizableFields();

        this._Lang = myfaces._impl._util._Lang;
        this._Dom = myfaces._impl._util._Dom;

        //we fixate the scopes just in case
        this._onException = this._Lang.hitch(this, this._stdErrorHandler);
        this._onWarn = this._Lang.hitch(this, this._stdErrorHandler);
        this._onError = this._Lang.hitch(this, this._stdXhrServerError);
        this._onSuccess = this._Lang.hitch(this, this._stdOnSuccess);
        this._onDone = this._Lang.hitch(this, this._stdOnDone);
        this._onTimeout = this._Lang.hitch(this, this._stdOnTimeout);

        //we clear any exceptions being thrown
        //if(this._xhrQueue) {
        //    if(this._xhrQueue.isEmpty()) {
        this._Lang.clearExceptionProcessed();
        //    }
        //}

    },

    /**
     * send the request
     */
    send: function() {
    },

    /**
     * the callback function after the request is done
     */
    callback: function() {

    },

    //non abstract ones
    /**
     * Spec. 13.3.1
     * Collect and encode input elements.
     * Additionally the hidden element javax.faces.ViewState
     *
     *
     * @return  an element of formDataWrapper
     * which keeps the final Send Representation of the
     */
    getViewState : function() {
        var ret = this._Lang.createFormDataDecorator(new Array());

        this._ajaxUtil.encodeSubmittableFields(ret, this._xhr, this._context, this._source,
                this._sourceForm, this._partialIdsArray);

        return ret;
    },

    _getImpl: function() {
        this._Impl = this._Impl || myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
        return this._Impl;
    },

    /**
     * standard on done handler which routes to the
     * event sending specified by the spec
     *
     * @param ajaxRequest request object holding all data
     */
    _stdOnDone: function() {
        this._getImpl().sendEvent(this._xhr, this._context, this._getImpl().COMPLETE);
    },

    /**
     * standard spec compliant success handler
     *
     * @param request the xhr request object
     * @param context the context holding all values for further processing
     */
    _stdOnSuccess: function() {
        //_onSuccess
        var _Impl = this._getImpl();

        try {
            //now we have to reroute into our official api
            //because users might want to decorate it, we will split it apart afterwards
            this._context._mfInternal = this._context._mfInternal || {};
            this._context._mfInternal._mfRequest = this;

            jsf.ajax.response(this._xhr, this._context);

            _Impl.sendEvent(this._xhr, this._context, _Impl.SUCCESS);
        } finally {
            if (this.isQueued()) {
                this._xhrQueue.processQueue();
            }
            //ie6 helper cleanup
            delete this._context.source;
        }
    },


    /**
     * now to the error case handlers which by spec should
     * route to our standard error queue
     *
     * @param {BaseRequest} ajaxRequest the ajax request object
     */
    _stdXhrServerError: function() {
        var _Impl = this._getImpl();

        //_onError
        var errorText;
        try {
            var UNKNOWN = this._Lang.getMessage("UNKNOWN");
            var errorText = this._Lang.getMessage("ERR_REQU_FAILED", null,
                    (this._xhr.status || UNKNOWN),
                    (this._xhr.statusText || UNKNOWN));

        } catch (e) {
            errorText = this._Lang.getMessage("ERR_REQ_FAILED_UNKNOWN", null);
        } finally {
            try {
                _Impl.sendError(this._xhr, this._context, _Impl.HTTPERROR,
                        _Impl.HTTPERROR, errorText);
            } finally {
                if (this._xhrQueue) {
                    this._xhrQueue.processQueue();
                }
                //ie6 helper cleanup
                delete this.getContext().source;

            }
        }
        //_onError

    },

    /**
     * standard timeout handler
     * the details on how to handle the timeout are
     * handled by the calling request object
     */
    _stdOnTimeout: function(request, context) {
        var _Impl = this._getImpl();
        try {
            //we issue an event not an error here before killing the xhr process
            _Impl.sendEvent(request, context, _Impl.TIMEOUT_EVENT,
                    _Impl.TIMEOUT_EVENT);
            //timeout done we process the next in the queue
        } finally {
            //We trigger the next one in the queue
            if (this._xhrQueue) {
                this._xhrQueue.processQueue();
            }
        }
        //ready state done should be called automatically
    },

    /**
     * Client error handlers which also in the long run route into our error queue
     * but also are able to deliver more meaningful messages
     * note, in case of an error all subsequent xhr requests are dropped
     * to get a clean state on things
     *
     * @param request the xhr request object
     * @param context the context holding all values for further processing
     * @param sourceClass (String) the issuing class for a more meaningful message
     * @param func the issuing function
     * @param exception the embedded exception
     */
    _stdErrorHandler: function(request, context, sourceClass, func, exception) {
        try {
            var _Impl = this._getImpl();
            _Impl.stdErrorHandler(request, context, sourceClass, func, exception);
        } finally {
            if (this._xhrQueue) {
                this._xhrQueue.cleanup();
            }
        }
    },

    getXhr: function() {
        return this._xhr;
    },

    getContext: function() {
        return this._context;
    },

    setQueue: function(queue) {
        this._xhrQueue = queue;
    },

    isQueued: function() {
        return this._xhrQueue;
    }


});