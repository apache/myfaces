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

import { expect } from 'chai';
import { describe, it } from 'mocha';
import {Lang} from "mona-dish";


const jsdom = require("jsdom");
const { JSDOM } = jsdom;
const dom = new JSDOM(`
    <!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
    </head>
    <body>
        <div />
        <div />
        <div />
        <div />
    </body>
    </html>
    
    `)




export const window = dom.window;

class Probe {

    constructor() {
    }

    val1 = 1;
    val2 = 2;
    val3 = 3;
}

describe('Lang tests', () => {


    it('initializable', () => {
        const lang = Lang;
        expect(lang).to.exist;
    });

    it('strToArray working', () => {
        const lang = Lang;

        let arr = lang.strToArray("hello.world.from.me", /\./gi);

        expect(arr).to.exist;
        expect(arr.length).to.eq(4);
        expect(arr[3]).to.eq("me");

    });



    it('trim working', () => {
        const lang = Lang;
        let origStr = " hello world from me    ";
        let trimmed = lang.trim(origStr);
        expect(trimmed).to.exist;
        expect(trimmed).to.eq("hello world from me");

    });

    it('isString working', () => {
        const lang = Lang;
        expect(lang.isString(" ")).to.be.true;
        expect(lang.isString('')).to.be.true;
        expect(lang.isString(null)).to.be.false;
        expect(lang.isString(undefined)).to.be.false;
        expect(lang.isString(function() {return true;})).to.be.false;
        expect(lang.isString(new Probe())).to.be.false;
    });

    it('isFunc working', () => {
        const lang = Lang;
        expect(lang.isFunc(() => {})).to.be.true;
        expect(lang.isFunc(function() {return true;})).to.be.true;
        expect(lang.isFunc("blarg")).to.be.false;
        expect(lang.isFunc(new Probe())).to.be.false;
    });

    it('objToArray working', () => {
        const lang = Lang;
        let obj_probe = new Probe();
        let resultArr = lang.objToArray(obj_probe);
        expect(lang.assertType(resultArr, Array)).to.be.true;
        expect(resultArr.length).to.eq(0);
        obj_probe = window.document.body.querySelectorAll("div");
        resultArr = lang.objToArray(obj_probe);
        expect(resultArr.length).to.eq(4);
        expect(lang.assertType(resultArr, Array)).to.be.true;
    });



    it('equals ignore case test', () => {
        const lang = Lang;
        expect(lang.equalsIgnoreCase(<any>null, <any>null)).to.be.true;
        expect(lang.equalsIgnoreCase("", "")).to.be.true;
        expect(lang.equalsIgnoreCase("null", "NuLL")).to.be.true;
        expect(lang.equalsIgnoreCase("null ", "NuLL")).to.be.false;
        expect(lang.equalsIgnoreCase("null", "NuLL2")).to.be.false;

    });

});
