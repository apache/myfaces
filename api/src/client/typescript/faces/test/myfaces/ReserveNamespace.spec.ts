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

describe('Tests for myfaces.reserveNamespace', function () {

    beforeEach(() => {
        return defaultMyFaces();
    });

    it('reserveNamespace must exist', function () {
        expect(myfaces?.reserveNamespace).to.exist;
        expect(typeof myfaces.reserveNamespace).to.eq('function');
    });

    it('must create a single-level namespace on window', function () {
        myfaces.reserveNamespace("testns1");
        expect((window as any).testns1).to.exist;
        expect(typeof (window as any).testns1).to.eq('object');
        delete (window as any).testns1;
    });

    it('must create a multi-level namespace on window', function () {
        myfaces.reserveNamespace("com.example.myapp");
        expect((window as any).com).to.exist;
        expect((window as any).com.example).to.exist;
        expect((window as any).com.example.myapp).to.exist;
        expect(typeof (window as any).com.example.myapp).to.eq('object');
        delete (window as any).com;
    });

    it('must not overwrite existing namespace levels', function () {
        (window as any).existing = {preserved: "value", sub: {kept: true}};
        myfaces.reserveNamespace("existing.sub.newlevel");
        expect((window as any).existing.preserved).to.eq("value");
        expect((window as any).existing.sub.kept).to.eq(true);
        expect((window as any).existing.sub.newlevel).to.exist;
        expect(typeof (window as any).existing.sub.newlevel).to.eq('object');
        delete (window as any).existing;
    });

    it('must handle deeply nested namespaces', function () {
        myfaces.reserveNamespace("a.b.c.d.e");
        expect((window as any).a.b.c.d.e).to.exist;
        expect(typeof (window as any).a.b.c.d.e).to.eq('object');
        delete (window as any).a;
    });
});
