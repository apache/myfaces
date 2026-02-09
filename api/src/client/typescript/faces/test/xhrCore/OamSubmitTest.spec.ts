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
import {Implementation} from "../../impl/AjaxImpl";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
const defaultMyFaces = StandardInits.defaultMyFaces;
import {_Es2019Array, DQ, DQ$} from "mona-dish";
import {expect} from "chai";
import * as nise from "nise";

describe('Tests for the MyFaces specific oam submit', function () {
    let oldFlatMap = null;
    beforeEach(async function () {

        let waitForResult = defaultMyFaces();

        return waitForResult.then((close) => {

            this.xhr = nise.fakeXhr.useFakeXMLHttpRequest();
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

    it(("must handle oam submit correctly, tuples"), (done) => {

        let options = [["booga1", "value1"], ["booga2", "value2"]];

        (DQ.byId("blarg")
            .getAsElem(0).value as HTMLFormElement).onsubmit = (evt) => {
            expect(DQ$("#blarg #booga1").inputValue.value).to.eq("value1");
            expect(DQ$("#blarg #booga2").inputValue.value).to.eq("value2");
            done();
            return false;
        };

        myfaces.oam.submitForm("blarg", null, null, options);

    });
    it(("must handle oam submit correctly, associative array"), (done) => {

        let options = {"booga1": "value1", "booga2": "value2"};

        (DQ.byId("blarg")
            .getAsElem(0).value as HTMLFormElement).onsubmit = (evt) => {
            expect(DQ$("#blarg #booga1").inputValue.value).to.eq("value1");
            expect(DQ$("#blarg #booga2").inputValue.value).to.eq("value2");
            done();
            return false;
        };

        myfaces.oam.submitForm("blarg", null, null, options);

    });

    it(("all hidden inputs must be cleared post submit"), (done) => {

        let options = {"booga1": "value1", "booga2": "value2"};

        (DQ.byId("blarg")
            .getAsElem(0).value as HTMLFormElement).onsubmit = (evt) => {
            expect(DQ$("#blarg #booga1").length).to.eq(1);
            expect(DQ$("#blarg #booga2").length).to.eq(1);
            return false;
        };

        myfaces.oam.submitForm("blarg", null, null, options);

        expect(DQ$("#blarg #booga1").length).to.eq(0);
        expect(DQ$("#blarg #booga2").length).to.eq(0);
        done();
    });

    it(("must handle linkid correctly, associative array"), (done) => {

        let options = {"booga1": "value1", "booga2": "value2"};

        (DQ.byId("blarg")
            .getAsElem(0).value as HTMLFormElement).onsubmit = (evt) => {
            expect(DQ.byId(`blarg:_idcl`).inputValue.value).to.eq("bla");
            done();
            return false;
        };

        myfaces.oam.submitForm("blarg", "bla", null, options);

    });

    it(("must handle target correctly, associative array"), (done) => {

        let options = {"booga1": "value1", "booga2": "value2"};
        (DQ.byId("blarg")
            .getAsElem(0).value as HTMLFormElement).onsubmit = (evt) => {
            expect(DQ$(`#blarg`).attr("target").value).to.eq("target1");
            done();
            return false;
        };
        myfaces.oam.submitForm("blarg", "bla", "target1", options);

    });

    it(("must handle limited parameters"), (done) => {

        (DQ.byId("blarg")
            .getAsElem(0).value as HTMLFormElement).onsubmit = (evt) => {
            expect(DQ.byId(`blarg:_idcl`).inputValue.value).to.eq("bla");
            done();
            return false;
        };
        try {
        myfaces.oam.submitForm("blarg", "bla");
        } catch(e) {
            done(e);
        }

    });
    it(("must handle limited parameters 2"), (done) => {

        (DQ.byId("blarg")
            .getAsElem(0).value as HTMLFormElement).onsubmit = (evt) => {
            expect(DQ.byId(`blarg:_idcl`).inputValue.value).to.eq("bla");
            done();
            return false;
        };
        try {
            myfaces.oam.submitForm("blarg", "bla", null);
        } catch(e) {
            done(e);
        }
    });
    it(("must handle limited parameters 3"), (done) => {

        (DQ.byId("blarg")
            .getAsElem(0).value as HTMLFormElement).onsubmit = (evt) => {
            expect(DQ.byId(`blarg:_idcl`).inputValue.value).to.eq("bla");
            done();
            return false;
        };
        try {
            myfaces.oam.submitForm("blarg", "bla", null, null);
        } catch(e) {
            done(e);
        }
    });

});