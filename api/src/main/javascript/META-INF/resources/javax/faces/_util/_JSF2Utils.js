

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
function _provide_Org_Apache_Myfaces() {

    /**
     *
     * reserve the root namespace for myfaces
     */
    if ('undefined' != typeof OpenAjax && ('undefined' == typeof org || null == org || 'undefined' == typeof org.apache || 'undefined' == typeof org.apache.myfaces ||
        null == org.apache || null == org.apache.myfaces)) {
        if(undefined == typeof(org) || null == org) {
            OpenAjax.hub.registerLibrary("org.apache.myfaces", "myfaces.apache.org", "1.0", null);
        }

    }
    /*hub registration failed for whatever reason*/
    //TODO figure out how to reserve the namespace in a different form
    if('undefined' == typeof org || null == org) {
        org = new Object();
    }
    if('undefined' == typeof org.apache || null == org.apache) {
        org.apache = new Object();
    }
    if('undefined' == typeof org.apache.myfaces || null == org.apache.myfaces) {
        org.apache.myfaces = new Object();
    }

}

_provide_Org_Apache_Myfaces();

/**
 * Central internal JSF2 Utils with code used
 * by various aspects of the JSF2 Ajax subsystem
 * 
 * Note parts of the code were crossported from the dojo
 * javascript library (see license.txt for more details 
 * on the dojo bsd license)
 */
if ('undefined' == typeof(org.apache.myfaces._JSF2Utils) || null == org.apache.myfaces._JSF2Utils) {
    org.apache.myfaces._JSF2Utils = function() {
        }
    org.apache.myfaces._JSF2Utils._underTest = false;
    org.apache.myfaces._JSF2Utils.isUnderTest = function() {
        return this._underTest;
    }
    
   
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
    org.apache.myfaces._JSF2Utils._toArray = function(/*Object*/obj, /*Number?*/offset, /*Array?*/ startWith) {
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
     * Backported from dojo
     */
    org.apache.myfaces._JSF2Utils.isString = function(/*anything*/ it) {
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
    org.apache.myfaces._JSF2Utils.hitch = function(/*Object*/scope, /*Function|String*/method /*,...*/) {
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
        //	|	org.apache.myfaces._JSF2Utils.hitch(foo, "bar")();
        //		runs foo.bar() in the scope of foo
        //	example:
        //	|	org.apache.myfaces._JSF2Utils.hitch(foo, myFunction);
        //		returns a function that runs myFunction in the scope of foo
        if (arguments.length > 2) {
            return org.apache.myfaces._JSF2Utils._hitchArgs._hitchArgs.apply(org.apache.myfaces._JSF2Utils._hitchArgs, arguments); // Function
        }
        if (!method) {
            method = scope;
            scope = null;
        }
        if (this.isString(method)) {
            scope = scope || window || function() {};/*since we do not have dojo global*/
            if (!scope[method]) {
                throw(['org.apache.myfaces._JSF2Utils: scope["', method, '"] is null (scope="', scope, '")'].join(''));
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
    org.apache.myfaces._JSF2Utils.logWarning = function(varArgs)
    {
        if (window.console && console.warn)
            console.warn(arguments);
    // else???
    }
    // Logging helper for use in Firebug
    /*static*/
    org.apache.myfaces._JSF2Utils.logError = function(varArgs)
    {
        if (window.console && console.error)
            console.error(arguments);
    // else???
    }
    org.apache.myfaces._JSF2Utils._hitchArgs = function(scope, method /*,...*/) {
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
     * fetches the values of a form and returns
     * the name/value pairs as javascript map!
     */
    org.apache.myfaces._JSF2Utils.getFormMap = function(/*Node*/actionForm, /*map*/ params) {
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
    };
    /**
     * fetches the encoding content of a single form element
     * and attaches the params to the resulting map
     */
    /*static*/
    org.apache.myfaces._JSF2Utils.getPostbackContent = function(/*Node*/actionForm, /*params*/ params)
    {
        formParams = org.apache.myfaces._JSF2Utils.getFormMap(actionForm, params);
        // 3. create form submit payload
        var content = "";
        for (var key in formParams)
        {
            var paramValue = formParams[key];
            if (paramValue != null)
            {
                // If it's an array...
                if (paramValue.join)
                {
                    var array = paramValue;
                    for (var i = 0; i < array.length; i++)
                    {
                        content = org.apache.myfaces._JSF2Utils._appendUrlFormEncoded(content, key, array[i]);
                    }
                }
                else
                {
                    content = org.apache.myfaces._JSF2Utils._appendUrlFormEncoded(content, key, paramValue);
                }
            }
        }
        return content;
    }
    /*helper to append the encoded url params*/
    /*static*/
    org.apache.myfaces._JSF2Utils._appendUrlFormEncoded = function(
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
