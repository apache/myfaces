/*!
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

/**
 * various helpers to deal with associative
 * arrays. If someone feels uncomfortable using
 * The config system, this is similar!
 */
import {IValueHolder} from "./Monad";
import {Es2019Array} from "./Es2019Array";

/**
 * A nop as assign functionality (aka ignore assign)
 */
class IgnoreAssign implements IValueHolder<any>  {
    constructor(private parent: any) {}

    set value(value: any | Array<any>) {
    }
    get value(): any | Array<any> {
        return this.parent;
    }
};

/**
 * uses the known pattern from config
 * assign(target, key1, key2, key3).value = value;
 * @param target
 * @param keys
 */
export function assign<T>(target: {[key: string]: any}, ...accessPath: string[]): IValueHolder<T> {
    if (accessPath.length < 1) {
        return new IgnoreAssign(target);
    }
    const lastPathItem = buildPath(target, ...accessPath);
    let assigner: IValueHolder<T> = new (class {
        set value(value: T | Array<T>) {
            lastPathItem.target[lastPathItem.key] = value;
        }

        get value(): T | Array<T> {
            return lastPathItem.target[lastPathItem.key];
        }
    })();
    return assigner;
}


export function append<T>(target: {[key: string]: any}, ...accessPath: string[]): IValueHolder<T> {
    if (accessPath.length < 1) {
        return new IgnoreAssign(target);
    }
    const lastPathItem = buildPath(target, ...accessPath);
    let appender: IValueHolder<T> = new (class {
        set value(value: T | Array<T>) {
            if(!Array.isArray(value)) {
                value = [value];
            }
            if(!lastPathItem.target[lastPathItem.key]) {
                lastPathItem.target[lastPathItem.key] = value
            } else {
                if(!Array.isArray(lastPathItem.target[lastPathItem.key])) {
                    lastPathItem.target[lastPathItem.key] = [lastPathItem.target[lastPathItem.key]];
                }
                lastPathItem.target[lastPathItem.key].push(...value);
            }
        }
    })();
    return appender;
}

/**
 * uses the known pattern from config
 * assign(target, key1, key2, key3).value = value;
 * @param target
 * @param keys
 */
export function assignIf<T>(condition: boolean, target: {[key: string]: any}, ...accessPath: string[]): IValueHolder<T> {
    if ((!condition) || accessPath.length < 1) {
        return new IgnoreAssign(target);
    }
    return assign(target, ...accessPath);
}


/**
 * uses the known pattern from config
 * assign(target, key1, key2, key3).value = value;
 * @param target
 * @param keys
 */
export function appendIf<T>(condition: boolean, target: {[key: string]: any}, ...accessPath: string[]): IValueHolder<T> {
    if ((!condition) || accessPath.length < 1) {
        return new IgnoreAssign(target);
    }
    return append(target, ...accessPath);
}

export function resolve<T>(target, ...accessPath: string[]): T | null {
    let ret = null;
    accessPath = flattenAccessPath(accessPath);
    let currPtr = target;
    for(let cnt = 0; cnt < accessPath.length; cnt++) {
        let accessKeyIndex: number | string = accessPath[cnt];
        accessKeyIndex = arrayIndex(accessKeyIndex) != -1 ? arrayIndex(accessKeyIndex) : accessKeyIndex;
        currPtr = currPtr?.[accessKeyIndex];
        if('undefined' == typeof currPtr) {
            return null;
        }
        ret = currPtr;
    }
    return currPtr;
}


function keyVal(key: string): string {
    let start = key.indexOf("[");

    if (start >= 0) {
        return key.substring(0, start);
    } else {
        return key;
    }
}

function arrayIndex(key: string): number {
    let start = key.indexOf("[");
    let end = key.indexOf("]");
    if (start >= 0 && end > 0 && start < end) {
        return parseInt(key.substring(start + 1, end));
    } else {
        return -1;
    }
}

function isArrayPos(currKey: string, arrPos: number): boolean {
    return currKey === "" && arrPos >= 0;
}

function isNoArray(arrPos: number): boolean {
    return arrPos == -1;
}

function alloc(arr: Array<any>, length: number, defaultVal = {}) {
    let toAdd = [];
    toAdd.length = length;
    toAdd[length - 1] = defaultVal;
    arr.push(...toAdd);
}


