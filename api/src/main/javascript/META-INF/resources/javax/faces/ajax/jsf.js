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

jsf.ajax._requestQueue = new myfaces._TrRequestQueue();
/**
 * collect and encode data for a given form element (must be of type form)
 * find the javax.faces.ViewState element and encode its value as well!
 * return a concatenated string of the encoded values!
 *
 * @throws an exception in case of the given element not being of type form!
 * https://issues.apache.org/jira/browse/MYFACES-2110
 */
jsf.viewState = function(formElement) {
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

    formValues["javax.faces.viewState"] = document.getElementById("javax.faces.viewState").value;
    formValues = myfaces._JSF2Utils.getPostbackContent(formElement, formValues);
    return formValues;
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


/* <p>Send an asynchronous Ajax request to the server.
             * <p><b>Usage:</b></p>
             * <pre><code>
             * Example showing all optional arguments:
             *
             * &lt;commandButton id="button1" value="submit"
             *     onclick="jsf.ajax.request(this,event,
             *       {execute:'button1',render:'status',onevent: handleEvent,onerror: handleError});return false;"/&gt;
             * &lt;/commandButton/&gt;
             * </pre></code>
             * <p><b>Implementation Requirements:</b></p>
             * This function must:
             * <ul>
             * <li>Capture the element that triggered this Ajax request
             * (from the <code>source</code> argument, also known as the
             * <code>source</code> element.</li>
             * <li>If the <code>source</code> element is <code>null</code> or
             * <code>undefined</code> throw an error.</li>
             * <li>If the <code>source</code> argument is not a <code>string</code> or
             * DOM element object, throw an error.</li>
             * <li>If the <code>source</code> argument is a <code>string</code>, find the
             * DOM element for that <code>string</code> identifier.
             * <li>If the DOM element could not be determined, throw an error.</li>
             * <li>If the <code>onerror</code> and <code>onevent</code> arguments are set,
             * they must be functions, or throw an error.
             * <li>Determine the <code>source</code> element's <code>form</code>
             * element.</li>
             * <li>Get the <code>form</code> view state by calling
             * {@link jsf.viewState} passing the
             * <code>form</code> element as the argument.</li>
             * <li>Collect post data arguments for the Ajax request.
             * <ul>
             * <li>The following name/value pairs are required post data arguments:
             * <ul>
             * <li><code>javax.faces.partial.source</code> with the value as the
             * source element identifier.</li>
             * <li>The name and value of the <code>source</code> element that
             * triggered this request;</li>
             * <li><code>javax.faces.partial.ajax</code> with the value
             * <code>true</code></li>
             * </ul>
             * </li>
             * </ul>
             * </li>
             * <li>Collect optional post data arguments for the Ajax request.
             * <ul>
             * <li>Determine additional arguments (if any) from the <code>options</code>
             * argument. If <code>options.execute</code> exists, create the post data argument
             * with the name <code>javax.faces.partial.execute</code> and the value as a
             * space delimited <code>string</code> of client identifiers.  If
             * <code>options.render</code> exists, create the post data argument with the name
             * <code>javax.faces.partial.render</code> and the value as a space delimited
             * <code>string</code> of client identifiers.</li>
             * <li>Determine additional arguments (if any) from the <code>event</code>
             * argument.  The following name/value pairs may be used from the
             * <code>event</code> object:
             * <ul>
             * <li><code>target</code> - the ID of the element that triggered the event.</li>
             * <li><code>captured</code> - the ID of the element that captured the event.</li>
             * <li><code>type</code> - the type of event (ex: onkeypress)</li>
             * <li><code>alt</code> - <code>true</code> if ALT key was pressed.</li>
             * <li><code>ctrl</code> - <code>true</code> if CTRL key was pressed.</li>
             * <li><code>shift</code> - <code>true</code> if SHIFT key was pressed. </li>
             * <li><code>meta</code> - <code>true</code> if META key was pressed. </li>
             * <li><code>right</code> - <code>true</code> if right mouse button
             * was pressed. </li>
             * <li><code>left</code> - <code>true</code> if left mouse button
             * was pressed. </li>
             * <li><code>keycode</code> - the key code.
             * </ul>
             * </li>
             * </ul>
             * </li>
             * <li>Encode the set of post data arguments.</li>
             * <li>Join the encoded view state with the encoded set of post data arguments
             * to form the <code>query string</code> that will be sent to the server.</li>
             * <li>Create a request <code>context</code> object and set the properties:
             * <ul><li><code>source</code> (the source DOM element for this request)</li>
             * <li><code>onerror</code> (the error handler for this request)</li>
             * <li><code>onevent</code> (the event handler for this request)</li></ul>
             * The request context will be used during error/event handling.</li>
             * <li>Send a <code>begin</code> event following the procedure as outlined
             * in the Chapter 13 "Sending Events" section of the spec prose document <a
             *  href="../../javadocs/overview-summary.html#prose_document">linked in the
             *  overview summary</a></li>
             * <li>Send the request as an <code>asynchronous POST</code> using the
             * <code>action</code> property of the <code>form</code> element as the
             * <code>url</code>.</li>
             * </ul>
             * Before the request is sent it must be put into a queue to ensure requests
             * are sent in the same order as when they were initiated.  The request callback function
             * must examine the queue and determine the next request to be sent.  The behavior of the
             * request callback function must be as follows:
             * <ul>
             * <li>If the request completed successfully invoke {@link jsf.ajax.response}
             * passing the <code>request</code> object.</li>
             * <li>If the request did not complete successfully, notify the client.</li>
             * <li>Regardless of the outcome of the request (success or error) every request in the
             * queue must be handled.  Examine the status of each request in the queue starting from
             * the request that has been in the queue the longest.  If the status of the request is
             * <code>complete</code> (readyState 4), dequeue the request (remove it from the queue).
             * If the request has not been sent (readyState 0), send the request.  Requests that are
             * taken off the queue and sent should not be put back on the queue.</li>
             * </ul>
             *
             * </p>
             *
             * @param source The DOM element that triggered this Ajax request, or an id string of the
             * element to use as the triggering element.
             * @param event The DOM event that triggered this Ajax request.  The
             * <code>event</code> argument is optional.
             * @param options The set of available options that can be sent as
             * request parameters to control client and/or server side
             * request processing. Acceptable name/value pair options are:
             * <table border="1">
             * <tr>
             * <th>name</th>
             * <th>value</th>
             * </tr>
             * <tr>
             * <td><code>execute</code></td>
             * <td><code>space seperated list of client identifiers</code></td>
             * </tr>
             * <tr>
             * <td><code>render</code></td>
             * <td><code>space seperated list of client identifiers</code></td>
             * </tr>
             * <tr>
             * <td><code>onevent</code></td>
             * <td><code>function to callback for event</code></td>
             * </tr>
             * <tr>
             * <td><code>onerror</code></td>
             * <td><code>function to callback for error</code></td>
             * </tr>
             * </table>
             * The <code>options</code> argument is optional.
             * @member jsf.ajax
             * @function jsf.ajax.request
             * @throws ArgNotSet Error if first required argument <code>element</code> is not specified
             */

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
 *
 * <li><code>target</code> - the ID of the element that triggered the event.</li>
 * <li><code>captured</code> - the ID of the element that captured the event.</li>
 * <li><code>type</code> - the type of event (ex: onkeypress)</li>
 * <li><code>alt</code> - <code>true</code> if ALT key was pressed.</li>
 * <li><code>ctrl</code> - <code>true</code> if CTRL key was pressed.</li>
 * <li><code>shift</code> - <code>true</code> if SHIFT key was pressed. </li>
 * <li><code>meta</code> - <code>true</code> if META key was pressed. </li>
 * <li><code>right</code> - <code>true</code> if right mouse button
 * was pressed. </li>
 * <li><code>left</code> - <code>true</code> if left mouse button
 * was pressed. </li>
 * <li><code>keycode</code> - the key code.
 */
jsf.ajax._caputureEventArgs = function(/*Dom node*/node, /*event*/ obj) {
    /*
     * TODO encode the rest of the arguments
     * once it is clear what has to be done here
     */
    var retVal = {};
    return retVal;
};



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


    passThroughArguments["jsf.partial"] = true;
    //finalOptions["javax.faces.viewState"]       = JSFAjax.viewState(parentForm);
    //finalOptions = JSF2Utils.mixMaps(finalOptions, options, true);

    var viewState = jsf.viewState(sourceForm);

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
    passThroughArguments["javax.faces.partial.source"] = sourceElement.id;

    /*
     * we pass down the name and value of the source element
     */
    passThroughArguments[sourceElement.name || sourceElement.id] = sourceElement.value || 'x';

    /*
     * javax.faces.partial.ajax must be set to true
     */
    passThroughArguments["javax.faces.partial.ajax"] = true;

    /**
     * if execute or render exist
     * we have to pass them down as a blank delimited string representation
     * of an array of ids!
     */
    if(JSF2Utils.exists(passThroughArguments,"execute")) {
        /*the options must be a blank delimited list of strings*/
        //TODO add the source id to the list
        passThroughArguments["javax.faces.partial.execute"] = JSF2Utils.arrayToString(passThroughArguments.execute,' ');
        passThroughArguments.execute = null;/*remap just in case we have a valid pointer to an existing object*/
        delete passThroughArguments.execute;
    }
    if(JSF2Utils.exists(passThroughArguments,"render")) {
        //TODO add the source id to the list
        passThroughArguments["javax.faces.partial.render"] = JSF2Utils.arrayToString(passThroughArguments.render, ' ');
        passThroughArguments.execute = null;
        delete passThroughArguments.execute;
    }
    
    /*additional passthrough cleanup*/
    passThroughArguments.onevent = null;
    delete passThroughArguments.onevent;
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

/**
 * Maps an internal Trinidad xmlhttprequest event
 * into one compatible with the events of the ri
 * this is done for compatibility purposes
 * since trinidad follows similar rules regarding
 * the events we just have to map the eventing accordingly
 */
jsf.ajax._mapTrinidadToRIEvents = function(/*object*/ xhrContext,/*_TrXMLRequestEvent*/ event) {

    //TODO do the mapping code here
    var riEvent = {};
    
    //TODO add the event mapping code here
    return null;
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


    /*
     * we now can handle the onerror and onevent decently
     * maybe we also have to remap the response function as well
     * lets see!
     * (Check the specs and RI for that)
     */

    jsf.ajax.ajaxResponse(event.getRequest());
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



