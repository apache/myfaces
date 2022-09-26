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
exports.__esModule = true;
exports.CancellablePromise = exports.Promise = exports.interval = exports.timeout = exports.PromiseStatus = void 0;
var Monad_1 = require("./Monad");
var PromiseStatus;
(function (PromiseStatus) {
    PromiseStatus[PromiseStatus["PENDING"] = 0] = "PENDING";
    PromiseStatus[PromiseStatus["FULLFILLED"] = 1] = "FULLFILLED";
    PromiseStatus[PromiseStatus["REJECTED"] = 2] = "REJECTED";
})(PromiseStatus = exports.PromiseStatus || (exports.PromiseStatus = {}));
/*
 * Promise wrappers for timeout and interval
 */
function timeout(timeout) {
    var handler = null;
    return new CancellablePromise(function (apply, reject) {
        handler = setTimeout(function () { return apply(); }, timeout);
    }, function () {
        if (handler) {
            clearTimeout(handler);
            handler = null;
        }
    });
}
exports.timeout = timeout;
function interval(timeout) {
    var handler = null;
    return new CancellablePromise(function (apply, reject) {
        handler = setInterval(function () {
            apply();
        }, timeout);
    }, function () {
        if (handler) {
            clearInterval(handler);
            handler = null;
        }
    });
}
exports.interval = interval;
/**
 * a small (probably not 100% correct, although I tried to be correct as possible) Promise implementation
 * for systems which do not have a promise implemented
 * Note, although an internal state is kept, this is sideffect free since
 * is value is a function to operate on, hence no real state is kept internally, except for the then
 * and catch calling order
 */
