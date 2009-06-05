/*
 * a classical listener queue pattern
 */


_reserveMyfacesNamespaces();

/**
 * Simple listener queue with closures which shall be
 * called
 *
 * idea:
 * var queue = new myfaces._impl._util._ListenerQueue();
 */
if(!myfaces._impl._util._LangUtils.exists(myfaces, "_ListenerQueue")) {
    myfaces._impl._util._ListenerQueue = function() {
        this._queue = [];

    }

    myfaces._impl._util._ListenerQueue.prototype._assertListener = function(/*function*/listener) {
        if("function" != typeof (listener)) {
            throw new Exception("Error: myfaces._impl._util._ListenerQueue." + arguments.caller.toString() + "Parameter must be of type function");
        }
    }

    myfaces._impl._util._ListenerQueue.prototype.add = function(/*function*/listener) {
        this._assertListener( listener);
        this._queue.push(listener);
    }

    myfaces._impl._util._ListenerQueue.prototype.remove = function(/*function*/listener) {
        this._assertListener( listener);
        /*find element in queue*/
        var cnt = 0;
        while(cnt < this._queue.length && this._queue[cnt] != listener) {
            cnt += 1;
        }
        /*found*/
        if(cnt < this._queue.length) {
            this._queue[cnt] = null;
            /*we remove the element now as fast as possible*/
            this._queue.splice(cnt, 1);
        }

    }
   /**
     * generic broadcast with a number of arguments being passed down
     * @param scope the execution scope for the event callback
     * @param argument,...*  the arguments list which has to be passed
     *                  down the queue function
     */
    myfaces._impl._util._ListenerQueue.prototype.broadcastScopedEvent = function(scope, /*any*/argument) {
        for(var cnt = 0; cnt < this._queue.length; cnt ++) {
            /**
             * we call the original method under its original scope
             * use hitch to keep the original scope in place
             * because there is no way that I can keep it from here
             **/
            var varArgs = [];
            for(var argsCnt = 1; argsCnt < arguments.length; argsCnt++) {
                varArgs.push(arguments[argsCnt]);
            }
            this._queue[cnt].apply(scope, varArgs);
        }
    }

    /**
     * generic broadcast with a number of arguments being passed down
     */
    myfaces._impl._util._ListenerQueue.prototype.broadcastEvent = function(/*any*/argument) {
        for(var cnt = 0; cnt < this._queue.length; cnt ++) {
            /**
             * we call the original method under its original scope
             * use hitch to keep the original scope in place
             * because there is no way that I can keep it from here
             **/
 
            this._queue[cnt].apply(null, arguments);
        }
    }
}