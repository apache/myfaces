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
var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (Object.prototype.hasOwnProperty.call(b, p)) d[p] = b[p]; };
        return extendStatics(d, b);
    };
    return function (d, b) {
        if (typeof b !== "function" && b !== null)
            throw new TypeError("Class extends value " + String(b) + " is not a constructor or null");
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
exports.__esModule = true;
exports.Config = exports.ValueEmbedder = exports.Optional = exports.Monad = void 0;
/**
 * A module which keeps  basic monadish like definitions in place without any sidedependencies to other modules.
 * Useful if you need the functions in another library to keep its dependencies down
 */
/*IMonad definitions*/
var Lang_1 = require("./Lang");
var SourcesCollectors_1 = require("./SourcesCollectors");
var Stream_1 = require("./Stream");
var objAssign = Lang_1.Lang.objAssign;
/**
 * Implementation of a monad
 * (Sideffect free), no write allowed directly on the monads
 * value state
 */
var Monad = /** @class */ (function () {
    function Monad(value) {
        this._value = value;
    }
    Object.defineProperty(Monad.prototype, "value", {
        get: function () {
            return this._value;
        },
        enumerable: false,
        configurable: true
    });
    Monad.prototype.map = function (fn) {
        if (!fn) {
            fn = function (inval) { return inval; };
        }
        var result = fn(this.value);
        return new Monad(result);
    };
    Monad.prototype.flatMap = function (fn) {
        var mapped = this.map(fn);
        while ((mapped === null || mapped === void 0 ? void 0 : mapped.value) instanceof Monad) {
            mapped = mapped.value;
        }
        return mapped;
    };
    return Monad;
}());
exports.Monad = Monad;
/**
 * optional implementation, an optional is basically an implementation of a Monad with additional syntactic
 * sugar on top
 * (Sideeffect free, since value assignment is not allowed)
 * */
var Optional = /** @class */ (function (_super) {
    __extends(Optional, _super);
    function Optional(value) {
        return _super.call(this, value) || this;
    }
    Object.defineProperty(Optional.prototype, "value", {
        get: function () {
            if (this._value instanceof Monad) {
                return this._value.flatMap().value;
            }
            return this._value;
        },
        enumerable: false,
        configurable: true
    });
    Optional.fromNullable = function (value) {
        return new Optional(value);
    };
    /*syntactic sugar for absent and present checks*/
    Optional.prototype.isAbsent = function () {
        return "undefined" == typeof this.value || null == this.value;
    };
    /**
     * any value present
     */
    Optional.prototype.isPresent = function (presentRunnable) {
        var absent = this.isAbsent();
        if (!absent && presentRunnable) {
            presentRunnable.call(this, this);
        }
        return !absent;
    };
    Optional.prototype.ifPresentLazy = function (presentRunnable) {
        if (presentRunnable === void 0) { presentRunnable = function () {
        }; }
        this.isPresent.call(this, presentRunnable);
        return this;
    };
    Optional.prototype.orElse = function (elseValue) {
        if (this.isPresent()) {
            return this;
        }
        else {
            //shortcut
            if (elseValue == null) {
                return Optional.absent;
            }
            return this.flatMap(function () { return elseValue; });
        }
    };
    /**
     * lazy, passes a function which then is lazily evaluated
     * instead of a direct value
     * @param func
     */
    Optional.prototype.orElseLazy = function (func) {
        if (this.isPresent()) {
            return this;
        }
        else {
            return this.flatMap(func);
        }
    };
    /*
     * we need to implement it to fullfill the contract, although it is used only internally
     * all values are flattened when accessed anyway, so there is no need to call this methiod
     */
    Optional.prototype.flatMap = function (fn) {
        var val = _super.prototype.flatMap.call(this, fn);
        if (!(val instanceof Optional)) {
            return Optional.fromNullable(val.value);
        }
        return val.flatMap();
    };
    /*
     * elvis operation, take care, if you use this you lose typesafety and refactoring
     * capabilites, unfortunately typesceript does not allow to have its own elvis operator
     * this is some syntactic sugar however which is quite useful*/
    Optional.prototype.getIf = function () {
        var key = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            key[_i] = arguments[_i];
        }
        var currentPos = this;
        for (var cnt = 0; cnt < key.length; cnt++) {
            var currKey = this.keyVal(key[cnt]);
            var arrPos = this.arrayIndex(key[cnt]);
            if (currKey === "" && arrPos >= 0) {
                currentPos = this.getClass().fromNullable(!(currentPos.value instanceof Array) ? null : (currentPos.value.length < arrPos ? null : currentPos.value[arrPos]));
                if (currentPos.isAbsent()) {
                    return currentPos;
                }
                continue;
            }
            else if (currKey && arrPos >= 0) {
                if (currentPos.getIfPresent(currKey).isAbsent()) {
                    return currentPos;
                }
                currentPos = (currentPos.getIfPresent(currKey).value instanceof Array) ? this.getClass().fromNullable(currentPos.getIfPresent(currKey).value[arrPos]) : this.getClass().absent;
                if (currentPos.isAbsent()) {
                    return currentPos;
                }
                continue;
            }
            else {
                currentPos = currentPos.getIfPresent(currKey);
            }
            if (currentPos.isAbsent()) {
                return currentPos;
            }
            else if (arrPos > -1) {
                currentPos = this.getClass().fromNullable(currentPos.value[arrPos]);
            }
        }
        var retVal = currentPos;
        return retVal;
    };
    /**
     * simple match, if the first order function call returns
     * true then there is a match, if the value is not present
     * it never matches
     *
     * @param fn the first order function performing the match
     */
    Optional.prototype.match = function (fn) {
        if (this.isAbsent()) {
            return false;
        }
        return fn(this.value);
    };
    /**
     * convenience function to flatmap the internal value
     * and replace it with a default in case of being absent
     *
     * @param defaultVal
     * @returns {Optional<any>}
     */
    Optional.prototype.get = function (defaultVal) {
        if (defaultVal === void 0) { defaultVal = Optional.absent; }
        if (this.isAbsent()) {
            return this.getClass().fromNullable(defaultVal).flatMap();
        }
        return this.getClass().fromNullable(this.value).flatMap();
    };
    Optional.prototype.toJson = function () {
        return JSON.stringify(this.value);
    };
    /**
     * helper to override several implementations in a more fluent way
     * by having a getClass operation we can avoid direct calls into the constructor or
     * static methods and do not have to implement several methods which rely on the type
     * of "this"
     * @returns {Monadish.Optional}
     */
    Optional.prototype.getClass = function () {
        return Optional;
    };
    /*helper method for getIf with array access aka <name>[<indexPos>]*/
    Optional.prototype.arrayIndex = function (key) {
        var start = key.indexOf("[");
        var end = key.indexOf("]");
        if (start >= 0 && end > 0 && start < end) {
            return parseInt(key.substring(start + 1, end));
        }
        else {
            return -1;
        }
    };
    /*helper method for getIf with array access aka <name>[<indexPos>]*/
    Optional.prototype.keyVal = function (key) {
        var start = key.indexOf("[");
        if (start >= 0) {
            return key.substring(0, start);
        }
        else {
            return key;
        }
    };
    /**
     * additional syntactic sugar which is not part of the usual optional implementation
     * but makes life easier, if you want to sacrifice typesafety and refactoring
     * capabilities in typescript
     */
    Optional.prototype.getIfPresent = function (key) {
        if (this.isAbsent()) {
            return this.getClass().absent;
        }
        return this.getClass().fromNullable(this.value[key]).flatMap();
    };
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
    Optional.prototype.resolve = function (resolver) {
        if (this.isAbsent()) {
            return Optional.absent;
        }
        try {
            return Optional.fromNullable(resolver(this.value));
        }
        catch (e) {
            return Optional.absent;
        }
    };
    /*default value for absent*/
    Optional.absent = Optional.fromNullable(null);
    return Optional;
}(Monad));
exports.Optional = Optional;
// --------------------- From here onwards we break out the sideffects free limits ------------
/**
 * ValueEmbedder is the writeable version
 * of optional, it basically is a wrappber
 * around a construct which has a state
 * and can be written to.
 *
 * For the readonly version see Optional
 */
