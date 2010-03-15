/*
 * Copyright 2009 Ganesh Jung
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Ganesh Jung (latest modification by $Author: ganeshpuri $)
 * Version: $Revision: 1.2 $ $Date: 2009/05/31 09:16:43 $
 *
 */

_reserveMyfacesNamespaces();

if (!myfaces._impl._util._LangUtils.exists(myfaces._impl._util, "_Utils")) {
    /**
     * Constructor
     */
    myfaces._impl._util._Utils = function() {
    }

    myfaces._impl._util._Utils.browserDetection = function() {
        /**
         * browser detection code
         * cross ported from dojo 1.2
         *
         * dojos browser detection code is very sophisticated
         * hence we port it over it allows a very fine grained detection of
         * browsers including the version number
         * this however only can work out if the user
         * does not alter the user agend, which they normally dont!
         *
         * the exception is the ie detection which relies on specific quirks in ie
         */
        var n = navigator;
        var dua = n.userAgent,
                dav = n.appVersion,
                tv = parseFloat(dav);

        myfaces._impl._util._Utils.browser = {};
        var d = myfaces._impl._util._Utils.browser;

        if (dua.indexOf("Opera") >= 0) {
            myfaces._impl._util._Utils.isOpera = tv;
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
     * encapsulated xhr object which tracks down various implementations
     * of the xhr object in a browser independend fashion
     * (ie pre 7 used to have non standard implementations because
     * the xhr object standard came after IE had implemented it first
     * newer ie versions adhere to the standard and all other new browsers do anyway)
     */
    myfaces._impl._util._Utils.getXHRObject = function() {
        if ('undefined' != typeof XMLHttpRequest && null != XMLHttpRequest) {
            return new XMLHttpRequest();
        }
        //IE
        try {
            return new ActiveXObject("Msxml2.XMLHTTP");
        } catch (e) {

        }
        return new ActiveXObject('Microsoft.XMLHTTP');
    }

    /**
     * [STATIC]
     * loads a script and executes it under a global scope
     * @param {String} src the source to be loaded
     * @param {String} type the mime type of the script (currently ignored
     * but in the long run it will be used)
     */
    myfaces._impl._util._Utils.loadScript = function(src, type, defer, charSet) {
        var xhr = myfaces._impl._util._Utils.getXHRObject();
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
                    myfaces._impl._util._Utils.globalEval(xhr.responseText);
                } else {
                    setTimeout(function() {
                        myfaces._impl._util._Utils.globalEval(xhr.responseText);
                    }, 1);
                }
            } else {
                throw Error(xhr.responseText);
            }
        } else {
            throw Error("Loading of script " + src + " failed ");
        }
    }

    /**
     * [STATIC]
     * Run through the given Html item and execute the inline scripts
     * (IE doesn't do this by itself)
     * @param {XMLHTTPRequest} request
     * @param {Map} context
     * @param {HtmlElement} item
     */
    myfaces._impl._util._Utils.runScripts = function(request, context, item) {
        if (item.nodeType == 1) { // only if it's an element node
            if (item.tagName.toLowerCase() == 'script') {
                try {
                    if (typeof item.getAttribute('src') != 'undefined'
                            && item.getAttribute('src') != null
                            && item.getAttribute('src').length > 0) {
                        // external script auto eval
                        myfaces._impl._util._Utils.loadScript(item.getAttribute('src'), item.getAttribute('type'), false, "ISO-8859-1");
                    } else {
                        // embedded script auto eval
                        var test = item.text;
                        var go = true;
                        while (go) {
                            go = false;
                            if (test.substring(0, 1) == " ") {
                                test = test.substring(1);
                                go = true;
                            }
                            if (test.substring(0, 4) == "<!--") {
                                test = test.substring(4);
                                go = true;
                            }
                            if (test.substring(0, 11) == "//<![CDATA[") {
                                test = test.substring(11);
                                go = true;
                            }
                        }
                        // we have to run the script under a global context
                        myfaces._impl._util._Utils.globalEval(test); // run the script
                    }
                } catch (e) {
                    myfaces._impl.xhrCore._Exception.throwNewError(request, context, "Utils", "runScripts", e);
                }
            } else {
                var child = item.firstChild;
                while (child) {
                    myfaces._impl._util._Utils.runScripts(request, context, child);
                    child = child.nextSibling;
                }
            }
        }
    }

    /**
     * Simple delete on an existing item
     */
    myfaces._impl._util._Utils.deleteItem = function(request, context, itemIdToReplace) {
        var item = document.getElementById(itemIdToReplace);
        if (item == null) {
            myfaces._impl.xhrCore._Exception.throwNewWarning
                    (request, context, "Utils", "deleteItem", "Unknown Html-Component-ID: " + itemIdToReplace);
            return;
        }

        item.parentNode.removeChild(item);
    }

    /**
     * [STATIC]
     * Replaces HTML elements through others
     * @param {XMLHTTPRequest} request
     * @param {Map} context
     * @param {String|node} itemIdToReplace - ID of the element to replace
     * @param {String} newTag - the new tag
     * @param {HTML Element} form - form element that is parent of the element
     */
    myfaces._impl._util._Utils.replaceHtmlItem = function(request, context, itemIdToReplace, newTag, form) {
        try {
            //for webkit we have to trim otherwise he does not add the adjancent elements correctly
            newTag = myfaces._impl._util._LangUtils.trim(newTag);

            // (itemIdToReplace instanceof Node) is NOT compatible with IE8
            var item = (typeof itemIdToReplace == "object") ? itemIdToReplace :
                       myfaces._impl._util._Utils.getElementFromForm(request, context, itemIdToReplace, form);

            if (item == null) {
                myfaces._impl.xhrCore._Exception.throwNewWarning
                        (request, context, "Utils", "replaceHTMLItem", "Unknown Html-Component-ID: " + itemIdToReplace);
                return;
            }

            if (newTag != "") {
                var evalNode = null;
                if (typeof window.Range != 'undefined'
                        && typeof Range.prototype.createContextualFragment == 'function') {
                    var range = document.createRange();
                    range.setStartBefore(item);
                    var fragment = range.createContextualFragment(newTag);
                    evalNode = item.parentNode.replaceChild(fragment, item);
                } else {
                    item.insertAdjacentHTML('beforeBegin', newTag);
                    evalNode = item.previousSibling;
                    item.parentNode.removeChild(item);
                }

                // and remove the old item
                //first we have to save the node newly insert for easier access in our eval part
                if (myfaces._impl._util._Utils.isManualScriptEval()) {
                    myfaces._impl._util._Utils.runScripts(request, context, evalNode);
                }
                return;
            }
            // and remove the old item, in case of an empty newtag and do nothing else
            item.parentNode.removeChild(item);

        } catch (e) {
            myfaces._impl.xhrCore._Exception.throwNewError(request, context, "Utils", "replaceHTMLItem", e);
        }
    };

    myfaces._impl._util._Utils.ieQuircksEvents = {
        "onabort": true,
        "onload":true,
        "onunload":true,
        "onchange": true,
        "onsubmit": true,
        "onreset": true,
        "onselect": true,
        "onblur": true,
        "onfocus": true,
        "onkeydown": true,
        "onkeypress": true,
        "onkeyup": true,
        "onclick": true,
        "ondblclick": true,
        "onmousedown": true,
        "onmousemove": true,
        "onmouseout": true,
        "onmouseover": true,
        "onmouseup": true
    };

    /**
     * bugfixing for ie6 which does not cope properly with setAttribute
     */
    myfaces._impl._util._Utils.setAttribute = function(domNode, attribute, value) {

        //quirks mode and ie7 mode has the attributes problems ie8 standards mode behaves like
        //a good citizen
        if (!myfaces._impl._util._Utils.isUserAgentInternetExplorer() || myfaces._impl._util._Utils.browser.isIE > 7) {
            domNode.setAttribute(attribute, value);
            return;
        }

        /*
            Now to the broken browsers IE6+.... ie7 and ie8 quirks mode

            we deal mainly with three problems here
            class and for are not handled correctly
            styles are arrays and cannot be set directly
            and javascript events cannot be set via setAttribute as well!

            or in original words of quirksmode.org ... this is a mess!

            Btw. thank you Microsoft for providing all necessary tools for free
            for being able to debug this entire mess in the ie rendering engine out
            (which is the Microsoft ie vms, developers toolbar, Visual Web Developer 2008 express
            and the ie8 8 developers toolset!)

            also thank you http://www.quirksmode.org/
            dojotoolkit.org and   //http://delete.me.uk/2004/09/ieproto.html
            for additional information on this mess!

            The lowest common denominator tested within this code
            is IE6, older browsers for now are legacy!
         */
        attribute = attribute.toLowerCase();

        if (attribute === "class") {
            domNode.setAttribute("className", value);
        } else if (attribute === "for") {

            domNode.setAttribute("htmlFor", value);
        } else if (attribute === "style") {
            //We have to split the styles here and assign them one by one
            var styleEntries = value.split(";");
            for (var loop = 0; loop < styleEntries.length; loop++) {
                var keyVal = styleEntries[loop].split(":");
                if (keyVal[0] != "" && keyVal[0] == "opacity") {
                    //special ie quirks handling for opacity

                    var opacityVal = Math.max(100, Math.round(parseFloat(keyVal[1]) * 10));
                    domNode.style.setAttribute("filter", "alpha(opacity=" + opacityVal + ")");
                    //if you need more hacks I would recommend
                    //to use the class attribute and conditional ie includes!
                } else if (keyVal[0] != "") {
                    domNode.style.setAttribute(keyVal[0], keyVal[1]);
                }
            }
        } else {
            //check if the attribute is an event, since this applies only
            //to quirks mode of ie anyway we can live with the standard html4/xhtml
            //ie supported events
            if (myfaces._impl._util._Utils.ieQuircksEvents[attribute]) {
                if (myfaces._impl._util._LangUtils.isString(attribute)) {
                    domNode.setAttribute(attribute, function(event) {
                        myfaces._impl._util._Utils.globalEval(attribute);
                    });
                }
            } else {
                //unknown cases we try to catch them via standard setAttributes
                domNode.setAttribute(attribute, value);
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
    myfaces._impl._util._Utils.isManualScriptEval = function() {
        var _LangUtils = myfaces._impl._util._LangUtils;
        //TODO test this with various browsers so that we have auto eval wherever possible
        //
        //tested currently safari, ie, firefox, opera
        var retVal = (_LangUtils.exists(myfaces._impl._util._Utils.browser, "isIE") &&
                      ( myfaces._impl._util._Utils.browser.isIE > 5.5)) ||
                     (_LangUtils.exists(myfaces._impl._util._Utils.browser, "isKhtml") &&
                      (myfaces._impl._util._Utils.browser.isKhtml > 0)) ||
                     (_LangUtils.exists(myfaces._impl._util._Utils.browser, "isWebKit") &&
                      (myfaces._impl._util._Utils.browser.isWebKit > 0)) ||
                     (_LangUtils.exists(myfaces._impl._util._Utils.browser, "isSafari") &&
                      (myfaces._impl._util._Utils.browser.isSafari > 0));

        return retVal;

        //another way to determine this without direct user agent parsing probably could
        //be to add an embedded script tag programmatically and check for the script variable
        //set by the script if existing, the add went through an eval if not then we
        //have to deal with it outselves, this might be dangerous in case of the ie however
        //so in case of ie we have to parse for all other browsers we can make a dynamic
        //check if the browser does auto eval
        //TODO discuss those things
    };

    /**
     * [STATIC]
     * Determines whether the user agent is IE or not
     * @return {boolean} - true if it is IE
     */
    myfaces._impl._util._Utils.isUserAgentInternetExplorer = function() {
        return myfaces._impl._util._Utils.browser.isIE;
    };

    /**
     * [STATIC]
     * gets an element from a form with its id -> sometimes two elements have got
     * the same id but are located in different forms -> MyFaces 1.1.4 two forms ->
     * 2 inputHidden fields with ID jsf_tree_64 & jsf_state_64 ->
     * http://www.arcknowledge.com/gmane.comp.jakarta.myfaces.devel/2005-09/msg01269.html
     * @param {Object} request
     * @param {Map} context
     * @param {String} itemIdOrName - ID of the HTML element located inside the form
     * @param {Html-Element} form - form element containing the element
     * @param {boolean} nameSearch if set to true a search for name is also done
     * @param {boolean} localSearchOnly if set to true a local search is performed only (a full document search is omitted)
     * @return {Obect}   the element if found else null
     *
     */
    myfaces._impl._util._Utils.getElementFromForm = function(request, context, itemIdOrName, form, nameSearch, localSearchOnly) {
        try {

            if ('undefined' == typeof form || form == null) {
                return document.getElementById(itemIdOrName);
            }
            if ('undefined' == typeof includeName || nameSearch == null) {
                nameSearch = false;
            }
            if ('undefined' == typeof localSearchOnly || localSearchOnly == null) {
                localSearchOnly = false;
            }

            var fLen = form.elements.length;

            //we first check for a name entry!
            if (nameSearch && 'undefined' != typeof form.elements[itemIdOrName] && null != form.elements[itemIdOrName]) {
                return form.elements[itemIdOrName];
            }
            //if no name entry is found we check for an Id
            for (var f = 0; f < fLen; f++) {
                var element = form.elements[f];
                if (element.id != null && element.id == itemIdOrName) {
                    return element;
                }
            }
            // element not found inside the form -> try document.getElementById
            // (kann be null if element doesn't exist)
            if (!localSearchOnly) {
                return document.getElementById(itemIdOrName);
            }
        } catch (e) {
            myfaces._impl.xhrCore._Exception.throwNewError(request, context, "Utils", "getElementFromForm", e);
        }
        return null;
    };

    /**
     * fuzzy form detection which tries to determine the form
     * an item has been detached.
     *
     * The problem is some Javascript libraries simply try to
     * detach controls and controls others by reusing the names
     * of the detached input controls. The thing is most of the times,
     * the name is unique in a jsf scenario due to the inherent form mappig
     * one way or the other, we will try to fix that by
     * identifying the proper form over the name
     *
     * We do it in several ways, in case of no form null is returned
     * in case of multiple forms we check all elements with a given name (which we determine
     * out of a name or id of the detached element) and then iterate over them
     * to find whether they are in a form or not.
     *
     * If only one element within a form and a given identifier found then we can pull out
     * and move on
     *
     * We cannot do much further because in case of two identical named elements
     * all checks must fail and the first elements form is served.
     *
     * Note, this method is only triggered in case of the issuer or an ajax request
     * is a detached element, otherwise already existing code has served the correct form.
     *
     * This method was added because of
     * https://issues.apache.org/jira/browse/MYFACES-2599
     * to support the integration of existing ajax libraries which do heavy dom manipulation on the
     * controls side (Dojos Dijit library for instance).
     *
     * @param {XMLHTTPRequest} request
     * @param {Map} context
     * @param {String} submitIdentifier - child elements name identifier
     *
     * @return either null or a form node if it could be determined
     */
    myfaces._impl._util._Utils.fuzzyFormDetection = function(request, context, submitIdentifier) {
        if (0 == document.forms.length) {
            return null;
        }

        if ('undefined' == typeof submitIdentifier || null == submitIdentifier) {
            //no identifier found we give it a shot with the first form
            return document.forms[0];
        }

        /**
         * highest chance is to find an element with an identifier
         */
        var elementById = document.getElementById(submitIdentifier);
        var foundElements = new Array();
        if (null != elementById) {
            if ('undefined' == typeof element.name || null == element.name || submitIdentifier == element.name) {
                foundElements.push(elementById);
            }
        }

        /**
         * the lesser chance is the elements which have the same name
         * (which is the more likely case in case of a brute dom replacement)
         */
        var namedFoundElements = document.getElementsByName(submitIdentifier);
        if (null != namedFoundElements) {
            for (var cnt = 0; cnt < namedFoundElements.length; cnt++) {
                // we already have covered the identifier case hence we only can deal with names,
                // since identifiers are unique
                // we have to filter that element out for the rest of the stack, to have a clean handling
                // of the number of referenced forms
                if('undefined' == typeof namedFoundElements[cnt].id || null == namedFoundElements[cnt].id || namedFoundElements[cnt].id != submitIdentifier) {
                    foundElements.push(namedFoundElements[cnt]);
                }
            }
        }

        if (null == foundElements || 0 == foundElements.length) {
            return null;
        }

        //we now iterate over all possible elements with the identifier element being the first if present
        //however if the identifier element has no parent form we must rely on our found named elements
        //to have at least one parent form
        var foundForm = null;
        var formCnt = 0;
        for (var cnt = 0; cnt < foundElements.length; cnt++) {
            var foundElement = foundElements[cnt];
            var parentItem = ('undefined' != typeof foundElement && null != foundElement) ? foundElement.parentNode : null;
            while (parentItem != null
                    && parentItem.tagName.toLowerCase() != "form") {
                parentItem = parentItem.parentNode;
            }
            if (parentItem != null) {
                foundForm = parentItem;
                formCnt++;
            }
            if(formCnt > 1) return null;
        }

        return null;
    };

    /**
     * [STATIC]
     * gets a parent of an item with a given tagname
     * @param {XMLHTTPRequest} request
     * @param {Map} context
     * @param {HtmlElement} item - child element
     * @param {String} tagNameToSearchFor - TagName of parent element
     */
    myfaces._impl._util._Utils.getParent = function(request, context, item, tagNameToSearchFor) {

		try {
            if('undefined' == typeof item || null == item) {
			    throw Error("myfaces._impl._util._Utils.getParen: item is null or undefined,this not allowed");
		    }

            //search parent tag parentName
            var parentItem = ('undefined' != typeof item.parentNode) ? item.parentNode: null;


            if ('undefined' != typeof item.tagName && null != item.tagName && item.tagName.toLowerCase() == tagNameToSearchFor) {
                return item;
            }

            while (parentItem != null
                    && parentItem.tagName.toLowerCase() != tagNameToSearchFor) {
                parentItem = parentItem.parentNode;
            }
            if (parentItem != null) {
                return parentItem;
            } else {
                //we issue a warning but proceed with a fuzzy search in case of a form later
                myfaces._impl.xhrCore._Exception.throwNewWarning
                        (request, context, "Utils", "getParent", "The item has no parent with type <" + tagNameToSearchFor + "> it might be outside of the parent or generally detached. ");
                return null;
            }
        } catch (e) {
            myfaces._impl.xhrCore._Exception.throwNewError(request, context, "Utils", "getParent", e);
        }
    };

    /**
     * [STATIC]
     * gets the child of an item with a given tag name
     * @param {HtmlElement} item - parent element
     * @param {String} childName - TagName of child element
     * @param {String} itemName - name-Attribut the child can have (can be null)
     */
    myfaces._impl._util._Utils.getChild = function(item, childName, itemName) {
        var childItems = item.childNodes;
        for (var c = 0, cLen = childItems.length; c < cLen; c++) {
            if (childItems[c].tagName != null
                    && childItems[c].tagName.toLowerCase() == childName
                    && (itemName == null || (itemName != null && itemName == childItems[c]
                    .getAttribute("name")))) {
                return childItems[c];
            }
        }
        return null;
    }

    /**
     * fetches a global config entry
     * @param {String} configName the name of the configuration entry
     * @param {Object} defaultValue
     *
     * @return either the config entry or if none is given the default value
     */
    myfaces._impl._util._Utils.getGlobalConfig = function(configName, defaultValue) {
        /*use(myfaces._impl._util)*/
        var _LangUtils = myfaces._impl._util._LangUtils;

        if (_LangUtils.exists(myfaces, "config") && _LangUtils.exists(myfaces.config, configName)) {
            return myfaces.config[configName];
        }
        return defaultValue;
    };

    /**
     * global eval on scripts
     *
     */
    myfaces._impl._util._Utils.globalEval = function(code) {
        //chrome as a diferent global eval, thanks for pointing this out
        //TODO add a config param which allows to evaluate global scripts even if the call
        //is embedded in an iframe
        if (myfaces._impl._util._Utils.browser.isIE && window.execScript) {
            //execScript definitely only for IE otherwise we might have a custom
            //window extension with undefined behavior on our necks
            window.execScript(code);
            return;
        } else if (undefined != typeof (window.eval) && null != window.eval) {

            //fix for a Mozilla bug, a bug, Mozilla prevents, that the window is properly applied
            //the former approach was to scope an outer anonymouse function but the scoping is not necessary
            //Mozilla behaves correctly if you just add an outer function, then the window scope is again
            //accepted as the real scope
            var func = function () {
                window.eval.call(window, code);
            };
            func();

            return;
        }
        //we probably have covered all browsers, but this is a safety net which might be triggered
        //by some foreign browser which is not covered by the above cases
        eval.call(window, code);
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
    myfaces._impl._util._Utils.getLocalOrGlobalConfig = function(localOptions, configName, defaultValue) {
        /*use(myfaces._impl._util)*/
        var _LangUtils = myfaces._impl._util._LangUtils;

        var globalOption = myfaces._impl._util._Utils.getGlobalConfig(configName, defaultValue);
        if (!_LangUtils.exists(localOptions, "myfaces") || !_LangUtils.exists(localOptions.myfaces, configName)) {
            return globalOption;
        }
        return localOptions.myfaces[configName];
    };

    /**
     * concatenation routine which concats all childnodes of a node which
     * contains a set of CDATA blocks to one big string
     * @param {Node} node the node to concat its blocks for
     */
    myfaces._impl._util._Utils.concatCDATABlocks = function(/*Node*/ node) {
        var cDataBlock = [];
        // response may contain several blocks
        for (var i = 0; i < node.childNodes.length; i++) {
            cDataBlock.push(node.childNodes[i].data);
        }
        return cDataBlock.join('');
    };

    myfaces._impl._util._Utils.browserDetection();
}
