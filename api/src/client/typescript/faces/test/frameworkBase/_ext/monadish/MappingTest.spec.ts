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

import {Probe1, Probe1Impl, Probe2, Probe2Impl} from "./MappingProbes";
import { expect } from 'chai';
import { describe, it } from 'mocha';

describe('mapping tests', () => {
    it('must map correctly', () => {
        let probe2: Probe2 = {val1: "hello from probe2"};

        let probe1: Probe1 = {
            val1: "hello from probe1",
            val2: new Date(),
            val3: {"hello": probe2},
            val4: [probe2, probe2],
            val5: probe2,
            val6: "something",
        };


        let probe1Impl = new Probe1Impl(probe1);



        expect(probe1Impl.val1).to.be.eq(probe1.val1);
        expect(probe1Impl.val4[1] instanceof Probe2Impl).to.be.eq(true);
        expect(probe1Impl.val5 instanceof Probe2Impl).to.be.eq(true);
        expect(probe1Impl.val3["hello"] instanceof Probe2Impl).to.be.eq(true);
    });


});