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

import {expect} from "chai";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import defaultMyFaces = StandardInits.defaultMyFaces;
import {oam} from "../../myfaces/OamSubmit";
import setHiddenInput = oam.setHiddenInput;
import {DomQuery} from "mona-dish";
import clearHiddenInput = oam.clearHiddenInput;
import submitForm = oam.submitForm;
import Sinon from "sinon";


declare var myfaces: any;

/**
 * specialized tests testing the xhr core behavior when it hits the xmlHttpRequest object
 */
describe('Tests on the xhr core when it starts to call the request', function () {

    beforeEach(() => {
        return defaultMyFaces();
    })

    it('namespace must exist', function() {
        expect(!!myfaces?.oam).to.eq(true);
        expect(!!myfaces?.oam?.setHiddenInput).to.eq(true);
        expect(!!myfaces?.oam?.clearHiddenInput).to.eq(true);
        expect(!!myfaces?.oam?.submitForm).to.eq(true);
    });

    it('hidden input setting must work', function() {
        let FORM_ID = "blarg";
        setHiddenInput(FORM_ID, "new_hidden", "hiddenvalue");
        expect(DomQuery.byId(FORM_ID).querySelectorAll("input[name='new_hidden']").isPresent());
        expect(DomQuery.byId(FORM_ID).querySelectorAll("input[name='new_hidden']").inputValue.value).to.eq("hiddenvalue");
    })

    it('resetting the hidden input must work', function() {
        let FORM_ID = "blarg";
        setHiddenInput(FORM_ID, "new_hidden", "hiddenvalue");
        clearHiddenInput(FORM_ID, "new_hidden");
        expect(DomQuery.byId(FORM_ID).querySelectorAll("input[name='new_hidden']").isAbsent()).to.eq(true);
    })

    it('submit form must work', function() {
        let FORM_ID = "blarg";
        let form = DomQuery.byId(FORM_ID);
        const submit_spy = Sinon.spy(() => {
            expect(form.querySelectorAll('input[name=\'booga1\']').isPresent()).to.eq(true);
            expect(form.querySelectorAll('input[name=\'booga1\']').length == 1).to.eq(true);
            expect(form.querySelectorAll('input[name=\'booga1\']').inputValue.value == 'val_booga1').to.eq(true);
            expect(form.querySelectorAll('input[name=\'booga2\']').isPresent()).to.eq(true);
            expect(form.querySelectorAll('input[name=\'booga2\']').length == 1).to.eq(true);
            expect(form.querySelectorAll('input[name=\'booga2\']').inputValue.value == 'val_booga2').to.eq(true);
            expect(form.querySelectorAll(`input[name='${FORM_ID}:_idcl']`).isPresent()).to.eq(true);
            expect(form.querySelectorAll(`input[name='${FORM_ID}:_idcl']`).length == 1).to.eq(true);
            expect(form.querySelectorAll(`input[name='${FORM_ID}:_idcl']`).inputValue.value == 'mylink').to.eq(true);
            expect(form.attr("target").value).to.eq('target1');
        });
        (form.value.value as any).submit = submit_spy;


        submitForm(FORM_ID, 'mylink', 'target1', {
            booga1: "val_booga1",
            booga2: "val_booga2"
        });

        expect(submit_spy.called).to.eq(true);
        form = DomQuery.byId(FORM_ID);
        expect(form.querySelectorAll('input[name=\'booga1\']').isAbsent()).to.eq(true);
        expect(form.querySelectorAll('input[name=\'booga2\']').isAbsent()).to.eq(true);
        expect(form.querySelectorAll(`input[name='${FORM_ID}:_idcl']`).isAbsent()).to.eq(true);

    })

    it('onsubmit form must work', function() {
        let FORM_ID = "blarg";
        let form = DomQuery.byId(FORM_ID);
        const onsumbit = () => {
            expect(form.querySelectorAll('input[name=\'booga1\']').isPresent()).to.eq(true);
            expect(form.querySelectorAll('input[name=\'booga1\']').length == 1).to.eq(true);
            expect(form.querySelectorAll('input[name=\'booga1\']').inputValue.value == 'val_booga1').to.eq(true);
            expect(form.querySelectorAll('input[name=\'booga2\']').isPresent()).to.eq(true);
            expect(form.querySelectorAll('input[name=\'booga2\']').length == 1).to.eq(true);
            expect(form.querySelectorAll('input[name=\'booga2\']').inputValue.value == 'val_booga2').to.eq(true);
            expect(form.querySelectorAll(`input[name='${FORM_ID}:_idcl']`).isPresent()).to.eq(true);
            expect(form.querySelectorAll(`input[name='${FORM_ID}:_idcl']`).length == 1).to.eq(true);
            expect(form.querySelectorAll(`input[name='${FORM_ID}:_idcl']`).inputValue.value == 'mylink').to.eq(true);
            expect(form.attr("target").value).to.eq('target1');
            return false;
        }
        const os_spy = Sinon.spy(onsumbit);
        const submit_spy = Sinon.spy(() => {});
        (form.value.value as any).onsubmit = os_spy;
        (form.value.value as any).submit = submit_spy;


        submitForm(FORM_ID, 'mylink', 'target1', {
            booga1: "val_booga1",
            booga2: "val_booga2"
        });

        //we also have to interceot onsbumit
        expect(os_spy.called).to.eq(true);
        expect(submit_spy.called).to.eq(false);

        form = DomQuery.byId(FORM_ID);
        expect(form.querySelectorAll('input[name=\'booga1\']').isAbsent()).to.eq(true);
        expect(form.querySelectorAll('input[name=\'booga2\']').isAbsent()).to.eq(true);
        expect(form.querySelectorAll(`input[name='${FORM_ID}:_idcl']`).isAbsent()).to.eq(true);
        // expect(submit_spy.called).to.eq(true);
    })

    // further tests will follow if needed, for now the namespace must be restored
});