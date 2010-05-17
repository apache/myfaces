/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

myfaces._impl.core._Runtime.extendClass("myfaces._impl._util._Queue", Object, {
    //faster queue by http://safalra.com/web-design/javascript/queues/Queue.js
    //license public domain
    //The trick is to simply reduce the number of slice and slice ops to a bare minimum.

    _queue : null,
    _queueSpace : 0,
    _queueSize: -1,

    constructor_: function() {
        this._queue = [];
    },

    length: function() {
        // return the number of elements in the queue
        return this._queue.length - this._queueSpace;

    },

    isEmpty: function() {
        // return true if the queue is empty, and false otherwise
        return (this._queue.length == 0);
    },

    setQueueSize: function(newSize) {
        this._queueSize = newSize;
        this._readjust();
    },

    /**
     * adds a listener to the queue
     *
     * @param element the listener to be added
     */
    enqueue : function(/*function*/element) {
        this._queue.push(element);
        //qeuesize is bigger than the limit we drop one element so that we are
        //back in line
        this._readjust();
    },

    _readjust: function() {
        while (this._queueSize > -1 && this.length() > this._queueSize) {
            this.dequeue();
        }
    },

    /**
     * removes a listener form the queue
     *
     * @param element the listener to be removed
     */
    remove : function(/*function*/element) {
        /*find element in queue*/
        var index = this.indexOf(element);
        /*found*/
        if (index != -1) {
            this._queue.splice(index, 1);
        }
    },

    dequeue: function() {
        // initialise the element to return to be undefined
        var element = null;

        // check whether the queue is empty
        if (this._queue.length) {

            // fetch the oldest element in the queue
            element = this._queue[this._queueSpace];

            // update the amount of space and check whether a shift should occur
            //added here a max limit of 30
            if (++this._queueSpace * 2 >= this._queue.length) {

                // set the queue equal to the non-empty portion of the queue
                this._queue = this._queue.slice(this._queueSpace);

                // reset the amount of space at the front of the queue
                this._queueSpace = 0;

            }

        }

        // return the removed element
        return element;
    },

    /**
     * simple foreach
     *
     * @param closure a closure which processes the element
     */
    each: function(closure) {
        var cnt = this._queueSpace;
        var len = this._queue.length;
        for (; cnt < len; cnt++) {
            closure(this._queue[cnt]);
        }
    },

    /**
     * Simple filter
     *
     * @param closure a closure which returns true or false depending
     * whether the filter has triggered
     *
     * @return an array of filtered queue entries
     */
    filter: function(closure) {
        var retVal = [];
        var cnt = this._queueSpace;
        var len = this._queue.length;
        for (; cnt < len; cnt++) {
            if (closure(this._queue[cnt])) {
                retVal.push(this._queue[cnt]);
            }
        }
        return retVal;
    },

    indexOf: function(element) {
        var cnt = this._queueSpace;
        var len = this._queue.length;
        while (cnt < len && this._queue[cnt] !== element) {
            cnt += 1;
        }
        /*found*/
        cnt = (cnt < len) ? cnt : -1;
        return cnt;
    },

    cleanup: function() {
        this._queue = [];
        this._queueSpace = 0;
    }
});

