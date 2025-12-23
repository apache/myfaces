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

/*
 * A small stream implementation
 */
import {IMonad, IValueHolder, Optional} from "./Monad";
import {
    ArrayCollector,
    ArrayStreamDataSource, calculateSkips,
    FilteredStreamDatasource,
    ICollector,
    IStreamDataSource,
    ITERATION_STATUS,
    MappedStreamDataSource, MultiStreamDatasource
} from "./SourcesCollectors";
import {DomQuery} from "./DomQuery";
import {Config} from "./Config";
//import {from, Observable} from "rxjs";


/*
 * some typedefs to make the code more reabable
 */
export type StreamMapper<T> = (data: T) => IStreamDataSource<any>;
export type ArrayMapper<T> = (data: T) => Array<any>;
export type IteratableConsumer<T> = (data: T, pos ?: number) => void | boolean;
export type Reducable<T, V> = (val1: T | V, val2: T) => V;
export type Matchable<T> = (data: T) => boolean;
export type Mappable<T, R> = (data: T) => R;
export type Comparator<T> = (el1: T, el2: T) => number;


/**
 * Same for flatmap to deal with element -> stream mappings
 */
export class FlatMapStreamDataSource<T, S> implements IStreamDataSource<S> {

    mapFunc: StreamMapper<T>;

    inputDataSource: IStreamDataSource<T>;

    /**
     * the currently active stream
     * coming from an incoming element
     * once the end of this one is reached
     * it is swapped out by another one
     * from the next element
     */
    activeDataSource: IStreamDataSource<S>;
    walkedDataSources = [];
    _currPos = 0;

    constructor(func: StreamMapper<T>, parent: IStreamDataSource<T>) {
        this.mapFunc = func;
        this.inputDataSource = parent;
    }

    hasNext(): boolean {
        return this.resolveActiveHasNext() || this.resolveNextHasNext();
    }

    private resolveActiveHasNext() {
        let next = false;
        if (this.activeDataSource) {
            next = this.activeDataSource.hasNext();
        }
        return next;
    }

    lookAhead(cnt = 1): ITERATION_STATUS | S {

        let lookAhead = this?.activeDataSource?.lookAhead(cnt);
        if (this?.activeDataSource && lookAhead != ITERATION_STATUS.EO_STRM) {
            //this should cover 95% of all cases
            return lookAhead;
        }

        if (this.activeDataSource) {
            cnt -= calculateSkips(this.activeDataSource)
        }

        //the idea is basically to look into the streams sub-sequentially for a match
        //after each stream we have to take into consideration that the skipCnt is
        //reduced by the number of datasets we already have looked into in the previous stream/datasource
        //unfortunately for now we have to loop into them, so we introduce a small o2 here
        for (let dsLoop = 1; true; dsLoop++) {
            let datasourceData = this.inputDataSource.lookAhead(dsLoop);
            //we have looped out
            //no embedded data anymore? we are done, data
            //can either be a scalar an array or another datasource
            if (datasourceData === ITERATION_STATUS.EO_STRM) {
                return ITERATION_STATUS.EO_STRM;
            }
            let mappedData = this.mapFunc(datasourceData as T);

            //it either comes in as datasource or as array
            //both cases must be unified into a datasource
            let currentDataSource = this.toDatasource(mappedData);
            //we now run again  a lookahead
            let ret = currentDataSource.lookAhead(cnt);
            //if the value is found then we are set
            if (ret != ITERATION_STATUS.EO_STRM) {
                return ret;
            }
            //reduce the next lookahead by the number of elements
            //we are now skipping in the current data source
            cnt -= calculateSkips(currentDataSource);
        }
    }

    private toDatasource(mapped: Array<S> | IStreamDataSource<S>) {
        let ds = Array.isArray(mapped) ? new ArrayStreamDataSource(...mapped) : mapped;
        this.walkedDataSources.push(ds)
        return ds;
    }

    private resolveNextHasNext() {
        let next = false;
        while (!next && this.inputDataSource.hasNext()) {
            let mapped = this.mapFunc(this.inputDataSource.next() as T);
            this.activeDataSource = this.toDatasource(mapped);
            next = this.activeDataSource.hasNext();
        }
        return next;
    }

    next(): S | ITERATION_STATUS {
        if (this.hasNext()) {
            this._currPos++;
            return this.activeDataSource.next();
        }
    }

