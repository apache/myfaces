/*! Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
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

import {describe, it} from "mocha";
import * as sinon from "sinon";
import {expect} from "chai";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import {_Es2019Array, DomQuery, DQ$} from "mona-dish";
import {
    COMPLETE, EMPTY_STR,
    P_AJAX,
    P_EXECUTE,
    P_AJAX_SOURCE,
    P_RENDER,
    P_VIEWSTATE,
    P_WINDOW_ID,
    SUCCESS
} from "../../impl/core/Const";
import defaultMyFaces = StandardInits.defaultMyFaces;
import initVirtualElement = StandardInits.initVirtualElement;
import STD_XML = StandardInits.STD_XML;
import exp from "constants";
import initCheckboxForm = StandardInits.initCheckboxRadioForm;

declare var faces: any;
declare var Implementation: any;

let issueStdReq = function (element) {
    faces.ajax.request(element, null, {
        execute: "input_1",
        render: "@form",
        params: {
            pass1: "pass1",
            pass2: "pass2"
        }
    });
};


let issueStdPPSReq = function (element) {
    faces.ajax.request(element, null, {
        execute: "input_1",
        render: "@form",
        /*
        * myfaces is the extension placeholder, atm only pps is used
        * which was the most useful extension in our legacy codebase
        */
        myfaces: {
            pps: true
        },
        /*
         * params is the spec conform way to pass additional request paramerters
         */
        params: {
            pass1: "pass1",
            pass2: "pass2"
        }
    });
};

/**
 * specialized tests testing the xhr core behavior when it hits the xmlHttpRequest object
 */
