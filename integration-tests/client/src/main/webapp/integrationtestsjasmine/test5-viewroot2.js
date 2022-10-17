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
let originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
window.viewRoot = true;
let htmlReporter, found;

afterEach(function () {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
    setTimeout(function () {
        myfaces.testcases.redirect("./test6-tablebasic.jsf");
    }, 1000);
});

beforeEach(function () {
    htmlReporter = DQ$(".jasmine_html-reporter");
    htmlReporter.detach();
    originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
});


describe("ViewRoot with execute @all and render @all", function () {


    //expect does not like double includes
    it("Needs to have the root replaced", function (done) {
        facesRequest("allKeyword", null, {render: "@all", execute: "@all"})
            .then(() => {
                DQ$("body").waitUntilDom(element => {
                    return (found = found || (DQ$("body").innerHTML.indexOf("refresh successul2") !== -1 && window.__mf_import_cnt == 2));
                }).then(element => {
                    htmlReporter.appendTo(DQ$("body"))
                    expect(true).toBeTruthy();
                    success(done);
                }).catch((err) => {
                    DQ$("body").append(htmlReporter);
                    done(err);
                });
            });


    });
});

