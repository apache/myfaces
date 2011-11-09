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
 * An implementation of an xhr request object
 * with partial page submit functionality, and jsf
 * ppr request and timeout handling capabilities
 *
 * Author: Werner Punz (latest modification by $Author: ganeshpuri $)
 * Version: $Revision: 1.4 $ $Date: 2009/05/31 09:16:44 $
 */

/**
 * @class
 * @name _AjaxRequest
 * @memberOf myfaces._impl.xhrCore
 * @extends myfaces._impl.core.Object
 */
_MF_CLS(_PFX_XHR + "_AjaxRequest", _MF_OBJECT, /** @lends myfaces._impl.xhrCore._AjaxRequest.prototype */ {

    _contentType: "application/x-www-form-urlencoded",
    /** source element issuing the request */
    _source: null,
    /** encoding for the submit */
    _encoding:null ,
    /** context passed down from the caller */
    _context:null,
    /** source form issuing the request */
    _sourceForm: null,
    /** passthrough parameters */
    _passThrough: null,

    /** queue control */
    _timeout: null,
    /** enqueuing delay */
    //_delay:null,
    /** queue size */
    _queueSize:-1,

    /**
     back reference to the xhr queue,
     only set if the object really is queued
     */
    _xhrQueue: null,

    /** pps an array of identifiers which should be part of the submit, the form is ignored */
    _partialIdsArray : null,

    /** xhr object, internal param */
    _xhr: null,

    /** response object which is exposed to the queue */
    _response: null,

    /** predefined method */
    _ajaxType:"POST",

    //CONSTANTS
    ENCODED_URL:"javax.faces.encodedURL",
    /*
     * constants used internally
     */
    _CONTENT_TYPE:"Content-Type",
    _HEAD_FACES_REQ:"Faces-Request",
    _VAL_AJAX: "partial/ajax",
    _XHR_CONST: myfaces._impl.xhrCore.engine.XhrConst,

    // _exception: null,
    // _requestParameters: null,
    /**
     * Constructor
     * <p />
     * note there is a load of common properties
     * inherited by the base class which define the corner
     * parameters and the general internal behavior
     * like _onError etc...
     * @param {Object} args an arguments map which an override any of the given protected
     * instance variables, by a simple name value pair combination
     */
    constructor_: function(args) {

        try {
            this._callSuper("constructor_", args);

            this._onException = this._Lang.hitch(this, this._stdErrorHandler);
            this._onWarn = this._Lang.hitch(this, this._stdErrorHandler);
            this._initDefaultFinalizableFields();
            delete this._resettableContent["_xhrQueue"];

            this.applyArgs(args);
            var mfInternal = this._context._mfInternal;
            mfInternal._onException = this._onException;
            mfInternal._onWarning = this._onWarn;

            /*namespace remapping for readability*/
            //we fetch in the standard arguments
            //and apply them to our protected attributes
            //we do not gc the entry hence it is not defined on top
            var xhrCore = myfaces._impl.xhrCore;
            this._AJAXUTIL = xhrCore._AjaxUtils;

            //we cannot eliminate it due to the direct reference to its request the response needs
            //at least for now, in the long run we can and must
            this._response = xhrCore._AjaxResponse;
        } catch (e) {
            //_onError
            this._onException(this._xhr, this._context, "myfaces._impl.xhrCore._AjaxRequest", "constructor", e);
        }
    },

    /**
     * Sends an Ajax request
     */
    send : function() {

        var _Lang = this._Lang;

        try {

            var scopeThis = _Lang.hitch(this, function(functionName) {
                return _Lang.hitch(this, this[functionName]);
            });
            this._xhr = _Lang.mixMaps(this._getTransport(), {
                onprogress: scopeThis("onprogress"),
                ontimeout:  scopeThis("ontimeout"),
                onloadend:  scopeThis("ondone"),
                onload:     scopeThis("onsuccess"),
                onerror:    scopeThis("onerror")

            }, true);
            var xhr = this._xhr,
                    sourceForm = this._sourceForm,
                    targetURL = (typeof sourceForm.elements[this.ENCODED_URL] == 'undefined') ?
                            sourceForm.action :
                            sourceForm.elements[this.ENCODED_URL].value,
                    formData = this.getFormData();

            for (var key in this._passThrough) {
                formData.append(key, this._passThrough[key]);
            }

            xhr.open(this._ajaxType, targetURL +
                    ((this._ajaxType == "GET") ? "?" + this._formDataToURI(formData) : "")
                    , true);
            xhr.timeout = this._timeout || 0;
            var contentType = this._contentType;
            if (this._encoding) {
                contentType = contentType + "; charset:" + this._encoding;
            }

            xhr.setRequestHeader(this._CONTENT_TYPE, this._contentType);
            xhr.setRequestHeader(this._HEAD_FACES_REQ, this._VAL_AJAX);

            this._sendEvent("BEGIN");
            //Check if it is a custom form data object
            //if yes we use makefinal for the final handling
            if (formData && formData.makeFinal) {
                formData = formData.makeFinal()
            }
            xhr.send((this._ajaxType != "GET") ? formData : null);

        } catch (e) {
            //_onError//_onError
            this._onException(this._xhr, this._context, "myfaces._impl.xhrCore._AjaxRequest", "send", e);
        }
    },


    ondone: function() {
        this._requestDone();
    },


    onsuccess: function(evt) {

        var context = this._context;
        var xhr = this._xhr;
        try {
            this._sendEvent("COMPLETE");
            //now we have to reroute into our official api
            //because users might want to decorate it, we will split it apart afterwards
            context._mfInternal = context._mfInternal || {};
            context._mfInternal._mfRequest = this;

            jsf.ajax.response((xhr.getXHRObject) ? xhr.getXHRObject() : xhr, context);

            //an error in the processing has been raised
            if(!context._mfInternal.internalError) {
                this._sendEvent("SUCCESS");
            }
        } catch (e) {
            this._onException(xhr, context, "myfaces._impl.xhrCore._AjaxRequest", "callback", e);
        }
    },

    onerror: function(evt) {

        var context = this._context;
        var xhr = this._xhr;
        var _Lang = this._Lang;

        var errorText = "";
        this._sendEvent("COMPLETE");
        try {
            var UNKNOWN = _Lang.getMessage("UNKNOWN");
            errorText = _Lang.getMessage("ERR_REQU_FAILED", null,
                    (xhr.status || UNKNOWN),
                    (xhr.statusText || UNKNOWN));

        } catch (e) {
            errorText = _Lang.getMessage("ERR_REQ_FAILED_UNKNOWN", null);
        } finally {
            var _Impl = this.attr("impl");
            _Impl.sendError(xhr, context, _Impl.HTTPERROR,
                    _Impl.HTTPERROR, errorText);
        }
        //_onError
    },

    onprogress: function(evt) {
        //do nothing for now
    },

    ontimeout: function(evt) {

        try {
            //we issue an event not an error here before killing the xhr process
            this._sendEvent("TIMEOUT_EVENT");
            //timeout done we process the next in the queue
        } finally {
            this._requestDone();
        }
    },

    _formDataToURI: function(formData) {
        if (formData && formData.makeFinal) {
            formData = formData.makeFinal()
        }
        return formData;
    },

    _getTransport: function() {
        var _Rt = myfaces._impl.core._Runtime;
        //same interface between 1 and 2, in the worst case
        //so we can make a drop in replacement, on level2
        //we have one layer less hence we can work directly
        //on the xhr object, on level2 we have our engine
        //emulation layer which thunks back into level1
        //for all calls

        var xhr = myfaces._impl.core._Runtime.getXHRObject();
        //the current xhr level2 timeout w3c spec is not implemented by the browsers yet
        //we have to do a fallback to our custom routines

        //Chrome fails in the current builds, on our loadend, we disable the xhr
        //level2 optimisations for now
        //if (('undefined' == typeof this._timeout || null == this._timeout) && _Rt.getXHRLvl() >= 2) {
        //no timeout we can skip the emulation layer
        //    return xhr;
        //}

        return new myfaces._impl.xhrCore.engine.Xhr1({xhrObject: xhr});
    },



    //----------------- backported from the base request --------------------------------
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
    getFormData : function() {
        var _AJAXUTIL = this._AJAXUTIL,
                _Lang = this._Lang,
                myfacesOptions = this._context.myfaces,
                ret = null,
                source = this._source,
                sourceForm = this._sourceForm;

        //now this is less performant but we have to call it to allow viewstate decoration
        if (!this._partialIdsArray || !this._partialIdsArray.length) {
            var viewState = jsf.getViewState(sourceForm);
            ret = _Lang.createFormDataDecorator(viewState);

            //just in case the source item is outside of the form
            //only if the form override is set we have to append the issuing item
            //otherwise it is an element of the parent form
            if (source && myfacesOptions && myfacesOptions.form)
                _AJAXUTIL.appendIssuingItem(source);
        } else {
            ret = _Lang.createFormDataDecorator(new Array());
            _AJAXUTIL.encodeSubmittableFields(ret, sourceForm, this._partialIdsArray);
            if (source && myfacesOptions && myfacesOptions.form)
                _AJAXUTIL.appendIssuingItem(source);
        }

        return ret;
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
        context._mfInternal.internalError = true;
        var xhrQueue = this._xhrQueue;
        try {
            this.attr("impl").stdErrorHandler(request, context, sourceClass, func, exception);
        } finally {
            if (xhrQueue) {
                xhrQueue.cleanup();
            }
        }
    },

    _sendEvent: function(evtType) {
        var _Impl = this.attr("impl");
        _Impl.sendEvent(this._xhr, this._context, _Impl[evtType]);
    },

    _requestDone: function() {
        var queue = this._xhrQueue;
        if (queue) {
            queue.processQueue();
        }
        //ie6 helper cleanup
        delete this._context.source;
        this._finalize();
    },

    //cleanup
    _finalize: function() {

        //final cleanup to terminate everything
        this._Lang.clearExceptionProcessed();

        if (this._xhr.readyState == this._XHR_CONST.READY_STATE_DONE) {
            this._callSuper("_finalize");
        }
    }
});

