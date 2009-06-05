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

var myfaces = null;
/**
 * A simple provide function
 * fixing the namespaces
 */
function _reserveMyfacesNamespaces() {
    /**
     *
     * reserve the namespaces used by the myfaces core
     */
    if ('undefined' != typeof OpenAjax && ( 'undefined' == typeof myfaces || null == myfaces)) {
        OpenAjax.hub.registerLibrary("myfaces", "myfaces.apache.org", "1.0", null);
    }

    /*originally we had it at org.apache.myfaces, but we are now down to myfaces since the openajax seems to have problems registering more than a root domain and org is not only apache specific*/
    if('undefined' == typeof myfaces || null == myfaces) {
        myfaces = new Object();
    }

    if('undefined' == typeof(myfaces._impl) || null == myfaces._impl) {
        myfaces._impl = new Object();
    }

    if('undefined' == typeof(myfaces._impl._util) || null == myfaces._impl._util) {
        myfaces._impl._util = new Object();
    }
    if('undefined' == typeof(myfaces._impl.core) || null == myfaces._impl.core) {
        myfaces._impl.core = new Object();
    }
    if('undefined' == typeof(myfaces._impl.xhrCore) || null == myfaces._impl.xhrCore) {
        myfaces._impl.xhrCore = new Object();
    }
    if('undefined' == typeof(myfaces.config) || null == myfaces.config) {
        myfaces.config = new Object();
    }

}

_reserveMyfacesNamespaces();

/**
 * Central internal JSF2 LangUtils with code used
 * by various aspects of the JSF2 Ajax subsystem
 *
 * Note parts of the code were crossported from the dojo
 * javascript library (see license.txt for more details
 * on the dojo bsd license)
 */
