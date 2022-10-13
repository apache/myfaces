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
        myfaces.testcases.redirect("./test12-apidecoration.jsf");
    }, 1000);
});
describe("Script blocks in various formats", function () {
    it("Performs a script bloc test", function (done) {

        DQ$("#resultArea").innerHTML = "";
        jsfAjaxRequestPromise('reloader', null, {
            execute: '@none',
            render: 'outputWriter',
            'jakarta.faces.behavior.event': 'action'
        }).finally(function () {
            setTimeout(function () {
                expect(DQ$(".result2").innerHTML == "normal script --&gt;").toBeTruthy();//contents of result2 must match
                expect(DQ$(".result3").innerHTML == "normal script --&gt;").toBeTruthy();//contents of result3 must match
                expect(DQ$(".result4").innerHTML == "normal script ]]&gt;").toBeTruthy();//contents of result4 must match
                done();
            }, 500);
        });

    });
});