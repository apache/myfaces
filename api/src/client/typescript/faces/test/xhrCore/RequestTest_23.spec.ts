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

/**
 * Request test, testing on the 2.3 namespace fallback code
 *
 */
import {describe, it} from "mocha";
import * as sinon from "sinon";
import {expect} from "chai";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import {DomQuery} from "mona-dish";
import {
    COMPLETE, EMPTY_STR,
    SUCCESS
} from "../../impl/core/Const";
import * as nise from "nise";

/**
 * wherever we reference the namespaces they must be mapped to javax instead of jakarta
 */
function remapNamespacesFor23() {
    const P_PARTIAL_SOURCE = "javax.faces.source";
    const P_VIEWSTATE = "javax.faces.ViewState";
    const P_VIEWROOT = "javax.faces.ViewRoot";
    const P_VIEWHEAD = "javax.faces.ViewHead";
    const P_VIEWBODY = "javax.faces.ViewBody";
    const P_AJAX = "javax.faces.partial.ajax";
    const P_EXECUTE = "javax.faces.partial.execute";
    const P_RENDER = "javax.faces.partial.render";
    const P_EVT = "javax.faces.partial.event";
    const P_CLIENT_WINDOW = "javax.faces.ClientWindow";
    const P_RESET_VALUES = "javax.faces.partial.resetValues";
    const P_WINDOW_ID = "javax.faces.windowId";
    const ENCODED_URL = "javax.faces.encodedURL";
    return {
        P_PARTIAL_SOURCE, P_VIEWSTATE, P_VIEWROOT, P_VIEWHEAD, P_VIEWBODY,
        P_AJAX, P_EXECUTE, P_RENDER, P_EVT, P_CLIENT_WINDOW, P_RESET_VALUES,
        P_WINDOW_ID, ENCODED_URL
    }
}



let {
    P_PARTIAL_SOURCE, P_VIEWSTATE,
    P_AJAX, P_EXECUTE, P_RENDER,
    P_WINDOW_ID
} = remapNamespacesFor23();


const STD_XML = StandardInits.STD_XML;
const defaultMyFaces23 = StandardInits.defaultMyFaces23;
const HTML_PREFIX_EMBEDDED_BODY = StandardInits.HTML_PREFIX_EMBEDDED_BODY;

declare var jsf: any;
declare var Implementation: any;

let issueStdReq = function (element) {
    jsf.ajax.request(element, null, {
        execute: "input_1",
        render: "@form",
        pass1: "pass1",
        pass2: "pass2"
    });
};
/**
 * specialized tests testing the xhr core behavior when it hits the xmlHttpRequest object
 */
