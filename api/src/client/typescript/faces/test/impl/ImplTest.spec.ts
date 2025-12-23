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
import {afterEach, describe, it} from 'mocha';
import {expect} from 'chai';
import * as sinon from 'sinon';

import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import {CTX_PARAM_REQ_PASS_THR, P_EXECUTE, P_RENDER} from "../../impl/core/Const";
const defaultMyFaces = StandardInits.defaultMyFaces;
import {_Es2019Array} from "mona-dish";


sinon.reset();

declare var faces: any;
declare var Implementation: any;

/**
 * testing the faces.ajax.request api without triggering any
 * xhr request...
 * the idea is to shim the code which triggers the request out and check what is going in
 * and what is coming out
 */

describe('faces.ajax.request test suite', () => {
    let oldFlatMap = null;
    beforeEach(async () => {
        //we test ES2019 with it and whether we have missed
        //a map somewhere
        return await defaultMyFaces().then( () => {
            if(Array.prototype.map) {
                oldFlatMap =Array.prototype["flatMap"];
                window["Es2019Array"] = _Es2019Array;
                delete Array.prototype["flatMap"];
            }
        });
    });

    afterEach(() => {
        if(oldFlatMap) {
            Array.prototype["flatMap"] = oldFlatMap;
            oldFlatMap = null;
        }
    })

    it("faces.ajax.request can be called", () => {
        //we stub the addRequestToQueue, to enable the request check only
        //without any xhr and response, both will be tested separately for
        //proper behavior
        const addRequestToQueue = sinon.stub(Implementation.queueHandler, "addRequestToQueue");
        //now the faces.ajax.request should trigger but should not go into
        //the asynchronous event loop.
        //lets check it out
        let flatMap = Array.prototype.flatMap;

        try {
            DomQuery.byId("input_2").addEventListener("click", (event: Event) => {

                faces.ajax.request(null, event, {render: '@all', execute: '@form'});
            }).click();


            expect(addRequestToQueue.called).to.be.true;
            expect(addRequestToQueue.callCount).to.eq(1);
            const context = (addRequestToQueue.args[0][2] as Config);

            expect(context.getIf(CTX_PARAM_REQ_PASS_THR, P_RENDER).value).eq("@all");
            //Execute issuing form due to @form and always the issuing element
            expect(context.getIf(CTX_PARAM_REQ_PASS_THR, P_EXECUTE).value).eq("blarg input_2");
        } finally {
            //once done we restore the proper state
            addRequestToQueue.restore();
        }

    });

    it("faces.ajax.request passthroughs must end up in passthrough", (done) => {
        //TODO implementation
        done();
    });

    it("faces.util.chain must work", () => {
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
        delete Array.prototype["flatMap"];
        faces.util.chain(this, called, func1, func2, func3, func4, func5);

        expect(called["func1"]).to.be.true;
        expect(called["func2"]).to.be.true;
        expect(!!called["func3"]).to.be.true;
        expect(!!called["func4"]).to.be.false;
        expect(!!called["func5"]).to.be.false;

        called = {};
        faces.util.chain(this, called, func1, func2, func4, func5);
        expect(called["func1"]).to.be.true;
        expect(called["func2"]).to.be.true;
        expect(!!called["func4"]).to.be.true;
        expect(!!called["func5"]).to.be.false;
    });


    it("chain must handle the true false return values correctly", function () {
        let func1 = () => {
            return true;
        }
        let func2 = () => {
            return false;
        }

        let func3 = () => {
            return undefined;
        }

        let func4 = () => {
            return null;
        }

        let ret =  faces.util.chain(this, {}, func1);
        expect(ret).to.be.true;

        ret =  faces.util.chain(this, {}, func2);
        expect(ret).to.be.false;

        ret =  faces.util.chain(this, {}, func3);
        expect(ret).to.be.true;

        ret =  faces.util.chain(this, {}, func4);
        expect(ret).to.be.true;

    })

    it("sidebehavior chain on undefined must not break the chain only a dedicated false does", function() {
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
            return null;
        }

        let func4 = `return (function func4(called) {
            called["func4"] = true;
            return undefined;
        })(event)`;

        let func5 = `return (function func4(called) {
            called["func5"] = true;
            return false;
        })(event)`;



        let func6 = () => {
            called["func6"] = true;
            return false;
        };
        delete Array.prototype["flatMap"];
        faces.util.chain(this, called, func1, func2, func3, func4, func5, func6);

        expect(called["func1"]).to.be.true;
        expect(called["func2"]).to.be.true;
        expect(!!called["func3"]).to.be.true;
        expect(!!called["func4"]).to.be.true;
        expect(!!called["func5"]).to.be.true;
        expect(!!called["func6"]).to.be.false;

        /*called = {};
        faces.util.chain(this, called, func1, func2, func4, func5, func6);
        expect(called["func1"]).to.be.true;
        expect(called["func2"]).to.be.true;
        expect(!!called["func4"]).to.be.true;
        expect(!!called["func5"]).to.be.false;*/
    });





});


