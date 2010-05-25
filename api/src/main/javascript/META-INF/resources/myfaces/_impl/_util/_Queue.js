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

/** @namespace myfaces._impl._util._Queue */
myfaces._impl.core._Runtime.extendClass("myfaces._impl._util._Queue", Object, {
    //faster queue by http://safalra.com/web-design/javascript/queues/Queue.js
    //license public domain
    //The trick is to simply reduce the number of slice and slice ops to a bare minimum.

    _q : null,
    _space : 0,
    _size: -1,

    constructor_: function() {
        this._q = [];
       
    },

    length: function() {
        // return the number of elements in the queue
        return this._q.length - this._space;

    },

    isEmpty: function() {
        // return true if the queue is empty, and false otherwise
        return (this._q.length == 0);
    },

    setQueueSize: function(newSize) {
        this._size = newSize;
        this._readjust();
    },

    /**
     * adds a listener to the queue
     *
     * @param element the listener to be added
     */
    enqueue : function(/*function*/element) {
        this._q.push(element);
        //qeuesize is bigger than the limit we drop one element so that we are
        //back in line
        this._readjust();
    },

    _readjust: function() {
        while (this._size > -1 && this.length() > this._size) {
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
            this._q.splice(index, 1);
        }
    },

    dequeue: function() {
        // initialise the element to return to be undefined
        var element = null;

        // check whether the queue is empty
        if (this._q.length) {

            // fetch the oldest element in the queue
            element = this._q[this._space];

            // update the amount of space and check whether a shift should occur
            //added here a max limit of 30
            if (++this._space * 2 >= this._q.length) {

                // set the queue equal to the non-empty portion of the queue
                this._q = this._q.slice(this._space);

                // reset the amount of space at the front of the queue
                this._space = 0;

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
        //we have a browser optimized ecmascript version present
        //which again is all newer browsers except ie
        if(Array.prototype.forEach) {
           return (this._space)? this._q.slice(this._space).forEach( closure ): this._q.forEach( closure ) ;
        }

        var len = this._q.length;
        for (var cnt = this._space; cnt < len; cnt++) {
            closure(this._q[cnt]);
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
        var ret = [];
        var len = this._q.length;

        //we have a browser optimized ecmascript version present
        //which again is all newer browsers except ie        
        if(Array.prototype.filter) {
           return (this._space)? this._q.slice(this._space).filter( closure ): this._q.filter( closure );
        }
        
        for (var cnt = this._space; cnt < len; cnt++) {
            if (closure(this._q[cnt])) {
                ret.push(this._q[cnt]);
            }
        }
        return ret;
    },

    indexOf: function(element) {
        var cnt = this._space;
        var len = this._q.length;
        while (cnt < len && this._q[cnt] !== element) {
            cnt += 1;
        }
        /*found*/
        cnt = (cnt < len) ? cnt : -1;
        return cnt;
    },

    cleanup: function() {
        this._q = [];
        this._space = 0;
    }
});

