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
 * Iframe request for communications over Iframes
 *
 * This method can be used by older browsers and if you have
 * a multipart request which includes
 * a fileupload element, fileuploads cannot be handled by
 */
myfaces._impl.core._Runtime.extendClass("myfaces._impl.xhrCore._IFrameRequest", myfaces._impl.xhrCore._BaseRequest, {

    _FRAME_ID: "myfaces-communications-frame",
    _frame: null,

    /**
     * constructor which shifts the arguments
     * to the protected properties of this clas
     *
     * @param arguments
     */
    constructor_: function(arguments) {
        try {
            //we fetch in the standard arguments
            this._Lang.applyArgs(this, arguments);
        } catch (e) {
            //_onError
            this._onException(null, this._context, "myfaces._impl.xhrCore._IFrameRequest", "constructor", e);
        }
    },

    /**
     * send method, central callback which sends the
     * request
     */
    send: function() {
        var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
        this._frame = this._createTransportFrame();
        //we append an onload handler to the frame
        //to cover the starting and loading events,
        //timeouts cannot be covered in a cross browser way

        //we point our onload handler to the frame
        this._Lang.addOnLoad(this._frame, this._Lang.hitch(this, this.callback));

        if (!this._response) {
            this._response = new myfaces._impl.xhrCore._AjaxResponse(this._onException, this._onWarning);
        }
        //now to the parameter passing:
        _Impl.sendEvent(this._xhr, _Impl.BEGIN);

        //viewstate should be in our parent form which we will isse we however have to add the execute and
        //render parameters as well as the usual javax.faces.request params to our target

        var oldTarget = this._sourceForm.target;
        var oldMethod = this._sourceForm.method;
        try {
            this._initAjaxParams();
            this._sourceForm.target = this._frame.name;
            this._sourceForm.method = this._ajaxType;
            this._sourceForm.submit();
        } finally {
            this._removeAjaxParams(oldTarget);
            this._sourceForm.target = oldTarget;
            this._sourceForm.method = oldMethod;
        }
    },

    /**
     * the callback function after the request is done
     */
    callback: function() {
        //now we have to do the processing, for that we have to parse the result, if it is a http 404 then
        //nothing could be delivered and we bomb out with an error anything else has to be parsed
        //via our xml parser

        var request = {};
        try {
            request.responseText = this._frame.contentWindow.document.body ? this._frame.contentWindow.document.body.innerHTML : this._frame.contentWindow.document.documentElement.textContent;
            request.responseXML = this._frame.contentWindow.document.XMLDocument ? this._frame.contentWindow.document.XMLDocument : this._frame.contentWindow.document;

            request.readyState = this._READY_STATE_DONE;
            this._onDone(request, this._context);

            if (!this._Lang.isXMLParseError(request.responseXML)) {
                request.status = 201;
                this._onSuccess(request, this._context);
            } else {
                request.status = 0;
                this._onError(request, this._context);
            }
        } catch (e) {
            //_onError
            this._onException(null, this._context, "myfaces._impl.xhrCore._IFrameRequest", "constructor", e);
        } finally {
            this._frame.parentNode.removeChild(this._frame);
            delete this._frame;
        }
    },

    _initAjaxParams: function() {
        var _Impl = myfaces._impl.core.Impl;
        //this._appendHiddenValue(_Impl.P_AJAX, "");

        for (var key in this._passThrough) {
            this._appendHiddenValue(key, this._passThrough[key]);
        }
    },

    _removeAjaxParams: function(oldTarget) {
        var _Impl = myfaces._impl.core.Impl;
        this._sourceForm.target = oldTarget;
        for (var key in this._passThrough) {
            this._removeHiddenValue(key);
        }
    },

    _appendHiddenValue: function(key, value) {
        if ('undefined' == typeof value) {
            return;
        }
        var input = document.createElement("input");
        this._Dom.setAttribute(input, "name", key);
        this._Dom.setAttribute(input, "style", "display:none");
        this._Dom.setAttribute(input, "value", value);
        this._sourceForm.appendChild(input);
    },

    _removeHiddenValue: function(key) {
        var elem = this._Dom.findByName(this._sourceForm, key, true);
        if (elem.length) {
            elem[0].parentNode.removeChild(elem[0]);
            delete elem[0];
        }
    },

    _createTransportFrame: function() {
        var frame = document.getElementById(this._FRAME_ID);
        //normally this code should not be called
        //but just to be sure
        if (frame) {
            frame.parentNode.removeChild(frame);
            delete frame;
        }

        frame = document.createElement('iframe');

        this._Dom.setAttribute(frame, "src", "about:blank");
        this._Dom.setAttribute(frame, "id", this._FRAME_ID);
        this._Dom.setAttribute(frame, "name", this._FRAME_ID);
        this._Dom.setAttribute(frame, "type", "content");
        this._Dom.setAttribute(frame, "collapsed", "true");
        this._Dom.setAttribute(frame, "style", "display:none");

        //security, we turn everything off
        //we wont need it
        if (frame.webNavigation) {
            frame.webNavigation.allowAuth = false;
            frame.webNavigation.allowImages = false;
            frame.webNavigation.allowJavascript = false;
            frame.webNavigation.allowMetaRedirects = false;
            frame.webNavigation.allowPlugins = false;
            frame.webNavigation.allowSubframes = false;
        }
        document.body.appendChild(frame);
        return frame;
    }

    //TODO pps, the idea behind pps is to generate another form
    // and temporarily shift the elements over which have to be
    // ppsed, but it is up for discussion if we do pps at all in case of
    // an iframe, so I wont implement anything for now
});