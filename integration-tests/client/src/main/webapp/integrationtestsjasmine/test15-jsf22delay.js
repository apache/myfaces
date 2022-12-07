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
    myfaces.testcases.redirect("./test17-responseonly.jsf");
});
describe("JSF 22 delay test", function () {
    it("Runs the delay test", function () {
        let promises = [];
        // noinspection JSUnresolvedVariable,JSUnresolvedFunction
        promises.push(facesRequest(document.getElementById("delayControl"), null, {
            execute: "delayControl",
            render: "delayoutput",
            op: "cleardelay"
        }));
        for (let cnt = 0; cnt < 100; cnt++) {
            // noinspection JSUnresolvedVariable,JSUnresolvedFunction
            promises.push(facesRequest(document.getElementById("delayControl"), null, {
                execute: "delayControl",
                render: "delayoutput",
                op: "delay",
                delay: 500,
                myfaces: {delay: 500}
            }));
        }
        Promise.all(promises).finally(function () {
            DQ$("delayoutput")
                .waitUntilDom(element => element.innerHTML.indexOf("Number of requests so far 1") !== -1)
                .then(() => success(done))
                .catch(done)
        })
    });
});