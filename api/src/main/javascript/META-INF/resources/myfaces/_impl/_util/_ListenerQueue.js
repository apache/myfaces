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
myfaces._impl.core._Runtime.extendClass("myfaces._impl._util._ListenerQueue", Object, {
    constructor_: function() {
        this._queue = [];
    },

    length: function() {
      return this._queue.length;  
    },

    /**
     * listener type safety assertion function
     *
     * @param listener must be of type function otherwise an error is raised
     */
    _assertListener : function(/*function*/listener) {
        if ("function" != typeof (listener)) {
            throw Error("Error: myfaces._impl._util._ListenerQueue." + arguments.caller.toString() + "Parameter must be of type function");
        }
    },

    /**
     * adds a listener to the queue
     *
     * @param listener the listener to be added
     */
    add : function(/*function*/listener) {
        this._assertListener(listener);
        this._queue.push(listener);
    },

    /**
     * removes a listener form the queue
     *
     * @param listener the listener to be removed
     */
    remove : function(/*function*/listener) {
        this._assertListener(listener);
        /*find element in queue*/
        var cnt = 0;
        var len = this._queue.length;
        while (cnt < len && this._queue[cnt] != listener) {
            cnt += 1;
        }
        /*found*/
        if (cnt < len) {
            this._queue[cnt] = null;
            /*we remove the element now as fast as possible*/
            this._queue.splice(cnt, 1);
        }

    },
    
    /**
     * generic broadcast with a number of arguments being passed down
     * @param scope the execution scope for the event callback
     * @param argument ,...*  the arguments list which has to be passed
     *                  down the queue function
     */
    broadcastScopedEvent : function(scope, /*any*/argument) {
        for (var cnt = 0; cnt < this._queue.length; cnt ++) {
            /**
             * we call the original method under its original scope
             * use hitch to keep the original scope in place
             * because there is no way that I can keep it from here
             **/
            var varArgs = [];
            var len = arguments.length;
            for (var argsCnt = 1; argsCnt < len; argsCnt++) {
                varArgs.push(arguments[argsCnt]);
            }
            this._queue[cnt].apply(scope, varArgs);
        }
    },

    /**
     * generic broadcast with a number of arguments being passed down
     */
    broadcastEvent : function(/*any*/argument) {
        var len = this._queue.length;
        for (var cnt = 0; cnt < len; cnt ++) {
            /**
             * we call the original method under its original scope
             * use hitch to keep the original scope in place
             * because there is no way that I can keep it from here
             **/

            this._queue[cnt].apply(null, arguments);
        }
    }
});