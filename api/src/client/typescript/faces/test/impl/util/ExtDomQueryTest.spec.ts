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

import {afterEach, describe, it} from 'mocha';
import {expect} from 'chai';

import {ExtConfig, ExtDomQuery} from "../../../impl/util/ExtDomQuery";
import {StandardInits} from "../../frameworkBase/_ext/shared/StandardInits";
const defaultMyFaces = StandardInits.defaultMyFaces;
import Sinon from "sinon";
import {Implementation} from "../../../impl/AjaxImpl";

declare var faces: any;
let oldProjectStage = null;

/**
 * Tests for our extended DomQuery functionality
 */
describe('ExtDomQuery test suite', () => {
    beforeEach(async () => {
        Sinon.reset();
        //we test ES2019 with it and whether we have missed
        //a map somewhere
        return await defaultMyFaces().then(() => {
            oldProjectStage = faces.getProjectStage;
            faces.getProjectStage = () => "Development";
        });
    });

    afterEach(() => {
        faces.getProjectStage = oldProjectStage;
    });

    it('ExtDomQuery.By id must log a console error with ProjectStage == Development and non existing id', (done) => {
        const spy = Sinon.spy(window.console, "error");
        try {
            ExtDomQuery.byId("no_existent_element");
            expect(spy.calledOnce).to.be.true;
        } finally {
            spy.restore();
        }
        done();
    });

    it('ExtDomQuery.By id must not log a console error with ProjectStage == Production and non existing id', (done) => {
        faces.getProjectStage = () => "Production";
        const spy = Sinon.spy(window.console, "error");
        try {
            ExtDomQuery.byId("no_existent_element");
            expect(spy.notCalled).to.be.true;
        } finally {
            spy.restore();
        }
        done();
    });

    it('ExtDomQuery.nonce falls back to finding nonce on a faces script tag in the DOM', () => {
        const script = document.createElement("script");
        // /javax.faces.resource/jsf.js matches IS_FACES_SOURCE pattern \/javax\.faces\.resource.*\/jsf\.js.*
        script.setAttribute("src", "/javax.faces.resource/jsf.js");
        script.setAttribute("nonce", "test-nonce-abc");
        document.head.appendChild(script);
        try {
            const nonce = new ExtDomQuery(document.body).nonce;
            expect(nonce.value).to.eq("test-nonce-abc");
        } finally {
            document.head.removeChild(script);
        }
    });

    it('ExtDomQuery.runHeadInserts handles text nodes (no tagName) without throwing', () => {
        const text = document.createTextNode("inline text");
        expect(() => new ExtDomQuery(text as any).runHeadInserts()).not.to.throw();
    });
});

describe('ExtConfig', () => {
    let closeIt: () => void;

    beforeEach(async function () {
        return defaultMyFaces().then((close) => {
            closeIt = () => { Implementation.reset(); close(); };
        });
    });

    afterEach(function () { closeIt(); });

    it('append stores a value under the given key', () => {
        const config = new ExtConfig({});
        config.append("items").value = ["first"];
        expect(config.getIf("items").value).to.deep.eq(["first"]);
    });

    it('appendIf stores a value when condition is true', () => {
        const config = new ExtConfig({});
        config.appendIf(true, "key").value = ["yes"];
        expect(config.getIf("key").value).to.deep.eq(["yes"]);
    });

    it('appendIf is a no-op when condition is false', () => {
        const config = new ExtConfig({existing: "val"});
        config.appendIf(false, "existing");
        expect(config.getIf("existing").value).to.eq("val");
    });

    it('deepCopy produces an independent copy', () => {
        const config = new ExtConfig({name: "Alice"});
        const copy = config.deepCopy;
        expect(copy.getIf("name").value).to.eq("Alice");
        // mutating the copy must not affect the original
        copy.assign("name").value = "Bob";
        expect(config.getIf("name").value).to.eq("Alice");
    });
});
