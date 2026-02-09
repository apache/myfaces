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
import * as nise from "nise";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import {Implementation} from "../../impl/AjaxImpl";

import {expect} from "chai";

const defaultMyFaces = StandardInits.defaultMyFaces;
import {_Es2019Array, Lang} from "mona-dish";
import {FakeWebsocket} from "./FakeWebsocket";
const assertType = Lang.assertType;

declare var faces: any;

describe('Tests the jsf websocket client side api on high level (generic test without any myfaces dependencies', function () {
    let oldFlatMap = null;
    beforeEach(async function () {
        let oldFlatMap = null;
        let waitForResult = defaultMyFaces();

        return waitForResult.then((close) => {

            this.xhr = nise.fakeXhr.useFakeXMLHttpRequest();
            this.requests = [];
            this.xhr.onCreate = (xhr) => {
                this.requests.push(xhr);
            };
            (global as any).XMLHttpRequest = this.xhr;
            window.XMLHttpRequest = this.xhr;

            this.jsfAjaxResponse = sinon.stub((global as any).faces.ajax, "response");

            this.fakeWebsocket = new FakeWebsocket();
            this.socket = sinon.stub(window, 'WebSocket').returns(this.fakeWebsocket);
            (global as any).WebSocket = this.socket;

            this.pushImpl = (global as any).PushImpl;
            this.initSpy = sinon.spy(this.pushImpl, "init");
            oldFlatMap =Array.prototype["flatMap"];
            window["Es2019Array"] = _Es2019Array;
            delete Array.prototype["flatMap"];

            this.closeIt = () => {
                (global as any).XMLHttpRequest = window.XMLHttpRequest = this.xhr.restore();
                this.jsfAjaxResponse.restore();
                this.socket.restore();
                this.initSpy.restore();
                delete (global as any).WebSocket;
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

    it("must register a channel", function (done: Function) {
        /**
         *   export function init(socketClientId: string,
         uri: string,
         channel: string,
         onopen: Function,
         onmessage: Function,
         onclose: Function,
         behaviorScripts: any,
         autoconnect: boolean) {
            PushImpl.init(socketClientId, uri, channel, onopen, onmessage, onclose, behaviorScripts, autoconnect);
        }
         */


        try {

            faces.push.init("clientId1", "booga.ws", "mychannel", () => {
                    done();
                },
                () => {

                },
                () => {
                },
                "",
                true
            );

            expect(this.initSpy.called).to.be.true;

            let calledArgs = this.initSpy?.getCall(0)?.args;

            expect(calledArgs[0] == "clientId1").to.be.true;
            expect(calledArgs[1] == "booga.ws").to.be.true;
            expect(calledArgs[2] == "mychannel").to.be.true;

            expect(assertType(calledArgs[3], "function")).to.be.true;
            expect(assertType(calledArgs[4], "function")).to.be.true;
            expect(assertType(calledArgs[5], "function")).to.be.true;

            //implementation specific
            expect("clientId1" in this.pushImpl.components, "a component must be registered").to.be.true;
            expect("booga.ws" in this.pushImpl.sockets, "a socket must be registered").to.be.true;
        } finally {
        }
    });

    it("callbacks must be called", function (done) {


        let openCalled = false;
        let closeCalled = false;
        let messageCalled = false;

        let msg = null;
        let cnl = null;
        new Promise((resolve) => {
            faces.push.init("blarg", "booga.ws", "mychannel", () => {
                    openCalled = true;
                    this.fakeWebsocket._respond({data: '"booga"'});
                },

                (message: string, channel: string) => {
                    messageCalled = true;
                    msg = message;
                    cnl = channel;
                    resolve(() => true);
                },
                () => {
                    closeCalled = true;
                },
                "",
                true
            );
        }).then(() => {
            expect(openCalled, "Open must have been called due to autoConnect").to.be.true;



            expect(messageCalled, "on a server response the message must have been called").to.be.true;
            expect(msg, "proper message must be passed").to.eq("booga");
            expect(cnl, "proper message must be passed").to.eq("mychannel");

            expect(closeCalled, "websocket still open").to.be.false;

            faces.push.close("blarg");
            expect(closeCalled, "websocket now closed").to.be.true;


            done();
        });

    });


    it("manual open must work", function (done) {


        let openCalled = false;
        let closeCalled = false;
        let messageCalled = false;

        let msg = null;
        let cnl = null;
        new Promise((resolve) => {
            faces.push.init("blarg", "booga.ws", "mychannel", () => {
                    openCalled = true;
                    this.fakeWebsocket._respond({data: '"booga"'});
                },

                (message: string, channel: string) => {
                    messageCalled = true;
                    msg = message;
                    cnl = channel;
                    resolve(() => true);
                },
                () => {
                    closeCalled = true;
                },
                "",
                false
            );
            faces.push.open("blarg");
        }).then(() => {
            expect(openCalled, "Open must have been called due to open").to.be.true;

            expect(messageCalled, "on a server response the message must have been called").to.be.true;
            expect(msg, "proper message must be passed").to.eq("booga");
            expect(cnl, "proper message must be passed").to.eq("mychannel");

            expect(closeCalled, "websocket still open").to.be.false;

            faces.push.close("blarg");
            expect(closeCalled, "websocket now closed").to.be.true;


            done();
        });

    });
});