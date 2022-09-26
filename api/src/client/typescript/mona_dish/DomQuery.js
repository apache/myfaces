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
var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (_) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
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
exports.DQ = exports.DomQueryCollector = exports.DomQuery = exports.ElementAttribute = void 0;
var Monad_1 = require("./Monad");
var Stream_1 = require("./Stream");
var SourcesCollectors_1 = require("./SourcesCollectors");
var Lang_1 = require("./Lang");
var trim = Lang_1.Lang.trim;
var objToArray = Lang_1.Lang.objToArray;
var isString = Lang_1.Lang.isString;
var equalsIgnoreCase = Lang_1.Lang.equalsIgnoreCase;
/**
 *
 *        // - submit checkboxes and radio inputs only if checked
 if ((tagName != "select" && elemType != "button"
 && elemType != "reset" && elemType != "submit" && elemType != "image")
 && ((elemType != "checkbox" && elemType != "radio"
 */
var Submittables;
(function (Submittables) {
    Submittables["SELECT"] = "select";
    Submittables["BUTTON"] = "button";
    Submittables["SUBMIT"] = "submit";
    Submittables["RESET"] = "reset";
    Submittables["IMAGE"] = "image";
    Submittables["RADIO"] = "radio";
    Submittables["CHECKBOX"] = "checkbox";
})(Submittables || (Submittables = {}));
/**
 * helper to fix a common problem that a system has to wait until a certain condition is reached
 * depening on the browser this uses either the mutation observer or a semi compatible interval as fallback
 * @param condition
 */
function waitUntilDom(root, condition, options) {
    if (options === void 0) { options = { attributes: true, childList: true, subtree: true, timeout: 500, interval: 100 }; }
    var ret = new Promise(function (success, error) {
        var MUT_ERROR = new Error("Mutation observer timeout");
        if ('undefined' != typeof window.MutationObserver) {
            var mutTimeout_1 = setTimeout(function () {
                return error(MUT_ERROR);
            }, options.timeout);
            var callback = function (mutationList, observer) {
                var found = new DomQuery(mutationList.map(function (mut) { return mut.target; })).first(condition);
                if (found.isPresent()) {
                    clearTimeout(mutTimeout_1);
                    success(found);
                }
            };
            var observer_1 = new window.MutationObserver(callback);
            // browsers might ignore it, but we cannot break the api in the case
            // hence no timeout is passed
            var observableOpts_1 = __assign({}, options);
            delete observableOpts_1.timeout;
            root.eachElem(function (item) {
                observer_1.observe(item, observableOpts_1);
            });
        }
        else { //fallback for legacy browsers without mutation observer
            //we do the same but for now ignore the options on the dom query
            var interval_1 = setInterval(function () {
                var found = null;
                if (options.childList) {
                    found = (condition(root)) ? root : root.childNodes.first(condition);
                }
                else if (options.subtree) {
                    found = (condition(root)) ? root : root.querySelectorAll(" * ").first(condition);
                }
                else {
                    found = (condition(root)) ? root : DomQuery.absent;
                }
                if (found.isPresent()) {
                    if (timeout_1) {
                        clearTimeout(timeout_1);
                        clearInterval(interval_1);
                        interval_1 = null;
                        success(found);
                    }
                }
            }, options.interval);
            var timeout_1 = setTimeout(function () {
                if (interval_1) {
                    clearInterval(interval_1);
                    error(MUT_ERROR);
                }
            }, options.timeout);
        }
    });
    return ret;
}
var ElementAttribute = /** @class */ (function (_super) {
    __extends(ElementAttribute, _super);
    function ElementAttribute(element, name, defaultVal) {
        if (defaultVal === void 0) { defaultVal = null; }
        var _this = _super.call(this, element, name) || this;
        _this.element = element;
        _this.name = name;
        _this.defaultVal = defaultVal;
        return _this;
    }
    Object.defineProperty(ElementAttribute.prototype, "value", {
        get: function () {
            var _a;
            var val = (_a = this.element.get(0)).orElse.apply(_a, []).values;
            if (!val.length) {
                return this.defaultVal;
            }
            return val[0].getAttribute(this.name);
        },
        set: function (value) {
            var _a;
            var val = (_a = this.element.get(0)).orElse.apply(_a, []).values;
            for (var cnt = 0; cnt < val.length; cnt++) {
                val[cnt].setAttribute(this.name, value);
            }
            val[0].setAttribute(this.name, value);
        },
        enumerable: false,
        configurable: true
    });
    ElementAttribute.prototype.getClass = function () {
        return ElementAttribute;
    };
    ElementAttribute.fromNullable = function (value, valueKey) {
        if (valueKey === void 0) { valueKey = "value"; }
        return new ElementAttribute(value, valueKey);
    };
    return ElementAttribute;
}(Monad_1.ValueEmbedder));
exports.ElementAttribute = ElementAttribute;
/**
 * small helper for the specialized jsf case
 * @param src
 * @constructor
 */
var DEFAULT_WHITELIST = function (src) {
    return true;
};
/**
 * Monadic DomNode representation, ala jquery
 * This is a thin wrapper over querySelectorAll
 * to get slim monadic support
 * to reduce implementation code on the users side.
 * This is vital for frameworks which want to rely on
 * plain dom but still do not want to lose
 * the reduced code footprint of querying dom trees and traversing
 * by using functional patterns.
 *
 * Also a few convenience methods are added to reduce
 * the code footprint of standard dom processing
 * operations like eval
 *
 * TODO add jquery fallback support, since it is supported
 * in most older systems
 * Note parts of this code still stem from the Dom.js I have written 10 years
 * ago, those parts look a little bit ancient and will be replaced over time.
 *
 */
