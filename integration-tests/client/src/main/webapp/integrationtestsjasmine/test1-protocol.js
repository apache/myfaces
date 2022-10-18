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

        myfaces.testcases.redirect("./test2-viewroot.jsf");

});

describe("Testsuite testing the protocol", function () {
    beforeEach(function () {
        //we reset the Ajax counter before each spec.
        //Because every spec has only one Ajax request
        //and needs the counter to detect the end of the
        //Ajax cycle
        myfaces.testcases.ajaxCnt = 0;
    });
    it("It should run an Eval Ajax command", function (done) {
        emitPPR("cmd_eval", null, "eval1").then(function () {
            //another faster and better way we use wait untilDom
            const condition = (element) => element.html().value.indexOf("succeed") != -1;
            DomQuery.querySelectorAll("#evalarea1").waitUntilDom(condition).then(() => {
                //  jasmine expects at least one expectation, but at this point in time, we
                //  already have fulfilled it
                expect(true).toBeTruthy();
                done();
            }).catch(done);
        })
    });

    it("It should run Update Insert Spec - Insert Path", function (done) {

        emitPPR("cmd_update_insert2", null, "updateinsert2").then(function () {
            DQ$("body").waitUntilDom(() => {
                return DomQuery.byId("evalarea2").innerHTML.indexOf("succeed") != -1 &&
                DomQuery.byId("evalarea3").innerHTML.indexOf("succeed") != -1 &&
                DomQuery.byId("insertbefore").length &&
                DomQuery.byId("insertafter").length;
            }).then(() => {
                DomQuery.byId("insertbefore").delete();
                DomQuery.byId("insertafter").delete();
                expect(true).toBeTruthy();
                done();
            }).catch(done);
        });
    });

    it("It should run delete", function (done) {

        emitPPR("cmd_delete", null, "delete1").then(function () {
            DomQuery.byId("deleteable").waitUntilDom((element) => element.isAbsent())
                .then(() => {
                    let newNode = DomQuery.fromMarkup("<div id='deleteable'>deletearea readded by automated test</div>");
                    newNode.appendTo(DomQuery.byId("testResults"));
                    expect(true).toBeTruthy();
                    done();
                }).catch(done);
        });
    });

    it("Should run change attributes", function (done) {

        emitPPR("cmd_attributeschange", null, "attributes").catch(ex => {
            fail();
        }).then(function () {
            DomQuery.byId("attributeChange")
                .waitUntilDom((element) => element.style('borderWidth').value == "1px")
                .then((element) => {
                    element.style('borderWidth').value = "0px";
                    expect(true).toBeTruthy();
                    done();
                }).catch(done);
        });
    });

    it("should trigger Error Trigger Ajax Illegal Response", function (done) {
        emitPPR("cmd_illegalresponse", null, "illegalResponse").then(() => {
            fail();
        }).catch(function () {
            DomQuery.byId("body").waitUntilDom(() => {
                return myfaces.testcases.ajaxEvent.type === "error" &&
                    myfaces.testcases.ajaxEvent.status === "malformedXML" &&
                    myfaces.testcases.ajaxEvent.responseCode == 200 &&
                    myfaces.testcases.ajaxEvent.source.id == "cmd_illegalresponse";
            }).then(() => {
                expect(true).toBeTruthy();
                done();
            }).catch(done);
        });
    });

    it("Should trigger an ajax server error and onerror and onsuccess must have been called in this case", function (done) {
        emitPPR("cmd_error", null, "errors").catch(function () {
            DomQuery.byId("body")
                .waitUntilDom(() => myfaces.testcases.ajaxEvents["error"] && myfaces.testcases.ajaxEvents["success"])
                .then(() => {
                    expect(true).toBeTruthy();
                    done();
                })
                .catch(done);
        });
    });
});

