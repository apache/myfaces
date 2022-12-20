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

_MF_CLS(_PFX_XHR + "_AjaxRequestQuirks", myfaces._impl.xhrCore._AjaxRequest, /** @lends myfaces._impl.xhrCore._AjaxRequest.prototype */ {
    constructor_: function (args) {
        this._callSuper("constructor_", args);
    },
    /**
     * Sends an Ajax request
     */
    send:function () {

        var _Lang = this._Lang;
        var _RT = this._RT;
        var _Dom = this._Dom;
        try {

            var scopeThis = _Lang.hitch(this, function (functionName) {
                return _Lang.hitch(this, this[functionName]);
            });
            this._xhr = _Lang.mixMaps(this._getTransport(), {
                onprogress:scopeThis("onprogress"),
                ontimeout:scopeThis("ontimeout"),
                //remove for xhr level2 support (chrome has problems with it)
                //for chrome we have to emulate the onloadend by calling it explicitely
                //and leave the onload out
                //onloadend:  scopeThis("ondone"),
                onload:scopeThis("onsuccess"),
                onerror:scopeThis("onerror")

            }, true);

            this._applyClientWindowId();
            var xhr = this._xhr,
                sourceForm = this._sourceForm,
                targetURL = (typeof sourceForm.elements[this.ENCODED_URL] == 'undefined') ?
                    sourceForm.action :
                    sourceForm.elements[this.ENCODED_URL].value,
                formData = this.getFormData();

            for (var key in this._passThrough) {
                if (!this._passThrough.hasOwnProperty(key)) continue;
                formData.append(key, this._passThrough[key]);
            }

            xhr.open(this._ajaxType, targetURL +
                ((this._ajaxType == "GET") ? "?" + this._formDataToURI(formData) : "")
                , true);

            xhr.timeout = this._timeout || 0;

            this._applyContentType(xhr);
            xhr.setRequestHeader(this._HEAD_FACES_REQ, this._VAL_AJAX);

            //some webkit based mobile browsers do not follow the w3c spec of
            // setting the accept headers automatically
            if (this._RT.browser.isWebKit) {
                xhr.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            }
            this._sendEvent("BEGIN");
            //Check if it is a custom form data object
            //if yes we use makefinal for the final handling
            if (formData && formData.makeFinal) {
                formData = formData.makeFinal()
            }
            xhr.send((this._ajaxType != "GET") ? formData : null);

        } catch (e) {
            //_onError//_onError
            e = (e._mfInternal) ? e : this._Lang.makeException(new Error(), "sendError", "sendError", this._nameSpace, "send", e.message);
            this._stdErrorHandler(this._xhr, this._context, e);
        } finally {
            //no finally possible since the iframe uses real asynchronousity
        }
    },

    _applyClientWindowId:function () {
        var _Impl = this.attr("impl");
        var clientWindow = this._Dom.getNamedElementFromForm(this._sourceForm, _Impl.P_CLIENTWINDOW);
        //pass through if exists already set by _Impl
        if ('undefined' != typeof this._context._mfInternal._clientWindow) {
            this._context._mfInternal._clientWindowOld = clientWindow.value;
            clientWindow.value = this._context._mfInternal._clientWindow;
        } else {
            if(clientWindow) {
                this._context._mfInternal._clientWindowDisabled = !! clientWindow.disabled;
                clientWindow.disabled = true;
            }
        }
    },

    _restoreClientWindowId:function () {
        //we have to reset the client window back to its original state
        var _Impl = this.attr("impl");
        var clientWindow = this._Dom.getNamedElementFromForm(this._sourceForm, _Impl.P_CLIENTWINDOW);
        if(!clientWindow) {
            return;
        }
        if ('undefined' != typeof this._context._mfInternal._clientWindowOld) {
            clientWindow.value =  this._context._mfInternal._clientWindow;
        }
        if('undefined' != typeof this._context._mfInternal._clientWindowDisabled) {
            //we reset it to the old value
            clientWindow.disabled = this._context._mfInternal._clientWindowDisabled;
        }
    },



    onsuccess:function (/*evt*/) {
        this._restoreClientWindowId();
        var context = this._context;
        var xhr = this._xhr;
        try {
            this._sendEvent("COMPLETE");
            //now we have to reroute into our official api
            //because users might want to decorate it, we will split it apart afterwards

            context._mfInternal = context._mfInternal || {};
            jsf.ajax.response((xhr.getXHRObject) ? xhr.getXHRObject() : xhr, context);

        } catch (e) {
            this._stdErrorHandler(this._xhr, this._context, e);

            //add for xhr level2 support
        } finally {
            //W3C spec onloadend must be called no matter if success or not
            this.ondone();
        }
    },

    onerror:function (/*evt*/) {
        this._restoreClientWindowId();
        //TODO improve the error code detection here regarding server errors etc...
        //and push it into our general error handling subframework
        var context = this._context;
        var xhr = this._xhr;
        var _Lang = this._Lang;

        var errorText = "";
        this._sendEvent("COMPLETE");
        try {
            var UNKNOWN = _Lang.getMessage("UNKNOWN");
            //status can be 0 and statusText can be ""
            var status = ('undefined' != xhr.status && null != xhr.status) ? xhr.status : UNKNOWN;
            var statusText = ('undefined' != xhr.statusText && null != xhr.statusText) ? xhr.statusText : UNKNOWN;
            errorText = _Lang.getMessage("ERR_REQU_FAILED", null, status, statusText);

        } catch (e) {
            errorText = _Lang.getMessage("ERR_REQ_FAILED_UNKNOWN", null);
        } finally {
            try {
                var _Impl = this.attr("impl");
                _Impl.sendError(xhr, context, _Impl.HTTPERROR,
                    _Impl.HTTPERROR, errorText, "", "myfaces._impl.xhrCore._AjaxRequest", "onerror");
            } finally {
                //add for xhr level2 support
                //since chrome does not call properly the onloadend we have to do it manually
                //to eliminate xhr level1 for the compile profile modern
                //W3C spec onloadend must be called no matter if success or not
                this.ondone();
            }
        }
        //_onError
    },


    ontimeout:function (/*evt*/) {
        try {
            this._restoreClientWindowId();
            //we issue an event not an error here before killing the xhr process
            this._sendEvent("TIMEOUT_EVENT");
            //timeout done we process the next in the queue
        } finally {
            this._requestDone();
        }
    },

    _formDataToURI:function (formData) {
        if (formData && formData.makeFinal) {
            formData = formData.makeFinal()
        }
        return formData;
    },

    _getTransport:function () {

        var xhr = this._RT.getXHRObject();
        //the current xhr level2 timeout w3c spec is not implemented by the browsers yet
        //we have to do a fallback to our custom routines

        //add for xhr level2 support
        //Chrome fails in the current builds, on our loadend, we disable the xhr
        //level2 optimisations for now
        if (/*('undefined' == typeof this._timeout || null == this._timeout) &&*/ this._RT.getXHRLvl() >= 2) {
            //no timeout we can skip the emulation layer
            return xhr;
        }
        return new myfaces._impl.xhrCore.engine.Xhr1({xhrObject:xhr});
    },

    //----------------- backported from the base request --------------------------------
    //non abstract ones
    /**
     * Spec. 13.3.1
     * Collect and encode input elements.
     * Additionally the hidden element jakarta/javax.faces.ViewState
     *
     *
     * @return  an element of formDataWrapper
     * which keeps the final Send Representation of the
     */
    getFormData:function () {
        var _AJAXUTIL = this._AJAXUTIL, myfacesOptions = this._context.myfaces;
        return this._Lang.createFormDataDecorator(jsf.getViewState(this._sourceForm));
    },

    /**
     * Client error handlers which also in the long run route into our error queue
     * but also are able to deliver more meaningful messages
     * note, in case of an error all subsequent xhr requests are dropped
     * to get a clean state on things
     *
     * @param request the xhr request object
     * @param context the context holding all values for further processing
     * @param exception the embedded exception
     */
    _stdErrorHandler:function (request, context, exception) {
        var xhrQueue = this._xhrQueue;
        try {
            this.attr("impl").stdErrorHandler(request, context, exception);
        } finally {
            if (xhrQueue) {
                xhrQueue.cleanup();
            }
        }
    },

    _sendEvent:function (evtType) {
        var _Impl = this.attr("impl");
        _Impl.sendEvent(this._xhr, this._context, _Impl[evtType]);
    },

    _requestDone:function () {
        var queue = this._xhrQueue;
        if (queue) {
            queue.processQueue();
        }
        //ie6 helper cleanup
        delete this._context.source;
        this._finalize();
    },

    //cleanup
    _finalize:function () {
        if (this._xhr.readyState == this._XHR_CONST.READY_STATE_DONE) {
            this._callSuper("_finalize");
        }
    }

});

(function() {
    /*some mobile browsers do not have a window object*/
    myfaces._impl.xhrCore._AjaxRequest = myfaces._impl.xhrCore._AjaxRequestQuirks;
})();