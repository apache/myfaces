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
// AI-generated: this file was created with assistance from Claude (Anthropic) — see AI_CONTRIBUTIONS.md
import {describe} from "mocha";
import * as sinon from "sinon";
import * as nise from "nise";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import {Implementation} from "../../impl/AjaxImpl";

import {expect} from "chai";

const defaultMyFaces = StandardInits.defaultMyFaces;
import {_Es2019Array, Lang} from "mona-dish";
import {FakeWebsocket} from "./FakeWebsocket";
import {MAX_RECONNECT_ATTEMPTS, REASON_EXPIRED, RECONNECT_INTERVAL} from "../../impl/core/Const";
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
        // faces.push.init (Faces 4) includes onerror. PushImpl keeps the same signature
        // so the JSF 2.3 compatibility shim can pass null for that callback.

        try {
            faces.push.init("clientId1", "booga.ws", "mychannel",
                () => { done(); },   // onopen
                () => {},            // onmessage
                () => {},            // onerror
                () => {},            // onclose
                "",                  // behaviorScripts
                true                 // autoConnect
            );

            expect(this.initSpy.called).to.be.true;

            let calledArgs = this.initSpy?.getCall(0)?.args;

            expect(calledArgs[0]).to.eq("clientId1");
            expect(calledArgs[1]).to.eq("booga.ws");
            expect(calledArgs[2]).to.eq("mychannel");

            expect(assertType(calledArgs[3], "function")).to.be.true;  // onopen
            expect(assertType(calledArgs[4], "function")).to.be.true;  // onmessage
            expect(assertType(calledArgs[5], "function")).to.be.true;  // onerror
            expect(assertType(calledArgs[6], "function")).to.be.true;  // onclose
            expect(calledArgs[7]).to.eq("");                           // behaviorScripts
            expect(calledArgs[8]).to.be.true;                          // autoConnect

            // implementation-level state
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
                () => {},
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
                () => {},
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

    it("must call onclose(-1) when WebSocket is not available", function () {
        // Null both window and global so DQ.global().WebSocket is falsy regardless of which the impl reads
        const savedWindow = (window as any).WebSocket;
        const savedGlobal = (global as any).WebSocket;
        (window as any).WebSocket = null;
        (global as any).WebSocket = null;

        let closeCalled = false;
        let closeCode: any = null;
        let closeChannel: any = null;

        faces.push.init("blarg", "booga.ws", "nochannel",
            () => {},
            () => {},
            () => {},
            (code: number, channel: string) => { closeCalled = true; closeCode = code; closeChannel = channel; },
            "",
            false
        );

        (window as any).WebSocket = savedWindow;
        (global as any).WebSocket = savedGlobal;

        expect(closeCalled, "onclose must be called when WebSocket is unavailable").to.be.true;
        expect(closeCode).to.eq(-1);
        expect(closeChannel).to.eq("nochannel");
    });

    it("must extract channelToken from the query-string part of the URL", function () {
        faces.push.init("blarg", "ws://example.com/push?mytoken", "mychannel",
            () => {}, () => {}, () => {}, () => {}, "", false
        );

        expect("mytoken" in this.pushImpl.sockets, "socket must be keyed by the query-string token").to.be.true;
        expect(this.pushImpl.components["blarg"].channelToken).to.eq("mytoken");
    });

    it("must be idempotent on repeated init with the same socketClientId", function () {
        faces.push.init("blarg", "booga.ws", "mychannel",
            () => {}, () => {}, () => {}, () => {}, "", false
        );
        faces.push.init("blarg", "booga.ws", "mychannel",
            () => {}, () => {}, () => {}, () => {}, "", false
        );

        expect(Object.keys(this.pushImpl.sockets).length).to.eq(1);
        expect(Object.keys(this.pushImpl.components).length).to.eq(1);
    });

    it("must fan out onopen to all components sharing the same socket URL", function (done) {
        let open1 = false;
        let open2 = false;

        faces.push.init("blarg", "booga.ws", "mychannel",
            () => { open1 = true; }, () => {}, () => {}, () => {}, "", false
        );
        faces.push.init("clientId2", "booga.ws", "mychannel",
            () => { open2 = true; }, () => {}, () => {}, () => {}, "", false
        );

        faces.push.open("blarg");

        setTimeout(() => {
            expect(open1, "first component onopen must be called").to.be.true;
            expect(open2, "second component onopen must be called").to.be.true;
            done();
        }, 20);
    });

    it("must invoke registered behavior functions on a matching message", function (done) {
        let behaviorCalled = false;
        const behaviors = { "booga": [() => { behaviorCalled = true; }] };

        new Promise<void>((resolve) => {
            this.pushImpl.init("blarg", "booga.ws", "mychannel",
                () => { this.fakeWebsocket._respond({data: '"booga"'}); },
                () => { resolve(); },
                null,
                () => {},
                behaviors,
                true
            );
        }).then(() => {
            expect(behaviorCalled, "behavior function must be invoked on matching message key").to.be.true;
            done();
        });
    });

    it("must invoke onerror and reconnect on a reconnectable abnormal close", function (done) {
        let errorCalled = false;
        let closeCalled = false;
        let errorCode: any = null;
        let errorChannel: any = null;

        new Promise<void>((resolve) => {
            faces.push.init("blarg", "booga.ws", "mychannel",
                () => {
                    setTimeout(() => {
                        this.fakeWebsocket._close({code: 1006, reason: "abnormal"});
                        resolve();
                    }, 20);
                },
                () => {},
                (code: number, channel: string) => { errorCalled = true; errorCode = code; errorChannel = channel; },
                () => { closeCalled = true; },
                "",
                true
            );
        }).then(() => {
            setTimeout(() => {
                expect(errorCalled, "onerror must be called before reconnecting").to.be.true;
                expect(closeCalled, "onclose must not be called while reconnecting").to.be.false;
                expect(errorCode).to.eq(1006);
                expect(errorChannel).to.eq("mychannel");
                expect(this.socket.callCount, "a reconnect must create another WebSocket").to.be.greaterThan(1);
                done();
            }, RECONNECT_INTERVAL + 150);
        });
    });

    it("must use cumulative reconnect delays across consecutive failed reconnect attempts", function () {
        const clock = sinon.useFakeTimers();
        const firstSocket = new FakeWebsocket();
        const secondSocket = new FakeWebsocket();
        const thirdSocket = new FakeWebsocket();

        try {
            this.socket.resetBehavior();
            this.socket.onCall(0).returns(firstSocket);
            this.socket.onCall(1).returns(secondSocket);
            this.socket.onCall(2).returns(thirdSocket);

            faces.push.init("blarg", "booga.ws", "mychannel",
                () => {},
                () => {},
                () => {},
                () => {},
                "",
                true
            );

            clock.tick(10);
            expect(this.socket.callCount, "initial open must create one WebSocket").to.eq(1);

            firstSocket._close({code: 1006, reason: "abnormal"});
            expect(firstSocket.readyState, "first socket must be closed before reconnect").to.eq(3);
            expect(this.pushImpl.sockets["booga.ws"].socket,
                "closed socket reference must be cleared before first reconnect is scheduled").to.eq(null);
            clock.tick(RECONNECT_INTERVAL - 1);
            expect(this.socket.callCount, "first reconnect must wait one reconnect interval").to.eq(1);
            clock.tick(1);
            expect(this.socket.callCount, "first reconnect must create the second WebSocket").to.eq(2);
            expect(this.pushImpl.sockets["booga.ws"].socket,
                "first reconnect must store the replacement WebSocket").to.eq(secondSocket);

            secondSocket._close({code: 1006, reason: "abnormal"});
            expect(secondSocket.readyState, "second socket must be closed before reconnect").to.eq(3);
            expect(this.pushImpl.sockets["booga.ws"].socket,
                "closed socket reference must be cleared before second reconnect is scheduled").to.eq(null);
            clock.tick((RECONNECT_INTERVAL * 2) - 1);
            expect(this.socket.callCount, "second reconnect must wait two reconnect intervals").to.eq(2);
            clock.tick(1);
            expect(this.socket.callCount, "second reconnect must create the third WebSocket").to.eq(3);
            expect(this.pushImpl.sockets["booga.ws"].socket,
                "second reconnect must store the replacement WebSocket").to.eq(thirdSocket);
        } finally {
            clock.restore();
        }
    });

    it("must not fire onopen again when a reconnect succeeds after a prior working connection", function () {
        // Spec (Jakarta Faces 3+): "This will be invoked on the very first connection attempt."
        // "This will not be invoked when the web socket auto-reconnects a broken connection
        // after the first successful connection."
        // Also verifies reconnectAttempts resets to 0 so a further drop can start a fresh cycle.
        const clock = sinon.useFakeTimers();
        const firstSocket = new FakeWebsocket();
        // Plain object instead of FakeWebsocket: we manually fire onopen so there is no
        // constructor setTimeout that fires at an uncontrolled point in fake-timer time.
        const reconnectWs: any = {
            readyState: 0,
            onopen: () => {}, onmessage: () => {}, onclose: () => {}, onerror: () => {},
            send() {},
            close() { this.readyState = 3; this.onclose({}); }
        };

        try {
            this.socket.resetBehavior();
            this.socket.onCall(0).returns(firstSocket);
            this.socket.onCall(1).returns(reconnectWs);

            let openCount = 0;
            faces.push.init("blarg", "booga.ws", "mychannel",
                () => { openCount++; },
                () => {}, () => {}, () => {},
                "",
                true
            );

            // initial connection succeeds — onopen must fire exactly once
            clock.tick(10);
            expect(openCount, "onopen must fire on the initial connect").to.eq(1);

            // abnormal close → reconnect scheduled, reconnectAttempts increments to 1
            firstSocket._close({code: 1006, reason: "abnormal"});
            clock.tick(RECONNECT_INTERVAL); // fires open() → reconnectWs bound via bindCallbacks

            // reconnect succeeds: manually trigger onopen on the newly-bound socket
            reconnectWs.readyState = 1;
            reconnectWs.onopen({});

            expect(openCount, "onopen must not fire again on a successful reconnect").to.eq(1);
            expect(this.pushImpl.sockets["booga.ws"].reconnectAttempts,
                "reconnectAttempts must reset to 0 after successful reconnect").to.eq(0);
        } finally {
            clock.restore();
        }
    });

    it("must call onclose and stop reconnecting after MAX_RECONNECT_ATTEMPTS failed attempts", function () {
        const clock = sinon.useFakeTimers();
        // Create the initial socket AFTER installing fake timers so its constructor
        // setTimeout is under fake-timer control — clock.tick(10) will fire onopen.
        // All reconnect attempts return the same object; bindCallbacks() rebinds on each open().
        const initialSocket = new FakeWebsocket();
        this.socket.resetBehavior();
        this.socket.returns(initialSocket);

        let closeCalled = false;
        let closeCode: any = null;

        try {
            faces.push.init("blarg", "booga.ws", "mychannel",
                () => {},
                () => {},
                () => {},
                (code: number) => { closeCalled = true; closeCode = code; },
                "",
                true
            );

            // tick past FakeWebsocket constructor setTimeout so initial onopen fires (hasEverConnected = true)
            clock.tick(10);

            // drive MAX_RECONNECT_ATTEMPTS abnormal closes; each close schedules the next open
            // and the tick fires it, leaving reconnectAttempts = MAX_RECONNECT_ATTEMPTS after the loop
            for (let attempt = 1; attempt <= MAX_RECONNECT_ATTEMPTS; attempt++) {
                initialSocket._close({code: 1006, reason: "abnormal"});
                clock.tick(RECONNECT_INTERVAL * attempt);
            }

            expect(closeCalled, "onclose must not fire while reconnect budget remains").to.be.false;

            // this close tips reconnectAttempts over the limit → terminal path
            initialSocket._close({code: 1006, reason: "abnormal"});

            expect(closeCalled, "onclose must fire once all reconnect attempts are exhausted").to.be.true;
            expect(closeCode).to.eq(1006);
        } finally {
            clock.restore();
        }
    });

    it("must call onclose callback when server closes with REASON_EXPIRED", function (done) {
        let closeCalled = false;
        let closeCode: any = null;

        new Promise<void>((resolve) => {
            faces.push.init("blarg", "booga.ws", "mychannel",
                () => { this.fakeWebsocket._close({code: 1000, reason: REASON_EXPIRED}); },
                () => {},
                () => {},
                (code: number) => { closeCalled = true; closeCode = code; resolve(); },
                "",
                true
            );
        }).then(() => {
            expect(closeCalled, "onclose must be called on REASON_EXPIRED terminal close").to.be.true;
            expect(closeCode).to.eq(1000);
            done();
        });
    });

    it("must treat any close code 1000 as terminal and not reconnect", function (done) {
        let closeCalled = false;
        let errorCalled = false;
        let closeCode: any = null;
        const wsCallCount = this.socket.callCount;

        new Promise<void>((resolve) => {
            faces.push.init("blarg", "booga.ws", "mychannel",
                () => { this.fakeWebsocket._close({code: 1000, reason: "Normal Closure"}); },
                () => {},
                () => { errorCalled = true; },
                (code: number) => { closeCalled = true; closeCode = code; resolve(); },
                "",
                true
            );
        }).then(() => {
            expect(closeCalled, "onclose must be called for any 1000 terminal close").to.be.true;
            expect(closeCode).to.eq(1000);
            expect(errorCalled, "onerror must not be called for a normal terminal close").to.be.false;
            expect(this.socket.callCount, "no reconnect WebSocket must be created after 1000").to.eq(wsCallCount + 1);
            done();
        });
    });

    it("must treat close code 1008 (Policy Violation) as terminal and not reconnect", function (done) {
        // 1008 = server rejected due to authorization/security failure.
        // Reconnecting would hit the same rejection, so it must be treated as terminal:
        // onclose fires, onerror does not, no new WebSocket is created.
        let closeCalled = false;
        let errorCalled = false;
        let closeCode: any = null;
        const wsCallCount = this.socket.callCount;

        new Promise<void>((resolve) => {
            faces.push.init("blarg", "booga.ws", "mychannel",
                () => { this.fakeWebsocket._close({code: 1008, reason: "Policy Violation"}); },
                () => {},
                () => { errorCalled = true; },
                (code: number) => { closeCalled = true; closeCode = code; resolve(); },
                "",
                true
            );
        }).then(() => {
            expect(closeCalled, "onclose must be called for a 1008 terminal close").to.be.true;
            expect(closeCode).to.eq(1008);
            expect(errorCalled, "onerror must not be called for a terminal close").to.be.false;
            expect(this.socket.callCount, "no reconnect WebSocket must be created after 1008").to.eq(wsCallCount + 1);
            done();
        });
    });

    it("must treat failed first connection attempt as terminal and not reconnect", function () {
        const firstSocket: any = {
            readyState: 0,
            onopen: () => {}, onmessage: () => {}, onclose: () => {}, onerror: () => {},
            send() {},
            close() { this.readyState = 3; this.onclose({}); }
        };
        this.socket.resetBehavior();
        this.socket.returns(firstSocket);

        let closeCalled = false;
        let errorCalled = false;
        let closeCode: any = null;

        faces.push.init("blarg", "booga.ws", "mychannel",
            () => {},
            () => {},
            () => { errorCalled = true; },
            (code: number) => { closeCalled = true; closeCode = code; },
            "",
            true
        );

        firstSocket.readyState = 3;
        firstSocket.onclose({code: 1006, reason: "initial failure"});

        expect(closeCalled, "onclose must be called when the first connection attempt fails").to.be.true;
        expect(closeCode).to.eq(1006);
        expect(errorCalled, "onerror must not be called for a failed first connection attempt").to.be.false;
        expect(this.socket.callCount, "no reconnect WebSocket must be created after first-attempt failure").to.eq(1);
    });

    it("must fire onopen again when open() is called after a terminal close", function (done) {
        // After any terminal close (1000, 1008, max retries, first-attempt failure),
        // a subsequent explicit faces.push.open() must treat the new attempt as a
        // fresh first connection — onopen fires again and the error/reconnect state is reset.
        let openCount = 0;
        let closeCount = 0;

        new Promise<void>((resolve) => {
            faces.push.init("blarg", "booga.ws", "mychannel",
                () => {
                    openCount++;
                    if (openCount === 1) {
                        // trigger terminal close from within onopen of first connection
                        this.fakeWebsocket._close({code: 1000, reason: "Normal"});
                    } else {
                        resolve();
                    }
                },
                () => {},
                () => {},
                () => { closeCount++; },
                "",
                true
            );
        }).then(() => {
            expect(openCount, "onopen must fire twice — once per explicit connection").to.eq(2);
            expect(closeCount, "onclose must fire exactly once for the terminal close").to.eq(1);
            done();
        });

        // Create secondSocket here so its constructor timer fires ~10ms AFTER bindCallbacks()
        // is called by the explicit open(), not 50ms before it.
        setTimeout(() => {
            const secondSocket = new FakeWebsocket();
            this.socket.onCall(1).returns(secondSocket);
            faces.push.open("blarg");
        }, 50);
    });

    it("must close existing sockets when PushImpl.reset() is called", function () {
        faces.push.init("blarg", "booga.ws", "mychannel",
            () => {},
            () => {},
            () => {},
            () => {},
            "",
            true
        );

        const closeSpy = sinon.spy(this.fakeWebsocket, "close");

        this.pushImpl.reset();

        expect(closeSpy.calledOnce, "reset must close the existing WebSocket").to.be.true;
    });

    it("must ignore late native onopen callback after reset tears down the channel registry", function () {
        let openCalled = false;

        faces.push.init("blarg", "booga.ws", "mychannel",
            () => { openCalled = true; },
            () => {},
            () => {},
            () => {},
            "",
            true
        );

        this.pushImpl.reset();

        expect(() => this.fakeWebsocket.onopen({})).not.to.throw();
        expect(openCalled, "user onopen must have fired for the initial connection attempt").to.be.true;
    });

    it("must ignore pending onclose callback after reset tears down the channel registry", function () {
        let closeCount = 0;

        faces.push.init("blarg", "booga.ws", "mychannel",
            () => {},
            () => {},
            () => {},
            () => { closeCount++; },
            "",
            true
        );

        this.pushImpl.reset();
        const closeCountAfterReset = closeCount;

        expect(() => this.fakeWebsocket._close({code: 1006, reason: "abnormal"})).not.to.throw();
        expect(closeCount, "late onclose must not call component callbacks after reset").to.eq(closeCountAfterReset);
    });

    it("must ignore pending onmessage callback after reset tears down the channel registry", function () {
        let messageCount = 0;

        faces.push.init("blarg", "booga.ws", "mychannel",
            () => {},
            () => { messageCount++; },
            () => {},
            () => {},
            "",
            true
        );

        this.pushImpl.reset();

        expect(() => this.fakeWebsocket._respond({data: JSON.stringify("message")})).not.to.throw();
        expect(messageCount, "late onmessage must not call component callbacks after reset").to.eq(0);
    });

    it("must ignore native WebSocket error events", function () {
        let errorCalled = false;

        faces.push.init("blarg", "booga.ws", "mychannel",
            () => {},
            () => {},
            () => { errorCalled = true; },
            () => {},
            "",
            true
        );

        expect(() => this.fakeWebsocket._error({data: '{"code":1006}'})).not.to.throw();
        expect(errorCalled, "Faces onerror is only fired from reconnectable close handling").to.be.false;
    });

    it("must remove stale components whose DOM element has been removed", function (done) {
        const channelToken = "booga.ws"; // no '?' so token == full URL

        new Promise<void>((resolve) => {
            faces.push.init("nonexistent-id", "booga.ws", "mychannel",
                () => { this.fakeWebsocket._respond({data: '"ping"'}); resolve(); },
                () => {},
                () => {},
                () => {},
                "",
                true
            );
        }).then(() => {
            expect(this.pushImpl.clientIdsByTokens[channelToken].length,
                "stale component must be removed from clientIdsByTokens").to.eq(0);
            done();
        });
    });
});
