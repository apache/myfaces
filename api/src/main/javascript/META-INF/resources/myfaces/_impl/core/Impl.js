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

/** @namespace myfaces._impl.core.Impl*/
/** @namespace myfaces._impl._util._ListenerQueue */
myfaces._impl.core._Runtime.singletonExtendClass("myfaces._impl.core.Impl", Object, {

    //third option myfaces._impl.xhrCoreAjax which will be the new core impl for now
    _transport : new (myfaces._impl.core._Runtime.getGlobalConfig("transport", myfaces._impl.xhrCore._Transports))(),

    /**
     * external event listener queue!
     */
    _evtListeners : new (myfaces._impl.core._Runtime.getGlobalConfig("eventListenerQueue", myfaces._impl._util._ListenerQueue))(),

    /**
     * external error listener queue!
     */
    _errListeners : new (myfaces._impl.core._Runtime.getGlobalConfig("errorListenerQueue", myfaces._impl._util._ListenerQueue))(),

    /*CONSTANTS*/

    /*internal identifiers for options*/
    IDENT_ALL : "@all",
    IDENT_NONE : "@none",
    IDENT_THIS : "@this",
    IDENT_FORM : "@form",

    /*
     * [STATIC] constants
     */

    P_PARTIAL_SOURCE : "javax.faces.source",
    P_VIEWSTATE : "javax.faces.ViewState",
    P_AJAX : "javax.faces.partial.ajax",
    P_EXECUTE : "javax.faces.partial.execute",
    P_RENDER : "javax.faces.partial.render",
    P_EVT : "javax.faces.partial.event",

    /* message types */
    ERROR : "error",
    EVENT : "event",

    /* event emitting stages */
    BEGIN : "begin",
    COMPLETE : "complete",
    SUCCESS : "success",

    /*ajax errors spec 14.4.2*/
    HTTPERROR : "httpError",
    EMPTY_RESPONSE : "emptyResponse",
    MALFORMEDXML : "malformedXML",
    SERVER_ERROR : "serverError",
    CLIENT_ERROR : "clientError",



    /**
     * collect and encode data for a given form element (must be of type form)
     * find the javax.faces.ViewState element and encode its value as well!
     * return a concatenated string of the encoded values!
     *
     * @throws error in case of the given element not being of type form!
     * https://issues.apache.org/jira/browse/MYFACES-2110
     */
    getViewState : function(form) {
        /**
         *  typecheck assert!, we opt for strong typing here
         *  because it makes it easier to detect bugs
         */
        if (form) {
            form = myfaces._impl._util._Lang.byId(form);
        }

        if (!form
                || !form.nodeName
                || form.nodeName.toLowerCase() != "form") {
            throw new Error("jsf.viewState: param value not of type form!");
        }

        var ajaxUtils = new myfaces._impl.xhrCore._AjaxUtils(0);
        return ajaxUtils.encodeSubmittableFields(null, null, null, form, null);

    },

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
     * @param {String|Node} elem any dom element no matter being it html or jsf, from which the event is emitted
     * @param {|Event|} event any javascript event supported by that object
     * @param {|Object|} options  map of options being pushed into the ajax cycle
     */
    request : function(elem, event, options) {

        /*namespace remap for our local function context we mix the entire function namespace into
         *a local function variable so that we do not have to write the entire namespace
         *all the time
         **/
        var _Lang = myfaces._impl._util._Lang;
        var elementId = null;
        /**
         * we cross reference statically hence the mapping here
         * the entire mapping between the functions is stateless
         */
        //null definitely means no event passed down so we skip the ie specific checks
        if('undefined' == typeof event) {
            event = window.event || null;
        }

        elem = _Lang.byId(elem);

        if (elem) {
            //detached element handling, we also store the element name
            //to get a fallback option in case the identifier is not determinable
            // anymore, in case of a framework induced detachment the element.name should
            // be shared if the identifier is not determinable anymore
            elementId = elem.id || null;
            if ((elementId == null || elementId == '') && elem.name) {
                elementId = elem.name;
            }
        }

        /*assert if the onerror is set and once if it is set it must be of type function*/
        _Lang.assertType(options.onerror, "function");
        /*assert if the onevent is set and once if it is set it must be of type function*/
        _Lang.assertType(options.onevent, "function");

        /*
         * We make a copy of our options because
         * we should not touch the incoming params!
         */
        var passThrgh = _Lang.mixMaps({}, options, true);

        /*additional passthrough cleanup*/
        /*ie6 supportive code to prevent browser leaks*/
        passThrgh.onevent = null;
        delete passThrgh.onevent;
        /*ie6 supportive code to prevent browser leaks*/
        passThrgh.onerror = null;
        delete passThrgh.onerror;

        if (event) {
            passThrgh[this.P_EVT] = event.type;
        }

        /**
         * ajax pass through context with the source
         * onevent and onerror
         */
        var context = {};
        context.source = elem;
        context.onevent = options.onevent;
        context.onerror = options.onerror;

        /**
         * fetch the parent form
         */

        var form = myfaces._impl._util._Dom.fuzzyFormDetection(elem);

        var formErr = "Sourceform could not be determined, either because element is not attached to a form or we have multiple forms with named elements of the same identifier or name, stopping the ajax processing";

        if (!form && event) {
            form = myfaces._impl._util._Dom.fuzzyFormDetection(_Lang.getEventTarget(event));
            if (!form) {
                throw Error(formErr);
            }
        } else if (!form) {
            throw Error(formErr);
        }

        /**
         * binding contract the javax.faces.source must be set
         */
        passThrgh[this.P_PARTIAL_SOURCE] = elementId;

        /**
         * javax.faces.partial.ajax must be set to true
         */
        passThrgh[this.P_AJAX] = true;

        /**
         * if execute or render exist
         * we have to pass them down as a blank delimited string representation
         * of an array of ids!
         */
        var exec, none, all, render = null;
        if (passThrgh.execute) {
            /*the options must be a blank delimited list of strings*/
            exec = _Lang.arrToString(passThrgh.execute, ' ');
            none = exec.indexOf(this.IDENT_NONE) != -1;
            all = exec.indexOf(this.IDENT_ALL) != -1;
            if (!none && !all) {
                exec = exec.replace(this.IDENT_FORM, form.id);
                exec = exec.replace(this.IDENT_THIS, elementId);

                passThrgh[this.P_EXECUTE] = exec;
            } else if (all) {
                passThrgh[this.P_EXECUTE] = this.IDENT_ALL;
            }

            passThrgh.execute = null;
            /*remap just in case we have a valid pointer to an existing object*/
            delete passThrgh.execute;
        } else {
            passThrgh[this.P_EXECUTE] = elementId;
        }

        if (passThrgh.render) {
            render = _Lang.arrToString(passThrgh.render, ' ');
            none = render.indexOf(this.IDENT_NONE) != -1;
            all = render.indexOf(this.IDENT_ALL) != -1;
            if (!none && !all) {
                render = render.replace(this.IDENT_FORM, form.id);
                render = render.replace(this.IDENT_THIS, elementId);
                passThrgh[this.P_RENDER] = render;
                passThrgh.render = null;
            } else if (all) {
                passThrgh[this.P_RENDER] = this.IDENT_ALL;

            }
            delete passThrgh.render;
        }

        //implementation specific options are added to the context for further processing
        if (passThrgh.myfaces) {
            context.myfaces = passThrgh.myfaces;
            delete passThrgh.myfaces;
        }

        this._transport.xhrQueuedPost(elem, form, context, passThrgh);

    },

    addOnError : function(/*function*/errorListener) {
        /*error handling already done in the assert of the queue*/
        this._errListeners.enqueue(errorListener);
    },

    addOnEvent : function(/*function*/eventListener) {
        /*error handling already done in the assert of the queue*/
        this._evtListeners.enqueue(eventListener);
    },



    /**
     * implementation triggering the error chain
     *
     * @param {Object} request the request object which comes from the xhr cycle
     * @param {Object} context (Map) the context object being pushed over the xhr cycle keeping additional metadata
     * @param {String} name the error name
     * @param {String} serverErrorName the server error name in case of a server error
     * @param {String} serverErrorMessage the server error message in case of a server error
     *
     *  handles the errors, in case of an onError exists within the context the onError is called as local error handler
     *  the registered error handlers in the queue receiv an error message to be dealt with
     *  and if the projectStage is at development an alert box is displayed
     *
     *  note: we have additional functionality here, via the global config myfaces.config.defaultErrorOutput a function can be provided
     *  which changes the default output behavior from alert to something else
     *
     *
     */
    sendError : function sendError(/*Object*/request, /*Object*/ context, /*String*/ name, /*String*/ serverErrorName, /*String*/ serverErrorMessage) {
        var eventData = {};
        //we keep this in a closure because we might reuse it for our serverErrorMessage
        var malFormedMessage = function() {
            return (name && name === myfaces._impl.core.Impl.MALFORMEDXML) ? "The server response could not be parsed, the server has returned with a response which is not xml !" : "";
        };

        eventData.type = this.ERROR;

        eventData.status = name;
        eventData.serverErrorName = serverErrorName;
        eventData.serverErrorMessage = serverErrorMessage;

        try {
            eventData.source = context.source;
            eventData.responseCode = request.status;
            eventData.responseText = request.responseText;
            eventData.responseXML = request.responseXML;
        } catch (e) {
            // silently ignore: user can find out by examining the event data
        }

        /**/
        if (myfaces._impl._util._Lang.exists(context, "onerror")) {
            context.onerror(eventData);
        }

        /*now we serve the queue as well*/
        this._errListeners.broadcastEvent(eventData);

        if (jsf.getProjectStage() === "Development" && this._errListeners.length() == 0) {
            var defaultErrorOutput = myfaces._impl.core._Runtime.getGlobalConfig("defaultErrorOutput", alert);
            var finalMessage = [];

            finalMessage.push((name) ? name : "");
            finalMessage.push((serverErrorName) ? serverErrorName : "");
            finalMessage.push((serverErrorMessage) ? serverErrorMessage : "");
            finalMessage.push(malFormedMessage());

            defaultErrorOutput(finalMessage.join("-") + " Note, this message is only sent, because project stage is development and no " +
                    "other error listeners are registered.");
        }
    },

    /**
     * sends an event
     */
    sendEvent : function sendEvent(/*Object*/request, /*Object*/ context, /*event name*/ name) {
        var eventData = {};
        eventData.type = this.EVENT;

        eventData.status = name;
        eventData.source = context.source;

        if (name !== this.BEGIN) {

            try {
                eventData.responseCode = request.status;
                eventData.responseText = request.responseText;
                eventData.responseXML = request.responseXML;

            } catch (e) {
                var impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
                impl.sendError(request, context, this.CLIENT_ERROR, "ErrorRetrievingResponse",
                        "Parts of the response couldn't be retrieved when constructing the event data: " + e);
                //client errors are not swallowed
                throw e;
            }

        }

        /**/
        if (context.onevent) {
            /*calling null to preserve the original scope*/
            context.onevent.call(null, eventData);
        }

        /*now we serve the queue as well*/
        this._evtListeners.broadcastEvent(eventData);
    },

    /**
     * processes the ajax response if the ajax request completes successfully
     * @param {Object} request (xhrRequest) the ajax request!
     * @param {Object} context (Map) context map keeping context data not being passed down over
     * the request boundary but kept on the client
     */
    response : function(request, context) {
        this._transport.response(request, context);
    },

    /**
     * @return the project stage also emitted by the server:
     * it cannot be cached and must be delivered over the server
     * The value for it comes from the request parameter of the jsf.js script called "stage".
     */
    getProjectStage : function() {
        /* run through all script tags and try to find the one that includes jsf.js */
        var scriptTags = document.getElementsByTagName("script");
        for (var i = 0; i < scriptTags.length; i++)
        {
            if (scriptTags[i].src.search(/\/javax\.faces\.resource\/jsf\.js.*ln=javax\.faces/) != -1)
            {
                var result = scriptTags[i].src.match(/stage=([^&;]*)/);
                if (result)
                {
                    // we found stage=XXX
                    // return only valid values of ProjectStage
                    if (result[1] == "Production"
                            || result[1] == "Development"
                            || result[1] == "SystemTest"
                            || result[1] == "UnitTest")
                    {
                        return result[1];
                    }
                }
                else
                {
                    //we found the script, but there was no stage parameter --> Production
                    return "Production";
                }
            }
        }
        /* we could not find anything valid --> return the default value */
        return "Production";
    },

    /**
     * implementation of the external chain function
     * moved into the impl
     *
     *  @param {Object} source the source which also becomes
     * the scope for the calling function (unspecified side behavior)
     * the spec states here that the source can be any arbitrary code block.
     * Which means it either is a javascript function directly passed or a code block
     * which has to be evaluated separately.
     *
     * After revisiting the code additional testing against components showed that
     * the this parameter is only targeted at the component triggering the eval
     * (event) if a string code block is passed. This is behavior we have to resemble
     * in our function here as well, I guess.
     *
     * @param {Event} event the event object being passed down into the the chain as event origin
     *   the spec is contradicting here, it on one hand defines event, and on the other
     *   it says it is optional, after asking, it meant that event must be passed down
     *   but can be undefined
     */
    chain : function(source, event) {
        var len = arguments.length;
        //the spec is contradicting here, it on one hand defines event, and on the other
        //it says it is optional, I have cleared this up now
        //the spec meant the param must be passed down, but can be 'undefined'
        if (len < 2) {
            throw new Error(" an event object or unknown must be passed as second parameter ");
        } else if (len < 3) {
            if ('function' == typeof event || myfaces._impl._util._Lang.isString(event)) {
                throw new Error(" an event must be passed down (either a an event object null or undefined) ");
            }
            //nothing to be done here, move along
            return true;
        }
        //now we fetch from what is given from the parameter list
        //we cannot work with splice here in any performant way so we do it the hard way
        //arguments only are give if not set to undefined even null values!

        //assertions source either null or set as dom element:

        if ('undefined' == typeof source) {
            throw new Error(" source must be defined or null");
            //allowed chain datatypes
        } else if ('function' == typeof source) {
            throw new Error(" source cannot be a function (probably source and event were not defined or set to null");
        }
        if (myfaces._impl._util._Lang.isString(source)) {
            throw new Error(" source cannot be a string ");
        }

        //assertion if event is a function or a string we already are in our function elements
        //since event either is undefined, null or a valid event object

        if ('function' == typeof event || myfaces._impl._util._Lang.isString(event)) {
            throw new Error(" an event must be passed down (either a an event object null or undefined) ");
        }

        for (var cnt = 2; cnt < len; cnt++) {
            //we do not change the scope of the incoming functions
            //but we reuse the argument array capabilities of apply
            var ret;

            if ('function' == typeof arguments[cnt]) {
                ret = arguments[cnt].call(source, event);
            } else {
                //either a function or a string can be passed in case of a string we have to wrap it into another function
                ret = new Function("event", arguments[cnt]).call(source, event);
            }
            //now if one function returns false in between we stop the execution of the cycle
            //here, note we do a strong comparison here to avoid constructs like 'false' or null triggering
            if (ret === false /*undefined check implicitly done here by using a strong compare*/) {
                return false;
            }
        }
        return true;

    }
});    
