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

afterEach(function () {
    myfaces.testcases.redirect("./test9-spreadsheet.jsf");
});
describe("Partial Page Rendering Nav Case", function () {
    it("Nav Case Test", function (done) {
        let htmlReporter = DQ$(".jasmine_html-reporter");

        htmlReporter.detach();
        const testFail = (err) => {
            DQ$("body").append(htmlReporter);
            done(err);
        };

        DQ$("#firstName").val = "Werner";
        DQ$("#lastName").val = "Tester";
        DQ$("#city").val = "Linz";
        DQ$("#zip").val = "Tester";
        facesRequest('forward', null, {
            execute: 'mainForm',
            render: 'fullContent',
            'jakarta.faces.behavior.event': 'action'
        }).then(function () {
            DQ$("body").waitUntilDom(() => {
                const ret = DQ$("span#firstName").innerHTML.indexOf("Werner") !== -1 &&
                    DQ$("span#lastName").innerHTML.indexOf("Tester") !== -1 &&
                    DQ$("body").innerHTML.indexOf("script executed") !== -1;
                return ret;
            }).then(() => {
                DQ$("body").append(htmlReporter);
                success(done);
            }).catch(err => {
                testFail(err);
            });
        }).catch(err => {
            testFail(err);
        });
    });
});