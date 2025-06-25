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
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import * as sinon from "sinon";


import {XmlResponses} from "../frameworkBase/_ext/shared/XmlResponses";
import {expect} from "chai";
import {DomQuery} from "mona-dish";
import shadowDomMyFaces = StandardInits.shadowDomMyFaces;


sinon.reset();

declare var Implementation: any;
declare const window: any;

describe('shadow dom testsuite', () => {

    beforeEach(async function () {
        let waitForResult = shadowDomMyFaces();
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


    it("shadow dom behavior tested", function() {
        //const addRequestToQueue = sinon.stub(Impl.queueHandler, "addRequestToQueue");

        expect(DomQuery.querySelectorAll("#shadowDomArea").length).to.eq(1);
        expect(DomQuery.querySelectorAll("* /shadow/ #shadowContent").length).to.eq(1);

        DomQuery.byId("blarg:input_1", true).addEventListener("click", (event: Event) => {
            window.faces.ajax.request(event.target, event, {render: 'shadowContent', execute: '@this'})
        }).click();
        this.respond(XmlResponses.SHADOW_DOM_UPDATE);

        //check if element is updated
        expect(DomQuery.byId("shadowContent", true).innerHtml).to.eq("after update");
        /*shadow dom in place now lets do an update on the element*/
    });


    it("shadow dom updated triggered from outside", function() {
        //const addRequestToQueue = sinon.stub(Impl.queueHandler, "addRequestToQueue");

        expect(DomQuery.querySelectorAll("#shadowDomArea").length).to.eq(1);
        expect(DomQuery.querySelectorAll("* /shadow/ #shadowContent").length).to.eq(1);


        DomQuery.byId("input_3", true).addEventListener("click", (event: Event) => {
           window.faces.ajax.request(event.target, event, {render: 'shadowContent', execute: '@this'});
        }).click();
        this.respond(XmlResponses.SHADOW_DOM_UPDATE);

        //check if element is updated
        expect(DomQuery.byId("shadowContent", true).innerHtml).to.eq("after update");
        /*shadow dom in place now lets do an update on the element*/
    });
});
