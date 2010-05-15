/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */


/*
 theoretically we could save some code
 by
 defining the parent object as
 var parent = new Object();
 parent.prototype = new myfaces._impl.core._Runtime();
 extendClass(function () {
 }, parent , {
 But for now we are not doing it the little bit of saved
 space is not worth the loss of readability
 */

myfaces._impl.core._Runtime.singletonExtendClass("myfaces._impl._util._Lang", Object, {

    fetchNamespace : function(namespacing) {
        return myfaces._impl.core._Runtime.fetchNamespace(namespacing);
    },

    reserveNamespace : function(namespacing) {
        return myfaces._impl.core._Runtime.reserveNamespace(namespacing);
    },

    globalEval : function(code) {
        return myfaces._impl.core._Runtime.globalEval(code);
    },

    /**
     * cross port from the dojo lib
     * browser save event resolution
     * @param event the event object
     * (with a fallback for ie events if none is present)
     */
    getEventTarget: function(event) {
        if (!event) {
            //ie6 and 7 fallback
            event = window.event || {};
        }
        var t = (event.srcElement ? event.srcElement : (event.target ? event.target : null));
        while ((t) && (t.nodeType != 1)) {
            t = t.parentNode;
        }
        return t;
    },

    /**
     * check if an element exists in the root
     */
    exists : function(root, element) {
        return myfaces._impl.core._Runtime.exists(root, element);
    },

    /**
     @see myfaces._impl.core._Runtime.extendClass
     */
    singletonExtendClass : function(newClass, extendsClass, functionMap, inherited) {
        return myfaces._impl.core._Runtime.singletonExtendClass(newClass, extendsClass, functionMap, inherited);
    },


    /**
     * equalsIgnoreCase, case insensitive comparison of two strings
     *
     * @param source
     * @param destination
     * @param strongCompare
     */
    equalsIgnoreCase: function(source, destination) {
        //either both are not set or null
        if (!source && !destination) {
            return true;
        }
        //source or dest is set while the other is not
        if (!source || !destination) return false;

        //in any other case we do a strong string comparison
        return source.toLowerCase() === destination.toLowerCase();
    },


    /**
     @see myfaces._impl.core._Runtime.extendClass
     */
    extendClass : function(newClass, extendsClass, functionMap, inherited) {
        return myfaces._impl.core._Runtime.extendClass(newClass, extendsClass, functionMap, inherited);
    },

    //core namespacing and inheritance done, now to the language extensions

    /**
     * Save document.getElementById (this code was ported over from dojo)
     * the idea is that either a string or domNode can be passed
     * @param {Object} reference the reference which has to be byIded
     */
    byId : function(/*object*/ reference) {
        if (this.isString(reference)) {
            return document.getElementById(reference);
        }
        return reference;
    },

    /**
     * backported from dojo
     * Converts an array-like object (i.e. arguments, DOMCollection) to an
     array. Returns a new Array with the elements of obj.
     * @param {Object} obj the object to "arrayify". We expect the object to have, at a
     minimum, a length property which corresponds to integer-indexed
     properties.
     * @param {int} offset the location in obj to start iterating from. Defaults to 0.
     Optional.
     * @param {Array} startWith An array to pack with the properties of obj. If provided,
     properties in obj are appended at the end of startWith and
     startWith is the returned array.
     */
    _toArray : function(obj, offset, startWith) {
        //	summary:
        //		Converts an array-like object (i.e. arguments, DOMCollection) to an
        //		array. Returns a new Array with the elements of obj.
        //	obj:
        //		the object to "arrayify". We expect the object to have, at a
        //		minimum, a length property which corresponds to integer-indexed
        //		properties.
        //	offset:
        //		the location in obj to start iterating from. Defaults to 0.
        //		Optional.
        //	startWith:
        //		An array to pack with the properties of obj. If provided,
        //		properties in obj are appended at the end of startWith and
        //		startWith is the returned array.
        var arr = startWith || [];
        for (var x = offset || 0; x < obj.length; x++) {
            arr.push(obj[x]);
        }
        return arr; // Array
    },

    /**
     * Helper function to provide a trim with a given splitter regular expression
     * @param {|String|} it the string to be trimmed
     * @param {|RegExp|} splitter the splitter regular expressiion
     *
     * FIXME is this still used?
     */
    trimStringInternal : function(it, splitter) {
        return this.strToArray(it, splitter).join(splitter);
    },

    /**
     * String to array function performs a string to array transformation
     * @param {String} it the string which has to be changed into an array
     * @param {RegExp} splitter our splitter reglar expression
     * @return an array of the splitted string
     */
    strToArray : function(/*string*/ it, /*regexp*/ splitter) {
        //	summary:
        //		Return true if it is a String

        if (!this.isString(it)) {
            throw Error("myfaces._impl._util._Lang.strToArray param not of type string");
        }
        var resultArr = it.split(splitter);
        var len = resultArr.length;
        for (var cnt = 0; cnt < len; cnt++) {
            resultArr[cnt] = this.trim(resultArr[cnt]);
        }
        return resultArr;
    },

    /**
     * hyperfast trim
     * http://blog.stevenlevithan.com/archives/faster-trim-javascript
     * crossported from dojo
     */
    trim : function(/*string*/ str) {

        str = str.replace(/^\s\s*/, '');
        var ws = /\s/;
        var i = str.length;
        while (ws.test(str.charAt(--i)));
        return str.slice(0, i + 1);
    },

    /**
     * Splits a string and fetches the last element of the String
     * @param {String} theString the string to be splitted
     * @param {String} delimiter a delimiting string regexp
     *
     */
    splitAndGetLast : function(theString, delimiter) {
        var arr = theString.split(delimiter);
        return arr[arr.length - 1];
    },

    /**
     * Backported from dojo
     * a failsafe string determination method
     * (since in javascript String != "" typeof alone fails!)
     * @param it {|Object|} the object to be checked for being a string
     * @return true in case of being a string false otherwiseÊ
     */
    isString: function(/*anything*/ it) {
        //	summary:
        //		Return true if it is a String
        return !!arguments.length && it != null && (typeof it == "string" || it instanceof String); // Boolean
    },
    /**
     * hitch backported from dojo
     * hitch allows to assign a function to a dedicated scope
     * this is helpful in situations when function reassignments
     * can happen
     * (notably happens often in lazy xhr code)
     *
     * @param {Function} scope of the function to be executed in
     * @param {Function} method to be executed
     *
     * @return whatevery the executed method returns
     */
    hitch : function(/*Object*/scope, /*Function|String*/method /*,...*/) {
        //	summary:
        //		Returns a function that will only ever execute in the a given scope.
        //		This allows for easy use of object member functions
        //		in callbacks and other places in which the "this" keyword may
        //		otherwise not reference the expected scope.
        //		Any number of default positional arguments may be passed as parameters
        //		beyond "method".
        //		Each of these values will be used to "placehold" (similar to curry)
        //		for the hitched function.
        //	scope:
        //		The scope to use when method executes. If method is a string,
        //		scope is also the object containing method.
        //	method:
        //		A function to be hitched to scope, or the name of the method in
        //		scope to be hitched.
        //	example:
        //	|	myfaces._impl._util._Lang.hitch(foo, "bar")();
        //		runs foo.bar() in the scope of foo
        //	example:
        //	|	myfaces._impl._util._Lang.hitch(foo, myFunction);
        //		returns a function that runs myFunction in the scope of foo
        if (arguments.length > 2) {
            return this._hitchArgs._hitchArgs.apply(this._hitchArgs, arguments); // Function
        }
        if (!method) {
            method = scope;
            scope = null;
        }
        if (this.isString(method)) {
            scope = scope || window || function() {
            };
            /*since we do not have dojo global*/
            if (!scope[method]) {
                throw(['myfaces._impl._util._Lang: scope["', method, '"] is null (scope="', scope, '")'].join(''));
            }
            return function() {
                return scope[method].apply(scope, arguments || []);
            }; // Function
        }
        return !scope ? method : function() {
            return method.apply(scope, arguments || []);
        }; // Function
    }
    ,

    _hitchArgs : function(scope, method /*,...*/) {
        var pre = this._toArray(arguments, 2);
        var named = this.isString(method);
        return function() {
            // array-fy arguments
            var args = this._toArray(arguments);
            // locate our method
            var f = named ? (scope || this.global)[method] : method;
            // invoke with collected args
            return f && f.apply(scope || this, pre.concat(args)); // mixed
        }; // Function
    }
    ,

    /**
     * Helper function to merge two maps
     * into one
     * @param {|Object|} destination the destination map
     * @param {|Object|} source the source map
     * @param {|boolean|} overwriteDest if set to true the destination is overwritten if the keys exist in both maps
     **/
    mixMaps : function(destination, source, overwriteDest) {
        /**
         * mixing code depending on the state of dest and the overwrite param
         */
        var _Lang = this;
        var result = {};
        var keyIdx = {};
        var key = null;
        for (key in source) {
            /**
             *we always overwrite dest with source
             *unless overWrite is not set or source does not exist
             *but also only if dest exists otherwise source still is taken
             */
            if (!overwriteDest) {
                /**
                 *we use exists instead of booleans because we cannot rely
                 *on all values being non boolean, we would need an elvis
                 *operator in javascript to shorten this :-(
                 */
                result[key] = _Lang.exists(dest, key) ? destination[key] : source[key];
            } else {
                result[key] = _Lang.exists(source, key) ? source[key] : destination[key];
            }
            keyIdx[key] = true;
        }
        for (key in destination) {
            /*if result.key does not exist we push in dest.key*/
            result[key] = _Lang.exists(result, key) ? result[key] : destination[key];
        }
        return result;
    }
    ,

    /**
     * checks if an array contains an element
     * @param {Array} arr   array
     * @param {String} string_name string to check for
     */
    arrayContains : function(arr, string_name) {
        for (var loop = 0; loop < arr.length; loop++) {
            if (arr[loop] == string_name) {
                return true;
            }
        }
        return false;
    }
    ,

    /**
     * Concatenates an array to a string
     * @param {Array} arr the array to be concatenated
     * @param {String} delimiter the concatenation delimiter if none is set \n is used
     *
     * @return the concatenated array, one special behavior to enable j4fry compatibility has been added
     * if no delimiter is used the [entryNumber]+entry is generated for a single entry
     * TODO check if this is still needed it is somewhat outside of the scope of the function
     * and functionality wise dirty
     */
    arrayToString : function(/*String or array*/ arr, /*string*/ delimiter) {
        if (this.isString(arr)) {
            return arr;
        }
        var finalDelimiter = (null == delimiter) ? "\n" : delimiter;

        var resultArr = [];
        for (var cnt = 0; cnt < arr.length; cnt ++) {
            if (this.isString(arr[cnt])) {
                resultArr.push(((delimiter == null) ? ("[" + cnt + "] ") : "") + arr[cnt]);
            } else {
                resultArr.push(((delimiter == null) ? ("[" + cnt + "] ") : "") + arr[cnt].toString());
            }
        }
        return resultArr.join(finalDelimiter);
    }
    ,

    /**
     * general type assertion routine
     *
     * @param probe the probe to be checked for the correct type
     * @param type the type to be checked for
     */
    _assertType : function(probe, type) {
        if (type != typeof probe) {
            throw Error("probe must be of type " + type);
        }
    },

    /**
     * [STATIC]
     * static method used by static methods that throw errors
     */
    throwNewError : function(request, context, sourceClass, func, exception) {
        var newException = new myfaces._impl.xhrCore._Exception(request, context, sourceClass, "ERROR");
        newException.throwError(request, context, func, exception);
    },

    /**
     * [STATIC]
     * static method used by static methods that throw warnings
     */
    throwNewWarning: function(request, context, sourceClass, func, message) {
        var newException = new myfaces._impl.xhrCore._Exception(request, context, sourceClass, "WARNING");
        newException.throwWarning(request, context, func, message);
    },

    /**
     * onload wrapper for chaining the onload cleanly
     * @param func the function which should be added to the load
     * chain (note we cannot rely on return values here, hence jsf.util.chain will fail)
     */
    addOnLoad: function(func) {
        var oldonload = window.onload;
        if (typeof window.onload != "function") {
            window.onload = func;
        } else {
            window.onload = function() {
                oldonload();
                func();
            }
        }
    },
    /**
     * Simple simple logging only triggering at
     * firebug compatible logging consoles
     *
     * note: ;; means the code will be stripped
     * from the production code by the build system
     */
    _logToContainer: function(styleClass /*+arguments*/, loggingArguments) {
        var loggingContainer = document.getElementById("myfaces.logging");
        if (loggingContainer) {
            var element = document.createElement("div");
            //element.className = styleClass;
            element.innerHTML = loggingArguments.join(" ");
            loggingContainer.appendChild(element);
        }
    },

    objToArray: function(obj) {

        try {
            return Array.prototype.slice.call(obj, 0);
        } catch (e) {
            //ie8 (again as only browser) delivers for css 3 selectors a non convertible object
            //we have to do it the hard way
            //ie8 seems generally a little bit strange in its behavior some
            //objects break the function is everything methodology of javascript
            //and do not implement apply call, or are pseudo arrays which cannot
            //be sliced
            var retVal = [];
            for (var cnt = 0; cnt < obj.length; cnt++) {
                retVal.push(obj[cnt]);
            }
            return retVal;
        }

    },

    logLog: function(/*varargs*/) {
        var argumentStr = this.objToArray(arguments).join(" ");
        if (window.console && window.console.log) {
            window.console.log(argumentStr);
        }
        this._logToContainer("logLog", ["Log:"].concat([argumentStr]));
    },
    logDebug: function(/*varargs*/) {
        var argumentStr = this.objToArray(arguments).join(" ");

        if (window.console && window.console.debug) {
            window.console.debug(argumentStr);
        }
        this._logToContainer("logDebug", ["Debug:"].concat([argumentStr]));
    },
    logError: function(/*varargs*/) {
        var argumentStr = this.objToArray(arguments).join(" ");

        if (window.console && window.console.error) {
            window.console.error(argumentStr);
        }
        this._logToContainer("logError", ["Error:"].concat([argumentStr]));

    },
    logInfo: function(/*varargs*/) {
        var argumentStr = this.objToArray(arguments).join(" ");

        if (window.console && window.console.info) {
            window.console.info(argumentStr);
        }
        this._logToContainer("logInfo", ["Info:"].concat([argumentStr]));
    },
    logWarn: function(/*varargs*/) {
        var argumentStr = this.objToArray(arguments).join(" ");
        if (window.console && window.console.warn) {
            window.console.warn(argumentStr);
        }
        this._logToContainer("logWarn", ["Warn:"].concat([argumentStr]));
    }
});
