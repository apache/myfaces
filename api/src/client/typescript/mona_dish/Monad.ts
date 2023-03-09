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
 * A module which keeps  basic monad like definitions in place
 * Useful if you need the functions in another library to keep its dependencies down
 */

/*IMonad definitions*/

import {Lang} from "./Lang";
import {Es2019Array} from "./Es2019Array";


/**
 * IFunctor interface,
 * defines an interface which allows to map a functor
 * via a first order function to another functor
 */
export interface IFunctor<T> {
    map<R>(fn: (data: T) => R): IFunctor<R>;
}

/**
 * IMonad definition, basically a functor with a flatMap implementation, flatMap reduces all nested monads after a
 * function call f into a monad with the nesting level of 1
 *
 * flatmap flats nested Monads into a IMonad of the deepest nested implementation
 */
export interface IMonad<T, M extends IMonad<any, any>> extends IFunctor<T> {
    flatMap<T, M>(f: (T) => M): IMonad<any, any>;
}

/**
 * a stateful functor which holds a value upn which a
 * function can be applied
 *
 * as value holder of type T
 */
export interface IIdentity<T> extends IFunctor<T> {
    readonly value: T;
}


/**
 *  custom value holder definition, since we are not pure functional
 *  but iterative we have structures which allow the assignment of a value
 *  also not all structures are side - effect free
 */
export interface IValueHolder<T> {
    value: T | Array<T>;
}

/**
 * Implementation of a monad
 * (Side - effect free), no write allowed directly on the monads
 * value state
 */
export class Monad<T> implements IMonad<T, Monad<any>>, IValueHolder<T> {
    constructor(value: T) {
        this._value = value;
    }

    protected _value: T;

    get value(): T {
        return this._value;
    }

    map<R>(fn?: (data: T) => R): Monad<R> {
        if (!fn) {
            fn = (inVal: any) => <R>inVal;
        }
        let result: R = fn(this.value);
        return new Monad(result);
    }

    flatMap<R>(fn?: (data: T) => R): Monad<any> {
        let mapped: Monad<any> = this.map(fn);
        while (mapped?.value instanceof Monad) {
            mapped = mapped.value
        }
        return mapped;
    }

}

/**
 * optional implementation, an optional is basically an implementation of a Monad with additional syntactic
 * sugar on top
 * (Side - effect free, since value assignment is not allowed)
 * */
export class Optional<T> extends Monad<T> {

    /*default value for absent*/
    static absent = Optional.fromNullable(null);

    constructor(value: T) {
        super(value);
    }

    get value(): T {
        if (this._value instanceof Monad) {
            return this._value.flatMap().value
        }
        return this._value;
    }

    static fromNullable<V extends Optional<T>, T>(value?: T): Optional<T> {
        return <V> new Optional(value);
    }

    /*syntactic sugar for absent and present checks*/
    isAbsent(): boolean {
        return "undefined" == typeof this.value || null == this.value;
    }

    /**
     * any value present
     */
    isPresent(presentRunnable ?: (val ?: Monad<T>) => void): boolean {
        let absent = this.isAbsent();
        if (!absent && presentRunnable) {
            presentRunnable.call(this, this)
        }
        return !absent;
    }

    ifPresentLazy(presentRunnable: (val ?: Monad<T>) => void = () => {
    }): Monad<T> {
        this.isPresent.call(this, presentRunnable);
        return this;
    }

    orElse(elseValue: any): Optional<any> {
        if (this.isPresent()) {
            return this;
        } else {
            //shortcut
            if (elseValue == null) {
                return Optional.absent;
            }
            return this.flatMap(() => elseValue);
        }
    }

    /**
     * lazy, passes a function which then is lazily evaluated
     * instead of a direct value
     * @param func
     */
    orElseLazy(func: () => any): Optional<any> {
        if (this.isPresent()) {
            return this;
        } else {
            return this.flatMap(func);
        }
    }

    /*
     * we need to implement it to fulfill the contract, although it is used only internally
     * all values are flattened when accessed anyway, so there is no need to call this method
     */
    flatMap<R>(fn?: (data: T) => R): Optional<any> {
        let val = super.flatMap(fn);
        if (!(val instanceof Optional)) {
            return Optional.fromNullable(val.value);
        }

        return <Optional<any>>val.flatMap();
    }

    /*
     * elvis operation, take care, if you use this you lose typesafety and refactoring
     * capabilities, unfortunately typescript does not allow to have its own elvis operator
     * this is some syntactic sugar however which is quite useful*/
    getIf<R>(...key: string[]): Optional<R> {

        key = this.preprocessKeys(...key);

        let currentPos: Optional<any> = this;
        for (let cnt = 0; cnt < key.length; cnt++) {
            let currKey = this.keyVal(key[cnt]);
            let arrPos = this.arrayIndex(key[cnt]);

            if (currKey === "" && arrPos >= 0) {
                currentPos = this.getClass().fromNullable(!(currentPos.value instanceof Array) ? null : (currentPos.value.length < arrPos ? null : currentPos.value[arrPos]));
                if (currentPos.isAbsent()) {
                    return currentPos;
                }
                continue;
            } else if (currKey && arrPos >= 0) {
                if (currentPos.getIfPresent(currKey).isAbsent()) {
                    return currentPos;
                }
                currentPos = (currentPos.getIfPresent(currKey).value instanceof Array) ? this.getClass().fromNullable(currentPos.getIfPresent(currKey).value[arrPos]) : this.getClass().absent;
                if (currentPos.isAbsent()) {
                    return currentPos;
                }
                continue;

            } else {
                currentPos = currentPos.getIfPresent(currKey);
            }
            if (currentPos.isAbsent()) {
                return currentPos;
            } else if (arrPos > -1) {
                currentPos = this.getClass().fromNullable(currentPos.value[arrPos]);
            }
        }
        return currentPos;
    }

