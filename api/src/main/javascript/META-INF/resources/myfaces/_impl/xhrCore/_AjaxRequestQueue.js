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
 * Version: $Revision: 1.3 $ $Date: 2009/05/31 09:16:44 $
 *
 */
myfaces._impl.core._Runtime.extendClass("myfaces._impl.xhrCore._AjaxRequestQueue", myfaces._impl._util._Queue, {

    _curReq : null,

    constructor_: function(){
        this._callSuper("constructor");

        var _this = this;

        var _handleCallback = this.handleCallback;
        //we have to scope this because the xhr request will change the
        //callback scope to window from outside, hence we have to enforce a scope here
        this.handleCallback = myfaces._impl._util._Lang.hitch(_this, _handleCallback);
    },

    /**
     *
     * provides api callback
     *
     * we have to hitch this function since
     * the xhr request changes scopes
     * in the asynchronous case, the callback always
     * has to reference this
     */
    handleCallback : function() {

        if (this._curReq != null) {
            this._curReq.requestCallback();
            //this.processQueue();
        }
    },

    /**
     * delay request, then call queueNow
     * @param {Object} request (myfaces._impl.xhrCore._AjaxRequest) request to send
     */
    queueRequest : function(request) {
        if (typeof request._delay == "number") {
            this.clearDelayTimeout();
            var _Lang = myfaces._impl._util._Lang;
            this.delayTimeoutId = window.setTimeout(
                    _Lang.hitch(this, function() {
                        this.clearDelayTimeout();
                        this.queueNow(request);
                    }), request._delay);
        } else {
            this.enqueue(request);
        }
    },

    /**
     * timeout clearing routine
     * for timeout requests
     */
    clearDelayTimeout : function() {
        try {
            if (typeof this.delayTimeoutId == "number") {
                window.clearTimeout(this.delayTimeoutId);
                delete this.delayTimeoutId;
            }
        } catch (e) {
            // already timed out
        }
    },

    /**
     * send a request or keep it in a queue
     * @param {myfaces._impl.xhrCore._AjaxRequest} request - request to send
     */
    enqueue : function(request) {
        if (this._curReq == null) {
            this._curReq = request;
            this._curReq.send();
        } else {

            this._callSuper("enqueue",request);
            if (request._queueSize != this._queueSize) {
                this.setQueueSize(request._queueSize);
            }
        }
    },

    /**
     * process queue, send request, if exists
     */
    processQueue: function() {
        this._curReq = this.dequeue();
        if (null != this._curReq) {
            this._curReq.send();
        }
    },

    /**
     * cleanup queue
     */
    cleanup: function() {
        this._curReq = null;
        this._callSuper("cleanup");
    }
});

//TODO replace this with a singleton hooked to the direct xhr object
//instead of one hooked to the qeue
if (myfaces._impl.core._Runtime.reserveNamespace("myfaces._impl.xhrCore._RQInstance")) {
    myfaces._impl.xhrCore._RQInstance = new myfaces._impl.xhrCore._AjaxRequestQueue();
}