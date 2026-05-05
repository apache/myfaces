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
import {expect} from "chai";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import {FakeWebsocket} from "../xhrCore/FakeWebsocket";

const defaultMyFaces23 = StandardInits.defaultMyFaces23;

describe("JSF 2.3 push compatibility shim", function () {
    beforeEach(async function () {
        return defaultMyFaces23().then((close) => {
            this.fakeWebsocket = new FakeWebsocket();
            this.socket = sinon.stub(window, 'WebSocket').returns(this.fakeWebsocket);
            (global as any).WebSocket = this.socket;
            this.pushImpl = (global as any).PushImpl;
            this.initSpy = sinon.spy(this.pushImpl, "init");
            this.closeIt = () => {
                this.initSpy.restore();
                this.socket.restore();
                delete (global as any).WebSocket;
                this.pushImpl.reset();
                close();
            };
        });
    });

    afterEach(function () {
        this.closeIt();
    });

    it("must adapt legacy jsf.push.init without onerror to the Faces 4 init signature", function () {
        const onopen = () => {};
        const onmessage = () => {};
        const onclose = () => {};
        const behaviors = {"event": [() => {}]};

        window.jsf.push.init("blarg", "booga.ws", "mychannel",
            onopen,
            onmessage,
            onclose,
            behaviors,
            false
        );

        expect(this.initSpy.calledOnce).to.be.true;
        const args = this.initSpy.firstCall.args;
        expect(args[0]).to.eq("blarg");
        expect(args[1]).to.eq("booga.ws");
        expect(args[2]).to.eq("mychannel");
        expect(args[3]).to.eq(onopen);
        expect(args[4]).to.eq(onmessage);
        expect(args[5], "legacy shim must pass null for Faces 4 onerror").to.eq(null);
        expect(args[6]).to.eq(onclose);
        expect(args[7]).to.eq(behaviors);
        expect(args[8]).to.eq(false);
        expect(args.length).to.eq(9);
    });

    it("must route legacy terminal close through onclose", function () {
        let closeCalled = false;
        let closeCode: any = null;
        let closeChannel: any = null;
        let closeEvent: any = null;

        window.jsf.push.init("blarg", "booga.ws", "mychannel",
            () => {},
            () => {},
            (code: number, channel: string, event: any) => {
                closeCalled = true;
                closeCode = code;
                closeChannel = channel;
                closeEvent = event;
            },
            "",
            true
        );

        const event = {code: 1000, reason: "Normal Closure"};
        this.fakeWebsocket._close(event);

        expect(closeCalled, "legacy jsf.push terminal close must call onclose").to.be.true;
        expect(closeCode).to.eq(1000);
        expect(closeChannel).to.eq("mychannel");
        expect(closeEvent).to.eq(event);
    });

    it("must keep legacy reconnectable broken connection internal without calling onclose", function () {
        const clock = sinon.useFakeTimers();
        let closeCalled = false;
        let openCount = 0;

        try {
            window.jsf.push.init("blarg", "booga.ws", "mychannel",
                () => { openCount++; },
                () => {},
                () => { closeCalled = true; },
                "",
                true
            );

            this.fakeWebsocket.onopen({});
            this.fakeWebsocket._close({code: 1006, reason: "abnormal"});

            expect(openCount, "legacy onopen must fire for the first connection attempt").to.eq(1);
            expect(closeCalled, "legacy onclose must not fire while reconnect is possible").to.be.false;
        } finally {
            clock.restore();
        }
    });
});
