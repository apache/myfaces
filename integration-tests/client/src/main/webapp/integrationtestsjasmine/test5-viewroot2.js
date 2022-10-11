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

if (!window.viewRoot) {
    window.viewRoot = true;

    afterEach(function () {
        jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
        setTimeout(function () {
            myfaces.testcases.redirect("./test6-tablebasic.jsf");
        }, 1000);
    });

    beforeEach(function () {
        originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
        jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
    });


    let env = getJasmineRequireObj().Env;
    describe("Viewroot with execute @all and render @all", function () {

        beforeEach(function (done) {
            let htmlReporter = $(".jasmine_html-reporter");
            htmlReporter.detach();
            //render all kills the new jasmine code because it kills off old script configs
            jsfAjaxRequestPromise("allKeyword", null, {render: "@all", execute: "@all"}).then(function () {

                setTimeout(function() {
                    htmlReporter.appendTo("body");
                    done();
                }, 1000)

            });

        });

        it("Needs to have the root replaced", function () {
            //return setTimeout(function () {
            //getJasmineRequireObj().Env = env;
                    //expect($("body").html().indexOf("refresh successul2")).not.toBe(-1);
            if($("body").html().indexOf("refresh successul2") == -1) {
                throw new Error("Test not passed");
            }

        });
    });

}
