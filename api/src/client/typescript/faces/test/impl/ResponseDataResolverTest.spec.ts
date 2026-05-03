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
import {resolveContexts} from "../../impl/xhrCore/ResponseDataResolver";
import {
    CTX_PARAM_MF_INTERNAL,
    DEFERRED_HEAD_INSERTS,
    UPDATE_ELEMS,
    UPDATE_FORMS
} from "../../impl/core/Const";

describe("resolveContexts", () => {

    it("creates a fresh internal context when none is present in the external context", () => {
        const {externalContext, internalContext} = resolveContexts({});

        expect(internalContext.isPresent(),
            "internalContext must be present even when none was supplied").to.be.true;
        expect(internalContext.getIf(DEFERRED_HEAD_INSERTS).value,
            "DEFERRED_HEAD_INSERTS must be initialised to an empty array").to.deep.eq([]);
        expect(internalContext.getIf(UPDATE_FORMS).value,
            "UPDATE_FORMS must be initialised to an empty array").to.deep.eq([]);
        expect(internalContext.getIf(UPDATE_ELEMS).value,
            "UPDATE_ELEMS must be initialised to an empty array").to.deep.eq([]);
    });

    it("reuses the existing internal context when one is present in the external context", () => {
        // assign/getIf in mona-dish treat dot-strings as flat keys, not nested paths.
        // CTX_PARAM_MF_INTERNAL = "myfaces.internal" is stored as a literal flat key.
        const context = {[CTX_PARAM_MF_INTERNAL]: {existingKey: "existingValue"}};
        const {externalContext, internalContext} = resolveContexts(context);

        expect(internalContext.getIf("existingKey").value,
            "existing internal context values must be preserved").to.eq("existingValue");
        expect(internalContext.getIf(DEFERRED_HEAD_INSERTS).value,
            "DEFERRED_HEAD_INSERTS must be initialised even when reusing an existing context").to.deep.eq([]);
        expect(internalContext.getIf(UPDATE_FORMS).value).to.deep.eq([]);
        expect(internalContext.getIf(UPDATE_ELEMS).value).to.deep.eq([]);
    });

    it("wraps the original object as the external context", () => {
        const context = {source: "button1"};
        const {externalContext} = resolveContexts(context);

        expect(externalContext.getIf("source").value).to.eq("button1");
    });
});
