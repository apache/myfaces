"use strict";
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
exports.ExtendedArray = void 0;
/**
 * Array with a set of shim functions for older browsers
 * we do not extend prototype (rule #1)
 *
 * This is a helper which for now adds the missing flatMap, without prototype pollution
 *
 * that way we can avoid streams wherever we just want to go pure JS
 * This class is self isolated, so it suffices to just dump it into a project one way or the other
 * without anything else
 */
var ExtendedArray = /** @class */ (function (_super) {
    __extends(ExtendedArray, _super);
    function ExtendedArray() {
        var items = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            items[_i] = arguments[_i];
        }
        var _this = _super.apply(this, items) || this;
        //es5 base class see //fix for es5 deficit from https://github.com/Microsoft/TypeScript/issues/13720
        //for testing it definitely runs into this branch because we are on es5 level
        if (!Array.prototype.flatMap) {
            var flatmapFun = ExtendedArray.prototype.flatMap;
            //unfortunately in es5 the flaptmap function is lost due to inheritance of a primitive
            //es  class, we have to remap it back in
            _this.flatMap = flatmapFun;
        }
        return _this;
    }
    ExtendedArray.prototype.flatMap = function (mapperFunction, noFallback) {
        if (noFallback === void 0) { noFallback = false; }
        var res = [];
        var remap = function (item) {
            var opRes = mapperFunction(item);
            if (Array.isArray(opRes)) {
                if (opRes.length == 1) {
                    res.push(opRes[1]);
                    return;
                }
                if (opRes.length > 1) {
                    opRes.forEach(function (newItem) { return remap(newItem); });
                }
            }
            else {
                res.push(item);
            }
        };
        this.forEach(function (item) { return remap(item); });
        return new (ExtendedArray.bind.apply(ExtendedArray, __spreadArray([void 0], res, false)))();
    };
    return ExtendedArray;
}(Array));
exports.ExtendedArray = ExtendedArray;
//# sourceMappingURL=ExtendedArray.js.map