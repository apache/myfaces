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

/**
 *  MyFaces core javascripting libraries
 *
 *  Those are the central public API functions in the Faces2
 *  Ajax API! They handle the entire form submit and ajax send
 *  and resolve cycle!
 */

/**
* @ignore
*/
if (!window.faces) {
    /**
    * @namespace faces
    */
    var faces = new function() {
        /*
         * Version of the implementation for the faces.js.
         * <p />
         * as specified within the faces specifications:
         * <ul>
         * <li>left two digits major release number</li>
         * <li>middle two digits minor spec release number</li>
         * <li>right two digits bug release number</li>
         * </ul>
         * @constant
         */
        this.specversion = 220000;
        /**
         * Implementation version as specified within the faces specification.
         * <p />
         * A number increased with every implementation version
         * and reset by moving to a new spec release number
         *
		 * @constant
         */
        this.implversion = 0;

        /**
         * SeparatorChar as defined by UINamingContainer.getNamingContainerSeparatorChar()
         * @type {Char}
         */
        this.separatorchar = getSeparatorChar();

        /**
         * This method is responsible for the return of a given project stage as defined
         * by the faces specification.
         * <p/>
         * Valid return values are:
         * <ul>
         *     <li>&quot;Production&quot;</li>
         *     <li>&quot;Development&quot;</li>
         *     <li>&quot;SystemTest&quot;</li>
         *     <li>&quot;UnitTest&quot;</li>
         * </li>
         *
         * @return {String} the current project state emitted by the server side method:
         * <i>jakarta.faces.application.Application.getProjectStage()</i>
         */
        this.getProjectStage = function() {
            var impl = myfaces._impl.core._Runtime.getGlobalConfig("facesAjaxImpl", myfaces._impl.core.Impl);
            return impl.getProjectStage();
        };

        /**
         * collect and encode data for a given form element (must be of type form)
         * find the jakarta.faces.ViewState element and encode its value as well!
         * return a concatenated string of the encoded values!
         *
         * @throws an exception in case of the given element not being of type form!
         * https://issues.apache.org/jira/browse/MYFACES-2110
         */
        this.getViewState = function(formElement) {
            /*we are not allowed to add the impl on a global scope so we have to inline the code*/
            var impl = myfaces._impl.core._Runtime.getGlobalConfig("facesAjaxImpl", myfaces._impl.core.Impl);
            return impl.getViewState(formElement);
        };

        /**
         * returns the window identifier for the given node / window
         * @param {optional String | DomNode}  the node for which the client identifier has to be determined
         * @return the window identifier or null if none is found
         */
        this.getClientWindow = function() {
            /*we are not allowed to add the impl on a global scope so we have to inline the code*/
            var impl = myfaces._impl.core._Runtime.getGlobalConfig("facesAjaxImpl", myfaces._impl.core.Impl);
            return (arguments.length)? impl.getClientWindow(arguments[0]) : impl.getClientWindow();
        }

        //private helper functions
        function getSeparatorChar() {
            var impl = myfaces._impl.core._Runtime.getGlobalConfig("facesAjaxImpl", myfaces._impl.core.Impl);
            return impl.getSeparatorChar();
        }

    };

    //jsdoc helper to avoid warnings, we map later
    window.faces = faces;
}

/**
 * just to make sure no questions arise, I simply prefer here a weak
 * typeless comparison just in case some frameworks try to interfere
 * by overriding null or fiddeling around with undefined or typeof in some ways
 * it is safer in this case than the standard way of doing a strong comparison
 **/
