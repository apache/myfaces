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
 * Version: $Revision: 1.2 $ $Date: 2009/05/30 17:54:57 $
 *
 */

myfaces._impl.core._Runtime.extendClass("myfaces._impl.xhrCore._AjaxResponse", Object, {




    /*partial response types*/
    _RESPONSE_PARTIAL : "partial-response",
    _RESPONSETYPE_ERROR : "error",
    /*TODO: -=Leonardo Uribe=- Does this response type really exists? really from server comes a partial-response with redirect command*/
    /*This really exists, it is for cases where a button issues a command and the server wants to redirect after processing the ajax request*/
    _RESPONSETYPE_REDIRECT : "redirect",
    _RESPONSETYPE_CHANGES : "changes",

    /*partial commands*/
    _PCMD_CHANGES : "changes",
    _PCMD_UPDATE : "update",
    _PCMD_DELETE : "delete",
    _PCMD_INSERT : "insert",
    _PCMD_EVAL : "eval",
    _PCMD_ERROR : "error",
    _PCMD_ATTRIBUTES : "attributes",
    _PCMD_EXTENSION : "extension",
    _PCMD_REDIRECT : "redirect",

    /*other constants*/
    _PROP_VIEWSTATE: "javax.faces.ViewState",
    _PROP_VIEWROOT: "javax.faces.ViewRoot",
    _PROP_VIEWHEAD: "javax.faces.ViewHead",
    _PROP_VIEWBODY: "javax.faces.ViewBody",

    /**
     * Constructor
     * @param {String} alarmThreshold
     */
    constructor_: function(onException, onWarning) {

        this.changeTrace = [];
        this._onException = onException;
        this._onWarning = onWarning;

        this.appliedViewState = null;
    },
    /**
     * uses response to start Html element replacement
     *
     * @param {Object} request (xhrRequest) - xhr request object
     * @param {Ojbect} context (Map) - AJAX context
     *
     * A special handling has to be added to the update cycle
     * according to the JSDoc specs if the CDATA block contains html tags the outer rim must be stripped
     * if the CDATA block contains a head section the document head must be replaced
     * and if the CDATA block contains a body section the document body must be replaced!
     *
     */
    processResponse : function(request, context) {
        try {
            var _Lang = myfaces._impl._util._Lang;
            var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);

            // TODO:
            // Solution from
            // http://www.codingforums.com/archive/index.php/t-47018.html
            // to solve IE error 1072896658 when a Java server sends iso88591
            // istead of ISO-8859-1

            if ('undefined' == typeof(request) || null == request) {
                throw Exception("jsf.ajaxResponse: The response cannot be null or empty!");
            }

            if (!_Lang.exists(request, "responseXML")) {
                _Impl.sendError(request, context, myfaces._impl.core.Impl._ERROR_EMPTY_RESPONSE);
                return;
            }

            var xmlContent = request.responseXML;
            //ie6+ keeps the parsing response under xmlContent.parserError
            //while the rest of the world keeps it as element under the first node
            var parseError

            if ((_Lang.exists(xmlContent, "parseError.errorCode") && xmlContent.parseError.errorCode != 0) || _Lang.equalsIgnoreCase(xmlContent.firstChild.tagName, "parsererror")) {
                //TODO improve error name and message sending here

                _Impl.sendError(request, context, myfaces._impl.core.Impl._ERROR_MALFORMEDXML);
                return;
            }
            var partials = xmlContent.childNodes[0];
            if ('undefined' == typeof partials || partials == null) {
                _Impl.sendError(request, context, myfaces._impl.core.Impl._ERROR_MALFORMEDXML);
                return;
            } else {
                if (partials.tagName != this._RESPONSE_PARTIAL) {
                    // IE 8 sees XML Header as first sibling ...
                    partials = partials.nextSibling;
                    if ('undefined' == typeof partials || partials == null || partials.tagName != this._RESPONSE_PARTIAL) {
                        _Impl.sendError(request, context, myfaces._impl.core.Impl._ERROR_MALFORMEDXML);
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
                if (tagName == this._PCMD_ERROR) {
                    this.processError(request, context, childNode);
                    return;
                } else if (tagName == this._PCMD_REDIRECT) {
                    if (!this.processRedirect(request, context, childNode)) return;
                } else if (tagName == this._PCMD_CHANGES) {
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
        /*namespace remapping*/
        var _Dom = myfaces._impl._util._Dom;

        //note the spec here says clearly it is done, but mojarra not and there is a corner case
        //regarding cross form submits, hence we should check all processed items for embedded forms
        for (var cnt = 0; cnt < this.changeTrace.length; cnt ++) {
            var replacementElem = this.changeTrace[cnt];
            var replacedForms = myfaces._impl._util._Dom.findByTagName(replacementElem, "form", false);
            for (var formCnt = 0; formCnt < replacedForms.length; formCnt++) {
                //we first have to fetch the real form element because the fragment
                //might be detached in some browser implementations
                var appliedReplacedFrom = document.getElementById(replacedForms[formCnt].id);
                var viewStateField = myfaces._impl._util._Dom.findFormElement(appliedReplacedFrom, this._PROP_VIEWSTATE);
                if (null == viewStateField) {
                    var element = document.createElement("input");
                    _Dom.setAttribute(element, "type", "hidden");
                    _Dom.setAttribute(element, "name", this._PROP_VIEWSTATE);
                    appliedReplacedFrom.appendChild(element);

                    _Dom.setAttribute(element, "value", this.appliedViewState);
                }
            }
        }
    },

    processError : function(request, context, node) {
        /**
         * <error>
         *      <error-name>String</error-name>
         *      <error-message><![CDATA[message]]></error-message>
         * <error>
         */
        var errorName = node.firstChild.textContent;
        var errorMessage = node.childNodes[1].firstChild.data;

        if ('undefined' == typeof errorName || null == errorName) {
            errorName = "";
        }
        if ('undefined' == typeof errorMessage || null == errorMessage) {
            errorMessage = "";
        }
        var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);

        _Impl.sendError(request, context, myfaces._impl.core.Impl._ERROR_SERVER_ERROR, errorName, errorMessage);
    },

    processRedirect : function(request, context, node) {
        /**
         * <redirect url="url to redirect" />
         */
        var redirectUrl = node.getAttribute("url");
        if ('undefined' == typeof redirectUrl || null == redirectUrl) {
            var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);

            _Impl.sendError(request, context, myfaces._impl.core.Impl._ERROR_MALFORMEDXML, myfaces._impl.core.Impl._ERROR_MALFORMEDXML, "Redirect without url");
            return false;
        }
        redirectUrl = myfaces._impl._util._Lang.trim(redirectUrl);
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

        var _Lang = myfaces._impl._util._Lang;

        var changes = node.childNodes;

        //note we need to trace the changes which could affect our insert update or delete
        //se that we can realign our ViewStates afterwards
        //the realignment must happen post change processing

        for (var i = 0; i < changes.length; i++) {
            if (changes[i].tagName == "update") {
                if (!this.processUpdate(request, context, changes[i])) return false;
            } else if (changes[i].tagName == this._PCMD_EVAL) {
                //eval is always in CDATA blocks
                _Lang.globalEval(changes[i].firstChild.data);
            } else if (changes[i].tagName == this._PCMD_INSERT) {
                if (!this.processInsert(request, context, changes[i])) return false;
            } else if (changes[i].tagName == this._PCMD_DELETE) {
                if (!this.processDelete(request, context, changes[i])) return false;
            } else if (changes[i].tagName == this._PCMD_ATTRIBUTES) {
                if (!this.processAttributes(request, context, changes[i])) return false;
            } else if (changes[i].tagName == this._PCMD_EXTENSION) {
                //DO nothing we do not have any implementation specifics for now!
                //but if you need some implementation specific stuff
                //you have to insert it here
                //  this._responseHandler.doExtension(childNode);
            } else {
                var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);

                _Impl.sendError(request, context, myfaces._impl.core.Impl._ERROR_MALFORMEDXML);
                return false;
            }
        }
        //second step we step over all updates which have identifiers to check whether there are lost
        // viewStates

        return true;
    },

    processUpdate : function(request, context, node) {
        /*local namespace remapping*/
        var _Dom = myfaces._impl._util._Dom;
        var _Lang = myfaces._impl._util._Lang;

        if (node.getAttribute('id') == this._PROP_VIEWSTATE) {
            //update the submitting forms viewstate to the new value
            // The source form has to be pulled out of the CURRENT document first because the context object
            // may refer to an invalid document if an update of the entire body has occurred before this point.
            var viewStateValue = node.firstChild.nodeValue;
            var sourceForm = myfaces._impl._util._Dom.fuzzyFormDetection(context.source);

            //the source form could be determined absolutely by either the form, the identifier of the node, or the name
            //if only one element is given
            if (null != sourceForm) {
                /*we check for an element and include a namesearch, but only within the bounds of the committing form*/
                var element = null;
                try {
                    element = _Dom.getElementFromForm(this._PROP_VIEWSTATE, sourceForm, true, true);
                } catch (e) {
                    //in case of an error here we try an early recovery but throw an error to our error handler
                    this._onException(request, context, "_AjaxResponse", "processUpdate('javax.faces.ViewState')", e);
                }

                if (null == element) {//no element found we have to append a hidden field
                    element = document.createElement("input");
                    _Dom.setAttribute(element, "type", "hidden");
                    _Dom.setAttribute(element, "name", this._PROP_VIEWSTATE);
                    sourceForm.appendChild(element);
                }
                //viewstate cannot have split cdata blocks so we can skip the costlier operation

                _Dom.setAttribute(element, "value", viewStateValue);
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
            var cDataBlock = myfaces._impl._util._Dom.concatCDATABlocks(node);

            switch (node.getAttribute('id')) {
                case this._PROP_VIEWROOT:
                    this._replaceBody(request, context, cDataBlock);

                    break;
                case this._PROP_VIEWHEAD:
                    //we cannot replace the head, almost no browser allows this, some of them throw errors
                    //others simply ignore it or replace it and destroy the dom that way!
                    throw new Error("Head cannot be replaced, due to browser deficiencies!");

                    break;
                case this._PROP_VIEWBODY:
                    //we assume the cdata block is our body including the tag
                    this._replaceBody(request, context, cDataBlock);
                    break;

                default:
                    var resultNode = this._replaceElement(request, context, node.getAttribute('id'), cDataBlock);
                    if ('undefined' != typeof resultNode && null != resultNode) {
                        this.changeTrace.push(resultNode);
                    }
                    break;
            }
        }
        return true;
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

        var _Dom = myfaces._impl._util._Dom;

        var parser = new (myfaces._impl.core._Runtime.getGlobalConfig("updateParser", myfaces._impl._util._HtmlStripper))();

        var oldBody = document.getElementsByTagName("body")[0];
        var newBody = document.createElement("body");
        var placeHolder = document.createElement("div");
        placeHolder.id = "myfaces_bodyplaceholder";
        var bodyParent = oldBody.parentNode;

        newBody.appendChild(placeHolder);

        //the contextualFragment trick does not work on the body tag instead we have to generate a manual body
        //element and then add a child which then is the replacement holder for our fragment!

        //TODO we probably should try to offload this to the browser first via the integrated xml parsing
        //and if it fails revert to our internal parser
        var bodyData = parser.parse(newData, "body");
        bodyParent.replaceChild(newBody, oldBody);
        this._replaceElement(request, context, placeHolder, bodyData);

        for (var key in parser.tagAttributes) {
            var value = parser.tagAttributes[key];
            _Dom.setAttribute(newBody, key, value);
        }
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
                oldElement, newData, this.m_htmlFormElement);
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
            var _Lang = myfaces._impl._util._Lang;
            // (itemIdToReplace instanceof Node) is NOT compatible with IE8
            var item = (!_Lang.isString(itemIdToReplace)) ? itemIdToReplace :
                    myfaces._impl._util._Dom.getElementFromForm(itemIdToReplace, form);
            if ('undefined' == typeof item || null == item) {
                throw Error("myfaces._impl.xhrCore._AjaxResponse.replaceHtmlItem: item with identifier "+itemIdToReplace.toString()+" could not be found");
            }
            return myfaces._impl._util._Dom.outerHTML(item, markup);

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
        var _Lang = myfaces._impl._util._Lang;
        var _Dom = myfaces._impl._util._Dom;
        var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);

        var insertId = node.getAttribute('id');
        var beforeId = node.getAttribute('before');
        var afterId = node.getAttribute('after');

        var insertSet = 'undefined' != typeof insertId && null != insertId && _Lang.trim(insertId) != "";
        var beforeSet = 'undefined' != typeof beforeId && null != beforeId && _Lang.trim(beforeId) != "";
        var afterSet = 'undefined' != typeof afterId && null != afterId && _Lang.trim(afterId) != "";

        if (!insertSet) {
            _Impl.sendError(request, context, _Impl._ERROR_MALFORMEDXML, _Impl._ERROR_MALFORMEDXML, "Error in PPR Insert, id must be present");
            return false;
        }
        if (!(beforeSet || afterSet)) {
            _Impl.sendError(request, context, _Impl._ERROR_MALFORMEDXML, _Impl._ERROR_MALFORMEDXML, "Error in PPR Insert, before id or after id must be present");
            return false;
        }
        //either before or after but not two at the same time
        var nodeHolder = null;
        var parentNode = null;

        var cDataBlock = _Dom.concatCDATABlocks(node);
        var replacementFragment;
        if (beforeSet) {
            beforeId = _Lang.trim(beforeId);
            var beforeNode = document.getElementById(beforeId);
            if ('undefined' == typeof beforeNode || null == beforeNode) {
                _Impl.sendError(request, context, _Impl._ERROR_MALFORMEDXML, _Impl._ERROR_MALFORMEDXML, "Error in PPR Insert, before  node of id " + beforeId + " does not exist in document");
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

            if ('undefined' != typeof replacementFragment && null != replacementFragment) {
                this.changeTrace.push(replacementFragment);
            }

        } else {
            afterId = _Lang.trim(afterId);
            var afterNode = document.getElementById(afterId);
            if ('undefined' == typeof afterNode || null == afterNode) {
                _Impl.sendError(request, context, _Impl._ERROR_MALFORMEDXML, _Impl._ERROR_MALFORMEDXML, "Error in PPR Insert, after  node of id " + after + " does not exist in document");
                return false;
            }

            nodeHolder = document.createElement("div");
            parentNode = afterNode.parentNode;
            parentNode.insertBefore(nodeHolder, afterNode.nextSibling);

            replacementFragment = this.replaceHtmlItem(request, context,
                    nodeHolder, cDataBlock, null);

            if ('undefined' != typeof replacementFragment && null != replacementFragment) {
                this.changeTrace.push(replacementFragment);
            }

        }
        return true;
    }
    ,

    processDelete : function(request, context, node) {
        var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
        var _Dom = myfaces._impl._util._Dom;

        var deleteId = node.getAttribute('id');
        if ('undefined' == typeof deleteId || null == deleteId) {
            _Impl.sendError(request, context, _Impl._ERROR_MALFORMEDXML,
                    _Impl._ERROR_MALFORMEDXML, "Error in delete, id not in xml markup");
            return false;
        }

        _Dom.deleteItem(deleteId);

        return true;
    }
    ,

    processAttributes : function(request, context, node) {
        //we now route into our attributes function to bypass
        //IE quirks mode incompatibilities to the biggest possible extent
        //most browsers just have to do a setAttributes but IE
        //behaves as usual not like the official standard
        //myfaces._impl._util._Dom.setAttribute(domNode, attribute, value;

        var _Dom = myfaces._impl._util._Dom;
        var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);

        //<attributes id="id of element"> <attribute name="attribute name" value="attribute value" />* </attributes>
        var elementId = node.getAttribute('id');
        if ('undefined' == typeof elementId || null == elementId) {
            _Impl.sendError(request, context, _Impl._ERROR_MALFORMEDXML
                    , _Impl._ERROR_MALFORMEDXML, "Error in attributes, id not in xml markup");
            return false;
        }
        var childNodes = node.childNodes;

        if ('undefined' == typeof childNodes || null == childNodes) {
            return false;
        }
        for (var loop2 = 0; loop2 < childNodes.length; loop2++) {
            var attributesNode = childNodes[loop2];

            var attributeName = attributesNode.getAttribute("name");
            var attributeValue = attributesNode.getAttribute("value");

            if ('undefined' == typeof attributeName || null == attributeName) {
                continue;
            }

            attributeName = myfaces._impl._util._Lang.trim(attributeName);
            /*no value means reset*/
            if ('undefined' == typeof attributeValue || null == attributeValue) {
                attributeValue = "";
            }

            switch (elementId) {
                case this._PROP_VIEWROOT:
                    throw new Error("Changing of viewRoot attributes is not supported");
                    break;

                case this._PROP_VIEWHEAD:
                    throw new Error("Changing of head attributes is not supported");
                    break;

                case this._PROP_VIEWBODY:
                    var element = document.getElementsByTagName("body")[0];
                    _Dom.setAttribute(element, attributeName, attributeValue);
                    break;

                default:
                    _Dom.setAttribute(document.getElementById(elementId), attributeName, attributeValue);
                    break;
            }

        }
        return true;
    }

})
        ;
