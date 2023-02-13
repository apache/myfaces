/*!
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * Array with a set of shim functions for older browsers
 * we do not extend prototype (rule #1)
 *
 * This is a helper which for now adds the missing flatMap, without prototype pollution
 *
 * that way we can avoid streams wherever we just want to go pure JS
 * This class is self isolated, so it suffices to just dump it into a project one way or the other
 * without anything else
 */
export class ExtendedArray<T> extends Array<T> {

    constructor(...items: T[]) {
        super(...items);
        //es5 base class see //fix for es5 deficit from https://github.com/Microsoft/TypeScript/issues/13720

        //for testing it definitely runs into this branch because we are on es5 level
        if(!(<any>Array.prototype).flatMap) {
            let flatmapFun = (<any>ExtendedArray).prototype.flatMap;
            //unfortunately in es5 the flaptmap function is lost due to inheritance of a primitive
            //es  class, we have to remap it back in
            this.flatMap = flatmapFun;
        }
    }

    flatMap(mapperFunction: Function, noFallback: boolean = false): ExtendedArray<T> {

        let res = [];

        let remap = item => {
            let opRes = mapperFunction(item);
            if(Array.isArray(opRes)) {
                if(opRes.length == 1) {
                    res.push(opRes[1])
                    return;
                }
                if(opRes.length > 1) {
                    opRes.forEach(newItem => remap(newItem))
                }
            } else {
                res.push(item);
            }
        };
        this.forEach( item => remap(item) )

        return new ExtendedArray(...res);
    }
}