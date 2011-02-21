/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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
/*
 * a classical listener queue pattern
 */




/**
 * Simple listener queue with closures which shall be
 * called
 *
 * idea:
 * var queue = new myfaces._impl._util._ListenerQueue();
 */

/** @namespace myfaces._impl._util._ListenerQueue */
myfaces._impl.core._Runtime.extendClass("myfaces._impl._util._ListenerQueue", myfaces._impl._util._Queue, {

    constructor_: function() {
        this._callSuper("constructor");
    },

    /**
     * listener type safety assertion function
     *
     * @param listener must be of type function otherwise an error is raised
     */
    _assertListener : function(/*function*/listener) {
        if ("function" != typeof (listener)) {
            var msg = myfaces._impl._util._Lang.getMessage("ERR_PARAM_GENERIC",null,"_ListenerQueue", arguments.caller.toString(),"function" );
            throw Error(msg);
        }
    },

    /**
     * adds a listener to the queue
     *
     * @param listener the listener to be added
     */
    enqueue : function(/*function*/listener) {
        this._assertListener(listener);
        this._callSuper("enqueue", listener);
    },

    /**
     * removes a listener form the queue
     *
     * @param listener the listener to be removed
     */
    remove : function(/*function*/listener) {
        this._assertListener(listener);
        this._callSuper("remove", listener);
    },

    /**
     * generic broadcast with a number of arguments being passed down
     */
    broadcastEvent : function(/*any*/argument) {
        var _Lang = myfaces._impl._util._Lang;
        var _args = _Lang.objToArray(arguments);

        var broadCastFunc = function(element) {
            element.apply(null, _args);
        };
        try {
            this.each(broadCastFunc);
        } finally {
            broadCastFunc = null;
        }
    }
});