var ValueEmbedder = /** @class */ (function (_super) {
    __extends(ValueEmbedder, _super);
    function ValueEmbedder(rootElem, valueKey) {
        if (valueKey === void 0) { valueKey = "value"; }
        var _this = _super.call(this, rootElem) || this;
        _this.key = valueKey;
        return _this;
    }
    Object.defineProperty(ValueEmbedder.prototype, "value", {
        get: function () {
            return this._value ? this._value[this.key] : null;
        },
        set: function (newVal) {
            if (!this._value) {
                return;
            }
            this._value[this.key] = newVal;
        },
        enumerable: false,
        configurable: true
    });
    ValueEmbedder.prototype.orElse = function (elseValue) {
        var alternative = {};
        alternative[this.key] = elseValue;
        return this.isPresent() ? this : new ValueEmbedder(alternative, this.key);
    };
    ValueEmbedder.prototype.orElseLazy = function (func) {
        if (this.isPresent()) {
            return this;
        }
        else {
            var alternative = {};
            alternative[this.key] = func();
            return new ValueEmbedder(alternative, this.key);
        }
    };
    /**
     * helper to override several implementations in a more fluent way
     * by having a getClass operation we can avoid direct calls into the constructor or
     * static methods and do not have to implement several methods which rely on the type
     * of "this"
     * @returns {Monadish.Optional}
     */
    ValueEmbedder.prototype.getClass = function () {
        return ValueEmbedder;
    };
    ValueEmbedder.fromNullable = function (value, valueKey) {
        if (valueKey === void 0) { valueKey = "value"; }
        return new ValueEmbedder(value, valueKey);
    };
    /*default value for absent*/
    ValueEmbedder.absent = ValueEmbedder.fromNullable(null);
    return ValueEmbedder;
}(Optional));
exports.ValueEmbedder = ValueEmbedder;
/**
 * specialized value embedder
 * for our Configuration
 */
