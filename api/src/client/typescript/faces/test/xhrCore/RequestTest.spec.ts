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
import {DomQuery} from "mona-dish";
import {
    COMPLETE,
    P_AJAX,
    P_EXECUTE,
    P_PARTIAL_SOURCE,
    P_RENDER,
    P_VIEWSTATE,
    P_WINDOW_ID,
    SUCCESS
} from "../../impl/core/Const";
import defaultMyFaces = StandardInits.defaultMyFaces;
import STD_XML = StandardInits.STD_XML;

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
/**
 * specialized tests testing the xhr core behavior when it hits the xmlHttpRequest object
 */
describe('Tests on the xhr core when it starts to call the request', function () {

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

            expect(resultsMap["pass1"]).to.eq("pass1");
            expect(resultsMap["pass2"]).to.eq("pass2");
            expect(!!resultsMap["render"]).to.be.false;
            expect(!!resultsMap["execute"]).to.be.false;
            expect(P_WINDOW_ID in resultsMap).to.be.false;
            expect(P_VIEWSTATE in resultsMap).to.be.true;
            expect(resultsMap[P_PARTIAL_SOURCE]).to.eq("input_2");
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
                        expect(!!lastArg[P_PARTIAL_SOURCE]).to.be.true;
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
                        expect(!!lastArg[P_PARTIAL_SOURCE]).to.be.true;
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
                        expect(!!lastArg[P_PARTIAL_SOURCE]).to.be.true;
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
                    expect(!!error.status).to.eq(true);
                    expect(!!error.message).to.eq(true);
                    expect(!!error.source).to.eq(true);
                    expect(!!error.responseCode).to.eq(true);
                    expect(!!error.responseText).to.eq(true);
                    expect(!error.responseXML).to.eq(true);
                    done();
                },
                onevent: (evt: any) => {
                    if (evt.status == COMPLETE) {
                        throw Error("This error is wanted, ignore the log");
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
});

