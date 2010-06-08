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
/** @namespace myfaces._impl.core._Runtime*/

(!window.myfaces) ? window.myfaces = {} : null;
(!myfaces._impl) ? myfaces._impl = {} : null;
(!myfaces._impl.core) ? myfaces._impl.core = {} : null;
//now this is the only time we have to do this cascaded and manually
//for the rest of the classes our reserveNamespace function will do the trick
//Note, this class uses the classical closure approach (to save code)
//it cannot be inherited by our inheritance mechanism, but must be delegated
//if you want to derive from it
//closures and prototype inheritance do not mix, closures and delegation however do
if (!myfaces._impl.core._Runtime) {
    myfaces._impl.core._Runtime = new function() {
        //the rest of the namespaces can be handled by our namespace feature
        //helper to avoid unneeded hitches
        var _this = this;
        /**
         * global eval on scripts
         *
         * usage return this.globalEval('myvar.myvar2;');
         *
         */
        this.globalEval = function(code) {
            //chrome as a diferent global eval, thanks for pointing this out
            //TODO add a config param which allows to evaluate global scripts even if the call
            //is embedded in an iframe
            if (_this.browser.isIE && window.execScript) {
                //execScript definitely only for IE otherwise we might have a custom
                //window extension with undefined behavior on our necks
                //window.execScript does not return anything
                //on htmlunit it return "null object"
                return window.execScript(code);
            } else if (window.eval) {

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
         * @param nms the namespace to be assigned to
         * @param obj the  object to be assigned
         */
        this.applyToGlobalNamespace = function(nms, obj) {
            var splitted = nms.split(/\./);
            if (splitted.length == 1) {
                window[namespace] = obj;
                return;
            }
            var parent = splitted.slice(0, splitted.length - 1);
            var child = splitted[splitted.length - 1];
            var parentNamespace = _this.fetchNamespace(parent.join("."));
            parentNamespace[child] = obj;
        };

        /**
         * fetches the object the namespace points to
         * @param nms the namespace which has to be fetched
         * @return the object the namespace points to or null if nothing is found
         */
        this.fetchNamespace = function(nms) {
            if ('undefined' == typeof nms || null == nms) {
                return null;
            }

            var ret = null;
            try {
                ret = _this.globalEval("window." + nms);
                //namespace could point to numeric or boolean hence full
                //save check

            } catch (e) {/*wanted*/
            }
            //ie fallback path because it cannot eval namespaces
            //ie in any version does not like that particularily
            //we do it the hard way now
            if ('undefined' != typeof ret && null != ret) {
                return ret;
            }
            nms = nms.split(/\./);
            ret = window;
            var len = nms.length;

            for (var cnt = 0; cnt < len; cnt++) {
                ret = ret[nms[cnt]];
                if ('undefined' == typeof ret || null == ret) {
                    return null;
                }
            }
            return ret;

        };

        /**
         * Backported from dojo
         * a failsafe string determination method
         * (since in javascript String != "" typeof alone fails!)
         * @param it {|Object|} the object to be checked for being a string
         * @return true in case of being a string false otherwise
         */
        this.isString = function(/*anything*/ it) {
            //	summary:
            //		Return true if it is a String
            return !!arguments.length && it != null && (typeof it == "string" || it instanceof String); // Boolean
        };

        /**
         * reserves a namespace in the specific scope
         *
         * usage:
         * if(this.reserve("org.apache.myfaces.MyUtils")) {
         *      org.apache.myfaces.MyUtils = function() {
         *      }
         * }
         *
         * reserves a namespace and if the namespace is new the function itself is reserved
         *
         *
         *
         * or:
         * this.reserve("org.apache.myfaces.MyUtils", function() { .. });
         *
         * reserves a namespace and if not already registered directly applies the function the namespace
         *
         * @param {|String|} nms
         * @returns true if it was not provided
         * false otherwise for further action
         */
        this.reserveNamespace = function(nms, obj) {

            if (!_this.isString(nms)) {
                throw Error("Namespace must be a string with . as delimiter");
            }
            if (null != _this.fetchNamespace(nms)) {
                return false;
            }
            var entries = nms.split(/\./);
            var currNms = window;
            for (var cnt = 0; cnt < entries.length; cnt++) {
                var subNamespace = entries[cnt];
                if ('undefined' == typeof currNms[subNamespace]) {
                    currNms[subNamespace] = {};
                }
                if (cnt == entries.length - 1 && obj) {
                    currNms[subNamespace] = obj;
                }
                currNms = currNms[subNamespace];
            }

            return true;
        };

        /**
         * check if an element exists in the root
         * also allows to check for subelements
         * usage
         * this.exists(rootElem,"my.name.space")
         * @param {Object} root the root element
         * @param {String} subNms the namespace
         */
        this.exists = function(root, subNms) {
            if (!root) {
                return false;
            }

            //initial condition root set element not set or null
            //equals to element exists
            if (!subNms) {
                return true;
            }

            //special condition subnamespace exists as full blown key with . instead of function map
            if ('undefined' != typeof root[subNms]) {
                return true;
            }

            //crossported from the dojo toolkit
            // summary: determine if an object supports a given method
            // description: useful for longer api chains where you have to test each object in the chain
            var p = subNms.split(".");
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
         * fetches a global config entry
         * @param {String} configName the name of the configuration entry
         * @param {Object} defaultValue
         *
         * @return either the config entry or if none is given the default value
         */
        this.getGlobalConfig = function(configName, defaultValue) {
            /*use(myfaces._impl._util)*/

            if (_this.exists(myfaces, "config") && _this.exists(myfaces.config, configName)) {
                return myfaces.config[configName];
            }
            return defaultValue;
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
        this.getLocalOrGlobalConfig = function(localOptions, configName, defaultValue) {
            /*use(myfaces._impl._util)*/

            return (!_this.exists(localOptions, "myfaces." + configName)) ? _this.getGlobalConfig(configName, defaultValue) : localOptions.myfaces[configName];
        };

        /**
         * encapsulated xhr object which tracks down various implementations
         * of the xhr object in a browser independent fashion
         * (ie pre 7 used to have non standard implementations because
         * the xhr object standard came after IE had implemented it first
         * newer ie versions adhere to the standard and all other new browsers do anyway)
         *
         * @return the xhr object according to the browser type
         */
        this.getXHRObject = function() {
            //since this is a global object ie hates it if we do not check for undefined
            if (window.XMLHttpRequest) {
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
         * loads a script and executes it under a global scope
         * @param {String} src  the source of the script
         * @param {String} type the type of the script
         * @param {Boolean} defer  defer true or false, same as the javascript tag defer param
         * @param {String} charSet the charset under which the script has to be loaded
         */
        this.loadScriptEval = function(src, type, defer, charSet) {
            var xhr = _this.getXHRObject();
            xhr.open("GET", src, false);

            if (charSet) {
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
                        _this.globalEval(xhr.responseText.replace("\n", "\r\n") + "\r\n//@ sourceURL=" + src);
                    } else {
                        setTimeout(function() {
                            _this.globalEval(xhr.responseText + "\r\n//@ sourceURL=" + src);
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
         * load script functionality which utilizes the browser internal
         * script loading capabilities
         *
         * @param {String} src  the source of the script
         * @param {String} type the type of the script
         * @param {Boolean} defer  defer true or false, same as the javascript tag defer param
         * @param {String} charSet the charset under which the script has to be loaded
         */
        this.loadScriptByBrowser = function(src, type, defer, charSet) {
            //if a head is already present then it is safer to simply
            //use the body, some browsers prevent head alterations
            //after the first initial rendering
            var position = document.getElementsByTagName("body").length ? "body" : "head";

            try {
                var holder = document.getElementsByTagName(position)[0];
                if ('undefined' == typeof holder || null == holder)
                {
                    holder = document.createElement(position);
                    var html = document.getElementsByTagName("html");
                    html.appendChild(holder);
                }
                var script = document.createElement("script");
                script.type = type || "text/javascript";
                script.src = src;
                if (charSet) {
                    script.charset = charSet;
                }
                if (defer) {
                    script.defer = defer;
                }
                holder.appendChild(script);
            } catch (e) {

                return false;
            }

            return true;
        };

        this.loadScript = function(src, type, defer, charSet) {
            if (!_this.loadScriptByBrowser(src, type, defer, charSet)) {
                _this.loadScriptEval(src, type, defer, charSet);
            }
        };

        //Base Patterns, Inheritance, Delegation and Singleton

        /**
         * delegation pattern
         * usage:
         * this.delegateObject("my.name.space", delegate,
         * {
         *  constructor_ :function(bla, bla1) {
         *      this._callDelegate("constructor", bla1);
         *  },
         *  myFunc: function(yyy) {
         *      DoSomething;
         *      this._callDelegate("someOtherFunc", yyyy);
         *  }, null
         * });
         *
         * or
         * usage var newClass = this.delegateObject(
         * function (var1, var2) {
         *  this._callDelegate("constructor", var1,var2);
         * };
         * ,delegateObject);
         * newClass.prototype.myMethod = function(arg1) {
         *      this._callDelegate("myMethod", arg1,"hello world");
         *
         *
         * @param newCls the new class name to be generated
         * @param delegateObj the delegation object
         * @param protoFuncs the prototype functions which should be attached
         * @param nmsFuncs the namespace functions which should be attached to the namespace
         */
        this.delegateObj = function(newCls, delegateObj, protoFuncs, nmsFuncs) {
            if (!_this.isString(newCls)) {
                throw Error("new class namespace must be of type String");
            }

            if ('function' != typeof newCls) {
                newCls = _reserveClsNms(newCls, protoFuncs);
                if (!newCls) return null;
            }

            //central delegation mapping core
            var proto = newCls.prototype;

            //the trick here is to isolate the entries to bind the
            //keys in a private scope see
            //http://www.ruzee.com/blog/2008/12/javascript-inheritance-via-prototypes-and-closures
            for (var key in delegateObj) (function(key, delFn) {
                //The isolation is needed otherwise the last _key assigend would be picked
                //up internally
                if (key && typeof delFn == "function") {
                    proto[key] = function(/*arguments*/) {
                        var ret = delFn.apply(delegateObj, arguments);
                        if ('undefined' != ret) return ret;
                    };
                }
            })(key, delegateObj[key]);

            proto._delegateObj = delegateObj;
            proto.constructor = newCls;

            proto._callDelegate = function(methodName) {
                var passThrough = (arguments.length == 1) ? [] : Array.prototype.slice.call(arguments, 1);
                var ret = this._delegateObj[methodName].apply(this._delegateObj, passThrough);
                if ('undefined' != ret) return ret;
            };

            //we now map the function map in
            _applyFuncs(newCls, protoFuncs, true);
            _applyFuncs(newCls, nmsFuncs, false);

            return newCls;
        };

        /**
         * prototype based delegation inheritance
         *
         * implements prototype delegaton inheritance dest <- a
         *
         * usage var newClass = this.extends( function (var1, var2) {
         *                                          this._callSuper("constructor", var1,var2);
         *                                     };
         *                                  ,origClass);
         *
         *       newClass.prototype.myMethod = function(arg1) {
         *              this._callSuper("myMethod", arg1,"hello world");
         *       ....
         *
         * other option
         *
         * myfaces._impl._core._Runtime.extends("myNamespace.newClass", parent, {
         *                              init: function() {constructor...},
         *                              method1: function(f1, f2) {},
         *                              method2: function(f1, f2,f3) {
         *                                  this._callSuper("method2", F1,"hello world");
         *                              }
         *              });
         *
         * @param {function|String} newCls either a unnamed function which can be assigned later or a namespace
         * @param {function} extendCls the function class to be extended
         * @param {Object} protoFuncs (Map) an optional map of prototype functions which in case of overwriting a base function get an inherited method
         *
         * To explain further
         * prototype functions:
         *  newClass.prototype.<prototypeFunction>
         * namspace function
         *  newClass.<namespaceFunction> = function() {...}
         */

        this.extendClass = function(newCls, extendCls, protoFuncs, nmsFuncs) {
            if (!_this.isString(newCls)) {
                throw Error("new class namespace must be of type String");
            }

            if ('function' != typeof newCls) {
                newCls = _reserveClsNms(newCls, protoFuncs);
                if (!newCls) return null;
            }
            if (extendCls._mfProto) {
                extendCls = extendCls._mfProto;
            }

            if ('undefined' != typeof extendCls && null != extendCls) {
                newCls.prototype = new extendCls;
                newCls.prototype.constructor = newCls;
                newCls.prototype._parentCls = extendCls.prototype;

                newCls.prototype._callSuper = function(methodName) {
                    var passThrough = (arguments.length == 1) ? [] : Array.prototype.slice.call(arguments, 1);
                    this._parentCls[methodName].apply(this, passThrough);
                };
            }

            //we now map the function map in
            _applyFuncs(newCls, protoFuncs, true);
            //we could add inherited but that would make debugging harder
            //see http://www.ruzee.com/blog/2008/12/javascript-inheritance-via-prototypes-and-closures on how to do it

            _applyFuncs(newCls, nmsFuncs, false);

            return newCls;
        };

        /**
         * Extends a class and puts a singleton instance at the reserved namespace instead
         * of its original class
         *
         * @param {function|String} newCls either a unnamed function which can be assigned later or a namespace
         * @param {function} extendsCls the function class to be extended
         * @param {Object} protoFuncs (Map) an optional map of prototype functions which in case of overwriting a base function get an inherited method
         */
        this.singletonExtendClass = function(newCls, extendsCls, protoFuncs, nmsFuncs) {
            return _makeSingleton(this.extendClass, newCls, extendsCls, protoFuncs, nmsFuncs);
        };

        /**
         * delegation pattern which attached singleton generation
         *
         * @param newCls the new namespace object to be generated as singletoin
         * @param delegateObj the object which has to be delegated
         * @param protoFuncs the prototype functions which are attached on prototype level
         * @param nmsFuncs the functions which are attached on the classes namespace level
         */
        this.singletonDelegateObj = function(newCls, delegateObj, protoFuncs, nmsFuncs) {
            return _makeSingleton(this.delegateObj, newCls, delegateObj, protoFuncs, nmsFuncs);
        };

        //since the object is self contained and only
        //can be delegated we can work with real private
        //functions here, the other parts of the
        //system have to emulate them via _ prefixes
        var _makeSingleton = function(ooFunc, newCls, delegateObj, protoFuncs, nmsFuncs) {
            if (_this.fetchNamespace(newCls)) {
                return null;
            }
            var clazz = ooFunc(newCls + "._mfProto", delegateObj, protoFuncs, nmsFuncs);
            if (clazz != null) {
                _this.applyToGlobalNamespace(newCls, new clazz());
            }
            _this.fetchNamespace(newCls)["_mfProto"] = clazz;
        };

        //internal class namespace reservation depending on the type (string or function)
        var _reserveClsNms = function(newCls, protoFuncs) {
            var constr = null;
            if ('undefined' != typeof protoFuncs && null != protoFuncs) {
                constr = ('undefined' != typeof null != protoFuncs['constructor_'] && null != protoFuncs['constructor_']) ? protoFuncs['constructor_'] : function() {
                };
            } else {
                constr = function() {
                };
            }
            if (!_this.reserveNamespace(newCls, constr)) {
                return null;
            }
            newCls = _this.fetchNamespace(newCls);
            return newCls;
        };

        var _applyFuncs = function (newCls, funcs, proto)
        {
            if (funcs) {
                for (var key in funcs) {
                    //constructor already passed, callSuper already assigned
                    if ('undefined' == typeof key || null == key || key == "_callSuper") {
                        continue;
                    }
                    if (!proto)
                        newCls[key] = funcs[key];
                    else
                        newCls.prototype[key] = funcs[key];
                }
            }
        };
        
        /**
         * determines if the embedded scripts have to be evaled manually
         * @return true if a browser combination is given which has to
         * do a manual eval
         * which is currently ie > 5.5, chrome, khtml, webkit safari
         *
         */
        this.isManualScriptEval = function() {

            var d = _this.browser;

            return (_this.exists(d, "isIE") &&
                    ( d.isIE > 5.5)) ||
                    (_this.exists(d, "isKhtml") &&
                            (d.isKhtml > 0)) ||
                    (_this.exists(d, "isWebKit") &&
                            (d.isWebKit > 0)) ||
                    (_this.exists(d, "isSafari") &&
                            (d.isSafari > 0));

            //another way to determine this without direct user agent parsing probably could
            //be to add an embedded script tag programmatically and check for the script variable
            //set by the script if existing, the add went through an eval if not then we
            //have to deal with it ourselves, this might be dangerous in case of the ie however
            //so in case of ie we have to parse for all other browsers we can make a dynamic
            //check if the browser does auto eval

        };

        //initial browser detection, we encapsule it in a closure
        //to drop all temporary variables from ram as soon as possible
        (function() {
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

            _this.browser = {};
            var d = _this.browser;

            if (dua.indexOf("Opera") >= 0) {
                _this.isOpera = tv;
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

                /** @namespace document.documentMode */
                if (d.isIE >= 8 && document.documentMode != 5) {
                    d.isIE = document.documentMode;
                }
            }
        })();

    };
}