var ConfigEntry = /** @class */ (function (_super) {
    __extends(ConfigEntry, _super);
    function ConfigEntry(rootElem, key, arrPos) {
        var _this = _super.call(this, rootElem, key) || this;
        _this.arrPos = arrPos !== null && arrPos !== void 0 ? arrPos : -1;
        return _this;
    }
    Object.defineProperty(ConfigEntry.prototype, "value", {
        get: function () {
            if (this.key == "" && this.arrPos >= 0) {
                return this._value[this.arrPos];
            }
            else if (this.key && this.arrPos >= 0) {
                return this._value[this.key][this.arrPos];
            }
            return this._value[this.key];
        },
        set: function (val) {
            if (this.key == "" && this.arrPos >= 0) {
                this._value[this.arrPos] = val;
                return;
            }
            else if (this.key && this.arrPos >= 0) {
                this._value[this.key][this.arrPos] = val;
                return;
            }
            this._value[this.key] = val;
        },
        enumerable: false,
        configurable: true
    });
    /*default value for absent*/
    ConfigEntry.absent = ConfigEntry.fromNullable(null);
    return ConfigEntry;
}(ValueEmbedder));
/**
 * Config, basically an optional wrapper for a json structure
 * (not sideeffect free, since we can alter the internal config state
 * without generating a new config), not sure if we should make it sideffect free
 * since this would swallow a lot of performane and ram
 */
