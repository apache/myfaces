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

_reserveMyfacesNamespaces();

if (!myfaces._impl._util._LangUtils.exists(myfaces._impl.core, "_jsfImpl")) {

    myfaces._impl.core._jsfImpl = function() {

        //third option myfaces._impl.xhrCoreAjax which will be the new core impl for now
        this._requestHandler = new (myfaces._impl._util._Utils.getGlobalConfig("transport", myfaces._impl.xhrCore._Ajax))();

        /**
         * external event listener queue!
         */
        this._eventListenerQueue = new (myfaces._impl._util._Utils.getGlobalConfig("eventListenerQueue", myfaces._impl._util._ListenerQueue))();

        /**
         * external error listener queue!
         */
        this._errorListenerQueue = new (myfaces._impl._util._Utils.getGlobalConfig("errorListenerQueue", myfaces._impl._util._ListenerQueue))();

    };

    /*CONSTANTS*/

    /*internal identifiers for options*/
    myfaces._impl.core._jsfImpl.prototype._OPT_IDENT_ALL = "@all";
    myfaces._impl.core._jsfImpl.prototype._OPT_IDENT_NONE = "@none";
    myfaces._impl.core._jsfImpl.prototype._OPT_IDENT_THIS = "@this";
    myfaces._impl.core._jsfImpl.prototype._OPT_IDENT_FORM = "@form";

    /*
     * [STATIC] constants
     */

    myfaces._impl.core._jsfImpl._PROP_PARTIAL_SOURCE = "javax.faces.source";
    myfaces._impl.core._jsfImpl._PROP_VIEWSTATE = "javax.faces.ViewState";
    myfaces._impl.core._jsfImpl._PROP_AJAX = "javax.faces.partial.ajax";
    myfaces._impl.core._jsfImpl._PROP_EXECUTE = "javax.faces.partial.execute";
    myfaces._impl.core._jsfImpl._PROP_RENDER = "javax.faces.partial.render";
    myfaces._impl.core._jsfImpl._PROP_EVENT = "javax.faces.partial.event";

    /* message types */
    myfaces._impl.core._jsfImpl._MSG_TYPE_ERROR = "error";
    myfaces._impl.core._jsfImpl._MSG_TYPE_EVENT = "event";

    /* event emitting stages */
    myfaces._impl.core._jsfImpl._AJAX_STAGE_BEGIN = "begin";
    myfaces._impl.core._jsfImpl._AJAX_STAGE_COMPLETE = "complete";
    myfaces._impl.core._jsfImpl._AJAX_STAGE_SUCCESS = "success";

    /*ajax errors spec 14.4.2*/
    myfaces._impl.core._jsfImpl._ERROR_HTTPERROR = "httpError";
    myfaces._impl.core._jsfImpl._ERROR_EMPTY_RESPONSE = "emptyResponse";
    myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML = "malformedXML";
    myfaces._impl.core._jsfImpl._ERROR_SERVER_ERROR = "serverError";
    myfaces._impl.core._jsfImpl._ERROR_CLIENT_ERROR = "clientError";

    /**
     * collect and encode data for a given form element (must be of type form)
     * find the javax.faces.ViewState element and encode its value as well!
     * return a concatenated string of the encoded values!
     *
     * @throws an exception in case of the given element not being of type form!
     * https://issues.apache.org/jira/browse/MYFACES-2110
     */
    myfaces._impl.core._jsfImpl.prototype.getViewState = function(formElement) {
        /**
         *  typecheck assert!, we opt for strong typing here
         *  because it makes it easier to detect bugs
         */

        if ('undefined' == typeof(formElement)
                || null == formElement
                || 'undefined' == typeof(formElement.nodeName)
                || null == formElement.nodeName
                || formElement.nodeName.toLowerCase() != "form") {
            throw Exception("jsf.viewState: param value not of type form!");
        }
        return this._requestHandler.getViewState(formElement);
    };

    /**
     * internal assertion check for the element parameter
     * it cannot be null or undefined
     * it must be either a string or a valid existing dom node
     */
    myfaces._impl.core._jsfImpl.prototype._assertElement = function(/*String|Dom Node*/ element) {
        /*namespace remap for our local function context we mix the entire function namespace into
         *a local function variable so that we do not have to write the entire namespace
         *all the time
         **/
        var JSF2Utils = myfaces._impl._util._LangUtils;

        /**
         * assert element
         */
        if ('undefined' == typeof( element ) || null == element) {
            throw new Exception("jsf.ajax, element must be set!");
        }
        //        if (!JSF2Utils.isString(element) && !(element instanceof Node)) {
        //            throw new Exception("jsf.ajax, element either must be a string or a dom node");
        //        }

        element = JSF2Utils.byId(element);
        if ('undefined' == typeof element || null == element) {
            throw new Exception("Element either must be a string to a or must be a valid dom node");
        }

    };

    myfaces._impl.core._jsfImpl.prototype._assertFunction = function(func) {
        if ('undefined' == typeof func || null == func) {
            return;
        }
        if (!(func instanceof Function)) {
            throw new Exception("Functioncall " + func + " is not a function! ");
        }
    }

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
     * @param {String|Node} element: any dom element no matter being it html or jsf, from which the event is emitted
     * @param {|Event|} event: any javascript event supported by that object
     * @param {|Map|} options : map of options being pushed into the ajax cycle
     */
    myfaces._impl.core._jsfImpl.prototype.request = function(element, event, options) {

        /*namespace remap for our local function context we mix the entire function namespace into
         *a local function variable so that we do not have to write the entire namespace
         *all the time
         **/
        var JSF2Utils = myfaces._impl._util._LangUtils;

        /**
         * we cross reference statically hence the mapping here
         * the entire mapping between the functions is stateless
         */

        /*assert a valid structure of a given element*/
        this._assertElement(element);
        /*assert if the onerror is set and once if it is set it must be of type function*/
        this._assertFunction(options.onerror);
        /*assert if the onevent is set and once if it is set it must be of type function*/
        this._assertFunction(options.onevent);

        /*
         * We make a copy of our options because
         * we should not touch the incoming params!
         */
        var passThroughArguments = JSF2Utils.mixMaps({}, options, true);

        /*additional passthrough cleanup*/
        /*ie6 supportive code to prevent browser leaks*/
        passThroughArguments.onevent = null;
        delete passThroughArguments.onevent;
        /*ie6 supportive code to prevent browser leaks*/
        passThroughArguments.onerror = null;
        delete passThroughArguments.onerror;

        if ('undefined' != typeof event && null != event) {
            passThroughArguments[myfaces._impl.core._jsfImpl._PROP_EVENT] = event.type;
        }

        /**
         * ajax pass through context with the source
         * onevent and onerror
         */
        var ajaxContext = {};
        ajaxContext.source = element;
        ajaxContext.onevent = options.onevent;
        ajaxContext.onerror = options.onerror;

        /**
         * fetch the parent form
         */
        var sourceForm = myfaces._impl._util._Utils.getParent(null, ajaxContext, element, "form");

        if ('undefined' == typeof sourceForm || null == sourceForm) {
            sourceForm = document.forms[0];
        }

        /**
         * binding contract the javax.faces.source must be set
         */
        passThroughArguments[myfaces._impl.core._jsfImpl._PROP_PARTIAL_SOURCE] = element.id;

        /**
         * javax.faces.partial.ajax must be set to true
         */
        passThroughArguments[myfaces._impl.core._jsfImpl._PROP_AJAX] = true;

        /**
         * if execute or render exist
         * we have to pass them down as a blank delimited string representation
         * of an array of ids!
         */
        if (JSF2Utils.exists(passThroughArguments, "execute")) {
            /*the options must be a blank delimited list of strings*/
            var execString = JSF2Utils.arrayToString(passThroughArguments.execute, ' ');
            var execNone = execString.indexOf(this._OPT_IDENT_NONE) != -1;
            var execAll = execString.indexOf(this._OPT_IDENT_ALL) != -1;
            if (!execNone && !execAll) {
                execString = execString.replace(this._OPT_IDENT_FORM, sourceForm.id);
                execString = execString.replace(this._OPT_IDENT_THIS, element.id);

                passThroughArguments[myfaces._impl.core._jsfImpl._PROP_EXECUTE] = execString;
            } else if (execAll) {
                passThroughArguments[myfaces._impl.core._jsfImpl._PROP_EXECUTE] = this._OPT_IDENT_ALL;
            }

            passThroughArguments.execute = null;
            /*remap just in case we have a valid pointer to an existing object*/
            delete passThroughArguments.execute;
        } else {
            passThroughArguments[myfaces._impl.core._jsfImpl._PROP_EXECUTE] = element.id;
        }

        if (JSF2Utils.exists(passThroughArguments, "render")) {
            var renderString = JSF2Utils.arrayToString(passThroughArguments.render, ' ');
            var renderNone = renderString.indexOf(this._OPT_IDENT_NONE) != -1;
            var renderAll = renderString.indexOf(this._OPT_IDENT_ALL) != -1;
            if (!renderNone && !renderAll) {
                renderString = renderString.replace(this._OPT_IDENT_FORM, sourceForm.id);
                renderString = renderString.replace(this._OPT_IDENT_THIS, element.id);
                passThroughArguments[myfaces._impl.core._jsfImpl._PROP_RENDER] = JSF2Utils.arrayToString(passThroughArguments.render, ' ');
                passThroughArguments.render = null;
            } else if (renderAll) {
                passThroughArguments[myfaces._impl.core._jsfImpl._PROP_RENDER] = this._OPT_IDENT_ALL;

            }
            delete passThroughArguments.render;
        }

        //implementation specific options are added to the context for further processing
        if ('undefined' != typeof passThroughArguments.myfaces && null != passThroughArguments.myfaces) {
            ajaxContext.myfaces = passThroughArguments.myfaces;
            delete passThroughArguments.myfaces;
        }

        this._requestHandler._ajaxRequest(element, sourceForm, ajaxContext, passThroughArguments);

    };

    myfaces._impl.core._jsfImpl.prototype.addOnError = function(/*function*/errorListener) {
        /*error handling already done in the assert of the queue*/
        this._errorListenerQueue.add(errorListener);
    }

    myfaces._impl.core._jsfImpl.prototype.addOnEvent = function(/*function*/eventListener) {
        /*error handling already done in the assert of the queue*/
        this._eventListenerQueue.add(eventListener);
    }

    /**
     * implementation triggering the error chain
     *
     * @param {Object} request the request object which comes from the xhr cycle
     * @param {Map} context the context object being pushed over the xhr cycle keeping additional metadata �
     * @param {String} name, the error name
     * @param {String} serverErrorName the server error name in case of a server error
     * @param {String} serverErrorMessage the server error message in case of a server error
     *
     *  handles the errors, in case of an onError exists within the context the onError is called as local error handler
     *  the registered error handlers in the queue receiv an error message to be dealt with
     *  and if the projectStage is at development an alert box is displayed
     *
     *  note: we have additonal functionality here, via the global config myfaces.config.defaultErrorOutput a function can be provieded
     *  which changes the default output behavior from alert to something else
     *
     *
     */
    myfaces._impl.core._jsfImpl.prototype.sendError = function sendError(/*Object*/request, /*Object*/ context, /*String*/ name, /*String*/ serverErrorName, /*String*/ serverErrorMessage) {
        var eventData = {};
        eventData.type = myfaces._impl.core._jsfImpl._MSG_TYPE_ERROR;

        eventData.name = name;
        eventData.serverErrorName = serverErrorName;
        eventData.serverErrorMessage = serverErrorMessage;

        try {
            eventData.source = context.source;
            eventData.responseXML = request.responseXML;
            eventData.responseText = request.responseText;
            eventData.responseCode = request.status;
        } catch (e) {
            // silently ignore: user can find out by examining the event data
        }

        /**/
        if (myfaces._impl._util._LangUtils.exists(context, "onerror")) {
            context.onerror(eventData);
        }

        /*now we serve the queue as well*/
        this._errorListenerQueue.broadcastEvent(eventData);

        if (jsf.getProjectStage() === "Development") {
            var defaultErrorOutput = myfaces._impl._util._Utils.getGlobalConfig("defaultErrorOutput", alert);
            var finalMessage = [];

            finalMessage.push(('undefined' != typeof name && null != name) ? name : "");
            finalMessage.push(('undefined' != typeof serverErrorName && null != serverErrorName) ? serverErrorName : "");
            finalMessage.push(('undefined' != typeof serverErrorMessage && null != serverErrorMessage) ? serverErrorMessage : "");

            defaultErrorOutput(finalMessage.join("-"));
        }
    };

    /**
     * sends an event
     */
    myfaces._impl.core._jsfImpl.prototype.sendEvent = function sendEvent(/*Object*/request, /*Object*/ context, /*event name*/ name) {
        var eventData = {};
        eventData.type = myfaces._impl.core._jsfImpl._MSG_TYPE_EVENT;

        eventData.name = name;
        eventData.source = context.source;

        if (name !== myfaces._impl.core._jsfImpl._AJAX_STAGE_BEGIN) {

            try {
                eventData.responseXML = request.responseXML;
                eventData.responseText = request.responseText;
                eventData.responseCode = request.status;
            } catch (e) {
                myfaces.ajax.sendError(request, context, myfaces._impl.core._jsfImpl._ERROR_CLIENT_ERROR, "ErrorRetrievingResponse",
                        "Parts of the response couldn't be retrieved when constructing the event data: " + e);
                //client errors are not swallowed 
                throw e;
            }

        }

        /**/
        if (myfaces._impl._util._LangUtils.exists(context, "onevent")) {
            /*calling null to preserve the original scope*/
            context.onevent.call(null, eventData);
        }

        /*now we serve the queue as well*/
        this._eventListenerQueue.broadcastEvent(eventData);
    }

    /**
     * processes the ajax response if the ajax request completes successfully
     * @param {xhrRequest} request the ajax request!
     * @param {Map} context, context map keeping context data not being passed down over
     * the request boundary but kept on the client
     */
    myfaces._impl.core._jsfImpl.prototype.response = function(request, context) {
        this._requestHandler._ajaxResponse(request, context);
    };

    /**
     * @return the project stage also emitted by the server:
     * it cannot be cached and must be delivered over the server
     *
     */
    myfaces._impl.core._jsfImpl.prototype.getProjectStage = function() {
        return "#{facesContext.application.projectStage}";
    };

    /**
     * implementation of the external chain function
     * moved into the impl
     *
     *  @param {Object} source the source which also becomes
     * the scope for the calling function (unspecified sidebehavior)
     * the spec states here that the source can be any arbitrary code block.
     * Which means it either is a javascript function directly passed or a codeblock
     * which has to be evaluated separately.
     *
     * Aftert revisiting the code additional testing against components showed that
     * the this parameter is only targetted at the component triggering the eval
     * (event) if a string codeblock is passed. This is behavior we have to resemble
     * in our function here as well, I guess.
     *
     * @param {Event} event the event object being passed down into the the chain as event origin
     */
    myfaces._impl.core._jsfImpl.prototype.chain = function(source, event) {
        var len = arguments.length;
        if (len < 3) return;
        //now we fetch from what is given from the parameter list
        //we cannot work with splice here in any performant way so we do it the hard way
        //arguments only are give if not set to undefined even null values!

        //assertions source either null or set as dom element:
        //assertion 2 event either null or cannot be a function or string
        //assertion 3 source and ev

        if ('undefined' == typeof source) {
            throw new Error(" source must be defined");
        //allowed chain datatypes
        } else if ('function' == typeof source) {
            throw new Error(" source cannot be a function (probably source and event were not defined or set to null");
        } if (myfaces._impl._util._LangUtils.isString(source)) {
            throw new Error(" source cannot be a string ");
        }

        if ('undefined' == typeof event) {
            throw new Error(" event must be defined or null");
        } else if ('function' == typeof event) {
            throw new Error(" event cannot be a function (probably source and event were not defined or set to null");
        } else if (myfaces._impl._util._LangUtils.isString(event)) {
                throw new Error(" event cannot be a string ");
        }

        var thisVal = source;
        var eventParam = event;

        for (var loop = 2; loop < len; loop++) {
            //we do not change the scope of the incoming functions
            //but we reuse the argument array capabilities of apply
            var retVal = false;

            /*
             * Ok I have revisted this part again, the blackboxed ri test reveals:
             *
             <h:outputScript name = "jsf.js" library = "javax.faces" target = "head" />
             <script type="text/javascript">
             function pressMe(event) {
             alert(this);
             return true;
             }
             function chainMe(origin, event) {
             jsf.util.chain(origin, event, "alert('hello world'); return true;", pressMe);
             }

             </script>

             <div onclick="chainMe(this, event);">
             press me
             </div>

             that the RI can only handle stringed scripts we can handle functions and scripts
             I will contact the members of the EG on what the correct behavior is
             *
             * Arbitrary code block in my opinon means that we have to deal with both functions
             * and evaled strings
             */
            if ('function' == typeof arguments[loop]) {
                retVal = arguments[loop].call(thisVal, eventParam);
            } else {
                //either a function or a string can be passed in case of a string we have to wrap it into another functon
                retVal = new Function("event", arguments[loop]).call(thisVal, eventParam);
            }
            //now if one function returns false in between we stop the execution of the cycle
            //here
            if ('undefined' != typeof retVal && retVal === false) return;
        }

    }

    // singleton
    myfaces.ajax = new myfaces._impl.core._jsfImpl();
}