describe('Tests on the xhr core when it starts to call the request', function () {

    beforeEach(async function () {

        let waitForResult = defaultMyFaces23();

        return waitForResult.then((close) => {

            this.xhr = nise.fakeXhr.useFakeXMLHttpRequest();
            this.requests = [];
            this.xhr.onCreate = (xhr) => {
                this.requests.push(xhr);
            };
            (global as any).XMLHttpRequest = this.xhr;
            window.XMLHttpRequest = this.xhr;

            this.jsfAjaxResponse = sinon.spy((global as any).jsf.ajax, "response");

            this.closeIt = () => {
                (global as any).XMLHttpRequest = window.XMLHttpRequest = this.xhr.restore();
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
        //issue a standard jsf.ajax.request upon the standard simple form case and check the passed parameters
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

            //sent params jakarta.jsf.ViewState=null&execute=input_1&render=%40form&pass1=pass1&pass2=pass2&jakarta.jsf.windowId=null&jakarta.jsf.source=input_2&jakarta.jsf.partial.ajax=input_2&blarg=blarg&jakarta.jsf.partial.execute=input_1%20input_2&jakarta.jsf.partial.render=blarg

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

        let waitForResult = defaultMyFaces23();

        return waitForResult.then((close) => {

            this.xhr = nise.fakeXhr.useFakeXMLHttpRequest();
            this.requests = [];
            this.xhr.onCreate = (xhr) => {
                this.requests.push(xhr);
            };
            (global as any).XMLHttpRequest = this.xhr = nise.fakeXhr.useFakeXMLHttpRequest();

            // @ts-ignore
            window.XMLHttpRequest = this.xhr = nise.fakeXhr.useFakeXMLHttpRequest() as XMLHttpRequest;

            this.jsfAjaxResponse = sinon.spy((global as any).jsf.ajax, "response");

            this.closeIt = () => {
                (global as any).XMLHttpRequest = window.XMLHttpRequest = this.xhr.restore();
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
            jsf.ajax.addOnEvent(() => {
                globalCnt++;
            });
            jsf.ajax.request(element, null, {
                execute: "input_1",
                render: "@form",
                pass1: "pass1",
                pass2: "pass2",
                onevent: () => {
                    localCnt++;
                }
            });

            let xhrReq = this.requests[0];

            xhrReq.respond(200, {'Content-Type': 'text/xml'}, STD_XML);
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

    it('it must have called request and the pass through values must be properly transferred into the context', function (done) {
        let send = sinon.spy(XMLHttpRequest.prototype, "send");
        let globalCnt = 0;
        let localCnt = 0;
        let xhrReq = null;

        try {
            let element = DomQuery.byId("input_2").getAsElem(0).value;
            jsf.ajax.addOnEvent(() => {
                globalCnt++;
            });


            jsf.ajax.request(element, null, {
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



    it('it must have called onError in the error case', function (done) {
        //on hold until it is clear why sinon is not giving me the response XML as expected

        let send = sinon.spy(XMLHttpRequest.prototype, "send");
        let xhrReq = null;
        let oldErr = console.error;
        try {
            let element = DomQuery.byId("input_2").getAsElem(0).value;
            jsf.ajax.request(element, null, {
                execute: "input_1",
                render: "@form",
                pass1: "pass1",
                pass2: "pass2",
                onerror: (error: any) => {
                    expect(error.type).to.eq("error");
                    expect(error.status).to.eq(null);
                    expect(!!error.errorMessage).to.eq(true);
                    expect(!!error.source).to.eq(true);
                    expect(!!error.responseCode).to.eq(true);
                    expect(typeof error.responseCode).to.eq("number");
                    expect(!!error.responseText).to.eq(true);
                    expect(!error.responseXML).to.eq(true);
                    done();
                },
                onevent: (evt: any) => {
                    if (evt.status == COMPLETE) {
                        console.error = () => {};
                        throw Error("This error is wanted, ignore the log");
                    }
                }
            });

            xhrReq = this.requests[0];
            xhrReq.responsetype = "text/xml";
            xhrReq.respond(200, {'Content-Type': 'text/xml'}, STD_XML);

        } catch (e) {
            if(e.message.indexOf("This error is wanted") != -1) {
                return;
            }
            console.error(e);

        } finally {
            console.error = oldErr;
            send.restore();
        }

    });

    it("must handle prefixed inputs properly (prefixes must be present)", function (done) {
        window.document.body.innerHTML = HTML_PREFIX_EMBEDDED_BODY;

        //we now run the tests here
        try {

            let event = {
                isTrusted: true,
                type: 'change',
                target: document.getElementById("page:input::field"),
                currentTarget: document.getElementById("page:input::field")
            };
            jsf.ajax.request(document.getElementById("page:input"), event as any, {
                render: "page:output",
                execute: "page:input",
                params: {
                    "booga2.xxx": "yyy",
                    "javax.faces.behavior.event": "change",
                    "booga": "bla"
                },
            });
        } catch (err) {
            console.error(err);
            expect(false).to.eq(true);
        }
        const requestBody = this.requests[0].requestBody;
        //We check if the base64 encoded string matches the original
        expect(requestBody.indexOf("javax.faces.behavior.event")).to.not.eq(-1);
        expect(requestBody.indexOf("javax.faces.behavior.event=change")).to.not.eq(-1);
        expect(requestBody.indexOf("page%3Ainput=input_value")).to.not.eq(-1);
        done();
    });


});

