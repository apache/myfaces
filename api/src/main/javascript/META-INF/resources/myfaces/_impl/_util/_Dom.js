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
 * A collection of dom helper routines
 * (which in later incarnations will
 * get browser specific speed optimizations)
 *
 * Since we have to be as tight as possible
 * we will focus with our dom routines to only
 * the parts which our impl uses.
 * A jquery like query API would be nice
 * but this would blow up our codebase significantly
 *
 * TODO selector shortcuts bei chrome abdrehen da knallt es
 *
 */
myfaces._impl.core._Runtime.singletonExtendClass("myfaces._impl._util._Dom", Object, {
    IE_QUIRKS_EVENTS : {
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
    },

    /**
     * Run through the given Html item and execute the inline scripts
     * (IE doesn't do this by itself)
     * @param {|Node|} item
     */
    runScripts: function(item) {

        var executeScriptTag = myfaces._impl._util._Lang.hitch(this, function(item) {
            if ('undefined' != typeof item.tagName && item.tagName.toLowerCase() == 'script') {

                if (typeof item.getAttribute('src') != 'undefined'
                        && item.getAttribute('src') != null
                        && item.getAttribute('src').length > 0) {
                    // external script auto eval
                    //TODO fix the encoding here, we have to assume the src is the same encoding as the document
                    //or enforce auto
                    myfaces._impl.core._Runtime.loadScript(item.getAttribute('src'), item.getAttribute('type'), false, "UTF-8");
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
                    myfaces._impl.core._Runtime.globalEval(test); // run the script
                }
            }
        });
        try {
            var scriptElements = this.findByTagName(item, "script", true);
            if (scriptElements == null) return;
            for (var cnt = 0; cnt < scriptElements.length; cnt++) {
                executeScriptTag(scriptElements[cnt]);
            }
        } finally {
            //the usual ie6 fix code
            //the IE6 garbage collector is broken
            //nulling closures helps somewhat to reduce
            //mem leaks, which are impossible to avoid
            //at this browser
            executeScriptTag = null;
        }
    },

    /**
     * Simple delete on an existing item
     */
    deleteItem: function(itemIdToReplace) {
        var item = this.byId(itemIdToReplace);
        if (item == null) {
            throw Error("_Dom.deleteItem  Unknown Html-Component-ID: " + itemIdToReplace);
        }

        item.parentNode.removeChild(item);
    },

    /**
     * outerHTML replacement which works cross browserlike
     * but still is speed optimized
     *
     * @param item the item to be replaced
     * @param markup the markup for the replacement
     */
    outerHTML : function(item, markup) {
        markup = myfaces._impl._util._Lang.trim(markup);
        if ('undefined' == typeof item || null == item) {
            throw Error("myfaces._impl._util._Dom.outerHTML: item must be passed down");
        }
        if ('undefined' == typeof markup || null == markup) {
            throw Error("myfaces._impl._util._Dom.outerHTML: markup must be passed down");
        }

        if (markup != "") {
            var evalNode = null;

            //w3c compliant browsers with proper contextual fragments
            var parentNode;
            if (typeof window.Range != 'undefined'
                    && typeof Range.prototype.createContextualFragment == 'function') {
                var range = document.createRange();
                range.setStartBefore(item);
                var fragment = range.createContextualFragment(markup);
                //special case update body, we have to replace the placeholder
                //with the first element (the place holder is the the only child)
                //and then append additional elements as additional childs
                //the body itself then is the root for the eval part!
                if (item.id == 'myfaces_bodyplaceholder') {
                    parentNode = item.parentNode;
                    parentNode.appendChild(fragment);
                    evalNode = parentNode;
                } else {
                    //normal dom node case we replace only the client id fragment!

                    parentNode = item.parentNode;

                    evalNode = fragment.childNodes[0];
                    parentNode.replaceChild(fragment, item);
                }
            } else {

                //now to the non w3c compliant browsers
                //http://blogs.perl.org/users/clinton_gormley/2010/02/forcing-ie-to-accept-script-tags-in-innerhtml.html
                var dummyPlaceHolder = document.createElement("div");

                //fortunately a table element also works which is less critical than form elements regarding
                //the inner content
                dummyPlaceHolder.innerHTML = "<table>" + markup + "</table>";
                evalNode = dummyPlaceHolder.childNodes[0].childNodes[0].childNodes[0];
                parentNode = item.parentNode;
                item.parentNode.replaceChild(evalNode, item);

                //if this as well will fail in the future, we can let ie parse a proper xml
                //extract the script elements and then create the script elements manually
                //but for now we will not need it, and this solution is faster
                //the downside of that solution would be that the fragment itself
                //must resolve to a valid xml
            }

            // and remove the old item
            //first we have to save the node newly insert for easier access in our eval part
            if (myfaces._impl.core._Runtime.isManualScriptEval()) {
                this.runScripts(evalNode);
            }
            return evalNode;
        }
        // and remove the old item, in case of an empty newtag and do nothing else
        item.parentNode.removeChild(item);
        return null;
    },

    /**
     * finds a corresponding html item from a given identifier and
     * dom fragment
     * @param fragment the dom fragment to find the item for
     * @param itemId the identifier of the item
     */
    findById : function(fragment, itemId) {

        if(fragment === document) {
            return document.getElementById(itemId);
        }

        if (fragment.nodeType == 1 && fragment.querySelector) {
            //we can use the query selector here
            if (fragment.id && fragment.id === itemId) return fragment;
            return fragment.querySelector("#" + itemId);
        }

        var filter = function(node) {
            return 'undefined' != typeof node.id && node.id === itemId;
        };
        try {
            return this.findFirst(fragment, filter);
        } finally {
            //ie6 fix code
            filter = null;
        }
    },

    /**
     * findfirst functionality, finds the first element
     * for which the filter can trigger
     *
     * @param fragment the processed fragment/domNode
     * @param filter a filter closure which either returns true or false depending on triggering or not
     */
    findFirst : function(fragment, filter) {
        myfaces._impl._util._Lang._assertType(filter, "function");

        if (document.createTreeWalker && NodeFilter) {
            return this._iteratorBasedFindFirst(fragment, filter);
        } else {
            return this._recursionBasedFindFirst(fragment, filter);
        }
    },

    /**
     * a simple recusion based find first which iterates over the
     * dom tree the classical way which is also the slowest one
     * but that one will work back to ie6+
     *
     * @param fragment the starting fragment
     * @param filter the filter to be applied to
     */
    _recursionBasedFindFirst: function(fragment, filter) {
        if (filter(fragment)) {
            return fragment;
        }

        if (fragment.childNodes == null)
            return null;

        //sub-fragment usecases
        var child;
        var cnt;
        var childLen = fragment.childNodes.length;
        for (cnt = 0; cnt < childLen; cnt++) {
            child = fragment.childNodes[cnt];
            var item = this._recursionBasedFindFirst(child, filter);
            if (item != null)
                return item;
        }
        return null;
    },

    /**
     * the faster based iterator findFirst which will work
     * on all html5 compliant browsers and a bunch of older ones
     *
     * @param fragment the fragment to be started from
     * @param filter the filter which has to be used
     */
    _iteratorBasedFindFirst:function(fragment, filter) {
        if (filter(fragment)) {
            return fragment;
        }
        //we have a tree walker in place this allows for an optimized deep scan
        var lastElementFound = null;
        var treeWalkerfilter = function (node) {
            return ((filter(node)) ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_SKIP);
        };
        var treeWalker = document.createTreeWalker(fragment, NodeFilter.SHOW_ELEMENT, treeWalkerfilter, false);
        if (treeWalker.nextNode()) {
            return treeWalker.currentNode;
        }
        return null;
    },

    /**
     * determines the number of nodes according to their tagType
     *
     * @param {Node} fragment (Node or fragment) the fragment to be investigated
     * @param {String} tagName the tag name (lowercase)
     * @param {Boolean} deepScan if set to true a found element does not prevent to scan deeper
     * (the normal usecase is false, which means if the element is found only its
     * adjacent elements will be scanned, due to the recursive descension
     * this should work out with elements with different nesting depths but not being
     * parent and child to each other
     *
     * TODO rename to getElementsByTagName
     * TODO add iterator handlers here for browsers which allow dom filters and iterators
     */
    findByTagName : function(fragment, tagName, deepScan) {
        var _Lang = myfaces._impl._util._Lang;

        if ('undefined' == typeof deepScan) {
            deepScan = false;
        }

        var filter = function(node) {
            return _Lang.exists(node, "tagName") && _Lang.equalsIgnoreCase(node.tagName, tagName);
        };
        try {

            //html 5 selector
            if (deepScan && fragment.querySelectorAll) {
                var result = fragment.querySelectorAll(tagName);
                if (fragment.nodeType == 1 && filter(fragment)) {
                    result = (result == null) ? [] : _Lang.objToArray(result);
                    result.push(fragment);
                }
                return result;
            }
            //if we are not in a html 5 environment which supports node selectors
            //we use the usual recursive fallback.
            return this.findAll(fragment, filter, deepScan);
        } finally {
            //the usual IE6 is broken, fix code
            filter = null;
            _Lang = null;
        }

    },

    findByName : function(fragment, name, deepScan) {
        var _Lang = myfaces._impl._util._Lang;
        var filter = function(node) {
            return  _Lang.exists(node, "name") && _Lang.equalsIgnoreCase(node.name, name);
        };
        try {
            if ('undefined' == typeof deepScan) {
                deepScan = false;
            }

            if (deepScan && fragment.querySelectorAll) {
                var result = fragment.querySelectorAll("[name=" + name + "]");
                if (fragment.nodeType == 1 && filter(fragment)) {
                    result = (result == null) ? [] : _Lang.objToArray(result);
                    result.push(fragment);
                }
                return result;
            }

            return this.findAll(fragment, filter, deepScan);
        } finally {
            //the usual IE6 is broken, fix code
            filter = null;
            _Lang = null;
        }
    }
    ,

    /**
     * finds the elements by an attached style class
     *
     * @param fragment the source fragment which is the root of our search (included in the search)
     * @param styleClass the styleclass to search for
     * @param deepScan if set to true a deep scan can be performed
     */
    findByStyleClass : function(fragment, styleClass, deepScan) {
        var _Lang = myfaces._impl._util._Lang;
        var filter = _Lang.hitch(this, function(node) {
            var classes = this.getClasses(node);
            var len = classes.length;
            if (len == 0) return false;
            else {
                for (var cnt = 0; cnt < len; cnt++) {
                    if (classes[cnt] === styleClass) return true;
                }
            }
            return false;
        });
        try {
            if ('undefined' == typeof deepScan) {
                deepScan = false;
            }

            //html5 getElementsByClassname
            if (fragment.getElementsByClassName && deepScan) {
                return fragment.getElementsByClassName(styleClass);
            }
            //html5 speed optimization for browsers which do not ,
            //have the getElementsByClassName implemented
            //but only for deep scan and normal parent nodes
            else if (fragment.querySelectorAll && deepScan) {
                var selector = "." + styleClass;
                var result = fragment.querySelectorAll(selector);

                if (fragment.nodeType == 1 && filter(fragment)) {
                    result = (result == null) ? [] : result;
                    result = _Lang.objToArrayl(result);
                    result.push(fragment);
                }
                return result;
            } else {
                //fallback to the classical filter methods if we cannot use the
                //html 5 selectors for whatever reason
                return this.findAll(fragment, filter, deepScan);
            }

        } finally {
            //the usual IE6 is broken, fix code
            filter = null;
            _Lang = null;
        }
    }
    ,

    /**
     * a filtered findAll for subdom treewalking
     * (which uses browser optimizations wherever possible)
     *
     * @param {|Node|} rootNode the rootNode so start the scan
     * @param filter filter closure with the syntax {boolean} filter({Node} node)
     * @param deepScan if set to true or not set at all a deep scan is performed (for form scans it does not make much sense to deeply scan)
     */
    findAll : function(rootNode, filter, deepScan) {
        var _Lang = myfaces._impl._util._Lang;
        _Lang._assertType(filter, "function");

        if (document.createTreeWalker && NodeFilter) {
            return this._iteratorBasedSearchAll(rootNode, filter, deepScan);
        } else {
            return this._recursionBasedSearchAll(rootNode, filter, deepScan);
        }

    }
    ,

    /**
     * classical recursive way which definitely will work on all browsers
     * including the IE6
     *
     * @param rootNode the root node
     * @param filter the filter to be applied to
     * @param deepScan if set to true a deep scan is performed
     */
    _recursionBasedSearchAll: function(rootNode, filter, deepScan) {
        var _Lang = myfaces._impl._util._Lang;
        var retVal = [];
        //fix the value to prevent undefined errors
        if ('undefined' == typeof deepScan) {
            deepScan = true;
        }

        if (filter(rootNode)) {
            retVal.push(rootNode);
            if (!deepScan) return retVal;
        }

        //
        if (!_Lang.exists(rootNode, "childNodes"))
            return retVal;

        //subfragment usecases

        var retValLen = retVal.length;
        var childLen = rootNode.childNodes.length;
        for (var cnt = 0; (deepScan || retValLen == 0) && cnt < childLen; cnt++) {
            var childNode = rootNode.childNodes[cnt];
            var subRetVals = this._recursionBasedSearchAll(childNode, filter, deepScan);
            retVal = retVal.concat(subRetVals);
        }
        return retVal;
    }
    ,

    /**
     * the faster dom iterator based search, works on all newer browsers
     * except ie8 which already have implemented the dom iterator functions
     * of html 5 (which is pretty all standard compliant browsers)
     *
     * The advantage of this method is a faster tree iteration compared
     * to the normal recursive tree walking.
     *
     * @param rootNode the root node to be iterated over
     * @param filter the iteration filter
     * @param deepScan if set to true a deep scan is performed
     */
    _iteratorBasedSearchAll: function(rootNode, filter, deepScan) {
        var retVal = [];
        //Works on firefox and webkit, opera and ie have to use the slower fallback mechanis
        //we have a tree walker in place this allows for an optimized deep scan
        var lastElementFound = null;
        if (filter(rootNode)) {
            lastElementFound = rootNode;
            retVal.push(rootNode);
            if (!deepScan) {
                return retVal;
            }
        }
        //we use the reject mechanism to prevent a deep scan reject means any
        //child elements will be omitted from the scan
        var treeWalkerfilter = function (node) {
            var retCode = (filter(node)) ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_SKIP;
            retCode = (!deepScan && retCode == NodeFilter.FILTER_ACCEPT) ? NodeFilter.FILTER_REJECT : retCode;
            if (retCode == NodeFilter.FILTER_ACCEPT || retCode == NodeFilter.FILTER_REJECT) {
                retVal.push(node);
            }
            return retCode;
        };
        var treeWalker = document.createTreeWalker(rootNode, NodeFilter.SHOW_ELEMENT, treeWalkerfilter, false);
        while (treeWalker.nextNode());
        return retVal;
    }
    ,

    /**
     *
     * @param {Node} form
     * @param {String} nameOrIdenitifier
     *
     * checks for a a element with the name or identifier of nameOrIdentifier
     * @returns the found node or null otherwise
     */
    findFormElement : function(form, nameOrIdenitifier) {
        var eLen = form.elements.length;
        //TODO add iterator handlers here for browsers which allow dom filters and iterators
        var _RT = myfaces._impl.core._Runtime;

        for (var e = 0; e < eLen; e++) {
            var elem = form.elements[e];
            if (_RT.exists(elem,"name") && elem.name === nameOrIdenitifier) return elem;
            if (_RT.exists(elem,"id") && elem.id === nameOrIdenitifier) return elem;
        } // end of for (formElements)
        return null;
    }
    ,

    /**
     * bugfixing for ie6 which does not cope properly with setAttribute
     */
    setAttribute : function(domNode, attribute, value) {

        //quirks mode and ie7 mode has the attributes problems ie8 standards mode behaves like
        //a good citizen
        if (!myfaces._impl.core._Runtime.browser.isIE || myfaces._impl.core._Runtime.browser.isIE > 7) {
            domNode.setAttribute(attribute, value);
            return;
        }
        var _Lang = myfaces._impl._util._Lang;
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
            var styleEntriesLen = styleEntries.length;
            for (var loop = 0; loop < styleEntriesLen; loop++) {
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
            if (this.IE_QUIRKS_EVENTS[attribute]) {
                if (_Lang.isString(attribute)) {
                    domNode.setAttribute(attribute, function(event) {
                        //event implicitly used
                        return _Lang.globalEval(value);
                    });
                }
            } else {
                //unknown cases we try to catch them via standard setAttributes
                domNode.setAttribute(attribute, value);
            }
        }
    }
    ,

    /**
     * gets an element from a form with its id -> sometimes two elements have got
     * the same id but are located in different forms -> MyFaces 1.1.4 two forms ->
     * 2 inputHidden fields with ID jsf_tree_64 & jsf_state_64 ->
     * http://www.arcknowledge.com/gmane.comp.jakarta.myfaces.devel/2005-09/msg01269.html
     *
     * @param {String} itemIdOrName - ID of the HTML element located inside the form
     * @param {Node} form - form element containing the element
     * @param {boolean} nameSearch if set to true a search for name is also done
     * @param {boolean} localSearchOnly if set to true a local search is performed only (a full document search is omitted)
     * @return {Object}   the element if found else null
     *
     */
    getElementFromForm : function(itemIdOrName, form, nameSearch, localSearchOnly) {
        var _Lang = myfaces._impl._util._Lang;

        if ('undefined' == typeof form || form == null) {
            return this.findById(document, itemIdOrName);
        }


        var isNameSearch = ('undefined' == typeof nameSearch || nameSearch == null)? false: nameSearch;
        var isLocalSearchOnly = ('undefined' == typeof localSearchOnly || localSearchOnly == null)? false: localSearchOnly;


        var fLen = form.elements.length;

        //we first check for a name entry!
        if (isNameSearch && _Lang.exists(form, "elements." + itemIdOrName)) {
            return form.elements[itemIdOrName];
        }
        //if no name entry is found we check for an Id
        var element = this.findById(form, itemIdOrName);
        if(element != null)  {
            return element;
        }

        // element not found inside the form -> try document.getElementById
        // (kann be null if element doesn't exist)
        if (!isLocalSearchOnly) {
            return this.findById(document, itemIdOrName);
        }

        return null;
    }
    ,

    /**
     * fuzzy form detection which tries to determine the form
     * an item has been detached.
     *
     * The problem is some Javascript libraries simply try to
     * detach controls by reusing the names
     * of the detached input controls. Most of the times,
     * the name is unique in a jsf scenario, due to the inherent form mapping.
     * One way or the other, we will try to fix that by
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
     * @param {Node} element - element as source, can be detached, undefined or null
     *
     * @return either null or a form node if it could be determined
     */
    fuzzyFormDetection : function(element) {
        if (0 == document.forms.length) {
            return null;
        } else if (1 == document.forms.length) {
            return document.forms[0];
        }
        if ('undefined' == typeof element || null == element) {
            return null;
        }
        var _Lang = myfaces._impl._util._Lang;

        //before going into the more complicated stuff we try the simple approach
        if (!_Lang.isString(element)) {
            return this.getParent(element, "form");
        }

        var submitIdentifier = (_Lang.exists(element, "id")) ? element.id : null;
        var submitName = (_Lang.exists.exists(element, "name")) ? element.name : null;
        //a framework in a detachment case also can replace an existing identifier element
        // with a name element
        submitName = ('undefined' == typeof submitName || null == submitName) ? submitIdentifier : submitName;
        var foundForm;

        if ('undefined' != typeof submitIdentifier && null != submitIdentifier && '' != submitIdentifier) {
            //we have to assert that the element passed down is detached
            var domElement = myfaces._impl._util._Lang.byId(submitIdentifier);
            if ('undefined' != typeof domElement && null != domElement) {
                foundForm = this.getParent(domElement, "form");
                if (null != foundForm) return foundForm;
            }
        }

        /**
         * name check
         */
        var foundElements = new Array();

        /**
         * the lesser chance is the elements which have the same name
         * (which is the more likely case in case of a brute dom replacement)
         */
        var namedFoundElements = document.getElementsByName(submitName);
        if (null != namedFoundElements) {
            for (var cnt = 0; cnt < namedFoundElements.length; cnt++) {
                // we already have covered the identifier case hence we only can deal with names,
                foundForm = this.getParent(namedFoundElements[cnt], "form");
                if (null != foundForm) {
                    foundElements.push(foundForm);
                }
            }
        }

        if (null == foundElements || 0 == foundElements.length || foundElements.length > 1) {
            return null;
        }

        return foundElements[0];
    }
    ,

    /**
     * gets a parent of an item with a given tagname
     * @param {Node} item - child element
     * @param {String} tagNameToSearchFor - TagName of parent element
     */
    getParent : function(item, tagNameToSearchFor) {

        if ('undefined' == typeof item || null == item) {
            throw Error("myfaces._impl._util._Dom.getParent: item is null or undefined,this not allowed");
        }

        var _Lang = myfaces._impl._util._Lang;
        var searchClosure = function(parentItem) {
            return parentItem != null && _Lang.exists(parentItem, "tagName")
                    && _Lang.equalsIgnoreCase(parentItem.tagName, tagNameToSearchFor);
        };

        return this.getFilteredParent(item, searchClosure);
    }
    ,

    /**
     * A parent walker which uses
     * a filter closure for filtering
     *
     * @param {Node} item the root item to ascend from
     * @param {function} filter the filter closure
     */
    getFilteredParent : function(item, filter) {
        if ('undefined' == typeof item || null == item) {
            throw Error("myfaces._impl._util._Dom.getParen: item is null or undefined,this not allowed");
        }

        //search parent tag parentName
        var parentItem = ('undefined' != typeof item.parentNode) ? item.parentNode : null;

        while ('undefined' != typeof parentItem && null != parentItem && !filter(parentItem)) {
            parentItem = parentItem.parentNode;
        }
        if ('undefined' != typeof parentItem && null != parentItem) {
            return parentItem;
        } else {
            return null;
        }
    }
    ,

    /**
     * a closure based child filtering routine
     * which steps one level down the tree and
     * applies the filter closure
     *
     * @param item the node which has to be investigates
     * @param filter the filter closure
     */
    getFilteredChild: function(item, filter) {
        var childItems = item.childNodes;
        if ('undefined' == typeof childItems || null == childItems) {
            return null;
        }
        for (var c = 0, cLen = childItems.length; c < cLen; c++) {
            if (filter(childItems[c])) {
                return childItems[c];
            }
        }
        return null;
    }
    ,

    /**
     * gets the child of an item with a given tag name
     * @param {Node} item - parent element
     * @param {String} childName - TagName of child element
     * @param {String} itemName - name  attribute the child can have (can be null)
     * @Deprecated
     */
    getChild: function(item, childName, itemName) {

        function filter(node) {
            return node.tagName != null
                    && node.tagName.toLowerCase() == childName
                    && (itemName == null || (itemName != null && itemName == node.getAttribute("name")));

        }

        return this.getFilteredChild(item, filter);
    }
    ,

    /**
     * cross ported from dojo
     * fetches an attribute from a node
     *
     * @param {String} node the node
     * @param {String} attr the attribute
     * @return the attributes value or null
     */
    getAttribute : function(/* HTMLElement */node, /* string */attr) {
        //	summary
        //	Returns the value of attribute attr from node.
        node = this.byId(node);
        // FIXME: need to add support for attr-specific accessors
        if ((!node) || (!node.getAttribute)) {
            // if(attr !== 'nwType'){
            //	alert("getAttr of '" + attr + "' with bad node");
            // }
            return null;
        }
        var ta = typeof attr == 'string' ? attr : new String(attr);

        // first try the approach most likely to succeed
        var v = node.getAttribute(ta.toUpperCase());
        if ((v) && (typeof v == 'string') && (v != "")) {
            return v;	//	string
        }

        // try returning the attributes value, if we couldn't get it as a string
        if (v && v.value) {
            return v.value;	//	string
        }

        // this should work on Opera 7, but it's a little on the crashy side
        if ((node.getAttributeNode) && (node.getAttributeNode(ta))) {
            return (node.getAttributeNode(ta)).value;	//	string
        } else if (node.getAttribute(ta)) {
            return node.getAttribute(ta);	//	string
        } else if (node.getAttribute(ta.toLowerCase())) {
            return node.getAttribute(ta.toLowerCase());	//	string
        }
        return null;	//	string
    }
    ,

    /**
     * checks whether the given node has an attribute attached
     *
     * @param {String|Object} node the node to search for
     * @param {String} attr the attribute to search for
     * @true if the attribute was found
     */
    hasAttribute : function(/* HTMLElement */node, /* string */attr) {
        //	summary
        //	Determines whether or not the specified node carries a value for the attribute in question.
        return this.getAttribute(node, attr) ? true : false;	//	boolean
    }
    ,

    /**
     * fetches the style class for the node
     * cross ported from the dojo toolkit
     * @param {String|Object} node the node to search
     * @returns the className or ""
     */
    getClass : function(node) {
        node = this.byId(node);
        if (!node) {
            return "";
        }
        var cs = "";
        if (node.className) {
            cs = node.className;
        } else {
            if (this.hasAttribute(node, "class")) {
                cs = this.getAttribute(node, "class");
            }
        }
        return cs.replace(/^\s+|\s+$/g, "");
    }
    ,
    /**
     * fetches the class for the node,
     * cross ported from the dojo toolkit
     * @param {String|Object}node the node to search
     */
    getClasses : function(node) {
        var c = this.getClass(node);
        return (c == "") ? [] : c.split(/\s+/g);
    }
    ,

    /**
     * concatenation routine which concats all childnodes of a node which
     * contains a set of CDATA blocks to one big string
     * @param {Node} node the node to concat its blocks for
     */
    concatCDATABlocks
            :
            function(/*Node*/ node) {
                var cDataBlock = [];
                // response may contain several blocks
                for (var i = 0; i < node.childNodes.length; i++) {
                    cDataBlock.push(node.childNodes[i].data);
                }
                return cDataBlock.join('');
            }
    ,

    byId: function(identifier) {
        return myfaces._impl._util._Lang.byId(identifier);
    }
})
        ;
    
