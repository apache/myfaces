/*
 * Copyright 2009 Ganesh Jung
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Ganesh Jung (latest modification by $Author: ganeshpuri $)
 * Version: $Revision: 1.4 $ $Date: 2009/05/31 09:16:44 $
 *
 */
/**
 * an implementation of a queued
 * asynchronoues request 
 */
myfaces._impl.core._Runtime.extendClass("myfaces._impl.xhrCore._AjaxRequest", Object, {
    _contentType: "application/x-www-form-urlencoded",
    _source: null,
    _xhr: null,
    _partialIdsArray: null,
    _queueSize: -1,
    _context:null,
    response: null,
    _ajaxUtil: null,
    _sourceForm: null,
    _passThrough: null,
    _requestParameters: null,
    _exception: null,
    _timeout: null,
    _delay:null,
    _partialIdsArray : null,
    _alarmThreshold : "ERROR",
    _xhrQueue: null,


    _PAR_ERRORLEVEL:"errorlevel",
    _PAR_QUEUESIZE:"queuesize",
    _PAR_PPS:"pps",
    _PAR_TIMEOUT:"timeout",
    _PAR_DELAY:"delay",

    _HEAD_POST: "POST",
    _HEAD_TYPE:"Content-Type",
    _HEAD_FACES_REQ:"Faces-Request",

    _VAL_AJAX: "partial/ajax",

    /**
     * Constructor
     * @param {Node} source - Item that triggered the request
     * @param {Node} sourceForm (form) - Form containing source
     * @param {Object} context (Map) - AJAX context
     * @param {Object} passThrough (Map) - parameters to pass through to the server (execute/render)
     */
    constructor_: function(source, sourceForm, context, passThrough, queue) {
        this._exception = new myfaces._impl.xhrCore._Exception("myfaces._impl.xhrCore._AjaxRequest", this._alarmThreshold);
        try {

            /*namespace remapping for readability*/
            var _Runtime = myfaces._impl.core._Runtime;
            var _Lang = myfaces._impl._util._Lang;
            var _getConfig = _Runtime.getLocalOrGlobalConfig;

            _Lang.applyArguments(this, arguments, ["source", "sourceForm", "context", "passThrough", "xhrQueue"]);

            //this._source = source;
            // myfaces parameters
            //this._partialIdsArray = null;
            //this._queueSize = -1;

            this._applyConfig("_alarmThreshold", this._PAR_ERRORLEVEL);
            this._applyConfig("_queueSize", this._PAR_QUEUESIZE);
            this._applyConfig("_timeout", this._PAR_TIMEOUT);
            this._applyConfig("_delay", this._PAR_DELAY);

            if (_getConfig(context, this._PAR_PPS, null) != null
                    && _Lang.exists(passThrough, myfaces._impl.core.Impl._PROP_EXECUTE)
                    && passThrough[myfaces._impl.core.Impl._PROP_EXECUTE].length > 0) {
                this._partialIdsArray = passThrough[myfaces._impl.core.Impl._PROP_EXECUTE].split(" ");
            }

            this.response = new myfaces._impl.xhrCore._AjaxResponse(this._alarmThreshold);
            this._ajaxUtil = new myfaces._impl.xhrCore._AjaxUtils(this._alarmThreshold);

            this._requestParameters = this.getViewState();

            for (var key in this._passThrough) {
                this._requestParameters = this._requestParameters +
                        "&" + encodeURIComponent(key) +
                        "=" + encodeURIComponent(this._passThrough[key]);
            }

        } catch (e) {
            //_onError
            this._exception.throwError(null, context, "Ctor", e);
        }
    },

    _applyConfig: function(destParm, srcParm) {
        var _Runtime = myfaces._impl.core._Runtime;
        var _getConfig = _Runtime.getLocalOrGlobalConfig;
        if (_getConfig(this._context, srcParm, null) != null) {
            this[destParm] = _getConfig(this._context, srcParm, null);
        }
    },

    /**
     * Sends an Ajax request
     */
    send : function() {
        try {

            this._xhr = myfaces._impl.core._Runtime.getXHRObject();

            this._xhr.open(this._HEAD_POST, this._sourceForm.action, true);
            this._xhr.setRequestHeader(this._HEAD_TYPE, this._contentType);
            this._xhr.setRequestHeader(this._HEAD_FACES_REQ, this._VAL_AJAX);

            this._xhr.onreadystatechange = this._xhrQueue.handleCallback;
            var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
            _Impl.sendEvent(this._xhr, this._context, myfaces._impl.core.Impl._AJAX_STAGE_BEGIN);
            this._xhr.send(this._requestParameters);
            if ('undefined' != typeof this._timeout) {
                var timeoutId = window.setTimeout(
                        function() {
                            try {
                                if (this._xhrQueue._curReq._xhr.readyState > 0
                                        && this._xhrQueue._curReq._xhr.readyState < 4) {
                                    this._xhrQueue._curReq._xhr.abort();
                                }
                            } catch (e) {
                                // don't care about exceptions here
                            }
                        }, this._timeout);
            }
        } catch (e) {
            //_onError//_onError
            this._exception.throwError(this._xhr, this._context, "send", e);
        }
    },

    /**
     * Callback method to process the Ajax response
     * triggered by RequestQueue
     */
    requestCallback : function() {
        var READY_STATE_DONE = 4;
        try {
            //local namespace remapping
            var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);

            if (this._xhr.readyState == READY_STATE_DONE) {
                //_onDone
                _Impl.sendEvent(this._xhr, this._context, myfaces._impl.core.Impl._AJAX_STAGE_COMPLETE);

                if (this._xhr.status >= 200 && this._xhr.status < 300) {
                     //_onSuccess
                    _Impl.response(this._xhr, this._context);
                    _Impl.sendEvent(this._xhr, this._context, myfaces._impl.core.Impl._AJAX_STAGE_SUCCESS);
                    this._xhrQueue.processQueue();
                } else {
                    //_onError
                    var errorText;
                    try {
                        errorText = "Request failed";
                        if (this._xhr.status) {
                            errorText += "with status " + this._xhr.status;
                            if (this._xhr.statusText) {
                                errorText += " and reason " + this._xhr.statusText;
                            }
                        }
                    } catch (e) {
                        errorText = "Request failed with unknown status";
                    }
                    //_onError
                    _Impl.sendError(this._xhr, this._context, myfaces._impl.core.Impl._ERROR_HTTPERROR,
                            myfaces._impl.core.Impl._ERROR_HTTPERROR, errorText);
                }
            }
        } catch (e) {
            //_onError
            this._exception.throwError(this._xhr, this._context, "requestCallback", e);
        }
    },

    /**
     * Spec. 13.3.1
     * Collect and encode input elements.
     * Additionally the hidden element javax.faces.ViewState
     * @return {String} - Concatenated String of the encoded input elements
     *             and javax.faces.ViewState element
     */
    getViewState : function() {
        return this._ajaxUtil.encodeSubmittableFields(this._xhr, this._context, this._source,
                this._sourceForm, this._partialIdsArray);
    }

});

