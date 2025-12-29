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

import {describe, it} from "mocha";
import * as sinon from "sinon";
import {expect} from "chai";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import {_Es2019Array, DQ$} from "mona-dish";
import {
    P_AJAX,
    P_EXECUTE,
    P_AJAX_SOURCE,
    P_RENDER,
    P_VIEWSTATE,
    P_WINDOW_ID
} from "../../impl/core/Const";
const defaultMyFacesNamespaces = StandardInits.defaultMyFacesNamespaces;
import {escape} from "querystring";
import {ExtLang} from "../../impl/util/Lang";
const ofAssoc = ExtLang.ofAssoc;

declare var faces: any;
declare var Implementation: any;

let issueStdReq = function (element) {
    faces.ajax.request(element, null, {
        execute: "input_1",
        render: "@form",
        params: {
            pass1: "pass1",
            pass2: "pass2"
        }
    });
};

describe('Namespacing tests', function () {
    let oldFlatMap = null;
    beforeEach(async function () {

        let waitForResult = defaultMyFacesNamespaces();

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

    it('must send the element identifiers properly encoded', function () {
        let send = sinon.spy(XMLHttpRequest.prototype, "send");
        try {
            global["debug3"] = true;
            faces.ajax.request(document.getElementById("jd_0:input_2"), null, {
                execute: ":input_1",
                render: ":blarg :input_2",
                params: {
                    pass1: "pass1",
                    pass2: "pass2"
                }
            });

            expect(send.called).to.be.true;
            let argsVal: any = send.args[0][0];
            let arsArr = argsVal.split("&");
            let resultsMap = {};
            for (let val of arsArr) {
                let keyVal = val.split("=");
                resultsMap[keyVal[0]] = keyVal[1];
            }

            expect(resultsMap["pass1"]).to.eq("pass1");
            expect(resultsMap["pass2"]).to.eq("pass2");
            expect(!!resultsMap["render"]).to.be.false;
            expect(!!resultsMap["execute"]).to.be.false;
            expect(P_WINDOW_ID in resultsMap).to.be.false;
            expect(P_VIEWSTATE in resultsMap).to.be.true;
            expect(resultsMap[P_AJAX_SOURCE]).to.eq(escape("jd_0:input_2"));
            expect(resultsMap[P_AJAX]).to.eq("true");
            expect(resultsMap[P_RENDER]).to.eq(escape("jd_0:blarg jd_0:input_2"));
            expect(resultsMap[P_EXECUTE]).to.eq(escape("jd_0:input_1 jd_0:input_2"));
        } finally {
            send.restore();
        }
    })

    it('must send the element identifiers properly encoded 2', function () {
        let send = sinon.spy(XMLHttpRequest.prototype, "send");
        try {
            faces.ajax.request(document.getElementById("jd_0:input_2"), null, {
                execute: "jd_0:input_1",
                render: ":blarg jd_0:input_2",
                params: {
                    pass1: "pass1",
                    pass2: "pass2"
                }
            });

            expect(send.called).to.be.true;
            let argsVal: any = send.args[0][0];
            let arsArr = argsVal.split("&");
            let resultsMap = {};
            for (let val of arsArr) {
                let keyVal = val.split("=");
                resultsMap[keyVal[0]] = keyVal[1];
            }

            expect(resultsMap["pass1"]).to.eq("pass1");
            expect(resultsMap["pass2"]).to.eq("pass2");
            expect(!!resultsMap["render"]).to.be.false;
            expect(!!resultsMap["execute"]).to.be.false;
            expect(P_WINDOW_ID in resultsMap).to.be.false;
            expect(P_VIEWSTATE in resultsMap).to.be.true;
            expect(resultsMap[P_AJAX_SOURCE]).to.eq(escape("jd_0:input_2"));
            expect(resultsMap[P_AJAX]).to.eq("true");
            expect(resultsMap[P_RENDER]).to.eq(escape("jd_0:blarg jd_0:input_2"));
            expect(resultsMap[P_EXECUTE]).to.eq(escape("jd_0:input_1 jd_0:input_2"));
        } finally {
            send.restore();
        }
    })


    it('must get name prefixed viewstate properly', function () {
        let send = sinon.spy(XMLHttpRequest.prototype, "send");
        try {
            DQ$(`[name*='${P_VIEWSTATE}']`).attr("name").value = `jd_0:${P_VIEWSTATE}`;
            DQ$(`[name*='${P_VIEWSTATE}']`).val = "booga";

            faces.ajax.request(document.getElementById("jd_0:input_2"), null, {
                execute: "jd_0:input_1",
                render: ":blarg jd_0:input_2",
                params: {
                    pass1: "pass1",
                    pass2: "pass2"
                }
            });

            expect(send.called).to.be.true;
            let argsVal: any = send.args[0][0];
            let arsArr = argsVal.split("&");
            let resultsMap = {};
            for (let val of arsArr) {
                let keyVal = val.split("=");
                resultsMap[unescape(keyVal[0])] = unescape(keyVal[1]);
            }
            const NAMING_CONTAINER_PREF = "jd_0:";
            expect(resultsMap[NAMING_CONTAINER_PREF + "pass1"]).to.eq("pass1");
            expect(resultsMap[NAMING_CONTAINER_PREF + "pass2"]).to.eq("pass2");
            expect(!!resultsMap["render"]).to.be.false;
            expect(!!resultsMap["execute"]).to.be.false;

            let hasWindowdId = ofAssoc(resultsMap).filter(data => data[0].indexOf(P_WINDOW_ID) != -1)?.[0];
            let hasViewState = ofAssoc(resultsMap).filter(data => data[0].indexOf(P_VIEWSTATE) != -1)?.[0];

            expect(!!hasWindowdId).to.be.false;
            expect(!!hasViewState).to.be.true;

            let viewState = ofAssoc(resultsMap).filter(data => data[0].indexOf(P_VIEWSTATE) != -1).map(item => item[1])?.[0];

            expect(viewState).to.eq("booga");
            expect(resultsMap[NAMING_CONTAINER_PREF + P_AJAX_SOURCE]).to.eq("jd_0:input_2");
            expect(resultsMap[NAMING_CONTAINER_PREF + P_AJAX]).to.eq("true");
            expect(resultsMap[NAMING_CONTAINER_PREF + P_RENDER]).to.eq("jd_0:blarg jd_0:input_2");
            expect(resultsMap[NAMING_CONTAINER_PREF + P_EXECUTE]).to.eq("jd_0:input_1 jd_0:input_2");
        } finally {
            send.restore();
        }
    })

});
