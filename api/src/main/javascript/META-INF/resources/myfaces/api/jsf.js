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
 *MyFaces core javascripting libraries
 *
 *  Those are the central public API functions in the JSF2
 *  Ajax API! They handle the entire form submit and ajax send
 *  and resolve cycle!
 */

/**
 * reserve the root namespace
 */
if ('undefined' != typeof OpenAjax && ('undefined' == typeof jsf || null == typeof jsf)) {
    OpenAjax.hub.registerLibrary("jsf", "www.sun.com", "1.0", null);
}
//just in case openajax has failed (testing environment)
//under normal circumstances this should not happen
//either check for window.jsf == true or do it the verbose way
if (!window.jsf) {
    window.jsf = new function() {
        /*
         * specified by the spec symbols/jsf.html#.specversion
         * as specified left two digits major release number
         * middle two digits minor spec release number
         * right two digits bug release number
         */
        this.specversion = 200000;
        /**
         * specified by the spec symbols/jsf.html#.implversion
         * a number increased with every implementation version
         * and reset by moving to a new spec release number
         *
         * Due to the constraints that we cannot put
         * non jsf.&lt;namespace&gt; references outside of functions in the api
         * we have to set the version here instead of the impl.
         */
        this.implversion = 6;

        /**
         * @return the current project state emitted by the server side method:
         * javax.faces.application.Application.getProjectStage()
         */
        this.getProjectStage = function() {
            var impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
            return impl.getProjectStage();
        };

        /**
         * collect and encode data for a given form element (must be of type form)
         * find the javax.faces.ViewState element and encode its value as well!
         * return a concatenated string of the encoded values!
         *
         * @throws an exception in case of the given element not being of type form!
         * https://issues.apache.org/jira/browse/MYFACES-2110
         */
        this.getViewState = function(formElement) {
            /*we are not allowed to add the impl on a global scope so we have to inline the code*/
            var impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
            return impl.getViewState(formElement);
        };
    };
}

/**
 * just to make sure no questions arise, I simply prefer here a weak
 * typeless comparison just in case some frameworks try to interfere
 * by overriding null or fiddeling around with undefined or typeof in some ways
 * it is safer in this case than the standard way of doing a strong comparison
 **/
if (!jsf.ajax) {
    jsf.ajax = new function() {


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
         * @param {|EVENT|} event: any javascript event supported by that object
         * @param {Map||} options : map of options being pushed into the ajax cycle
         */
        this.request = function(element, event, options) {
            if (!options) {
                options = {};
            }
            /*we are not allowed to add the impl on a global scope so we have to inline the code*/
            var impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
            return impl.request(element, event, options);
        };

        this.addOnError = function(/*function*/errorListener) {
            var impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
            return impl.addOnError(errorListener);
        };

        this.addOnEvent = function(/*function*/eventListener) {
            var impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
            return impl.addOnEvent(eventListener);
        };

        /**
         * processes the ajax response if the ajax request completes successfully
         * @param request the ajax request!
         * @param context the ajax context!
         */
        this.response = function(/*xhr request object*/request, context) {
            var impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
            return impl.response(request, context);
        };
    }
}

if (!jsf.util) {
    jsf.util = new function() {

        /**
         * varargs function which executes a chain of code (functions or any other code)
         *
         * if any of the code returns false, the execution
         * is terminated prematurely skipping the rest of the code!
         *
         * @param {DomNode} source, the callee object
         * @param {Event} event, the event object of the callee event triggering this function
         *
         */
        this.chain = function(source, event) {
            var impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
            return impl.chain.apply(impl, arguments);
        };
    }
}


