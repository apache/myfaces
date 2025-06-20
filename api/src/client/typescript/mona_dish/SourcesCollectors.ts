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

import {DomQuery} from "./DomQuery";
import {Es2019Array} from "./Es2019Array";
import {Config} from "./Config";

/**
 * special status of the datasource location pointer
 * if an access, outside - of the possible data boundaries is happening
 * (example for instance current without a first next call, or next
 * which goes over the last possible dataset), an iteration status return
 * value is returned marking this boundary instead of a classical element
 *
 * Note this is only internally used but must be implemented to fulfill
 * internal contracts, the end user will never see those values if he uses
 * streams!
 */
export enum ITERATION_STATUS {
    EO_STRM = '__EO_STRM__',
    BEF_STRM = '___BEF_STRM__',
}

export function calculateSkips(next_strm: IStreamDataSource<any>) {
    let pos = 1;
    while (next_strm.lookAhead(pos) != ITERATION_STATUS.EO_STRM) {
        pos++;
    }
    return --pos;
}

/**
 * Every data source which feeds data into the lazy stream
 * or stream generally must implement this interface
 *
 * It is basically an iteratable to the core
 */
export interface IStreamDataSource<T> {

    /**
     * @returns true if additional data is present false if not
     */
    hasNext(): boolean;

    /**
     * returns the next element in the stream
     */
    next(): T | ITERATION_STATUS;

    /**
     * looks ahead cnt without changing the internal data "pointers" of the data source
     * (this is mostly needed by possibly infinite constructs like lazy streams,
     * because they do not know by definition their
     * boundaries)
     *
     * @param cnt the elements to look ahead
     * @return either the element or ITERATION_STATUS.EO_STRM if we hit the end of the stream before
     * finding the "cnt" element
     */
    lookAhead(cnt ?: number): T | ITERATION_STATUS;

    /**
     * returns the current element, returns the same element as the previous next call
     * if there is no next before current called then we will call next as initial element
     */
    current(): T | ITERATION_STATUS;

    /**
     * resets the position to the beginning
     */
    reset(): void;
}

/**
 * A collector, needs to be implemented
 */
export interface ICollector<T, S> {
    /**
     * this method basically takes a single stream element
     * and does something with it (collecting it one way or the other
     * in most cases)
     *
     * @param element
     */
    collect(element: T);

    /**
     * the final result after all the collecting is done
     */
    finalValue: S;
}


/**
 * A data source which combines multiple streams sequentially into one
 * (this is used internally by  flatmap, but also can be used externally)
 */
export class MultiStreamDatasource<T> implements IStreamDataSource<T> {

    private  activeStrm;
    private  selectedPos = 0;
    private  strms;

    constructor(private first, ...strms: Array<IStreamDataSource<T>>) {
        this.strms = [first].concat(...strms);
        this.activeStrm = this.strms[this.selectedPos];
    }

    current(): any {
        return this.activeStrm.current();
    }

    hasNext(): boolean {
        if(this.activeStrm.hasNext()) {
            return true;
        }
        if(this.selectedPos >= this.strms.length) {
            return false;
        }
        return this.findNextStrm() != -1;
    }

    private findNextStrm(): number {
        let hasNext = false;
        let cnt = this.selectedPos;
        while(!hasNext && cnt < this.strms.length) {
            hasNext = this.strms[cnt].hasNext();
            if(!hasNext) {
                cnt++;
            }
        }
        return hasNext ? cnt : -1;
    }

    lookAhead(cnt: number = 1): T | ITERATION_STATUS {
        //lets clone
        const strms = this.strms.slice(this.selectedPos);

        if(!strms.length) {
            return ITERATION_STATUS.EO_STRM;
        }

        const all_strms = [...strms];
        while(all_strms.length) {
            let next_strm = all_strms.shift();
            let lookAhead = next_strm.lookAhead(cnt);

            if (lookAhead != ITERATION_STATUS.EO_STRM) {
                return lookAhead;
            }
            cnt = cnt - calculateSkips(next_strm);
        }
        return ITERATION_STATUS.EO_STRM;
    }


    next(): any {
        if(this.activeStrm.hasNext()) {
            return this.activeStrm.next();
        }
        this.selectedPos = this.findNextStrm();
        if(this.selectedPos == -1) {
            return ITERATION_STATUS.EO_STRM;
        }
        this.activeStrm = this.strms[this.selectedPos];
        return this.activeStrm.next();
    }

    reset(): void {
        this.activeStrm = this.strms[0];
        this.selectedPos = 0;
        for(let cnt = 0; cnt < this.strms.length; cnt++) {
            this.strms[cnt].reset();
        }
    }

}

/**
 * defines a sequence of numbers for our stream input
 */
export class SequenceDataSource implements IStreamDataSource<number> {

