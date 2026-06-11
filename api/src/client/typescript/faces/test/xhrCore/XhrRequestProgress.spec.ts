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

import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import * as sinon from "sinon";
import {Implementation} from "../../impl/AjaxImpl";
import {expect} from "chai";
const protocolPage = StandardInits.protocolPage;

const jsdom = require("jsdom");
const {JSDOM} = jsdom;
import * as nise from "nise";

describe("Should trigger the progress on xhr request", function () {
    beforeEach(async function () {
        let waitForResult = protocolPage();

        //build up the test fixture
        return waitForResult.then((close) => {
            //we generate an xhr mock class replacement
            this.xhr = nise.fakeXhr.useFakeXMLHttpRequest();
            this.requests = [];

            //we store the requests to have access to them later
            this.xhr.onCreate = (xhr) => {
                this.requests.push(xhr);
            };
            //we anchchor the mock into the fake dom
            (global as any).XMLHttpRequest = this.xhr;
            window.XMLHttpRequest = this.xhr;

            //general cleanup of overloaded resources
            this.closeIt = () => {
                (global as any).XMLHttpRequest = window.XMLHttpRequest = this.xhr.restore();
                Implementation.reset();
                close();
            };
        });
    });
    afterEach(function () {
        this.closeIt();
    });

    it("must trigger progress on xhr request", function() {
        let caughtProgressEvents = [];
        var preinitTriggered = false;
        var loadstartTriggered = false;
        var loadTriggered = false;
        var loadendTriggered = false;
        var timeoutTriggered = false;
        var abortTriggered = false;
        var errorTriggered = false;
        faces.ajax.request(document.getElementById("cmd_eval"), null,
            {
                render: '@form',
                execute: '@form',
                myfaces: {
                    upload: {
                        progress: (upload: XMLHttpRequestUpload, event: ProgressEvent) => {
                            caughtProgressEvents.push(event);
                        },
                        preinit: (upload: XMLHttpRequestUpload) => preinitTriggered = true,
                        loadstart: (upload: XMLHttpRequestUpload, event: ProgressEvent) => loadstartTriggered = true,
                        load: (upload: XMLHttpRequestUpload,  event: ProgressEvent) => loadTriggered = true,
                        loadend: (upload: XMLHttpRequestUpload,  event: ProgressEvent) => loadendTriggered = true,
                        error: (upload: XMLHttpRequestUpload,  event: ProgressEvent) => errorTriggered = true,
                        abort: (upload: XMLHttpRequestUpload,  event: ProgressEvent) => abortTriggered = true,
                        timeout: (upload: XMLHttpRequestUpload,  event: ProgressEvent) => timeoutTriggered = true,

                    }
                }
            });

        let progressEvent = new ProgressEvent("progress");
        let progressEvent2 =  new ProgressEvent("progress");
        let xhr = this.requests.shift();
        xhr.upload.dispatchEvent(new ProgressEvent("loadstart"));
        xhr.upload.dispatchEvent(new ProgressEvent("load"));
        xhr.upload.dispatchEvent(progressEvent);
        xhr.upload.dispatchEvent(progressEvent2);
        xhr.upload.dispatchEvent(new ProgressEvent("loadend"));
        xhr.upload.dispatchEvent(new ProgressEvent("error"));
        xhr.upload.dispatchEvent(new ProgressEvent("abort"));
        xhr.upload.dispatchEvent(new ProgressEvent("timeout"));

        expect(caughtProgressEvents.length).to.eq(2);
        expect(caughtProgressEvents[0] === progressEvent).to.eq(true);
        expect(caughtProgressEvents[1] === progressEvent2).to.eq(true);

        expect(preinitTriggered).to.eq(true);
        expect(loadstartTriggered).to.eq(true);
        expect(loadTriggered).to.eq(true);
        expect(loadendTriggered).to.eq(true);
        expect(errorTriggered).to.eq(true);
        expect(abortTriggered).to.eq(true);
        expect(timeoutTriggered).to.eq(true);
    });
});