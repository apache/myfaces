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
    it("Should run the Ajax cycle and replace the body", function (done) {
        let htmlReporter = DomQuery.querySelectorAll(".jasmine_html-reporter");
        htmlReporter.detach();
        emitPPR("form1", null, "body2").then(function () {
            DQ$("body").waitUntilDom((element) =>  {
                return element.innerHTML.indexOf('Body replacement test successful') != -1;
            }).then(() =>  {
                expect(DQ$("body").append(htmlReporter).innerHTML.indexOf("testResults102")).not.toBe(-1);
                done();
            }).catch((err) => {
                DQ$("body").append(htmlReporter)
                done(err);
            });
        });

    });
});
