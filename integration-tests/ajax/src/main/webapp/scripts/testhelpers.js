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
 * varius helper scripts to ease our testing
 */

/**
 * /**
 * ppr emitting function, which encapsules
 * jsf.ajax.request with additioonal helpers
 *
 * @param source the source item triggering the event
 * @param event an outer html event object
 * @param action the action to perform on the jsf side (additional parameter which can be interpreted on the server)
 * @param formName formName for the issuing form
 * @param target the action target (ootional)
 * @param onError onError handler
 * @param onEvent onEvent handler
 */
function emitPPR(source, event, action, formName,  target, onError, onEvent) {

    document.getElementById(formName || "form1").action = target || "test.mockup";

    try {
        jsf.ajax.request(/*String|Dom Node*/ source, /*|EVENT|*/ (window.event) ? window.event : event, /*{|OPTIONS|}*/ {
            op: action,
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
    }

}


/**
 * special element holding the processed errors
 */
var processError = function (data) {
    if (document.querySelectorAll("#processedErrror").length == 0) {
        var element = document.createElement("div");
        element.id = "processedErrror";
        document.body.append(element);
    }
    let logElement = document.querySelectorAll("#processedErrror")[0];
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
 * to have later referemnces to that one
 * @type {Array}
 */

if(console.error) {
    var oldErrorFunc = console.error;
    console.error = function() {
        logError(JSON.stringify(arguments[0]));
        oldErrorFunc.apply(console, arguments);
    }
}