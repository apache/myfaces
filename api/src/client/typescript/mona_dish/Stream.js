"use strict";
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
var __spreadArray = (this && this.__spreadArray) || function (to, from, pack) {
    if (pack || arguments.length === 2) for (var i = 0, l = from.length, ar; i < l; i++) {
        if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
        }
    }
    return to.concat(ar || Array.prototype.slice.call(from));
};
exports.__esModule = true;
exports.LazyStream = exports.Stream = void 0;
/*
 * A small stream implementation
 */
var Monad_1 = require("./Monad");
var SourcesCollectors_1 = require("./SourcesCollectors");
/**
 * A simple typescript based reimplementation of streams
 *
 * This is the early eval version
 * for a lazy eval version check, LazyStream, which is api compatible
 * to this implementation, however with the benefit of being able
 * to provide infinite data sources and generic data providers, the downside
 * is, it might be a tad slower in some situations
 */
var Stream = /** @class */ (function () {
    function Stream() {
        var value = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            value[_i] = arguments[_i];
        }
        this._limits = -1;
        this.pos = -1;
        this.value = value;
    }
    Stream.of = function () {
        var data = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            data[_i] = arguments[_i];
        }
        return new (Stream.bind.apply(Stream, __spreadArray([void 0], data, false)))();
    };
    Stream.ofAssoc = function (data) {
        return this.of.apply(this, Object.keys(data)).map(function (key) { return [key, data[key]]; });
    };
    Stream.ofDataSource = function (dataSource) {
        var value = [];
        while (dataSource.hasNext()) {
            value.push(dataSource.next());
        }
        return new (Stream.bind.apply(Stream, __spreadArray([void 0], value, false)))();
    };
    Stream.prototype.limits = function (end) {
        this._limits = end;
        return this;
    };
    /**
     * concat for streams, so that you can concat two streams together
     * @param toAppend
     */
    Stream.prototype.concat = function () {
        //let dataSource = new MultiStreamDatasource<T>(this, ...toAppend);
        //return Stream.ofDataSource<T>(dataSource);
        var toAppend = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            toAppend[_i] = arguments[_i];
        }
        return Stream.of.apply(Stream, __spreadArray([this], toAppend, false)).flatMap(function (item) { return item; });
    };
    Stream.prototype.onElem = function (fn) {
        for (var cnt = 0; cnt < this.value.length && (this._limits == -1 || cnt < this._limits); cnt++) {
            if (fn(this.value[cnt], cnt) === false) {
                break;
            }
        }
        return this;
    };
    Stream.prototype.each = function (fn) {
        this.onElem(fn);
        this.reset();
    };
    Stream.prototype.map = function (fn) {
        if (!fn) {
            fn = function (inval) { return inval; };
        }
        var res = [];
        this.each(function (item) {
            res.push(fn(item));
        });
        return new (Stream.bind.apply(Stream, __spreadArray([void 0], res, false)))();
    };
    /*
     * we need to implement it to fullfill the contract, although it is used only internally
     * all values are flattened when accessed anyway, so there is no need to call this methiod
     */
    Stream.prototype.flatMap = function (fn) {
        var ret = [];
        this.each(function (item) {
            var strmR = fn(item);
            ret = Array.isArray(strmR) ? ret.concat(strmR) : ret.concat.apply(ret, strmR.value);
        });
        return Stream.of.apply(Stream, ret);
    };
    Stream.prototype.filter = function (fn) {
        var res = [];
        this.each(function (data) {
            if (fn(data)) {
                res.push(data);
            }
        });
        return new (Stream.bind.apply(Stream, __spreadArray([void 0], res, false)))();
    };
    Stream.prototype.reduce = function (fn, startVal) {
        if (startVal === void 0) { startVal = null; }
        var offset = startVal != null ? 0 : 1;
        var val1 = startVal != null ? startVal : this.value.length ? this.value[0] : null;
        for (var cnt = offset; cnt < this.value.length && (this._limits == -1 || cnt < this._limits); cnt++) {
            val1 = fn(val1, this.value[cnt]);
        }
        this.reset();
        return Monad_1.Optional.fromNullable(val1);
    };
    Stream.prototype.first = function () {
        this.reset();
        return this.value && this.value.length ? Monad_1.Optional.fromNullable(this.value[0]) : Monad_1.Optional.absent;
    };
    Stream.prototype.last = function () {
        //could be done via reduce, but is faster this way
        var length = this._limits > 0 ? Math.min(this._limits, this.value.length) : this.value.length;
        this.reset();
        return Monad_1.Optional.fromNullable(length ? this.value[length - 1] : null);
    };
    Stream.prototype.anyMatch = function (fn) {
        for (var cnt = 0; cnt < this.value.length && (this._limits == -1 || cnt < this._limits); cnt++) {
            if (fn(this.value[cnt])) {
                return true;
            }
        }
        this.reset();
        return false;
    };
    Stream.prototype.allMatch = function (fn) {
        if (!this.value.length) {
            return false;
        }
        var matches = 0;
        for (var cnt = 0; cnt < this.value.length; cnt++) {
            if (fn(this.value[cnt])) {
                matches++;
            }
        }
        this.reset();
        return matches == this.value.length;
    };
    Stream.prototype.noneMatch = function (fn) {
        var matches = 0;
        for (var cnt = 0; cnt < this.value.length; cnt++) {
            if (!fn(this.value[cnt])) {
                matches++;
            }
        }
        this.reset();
        return matches == this.value.length;
    };
    Stream.prototype.sort = function (comparator) {
        var newArr = this.value.slice().sort(comparator);
        return Stream.of.apply(Stream, newArr);
    };
    Stream.prototype.collect = function (collector) {
        this.each(function (data) { return collector.collect(data); });
        this.reset();
        return collector.finalValue;
    };
    //-- internally exposed methods needed for the interconnectivity
    Stream.prototype.hasNext = function () {
        var isLimitsReached = this._limits != -1 && this.pos >= this._limits - 1;
        var isEndOfArray = this.pos >= this.value.length - 1;
        return !(isLimitsReached || isEndOfArray);
    };
    Stream.prototype.next = function () {
        if (!this.hasNext()) {
            return null;
        }
        this.pos++;
        return this.value[this.pos];
    };
    Stream.prototype.lookAhead = function (cnt) {
        if (cnt === void 0) { cnt = 1; }
        if ((this.pos + cnt) >= this.value.length) {
            return SourcesCollectors_1.ITERATION_STATUS.EO_STRM;
        }
        return this.value[this.pos + cnt];
    };
    Stream.prototype[Symbol.iterator] = function () {
        var _this = this;
        return {
            next: function () {
                var done = !_this.hasNext();
                var val = _this.next();
                return {
                    done: done,
                    value: val
                };
            }
        };
    };
    /*get observable(): Observable<T> {
        return from(this);
    }*/
    Stream.prototype.reset = function () {
        this.pos = -1;
    };
    return Stream;
}());
exports.Stream = Stream;
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
var LazyStream = /** @class */ (function () {
    function LazyStream(parent) {
        this._limits = -1;
        /*
         * needed to have the limits check working
         * we need to keep track of the current position
         * in the stream
         */
        this.pos = -1;
        this.dataSource = parent;
    }
    LazyStream.of = function () {
        var values = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            values[_i] = arguments[_i];
        }
        return new LazyStream(new (SourcesCollectors_1.ArrayStreamDataSource.bind.apply(SourcesCollectors_1.ArrayStreamDataSource, __spreadArray([void 0], values, false)))());
    };
    LazyStream.ofAssoc = function (data) {
        return this.of.apply(this, Object.keys(data)).map(function (key) { return [key, data[key]]; });
    };
    LazyStream.ofStreamDataSource = function (value) {
        return new LazyStream(value);
    };
    LazyStream.prototype.hasNext = function () {
        if (this.isOverLimits()) {
            return false;
        }
        return this.dataSource.hasNext();
    };
    LazyStream.prototype.next = function () {
        var next = this.dataSource.next();
        // @ts-ignore
        this.pos++;
        return next;
    };
    LazyStream.prototype.lookAhead = function (cnt) {
        if (cnt === void 0) { cnt = 1; }
        return this.dataSource.lookAhead(cnt);
    };
    LazyStream.prototype.current = function () {
        return this.dataSource.current();
    };
    LazyStream.prototype.reset = function () {
        this.dataSource.reset();
        this.pos = -1;
        this._limits = -1;
    };
    /**
     * concat for streams, so that you can concat two streams together
     * @param toAppend
     */
    LazyStream.prototype.concat = function () {
        var toAppend = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            toAppend[_i] = arguments[_i];
        }
        //this.dataSource =  new MultiStreamDatasource<T>(this, ... toAppend);
        //return this;
        return LazyStream.of.apply(LazyStream, __spreadArray([this], toAppend, false)).flatMap(function (item) { return item; });
    };
    LazyStream.prototype.nextFilter = function (fn) {
        if (this.hasNext()) {
            var newVal = this.next();
            if (!fn(newVal)) {
                return this.nextFilter(fn);
            }
            return newVal;
        }
        return null;
    };
    LazyStream.prototype.limits = function (max) {
        this._limits = max;
        return this;
    };
    //main stream methods
    LazyStream.prototype.collect = function (collector) {
        while (this.hasNext()) {
            var t = this.next();
            collector.collect(t);
        }
        this.reset();
        return collector.finalValue;
    };
    LazyStream.prototype.onElem = function (fn) {
        var _this = this;
        return new LazyStream(new SourcesCollectors_1.MappedStreamDataSource(function (el) {
            if (fn(el, _this.pos) === false) {
                _this.stop();
            }
            return el;
        }, this));
    };
    LazyStream.prototype.filter = function (fn) {
        return new LazyStream(new SourcesCollectors_1.FilteredStreamDatasource(fn, this));
    };
    LazyStream.prototype.map = function (fn) {
        return new LazyStream(new SourcesCollectors_1.MappedStreamDataSource(fn, this));
    };
    LazyStream.prototype.flatMap = function (fn) {
        return new LazyStream(new SourcesCollectors_1.FlatMapStreamDataSource(fn, this));
    };
    //endpoint
    LazyStream.prototype.each = function (fn) {
        while (this.hasNext()) {
            if (fn(this.next()) === false) {
                this.stop();
            }
        }
        this.reset();
    };
    LazyStream.prototype.reduce = function (fn, startVal) {
        if (startVal === void 0) { startVal = null; }
        if (!this.hasNext()) {
            return Monad_1.Optional.absent;
        }
        var value1;
        var value2 = null;
        if (startVal != null) {
            value1 = startVal;
            value2 = this.next();
        }
        else {
            value1 = this.next();
            if (!this.hasNext()) {
                return Monad_1.Optional.fromNullable(value1);
            }
            value2 = this.next();
        }
        value1 = fn(value1, value2);
        while (this.hasNext()) {
            value2 = this.next();
            value1 = fn(value1, value2);
        }
        this.reset();
        return Monad_1.Optional.fromNullable(value1);
    };
    LazyStream.prototype.last = function () {
        if (!this.hasNext()) {
            return Monad_1.Optional.absent;
        }
        return this.reduce(function (el1, el2) { return el2; });
    };
    LazyStream.prototype.first = function () {
        this.reset();
        if (!this.hasNext()) {
            return Monad_1.Optional.absent;
        }
        return Monad_1.Optional.fromNullable(this.next());
    };
    LazyStream.prototype.anyMatch = function (fn) {
        while (this.hasNext()) {
            if (fn(this.next())) {
                return true;
            }
        }
        return false;
    };
    LazyStream.prototype.allMatch = function (fn) {
        while (this.hasNext()) {
            if (!fn(this.next())) {
                return false;
            }
        }
        return true;
    };
    LazyStream.prototype.noneMatch = function (fn) {
        while (this.hasNext()) {
            if (fn(this.next())) {
                return false;
            }
        }
        return true;
    };
    LazyStream.prototype.sort = function (comparator) {
        var arr = this.collect(new SourcesCollectors_1.ArrayCollector());
        arr = arr.sort(comparator);
        return LazyStream.of.apply(LazyStream, arr);
    };
    Object.defineProperty(LazyStream.prototype, "value", {
        get: function () {
            return this.collect(new SourcesCollectors_1.ArrayCollector());
        },
        enumerable: false,
        configurable: true
    });
    LazyStream.prototype[Symbol.iterator] = function () {
        var _this = this;
        return {
            next: function () {
                var done = !_this.hasNext();
                var val = _this.next();
                return {
                    done: done,
                    value: val
                };
            }
        };
    };
    /*get observable(): Observable<T> {
        return from(this);
    }*/
    LazyStream.prototype.stop = function () {
        this.pos = this._limits + 1000000000;
        this._limits = 0;
    };
    LazyStream.prototype.isOverLimits = function () {
        return this._limits != -1 && this.pos >= this._limits - 1;
    };
    return LazyStream;
}());
exports.LazyStream = LazyStream;
//# sourceMappingURL=Stream.js.map