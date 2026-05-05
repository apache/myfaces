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
import {Config} from "mona-dish";
import {decodeEncodedValues, encodeFormData, fixEmptyParameters} from "../../impl/util/FileUtils";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import {Implementation} from "../../impl/AjaxImpl";

const defaultMyFaces = StandardInits.defaultMyFaces;

// Pure functions — no DOM required.
describe("decodeEncodedValues", () => {

    it("decodes a single key=value pair", () => {
        expect(decodeEncodedValues("foo=bar")).to.deep.eq([["foo", "bar"]]);
    });

    it("splits multiple pairs on &", () => {
        expect(decodeEncodedValues("a=1&b=2")).to.deep.eq([["a", "1"], ["b", "2"]]);
    });

    it("returns a one-element array for a key-only segment (no =)", () => {
        expect(decodeEncodedValues("keyonly")).to.deep.eq([["keyonly"]]);
    });

    it("decodes percent-encoded characters in key and value", () => {
        // %20 = space, %21 = !; the & between pairs is literal so both segments are decoded
        expect(decodeEncodedValues("hello%20world=test%21&a=b"))
            .to.deep.eq([["hello world", "test!"], ["a", "b"]]);
    });

    it("splits only on the first = so values containing = are preserved", () => {
        expect(decodeEncodedValues("key=val=ue")).to.deep.eq([["key", "val=ue"]]);
    });

    it("filters out blank and whitespace-only segments", () => {
        expect(decodeEncodedValues("a=1& &b=2")).to.deep.eq([["a", "1"], ["b", "2"]]);
    });

    it("returns an empty array for an empty string", () => {
        expect(decodeEncodedValues("")).to.deep.eq([]);
    });
});

describe("fixEmptyParameters", () => {

    it("fills both slots with empty arrays when the input is empty", () => {
        expect(fixEmptyParameters([])).to.deep.eq([[], []]);
    });

    it("fills the second slot with an empty array when only a key is present", () => {
        expect(fixEmptyParameters(["key"])).to.deep.eq(["key", []]);
    });

    it("passes through a two-element array unchanged", () => {
        expect(fixEmptyParameters(["key", "val"])).to.deep.eq(["key", "val"]);
    });

    it("passes through arrays of three or more elements unchanged", () => {
        expect(fixEmptyParameters(["key", "val", "extra"])).to.deep.eq(["key", "val", "extra"]);
    });
});

// encodeFormData accesses window.File via ExtDomQuery.global(), so JSDOM is required.
describe("encodeFormData", () => {

    beforeEach(async function () {
        return defaultMyFaces().then((close) => {
            this.closeIt = () => { Implementation.reset(); close(); };
        });
    });

    afterEach(function () {
        this.closeIt();
    });

    it("returns the defaultStr when the config is absent", () => {
        expect(encodeFormData(new Config(null), undefined, "FALLBACK")).to.eq("FALLBACK");
    });

    it("returns empty string as default when no defaultStr is supplied and config is absent", () => {
        expect(encodeFormData(new Config(null))).to.eq("");
    });

    it("encodes a single key-value pair", () => {
        const config = new Config({name: ["Alice"]});
        expect(encodeFormData(config)).to.eq("name=Alice");
    });

    it("encodes multiple values for one key as separate params", () => {
        const config = new Config({color: ["red", "blue"]});
        expect(encodeFormData(config)).to.eq("color=red&color=blue");
    });

    it("percent-encodes special characters in keys and values", () => {
        const config = new Config({"a b": ["x=y"]});
        expect(encodeFormData(config)).to.eq("a%20b=x%3Dy");
    });

    it("applies the paramsMapper to rename keys before encoding", () => {
        const config = new Config({originalKey: ["value"]});
        const mapper = (_key: string, val: any) => ["renamedKey", val];
        expect(encodeFormData(config, mapper)).to.eq("renamedKey=value");
    });

    it("filters out File instances from the encoded output", () => {
        const file = new (window as any).File(["content"], "upload.txt");
        const config = new Config({text: ["hello"], upload: [file]});
        expect(encodeFormData(config)).to.eq("text=hello");
    });
});
