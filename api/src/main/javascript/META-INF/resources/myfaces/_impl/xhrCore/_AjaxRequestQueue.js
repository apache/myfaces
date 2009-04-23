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
 * Version: $Revision: 1.6 $ $Date: 2009/04/18 17:19:12 $
 *
 */

_reserveMyfacesNamespaces();

if (!myfaces._impl._util._LangUtils.exists(myfaces._impl.xhrCore, "_AjaxRequestQueue")) {

    /**
     * Constructor
     */
    myfaces._impl.xhrCore._AjaxRequestQueue = function() {
        this.m_request = null;
        this.m_queuedRequests = [];
        this.m_exception = new myfaces._impl.xhrCore._Exception("myfaces._impl.xhrCore._AjaxRequestQueue", "NONE");
    };

    /**
     * [STATIC PROPERTIES]
     */
    myfaces._impl.xhrCore._AjaxRequestQueue.queue = new myfaces._impl.xhrCore._AjaxRequestQueue();

    /**
     * [STATIC]
     * provides api callback
     */
    myfaces._impl.xhrCore._AjaxRequestQueue.handleCallback = function() {
        if (myfaces._impl.xhrCore._AjaxRequestQueue.queue.m_request != null) {
            myfaces._impl.xhrCore._AjaxRequestQueue.queue.m_request.requestCallback();
        } else {
            myfaces._impl.xhrCore._AjaxRequestQueue.queue.m_exception.throwWarning
            (null, null, "doRequestCallback", "No request object available");
        }
    };

    /**
     * send a reuest or keeps it in a queue
     * @param {myfaces._impl.xhrCore._AjaxRequest} request - request to send
     */
    myfaces._impl.xhrCore._AjaxRequestQueue.prototype.queueRequest = function(request) {
        if (this.m_request == null) {
            this.m_request = request;
            this.m_request.send();
        } else {
            this.m_queuedRequests.push(request);
            if (request.m_queuesize > -1 && request.m_queuesize < this.m_queuedRequests.length)
                this.m_queuedRequests.shift();
        }
    };

    /**
     * process queue, send request, if exists
     */
    myfaces._impl.xhrCore._AjaxRequestQueue.prototype.processQueue = function() {
        if (this.m_queuedRequests.length > 0) {
            // Using Javascripts build-in queue capabilities here!
            // JSF RI is using Delayed Shift Queue (DSQ), which starts to outperform the build-in queue
            // when queue size exceeds ~10 requests (http://safalra.com/web-design/javascript/queues/).
            // With JSF Ajax the queue will hardly ever reach this size.
            this.m_request = this.m_queuedRequests.shift();
            this.m_request.send();
        } else {
            this.m_request = null;
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

}