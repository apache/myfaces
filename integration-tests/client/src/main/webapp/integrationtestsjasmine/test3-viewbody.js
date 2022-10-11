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
        myfaces.testcases.redirect("./test4-chain.jsf");
    }, 1000);
});
describe("Full body replacement via protocol view body", function () {
    it("Should run the ajax and replace the body", function (done) {
        let htmlReporter = DomQuery.querySelectorAll(".jasmine_html-reporter");
        htmlReporter.detach();
        emitPPR("form1", null, "body2").then(function () {
            setTimeout(function () {
                htmlReporter.appendTo(DomQuery.querySelectorAll("body"));
                let html = DomQuery.querySelectorAll("body").html().value;
                expect(html.indexOf("testResults59")).not.toBe(-1);
                expect(html.indexOf("Body replacement test successful")).not.toBe(-1);
                done();
            }, 500);
        });

    });
});
