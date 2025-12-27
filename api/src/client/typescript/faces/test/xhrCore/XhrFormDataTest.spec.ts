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

const jsdom = require("jsdom");
const {JSDOM} = jsdom;

describe('XhrFormData tests', function () {
    let oldFlatMap = null;
    beforeEach(async function () {

        let waitForResult = defaultMyFaces();

        return waitForResult.then((close) => {

            this.xhr = sinon.useFakeXMLHttpRequest();
            this.requests = [];
            this.xhr.onCreate = (xhr) => {
                this.requests.push(xhr);
            };
            (global as any).XMLHttpRequest = this.xhr;
            window.XMLHttpRequest = this.xhr;

            this.jsfAjaxResponse = sinon.spy((global as any).faces.ajax, "response");
            oldFlatMap =Array.prototype["flatMap"];
            window["Es2019Array"] = _Es2019Array;
            delete Array.prototype["flatMap"];

            this.closeIt = () => {
                (global as any).XMLHttpRequest = window.XMLHttpRequest = this.xhr.restore();
                this.jsfAjaxResponse.restore();
                Implementation.reset();
                close();
                if(oldFlatMap) {
                    Array.prototype["flatMap"] = oldFlatMap;
                    oldFlatMap = null;
                }
            }
        });
    });

    beforeEach(function () {

        let waitForResult = defaultMyFaces();

        return waitForResult.then((close) => {
            (global as any).window = window;
            (global as any).body = window.document.body;
            (global as any).document = window.document;
            global.body.innerHTML = `
      <div id="id_1"></div>
      <div id="id_2" booga="blarg"></div>
      <div id="id_3"></div>
      <div id="id_4"></div>
    `;
            (global as any).navigator = {
                language: "en-En"
            };

            this.xhr = sinon.useFakeXMLHttpRequest();
            this.requests = [];
            this.xhr.onCreate = (xhr) => {
                this.requests.push(xhr);
            };
            (global as any).XMLHttpRequest = this.xhr;
            window.XMLHttpRequest = this.xhr;

        });

        this.afterEach(function () {
            (global as any).XMLHttpRequest = window.XMLHttpRequest = this.xhr.restore();
        });
    });

    it("must have multiple values for a name", function () {
        window.document.body.innerHTML = `<form id="page::form">
      <tobago-select-many-checkbox id="page:animals">
        <label for="page:animals">Checkbox Group</label>
        <label><input type="checkbox" name="page:animals" id="page:animals::0" value="Cat" checked="checked">Cat</label>
        <label><input type="checkbox" name="page:animals" id="page:animals::1" value="Dog">Dog</label>
        <label><input type="checkbox" name="page:animals" id="page:animals::2" value="Fox" checked="checked">Fox</label>
        <label><input type="checkbox" name="page:animals" id="page:animals::3" value="Rabbit">Rabbit</label>
      </tobago-select-many-checkbox>
      <div id="page:animalsOutput">
        <label for="page:animalsOutput">Selected Animals</label>
        <span>Cat, Fox</span>
      </div>
    </form>`;

        global.debugf2 = true;
        const xhrFormData = new XhrFormData(DQ.byId("page::form"));
        const formData = xhrFormData.toString();

        expect(formData).to.contain("animals=Cat");
        expect(formData).to.contain("animals=Fox");
    });
});
