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

        myfaces.testcases.redirect("./test11-scriptblocks.jsf");

});
describe("Regression test for double eval on a single script element", function () {
    it("Runs the double eval test", function (done) {
        let promises = [];
        for (let cnt = 0; cnt < 2; cnt++) {
            promises.push(facesRequest('reloader', null, {
                execute: '@none',
                render: 'outputWriter',
                'jakarta.faces.behavior.event': 'action'
            }));
        }
        Promise.all(promises).then(() => {
            DQ$("#output")
                .waitUntilDom(element => element.innerHTML === "0 1 2 ")
                .then(() => success(done))
                .catch(done);
        }).catch(done);
    });
});