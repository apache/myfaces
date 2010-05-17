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
            throw Error("Error: myfaces._impl._util._ListenerQueue." + arguments.caller.toString() + "Parameter must be of type function");
        }
    },

    /**
     * adds a listener to the queue
     *
     * @param listener the listener to be added
     */
    enqueue : function(/*function*/listener) {
        this._assertListener(listener);
        this._callSuper("enqueue");
    },

    /**
     * removes a listener form the queue
     *
     * @param listener the listener to be removed
     */
    remove : function(/*function*/listener) {
        this._assertListener(listener);
        this._callSuper("remove");
    },

    /**
     * generic broadcast with a number of arguments being passed down
     * @param scope the execution scope for the event callback
     * @param argument ,...*  the arguments list which has to be passed
     *                  down the queue function
     */
    broadcastScopedEvent : function(scope, /*any*/argument) {
        var _Lang = myfaces._impl._util._Lang;
        var _args = _Lang.objToArray(arguments);

        var broadCastFunc = function(element) {
            element.apply(scope, Array.prototype.slice(_args, 1));
        };
        this.each(broadCastFunc);
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
        this.each(broadCastFunc);
    }
});