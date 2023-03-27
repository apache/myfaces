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


import {describe} from "mocha";
import * as sinon from "sinon";
import {$nsp} from "../../impl/core/Const";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import {XmlResponses} from "../frameworkBase/_ext/shared/XmlResponses";
import {expect} from "chai";
import {Implementation} from "../../impl/AjaxImpl";
import errorChainPage = StandardInits.errorChainPage;
import {DQ} from "mona-dish";

/**
 * Tests for error recover if an error is triggered mid chain
 */
describe('Tests of the various aspects of the response protocol functionality', function () {

    beforeEach(async function () {
        let waitForResult = errorChainPage(true);
        return waitForResult.then((close) => {

            this.xhr = sinon.useFakeXMLHttpRequest();
            this.requests = [];

            this.respond = (response: string): XMLHttpRequest => {
                response = $nsp(response);
                let xhrReq = this.requests.shift();
                xhrReq.responsetype = "text/xml";
                xhrReq.respond(200, {'Content-Type': 'text/xml'}, response);
                return xhrReq;
            };

            this.respond403 = (response: string): XMLHttpRequest => {
                let xhrReq = this.requests.shift();
                xhrReq.responsetype = "text/html";
                xhrReq.respond(403, {'Content-Type': 'text/html'}, "server error");
                return xhrReq;
            };

            this.xhr.onCreate = (xhr) => {
                this.requests.push(xhr);
            };
            (<any>global).XMLHttpRequest = this.xhr;
            window.XMLHttpRequest = this.xhr;

            this.closeIt = () => {
                (<any>global).XMLHttpRequest = window.XMLHttpRequest = this.xhr.restore();
                Implementation.reset();
                close();
            }
        });
    });

    afterEach(function () {
        this.closeIt();
    });

    it("No Error Case", function (done) {
        DQ.byId("form1:button1").click();
        expect(Implementation.requestQueue.queue.length >= 4).to.be.true;
        for (let cnt = 1; cnt <= 5; cnt++) {
            if(!Implementation.requestQueue.queue.length) {
                //the last non queued element waits for another response
                this.respond(XmlResponses.ERROR_CHAIN_RESPOND_OK(cnt));
                break;
            }
            this.respond(XmlResponses.ERROR_CHAIN_RESPOND_OK(cnt))
        }


        expect(DQ.byId("errorCalled").innerHTML).to.eq("");
        expect(DQ.byId("form1:out1").innerHTML).to.eq("5");
        done();
    })

    it("must process the error chain properly", function (done) {
        DQ.byId("form1:button1").click();
        expect(Implementation.requestQueue.queue.length >= 4).to.be.true;

        for (let cnt = 1; cnt <= 5; cnt++) {
            if(!Implementation.requestQueue.queue.length) {
                this.respond(XmlResponses.ERROR_CHAIN_RESPOND_OK(cnt));
                break;
            }
            if (cnt == 3) {
                //any error suffices
                this.respond403();
            } else {
                this.respond(XmlResponses.ERROR_CHAIN_RESPOND_OK(cnt))
            }
        }
        expect(Implementation.requestQueue.queue.length).to.eq(0);
        expect(DQ.byId("errorCalled").innerHTML).to.eq("1");
        expect(DQ.byId("form1:out1").innerHTML).to.eq("5");
        done();
    })


    it("must process the error chain properly with a standard Ajax Error", function (done) {
        DQ.byId("form1:button1").click();
        expect(Implementation.requestQueue.queue.length >= 4).to.be.true;

        for (let cnt = 1; cnt <= 5; cnt++) {
            if(!Implementation.requestQueue.queue.length) {
                this.respond(XmlResponses.ERROR_CHAIN_RESPOND_OK(cnt));
                break;
            }
            if (cnt == 3) {
                //any error suffices
                this.respond(XmlResponses.ERROR_1);
            } else {
                this.respond(XmlResponses.ERROR_CHAIN_RESPOND_OK(cnt))
            }
        }
        expect(Implementation.requestQueue.queue.length).to.eq(0);
        expect(DQ.byId("errorCalled").innerHTML).to.eq("1");
        expect(DQ.byId("form1:out1").innerHTML).to.eq("5");
        done();
    })
});

