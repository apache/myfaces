/* Licensed to the Apache Software Foundation (ASF) under one or more
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
import {expect} from "chai";
import {ArrayCollector, LazyStream, Stream} from "mona-dish";

describe('early stream tests', () => {

    beforeEach(function () {
        this.probe = [1, 2, 3, 4, 5];
    });

    it("must iterate normal", function () {
        let stream = Stream.of<number>(...this.probe);
        let sum = 0;
        stream.each((data) => {
            sum = sum + data;
        });
        expect(sum).to.eq(15);

        let stream2 = LazyStream.of<number>(...this.probe);
        sum = 0;
        stream2.each((data: number) => {
            sum = sum + data;
        });
        expect(sum).to.eq(15);
    });

    it("must iterate filtered", function () {
        let stream = Stream.of<number>(...this.probe);
        let sum = 0;
        stream.filter((data) => data != 5).each((data) => {
            sum = sum + data;
        });
        expect(sum).to.eq(10);

        let stream2 = LazyStream.of<number>(...this.probe);
        sum = 0;
        stream2.filter((data) => data != 5).each((data: number) => {
            sum = sum + data;
        });
        expect(sum).to.eq(10);
    });

    it("must onElem", function () {
        let stream = Stream.of<number>(...this.probe);
        let sum = 0;
        let sum2: number = stream.filter((data) => data != 5).onElem((data) => {
            sum = sum + data;
        }).reduce<number>((el1, el2) => el1 + el2).value;
        expect(sum).to.eq(10);
        expect(sum2).to.eq(10);

        let stream2 = LazyStream.of<number>(...this.probe);
        sum = 0;
        sum2 = stream2.filter((data) => data != 5).onElem((data: number) => {
            sum = sum + data;
        }).reduce((el1: number, el2: number) => el1 + el2).value;
        expect(sum).to.eq(10);
        expect(sum2).to.eq(10);
    })

    it("must have a correct first last", function () {
        let first = Stream.of<number>(...this.probe).filter((data) => data != 5).onElem(() => {
        }).first().value;
        let last = Stream.of<number>(...this.probe).filter((data) => data != 5).onElem(() => {
        }).last().value;
        expect(first).to.eq(1);
        expect(last).to.eq(4);

    });

    it("must have a correct first last lazy", function () {
        let first = LazyStream.of<number>(...this.probe).filter((data) => data != 5).onElem((data) => {
            data;
        }).first().value;
        let last = Stream.of<number>(...this.probe).filter((data) => data != 5).onElem((data) => {
            data;
        }).last().value;
        expect(first).to.eq(1);
        expect(last).to.eq(4);

    });

    it("must have a correct limits", function () {
        let cnt = 0;
        let last = Stream.of<number>(...this.probe).filter((data) => data != 5).limits(2).onElem(() => {
            cnt++;
        }).last().value;

        expect(last).to.eq(2);
        expect(cnt).to.eq(2);

    });

    it("must initialize correctly from assoc array", function () {
        let probe = {
            key1: "val1",
            key2: 2,
            key3: "val3"
        }

        let arr1: Array<string | number> = [];
        let arr2: Array<string | number> = [];

        Stream.ofAssoc(probe).each(item => {
            expect(item.length).to.eq(2);
            arr1.push(item[0]);
            arr2.push(item[1]);
        });

        expect(arr1.join(",")).to.eq("key1,key2,key3");
        expect(arr2.join(",")).to.eq("val1,2,val3");

    });

    it("must have a correct lazy limits", function () {
        let last = LazyStream.of<number>(...this.probe).filter((data) => data != 5).limits(2).onElem((data) => {
            data;
        }).last().value;

        expect(last).to.eq(2);

    })

    it("must correctly lazily flatmap", function () {

        let resultingArr = LazyStream.of<number>(...this.probe).flatMap((data) => LazyStream.of(...[data, 2])).value;

        expect(resultingArr.length == 10).to.be.true;
        expect(resultingArr.join(",")).to.eq("1,2,2,2,3,2,4,2,5,2");
    });

    it("must correctly lazily flatmap with arrays", function () {

        let resultingArr = LazyStream.of<number>(...this.probe).flatMap((data) => [data, 2]).value;

        expect(resultingArr.length == 10).to.be.true;
        expect(resultingArr.join(",")).to.eq("1,2,2,2,3,2,4,2,5,2");
    });

    it("must correctly early flatmap", function () {

        let resultingArr = Stream.of<number>(...this.probe).flatMap((data) => Stream.of(...[data, 2])).value;

        expect(resultingArr.length == 10).to.be.true;
        expect(resultingArr.join(",")).to.eq("1,2,2,2,3,2,4,2,5,2");
    });

    it("must correctly early flatmap with arrays", function () {

        let resultingArr = Stream.of<number>(...this.probe).flatMap((data) => [data, 2]).value;

        expect(resultingArr.length == 10).to.be.true;
        expect(resultingArr.join(",")).to.eq("1,2,2,2,3,2,4,2,5,2");
    });

    it("must correctly flatmap intermixed", function () {

        let resultingArr = LazyStream.of<number>(...this.probe).flatMap((data) => Stream.of(...[data, 2])).value;

        expect(resultingArr.length == 10).to.be.true;
        expect(resultingArr.join(",")).to.eq("1,2,2,2,3,2,4,2,5,2");
    });

    it("must correctly flatmap intermixed2", function () {

        let resultingArr = Stream.of<number>(...this.probe).flatMap((data) => LazyStream.of(...[data, 2])).value;

        expect(resultingArr.length == 10).to.be.true;
        expect(resultingArr.join(",")).to.eq("1,2,2,2,3,2,4,2,5,2");
    });

    it("must correctly pass anyMatch allMatch noneMatch", function () {
        let anyMatch = LazyStream.of<number>(...this.probe).anyMatch((item) => item == 3);
        let allMatch = LazyStream.of<number>(...this.probe).allMatch((item) => item < 6);
        let noneMatch = LazyStream.of<number>(...this.probe).noneMatch((item) => item > 5);

        expect(anyMatch).to.be.true;
        expect(allMatch).to.be.true;
        expect(noneMatch).to.be.true;
    })

    it("must correctly pass anyMatch allMatch noneMatch early", function () {
        let anyMatch = Stream.of<number>(...this.probe).anyMatch((item) => item == 3);
        let allMatch = Stream.of<number>(...this.probe).allMatch((item) => item < 6);
        let noneMatch = Stream.of<number>(...this.probe).noneMatch((item) => item > 5);

        expect(anyMatch).to.be.true;
        expect(allMatch).to.be.true;
        expect(noneMatch).to.be.true;
    })

    it("must sort correctly", function () {

        let probe: Array<number> = [1, 5, 3, 2, 4];

        let res = Stream.of<number>(...probe)
            .sort((el1: number, el2: number) => el1 - el2)
            .collect(new ArrayCollector());

        expect(res.join(",")).to.eq("1,2,3,4,5");

    })

    it("must sort correctly lazy", function () {

        let probe: Array<number> = [1, 5, 3, 2, 4];

        let res = LazyStream.of<number>(...probe)
            .sort((el1: number, el2: number) => el1 - el2)
            .collect(new ArrayCollector());

        expect(res.join(",")).to.eq("1,2,3,4,5");

    })
});