/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
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
 var parent = new Object();
 parent.prototype = new myfaces._impl.core._Runtime();
 extendClass(function () {
 }, parent , {
 But for now we are not doing it the little bit of saved
 space is not worth the loss of readability
 */


/**
 * @memberOf myfaces._impl
 * @namespace
 * @name _util
 */



/**
 * @class
 * @name _Lang
 * @memberOf myfaces._impl._util
 * @extends myfaces._impl.core._Runtime
 * @namespace
 * @description Object singleton for Language related methods, this object singleton
 * decorates the namespace myfaces._impl.core._Runtime and adds a bunch of new methods to
 * what _Runtime provided
 * */
_MF_SINGLTN(_PFX_UTIL+"_Lang", Object,
        /**
         * @lends myfaces._impl._util._Lang.prototype
         */
        {

    _processedExceptions: {},

    _installedLocale: null,

    _RT: myfaces._impl.core._Runtime,



    /**
     * returns a given localized message upon a given key
     * basic java log like templating functionality is included
     *
     * @param {String} key the key for the message
     * @param {String} optional default message if none was found
     *
     * Additionally you can pass additional arguments, which are used
     * in the same way java log templates use the params
     *
     * @param key
     */
    getMessage: function(key, defaultMessage /*,vararg templateParams*/) {
        if(!this._installedLocale) {
            //we first try to install language and variant, if that one fails
            //we try to install the language only, and if that one fails
            //we install the base messages
            this.initLocale();
        }

        var msg = this._installedLocale[key] || defaultMessage || key + " - undefined message";
        for(var cnt = 2; cnt < arguments.length; cnt++) {
          msg = msg.replace(new RegExp(["\\{",cnt-2,"\\}"].join(""),"g"),new String(arguments[cnt]));
        }
        return msg;
    },

    /**
     * (re)inits the currently installed
     * messages so that after loading the main scripts
     * a new locale can be installed optionally
     * to our i18n subsystem
     *
     * @param newLocale locale override
     */
    initLocale: function(newLocale) {
        if(newLocale) {
            this._installedLocale = new newLocale();
            return;
        }
        var language_Variant = this._RT.getLanguage(this._RT.getGlobalConfig("locale"));
        var langStr = language_Variant ? language_Variant.language:"";
        var variantStr = language_Variant ? [language_Variant.language,"_",language_Variant.variant||""].join(""):"";

        var i18nRoot = myfaces._impl.i18n;
        var i18nHolder = i18nRoot["Messages_"+variantStr] ||
                         i18nRoot["Messages_"+langStr]    ||
                         i18nRoot["Messages"];

        this._installedLocale = new i18nHolder();
    },

    assertType: function(probe, theType) {
       return this._RT.assertType(probe, theType);
    },

     exists: function(nms, theType) {
       return this._RT.exists(nms, theType);
    },

    isExceptionProcessed: function(e) {
        return !! this._processedExceptions[e.toString()];
    },

    setExceptionProcessed: function(e) {
        this._processedExceptions[e.toString()] = true;
    },

    clearExceptionProcessed: function() {
        //ie again
        for (var key in this._processedExceptions) {
            this._processedExceptions[key] = null;
        }
        this._processedExceptions = {};
    },

    fetchNamespace : function(namespace) {
        if (!namespace || !this.isString(namespace)) {
            throw Error(this.getMessage("ERR_MUST_STRING",null,"_Lang.fetchNamespace","namespace"));
        }
        return this._RT.fetchNamespace(namespace);
    },

    reserveNamespace : function(namespace) {
        if (!this.isString(namespace)) {
            throw Error(this.getMessage("ERR_MUST_STRING",null,"_Lang.reserveNamespace", "namespace"));
        }
        return this._RT.reserveNamespace(namespace);
    },

    globalEval : function(code) {
        if (!this.isString(code)) {
            throw Error(this.getMessage("ERR_MUST_STRING",null,"_Lang.globalEval", "code"));
        }
        return  this._RT.globalEval(code);
    },


    /**
     * determines the correct event depending
     * on the browsers state
     *
     * @param evt incoming event object (note not all browsers
     * have this)
     *
     * @return an event object no matter what is incoming
     */
    getEvent: function(evt) {
        evt = (!evt) ? window.event || {} : evt;
        return evt;
    },

    /**
     * cross port from the dojo lib
     * browser save event resolution
     * @param evt the event object
     * (with a fallback for ie events if none is present)
     */
    getEventTarget: function(evt) {
        //ie6 and 7 fallback
        evt = this.getEvent(evt);
        /**
         * evt source is defined in the jsf events
         * seems like some component authors use our code
         * so we add it here see also
         * https://issues.apache.org/jira/browse/MYFACES-2458
         * not entirely a bug but makes sense to add this
         * behavior. I dont use it that way but nevertheless it
         * does not break anything so why not
         * */
        var t = evt.srcElement || evt.target || evt.source || null;
        while ((t) && (t.nodeType != 1)) {
            t = t.parentNode;
        }
        return t;
    },

    /**
     * consume event in a browser independend manner
     * @param event the event which should not be propagated anymore
     */
    consumeEvent: function(event) {
        //w3c model vs ie model again
        event = event || window.event;
        (event.stopPropagation) ? event.stopPropagation() : event.cancelBubble = true;
    },

    /**
     * equalsIgnoreCase, case insensitive comparison of two strings
     *
     * @param source
     * @param destination
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
     * escapes a strings special chars (crossported from dojo 1.3+)
     *
     * @param str the string
     *
     * @param except a set of exceptions
     */
    escapeString: function(/*String*/str, /*String?*/except) {
        //	summary:
        //		Adds escape sequences for special characters in regular expressions
        // except:
        //		a String with special characters to be left unescaped

        return str.replace(/([\.$?*|:{}\(\)\[\]\\\/\+^])/g, function(ch) {
            if (except && except.indexOf(ch) != -1) {
                return ch;
            }
            return "\\" + ch;
        }); // String
    },

    /**
     * Save document.getElementById (this code was ported over from dojo)
     * the idea is that either a string or domNode can be passed
     * @param {Object} reference the reference which has to be byIded
     */
    byId : function(/*object*/ reference) {
        if (!reference) {
            throw Error(this.getMessage("ERR_REF_OR_ID",null,"_Lang.byId","reference"));
        }
        return (this.isString(reference)) ? document.getElementById(reference) : reference;
    },

    /**
     * Helper function to provide a trim with a given splitter regular expression
     * @param {String} it the string to be trimmed
     * @param {RegExp} splitter the splitter regular expressiion
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
            throw Error(this.getMessage("ERR_PARAM_STR",null, "myfaces._impl._util._Lang.strToArray", "it"));
        }
        if (!splitter) {
            throw Error(this.getMessage("ERR_PARAM_STR_RE",null, "myfaces._impl._util._Lang.strToArray", "splitter"));
        }
        var retArr = it.split(splitter);
        var len = retArr.length;
        for (var cnt = 0; cnt < len; cnt++) {
            retArr[cnt] = this.trim(retArr[cnt]);
        }
        return retArr;
    },

    /**
     * hyperfast trim
     * http://blog.stevenlevithan.com/archives/faster-trim-javascript
     * crossported from dojo
     */
    trim : function(/*string*/ str) {
        if (!this.isString(str)) {
            throw Error(this.getMessage("ERR_PARAM_STR",null,"_Lang.trim", "str"));
        }
        str = str.replace(/^\s\s*/, '');
        var ws = /\s/;
        var i = str.length;
        while (ws.test(str.charAt(--i))){
            //do nothing
        }
        return str.slice(0, i + 1);
    },

    /**
     * Backported from dojo
     * a failsafe string determination method
     * (since in javascript String != "" typeof alone fails!)
     * @param it {|Object|} the object to be checked for being a string
     * @return true in case of being a string false otherwise
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
     * @param {Function} method to be executed, the method must be of type function
     *
     * @return whatever the executed method returns
     */
    hitch : function(scope, method) {
        return !scope ? method : function() {
            return method.apply(scope, arguments || []);
        }; // Function
    },

    /**
     * Helper function to merge two maps
     * into one
     * @param {Object} dest the destination map
     * @param {Object} src the source map
     * @param {boolean} overwrite if set to true the destination is overwritten if the keys exist in both maps
     **/
    mixMaps: function(dest, src, overwrite, blockFilter, whitelistFilter) {
      if (!dest || !src) {
            throw Error(this.getMessage("ERR_PARAM_MIXMAPS",null,"_Lang.mixMaps"));
      }
      var _undef = "undefined";
      for (var key in src) {
         if(blockFilter && blockFilter[key]) {
            continue;
         }
         if(whitelistFilter && !whitelistFilter[key]) {
            continue;
         }
         if (!overwrite) {

                /**
                 *we use exists instead of booleans because we cannot rely
                 *on all values being non boolean, we would need an elvis
                 *operator in javascript to shorten this :-(
                 */
                dest[key] = (_undef != typeof dest[key]) ? dest[key] : src[key];
            } else {
                dest[key] = (_undef != typeof src[key]) ? src[key] : dest[key];
            }
      }
        return dest;
    },

    /**
     * checks if an array contains an element
     * @param {Array} arr   array
     * @param {String} str string to check for
     */
    contains : function(arr, str) {
        if (!arr || !str) {
            throw Error(this.getMessage("ERR_MUST_BE_PROVIDED",null,"_Lang.contains", "arr {array}", "str {string}"));
        }

        return this.arrIndexOf(arr, str) != -1;
    },


    arrToMap: function(arr, offset) {
        var ret = new Array(arr.length);
        var len = arr.length;
        offset = (offset) ? offset : 0;

        for (var cnt = 0; cnt < len; cnt++) {
            ret[arr[cnt]] = cnt + offset;
        }
        return ret;
    },

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
    arrToString : function(/*String or array*/ arr, /*string*/ delimiter) {
        if (!arr) {
            throw Error(this.getMessage("ERR_MUST_BE_PROVIDED1",null, "arr {array}"));
        }
        if (this.isString(arr)) {
            return arr;
        }

        delimiter = delimiter || "\n";
        return arr.join(delimiter);
    },

    objToArray: function(obj, offset, pack) {
        if (!obj) {
            return null;
        }
        //since offset is numeric we cannot use the shortcut due to 0 being false
        //special condition array delivered no offset no pack
        if(obj instanceof Array && !offset && !pack)  return obj;

        var finalOffset = ('undefined' != typeof offset || null != offset) ? offset : 0;
        var finalPack = pack || [];
        try {
            return finalPack.concat(Array.prototype.slice.call(obj, finalOffset));
        } catch (e) {
            //ie8 (again as only browser) delivers for css 3 selectors a non convertible object
            //we have to do it the hard way
            //ie8 seems generally a little bit strange in its behavior some
            //objects break the function is everything methodology of javascript
            //and do not implement apply call, or are pseudo arrays which cannot
            //be sliced
            for (var cnt = finalOffset; cnt < obj.length; cnt++) {
                finalPack.push(obj[cnt]);
            }
            return finalPack;
        }

    },

    /**
     * foreach implementation utilizing the
     * ECMAScript wherever possible
     * with added functionality
     *
     * @param arr the array to filter
     * @param func the closure to apply the function to, with the syntax defined by the ecmascript functionality
     * function (element<,key, array>)
     * <p />
     * optional params
     * <p />
     * <ul>
     *      <li>param startPos (optional) the starting position </li>
     *      <li>param scope (optional) the scope to apply the closure to  </li>
     * </ul>
     */
    arrForEach: function(arr, func /*startPos, scope*/) {
        if(!arr || !arr.length ) return;

        var startPos = Number(arguments[2]) || 0;
        var thisObj = arguments[3];

        //check for an existing foreach mapping on array prototypes
        //IE9 still does not pass array objects as result for dom ops
        arr = this.objToArray(arr);
        (startPos) ? arr.slice(startPos).forEach(func, thisObj) : arr.forEach(func, thisObj);
    },


    /**
     * foreach implementation utilizing the
     * ECMAScript wherever possible
     * with added functionality
     *
     * @param arr the array to filter
     * @param func the closure to apply the function to, with the syntax defined by the ecmascript functionality
     * function (element<,key, array>)
     * <p />
     * additional params
     * <ul>
     *  <li> startPos (optional) the starting position</li>
     *  <li> scope (optional) the scope to apply the closure to</li>
     * </ul>
     */
    arrFilter: function(arr, func /*startPos, scope*/) {
        if(!arr || !arr.length ) return [];
        arr = this.objToArray(arr);
        return ((startPos) ? arr.slice(startPos).filter(func, thisObj) : arr.filter(func, thisObj));
    },

    /**
     * adds a EcmaScript optimized indexOf to our mix,
     * checks for the presence of an indexOf functionality
     * and applies it, otherwise uses a fallback to the hold
     * loop method to determine the index
     *
     * @param arr the array
     * @param element the index to search for
     */
    arrIndexOf: function(arr, element /*fromIndex*/) {
        if (!arr || !arr.length) return -1;
        var pos = Number(arguments[2]) || 0;
        arr = this.objToArray(arr);
        return arr.indexOf(element, pos);
    },


    /**
     * helper to automatically apply a delivered arguments map or array
     * to its destination which has a field "_"<key> and a full field
     *
     * @param dest the destination object
     * @param args the arguments array or map
     * @param argNames the argument names to be transferred
     */
    applyArgs: function(dest, args, argNames) {
        var _undef = 'undefined';
        if (argNames) {
            for (var cnt = 0; cnt < args.length; cnt++) {
                //dest can be null or 0 hence no shortcut
                if (_undef != typeof dest["_" + argNames[cnt]]) {
                    dest["_" + argNames[cnt]] = args[cnt];
                }
                if (_undef != typeof dest[ argNames[cnt]]) {
                    dest[argNames[cnt]] = args[cnt];
                }
            }
        } else {
            for (var key in args) {
                if (_undef != typeof dest["_" + key]) {
                    dest["_" + key] = args[key];
                }
                if (_undef != typeof dest[key]) {
                    dest[key] = args[key];
                }
            }
        }
    }
    ,
    /**
     * creates a standardized error message which can be reused by the system
     *
     * @param sourceClass the source class issuing the exception
     * @param func the function issuing the exception
     * @param error the error object itself (optional)
     */
    createErrorMsg: function(sourceClass, func, error) {
        var ret = [];

        var keyValToStr = this.hitch(this, this.keyValToStr);
        var getMsg = this.hitch(this, this.getMessage);

        ret.push(keyValToStr(getMsg("MSG_AFFECTED_CLASS"), sourceClass));
        ret.push(keyValToStr(getMsg("MSG_AFFECTED_METHOD"), func));

        /*we push the values into separate vars to improve the compression*/
        var errName = error.name;
        var errMsg = error.message;
        var errDesc = error.description;
        var errNum = error.number;
        var errLineNo = error.lineNumber;

        if (error) {
            var _UDEF = "undefined";

            ret.push(keyValToStr(getMsg("MSG_ERROR_NAME"), errName ? errName : _UDEF));
            ret.push(keyValToStr(getMsg("MSG_ERROR_MESSAGE"), errMsg ? errMsg : _UDEF));
            ret.push(keyValToStr(getMsg("MSG_ERROR_DESC"), errDesc ? errDesc : _UDEF));
            ret.push(keyValToStr(getMsg("MSG_ERROR_NO"), _UDEF != typeof errNum ? errNum : _UDEF));
            ret.push(keyValToStr(getMsg("MSG_ERROR_LINENO"), _UDEF != typeof errLineNo ? errLineNo : _UDEF));
        }
        return ret.join("");
    }
    ,

    /**
     * transforms a key value pair into a string
     * @param key the key
     * @param val the value
     * @param delimiter the delimiter
     */
    keyValToStr: function(key, val, delimiter) {
        var ret = [];
        ret.push(key);
        ret.push(val);
        if ('undefined' == typeof delimiter) {
            delimiter = "\n";
        }
        ret.push(delimiter);
        return ret.join("");
    }
    ,


    parseXML: function(txt) {
        try {
            var parser = new DOMParser();
            return parser.parseFromString(txt, "text/xml");
        } catch (e) {
            //undefined internal parser error
            return null;
        }
    },

    serializeXML: function(xmlNode, escape) {
        if(!escape) {
            if (xmlNode.data) return xmlNode.data; //CDATA block has raw data
            if (xmlNode.textContent) return xmlNode.textContent; //textNode has textContent
        }
        return (new XMLSerializer()).serializeToString(xmlNode);
    },

    serializeChilds: function(xmlNode) {
        var buffer = [];
        if (!xmlNode.childNodes) return "";
        for (var cnt = 0; cnt < xmlNode.childNodes.length; cnt++) {
            buffer.push(this.serializeXML(xmlNode.childNodes[cnt]));
        }
        return buffer.join("");
    },

    isXMLParseError: function(xmlContent) {
        //TODO determine the ie specific part here
        //no xml content
        if (xmlContent == null) return true;

        var findParseError = function(node) {
            if (!node || !node.childNodes) return false;
            for (var cnt = 0; cnt < node.childNodes.length; cnt++) {
                var childNode = node.childNodes[cnt];
                if (childNode.tagName && childNode.tagName == "parsererror") return true;
            }
            return false;
        };
        return !xmlContent ||
                (this.exists(xmlContent, "parseError.errorCode") && xmlContent.parseError.errorCode != 0) ||
                findParseError(xmlContent);


    },

    /**
     * creates a neutral form data wrapper over an existing form Data element
     * the wrapper delegates following methods, append
     * and adds makeFinal as finalizing method which returns the final
     * send representation of the element
     *
     * @param formData an array
     */
    createFormDataDecorator: function(formData) {
        //we simulate the dom level 2 form element here
        var _newCls = null;
        var bufInstance = null;

        if (!this.FormDataDecoratorArray) {
            this.FormDataDecoratorArray = function (theFormData) {
                this._valBuf = theFormData;
                this._idx = {};
            };
            _newCls = this.FormDataDecoratorArray;
            _newCls.prototype.append = function(key, val) {
                this._valBuf.push([encodeURIComponent(key), encodeURIComponent(val)].join("="));
                this._idx[key] = true;
            };
            _newCls.prototype.hasKey = function(key) {
                return !!this._idx[key];
            };
            _newCls.prototype.makeFinal = function() {
                return this._valBuf.join("&");
            };

        }
        if (!this.FormDataDecoratorString) {
            this.FormDataDecoratorString = function (theFormData) {
                this._preprocessedData = theFormData;
                this._valBuf = [];
                this._idx = {};

            };
            _newCls = this.FormDataDecoratorString;
            _newCls.prototype.append = function(key, val) {
                this._valBuf.push([encodeURIComponent(key), encodeURIComponent(val)].join("="));
                this._idx[key] = true;
            };
            //for now we check only for keys which are added subsequently otherwise we do not perform any checks
            _newCls.prototype.hasKey = function(key) {
                return !!this._idx[key];
            };
            _newCls.prototype.makeFinal = function() {
                if(this._preprocessedData != "") {
                  return this._preprocessedData + "&"+ this._valBuf.join("&")
                } else {
                  return this._valBuf.join("&");
                }
            };
        }
        if (!this.FormDataDecoratorOther) {
            this.FormDataDecoratorOther = function (theFormData) {
                this._valBuf = theFormData;
                this._idx = {};
            };
            _newCls = this.FormDataDecoratorOther;
            _newCls.prototype.append = function(key, val) {
                this._valBuf.append(key, val);
                this._idx[key] = true;
            };
            _newCls.prototype.hasKey = function(key) {
                return !!this._idx[key];
            };
            _newCls.prototype.makeFinal = function() {
                return this._valBuf;
            };
        }

        if (formData instanceof Array) {
            bufInstance = new this.FormDataDecoratorArray(formData);
        } else if(this.isString(formData)) {
            bufInstance = new this.FormDataDecoratorString(formData);
        } else {
            bufInstance = new this.FormDataDecoratorOther(formData);
        }

        return bufInstance;
    },

    /**
     * define a property mechanism which is browser neutral
     * we cannot use the existing setter and getter mechanisms
     * for now because old browsers do not support them
     * in the long run we probably can switch over
     * or make a code split between legacy and new
     *
     * TODO find a way to map this mechanism to the standard
     * props mechanism modern browsers have
     *
     * @param obj
     * @param name
     * @param value
     */
    attr: function(obj, name, value) {
        var findAccessor =  function(theObj, theName) {
            return (theObj["_" + theName]) ? "_" + theName : ( (theObj[theName]) ? theName : null)
        };
        var applyAttr = function(theObj, theName, value, isFunc) {
             if (value) {
                 if(isFunc) {
                    theObj[theName](value);
                 } else {
                     theObj[theName] = value;
                 }
                return null;
            }
            return (isFunc)?theObj[theName]() : theObj[theName];
        };
        try {
            var finalAttr = findAccessor(obj, name);
            //simple attibute no setter and getter overrides
            if(finalAttr) {
              return applyAttr(obj, finalAttr, value);
            }
            //lets check for setter and getter overrides
            var found = false;

            var prefix = (value)?"set":"get";
            finalAttr = [prefix,name.substr(0,1).toUpperCase(),name.substr(1)].join("");
            finalAttr = findAccessor(obj, finalAttr);
            if(finalAttr) {
              return applyAttr(obj, finalAttr, value, true);
            }

            throw Error("property "+name+" not found");
        } finally {
            findAccessor = null;
            applyAttr = null;
        }
    }
});
