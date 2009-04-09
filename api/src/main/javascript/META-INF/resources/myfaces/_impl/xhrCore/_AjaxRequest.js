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
 * Version: $Revision: 1.2 $ $Date: 2009/04/09 13:02:00 $
 *
 */

_reserveMyfacesNamespaces();

/**
 * Constructor
 * @param {HtmlElement} sourceItem - Item that triggered the request
 * @param {String} actionIds (JSF 2.0) - Client IDs of the components to trigger during execution phase
 * @param {String} reRenderIds - Client IDs of the components to rerender
 * @param {String} alarmThreshold - errorlevel
 * @param {String} triggerElement - designation for AjaxRequest
 * @param {String} partialIds - ClientIDs to submit in PartialSubmit (PPS)
 * @param {Function} callbackFunction (JSF 2.0) - callback Function with XmlHttpRequest parameter
 */
myfaces._impl.xhrCore_AjaxRequest = function(sourceItem, actionIds, reRenderIds,
		alarmThreshold, triggerElement, partialIds) {
	this.m_exception = new myfaces._impl.xhrCore_Exception("myfaces._impl.xhrCore_AjaxRequest", this.alarmThreshold);
	try {
		this.m_actionIds = actionIds; 
		this.m_reRenderIds = reRenderIds;
		this.m_partialIds = partialIds;
		this.m_contentType = "application/x-www-form-urlencoded";
		this.m_sourceItem = sourceItem;
		this.m_request = null;
		this.m_response = new myfaces._impl.xhrCore_AjaxResponse(this.alarmThreshold);
		this.m_ajaxUtil = new myfaces._impl.xhrCore_AjaxUtils(this.alarmThreshold);
		this.m_requestQueue = null;
		this.m_sourceItemParentForm = null;
		this.m_triggerElement = triggerElement;
	} catch (e) {
		this.m_exception.throwError("Ctor", e);
	}
};

/**
 * Sends an Ajax request
 * @param {RequestQueue} requestQueue - Queue object to trigger next request
 */
myfaces._impl.xhrCore_AjaxRequest.prototype.send = function(requestQueue) {
	try {
		this.m_requestQueue = requestQueue;

		// FORM-Element holen
		this.m_sourceItemParentForm = myfaces._impl._util._Utils.getParent(this.m_sourceItem, "form");

		// partialIds behandeln (Form: idEins, idZwei, ...)
		var partialIdsArray = null;
		var partialFlag = "";
		if (this.m_partialIds != null && this.m_partialIds.length > 0) {
			partialIdsArray = this.m_partialIds.split(",");
			partialFlag = "partial_submit=true&";
		}

		// Daten aus aktueller Form holen und Felder gleichzeitig disablen
		requestParameters = this.m_ajaxUtil.processUserEntries(this.m_sourceItem,
				this.m_sourceItemParentForm, partialIdsArray);

		requestAction = this.m_sourceItemParentForm.action;
		if (myfaces._impl._util._Utils.isUserAgentInternetExplorer()) {
			// request-Objekt for Internet Explorer
			try {
				this.m_request = new ActiveXObject("Msxml2.XMLHTTP");
			} catch (e) {
				this.m_request = new ActiveXObject('Microsoft.XMLHTTP');
			}
		} else {
			// request-Objekt for standard browser
			this.m_request = new XMLHttpRequest();
		}
		this.m_request.open("POST", requestAction, true);
		this.m_request.setRequestHeader("Content-Type", this.m_contentType);
		this.m_request.setRequestHeader("Faces-Request", "partial/ajax");
		this.m_request.onreadystatechange = myfaces._impl.xhrCore_AjaxRequestQueue.handleCallback;
		this.m_request.send(requestParameters + this.m_triggerElement + "&"
				+ partialFlag + this.m_actionIds + "&" + this.m_reRenderIds
                + "&javax.faces.partial.ajax=true");
	} catch (e) {
		this.m_exception.throwError("send", e);
	}
};

/**
 * Callback method to process the Ajax response
 * triggered by RequestQueue
 */
myfaces._impl.xhrCore_AjaxRequest.prototype.requestCallback = function() {
	try {
		if (this.isComplete() == true) {
			this.m_response.processResponse(this.m_request, this.m_sourceItemParentForm);
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
 * Enable HTML elements
 */
myfaces._impl.xhrCore_AjaxRequest.prototype.enableElements = function() {
	if (this.m_disableFlag == true) {
		// alle Elemente aus dem Cache wieder enablen
		this.m_ajaxUtil.enableElementsFromCache(this.m_disableTypesArr);
	}
};

/**
 * @return {String} status text of request
 */
myfaces._impl.xhrCore_AjaxRequest.prototype.getHtmlStatusText = function() {
	return this.m_request.statusText;
};

/**
 * @return {boolean} true if ajax request successfull
 */
myfaces._impl.xhrCore_AjaxRequest.prototype.isComplete = function() {
	return (this.m_request.readyState == 4 && this.m_request.status == 200);
};

/**
 * @return {boolean} true if ajax request failed
 */
myfaces._impl.xhrCore_AjaxRequest.prototype.isFailed = function() {
	return (this.m_request.readyState == 4 && this.m_request.status != 200);
};

/**
 * @return {boolean} true while ajax request is running
 */
myfaces._impl.xhrCore_AjaxRequest.prototype.isPending = function() {
	return (this.m_request.readyState == 1);
};

myfaces._impl.xhrCore_AjaxRequest.prototype.getSourceItem = function() {
	return this.m_sourceItem;
};
myfaces._impl.xhrCore_AjaxRequest.prototype.setSourceItem = function(sourceItem) {
	this.m_sourceItem = sourceItem;
};

myfaces._impl.xhrCore_AjaxRequest.prototype.getContentType = function() {
	return this.m_contentType;
};
myfaces._impl.xhrCore_AjaxRequest.prototype.setContentType = function(contentType) {
	this.m_contentType = contentType;
};