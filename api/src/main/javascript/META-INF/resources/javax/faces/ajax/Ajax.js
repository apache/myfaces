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
if('undefined' != typeof OpenAjax && ('undefined' == typeof javax || null == typeof javax)) {
    OpenAjax.hub.registerLibrary("javax", "www.sun.com", "1.0", null);
}

if('undefined' == typeof javax.faces || null == javax.faces ) {
    javax.faces = new Object();
}

if('undefined' == typeof javax.faces.Ajax || null == javax.faces.Ajax ) {
    javax.faces.Ajax = new function() {};
}

/**
 * collect and encode data for a given form element (must be of type form)
 * find the javax.faces.ViewState element and encode its value as well!
 * return a concatenated string of the encoded values!
 *
 * @throws an exception in case of the given element not being of type form!
 */
javax.faces.Ajax.viewState = function(formElement) {
    /**
     *  typecheck assert!, we opt for strong typing here
     *  because it makes it easier to detect bugs
     */

    if( 'undefined' == typeof(formElement)
        || null == formElement
        ||'undefined' == typeof(formElement.nodeValue)
        || null == formElement.nodeValue 
        || formElement.nodeValue != "form") {

        throw Exception("javax.faces.Ajax.viewState: param value not of type form!");

    }
/*
     * TODO #60
     * https://issues.apache.org/jira/browse/MYFACES-2110
     */
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
javax.faces.Ajax.ajaxRequest = function(/*Dom*/ element,/*|EVENT|*/ event,/*{|OPTIONS|}*/ options) {
    //condition asserts!
    if('undefined' == typeof(element) || null == element) {
        throw Exception("javax.faces.Ajax.ajaxRequest: element cannot be null or undefined!");
    }
    if('undefined' == typeof(options) || null == options) {
        throw Exception("javax.faces.Ajax.ajaxRequest: options cannot be null or undefined! it must be at least an empty map!");
    }

    /*
     * TODO #61
     * https://issues.apache.org/jira/browse/MYFACES-2112
     */
};

/**
 * processes the ajax response if the ajax request completes successfully
 * @param request the ajax request!
 */
javax.faces.Ajax.ajaxResponse = function(request) {
    if('undefined' == typeof(request) || null == request) {
        throw Exception("javax.faces.Ajax.ajaxResponse: The response cannot be null or empty!");
    }
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

    /**
     * TODO #62
     * https://issues.apache.org/jira/browse/MYFACES-2115
     */
    return null;
};