if (!faces.ajax) {
    /**
    * @namespace faces.ajax
    */
    faces.ajax = new function() {


        /**
         * this function has to send the ajax requests
         *
         * following request conditions must be met:
         * <ul>
         *  <li> the request must be sent asynchronously! </li>
         *  <li> the request must be a POST!!! request </li>
         *  <li> the request url must be the form action attribute </li>
         *  <li> all requests must be queued with a client side request queue to ensure the request ordering!</li>
         * </ul>
         *
         * @param {String|Node} element: any dom element no matter being it html or faces, from which the event is emitted
         * @param {EVENT} event: any javascript event supported by that object
         * @param {Map} options : map of options being pushed into the ajax cycle
         */
        this.request = function(element, event, options) {
            if (!options) {
                options = {};
            }
            /*we are not allowed to add the impl on a global scope so we have to inline the code*/
            var impl = myfaces._impl.core._Runtime.getGlobalConfig("facesAjaxImpl", myfaces._impl.core.Impl);
            return impl.request(element, event, options);
        };

        /**
         * Adds an error handler to our global error queue.
         * the error handler must be of the format <i>function errorListener(&lt;errorData&gt;)</i>
         * with errorData being of following format:
         * <ul>
         *     <li> errorData.type : &quot;error&quot;</li>
         *     <li> errorData.status : the error status message</li>
         *     <li> errorData.errorName : the server error name in case of a server error</li>
         *     <li> errorData.errorMessage : the server error message in case of a server error</li>
         *     <li> errorData.source  : the issuing source element which triggered the request </li>
         *     <li> eventData.responseCode: the response code (aka http request response code, 401 etc...) </li>
         *     <li> eventData.responseText: the request response text </li>
         *     <li> eventData.responseXML: the request response xml </li>
        * </ul>
         *
         * @param {function} errorListener error handler must be of the format <i>function errorListener(&lt;errorData&gt;)</i>
		*/
        this.addOnError = function(/*function*/errorListener) {
            var impl = myfaces._impl.core._Runtime.getGlobalConfig("facesAjaxImpl", myfaces._impl.core.Impl);
            return impl.addOnError(errorListener);
        };

        /**
         * Adds a global event listener to the ajax event queue. The event listener must be a function
         * of following format: <i>function eventListener(&lt;eventData&gt;)</i>
         *
         * @param {function} eventListener event must be of the format <i>function eventListener(&lt;eventData&gt;)</i>
         */
        this.addOnEvent = function(/*function*/eventListener) {
            var impl = myfaces._impl.core._Runtime.getGlobalConfig("facesAjaxImpl", myfaces._impl.core.Impl);
            return impl.addOnEvent(eventListener);
        };

        /**
         * processes the ajax response if the ajax request completes successfully
         * @param request the ajax request!
         * @param context the ajax context!
         */
        this.response = function(/*xhr request object*/request, context) {
            var impl = myfaces._impl.core._Runtime.getGlobalConfig("facesAjaxImpl", myfaces._impl.core.Impl);
            return impl.response(request, context);
        };
    };
}

if (!faces.util) {
    /**
    * @namespace faces.util
    */
    faces.util = new function() {

        /**
         * varargs function which executes a chain of code (functions or any other code)
         *
         * if any of the code returns false, the execution
         * is terminated prematurely skipping the rest of the code!
         *
         * @param {DomNode} source, the callee object
         * @param {Event} event, the event object of the callee event triggering this function
         * @param {optional} functions to be chained, if any of those return false the chain is broken
         */
        this.chain = function(source, event) {
            var impl = myfaces._impl.core._Runtime.getGlobalConfig("facesAjaxImpl", myfaces._impl.core.Impl);
            return impl.chain.apply(impl, arguments);
        };
    };
}