var Config = /** @class */ (function (_super) {
    __extends(Config, _super);
    function Config(root) {
        return _super.call(this, root) || this;
    }
    Object.defineProperty(Config.prototype, "shallowCopy", {
        /**
         * shallow copy getter, copies only the first level, references the deeper nodes
         * in a shared manner
         */
        get: function () {
            return new Config(Stream_1.Stream.ofAssoc(this.value).collect(new SourcesCollectors_1.AssocArrayCollector()));
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(Config.prototype, "deepCopy", {
        /**
         * deep copy, copies all config nodes
         */
        get: function () {
            return new Config(objAssign({}, this.value));
        },
        enumerable: false,
        configurable: true
    });
    /**
     * creates a config from an initial value or null
     * @param value
     */
    Config.fromNullable = function (value) {
        return new Config(value);
    };
    /**
     * simple merge for the root configs
     */
    Config.prototype.shallowMerge = function (other, overwrite, withAppend) {
        var _this = this;
        if (overwrite === void 0) { overwrite = true; }
        if (withAppend === void 0) { withAppend = false; }
        var _loop_1 = function (key) {
            if (overwrite || !(key in this_1.value)) {
                if (!withAppend) {
                    this_1.assign(key).value = other.getIf(key).value;
                }
                else {
                    if (Array.isArray(other.getIf(key).value)) {
                        Stream_1.Stream.of.apply(Stream_1.Stream, other.getIf(key).value).each(function (item) { return _this.append(key).value = item; });
                    }
                    else {
                        this_1.append(key).value = other.getIf(key).value;
                    }
                }
            }
        };
        var this_1 = this;
        for (var key in other.value) {
            _loop_1(key);
        }
    };
    /**
     * assigns a single value as array, or appends it
     * to an existing value mapping a single value to array
     *
     *
     * usage myConfig.append("foobaz").value = "newValue"
     *       myConfig.append("foobaz").value = "newValue2"
     *
     * resulting in myConfig.foobaz == ["newValue, newValue2"]
     *
     * @param {string[]} accessPath
     */
    Config.prototype.append = function () {
        var accessPath = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            accessPath[_i] = arguments[_i];
        }
        var noKeys = accessPath.length < 1;
        if (noKeys) {
            return;
        }
        var lastKey = accessPath[accessPath.length - 1];
        var currKey, finalKey = this.keyVal(lastKey);
        var pathExists = this.getIf.apply(this, accessPath).isPresent();
        this.buildPath(accessPath);
        var finalKeyArrPos = this.arrayIndex(lastKey);
        if (finalKeyArrPos > -1) {
            throw Error("Append only possible on non array properties, use assign on indexed data");
        }
        var value = this.getIf.apply(this, accessPath).value;
        if (!Array.isArray(value)) {
            value = this.assign.apply(this, accessPath).value = [value];
        }
        if (pathExists) {
            value.push({});
        }
        finalKeyArrPos = value.length - 1;
        var retVal = new ConfigEntry(accessPath.length == 1 ? this.value : this.getIf.apply(this, accessPath.slice(0, accessPath.length - 1)).value, lastKey, finalKeyArrPos);
        return retVal;
    };
    /**
     * appends to an existing entry (or extends into an array and appends)
     * if the condition is met
     * @param {boolean} condition
     * @param {string[]} accessPath
     */
    Config.prototype.appendIf = function (condition) {
        var accessPath = [];
        for (var _i = 1; _i < arguments.length; _i++) {
            accessPath[_i - 1] = arguments[_i];
        }
        if (!condition) {
            return { value: null };
        }
        return this.append.apply(this, accessPath);
    };
    /**
     * assings an new value on the given access path
     * @param accessPath
     */
    Config.prototype.assign = function () {
        var accessPath = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            accessPath[_i] = arguments[_i];
        }
        if (accessPath.length < 1) {
            return;
        }
        this.buildPath(accessPath);
        var currKey = this.keyVal(accessPath[accessPath.length - 1]);
        var arrPos = this.arrayIndex(accessPath[accessPath.length - 1]);
        var retVal = new ConfigEntry(accessPath.length == 1 ? this.value : this.getIf.apply(this, accessPath.slice(0, accessPath.length - 1)).value, currKey, arrPos);
        return retVal;
    };
    /**
     * assign a value if the condition is set to true, otherwise skip it
     *
     * @param condition the condition, the access accessPath into the config
     * @param accessPath
     */
    Config.prototype.assignIf = function (condition) {
        var accessPath = [];
        for (var _i = 1; _i < arguments.length; _i++) {
            accessPath[_i - 1] = arguments[_i];
        }
        return condition ? this.assign.apply(this, accessPath) : { value: null };
    };
    /**
     * get if the access path is present (get is reserved as getter with a default, on the current path)
     * TODO will be renamed to something more meaningful and deprecated, the name is ambigous
     * @param accessPath the access path
     */
    Config.prototype.getIf = function () {
        var accessPath = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            accessPath[_i] = arguments[_i];
        }
        return this.getClass().fromNullable(_super.prototype.getIf.apply(this, accessPath).value);
    };
    /**
     * gets the current node and if none is present returns a config with a default value
     * @param defaultVal
     */
    Config.prototype.get = function (defaultVal) {
        return this.getClass().fromNullable(_super.prototype.get.call(this, defaultVal).value);
    };
    //empties the current config entry
    Config.prototype["delete"] = function (key) {
        if (key in this.value) {
            delete this.value[key];
        }
        return this;
    };
    /**
     * converts the entire config into a json object
     */
    Config.prototype.toJson = function () {
        return JSON.stringify(this.value);
    };
    Config.prototype.getClass = function () {
        return Config;
    };
    Config.prototype.setVal = function (val) {
        this._value = val;
    };
    /**
     * builds the config path
     *
     * @param accessPath a sequential array of accessPath containing either a key name or an array reference name[<index>]
     */
    Config.prototype.buildPath = function (accessPath) {
        var val = this;
        var parentVal = this.getClass().fromNullable(null);
        var parentPos = -1;
        var alloc = function (arr, length) {
            var length1 = arr.length;
            var length2 = length1 + length;
            for (var cnt = length1; cnt < length2; cnt++) {
                arr.push({});
            }
        };
        for (var cnt = 0; cnt < accessPath.length; cnt++) {
            var currKey = this.keyVal(accessPath[cnt]);
            var arrPos = this.arrayIndex(accessPath[cnt]);
            if (currKey === "" && arrPos >= 0) {
                val.setVal((val.value instanceof Array) ? val.value : []);
                alloc(val.value, arrPos + 1);
                if (parentPos >= 0) {
                    parentVal.value[parentPos] = val.value;
                }
                parentVal = val;
                parentPos = arrPos;
                val = this.getClass().fromNullable(val.value[arrPos]);
                continue;
            }
            var tempVal = val.getIf(currKey);
            if (arrPos == -1) {
                if (tempVal.isAbsent()) {
                    tempVal = this.getClass().fromNullable(val.value[currKey] = {});
                }
                else {
                    val = tempVal;
                }
            }
            else {
                var arr = (tempVal.value instanceof Array) ? tempVal.value : [];
                alloc(arr, arrPos + 1);
                val.value[currKey] = arr;
                tempVal = this.getClass().fromNullable(arr[arrPos]);
            }
            parentVal = val;
            parentPos = arrPos;
            val = tempVal;
        }
        return this;
    };
    return Config;
}(Optional));
exports.Config = Config;
//# sourceMappingURL=Monad.js.map