describe('Tests on the xhr core when it starts to call the request', function () {
    let oldFlatMap = null;
    beforeEach(async function () {

        let waitForResult = defaultMyFaces();

        return waitForResult.then((close) => {

            this.xhr = sinon.useFakeXMLHttpRequest();
            this.requests = [];
            this.xhr.onCreate = (xhr) => {
                this.requests.push(xhr);
            };
            (<any>global).XMLHttpRequest = this.xhr;
            window.XMLHttpRequest = this.xhr;

            this.jsfAjaxResponse = sinon.spy((<any>global).faces.ajax, "response");
            oldFlatMap =Array.prototype["flatMap"];
            window["Es2019Array"] = _Es2019Array;
            delete Array.prototype["flatMap"];

            this.closeIt = () => {
                (<any>global).XMLHttpRequest = window.XMLHttpRequest = this.xhr.restore();
                this.jsfAjaxResponse.restore();
                Implementation.reset();
                close();
            }
        });
    });

    afterEach(function () {
        this.closeIt();
        if(oldFlatMap) {
            Array.prototype["flatMap"] = oldFlatMap;
            oldFlatMap = null;
        }
    });

    it('must have the standard parameters all in', function (done) {
        //issue a standard faces.ajax.request upon the standard simple form case and check the passed parameters
        //and whether send was called
        let send = sinon.spy(XMLHttpRequest.prototype, "send");

        try {
            let element = DomQuery.byId("input_2").getAsElem(0).value;
            issueStdReq(element);

            expect(this.requests.length).to.eq(1);
            expect(this.requests[0].method).to.eq("POST");
            expect(this.requests[0].async).to.be.true;
            expect(send.called).to.be.true;
            expect(send.callCount).to.eq(1);

            //sent params jakarta.faces.ViewState=null&execute=input_1&render=%40form&pass1=pass1&pass2=pass2&jakarta.faces.windowId=null&jakarta.faces.source=input_2&jakarta.faces.partial.ajax=input_2&blarg=blarg&jakarta.faces.partial.execute=input_1%20input_2&jakarta.faces.partial.render=blarg

        } finally {

            send.restore();
        }

        done();
    });

    it('it must have the pass through values properly passed', function (done) {
        let send = sinon.spy(XMLHttpRequest.prototype, "send");
        try {
            let element = DomQuery.byId("input_2").getAsElem(0).value;
            issueStdReq(element);

            expect(send.called).to.be.true;
            let argsVal: any = send.args[0][0];
            let arsArr = argsVal.split("&");
            let resultsMap = {};
            for (let val of arsArr) {
                let keyVal = val.split("=");
                resultsMap[keyVal[0]] = keyVal[1];
            }

            // normal request, all issuing form must be encoded!
            expect(!!(resultsMap?.["input_2_text"] ?? false)).to.eq(true);
            expect(!!(resultsMap?.["input_1"] ?? false)).to.eq(true);
            expect(resultsMap["pass1"]).to.eq("pass1");
            expect(resultsMap["pass2"]).to.eq("pass2");
            expect(!!resultsMap["render"]).to.be.false;
            expect(!!resultsMap["execute"]).to.be.false;
            expect(P_WINDOW_ID in resultsMap).to.be.false;
            expect(P_VIEWSTATE in resultsMap).to.be.true;
            expect(resultsMap[P_AJAX_SOURCE]).to.eq("input_2");
            expect(resultsMap[P_AJAX]).to.eq("true");
            expect(resultsMap[P_RENDER]).to.eq("blarg");
            expect(resultsMap[P_EXECUTE]).to.eq("input_1%20input_2");

        } finally {
            send.restore();
        }
        done();
    });

    it('it must handle resetValues properly', function (done) {
        let send = sinon.spy(XMLHttpRequest.prototype, "send");
        try {
            let element = DomQuery.byId("input_2").getAsElem(0).value;
            faces.ajax.request(element, null, {
                execute: "input_1",
                resetValues: true,
                render: "@form",
                params: {
                    pass1: "pass1",
                    pass2: "pass2"
                }
            });

            expect(send.called).to.be.true;
            let argsVal: any = send.args[0][0];
            let arsArr = argsVal.split("&");
            let resultsMap = {};
            for (let val of arsArr) {
                let keyVal = val.split("=");
                resultsMap[keyVal[0]] = keyVal[1];
            }
            expect(resultsMap["jakarta.faces.partial.resetValues"]).to.eq("true");
        } finally {
            send.restore();
        }
        done();
    });

    it('it must have the proper target type', function (done) {
        let send = sinon.spy(XMLHttpRequest.prototype, "send");
        try {
            let element = DomQuery.byId("input_2").getAsElem(0).value;
            issueStdReq(element);

            expect(this.requests[0].requestHeaders.Accept.indexOf("application/xml") != -1).to.be.true;

        } finally {
            send.restore();
        }
        done();
    });

});

