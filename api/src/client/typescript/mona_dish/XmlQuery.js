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
exports.XQ = exports.XMLQuery = void 0;
var Lang_1 = require("./Lang");
var DomQuery_1 = require("./DomQuery");
var isString = Lang_1.Lang.isString;
/**
 * xml query as specialized case for DomQuery
 */
var XMLQuery = /** @class */ (function (_super) {
    __extends(XMLQuery, _super);
    function XMLQuery(rootNode, docType) {
        if (docType === void 0) { docType = "text/xml"; }
        var _this = this;
        var createIe11DomQueryShim = function () {
            //at the time if wroting ie11 is the only relevant browser
            //left withut any DomQuery support
            var parser = new ActiveXObject("Microsoft.XMLDOM");
            parser.async = false;
            //we shim th dom parser from ie in
            return {
                parseFromString: function (text, contentType) {
                    return parser.loadXML(text);
                }
            };
        };
        var parseXML = function (xml) {
            if (xml == null) {
                return null;
            }
            var domParser = Lang_1.Lang.saveResolveLazy(function () { return new window.DOMParser(); }, function () { return createIe11DomQueryShim(); }).value;
            return domParser.parseFromString(xml, docType);
        };
        if (isString(rootNode)) {
            _this = _super.call(this, parseXML(rootNode)) || this;
        }
        else {
            _this = _super.call(this, rootNode) || this;
        }
        return _this;
    }
    XMLQuery.prototype.isXMLParserError = function () {
        return this.querySelectorAll("parsererror").isPresent();
    };
    XMLQuery.prototype.toString = function () {
        var ret = [];
        this.eachElem(function (node) {
            var _a, _b, _c;
            var serialized = (_c = (_b = (_a = window === null || window === void 0 ? void 0 : window.XMLSerializer) === null || _a === void 0 ? void 0 : _a.constructor()) === null || _b === void 0 ? void 0 : _b.serializeToString(node)) !== null && _c !== void 0 ? _c : node === null || node === void 0 ? void 0 : node.xml;
            if (!!serialized) {
                ret.push(serialized);
            }
        });
        return ret.join("");
    };
    XMLQuery.prototype.parserErrorText = function (joinstr) {
        return this.querySelectorAll("parsererror").textContent(joinstr);
    };
    XMLQuery.parseXML = function (txt) {
        return new XMLQuery(txt);
    };
    XMLQuery.parseHTML = function (txt) {
        return new XMLQuery(txt, "text/html");
    };
    XMLQuery.fromString = function (txt, parseType) {
        if (parseType === void 0) { parseType = "text/xml"; }
        return new XMLQuery(txt, parseType);
    };
    return XMLQuery;
}(DomQuery_1.DomQuery));
exports.XMLQuery = XMLQuery;
exports.XQ = XMLQuery;
//# sourceMappingURL=XmlQuery.js.map