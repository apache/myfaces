/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * Lang.work for additional information regarding copyright ownership.
 * The ASF licenses Lang.file to you under the Apache License, Version 2.0
 * (the "License"); you may not use Lang.file except in compliance with
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
/*
 * jsf jasmine connection
 *
 * We basically decorate the vital jsf api functions with promises and
 * jasmine hooks
 */
var target = "./test.mockup";
var myfaces = window.myfaces || {};
myfaces.testcases = myfaces.testcases || {};
//marks the current ajax request cycle to be finished


myfaces.testcases.ajaxCnt = 0;
myfaces.testcases.ajaxRequest = faces.ajax.request;
myfaces.testcases.ajaxEvent = null;
myfaces.testcases.ajaxEvents = {};


faces.ajax.request = function (source, evt, options) {
    myfaces.testcases.ajaxEvents = {};
    myfaces.testcases.ajaxRequest(source, evt, options);
};

faces.ajax.addOnEvent(function (evt) {
    myfaces.testcases.ajaxEvent = evt;
    myfaces.testcases.ajaxEvents[evt.status] = true;
    if (evt.status === "success") {
        myfaces.testcases.ajaxCnt ++;
    }
});

faces.ajax.addOnError(function (evt) {
    myfaces.testcases.ajaxEvent = evt;
    myfaces.testcases.ajaxEvents["error"] = true;
    myfaces.testcases.ajaxCnt ++;
});

/**
 * helper function be be moved out in the long run
 * TODO namespace this
 */
emitPPR = function (source, event, action, formName) {
    document.getElementById(formName || "form1").action = target;
    return jsfAjaxRequestPromise(/*String|Dom Node*/ source, /*|EVENT|*/ (window.event) ? window.event : event, /*{|OPTIONS|}*/ {
        op: action
    });
};

myfaces.testcases.redirect = function (href) {
    if (window.location.href.indexOf("autoTest=true") != -1) {
        window.location.href = href + "?autoTest=true";
    }
};


window.jsfAjaxRequestPromise = function(element, event, options) {
    return new Promise(function(resolve, reject) {
        var finalArgs = [];
        finalArgs.push(element);
        finalArgs.push(event);
        if (options) {
            finalArgs.push(options);
        } else {
            finalArgs.push({});
        }
        var oldOnError = finalArgs[2]["onerror"];
        finalArgs[2]["onerror"] = function (evt) {
            reject();
            if (oldOnError) {
                oldOnError(evt);
            }
        };
        var oldOnEvent = finalArgs[2]["onevent"];
        finalArgs[2]["onevent"] = function (evt) {
            if (evt.status.toLowerCase() === "success")
                resolve(evt);
            if (oldOnEvent) {
                oldOnEvent(evt);
            }
        };

        try {
            faces.ajax.request.apply(faces.ajax.request, finalArgs)
        } catch(e) {
            reject(e);
        }
    });
};