describe('Tests after core when it hits response', function () {

    beforeEach(async function () {

        let waitForResult = defaultMyFaces();

        return waitForResult.then((close) => {

            this.xhr = sinon.useFakeXMLHttpRequest();
            this.requests = [];
            this.xhr.onCreate = (xhr) => {
                this.requests.push(xhr);
            };
            (<any>global).XMLHttpRequest = this.xhr = sinon.useFakeXMLHttpRequest();
            // @ts-ignore
            window.XMLHttpRequest = this.xhr = sinon.useFakeXMLHttpRequest() as XMLHttpRequest;

            this.jsfAjaxResponse = sinon.spy((<any>global).faces.ajax, "response");

            this.closeIt = () => {
                (<any>global).XMLHttpRequest = window.XMLHttpRequest = this.xhr.restore();
                this.jsfAjaxResponse.restore();
                Implementation.reset();
                close();
            }
        });
    });

    afterEach(function () {
        this.closeIt();
    });

    it('must have passed all ajax request phase events', function (done) {

        let send = sinon.spy(XMLHttpRequest.prototype, "send");
        let globalCnt = 0;
        let localCnt = 0;
        try {
            let element = DomQuery.byId("input_2").getAsElem(0).value;
            faces.ajax.addOnEvent(() => {
                globalCnt++;
            });
            faces.ajax.request(element, null, {
                execute: "input_1",
                render: "@form",
                params: {
                    pass1: "pass1",
                    pass2: "pass2"
                },
                message: "Hello World",
                onevent: (evt: any) => {
                    localCnt++;
                }
            });

            let xhrReq = this.requests[0];
            let requestBody = xhrReq.requestBody.split("&");

            xhrReq.respond(200, {'Content-Type': 'text/xml'}, STD_XML);
            expect(requestBody.indexOf("pass1=pass1")).not.to.eq(-1);
            expect(requestBody.indexOf("pass2=pass2")).not.to.eq(-1);
            expect(requestBody.indexOf("message=Hello%20World")).not.to.eq(-1);

            expect(this.jsfAjaxResponse.callCount).to.eq(1);
            //success ommitted due to fake response
            expect(globalCnt == 3).to.eq(true);
            expect(localCnt == 3).to.eq(true);
            done();
        } catch (e) {
            console.error(e);

        } finally {
            send.restore();
        }

    });

    it('it must have called request and the pass through values must be properly transferred ' +
        'into the context, via the old non spec conform behavior', function (done) {
        let send = sinon.spy(XMLHttpRequest.prototype, "send");
        let globalCnt = 0;
        let localCnt = 0;
        let xhrReq = null;

        try {
            let element = DomQuery.byId("input_2").getAsElem(0).value;
            faces.ajax.addOnEvent(() => {
                globalCnt++;
            });


            faces.ajax.request(element, null, {
                execute: "input_1",
                render: "@form",
                pass1: "pass1",
                pass2: "pass2",
                onevent: (evt: any) => {
                    localCnt++;
                    if (evt.status == COMPLETE) {
                        expect(!!xhrReq.responseXML).to.be.true;
                    }
                    if (evt.status == SUCCESS) {
                        expect(this.jsfAjaxResponse.callCount).to.eq(1);

                        expect(this.jsfAjaxResponse.firstCall.args[0] instanceof XMLHttpRequest).to.be.true;
                        let lastArg = this.jsfAjaxResponse.firstCall.args[1];
                        expect(lastArg.onevent != null).to.be.true;
                        expect(lastArg.onevent instanceof Function).to.be.true;
                        expect(!!lastArg.onError).to.be.false;
                        expect(lastArg.pass1 == "pass1").to.be.true;
                        expect(lastArg.pass2 == "pass2").to.be.true;
                        expect(!!lastArg[P_AJAX_SOURCE]).to.be.true;
                        expect(!!lastArg[P_AJAX]).to.be.true;
                        expect(!!lastArg[P_EXECUTE]).to.be.true;
                        expect(!!lastArg[P_RENDER]).to.be.true;

                        expect(this.jsfAjaxResponse.firstCall.args.length).to.eq(2);

                        expect(globalCnt == 2).to.eq(true); //local before global
                        expect(localCnt == 3).to.eq(true);

                        done();
                    }
                }
            });

            xhrReq = this.requests[0];
            xhrReq.responsetype = "text/xml";
            xhrReq.respond(200, {'Content-Type': 'text/xml'}, STD_XML);


        } catch (e) {
            console.error(e);

        } finally {
            send.restore();
        }
    });

    it('it must have allow array key value pairs as passthroughs', function (done) {
        let send = sinon.spy(XMLHttpRequest.prototype, "send");
        let globalCnt = 0;
        let localCnt = 0;
        let xhrReq = null;

        try {
            let element = DomQuery.byId("input_2").getAsElem(0).value;
            faces.ajax.addOnEvent(() => {
                globalCnt++;
            });


            faces.ajax.request(element, null, {
                execute: "input_1",
                render: "@form",
                params: [["pass1", "pass1"],
                    ["pass2", "pass2"]],

                onevent: (evt: any) => {
                    localCnt++;
                    if (evt.status == COMPLETE) {
                        expect(!!xhrReq.responseXML).to.be.true;
                    }
                    if (evt.status == SUCCESS) {
                        expect(this.jsfAjaxResponse.callCount).to.eq(1);

                        expect(this.jsfAjaxResponse.firstCall.args[0] instanceof XMLHttpRequest).to.be.true;
                        let lastArg = this.jsfAjaxResponse.firstCall.args[1];
                        expect(lastArg.onevent != null).to.be.true;
                        expect(lastArg.onevent instanceof Function).to.be.true;
                        expect(!!lastArg.onError).to.be.false;
                        expect(lastArg.pass1 == "pass1").to.be.true;
                        expect(lastArg.pass2 == "pass2").to.be.true;
                        expect(!!lastArg[P_AJAX_SOURCE]).to.be.true;
                        expect(!!lastArg[P_AJAX]).to.be.true;
                        expect(!!lastArg[P_EXECUTE]).to.be.true;
                        expect(!!lastArg[P_RENDER]).to.be.true;

                        expect(this.jsfAjaxResponse.firstCall.args.length).to.eq(2);

                        expect(globalCnt == 2).to.eq(true); //local before global
                        expect(localCnt == 3).to.eq(true);

                        done();
                    }
                }
            });

            xhrReq = this.requests[0];
            xhrReq.responsetype = "text/xml";
            xhrReq.respond(200, {'Content-Type': 'text/xml'}, STD_XML);


        } catch (e) {
            console.error(e);

        } finally {
            send.restore();
        }
    });

    it('it must have called request and the pass through values must be properly transferred into the context', function (done) {
        let send = sinon.spy(XMLHttpRequest.prototype, "send");
        let globalCnt = 0;
        let localCnt = 0;
        let xhrReq = null;

        try {
            let element = DomQuery.byId("input_2").getAsElem(0).value;
            faces.ajax.addOnEvent(() => {
                globalCnt++;
            });


            faces.ajax.request(element, null, {
                execute: "input_1",
                render: "@form",
                params: {
                    pass1: "pass1",
                    pass2: "pass2",
                },
                onevent: (evt: any) => {
                    localCnt++;
                    if (evt.status == COMPLETE) {
                        expect(!!xhrReq.responseXML).to.be.true;
                    }
                    if (evt.status == SUCCESS) {
                        expect(this.jsfAjaxResponse.callCount).to.eq(1);

                        expect(this.jsfAjaxResponse.firstCall.args[0] instanceof XMLHttpRequest).to.be.true;
                        let lastArg = this.jsfAjaxResponse.firstCall.args[1];
                        expect(lastArg.onevent != null).to.be.true;
                        expect(lastArg.onevent instanceof Function).to.be.true;
                        expect(!!lastArg.onError).to.be.false;
                        expect(lastArg.pass1 == "pass1").to.be.true;
                        expect(lastArg.pass2 == "pass2").to.be.true;
                        expect(!!lastArg[P_AJAX_SOURCE]).to.be.true;
                        expect(!!lastArg[P_AJAX]).to.be.true;
                        expect(!!lastArg[P_EXECUTE]).to.be.true;
                        expect(!!lastArg[P_RENDER]).to.be.true;

                        expect(this.jsfAjaxResponse.firstCall.args.length).to.eq(2);

                        expect(globalCnt == 2).to.eq(true); //local before global
                        expect(localCnt == 3).to.eq(true);

                        done();
                    }
                }
            });

            xhrReq = this.requests[0];
            xhrReq.responsetype = "text/xml";
            xhrReq.respond(200, {'Content-Type': 'text/xml'}, STD_XML);


        } catch (e) {
            console.error(e);

        } finally {
            send.restore();
        }
    });


    it('it must have called onError in the error case', function (done) {
        //on hold until it is clear why sinon is not giving me the response XML as expected

        let send = sinon.spy(XMLHttpRequest.prototype, "send");
        let xhrReq = null;
        const oldErr = console.error;
        try {
            let errorCnt = 0;

            let element = DomQuery.byId("input_2").getAsElem(0).value;
            faces.ajax.request(element, null, {
                execute: "input_1",
                render: "@form",
                params: {
                    pass1: "pass1",
                    pass2: "pass2",
                },
                onerror: (error: any) => {
                    expect(error.type).to.eq("error");
                    expect(error.status).to.eq(EMPTY_STR);
                    expect(!!error.message).to.eq(true);
                    expect(!!error.source?.id).to.eq(true);
                    expect(!!error.responseCode).to.eq(true);
                    expect(!!error.responseText).to.eq(true);
                    expect(!error.responseXML).to.eq(true);
                    done();
                },
                onevent: (evt: any) => {
                    if (evt.status == COMPLETE) {
                        console.error = () => {
                        };
                        throw Error("This error is wanted, ignore the log");
                    }
                }
            });

            xhrReq = this.requests[0];
            xhrReq.responsetype = "text/xml";
            xhrReq.respond(200, {'Content-Type': 'text/xml'}, STD_XML);

        } catch (e) {
            if (e.message.indexOf("This error is wanted") != -1) {
                return;
            }
            console.error(e);

        } finally {
            console.error = oldErr;
            send.restore();
        }

    });

    // We can cover this TCK issue in a simple code unit test, the case is simple enough
    it("must throw an error on invalid delays (MYFACES-4499, TCK_ISSUE320IT )", (done) => {

        let element = DomQuery.byId("input_2").getAsElem(0).value;
        try {
            faces.ajax.request(element, null, {
                execute: "input_1",
                render: "@form",
                delay: NaN,
                params: {
                    pass1: "pass1",
                    pass2: "pass2",
                }
            });
        } catch (e) {
            expect(e.message.indexOf("NaN") > 0).to.eq(true, "Invalid NaN in message");
            done();
            return;
        }
        done("Expecting a client error to be thrown")
    });

    it("must throw an error on invalid delays (MYFACES-4499, TCK_ISSUE320IT ) - 2", (done) => {

        let element = DomQuery.byId("input_2").getAsElem(0).value;
        try {
            faces.ajax.request(element, null, {
                execute: "input_1",
                render: "@form",
                delay: -1,
                params: {
                    pass1: "pass1",
                    pass2: "pass2",
                }
            });
        } catch (e) {
            expect(e.message.indexOf("-1") > 0).to
                .eq(true, "Invalid integer value in message");
            done();
            return;
        }
        done("Expecting a client error to be thrown")
    });
    it("must throw an error on invalid delays (MYFACES-4499, TCK_ISSUE320IT ) - 3", (done) => {

        let element = DomQuery.byId("input_2").getAsElem(0).value;
        try {
            faces.ajax.request(element, null, {
                execute: "input_1",
                render: "@form",
                delay: "booga",
                params: {
                    pass1: "pass1",
                    pass2: "pass2",
                }
            });
        } catch (e) {
            expect(e.message.indexOf("booga") > 0).to.be
                .eq(true, "Invalid string value in message");
            done();
            return;
        }
        done("Expecting a client error to be thrown")
    });

    it("must have a proper working myfaces.pps = true, partial page submit", function (done) {
        /**
         * derived from the passthrough test
         */
        let send = sinon.spy(XMLHttpRequest.prototype, "send");
        try {
            //we only issue input 1
            let element = DomQuery.byId("input_1").getAsElem(0).value;
            issueStdPPSReq(element);

            expect(send.called).to.be.true;
            let argsVal: any = send.args[0][0];
            let arsArr = argsVal.split("&");
            let resultsMap = {};
            for (let val of arsArr) {
                let keyVal = val.split("=");
                resultsMap[keyVal[0]] = keyVal[1];
            }

            expect(resultsMap?.["input_2_text"] ?? false).to.eq(false);
            expect(!!(resultsMap?.["input_1"] ?? false)).to.eq(true);
            expect(resultsMap["pass1"]).to.eq("pass1");
            expect(resultsMap["pass2"]).to.eq("pass2");
            expect(!!resultsMap["render"]).to.be.false;
            expect(!!resultsMap["execute"]).to.be.false;
            expect(P_WINDOW_ID in resultsMap).to.be.false;
            expect(P_VIEWSTATE in resultsMap).to.be.true;
            expect(resultsMap[P_AJAX_SOURCE]).to.eq("input_1");
            expect(resultsMap[P_AJAX]).to.eq("true");
            expect(resultsMap[P_RENDER]).to.eq("blarg");
            expect(resultsMap[P_EXECUTE]).to.eq("input_1");


            // TODO the request map only has the params and input1_ and input_2 passed no matter now many other values
            // we might have to add some input elements into the form which are filetered out
        } finally {
            send.restore();
        }
        done();
    });

    it("must handle the virtual issuing elements correctly", function() {

        const waitForResult = initVirtualElement();
        return waitForResult.then((close) => {
            const send = sinon.spy(XMLHttpRequest.prototype, "send");

            try {
                let dqElem = DomQuery.byId("page:numbers");
                let element = dqElem.getAsElem(0).value;
                dqElem.querySelectorAll("input").click();

                faces.ajax.request(element, null, {
                    execute: "jd_0:blarg",
                    render: "@form",
                    params: {
                        pass1: "pass1",
                        pass2: "pass2"
                    }
                });

                let argsVal: any = send.args[0][0];
                let arsArr = argsVal.split("&");
                let resultsMap = {};
                let doubles = 0;
                for (let val of arsArr) {
                    let keyVal = val.split("=");
                    if(!!resultsMap[keyVal[0]]) {
                        doubles++;
                    }
                    resultsMap[keyVal[0]] = keyVal[1];
                }

                expect(doubles).to.eq(2);

            } finally {

                send.restore();
            }

            return true;
        });

    });

    /**
     * the idea is if a checkbox or radio button is the issuing element
     * we cannot attach it if unchecked
     */
    it("must handle issuing checkboxes and radio buttons with values correctly", () => {
        const waitForResult = initCheckboxForm();
        return waitForResult.then((close) => {
            const send = sinon.spy(XMLHttpRequest.prototype, "send");
            let dqElem = DomQuery.byId("page:numbers:1");
            dqElem.removeAttribute("checked");
            let element = dqElem.getAsElem(0).value;

            faces.ajax.request(element, null, {
                execute: "form1",
                render: "@form",
                params: {
                    pass1: "pass1",
                    pass2: "pass2"
                }
            });

            let argsVal: any = send.args[0][0];
            let arsArr = argsVal.split("&");
            let resultsMap = {};
            let doubles = 0;
            for (let val of arsArr) {
                let keyVal = val.split("=");
                if(!!resultsMap[keyVal[0]]) {
                    doubles++;
                }
                resultsMap[keyVal[0]] = keyVal[1];
            }

            //TODO test assert here
            expect(resultsMap["page%3Anumbers%3A1"]).to.not.exist;
            //expect(doubles).to.eq(2);

        });
    });

    /**
     * the idea is if a checkbox or radio button is the issuing element
     *  if checked
     */
    it("must handle issuing checkboxes and radio buttons with values correctly", () => {
        const waitForResult = initCheckboxForm();
        return waitForResult.then((close) => {
            const send = sinon.spy(XMLHttpRequest.prototype, "send");
            let dqElem = DomQuery.byId("page:numbers:1");
            let element = dqElem.getAsElem(0).value;

            faces.ajax.request(element, null, {
                execute: "form1",
                render: "@form",
                params: {
                    pass1: "pass1",
                    pass2: "pass2"
                }
            });

            let argsVal: any = send.args[0][0];
            let arsArr = argsVal.split("&");
            let resultsMap = {};
            let doubles = 0;
            for (let val of arsArr) {
                let keyVal = val.split("=");
                if(!!resultsMap[keyVal[0]]) {
                    doubles++;
                }
                resultsMap[keyVal[0]] = keyVal[1];
            }

            //TODO test assert here
            expect(resultsMap["page%3Anumbers%3A1"]).to.eq("1");
            //expect(doubles).to.eq(2);

        });
    });

    it("must handle issuing checkboxes and radio buttons with values correctly", () => {
        const waitForResult = initCheckboxForm();
        return waitForResult.then((close) => {
            const send = sinon.spy(XMLHttpRequest.prototype, "send");
            let dqElem = DomQuery.byId("page:numbers:r:1");
            dqElem.removeAttribute("checked");
            let element = dqElem.getAsElem(0).value;

            faces.ajax.request(element, null, {
                execute: "form2",
                render: "@form",
                params: {
                    pass1: "pass1",
                    pass2: "pass2"
                }
            });

            let argsVal: any = send.args[0][0];
            let arsArr = argsVal.split("&");
            let resultsMap = {};
            let doubles = 0;
            for (let val of arsArr) {
                let keyVal = val.split("=");
                if(!!resultsMap[keyVal[0]]) {
                    doubles++;
                }
                resultsMap[keyVal[0]] = keyVal[1];
            }

            //TODO test assert here
            expect(resultsMap["page%3Anumbers%3Ar%3A1"]).to.not.exist;
            //expect(doubles).to.eq(2);

        });
    });

    it("must handle issuing checkboxes and radio buttons with values correctly with value", () => {
        const waitForResult = initCheckboxForm();
        return waitForResult.then((close) => {
            const send = sinon.spy(XMLHttpRequest.prototype, "send");
            let dqElem = DomQuery.byId("page:numbers:r:1");
            let element = dqElem.getAsElem(0).value;

            faces.ajax.request(element, null, {
                execute: "form2",
                render: "@form",
                params: {
                    pass1: "pass1",
                    pass2: "pass2"
                }
            });

            let argsVal: any = send.args[0][0];
            let arsArr = argsVal.split("&");
            let resultsMap = {};
            let doubles = 0;
            for (let val of arsArr) {
                let keyVal = val.split("=");
                if(!!resultsMap[keyVal[0]]) {
                    doubles++;
                }
                resultsMap[keyVal[0]] = keyVal[1];
            }

            //TODO test assert here
            expect(resultsMap["page%3Anumbers%3Ar%3A1"]).to.exist;
            //expect(doubles).to.eq(2);

        });
    });

    /**
     * https://issues.apache.org/jira/browse/MYFACES-4638
     */
    it("must pass values with & in its value correctly", function (done) {
        let send = sinon.spy(XMLHttpRequest.prototype, "send");
        let globalCnt = 0;
        let localCnt = 0;
        DomQuery.byId("input_1").val = "aaa&bbb";
        try {
            let element = DomQuery.byId("input_2").getAsElem(0).value;
            faces.ajax.addOnEvent(() => {
                globalCnt++;
            });
            faces.ajax.request(element, null, {
                execute: "input_1",
                render: "@form",
                params: {
                    pass1: "pass1",
                    pass2: "pass2"
                },
                message: "Hello World",
                onevent: (evt: any) => {
                    localCnt++;
                }
            });

            let xhrReq = this.requests[0];
            let requestBody = xhrReq.requestBody.split("&");

            xhrReq.respond(200, {'Content-Type': 'text/xml'}, STD_XML);
            expect(requestBody.indexOf("input_1=aaa%26bbb")).not.to.eq(-1);
            expect(requestBody.indexOf("pass1=pass1")).not.to.eq(-1);
            expect(requestBody.indexOf("pass2=pass2")).not.to.eq(-1);
            expect(requestBody.indexOf("message=Hello%20World")).not.to.eq(-1);

            expect(this.jsfAjaxResponse.callCount).to.eq(1);
            //success ommitted due to fake response
            expect(globalCnt == 3).to.eq(true);
            expect(localCnt == 3).to.eq(true);
            done();
        } catch (e) {
            console.error(e);

        } finally {
            send.restore();
        }
    });

});

