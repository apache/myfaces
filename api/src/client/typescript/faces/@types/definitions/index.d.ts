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

import type { faces, myfaces } from '../../api/_api';

/**
 * Internal utility types used throughout the implementation.
 * Public API types live in _api.ts; these are build-only helpers.
 */
declare global {

    type Consumer<T> = (s?: T) => void;
    type Tuple<V,K> = [V, K];
    type Tuples<V,K> = [Tuple<V, K>];
    type AssocArr<T> = { [key: string]: T };
    type EvalFuncs = Array<Function | string>;

    type Options = {
        render ?: string,
        execute ?: string,
        onevent ?: Function,
        onerror ?: Function,
        params ?: AssocArr<any>,
        delay ?: number,
        resetValues ?: boolean,
        [key: string]: any
    }

    type Context = AssocArr<any>;
    type ElemDef = Element | string;

    /*
     * Global namespace type definitions — typed from the canonical _api.ts source.
     */
    let faces: typeof faces;
    let jsf: typeof faces;
    let myfaces: typeof myfaces;

    // see https://www.typescriptlang.org/docs/handbook/declaration-files/templates/global-modifying-module-d-ts.html
    // noinspection JSUnusedGlobalSymbols
    interface Window {
        [key: string]: any;
        faces: typeof faces;
        jsf: typeof faces;
        myfaces: typeof myfaces;
        XMLHttpRequest: XMLHttpRequest;
        called: { [key: string]: any };
    }
}

// needed to make this file a module so the declare global block is valid
export {};
