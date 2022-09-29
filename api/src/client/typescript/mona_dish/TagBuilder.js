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
exports.TagBuilder = void 0;
//poliyfill from @webcomponents/webcomponentsjs
var DomQuery_1 = require("./DomQuery");
if ("undefined" != typeof window) {
    (function () {
        if (void 0 === window.Reflect || void 0 === window.customElements || window.customElements.polyfillWrapFlushCallback)
            return;
        var a = HTMLElement;
        window.HTMLElement = {
            HTMLElement: function HTMLElement() {
                return Reflect.construct(a, [], this.constructor);
            }
        }.HTMLElement, HTMLElement.prototype = a.prototype, HTMLElement.prototype.constructor = HTMLElement, Object.setPrototypeOf(HTMLElement, a);
    })();
}
/**
 * beginning custom tag support
 *
 * This api is still experimental
 * and might be interwoven with DomQuery
 * so it is bound to change
 *
 * it follows a builder pattern to allow easier creations
 * with less code of custom tags
 */
var TagBuilder = /** @class */ (function () {
    // noinspection JSUnusedGlobalSymbols
    function TagBuilder(tagName) {
        this.extendsType = HTMLElement;
        this.observedAttrs = [];
        this.tagName = tagName;
    }
    // noinspection JSUnusedGlobalSymbols
    TagBuilder.withTagName = function (tagName) {
        return new TagBuilder(tagName);
    };
    // noinspection JSUnusedGlobalSymbols
    TagBuilder.prototype.withObservedAttributes = function () {
        var oAttrs = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            oAttrs[_i] = arguments[_i];
        }
        this.observedAttrs = oAttrs;
    };
    // noinspection JSUnusedGlobalSymbols
    TagBuilder.prototype.withConnectedCallback = function (callback) {
        this.connectedCallback = callback;
        return this;
    };
    // noinspection JSUnusedGlobalSymbols
    TagBuilder.prototype.withDisconnectedCallback = function (callback) {
        this.disconnectedCallback = callback;
        return this;
    };
    // noinspection JSUnusedGlobalSymbols
    TagBuilder.prototype.withAdoptedCallback = function (callback) {
        this.adoptedCallback = callback;
        return this;
    };
    // noinspection JSUnusedGlobalSymbols
    TagBuilder.prototype.withAttributeChangedCallback = function (callback) {
        this.attributeChangedCallback = callback;
        return this;
    };
    // noinspection JSUnusedGlobalSymbols
    TagBuilder.prototype.withExtendsType = function (extendsType) {
        this.extendsType = extendsType;
        return this;
    };
    // noinspection JSUnusedGlobalSymbols
    TagBuilder.prototype.withOptions = function (theOptions) {
        this.theOptions = theOptions;
        return this;
    };
    // noinspection JSUnusedGlobalSymbols
    TagBuilder.prototype.withClass = function (clazz) {
        if (this.markup) {
            throw Error("Markup already defined, markup must be set in the class");
        }
        this.clazz = clazz;
        return this;
    };
    // noinspection JSUnusedGlobalSymbols
    TagBuilder.prototype.withMarkup = function (markup) {
        if (this.clazz) {
            throw Error("Class already defined, markup must be set in the class");
        }
        this.markup = markup;
        return this;
    };
    // noinspection JSUnusedGlobalSymbols
    TagBuilder.prototype.register = function () {
        var _this = this;
        if (!this.clazz && !this.markup) {
            throw Error("Class or markup must be defined");
        }
        if (this.clazz) {
            var applyCallback = function (name) {
                var outerCallback = _this[name];
                var protoCallback = _this.clazz.prototype[name];
                var finalCallback = outerCallback || protoCallback;
                if (finalCallback) {
                    _this.clazz.prototype[name] = function () {
                        if (outerCallback) {
                            finalCallback.apply(DomQuery_1.DomQuery.byId(this));
                        }
                        else {
                            protoCallback.apply(this);
                        }
                    };
                }
            };
            applyCallback("connectedCallback");
            applyCallback("disconnectedCallback");
            applyCallback("adoptedCallback");
            applyCallback("attributeChangedCallback");
            //TODO how do we handle the oAttrs?
            if (this.observedAttrs.length) {
                Object.defineProperty(this.clazz.prototype, "observedAttributes", {
                    get: function () {
                        return this.observedAttrs;
                    }
                });
            }
            window.customElements.define(this.tagName, this.clazz, this.theOptions || null);
        }
        else {
            var _t_1 = this;
            var applyCallback_1 = function (name, scope) {
                if (_t_1[name]) {
                    _t_1[name].apply(DomQuery_1.DomQuery.byId(scope));
                }
            };
            window.customElements.define(this.tagName, /** @class */ (function (_super) {
                __extends(class_1, _super);
                function class_1() {
                    var _this = _super.call(this) || this;
                    _this.innerHTML = _t_1.markup;
                    return _this;
                }
                Object.defineProperty(class_1, "observedAttributes", {
                    // noinspection JSUnusedGlobalSymbols
                    get: function () {
                        return _t_1.observedAttrs;
                    },
                    enumerable: false,
                    configurable: true
                });
                // noinspection JSUnusedGlobalSymbols
                class_1.prototype.connectedCallback = function () {
                    applyCallback_1("connectedCallback", this);
                };
                // noinspection JSUnusedGlobalSymbols
                class_1.prototype.disconnectedCallback = function () {
                    applyCallback_1("disconnectedCallback", this);
                };
                // noinspection JSUnusedGlobalSymbols
                class_1.prototype.adoptedCallback = function () {
                    applyCallback_1("adoptedCallback", this);
                };
                // noinspection JSUnusedGlobalSymbols
                class_1.prototype.attributeChangedCallback = function () {
                    applyCallback_1("attributeChangedCallback", this);
                };
                return class_1;
            }(this.extendsType)), this.theOptions || null);
        }
    };
    return TagBuilder;
}());
exports.TagBuilder = TagBuilder;
//# sourceMappingURL=TagBuilder.js.map