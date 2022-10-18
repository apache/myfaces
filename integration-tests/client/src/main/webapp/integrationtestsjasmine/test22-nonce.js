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
    myfaces.testcases.redirect("./test23-nonce.jsf");
});

describe("Nonce testing working", function () {

    it("runs an embedded script with a nonce and works", function (done) {
        emitPPR("cmd_eval", null, "execute_nonce2").then(function () {
            //another faster and better way we use wait untilDom
            DomQuery.byId("body")
                .waitUntilDom(() => DQ$("#result3").innerHTML == "success")
                .then(() => {
                    expect(DQ$("#result3").innerHTML == "success").toBe(true);
                    done();
                }).catch(done);
        })
    });

});

