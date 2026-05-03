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
import {expect} from "chai";
import * as sinon from "sinon";
import {Config} from "mona-dish";
import {ExtLang} from "../../../impl/util/Lang";
import {StandardInits} from "../../frameworkBase/_ext/shared/StandardInits";

describe("ExtLang", function () {

    beforeEach(function () {
        return StandardInits.defaultMyFaces().then((close) => {
            this.closeIt = close;
        });
    });

    afterEach(function () {
        this.closeIt();
    });

    it("must resolve messages with default fallback and template replacement", function () {
        expect(ExtLang.getMessage("missing.key", "Hello {0}", "World")).to.eq("Hello World");
        expect(ExtLang.getMessage("missing.key")).to.eq("missing.key");
    });

    it("must read global and local myfaces config with local priority", function () {
        window.myfaces.config = {...window.myfaces.config, delay: 50, timeout: 100};

        expect(ExtLang.getGlobalConfig("delay", 0)).to.eq(50);
        expect(ExtLang.getGlobalConfig("unknown", 7)).to.eq(7);

        const localOptions = new Config({myfaces: {config: {delay: 10}}});

        expect(ExtLang.getLocalOrGlobalConfig(localOptions, "delay", 0)).to.eq(10);
        expect(ExtLang.getLocalOrGlobalConfig(localOptions, "timeout", 0)).to.eq(100);
        expect(ExtLang.getLocalOrGlobalConfig(localOptions, "unknown", 7)).to.eq(7);
    });

    it("must convert associative objects to tuples and collect tuples back into an object", function () {
        const tuples = ExtLang.ofAssoc({one: 1, two: 2});

        expect(tuples).to.deep.eq([["one", 1], ["two", 2]]);
        expect(tuples.reduce(ExtLang.collectAssoc, {})).to.deep.eq({one: 1, two: 2});
    });

    it("must provide fail-safe resolving and execution helpers", function () {
        let executed = false;

        expect(ExtLang.failSaveResolve(() => "ok").value).to.eq("ok");
        expect(ExtLang.failSaveResolve(() => { throw new Error("boom"); }, "fallback").isAbsent()).to.be.true;
        expect(() => ExtLang.failSaveExecute(() => { executed = true; })).not.to.throw();
        expect(() => ExtLang.failSaveExecute(() => { throw new Error("boom"); })).not.to.throw();
        expect(executed).to.be.true;
    });

    it("must resolve forms from direct form, form attribute and parent form", function () {
        document.body.innerHTML = `
            <form id="outer">
                <button id="nested"></button>
            </form>
            <form id="target"></form>
            <button id="detached" form="target"></button>`;

        expect(ExtLang.getForm(document.getElementById("outer")!).id.value).to.eq("outer");
        expect(ExtLang.getForm(document.getElementById("detached")!).id.value).to.eq("target");
        expect(ExtLang.getForm(document.getElementById("nested")!).id.value).to.eq("outer");
    });

    it("must debounce repeated calls by key", function () {
        const clock = sinon.useFakeTimers();
        let callCount = 0;

        try {
            ExtLang.debounce("key", () => { callCount++; }, 50);
            ExtLang.debounce("key", () => { callCount++; }, 50);

            clock.tick(49);
            expect(callCount).to.eq(0);

            clock.tick(1);
            expect(callCount).to.eq(1);

            ExtLang.debounce("immediate", () => { callCount++; }, 0);
            expect(callCount).to.eq(2);
        } finally {
            clock.restore();
        }
    });
});
