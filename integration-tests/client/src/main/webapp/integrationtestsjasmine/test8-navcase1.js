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
    setTimeout(function () {
        myfaces.testcases.redirect("./test9-spreadsheet.jsf");
    }, 1000);
});
describe("Partial Page Rendering Nav Case", function () {
    it("Nav Case Test", function (done) {
        let htmlReporter = DQ$(".jasmine_html-reporter");


        htmlReporter.detach();
        DQ$("#firstName").val = "Werner";
        DQ$("#lastName").val = "Tester";
        DQ$("#city").val = "Linz";
        DQ$("#zip").val = "Tester";
        jsfAjaxRequestPromise('forward', null, {
            execute: 'mainForm',
            render: 'fullContent',
            'jakarta.faces.behavior.event': 'action'
        }).then(function () {
            setTimeout(function () {
                htmlReporter.appendTo("body");
                expect(DQ$("span#firstName").innerHTML.indexOf("Werner")).not.toBe(-1);
                expect(DQ$("span#lastName").innerHTML.indexOf("Tester")).not.toBe(-1);
                expect(DQ$("body").innerHTML.indexOf("script executed")).not.toBe(-1);
                done();
            }, 500);
        });
    });
});