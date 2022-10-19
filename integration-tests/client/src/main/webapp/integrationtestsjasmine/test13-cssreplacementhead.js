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

    myfaces.testcases.redirect("./test14-multiform.jsf");

});
describe("CSS Head replacement test", function () {
    it("replaces the head and checks whether the css has been replaced", function (done) {
        let htmlReporter = DQ$(".jasmine_html-reporter");

        htmlReporter.detach();
        facesRequest('nextPage', null, {
            execute: 'mainForm',
            render: '@all',
            'jakarta.faces.behavior.event': 'action'
        }).then(function () {
            // this triggers the waitUntil in the dom and reattaches the html reporter
            // this is needed because the style variations do not trigger a change anymore
            setTimeout(() => DQ$("body").innerHTML = DQ$("body").innerHTML + " ", 100);
            DQ$("body")
                .append(htmlReporter)
                .waitUntilDom(() => {
                    return DQ$("#div1").offsetWidth > 120 &&
                    DQ$("#div2").offsetWidth > 120 &&
                    DQ$("#div3").offsetWidth > 120 &&
                    DQ$("#div4").offsetWidth < 120 &&
                    DQ$("#div5").offsetWidth < 120 &&
                    DQ$("#div6").offsetWidth < 120 &&
                    DQ$("#div7").offsetWidth < 120;
            }).then(() => {
                success(done)
            }).catch(done);
        }).catch(done);


    });

});