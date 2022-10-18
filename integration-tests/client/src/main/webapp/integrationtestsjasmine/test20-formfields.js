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
    myfaces.testcases.redirect("./test21-nonce.jsf");
});
describe("Execute none handling", function () {
    it("runs an execute request with execute @none", function (done) {
        DQ$("#centerForm").getAsElem(0).value.action = './test.mockup'
        facesRequest(document.getElementById("submitme"), null, {
            render: "booga @none",
            execute: "booga2 @none",
            op: "executeNone"
        }).finally(function () {
            DQ$("#result")
                .waitUntilDom(item => item.innerHTML.indexOf("success") !== -1)
                .then(() => success(done))
                .catch(done);
        });

    });
});
