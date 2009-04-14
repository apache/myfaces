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

if (!myfaces._impl._util._LangUtils.exists(myfaces, "_jsfImpl")) {

    myfaces._impl.core._jsfImpl = function() {

        //third option myfaces._impl.xhrCoreAjax which will be the new core impl for now
        this._requestHandler = new myfaces._impl.xhrCore._Ajax();

        /**
         * external event listener queue!
         */
        this._eventListenerQueue = new myfaces._impl._util._ListenerQueue();

        /**
         * external error listener queue!
         */
        this._errorListenerQueue = new myfaces._impl._util._ListenerQueue();

    };

    /*CONSTANTS*/

    myfaces._impl.core._jsfImpl.prototype._PROP_PARTIAL_SOURCE = "javax.faces.partial.source";
    myfaces._impl.core._jsfImpl.prototype._PROP_VIEWSTATE = "javax.faces.viewState";
    myfaces._impl.core._jsfImpl.prototype._PROP_AJAX = "javax.faces.partial.ajax";
    myfaces._impl.core._jsfImpl.prototype._PROP_EXECUTE = "javax.faces.partial.execute";
    myfaces._impl.core._jsfImpl.prototype._PROP_RENDER = "javax.faces.partial.render";
    myfaces._impl.core._jsfImpl.prototype._PROP_EVENT = "javax.faces.partial.event";

    /*internal identifiers for options*/
    myfaces._impl.core._jsfImpl.prototype._OPT_IDENT_ALL = "@all";
    myfaces._impl.core._jsfImpl.prototype._OPT_IDENT_NONE = "@none";
    myfaces._impl.core._jsfImpl.prototype._OPT_IDENT_THIS = "@this";
    myfaces._impl.core._jsfImpl.prototype._OPT_IDENT_FORM = "@form";

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
        if (!JSF2Utils.isString(element) && !(element instanceof Node)) {
            throw new Exception("jsf.ajax, element either must be a string or a dom node");
        }

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
     * Captures the event arguments according to the list in the specification
     */
    myfaces._impl.core._jsfImpl.prototype._caputureEventArgs = function(/*Dom node*/node, /*event*/ obj) {
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
    myfaces._impl.core._jsfImpl.prototype.request = function(/*String|Dom Node*/ element, /*|EVENT|*/ event, /*{|OPTIONS|}*/ options) {

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

        /**
         * fetch the parent form first
         */
        var sourceForm = myfaces._impl._util._Utils.getParent(element, "form");

        if ('undefined' == typeof sourceForm || null == sourceForm) {
            sourceForm = document.forms[0];
        }

        /*
         * We make a copy of our options because
         * we should not touch the incoming params!
         */
        var passThroughArguments = JSF2Utils.mixMaps({}, options, true);

        /*
         * binding contract the javax.faces.partial.source must be
         * set according to the december 2008 preview
         */
        passThroughArguments[this._PROP_PARTIAL_SOURCE] = element.id;

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
                execString = execString.replace(this._OPT_IDENT_THIS, element.id);

                passThroughArguments[this._PROP_EXECUTE] = execString;
            } else if (execAll) {
                passThroughArguments[this._PROP_EXECUTE] = this._OPT_IDENT_ALL;
            }

            passThroughArguments.execute = null;
            /*remap just in case we have a valid pointer to an existing object*/
            delete passThroughArguments.execute;
        } else {
            passThroughArguments[this._PROP_EXECUTE] = element.id;
        }
        if (JSF2Utils.exists(passThroughArguments, "render")) {
            var renderString = JSF2Utils.arrayToString(passThroughArguments.render, ' ');
            var renderNone = renderString.indexOf(this._OPT_IDENT_NONE) != -1;
            var renderAll = renderString.indexOf(this._OPT_IDENT_ALL) != -1;
            if (!renderNone && !renderAll) {
                renderString = renderString.replace(this._OPT_IDENT_FORM, sourceForm.id);
                renderString = renderString.replace(this._OPT_IDENT_THIS, element.id);
                passThroughArguments[this._PROP_RENDER] = JSF2Utils.arrayToString(passThroughArguments.render, ' ');
                passThroughArguments.render = null;
            } else if (renderAll) {
                passThroughArguments[this._PROP_RENDER] = this._OPT_IDENT_ALL;

            }
            delete passThroughArguments.render;
        }

        /*additional passthrough cleanup*/
        /*ie6 supportive code to prevent browser leaks*/
        passThroughArguments.onevent = null;
        delete passThroughArguments.onevent;
        /*ie6 supportive code to prevent browser leaks*/
        passThroughArguments.onerror = null;
        delete passThroughArguments.onerror;

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

       //implementation specific options are added to the context for further processing
        if('undefined' != typeof passThroughArguments.myfaces && null != passThroughArguments.myfaces) {
            ajaxContext.myfaces = passThroughArguments.myfaces;
            delete passThroughArguments.myfaces;
        }
        this._requestHandler._ajaxRequest(element, sourceForm, ajaxContext, passThroughArguments);

        /*
         * TODO #61
         * https://issues.apache.org/jira/browse/MYFACES-2112
         * done
         */
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
     * RI compatibility method
     * TODO make sure this method also occurrs in the specs
     * otherwise simply pull it
     */
    myfaces._impl.core._jsfImpl.prototype.sendError = function sendError(/*Object*/request, /*Object*/ context, /*String*/ name, /*String*/ serverErrorName, /*String*/ serverErrorMessage) {
        var eventData = {};
        eventData.type = this._MSG_TYPE_ERROR;

        eventData.name = name;
        eventData.source = context.source;
        eventData.responseXML = request.responseXML;
        eventData.responseText = request.responseText;
        eventData.responseCode = request.status;

        /**/
        if (myfaces._impl._util._LangUtils.exists(context, "onerror")) {
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
    myfaces._impl.core._jsfImpl.prototype.sendEvent = function sendEvent(/*Object*/request, /*Object*/ context, /*even name*/ name) {
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
        /**
         * TODO #62
         * https://issues.apache.org/jira/browse/MYFACES-2114
         */
    };
    /**
     * @return the project stage also emitted by the server:
     * it cannot be cached and must be delivered over the server
     *
     */
    myfaces._impl.core._jsfImpl.prototype.getProjectStage = function() {

        return "#{facesContext.application.projectStage}";
        /**
         * TODO #62
         * https://issues.apache.org/jira/browse/MYFACES-2115
         */
    };


  /**
   * implementation of the external chain function
   * moved into the impl
   *
   * according to the ri the source will bhe the scope
   * for the functions the event will be the object passed
   * @param {Object} source the source which also becomes
   * the scope for the calling function (not sure if this is correct however
   * the RI does it that way!
   * @param {Event} event the event object being passed down into the
   */
   myfaces._impl.core._jsfImpl.prototype.chain = function(source, event) {
        var len = arguments.length;
        if(len < 3) return;
        //now we fetch from what is given from the parameter list
        //we cannot work with splice here in any performant way so we do it the hard way
        //arguments only are give if not set to undefined even null values!

        var thisVal = ('object' == typeof source ) ? source: null;
        var param   = ('undefined' != typeof event) ? event: null;

        for(loop = 2; loop < len; loop++) {
            //we do not change the scope of the incoming functions
            //but we reuse the argument array capabilities of apply
            var retVal = false;
            //The spec states arbitrary codeblock
            //the ri wraps everything into functions
            //we do it differently here,
            //
            //We wrap only if the block itself
            //is not a function! Should be compatible
            //to the ri, but saner in its usage because
            //it saves one intermediate step in most cases
            //not my personal design decision, I probably would
            //enforce functions only to keep the caller code clean,
            //oh well
            if('function' == typeof arguments[loop]) {    
                 retVal = arguments[loop].call(thisVal, param);
            } else {
                 retVal = (new Function("evt", arguments[loop])).call(thisVal, param);
            }
            //now if one function returns null in between we stop the execution of the cycle
            //here
            if('undefined' != typeof retVal && retVal === false) return;
        }
   };
    
    //for debugging purposes only remove before going into production
    //should be removed automatically by the build process!!!
    ;;var myfaces_JSFDebug = new myfaces._impl.core._jsfImpl();

}