function flattenAccessPath(accessPath: string[]) {
    return new Es2019Array(...accessPath).flatMap(path => path.split("["))
        .map(path => path.indexOf("]") != -1 ? "[" + path : path)
        .filter(path => path != "");
}

/**
 * builds up a path, only done if no data is present!
 * @param target
 * @param accessPath
 * @returns the last assignable entry
 */
export function buildPath(target, ...accessPath: string[]) {
    accessPath = flattenAccessPath(accessPath);
    //we now have a pattern of having the array accessors always in separate items
    let parentPtr = target;
    let parKeyArrPos = null;
    let currKey = null;
    let arrPos = -1;

    for (let cnt = 0; cnt < accessPath.length; cnt++) {
        currKey = keyVal(accessPath[cnt]);
        arrPos = arrayIndex(accessPath[cnt]);
        //it now is either key or arrPos
        if (arrPos != -1) {
            //case root(array)[5] -> root must be array and allocate 5 elements
            //case root.item[5] root.item must be array and of 5 elements
            if(!Array.isArray(parentPtr)) {
                throw Error("Associative array referenced as index array in path reference");
            }

            //we need to look ahead for proper allocation
            //not end reached
            let nextArrPos = -1;
            if(cnt < accessPath.length - 1) {
                nextArrPos = arrayIndex(accessPath[cnt + 1])
            }
            let dataPresent = 'undefined' != typeof parentPtr?.[arrPos];
            //no data present check here is needed, because alloc only reserves if not present
            alloc(parentPtr, arrPos + 1, nextArrPos != -1 ?[]: {});
            parKeyArrPos = arrPos;
            //we now go to the reserved element
            if(cnt == accessPath.length - 1) {
                parentPtr[arrPos] = (dataPresent) ? parentPtr[arrPos] : null;
            } else {
                parentPtr = parentPtr[arrPos];
            }
        } else {
            if(Array.isArray(parentPtr)) {
                throw Error("Index array referenced as associative array in path reference");
            }
            //again look ahead whether the next value is an array or assoc array
            let nextArrPos = -1;
            if(cnt < accessPath.length - 1) {
                nextArrPos = arrayIndex(accessPath[cnt + 1])
            }
            parKeyArrPos = currKey;
            let dataPresent = 'undefined' != typeof parentPtr?.[currKey];
            if(cnt == accessPath.length - 1) {
                if(!dataPresent) {
                    parentPtr[currKey] = null;
                }
            } else {
                if(!dataPresent) {
                    parentPtr[currKey] = nextArrPos == -1 ? {} : [];
                }
                parentPtr = parentPtr[currKey];
            }
        }
    }

    return {target: parentPtr, key: parKeyArrPos};

}

export function deepCopy(fromAssoc: {[key: string]: any}): {[key: string]: any} {
    return JSON.parse(JSON.stringify(fromAssoc));
}

/**
 * simple left to right merge
 *
 * @param assocArrays
 */
export function simpleShallowMerge(...assocArrays) {
   return shallowMerge(true, false, ...assocArrays);
}

/**
 * Shallow merge as in config, but on raw associative arrays
 *
 * @param overwrite
 * @param withAppend
 * @param assocArrays
 */
export function shallowMerge(overwrite = true, withAppend = false, ...assocArrays) {
    let target: {[key: string]: any} = {};
    new Es2019Array(...assocArrays).map(arr => {
        return {arr, keys: Object.keys(arr)};
    }).forEach(({arr, keys}) => {
        keys.forEach(key => {
            let toAssign = arr[key];
            if(!Array.isArray(toAssign) && withAppend) {
                toAssign = new Es2019Array(...[toAssign]);
            }
            if(overwrite || !target?.[key]) {
                if(!withAppend) {
                    target[key] = arr[key];
                } else {
                        if('undefined' == typeof target?.[key]) {
                            target[key] = toAssign
                        } else if(!Array.isArray(target[key])) {
                            let oldVal = target[key];
                            target[key] = new Es2019Array(...[]);
                            target[key].push(oldVal);
                            target[key].push(...toAssign);
                        } else {
                            target[key].push(...toAssign);
                        }
                }
            }
        })
    });
    return target;
}

