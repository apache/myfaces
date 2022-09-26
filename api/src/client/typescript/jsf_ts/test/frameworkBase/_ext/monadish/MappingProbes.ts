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


export interface Probe1 {
    val1: string;
    val2: Date;
    val3: any;
    val4: Probe2[];
    val5: Probe2;
    val6: any;
}

export interface Probe2 {
    val1: string;
}

class ArrType {


    constructor(public clazz: any) {

    }
}


class DtoUils {

    static mapIt(target: any, src: any, mappings: any): any {
        for (let key in src) {
            if (!src.hasOwnProperty(key)) {
                continue;
            }

            let newVal = src[key];
            if (mappings[key]  &&
                mappings[key] instanceof ArrType) {
                //do the array here
                (<any>target)[key] = {};

                for (let key2 in newVal) {
                    (<any>target)[key][key2] = new mappings[key].clazz(newVal[key2]);
                }
            } else if (mappings && mappings[key]) {
                (<any>target)[key] = new mappings[key](newVal);
            } else {
                (<any>target)[key] = newVal
            }

        }

        return target;
    }

}

// noinspection JSUnusedLocalSymbols
class BaseDto<T> {

    TYPES = "___mappable_types___";

    constructor(data?: T, dtoTypes: any = {}) {

        (<any>this)[this.TYPES] = dtoTypes;

        if (data) {
            this.mapIt(this, data);
        }
    }


    mapIt(target: any, src: any): any {
        for (let key in src) {
            if (!src.hasOwnProperty(key)) {
                continue;
            }

            let newVal = src[key];
            if (target[this.TYPES] &&
                target[this.TYPES][key]  &&
                target[this.TYPES][key] instanceof ArrType) {
                //do the array here
                (<any>target)[key] = {};

                for (let key2 in newVal) {
                    //   subTarget = this.mapIt(subTarget, <any> newVal[key2]);
                    (<any>target)[key][key2] = new target[this.TYPES][key].clazz(newVal[key2]);
                }
            } else if (target[this.TYPES] && target[this.TYPES][key]) {
                (<any>target)[key] = new target[this.TYPES][key](newVal);
            } else {
                (<any>target)[key] = newVal
            }

        }

        return target;
    }

}

export class Probe2Impl implements Probe2 {

    val1: string;

    constructor(data: Probe2) {
        this.val1 = data.val1;
    }
}

function mixMaps(target: any, src: any): any {
    for(var key in src) {
        target[key] = src[key];
    }
    return target;
}

export class Probe1Impl  implements Probe1 {

    val1!: string;
    val2!: Date;
    val3!: any;
    val4!: Probe2[];
    val5!: Probe2;
    val6!: any;

    constructor(data: Probe1, mixin: any = {} /*put your own arguments in here*/) {
        DtoUils.mapIt(this, data, mixMaps({
            val3: new ArrType(Probe2Impl),
            val4: new ArrType(Probe2Impl),
            val5: Probe2Impl
        }, mixin))
    }

}