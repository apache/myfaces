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
 * iframe request for communications over iframe
 *
 * This method can be used by older browsers and if you have
 * a multipart request which includes
 * a fileupload element, fileuploads cannot be handled by
 * normal xhr requests. The standard html 4+ compliant way to do this
 * is to use an iframe as submit target for a form.
 *
 * Note on almost all browsers this method induces a real asynchronity, the only
 * exception is firefox 3.6- which blocks the ui, this is resolved in Firefox 4
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
        var _RT = myfaces._impl.core._Runtime;

        this._frame = this._createTransportFrame();

        //we append an onload handler to the frame
        //to cover the starting and loading events,
        //timeouts cannot be covered in a cross browser way

        //we point our onload handler to the frame, we do not use addOnLoad
        //because the frame should not have other onload handlers in place
        if (!_RT.browser.isIE) {
            this._frame.onload = this._Lang.hitch(this, this.callback);
        } else {
            //ie has a bug, onload is not settable outside of innerHTML on iframes
            this._frame.onload_IE = this._Lang.hitch(this, this.callback);
        }

        if (!this._response) {
            this._response = new myfaces._impl.xhrCore._AjaxResponse(this._onException, this._onWarning);
        }
        //now to the parameter passing:
        _Impl.sendEvent(this._xhr, this._context, _Impl.BEGIN);

        //viewstate should be in our parent form which we will isse we however have to add the execute and
        //render parameters as well as the usual javax.faces.request params to our target

        var oldTarget = this._sourceForm.target;
        var oldMethod = this._sourceForm.method;
        var _progress = 0;
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
            //Derived from the YUI library, looking this up saved me some time
            request.responseText = this._getFrameText();
            request.responseXML = this._getFrameXml();
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
            //this closes any hanging or pedning comm channel caused by the iframe
            //this._frame.src = "about:blank";
            this._setFrameText("");
            this._frame = null;
        }
    },

    /**
     * returns the frame text in a browser independend manner
     */
    _getFrameText: function() {
        return this._frame.contentWindow.document.body ? this._frame.contentWindow.document.body.innerHTML : this._frame.contentWindow.document.documentElement.textContent;
    },

    /**
     * sets the frame text in a browser independend manner
     *
     * @param text to be set
     */
    _setFrameText: function(text) {
        this._frame.contentWindow.document.body ? (this._frame.contentWindow.document.body.innerHTML = text) : (this._frame.contentWindow.document.documentElement.textContent = text);
    },

    /**
     * returns the processed xml from the frame
     */
    _getFrameXml: function() {
        return this._frame.contentWindow.document.XMLDocument ? this._frame.contentWindow.document.XMLDocument : this._frame.contentWindow.document;
    },


    _initAjaxParams: function() {
        var _Impl = myfaces._impl.core.Impl;
        //this._appendHiddenValue(_Impl.P_AJAX, "");

        for (var key in this._passThrough) {
            this._appendHiddenValue(key, this._passThrough[key]);
        }
        //marker that this is an ajax iframe request
        this._appendHiddenValue("javax.faces.partial.iframe", "true");
        this._appendHiddenValue("org.apache.myfaces.partial.iframe", "true");

    },

    _removeAjaxParams: function(oldTarget) {
        var _Impl = myfaces._impl.core.Impl;
        this._sourceForm.target = oldTarget;
        for (var key in this._passThrough) {
            this._removeHiddenValue(key);
        }
        this._removeHiddenValue("javax.faces.partial.iframe");
        this._removeHiddenValue("org.apache.myfaces.partial.iframe");
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
        var _RT = myfaces._impl.core._Runtime;
        var frame = document.getElementById(this._FRAME_ID);
        //normally this code should not be called
        //but just to be sure
        if (!frame) {
            if (!_RT.browser.isIE) {
                frame = document.createElement('iframe');

                //probably the ie method would work on all browsers
                //but this code is the safe bet it works on all standards
                //compliant browsers in a clean manner
                var setAttr = this._Dom.setAttribute;
                setAttr(frame, "src", "about:blank");
                setAttr(frame, "id", this._FRAME_ID);
                setAttr(frame, "name", this._FRAME_ID);
                setAttr(frame, "type", "content");
                setAttr(frame, "collapsed", "true");
                setAttr(frame, "style", "display:none");

                document.body.appendChild(frame);
            } else { //Now to the non compliant browsers
                var node = document.createElement("div");
                this._Dom.setAttribute(node, "style", "display:none");
                //we are dealing with two well known iframe ie bugs here
                //first the iframe has to be set via innerHTML to be present
                //secondly the onload handler is immutable on ie, we have to
                //use a dummy onload handler in this case and call that one
                //from the onload handler
                node.innerHTML = "<iframe id='" + this._FRAME_ID + "' name='" + this._FRAME_ID + "' style='display:none;' src='about:blank' type='content' onload='this.onload_IE();'  ></iframe>";
                //avoid the ie open tag problem
                if (document.body.firstChild) {
                    document.body.insertBefore(node, document.body.firstChild);
                } else {
                    document.body.appendChild(node);
                }
            }
        }
        //helps to for the onload handlers and innerhtml to be in sync again
        return document.getElementById(this._FRAME_ID);

    }

    //TODO pps, the idea behind pps is to generate another form
    // and temporarily shift the elements over which have to be
    // ppsed, but it is up for discussion if we do pps at all in case of
    // an iframe, so I wont implement anything for now
});