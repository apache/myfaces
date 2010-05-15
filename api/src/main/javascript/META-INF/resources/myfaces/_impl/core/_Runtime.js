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


/**
 * Runtime/Startup class
 * this is the central class which initializes all base mechanisms
 * used by the rest of the system such as
 * a) namespacing system
 * b) browser detection
 * c) loose configuration coupling
 * d) utils methods to fetch the implementation
 * e) ajaxed script loading
 * f) global eval (because it is used internally)
 *
 * Note this class is self contained and must!!! be loaded
 * as absolute first class before going into anything else
 *
 *
 */
if ('undefined' == typeof myfaces || myfaces == null) {
    /**
     * A simple provide function
     * fixing the namespaces
     */
    /*originally we had it at org.apache.myfaces, but we are now down to myfaces since the openajax seems to have problems registering more than a root domain and org is not only apache specific*/
    myfaces = new Object();
}

if ('undefined' == typeof(myfaces._impl) || null == myfaces._impl) {
    myfaces._impl = new Object();
}

if ('undefined' == typeof(myfaces._impl.core) || null == myfaces._impl.core) {
    myfaces._impl.core = new Object();
}

//now this is the only time we have to do this cascaded and manually
//for the rest of the classes our reserveNamespace function will do the trick
if ('undefined' == typeof  myfaces._impl.core._Runtime || myfaces._impl.core._Runtime == null) {
    myfaces._impl.core._Runtime = new Object();
    //the rest of the namespaces can be handled by our namespace feature

    /**
     * global eval on scripts
     *
     * usage return myfaces._impl.core._Runtime.globalEval('myvar.myvar2;');
     *
     */
    myfaces._impl.core._Runtime.globalEval = function(code) {
        //chrome as a diferent global eval, thanks for pointing this out
        //TODO add a config param which allows to evaluate global scripts even if the call
        //is embedded in an iframe
        if (myfaces._impl.core._Runtime.browser.isIE && window.execScript) {
            //execScript definitely only for IE otherwise we might have a custom
            //window extension with undefined behavior on our necks
            return window.execScript(code);

        } else if (undefined != typeof (window.eval) && null != window.eval) {

            //fix for a Mozilla bug, Mozilla prevents, that the window is properly applied
            //the former approach was to scope an outer anonymous function but the scoping is not necessary
            //Mozilla behaves correctly if you just add an outer function, then the window scope is again
            //accepted as the real scope
            var func = function () {
                return window.eval.call(window, code);
            };
            return func();
        }
        //we probably have covered all browsers, but this is a safety net which might be triggered
        //by some foreign browser which is not covered by the above cases
        return eval.call(window, code);
    };

    /**
     * applies an object to a namespace
     * basically does what bla.my.name.space = obj does
     * note we cannot use var myNameSpace = fetchNamespace("my.name.space")
     * myNameSpace = obj because the result of fetch is already the object
     * which the namespace points to, hence this function
     *
     * @param nameSpace the namespace to be assigned to
     * @param obj the  object to be assigned
     */
    myfaces._impl.core._Runtime.applyToGlobalNamespace = function(nameSpace, obj) {
        var splittedNamespace = nameSpace.split(/\./);
        if (splittedNamespace.length == 1) {
            window[namespace] = obj;
            return null;
        }
        var parent = splittedNamespace.slice(0, splittedNamespace.length - 1);
        var child = splittedNamespace[splittedNamespace.length - 1];
        var parentNamespace = myfaces._impl.core._Runtime.fetchNamespace(parent.join("."));
        parentNamespace[child] = obj;
    };

    /**
     * fetches the object the namespace points to
     * @param nameSpace the namespace which has to be fetched
     * @return the object the namespace points to or null if nothing is found
     */
    myfaces._impl.core._Runtime.fetchNamespace = function(nameSpace) {
        try {
            var origNamespace = nameSpace;
            nameSpace = myfaces._impl.core._Runtime.globalEval("window." + nameSpace);
            if ('undefined' == typeof nameSpace || null == nameSpace) {
                //ie in any version does not like that particularily
                //we do it the hard way now
                nameSpace = origNamespace.split(/\./);
                var currentElem = window;
                var namespaceLen = nameSpace.length;

                for (var cnt = 0; cnt < namespaceLen; cnt++) {
                    currentElem = currentElem[nameSpace[cnt]];
                    if ('undefined' == typeof currentElem || null == currentElem) {
                        return null;
                    }
                }
                return currentElem;
            }
            return nameSpace;
        } catch (e) {/*wanted*/
        }
        return null;
    };

    /**
     * Backported from dojo
     * a failsafe string determination method
     * (since in javascript String != "" typeof alone fails!)
     * @param it {|Object|} the object to be checked for being a string
     * @return true in case of being a string false otherwiseÊ
     */
    myfaces._impl.core._Runtime.isString = function(/*anything*/ it) {
        //	summary:
        //		Return true if it is a String
        return !!arguments.length && it != null && (typeof it == "string" || it instanceof String); // Boolean
    };

    /**
     * reserves a namespace in the specific scope
     *
     * usage:
     * if(myfaces._impl.core._Runtime.reserve("org.apache.myfaces.MyUtils")) {
     *      org.apache.myfaces.MyUtils = function() {
     *      }
     * }
     *
     * reserves a namespace and if the namespace is new the function itself is reserved
     *
     *
     *
     * or:
     * myfaces._impl.core._Runtime.reserve("org.apache.myfaces.MyUtils", function() { .. });
     *
     * reserves a namespace and if not already registered directly applies the function the namespace
     *
     * @param {|String|} nameSpace
     * @returns true if it was not provided
     * false otherwise for further action
     */
    myfaces._impl.core._Runtime.reserveNamespace = function(nameSpace, reservationFunction) {
        var _RT = myfaces._impl.core._Runtime;
        if (!_RT.isString(nameSpace)) {
            throw Error("Namespace must be a string with . as delimiter");
        }
        if (null != _RT.fetchNamespace(nameSpace)) {
            return false;
        }
        var namespaceEntries = nameSpace.split(/\./);
        var currentNamespace = window;
        for (var cnt = 0; cnt < namespaceEntries.length; cnt++) {
            var subNamespace = namespaceEntries[cnt];
            if ('undefined' == typeof currentNamespace[subNamespace]) {
                currentNamespace[subNamespace] = {};
            }
            if (cnt == namespaceEntries.length - 1 && 'undefined' != typeof reservationFunction && null != reservationFunction) {
                currentNamespace[subNamespace] = reservationFunction;
            }
            currentNamespace = currentNamespace[subNamespace];
        }

        return true;
    };

    myfaces._impl.core._Runtime.browserDetection = function() {
        /**
         * browser detection code
         * cross ported from dojo 1.2
         *
         * dojos browser detection code is very sophisticated
         * hence we port it over it allows a very fine grained detection of
         * browsers including the version number
         * this however only can work out if the user
         * does not alter the user agent, which they normally dont!
         *
         * the exception is the ie detection which relies on specific quirks in ie
         */
        var n = navigator;
        var dua = n.userAgent,
                dav = n.appVersion,
                tv = parseFloat(dav);

        myfaces._impl.core._Runtime.browser = {};
        var d = myfaces._impl.core._Runtime.browser;

        if (dua.indexOf("Opera") >= 0) {
            myfaces._impl.core._Runtime.isOpera = tv;
        }
        if (dua.indexOf("AdobeAIR") >= 0) {
            d.isAIR = 1;
        }
        d.isKhtml = (dav.indexOf("Konqueror") >= 0) ? tv : 0;
        d.isWebKit = parseFloat(dua.split("WebKit/")[1]) || undefined;
        d.isChrome = parseFloat(dua.split("Chrome/")[1]) || undefined;

        // safari detection derived from:
        //		http://developer.apple.com/internet/safari/faq.html#anchor2
        //		http://developer.apple.com/internet/safari/uamatrix.html
        var index = Math.max(dav.indexOf("WebKit"), dav.indexOf("Safari"), 0);
        if (index && !d.isChrome) {
            // try to grab the explicit Safari version first. If we don't get
            // one, look for less than 419.3 as the indication that we're on something
            // "Safari 2-ish".
            d.isSafari = parseFloat(dav.split("Version/")[1]);
            if (!d.isSafari || parseFloat(dav.substr(index + 7)) <= 419.3) {
                d.isSafari = 2;
            }
        }

        //>>excludeStart("webkitMobile", kwArgs.webkitMobile);
        if (dua.indexOf("Gecko") >= 0 && !d.isKhtml && !d.isWebKit) {
            d.isMozilla = d.isMoz = tv;
        }
        if (d.isMoz) {
            //We really need to get away from this. Consider a sane isGecko approach for the future.
            d.isFF = parseFloat(dua.split("Firefox/")[1] || dua.split("Minefield/")[1] || dua.split("Shiretoko/")[1]) || undefined;
        }
        if (document.all && !d.isOpera) {
            d.isIE = parseFloat(dav.split("MSIE ")[1]) || undefined;
            //In cases where the page has an HTTP header or META tag with
            //X-UA-Compatible, then it is in emulation mode, for a previous
            //version. Make sure isIE reflects the desired version.
            //document.documentMode of 5 means quirks mode.
            if (d.isIE >= 8 && document.documentMode != 5) {
                d.isIE = document.documentMode;
            }
        }
    };

    /**
     * fetches a global config entry
     * @param {String} configName the name of the configuration entry
     * @param {Object} defaultValue
     *
     * @return either the config entry or if none is given the default value
     */
    myfaces._impl.core._Runtime.getGlobalConfig = function(configName, defaultValue) {
        /*use(myfaces._impl._util)*/
        var _RT = myfaces._impl.core._Runtime;

        if (_RT.exists(myfaces, "config") && _RT.exists(myfaces.config, configName)) {
            return myfaces.config[configName];
        }
        return defaultValue;
    };

    /**
     * check if an element exists in the root
     */
    myfaces._impl.core._Runtime.exists = function(root, name) {
        if ('undefined' == typeof root || null == root) {
            return false;
        }

        //initial condition root set element not set or null
        //equals to element exists
        if ('undefined' == typeof name || null == name) {
            return true;
        }
        //crossported from the dojo toolkit
        // summary: determine if an object supports a given method
        // description: useful for longer api chains where you have to test each object in the chain
        var p = name.split(".");
        var len = p.length;
        for (var i = 0; i < len; i++) {
            //the original dojo code here was false because
            //they were testing against ! which bombs out on exists
            //which has a value set to false
            // (TODO send in a bugreport to the Dojo people)

            if ('undefined' == typeof root[p[i]]) {
                return false;
            } // Boolean
            root = root[p[i]];
        }
        return true; // Boolean
    };

    /**
     * Convenience method
     * to fetch the implementation from
     * our lazily binding configuration system
     */
    myfaces._impl.core._Runtime.getImpl = function() {
        myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core._jsfImpl);
    };

    /**
     * gets the local or global options with local ones having higher priority
     * if no local or global one was found then the default value is given back
     *
     * @param {String} configName the name of the configuration entry
     * @param {String} localOptions the local options root for the configuration myfaces as default marker is added implicitely
     *
     * @param {Object} defaultValue
     *
     * @return either the config entry or if none is given the default value
     */
    myfaces._impl.core._Runtime.getLocalOrGlobalConfig = function(localOptions, configName, defaultValue) {
        /*use(myfaces._impl._util)*/
        var _RT = myfaces._impl.core._Runtime;

        var globalOption = _RT.getGlobalConfig(configName, defaultValue);
        if (!_RT.exists(localOptions, "myfaces") || !_RT.exists(localOptions.myfaces, configName)) {
            return globalOption;
        }
        return localOptions.myfaces[configName];
    };

    /**
     * encapsulated xhr object which tracks down various implementations
     * of the xhr object in a browser independent fashion
     * (ie pre 7 used to have non standard implementations because
     * the xhr object standard came after IE had implemented it first
     * newer ie versions adhere to the standard and all other new browsers do anyway)
     */
    myfaces._impl.core._Runtime.getXHRObject = function() {
        if ('undefined' != typeof XMLHttpRequest && null != XMLHttpRequest) {
            return new XMLHttpRequest();
        }
        //IE
        try {
            return new ActiveXObject("Msxml2.XMLHTTP");
        } catch (e) {

        }
        return new ActiveXObject('Microsoft.XMLHTTP');
    };

    /**
     * [STATIC]
     * loads a script and executes it under a global scope
     * @param {String} src the source to be loaded
     * @param {String} type the mime type of the script (currently ignored
     * but in the long run it will be used)
     */
    myfaces._impl.core._Runtime.loadScript = function(src, type, defer, charSet) {
        var xhr = myfaces._impl.core._Runtime.getXHRObject();
        xhr.open("GET", src, false);

        if ('undefined' != typeof charSet && null != charSet) {
            xhr.setRequestHeader("Content-Type", "application/x-javascript; charset:" + charSet);
        }

        xhr.send(null);

        //since we are synchronous we do it after not with onReadyStateChange
        if (xhr.readyState == 4) {
            if (xhr.status == 200) {
                //defer also means we have to process after the ajax response
                //has been processed
                //we can achieve that with a small timeout, the timeout
                //triggers after the processing is done!
                if (!defer) {
                    myfaces._impl.core._Runtime.globalEval(xhr.responseText);
                } else {
                    setTimeout(function() {
                        myfaces._impl.core._Runtime.globalEval(xhr.responseText);
                    }, 1);
                }
            } else {
                throw Error(xhr.responseText);
            }
        } else {
            throw Error("Loading of script " + src + " failed ");
        }
    };

    /**
     * Extends a class and puts a singleton instance at the reserved namespace instead
     * of its original class
     *
     * @param {function|String} newClass either a unnamed function which can be assigned later or a namespace
     * @param {function} extendsClass the function class to be extended
     * @param {Object} prototypeFunctions (Map) an optional map of prototype functions which in case of overwriting a base function get an inherited method
     * @param {Object} prototypeFunctions (Map) an optional map of normal namespace functions which are attached directly to the function of newClass instead of its prototype object
     */
    myfaces._impl.core._Runtime.singletonExtendClass = function(newClass, extendsClass, prototypeFunctions, namespaceFunctions) {

        var _RT = myfaces._impl.core._Runtime;
        if (!_RT.isString(newClass)) {
            throw Error("New class namespace must be of type string for static initialisation");
        }
        //namespace already declared we do not do anything further
        if (_RT.fetchNamespace(newClass)) {
            return null;
        }

        var initializer = myfaces._impl.core._Runtime.extendClass(newClass, extendsClass, prototypeFunctions, namespaceFunctions);
        if (initializer != null) {
            _RT.applyToGlobalNamespace(newClass, new initializer());
        }
    };

    /**
     * prototype based delegation inheritance
     *
     * implements prototype delegaton inheritance dest <- a
     *
     * usage var newClass = myfaces._impl.core._Runtime.extends(
     * function (var1, var2) {
     *  this.callSuper("constructor", var1,var2);
     * };
     * ,origClass);
     * newClass.prototype.myMethod = function(arg1) {
     *      this.callSuper("myMethod", arg1,"hello world");

     other option
     myfaces._impl._core._Runtime.extends("myNamespace.newClass", parent, {
     init: function() {constructor...},
     method1: function(f1, f2) {},
     method2: function(f1, f2,f3)

     });
     *
     * @param {function|String} newClass either a unnamed function which can be assigned later or a namespace
     * @param {function} extendsClass the function class to be extended
     * @param {Object} prototypeFunctions (Map) an optional map of prototype functions which in case of overwriting a base function get an inherited method
     * @param {Object} prototypeFunctions (Map) an optional map of normal namespace functions which are attached directly to the function of newClass instead of its prototype object
     *
     * To explain further
     * prototype functions:
     *  newClass.prototype.<prototypeFunction>
     * namspace function
     *  newClass.<namespaceFunction> = function() {...}
     */

    myfaces._impl.core._Runtime.extendClass = function(newClass, extendsClass, prototypeFunctions) {
        var _RT = myfaces._impl.core._Runtime;

        if ('function' != typeof newClass) {

            var constructor = null;
            if ('undefined' != typeof prototypeFunctions && null != prototypeFunctions) {
                constructor = ('undefined' != typeof null != prototypeFunctions['constructor_'] && null != prototypeFunctions['constructor_']) ? prototypeFunctions['constructor_'] : function() {
                };
            } else {
                constructor = function() {
                };
            }
            if (!_RT.reserveNamespace(newClass, constructor)) {
                return null;
            }
            newClass = _RT.fetchNamespace(newClass);
        }

        if (null != extendsClass) {
            newClass.prototype = new extendsClass;
            newClass.prototype.constructor = newClass;
            newClass.prototype.parent = extendsClass.prototype;

            newClass.prototype._callSuper = function(methodName) {
                var passThrough = (arguments.length == 1) ? [] : Array.prototype.slice.call(arguments, 1);
                this.parent[methodName].apply(this, passThrough);
            };
        }

        //we now map the function map in
        if ('undefined' != typeof prototypeFunctions && null != prototypeFunctions) {
            for (var key in prototypeFunctions) {
                newClass.prototype[key] = prototypeFunctions[key];
                //we also can apply a direct _inherited method if the method overwrites an existing one
                //http://ejohn.org/blog/simple-javascript-inheritance/ i don not eliminate it multiple calls to super
                //can happen, this is the way dojo does it

                if (null != extendsClass && 'function' == typeof newClass.prototype.parent[key]) {
                    //we now aop a decorator function on top of everything,
                    //to make sure we have super set while it is executing
                    var assignedFunction = newClass.prototype[key];

                    newClass.prototype[key] = function() {
                        var oldSuper = newClass.prototype["_inherited"];
                        newClass.prototype["_inherited"] = function() {
                            this.parent[key].apply(this, arguments);
                        };
                        try {
                            return assignedFunction.apply(this, arguments);
                        } finally {
                            newClass.prototype["_inherited"] = oldSuper;
                        }
                    }
                }
            }
        }
        if ('undefined' != typeof namespaceFunctions && null != namespaceFunctions) {
            for (var key in namespaceFunctions) {
                newClass[key] = namespaceFunctions[key];
            }
        }
        return newClass;
    };
    
    /**
     * determines if the embedded scripts have to be evaled manually
     * @return true if a browser combination is given which has to
     * do a manual eval
     * which is currently ie > 5.5, chrome, khtml, webkit safari
     *
     */
    myfaces._impl.core._Runtime.isManualScriptEval = function() {
        var _RT = myfaces._impl.core._Runtime;
        var browser = myfaces._impl.core._Runtime.browser;
        //TODO test this with various browsers so that we have auto eval wherever possible
        //
        //tested currently safari, ie, firefox, opera
        return (_RT.exists(browser, "isIE") &&
                ( browser.isIE > 5.5)) ||
                (_RT.exists(browser, "isKhtml") &&
                        (browser.isKhtml > 0)) ||
                (_RT.exists(browser, "isWebKit") &&
                        (browser.isWebKit > 0)) ||
                (_RT.exists(browser, "isSafari") &&
                        (browser.isSafari > 0));

        //another way to determine this without direct user agent parsing probably could
        //be to add an embedded script tag programmatically and check for the script variable
        //set by the script if existing, the add went through an eval if not then we
        //have to deal with it ourselves, this might be dangerous in case of the ie however
        //so in case of ie we have to parse for all other browsers we can make a dynamic
        //check if the browser does auto eval

    };

    myfaces._impl.core._Runtime.browserDetection();
}