    /**
     * simple match, if the first order function call returns
     * true then there is a match, if the value is not present
     * it never matches
     *
     * @param fn the first order function performing the match
     */
    match(fn: (item: T) => boolean): boolean {
        if (this.isAbsent()) {
            return false
        }
        return fn(this.value);
    }

    /**
     * convenience function to flatmap the internal value
     * and replace it with a default in case of being absent
     *
     * @param defaultVal
     * @returns {Optional<any>}
     */
    get<R>(defaultVal: any = Optional.absent): Optional<R> {
        if (this.isAbsent()) {
            return this.getClass().fromNullable(defaultVal).flatMap();
        }

        return this.getClass().fromNullable(this.value).flatMap();
    }

    toJson(): string {
        return JSON.stringify(this.value);
    }

    /**
     * helper to override several implementations in a more fluent way
     * by having a getClass operation we can avoid direct calls into the constructor or
     * static methods and do not have to implement several methods which rely on the type
     * of "this"
     * @returns the type of Optional
     */
    protected getClass(): any {
        return Optional;
    }

    /*helper method for getIf with array access aka <name>[<indexPos>]*/
    protected arrayIndex(key: string): number {
        let start = key.indexOf("[");
        let end = key.indexOf("]");
        if (start >= 0 && end > 0 && start < end) {
            return parseInt(key.substring(start + 1, end));
        } else {
            return -1;
        }
    }

    /*helper method for getIf with array access aka <name>[<indexPos>]*/
    protected keyVal(key: string): string {
        let start = key.indexOf("[");

        if (start >= 0) {
            return key.substring(0, start);
        } else {
            return key;
        }
    }

    /**
     * additional syntactic sugar which is not part of the usual optional implementation
     * but makes life easier, if you want to sacrifice typesafety and refactoring
     * capabilities in typescript
     */
    getIfPresent<R>(key: string): Optional<R> {
        if (this.isAbsent()) {
            return this.getClass().absent;
        }
        return this.getClass().fromNullable(this.value[key]).flatMap();
    }

    /**
     * elvis like typesafe functional save resolver
     * a typesafe option for getIfPresent
     *
     * usage myOptional.resolve(value => value.subAttr.subAttr2).orElseLazy(....)
     * if this is resolvable without any errors an Optional with the value is returned
     * if not, then an Optional absent is returned, also if you return Optional absent
     * it is flatmapped into absent
     *
     * @param resolver the resolver function, can throw any arbitrary errors, int  the error case
     * the resolution goes towards absent
     */
    resolve<V>(resolver: (item: T) => V): Optional<V> {
        if (this.isAbsent()) {
            return Optional.absent;
        }
        try {
            return Optional.fromNullable(resolver(this.value))
        } catch (e) {
            return Optional.absent;
        }
    }


    protected preprocessKeys(...keys): string[] {
        return new Es2019Array(...keys)
            .flatMap(item => {
                return new Es2019Array(...item.split(/]\s*\[/gi))
                    .map(item => {
                        item = item.replace(/^\s+|\s+$/g, "");
                        if(item.indexOf("[") == -1 && item.indexOf("]") != -1) {
                            item = "[" + item;
                        }
                        if(item.indexOf("]") == -1 && item.indexOf("[") != -1) {
                            item = item + "]";
                        }
                        return item;
                    })
            });
    }
}


// --------------------- From here onwards we break out the side effect free limits ------------

/**
 * ValueEmbedder is the writeable version
 * of optional, it basically is a wrapper
 * around a construct which has a state
 * and can be written to.
 *
 * For the readonly version see Optional
 */
export class ValueEmbedder<T> extends Optional<T> implements IValueHolder<T> {

    /*default value for absent*/
    static absent = ValueEmbedder.fromNullable(null);

    protected key: string;

    constructor(rootElem: any, valueKey: string = "value") {
        super(rootElem);

        this.key = valueKey;
    }

    get value(): T {
        return this._value ? <T>this._value[this.key] : null;
    }

    set value(newVal: T) {
        if (!this._value) {
            return;
        }
        this._value[this.key] = newVal
    }

    orElse(elseValue: any): Optional<any> {
        let alternative = {};
        alternative[this.key] = elseValue;
        return this.isPresent() ? this : new ValueEmbedder(alternative, this.key);
    }

    orElseLazy(func: () => any): Optional<any> {
        if (this.isPresent()) {
            return this;
        } else {
            let alternative = {};
            alternative[this.key] = func();
            return new ValueEmbedder(alternative, this.key);
        }
    }

    /**
     * helper to override several implementations in a more fluent way
     * by having a getClass operation we can avoid direct calls into the constructor or
     * static methods and do not have to implement several methods which rely on the type
     * of "this"
     * @returns ValueEmbedder
     */
    protected getClass(): any {
        return ValueEmbedder;
    }

    static fromNullable<V extends Optional<T>,T>(value?: any, valueKey: string = "value"): V {
        return <any> new ValueEmbedder<T>(value, valueKey);
    }

}



