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
import * as sinon from "sinon";
import {Implementation} from "../../impl/AjaxImpl";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";

import protocolPage = StandardInits.protocolPage;
import {Config, DQ} from "mona-dish";
import {expect} from "chai";
import HTML_PREFIX_EMBEDDED_BODY = StandardInits.HTML_PREFIX_EMBEDDED_BODY;
import {it} from "mocha";
import {decodeEncodedValues} from "../../impl/util/FileUtils";
import {ExtConfig} from "../../impl/util/ExtDomQuery";

/**
 * merges a list of key value entries into a target config
 * @param target the target receiving the key value entries
 * @param keyValueEntries a list of key value entries divided by =
 * @param paramsMapper a key value remapper
 */
function mergeKeyValueEntries(target: Config, keyValueEntries: string[][], paramsMapper = (key, value) => [key, value]) {

    function fixKeyWithoutVal(keyVal: string[]) {
        return keyVal.length < 3 ? [keyVal?.[0] ?? [], keyVal?.[1] ?? []] : keyVal;
    }

    let toMerge = new ExtConfig({});
    keyValueEntries
        //special case of having keys without values
        .map(keyVal => fixKeyWithoutVal(keyVal))
        .map(keyVal => paramsMapper(keyVal[0] as string, keyVal[1]))
        .forEach(keyVal => {
            let value = keyVal?.splice(1)?.join("") ?? "";
            if(toMerge.getIfPresent(keyVal[0]).isPresent()) {
                toMerge.append(keyVal[0] as string).value = value;
            } else {
                toMerge.assign(keyVal[0] as string).value = value;
            }
        });

    target.shallowMerge(toMerge);
}

function getFormData(requestBody: string): Config {
    let ret = new Config({});
    mergeKeyValueEntries(ret, decodeEncodedValues(requestBody));
    return ret;
}

