/**
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

/**
 * various helper scripts to ease our testing
 */

/**
 * /**
 * ppr emitting function, which encapsules
 * faces.ajax.request with additional helpers
 *
 * @param source the source item triggering the event
 * @param event an outer html event object
 * @param action the action to perform on the jsf side (additional parameter which can be interpreted on the server)
 * @param formId formName for the issuing form
 * @param target the action target (optional)
 * @param onError onError handler
 * @param onEvent onEvent handler
 */
function emitPPR(source, event, action, formId, target, onError, onEvent) {

    var oldAction = document.getElementById(formId || "form1").action;
    document.getElementById(formId || "form1").action = target || "test.mockup";


    try {
        faces.ajax.request(/*String|Dom Node*/ source, /*|EVENT|*/ (window.event) ? window.event : event, /*{|OPTIONS|}*/ {
            op: action,
            origin: window.location.href,
            onerror: onError || function (data) {
                /*
                * generic error check, all the error data coming in is dumped into a special entry
                * the exception are http errors which are handled by the browser only for now
                * */
                processError(data);
            }
        });
    } catch (e) {
        console.error(e);
    } finally {
        document.getElementById(formId || "form1").action = oldAction;
    }

}

/**
 * resets the current views servlet state
 * of the mockup servlet
 *
 * @param evt the triggering event
 */
function resetServerValues(evt) {

    var formId = document.querySelectorAll("form").length ? document.querySelectorAll("form")[0].id : null;
    emitPPR(evt.target, evt, "reset_counters", formId)
}


/**
 * special element holding the processed errors
 */
var processError = function (data) {
    if (document.querySelectorAll("#processedError").length == 0) {
        var element = document.createElement("div");
        element.id = "processedError";
        document.body.append(element);
    }
    let logElement = document.querySelectorAll("#processedError")[0];
    logElement.innerText = logElement.innerText + JSON.stringify(data);
};

/**
 * error log inframe simulation for further processing
 */
var logError = function () {
    if (document.querySelectorAll("#logError").length == 0) {
        var element = document.createElement("div");
        element.id = "logError";
        document.body.append(element);
    }
    let logElement = document.querySelectorAll("#logError")[0];
    logElement.innerText = logElement.innerText + arguments[0];
};

/**
 * we log our console error output into an array
 * to have later references to that one
 * @type {Array}
 */

if (console.error) {
    var oldErrorFunc = console.error;
    console.error = function () {
        logError(JSON.stringify(arguments[0]));
        oldErrorFunc.apply(console, arguments);
    }
}

/**
 * we add a standardized reset button to our first form to reset the counters
 */
window.addEventListener("DOMContentLoaded", function () {
    if (document.body.querySelectorAll("#_reset_all").length == 0) {
        var button = document.createElement("button");
        button.id = "_reset_all";
        button.onclick = function (evt) {
            resetServerValues(evt);
            return false;
        };
        button.innerText = "Reset State";
        let form = document.body.querySelectorAll("form").length ?
            document.body.querySelectorAll("form")[0] : document.body;
        form.appendChild(button);

    }
});