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

_reserveMyfaces();

if (!myfaces._JSF2Utils.exists(myfaces, "_jsfImpl")) {

    myfaces._jsfImpl = function() {

        /*Transports including queues and adapters!*/
        this._requestHandler = ('undefined' != typeof (myfaces._SimpleXHRFrameworkAdapter)) ? new myfaces._SimpleXHRFrameworkAdapter() : new myfaces._TrinidadFrameworkAdapter();

        /*Response handler to isolate the ppr specific parts*/
        //myfaces._jsfImpl.prototype._responseHandler = new myfaces._ResponseHandler();

        /**
         * external event listener queue!
         */
        this._eventListenerQueue = new myfaces._ListenerQueue();

        /**
         * external error listener queue!
         */
        this._errorListenerQueue = new myfaces._ListenerQueue();

    };

    /*CONSTANTS*/

    myfaces._jsfImpl.prototype._PROP_PARTIAL_SOURCE = "javax.faces.partial.source";
    myfaces._jsfImpl.prototype._PROP_VIEWSTATE = "javax.faces.viewState";
    myfaces._jsfImpl.prototype._PROP_AJAX = "javax.faces.partial.ajax";
    myfaces._jsfImpl.prototype._PROP_EXECUTE = "javax.faces.partial.execute";
    myfaces._jsfImpl.prototype._PROP_RENDER = "javax.faces.partial.render";
    myfaces._jsfImpl.prototype._PROP_EVENT = "javax.faces.partial.event";

    /*internal identifiers for options*/
    myfaces._jsfImpl.prototype._OPT_IDENT_ALL = "@all";
    myfaces._jsfImpl.prototype._OPT_IDENT_NONE = "@none";
    myfaces._jsfImpl.prototype._OPT_IDENT_THIS = "@this";
    myfaces._jsfImpl.prototype._OPT_IDENT_FORM = "@form";

    /*partial response types*/
    myfaces._jsfImpl.prototype._RESPONSE_PARTIAL = "partial-response";
    myfaces._jsfImpl.prototype._RESPONSETYPE_ERROR = "error";
    myfaces._jsfImpl.prototype._RESPONSETYPE_REDIRECT = "redirect";
    myfaces._jsfImpl.prototype._RESPONSETYPE_REDIRECT = "changes";

    /*partial commands*/
    myfaces._jsfImpl.prototype._PCMD_UPDATE = "update";
    myfaces._jsfImpl.prototype._PCMD_DELETE = "delete";
    myfaces._jsfImpl.prototype._PCMD_INSERT = "insert";
    myfaces._jsfImpl.prototype._PCMD_EVAL = "eval";
    myfaces._jsfImpl.prototype._PCMD_ATTRIBUTES = "attributes";
    myfaces._jsfImpl.prototype._PCMD_EXTENSION = "extension";

    /*various errors within the rendering stage*/
    myfaces._jsfImpl.prototype._ERROR_EMPTY_RESPONSE = "emptyResponse";
    myfaces._jsfImpl.prototype._ERROR_MALFORMEDXML = "malformedXML";
    myfaces._jsfImpl.prototype._MSG_SUCCESS = "success";

    /*various ajax message types*/
    myfaces._jsfImpl.prototype._MSG_TYPE_ERROR = "error";
    myfaces._jsfImpl.prototype._MSG_TYPE_EVENT = "event";
    myfaces._jsfImpl.prototype._AJAX_STAGE_BEGIN = "begin";
    myfaces._jsfImpl.prototype._AJAX_STAGE_COMPLETE = "complete";
    myfaces._jsfImpl.prototype._AJAX_STAGE_HTTPERROR = "httpError";

    /**
     * collect and encode data for a given form element (must be of type form)
     * find the javax.faces.ViewState element and encode its value as well!
     * return a concatenated string of the encoded values!
     *
     * @throws an exception in case of the given element not being of type form!
     * https://issues.apache.org/jira/browse/MYFACES-2110
     */
    myfaces._jsfImpl.prototype.getViewState = function(formElement) {
        /**
         *  typecheck assert!, we opt for strong typing here
         *  because it makes it easier to detect bugs
         */

        if ('undefined' == typeof(formElement)
                || null == formElement
                || 'undefined' == typeof(formElement.nodeName)
                || null == formElement.nodeName
                || formElement.nodeName != "FORM") {
            throw Exception("jsf.viewState: param value not of type form!");
        }
        var formValues = {};

        formValues[this._PROP_VIEWSTATE] = document.getElementById(this._PROP_VIEWSTATE).value;
        formValues = myfaces._JSF2Utils.getPostbackContent(formElement, formValues);
        return formValues;
    };

    /**
     * internal assertion check for the element parameter
     * it cannot be null or undefined
     * it must be either a string or a valid existing dom node
     */
    myfaces._jsfImpl.prototype._assertElement = function(/*String|Dom Node*/ element) {
        /*namespace remap for our local function context we mix the entire function namespace into
         *a local function variable so that we do not have to write the entire namespace
         *all the time
         **/
        var JSF2Utils = myfaces._JSF2Utils;

        /**
         * assert element
         */
        if ('undefined' == typeof( element ) || null == element) {
            throw new Exception("jsf.ajax, element must be set!");
        }
        if (!JSF2Utils.isString(element) && !(element instanceof Node)) {
            throw new Exception("jsf.ajax, element either must be a string or a dom node");
        }

        element = JSF2Utils.byId(element);
        if ('undefined' == typeof element || null == element) {
            throw new Exception("Element either must be a string to a or must be a valid dom node");
        }

    };

    myfaces._jsfImpl.prototype._assertFunction = function(/*Objec*/ obj, /*String*/ functionName) {
        if ('undefined' == typeof(obj) || null == obj) return;
        var func = obj[functionName];
        if ('undefined' == typeof func || null == func) {
            return;
        }
        if (!(func instanceof Function)) {
            throw new Exception("Functioncall " + func + " is not a function! ");
        }
    }

    /**
     * Captures the event arguments according to the list in the specification
     */
    myfaces._jsfImpl.prototype._caputureEventArgs = function(/*Dom node*/node, /*event*/ obj) {
        /*
         * TODO encode the rest of the arguments
         * once it is clear what has to be done here
         */
        var retVal = {};
        return retVal;
    };

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
     * @param element: any dom element no matter being it html or jsf, from which the event is emitted
     * @param event: any javascript event supported by that object
     * @param options : map of options being pushed into the ajax cycle
     */
    myfaces._jsfImpl.prototype.request = function(/*String|Dom Node*/ element, /*|EVENT|*/ event, /*{|OPTIONS|}*/ options) {

        /*namespace remap for our local function context we mix the entire function namespace into
         *a local function variable so that we do not have to write the entire namespace
         *all the time
         **/
        var JSF2Utils = myfaces._JSF2Utils;

        /**
         * we cross reference statically hence the mapping here
         * the entire mapping between the functions is stateless
         */

        /*assert a valid structure of a given element*/
        this._assertElement(element);
        /*assert if the onerror is set and once if it is set it must be of type function*/
        this._assertFunction(options, "onerror");
        /*assert if the onevent is set and once if it is set it must be of type function*/
        this._assertFunction(options, "onevent");

        /**
         * fetch the parent form first
         */
        var sourceForm = JSF2Utils.getParentForm(element);

        if ('undefined' == typeof sourceForm || null == sourceForm) {
            sourceForm = document.forms[0];
        }

        /*
         * We make a copy of our options because
         * we should not touch the incoming params!
         */
        var passThroughArguments = JSF2Utils.mixMaps({}, options, true);
        var viewState = jsf.ajax.getViewState(sourceForm);

        /*
         * either we have a valid form element or an element then we have to pass the form
         * if it is a dummy element we have to pass it down as additional parameter
         * the same applies to non form elements which cannot hold values
         * but have ids!
         */
        var sourceElement = JSF2Utils.byId(element);

        /*
         * binding contract the javax.faces.partial.source must be
         * set according to the december 2008 preview
         */
        passThroughArguments[this._PROP_PARTIAL_SOURCE] = sourceElement.id;

        /*
         * we pass down the name and value of the source element
         * seems to be removed on the ri side
         * the have it in our viewstate values anyway
         * and also in the exectute if no other values are set!
         */
        //passThroughArguments[sourceElement.name || sourceElement.id] = sourceElement.value || 'x';

        /*
         * javax.faces.partial.ajax must be set to true
         */
        passThroughArguments[this._PROP_AJAX] = true;

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
                execString = execString.replace(this._OPT_IDENT_THIS, sourceElement.id);

                passThroughArguments[this._PROP_EXECUTE] = execString;
            } else if (execAll) {
                passThroughArguments[this._PROP_EXECUTE] = this._OPT_IDENT_ALL;
            }

            passThroughArguments.execute = null;
            /*remap just in case we have a valid pointer to an existing object*/
            delete passThroughArguments.execute;
        } else {
            passThroughArguments[this._PROP_EXECUTE] = sourceElement.id;
        }
        if (JSF2Utils.exists(passThroughArguments, "render")) {
            var renderString = JSF2Utils.arrayToString(passThroughArguments.render, ' ');
            var renderNone = renderString.indexOf(this._OPT_IDENT_NONE) != -1;
            var renderAll = renderString.indexOf(this._OPT_IDENT_ALL) != -1;
            if (!renderNone && !renderAll) {
                renderString = renderString.replace(this._OPT_IDENT_FORM, sourceForm.id);
                renderString = renderString.replace(this._OPT_IDENT_THIS, sourceElement.id);
                passThroughArguments[this._PROP_RENDER] = JSF2Utils.arrayToString(passThroughArguments.render, ' ');
                passThroughArguments.render = null;
            } else if (renderAll) {
                passThroughArguments[this._PROP_RENDER] = this._OPT_IDENT_ALL;

            }
            delete passThroughArguments.render;
        } else {
            passThroughArguments[this._PROP_RENDER] = sourceElement.id;
        }

        /*additional passthrough cleanup*/
        /*ie6 supportive code to prevent browser leaks*/
        passThroughArguments.onevent = null;
        delete passThroughArguments.onevent;
        /*ie6 supportive code to prevent browser leaks*/
        passThroughArguments.onerror = null;
        delete passThroughArguments.onevent;

        var extractedEventArguments = this._caputureEventArgs(element, event);

        if ('undefined' != typeof event && null != event) {
            passThroughArguments[this._PROP_EVENT] = event.type;
        }

        /*we mixin the event params but do not override existing ones!*/

        passThroughArguments = JSF2Utils.mixMaps(passThroughArguments, extractedEventArguments, false);

        /**
         * ajax pass through context with the source
         * onevent and onerror
         */
        var ajaxContext = {};
        ajaxContext.source = element;
        ajaxContext.onevent = options.onevent;
        ajaxContext.onerror = options.onerror;

        /**
         * we now use the trinidad request queue to send down the ajax request
         */
        this._requestHandler.sendRequest(ajaxContext, sourceForm.action, viewState, passThroughArguments);

        /*
         * TODO #61
         * https://issues.apache.org/jira/browse/MYFACES-2112
         * done
         */
    };

    myfaces._jsfImpl.prototype.addOnError = function(/*function*/errorListener) {
        /*error handling already done in the assert of the queue*/
        this._errorListenerQueue.add(errorListener);
    }

    myfaces._jsfImpl.prototype.addOnEvent = function(/*function*/eventListener) {
        /*error handling already done in the assert of the queue*/
        this._eventListenerQueue.add(eventListener);
    }

    /**
     * RI compatibility method
     * TODO make sure this method also occurrs in the specs
     * otherwise simply pull it
     */
    myfaces._jsfImpl.prototype.sendError = function sendError(/*Object*/request, /*Object*/ context, /*String*/ name, /*String*/ serverErrorName, /*String*/ serverErrorMessage) {
        var eventData = {};
        eventData.type = this._MSG_TYPE_ERROR;

        eventData.name = name;
        eventData.source = context.source;
        eventData.responseXML = request.responseXML;
        eventData.responseText = request.responseText;
        eventData.responseCode = request.status;

        /**/
        if (myfaces._JSF2Utils.exists(context, "onerror")) {
            context.onerror(eventData);
        }
        /*now we serve the queue as well*/
        this._errorListenerQueue.broadcastEvent(eventData);
    };

    /**
     * RI compatibility method
     * TODO make sure this method also occurrs in the specs
     * otherwise simply pull it
     */
    myfaces._jsfImpl.prototype.sendEvent = function sendEvent(/*Object*/request, /*Object*/ context, /*even name*/ name) {
        var eventData = {};
        eventData.type = this._MSG_TYPE_EVENT;

        eventData.name = name;
        eventData.source = context.source;
        if (name !== this._AJAX_STAGE_BEGIN) {
            eventData.responseXML = request.responseXML;
            eventData.responseText = request.responseText;
            eventData.responseCode = request.status;
        }

        /**/
        if (myfaces._JSF2Utils.exists(context, "onevent")) {
            /*calling null to preserve the original scope*/
            context.onevent.call(null, eventData);
        }
        /*now we serve the queue as well*/
        this._eventListenerQueue.broadcastEvent(eventData);
    }

    /**
     * processes the ajax response if the ajax request completes successfully
     * @param request the ajax request!
     */
    myfaces._jsfImpl.prototype.response = function(/*xhr request object*/request, context) {
        if ('undefined' == typeof(request) || null == request) {
            throw Exception("jsf.ajaxResponse: The response cannot be null or empty!");
        }

        if (!myfaces._JSF2Utils.exists(request, "responseXML")) {
            this.sendError(request, context, this._ERROR_EMPTY_RESPONSE);

            return;
        }

        var xmlContent = request.responseXML;
        if (xmlContent.firstChild.tagName == "parsererror") {
            this.sendError(request, context, this._ERROR_MALFORMEDXML);
            return;
        }

        var partials = xmlContent.getElementsByTagName(this._RESPONSE_PARTIAL);
        if ('undefined' == typeof partials || partials == null || partials.length != 1) {
            this.sendError(request, context, this._ERROR_MALFORMEDXML);
            return;
        }

        var partialXmlData = partials[0];
        var childNodesLength = partialXmlData.childNodes.length;

        for (var loop = 0; loop < childNodesLength; loop++) {
            var childNode = partialXmlData.childNodes[loop];
            var nodeName = childNode.nodeName.toLowerCase();

            if (nodeName == this._PCMD_EVAL) {
                // this._responseHandler.doEval(childNode);
            } else if (nodeName == this._PCMD_UPDATE) {
                //  this._responseHandler.doUpdate(childNode);
            } else if (nodeName == this._PCMD_INSERT) {
                //  this._responseHandler.doInsert(childNode);
            } else if (nodeName == this._PCMD_DELETE) {
                // this._responseHandler.doDelete(childNode);
            } else if (nodeName == this._PCMD_ATTRIBUTES) {
                // this._responseHandler.doAtttributes(childNode);
            } else if (nodeName == this._PCMD_EXTENSION) {
                //  this._responseHandler.doExtension(childNode);
            } else {
                this.sendError(request, context, this._ERROR_MALFORMEDXML);
                return;
            }
        }
        this.sendEvent(request, context, this._MSG_SUCCESS);

        /**
         * TODO #62
         * https://issues.apache.org/jira/browse/MYFACES-2114
         */
    };
    /**
     * @return the current project state emitted by the server side method:
     * javax.faces.application.Application.getProjectStage()
     */
    myfaces._jsfImpl.prototype.getProjectStage = function() {

        if ('undefined' == typeof(this._projectStage) ||
            null == this._projectStage) {
            //TODO add small templating capabilities to our resource loader
            //so that the project stage is loaded on the fly!
            //or solve it with a separate xmlhttprequest
        }

        /**
         * TODO #62
         * https://issues.apache.org/jira/browse/MYFACES-2115
         */
        return this._projectStage;
    };

}
