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
import {expect} from 'chai';
import * as sinon from 'sinon';
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import defaultMyFaces = StandardInits.defaultMyFaces;
import defaultSeparatorChar = StandardInits.defaultSeparatorChar;

sinon.reset();

declare var faces: any;
declare var Implementation: any;

describe('various tests for get separator char', () => {

    it("should have : as standard separator char", (done) => {

        let waitForResult = defaultMyFaces();
        waitForResult.then((close) => {
            try {
                let separator = faces.separatorchar;
                expect(separator).to.eq(":");
            } finally {
                Implementation.reset();
                close();
                done();
            }
        })
    });

    it("should have a custom separator char", (done) => {

        let waitForResult = defaultSeparatorChar("$", false);
        waitForResult.then((close) => {
            try {
                let separator = Implementation.getSeparatorChar();
                expect(separator).to.eq("$");
                done();
            } catch(ex) {
                console.error(ex);
                expect(false).to.eq(true);
            } finally {
                Implementation.reset();
                close();

            }
        });

    });

    it("should have a url encoded separator char", (done) => {

        let waitForResult = defaultSeparatorChar("%21");
        waitForResult.then((close) => {
            try {
                let separator = Implementation.getSeparatorChar();
                expect(separator).to.eq("!");
                done();
            } finally {
                Implementation.reset();
                close();

            }
        });

    });
});