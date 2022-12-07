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
    myfaces.testcases.redirect("./test3-viewbody.jsf");
});
describe("Full root replacement via protocol ViewRoot", function () {
    it("Should run the Ajax cycle and replace the ViewRoot element", function (done) {
        let htmlReporter = DQ$(".jasmine_html-reporter");
        htmlReporter.detach();
        emitPPR("form1", null, "body").then(function () {
            setTimeout(function () {
                DQ$("body").append(htmlReporter);
                expect(DQ$("#scriptreceiver").innerHTML.indexOf("hello from embedded script")).not.toBe(-1);
                done();
            }, 500);
        });
    });
});
