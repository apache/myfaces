/**
 * @namespace
 * @name window
 * @description supplimental window methods.
 */

if (!window.myfaces) {
    /**
     * @namespace
     * @name myfaces
     */
    var myfaces = new function() {
    };
    window.myfaces = myfaces;
}

/**
 * @memberOf myfaces
 * @namespace
 * @name _impl
 */
myfaces._impl = (myfaces._impl) ? myfaces._impl : {};
/**
 * @memberOf myfaces._impl
 * @namespace
 * @name core
 */
myfaces._impl.core = (myfaces._impl.core) ? myfaces._impl.core :{};

if (!myfaces._impl.core._EvalHandlers) {
    /**
     * @memberOf myfaces._impl.core
     * @namespace
     * @name _EvalHandlers
     */
    myfaces._impl.core._EvalHandlers = new function() {
        //the rest of the namespaces can be handled by our namespace feature
        //helper to avoid unneeded hitches
        /**
         * @borrows myfaces._impl.core._Runtime as _T
         */
        var _T = this;

        /*cascaded eval methods depending upon the browser*/

        /**
         * @function
         * @param code

         *
         * evals a script globally using exec script (ie6 fallback)
         * @param {String} code the code which has to be evaluated
         * @borrows myfaces._impl.core._Runtime as _T
         */
        _T._evalExecScript = function(code) {
            //execScript definitely only for IE otherwise we might have a custom
            //window extension with undefined behavior on our necks
            //window.execScript does not return anything
            //on htmlunit it return "null object"
            var ret = window.execScript(code);
            if ('undefined' != typeof ret && ret == "null" /*htmlunit bug*/) {
                return null;
            }
            return ret;
        };

        /**
         * flakey head appendix method which does not work in the correct
         * order or at all for all modern browsers
         * but seems to be the only method which works on blackberry correctly
         * hence we are going to use it as fallback
         *
         * @param {String} code the code part to be evaled
         * @borrows myfaces._impl.core._Runtime as _T
         */
        _T._evalBBOld = function(code) {
            var location = document.getElementsByTagName("head")[0] || document.documentElement;
            var placeHolder = document.createElement("script");
            placeHolder.type = "text/javascript";
            placeHolder.text = code;
            location.insertBefore(placeHolder, location.firstChild);
            location.removeChild(placeHolder);
            return null;
        };

        /**
         * @name myfaces._impl.core._Runtime._standardGlobalEval
         * @private
         * @param {String} code
         */
        _T._standardGlobalEval = function(code) {
            //fix which works in a cross browser way
            //we used to scope an anonymous function
            //but I think this is better
            //the reason is firefox applies a wrong scope
            //if we call eval by not scoping

            var gEval = function () {

                var ret = window.eval.call(window, code);
                if ('undefined' == typeof ret) return null;
                return ret;
            };
            var ret = gEval();
            if ('undefined' == typeof ret) return null;
            return ret;
        };

        /**
         * global eval on scripts
         * @param {String} code
         * @name myfaces._impl.core._Runtime.globalEval
         * @function
         */
        _T.globalEval = function(code) {
            //TODO add a config param which allows to evaluate global scripts even if the call
            //is embedded in an iframe
            //We lazy init the eval type upon the browsers
            //capabilities
            if ('undefined' == typeof _T._evalType) {
                _T._evalType = window.execScript ? "_evalExecScript" : null;
                _T._evalType = !_T._evalType && window.eval && (!_T.browser.isBlackBerry || _T.browser.isBlackBerry >= 6) ? "_standardGlobalEval" : null;
                _T._evalType = (window.eval && !_T._evalType) ? "_evalBBOld" : null;
            }
            if (_T._evalType) {
                return _T[_T._evalType](code);
            }
            //we probably have covered all browsers, but this is a safety net which might be triggered
            //by some foreign browser which is not covered by the above cases
            eval.call(window, code);
            return null;
        };

    };
}