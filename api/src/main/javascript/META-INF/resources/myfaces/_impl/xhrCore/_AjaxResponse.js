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
myfaces._impl.core._Runtime.extendClass("myfaces._impl.xhrCore._AjaxResponse", Object, {

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
            var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);

            // TODO:
            // Solution from
            // http://www.codingforums.com/archive/index.php/t-47018.html
            // to solve IE error 1072896658 when a Java server sends iso88591
            // istead of ISO-8859-1

            if (!request) {
                throw Exception("jsf.ajaxResponse: The response cannot be null or empty!");
            }

            if (!this._Lang.exists(request, "responseXML")) {
                _Impl.sendError(request, context, myfaces._impl.core.Impl.EMPTY_RESPONSE);
                return;
            }
            //check for a parseError under certain browsers


            var xmlContent = request.responseXML;
            //ie6+ keeps the parsing response under xmlContent.parserError
            //while the rest of the world keeps it as element under the first node




            if ( this._Lang.isXMLParseError(xmlContent)) {
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
            this.fixViewStates();
        } catch (e) {
            this._onException(request, context, "myfaces._impl.xhrCore._AjaxResponse", "processResponse", e);
        }
    },

    fixViewStates : function() {

        if (null == this.appliedViewState) {
            return;
        }

        // Now update the forms that were not replaced but forced to be updated, because contains child ajax tags
        // we should only update forms with view state hidden field. If by some reason, the form was set to be
        // updated but the form was replaced, it does not have hidden view state, so later in changeTrace processing the
        // view state is updated.

        //set the viewstates of all outer forms parents of our updated elements
        this._Lang.arrForEach(this._updateForms, this._setVSTOuterForm, 0, this);
        
        //set the viewstate of all forms within our updated elements
        this._Lang.arrForEach(this._updateElems, this._setVSTInnerForms, 0, this);
    },

    _setVSTOuterForm: function(elem) {
        var viewStateField = this._Dom.findFormElement(elem, this.P_VIEWSTATE);
        if (null != viewStateField) {
            this._Dom.setAttribute(viewStateField,"value", this.appliedViewState);
        }
    },

    _setVSTInnerForms: function(elem) {
        var replacedForms = this._Dom.findByTagName(elem, "form", false);
        this._Lang.arrForEach(replacedForms, this._setVSTInnerForm, 0, this);
    },

    _setVSTInnerForm: function(elem) {
        //we first have to fetch the real form element because the fragment
        //might be detached in some browser implementations
        var appliedReplacedFrom = document.getElementById(elem.id);
        var viewStateField = this._Dom.findFormElement(appliedReplacedFrom, this.P_VIEWSTATE);

        //we have to add the viewstate field in case it is not rendered
        //otherwise those forms cannot issue another submit
        //if the viewstate field is present we can rely on the viewstate being
        //at the current state no further updates have to be done
        if (null == viewStateField) {
            var element = document.createElement("input");
            this._Dom.setAttribute(element, "type", "hidden");
            this._Dom.setAttribute(element, "name", this.P_VIEWSTATE);
            appliedReplacedFrom.appendChild(element);

            this._Dom.setAttribute(element, "value", this.appliedViewState);
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

        var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);

        _Impl.sendError(request, context, myfaces._impl.core.Impl.SERVER_ERROR, errorName, errorMessage);
    },

    processRedirect : function(request, context, node) {
        /**
         * <redirect url="url to redirect" />
         */
        var redirectUrl = node.getAttribute("url");
        if (!redirectUrl) {
            var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);

            _Impl.sendError(request, context, myfaces._impl.core.Impl.MALFORMEDXML, myfaces._impl.core.Impl.MALFORMEDXML, "Redirect without url");
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
                    if (!this.processUpdate(request, context, changes[i])) return false;
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
                    var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
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
            var sourceForm = this._Dom.fuzzyFormDetection(context.source);

            // TODO: After some tests, it was found sourceForm could point to a detached instance, but
            // there is no harm if we update it. Below there is a code that check if the node has been
            // detached or not to prevent manipulation. I'm not sure if that code works in all browser
            // so to prevent introduce bugs, I let it commented with the hope somebody check if let this
            // code is safe or if it is worth. If it is not detached, without this code we could update
            // the same input hidden view state twice.
            //if (null != sourceForm) {
            // Check if sourceForm is inside the document, or in other words, it was not detached.
            // We have to walk to the parent node
            //var this._Lang = myfaces._impl._util.this._Lang;
            //var searchClosure = function(parentItem) {
            //    return parentItem && (parentItem == document);
            //};
            //var sourceFormAncestor = this._Dom.getFilteredParent(sourceForm, searchClosure);
            //Is not on the document?
            //if (null == sourceFormAncestor)
            //{
            // Let fixViewStates do the job, because after the blocks are processed, we register
            // the target forms to be updated if any.
            //sourceForm = null;
            //}
            //}

            //the source form could be determined absolutely by either the form, the identifier of the node, or the name
            //if only one element is given
            if (null != sourceForm) {
                /*we check for an element and include a namesearch, but only within the bounds of the committing form*/
                var element = null;
                try {
                    element = this._Dom.getElementFromForm(this.P_VIEWSTATE, sourceForm, true, true);
                } catch (e) {
                    //in case of an error here we try an early recovery but throw an error to our error handler
                    this._onException(request, context, "_AjaxResponse", "processUpdate('javax.faces.ViewState')", e);
                }

                if (null == element) {//no element found we have to append a hidden field
                    element = document.createElement("input");
                    this._Dom.setAttribute(element, "type", "hidden");
                    this._Dom.setAttribute(element, "name", this.P_VIEWSTATE);
                    sourceForm.appendChild(element);
                }
                //viewState cannot have split cdata blocks so we can skip the costlier operation

                this._Dom.setAttribute(element, "value", viewStateValue);
            }
            //note due to a missing spec we have to apply the viewstate as well
            //to any form which might be rerendered within the render cycle
            //hence we store the viewstate element for later refererence
            //to fix up all elements effected by the cycle
            this.appliedViewState = viewStateValue;
        }
        else
        {
            // response may contain several blocks
            var cDataBlock = this._Dom.concatCDATABlocks(node);

            switch (node.getAttribute('id')) {
                case this.P_VIEWROOT:
                    cDataBlock = cDataBlock.substring(cDataBlock.indexOf("<html"));
                    this._replaceHead(request, context, cDataBlock);    
                    var resultNode = this._replaceBody(request, context, cDataBlock);
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
                    var resultNode = this._replaceElement(request, context, node.getAttribute('id'), cDataBlock);
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
            if (null != parentForm)
            {
                this._updateForms.push(parentForm);
            }
            else
            {
                this._updateElems.push(currNode);
            }
        });
        if (resultNode.length) {
            for (var cnt = 0; cnt < resultNode.length; cnt++) {
                pushSubnode(resultNode[cnt]);
            }
        } else {
            pushSubnode(resultNode);
        }

    },

    _replaceHead: function(request, context, newData) {
        var doc = this._Lang.parseXML(newData);
        var newHead = null;
        if(this._Lang.isXMLParseError(doc)) {
            //the standard xml parser failed we retry with the stripper
            var parser = new (myfaces._impl.core._Runtime.getGlobalConfig("updateParser", myfaces._impl._util._HtmlStripper))();
            var headData = parser.parse(newData, "head");
            newHead =  this._Lang.parseXML("<root>"+headData+"</root>");
            if(this._Lang.isXMLParseError(newHead)) {
                //we give up no further fallbacks
                this._Impl.sendError(request, context, _Impl.MALFORMEDXML, _Impl.MALFORMEDXML, "Error in PPR Insert, before id or after id must be present");
                return;
            }
        } else {
            //parser worked we go on
            newHead = doc.getElementsByTagName("head")[0];
        }

        //since we are xml enabled here we probably can walk over it via xml <head> ... </head> normally is valid xml
        //the ie tricks with form etc... do not work out, so we have to rely on internal xml parsing
        //prestripping the body should reduce the failure rate of this method
        ///var xmlData = this._Lang.parseXML("<root>"+headData+"</root>");
        this._Dom.runScripts(newHead, true);
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
     */
    _replaceBody : function(request, context, newData) {
        var parser = new (myfaces._impl.core._Runtime.getGlobalConfig("updateParser", myfaces._impl._util._HtmlStripper))();

        var oldBody = document.getElementsByTagName("body")[0];
        var newBody = document.createElement("body");
        var placeHolder = document.createElement("div");
        placeHolder.id = "myfaces_bodyplaceholder";
        var bodyParent = oldBody.parentNode;

        newBody.appendChild(placeHolder);

        //the contextualFragment trick does not work on the body tag instead we have to generate a manual body
        //element and then add a child which then is the replacement holder for our fragment!

        //Note, we also could offload this to the browser,
        //but for now our parser seems to be faster than the browser offloading method
        //and also does not interfere on security level
        //the browser offloading methods would be first to use the xml parsing
        //and if that fails revert to a hidden iframe
        //var bodyData = parser.parse(newData, "body");
        var bodyData = null;
        var doc = this._Lang.parseXML(newData);
        if(this._Lang.isXMLParseError(doc)) {
             //the standard xml parser failed we retry with the stripper
             var parser = new (myfaces._impl.core._Runtime.getGlobalConfig("updateParser", myfaces._impl._util._HtmlStripper))();
             bodyData = parser.parse(newData, "body");
        } else {
             //parser worked we go on
            var newBodyData = doc.getElementsByTagName("body")[0];
            bodyData = this._Lang.serializeChilds(newBodyData);
            for (var cnt = 0; cnt < newBodyData.attributes.length; cnt++) {
                var value = newBodyData.attributes[cnt].value;
                if(value)
                    this._Dom.setAttribute(newBody, newBodyData.attributes[cnt].name, value);
            }
        }

        bodyParent.replaceChild(newBody, oldBody);
        var returnedElement = this._replaceElement(request, context, placeHolder, bodyData);

        return returnedElement;
    }
    ,

    /**
     * Helper method to avoid duplicate code
     * @param {Object} request our request object
     * @param {Object} context (Map) the response context
     * @param {Node} oldElement the element to be replaced
     * @param {String} newData the markup which replaces the old dom node!
     */
    _replaceElement : function(request, context, oldElement, newData) {
        return this.replaceHtmlItem(request, context,
                oldElement, newData);
    }
    ,


    /**
     * Replaces HTML elements through others and handle errors if the occur in the replacement part
     *
     * @param {Object} request (xhrRequest)
     * @param {Object} context (Map)
     * @param {Object} itemIdToReplace (String|Node) - ID of the element to replace
     * @param {String} markup - the new tag
     * @param {Node} form - form element that is parent of the element
     */
    replaceHtmlItem : function(request, context, itemIdToReplace, markup, form) {
        try {
            // (itemIdToReplace instanceof Node) is NOT compatible with IE8
            var item = (!this._Lang.isString(itemIdToReplace)) ? itemIdToReplace :
                    this._Dom.getElementFromForm(itemIdToReplace, form);
            if (!item) {
                throw Error("myfaces._impl.xhrCore._AjaxResponse.replaceHtmlItem: item with identifier " + itemIdToReplace.toString() + " could not be found");
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
        var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);

        var insertId = node.getAttribute('id');
        var beforeId = node.getAttribute('before');
        var afterId = node.getAttribute('after');

        var isInsert = insertId && this._Lang.trim(insertId) != "";
        var isBefore = beforeId && this._Lang.trim(beforeId) != "";
        var isAfter = afterId && this._Lang.trim(afterId) != "";

        if (!isInsert) {
            _Impl.sendError(request, context, _Impl.MALFORMEDXML, _Impl.MALFORMEDXML, "Error in PPR Insert, id must be present");
            return false;
        }
        if (!(isBefore || isAfter)) {
            _Impl.sendError(request, context, _Impl.MALFORMEDXML, _Impl.MALFORMEDXML, "Error in PPR Insert, before id or after id must be present");
            return false;
        }
        //either before or after but not two at the same time
        var nodeHolder = null;
        var parentNode = null;

        var cDataBlock = this._Dom.concatCDATABlocks(node);
        var replacementFragment;
        if (isBefore) {
            beforeId = this._Lang.trim(beforeId);
            var beforeNode = document.getElementById(beforeId);
            if (!beforeNode) {
                _Impl.sendError(request, context, _Impl.MALFORMEDXML, _Impl.MALFORMEDXML, "Error in PPR Insert, before  node of id " + beforeId + " does not exist in document");
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
                    nodeHolder, cDataBlock, null);

            if (replacementFragment) {
                this._pushOperationResult(replacementFragment);
            }

        } else {
            afterId = this._Lang.trim(afterId);
            var afterNode = document.getElementById(afterId);
            if (!afterNode) {
                _Impl.sendError(request, context, _Impl.MALFORMEDXML, _Impl.MALFORMEDXML, "Error in PPR Insert, after  node of id " + afterId + " does not exist in document");
                return false;
            }

            nodeHolder = document.createElement("div");
            parentNode = afterNode.parentNode;
            parentNode.insertBefore(nodeHolder, afterNode.nextSibling);

            replacementFragment = this.replaceHtmlItem(request, context,
                    nodeHolder, cDataBlock, null);

            if (replacementFragment) {
                this._pushOperationResult(replacementFragment);
            }

        }
        return true;
    }
    ,

    processDelete : function(request, context, node) {
        var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);

        var deleteId = node.getAttribute('id');
        if (!deleteId) {
            _Impl.sendError(request, context, _Impl.MALFORMEDXML,
                    _Impl.MALFORMEDXML, "Error in delete, id not in xml markup");
            return false;
        }

        var item = this._Dom.byId(deleteId);
        if (!item) {
            throw Error("_AjaxResponse.processDelete  Unknown Html-Component-ID: " + deleteId);
        }

        var parentForm = this._Dom.getParent(item, "form");
        if (null != parentForm)
        {
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

        var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);

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
                    throw new Error("Changing of viewRoot attributes is not supported");
                    break;

                case this.P_VIEWHEAD:
                    throw new Error("Changing of head attributes is not supported");
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
    }

});
