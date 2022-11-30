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

import {Config, DomQuery} from "mona-dish";
import {describe, it} from 'mocha';
import {expect} from 'chai';
import * as sinon from 'sinon';

import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import {CTX_PARAM_REQ_PASS_THR, P_EXECUTE, P_RENDER} from "../../impl/core/Const";
import defaultMyFaces23 = StandardInits.defaultMyFaces23;


sinon.reset();

declare var jsf: any;
declare var Implementation: any;

/**
 * testing the javax.ajax.request api without triggering any
 * xhr request...
 * the idea is to shim the code which triggers the request out and check what is going in
 * and what is coming out
 */

describe('javax.ajax.request test suite', () => {

    beforeEach(async () => {
        return await defaultMyFaces23();
    });

    it("jsf.ajax.request can be called", () => {
        //we stub the addRequestToQueue, to enable the request check only
        //without any xhr and response, both will be tested separately for
        //proper behavior
        const addRequestToQueue = sinon.stub(Implementation.queueHandler, "addRequestToQueue");
        //now the javax.ajax.request should trigger but should not go into
        //the asynchronous event loop.
        //lets check it out

        try {
            DomQuery.byId("input_2").addEventListener("click", (event: Event) => {
                jsf.ajax.request(null, event, {render: '@all', execute: '@form'})
            }).click();

            expect(addRequestToQueue.called).to.be.true;
            expect(addRequestToQueue.callCount).to.eq(1);
            const context = (<Config>addRequestToQueue.args[0][2]);

            expect(context.getIf(CTX_PARAM_REQ_PASS_THR, P_RENDER).value).eq("@all");
            //Execute issuing form due to @form and always the issuing element
            expect(context.getIf(CTX_PARAM_REQ_PASS_THR, P_EXECUTE).value).eq("blarg input_2");
        } finally {
            //once done we restore the proper state
            addRequestToQueue.restore();
        }

    });

    it("javax.ajax.request passthroughs must end up in passthrough", (done) => {
        //TODO implementation
        done();
    });

    it("javax.util.chain must work", () => {
        let called = {};
        window.called = called;

        let func1 = () => {
            called["func1"] = true;
            return true;
        }

        let func2 = `function func2(called) {
            called["func2"] = true;
            return true;
        }`;

        let func3 = () => {
            called["func3"] = true;
            return false;
        }

        let func4 = `return (function func4(called) {
            called["func4"] = true;
            return false;
        })(event)`;

        let func5 = () => {
            called["func5"] = true;
            return false;
        };

        window.jsf.util.chain(this, called, func1, func2, func3, func4, func5);

        expect(called["func1"]).to.be.true;
        expect(called["func2"]).to.be.true;
        expect(!!called["func3"]).to.be.true;
        expect(!!called["func4"]).to.be.false;
        expect(!!called["func5"]).to.be.false;

        called = {};
        window.jsf.util.chain(this, called, func1, func2, func4, func5);
        expect(called["func1"]).to.be.true;
        expect(called["func2"]).to.be.true;
        expect(!!called["func4"]).to.be.true;
        expect(!!called["func5"]).to.be.false;

    });




});


