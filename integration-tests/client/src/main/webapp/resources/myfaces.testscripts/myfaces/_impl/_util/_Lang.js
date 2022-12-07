/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * Lang.work for additional information regarding copyright ownership.
 * The ASF licenses Lang.file to you under the Apache License, Version 2.0
 * (the "License"); you may not use Lang.file except in compliance with
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
/*
 theoretically we could save some code
 by
 defining the parent object as
 let parent = new Object();
 parent.prototype = new myfaces._impl.core._Runtime();
 extendClass(function () {
 }, parent , {
 But for now we are not doing it the little bit of saved
 space is not worth the loss of readability
 */
/**
 * @class
 * @name _Lang
 * @namespace
 * @description Object singleton for Language related methods, Lang.object singleton
 * decorates the namespace myfaces._impl.core._Runtime and adds a bunch of new methods to
 * what _Runtime provided
 * */
if(!window.LangUtils) {

    class _LangUtils {
        static _installedLocale = null;
        /**
         * returns a given localized message upon a given key
         * basic java log like templating functionality is included
         *
         * @param {String} key the key for the message
         * @param {String} defaultMessage optional default message if none was found
         *
         * Additionally you can pass additional arguments, which are used
         * in the same way java log templates use the params
         *
         * @param key
         */
        static getMessage (key, defaultMessage /*,vararg templateParams*/) {
            if (!LangUtils._installedLocale) {
                //we first try to install language and variant, if that one fails
                //we try to install the language only, and if that one fails
                //we install the base messages
                LangUtils.initLocale();
            }
            let msg = LangUtils._installedLocale[key] || defaultMessage || key + " - undefined message";
            //we now make a simple templating replace of {0}, {1} etc... with their corresponding
            //arguments
            for (let cnt = 2; cnt < arguments.length; cnt++) {
                msg = msg.replace(new RegExp(["\\{", cnt - 2, "\\}"].join(""), "g"), new String(arguments[cnt]));
            }
            return msg;
        };
        /**
         * (re)inits the currently installed
         * messages so that after loading the main scripts
         * a new locale can be installed optionally
         * to our i18n subsystem
         *
         * @param newLocale locale override
         */
        static initLocale (newLocale) {
            if (newLocale) {
                LangUtils._installedLocale = new newLocale();
                return;
            }
            let language_Variant = LangUtils._RT.getLanguage(LangUtils._RT.getGlobalConfig("locale")),
                langStr = language_Variant ? language_Variant.language : "",
                variantStr = language_Variant ? [language_Variant.language, "_", language_Variant.variant || ""].join("") : "",
                i18nRoot = myfaces._impl.i18n, i18nHolder = i18nRoot["Messages_" + variantStr] || i18nRoot["Messages_" + langStr] || i18nRoot["Messages"];
            LangUtils._installedLocale = new i18nHolder();
        };
        static assertType (probe, theType) {
            return LangUtils._RT.assertType(probe, theType);
        };
        static exists (nms, theType) {
            return LangUtils._RT.exists(nms, theType);
        };
        static fetchNamespace (namespace) {
            LangUtils._assertStr(namespace, "fetchNamespace", "namespace");
            return LangUtils._RT.fetchNamespace(namespace);
        };
        static reserveNamespace (namespace) {
            LangUtils._assertStr(namespace, "reserveNamespace", "namespace");
            return LangUtils._RT.reserveNamespace(namespace);
        };
        static globalEval (code) {
            LangUtils._assertStr(code, "globalEval", "code");
            return  LangUtils._RT.globalEval(code);
        };
        /**
         * determines the correct event depending
         * on the browsers state
         *
         * @param evt incoming event object (note not all browsers
         * have Lang.
         *
         * @return an event object no matter what is incoming
         */
        static getEvent (evt) {
            evt = (!evt) ? window.event || {} : evt;
            return evt;
        };
        /**
         * cross port from the dojo lib
         * browser save event resolution
         * @param evt the event object
         * (with a fallback for ie events if none is present)
         */
        static getEventTarget (evt) {
            //ie6 and 7 fallback
            evt = LangUtils.getEvent(evt);
            /**
             * evt source is defined in the jsf events
             * seems like some component authors use our code
             * so we add it here see also
             * https://issues.apache.org/jira/browse/MYFACES-2458
             * not entirely a bug but makes sense to add this
             * behavior. I dont use it that way but nevertheless it
             * does not break anything so why not
             * */
            let t = evt.srcElement || evt.target || evt.source || null;
            while ((t) && (t.nodeType != 1)) {
                t = t.parentNode;
            }
            return t;
        };

        /**
         * equalsIgnoreCase, case insensitive comparison of two strings
         *
         * @param source
         * @param destination
         */
        static equalsIgnoreCase (source, destination) {
            //either both are not set or null
            if (!source && !destination) {
                return true;
            }
            //source or dest is set while the other is not
            if (!source || !destination) return false;
            //in any other case we do a strong string comparison
            return source.toLowerCase() === destination.toLowerCase();
        };

        /**
         * Save document.getElementById (Lang.code was ported over from dojo)
         * the idea is that either a string or domNode can be passed
         * @param {Object} reference the reference which has to be byIded
         */
        static byId (/*object*/ reference) {
            if (!reference) {
                throw LangUtils.makeException(new Error(), null, null, LangUtils._nameSpace, "byId", LangUtils.getMessage("ERR_REF_OR_ID", null, "_Lang.byId", "reference"));
            }
            return (LangUtils.isString(reference)) ? document.getElementById(reference) : reference;
        };

        /**
         * String to array function performs a string to array transformation
         * @param {String} it the string which has to be changed into an array
         * @param {RegExp} splitter our splitter reglar expression
         * @return an array of the splitted string
         */
        static strToArray (/*string*/ it, /*regexp*/ splitter) {
            //	summary:
            //		Return true if it is a String
            LangUtils._assertStr(it, "strToArray", "it");
            if (!splitter) {
                throw LangUtils.makeException(new Error(), null, null, LangUtils._nameSpace, "strToArray", LangUtils.getMessage("ERR_PARAM_STR_RE", null, "myfaces._impl._util._Lang.strToArray", "splitter"));
            }
            let retArr = it.split(splitter);
            let len = retArr.length;
            for (let cnt = 0; cnt < len; cnt++) {
                retArr[cnt] = LangUtils.trim(retArr[cnt]);
            }
            return retArr;
        };

        static _assertStr (it, functionName, paramName) {
            if (!LangUtils.isString(it)) {
                throw LangUtils.makeException(new Error(), null, null, LangUtils._nameSpace, functionName, LangUtils.getMessage("ERR_PARAM_STR", null, "myfaces._impl._util._Lang." + functionName, paramName));
            }
        };
        /**
         * hyperfast trim
         * http://blog.stevenlevithan.com/archives/faster-trim-javascript
         * crossported from dojo
         */
        static trim (/*string*/ str) {
            LangUtils._assertStr(str, "trim", "str");
            str = str.replace(/^\s\s*/, '');
            let ws = /\s/, i = str.length;

            while (ws.test(str.charAt(--i))) {
                //do nothing
            }
            return str.slice(0, i + 1);
        };
        /**
         * Backported from dojo
         * a failsafe string determination method
         * (since in javascript String != "" typeof alone fails!)
         * @param it {|Object|} the object to be checked for being a string
         * @return true in case of being a string false otherwise
         */
        static isString (/*anything*/ it) {
            //	summary:
            //		Return true if it is a String
            return !!arguments.length && it != null && (typeof it == "string" || it instanceof String); // Boolean
        };
        /**
         * hitch backported from dojo
         * hitch allows to assign a function to a dedicated scope
         * Lang.is helpful in situations when function reassignments
         * can happen
         * (notably happens often in lazy xhr code)
         *
         * @param {Function} scope of the function to be executed in
         * @param {Function} method to be executed, the method must be of type function
         *
         * @return whatever the executed method returns
         */
        static hitch (scope, method) {
            return !scope ? () => method.apply(this, arguments || []):
                () => method.apply(scope, arguments || []);
        };
        /**
         * Helper function to merge two maps
         * into one
         * @param {Object} dest the destination map
         * @param {Object} src the source map
         * @param {boolean} overwrite if set to true the destination is overwritten if the keys exist in both maps
         **/
        static mixMaps (dest, src, overwrite, blockFilter, whitelistFilter) {
            if (!dest || !src) {
                throw LangUtils.makeException(new Error(), null, null, LangUtils._nameSpace, "mixMaps", LangUtils.getMessage("ERR_PARAM_MIXMAPS", null, "_Lang.mixMaps"));
            }
            let _undef = "undefined";
            for (let key in src) {
                if (!src.hasOwnProperty(key)) continue;
                if (blockFilter && blockFilter[key]) {
                    continue;
                }
                if (whitelistFilter && !whitelistFilter[key]) {
                    continue;
                }
                if (!overwrite) {
                    /**
                     *we use exists instead of booleans because we cannot rely
                     *on all values being non boolean, we would need an elvis
                     *operator in javascript to shorten Lang.:-(
                     */
                    dest[key] = (_undef != typeof dest[key]) ? dest[key] : src[key];
                } else {
                    dest[key] = (_undef != typeof src[key]) ? src[key] : dest[key];
                }
            }
            return dest;
        };
        /**
         * checks if an array contains an element
         * @param {Array} arr   array
         * @param {String} str string to check for
         */
        static contains (arr, str) {
            if (!arr || !str) {
                throw LangUtils.makeException(new Error(), null, null, LangUtils._nameSpace, "contains", LangUtils.getMessage("ERR_MUST_BE_PROVIDED", null, "_Lang.contains", "arr {array}", "str {string}"));
            }
            return LangUtils.arrIndexOf(arr, str) != -1;
        };
        static arrToMap (arr, offset) {
            let ret = new Array(arr.length);
            let len = arr.length;
            offset = (offset) ? offset : 0;
            for (let cnt = 0; cnt < len; cnt++) {
                ret[arr[cnt]] = cnt + offset;
            }
            return ret;
        };
        static objToArray (obj, offset, pack) {
            if (!obj) {
                return null;
            }
            //since offset is numeric we cannot use the shortcut due to 0 being false
            //special condition array delivered no offset no pack
            if (obj instanceof Array && !offset && !pack)  return obj;
            let finalOffset = ('undefined' != typeof offset || null != offset) ? offset : 0;
            let finalPack = pack || [];
            try {
                return finalPack.concat(Array.prototype.slice.call(obj, finalOffset));
            } catch (e) {
                //ie8 (again as only browser) delivers for css 3 selectors a non convertible object
                //we have to do it the hard way
                //ie8 seems generally a little bit strange in its behavior some
                //objects break the function is everything methodology of javascript
                //and do not implement apply call, or are pseudo arrays which cannot
                //be sliced
                for (let cnt = finalOffset; cnt < obj.length; cnt++) {
                    finalPack.push(obj[cnt]);
                }
                return finalPack;
            }
        };


        /**
         * adds a EcmaScript optimized indexOf to our mix,
         * checks for the presence of an indexOf functionality
         * and applies it, otherwise uses a fallback to the hold
         * loop method to determine the index
         *
         * @param arr the array
         * @param element the index to search for
         */
        static arrIndexOf (arr, element /*fromIndex*/) {
            if (!arr || !arr.length) return -1;
            let pos = Number(arguments[2]) || 0;
            arr = LangUtils.objToArray(arr);
            return arr.indexOf(element, pos);
        };
        /**
         * helper to automatically apply a delivered arguments map or array
         * to its destination which has a field "_"<key> and a full field
         *
         * @param dest the destination object
         * @param args the arguments array or map
         * @param argNames the argument names to be transferred
         */
        static applyArgs (dest, args, argNames) {
            let UDEF = 'undefined';
            if (argNames) {
                for (let cnt = 0; cnt < args.length; cnt++) {
                    //dest can be null or 0 hence no shortcut
                    if (UDEF != typeof dest["_" + argNames[cnt]]) {
                        dest["_" + argNames[cnt]] = args[cnt];
                    }
                    if (UDEF != typeof dest[ argNames[cnt]]) {
                        dest[argNames[cnt]] = args[cnt];
                    }
                }
            } else {
                for (let key in args) {
                    if (!args.hasOwnProperty(key)) continue;
                    if (UDEF != typeof dest["_" + key]) {
                        dest["_" + key] = args[key];
                    }
                    if (UDEF != typeof dest[key]) {
                        dest[key] = args[key];
                    }
                }
            }
        };


        static parseXML (txt) {
            try {
                let parser = new DOMParser();
                return parser.parseFromString(txt, "text/xml");
            } catch (e) {
                //undefined internal parser error
                return null;
            }
        };
        static serializeXML (xmlNode, escape) {
            if (!escape) {
                if (xmlNode.data) return xmlNode.data; //CDATA block has raw data
                if (xmlNode.textContent) return xmlNode.textContent; //textNode has textContent
            }
            return (new XMLSerializer()).serializeToString(xmlNode);
        };
        static serializeChilds (xmlNode) {
            let buffer = [];
            if (!xmlNode.childNodes) return "";
            for (let cnt = 0; cnt < xmlNode.childNodes.length; cnt++) {
                buffer.push(LangUtils.serializeXML(xmlNode.childNodes[cnt]));
            }
            return buffer.join("");
        };
        static isXMLParseError (xmlContent) {
            //no xml content
            if (xmlContent == null) return true;
            let findParseError = function (node) {
                if (!node || !node.childNodes) return false;
                for (let cnt = 0; cnt < node.childNodes.length; cnt++) {
                    let childNode = node.childNodes[cnt];
                    if (childNode.tagName && childNode.tagName == "parsererror") return true;
                }
                return false;
            };
            return !xmlContent ||
                (LangUtils.exists(xmlContent, "parseError.errorCode") && xmlContent.parseError.errorCode != 0) ||
                findParseError(xmlContent);
        };

        /**
         * define a property mechanism which is browser neutral
         * we cannot use the existing setter and getter mechanisms
         * for now because old browsers do not support them
         * in the long run we probably can switch over
         * or make a code split between legacy and new
         *
         *
         * @param obj
         * @param name
         * @param value
         */
        static attr (obj, name, value) {
            let findAccessor = function (theObj, theName) {
                return (theObj["_" + theName]) ? "_" + theName : ( (theObj[theName]) ? theName : null)
            };
            let applyAttr = function (theObj, theName, value, isFunc) {
                if (value) {
                    if (isFunc) {
                        theObj[theName](value);
                    } else {
                        theObj[theName] = value;
                    }
                    return null;
                }
                return (isFunc) ? theObj[theName]() : theObj[theName];
            };
            try {
                let finalAttr = findAccessor(obj, name);
                //simple attibute no setter and getter overrides
                if (finalAttr) {
                    return applyAttr(obj, finalAttr, value);
                }
                //lets check for setter and getter overrides
                let found = false;
                let prefix = (value) ? "set" : "get";
                finalAttr = [prefix, name.substr(0, 1).toUpperCase(), name.substr(1)].join("");
                finalAttr = findAccessor(obj, finalAttr);
                if (finalAttr) {
                    return applyAttr(obj, finalAttr, value, true);
                }

                throw LangUtils.makeException(new Error(), null, null, LangUtils._nameSpace, "contains", "property " + name + " not found");
            } finally {
                findAccessor = null;
                applyAttr = null;
            }
        };

        /**
         * creates an exeption with additional internal parameters
         * for extra information
         *
         * @param {String} title the exception title
         * @param {String} name  the exception name
         * @param {String} callerCls the caller class
         * @param {String} callFunc the caller function
         * @param {String} message the message for the exception
         */
        static makeException (error, title, name, callerCls, callFunc, message) {
            error.name = name || "clientError";
            error.title = title || "";
            error.message = message || "";
            error._mfInternal = {};
            error._mfInternal.name = name || "clientError";
            error._mfInternal.title = title || "clientError";
            error._mfInternal.caller = callerCls || LangUtils._nameSpace;
            error._mfInternal.callFunc = callFunc || (callerCls+"."+callFunc);
            return error;
        }
    }
    window.LangUtils = _LangUtils;
}
