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
import {DQ} from "mona-dish";
import {Implementation} from "../../../impl/AjaxImpl";
import {HiddenInputBuilder} from "../../../impl/util/HiddenInputBuilder";
import {StandardInits} from "../../frameworkBase/_ext/shared/StandardInits";

describe("HiddenInputBuilder", function () {

    beforeEach(function () {
        return StandardInits.defaultMyFaces().then((close) => {
            this.closeIt = () => {
                Implementation.reset();
                close();
            };
        });
    });

    afterEach(function () {
        this.closeIt();
    });

    it("must build and append the next ViewState hidden input for a named view root", function () {
        document.body.innerHTML = `
            <form id="viewroot_1:form1">
                <input type="hidden" id="viewroot_1:jakarta.faces.ViewState:2" name="viewroot_1:jakarta.faces.ViewState" value="old">
            </form>`;

        const form = DQ.byId("viewroot_1:form1");
        const hiddenInput = new HiddenInputBuilder("input[name*='jakarta.faces.ViewState']")
            .withNamingContainerId("viewroot_1")
            .withNamedViewRoot(true)
            .withParent(form)
            .build();

        hiddenInput.val = "new";

        expect(hiddenInput.id.value).to.eq("viewroot_1:jakarta.faces.ViewState:3");
        expect(hiddenInput.name.value).to.eq("viewroot_1:jakarta.faces.ViewState");
        expect(hiddenInput.val).to.eq("new");
        expect(form.querySelectorAll("#viewroot_1\\:jakarta\\.faces\\.ViewState\\:3").isPresent()).to.be.true;
    });

    it("must build ClientWindow without prefixing the name for a non-named view root", function () {
        document.body.innerHTML = `<form id="form1"></form>`;

        const hiddenInput = new HiddenInputBuilder("input[name*='jakarta.faces.ClientWindow']")
            .withNamingContainerId("viewroot_1")
            .withNamedViewRoot(false)
            .withParent(DQ.byId("form1"))
            .build();

        expect(hiddenInput.id.value).to.eq("viewroot_1:jakarta.faces.ClientWindow:1");
        expect(hiddenInput.name.value).to.eq("jakarta.faces.ClientWindow");
        expect(DQ.byId("form1").querySelectorAll("[name='jakarta.faces.ClientWindow']").isPresent()).to.be.true;
    });
});
