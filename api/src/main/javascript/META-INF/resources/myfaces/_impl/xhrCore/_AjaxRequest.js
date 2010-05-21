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
 *
 */

/**
 * An implementation of an xhr request object
 * with partial page submit functionality, and jsf
 * ppr request and timeout handling capabilities
 *
 * TODO there is still some jsf related logic in here
 * which has to be moved one level up in the call chain,
 * to get a clear separation of concerns
 *
 * Author: Ganesh Jung (latest modification by $Author: ganeshpuri $)
 * Version: $Revision: 1.4 $ $Date: 2009/05/31 09:16:44 $
 */
/** @namespace myfaces._impl.xhrCore._AjaxRequest */
myfaces._impl.core._Runtime.extendClass("myfaces._impl.xhrCore._AjaxRequest", Object, {

    /*all instance vars can be set from the outside
     * via a parameter map*/
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


    _partialIdsArray : null,
    _ajaxType: "POST",


    //callbacks for onDone... done issues
    //onSuccess everything has passed through
    //onError server side error

    //onException exception thrown by the client
    //onWarning warning issued by the client
    _onDone : null,
    _onSuccess: null,
    _onError: null,
    _onException: null,
    _onWarning: null,

    /*response object which is exposed to the queue*/
    _response: null,


    /*
     * constants used internally 
     */
    _CONTENT_TYPE:"Content-Type",
    _HEAD_FACES_REQ:"Faces-Request",
    _VAL_AJAX: "partial/ajax",

    /**
     * Constructor
     * @arguments  an arguments map which an override any of the given protected
     * instance variables, by a simple name value pair combination
     *
     */
    constructor_: function(arguments) {
        try {

            /*namespace remapping for readability*/
            var _Lang = myfaces._impl._util._Lang;
            //we fetch in the standard arguments
            _Lang.applyArgs(this, arguments);

            //if our response handler is not set
            if (!this._response) {
                this._response = new myfaces._impl.xhrCore._AjaxResponse(this._onException, this._onWarning);
            }
            this._ajaxUtil = new myfaces._impl.xhrCore._AjaxUtils(this._onException, this._onWarning);

            this._requestParameters = this.getViewState();

            for (var key in this._passThrough) {
                this._requestParameters = this._requestParameters +
                        "&" + encodeURIComponent(key) +
                        "=" + encodeURIComponent(this._passThrough[key]);
            }
        } catch (e) {
            //_onError
            this._onException(null, this._context, "myfaces._impl.xhrCore._AjaxRequest", "constructor", e);
        }
    },

    /**
     * Sends an Ajax request
     */
    send : function() {
        var _Lang = myfaces._impl._util._Lang;
        try {

            this._xhr = myfaces._impl.core._Runtime.getXHRObject();

            this._xhr.open(this._ajaxType, this._sourceForm.action, true);

            var contentType = this._contentType;
            if (this._encoding) {
                contentType = contentType + "; charset:" + this._encoding;
            }

            this._xhr.setRequestHeader(this._CONTENT_TYPE, this._contentType);
            this._xhr.setRequestHeader(this._HEAD_FACES_REQ, this._VAL_AJAX);

            this._xhr.onreadystatechange = _Lang.hitch(this, this.callback);
            var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
            _Impl.sendEvent(this._xhr, this._context, myfaces._impl.core.Impl.BEGIN);
            this._xhr.send(this._requestParameters);
            if (this._timeout && this._onTimeout) {
                var timeoutId = window.setTimeout(this._onTimeout, this._timeout);
            }
        } catch (e) {
            //_onError//_onError
            this._onException(this._xhr, this._context, "myfaces._impl.xhrCore._AjaxRequest", "send", e);
        }
    },

    abort: function() {
        try {
            if (this._xhr.readyState > 0
                    && this._xhr.readyState < 4) {
                this._xhr.abort();
            }
        } catch (e) {
        }
    },


    /**
     * Callback method to process the Ajax response
     * triggered by RequestQueue
     */
    callback : function() {
        var READY_STATE_DONE = 4;
        try {
            var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);

            if (this._xhr.readyState == READY_STATE_DONE) {
                this._onDone(this._xhr, this._context);
                if (this._xhr.status >= 200 && this._xhr.status < 300) {
                    this._onSuccess(this._xhr, this._context);
                } else {
                    this._onError(this._xhr, this._context);
                }
            }
        } catch (e) {
            this._onException(this._xhr, this._context, "myfaces._impl.xhrCore._AjaxRequest", "callback", e);
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

