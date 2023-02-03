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
import defaultFileForm = StandardInits.defaultFileForm;
import {Implementation} from "../../impl/AjaxImpl";

declare var faces: any;

/**
 * specialized tests testing the xhr core behavior when it hits the xmlHttpRequest object
 */
describe('Tests on the xhr core when it starts to call the request', function () {
    beforeEach(async function () {

        let waitForResult = defaultFileForm();
        return waitForResult.then((close) => {

            this.xhr = sinon.useFakeXMLHttpRequest();
            this.requests = [];

            this.respond = (response: string): XMLHttpRequest => {
                let xhrReq = this.requests.shift();
                xhrReq.responsetype = "text/xml";
                xhrReq.respond(200, {'Content-Type': 'text/xml'}, response);
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

    it('must have sent a form multipart request', function (done) {
        let send = sinon.spy(XMLHttpRequest.prototype, "send");
        const CONTENT_TYPE = "Content-Type";
        const POST = "POST";

        global["debug_f"] = true;

        try {

            let button = DomQuery.byId("input_1");

            button.addEventListener("click", (event: Event) => {
                faces.ajax.request(event.target, event, {render: '@all', execute: '@form'})
            }).click();
            //this.resonse("ok");

            expect(this.requests.length).to.eq(1);
            let request = this.requests[0];
            expect(request.method).to.eq(POST);
            expect(request.async).to.be.true;
            expect(send.called).to.be.true;
            expect(send.callCount).to.eq(1);

            expect(request.requestBody instanceof FormData).to.be.true;
            expect(request.requestHeaders[CONTENT_TYPE] == undefined).to.be.true;

        } finally {
            send.restore();
        }
        done();
    });

    it('must have sent a multipart request with a dedicated execute', function (done) {
        let send = sinon.spy(XMLHttpRequest.prototype, "send");

        const CONTENT_TYPE = "Content-Type";
        const POST = "POST";

        try {
            let button = DomQuery.byId("input_1");

            button.addEventListener("click", (event: Event) => {
                faces.ajax.request(event.target, event, {render: '@all', execute: 'input_1 fíleupload'})
            }).click();
            //this.resonse("ok");

            expect(this.requests.length).to.eq(1);
            let request = this.requests[0];
            expect(request.method).to.eq(POST);
            expect(request.async).to.be.true;
            expect(send.called).to.be.true;
            expect(send.callCount).to.eq(1);
            expect(request.requestBody instanceof FormData).to.be.true;
            //undefined means the browser fills in the rest
            expect(request.requestHeaders[CONTENT_TYPE] == undefined).to.be.true;

        } finally {
            send.restore();
        }
        done();
    });

    it('must have sent a single part request with a dedicated execute', function (done) {
        let send = sinon.spy(XMLHttpRequest.prototype, "send");

        const CONTENT_TYPE = "Content-Type";
        const MULTIPART_FORM = "multipart/form-data";
        const POST = "POST";

        try {
            let button = DomQuery.byId("input_1");

            button.addEventListener("click", (event: Event) => {
                faces.ajax.request(event.target, event, {render: '@all', execute: 'input_1 fileupload'});
            }).click();

            expect(this.requests.length).to.eq(1);
            let request = this.requests[0];
            expect(request.method).to.eq(POST);
            expect(request.async).to.be.true;
            expect(send.called).to.be.true;
            expect(send.callCount).to.eq(1);
            expect(request.requestBody instanceof FormData).to.be.false;
            expect(request.requestHeaders[CONTENT_TYPE].indexOf(MULTIPART_FORM) == -1).to.be.true;

        } finally {
            send.restore();
        }
        done();
    });

});