    reset(): void {
        this.inputDataSource.reset();
        this.walkedDataSources.forEach(ds => ds.reset());
        this.walkedDataSources = [];
        this._currPos = 0;
        this.activeDataSource = null;
    }

    current(): S | ITERATION_STATUS {
        if (!this.activeDataSource) {
            this.hasNext();
        }
        return this.activeDataSource.current();
    }
}

/**
 * Generic interface defining a stream
 */
export interface IStream<T> {
    /**
     * Perform the operation fn on a single element in the stream at a time
     * then pass the stream over for further processing
     * This is basically an intermediate point in the stream
     * with further processing happening later, do not use
     * this method to gather data or iterate over all date for processing
     * (for the second case each has to be used)
     *
     * @param fn the processing function, if it returns false, further processing is stopped
     */
    onElem(fn: IteratableConsumer<T>): IStream<T>;

    /**
     * Iterate over all elements in the stream and do some processing via fn
     *
     * @param fn takes a single element and if it returns false
     * then further processing is stopped
     */
    each(fn: IteratableConsumer<T>): void;


    /**
     * maps a single element into another via fn
     * @param fn function which takes one element in and returns another
     */
    map<R>(fn?: Mappable<T, R>): IStream<R>;

    /**
     * Takes an element in and returns a set of something
     * the set then is flatted into a single stream to be further processed
     *
     * @param fn
     */
    flatMap<R>(fn?: StreamMapper<T> | ArrayMapper<T>): IStream<R>;

    /**
     * filtering, takes an element in and is processed by fn.
     * If it returns false then further processing on this element is skipped
     * if it returns true it is passed down the chain.
     *
     * @param fn
     */
    filter(fn?: Matchable<T>): IStream<T>;

    /**
     * functional reduce... takes two elements in the stream and reduces to
     * one from left to right
     *
     * @param fn the reduction function for instance (val1,val2) => val1l+val2
     * @param startVal an optional starting value, if provided the the processing starts with this element
     * and further goes down into the stream, if not, then the first two elements are taken as reduction starting point
     */
    reduce<V>(fn: Reducable<T, V>, startVal: T | V): Optional<T | V>;

    /**
     * returns the first element in the stream is given as Optional
     */
    first(): Optional<T>;

    /**
     * Returns the last stream element (note in endless streams without filtering and limiting you will never reach that
     * point hence producing an endless loop)
     */
    last(): Optional<T>;

    /**
     * returns true if there is at least one element where a call fn(element) produces true
     *
     * @param fn
     */
    anyMatch(fn: Matchable<T>): boolean;

    /**
     * returns true if all elmements produce true on a call to fn(element)
     *
     * @param fn
     */
    allMatch(fn: Matchable<T>): boolean;

    /**
     * returns true if no elmements produce true on a call to fn(element)
     *
     * @param fn
     */
    noneMatch(fn: Matchable<T>): boolean;

    /**
     * Collect the elements with a collector given
     * There are a number of collectors provided
     *
     * @param collector
     */
    collect(collector: ICollector<T, any>): any;

    /**
     * sort on the stream, this is a special case
     * of an endpoint, so your data which is fed in needs
     * to be limited otherwise it will fail
     * it still returns a stream for further processing
     *
     * @param comparator
     */
    sort(comparator: Comparator<T>): IStream<T>;

    /**
     * Limits the stream to a certain number of elements
     *
     * @param end the limit of the stream
     */
    limits(end: number): IStream<T>;


    concat(...toAppend: Array<IStream<T>>): IStream<T>

    /**
     * returns the stream collected into an array (90% use-case abbreviation
     */
    value: Array<T>;

    /**
     * returns the currently element selected in the stream
     */
    current(): T | ITERATION_STATUS

    /**
     * returns an observable of the given stream
     */
    [Symbol.iterator](): Iterator<T>;

    //observable: Observable<T>;
}

/**
 * A simple typescript based reimplementation of streams
 *
 * This is the early eval version
 * for a lazy eval version check, LazyStream, which is api compatible
 * to this implementation, however with the benefit of being able
 * to provide infinite data sources and generic data providers, the downside
 * is, it might be a tad slower in some situations
 */
export class Stream<T> implements IMonad<T, Stream<any>>, IValueHolder<Array<T>>, IStream<T>, IStreamDataSource<T> {

    value: Array<T>;
    _limits = -1;

    private pos = -1;

    constructor(...value: T[]) {
        this.value = value;
    }

