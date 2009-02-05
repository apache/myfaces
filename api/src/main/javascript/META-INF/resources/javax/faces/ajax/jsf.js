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

/**
 *MyFaces core javascripting libraries
 *
 *  Those are the central public functions in the JSF2
 *  Ajax API! They handle the entire form submit and ajax send
 *  and resolve cycle!
 *
 *
 *  TODO isolate the entire Trinidad part in a neutral
 *  framework adapter class so that we can switch
 *  transport implementations on the fly without
 *  touching the core code (so that we might be able
 *  to check for an existing transport and hook into that one
 *  to keep the javascript size down)
 */

/**
 *
 * reserve the root namespace
 */
if ('undefined' != typeof OpenAjax && ('undefined' == typeof jsf || null == typeof jsf)) {
    OpenAjax.hub.registerLibrary("jsf", "www.sun.com", "1.0", null);
}
//just in case openajax has failed (testing environment)
//under normal circumstances this should not happen
if ('undefined' == typeof jsf || null == jsf) {
    jsf = new Object();
}

if ('undefined' == typeof jsf.ajax || null == jsf.ajax) {
    jsf.ajax = new Object();
}

/*CONSTANTS*/

jsf.ajax._PROP_PARTIAL_SOURCE = "javax.faces.partial.source";
jsf.ajax._PROP_VIEWSTATE = "javax.faces.viewState";
jsf.ajax._PROP_AJAX = "javax.faces.partial.ajax";
jsf.ajax._PROP_EXECUTE = "javax.faces.partial.execute";
jsf.ajax._PROP_RENDER = "javax.faces.partial.render";


jsf.ajax._MSG_TYPE_ERROR = "error";
jsf.ajax._MSG_TYPE_EVENT = "event";
jsf.ajax._AJAX_STAGE_BEGIN = "begin";
jsf.ajax._AJAX_STAGE_COMPLETE = "complete";
jsf.ajax._AJAX_STAGE_HTTPERROR = "httpError";

/*Event queues*/
jsf.ajax._requestQueue = new myfaces._TrRequestQueue();

/**
 * external event listener queue!
 */
jsf.ajax._eventListenerQueue = new myfaces._ListenerQueue();

/**
 * external error listener queue!
 */
jsf.ajax._errorListenerQueue = new myfaces._ListenerQueue();

/**
 * collect and encode data for a given form element (must be of type form)
 * find the javax.faces.ViewState element and encode its value as well!
 * return a concatenated string of the encoded values!
 *
 * @throws an exception in case of the given element not being of type form!
 * https://issues.apache.org/jira/browse/MYFACES-2110
 */
jsf.getViewState = function(formElement) {
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

    formValues[jsf.ajax._PROP_VIEWSTATE] = document.getElementById(jsf.ajax._PROP_VIEWSTATE).value;
    formValues = myfaces._JSF2Utils.getPostbackContent(formElement, formValues);
    return formValues;
};


/**
 * internal assertion check for the element parameter
 * it cannot be null or undefined
 * it must be either a string or a valid existing dom node
 */
jsf.ajax._assertElement = function(/*String|Dom Node*/ element) {
    /*namespace remap for our local function context we mix the entire function namespace into
     *a local function variable so that we do not have to write the entire namespace
     *all the time
     **/
    var JSF2Utils   = myfaces._JSF2Utils;



    /**
     * assert element
     */
    if('undefined' == typeof( element ) || null == element) {
        throw new Exception("jsf.ajax, element must be set!");
    }
    if(!JSF2Utils.isString(element) && Node != typeof element) {
        throw new Exception("jsf.ajax, element either must be a string or a dom node");
    }

    element = JSF2Utils.byId(element);
    if('undefined' == typeof element || null == element) {
        throw new Exception("Element either must be a string to a or must be a valid dom node");
    }

};


jsf.ajax._assertFunction = function(/*Objec*/ obj, /*String*/ functionName) {
    if('undefined' == typeof(obj) || null == obj) return;
    var func = obj[functionName];
    if('undefined' == typeof func || null == func) {
        return;
    }
    if(!(func instanceof Function)) {
        throw new Exception("Functioncall "+func+" is not a function! ");
    }
}

/**
 * Captures the event arguments according to the list in the specification
 */
