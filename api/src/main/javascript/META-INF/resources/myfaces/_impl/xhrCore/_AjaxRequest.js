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
 * Author: Ganesh Jung (latest modification by $Author: werpu $)
 * Version: $Revision: 1.10 $ $Date: 2009/04/23 11:03:09 $
 *
 */

_reserveMyfacesNamespaces();

if (!myfaces._impl._util._LangUtils.exists(myfaces._impl.xhrCore, "_AjaxRequest")) {

    /**
     * Constructor
     * @param {HtmlElement} source - Item that triggered the request
     * @param {Html Form} sourceForm - Form containing source
     * @param {Map} context - AJAX context
     * @param {Map} passThough - parameters to pass through to the server (execute/render)
     */
    myfaces._impl.xhrCore._AjaxRequest = function(source, sourceForm, context, passThrough) {
        this.m_exception = new myfaces._impl.xhrCore._Exception("myfaces._impl.xhrCore._AjaxRequest", this.alarmThreshold);
        try {
            this.m_contentType = "application/x-www-form-urlencoded";
            this.m_source = source;
            this.m_xhr = null;
            // myfaces parameters
            this.m_partialIdsArray = null;
            var errorlevel = 'NONE';
            this.m_queuesize = -1;

            /*namespace remapping for readability*/
            var _Utils = myfaces._impl._util._Utils;
            var _LangUtils = myfaces._impl._util._LangUtils;

            if (_Utils.getLocalOrGlobalConfig(context,"errorlevel", null) != null) {
                errorlevel = context.myfaces.errorlevel;
            }
            if (_Utils.getLocalOrGlobalConfig(context,"queuesize", null) != null) {
                this.m_queuesize = context.myfaces.queuesize;
            }
            if (_Utils.getLocalOrGlobalConfig(context,"pps", null) != null
                &&  _LangUtils.exists(passThrough,myfaces._impl.core._jsfImpl._PROP_EXECUTE)
                && passThrough[myfaces._impl.core._jsfImpl._PROP_EXECUTE].length > 0) {
                this.m_partialIdsArray = passThrough[myfaces._impl.core._jsfImpl._PROP_EXECUTE].split(" ");
            }
        
            this.m_context = context;
            this.m_response = new myfaces._impl.xhrCore._AjaxResponse(errorlevel);
            this.m_ajaxUtil = new myfaces._impl.xhrCore._AjaxUtils(errorlevel);
            this.m_sourceForm = sourceForm;
            this.m_passThrough = passThrough;
            this.m_requestParameters = this.getViewState();
            for (var key in this.m_passThrough) {
                this.m_requestParameters = this.m_requestParameters +
                "&" + encodeURIComponent(key) +
                "=" + encodeURIComponent(this.m_passThrough[key]);
            }
        } catch (e) {
            this.m_exception.throwError(null, context, "Ctor", e);
        }
    };

    /**
     * Sends an Ajax request
     * @param {RequestQueue} requestQueue - Queue object to trigger next request
     */

    myfaces._impl.xhrCore._AjaxRequest.prototype.send = function() {
        try {
            if (myfaces._impl._util._Utils.isUserAgentInternetExplorer()) {
                // request object for Internet Explorer
                try {
                    this.m_xhr = new ActiveXObject("Msxml2.XMLHTTP");
                } catch (e) {
                    this.m_xhr = new ActiveXObject('Microsoft.XMLHTTP');
                }
            } else {
                // request object for standard browser
                this.m_xhr = new XMLHttpRequest();
            }
            this.m_xhr.open("POST", this.m_sourceForm.action, true);
            this.m_xhr.setRequestHeader("Content-Type", this.m_contentType);
            this.m_xhr.setRequestHeader("Faces-Request", "partial/ajax");
            this.m_xhr.onreadystatechange = myfaces._impl.xhrCore._AjaxRequestQueue.handleCallback;
            jsf.ajax.sendEvent(this.m_xhr, this.m_context, myfaces._impl.core._jsfImpl._AJAX_STAGE_BEGIN);
            this.m_xhr.send(this.m_requestParameters);
        } catch (e) {
            this.m_exception.throwError(this.m_xhr, this.m_context, "send", e);
        }
    };

    /**
     * Callback method to process the Ajax response
     * triggered by RequestQueue
     */
    myfaces._impl.xhrCore._AjaxRequest.prototype.requestCallback = function() {
        var READY_STATE_DONE = 4;
        try {

            if (this.m_xhr.readyState == READY_STATE_DONE) {
                if (this.m_xhr.status >= 200 || this.m_xhr.status < 300) {
                    jsf.ajax.sendEvent(this.m_xhr, this.m_context, myfaces._impl.core._jsfImpl._AJAX_STAGE_COMPLETE);
                    jsf.ajax.response(this.m_xhr, this.m_context);
                    jsf.ajax.sendEvent(this.m_xhr, this.m_context, myfaces._impl.core._jsfImpl._AJAX_STAGE_SUCCESS);
                    myfaces._impl.xhrCore._AjaxRequestQueue.queue.processQueue();
                } else {
                    jsf.ajax.sendEvent(this.m_xhr, this.m_context, myfaces._impl.core._jsfImpl._AJAX_STAGE_COMPLETE);
                    jsf.ajax.sendError(this.m_xhr, this.m_context, myfaces._impl.core._jsfImpl._ERROR_HTTPERROR,
                        myfaces._impl.core._jsfImpl._ERROR_HTTPERROR, "Request failed with status " + this.m_xhr.status
                        + " and reason " + this.getHtmlStatusText());
                }
            }
        } catch (e) {
            this.m_exception.throwError(this.m_xhr, this.m_context, "requestCallback", e);
        }
    }

    /**
     * Spec. 13.3.1
     * Collect and encode input elements.
     * Additionally the hidden element javax.faces.ViewState
     * @return {String} - Concatenated String of the encoded input elements
     * 			and javax.faces.ViewState element
     */
    myfaces._impl.xhrCore._AjaxRequest.prototype.getViewState = function() {
        return this.m_ajaxUtil.processUserEntries(this.m_xhr, this.m_context, this.m_source,
            this.m_sourceForm, this.m_partialIdsArray);
    }


}
