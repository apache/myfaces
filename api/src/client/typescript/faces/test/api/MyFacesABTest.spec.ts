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

import {describe} from "mocha";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
const defaultMyFaces = StandardInits.defaultMyFaces;
import {expect} from "chai";

describe('API tests', () => {

    beforeEach(() => {
        return defaultMyFaces();
    });

    it("must pass into ajax.request properly with user parameters", () => {
        let passedSource = null;
        let passedEvent = null;
        let passedOptions = null;
        let oldRequest = (window?.faces ?? window.jsf).ajax.request;
        try {
            (window?.faces ?? window.jsf).ajax.request = function(source, event, options) {
                passedSource = source;
                passedEvent = event;
                passedOptions = options;
            }
            myfaces.ab(null, null, null, null, null, {}, {booga: "foobaz"});

            expect(passedSource).to.eq(null);
            expect(passedEvent).to.eq(null);
            expect(passedOptions).to.deep.eq({params:{booga: "foobaz"}});
        } finally {
            (window?.faces ?? window.jsf).ajax.request = oldRequest;
        }
    })

    it("must pass into ajax.request properly without user parameters", () => {
        let passedSource = null;
        let passedEvent = null;
        let passedOptions = null;
        let oldRequest = (window?.faces ?? window.jsf).ajax.request;
        try {
            (window?.faces ?? window.jsf).ajax.request = function(source, event, options) {
                passedSource = source;
                passedEvent = event;
                passedOptions = options;
            }
            myfaces.ab(null, null, null, null, null, {});

            expect(passedSource).to.eq(null);
            expect(passedEvent).to.eq(null);
            expect(passedOptions).to.deep.eq({params:{}});
        } finally {
            (window?.faces ?? window.jsf).ajax.request = oldRequest;
        }
    });

    it("must pass into ajax.request properly with null options", () => {
        let passedSource = null;
        let passedEvent = null;
        let passedOptions = null;
        let oldRequest = (window?.faces ?? window.jsf).ajax.request;
        try {
            (window?.faces ?? window.jsf).ajax.request = function(source, event, options) {
                passedSource = source;
                passedEvent = event;
                passedOptions = options;
            }
            myfaces.ab(null, null, null, null, null, null);

            expect(passedSource).to.eq(null);
            expect(passedEvent).to.eq(null);
            expect(passedOptions).to.deep.eq({params:{}});
        } finally {
            (window?.faces ?? window.jsf).ajax.request = oldRequest;
        }
    });

    it("must pass into ajax.request properly with null options and null user options", () => {
        let passedSource = null;
        let passedEvent = null;
        let passedOptions = null;
        let oldRequest = (window?.faces ?? window.jsf).ajax.request;
        try {
            (window?.faces ?? window.jsf).ajax.request = function(source, event, options) {
                passedSource = source;
                passedEvent = event;
                passedOptions = options;
            }
            myfaces.ab(null, null, null, null, null, null, null);

            expect(passedSource).to.eq(null);
            expect(passedEvent).to.eq(null);
            expect(passedOptions).to.deep.eq({params:{}});
        } finally {
            (window?.faces ?? window.jsf).ajax.request = oldRequest;
        }
    });

    it("must pass into ajax.request properly without options and user params", () => {
        let passedSource = null;
        let passedEvent = null;
        let passedOptions = null;
        let oldRequest = (window?.faces ?? window.jsf).ajax.request;
        try {
            (window?.faces ?? window.jsf).ajax.request = function(source, event, options) {
                passedSource = source;
                passedEvent = event;
                passedOptions = options;
            }
            myfaces.ab(null, null, null, null, null);

            expect(passedSource).to.eq(null);
            expect(passedEvent).to.eq(null);
            expect(passedOptions).to.deep.eq({params:{}});
        } finally {
            (window?.faces ?? window.jsf).ajax.request = oldRequest;
        }
    });

});