if ('undefined' == typeof(myfaces._impl._util._LangUtils) || null == myfaces._impl._util._LangUtils) {
    myfaces._impl._util._LangUtils = function() {
        }

    myfaces._impl._util._LangUtils.global = this;

    myfaces._impl._util._LangUtils._underTest = false;
    myfaces._impl._util._LangUtils._logger = null;

    myfaces._impl._util._LangUtils.isUnderTest = function() {
        return this._underTest;
    };


    myfaces._impl._util._LangUtils.byId = function(/*object*/ reference) {
        if(myfaces._impl._util._LangUtils.isString(reference)) {
            return document.getElementById(reference);
        }
        return reference;
    };

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
    myfaces._impl._util._LangUtils._toArray = function(obj, offset,  startWith) {
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
    };

    /**
     * Helper function to provide a trim with a given splitter regular expression
     * @param {string} it the string to be trimmed
     * @param {regexp} splitter the splitter regular expressiion
     *
     * FIXME is this still used?
     */
    myfaces._impl._util._LangUtils.trimStringInternal = function(it, splitter) {
        return myfaces._impl._util._LangUtils.strToArray(it, splitter).join(splitter);
    };

    /**
     * String to array function performs a string to array transformation
     * @param {String} it the string which has to be changed into an array
     * @param {RegExp} splitter our splitter reglar expression
     * @return an array of the splitted string
     */
    myfaces._impl._util._LangUtils.strToArray = function(/*string*/ it,/*regexp*/ splitter) {
        //	summary:
        //		Return true if it is a String

        if(!myfaces._impl._util._LangUtils.isString(it)) {
            throw new Exception("myfaces._impl._util._LangUtils.strToArray param not of type string")
        }
        var resultArr = it.split(splitter);
        for(var cnt = 0; cnt < resultArr.length; cnt++) {
            resultArr[cnt] = myfaces._impl._util._LangUtils.trim(resultArr[cnt]);
        }
        return resultArr;
    };

    /**
     * hyperfast trim
     * http://blog.stevenlevithan.com/archives/faster-trim-javascript
     * crossported from dojo
     */
    myfaces._impl._util._LangUtils.trim = function(/*string*/ str) {

        str = str.replace(/^\s\s*/, ''),
        ws = /\s/,
        i = str.length;
        while (ws.test(str.charAt(--i)));
        return str.slice(0, i + 1);
    };

    /**
     * Splits a string and fetches the last element of the String
     * @param {String} theString the string to be splitted
     * @param {String} delimiter a deliminating string regexp
     *
     */
    myfaces._impl._util._LangUtils.splitAndGetLast = function(theString, delimiter) {
        var arr = theString.split(delimiter);
        return arr[arr.length - 1];
    };

    

    /**
     * Backported from dojo
     * a failsafe string determination method
     * (since in javascript String != "" typeof alone fails!)
     * @param it {anything} the object to be checked for being a string
     * @return true in case of being a string false otherwiseÊ
     */
    myfaces._impl._util._LangUtils.isString = function(/*anything*/ it) {
        //	summary:
        //		Return true if it is a String
        return !!arguments.length && it != null && (typeof it == "string" || it instanceof String); // Boolean
    };
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
    myfaces._impl._util._LangUtils.hitch = function(/*Object*/scope, /*Function|String*/method /*,...*/) {
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
        //	|	myfaces._impl._util._LangUtils.hitch(foo, "bar")();
        //		runs foo.bar() in the scope of foo
        //	example:
        //	|	myfaces._impl._util._LangUtils.hitch(foo, myFunction);
        //		returns a function that runs myFunction in the scope of foo
        if (arguments.length > 2) {
            return myfaces._impl._util._LangUtils._hitchArgs._hitchArgs.apply(myfaces._impl._util._LangUtils._hitchArgs, arguments); // Function
        }
        if (!method) {
            method = scope;
            scope = null;
        }
        if (this.isString(method)) {
            scope = scope || window || function() {};/*since we do not have dojo global*/
            if (!scope[method]) {
                throw(['myfaces._impl._util._LangUtils: scope["', method, '"] is null (scope="', scope, '")'].join(''));
            }
            return function() {
                return scope[method].apply(scope, arguments || []);
            }; // Function
        }
        return !scope ? method : function() {
            return method.apply(scope, arguments || []);
        }; // Function
    };
    /**
     * used internally to lazy init the logger
     * Lazy init for the logging subsystem
     * @deprecated will be removed soon
     */
    myfaces._impl._util._LangUtils._getLogger = function() {
        if(null ==  myfaces._impl._util._LangUtils._logger) {
            myfaces._impl._util._LangUtils._logger = myfaces._impl._util._Logger.getInstance();
        }
        return myfaces._impl._util._LangUtils._logger;
    };


    myfaces._impl._util._LangUtils._hitchArgs = function(scope, method /*,...*/) {
        var pre = this._toArray(arguments, 2);
        var named = this.isString(method);
        return function() {
            // arrayify arguments
            var args = this._toArray(arguments);
            // locate our method
            var f = named ? (scope || myfaces._impl._util._LangUtils.global)[method] : method;
            // invoke with collected args
            return f && f.apply(scope || this, pre.concat(args)); // mixed
        } // Function
    };


    /**
     * Helper function to merge two maps
     * into one
     * @param {Map}Êdestination the destination map
     * @param {Map}Êsource the source map
     * @param {boolean} overwriteDest if set to true the destination is overwritten if the keys exist in both maps
     **/
    myfaces._impl._util._LangUtils.mixMaps = function(destination, source, overwriteDest) {
        /**
         * mixin code depending on the state of dest and the overwrite param
         */
        var _JSF2Utils = myfaces._impl._util._LangUtils;
        var result = {};
        var keyIdx = {};
        var key = null;
        for(key in source) {
            /**
           *we always overwrite dest with source
           *unless overWrite is not set or source does not exist
           *but also only if dest exists otherwise source still is taken
           */
            if(!overwriteDest) {
                /**
                 *we use exists instead of booleans because we cannot reloy
                 *on all values being non boolean, we would need an elvis
                 *operator in javascript to shorten this :-(
                 */
                result[key] = _JSF2Utils.exists(dest,key) ? dest[key] : source[key];
            } else {
                result[key] = _JSF2Utils.exists(source,key) ? source[key] : dest[key];
            }
            keyIdx[key] = true;
        }
        for(key in destination) {
            /*if result.key does not exist we push in dest.key*/
            result[key] = _JSF2Utils.exists(result,key) ? result[key] : destination[key];
        }
        return result;
    };

    /**
     * check if an element exists in the root
     */
    myfaces._impl._util._LangUtils.exists = function(root, element) {
        return ('undefined' != typeof root && null != root &&  'undefined' != typeof root[element] && null != root[element]);
    }


    /**
     * checks if an array contains an element
     * @param {Array} arr   array
     * @param {String} string_name string to check for
     */
    myfaces._impl._util._LangUtils.arrayContains = function(arr, string_name) {
        for ( var loop = 0; loop < arr.length; loop++) {
            if (arr[loop] == string_name) {
                return true;
            }
        }
        return false;
    };

    /**
     * Concatenates an array to a string
     * @param {Array} arr the array to be concatenated
     * @param {String} delimiter, the concateation delimiter if none is set \n is used
     *
     * @return the concatenated array, one special behavior to enable j4fry compatibility has been added
     * if no delimiter is used the [entryNumber]+entry is generated for a single entry
     * TODO check if this is still needed it is somewhat outside of the scope of the function
     * and functionalitywise dirty
     */
    myfaces._impl._util._LangUtils.arrayToString = function(/*String or array*/ arr, /*string*/ delimiter) {
        if( myfaces._impl._util._LangUtils.isString(arr) ) {
            return arr;
        }
        var finalDelimiter = (null == delimiter) ? "\n" : delimiter;

        var resultArr = [];
        for(var cnt = 0; cnt < arr.length; cnt ++) {
            if(myfaces._impl._util._LangUtils.isString(arr[cnt])) {
                resultArr.push(((delimiter == null) ? ("[" + cnt + "] ") : "") + arr[cnt]);
            } else {
                resultArr.push(((delimiter == null) ? ("[" + cnt + "] ") : "") + arr[cnt].toString());
            }
        }
        return resultArr.join(finalDelimiter);
    };
   
}
