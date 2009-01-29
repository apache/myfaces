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
if ('undefined' != typeof OpenAjax && ('undefined' == typeof javax || null == typeof javax)) {
    OpenAjax.hub.registerLibrary("javax", "www.sun.com", "1.0", null);
}
//just in case openajax has failed (testing environment)
//under normal circumstances this should not happen
if ('undefined' == typeof javax || null == javax) {
    window.javax = new Object();
}

if ('undefined' == typeof javax.faces || null == javax.faces) {
    javax.faces = new Object();
}
if ('undefined' == typeof javax.faces.Ajax || null == javax.faces.Ajax) {
    javax.faces.Ajax = new function() {
        };
}
javax.faces.Ajax._requestQueue = new org.apache.myfaces._TrRequestQueue();
/**
 * collect and encode data for a given form element (must be of type form)
 * find the javax.faces.ViewState element and encode its value as well!
 * return a concatenated string of the encoded values!
 *
 * @throws an exception in case of the given element not being of type form!
 * https://issues.apache.org/jira/browse/MYFACES-2110
 */
javax.faces.Ajax.viewState = function(formElement) {
    /**
     *  typecheck assert!, we opt for strong typing here
     *  because it makes it easier to detect bugs
     */

    if ('undefined' == typeof(formElement)
        || null == formElement
        || 'undefined' == typeof(formElement.nodeName)
        || null == formElement.nodeName
        || formElement.nodeName != "FORM") {
        throw Exception("javax.faces.Ajax.viewState: param value not of type form!");
    }
    var formValues = {};

    formValues["javax.faces.viewState"] = document.getElementById("javax.faces.viewState").value;
    formValues = org.apache.myfaces._JSF2Utils.getPostbackContent(formElement, formValues);
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

javax.faces.Ajax.ajaxRequest = function(/*Dom*/ element, /*|EVENT|*/ event, /*{|OPTIONS|}*/ options) {

    /**
     * input constraints
     */
    if('undefined' == typeof( element ) || null == element) {
        throw new Exception("javax.faces.Ajax.ajaxRequest, element must be set!");
    }

    /*namespace remap for our local function context we mix the entire function namespace into
     *a local function variable so that we do not have to write the entire namespace
     *all the time
     **/
    var JSF2Utils   = org.apache.myfaces._JSF2Utils;
    /**
     * we cross reference statically hence the mapping here
     * the entire mapping between the functions is stateless
     */
    var JSFAjax     = javax.faces.Ajax;
    /**
     * fetch the parent form first
     */
    var parentForm  = JSF2Utils.getParentForm(element);

    if('undefined' == typeof parentForm || null == parentForm) {
        parentForm = document.forms[0];
    }

    /**
     * We make a copy of our options because
     * we should not touch the incoming params!
     **/
    var finalOptions = JSF2Utils.mixMaps({}, options, true);

    finalOptions["javax.faces.ajax.partial"]    = true;
    //finalOptions["javax.faces.viewState"]       = JSFAjax.viewState(parentForm);
    //finalOptions = JSF2Utils.mixMaps(finalOptions, options, true);

    var viewState = JSFAjax.viewState(parentForm);

    /*
     * either we have a valid form element or an element then we have to pass the form
     * if it is a dummy element we have to pass it down as additional parameter
     * the same applies to non form elements which cannot hold values
     * but have ids!
     *
     */
    var sourceElement = JSF2Utils.byId(element);
    if ('undefined' != typeof(sourceElement) && null != sourceElement &&
        'undefined' != typeof(sourceElement.form)) {
        /*input element */
        /*either name or id name preferrably*/
        finalOptions[sourceElement.name || sourceElement.id] = sourceElement.value || 'x';
    } else {
        /*no form element*/
        /*dummy element or string value both passed as string not existing in the dom*/
        if(JSF2Utils.isString(element)) {
            finalOptions[element] = element;
        } else {
            /*element passed directly the id must be set to be present!*/
            finalOptions[element.id] = element.id;
        }
    }



    if('undefined' != typeof  finalOptions.execute && null !=  finalOptions.execute) {
        /* execute is string of client ids which should be executed*/
        /*not specified but the ri trims all the strings in the array before passing the execute*/
        finalOptions["javax.faces.partial.execute"] = JSF2Utils.trimStringInternal( finalOptions.execute,',');
        finalOptions.execute = null;/*remap just in case we have a valid pointer to an existing object*/
        delete finalOptions.execute;
    }
    if('undefined' != typeof  finalOptions.render && null !=  finalOptions.render) {
        finalOptions["javax.faces.partial.render"] = JSF2Utils.trimStringInternal(finalOptions.render, ',');
        finalOptions.execute = null;
        delete finalOptions.execute;
    }


    /*according to the specs we only deal with queued asynchronous events in our transport layer!*/

    //TODO encode all parameters including the viewstate
    JSFAjax._requestQueue.sendRequest(this, JSFAjax._trSuccessCallback, parentForm.action, viewState+"&"+JSF2Utils.getPostbackContentFromMap(finalOptions) );
/*
     * TODO #61
     * https://issues.apache.org/jira/browse/MYFACES-2112
     * done
     */
};
/**
 * Internal Trinidad
 * JSF 2.0 callback compatibility handler
 * since we use trinidad as our transport we have to do it that way
 */
javax.faces.Ajax._trSuccessCallback = function(/*_TrXMLRequestEvent*/ event) {
    javax.faces.Ajax.ajaxResponse(event.getRequest());
}
/**
 * processes the ajax response if the ajax request completes successfully
 * @param request the ajax request!
 */
javax.faces.Ajax.ajaxResponse = function(/*xhr request object*/request) {
    if ('undefined' == typeof(request) || null == request) {
        throw Exception("javax.faces.Ajax.ajaxResponse: The response cannot be null or empty!");
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
javax.faces.Ajax.getProjectStage = function() {


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



