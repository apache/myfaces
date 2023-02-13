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
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import {Implementation} from "../../impl/AjaxImpl";

import {expect} from "chai";
import protocolPage = StandardInits.protocolPage;
import {DQ} from "mona-dish";
import {XmlResponses} from "../frameworkBase/_ext/shared/XmlResponses";

declare var faces: any;

describe('tests the addOnEvent and addOnError handling', function () {

    // noinspection DuplicatedCode
    beforeEach(async function () {
        let waitForResult = protocolPage();
        return waitForResult.then((close) => {

            this.xhr = sinon.useFakeXMLHttpRequest();
            this.requests = [];

            this.respond = (response: string): XMLHttpRequest => {
                let xhrReq = (this.requests as Array<any>).shift();
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
    it("must have a global add on event call with all three phases passed", function () {
        let onEventCalled1 = 0;
        let onEventCalled2 = 0;

        faces.ajax.addOnEvent((data: any) => {
            onEventCalled1++;
            if (onEventCalled1 == 1 && data.status != "begin") {
                throw ("Wrong status")
            }
            if (onEventCalled1 == 2 && data.status != "complete") {
                throw ("Wrong status")
            }
            if (onEventCalled1 == 3 && data.status != "success") {
                throw ("Wrong status")
            }
        });
        faces.ajax.addOnEvent(() => {
            onEventCalled2++;
        });
        DQ.byId("cmd_update_insert").click();
        this.respond(XmlResponses.UPDATE_INSERT_1);

        expect(onEventCalled1).to.eq(3);
        expect(onEventCalled2).to.eq(3);
    });

    it("must have onError called in case of error", function () {
        let onErrorCalled1 = 0;
        let onErrorCalled2 = 0;

        let errorTitle = '';
        let errorMessage = '';
        faces.ajax.addOnError((data: any) => {
            errorTitle = data.errorName;
            errorMessage = data.errorMessage;
            onErrorCalled1++
        });
        faces.ajax.addOnError(() => {
            onErrorCalled2++;
        });

        //cmd_error_component
        DQ.byId("cmd_error_component").click();
        this.respond(XmlResponses.ERROR_2);

        expect(onErrorCalled1).to.eq(1);
        expect(onErrorCalled2).to.eq(1);
        expect(errorTitle).to.eq('Erro21');
        expect(errorMessage).to.eq('serverError: Error2 Text');
    });

    it("must have an id set if there is an emitting element", function () {
        let onEventCalled2 = 0;

        let assertSourceExists = (data: any) => {
            expect(!!data?.source?.id).to.be.true;
        }

        faces.ajax.addOnEvent((data: any) => {
            assertSourceExists(data);
        });
        faces.ajax.addOnEvent(() => {
            onEventCalled2++;

        });
        DQ.byId("cmd_update_insert").click();
        this.respond(XmlResponses.UPDATE_INSERT_1);

    });

});