    static of<T>(...data: Array<T>): Stream<T> {
        return new Stream<T>(...data);
    }

    static ofAssoc<T>(data: { [key: string]: T }): Stream<[string, T]> {
        return this.of(...Object.keys(data)).map(key => [key, data[key]]);
    }

    static ofDataSource<T>(dataSource: IStreamDataSource<T>) {
        let value: T[] = [];
        while (dataSource.hasNext()) {
            value.push(dataSource.next() as T);
        }

        return new Stream(...value);
    }

    static ofDomQuery(value: DomQuery): Stream<DomQuery> {
        return Stream.of(...value.asArray);
    }

    static ofConfig(value: Config): Stream<[string, any]> {
        return Stream.of(... Object.keys(value.value)).map(key => [key, value.value[key]])
    }

    current(): T | ITERATION_STATUS {
        if(this.pos == -1) {
            return ITERATION_STATUS.BEF_STRM;
        }
        if(this.pos >= this.value.length) {
            return ITERATION_STATUS.EO_STRM;
        }
        return this.value[this.pos];
    }

    limits(end: number): Stream<T> {
        this._limits = end;
        return this;
    }

    /**
     * concat for streams, so that you can concat two streams together
     * @param toAppend
     */
    concat(...toAppend: Array<IStream<T>>): Stream<T> {
        let toConcat = [this].concat(toAppend as any);
        return Stream.of(...toConcat).flatMap(item => item);
    }


    onElem(fn: (data: T, pos ?: number) => void | boolean): Stream<T> {
        for (let cnt = 0; cnt < this.value.length && (this._limits == -1 || cnt < this._limits); cnt++) {
            if (fn(this.value[cnt], cnt) === false) {
                break;
            }
        }
        return this;
    }


    each(fn: (data: T, pos ?: number) => void | boolean) {
        this.onElem(fn);
        this.reset();
    }

    map<R>(fn?: (data: T) => R): Stream<R> {
        if (!fn) {
            fn = (inval: any) => <R>inval;
        }
        let res: R[] = [];
        this.each((item) => {
            res.push(fn(item))
        });

        return new Stream<R>(...res);
    }

    /*
     * we need to implement it to fullfill the contract, although it is used only internally
     * all values are flattened when accessed anyway, so there is no need to call this methiod
     */

    flatMap<IStreamDataSource>(fn: (data: T) => IStreamDataSource | Array<any>): Stream<any> {
        let ret = [];
        this.each(item => {
            let strmR: any = fn(item);
            ret = Array.isArray(strmR) ? ret.concat(strmR) : ret.concat(strmR.value);
        });
        return <Stream<any>>Stream.of(...ret);
    }

    filter(fn?: (data: T) => boolean): Stream<T> {
        let res: Array<T> = [];
        this.each((data) => {
            if (fn(data)) {
                res.push(data);
            }
        });
        return new Stream<T>(...res);
    }

    reduce<V>(fn: Reducable<T, V | T>, startVal: V = null): Optional<V | T> {
        let offset = startVal != null ? 0 : 1;
        let val1: V | T = startVal != null ? startVal : this.value.length ? this.value[0] : null;

        for (let cnt = offset; cnt < this.value.length && (this._limits == -1 || cnt < this._limits); cnt++) {
            val1 = fn(val1, this.value[cnt]);
        }
        this.reset();
        return Optional.fromNullable<Optional<any>, V | T>(val1);
    }

    first(): Optional<T> {
        this.reset();
        return this.value && this.value.length ? Optional.fromNullable(this.value[0]) : Optional.absent;
    }

    last(): Optional<T> {
        //could be done via reduce, but is faster this way
        let length = this._limits > 0 ? Math.min(this._limits, this.value.length) : this.value.length;
        this.reset();
        return Optional.fromNullable(length ? this.value[length - 1] : null);
    }

    anyMatch(fn: Matchable<T>): boolean {
        for (let cnt = 0; cnt < this.value.length && (this._limits == -1 || cnt < this._limits); cnt++) {
            if (fn(this.value[cnt])) {
                return true;
            }
        }
        this.reset();
        return false;
    }

    allMatch(fn: Matchable<T>): boolean {
        if (!this.value.length) {
            return false;
        }
        let matches = 0;
        for (let cnt = 0; cnt < this.value.length; cnt++) {
            if (fn(this.value[cnt])) {
                matches++;
            }
        }
        this.reset();
        return matches == this.value.length;
    }

