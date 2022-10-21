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
import { expect } from "chai";

describe("test for proper request param patterns identical to the old implementation", function () {
    const DELETE_PATTERN = {
        op: "delete1",
        "jakarta.faces.source": "cmd_delete",
        "jakarta.faces.partial.event": "click",
        "jakarta.faces.partial.ajax": "true",
        "jakarta.faces.partial.execute": "cmd_delete",
        "form1": "form1",
        "jakarta.faces.ViewState": "blubbblubblubb"
    }

    const UPDATE_INSERT_2 = {
        "op": "updateinsert2",
        "jakarta.faces.partial.event": "click",
        "jakarta.faces.source": "cmd_update_insert2",
        "jakarta.faces.partial.ajax": "true",
        "jakarta.faces.partial.execute": "cmd_update_insert2",
        "form1": "form1",
        "jakarta.faces.ViewState": "blubbblubblubb"
    }

    const ERRORS = {
        "op": "errors",
        "jakarta.faces.partial.event": "click",
        "jakarta.faces.source": "cmd_error",
        "jakarta.faces.partial.ajax": "true",
        "jakarta.faces.partial.execute": "cmd_error",
        "form1": "form1",
        "jakarta.faces.ViewState": "blubbblubblubb"
    }

    /**
     * matches two maps for absolute identicality
     */
    let matches = (item1: {[key: string]: any}, item2: {[key: string]: any}): boolean => {
        if(Object.keys(item1).length != Object.keys(item2).length) {
            return false;
        }
        for(let key in item1) {
            if((!(key in item2)) || item1[key] != item2[key]) {
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

    it("must pass updateinsert2 with proper parameters", function() {
        DQ.byId("cmd_update_insert2").click();

        let requestBody = this.requests[0].requestBody;
        let formData = new XhrFormData(requestBody);

        expect(matches(formData.value, UPDATE_INSERT_2)).to.be.true;

    });


    it("must handle base64 encoded strings properly as request data", function() {
        let probe = "YWFhYWFhc1Rlc3RpdCDDpGtvNDU5NjczMDA9PSsrNDU5MGV3b3UkJiUmLyQmJQ==";
        DQ.byId("jakarta.faces.ViewState").inputValue.value = probe;
        DQ.byId("cmd_update_insert2").click();
        let requestBody = this.requests[0].requestBody;
        //We check if the base64 encoded string matches the original
        let formData = new XhrFormData(requestBody);

        expect(decodeURIComponent(formData.getIf("jakarta.faces.ViewState").value) == probe).to.be.true;
    });


    it("must handle empty parameters properly", function() {
        let probe = "";
        DQ.byId("jakarta.faces.ViewState").inputValue.value = probe;
        DQ.byId("cmd_update_insert2").click();
        let requestBody = this.requests[0].requestBody;
        //We check if the base64 encoded string matches the original
        let formData = new XhrFormData(requestBody);

        expect(decodeURIComponent(formData.getIf("jakarta.faces.ViewState").value) == probe).to.be.true;
    });

    //KssbpZfCe+0lwDhgMRQ44wRFkaM1o1lbMMUO3lini5YhXWm6

    it("must handle base64 special cases properly (+ in encoding)", function() {
        let probe = "KssbpZfCe+0lwDhgMRQ44wRFkaM1o1lbMMUO3lini5YhXWm6";
        DQ.byId("jakarta.faces.ViewState").inputValue.value = probe;
        DQ.byId("cmd_update_insert2").click();
        let requestBody = this.requests[0].requestBody;
        //We check if the base64 encoded string matches the original
        let formData = new XhrFormData(requestBody);

        expect(decodeURIComponent(formData.getIf("jakarta.faces.ViewState").value) == probe).to.be.true;
    });
});