if (!faces.push) {

  /**
   * @namespace faces.push
   */
  faces.push = new function() {

    // "Constant" fields ----------------------------------------------------------------------------------------------
    var URL_PROTOCOL = window.location.protocol.replace("http", "ws") + "//";
    var RECONNECT_INTERVAL = 500;
    var MAX_RECONNECT_ATTEMPTS = 25;
    var REASON_EXPIRED = "Expired";

    // Private static fields ------------------------------------------------------------------------------------------

    /* socket map by token */
    var sockets = {};
    /* component attributes by clientId */
    var components = {};
    /* client ids by token (share websocket connection) */
    var clientIdsByTokens = {};
    var self = {};

    // Private constructor functions ----------------------------------------------------------------------------------
    /**
     * Creates a reconnecting web socket. When the web socket successfully connects on first attempt, then it will
     * automatically reconnect on timeout with cumulative intervals of 500ms with a maximum of 25 attempts (~3 minutes).
     * The <code>onclose</code> function will be called with the error code of the last attempt.
     * @constructor
     * @param {string} channelToken the channel token associated with this websocket connection
     * @param {string} url The URL of the web socket
     * @param {string} channel The name of the web socket channel.
     */
    function Socket(channelToken, url, channel) {

        // Private fields -----------------------------------------------------------------------------------------

        var socket;
        var reconnectAttempts = 0;
        var self = this;

        // Public functions ---------------------------------------------------------------------------------------

        /**
         * Opens the reconnecting web socket.
         */
        self.open = function() {
            if (socket && socket.readyState == 1) {
                return;
            }

            socket = new WebSocket(url);

            socket.onopen = function(event) {
                if (!reconnectAttempts) {
                    var clientIds = clientIdsByTokens[channelToken];
                    for (var i = clientIds.length - 1; i >= 0; i--){
                        var socketClientId = clientIds[i];
                        components[socketClientId]['onopen'](channel);
                    }
                }
                reconnectAttempts = 0;
            };

            socket.onmessage = function(event) {
                var message = JSON.parse(event.data);
                for (var i = clientIdsByTokens[channelToken].length - 1; i >= 0; i--){
                    var socketClientId = clientIdsByTokens[channelToken][i];
                    if(document.getElementById(socketClientId)) {
                        try{
                            components[socketClientId]['onmessage'](message, channel, event);
                        }catch(e){
                            //Ignore
                        }
                        var behaviors = components[socketClientId]['behaviors'];
                        var functions = behaviors[message];
                        if (functions && functions.length) {
                            for (var j = 0; j < functions.length; j++) {
                                try{
                                    functions[j](null);
                                }catch(e){
                                    //Ignore
                                }
                            }
                        }
                    } else {
                        clientIdsByTokens[channelToken].splice(i,1);
                    }
                }
                if (clientIdsByTokens[channelToken].length == 0){
                    //tag dissapeared
                    self.close();
                }

            };

            socket.onclose = function(event) {
                if (!socket
                    || (event.code == 1000 && event.reason == REASON_EXPIRED)
                    || (event.code == 1008)
                    || (!reconnectAttempts)
                    || (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS))
                {
                    var clientIds = clientIdsByTokens[channelToken];
                    for (var i = clientIds.length - 1; i >= 0; i--){
                        var socketClientId = clientIds[i];
                        components[socketClientId]['onclose'](event.code, channel, event);
                    }
                }
                else {
                    setTimeout(self.open, RECONNECT_INTERVAL * reconnectAttempts++);
                }
            };
        };

        socket.onerror = function(event) {
            var clientIds = clientIdsByTokens[channelToken];
            for (var i = clientIds.length - 1; i >= 0; i--){
                var socketClientId = clientIds[i];
                components[socketClientId]['onerror'](channel);
            }
        };

        /**
         * Closes the reconnecting web socket.
         */
        self.close = function() {
            if (socket) {
                var s = socket;
                socket = null;
                s.close();
            }
        };

    }

    // Public static functions ----------------------------------------------------------------------------------------

    /**
     *
     * @param {function} onopen The function to be invoked when the web socket is opened.
     * @param {function} onmessage The function to be invoked when a message is received.
     * @param {function} onerror The function to be invoked when the web socket throws a error.
     * @param {function} onclose The function to be invoked when the web socket is closed.
     * @param {boolean} autoconnect Whether or not to immediately open the socket. Defaults to <code>false</code>.
     */
    this.init = function(socketClientId, uri, channel, onopen, onmessage, onerror, onclose, behaviorScripts, autoconnect) {

        onclose = resolveFunction(onclose);

        if (!window.WebSocket) { // IE6-9.
            onclose(-1, channel);
            return;
        }

        var channelToken = uri.substr(uri.indexOf('?')+1);

        if (!components[socketClientId]) {
            components[socketClientId] = {
                'channelToken': channelToken,
                'onopen': resolveFunction(onopen),
                'onmessage' : resolveFunction(onmessage),
                'onerror' : resolveFunction(onerror),
                'onclose': onclose,
                'behaviors': behaviorScripts,
                'autoconnect': autoconnect};
            if (!clientIdsByTokens[channelToken]) {
                clientIdsByTokens[channelToken] = [];
            }
            clientIdsByTokens[channelToken].push(socketClientId);
            if (!sockets[channelToken]){
                sockets[channelToken] = new Socket(channelToken,
                                    getBaseURL(uri), channel);
            }
        }

        if (autoconnect) {
            this.open(socketClientId);
        }
    };

    /**
     * Open the web socket on the given channel.
     * @param {string} channel The name of the web socket channel.
     * @throws {Error} When channel is unknown.
     */
    this.open = function(socketClientId) {
        getSocket(components[socketClientId]['channelToken']).open();
    };

    /**
     * Close the web socket on the given channel.
     * @param {string} channel The name of the web socket channel.
     * @throws {Error} When channel is unknown.
     */
    this.close = function(socketClientId) {
        getSocket(components[socketClientId]['channelToken']).close();
    };

    // Private static functions ---------------------------------------------------------------------------------------

    /**
     *
     */
    function getBaseURL(url) {
        if (url.indexOf("://") < 0)
        {
            var base = window.location.hostname+":"+window.location.port
            return URL_PROTOCOL + base + url;
        }else
        {
            return url;
        }
    }

    /**
     * Get socket associated with given channelToken.
     * @param {string} channelToken The name of the web socket channelToken.
     * @return {Socket} Socket associated with given channelToken.
     * @throws {Error} When channelToken is unknown, you may need to initialize
     *                 it first via <code>init()</code> function.
     */
    function getSocket(channelToken) {
        var socket = sockets[channelToken];
        if (socket) {
            return socket;
        } else {
            throw new Error("Unknown channelToken: " + channelToken);
        }
    }

    function resolveFunction(fn) {
        return (typeof fn !== "function") && (fn = window[fn] || function(){}), fn;
    }
    // Expose self to public ------------------------------------------------------------------------------------------

    //return self;
    };
}


(!window.myfaces) ? window.myfaces = {} : null;
if (!myfaces.ab) {
    /*
     * Shortcut of the faces.ajax.request, to shorten the rendered JS.
     */
    myfaces.ab = function(source, event, eventName, execute, render, options) {
        if (!options) {
            options = {};
        }

        if (eventName) {
            options["jakarta.faces.behavior.event"] = eventName;
        }
        if (execute) {
            options["execute"] = execute;
        }
        if (render) {
            options["render"] = render;
        }

        faces.ajax.request(source, event, options);
    };
}