    noneMatch(fn: Matchable<T>): boolean {
        let matches = 0;
        for (let cnt = 0; cnt < this.value.length; cnt++) {
            if (!fn(this.value[cnt])) {
                matches++;
            }
        }
        this.reset();
        return matches == this.value.length;
    }

    sort(comparator: Comparator<T>): IStream<T> {
        let newArr = this.value.slice().sort(comparator);
        return Stream.of(...newArr);
    }


    collect(collector: ICollector<T, any>): any {
        this.each(data => collector.collect(data));
        this.reset();
        return collector.finalValue;
    }

    //-- internally exposed methods needed for the interconnectivity
    hasNext(): boolean {
        let isLimitsReached = this._limits != -1 && this.pos >= this._limits - 1;
        let isEndOfArray = this.pos >= this.value.length - 1;
        return !(isLimitsReached || isEndOfArray);
    }

    next(): T {
        if (!this.hasNext()) {
            return null;
        }
        this.pos++;
        return this.value[this.pos];
    }

    lookAhead(cnt = 1): T | ITERATION_STATUS {
        if((this.pos + cnt) >= this.value.length) {
            return ITERATION_STATUS.EO_STRM;
        }
        return this.value[this.pos + cnt];
    }


    [Symbol.iterator]() : Iterator<T> {
        return {
            next: () => {
                let done = !this.hasNext();
                let val = this.next();
                return {
                    done: done,
                    value: <T>val
                }
            }
        }
    }

    /*get observable(): Observable<T> {
        return from(this);
    }*/

    reset() {
        this.pos = -1;
    }
}

/**
 * Lazy implementation of a Stream
 * The idea is to connect the intermediate
 * streams as datasources like a linked list
 * with reverse referencing and for special
 * operations like filtering flatmapping
 * have intermediate datasources in the list
 * with specialized functions.
 *
 * Sort of a modified pipe valve pattern
 * the streams are the pipes the intermediate
 * data sources are the valves
 *
 * We then can use passed in functions to control
 * the flow in the valves
 *
 * That way we can have a lazy evaluating stream
 *
 * So if an endpoint requests data
 * a callback trace goes back the stream list
 * which triggers an operation upwards
 * which sends data down the drain which then is processed
 * and filtered until one element hits the endpoint.
 *
 * That is repeated, until all elements are processed
 * or an internal limit is hit.
 *
 */
export class LazyStream<T> implements IStreamDataSource<T>, IStream<T>, IMonad<T, LazyStream<any>> {

    protected dataSource: IStreamDataSource<T>;
    _limits = -1;

    /*
     * needed to have the limits check working
     * we need to keep track of the current position
     * in the stream
     */
    pos = -1;

    static of<T>(...values: Array<T>): LazyStream<T> {
        return new LazyStream<T>(new ArrayStreamDataSource(...values));
    }

    static ofAssoc<T>(data: { [key: string]: T }): LazyStream<[string, T]> {
        return this.of(...Object.keys(data)).map(key => [key, data[key]]);
    }

    static ofStreamDataSource<T>(value: IStreamDataSource<T>): LazyStream<T> {
        return new LazyStream(value);
    }

    static ofDomQuery(value: DomQuery): LazyStream<DomQuery> {
        return LazyStream.of(...value.asArray);
    }

    static ofConfig(value: Config): LazyStream<[string, any]> {
        return LazyStream.of(... Object.keys(value.value)).map(key => [key, value.value[key]])
    }

    constructor(parent: IStreamDataSource<T>) {
        this.dataSource = parent;

    }

    hasNext(): boolean {
        if (this.isOverLimits()) {
            return false;
        }

        return this.dataSource.hasNext();
    }

    next(): T | ITERATION_STATUS {
        let next = this.dataSource.next();
        // @ts-ignore
        this.pos++;
        return next;
    }

    lookAhead(cnt= 1): ITERATION_STATUS | T {
        return this.dataSource.lookAhead(cnt);
    }



    current(): T | ITERATION_STATUS {
        return this.dataSource.current();
    }

    reset(): void {
        this.dataSource.reset();
        this.pos = -1;
        this._limits = -1;
    }

    /**
     * concat for streams, so that you can concat two streams together
     * @param toAppend
     */
    concat(...toAppend: Array<IStream<T>>): LazyStream<T> {
        //this.dataSource =  new MultiStreamDatasource<T>(this, ... toAppend);
        //return this;
        return LazyStream.ofStreamDataSource(new MultiStreamDatasource(this, toAppend as any) as any)
        //return LazyStream.of(<IStream<T>>this, ...toAppend).flatMap(item => item);
    }

