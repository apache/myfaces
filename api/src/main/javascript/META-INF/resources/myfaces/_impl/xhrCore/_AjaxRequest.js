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
 * Version: $Revision: 1.5 $ $Date: 2009/04/13 19:50:24 $
 *
 */

_reserveMyfacesNamespaces();

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
		this.m_request = null;
        // myfaces parameters
        this.m_partialIdsArray = null;
        var errorlevel = 'NONE';
        if (typeof context != 'undefined'
            && context != null
            && typeof context.myfaces != 'undefined'
            && context.myfaces != null) {
            if (typeof context.myfaces.errorlevel != 'undefined'
                && context.myfaces.errorlevel != null)
                errorlevel = context.myfaces.errorlevel;
            if (typeof context.myfaces.pps != 'undefined'
                && context.myfaces.pps
                && typeof passThrough.execute != 'undefined'
                && passThrough.execute != null
                && passThrough.execute.length > 0) {
                this.m_partialIdsArray = passThrough.execute.split(" ");
            }
        }
        this.m_context = context;
		this.m_response = new myfaces._impl.xhrCore._AjaxResponse(errorlevel);
		this.m_ajaxUtil = new myfaces._impl.xhrCore._AjaxUtils(errorlevel);
		this.m_requestQueue = null;
		this.m_sourceForm = sourceForm;
        this.m_passThrough = passThrough;
	} catch (e) {
		this.m_exception.throwError("Ctor", e);
	}
};

/**
 * Prepares an Ajax request with the request parameters
 */
myfaces._impl.xhrCore._AjaxRequest.prototype.prepare = function() {
    // read data from form
    this.m_requestParameters = jsf.getViewState(this.m_sourceForm);
    for (var key in this.m_passThrough) {
        this.m_requestParameters = this.m_requestParameters +
            "&" + encodeURIComponent(key) +
            "=" + encodeURIComponent(this.m_passThrough[key]);
    }
    return this;
}
/**
 * Sends an Ajax request
 * @param {RequestQueue} requestQueue - Queue object to trigger next request
 */

myfaces._impl.xhrCore._AjaxRequest.prototype.send = function(requestQueue) {
	try {
		this.m_requestQueue = requestQueue;

		if (myfaces._impl._util._Utils.isUserAgentInternetExplorer()) {
			// request object for Internet Explorer
			try {
				this.m_request = new ActiveXObject("Msxml2.XMLHTTP");
			} catch (e) {
				this.m_request = new ActiveXObject('Microsoft.XMLHTTP');
			}
		} else {
			// request object for standard browser
			this.m_request = new XMLHttpRequest();
		}
		this.m_request.open("POST", this.m_sourceForm.action, true);
		this.m_request.setRequestHeader("Content-Type", this.m_contentType);
		this.m_request.setRequestHeader("Faces-Request", "partial/ajax");
		this.m_request.onreadystatechange = myfaces._impl.xhrCore._AjaxRequestQueue.handleCallback;
		this.m_request.send(this.m_requestParameters);
	} catch (e) {
		this.m_exception.throwError("send", e);
	}
};

/**
 * Callback method to process the Ajax response
 * triggered by RequestQueue
 */
myfaces._impl.xhrCore._AjaxRequest.prototype.requestCallback = function() {
	try {
		if (this.isComplete() == true) {
			this.m_response.processResponse(this.m_request, this.m_sourceForm, this.m_context);
			this.m_requestQueue.processQueue();
		} else if (this.isPending() == false && this.isFailed() == true) {
			this.m_exception.throwWarning("requestCallback",
					"Request failed.\nReason " + this.getHtmlStatusText());
		}
	} catch (e) {
		this.m_exception.throwError("requestCallback", e);
	}
};
/**
 * Spec. 13.3.1
 * Collect and encode input elements.
 * Additionally the hidden element javax.faces.ViewState
 * @param {String} FORM_ELEMENT - Client-Id of Form-Element
 * @return {String} - Concatenated String of the encoded input elements
 * 			and javax.faces.ViewState element
 */
myfaces._impl.xhrCore._AjaxRequest.prototype.getViewState = function(FORM_ELEMENT) {
    return this.m_ajaxUtil.processUserEntries(this.m_source, FORM_ELEMENT, this.m_partialIdsArray);
}

/**
 * @return {String} status text of request
 */
myfaces._impl.xhrCore._AjaxRequest.prototype.getHtmlStatusText = function() {
	return this.m_request.statusText;
};

/**
 * @return {boolean} true if ajax request successfull
 */
myfaces._impl.xhrCore._AjaxRequest.prototype.isComplete = function() {
	return (this.m_request.readyState == 4 && this.m_request.status == 200);
};

/**
 * @return {boolean} true if ajax request failed
 */
myfaces._impl.xhrCore._AjaxRequest.prototype.isFailed = function() {
	return (this.m_request.readyState == 4 && this.m_request.status != 200);
};

/**
 * @return {boolean} true while ajax request is running
 */
myfaces._impl.xhrCore._AjaxRequest.prototype.isPending = function() {
	return (this.m_request.readyState == 1);
};

myfaces._impl.xhrCore._AjaxRequest.prototype.getSourceItem = function() {
	return this.m_sourceItem;
};
myfaces._impl.xhrCore._AjaxRequest.prototype.setSourceItem = function(sourceItem) {
	this.m_sourceItem = sourceItem;
};

myfaces._impl.xhrCore._AjaxRequest.prototype.getContentType = function() {
	return this.m_contentType;
};
myfaces._impl.xhrCore._AjaxRequest.prototype.setContentType = function(contentType) {
	this.m_contentType = contentType;
};