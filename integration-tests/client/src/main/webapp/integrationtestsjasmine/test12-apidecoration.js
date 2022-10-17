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

let req,resp, viest;

beforeEach(function () {


    let oldReq = faces.ajax.request;
    faces.ajax.request = function(element, event, options) {
        try {
            req = true;
            oldReq(element, event, options);
        } finally {
            faces.ajax.request = oldReq;
        }
    }
    let oldResp = faces.ajax.response;
    faces.ajax.response = (request, context) => {
        try {
            resp = true;
            oldResp(request, context);
        } finally {
            faces.ajax.response = oldResp;
        }
    };
    let oldViewst = faces.getViewState;
    faces.getViewState = (formElement) => {
        try {
            viest = true;
            return oldViewst(formElement);
        } finally {
            faces.getViewState = oldViewst;
        }
    };
});
afterEach(function () {
    setTimeout(function () {
        myfaces.testcases.redirect("./test13-cssreplacementhead.jsf");
    }, 1000);
});
describe("Test for decoratable calls within our Faces lifecycle", function () {
    it("checks whether all functions are properly called", function (done) {
        facesRequest('reloader', null, {
            execute: '@none',
            render: 'outputWriter',
            'jakarta.faces.behavior.event': 'action'
        }).then(function (success) {
            setTimeout(function () {
                expect(req).toEqual(true);
                expect(resp).toEqual(true);
                expect(viest).toEqual(true);
                done();
            }, 500);
        }).catch(done);
    });
});