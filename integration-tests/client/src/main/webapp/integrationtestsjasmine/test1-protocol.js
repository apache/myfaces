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
//const _it = () => {}

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
            const condition = (element) => element.innerHTML.indexOf("succeed") !== -1;
            DQ$("#evalarea1")
                .waitUntilDom(condition)
                .then(() => success(done));
        }).catch(done);
    })

    it("It should run Update Insert Spec - Insert Path", function (done) {
        emitPPR("cmd_update_insert2", null, "updateinsert2").then(function () {
            DQ$("body").waitUntilDom(() => {
                return DQ$("#evalarea2").innerHTML.indexOf("succeed") !== -1 &&
                    DQ$("#evalarea3").innerHTML.indexOf("succeed") !== -1 &&
                    DQ$("#insertbefore").length &&
                    DQ$("#insertafter").length;
            }).then(() => {
                DQ$("#insertbefore").delete();
                DQ$("#insertafter").delete();
                success(done);
            }).catch(done);
        });
    });

    it("It should run delete", function (done) {
        emitPPR("cmd_delete", null, "delete1").then(function () {
            DQ$("#deleteable").waitUntilDom((element) => element.isAbsent())
                .then(() => {
                    let newNode = DomQuery.fromMarkup("<div id='deleteable'>deletearea readded by automated test</div>");
                    newNode.appendTo(DQ$("#testResults"));
                    success(done);
                }).catch(done);
        });
    });

    it("Should run change attributes", function (done) {
        emitPPR("cmd_attributeschange", null, "attributes").catch(done).then(function () {
            DQ$("#attributeChange")
                .waitUntilDom((element) => element.style('borderWidth').value === "1px")
                .then((element) => {
                    element.style('borderWidth').value = "0px";
                    success(done);
                }).catch(done);
        }).catch(done);
    });

    it("should trigger Error Trigger Ajax Illegal Response", function (done) {
        emitPPR("cmd_illegalresponse", null, "illegalResponse").then(() => done(new Error("fail"))).catch(function () {
            DQ$("body").waitUntilDom(() => {
                return myfaces.testcases.ajaxEvent.type === "error" &&
                    myfaces.testcases.ajaxEvent.status === "malformedXML" &&
                    myfaces.testcases.ajaxEvent.responseCode === "200" &&
                    myfaces.testcases.ajaxEvent.source.id === "cmd_illegalresponse";
            }).then(() => success(done)).catch(done);
        });
    });

    it("Should trigger an ajax server error and onerror and onsuccess must have been called in this case",
        (done) => {
            emitPPR("cmd_error", null, "errors")
                .then(() => {
                    debugger;
                    done("Fail");
                })
                .catch(() => {
                    DQ$("body")
                        .waitUntilDom(() => {
                            return myfaces.testcases.ajaxEvents["error"] && myfaces.testcases.ajaxEvents["success"]
                        })
                        .then(() => success(done))
                        .catch(done);
                })
        });
});