    start: number;
    total: number;
    value: number;

    constructor(start: number, total: number) {
        this.total = total;
        this.start = start;
        this.value = start - 1;
    }


    hasNext(): boolean {
        return this.value < (this.total - 1);
    }

    next(): number | ITERATION_STATUS {
        this.value++;
        return this.value <= (this.total - 1) ? this.value : ITERATION_STATUS.EO_STRM;
    }

    lookAhead(cnt = 1): number | ITERATION_STATUS {
        if ((this.value + cnt) > this.total - 1) {
            return ITERATION_STATUS.EO_STRM;
        } else {
            return this.value + cnt;
        }
    }

    reset(): void {
        this.value = this.start - 1;
    }

    current(): number | ITERATION_STATUS {
        //first condition current without initial call for next
        return (this.start - 1) ? ITERATION_STATUS.BEF_STRM : this.value;
    }
}


/**
 * implementation of a datasource on top of a standard array
 */
export class ArrayStreamDataSource<T> implements IStreamDataSource<T> {
    value: Array<T>;
    dataPos = -1;

    constructor(...value: Array<T>) {
        this.value = value;
    }

    lookAhead(cnt = 1): T | ITERATION_STATUS {
        if ((this.dataPos + cnt) > this.value.length - 1) {
            return ITERATION_STATUS.EO_STRM;
        }
        return this.value[this.dataPos + cnt];
    }

    hasNext(): boolean {
        return this.value.length - 1 > this.dataPos;
    }

    next(): T | ITERATION_STATUS {
        this.dataPos++;
        return this?.value[this.dataPos] ?? ITERATION_STATUS.EO_STRM;
    }

    reset() {
        this.dataPos = -1;
    }

    current(): T {
        return this.value[Math.max(0, this.dataPos)];
    }
}

/**
 * an intermediate data source which prefilters
 * incoming stream data
 * and lets only the data out which
 * passes the filter function check
 */
export class FilteredStreamDatasource<T> implements IStreamDataSource<T> {

    filterFunc: (T) => boolean;
    inputDataSource: IStreamDataSource<T>;

    _current: T | ITERATION_STATUS = ITERATION_STATUS.BEF_STRM;
    // we have to add a filter idx because the external filter values might change over time, so
    // we cannot reset the state properly unless we do it from a snapshot
    _filterIdx = {};
    _unfilteredPos = 0;

    constructor(filterFunc: (T) => boolean, parent: IStreamDataSource<T>) {
        this.filterFunc = filterFunc;
        this.inputDataSource = parent;
    }

    /**
     * in order to filter we have to make a look ahead until the
     * first next allowed element
     * hence we prefetch the element and then
     * serve it via next
     */
    hasNext(): boolean {
        let steps = 1;
        let found = false;
        let next;

        while (!found && (next = this.inputDataSource.lookAhead(steps)) != ITERATION_STATUS.EO_STRM) {
            if (this.filterFunc(next)) {
                this._filterIdx[this._unfilteredPos + steps] = true;
                found = true;
            } else {
                steps++;
            }
        }
        return found;
    }

    /**
     * serve the next element
     */
    next(): T | ITERATION_STATUS {
        let found: T | ITERATION_STATUS = ITERATION_STATUS.EO_STRM;
        while (this.inputDataSource.hasNext()) {
            this._unfilteredPos++;
            let next: T = <T>this.inputDataSource.next();
            //again here we cannot call the filter function twice, because its state might change, so if indexed, we have a decent snapshot, either has next or next can trigger
            //the snapshot
            if (next != ITERATION_STATUS.EO_STRM &&
                ((this._filterIdx?.[this._unfilteredPos] ?? false) || this.filterFunc(next))) {
                this._filterIdx[this._unfilteredPos] = true;
                found = next;
                break;
            }
        }
        this._current = found as T;
        return found;
    }

    /**
     * looks ahead cnt without changing the internal data "pointers" of the data source
     * (this is mostly needed by LazyStreams, because they do not know by definition their
     * boundaries)
     *
     * @param cnt the elements to look ahead
     * @return either the element or ITERATION_STATUS.EO_STRM if we hit the end of the stream before
     * finding the "cnt" element
     */
    lookAhead(cnt = 1): ITERATION_STATUS | T {
        let lookupVal: T | ITERATION_STATUS;

        for (let loop = 1; cnt > 0 && (lookupVal = this.inputDataSource.lookAhead(loop)) != ITERATION_STATUS.EO_STRM; loop++) {
            let inCache = this._filterIdx?.[this._unfilteredPos + loop];
            if (inCache || this.filterFunc(lookupVal)) {
                cnt--;
                this._filterIdx[this._unfilteredPos + loop] = true;
            }
        }
        return lookupVal;
    }

    current(): T | ITERATION_STATUS {
        return this._current;
    }