    nextFilter(fn: Matchable<T>): T {
        if (this.hasNext()) {
            let newVal: T = this.next() as T;
            if (!fn(newVal)) {
                return this.nextFilter(fn);
            }
            return <T>newVal;
        }
        return null;
    }

    limits(max: number): LazyStream<T> {
        this._limits = max;
        return this;
    }

    //main stream methods
    collect(collector: ICollector<T, any>): any {
        while (this.hasNext()) {
            let t = this.next();
            collector.collect(<T>t);
        }
        this.reset();
        return collector.finalValue;
    }

    onElem(fn: IteratableConsumer<T>): LazyStream<T> {
        return new LazyStream(new MappedStreamDataSource((el) => {
            if (fn(el, this.pos) === false) {
                this.stop();
            }
            return el;
        }, this));
    }

    filter(fn: Matchable<T>): LazyStream<T> {
        return <LazyStream<T>>new LazyStream<T>(new FilteredStreamDatasource<any>(fn, this));
    }

    map<R>(fn: Mappable<T, R>): LazyStream<any> {
        return new LazyStream(new MappedStreamDataSource(fn, this));
    }

    flatMap<StreamMapper>(fn: StreamMapper | ArrayMapper<any>): LazyStream<any> {
        return new LazyStream<any>(new FlatMapStreamDataSource(fn as any, this));
    }

    //endpoint
    each(fn: IteratableConsumer<T>) {
        while (this.hasNext()) {
            if (fn(this.next() as T) === false) {
                this.stop();
            }
        }
        this.reset();
    }

    reduce<V>(fn: Reducable<T, V>, startVal: T | V = null): Optional<T | V> {
        if (!this.hasNext()) {
            return Optional.absent;
        }
        let value1;
        let value2 = null;
        if (startVal != null) {
            value1 = startVal;
            value2 = this.next();
        } else {
            value1 = this.next();
            if (!this.hasNext()) {
                return Optional.fromNullable(value1);
            }
            value2 = this.next();
        }
        value1 = fn(value1, value2);
        while (this.hasNext()) {
            value2 = this.next();
            value1 = fn(value1, value2);
        }
        this.reset();
        return Optional.fromNullable(value1);
    }

    last(): Optional<T> {
        if (!this.hasNext()) {
            return Optional.absent;
        }
        return this.reduce((el1, el2) => el2);
    }

    first(): Optional<T> {
        this.reset();
        if (!this.hasNext()) {
            return Optional.absent;
        }
        return Optional.fromNullable(this.next() as T);
    }

    anyMatch(fn: Matchable<T>): boolean {
        while (this.hasNext()) {
            if (fn(this.next() as T)) {
                return true;
            }
        }
        return false;
    }

    allMatch(fn: Matchable<T>): boolean {
        while (this.hasNext()) {
            if (!fn(this.next() as T)) {
                return false;
            }
        }
        return true;
    }

    noneMatch(fn: Matchable<T>): boolean {
        while (this.hasNext()) {
            if (fn(this.next() as T)) {
                return false;
            }
        }
        return true;
    }

    sort(comparator: Comparator<T>): IStream<T> {
        let arr = this.collect(new ArrayCollector());
        arr = arr.sort(comparator);
        return LazyStream.of(...arr);
    }

    get value(): Array<T> {
        return this.collect(new ArrayCollector<T>());
    }

    [Symbol.iterator]() : Iterator<T> {
        return {
            next: () => {
                let done = !this.hasNext();
                let val = this.next();
                return {
                    done: done,
                    value: <T>val
                }
            }
        }
    }

    /*get observable(): Observable<T> {
        return from(this);
    }*/

    private stop() {
        this.pos = this._limits + 1000000000;
        this._limits = 0;
    }

    private isOverLimits() {
        return this._limits != -1 && this.pos >= this._limits - 1;
    }

}


/**
 * 1.0 backwards compatibility functions
 *
 * this restores the stream and lazy stream
 * property on DomQuery on prototype level
 *
 */

Object.defineProperty(DomQuery.prototype, "stream", {
    get: function stream(){
        return Stream.ofDomQuery(this);
    }
})


Object.defineProperty(DomQuery.prototype, "lazyStream", {
    get: function lazyStream(){
        return LazyStream.ofDomQuery(this);
    }
})