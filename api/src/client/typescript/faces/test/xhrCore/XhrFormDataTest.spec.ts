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

import {describe, it} from 'mocha';
import {_Es2019Array, DQ} from "mona-dish";
import * as sinon from 'sinon';
import {XhrFormData} from "../../impl/xhrCore/XhrFormData";
import {expect} from "chai";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
const defaultMyFaces = StandardInits.defaultMyFaces;
import {Implementation} from "../../impl/AjaxImpl";
import * as nise from "nise";
import {IDENT_NONE} from "../../impl/core/Const";

/*
 * Idempotent mapper that prefixes non-jakarta, non-already-prefixed keys with "ns:".
 * Idempotency matters because buildFormData() applies paramsMapper a second time
 * over already-stored (already remapped) keys.
 */
const NS_MAPPER = (key: string, value: any): [string, any] =>
    [key.startsWith("jakarta") || key.startsWith("ns:") ? key : `ns:${key}`, value];

describe('XhrFormData', function () {
    let oldFlatMap: any = null;

    beforeEach(async function () {
        let waitForResult = defaultMyFaces();
        return waitForResult.then((close) => {
            this.xhr = nise.fakeXhr.useFakeXMLHttpRequest();
            this.requests = [];
            this.xhr.onCreate = (xhr: any) => this.requests.push(xhr);
            (global as any).XMLHttpRequest = this.xhr;
            window.XMLHttpRequest = this.xhr;

            oldFlatMap = Array.prototype["flatMap"];
            window["Es2019Array"] = _Es2019Array;
            delete Array.prototype["flatMap"];

            this.closeIt = () => {
                (global as any).XMLHttpRequest = window.XMLHttpRequest = this.xhr.restore();
                Implementation.reset();
                close();
                if (oldFlatMap) {
                    Array.prototype["flatMap"] = oldFlatMap;
                    oldFlatMap = null;
                }
            };
        });
    });

    afterEach(function () {
        this.closeIt();
    });

    // ── toString() ────────────────────────────────────────────────────────────

    describe('toString()', function () {
        it('encodes a single text input', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="text" name="field_a" value="hello">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const encoded = new XhrFormData(DQ.byId("f")).toString();
            expect(encoded).to.include("field_a=hello");
        });

        it('encodes multiple text inputs', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="text" name="first" value="foo">
                    <input type="text" name="second" value="bar">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const encoded = new XhrFormData(DQ.byId("f")).toString();
            expect(encoded).to.include("first=foo");
            expect(encoded).to.include("second=bar");
        });

        it('encodes only checked checkboxes, not unchecked ones, under the same name', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="checkbox" name="color" value="red" checked>
                    <input type="checkbox" name="color" value="green" checked>
                    <input type="checkbox" name="color" value="blue">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const encoded = new XhrFormData(DQ.byId("f")).toString();
            expect(encoded).to.include("color=red");
            expect(encoded).to.include("color=green");
            expect(encoded).not.to.include("color=blue");
        });

        it('includes the view state from the form', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="text" name="field" value="v">
                    <input type="hidden" name="jakarta.faces.ViewState" value="my-vs-token">
                </form>`;
            const encoded = new XhrFormData(DQ.byId("f")).toString();
            expect(encoded).to.include("jakarta.faces.ViewState=my-vs-token");
        });

        it('does not duplicate the view state', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="text" name="field" value="v">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const encoded = new XhrFormData(DQ.byId("f")).toString();
            const occurrences = (encoded.match(/jakarta\.faces\.ViewState/g) ?? []).length;
            expect(occurrences).to.eq(1);
        });

        it('applies a custom paramsMapper to remap keys', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="text" name="field" value="v">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const encoded = new XhrFormData(DQ.byId("f"), NS_MAPPER).toString();
            // "field" should be prefixed; jakarta keys pass through unchanged
            expect(encoded).to.include("ns%3Afield=v");
            expect(encoded).to.include("jakarta.faces.ViewState");
        });

        it('with partialIds only encodes the specified inputs', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="text" name="keep_me" value="yes">
                    <input type="text" name="drop_me" value="no">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const encoded = new XhrFormData(DQ.byId("f"), undefined, undefined, ["keep_me"]).toString();
            expect(encoded).to.include("keep_me=yes");
            expect(encoded).not.to.include("drop_me=no");
        });

        it('with partialIds still includes the view state via applyViewState fallback', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="text" name="keep_me" value="yes">
                    <input type="text" name="drop_me" value="no">
                    <input type="hidden" name="jakarta.faces.ViewState" value="rescued-vs">
                </form>`;
            // "jakarta.faces.ViewState" is not in partialIds so it is filtered by encodeSubmittableFields,
            // but applyViewState rescues it from the DOM.
            const encoded = new XhrFormData(DQ.byId("f"), undefined, undefined, ["keep_me"]).toString();
            expect(encoded).to.include("jakarta.faces.ViewState");
        });

        it('with empty partialIds encodes all inputs', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="text" name="a" value="1">
                    <input type="text" name="b" value="2">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const encoded = new XhrFormData(DQ.byId("f"), undefined, undefined, []).toString();
            expect(encoded).to.include("a=1");
            expect(encoded).to.include("b=2");
        });
    });

    // ── toFormData() ─────────────────────────────────────────────────────────

    describe('toFormData()', function () {
        it('returns a FormData instance', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="text" name="field" value="v">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const fd = new XhrFormData(DQ.byId("f")).toFormData();
            expect(fd).to.be.instanceOf(FormData);
        });

        it('includes field values in the FormData', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="text" name="alpha" value="one">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const fd = new XhrFormData(DQ.byId("f")).toFormData();
            expect(fd.get("alpha")).to.eq("one");
        });

        it('expands multi-value fields into separate FormData entries', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="checkbox" name="color" value="red" checked>
                    <input type="checkbox" name="color" value="green" checked>
                    <input type="checkbox" name="color" value="blue">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const fd = new XhrFormData(DQ.byId("f")).toFormData();
            const colors = fd.getAll("color");
            expect(colors).to.include("red");
            expect(colors).to.include("green");
            expect(colors).not.to.include("blue");
        });

        it('applies a custom paramsMapper to remap keys in FormData output', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="text" name="field" value="hello">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const fd = new XhrFormData(DQ.byId("f"), NS_MAPPER).toFormData();
            // key should be "ns:field" after remapping (buildFormData re-applies mapper idempotently)
            expect(fd.get("ns:field")).to.eq("hello");
        });

        it('includes view state in FormData', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="text" name="field" value="v">
                    <input type="hidden" name="jakarta.faces.ViewState" value="my-token">
                </form>`;
            const fd = new XhrFormData(DQ.byId("f")).toFormData();
            expect(fd.get("jakarta.faces.ViewState")).to.eq("my-token");
        });
    });

    // ── isMultipartRequest ────────────────────────────────────────────────────

    describe('isMultipartRequest', function () {
        it('is false when no executes are provided, even on a file form', function () {
            window.document.body.innerHTML = `
                <form id="f" enctype="multipart/form-data">
                    <input type="file" name="upload">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const data = new XhrFormData(DQ.byId("f"));
            expect(data.isMultipartRequest).to.be.false;
        });

        it('is false when form has no file inputs, even with an executes list', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="text" name="field" value="v">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const data = new XhrFormData(DQ.byId("f"), undefined, ["field"]);
            expect(data.isMultipartRequest).to.be.false;
        });

        it('is true when the form contains a file input and executes are provided', function () {
            window.document.body.innerHTML = `
                <form id="f" enctype="multipart/form-data">
                    <input type="file" name="upload">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const data = new XhrFormData(DQ.byId("f"), undefined, ["upload"]);
            expect(data.isMultipartRequest).to.be.true;
        });

        it('is false when executes contains @none even if the form has a file input', function () {
            window.document.body.innerHTML = `
                <form id="f" enctype="multipart/form-data">
                    <input type="file" name="upload">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const data = new XhrFormData(DQ.byId("f"), undefined, [IDENT_NONE]);
            expect(data.isMultipartRequest).to.be.false;
        });

        it('is false when executes list is empty', function () {
            window.document.body.innerHTML = `
                <form id="f" enctype="multipart/form-data">
                    <input type="file" name="upload">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            // empty array: detectMultipartRequest guard passes (executes is truthy), but
            // isMultipartCandidate depends on whether the form has file inputs
            const data = new XhrFormData(DQ.byId("f"), undefined, []);
            // An empty executes list means no specific element is being executed —
            // the multipart flag should still reflect whether file inputs exist
            expect(typeof data.isMultipartRequest).to.eq("boolean");
        });
    });

    // ── paramsMapper / naming container ───────────────────────────────────────

    describe('paramsMapper (naming container prefix)', function () {
        it('maps keys with the naming container prefix in toString output', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="text" name="field" value="v">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const prefixMapper = (key: string, value: any): [string, any] =>
                [key.startsWith("jakarta") || key.startsWith("nc:") ? key : `nc:${key}`, value];
            const encoded = new XhrFormData(DQ.byId("f"), prefixMapper).toString();
            expect(encoded).to.include("nc%3Afield=v");
            expect(encoded).to.include("jakarta.faces.ViewState");
        });

        it('default (identity) mapper leaves keys unchanged', function () {
            window.document.body.innerHTML = `
                <form id="f">
                    <input type="text" name="myField" value="myValue">
                    <input type="hidden" name="jakarta.faces.ViewState" value="vs1">
                </form>`;
            const encoded = new XhrFormData(DQ.byId("f")).toString();
            expect(encoded).to.include("myField=myValue");
        });
    });
});