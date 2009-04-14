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
 * Version: $Revision: 1.4 $ $Date: 2009/04/12 05:41:47 $
 *
 */

_reserveMyfacesNamespaces();


/**
 * Constructor
 */
myfaces._impl.xhrCore._AjaxRequestQueue = function() {
	this.m_request = null;
	this.m_queuedRequests = new Object();
	this.m_requestPending = false;
	this.m_exception = new myfaces._impl.xhrCore._Exception("myfaces._impl.xhrCore._AjaxRequestQueue", "NONE");
};

/**
 * [STATIC PROPERTIES]
 */
myfaces._impl.xhrCore._AjaxRequestQueue.queue = new myfaces._impl.xhrCore._AjaxRequestQueue();

/**
 * [STATIC]
 * Triggers callback methode of class Request as callback referencing
 * of an object is not possible
 */
myfaces._impl.xhrCore._AjaxRequestQueue.handleCallback = function() {
	if (myfaces._impl.xhrCore._AjaxRequestQueue.queue.m_request != null) {
        jsf.ajax.response(myfaces._impl.xhrCore._AjaxRequestQueue.queue.m_request.m_request, "dummy");
	} else {
		myfaces._impl.xhrCore._AjaxRequestQueue.queue.m_exception.throwWarning
			("doRequestCallback", "No request object available");
	}
};

/**
 * send a reuest or keeps it in a queue
 * @param {myfaces._impl.xhrCore._AjaxRequest} request - request to send
 */
myfaces._impl.xhrCore._AjaxRequestQueue.prototype.queueRequest = function(request) {
	if (this.m_requestPending == false) {
		this.m_requestPending = true;
		this.m_request = request;
		this.doRequest();
	} else {
		this.m_queuedRequest = request;
	}
};

/**
 * process queue, send request, if exists
 */
myfaces._impl.xhrCore._AjaxRequestQueue.prototype.processQueue = function() {
	if (this.m_queuedRequest != null) {
		this.m_request = this.m_queuedRequest;
		this.m_queuedRequest = null;
		this.doRequest();
	} else {
		this.m_requestPending = false;
	}
};

/**
 * send ajax request
 */
myfaces._impl.xhrCore._AjaxRequestQueue.prototype.doRequest = function() {
	if (this.m_request != null) {
		this.m_request.send(this);
	} else {
		this.m_exception.throwWarning("doRequest",
				"No request object available");
	}
};

/**
 * cleanup queue
 */
myfaces._impl.xhrCore._AjaxRequestQueue.prototype.clearQueue = function() {
	this.m_request = null;
	this.m_queuedRequest = null;
	this.m_requestPending = false;
};