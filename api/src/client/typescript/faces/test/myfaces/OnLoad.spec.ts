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
const defaultMyFaces = StandardInits.defaultMyFaces;



declare var myfaces: any;

/**
 * Adds test s for the newly introduced onload handling
 */
describe('Tests on the xhr core when it starts to call the request', function () {

    beforeEach(async () => {
        return
    });

    it("must be present", async function()  {
        await defaultMyFaces();
        expect(myfaces?.onDomReady).to.exist;
        return true;
    });

    it("must be called on onDocumentReady", async function(done) {
        let called: boolean = false;
        const onDomCalled = () => {
            expect(true).to.true;
            done();
        }
        myfaces?.onDomReady(onDomCalled);
        await defaultMyFaces();
    });

});
