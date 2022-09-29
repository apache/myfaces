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

import {Optional} from "./Monad";

/**
 * Lang helpers crossported from the apache myfaces project
 */
export module Lang {
    

    //should be in lang, but for now here to avoid recursive imports, not sure if typescript still has a problem with those
    /**
     * helper function to savely resolve anything
     * this is not an elvis operator, it resolves
     * a value without exception in a tree and if
     * it is not resolvable then an optional of
     * a default value is restored or Optional.empty
     * if none is given
     *
     * usage
     * <code>
     *     let var: Optiona<string> = saveResolve(() => a.b.c.d.e, "foobaz")
     * </code>
     *
     * @param resolverProducer a lambda which can produce the value
     * @param defaultValue an optional default value if the producer failes to produce anything
     * @returns an Optional of the produced value
     */
    export function saveResolve<T>(resolverProducer: () => T, defaultValue: T = null): Optional<T> {
        try {
            let result = resolverProducer();
            return Optional.fromNullable(result ?? defaultValue);
        } catch (e) {
            return Optional.absent;
        }
    }

    export function saveResolveLazy<T>(resolverProducer: () => T, defaultValue: () => T = null): Optional<T> {
        try {
            let result = resolverProducer();
            return Optional.fromNullable(result ?? defaultValue());
        } catch (e) {
            return Optional.absent;
        }
    }

    /**
     * String to array function performs a string to array transformation
     * @param {String} it the string which has to be changed into an array
     * @param {RegExp} splitter our splitter reglar expression
     * @return a trimmed array of the splitted string
     */
    export function strToArray(it: string, splitter: string | RegExp = /\./gi): Array<string> {

        let ret = [];
        it.split(splitter).forEach((element => {
            ret.push(trim(element));
        }));
        return ret;
    }

    /**
     * hyperfast trim
     * http://blog.stevenlevithan.com/archives/faster-trim-javascript
     * crossported from dojo
     */
    export function trim(str: string): string {
        str = str.replace(/^\s\s*/, '');
        let ws = /\s/, i = str.length;

        while (ws.test(str.charAt(--i))) {
            //do nothing
        }
        return str.slice(0, i + 1);
    }

    /**
     * generic object arrays like dom definitions to array conversion method which
     * transforms any object to something array like
     * @param obj
     * @param offset
     * @param pack
     * @returns an array converted from the object
     */
    export function objToArray<T>(obj: any, offset: number = 0, pack: Array<T> = []): Array<T> {
        if ((obj ?? "__undefined__") == "__undefined__") {
            return pack ?? null;
        }
        //since offset is numeric we cannot use the shortcut due to 0 being false
        //special condition array delivered no offset no pack
        if ((<any>obj) instanceof Array && !offset && !pack) return obj;

        return pack.concat(Array.prototype.slice.call(obj, offset));
    }

    /**
     * equalsIgnoreCase, case insensitive comparison of two strings
     *
     * @param source
     * @param destination
     */
    export function equalsIgnoreCase(source?: string, destination?: string): boolean {
        let finalSource = source ?? "___no_value__";
        let finalDest = destination ?? "___no_value__";

        //in any other case we do a strong string comparison
        return finalSource.toLowerCase() === finalDest.toLowerCase();
    }


    /**
     * runtime type assertion
     *
     * @param probe the probe to be tested for a type
     * @param theType the type to be tested for
     */
    export function assertType(probe: any, theType: any): boolean {
        return isString(theType) ? typeof probe == theType : probe instanceof theType;
    }

    /**
     * Backported from dojo
     * a failsafe string determination method
     * (since in javascript String != "" typeof alone fails!)
     * @param it {|Object|} the object to be checked for being a string
     * @return true in case of being a string false otherwise
     */
    export function isString(it?: any): boolean {
        //	summary:
        //		Return true if it is a String
        return !!arguments.length && it != null && (typeof it == "string" || it instanceof String); // Boolean
    }

    export function isFunc(it: any): boolean {
        return it instanceof Function || typeof it === "function";
    }

    // code from https://developer.mozilla.org/de/docs/Web/JavaScript/Reference/Global_Objects/Object/assign
    // license https://creativecommons.org/licenses/by-sa/2.5/
    export function objAssign(target: any, ...theArgs: any) { // .length of function is 2
        if (target == null) { // TypeError if undefined or null
            throw new TypeError('Cannot convert undefined or null to object');
        }

        let to = Object(target);
        if((<any>Object).assign) {
            theArgs.forEach(item => (<any>Object).assign(to, item));
            return to;
        }

        theArgs.filter(item => item != null).forEach(item => {
            let nextSource = item;
            Object.keys(nextSource)
                .filter(nextKey => Object.prototype.hasOwnProperty.call(nextSource, nextKey))
                .forEach(nextKey => to[nextKey] = nextSource[nextKey]);
        });
        return to;
    }


}

