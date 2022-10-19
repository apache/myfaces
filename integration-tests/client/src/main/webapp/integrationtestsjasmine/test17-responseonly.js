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
let oldResponse = faces.ajax.response;

//we are going to decorate the response for the first testcase
function applySourceOnly() {
    faces.ajax.response = function (request, context) {
        let newContext = {};
        newContext.source = context.source;
        newContext.onevent = context.onevent;
        newContext.onerror = context.onerror;

        oldResponse(request, newContext);
    };
}


function applyEmpty() {

    faces.ajax.response = function (request, context) {
        let newContext = {};

        newContext.onevent = context.onevent;
        newContext.onerror = context.onerror;

        oldResponse(request, newContext);
    };
}

afterEach(function () {
    myfaces.testcases.redirect("./test18-eventlocations.jsf");
});
describe("Various response tests giving the codebase something to chew on in the reponse part", function () {
    beforeEach(function () {
        myfaces.testcases.ajaxCnt = 0;
    });
    it("handles a normal reset case", function (done) {

        facesRequest('resetme', null, {
            execute: '@this',
            render: 'myVal',
            'jakarta.faces.behavior.event': 'action'
        }).then(() => success(done)).catch(done);


    });
    it("minimalistic context, source id is given", function (done) {

        applySourceOnly();
        facesRequest('idgiven', null, {
            execute: '@this',
            render: 'myVal',
            'jakarta.faces.behavior.event': 'action'
        }).then(function () {
            DQ$("body")
                .waitUntilDom(() => DQ$("#myVal").innerHTML.replace(/\s+/gi, "").indexOf("1") !== -1)
                .then(() => success(done))
                .catch(done);
        }).catch(done);

    });
    it("runs on an empty context map", function (done) {
        applyEmpty();
        facesRequest('emptymap', null, {
            execute: '@none',
            render: 'outputWriter',
            'jakarta.faces.behavior.event': 'action'
        }).then(function () {
            DQ$("body")
                .waitUntilDom(() => DQ$("#myVal").innerHTML.replace(/\s+/gi, "").indexOf("1") !== -1)
                .then(() => success(done))
                .catch(done);
        }).catch(done);
    });
});