    reset(): void {
        this._current = ITERATION_STATUS.BEF_STRM;
        this._filterIdx = {};
        this._unfilteredPos = 0;
        this.inputDataSource.reset();
    }
}

/**
 * an intermediate datasource which maps the items from
 * one into another
 */
export class MappedStreamDataSource<T, S> implements IStreamDataSource<S> {

    mapFunc: (T) => S;
    inputDataSource: IStreamDataSource<T>;

    constructor(mapFunc: (T) => S, parent: IStreamDataSource<T>) {
        this.mapFunc = mapFunc;
        this.inputDataSource = parent;
    }

    hasNext(): boolean {
        return this.inputDataSource.hasNext();
    }

    next(): S {
        return this.mapFunc(this.inputDataSource.next());
    }

    reset(): void {
        this.inputDataSource.reset();
    }

    current(): S {
        return this.mapFunc(this.inputDataSource.current());
    }

    lookAhead(cnt = 1): ITERATION_STATUS | S {
        const lookAheadVal = this.inputDataSource.lookAhead(cnt);
        return (lookAheadVal == ITERATION_STATUS.EO_STRM) ? lookAheadVal as ITERATION_STATUS : this.mapFunc(lookAheadVal) as S;
    }
}


/**
 * For the time being we only need one collector
 * a collector which collects a stream back into arrays
 */
export class ShimArrayCollector<S> implements ICollector<S, Array<S>> {
    private data: Array<S> = new Es2019Array(...[]);

    collect(element: S) {
        this.data.push(element);
    }

    get finalValue(): Array<S> {
        return this.data;
    }
}

/**
 * collects the values as inverse array
 */
export class InverseArrayCollector<S> implements ICollector<S, Array<S>> {
    private data: Array<S> = [];

    collect(element: S) {
        this.data.unshift(element);
    }

    get finalValue(): Array<S> {
        return this.data;
    }
}


/**
 * collects an tuple array stream into an assoc array with elements being collected into arrays
 *
 */
export class ArrayAssocArrayCollector<S> implements ICollector<[string, S] | string, { [key: string]: S }> {
    finalValue: { [key: string]: any } = {};

    collect(element: [string, S] | string) {
        let key = element?.[0] ?? <string>element;
        this.finalValue[key] = this.finalValue?.[key] ?? [];
        this.finalValue[key].push(element?.[1] ?? true);
    }
}

/**
 * dummy collector which just triggers a run
 * on lazy streams without collecting anything
 */
export class Run<S> implements ICollector<S, any> {
    collect(element: S) {

    }

    get finalValue(): any {
        return null;
    }
}

/**
 * collects an assoc stream back to an assoc array
 */
export class AssocArrayCollector<S> implements ICollector<[string, S] | string, { [key: string]: S }> {

    finalValue: { [key: string]: any } = {};

    collect(element: [string, S] | string) {
        this.finalValue[element[0] ?? <string>element] = element[1] ?? true;
    }
}


/**
 * A Config collector similar to the FormDFata Collector
 */
export class ConfigCollector implements ICollector<{ key: string, value: any }, Config> {
    finalValue: Config = new Config({});

    collect(element: { key: string; value: any }) {
        this.finalValue.append(element.key).value = element.value;
    }
}


/**
 * Form data collector for key value pair streams
 */
export class FormDataCollector implements ICollector<{ key: string, value: any }, FormData> {
    finalValue: FormData = new FormData();

    collect(element: { key: string; value: any }) {
        this.finalValue.append(element.key, element.value);
    }
}

/**
 * Form data collector for DomQuery streams
 */
export class QueryFormDataCollector implements ICollector<DomQuery, FormData> {
    finalValue: FormData = new FormData();

    collect(element: DomQuery) {
        let toMerge = element.encodeFormElement();
        if (toMerge.isPresent()) {
            this.finalValue.append(element.name.value, toMerge.get(element.name).value);
        }
    }
}

/**
 * Encoded String collector from dom query streams
 */
export class QueryFormStringCollector implements ICollector<DomQuery, string> {

    formData: [[string, string]] = <any>[];

    collect(element: DomQuery) {
        let toMerge = element.encodeFormElement();
        if (toMerge.isPresent()) {
            this.formData.push([element.name.value, toMerge.get(element.name).value]);
        }
    }

    get finalValue(): string {
        return new Es2019Array(...this.formData)
            .map(keyVal => keyVal.join("="))
            .reduce((item1, item2) => [item1, item2].join("&"));
    }
}

/**
 * For the time being we only need one collector
 * a collector which collects a stream back into arrays
 */
export class ArrayCollector<S> implements ICollector<S, Array<S>> {
    private data: Array<S> = [];

    collect(element: S) {
        this.data.push(element);
    }

    get finalValue(): Array<S> {
        return this.data;
    }
}
