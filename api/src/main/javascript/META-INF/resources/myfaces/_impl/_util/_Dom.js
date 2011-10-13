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

/**
 * @class
 * @name _Dom
 * @memberOf myfaces._impl._util
 * @extends myfaces._impl.core._Runtime
 * @description Object singleton collection of dom helper routines
 * (which in later incarnations will
 * get browser specific speed optimizations)
 *
 * Since we have to be as tight as possible
 * we will focus with our dom routines to only
 * the parts which our impl uses.
 * A jquery like query API would be nice
 * but this would blow up our codebase significantly
 */
myfaces._impl.core._Runtime.singletonExtendClass("myfaces._impl._util._Dom", Object,
/**
 * @lends myfaces._impl._util._Dom.prototype
 */
{
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

    /*table elements which are used in various parts */
    TABLE_ELEMS:  {
        "thead": true,
        "tbody": true,
        "tr": true,
        "th": true,
        "td": true,
        "tfoot" : true
    },

    _Lang:  myfaces._impl._util._Lang,
    _RT:    myfaces._impl.core._Runtime,
    _dummyPlaceHolder:null,

    /**
     * standard constructor
     */
    constructor_: function() {
        //we have to trigger it upfront because mozilla runs the eval
        //after the dom updates and hence causes a race conditon if used on demand
        //under normal circumstances this works, if there are no normal ones
        //then this also will work at the second time, but the onload handler
        //should cover 99% of all use cases to avoid a loading race condition
        var b = myfaces._impl.core._Runtime.browser;

        if (b.isIE <= 6 && b.isIEMobile) {
            //winmobile hates add onLoad, and checks on the construct
            //it does not eval scripts anyway
            myfaces.config = myfaces.config || {};
            myfaces.config._autoeval = false;
            return;
        }
        this._RT.addOnLoad(window, function() {
            myfaces._impl._util._Dom.isManualScriptEval();
        });
        //safety fallback if the window onload handler is overwritten and not chained
        if (document.body) {
            this._RT.addOnLoad(document.body, function() {
                myfaces._impl._util._Dom.isManualScriptEval();
            });
        }
        //now of the onload handler also is overwritten we have a problem
    },

    runCss: function(item, xmlData) {

        var stylesheets = document.styleSheets;
        var finalCss    = [];

        var applyStyle = this._Lang.hitch(this, function(item, style) {
            var newSS = document.createElement("style");

            newSS.setAttribute("rel",item.getAttribute("rel") || "stylesheet");
            newSS.setAttribute("type",item.getAttribute("type") || "text/css");
            document.getElementsByTagName("head")[0].appendChild(newSS);
            //ie merrily again goes its own way
            if(window.attachEvent && !this._RT.isOpera  && 'undefined' != typeof newSS.styleSheet && 'undefined' != newSS.styleSheet.cssText) newSS.styleSheet.cssText = style;
            else newSS.appendChild(document.createTextNode(style));
        });

        var execCss = this._Lang.hitch(this, function(item) {
            var _eqi = this._Lang.equalsIgnoreCase;

            if (item.tagName && _eqi(item.tagName, "link") && _eqi(item.getAttribute("type"), "text/css")) {
                var style = "@import url('"+item.getAttribute("href")+"');";
                applyStyle(item, style);
            } else if(item.tagName && _eqi(item.tagName, "style") && _eqi(item.getAttribute("type"), "text/css")) {
                var innerText = [];
                //compliant browsers know childnodes
                if(item.childNodes) {
                    var len = item.childNodes.length;
                    for(var cnt = 0; cnt < len; cnt++) {
                        innerText.push(item.childNodes[cnt].innerHTML || item.childNodes[cnt].data);
                    }
                //non compliant ones innerHTML
                } else if(item.innerHTML) {
                    innerText.push(item.innerHTML);
                }

                var style = innerText.join("");
                applyStyle(item, style);
            }
        });

        try {
            var scriptElements = this.findByTagNames(item, {"link":true,"style":true}, true);
            if (scriptElements == null) return;
            for (var cnt = 0; cnt < scriptElements.length; cnt++) {
                execCss(scriptElements[cnt]);
            }

        } finally {
            //the usual ie6 fix code
            //the IE6 garbage collector is broken
            //nulling closures helps somewhat to reduce
            //mem leaks, which are impossible to avoid
            //at this browser
            execCss = null;
            applyStyle = null;
        }
    },


    deleteScripts: function(nodeList) {
        if(!nodeList || !nodeList.length) return;
        var len = nodeList.length;
        for(var cnt = 0; cnt < len; cnt++) {
             var item = nodeList[cnt];
             var src = item.getAttribute('src');
             if (src  && src.length > 0 && (src.indexOf("/jsf.js") != -1 || src.indexOf("/jsf-uncompressed.js") != -1))  {
                        continue;
             }
             this.deleteItem(item);
        }
    },

    /**
     * Run through the given Html item and execute the inline scripts
     * (IE doesn't do this by itself)
     * @param {Node} item
     */
    runScripts: function(item, xmlData) {
        var finalScripts    = [];
        var execScrpt       = this._Lang.hitch(this, function(item) {
            if (item.tagName && this._Lang.equalsIgnoreCase(item.tagName, "script")) {
                var src = item.getAttribute('src');
                if ('undefined' != typeof src
                        && null != src
                        && src.length > 0
                        ) {
                    //we have to move this into an inner if because chrome otherwise chokes
                    //due to changing the and order instead of relying on left to right
                    if ((src.indexOf("ln=scripts") == -1 && src.indexOf("ln=javax.faces") == -1) || (src.indexOf("/jsf.js") == -1
                            && src.indexOf("/jsf-uncompressed.js") == -1))  {
                        if (finalScripts.length) {
                            //script source means we have to eval the existing
                            //scripts before running the include
                            this._RT.globalEval(finalScripts.join("\n"));

                            finalScripts = [];
                        }
                        //if jsf.js is already registered we do not replace it anymore
                        if(!window.jsf) {
                            this._RT.loadScriptEval(src, item.getAttribute('type'), false, "UTF-8", false);
                        }
                    }
                    //TODO handle embedded scripts
                } else {
                    // embedded script auto eval
                    var test = (!xmlData) ? item.text : this._Lang.serializeChilds(item);
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
                    //we store the script for less calls to eval
                    finalScripts.push(test);

                }
            }
        });
        try {
            var scriptElements = this.findByTagName(item, "script", true);
            if (scriptElements == null) return;
            for (var cnt = 0; cnt < scriptElements.length; cnt++) {
                execScrpt(scriptElements[cnt]);
            }
            if (finalScripts.length) {
                this._RT.globalEval(finalScripts.join("\n"));
            }
        } finally {
            //the usual ie6 fix code
            //the IE6 garbage collector is broken
            //nulling closures helps somewhat to reduce
            //mem leaks, which are impossible to avoid
            //at this browser
            execScrpt = null;
        }
    },


    /**
     * determines to fetch a node
     * from its id or name, the name case
     * only works if the element is unique in its name
     * @param {String} elem
     */
    byIdOrName: function(elem) {
        if(!this._Lang.isString(elem)) return elem;
        if(!elem) return null;
        var ret = this.byId(elem);
        if(ret) return ret;
        //we try the unique name fallback
        var items = document.getElementsByName(elem);
        return ((items.length == 1)? items[0]: null);
    },

    /**
     * node id or name, determines the valid form identifier of a node
     * depending on its uniqueness
     *
     * Usually the id is chosen for an elem, but if the id does not
     * exist we try a name fallback. If the passed element has a unique
     * name we can use that one as subsequent identifier.
     *
     *
     * @param {String} elem
     */
    nodeIdOrName: function(elem) {
        if (elem) {
            //just to make sure that the pas

            elem = this.byId(elem);
            if(!elem) return null;
            //detached element handling, we also store the element name
            //to get a fallback option in case the identifier is not determinable
            // anymore, in case of a framework induced detachment the element.name should
            // be shared if the identifier is not determinable anymore
            //the downside of this method is the element name must be unique
            //which in case of jsf it is
            var elementId = elem.id || elem.name;
            if ((elem.id == null || elem.id == '') && elem.name) {
                elementId = elem.name;

                //last check for uniqueness
                if(this.getElementsByName(elementId).length > 1) {
                    //no unique element name so we need to perform
                    //a return null to let the caller deal with this issue
                    return null;
                }
            }
            return elementId;
        }
        return null;
    },

    deleteItems: function(items) {
        if(! items || ! items.length) return;
        for(var cnt = 0; cnt < items.length; cnt++) {
            this.deleteItem(items[cnt]);
        }
    },

    /**
     * Simple delete on an existing item
     */
    deleteItem: function(itemIdToReplace) {
        var item = this.byId(itemIdToReplace);
        if (!item) {
            throw Error("_Dom.deleteItem  Unknown Html-Component-ID: " + itemIdToReplace);
        }

        this._removeNode(item, false);
    },

    /**
     * creates a node upon a given node name
     * @param nodeName {String} the node name to be created
     * @param attrs {Array} a set of attributes to be set
     */
    createElement: function(nodeName, attrs) {
        var ret = document.createElement(nodeName);
        if(attrs) {
            for(var key in attrs) {
                this.setAttribute(ret, key, attrs[key]);
            }
        }
        return ret;
    },

    /**
     * Checks whether the browser is dom compliant.
     * Dom compliant means that it performs the basic dom operations safely
     * without leaking and also is able to perform a native setAttribute
     * operation without freaking out
     *
     *
     * Not dom compliant browsers are all microsoft browsers in quirks mode
     * and ie6 and ie7 to some degree in standards mode
     * and pretty much every browser who cannot create ranges
     * (older mobile browsers etc...)
     *
     * We dont do a full browser detection here because it probably is safer
     * to test for existing features to make an assumption about the
     * browsers capabilities
     */
    isDomCompliant: function() {
        if('undefined' == typeof this._isCompliantBrowser) {
            this._isCompliantBrowser = !! ((window.Range
                    && typeof Range.prototype.createContextualFragment == 'function') //createContextualFragment hints to a no quirks browser but we need more fallbacks
                    || document.querySelectoryAll  //query selector all hints to html5 capabilities
                    || document.createTreeWalker);   //treewalker is either firefox 3.5+ or ie9 standards mode
        }
        return this._isCompliantBrowser;
    },

    /**
     * proper insert before which takes tables into consideration as well as
     * browser deficiencies
     * @param item the node to insert before
     * @param markup the markup to be inserted
     */
    insertBefore: function(item, markup) {
        this._assertStdParams(item, markup, "insertBefore");
        markup = this._Lang.trim(markup);
        if (markup === "") return null;

        var evalNodes = this._buildEvalNodes(item, markup);
        var currentRef = item;
        var parentNode = item.parentNode;
        var ret = [];
        for(var cnt = evalNodes.length -1; cnt >= 0; cnt--) {
            currentRef = parentNode.insertBefore(evalNodes[cnt], currentRef);
            ret.push(currentRef);
        }
        ret = ret.reverse();
        this._eval(ret);
        return ret;
    },

    /**
     * proper insert before which takes tables into consideration as well as
     * browser deficiencies
     * @param item the node to insert before
     * @param markup the markup to be inserted
     */
    insertAfter: function(item, markup) {
        this._assertStdParams(item, markup, "insertAfter");
        markup = this._Lang.trim(markup);
        if (markup === "") return null;

        var evalNodes = this._buildEvalNodes(item, markup);
        var currentRef = item;
        var parentNode = item.parentNode;
        var ret = [];
        for(var cnt = 0; cnt < evalNodes.length; cnt++) {
            if(currentRef.nextSibling) {
                //TODO winmobile6 has problems with this strategy
                currentRef = parentNode.insertBefore(evalNodes[cnt], currentRef.nextSibling);
            } else {
                currentRef = parentNode.appendChild(evalNodes[cnt]);
            }
            ret.push(currentRef);
        }
        this._eval(ret);
        return ret;
    },


    /**
     * outerHTML replacement which works cross browserlike
     * but still is speed optimized
     *
     * @param item the item to be replaced
     * @param markup the markup for the replacement
     */
    outerHTML : function(item, markup) {
        this._assertStdParams(item, markup, "outerHTML");

        markup = this._Lang.trim(markup);
        if (markup !== "") {
            var ret = null;

            // we try to determine the browsers compatibility
            // level to standards dom level 2 via various methods
            if (this.isDomCompliant()) {
                ret = this._outerHTMLCompliant(item, markup);
            } else {

                ret = this._outerHTMLNonCompliant(item, markup);
            }

            // and remove the old item
            //first we have to save the node newly insert for easier access in our eval part
            this._eval(ret);
            return ret;
        }
        // and remove the old item, in case of an empty newtag and do nothing else
        this._removeNode(item, false);
        return null;
    },

    /**
     * detaches a set of nodes from their parent elements
     * in a browser independend manner
     * @param {Object} items the items which need to be detached
     * @return {Array} an array of nodes with the detached dom nodes
     */
    detach: function(items) {
        var ret = [];
        if('undefined' != typeof items.nodeType) {
            if(items.parentNode) {
                ret.push(items.parentNode.removeChild(items));
            } else {
                ret.push(items);
            }
            return ret;
        }
        //all ies treat node lists not as arrays so we have to take
        //an intermediate step
        var nodeArr = this._Lang.objToArray(items);
        for(var cnt = 0; cnt < nodeArr.length; cnt++) {
            ret.push(nodeArr[cnt].parentNode.removeChild(nodeArr[cnt]));
        }
        return ret;
    },

    _outerHTMLCompliant: function(item, markup) {
        var evalNodes;
        //table element replacements like thead, tbody etc... have to be treated differently
        var evalNodes = this._buildEvalNodes(item, markup);

        if (evalNodes.length == 1) {
            var ret = evalNodes[0];
            item.parentNode.replaceChild(ret, item);
            return ret;
        } else {
            return this.replaceElements(item, evalNodes);
        }
    },

    _isTable: function(item) {
        var itemNodeName = (item.nodeName || item.tagName).toLowerCase();
        return itemNodeName == "table";
    },

    /**
     * checks if the provided element is a subelement of a table element
     * @param itemNodeName
     */
    _isTableElement: function(item) {
      var itemNodeName = (item.nodeName || item.tagName).toLowerCase();
      return !!this.TABLE_ELEMS[itemNodeName];
    },


    /**
     * now to the evil browsers
     * of what we are dealing with is various bugs
     * first a simple replaceElement leaks memory
     * secondly embedded scripts can be swallowed upon
     * innerHTML, we probably could also use direct outerHTML
     * but then we would run into the script swallow bug
     *
     * the entire mess is called IE6 and IE7
     *
     * @param item
     * @param markup
     */
    _outerHTMLNonCompliant: function(item, markup) {
        
        var b = this._RT.browser;
        var evalNodes = null;

        try {
            //check for a subtable rendering case

            var evalNodes = this._buildEvalNodes(item, markup);

            if (evalNodes.length == 1) {
                var ret = evalNodes[0];
                this.replaceElement(item, evalNodes[0]);
                return ret;
            } else {
                return this.replaceElements(item, evalNodes);
            }

        } finally {

            var dummyPlaceHolder = this.getDummyPlaceHolder();
            //now that Microsoft has finally given
            //ie a working gc in 8 we can skip the costly operation
            if (b.isIE && b.isIE < 8) {
                this._removeChildNodes(dummyPlaceHolder, false);
            }
            dummyPlaceHolder.innerHTML = "";
        }

    },

    /**
     * non ie browsers do not have problems with embedded scripts or any other construct
     * we simply can use an innerHTML in a placeholder
     *
     * @param markup the markup to be used
     */
    _buildNodesCompliant: function(markup) {
        var dummyPlaceHolder = this.getDummyPlaceHolder(); //document.createElement("div");
        dummyPlaceHolder.innerHTML = markup;
        return this._Lang.objToArray(dummyPlaceHolder.childNodes);
    },




    /**
     * builds up a correct dom subtree
     * if the markup is part of table nodes
     * The usecase for this is to allow subtable rendering
     * like single rows thead or tbody
     *
     * @param item
     * @param markup
     */
    _buildTableNodes: function(item, markup) {
        var evalNodes;
        var itemNodeName = (item.nodeName || item.tagName).toLowerCase();
        var probe = this.getDummyPlaceHolder(); //document.createElement("div");
        if (itemNodeName == "td") {
            probe.innerHTML = "<table><tbody><tr><td></td></tr></tbody></table>";
        } else {
            probe.innerHTML = "<table><" + itemNodeName + "></" + itemNodeName + ">" + "</table>";
        }
        var depth = this._determineDepth(probe);

        this._removeChildNodes(probe, false);
        probe.innerHTML = "";

        var dummyPlaceHolder = this.getDummyPlaceHolder();//document.createElement("div");
        if (itemNodeName == "td") {
            dummyPlaceHolder.innerHTML = "<table><tbody><tr>" + markup + "</tr></tbody></table>";
        } else {
            dummyPlaceHolder.innerHTML = "<table>" + markup + "</table>";
        }
        evalNodes = dummyPlaceHolder;
        for (var cnt = 0; cnt < depth; cnt++) {
            evalNodes = evalNodes.childNodes[0];
        }
        evalNodes = (evalNodes.parentNode) ? evalNodes.parentNode.childNodes : null;
        return this.detach(evalNodes);
    }
    ,

    /**
     * builds the ie nodes properly in a placeholder
     * and bypasses a non script insert bug that way
     * @param markup the marku code
     */
    _buildNodesNonCompliant: function(markup) {

        var evalNodes = null;

        //now to the non w3c compliant browsers
        //http://blogs.perl.org/users/clinton_gormley/2010/02/forcing-ie-to-accept-script-tags-in-innerhtml.html
        //we have to cope with deficiencies between ie and its simulations in this case
        var probe = this.getDummyPlaceHolder();//document.createElement("div");

        probe.innerHTML = "<table><tbody><tr><td><div></div></td></tr></tbody></table>";

        //we have customers using html unit, this has a bug in the table resolution
        //hence we determine the depth dynamically
        var depth = this._determineDepth(probe);

        this._removeChildNodes(probe, false);
        probe.innerHTML = "";

        var dummyPlaceHolder = this.getDummyPlaceHolder();//document.createElement("div");

        //fortunately a table element also works which is less critical than form elements regarding
        //the inner content
        dummyPlaceHolder.innerHTML = "<table><tbody><tr><td>" + markup + "</td></tr></tbody></table>";
        evalNodes = dummyPlaceHolder;

        for (var cnt = 0; cnt < depth; cnt++) {
            evalNodes = evalNodes.childNodes[0];
        }
        var ret = (evalNodes.parentNode) ? this.detach(evalNodes.parentNode.childNodes) : null;

        if ('undefined' == typeof evalNodes || null == evalNodes) {
            //fallback for htmlunit which should be good enough
            //to run the tests, maybe we have to wrap it as well
            dummyPlaceHolder.innerHTML = "<div>" + markup + "</div>";
            //note this is triggered only in htmlunit no other browser
            //so we are save here
            evalNodes = this.detach(dummyPlaceHolder.childNodes[0].childNodes);
        }

        this._removeChildNodes(dummyPlaceHolder, false);
        //ie fix any version, ie does not return true javascript arrays so we have to perform
        //a cross conversion
        return ret;

    },

    _determineDepth: function(probe) {
        var depth = 0;
        var newProbe = probe;
        for (;newProbe &&
                newProbe.childNodes &&
                newProbe.childNodes.length &&
                newProbe.nodeType == 1; depth++) {
            newProbe = newProbe.childNodes[0];
        }
        return depth;
    },


    //now to another nasty issue:
    //for ie we have to walk recursively over all nodes:
    //http://msdn.microsoft.com/en-us/library/bb250448%28VS.85%29.aspx
    //http://weblogs.java.net/blog/driscoll/archive/2009/11/13/ie-memory-management-and-you
    //http://home.orange.nl/jsrosman/
    //http://www.quirksmode.org/blog/archives/2005/10/memory_leaks_li.html
    //http://www.josh-davis.org/node/7
    _removeNode: function(node, breakEventsOpen) {
        if (!node) return;
        var b = this._RT.browser;
        if (this.isDomCompliant()) {
            //recursive descension only needed for old ie versions
            //all newer browsers cleanup the garbage just fine without it
            //thank you
            if ('undefined' != typeof node.parentNode && null != node.parentNode) //if the node has a parent
                node.parentNode.removeChild(node);
            return;
        }

        //now to the browsers with non working garbage collection
        this._removeChildNodes(node, breakEventsOpen);

        try {
            //outer HTML setting is only possible in earlier IE versions all modern browsers throw an exception here
            //again to speed things up we precheck first
            if(!this._isTableElement(node)) {
                //we do not do a table structure innnerhtml on table elements except td
                //htmlunit rightfully complains that we should not do it
                node.innerHTML = "";
            }
            if (b.isIE && 'undefined' != typeof node.outerHTML) {//ie8+ check done earlier we skip it here
                node.outerHTML = '';
            } else {
                node = this.detach(node)[0];
            }
            if (!b.isIEMobile) {
                delete node;
            }
        } catch (e) {
            //on some elements we might not have covered by our table check on the outerHTML
            // can fail we skip those in favor of stability
            try {
                // both innerHTML and outerHTML fails when <tr> is the node, but in that case
                // we need to force node removal, otherwise it will be on the tree (IE 7 IE 6)
                this.detach(node);
                if (!b.isIEMobile) {
                    delete node;
                }
            } catch (e1) {
            }
        }
    }
    ,



    /**
     * recursive delete child nodes
     * node, this method only makes sense in the context of IE6 + 7 hence
     * it is not exposed to the public API, modern browsers
     * can garbage collect the nodes just fine by doing the standard removeNode method
     * from the dom API!
     *
     * @param node  the node from which the childnodes have to be deletd
     * @param breakEventsOpen if set to true a standard events breaking is performed
     */
    _removeChildNodes: function(node, breakEventsOpen) {
        if (!node) return;

        //node types which cannot be cleared up by normal means
        var disallowedNodes = this.TABLE_ELEMS;

        //for now we do not enable it due to speed reasons
        //normally the framework has to do some event detection
        //which we cannot do yet, I will dig for options
        //to enable it in a speedly manner
        //ie7 fixes this area anyway
        //this.breakEvents(node);

        var b = this._RT.browser;
        if (breakEventsOpen) {
            this.breakEvents(node);
        }

        for (var cnt = node.childNodes.length - 1; cnt >= 0; cnt -= 1) {
            var childNode = node.childNodes[cnt];
            //we cannot use our generic recursive tree walking due to the needed head recursion
            //to clean it up bottom up, the tail recursion we were using in the search either would use more time
            //because we had to walk and then clean bottom up, so we are going for a direct head recusion here
            if ('undefined' != typeof childNode.childNodes && node.childNodes.length)
                this._removeChildNodes(childNode);
            try {
                var nodeName = (childNode.nodeName || childNode.tagName) ? (childNode.nodeName || childNode.tagName).toLowerCase() : null;
                //ie chokes on clearing out table inner elements, this is also covered by our empty
                //catch block, but to speed things up it makes more sense to precheck that
                if (!disallowedNodes[nodeName]) {
                    //outer HTML setting is only possible in earlier IE versions all modern browsers throw an exception here
                    //again to speed things up we precheck first
                    if(!this._isTableElement(childNode)) {    //table elements cannot be deleted
                        childNode.innerHTML = "";
                    }
                    if (b.isIE && b.isIE < 8 && 'undefined' != childNode.outerHTML)
                        childNode.outerHTML = '';
                    else {
                        node.removeChild(childNode);
                    }
                    if (!b.isIEMobile) {
                        delete childNode;
                    }
                }
            } catch (e) {
                //on some elements the outerHTML can fail we skip those in favor
                //of stability

            }
        }
    }
    ,

    /**
     * build up the nodes from html markup in a browser independend way
     * so that it also works with table nodes
     *
     * @param item the parent item upon the nodes need to be processed upon after building
     * @param markup the markup to be built up
     */
    _buildEvalNodes: function(item, markup) {
        var evalNodes = null;
        if (this._isTableElement(item)) {
            evalNodes = this._buildTableNodes(item, markup);
        } else {
            evalNodes = (this.isDomCompliant()) ? this._buildNodesCompliant(markup): this._buildNodesNonCompliant(markup);
        }
        return evalNodes;
    },

    /**
     * we have lots of methods with just an item and a markup as params
     * this method builds an assertion for those methods to reduce code
     *
     * @param item  the item to be tested
     * @param markup the mark
     * @param caller
     */
    _assertStdParams: function(item, markup, caller) {
         //internal error
         if(!caller) throw Error("Caller must be set for assertion");
         if (!item) {
            throw Error(this._Lang.getMessage("ERR_MUST_BE_PROVIDED1",null,"myfaces._impl._util._Dom."+caller, "item"));
        }
        if (!markup) {
            throw Error(this._Lang.getMessage("ERR_MUST_BE_PROVIDED1",null, "myfaces._impl._util._Dom."+caller, "markup"));
        }
    },

    /**
     * internal eval handler used by various functions
     * @param _nodeArr
     */
    _eval: function(_nodeArr) {
        if (this.isManualScriptEval()) {
            var isArr = _nodeArr instanceof Array;
            if (isArr && _nodeArr.length) {
                for (var cnt = 0; cnt < _nodeArr.length; cnt++) {
                    this.runScripts(_nodeArr[cnt]);
                }
            } else if (!isArr) {
                this.runScripts(_nodeArr);
            }
        }
    },

    /**
     * break the standard events from an existing dom node
     * (note this method is not yet used, but can be used
     * by framework authors to get rid of ie circular event references)
     *
     * another way probably would be to check all attributes of a node
     * for a function and if one is present break it by nulling it
     * I have to do some further investigation on this.
     *
     * The final fix is to move away from ie6 at all which is the root cause of
     * this.
     *
     * @param node the node which has to be broken off its events
     */
    breakEvents: function(node) {
        if (!node) return;
        var evtArr = this.IE_QUIRKS_EVENTS;
        for (var key in evtArr) {
            if (key != "onunload" && node[key]) {
                node[key] = null;
            }
        }
    }
    ,


    /**
     * for performance reasons we work with replaceElement and replaceElements here
     * after measuring performance it has shown that passing down an array instead
     * of a single node makes replaceElement twice as slow, however
     * a single node case is the 95% case
     *
     * @param item
     * @param evalNodes
     */
    replaceElement: function(item, evalNode) {

        var _Browser = this._RT.browser;
        if (!_Browser.isIE || _Browser.isIE >= 8) {
            //standards conform no leaking browser
            item.parentNode.replaceChild(evalNode, item);
        } else {
            //browsers with defect garbage collection
            item.parentNode.insertBefore(evalNode, item);
            this._removeNode(item, false);
        }
    }
    ,


    /**
     * replaces an element with another element or a set of elements
     *
     * @param item the item to be replaced
     *
     * @param evalNodes the elements
     */
    replaceElements: function (item, evalNodes) {
        var evalNodesDefined = evalNodes && 'undefined' != typeof evalNodes.length;
        if (!evalNodesDefined) {
            throw new Error(this._Lang.getMessage("ERR_REPLACE_EL"));
        }

        var parentNode = item.parentNode;

        var sibling = item.nextSibling;
        var resultArr = this._Lang.objToArray(evalNodes);

        for (var cnt = 0; cnt < resultArr.length; cnt++) {
            if (cnt == 0) {
                this.replaceElement(item, resultArr[cnt]);
            } else {
                if (sibling) {
                    parentNode.insertBefore(resultArr[cnt], sibling);
                } else {
                    parentNode.appendChild(resultArr[cnt]);

                }
            }
        }

        return resultArr;
    }
    ,

    /**
     * optimized search for an array of tag names
     * deep scan will always be performed.
     * @param fragment the fragment which should be searched for
     * @param tagNames an map indx of tag names which have to be found
     * 
     */
    findByTagNames: function(fragment, tagNames) {
        if(!fragment) {
            throw Error(this._Lang.getMessage("ERR_MUST_BE_PROVIDED1",null, "myfaces._impl._util._Dom.findByTagNames", "fragment"));
        }
        if(!tagNames) {
            throw Error(this._Lang.getMessage("ERR_MUST_BE_PROVIDED1",null, "myfaces._impl._util._Dom.findByTagNames", "tagNames"));
        }

        var nodeType = fragment.nodeType;
        if(nodeType != 1 && nodeType != 9 && nodeType != 11) return null;

        //we can use the shortcut
        if(fragment.querySelectorAll) {
           var query = [];
           for(var key in tagNames) {
               query.push(key);
           }
           var res = [];
           if(fragment.tagName && tagNames[fragment.tagName.toLowerCase()]) {
               res.push(fragment);
           }
           return res.concat(this._Lang.objToArray( fragment.querySelectorAll(query.join(", "))));
        }


        //now the filter function checks case insensitively for the tag names needed
        var filter = function(node) {
            return node.tagName && tagNames[node.tagName.toLowerCase()];
        };

        //now we run an optimized find all on it
        try {
            return this.findAll(fragment, filter, true);
        } finally {
            //the usual IE6 is broken, fix code
            filter = null;
        }
    }
    ,

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
     * @return the child elements as array or null if nothing is found
     *
     */
    findByTagName : function(fragment, tagName, deepScan) {
        if(!fragment) {
            throw Error(this._Lang.getMessage("ERR_MUST_BE_PROVIDED1",null, "myfaces._impl._util._Dom.findByTagName", "fragment"));
        }
        if(!tagName) {
            throw Error(this._Lang.getMessage("ERR_MUST_BE_PROVIDED1",null, "myfaces._impl._util._Dom.findByTagName", "tagName"));
        }

        var nodeType = fragment.nodeType;
        if(nodeType != 1 && nodeType != 9 && nodeType != 11) return null;


        //remapping to save a few bytes
        var _Lang = this._Lang;

        deepScan = !!deepScan;

        //elements by tagname is the fastest, ie throws an error on fragment.getElementsByTagName, the exists type
        //via namespace array checking is safe
        if (deepScan && _Lang.exists(fragment, "getElementsByTagName")) {
            var ret = _Lang.objToArray(fragment.getElementsByTagName(tagName));
            if (fragment.tagName && _Lang.equalsIgnoreCase(fragment.tagName, tagName)) ret.unshift(fragment);
            return ret;
        } else if (deepScan) {
            //no node type with child tags we can handle that without node type checking
            return null;
        }
        //since getElementsByTagName is a standardized dom node function and ie also supports
        //it since 5.5
        //we need no fallback to the query api and the recursive filter
        //also is only needed in case of no deep scan or non dom elements

        var filter = function(node) {
            return node.tagName && _Lang.equalsIgnoreCase(node.tagName, tagName);
        };
        try {
            return this.findAll(fragment, filter, deepScan);
        } finally {
            //the usual IE6 is broken, fix code
            filter = null;
            _Lang = null;
        }

    }
    ,

    findByName : function(fragment, name, deepScan) {
        if(!fragment) {
            throw Error(this._Lang.getMessage("ERR_MUST_BE_PROVIDED1",null, "myfaces._impl._util._Dom.findByName", "fragment"));
        }
        if(!name) {
            throw Error(this._Lang.getMessage("ERR_MUST_BE_PROVIDED1",null, "myfaces._impl._util._Dom.findByName", "name"));
        }


        var nodeType = fragment.nodeType;
        if(nodeType != 1 && nodeType != 9 && nodeType != 11) return null;

        var _Lang = this._Lang;
        var filter = function(node) {
            return  node.name && _Lang.equalsIgnoreCase(node.name, name);
        };
        try {
            deepScan = !!deepScan;

            //elements byName is the fastest
            if (deepScan && _Lang.exists(fragment, "getElementsByName")) {
                var ret = _Lang.objToArray(fragment.getElementsByName(name));
                if (fragment.name == name) ret.unshift(fragment);
                return ret;
            }

            if (deepScan && _Lang.exists(fragment, "querySelectorAll")) {
                try {
                    var newName = name;
                    if (_Lang.isString(newName)) {
                        newName = _Lang.escapeString(newName);
                    }
                    var result = fragment.querySelectorAll("[name=" + newName + "]");
                    if (fragment.nodeType == 1 && filter(fragment)) {
                        result = (result == null) ? [] : _Lang.objToArray(result);
                        result.push(fragment);
                    }
                    return result;
                } catch(e) {
                    //in case the selector bombs we retry manually
                }
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
     * a filtered findAll for subdom treewalking
     * (which uses browser optimizations wherever possible)
     *
     * @param {|Node|} rootNode the rootNode so start the scan
     * @param filter filter closure with the syntax {boolean} filter({Node} node)
     * @param deepScan if set to true or not set at all a deep scan is performed (for form scans it does not make much sense to deeply scan)
     */
    findAll : function(rootNode, filter, deepScan) {
        this._Lang.assertType(filter, "function");
        deepScan = !!deepScan;

        if (document.createTreeWalker && NodeFilter) {
            return this._iteratorSearchAll(rootNode, filter, deepScan);
        } else {
            return this._recursionSearchAll(rootNode, filter, deepScan);
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
    _recursionSearchAll: function(rootNode, filter, deepScan) {
        var ret = [];
        //fix the value to prevent undefined errors

        if (filter(rootNode)) {
            ret.push(rootNode);
            if (!deepScan) return ret;
        }

        //
        if (!rootNode.childNodes) {
            return ret;
        }

        //subfragment usecases

        var retLen = ret.length;
        var childLen = rootNode.childNodes.length;
        for (var cnt = 0; (deepScan || retLen == 0) && cnt < childLen; cnt++) {
            ret = ret.concat(this._recursionSearchAll(rootNode.childNodes[cnt], filter, deepScan));
        }
        return ret;
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
    _iteratorSearchAll: function(rootNode, filter, deepScan) {
        var retVal = [];
        //Works on firefox and webkit, opera and ie have to use the slower fallback mechanis
        //we have a tree walker in place this allows for an optimized deep scan
        if (filter(rootNode)) {

            retVal.push(rootNode);
            if (!deepScan) {
                return retVal;
            }
        }
        //we use the reject mechanism to prevent a deep scan reject means any
        //child elements will be omitted from the scan
        var walkerFilter = function (node) {
            var retCode = (filter(node)) ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_SKIP;
            retCode = (!deepScan && retCode == NodeFilter.FILTER_ACCEPT) ? NodeFilter.FILTER_REJECT : retCode;
            if (retCode == NodeFilter.FILTER_ACCEPT || retCode == NodeFilter.FILTER_REJECT) {
                retVal.push(node);
            }
            return retCode;
        };
        var treeWalker = document.createTreeWalker(rootNode, NodeFilter.SHOW_ELEMENT, walkerFilter, false);
        //noinspection StatementWithEmptyBodyJS
        while (treeWalker.nextNode());
        return retVal;
    }
    ,

    /**
     * bugfixing for ie6 which does not cope properly with setAttribute
     */
    setAttribute : function(node, attr, val) {

        if (!node) {
            throw Error(this._Lang.getMessage("ERR_MUST_BE_PROVIDED1",null, "_Dom.setAttribute", "node {DomNode}"));
        }
        if (!attr) {
            throw Error(this._Lang.getMessage("ERR_MUST_BE_PROVIDED1",null, "_Dom.setAttribute", "attr {String}"));
        }

        //quirks mode and ie7 mode has the attributes problems ie8 standards mode behaves like
        //a good citizen
        var _Browser = this._RT.browser;
        //in case of ie > ie7 we have to check for a quirks mode setting
        if (!_Browser.isIE || _Browser.isIE > 7 && this.isDomCompliant()) {
            if (!node.setAttribute) {
                return;
            }
            node.setAttribute(attr, val);
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
        attr = attr.toLowerCase();

        if (attr === "class") {
            //setAttribute does not work for winmobile browsers
            //firect calls work
            node.className = val;
        } else if (attr === "name") {
            //the ie debugger fails to assign the name via setAttr
            //in quirks mode
            node[attr] = val;
        } else if (attr === "for") {
            if (!_Browser.isIEMobile || _Browser.isIEMobile >= 7) {
                node.setAttribute("htmlFor", val);
            } else {
                node.htmlFor = val;
            }
        } else if (attr === "style") {
            //We have to split the styles here and assign them one by one
            var styles = val.split(";");
            var stylesLen = styles.length;
            for (var loop = 0; loop < stylesLen; loop++) {
                var keyVal = styles[loop].split(":");
                if (keyVal[0] != "" && keyVal[0] == "opacity") {
                    //special ie quirks handling for opacity

                    var opacityVal = Math.max(100, Math.round(parseFloat(keyVal[1]) * 10));
                    //probably does not work in ie mobile anyway
                    if (!_Browser.isIEMobile || _Browser.isIEMobile >= 7) {
                        node.style.setAttribute("arrFilter", "alpha(opacity=" + opacityVal + ")");
                    }
                    //if you need more hacks I would recommend
                    //to use the class attribute and conditional ie includes!
                } else if (keyVal[0] != "") {
                    if (!_Browser.isIEMobile || _Browser.isIEMobile >= 7) {
                        node.style.setAttribute(keyVal[0], keyVal[1]);
                    } else {
                        node.style[keyVal[0]] = keyVal[1];
                    }
                }
            }
        } else {
            //check if the attribute is an event, since this applies only
            //to quirks mode of ie anyway we can live with the standard html4/xhtml
            //ie supported events
            if (this.IE_QUIRKS_EVENTS[attr]) {
                if (this._Lang.isString(attr)) {
                    //event resolves to window.event in ie
                    node.setAttribute(attr, function() {
                        //event implicitly used
                        return this._Lang.globalEval(val);
                    });
                }
            } else {
                //unknown cases we try to catch them via standard setAttributes
                if (!_Browser.isIEMobile || _Browser.isIEMobile >= 7) {
                    node.setAttribute(attr, val);
                } else {
                    node[attr] = val;
                }
            }
        }
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
     * @param {Node} elem - element as source, can be detached, undefined or null
     *
     * @return either null or a form node if it could be determined
     */
    fuzzyFormDetection : function(elem) {
        if (!document.forms || !document.forms.length) {
            return null;
        }

        // This will not work well on portlet case, because we cannot be sure
        // the returned form is right one.
        //we can cover that case by simply adding one of our config params
        //the default is the weaker, but more correct portlet code
        //you can override it with myfaces_config.no_portlet_env = true globally
        else if (1 == document.forms.length && this._RT.getGlobalConfig("no_portlet_env", false)) {
            return document.forms[0];
        }
        if (!elem) {
            return null;
        }

        //before going into the more complicated stuff we try the simple approach
        var ret = null;
        var elemForm =  null;
        if (!this._Lang.isString(elem)) {

            //html 5 allows finally the detachement of elements
            //by introducing a form attribute

            elemForm = this.html5FormDetection(elem);
            if (elemForm) {
                return elemForm;
            }

            //element of type form then we are already
            //at form level for the issuing element
            //https://issues.apache.org/jira/browse/MYFACES-2793

            if (this._Lang.equalsIgnoreCase(elem.tagName, "form")) {
                return elem;
            }
            ret = this.getParent(elem, "form");
            if (ret) return ret;
        } else {
            elem = this.byId(elem);
            // element might have removed from DOM in method processUpdate
            if (!elem){
            	return null;
            }
            ret = this.getParent(elem, "form");
            if (ret) return ret;
        }

        var id = elem.id || null;
        var name = elem.name || null;
        //a framework in a detachment case also can replace an existing identifier element
        // with a name element
        name = name || id;
        var foundForm;

        if (id && '' != id) {
            //we have to assert that the element passed down is detached
            var domElement = this.byId(id);
            elemForm = this.html5FormDetection(domElement);
            if (elemForm) {
                return elemForm;
            }

            if (domElement) {
                foundForm = this.getParent(domElement, "form");
                if (null != foundForm) return foundForm;
            }
        }

        /**
         * name check
         */
        var foundElements = [];

        /**
         * the lesser chance is the elements which have the same name
         * (which is the more likely case in case of a brute dom replacement)
         */
        var nameElems = document.getElementsByName(name);
        if (nameElems) {
            for (var cnt = 0; cnt < nameElems.length && foundElements.length < 2; cnt++) {
                // we already have covered the identifier case hence we only can deal with names,
                foundForm = this.getParent(nameElems[cnt], "form");
                if (null != foundForm) {
                    foundElements.push(foundForm);
                }
            }
        }

        return (1 == foundElements.length ) ? foundElements[0] : null;
    }
    ,

    html5FormDetection: function(item) {
        if (this._RT.browser.isIEMobile && this._RT.browser.isIEMobile <= 7) {
            return null;
        }
        var elemForm = this.getAttribute(item, "form");
        if (elemForm) {
            return this.byId(elemForm);
        }
        return null;
    }
    ,

    /**
     * gets a parent of an item with a given tagname
     * @param {Node} item - child element
     * @param {String} tagName - TagName of parent element
     */
    getParent : function(item, tagName) {

        if (!item) {
            throw Error(this._Lang.getMessage("ERR_MUST_BE_PROVIDED1",null, "_Dom.getParent", "item {DomNode}"));
        }

        var _Lang = this._Lang;
        var searchClosure = function(parentItem) {
            return parentItem && parentItem.tagName
                    && _Lang.equalsIgnoreCase(parentItem.tagName, tagName);
        };
        try {
            return this.getFilteredParent(item, searchClosure);
        } finally {
            searchClosure = null;
            _Lang = null;
        }
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
        if (!item) {
            throw Error(this._Lang.getMessage("ERR_MUST_BE_PROVIDED1",null, "_Dom.getFilteredParent", "item {DomNode}"));
        }
        if (!filter) {
            throw Error(this._Lang.getMessage("ERR_MUST_BE_PROVIDED1",null, "_Dom.getFilteredParent", "filter {function}"));
        }

        //search parent tag parentName
        var parentItem = (item.parentNode) ? item.parentNode : null;

        while (parentItem && !filter(parentItem)) {
            parentItem = parentItem.parentNode;
        }
        return (parentItem) ? parentItem : null;
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
        if (!item) {
            throw Error(this._Lang.getMessage("ERR_MUST_BE_PROVIDED1",null, "_Dom.getFilteredParent", "item {DomNode}"));
        }
        if (!filter) {
            throw Error(this._Lang.getMessage("ERR_MUST_BE_PROVIDED1",null, "_Dom.getFilteredParent", "filter {function}"));
        }

        var childs = item.childNodes;
        if (!childs) {
            return null;
        }
        for (var c = 0, cLen = childs.length; c < cLen; c++) {
            if (filter(childs[c])) {
                return childs[c];
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
        var _Lang = this._Lang;

        function filter(node) {
            return node.tagName
                    && _Lang.equalsIgnoreCase(node.tagName, childName)
                    && (!itemName || (itemName && itemName == node.getAttribute("name")));

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
    concatCDATABlocks : function(/*Node*/ node) {
        var cDataBlock = [];
        // response may contain several blocks
        for (var i = 0; i < node.childNodes.length; i++) {
            cDataBlock.push(node.childNodes[i].data);
        }
        return cDataBlock.join('');
    }
    ,

    isManualScriptEval: function() {

        if (!this._Lang.exists(myfaces, "config._autoeval")) {

            //now we rely on the document being processed if called for the first time
            var evalDiv = document.createElement("div");
            this._Lang.reserveNamespace("myfaces.config._autoeval");
            //null not swallowed
            myfaces.config._autoeval = false;

            var markup = "<script type='text/javascript'> myfaces.config._autoeval = true; </script>";
            //now we rely on the same replacement mechanisms as outerhtml because
            //some browsers have different behavior of embedded scripts in the contextualfragment
            //or innerhtml case (opera for instance), this way we make sure the
            //eval detection is covered correctly
            this.setAttribute(evalDiv, "style", "display:none");

            //it is less critical in some browsers (old ie versions)
            //to append as first element than as last
            //it should not make any difference layoutwise since we are on display none anyway.
            this.insertFirst(evalDiv);

            //we remap it into a real boolean value
            if (window.Range
                    && typeof Range.prototype.createContextualFragment == 'function') {
                this._outerHTMLCompliant(evalDiv, markup);
            } else {
                this._outerHTMLNonCompliant(evalDiv, markup);
            }

        }

        return  !myfaces.config._autoeval;
        /* var d = _this.browser;


         return (_this.exists(d, "isIE") &&
         ( d.isIE > 5.5)) ||
         //firefox at version 4 beginning has dropped
         //auto eval to be compliant with the rest
         (_this.exists(d, "isFF") &&
         (d.isFF > 3.9)) ||
         (_this.exists(d, "isKhtml") &&
         (d.isKhtml > 0)) ||
         (_this.exists(d, "isWebKit") &&
         (d.isWebKit > 0)) ||
         (_this.exists(d, "isSafari") &&
         (d.isSafari > 0));
         */
        //another way to determine this without direct user agent parsing probably could
        //be to add an embedded script tag programmatically and check for the script variable
        //set by the script if existing, the add went through an eval if not then we
        //have to deal with it ourselves, this might be dangerous in case of the ie however
        //so in case of ie we have to parse for all other browsers we can make a dynamic
        //check if the browser does auto eval

    }
    ,

    isMultipartCandidate: function(executes) {
        if (this._Lang.isString(executes)) {
            executes = this._Lang.strToArray(executes, /\s+/);
        }

        for (var exec in executes) {
            var element = this.byId(executes[exec]);
            var inputs = this.findByTagName(element, "input", true);
            for (var key in inputs) {
                if (this.getAttribute(inputs[key], "type") == "file") return true;
            }
        }
        return false;
    }
    ,


    insertFirst: function(newNode) {
        var body = document.body;
        if (body.childNodes.length > 0) {
            body.insertBefore(newNode, body.firstChild);
        } else {
            body.appendChild(newNode);
        }
    }
    ,

    byId: function(id) {
        return this._Lang.byId(id);
    }
    ,

    getDummyPlaceHolder: function() {
        var created = false;
        if (!this._dummyPlaceHolder) {
            this._dummyPlaceHolder = document.createElement("div");
            created = true;
        }

        //ieMobile in its 6.1-- incarnation cannot handle innerHTML detached objects so we have
        //to attach the dummy placeholder, we try to avoid it for
        //better browsers so that we do not have unecessary dom operations
        if (this._RT.browser.isIEMobile && created) {
            this.insertFirst(this._dummyPlaceHolder);

            this.setAttribute(this._dummyPlaceHolder, "style", "display: none");

        }

        return this._dummyPlaceHolder;
    },

    /**
     * fetches the window id for the current request
     * note, this is a preparation method for jsf 2.2
     */
    getWindowId: function() {
        var href = window.location.href;
        var windowId = "windowId";
        var regex = new RegExp("[\\?&]" + windowId + "=([^&#\\;]*)");
        var results = regex.exec(href);
        return (results != null) ? results[1] : null;
    }

});