jsf.ajax._caputureEventArgs = function(/*Dom node*/node, /*event*/ obj) {
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
jsf.ajax.request = function(/*String|Dom Node*/ element, /*|EVENT|*/ event, /*{|OPTIONS|}*/ options) {

    /*namespace remap for our local function context we mix the entire function namespace into
     *a local function variable so that we do not have to write the entire namespace
     *all the time
     **/
    var JSF2Utils   = myfaces._JSF2Utils;

    /**
     * we cross reference statically hence the mapping here
     * the entire mapping between the functions is stateless
     */
    var JSFAjax     = jsf.ajax;

    /*assert a valid structure of a given element*/
    JSFAjax._assertElement(element);
    /*assert if the onerror is set and once if it is set it must be of type function*/
    JSFAjax._assertFunction(options,"onerror");
    /*assert if the onevent is set and once if it is set it must be of type function*/
    JSFAjax._assertFunction(options,"onevent");

    /**
     * fetch the parent form first
     */
    var sourceForm  = JSF2Utils.getParentForm(element);

    if('undefined' == typeof sourceForm || null == sourceForm) {
        sourceForm = document.forms[0];
    }

    /*
     * We make a copy of our options because
     * we should not touch the incoming params!
     */
    var passThroughArguments = JSF2Utils.mixMaps({}, options, true);
    var viewState = jsf.getViewState(sourceForm);

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
    passThroughArguments[jsf.ajax._PROP_PARTIAL_SOURCE] = sourceElement.id;

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
    passThroughArguments[jsf.ajax._PROP_AJAX] = true;

    /**
     * if execute or render exist
     * we have to pass them down as a blank delimited string representation
     * of an array of ids!
     */
    if(JSF2Utils.exists(passThroughArguments,"execute")) {
        /*the options must be a blank delimited list of strings*/
        //TODO add the source id to the list
        passThroughArguments[jsf.ajax._PROP_EXECUTE] = JSF2Utils.arrayToString(passThroughArguments.execute,' ');
        passThroughArguments.execute = null;/*remap just in case we have a valid pointer to an existing object*/
        delete passThroughArguments.execute;
    } else {
        passThroughArguments[jsf.ajax._PROP_EXECUTE] = sourceElement.id;
    }
    if(JSF2Utils.exists(passThroughArguments,"render")) {
        //TODO add the source id to the list
        passThroughArguments[jsf.ajax._PROP_RENDER] = JSF2Utils.arrayToString(passThroughArguments.render, ' ');
        passThroughArguments.execute = null;
        delete passThroughArguments.execute;
    }

    /*additional passthrough cleanup*/
    /*ie6 supportive code to prevent browser leaks*/
    passThroughArguments.onevent = null;
    delete passThroughArguments.onevent;
    /*ie6 supportive code to prevent browser leaks*/
    passThroughArguments.onerror = null;
    delete passThroughArguments.onevent;



    var extractedEventArguments = JSFAjax._caputureEventArgs(element, event);

    /*we mixin the event params but do not override existing ones!*/
    passThroughArguments = JSF2Utils.mixMaps(passThroughArguments, extractedEventArguments, false);

    /*according to the specs we only deal with queued asynchronous events in our transport layer!*/

    //TODO encode all parameters including the viewstate

    //TODO we have to deal with the onerror
    //and onevent handlers with our own Trinidad derived ajax engine

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
    JSFAjax._requestQueue.sendRequest(ajaxContext, JSFAjax._trXhrCallback, sourceForm.action, viewState+"&"+JSF2Utils.getPostbackContentFromMap(passThroughArguments) );

/*
     * TODO #61
     * https://issues.apache.org/jira/browse/MYFACES-2112
     * done
     */
};

jsf.ajax.addOnError = function(/*function*/errorListener) {
    /*error handling already done in the assert of the queue*/
    jsf.ajax._errorListenerQueue.add(errorListener);
}

jsf.ajax.addOnEvent = function(/*function*/eventListener) {
    /*error handling already done in the assert of the queue*/
    jsf.ajax._eventListenerQueue.add(eventListener);
}


/**
 * RI compatibility method
 * TODO make sure this method also occurrs in the specs
 * otherwise simply pull it
 */
jsf.ajax.sendError = function sendError(/*Object*/request,/*Object*/ context,/*String*/ name,/*String*/ serverErrorName,/*String*/ serverErrorMessage) {
    var eventData = {};
    eventData.type = jsf.ajax._MSG_TYPE_ERROR;

    eventData.name = name;
    eventData.source = context.source;
    eventData.responseXML = request.responseXML;
    eventData.responseText = request.responseText;
    eventData.responseCode = request.status;

    /**/
    if(myfaces._JSF2Utils.exists(context, "onerror")) {
        context.onerror(eventData);
    }
    /*now we serve the queue as well*/
    jsf.ajax._errorListenerQueue.broadcastEvent(eventData);
};

/**
 * RI compatibility method
 * TODO make sure this method also occurrs in the specs
 * otherwise simply pull it
 */
jsf.ajax.sendEvent = function sendEvent(/*Object*/request,/*Object*/ context,/*even name*/ name) {
    var eventData = {};
    eventData.type = jsf.ajax._MSG_TYPE_EVENT;

    eventData.name = name;
    eventData.source = context.source;
    if (name !== jsf.ajax._AJAX_STAGE_BEGIN) {
        eventData.responseXML =  request.responseXML;
        eventData.responseText = request.responseText;
        eventData.responseCode = request.status;
    }

    /**/
    if(myfaces._JSF2Utils.exists(context, "onevent")) {
        /*calling null to preserve the original scope*/
        context.onevent.call(null, eventData);
    }
    /*now we serve the queue as well*/
    jsf.ajax._eventListenerQueue.broadcastEvent(eventData);
}



/**
 * Maps an internal Trinidad xmlhttprequest event
 * into one compatible with the events of the ri
 * this is done for compatibility purposes
 * since trinidad follows similar rules regarding
 * the events we just have to map the eventing accordingly
 *
 * note I am implementing this after reading the ri code
 *
 */
jsf.ajax._mapTrinidadToRIEvents = function(/*object*/ xhrContext,/*_TrXMLRequestEvent*/ event) {
    var complete = false;

    switch(event.getStatus()) {
        //TODO add mapping code here
        case myfaces._TrXMLRequestEvent.STATUS_QUEUED:
            break; /*we have to wait*/
        case myfaces._TrXMLRequestEvent.STATUS_SEND_BEFORE:
            jsf.ajax.sendEvent(null, xhrContext, jsf.ajax._AJAX_STAGE_BEGIN)
            break;;
        case myfaces._TrXMLRequestEvent.STATUS_SEND_AFTER:
            /*still waiting, we can add listeners later if it is allowed*/
            break;
        case myfaces._TrXMLRequestEvent.STATUS_COMPLETE:
            /**
            *here we can do our needed callbacks so
            *that the specification is satisfied
            **/
            complete = true;
            var responseStatusCode = event.getResponseStatusCode();
            if(200 <= responseStatusCode && 300 > responseStatusCode ) {
                jsf.ajax.sendEvent(event.getRequest(), xhrContext, jsf.ajax._AJAX_STAGE_COMPLETE);
            } else {
                jsf.ajax.sendEvent(event.getRequest(), xhrContext, jsf.ajax._AJAX_STAGE_COMPLETE);
                jsf.ajax.sendError(event.getRequest(), xhrContext, jsf.ajax._AJAX_STAGE_HTTPERROR);
            }
            break;
        default:
            break;
    }
    return complete;
};

/**
 * Internal Trinidad
 * JSF 2.0 callback compatibility handler
 * since we use trinidad as our transport we have to do it that way
 *
 * this function switches the context via the
 * xhr bindings
 * it is now under the context of
 * given by the calling function
 */
jsf.ajax._trXhrCallback = function(/*_TrXMLRequestEvent*/ event) {
    var context = {};
    /*to ease readability we switch the transferred context
     *params back into a real context map*/
    context.onerror = this.onerror;
    context.onevent = this.onevent;
    context.source = this.source;

    /**
     *generally every callback into this method must issue an event
     */
    var complete = jsf.ajax._mapTrinidadToRIEvents(context, event);

    /**
     * the standard incoming events are handled appropriately we now can deal with the response
     */
    if(complete) {
        jsf.ajax.ajaxResponse(event.getRequest());
    }
}
/**
 * processes the ajax response if the ajax request completes successfully
 * @param request the ajax request!
 */
jsf.ajax.ajaxResponse = function(/*xhr request object*/request) {
    if ('undefined' == typeof(request) || null == request) {
        throw Exception("jsf.ajaxResponse: The response cannot be null or empty!");
    }
//TODO handle the ppr part here
//check the specs on the format of the return xml to do the ppr as expected!


/**
     * TODO #62
     * https://issues.apache.org/jira/browse/MYFACES-2114
     */
};
/**
 * @return the current project state emitted by the server side method:
 * javax.faces.application.Application.getProjectStage()
 */
jsf.getProjectStage = function() {

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



