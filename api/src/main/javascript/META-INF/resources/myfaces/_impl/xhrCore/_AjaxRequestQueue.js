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
/** @namespace myfaces._impl.xhrCore._AjaxRequestQueue */
_MF_CLS("myfaces._impl.xhrCore._AjaxRequestQueue", myfaces._impl._util._Queue,
        /** @lends  myfaces._impl.xhrCore._AjaxRequestQueue.prototype */
        {

    /**
     * a pointer towards the currently processed
     * request in our queue
     */
    _curReq : null,


    /**
     * the standard constructur of our class
     */
    constructor_: function() {
        this._callSuper("constructor");
    },

    /**
     * delay request, then call enqueue
     * @param {Object} request (myfaces._impl.xhrCore._AjaxRequest) request to send
     */
    enqueue : function(request) {

        if (typeof request._delay == "number") {
            this.clearDelayTimeout();
            var _Lang = myfaces._impl._util._Lang;
            this._delayTimeoutId = window.setTimeout(
                    _Lang.hitch(this, function() {
                        this.clearDelayTimeout();
                        //lets clear the delay time to enqueue correctly 
                        delete request._delay;
                        this.enqueue(request);
                    }), request._delay);
        } else {
            if (this._curReq == null) {
                this._curReq = request;
                this._curReq.send();
            } else {
                this._callSuper("enqueue", request);
                if (request._queueSize != this._size) {
                    this.setQueueSize(request._queueSize);
                }
            }
        }
    },

    /**
     * timeout clearing routine
     * for timeout requests
     */
    clearDelayTimeout : function() {
        try {
            if (typeof this._delayTimeoutId == "number") {
                window.clearTimeout(this._delayTimeoutId);
                delete this._delayTimeoutId;
            }
        } catch (e) {
            // already timed out
        }
    },


    /**
     * process queue, send request, if exists
     */
    processQueue: function() {
        this._curReq = this.dequeue();
        if (this._curReq) {
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