var Promise = /** @class */ (function () {
    function Promise(executor) {
        var _this = this;
        this.status = PromiseStatus.PENDING;
        this.allFuncs = [];
        //super(executor);
        this.value = executor;
        this.value(function (data) { return _this.resolve(data); }, function (data) { return _this.reject(data); });
    }
    Promise.all = function () {
        var promises = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            promises[_i] = arguments[_i];
        }
        var promiseCnt = 0;
        var myapply;
        var myPromise = new Promise(function (apply, reject) {
            myapply = apply;
        });
        var executor = function () {
            promiseCnt++;
            if (promises.length == promiseCnt) {
                myapply();
            }
        };
        executor.__last__ = true;
        for (var cnt = 0; cnt < promises.length; cnt++) {
            promises[cnt]["finally"](executor);
        }
        return myPromise;
    };
    Promise.race = function () {
        var promises = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            promises[_i] = arguments[_i];
        }
        var promiseCnt = 0;
        var myapply;
        var myreject;
        var myPromise = new Promise(function (apply, reject) {
            myapply = apply;
            myreject = reject;
        });
        var thenexecutor = function () {
            if (!!myapply) {
                myapply();
            }
            myapply = null;
            myreject = null;
            return null;
        };
        thenexecutor.__last__ = true;
        var catchexeutor = function () {
            if (!!myreject) {
                myreject();
            }
            myreject = null;
            myapply = null;
            return null;
        };
        catchexeutor.__last__ = true;
        for (var cnt = 0; cnt < promises.length; cnt++) {
            promises[cnt].then(thenexecutor);
            promises[cnt]["catch"](catchexeutor);
        }
        return myPromise;
    };
    Promise.reject = function (reason) {
        var retVal = new Promise(function (resolve, reject) {
            //not really doable without a hack
            if (reason instanceof Promise) {
                reason.then(function (val) {
                    reject(val);
                });
            }
            else {
                setTimeout(function () {
                    reject(reason);
                }, 1);
            }
        });
        return retVal;
    };
    Promise.resolve = function (reason) {
        var retVal = new Promise(function (resolve, reject) {
            //not really doable without a hack
            if (reason instanceof Promise) {
                reason.then(function (val) { return resolve(val); });
            }
            else {
                setTimeout(function () {
                    resolve(reason);
                }, 1);
            }
        });
        return retVal;
    };
    Promise.prototype.then = function (executorFunc, catchfunc) {
        this.allFuncs.push({ "then": executorFunc });
        if (catchfunc) {
            this.allFuncs.push({ "catch": catchfunc });
        }
        this.spliceLastFuncs();
        return this;
    };
    Promise.prototype["catch"] = function (executorFunc) {
        this.allFuncs.push({ "catch": executorFunc });
        this.spliceLastFuncs();
        return this;
    };
    Promise.prototype["finally"] = function (executorFunc) {
        if (this.__reason__) {
            this.__reason__["finally"](executorFunc);
            return;
        }
        this.allFuncs.push({ "finally": executorFunc });
        this.spliceLastFuncs();
        return this;
    };
    Promise.prototype.resolve = function (val) {
        while (this.allFuncs.length) {
            if (!this.allFuncs[0].then) {
                break;
            }
            var fn = this.allFuncs.shift();
            var funcResult = Monad_1.Optional.fromNullable(fn.then(val));
            if (funcResult.isPresent()) {
                funcResult = funcResult.flatMap();
                val = funcResult.value;
                if (val instanceof Promise) {
                    //let func = (newVal: any) => {this.resolve(newVal)};
                    //func.__last__  = true;
                    //val.then(func);
                    this.transferIntoNewPromise(val);
                    return;
                }
            }
            else {
                break;
            }
        }
        this.appyFinally();
        this.status = PromiseStatus.FULLFILLED;
    };
    Promise.prototype.reject = function (val) {
        while (this.allFuncs.length) {
            if (this.allFuncs[0]["finally"]) {
                break;
            }
            var fn = this.allFuncs.shift();
            if (fn["catch"]) {
                var funcResult = Monad_1.Optional.fromNullable(fn["catch"](val));
                if (funcResult.isPresent()) {
                    funcResult = funcResult.flatMap();
                    val = funcResult.value;
                    if (val instanceof Promise) {
                        //val.then((newVal: any) => {this.resolve(newVal)});
                        this.transferIntoNewPromise(val);
                        return;
                    }
                    this.status = PromiseStatus.REJECTED;
                    break;
                }
                else {
                    break;
                }
            }
        }
        this.status = PromiseStatus.REJECTED;
        this.appyFinally();
    };
    Promise.prototype.appyFinally = function () {
        while (this.allFuncs.length) {
            var fn = this.allFuncs.shift();
            if (fn["finally"]) {
                fn["finally"]();
            }
        }
    };
    Promise.prototype.spliceLastFuncs = function () {
        var lastFuncs = [];
        var rest = [];
        for (var cnt = 0; cnt < this.allFuncs.length; cnt++) {
            for (var key in this.allFuncs[cnt]) {
                if (this.allFuncs[cnt][key].__last__) {
                    lastFuncs.push(this.allFuncs[cnt]);
                }
                else {
                    rest.push(this.allFuncs[cnt]);
                }
            }
        }
        this.allFuncs = rest.concat(lastFuncs);
    };
    Promise.prototype.transferIntoNewPromise = function (val) {
        for (var cnt = 0; cnt < this.allFuncs.length; cnt++) {
            for (var key in this.allFuncs[cnt]) {
                val[key](this.allFuncs[cnt][key]);
            }
        }
    };
    return Promise;
}());
exports.Promise = Promise;
/**
 * a cancellable promise
 * a Promise with a cancel function, which can be cancellend any time
 * this is useful for promises which use cancellable asynchronous operations
 * note, even in a cancel state, the finally of the promise is executed, however
 * subsequent thens are not anymore.
 * The current then however is fished or a catch is called depending on how the outer
 * operation reacts to a cancel order.
 */
var CancellablePromise = /** @class */ (function (_super) {
    __extends(CancellablePromise, _super);
    /**
     * @param executor asynchronous callback operation which triggers the callback
     * @param cancellator cancel operation, separate from the trigger operation
     */
    function CancellablePromise(executor, cancellator) {
        var _this = _super.call(this, executor) || this;
        _this.cancellator = function () {
        };
        _this.cancellator = cancellator;
        return _this;
    }
    CancellablePromise.prototype.cancel = function () {
        this.status = PromiseStatus.REJECTED;
        this.appyFinally();
        //lets terminate it once and for all, the finally has been applied
        this.allFuncs = [];
    };
    CancellablePromise.prototype.then = function (executorFunc, catchfunc) {
        return _super.prototype.then.call(this, executorFunc, catchfunc);
    };
    CancellablePromise.prototype["catch"] = function (executorFunc) {
        return _super.prototype["catch"].call(this, executorFunc);
    };
    CancellablePromise.prototype["finally"] = function (executorFunc) {
        return _super.prototype["finally"].call(this, executorFunc);
    };
    return CancellablePromise;
}(Promise));
exports.CancellablePromise = CancellablePromise;
//# sourceMappingURL=Promise.js.map