describe("test for proper request param patterns identical to the old implementation", function () {
    const UPDATE_INSERT_2 = {
        "op": "updateinsert2",
        "jakarta.faces.partial.event": "click",
        "jakarta.faces.source": "cmd_update_insert2",
        "jakarta.faces.partial.ajax": "true",
        "jakarta.faces.partial.execute": "cmd_update_insert2",
        "form1": "form1",
        "jakarta.faces.ViewState": "blubbblubblubb",
        "cmd_update_insert2": "update insert second protocol path"
    }
    /**
     * matches two maps for absolute identicality
     */
    let matches = (item1: { [key: string]: any }, item2: { [key: string]: any }): boolean => {
        if (Object.keys(item1).length != Object.keys(item2).length) {
            return false;
        }
        for (let key in item1) {
            if ((!(key in item2)) || item1[key] != item2[key]) {
                return false;
            }
        }
        return true;
    }


    beforeEach(async function () {

        let waitForResult = protocolPage();

        return waitForResult.then((close) => {

            this.xhr = sinon.useFakeXMLHttpRequest();
            this.requests = [];
            this.xhr.onCreate = (xhr) => {
                this.requests.push(xhr);
            };
            (<any>global).XMLHttpRequest = this.xhr;
            window.XMLHttpRequest = this.xhr;

            this.jsfAjaxResponse = sinon.stub((<any>global).faces.ajax, "response");

            this.closeIt = () => {
                (<any>global).XMLHttpRequest = window.XMLHttpRequest = this.xhr.restore();
                this.jsfAjaxResponse.restore();
                Implementation.reset();
                close();
            }
        });
    });

    afterEach(function () {
        this.closeIt();
    });

    it("must pass updateinsert2 with proper parameters", function () {
        DQ.byId("cmd_update_insert2").click();

        let requestBody = this.requests[0].requestBody;
        let formData = getFormData(requestBody);

        expect(matches(formData.value, UPDATE_INSERT_2)).to.be.true;
    });


    it("must handle base64 encoded strings properly as request data", function () {
        let probe = "YWFhYWFhc1Rlc3RpdCDDpGtvNDU5NjczMDA9PSsrNDU5MGV3b3UkJiUmLyQmJQ==";
        DQ.byId("jakarta.faces.ViewState").inputValue.value = probe;
        DQ.byId("cmd_update_insert2").click();
        let requestBody = this.requests[0].requestBody;
        //We check if the base64 encoded string matches the original
        let formData = getFormData(requestBody);

        expect(formData.getIf("jakarta.faces.ViewState").value == probe).to.be.true;
    });


    it("must handle empty parameters properly", function () {
        let probe = "";
        DQ.byId("jakarta.faces.ViewState").inputValue.value = probe;
        DQ.byId("cmd_update_insert2").click();
        let requestBody = this.requests[0].requestBody;
        //We check if the base64 encoded string matches the original
        let formData = getFormData(requestBody);

        expect(decodeURIComponent(formData.getIf("jakarta.faces.ViewState").value) == probe).to.be.true;
    });

    //KssbpZfCe+0lwDhgMRQ44wRFkaM1o1lbMMUO3lini5YhXWm6

    it("must handle base64 special cases properly (+ in encoding)", function () {
        let probe = "KssbpZfCe+0lwDhgMRQ44wRFkaM1o1lbMMUO3lini5YhXWm6";
        DQ.byId("jakarta.faces.ViewState").inputValue.value = probe;
        DQ.byId("cmd_update_insert2").click();
        let requestBody = this.requests[0].requestBody;
        //We check if the base64 encoded string matches the original
        let formData = getFormData(requestBody);

        expect(decodeURIComponent(formData.getIf("jakarta.faces.ViewState").value) == probe).to.be.true;
    });

    it("must handle prefixed inputs properly (prefixes must be present) faces4", function (done) {
        window.document.body.innerHTML = HTML_PREFIX_EMBEDDED_BODY;

        global["debug_inp"] = true;
        //we now run the tests here
        try {

            let event = {
                isTrusted: true,
                type: 'change',
                target: document.getElementById("page:input::field"),
                currentTarget: document.getElementById("page:input::field")
            };
            faces.ajax.request(document.getElementById("page:input"), event as any, {
                render: "page:output",
                execute: "page:input",
                params: {
                    "booga2.xxx": "yyy",
                    "javax.faces.behavior.event": "change",
                    "booga": "bla"
                },
            });
        } catch (err) {
            console.error(err);
            expect(false).to.eq(true);
        }
        const requestBody = this.requests[0].requestBody;
        //We check if the base64 encoded string matches the original
        expect(requestBody.indexOf("javax.faces.behavior.event")).to.not.eq(-1);
        expect(requestBody.indexOf("javax.faces.behavior.event=change")).to.not.eq(-1);
        expect(requestBody.indexOf("page%3Ainput=input_value")).to.not.eq(-1);
        done();
    });




    /**
     * This test is based on Tobago 6 (Jakarte EE 9).
     */
    it("must handle ':' in IDs properly", function (done) {
        window.document.body.innerHTML = `

<tobago-page locale="en" class="container-fluid" id="page" focus-on-error="true" wait-overlay-delay-full="1000" wait-overlay-delay-ajax="1000">
    <form action="/content/010-input/10-in/In.xhtml?jfwid=q6qbeuqed" id="page::form" method="post" accept-charset="UTF-8" data-tobago-context-path="">
        <input type="hidden" name="jakarta.faces.source" id="jakarta.faces.source" disabled="disabled">
        <tobago-focus id="page::lastFocusId">
            <input type="hidden" name="page::lastFocusId" id="page::lastFocusId::field">
        </tobago-focus>
        <input type="hidden" name="org.apache.myfaces.tobago.webapp.Secret" id="org.apache.myfaces.tobago.webapp.Secret" value="secretValue">
        <tobago-in id="page:input" class="tobago-auto-spacing">
            <input type="text" name="page:input" id="page:input::field" class="form-control" value="Bob">
            <tobago-behavior event="change" client-id="page:input" field-id="page:input::field" execute="page:input" render="page:output"></tobago-behavior>
        </tobago-in>
        <tobago-out id="page:output" class="tobago-auto-spacing">
            <span class="form-control-plaintext"></span>
        </tobago-out>
        <div class="tobago-page-menuStore">
        </div>
        <span id="page::faces-state-container">
            <input type="hidden" name="jakarta.faces.ViewState" id="j_id__v_0:jakarta.faces.ViewState:1" value="viewStateValue" autocomplete="off">
            <input type="hidden" name="jakarta.faces.RenderKitId" value="tobago">
            <input type="hidden" id="j_id__v_0:jakarta.faces.ClientWindow:1" name="jakarta.faces.ClientWindow" value="clientWindowValue">
        </span>
    </form>
</tobago-page>
`;

        //we now run the tests here
        try {

            let event = {
                isTrusted: true,
                type: 'change',
                target: document.getElementById("page:input::field"),
                currentTarget: document.getElementById("page:input::field")
            };
            global.debug2 = true;
            faces.ajax.request(document.getElementById("page:input"), event as any, {
                "jakarta.faces.behavior.event": 'change',
                execute: "page:input",
                render: "page:output"
            });
        } catch (err) {
            console.error(err);
            expect(false).to.eq(true);
        }
        const requestBody = this.requests[0].requestBody;
        expect(requestBody.indexOf("org.apache.myfaces.tobago.webapp.Secret=secretValue")).to.not.eq(-1);
        expect(requestBody.indexOf("page%3Ainput=Bob")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.ViewState=viewStateValue")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.RenderKitId=tobago")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.ClientWindow=clientWindowValue")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.behavior.event=change")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.partial.event=change")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.source=page%3Ainput")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.partial.ajax=true")).to.not.eq(-1);
        expect(requestBody.indexOf("page%3A%3Aform=page%3A%3Aform")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.partial.execute=page%3Ainput")).to.not.eq(-1);
        expect(requestBody.indexOf("jakarta.faces.partial.render=page%3Aoutput")).to.not.eq(-1);
        done();
    });

    /**
     * This test is based on Tobago 6.0.0 (Jakarte EE 10).
     */
    it("tobago tree select", function (done) {
        window.document.body.innerHTML = `
<tobago-page locale='de' class='container-fluid' id='page' focus-on-error='true' wait-overlay-delay-full='1000' wait-overlay-delay-ajax='1000'>
   <form action='/content/090-tree/01-select/Tree_Select.xhtml' id='page::form' method='post' accept-charset='UTF-8' data-tobago-context-path=''>
    <input type='hidden' name='jakarta.faces.source' id='jakarta.faces.source' disabled='disabled'>
    <tobago-focus id='page::lastFocusId'>
     <input type='hidden' name='page::lastFocusId' id='page::lastFocusId::field'>
    </tobago-focus>
    <input type='hidden' name='org.apache.myfaces.tobago.webapp.Secret' id='org.apache.myfaces.tobago.webapp.Secret' value='secretValue'>
    <div class='tobago-page-menuStore'>
    </div>
    <div class='tobago-page-toastStore'>
    </div>
    <span id='page::faces-state-container'><input type='hidden' name='jakarta.faces.ViewState' id='j_id__v_0:jakarta.faces.ViewState:1' value='viewStateValue' autocomplete='off'><input type='hidden' name='jakarta.faces.RenderKitId' value='tobago'><input type='hidden' id='j_id__v_0:jakarta.faces.ClientWindow:1' name='jakarta.faces.ClientWindow' value='clientWindowValue'></span>
    <tobago-tree id='page:categoriesTree' data-tobago-selectable='multi' selectable='multi'>
<tobago-tree-node id='page:categoriesTree:0:j_id_3' class='tobago-folder tobago-expanded' expandable='expandable' index='0' data-tobago-level='0'>
<span id='page:categoriesTree:0:j_id_4' class='tobago-toggle'><i class='bi-dash-square' data-tobago-open='bi-dash-square' data-tobago-closed='bi-plus-square'></i></span>
<tobago-tree-select class='form-check-inline form-check'>
<input class='form-check-input' type='checkbox' name='page:categoriesTree:0:select' value='page:categoriesTree:0:select' id='page:categoriesTree:0:select'>
<label class='form-check-label' for='page:categoriesTree:0:select'>Category</label>
<tobago-behavior event='change' client-id='page:categoriesTree:0:select' execute='page:categoriesTree:0:select page:categoriesTree' render='page:selectedNodesOutput page:categoriesTree'></tobago-behavior>
</tobago-tree-select>
</tobago-tree-node>
<tobago-tree-node id='page:categoriesTree:1:j_id_3' index='1' data-tobago-tree-parent='page:categoriesTree:0:j_id_3' parent='page:categoriesTree:0:j_id_3' data-tobago-level='1'>
<span id='page:categoriesTree:1:j_id_4' class='tobago-toggle invisible'><i class='bi-square invisible'></i></span>
<tobago-tree-select class='form-check-inline form-check'>
<input class='form-check-input' type='checkbox' name='page:categoriesTree:1:select' value='page:categoriesTree:1:select' id='page:categoriesTree:1:select'>
<label class='form-check-label' for='page:categoriesTree:1:select'>Sports</label>
<tobago-behavior event='change' client-id='page:categoriesTree:1:select' execute='page:categoriesTree:1:select page:categoriesTree' render='page:selectedNodesOutput page:categoriesTree'></tobago-behavior>
</tobago-tree-select>
</tobago-tree-node>
<tobago-tree-node id='page:categoriesTree:2:j_id_3' index='2' data-tobago-tree-parent='page:categoriesTree:0:j_id_3' parent='page:categoriesTree:0:j_id_3' data-tobago-level='1'>
<span id='page:categoriesTree:2:j_id_4' class='tobago-toggle invisible'><i class='bi-square invisible'></i></span>
<tobago-tree-select class='form-check-inline form-check'>
<input class='form-check-input' type='checkbox' name='page:categoriesTree:2:select' value='page:categoriesTree:2:select' id='page:categoriesTree:2:select'>
<label class='form-check-label' for='page:categoriesTree:2:select'>Movies</label>
<tobago-behavior event='change' client-id='page:categoriesTree:2:select' execute='page:categoriesTree:2:select page:categoriesTree' render='page:selectedNodesOutput page:categoriesTree'></tobago-behavior>
</tobago-tree-select>
</tobago-tree-node>
<tobago-tree-node id='page:categoriesTree:3:j_id_3' class='tobago-selected tobago-folder tobago-expanded' selected='selected' expandable='expandable' index='3' data-tobago-tree-parent='page:categoriesTree:0:j_id_3' parent='page:categoriesTree:0:j_id_3' data-tobago-level='1'>
<span id='page:categoriesTree:3:j_id_4' class='tobago-toggle'><i class='bi-dash-square' data-tobago-open='bi-dash-square' data-tobago-closed='bi-plus-square'></i></span>
<tobago-tree-select class='form-check-inline form-check'>
<input class='form-check-input' type='checkbox' name='page:categoriesTree:3:select' value='page:categoriesTree:3:select' id='page:categoriesTree:3:select' checked='checked'>
<label class='form-check-label' for='page:categoriesTree:3:select'>Music</label>
<tobago-behavior event='change' client-id='page:categoriesTree:3:select' execute='page:categoriesTree:3:select page:categoriesTree' render='page:selectedNodesOutput page:categoriesTree'></tobago-behavior>
</tobago-tree-select>
</tobago-tree-node>
<tobago-tree-node id='page:categoriesTree:4:j_id_3' index='4' data-tobago-tree-parent='page:categoriesTree:3:j_id_3' parent='page:categoriesTree:3:j_id_3' data-tobago-level='2'>
<span id='page:categoriesTree:4:j_id_4' class='tobago-toggle invisible'><i class='bi-square invisible'></i></span>
<tobago-tree-select class='form-check-inline form-check'>
<input class='form-check-input' type='checkbox' name='page:categoriesTree:4:select' value='page:categoriesTree:4:select' id='page:categoriesTree:4:select'>
<label class='form-check-label' for='page:categoriesTree:4:select'>Classic</label>
<tobago-behavior event='change' client-id='page:categoriesTree:4:select' execute='page:categoriesTree:4:select page:categoriesTree' render='page:selectedNodesOutput page:categoriesTree'></tobago-behavior>
</tobago-tree-select>
</tobago-tree-node>
<tobago-tree-node id='page:categoriesTree:5:j_id_3' index='5' data-tobago-tree-parent='page:categoriesTree:3:j_id_3' parent='page:categoriesTree:3:j_id_3' data-tobago-level='2'>
<span id='page:categoriesTree:5:j_id_4' class='tobago-toggle invisible'><i class='bi-square invisible'></i></span>
<tobago-tree-select class='form-check-inline form-check'>
<input class='form-check-input' type='checkbox' name='page:categoriesTree:5:select' value='page:categoriesTree:5:select' id='page:categoriesTree:5:select'>
<label class='form-check-label' for='page:categoriesTree:5:select'>Pop</label>
<tobago-behavior event='change' client-id='page:categoriesTree:5:select' execute='page:categoriesTree:5:select page:categoriesTree' render='page:selectedNodesOutput page:categoriesTree'></tobago-behavior>
</tobago-tree-select>
</tobago-tree-node>
<tobago-tree-node id='page:categoriesTree:6:j_id_3' class='tobago-folder' expandable='expandable' index='6' data-tobago-tree-parent='page:categoriesTree:3:j_id_3' parent='page:categoriesTree:3:j_id_3' data-tobago-level='2'>
<span id='page:categoriesTree:6:j_id_4' class='tobago-toggle'><i class='bi-plus-square' data-tobago-open='bi-dash-square' data-tobago-closed='bi-plus-square'></i></span>
<tobago-tree-select class='form-check-inline form-check'>
<input class='form-check-input' type='checkbox' name='page:categoriesTree:6:select' value='page:categoriesTree:6:select' id='page:categoriesTree:6:select'>
<label class='form-check-label' for='page:categoriesTree:6:select'>World</label>
<tobago-behavior event='change' client-id='page:categoriesTree:6:select' execute='page:categoriesTree:6:select page:categoriesTree' render='page:selectedNodesOutput page:categoriesTree'></tobago-behavior>
</tobago-tree-select>
</tobago-tree-node>
<tobago-tree-node id='page:categoriesTree:7:j_id_3' index='7' data-tobago-tree-parent='page:categoriesTree:0:j_id_3' parent='page:categoriesTree:0:j_id_3' data-tobago-level='1'>
<span id='page:categoriesTree:7:j_id_4' class='tobago-toggle invisible'><i class='bi-square invisible'></i></span>
<tobago-tree-select class='form-check-inline form-check'>
<input class='form-check-input' type='checkbox' name='page:categoriesTree:7:select' value='page:categoriesTree:7:select' id='page:categoriesTree:7:select'>
<label class='form-check-label' for='page:categoriesTree:7:select'>Games</label>
<tobago-behavior event='change' client-id='page:categoriesTree:7:select' execute='page:categoriesTree:7:select page:categoriesTree' render='page:selectedNodesOutput page:categoriesTree'></tobago-behavior>
</tobago-tree-select>
</tobago-tree-node>
<tobago-tree-node id='page:categoriesTree:8:j_id_3' class='tobago-folder tobago-expanded' expandable='expandable' index='8' data-tobago-tree-parent='page:categoriesTree:0:j_id_3' parent='page:categoriesTree:0:j_id_3' data-tobago-level='1'>
<span id='page:categoriesTree:8:j_id_4' class='tobago-toggle'><i class='bi-dash-square' data-tobago-open='bi-dash-square' data-tobago-closed='bi-plus-square'></i></span>
<tobago-tree-select class='form-check-inline form-check'>
<input class='form-check-input' type='checkbox' name='page:categoriesTree:8:select' value='page:categoriesTree:8:select' id='page:categoriesTree:8:select'>
<label class='form-check-label' for='page:categoriesTree:8:select'>Science</label>
<tobago-behavior event='change' client-id='page:categoriesTree:8:select' execute='page:categoriesTree:8:select page:categoriesTree' render='page:selectedNodesOutput page:categoriesTree'></tobago-behavior>
</tobago-tree-select>
</tobago-tree-node>
<tobago-tree-node id='page:categoriesTree:9:j_id_3' class='tobago-folder' expandable='expandable' index='9' data-tobago-tree-parent='page:categoriesTree:8:j_id_3' parent='page:categoriesTree:8:j_id_3' data-tobago-level='2'>
<span id='page:categoriesTree:9:j_id_4' class='tobago-toggle'><i class='bi-plus-square' data-tobago-open='bi-dash-square' data-tobago-closed='bi-plus-square'></i></span>
<tobago-tree-select class='form-check-inline form-check'>
<input class='form-check-input' type='checkbox' name='page:categoriesTree:9:select' value='page:categoriesTree:9:select' id='page:categoriesTree:9:select'>
<label class='form-check-label' for='page:categoriesTree:9:select'>Mathematics</label>
<tobago-behavior event='change' client-id='page:categoriesTree:9:select' execute='page:categoriesTree:9:select page:categoriesTree' render='page:selectedNodesOutput page:categoriesTree'></tobago-behavior>
</tobago-tree-select>
</tobago-tree-node>
<tobago-tree-node id='page:categoriesTree:10:j_id_3' index='10' data-tobago-tree-parent='page:categoriesTree:8:j_id_3' parent='page:categoriesTree:8:j_id_3' data-tobago-level='2'>
<span id='page:categoriesTree:10:j_id_4' class='tobago-toggle invisible'><i class='bi-square invisible'></i></span>
<tobago-tree-select class='form-check-inline form-check'>
<input class='form-check-input' type='checkbox' name='page:categoriesTree:10:select' value='page:categoriesTree:10:select' id='page:categoriesTree:10:select'>
<label class='form-check-label' for='page:categoriesTree:10:select'>Geography</label>
<tobago-behavior event='change' client-id='page:categoriesTree:10:select' execute='page:categoriesTree:10:select page:categoriesTree' render='page:selectedNodesOutput page:categoriesTree'></tobago-behavior>
</tobago-tree-select>
</tobago-tree-node>
<tobago-tree-node id='page:categoriesTree:11:j_id_3' class='tobago-folder' expandable='expandable' index='11' data-tobago-tree-parent='page:categoriesTree:8:j_id_3' parent='page:categoriesTree:8:j_id_3' data-tobago-level='2'>
<span id='page:categoriesTree:11:j_id_4' class='tobago-toggle'><i class='bi-plus-square' data-tobago-open='bi-dash-square' data-tobago-closed='bi-plus-square'></i></span>
<tobago-tree-select class='form-check-inline form-check'>
<input class='form-check-input' type='checkbox' name='page:categoriesTree:11:select' value='page:categoriesTree:11:select' id='page:categoriesTree:11:select'>
<label class='form-check-label' for='page:categoriesTree:11:select'>Astronomy</label>
<tobago-behavior event='change' client-id='page:categoriesTree:11:select' execute='page:categoriesTree:11:select page:categoriesTree' render='page:selectedNodesOutput page:categoriesTree'></tobago-behavior>
</tobago-tree-select>
</tobago-tree-node>
<input type='hidden' name='page:categoriesTree::selected' id='page:categoriesTree::selected' class='tobago-selected' value='[3]'>
<input type='hidden' name='page:categoriesTree::expanded' id='page:categoriesTree::expanded' class='tobago-expanded' value='[0,3,8]'>
<tobago-scroll>
<input id='page:categoriesTree::scrollPosition' name='page:categoriesTree::scrollPosition' type='hidden' value='[0,0]' data-tobago-scroll-position='true'>
</tobago-scroll>
</tobago-tree>
    <tobago-out id='page:selectedNodesOutput' class='tobago-label-container tobago-auto-spacing'><label for='page:selectedNodesOutput' class='col-form-label'>Selected Nodes</label><span class='form-control-plaintext'>Music</span></tobago-out>
   </form>
   <noscript>
    <div class='tobago-page-noscript'>Diese Seite benötigt JavaScript, allerdings ist JavaScript in Ihrem Browser derzeit deaktiviert. Um JavaScript zu aktivieren, lesen Sie ggf. die Anleitung Ihres Browsers.
    </div>
   </noscript>
  </tobago-page>
`;

        //we now run the tests here
        try {
            document.querySelector<HTMLInputElement>("input[name='page:categoriesTree:3:select']").checked = false;

            let event = {
                isTrusted: true,
                type: 'change',
                target: document.getElementById("page:categoriesTree:3:select"),
                currentTarget: document.getElementById("page:categoriesTree:3:select")
            };
            global.debug2 = true;
            faces.ajax.request(
                document.getElementById("page:categoriesTree:3:select"),
                event as any,
                {
                    "jakarta.faces.behavior.event": "change",
                    execute: 'page:categoriesTree:3:select page:categoriesTree',
                    render: 'page:selectedNodesOutput page:categoriesTree'
                });
        } catch (err) {
            console.error(err);
            expect(false).to.eq(true);
        }
        const requestBody = this.requests[0].requestBody;
        let arsArr = requestBody.split("&");
        let resultsMap = {};
        for (let val of arsArr) {
            let keyVal = val.split("=");

            if (resultsMap[keyVal[0]]) {
                console.log("duplicated key '" + keyVal[0] + "'");
                expect(resultsMap[keyVal[0]]).not.to.exist;
            }
            resultsMap[keyVal[0]] = keyVal[1];
        }

        expect(resultsMap[encodeURIComponent("page::lastFocusId")]).to.exist;
        expect(resultsMap["org.apache.myfaces.tobago.webapp.Secret"]).to.eq("secretValue");
        expect(resultsMap["jakarta.faces.ViewState"]).to.eq("viewStateValue");
        expect(resultsMap["jakarta.faces.RenderKitId"]).to.eq("tobago");
        expect(resultsMap["jakarta.faces.ClientWindow"]).to.eq("clientWindowValue");
        expect(resultsMap[encodeURIComponent("page:categoriesTree::selected")]).to.eq(encodeURIComponent("[3]"));
        expect(resultsMap[encodeURIComponent("page:categoriesTree::expanded")]).to.eq(encodeURIComponent("[0,3,8]"));
        expect(resultsMap[encodeURIComponent("page:categoriesTree::scrollPosition")]).to.eq(encodeURIComponent("[0,0]"));
        expect(resultsMap["jakarta.faces.behavior.event"]).to.eq("change");
        expect(resultsMap["jakarta.faces.partial.event"]).to.eq("change");
        expect(resultsMap["jakarta.faces.source"]).to.eq(encodeURIComponent("page:categoriesTree:3:select"));
        expect(resultsMap["jakarta.faces.partial.ajax"]).to.eq("true");
        expect(resultsMap[encodeURIComponent("page::form")]).to.eq(encodeURIComponent("page::form"));
        expect(resultsMap["jakarta.faces.partial.execute"]).to.eq(encodeURIComponent("page:categoriesTree:3:select page:categoriesTree"));
        expect(resultsMap["jakarta.faces.partial.render"]).to.eq(encodeURIComponent("page:selectedNodesOutput page:categoriesTree"));
        expect(resultsMap[encodeURIComponent("page:categoriesTree:3:select")]).not.to.exist;

        done();
    });

  /**
   * This test is based on Tobago 6.0.0 (Jakarte EE 10).
   */
  it("tobago selectManyShuttle", function (done) {
    window.document.body.innerHTML = `
<tobago-page locale="de" class="container-fluid" id="page" focus-on-error="true" wait-overlay-delay-full="1000" wait-overlay-delay-ajax="1000">
   <form action="/content/030-select/70-selectManyShuttle/Shuttle.xhtml" id="page::form" method="post" accept-charset="UTF-8" data-tobago-context-path="">
    <input type="hidden" name="jakarta.faces.source" id="jakarta.faces.source" disabled="disabled">
    <tobago-focus id="page::lastFocusId">
     <input type="hidden" name="page::lastFocusId" id="page::lastFocusId::field">
    </tobago-focus>
    <input type="hidden" name="org.apache.myfaces.tobago.webapp.Secret" id="org.apache.myfaces.tobago.webapp.Secret" value="secretValue">
    <div class="tobago-page-menuStore">
    </div>
    <div class="tobago-page-toastStore">
    </div>
    <span id="page::faces-state-container"><input type="hidden" name="jakarta.faces.ViewState" id="j_id__v_0:jakarta.faces.ViewState:1" value="viewStateValue" autocomplete="off"><input type="hidden" name="jakarta.faces.RenderKitId" value="tobago"><input type="hidden" id="j_id__v_0:jakarta.faces.ClientWindow:1" name="jakarta.faces.ClientWindow" value="clientWindowValue"></span>
    <tobago-select-many-shuttle id="page:ajaxExample" class="tobago-auto-spacing">
     <div class="tobago-body">
      <div class="tobago-unselected-container">
       <select id="page:ajaxExample::unselected" class="tobago-unselected form-select" multiple="multiple" size="4">
        <option value="Proxima Centauri">Proxima Centauri
        </option>
        <option value="Alpha Centauri">Alpha Centauri
        </option>
        <option value="Wolf 359">Wolf 359
        </option></select>
      </div>
      <div class="tobago-controls">
       <div class="btn-group-vertical">
        <button type="button" class="btn btn-secondary" id="page:ajaxExample::addAll"><i class="bi-chevron-double-right"></i></button>
        <button type="button" class="btn btn-secondary" id="page:ajaxExample::add"><i class="bi-chevron-right"></i></button>
        <button type="button" class="btn btn-secondary" id="page:ajaxExample::remove"><i class="bi-chevron-left"></i></button>
        <button type="button" class="btn btn-secondary" id="page:ajaxExample::removeAll"><i class="bi-chevron-double-left"></i></button>
       </div>
      </div>
      <div class="tobago-selected-container">
       <select id="page:ajaxExample::selected" class="tobago-selected form-select" multiple="multiple" size="4">
        <option value="Sirius">Sirius
        </option></select>
      </div>
      <select class="d-none" id="page:ajaxExample::hidden" name="page:ajaxExample" multiple="multiple">
       <option value="Proxima Centauri">Proxima Centauri
       </option>
       <option value="Alpha Centauri">Alpha Centauri
       </option>
       <option value="Wolf 359">Wolf 359
       </option>
       <option value="Sirius" selected="selected">Sirius
       </option></select>
     </div>
     <tobago-behavior event="change" client-id="page:ajaxExample" execute="page:ajaxExample" render="page:outputStars"></tobago-behavior>
    </tobago-select-many-shuttle>
    <tobago-out id="page:outputStars" class="tobago-label-container tobago-auto-spacing"><label for="page:outputStars" class="col-form-label">Selected Stars</label><span class="form-control-plaintext">[Sirius]</span></tobago-out>
   </form>
  </tobago-page>
`;

    //we now run the tests here
    try {
      let siriusOption = document.querySelector<HTMLOptionElement>(".tobago-selected option");
      document.querySelector<HTMLSelectElement>(".tobago-unselected").add(siriusOption);
      document.getElementById("page:ajaxExample::hidden")
          .querySelector<HTMLOptionElement>("option[value='Sirius']").selected = false;

      let event = {
        isTrusted: true,
        type: 'change',
        target: document.getElementById("page:ajaxExample"),
        currentTarget: document.getElementById("page:ajaxExample")
      };
      global.debug2 = true;
      faces.ajax.request(
          document.getElementById("page:ajaxExample"),
          event as any,
          {
            "jakarta.faces.behavior.event": "change",
            execute: 'page:ajaxExample',
            render: 'page:outputStars'
          });
    } catch (err) {
      console.error(err);
      expect(false).to.eq(true);
    }
    const requestBody = this.requests[0].requestBody;
    let arsArr = requestBody.split("&");
    let resultsMap = {};
    for (let val of arsArr) {
      let keyVal = val.split("=");

      if (resultsMap[keyVal[0]]) {
        console.log("duplicated key '" + keyVal[0] + "'");
        expect(resultsMap[keyVal[0]]).not.to.exist;
      }
      resultsMap[keyVal[0]] = keyVal[1];
    }

    expect(resultsMap[encodeURIComponent("page::lastFocusId")]).to.exist;
    expect(resultsMap["org.apache.myfaces.tobago.webapp.Secret"]).to.eq("secretValue");
    expect(resultsMap["jakarta.faces.ViewState"]).to.eq("viewStateValue");
    expect(resultsMap["jakarta.faces.RenderKitId"]).to.eq("tobago");
    expect(resultsMap["jakarta.faces.ClientWindow"]).to.eq("clientWindowValue");
    expect(resultsMap["jakarta.faces.behavior.event"]).to.eq("change");
    expect(resultsMap["jakarta.faces.partial.event"]).to.eq("change");
    expect(resultsMap["jakarta.faces.source"]).to.eq(encodeURIComponent("page:ajaxExample"));
    expect(resultsMap["jakarta.faces.partial.ajax"]).to.eq("true");
    expect(resultsMap[encodeURIComponent("page::form")]).to.eq(encodeURIComponent("page::form"));
    expect(resultsMap["jakarta.faces.partial.execute"]).to.eq(encodeURIComponent("page:ajaxExample"));
    expect(resultsMap["jakarta.faces.partial.render"]).to.eq(encodeURIComponent("page:outputStars"));
    expect(resultsMap[encodeURIComponent("page:ajaxExample")]).not.to.exist;

    done();
  });
});
