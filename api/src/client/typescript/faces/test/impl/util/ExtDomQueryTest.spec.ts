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

import {ExtDomQuery} from "../../../impl/util/ExtDomQuery";
import {StandardInits} from "../../frameworkBase/_ext/shared/StandardInits";
import defaultMyFaces = StandardInits.defaultMyFaces;
import Sinon from "sinon";

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
});
