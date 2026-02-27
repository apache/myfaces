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
import pkg from 'mona-dish';
const { Config, Optional } = pkg;

describe('optional tests', () => {
    it('fromnullable null', () => {
        expect(Optional.fromNullable(null).isPresent()).to.be.false;
        expect(Optional.fromNullable(null).isAbsent()).to.be.true;
    });
    it('fromnullable absent', () => {
        expect(Optional.fromNullable(Optional.absent).isPresent()).to.be.false;
    });
    it('fromnullable value', () => {
        expect(Optional.fromNullable(1).isPresent()).to.be.true;
        expect(Optional.fromNullable(1).isAbsent()).to.be.false;
    });

    it('flatmap/map test', () => {
        expect(Optional.fromNullable(Optional.fromNullable(1)).value).to.be.eq(1 as any);
        expect(Optional.fromNullable(Optional.fromNullable(1)).value).to.be.eq(1 as any);
    });

    it('flatmap2/map test', () => {
        expect(Optional.fromNullable(Optional.fromNullable(null)).isAbsent()).to.be.true;
        expect(Optional.fromNullable(Optional.fromNullable()).isAbsent()).to.be.true;
    });

    it('elvis test', () => {
        var myStruct = {
            data: {
                value: 1,
                value2: Optional.absent,
                value4: Optional.fromNullable(1)
            },
            data2: [
                {booga: "hello"},
                "hello2"
            ]
        };
        expect(Optional.fromNullable(myStruct).getIf("data", "value").value).to.be.eq(1);
        expect(Optional.fromNullable(Optional.fromNullable(myStruct)).getIf("data", "value2").isAbsent()).to.be.true;
        expect(Optional.fromNullable(myStruct).getIf("data", "value3").isAbsent()).to.be.true;
        expect(Optional.fromNullable(myStruct).getIf("data", "value4").value).to.be.eq(1);
        expect(Optional.fromNullable(Optional.fromNullable(myStruct)).getIf("data2[0]", "booga").isPresent()).to.be.true;
        expect(Optional.fromNullable(Optional.fromNullable(myStruct)).getIf("data2[1]").isPresent()).to.be.true;
        expect(Optional.fromNullable(Optional.fromNullable(myStruct)).getIf("data2").isPresent()).to.be.true;
        expect(Optional.fromNullable(Optional.fromNullable(myStruct)).getIf("data2", "hello2").isPresent()).false;
        expect(Optional.fromNullable(Optional.fromNullable(Optional.fromNullable(myStruct))).getIf("data2[0]", "booga").value).to.be.eq("hello");

    });
});


describe('Config tests', () => {
    var setup = function ():any  {
        return new Config({
            data: {
                value: 1,
                value2: Optional.absent,
                value3: null
            },
            data2: [
                {booga: "hello"},
                "hello2"
            ]
        });
    };

    function structure(myStruct: any) {
        expect(Optional.fromNullable(myStruct).getIf("data", "value").value).to.be.eq(1);
        expect(Optional.fromNullable(Optional.fromNullable(myStruct)).getIf("data", "value2").isAbsent()).to.be.true;
        expect(Optional.fromNullable(myStruct).getIf("data", "value3").isAbsent()).to.be.true;
        expect(Optional.fromNullable(myStruct).getIf("data", "value4").isAbsent()).to.be.true;
        expect(Optional.fromNullable(Optional.fromNullable(myStruct)).getIf("data2[0]", "booga").isPresent()).to.be.true;
        expect(Optional.fromNullable(Optional.fromNullable(myStruct)).getIf("data2[1]").isPresent()).to.be.true;
        expect(Optional.fromNullable(Optional.fromNullable(myStruct)).getIf("data2").isPresent()).to.be.true;
        expect(Optional.fromNullable(Optional.fromNullable(myStruct)).getIf("data2", "hello2").isPresent()).false;
        expect(Optional.fromNullable(Optional.fromNullable(myStruct)).getIf("data2[0]", "booga").value).to.be.eq("hello");
    }

    function structureBroken(myStruct: any) {
        let valx = Optional.fromNullable(myStruct).getIf("data", "value").value;
        expect(!!Optional.fromNullable(myStruct).getIf("data", "value").value).to.be.false;
    }

    it('simple config', () => {
        let config = setup();
        config.assign("hello", "world", "from").value = "me";
        expect(Config.fromNullable(config.getIf("hello", "world", "from")).value).to.be.eq("me");
        expect(config.getIf("hello", "booga", "from").isAbsent()).to.be.eq(true);
        structure(config.value);

    });

    it('simple config2', () => {
        let config = setup();
        config.assign("hello", "world", "from").value = "me";
        expect(config.value.hello.world.from).to.be.eq("me");
        structure(config.value);
    });

    it('array config', () => {
        let config = setup();
        config.assign("hello[5]", "world[3]", "from[5]").value = "me";
        expect(config.getIf("hello[5]", "world[3]", "from[5]").value).to.be.eq("me");
        expect(config.value.hello[5].world[3].from[5]).to.be.eq("me");
        structure(config.value);
    });

    it('array config2', () => {
        let config = new Config([]);
        config.assign("[5]", "world[3]", "from").value = "me";
        expect(config.getIf("[5]", "world[3]", "from").value).to.be.eq("me");
        expect(config.value[5].world[3].from).to.be.eq("me");
        structureBroken(config.value);
    });

    it('array config3', () => {
        let config = new Config([]);
        config.assign("[5]", "[3]", "from").value = "me";
        expect(config.getIf("[5]", "[3]", "from").value).to.be.eq("me");
        expect(config.value[5][3].from).to.be.eq("me");
        structureBroken(config.value);
    });

    it('array config4', () => {
        let config = new Config([]);
        config.assign("[5]", "[3]", "[2]").value = "me";
        expect(config.getIf("[5]", "[3]", "[2]").value).to.be.eq("me");
        expect(config.value[5][3][2]).to.be.eq("me");
        structureBroken(config.value);
    });

    it('array config5', () => {
        let config = new Config([]);
        config.assign("[5]", "world[3]", "from[2]").value = "me";
        expect(config.getIf("[5]", "world[3]", "from[2]").value).to.be.eq("me");
        expect(config.value[5].world[3].from[2]).to.be.eq("me");
        structureBroken(config.value);
    });

    it('resolve test', () => {
        let probe = new Config({});
        probe.assign("test1","test2","test3").value = "hello";

        expect(probe.resolve((root) => root.test1.test2.test3).value).to.eq("hello");
        expect(probe.resolve((root) => root.test1.test2.testborked).isAbsent()).to.be.true;
        expect(probe.resolve((root) => root.test1.testborked.test3).isAbsent()).to.be.true;
    });

});


