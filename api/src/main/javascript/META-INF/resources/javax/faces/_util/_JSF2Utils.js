

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

if('undefined' == typeof org || null == org) {
    var org = null;
}
/**
 * A simple provide function
 * fixing the namespaces
 */
function _reserveMyfaces() {

    /**
     *
     * reserve the root namespace for myfaces
     */
    if ('undefined' != typeof OpenAjax && ('undefined' == typeof org || null == org || 'undefined' == typeof org.apache || 'undefined' == typeof myfaces ||
        null == org.apache || null == myfaces)) {
        if(undefined == typeof(org) || null == org) {
            OpenAjax.hub.registerLibrary("myfaces", "myfaces.apache.org", "1.0", null);
        }

    }

    /*originally we had it at org.apache.myfaces, but we are now down to myfaces since the openajax seems to have problems registering more than a root domain and org is not only apache specific*/
    if('undefined' == typeof myfaces || null == myfaces) {
        myfaces = new Object();
    }

}

_reserveMyfaces();

/**
 * Central internal JSF2 Utils with code used
 * by various aspects of the JSF2 Ajax subsystem
 *
 * Note parts of the code were crossported from the dojo
 * javascript library (see license.txt for more details
 * on the dojo bsd license)
 */
if ('undefined' == typeof(myfaces._JSF2Utils) || null == myfaces._JSF2Utils) {
    myfaces._JSF2Utils = function() {
        }
    myfaces._JSF2Utils._underTest = false;
    myfaces._JSF2Utils.isUnderTest = function() {
        return this._underTest;
    }


    myfaces._JSF2Utils.byId = function(/*object*/ reference) {
        if(myfaces._JSF2Utils.isString(reference)) {
            return document.getElementById(reference);
        }
        return reference;
    },

    /**
     * backported from dojo
     * Converts an array-like object (i.e. arguments, DOMCollection) to an
     array. Returns a new Array with the elements of obj.
     * @param obj the object to "arrayify". We expect the object to have, at a
     minimum, a length property which corresponds to integer-indexed
     properties.
     * @param offset the location in obj to start iterating from. Defaults to 0.
     Optional.
     * @param startWith An array to pack with the properties of obj. If provided,
     properties in obj are appended at the end of startWith and
     startWith is the returned array.
     */
    myfaces._JSF2Utils._toArray = function(/*Object*/obj, /*Number?*/offset, /*Array?*/ startWith) {
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

    myfaces._JSF2Utils.trimStringInternal = function(/*string*/ it,/*regexp*/ splitter) {
        return myfaces._JSF2Utils.strToArray(it, splitter).join(splitter);
    };


    myfaces._JSF2Utils.strToArray = function(/*string*/ it,/*regexp*/ splitter) {
        //	summary:
        //		Return true if it is a String

        if(!myfaces._JSF2Utils.isString(it)) {
            throw new Exception("myfaces._JSF2Utils.strToArray param not of type string")
        }
        var resultArr = it.split(splitter);
        for(var cnt = 0; cnt < resultArr.length; cnt++) {
          resultArr[cnt] = myfaces._JSF2Utils.trim(resultArr[cnt]);
        }
        return resultArr;
    };

    /**
     * hyperfast trim
     * http://blog.stevenlevithan.com/archives/faster-trim-javascript
     */
    myfaces._JSF2Utils.trim = function(/*string*/) {

      	var	str = str.replace(/^\s\s*/, ''),
		ws = /\s/,
		i = str.length;
        while (ws.test(str.charAt(--i)));
        return str.slice(0, i + 1);
    };

    /**
     * Backported from dojo
     */
    myfaces._JSF2Utils.isString = function(/*anything*/ it) {
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
     */
    myfaces._JSF2Utils.hitch = function(/*Object*/scope, /*Function|String*/method /*,...*/) {
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
        //	|	myfaces._JSF2Utils.hitch(foo, "bar")();
        //		runs foo.bar() in the scope of foo
        //	example:
        //	|	myfaces._JSF2Utils.hitch(foo, myFunction);
        //		returns a function that runs myFunction in the scope of foo
        if (arguments.length > 2) {
            return myfaces._JSF2Utils._hitchArgs._hitchArgs.apply(myfaces._JSF2Utils._hitchArgs, arguments); // Function
        }
        if (!method) {
            method = scope;
            scope = null;
        }
        if (this.isString(method)) {
            scope = scope || window || function() {};/*since we do not have dojo global*/
            if (!scope[method]) {
                throw(['myfaces._JSF2Utils: scope["', method, '"] is null (scope="', scope, '")'].join(''));
            }
            return function() {
                return scope[method].apply(scope, arguments || []);
            }; // Function
        }
        return !scope ? method : function() {
            return method.apply(scope, arguments || []);
        }; // Function
    };
    // Logging helper for use in Firebug
    /*static*/
    myfaces._JSF2Utils.logWarning = function(varArgs)
    {
        if (window.console && console.warn)
            console.warn(arguments);
    // else???
    }
    // Logging helper for use in Firebug
    /*static*/
    myfaces._JSF2Utils.logError = function(varArgs)
    {
        if (window.console && console.error)
            console.error(arguments);
    // else???
    }
    myfaces._JSF2Utils._hitchArgs = function(scope, method /*,...*/) {
        var pre = this._toArray(arguments, 2);
        var named = this.isString(method);
        return function() {
            // arrayify arguments
            var args = this._toArray(arguments);
            // locate our method
            var f = named ? (scope || dojo.global)[method] : method;
            // invoke with collected args
            return f && f.apply(scope || this, pre.concat(args)); // mixed
        } // Function
    };


    /**
     * Helper function to merge two maps
     * into one
     * @param dest the destination map
     * @param source the source map
     * @param overwriteDest if set to true the destination is overwritten if the keys exist in both maps
     **/
    myfaces._JSF2Utils.mixMaps = function(/*map*/ destination, /*map*/source, /*boolean*/ overwriteDest) {
        /**
         * mixin code depending on the state of dest and the overwrite param
         */
        var result = {};
        for(var key in source) {
            if(overwriteDest || 'undefined' == typeof (source[key]) || null == (source[key])) {
                result[key] = source[key];
            } else if (!overWrite ) {
                result[key] = dest[key];;
            }
        }
        return result;
    };

    /**
     * check if an element exists in the root
     */
    myfaces._JSF2Utils.exists = function(root, element) {
        return ('undefined' != typeof root[element] && null != root[element]);
    }

    myfaces._JSF2Utils.arrayToString = function(/*String or array*/ arr, /*string*/ delimiter) {
      if(myfaces._JSF2Utils.isString(arr)) {
          return arr;
      }
      return arr.join(delimiter);
    };

    /**
     * finds the parent form of a given node
     * @param node is the node which the parent form has to be determined
     **/
    myfaces._JSF2Utils.getParentForm = function(/*node*/ node) {
        if('undefined' == typeof node || null == node) return node;

        var tempNode = myfaces._JSF2Utils.byId(node);

        if(tempNode.tagName.toLowerCase() == "form") return tempNode;
        var tagName = (undefined != typeof tempNode.tagName && tempNode.tagName != null) ? tempNode.tagName.toLowerCase(): null;
        while(null != tagName &&tagName != "body" && tagName != "form") {
            tempNode = tempNode.parentNode;
            tagName = (undefined != typeof tempNode.tagName && tempNode.tagName != null) ? tempNode.tagName.toLowerCase(): null;

        }
        if("form" == tagName) {
            return tempNode;
        }
        return null;
    }

    /**
     * fetches the values of a form and returns
     * the name/value pairs as javascript map!
     */
    myfaces._JSF2Utils.getFormMap = function(/*Node*/actionForm, /*map*/ params) {
        var formElements = actionForm.elements;
        // 1. build up formParams
        var formParams = {};
        var viewStateProcessed = false;
        if (formElements)
        {
            for (var elementIndex = 0; elementIndex < formElements.length; elementIndex++)
            {
                var input = formElements[elementIndex];
                // todo: do not post values for non-triggering submit buttons
                // TRINIDAD-874 skip input.type="submit" fields
                if (input.name && !input.disabled && !(input.tagName == "INPUT" && input.type == "submit"))
                {
                    if (input.options)
                    {
                        formParams[input.name] = new Array();
                        for (var j = 0; j < input.options.length; j++)
                        {
                            var option = input.options[j];
                            if (option.selected)
                            {
                                var optionValue = (option.value === null) ?
                                option.text : option.value;
                                formParams[input.name].push(optionValue);
                            }
                        }
                    }
                    // if this happens to be an unselected checkbox or radio then
                    // skip it. Otherwise, if it is any other form control, or it is
                    // a selected checkbox or radio than add it:
                    else if (!((input.type == "checkbox" ||
                        input.type == "radio") &&
                    !input.checked))
                    {
                        // the value might already have been set (because of a
                        // multi-select checkbox group:
                        var current = formParams[input.name];
                        if (current)
                        {
                            // the value has already been set, so we need to create an array
                            // and push both values into the array.
                            // first check to see if we already have an array:
                            if (!current.join)
                            {
                                // we don't have an array so create one:
                                var list = new Array();
                                list.push(current);
                                formParams[input.name] = list;
                                current = list;
                            }
                            // we already have an array, so add the new value to the array:
                            current.push(input.value);
                        }
                        else
                        {
                            formParams[input.name] = input.value;
                        }
                    }
                }
            }
        }
        // 2. override formParams with params
        for (var key in params)
        {
            var value = params[key];
            formParams[key] = params[key];
        }
        return formParams;
    };


    myfaces._JSF2Utils.getPostbackContentFromMap = function(/*map*/ sourceMap) {
        var content = "";
        for (var key in sourceMap)
        {
            var paramValue = sourceMap[key];
            if (paramValue != null)
            {
                // If it's an array...
                if (paramValue.join)
                {
                    var array = paramValue;
                    for (var i = 0; i < array.length; i++)
                    {
                        content = myfaces._JSF2Utils._appendUrlFormEncoded(content, key, array[i]);
                    }
                }
                else
                {
                    content = myfaces._JSF2Utils._appendUrlFormEncoded(content, key, paramValue);
                }
            }
        }
        return content;

    };

    /**
     * fetches the encoding content of a single form element
     * and attaches the params to the resulting map
     */
    /*static*/
    myfaces._JSF2Utils.getPostbackContent = function(/*Node*/actionForm, /*params*/ params)
    {
        var formParams = myfaces._JSF2Utils.getFormMap(actionForm, params);
        // 3. create form submit payload
        return myfaces._JSF2Utils.getPostbackContentFromMap(formParams);
    }
    /*helper to append the encoded url params*/
    /*static*/
    myfaces._JSF2Utils._appendUrlFormEncoded = function(
        buffer,
        key,
        value)
        {
        if (buffer.length > 0)
        {
            buffer = buffer + "&";
        }
        return buffer + key + "=" + value.toString().replace(/\%/g, '%25')
        .replace(/\+/g, '%2B')
        .replace(/\//g, '%2F')
        .replace(/\&/g, '%26')
        .replace(/\"/g, '%22')
        .replace(/\'/g, '%27');
    }
}