var DomQuery = /** @class */ (function () {
    function DomQuery() {
        var _a;
        var rootNode = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            rootNode[_i] = arguments[_i];
        }
        this.rootNode = [];
        this.pos = -1;
        //TODO this part probably will be removed
        //because we can stream from an array stream directly into the dom query
        this._limits = -1;
        if (Monad_1.Optional.fromNullable(rootNode).isAbsent() || !rootNode.length) {
            return;
        }
        else {
            //we need to flatten out the arrays
            for (var cnt = 0; cnt < rootNode.length; cnt++) {
                if (!rootNode[cnt]) {
                    //we skip possible null entries which can happen in
                    //certain corner conditions due to the constructor re-wrapping single elements into arrays.
                    continue;
                }
                else if (isString(rootNode[cnt])) {
                    var foundElement = DomQuery.querySelectorAll(rootNode[cnt]);
                    if (!foundElement.isAbsent()) {
                        rootNode.push.apply(rootNode, foundElement.values);
                    }
                }
                else if (rootNode[cnt] instanceof DomQuery) {
                    (_a = this.rootNode).push.apply(_a, rootNode[cnt].values);
                }
                else {
                    this.rootNode.push(rootNode[cnt]);
                }
            }
        }
    }
    Object.defineProperty(DomQuery.prototype, "value", {
        /**
         * returns the first element
         */
        get: function () {
            return this.getAsElem(0);
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DomQuery.prototype, "values", {
        get: function () {
            return this.allElems();
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DomQuery.prototype, "id", {
        /**
         * returns the id of the first element
         */
        get: function () {
            return new ElementAttribute(this.get(0), "id");
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DomQuery.prototype, "length", {
        /**
         * length of the entire query set
         */
        get: function () {
            return this.rootNode.length;
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DomQuery.prototype, "tagName", {
        /**
         * convenience method for tagName
         */
        get: function () {
            return this.getAsElem(0).getIf("tagName");
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DomQuery.prototype, "nodeName", {
        /**
         * convenience method for nodeName
         */
        get: function () {
            return this.getAsElem(0).getIf("nodeName");
        },
        enumerable: false,
        configurable: true
    });
    DomQuery.prototype.isTag = function (tagName) {
        return !this.isAbsent()
            && (this.nodeName.orElse("__none___")
                .value.toLowerCase() == tagName.toLowerCase()
                || this.tagName.orElse("__none___")
                    .value.toLowerCase() == tagName.toLowerCase());
    };
    Object.defineProperty(DomQuery.prototype, "type", {
        /**
         * convenience property for type
         *
         * returns null in case of no type existing otherwise
         * the type of the first element
         */
        get: function () {
            return this.getAsElem(0).getIf("type");
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DomQuery.prototype, "name", {
        /**
         * convenience property for name
         *
         * returns null in case of no type existing otherwise
         * the name of the first element
         */
        get: function () {
            return new Monad_1.ValueEmbedder(this.getAsElem(0).value, "name");
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DomQuery.prototype, "inputValue", {
        /**
         * convenience property for value
         *
         * returns null in case of no type existing otherwise
         * the value of the first element
         */
        get: function () {
            if (this.getAsElem(0).getIf("value").isPresent()) {
                return new Monad_1.ValueEmbedder(this.getAsElem(0).value);
            }
            else {
                return Monad_1.ValueEmbedder.absent;
            }
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DomQuery.prototype, "checked", {
        get: function () {
            return Stream_1.Stream.of.apply(Stream_1.Stream, this.values).allMatch(function (el) { return !!el.checked; });
        },
        set: function (newChecked) {
            this.eachElem(function (el) { return el.checked = newChecked; });
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DomQuery.prototype, "elements", {
        get: function () {
            //a simple querySelectorAll should suffice
            return this.querySelectorAll("input, checkbox, select, textarea, fieldset");
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DomQuery.prototype, "deepElements", {
        get: function () {
            var elemStr = "input, select, textarea, checkbox, fieldset";
            return this.querySelectorAllDeep(elemStr);
        },
        enumerable: false,
        configurable: true
    });
    /**
     * a deep search which treats the single isolated shadow doms
     * separately and runs the query on earch shadow dom
     * @param queryStr
     */
    DomQuery.prototype.querySelectorAllDeep = function (queryStr) {
        var found = [];
        var queryRes = this.querySelectorAll(queryStr);
        if (queryRes.length) {
            found.push(queryRes);
        }
        var shadowRoots = this.querySelectorAll("*").shadowRoot;
        if (shadowRoots.length) {
            var shadowRes = shadowRoots.querySelectorAllDeep(queryStr);
            if (shadowRes.length) {
                found.push(shadowRes);
            }
        }
        return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], found, false)))();
    };
    Object.defineProperty(DomQuery.prototype, "disabled", {
        /**
         * todo align this api with the rest of the apis
         */
        get: function () {
            return this.attr("disabled").isPresent();
        },
        set: function (disabled) {
            // this.attr("disabled").value = disabled + "";
            if (!disabled) {
                this.removeAttribute("disabled");
            }
            else {
                this.attr("disabled").value = "disabled";
            }
        },
        enumerable: false,
        configurable: true
    });
    DomQuery.prototype.removeAttribute = function (name) {
        this.eachElem(function (item) { return item.removeAttribute(name); });
    };
    Object.defineProperty(DomQuery.prototype, "childNodes", {
        get: function () {
            var childNodeArr = [];
            this.eachElem(function (item) {
                childNodeArr = childNodeArr.concat(objToArray(item.childNodes));
            });
            return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], childNodeArr, false)))();
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DomQuery.prototype, "stream", {
        /**
         * binding into stream
         */
        get: function () {
            return new (Stream_1.Stream.bind.apply(Stream_1.Stream, __spreadArray([void 0], this.asArray, false)))();
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DomQuery.prototype, "lazyStream", {
        /**
         * fetches a lazy stream representation
         * lazy should be applied if you have some filters etc
         * in between, this can reduce the number of post filter operations
         * and ram usage
         * significantly because the operations are done lazily and stop
         * once they hit a dead end.
         */
        get: function () {
            return Stream_1.LazyStream.of.apply(Stream_1.LazyStream, this.asArray);
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DomQuery.prototype, "asArray", {
        get: function () {
            //filter not supported by IE11
            return [].concat(Stream_1.LazyStream.of.apply(Stream_1.LazyStream, this.rootNode).filter(function (item) {
                return item != null;
            })
                .map(function (item) {
                return DomQuery.byId(item);
            }).collect(new SourcesCollectors_1.ArrayCollector()));
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DomQuery.prototype, "asNodeArray", {
        get: function () {
            return [].concat(Stream_1.Stream.of(this.rootNode).filter(function (item) { return item != null; }).collect(new SourcesCollectors_1.ArrayCollector()));
        },
        enumerable: false,
        configurable: true
    });
    DomQuery.querySelectorAllDeep = function (selector) {
        return new DomQuery(document).querySelectorAllDeep(selector);
    };
    /**
     * easy query selector all producer
     *
     * @param selector the selector
     * @returns a results dom query object
     */
    DomQuery.querySelectorAll = function (selector) {
        if (selector.indexOf("/shadow/") != -1) {
            return new DomQuery(document)._querySelectorAllDeep(selector);
        }
        else {
            return new DomQuery(document)._querySelectorAll(selector);
        }
    };
    /**
     * byId producer
     *
     * @param selector id
     * @return a DomQuery containing the found elements
     */
    DomQuery.byId = function (selector, deep) {
        if (deep === void 0) { deep = false; }
        if (isString(selector)) {
            return (!deep) ? new DomQuery(document).byId(selector) : new DomQuery(document).byIdDeep(selector);
        }
        else {
            return new DomQuery(selector);
        }
    };
    /**
     * byTagName producer
     *
     * @param selector name
     * @return a DomQuery containing the found elements
     */
    DomQuery.byTagName = function (selector) {
        if (isString(selector)) {
            return new DomQuery(document).byTagName(selector);
        }
        else {
            return new DomQuery(selector);
        }
    };
    DomQuery.globalEval = function (code, nonce) {
        return new DomQuery(document).globalEval(code, nonce);
    };
    /**
     * builds the ie nodes properly in a placeholder
     * and bypasses a non script insert bug that way
     * @param markup the marku code
     */
    DomQuery.fromMarkup = function (markup) {
        //https://developer.mozilla.org/de/docs/Web/API/DOMParser license creative commons
        var doc = document.implementation.createHTMLDocument("");
        markup = trim(markup);
        var lowerMarkup = markup.toLowerCase();
        if (lowerMarkup.indexOf('<!doctype') != -1 ||
            lowerMarkup.indexOf('<html') != -1 ||
            lowerMarkup.indexOf('<head') != -1 || //TODO proper regexps here to avoid embedded tags with same element names to be triggered
            lowerMarkup.indexOf('<body') != -1) {
            doc.documentElement.innerHTML = markup;
            return new DomQuery(doc.documentElement);
        }
        else {
            var startsWithTag = function (str, tagName) {
                var tag1 = ["<", tagName, ">"].join("");
                var tag2 = ["<", tagName, " "].join("");
                return (str.indexOf(tag1) == 0) || (str.indexOf(tag2) == 0);
            };
            var dummyPlaceHolder = new DomQuery(document.createElement("div"));
            //table needs special treatment due to the browsers auto creation
            if (startsWithTag(lowerMarkup, "thead") || startsWithTag(lowerMarkup, "tbody")) {
                dummyPlaceHolder.html("<table>".concat(markup, "</table>"));
                return dummyPlaceHolder.querySelectorAll("table").get(0).childNodes.detach();
            }
            else if (startsWithTag(lowerMarkup, "tfoot")) {
                dummyPlaceHolder.html("<table><thead></thead><tbody><tbody".concat(markup, "</table>"));
                return dummyPlaceHolder.querySelectorAll("table").get(2).childNodes.detach();
            }
            else if (startsWithTag(lowerMarkup, "tr")) {
                dummyPlaceHolder.html("<table><tbody>".concat(markup, "</tbody></table>"));
                return dummyPlaceHolder.querySelectorAll("tbody").get(0).childNodes.detach();
            }
            else if (startsWithTag(lowerMarkup, "td")) {
                dummyPlaceHolder.html("<table><tbody><tr>".concat(markup, "</tr></tbody></table>"));
                return dummyPlaceHolder.querySelectorAll("tr").get(0).childNodes.detach();
            }
            dummyPlaceHolder.html(markup);
            return dummyPlaceHolder.childNodes.detach();
        }
    };
    /**
     * returns the nth element as domquery
     * from the internal elements
     * note if you try to reach a non existing element position
     * you will get back an absent entry
     *
     * @param index the nth index
     */
    DomQuery.prototype.get = function (index) {
        return (index < this.rootNode.length) ? new DomQuery(this.rootNode[index]) : DomQuery.absent;
    };
    /**
     * returns the nth element as optional of an Element object
     * @param index the number from the index
     * @param defaults the default value if the index is overrun default Optional.absent
     */
    DomQuery.prototype.getAsElem = function (index, defaults) {
        if (defaults === void 0) { defaults = Monad_1.Optional.absent; }
        return (index < this.rootNode.length) ? Monad_1.Optional.fromNullable(this.rootNode[index]) : defaults;
    };
    /**
     * returns the files from a given elmement
     * @param index
     */
    DomQuery.prototype.filesFromElem = function (index) {
        var _a;
        return (index < this.rootNode.length) ? ((_a = this.rootNode[index]) === null || _a === void 0 ? void 0 : _a.files) ? this.rootNode[index].files : [] : [];
    };
    /**
     * returns the value array< of all elements
     */
    DomQuery.prototype.allElems = function () {
        return this.rootNode;
    };
    /**
     * absent no values reached?
     */
    DomQuery.prototype.isAbsent = function () {
        return this.length == 0;
    };
    /**
     * should make the code clearer
     * note if you pass a function
     * this refers to the active dopmquery object
     */
    DomQuery.prototype.isPresent = function (presentRunnable) {
        var absent = this.isAbsent();
        if (!absent && presentRunnable) {
            presentRunnable.call(this, this);
        }
        return !absent;
    };
    /**
     * should make the code clearer
     * note if you pass a function
     * this refers to the active dopmquery object
     *
     *
     * @param presentRunnable
     */
    DomQuery.prototype.ifPresentLazy = function (presentRunnable) {
        if (presentRunnable === void 0) { presentRunnable = function () {
        }; }
        this.isPresent.call(this, presentRunnable);
        return this;
    };
    /**
     * remove all affected nodes from this query object from the dom tree
     */
    DomQuery.prototype["delete"] = function () {
        this.eachElem(function (node) {
            if (node.parentNode) {
                node.parentNode.removeChild(node);
            }
        });
    };
    DomQuery.prototype.querySelectorAll = function (selector) {
        //We could merge both methods, but for now this is more readable
        if (selector.indexOf("/shadow/") != -1) {
            return this._querySelectorAllDeep(selector);
        }
        else {
            return this._querySelectorAll(selector);
        }
    };
    /**
     * query selector all on the existing dom queryX object
     *
     * @param selector the standard selector
     * @return a DomQuery with the results
     */
    DomQuery.prototype._querySelectorAll = function (selector) {
        var _a, _b;
        if (!((_a = this === null || this === void 0 ? void 0 : this.rootNode) === null || _a === void 0 ? void 0 : _a.length)) {
            return this;
        }
        var nodes = [];
        for (var cnt = 0; cnt < this.rootNode.length; cnt++) {
            if (!((_b = this.rootNode[cnt]) === null || _b === void 0 ? void 0 : _b.querySelectorAll)) {
                continue;
            }
            var res = this.rootNode[cnt].querySelectorAll(selector);
            nodes = nodes.concat(objToArray(res));
        }
        return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], nodes, false)))();
    };
    /*deep with a selector and a peudo /shadow/ marker to break into the next level*/
    DomQuery.prototype._querySelectorAllDeep = function (selector) {
        var _a;
        if (!((_a = this === null || this === void 0 ? void 0 : this.rootNode) === null || _a === void 0 ? void 0 : _a.length)) {
            return this;
        }
        var nodes = [];
        var foundNodes = new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], this.rootNode, false)))();
        var selectors = selector.split(/\/shadow\//);
        for (var cnt2 = 0; cnt2 < selectors.length; cnt2++) {
            if (selectors[cnt2] == "") {
                continue;
            }
            var levelSelector = selectors[cnt2];
            foundNodes = foundNodes.querySelectorAll(levelSelector);
            if (cnt2 < selectors.length - 1) {
                foundNodes = foundNodes.shadowRoot;
            }
        }
        return foundNodes;
    };
    /**
     * core byId method
     * @param id the id to search for
     * @param includeRoot also match the root element?
     */
    DomQuery.prototype.byId = function (id, includeRoot) {
        var res = [];
        if (includeRoot) {
            res = res.concat(Stream_1.LazyStream.of.apply(Stream_1.LazyStream, ((this === null || this === void 0 ? void 0 : this.rootNode) || [])).filter(function (item) { return id == item.id; })
                .map(function (item) { return new DomQuery(item); })
                .collect(new SourcesCollectors_1.ArrayCollector()));
        }
        //for some strange kind of reason the # selector fails
        //on hidden elements we use the attributes match selector
        //that works
        res = res.concat(this.querySelectorAll("[id=\"".concat(id, "\"]")));
        return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], res, false)))();
    };
    DomQuery.prototype.byIdDeep = function (id, includeRoot) {
        var res = [];
        if (includeRoot) {
            res = res.concat(Stream_1.LazyStream.of.apply(Stream_1.LazyStream, ((this === null || this === void 0 ? void 0 : this.rootNode) || [])).filter(function (item) { return id == item.id; })
                .map(function (item) { return new DomQuery(item); })
                .collect(new SourcesCollectors_1.ArrayCollector()));
        }
        var subItems = this.querySelectorAllDeep("[id=\"".concat(id, "\"]"));
        if (subItems.length) {
            res.push(subItems);
        }
        return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], res, false)))();
    };
    /**
     * same as byId just for the tag name
     * @param tagName
     * @param includeRoot
     */
    DomQuery.prototype.byTagName = function (tagName, includeRoot, deep) {
        var _a;
        var res = [];
        if (includeRoot) {
            res = Stream_1.LazyStream.of.apply(Stream_1.LazyStream, ((_a = this === null || this === void 0 ? void 0 : this.rootNode) !== null && _a !== void 0 ? _a : [])).filter(function (element) { return (element === null || element === void 0 ? void 0 : element.tagName) == tagName; })
                .reduce(function (reduction, item) { return reduction.concat([item]); }, res)
                .orElse(res).value;
        }
        (deep) ? res.push(this.querySelectorAllDeep(tagName)) : res.push(this.querySelectorAll(tagName));
        return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], res, false)))();
    };
    /**
     * attr accessor, usage myQuery.attr("class").value = "bla"
     * or let value myQuery.attr("class").value
     * @param attr the attribute to set
     * @param defaultValue the default value in case nothing is presented (defaults to null)
     */
    DomQuery.prototype.attr = function (attr, defaultValue) {
        if (defaultValue === void 0) { defaultValue = null; }
        return new ElementAttribute(this, attr, defaultValue);
    };
    /**
     * hasclass, checks for an existing class in the class attributes
     *
     * @param clazz the class to search for
     */
    DomQuery.prototype.hasClass = function (clazz) {
        var hasIt = false;
        this.eachElem(function (node) {
            hasIt = node.classList.contains(clazz);
            if (hasIt) {
                return false;
            }
        });
        return hasIt;
    };
    /**
     * appends a class string if not already in the element(s)
     *
     * @param clazz the style class to append
     */
    DomQuery.prototype.addClass = function (clazz) {
        this.eachElem(function (item) { return item.classList.add(clazz); });
        return this;
    };
    /**
     * remove the style class if in the class definitions
     *
     * @param clazz
     */
    DomQuery.prototype.removeClass = function (clazz) {
        this.eachElem(function (item) { return item.classList.remove(clazz); });
        return this;
    };
    /**
     * checks whether we have a multipart element in our children
     * or are one
     */
    DomQuery.prototype.isMultipartCandidate = function (deep) {
        var _this = this;
        if (deep === void 0) { deep = false; }
        var isCandidate = function (item) {
            var _a;
            if (item.length == 0) {
                return false;
            }
            if (item.length == 1) {
                if (item.tagName.get("booga").value.toLowerCase() == "input" &&
                    (((_a = item.attr("type")) === null || _a === void 0 ? void 0 : _a.value) || "").toLowerCase() == "file") {
                    return true;
                }
                if (deep) {
                    return _this.querySelectorAllDeep("input[type='file']").firstElem().isPresent();
                }
                else {
                    return _this.querySelectorAll("input[type='file']").firstElem().isPresent();
                }
            }
            return item.isMultipartCandidate(deep);
        };
        var ret = this.stream.filter(function (item) { return isCandidate(item); }).first().isPresent();
        return ret;
    };
    /**
     * innerHtml equivalkent
     * equivalent to jqueries html
     * as setter the html is set and the
     * DomQuery is given back
     * as getter the html string is returned
     *
     * @param inval
     */
    DomQuery.prototype.html = function (inval) {
        if (Monad_1.Optional.fromNullable(inval).isAbsent()) {
            return this.isPresent() ? Monad_1.Optional.fromNullable(this.innerHtml) : Monad_1.Optional.absent;
        }
        this.innerHtml = inval;
        return this;
    };
    /**
     * Standard dispatch event method, delegated from node
     */
    DomQuery.prototype.dispatchEvent = function (evt) {
        this.eachElem(function (elem) { return elem.dispatchEvent(evt); });
        return this;
    };
    Object.defineProperty(DomQuery.prototype, "innerHtml", {
        get: function () {
            var retArr = [];
            this.eachElem(function (elem) { return retArr.push(elem.innerHTML); });
            return retArr.join("");
        },
        set: function (inVal) {
            this.eachElem(function (elem) { return elem.innerHTML = inVal; });
        },
        enumerable: false,
        configurable: true
    });
    //source: https://developer.mozilla.org/en-US/docs/Web/API/Element/matches
    //code snippet license: https://creativecommons.org/licenses/by-sa/2.5/
    DomQuery.prototype._mozMatchesSelector = function (toMatch, selector) {
        var prot = toMatch;
        var matchesSelector = prot.matches ||
            prot.matchesSelector ||
            prot.mozMatchesSelector ||
            prot.msMatchesSelector ||
            prot.oMatchesSelector ||
            prot.webkitMatchesSelector ||
            function (s) {
                var matches = (document || window.ownerDocument).querySelectorAll(s), i = matches.length;
                while (--i >= 0 && matches.item(i) !== toMatch) {
                }
                return i > -1;
            };
        return matchesSelector.call(toMatch, selector);
    };
    /**
     * filters the current dom query elements
     * upon a given selector
     *
     * @param selector
     */
    DomQuery.prototype.filterSelector = function (selector) {
        var _this = this;
        var matched = [];
        this.eachElem(function (item) {
            if (_this._mozMatchesSelector(item, selector)) {
                matched.push(item);
            }
        });
        return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], matched, false)))();
    };
    /**
     * checks whether any item in this domQuery level matches the selector
     * if there is one element only attached, as root the match is only
     * performed on this element.
     * @param selector
     */
    DomQuery.prototype.matchesSelector = function (selector) {
        var _this = this;
        var ret = this.lazyStream
            .map(function (item) { return _this._mozMatchesSelector(item.getAsElem(0).value, selector); })
            .filter(function (match) { return match; })
            .first();
        return ret.isPresent();
    };
    /**
     * easy node traversal, you can pass
     * a set of node selectors which are joined as direct childs
     *
     * not the rootnodes are not in the getIf, those are always the child nodes
     *
     * @param nodeSelector
     */
    DomQuery.prototype.getIf = function () {
        var nodeSelector = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            nodeSelector[_i] = arguments[_i];
        }
        var selectorStage = this.childNodes;
        for (var cnt = 0; cnt < nodeSelector.length; cnt++) {
            selectorStage = selectorStage.filterSelector(nodeSelector[cnt]);
            if (selectorStage.isAbsent()) {
                return selectorStage;
            }
        }
        return selectorStage;
    };
    DomQuery.prototype.eachElem = function (func) {
        for (var cnt = 0, len = this.rootNode.length; cnt < len; cnt++) {
            if (func(this.rootNode[cnt], cnt) === false) {
                break;
            }
        }
        return this;
    };
    DomQuery.prototype.firstElem = function (func) {
        if (func === void 0) { func = function (item) { return item; }; }
        if (this.rootNode.length > 1) {
            func(this.rootNode[0], 0);
        }
        return this;
    };
    DomQuery.prototype.each = function (func) {
        Stream_1.Stream.of.apply(Stream_1.Stream, this.rootNode).each(function (item, cnt) {
            //we could use a filter, but for the best performance we dont
            if (item == null) {
                return;
            }
            return func(DomQuery.byId(item), cnt);
        });
        return this;
    };
    /**
     * returns a new dom query containing only the first element max
     *
     * @param func a an optional callback function to perform an operation on the first element
     */
    DomQuery.prototype.first = function (func) {
        if (func === void 0) { func = function (item) { return item; }; }
        if (this.rootNode.length >= 1) {
            func(this.get(0), 0);
            return this.get(0);
        }
        return this;
    };
    /**
     * filter function which filters a subset
     *
     * @param func
     */
    DomQuery.prototype.filter = function (func) {
        var reArr = [];
        this.each(function (item) {
            func(item) ? reArr.push(item) : null;
        });
        return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], reArr, false)))();
    };
    //TODO append prepend
    /**
     * global eval head appendix method
     * no other methods are supported anymore
     * @param code the code to be evaled
     * @param  nonce optional  nonce key for higher security
     */
    DomQuery.prototype.globalEval = function (code, nonce) {
        var head = document.getElementsByTagName("head")[0] || document.documentElement;
        var script = document.createElement("script");
        if (nonce) {
            script.setAttribute("nonce", nonce);
        }
        script.type = "text/javascript";
        script.innerHTML = code;
        var newScriptElement = head.appendChild(script);
        head.removeChild(newScriptElement);
        return this;
    };
    /**
     * detaches a set of nodes from their parent elements
     * in a browser independend manner
     * @param {Object} items the items which need to be detached
     * @return {Array} an array of nodes with the detached dom nodes
     */
    DomQuery.prototype.detach = function () {
        this.eachElem(function (item) {
            item.parentNode.removeChild(item);
        });
        return this;
    };
    /**
     * appends the current set of elements
     * to the element or first element passed via elem
     * @param elem
     */
    DomQuery.prototype.appendTo = function (elem) {
        this.eachElem(function (item) {
            var value1 = elem.getAsElem(0).orElseLazy(function () {
                return {
                    appendChild: function (theItem) {
                    }
                };
            }).value;
            value1.appendChild(item);
        });
    };
    /**
     * loads and evals a script from a source uri
     *
     * @param src the source to be loaded and evaled
     * @param defer in miliseconds execution default (0 == no defer)
     * @param charSet
     */
    DomQuery.prototype.loadScriptEval = function (src, defer, charSet) {
        var _this = this;
        if (defer === void 0) { defer = 0; }
        if (charSet === void 0) { charSet = "utf-8"; }
        var xhr = new XMLHttpRequest();
        xhr.open("GET", src, false);
        if (charSet) {
            xhr.setRequestHeader("Content-Type", "application/x-javascript; charset:" + charSet);
        }
        xhr.send(null);
        xhr.onload = function (responseData) {
            //defer also means we have to process after the ajax response
            //has been processed
            //we can achieve that with a small timeout, the timeout
            //triggers after the processing is done!
            if (!defer) {
                _this.globalEval(xhr.responseText.replace(/\n/g, "\r\n") + "\r\n//@ sourceURL=" + src);
            }
            else {
                //TODO not ideal we maybe ought to move to something else here
                //but since it is not in use yet, it is ok
                setTimeout(function () {
                    _this.globalEval(xhr.responseText + "\r\n//@ sourceURL=" + src);
                }, defer);
            }
        };
        xhr.onerror = function (data) {
            throw Error(data);
        };
        //since we are synchronous we do it after not with onReadyStateChange
        return this;
    };
    DomQuery.prototype.insertAfter = function () {
        var toInsertParams = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            toInsertParams[_i] = arguments[_i];
        }
        this.each(function (existingItem) {
            var existingElement = existingItem.getAsElem(0).value;
            var rootNode = existingElement.parentNode;
            var _loop_1 = function (cnt) {
                var nextSibling = existingElement.nextSibling;
                toInsertParams[cnt].eachElem(function (insertElem) {
                    if (nextSibling) {
                        rootNode.insertBefore(insertElem, nextSibling);
                        existingElement = nextSibling;
                    }
                    else {
                        rootNode.appendChild(insertElem);
                    }
                });
            };
            for (var cnt = 0; cnt < toInsertParams.length; cnt++) {
                _loop_1(cnt);
            }
        });
        var res = [];
        res.push(this);
        res = res.concat(toInsertParams);
        return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], res, false)))();
    };
    DomQuery.prototype.insertBefore = function () {
        var toInsertParams = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            toInsertParams[_i] = arguments[_i];
        }
        this.each(function (existingItem) {
            var existingElement = existingItem.getAsElem(0).value;
            var rootNode = existingElement.parentNode;
            for (var cnt = 0; cnt < toInsertParams.length; cnt++) {
                toInsertParams[cnt].eachElem(function (insertElem) {
                    rootNode.insertBefore(insertElem, existingElement);
                });
            }
        });
        var res = [];
        res.push(this);
        res = res.concat(toInsertParams);
        return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], res, false)))();
    };
    DomQuery.prototype.orElse = function () {
        var elseValue = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            elseValue[_i] = arguments[_i];
        }
        if (this.isPresent()) {
            return this;
        }
        else {
            return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], elseValue, false)))();
        }
    };
    DomQuery.prototype.orElseLazy = function (func) {
        if (this.isPresent()) {
            return this;
        }
        else {
            return new DomQuery(func());
        }
    };
    DomQuery.prototype.parents = function (tagName) {
        var retSet = new Set();
        var retArr = [];
        var lowerTagName = tagName.toLowerCase();
        var resolveItem = function (item) {
            if ((item.tagName || "").toLowerCase() == lowerTagName && !retSet.has(item)) {
                retSet.add(item);
                retArr.push(item);
            }
        };
        this.eachElem(function (item) {
            var _a;
            while (item.parentNode || item.host) {
                item = (_a = item === null || item === void 0 ? void 0 : item.parentNode) !== null && _a !== void 0 ? _a : item === null || item === void 0 ? void 0 : item.host;
                resolveItem(item);
                //nested forms not possible, performance shortcut
                if (tagName == "form" && retArr.length) {
                    return false;
                }
            }
        });
        return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], retArr, false)))();
    };
    DomQuery.prototype.copyAttrs = function (sourceItem) {
        var _this = this;
        sourceItem.eachElem(function (sourceNode) {
            var attrs = objToArray(sourceNode.attributes);
            for (var _i = 0, attrs_1 = attrs; _i < attrs_1.length; _i++) {
                var item = attrs_1[_i];
                var value = item.value;
                var name_1 = item.name;
                switch (name_1) {
                    case "id":
                        _this.id.value = value;
                        break;
                    case "disabled":
                        _this.resolveAttributeHolder("disabled").disabled = value;
                        break;
                    case "checked":
                        _this.resolveAttributeHolder("checked").checked = value;
                        break;
                    default:
                        _this.attr(name_1).value = value;
                }
            }
        });
        return this;
    };
    /**
     * resolves an attribute holder compared
     * @param attrName the attribute name
     */
    DomQuery.prototype.resolveAttributeHolder = function (attrName) {
        if (attrName === void 0) { attrName = "value"; }
        var ret = [];
        ret[attrName] = null;
        return (attrName in this.getAsElem(0).value) ?
            this.getAsElem(0).value :
            ret;
    };
    /**
     * outerhtml convenience method
     * browsers only support innerHTML but
     * for instance for your jsf.js we have a full
     * replace pattern which needs outerHTML processing
     *
     * @param markup
     * @param runEmbeddedScripts
     * @param runEmbeddedCss
     */
    DomQuery.prototype.outerHTML = function (markup, runEmbeddedScripts, runEmbeddedCss, deep) {
        var _a;
        if (deep === void 0) { deep = false; }
        if (this.isAbsent()) {
            return;
        }
        var focusElementId = (_a = document === null || document === void 0 ? void 0 : document.activeElement) === null || _a === void 0 ? void 0 : _a.id;
        var caretPosition = (focusElementId) ? DomQuery.getCaretPosition(document.activeElement) : null;
        var nodes = DomQuery.fromMarkup(markup);
        var res = [];
        var toReplace = this.getAsElem(0).value;
        var firstInsert = nodes.get(0);
        var parentNode = toReplace.parentNode;
        var replaced = firstInsert.getAsElem(0).value;
        parentNode.replaceChild(replaced, toReplace);
        res.push(new DomQuery(replaced));
        //no replacement possible
        if (this.isAbsent()) {
            return this;
        }
        var insertAdditionalItems = [];
        if (nodes.length > 1) {
            insertAdditionalItems = insertAdditionalItems.concat.apply(insertAdditionalItems, nodes.values.slice(1));
            res.push(DomQuery.byId(replaced).insertAfter(new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], insertAdditionalItems, false)))()));
        }
        if (runEmbeddedScripts) {
            this.runScripts();
        }
        if (runEmbeddedCss) {
            this.runCss();
        }
        var focusElement = DomQuery.byId(focusElementId);
        if (focusElementId && focusElement.isPresent() &&
            caretPosition != null && "undefined" != typeof caretPosition) {
            focusElement.eachElem(function (item) { return DomQuery.setCaretPosition(item, caretPosition); });
        }
        return nodes;
    };
    /**
     * Run through the given nodes in the DomQuery execute the inline scripts
     * @param whilteListed: optional whitelist function which can filter out script tags which are not processed
     * defaults to the standard jsf.js exclusion (we use this code for myfaces)
     */
    DomQuery.prototype.runScripts = function (whilteListed) {
        var _this = this;
        if (whilteListed === void 0) { whilteListed = DEFAULT_WHITELIST; }
        var finalScripts = [], equi = equalsIgnoreCase, execScrpt = function (item) {
            var tagName = item.tagName;
            var itemType = item.type || "";
            if (tagName && equi(tagName, "script") &&
                (itemType === "" || equi(itemType, "text/javascript") ||
                    equi(itemType, "javascript") ||
                    equi(itemType, "text/ecmascript") ||
                    equi(itemType, "ecmascript"))) {
                var src = item.getAttribute('src');
                if ('undefined' != typeof src
                    && null != src
                    && src.length > 0) {
                    //we have to move this into an inner if because chrome otherwise chokes
                    //due to changing the and order instead of relying on left to right
                    //if jsf.js is already registered we do not replace it anymore
                    if (whilteListed(src)) {
                        if (finalScripts.length) {
                            //script source means we have to eval the existing
                            //scripts before running the include
                            _this.globalEval(finalScripts.join("\n"));
                            finalScripts = [];
                        }
                        _this.loadScriptEval(src, 0, "UTF-8");
                    }
                }
                else {
                    // embedded script auto eval
                    //TODO this probably needs to be changed due to our new parsing structures
                    //probably not needed anymore
                    var evalText = trim(item.text || item.innerText || item.innerHTML);
                    var go = true;
                    while (go) {
                        go = false;
                        if (evalText.substring(0, 4) == "<!--") {
                            evalText = evalText.substring(4);
                            go = true;
                        }
                        if (evalText.substring(0, 4) == "//<!--") {
                            evalText = evalText.substring(6);
                            go = true;
                        }
                        if (evalText.substring(0, 11) == "//<![CDATA[") {
                            evalText = evalText.substring(11);
                            go = true;
                        }
                    }
                    // we have to run the script under a global context
                    //we store the script for less calls to eval
                    finalScripts.push(evalText);
                }
            }
        };
        try {
            var scriptElements = new DomQuery(this.filterSelector("script"), this.querySelectorAll("script"));
            //script execution order by relative pos in their dom tree
            scriptElements.stream
                .flatMap(function (item) { return Stream_1.Stream.of(item.values); })
                .sort(function (node1, node2) { return node1.compareDocumentPosition(node2) - 3; }) //preceding 2, following == 4)
                .each(function (item) { return execScrpt(item); });
            if (finalScripts.length) {
                this.globalEval(finalScripts.join("\n"));
            }
        }
        catch (e) {
            if (window.console && window.console.error) {
                //not sure if we
                //should use our standard
                //error mechanisms here
                //because in the head appendix
                //method only a console
                //error would be raised as well
                console.error(e.message || e.description);
            }
        }
        finally {
            //the usual ie6 fix code
            //the IE6 garbage collector is broken
            //nulling closures helps somewhat to reduce
            //mem leaks, which are impossible to avoid
            //at this browser
            execScrpt = null;
        }
        return this;
    };
    DomQuery.prototype.runCss = function () {
        var applyStyle = function (item, style) {
            var _a, _b, _c, _d;
            var newSS = document.createElement("style");
            document.getElementsByTagName("head")[0].appendChild(newSS);
            var styleSheet = (_a = newSS.sheet) !== null && _a !== void 0 ? _a : newSS.styleSheet;
            newSS.setAttribute("rel", (_b = item.getAttribute("rel")) !== null && _b !== void 0 ? _b : "stylesheet");
            newSS.setAttribute("type", (_c = item.getAttribute("type")) !== null && _c !== void 0 ? _c : "text/css");
            if ((_d = styleSheet === null || styleSheet === void 0 ? void 0 : styleSheet.cssText) !== null && _d !== void 0 ? _d : false) {
                styleSheet.cssText = style;
            }
            else {
                newSS.appendChild(document.createTextNode(style));
            }
        }, execCss = function (item) {
            var tagName = item.tagName;
            if (tagName && equalsIgnoreCase(tagName, "link") && equalsIgnoreCase(item.getAttribute("type"), "text/css")) {
                applyStyle(item, "@import url('" + item.getAttribute("href") + "');");
            }
            else if (tagName && equalsIgnoreCase(tagName, "style") && equalsIgnoreCase(item.getAttribute("type"), "text/css")) {
                var innerText_1 = [];
                //compliant browsers know child nodes
                var childNodes = Array.prototype.slice.call(item.childNodes);
                if (childNodes) {
                    childNodes.forEach(function (child) { return innerText_1.push(child.innerHTML || child.data); });
                    //non compliant ones innerHTML
                }
                else if (item.innerHTML) {
                    innerText_1.push(item.innerHTML);
                }
                applyStyle(item, innerText_1.join(""));
            }
        };
        var scriptElements = new DomQuery(this.filterSelector("link, style"), this.querySelectorAll("link, style"));
        scriptElements.stream
            .flatMap(function (item) { return Stream_1.Stream.of(item.values); })
            .sort(function (node1, node2) { return node1.compareDocumentPosition(node2) - 3; })
            .each(function (item) { return execCss(item); });
        return this;
    };
    /**
     * fires a click event on the underlying dom elements
     */
    DomQuery.prototype.click = function () {
        this.fireEvent("click");
        return this;
    };
    DomQuery.prototype.addEventListener = function (type, listener, options) {
        this.eachElem(function (node) { return node.addEventListener(type, listener, options); });
        return this;
    };
    DomQuery.prototype.removeEventListener = function (type, listener, options) {
        this.eachElem(function (node) { return node.removeEventListener(type, listener, options); });
        return this;
    };
    /**
     * fires an event
     */
    DomQuery.prototype.fireEvent = function (eventName) {
        this.eachElem(function (node) {
            var doc;
            if (node.ownerDocument) {
                doc = node.ownerDocument;
            }
            else if (node.nodeType == 9) {
                // the node may be the document itself, nodeType 9 = DOCUMENT_NODE
                doc = node;
            }
            else {
                throw new Error("Invalid node passed to fireEvent: " + node.id);
            }
            if (node.dispatchEvent) {
                // Gecko-style approach (now the standard) takes more work
                var eventClass = "";
                // Different events have different event classes.
                // If this switch statement can't map an eventName to an eventClass,
                // the event firing is going to fail.
                switch (eventName) {
                    case "click": // Dispatching of 'click' appears to not work correctly in Safari. Use 'mousedown' or 'mouseup' instead.
                    case "mousedown":
                    case "mouseup":
                        eventClass = "MouseEvents";
                        break;
                    case "focus":
                    case "change":
                    case "blur":
                    case "select":
                        eventClass = "HTMLEvents";
                        break;
                    default:
                        throw "fireEvent: Couldn't find an event class for event '" + eventName + "'.";
                        break;
                }
                var event_1 = doc.createEvent(eventClass);
                event_1.initEvent(eventName, true, true); // All events created as bubbling and cancelable.
                event_1.synthetic = true; // allow detection of synthetic events
                // The second parameter says go ahead with the default action
                node.dispatchEvent(event_1);
            }
            else if (node.fireEvent) {
                // IE-old school style, you can drop this if you don't need to support IE8 and lower
                var event_2 = doc.createEventObject();
                event_2.synthetic = true; // allow detection of synthetic events
                node.fireEvent("on" + eventName, event_2);
            }
        });
    };
    DomQuery.prototype.textContent = function (joinstr) {
        if (joinstr === void 0) { joinstr = ""; }
        return this.stream
            .map(function (value) {
            var item = value.getAsElem(0).orElseLazy(function () {
                return {
                    textContent: ""
                };
            }).value;
            return item.textContent || "";
        })
            .reduce(function (text1, text2) { return text1 + joinstr + text2; }, "").value;
    };
    DomQuery.prototype.innerText = function (joinstr) {
        if (joinstr === void 0) { joinstr = ""; }
        return this.stream
            .map(function (value) {
            var item = value.getAsElem(0).orElseLazy(function () {
                return {
                    innerText: ""
                };
            }).value;
            return item.innerText || "";
        })
            .reduce(function (text1, text2) { return [text1, text2].join(joinstr); }, "").value;
    };
    /**
     * encodes all input elements properly into respective
     * config entries, this can be used
     * for legacy systems, for newer usecases, use the
     * HTML5 Form class which all newer browsers provide
     *
     * @param toMerge optional config which can be merged in
     * @return a copy pf
     */
    DomQuery.prototype.encodeFormElement = function (toMerge) {
        if (toMerge === void 0) { toMerge = new Monad_1.Config({}); }
        //browser behavior no element name no encoding (normal submit fails in that case)
        //https://issues.apache.org/jira/browse/MYFACES-2847
        if (this.name.isAbsent()) {
            return;
        }
        //lets keep it sideffects free
        var target = toMerge.shallowCopy;
        this.each(function (element) {
            var _a, _b;
            if (element.name.isAbsent()) { //no name, no encoding
                return;
            }
            var name = element.name.value;
            var tagName = element.tagName.orElse("__none__").value.toLowerCase();
            var elemType = element.type.orElse("__none__").value.toLowerCase();
            elemType = elemType.toLowerCase();
            // routine for all elements
            // rules:
            // - process only inputs, textareas and selects
            // - elements muest have attribute "name"
            // - elements must not be disabled
            if (((tagName == "input" || tagName == "textarea" || tagName == "select") &&
                (name != null && name != "")) && !element.disabled) {
                // routine for select elements
                // rules:
                // - if select-one and value-Attribute exist => "name=value"
                // (also if value empty => "name=")
                // - if select-one and value-Attribute don't exist =>
                // "name=DisplayValue"
                // - if select multi and multple selected => "name=value1&name=value2"
                // - if select and selectedIndex=-1 don't submit
                if (tagName == "select") {
                    // selectedIndex must be >= 0 sein to be submittet
                    var selectElem = element.getAsElem(0).value;
                    if (selectElem.selectedIndex >= 0) {
                        var uLen = selectElem.options.length;
                        for (var u = 0; u < uLen; u++) {
                            // find all selected options
                            //let subBuf = [];
                            if (selectElem.options[u].selected) {
                                var elementOption = selectElem.options[u];
                                target.append(name).value = (elementOption.getAttribute("value") != null) ?
                                    elementOption.value : elementOption.text;
                            }
                        }
                    }
                }
                // routine for remaining elements
                // rules:
                // - don't submit no selects (processed above), buttons, reset buttons, submit buttons,
                // - submit checkboxes and radio inputs only if checked
                if ((tagName != Submittables.SELECT &&
                    elemType != Submittables.BUTTON &&
                    elemType != Submittables.RESET &&
                    elemType != Submittables.SUBMIT &&
                    elemType != Submittables.IMAGE) && ((elemType != Submittables.CHECKBOX && elemType != Submittables.RADIO) ||
                    element.checked)) {
                    var files = (_b = (_a = element.value.value) === null || _a === void 0 ? void 0 : _a.files) !== null && _b !== void 0 ? _b : [];
                    if (files === null || files === void 0 ? void 0 : files.length) {
                        //xhr level2
                        target.append(name).value = files[0];
                    }
                    else {
                        target.append(name).value = element.inputValue.value;
                    }
                }
            }
        });
        return target;
    };
    Object.defineProperty(DomQuery.prototype, "cDATAAsString", {
        get: function () {
            var cDataBlock = [];
            var TYPE_CDATA_BLOCK = 4;
            var res = this.lazyStream.flatMap(function (item) {
                return item.childNodes.stream;
            }).filter(function (item) {
                var _a, _b;
                return ((_b = (_a = item === null || item === void 0 ? void 0 : item.value) === null || _a === void 0 ? void 0 : _a.value) === null || _b === void 0 ? void 0 : _b.nodeType) == TYPE_CDATA_BLOCK;
            }).reduce(function (reduced, item) {
                var _a, _b, _c;
                reduced.push((_c = (_b = (_a = item === null || item === void 0 ? void 0 : item.value) === null || _a === void 0 ? void 0 : _a.value) === null || _b === void 0 ? void 0 : _b.data) !== null && _c !== void 0 ? _c : "");
                return reduced;
            }, []).value;
            // response may contain several blocks
            return res.join("");
        },
        enumerable: false,
        configurable: true
    });
    DomQuery.prototype.subNodes = function (from, to) {
        if (Monad_1.Optional.fromNullable(to).isAbsent()) {
            to = this.length;
        }
        return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], this.rootNode.slice(from, Math.min(to, this.length)), false)))();
    };
    DomQuery.prototype.limits = function (end) {
        this._limits = end;
        return this;
    };
    //-- internally exposed methods needed for the interconnectivity
    DomQuery.prototype.hasNext = function () {
        var isLimitsReached = this._limits != -1 && this.pos >= this._limits - 1;
        var isEndOfArray = this.pos >= this.values.length - 1;
        return !(isLimitsReached ||
            isEndOfArray);
    };
    DomQuery.prototype.next = function () {
        if (!this.hasNext()) {
            return null;
        }
        this.pos++;
        return new DomQuery(this.values[this.pos]);
    };
    DomQuery.prototype.lookAhead = function (cnt) {
        if (cnt === void 0) { cnt = 1; }
        if ((this.values.length - 1) < (this.pos + cnt)) {
            return SourcesCollectors_1.ITERATION_STATUS.EO_STRM;
        }
        return new DomQuery(this.values[this.pos + cnt]);
    };
    DomQuery.prototype.current = function () {
        if (this.pos == -1) {
            return SourcesCollectors_1.ITERATION_STATUS.BEF_STRM;
        }
        return new DomQuery(this.values[this.pos]);
    };
    DomQuery.prototype.reset = function () {
        this.pos = -1;
    };
    DomQuery.prototype.attachShadow = function (params) {
        if (params === void 0) { params = { mode: "open" }; }
        var shadowRoots = [];
        this.eachElem(function (item) {
            var shadowElement;
            if (item === null || item === void 0 ? void 0 : item.attachShadow) {
                shadowElement = DomQuery.byId(item.attachShadow(params));
                shadowRoots.push(shadowElement);
            }
            else {
                throw new Error("Shadow dom creation not supported by the browser, please use a shim, to gain this functionality");
            }
        });
        return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], shadowRoots, false)))();
    };
    /**
     * helper to fix a common dom problem
     * we have to wait until a certain condition is met, in most of the cases we just want to know whether an element is present in the subdome before being able to proceed
     * @param condition
     * @param options
     */
    DomQuery.prototype.waitUntilDom = function (condition, options) {
        if (options === void 0) { options = { attributes: true, childList: true, subtree: true, timeout: 500, interval: 100 }; }
        return __awaiter(this, void 0, void 0, function () {
            return __generator(this, function (_a) {
                return [2 /*return*/, waitUntilDom(this, condition, options)];
            });
        });
    };
    Object.defineProperty(DomQuery.prototype, "shadowElements", {
        /**
         * returns the embedded shadow elements
         */
        get: function () {
            var shadowElements = this.querySelectorAll("*")
                .filter(function (item) { return item.hasShadow; });
            var mapped = (shadowElements.allElems() || []).map(function (element) { return element.shadowRoot; });
            return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], mapped, false)))();
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DomQuery.prototype, "shadowRoot", {
        get: function () {
            var shadowRoots = [];
            for (var cnt = 0; cnt < this.rootNode.length; cnt++) {
                if (this.rootNode[cnt].shadowRoot) {
                    shadowRoots.push(this.rootNode[cnt].shadowRoot);
                }
            }
            return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], shadowRoots, false)))();
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DomQuery.prototype, "hasShadow", {
        get: function () {
            for (var cnt = 0; cnt < this.rootNode.length; cnt++) {
                if (this.rootNode[cnt].shadowRoot) {
                    return true;
                }
            }
            return false;
        },
        enumerable: false,
        configurable: true
    });
    //from
    // http://blog.vishalon.net/index.php/javascript-getting-and-setting-caret-position-in-textarea/
    DomQuery.getCaretPosition = function (ctrl) {
        var caretPos = 0;
        try {
            if (document === null || document === void 0 ? void 0 : document.selection) {
                ctrl.focus();
                var selection = document.selection.createRange();
                //the selection now is start zero
                selection.moveStart('character', -ctrl.value.length);
                //the caretposition is the selection start
                caretPos = selection.text.length;
            }
        }
        catch (e) {
            //now this is ugly, but not supported input types throw errors for selectionStart
            //just in case someone dumps this code onto unsupported browsers
        }
        return caretPos;
    };
    /**
     * sets the caret position
     *
     * @param ctrl the control to set the caret position to
     * @param pos the position to set
     *
     * note if the control does not have any selectable and focusable behavior
     * calling this method does nothing (silent fail)
     *
     */
    DomQuery.setCaretPosition = function (ctrl, pos) {
        (ctrl === null || ctrl === void 0 ? void 0 : ctrl.focus) ? ctrl === null || ctrl === void 0 ? void 0 : ctrl.focus() : null;
        //the selection range is our caret position
        (ctrl === null || ctrl === void 0 ? void 0 : ctrl.setSelectiongRange) ? ctrl === null || ctrl === void 0 ? void 0 : ctrl.setSelectiongRange(pos, pos) : null;
    };
    /**
     * Implementation of an iterator
     * to allow loops over dom query collections
     */
    DomQuery.prototype[Symbol.iterator] = function () {
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
    /**
     * concats the elements of two Dom Queries into a single one
     * @param toAttach
     */
    DomQuery.prototype.concat = function (toAttach, filterDoubles) {
        if (filterDoubles === void 0) { filterDoubles = true; }
        var ret = this.lazyStream.concat(toAttach.lazyStream).collect(new DomQueryCollector());
        //we now filter the doubles out
        if (!filterDoubles) {
            return ret;
        }
        var idx = {}; //ie11 does not support sets, we have to fake it
        return ret.lazyStream.filter(function (node) {
            var notFound = !(idx === null || idx === void 0 ? void 0 : idx[node.value.value.outerHTML]);
            idx[node.value.value.outerHTML] = true;
            return notFound;
        }).collect(new DomQueryCollector());
    };
    DomQuery.absent = new DomQuery();
    return DomQuery;
}());
exports.DomQuery = DomQuery;
/**
 * Various collectors
 * which can be used in conjunction with Streams
 */
/**
 * A collector which bundles a full dom query stream into a single dom query element
 *
 * This connects basically our stream back into DomQuery
 */
var DomQueryCollector = /** @class */ (function () {
    function DomQueryCollector() {
        this.data = [];
    }
    DomQueryCollector.prototype.collect = function (element) {
        this.data.push(element);
    };
    Object.defineProperty(DomQueryCollector.prototype, "finalValue", {
        get: function () {
            return new (DomQuery.bind.apply(DomQuery, __spreadArray([void 0], this.data, false)))();
        },
        enumerable: false,
        configurable: true
    });
    return DomQueryCollector;
}());
exports.DomQueryCollector = DomQueryCollector;
/**
 * abbreviation for DomQuery
 */
exports.DQ = DomQuery;
//# sourceMappingURL=DomQuery.js.map