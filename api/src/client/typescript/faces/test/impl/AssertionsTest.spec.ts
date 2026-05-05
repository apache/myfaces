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
import {describe, it} from "mocha";
import {expect} from "chai";
import {XMLQuery} from "mona-dish";
import {Assertions} from "../../impl/util/Assertions";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import {Implementation} from "../../impl/AjaxImpl";

const defaultMyFaces = StandardInits.defaultMyFaces;

// Pure functions — no DOM required.
describe("Assertions.assert", () => {

    it("does not throw when the value is truthy", () => {
        expect(() => Assertions.assert(true, "msg")).not.to.throw();
        expect(() => Assertions.assert(1, "msg")).not.to.throw();
        expect(() => Assertions.assert("x", "msg")).not.to.throw();
    });

    it("throws when the value is falsy", () => {
        expect(() => Assertions.assert(false, "must fail")).to.throw();
        expect(() => Assertions.assert(0, "must fail")).to.throw();
        expect(() => Assertions.assert(null, "must fail")).to.throw();
    });

    it("includes the message in the thrown error", () => {
        expect(() => Assertions.assert(false, "sentinel message"))
            .to.throw(/sentinel message/);
    });
});

describe("Assertions.assertType", () => {

    it("does not throw when value is null or undefined", () => {
        expect(() => Assertions.assertType(null, "function")).not.to.throw();
        expect(() => Assertions.assertType(undefined, "function")).not.to.throw();
    });

    it("does not throw when value matches the expected type", () => {
        expect(() => Assertions.assertType(() => {}, "function")).not.to.throw();
        expect(() => Assertions.assertType("hello", "string")).not.to.throw();
    });

    it("throws when value is truthy but does not match the expected type", () => {
        expect(() => Assertions.assertType(42, "function", "bad type")).to.throw();
        expect(() => Assertions.assertType("str", "function", "bad type")).to.throw();
    });
});

describe("Assertions.assertFunction", () => {

    it("does not throw for null or undefined (falsy passthrough)", () => {
        expect(() => Assertions.assertFunction(null)).not.to.throw();
        expect(() => Assertions.assertFunction(undefined)).not.to.throw();
    });

    it("does not throw for an actual function", () => {
        expect(() => Assertions.assertFunction(() => {})).not.to.throw();
    });

    it("throws when a non-function truthy value is passed", () => {
        expect(() => Assertions.assertFunction(42 as any)).to.throw();
        expect(() => Assertions.assertFunction("callback" as any)).to.throw();
    });
});

describe("Assertions.assertDelay", () => {

    it("does not throw for zero", () => {
        expect(() => Assertions.assertDelay(0)).not.to.throw();
    });

    it("does not throw for positive numbers", () => {
        expect(() => Assertions.assertDelay(100)).not.to.throw();
        expect(() => Assertions.assertDelay(0.5)).not.to.throw();
    });

    it("throws for negative numbers", () => {
        expect(() => Assertions.assertDelay(-1)).to.throw("Invalid delay value");
    });

    it("throws for NaN", () => {
        expect(() => Assertions.assertDelay(NaN)).to.throw("Invalid delay value");
    });

    it("throws for a non-numeric string", () => {
        expect(() => Assertions.assertDelay("fast" as any)).to.throw("Invalid delay value");
    });
});

describe("Assertions.raiseError", () => {

    it("returns an Error instance", () => {
        const err = Assertions.raiseError(new Error(), "something went wrong");
        expect(err).to.be.instanceOf(Error);
    });

    it("falls back to MALFORMEDXML when title and name are omitted", () => {
        const err = Assertions.raiseError(new Error(), "msg", "caller") as any;
        expect(err).to.be.instanceOf(Error);
    });

    it("uses the supplied title and name when provided", () => {
        const err = Assertions.raiseError(new Error(), "msg", "caller", "MyTitle", "MyName") as any;
        expect(err).to.be.instanceOf(Error);
    });
});

// Tests that require XMLQuery / DOMParser — JSDOM setup needed.
describe("Assertions XML (requires DOM)", () => {

    beforeEach(async function () {
        return defaultMyFaces().then((close) => {
            this.closeIt = () => { Implementation.reset(); close(); };
        });
    });

    afterEach(function () { this.closeIt(); });

    describe("assertUrlExists", () => {

        function xqWithUrl(present: boolean): XMLQuery {
            // assertUrlExists only calls node.attr(ATTR_URL).isAbsent()
            return { attr: (_: string) => ({ isAbsent: () => !present }) } as any;
        }

        it("does not throw when the url attribute is present", () => {
            expect(() => Assertions.assertUrlExists(xqWithUrl(true))).not.to.throw();
        });

        it("throws when the url attribute is absent", () => {
            expect(() => Assertions.assertUrlExists(xqWithUrl(false))).to.throw();
        });
    });

    describe("assertValidXMLResponse", () => {

        it("does not throw for a well-formed partial-response document", () => {
            const doc = new DOMParser().parseFromString(
                '<partial-response><changes/></partial-response>', "text/xml");
            expect(() => Assertions.assertValidXMLResponse(new XMLQuery(doc))).not.to.throw();
        });

        it("throws when the response is absent (null)", () => {
            expect(() => Assertions.assertValidXMLResponse(new XMLQuery(null as any))).to.throw();
        });

        it("throws when the partial-response root element is missing", () => {
            const doc = new DOMParser().parseFromString('<other-root/>', "text/xml");
            expect(() => Assertions.assertValidXMLResponse(new XMLQuery(doc))).to.throw();
        });
    });
});
