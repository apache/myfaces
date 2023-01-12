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
import * as sinon from "sinon";
import {Implementation} from "../../impl/AjaxImpl";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";

import protocolPage = StandardInits.protocolPage;
import {DQ} from "mona-dish";
import {XhrFormData} from "../../impl/xhrCore/XhrFormData";
import {expect} from "chai";
import prefixPage = StandardInits.prefixEmbeddedPage;
import prefixEmbeddedPage = StandardInits.prefixEmbeddedPage;
import HTML_PREFIX_EMBEDDED_BODY = StandardInits.HTML_PREFIX_EMBEDDED_BODY;
import {it} from "mocha";

describe("test for proper request param patterns identical to the old implementation", function () {
    const UPDATE_INSERT_2 = {
        "op": "updateinsert2",
        "jakarta.faces.partial.event": "click",
        "jakarta.faces.source": "cmd_update_insert2",
        "jakarta.faces.partial.ajax": "true",
        "jakarta.faces.partial.execute": "cmd_update_insert2",
        "form1": "form1",
        "jakarta.faces.ViewState": "blubbblubblubb"
    }
    /**
     * matches two maps for absolute identicality
     */
    let matches = (item1: { [key: string]: any }, item2: { [key: string]: any }): boolean => {
        if (Object.keys(item1).length != Object.keys(item2).length) {
            return false;
        }
        for (let key in item1) {
            if ((!(key in item2)) || item1[key] != item2[key]) {
                return false;
            }
        }
        return true;
    }


    beforeEach(async function () {

        let waitForResult = protocolPage();

        return waitForResult.then((close) => {

            this.xhr = sinon.useFakeXMLHttpRequest();
            this.requests = [];
            this.xhr.onCreate = (xhr) => {
                this.requests.push(xhr);
            };
            (<any>global).XMLHttpRequest = this.xhr;
            window.XMLHttpRequest = this.xhr;

            this.jsfAjaxResponse = sinon.stub((<any>global).faces.ajax, "response");

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

    it("must pass updateinsert2 with proper parameters", function () {
        DQ.byId("cmd_update_insert2").click();

        let requestBody = this.requests[0].requestBody;
        let formData = new XhrFormData(requestBody);

        expect(matches(formData.value, UPDATE_INSERT_2)).to.be.true;

    });


    it("must handle base64 encoded strings properly as request data", function () {
        let probe = "YWFhYWFhc1Rlc3RpdCDDpGtvNDU5NjczMDA9PSsrNDU5MGV3b3UkJiUmLyQmJQ==";
        DQ.byId("jakarta.faces.ViewState").inputValue.value = probe;
        DQ.byId("cmd_update_insert2").click();
        let requestBody = this.requests[0].requestBody;
        //We check if the base64 encoded string matches the original
        let formData = new XhrFormData(requestBody);

        expect(decodeURIComponent(formData.getIf("jakarta.faces.ViewState").value) == probe).to.be.true;
    });


    it("must handle empty parameters properly", function () {
        let probe = "";
        DQ.byId("jakarta.faces.ViewState").inputValue.value = probe;
        DQ.byId("cmd_update_insert2").click();
        let requestBody = this.requests[0].requestBody;
        //We check if the base64 encoded string matches the original
        let formData = new XhrFormData(requestBody);

        expect(decodeURIComponent(formData.getIf("jakarta.faces.ViewState").value) == probe).to.be.true;
    });

    //KssbpZfCe+0lwDhgMRQ44wRFkaM1o1lbMMUO3lini5YhXWm6

    it("must handle base64 special cases properly (+ in encoding)", function () {
        let probe = "KssbpZfCe+0lwDhgMRQ44wRFkaM1o1lbMMUO3lini5YhXWm6";
        DQ.byId("jakarta.faces.ViewState").inputValue.value = probe;
        DQ.byId("cmd_update_insert2").click();
        let requestBody = this.requests[0].requestBody;
        //We check if the base64 encoded string matches the original
        let formData = new XhrFormData(requestBody);

        expect(decodeURIComponent(formData.getIf("jakarta.faces.ViewState").value) == probe).to.be.true;
    });

    it("must handle prefixed inputs properly (prefixes must be present) faces4", function (done) {
        window.document.body.innerHTML = HTML_PREFIX_EMBEDDED_BODY;

        //we now run the tests here
        try {

            let event = {
                isTrusted: true,
                type: 'change',
                target: document.getElementById("page:input::field"),
                currentTarget: document.getElementById("page:input::field")
            };
            faces.ajax.request(document.getElementById("page:input"), event as any, {
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




    /**
     * This test is based on Tobago 6 (Jakarte EE 9).
     */
    it("must handle ':' in IDs properly", function (done) {
        window.document.body.innerHTML = `

<tobago-page locale="en" class="container-fluid" id="page" focus-on-error="true" wait-overlay-delay-full="1000" wait-overlay-delay-ajax="1000">
    <form action="/content/010-input/10-in/In.xhtml?jfwid=q6qbeuqed" id="page::form" method="post" accept-charset="UTF-8" data-tobago-context-path="">
        <input type="hidden" name="jakarta.faces.source" id="jakarta.faces.source" disabled="disabled">
        <tobago-focus id="page::lastFocusId">
            <input type="hidden" name="page::lastFocusId" id="page::lastFocusId::field">
        </tobago-focus>
        <input type="hidden" name="org.apache.myfaces.tobago.webapp.Secret" id="org.apache.myfaces.tobago.webapp.Secret" value="secretValue">
        <tobago-in id="page:input" class="tobago-auto-spacing">
            <input type="text" name="page:input" id="page:input::field" class="form-control" value="Bob">
            <tobago-behavior event="change" client-id="page:input" field-id="page:input::field" execute="page:input" render="page:output"></tobago-behavior>
        </tobago-in>
        <tobago-out id="page:output" class="tobago-auto-spacing">
            <span class="form-control-plaintext"></span>
        </tobago-out>
        <div class="tobago-page-menuStore">
        </div>
        <span id="page::faces-state-container">
            <input type="hidden" name="jakarta.faces.ViewState" id="j_id__v_0:jakarta.faces.ViewState:1" value="viewStateValue" autocomplete="off">
            <input type="hidden" name="jakarta.faces.RenderKitId" value="tobago">
            <input type="hidden" id="j_id__v_0:jakarta.faces.ClientWindow:1" name="jakarta.faces.ClientWindow" value="clientWindowValue">
        </span>
    </form>
</tobago-page>
`;

        //we now run the tests here
        try {

            let event = {
                isTrusted: true,
                type: 'change',
                target: document.getElementById("page:input::field"),
                currentTarget: document.getElementById("page:input::field")
            };
            global.debug2 = true;
            faces.ajax.request(document.getElementById("page:input"), event as any, {
                "jakarta.faces.behavior.event": 'change',
                execute: "page:input",
                render: "page:output"
            });
        } catch (err) {
            console.error(err);
            expect(false).to.eq(true);
        }
        const requestBody = this.requests[0].requestBody;
        expect(requestBody.indexOf("org.apache.myfaces.tobago.webapp.Secret=secretValue")).to.not.eq(-1);
        expect(requestBody.indexOf("page%3Ainput=Bob")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.ViewState=viewStateValue")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.RenderKitId=tobago")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.ClientWindow=clientWindowValue")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.behavior.event=change")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.partial.event=change")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.source=page%3Ainput")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.partial.ajax=true")).to.not.eq(-1);
        expect(requestBody.indexOf("page%3A%3Aform=page%3A%3Aform")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.partial.execute=page%3Ainput")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.partial.render=page%3Aoutput")).to.not.eq(-1);
        done();
    });

});