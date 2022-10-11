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
        myfaces.testcases.redirect("./test14-multiform.jsf");
    }, 1000);
});
describe("CSS Head replacement test", function () {
    it("replaces the head and checks whether the css has been replaced", function (done) {
        let htmlReporter = $(".jasmine_html-reporter");

            htmlReporter.detach();
            jsfAjaxRequestPromise('nextPage', null, {
                execute: 'mainForm',
                render: '@all',
                'jakarta.faces.behavior.event': 'action'
            }).finally(function () {
                htmlReporter.appendTo("body");
                setTimeout(function () {
                    expect($("#div1").width() > 120).toBeTruthy();//"div1 has no width anymore",
                    expect($("#div2").width() > 120).toBeTruthy();//"div2 has no width anymore",
                    expect($("#div3").width() > 120).toBeTruthy();//"div3 has no width anymore",
                    expect($("#div4").width() < 120).toBeTruthy();//"div4 has a width",
                    expect($("#div5").width() < 120).toBeTruthy();//"div5 has a width",
                    expect($("#div6").width() < 120).toBeTruthy();//"div6 has a width",
                    expect($("#div7").width() < 120).toBeTruthy();//"div6 has a width",
                    done();
                }, 500);

            });


    });

});