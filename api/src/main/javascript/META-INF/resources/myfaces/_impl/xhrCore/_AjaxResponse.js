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

/** @namespace myfaces._impl.xhrCore._AjaxResponse */
myfaces._impl.core._Runtime.extendClass("myfaces._impl.xhrCore._AjaxResponse", myfaces._impl.xhrCore._FinalizeableObj, {

    /*partial response types*/
    RESP_PARTIAL : "partial-response",
    RESP_TYPE_ERROR : "error",
    RESP_TYPE_REDIRECT : "redirect",
    RESP_TYPE_CHANGES : "changes",

    /*partial commands*/
    CMD_CHANGES : "changes",
    CMD_UPDATE : "update",
    CMD_DELETE : "delete",
    CMD_INSERT : "insert",
    CMD_EVAL : "eval",
    CMD_ERROR : "error",
    CMD_ATTRIBUTES : "attributes",
    CMD_EXTENSION : "extension",
    CMD_REDIRECT : "redirect",

    /*other constants*/
    P_VIEWSTATE: "javax.faces.ViewState",
    P_VIEWROOT: "javax.faces.ViewRoot",
    P_VIEWHEAD: "javax.faces.ViewHead",
    P_VIEWBODY: "javax.faces.ViewBody",


    /**
     * Constructor
     * @param {function} base request classed parent object
     * @param {function} onException
     * @param {function} onWarning
     */
    constructor_: function(onException, onWarning) {
        //List of non form elements to be updated (which can have forms embedded)
        this._updateElems = [];
        // List of forms to be updated if any inner block is updated
        this._updateForms = [];
        this._onException = onException;
        this._onWarning = onWarning;

        this.appliedViewState = null;

        this._Lang = myfaces._impl._util._Lang;
        this._Dom = myfaces._impl._util._Dom;
        this._RT = myfaces._impl.core._Runtime;
        this._Impl = this._RT.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
    },
    /**
     * uses response to start Html element replacement
     *
     * @param {Object} request (xhrRequest) - xhr request object
     * @param {Object} context (Map) - AJAX context
     *
     * A special handling has to be added to the update cycle
     * according to the JSDoc specs if the CDATA block contains html tags the outer rim must be stripped
     * if the CDATA block contains a head section the document head must be replaced
     * and if the CDATA block contains a body section the document body must be replaced!
     *
     */
    processResponse : function(request, context) {
        try {
            var _Impl = this._Impl;

            // TODO:
            // Solution from
            // http://www.codingforums.com/archive/index.php/t-47018.html
            // to solve IE error 1072896658 when a Java server sends iso88591
            // istead of ISO-8859-1

            if (!request) {
                throw Exception(this._Lang.getMessage("ERR_EMPTY_RESPONSE",null,"jsf.ajaxResponse"));
            }

            if (!this._Lang.exists(request, "responseXML")) {
                _Impl.sendError(request, context, myfaces._impl.core.Impl.EMPTY_RESPONSE);
                return;
            }
            //check for a parseError under certain browsers

            var xmlContent = request.responseXML;
            //ie6+ keeps the parsing response under xmlContent.parserError
            //while the rest of the world keeps it as element under the first node

            if (this._Lang.isXMLParseError(xmlContent)) {
                _Impl.sendError(request, context, myfaces._impl.core.Impl.MALFORMEDXML);
                return;
            }
            var partials = xmlContent.childNodes[0];
            if ('undefined' == typeof partials || partials == null) {
                _Impl.sendError(request, context, _Impl.MALFORMEDXML);
                return;
            } else {
                if (partials.tagName != this.RESP_PARTIAL) {
                    // IE 8 sees XML Header as first sibling ...
                    partials = partials.nextSibling;
                    if (!partials || partials.tagName != this.RESP_PARTIAL) {
                        _Impl.sendError(request, context, myfaces._impl.core.Impl.MALFORMEDXML);
                        return;
                    }
                }
            }

            var childNodesLength = partials.childNodes.length;

            for (var loop = 0; loop < childNodesLength; loop++) {
                var childNode = partials.childNodes[loop];
                var tagName = childNode.tagName;
                /**
                 * <eval>
                 *      <![CDATA[javascript]]>
                 * </eval>
                 */

                //this ought to be enough for eval
                //however the run scripts still makes sense
                //in the update and insert area for components
                //which do not use the response writer properly
                //we might add this one as custom option in update and
                //insert!
                if (tagName == this.CMD_ERROR) {
                    this.processError(request, context, childNode);
                    return;
                } else if (tagName == this.CMD_REDIRECT) {
                    if (!this.processRedirect(request, context, childNode)) return;
                } else if (tagName == this.CMD_CHANGES) {
                    if (!this.processChanges(request, context, childNode)) return;
                }
            }

            //fixup missing viewStates due to spec deficiencies
            this.fixViewStates(context);
        } catch (e) {
            this._onException(request, context, "myfaces._impl.xhrCore._AjaxResponse", "processResponse", e);
        }
    },

    fixViewStates : function(context) {

        if (null == this.appliedViewState) {
            return;
        }
        //if we set our no portlet env we safely can update all forms with
        //the new viewstate
        if (this._RT.getLocalOrGlobalConfig(context, "no_portlet_env", false)) {
            for (var cnt = document.forms.length - 1; cnt >= 0; cnt --) {
                this._setVSTForm(document.forms[cnt]);
            }
            return;
        }

        // Now update the forms that were not replaced but forced to be updated, because contains child ajax tags
        // we should only update forms with view state hidden field. If by some reason, the form was set to be
        // updated but the form was replaced, it does not have hidden view state, so later in changeTrace processing the
        // view state is updated.

        //set the viewstates of all outer forms parents of our updated elements
        this._Lang.arrForEach(this._updateForms, this._setVSTForm, 0, this);

        //set the viewstate of all forms within our updated elements
        this._Lang.arrForEach(this._updateElems, this._setVSTInnerForms, 0, this);
    },

    /**
     * sets the viewstate element in a given form
     *
     * @param theForm the form to which the element has to be set to
     * @param doNotChange if set to true no change is performed if the element is found already to be rendered
     */
    _setVSTForm: function(theForm) {
        theForm = this._Lang.byId(theForm);
        if(!theForm) return;

        var viewStateField = (theForm.elements) ? theForm.elements[this.P_VIEWSTATE] : null;//this._Dom.findFormElement(elem, this.P_VIEWSTATE);

        if (viewStateField) {
            this._Dom.setAttribute(viewStateField, "value", this.appliedViewState);
        } else if (!viewStateField) {
            var element = this._Dom.getDummyPlaceHolder();
            element.innerHTML = ["<input type='hidden'", "id='", this.P_VIEWSTATE ,"' name='", this.P_VIEWSTATE ,"' value='" , this.appliedViewState , "' />"].join("");
            //now we go to proper dom handling after having to deal with another ie screwup
            try {
                theForm.appendChild(element.childNodes[0]);
            } finally {
                element.innerHTML = "";
            }
        }
    },

    _setVSTInnerForms: function(elem) {
        elem = this._Dom.byIdOrName(elem);
        var replacedForms = this._Dom.findByTagName(elem, "form", false);
        var applyVST = this._Lang.hitch(this, function(elem) {
            this._setVSTForm(elem);
        });

        try {
            this._Lang.arrForEach(replacedForms, applyVST, 0, this);
        } finally {
            delete applyVST;
        }
    },



    processError : function(request, context, node) {
        /**
         * <error>
         *      <error-name>String</error-name>
         *      <error-message><![CDATA[message]]></error-message>
         * <error>
         */
        var errorName = node.firstChild.textContent || "";
        var errorMessage = node.childNodes[1].firstChild.data || "";

        var _Impl = this._Impl;

        _Impl.sendError(request, context, myfaces._impl.core.Impl.SERVER_ERROR, errorName, errorMessage);
    },

    processRedirect : function(request, context, node) {
        /**
         * <redirect url="url to redirect" />
         */
        var redirectUrl = node.getAttribute("url");
        if (!redirectUrl) {
            var _Impl = this._Impl;

            _Impl.sendError(request, context, myfaces._impl.core.Impl.MALFORMEDXML, myfaces._impl.core.Impl.MALFORMEDXML,this._Lang.getMessage("ERR_RED_URL", null, "_AjaxResponse.processRedirect"));
            return false;
        }
        redirectUrl = this._Lang.trim(redirectUrl);
        if (redirectUrl == "") {
            return false;
        }
        window.location = redirectUrl;
        return true;
    },

    /**
     * main entry point for processing the changes
     * it deals with the &lt;changes&gt; node of the
     * response
     *
     * @param request the xhr request object
     * @param context the context map
     * @param node the changes node to be processed
     */
    processChanges : function(request, context, node) {
        var changes = node.childNodes;

        //note we need to trace the changes which could affect our insert update or delete
        //se that we can realign our ViewStates afterwards
        //the realignment must happen post change processing
        
        for (var i = 0; i < changes.length; i++) {
         
            switch (changes[i].tagName) {

                case this.CMD_UPDATE:
                    if (!this.processUpdate(request, context, changes[i])) {
                        return false;
                    }
                    break;
                case this.CMD_EVAL:
                    this._Lang.globalEval(changes[i].firstChild.data);
                    break;
                case this.CMD_INSERT:
                    if (!this.processInsert(request, context, changes[i])) return false;
                    break;
                case this.CMD_DELETE:
                    if (!this.processDelete(request, context, changes[i])) return false;
                    break;
                case this.CMD_ATTRIBUTES:
                    if (!this.processAttributes(request, context, changes[i])) return false;
                    break;
                case this.CMD_EXTENSION:
                    break;
                default:
                    var _Impl = this._Impl;
                    _Impl.sendError(request, context, myfaces._impl.core.Impl.MALFORMEDXML);
                    return false;
            }
        }

        return true;
    },

    /**
     * First substep process a pending update tag
     *
     * @param request the xhr request object
     * @param context the context map
     * @param node the changes node to be processed
     */
    processUpdate : function(request, context, node) {
        if (node.getAttribute('id') == this.P_VIEWSTATE) {
            //update the submitting forms viewstate to the new value
            // The source form has to be pulled out of the CURRENT document first because the context object
            // may refer to an invalid document if an update of the entire body has occurred before this point.
            var viewStateValue = node.firstChild.nodeValue;

            var elementId = (context._mfInternal)? context._mfInternal["_mfSourceControlId"] : context.source.id;
            var sourceForm = (context._mfInternal)? (document.forms[context._mfInternal["_mfSourceFormId"]] || this._Dom.fuzzyFormDetection(elementId)) : this._Dom.fuzzyFormDetection(elementId);
            this.appliedViewState = viewStateValue;
            //source form could not be determined either over the form identifer or the element
            //we now skip this phase and just add everything we need for the fixup code

            if (!sourceForm) {
                //no source form found is not an error because
                //we might be able to recover one way or the other
                return true;
            }

            this._updateForms.push(sourceForm.id)
            //this._setVSTForm(sourceForm);
        }
        else {
            // response may contain several blocks
            var cDataBlock = this._Dom.concatCDATABlocks(node);

            switch (node.getAttribute('id')) {
                case this.P_VIEWROOT:


                    cDataBlock = cDataBlock.substring(cDataBlock.indexOf("<html"));

                    var parsedData = this._replaceHead(request, context, cDataBlock);

                    var resultNode = ('undefined' != typeof parsedData &&  null != parsedData) ? this._replaceBody(request, context, cDataBlock, parsedData) : this._replaceBody(request, context, cDataBlock);
                    if (resultNode) {
                        this._pushOperationResult(resultNode);
                    }
                    break;
                case this.P_VIEWHEAD:
                    //we cannot replace the head, almost no browser allows this, some of them throw errors
                    //others simply ignore it or replace it and destroy the dom that way!
                    this._replaceHead(request, context, cDataBlock);

                    break;
                case this.P_VIEWBODY:
                    //we assume the cdata block is our body including the tag
                    var resultNode = this._replaceBody(request, context, cDataBlock);
                    if (resultNode) {
                        this._pushOperationResult(resultNode);
                    }
                    break;

                default:
                    var resultNode = this.replaceHtmlItem(request, context, node.getAttribute('id'), cDataBlock);
                    if (resultNode) {
                        this._pushOperationResult(resultNode);
                    }
                    break;
            }
        }
        return true;
    },

    _pushOperationResult: function(resultNode) {
        var pushSubnode = this._Lang.hitch(this, function(currNode) {
            var parentForm = this._Dom.getParent(currNode, "form");
            //if possible we work over the ids
            //so that elements later replaced are referenced
            //at the latest possibility
            if (null != parentForm) {
                this._updateForms.push(parentForm.id || parentForm);
            }
            else {
                this._updateElems.push(currNode.id || currNode);
            }
        });
        var isArr = 'undefined' != typeof resultNode.length && 'undefined' == typeof resultNode.nodeType;
        if (isArr && resultNode.length) {
            for (var cnt = 0; cnt < resultNode.length; cnt++) {
                pushSubnode(resultNode[cnt]);
            }
        } else if (!isArr) {
            pushSubnode(resultNode);
        }

    },

    /**
     * replaces a current head theoretically,
     * pratically only the scripts are evaled anew since nothing else
     * can be changed.
     *
     * @param request the current request
     * @param context the ajax context
     * @param newData the data to be processed
     *
     * @return an xml representation of the page for further processing if possible
     */
    _replaceHead: function(request, context, newData) {

        var _Impl = this._Impl;

        var isWebkit = this._RT.browser.isWebKit;

        //we have to work around an xml parsing bug in Webkit
        //see https://issues.apache.org/jira/browse/MYFACES-3061
        var doc = (!isWebkit)? this._Lang.parseXML(newData) : null;

        var newHead = null;
        if (!isWebkit  && this._Lang.isXMLParseError(doc)) {
            doc = this._Lang.parseXML(newData.replace(/<!\-\-[\s\n]*<!\-\-/g, "<!--").replace(/\/\/-->[\s\n]*\/\/-->/g, "//-->"));
        }

        if (isWebkit || this._Lang.isXMLParseError(doc) ) {
            //the standard xml parser failed we retry with the stripper
            var parser = new (this._RT.getGlobalConfig("updateParser", myfaces._impl._util._HtmlStripper))();
            var headData = parser.parse(newData, "head");
            //We cannot avoid it here, but we have reduced the parsing now down to the bare minimum
            //for further processing
            newHead = this._Lang.parseXML("<head>" + headData + "</head>");
            //last and slowest option create a new head element and let the browser
            //do its slow job
            if (this._Lang.isXMLParseError(newHead)) {
                try {
                    newHead = document.createElement("head");
                    newHead.innerHTML = headData;
                } catch (e) {
                    //we give up no further fallbacks
                    _Impl.sendError(request, context, _Impl.MALFORMEDXML, _Impl.MALFORMEDXML, "Error head replacement failed reason:"+e.toString());
                    return null;
                }
            }
        } else {
            //parser worked we go on
            newHead = doc.getElementsByTagName("head")[0];
        }

        this._Dom.runScripts(newHead, true);

        return doc;
    },


    /**
     * special method to handle the body dom manipulation,
     * replacing the entire body does not work fully by simply adding a second body
     * and by creating a range instead we have to work around that by dom creating a second
     * body and then filling it properly!
     *
     * @param {Object} request our request object
     * @param {Object} context (Map) the response context
     * @param {String} newData the markup which replaces the old dom node!
     * @param {Node} parsedData (optional) preparsed XML representation data of the current document
     */
    _replaceBody : function(request, context, newData /*varargs*/) {

        var oldBody = document.getElementsByTagName("body")[0];
        var placeHolder = document.createElement("div");
        var isWebkit = this._RT.browser.isWebKit;

        placeHolder.id = "myfaces_bodyplaceholder";

        var bodyParent = oldBody.parentNode;
        this._Dom._removeChildNodes(oldBody);
        oldBody.innerHTML = "";
        var newBody = oldBody;

        newBody.appendChild(placeHolder);

        var bodyData = null;

        var doc = null;

        //we have to work around an xml parsing bug in Webkit
        //see https://issues.apache.org/jira/browse/MYFACES-3061
        if(!isWebkit) {
            doc = (arguments.length > 3) ? arguments[3] : this._Lang.parseXML(newData);
        }

        if (!isWebkit && this._Lang.isXMLParseError(doc)) {
            doc = this._Lang.parseXML(newData.replace(/<!\-\-[\s\n]*<!\-\-/g, "<!--").replace(/\/\/-->[\s\n]*\/\/-->/g, "//-->"));
        }

        if (isWebkit || this._Lang.isXMLParseError(doc)) {
            //the standard xml parser failed we retry with the stripper

            var parser = new (this._RT.getGlobalConfig("updateParser", myfaces._impl._util._HtmlStripper))();


            bodyData = parser.parse(newData, "body");
        } else {
            //parser worked we go on
            var newBodyData = doc.getElementsByTagName("body")[0];

            //speedwise we serialize back into the code
            //for code reduction, speedwise we will take a small hit
            //there which we will clean up in the future, but for now
            //this is ok, I guess, since replace body only is a small subcase
            bodyData = this._Lang.serializeChilds(newBodyData);

            if (!this._RT.browser.isIEMobile || this._RT.browser.isIEMobile >= 7) {
                //TODO check what is failing there
                for (var cnt = 0; cnt < newBodyData.attributes.length; cnt++) {
                    var value = newBodyData.attributes[cnt].value;
                    if (value)
                        this._Dom.setAttribute(newBody, newBodyData.attributes[cnt].name, value);
                }
            }
        }

        //TODO eliminate the serialisation in case of already having a parsed tree
        var returnedElement = this.replaceHtmlItem(request, context, placeHolder, bodyData);

        if (returnedElement) {
            this._pushOperationResult(returnedElement);
        }
        return returnedElement;
    },

    /**
     * Replaces HTML elements through others and handle errors if the occur in the replacement part
     *
     * @param {Object} request (xhrRequest)
     * @param {Object} context (Map)
     * @param {Object} itemIdToReplace (String|Node) - ID of the element to replace
     * @param {String} markup - the new tag
     */
    replaceHtmlItem : function(request, context, itemIdToReplace, markup) {
        try {
            //TODO make a detachement fixup which tries to replace the item
            //with the correct name upon its parent form if given


            var origIdentifier = itemIdToReplace;
            var item = (!this._Lang.isString(itemIdToReplace)) ? itemIdToReplace :
                    this._Dom.byIdOrName(itemIdToReplace);

            if (!item) {
                throw Error(this._Lang.getMessage("ERR_ITEM_ID_NOTFOUND", null,"_AjaxResponse.replaceHtmlItem",(itemIdToReplace)? itemIdToReplace.toString():"undefined"));
            }
            return this._Dom.outerHTML(item, markup);

        } catch (e) {
            this._onException(request, context, "myfaces._impl.xhrCore._AjaxResponse", "replaceHTMLItem", e);
        }
        return null;
    }
    ,

    /*insert, three attributes can be present
     * id = insert id
     * before = before id
     * after = after  id
     *
     * the insert id is the id of the node to be inserted
     * the before is the id if set which the component has to be inserted before
     * the after is the id if set which the component has to be inserted after
     **/
    processInsert : function(request, context, node) {
        /*remapping global namespaces for speed and readability reasons*/
        var _Impl = this._Impl;
        var _Dom = this._Dom;
        var _Lang = this._Lang;

        var insertId = node.getAttribute('id');
        var beforeId = node.getAttribute('before');
        var afterId = node.getAttribute('after');

        var isInsert = insertId && this._Lang.trim(insertId) != "";
        var isBefore = beforeId && this._Lang.trim(beforeId) != "";
        var isAfter = afterId && this._Lang.trim(afterId) != "";

        if (!isInsert) {
            _Impl.sendError(request, context, _Impl.MALFORMEDXML, _Impl.MALFORMEDXML,this._Lang.getMessage("ERR_PPR_IDREQ"));
            return false;
        }
        if (!(isBefore || isAfter)) {
            _Impl.sendError(request, context, _Impl.MALFORMEDXML, _Impl.MALFORMEDXML,this._Lang.getMessage("ERR_PPR_INSERTBEFID"));
            return false;
        }
        //either before or after but not two at the same time
        var nodeHolder = null;
        var parentNode = null;


        var cDataBlock = this._Dom.concatCDATABlocks(node);

        var replacementFragment;
        if (isBefore) {
            beforeId = this._Lang.trim(beforeId);
            var beforeNode = this._Dom.byIdOrName(beforeId);
            if (!beforeNode) {
                _Impl.sendError(request, context, _Impl.MALFORMEDXML, _Impl.MALFORMEDXML,this._Lang.getMessage("ERR_PPR_INSERTBEFID_1", null,"_AjaxResponse.processInsert",beforeId));
                return false;
            }
            /**
             *we generate a temp holder
             *so that we can use innerHTML for
             *generating the content upfront
             *before inserting it"
             **/
            nodeHolder = document.createElement("div");
            parentNode = beforeNode.parentNode;
            parentNode.insertBefore(nodeHolder, beforeNode);
            replacementFragment = this.replaceHtmlItem(request, context,
                    nodeHolder, cDataBlock);
            if (replacementFragment) {
                this._pushOperationResult(replacementFragment);
            }

        } else {
            afterId = this._Lang.trim(afterId);
            var afterNode = this._Dom.byIdOrName(afterId);
            if (!afterNode) {
                _Impl.sendError(request, context, _Impl.MALFORMEDXML, _Impl.MALFORMEDXML, this._Lang.getMessage("ERR_PPR_INSERTBEFID_2", null,"_AjaxResponse.processInsert", afterId));
                return false;
            }

            nodeHolder = document.createElement("div");
            parentNode = afterNode.parentNode;

            //TODO nextsibling not working in ieMobile 6.1 we have to change the method
            //of accessing it to something else
            parentNode.insertBefore(nodeHolder, afterNode.nextSibling);

            replacementFragment = this.replaceHtmlItem(request, context,
                    nodeHolder, cDataBlock);

            if (replacementFragment) {
                this._pushOperationResult(replacementFragment);
            }

        }
        return true;
    }
    ,

    processDelete : function(request, context, node) {
        var _Impl = this._Impl;

        var deleteId = node.getAttribute('id');
        if (!deleteId) {
            _Impl.sendError(request, context, _Impl.MALFORMEDXML,
                    _Impl.MALFORMEDXML,this._Lang.getMessage("ERR_PPR_DELID", null,"_AjaxResponse.processDelete"));
            return false;
        }

        var item = this._Dom.byIdOrName(deleteId);
        if (!item) {
            throw Error(this._Lang.getMessage("ERR_PPR_UNKNOWNCID", null,"_AjaxResponse.processDelete",deleteId));
        }

        var parentForm = this._Dom.getParent(item, "form");
        if (null != parentForm) {
            this._updateForms.push(parentForm);
        }
        this._Dom.deleteItem(item);

        return true;
    }
    ,

    processAttributes : function(request, context, node) {
        //we now route into our attributes function to bypass
        //IE quirks mode incompatibilities to the biggest possible extent
        //most browsers just have to do a setAttributes but IE
        //behaves as usual not like the official standard
        //myfaces._impl._util.this._Dom.setAttribute(domNode, attribute, value;

        var _Impl = this._Impl;

        //<attributes id="id of element"> <attribute name="attribute name" value="attribute value" />* </attributes>
        var elemId = node.getAttribute('id');
        if (!elemId) {
            _Impl.sendError(request, context, _Impl.MALFORMEDXML
                    , _Impl.MALFORMEDXML, "Error in attributes, id not in xml markup");
            return false;
        }
        var childNodes = node.childNodes;

        if (!childNodes) {
            return false;
        }
        for (var loop2 = 0; loop2 < childNodes.length; loop2++) {
            var attributesNode = childNodes[loop2];

            var attrName = attributesNode.getAttribute("name");
            var attrValue = attributesNode.getAttribute("value");

            if (!attrName) {
                continue;
            }

            attrName = this._Lang.trim(attrName);
            /*no value means reset*/
            //value can be of boolean value hence full check
            if ('undefined' == typeof attrValue || null == attrValue) {
                attrValue = "";
            }

            switch (elemId) {
                case this.P_VIEWROOT:
                    throw new Error(this._Lang.getMessage("ERR_NO_VIEWROOTATTR", null,"_AjaxResponse.processAttributes"));
                    break;

                case this.P_VIEWHEAD:
                    throw new Error(this._Lang.getMessage("ERR_NO_HEADATTR", null,"_AjaxResponse.processAttributes"));
                    break;

                case this.P_VIEWBODY:
                    var element = document.getElementsByTagName("body")[0];
                    this._Dom.setAttribute(element, attrName, attrValue);
                    break;

                default:
                    this._Dom.setAttribute(document.getElementById(elemId), attrName, attrValue);
                    break;
            }

        }
        return true;
    },
    _finalize: function() {
        delete this._onException;
        delete this._onWarning;
        delete this._updateElems;
        // List of forms to be updated if any inner block is updated
        delete this._updateForms;
        delete this.appliedViewState;
    }

});
