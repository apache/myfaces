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

let successCalled = 0;
let assertSuccessPosition = false;
let oldResponse = faces.ajax.response;

faces.ajax.response = function (request, context) {

    let newContext = {};
    newContext.source = context.source;
    newContext.onevent = function (evt) {
        if (evt.status == "success") {
            successCalled++;
        }
        if (context.onevent) {
            context.onevent(evt);
        }
    };



    assertSuccessPosition = successCalled == 0;
    oldResponse(request, newContext);
    assertSuccessPosition = assertSuccessPosition && successCalled == 1;
};

afterEach(function () {
    setTimeout(function () {
        myfaces.testcases.redirect("./test19-execute.jsf");
    }, 1000);
});
describe("event location test, success must be called in response function", function () {
    it("runs the ajax cycle and checks for the proper event location of the success event", function (done) {

        facesRequest('idgiven', null, {
            execute: '@this',
            render: 'myVal',
            'jakarta.faces.behavior.event': 'action'
        }).finally(function () {
            setTimeout(function () {
                expect(assertSuccessPosition).toBeTruthy();
                done();
            }, 500);
        });
    });
});