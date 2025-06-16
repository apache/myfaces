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
 * @name _AjaxResponse
 * @memberOf myfaces._impl.xhrCore
 * @extends myfaces._impl.core.Object
 * @description
 * This singleton is responsible for handling the standardized xml ajax response
 * Note: since the semantic processing can be handled about 90% in a functional
 * style we make this class stateless. Every state information is stored
 * temporarily in the context.
 *
 * The singleton approach also improves performance
 * due to less object gc compared to the old instance approach.
 *
 */
_MF_SINGLTN(_PFX_XHR + "_AjaxResponse", _MF_OBJECT, /** @lends myfaces._impl.xhrCore._AjaxResponse.prototype */ {

    /*partial response types*/
    RESP_PARTIAL: "partial-response",
    RESP_TYPE_ERROR: "error",
    RESP_TYPE_REDIRECT: "redirect",
    RESP_TYPE_CHANGES: "changes",

    /*partial commands*/
    CMD_CHANGES: "changes",
    CMD_UPDATE: "update",
    CMD_DELETE: "delete",
    CMD_INSERT: "insert",
    CMD_EVAL: "eval",
    CMD_ERROR: "error",
    CMD_ATTRIBUTES: "attributes",
    CMD_EXTENSION: "extension",
    CMD_REDIRECT: "redirect",

    /*other constants*/
    P_VIEWSTATE: "javax.faces.ViewState",
    P_CLIENTWINDOW: "javax.faces.ClientWindow",
    P_VIEWROOT: "javax.faces.ViewRoot",
    P_VIEWHEAD: "javax.faces.ViewHead",
    P_VIEWBODY: "javax.faces.ViewBody",
    P_RESOURCE: "javax.faces.Resource",

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
    processResponse: function (request, context) {
        //mfinternal handling, note, the mfinternal is only optional
        //according to the spec
        context._mfInternal = context._mfInternal || {};
        var mfInternal = context._mfInternal;

        //the temporary data is hosted here
        mfInternal._updateElems = [];
        mfInternal._updateForms = [];
        mfInternal.appliedViewState = null;
        mfInternal.appliedClientWindow = null;
        mfInternal.namingModeId = null;


        try {
            var _Impl = this.attr("impl"), _Lang = this._Lang;
            // TODO:
            // Solution from
            // http://www.codingforums.com/archive/index.php/t-47018.html
            // to solve IE error 1072896658 when a Java server sends iso88591
            // istead of ISO-8859-1

            if (!request || !_Lang.exists(request, "responseXML")) {
                throw this.makeException(new Error(), _Impl.EMPTY_RESPONSE, _Impl.EMPTY_RESPONSE, this._nameSpace, "processResponse", "");
            }
            //check for a parseError under certain browsers

            var xmlContent = request.responseXML;
            //ie6+ keeps the parsing response under xmlContent.parserError
            //while the rest of the world keeps it as element under the first node
            var xmlErr = _Lang.fetchXMLErrorMessage(request.responseText || request.response, xmlContent)
            if (xmlErr) {
                throw this._raiseError(new Error(), xmlErr.errorMessage + "\n" + xmlErr.sourceText + "\n" + xmlErr.visualError + "\n", "processResponse");
            }
            var partials = xmlContent.childNodes[0];
            if ('undefined' == typeof partials || partials == null) {
                throw this._raiseError(new Error(), "No child nodes for response", "processResponse");

            } else {
                if (partials.tagName != this.RESP_PARTIAL) {
                    // IE 8 sees XML Header as first sibling ...
                    partials = partials.nextSibling;
                    if (!partials || partials.tagName != this.RESP_PARTIAL) {
                        throw this._raiseError(new Error(), "Partial response not set", "processResponse");
                    }
                }
            }


            /**
             * jsf 2.3 naming mode partial response,
             * we either viewstate all forms (non id mode)
             * or the forms under the viewroot defined by id
             *
             * @type {string} ... the naming mode id is set or an empty string
             * definitely not a null value to avoid type confusions later on
             */
            mfInternal.namingModeId = (partials.id || "");


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
                } else if (tagName == this.CMD_REDIRECT) {
                    this.processRedirect(request, context, childNode);
                } else if (tagName == this.CMD_CHANGES) {
                    this.processChanges(request, context, childNode);
                }
            }

            //fixup missing viewStates due to spec deficiencies
            if (mfInternal.appliedViewState) {
                this.fixViewStates(context);
            }
            if (mfInternal.appliedClientWindow) {
                this.fixClientWindows(context);
            }

            //spec jsdoc, the success event must be sent from response
            _Impl.sendEvent(request, context, _Impl["SUCCESS"]);
        } catch (e) {

            if (window.console && window.console.error) {
                //any error should be logged
                console.error(e);
            }
            throw e;
        } finally {
            delete mfInternal._updateElems;
            delete mfInternal._updateForms;
            delete mfInternal.appliedViewState;
            delete mfInternal.appliedClientWindow;
            delete mfInternal.namingModeId;
        }
    },

    /**
     * fixes the viewstates in the current page
     *
     * @param context
     */
    fixViewStates: function (context) {
        var _Lang = this._Lang;
        var mfInternal = context._mfInternal;

        if (null == mfInternal.appliedViewState) {
            return;
        }

        /**
         * JSF 2.3 we set all the viewstates under a given declared viewRoot or all forms
         * if none is given
         */
        this._updateJSFClientArtifacts(context,  mfInternal.appliedViewState, this.P_VIEWSTATE);
    },


    fixClientWindows: function (context, theForm) {
        var _Lang = this._Lang;
        var mfInternal = context._mfInternal;

        if (null == mfInternal.appliedClientWindow) {
            return;
        }

        /**
         * JSF 2.3 we set all the viewstates under a given declared viewRoot or all forms
         * if none is given
         */

        this._updateJSFClientArtifacts(context, mfInternal.appliedClientWindow, this.P_CLIENTWINDOW);

    },


    /**
     * sets the a jsf artifact element with a given identifier to a new value or adds this element
     *
     * @param theForm {Node} the form to which the element has to be set to
     * @param context the current request context
     */
    _applyJSFArtifactValueToForm: function (context, theForm, value, identifier) {

        if (!theForm) return;
        var _Lang = this._Lang;
        var _Dom = this._Dom;
        var prefix = this._getPrefix(context);

        //in IE7 looking up form elements with complex names (such as 'javax.faces.ViewState') fails in certain cases
        //iterate through the form elements to find the element, instead
        var fieldsFound = [];

        var elements = theForm.elements;
        for (var i = 0, l = elements.length; i < l; i++) {
            var e = elements[i];
            //https://issues.apache.org/jira/browse/MYFACES-4230
            //ie11 has a deviation from the standard behavior, we have to remap the null/undefined name
            //to an empty string
            var eName = e.name || "";

            if (eName.indexOf(identifier) != -1) {
                fieldsFound.push(e);
            }
        }

        if (fieldsFound.length) {
            _Lang.arrForEach(fieldsFound, function (fieldFound) {
                _Dom.setAttribute(fieldFound, "value", value);
            });
        } else {
            var element = this._Dom.getDummyPlaceHolder();

            //per JSF 2.3 spec the identifier of the element must be unique in the dom tree
            //otherwise we will break the html spec here
            element.innerHTML = ["<input type='hidden'", "id='", this._fetchUniqueId(prefix, identifier), "' name='", this._getNamingContainerPrefix(context) + identifier, "' value='", value, "' />"].join("");
            //now we go to proper dom handling after having to deal with another ie screw-up
            try {
                theForm.appendChild(element.childNodes[0]);
            } finally {
                element.innerHTML = "";
            }
        }
    },

    _fetchUniqueId: function(prefix, identifier) {
        var cnt = 1;
        var retVal = prefix + identifier + jsf.separatorchar + cnt;
        while(this._Dom.byId(retVal) != null) {
            cnt++;
            retVal = prefix + identifier + jsf.separatorchar + cnt;
        }
        return retVal;
    },

    /**
     * updates/inserts the jsf client artifacts under a given viewroot element
     *
     * @param context the client context holding all request context data and some internal data
     * @param elem the root to start with, must be a dom node not an identifier
     * @param value the new value
     * @param identifier the identifier for the client artifact aka javax.faces.ViewState, ClientWindowId etc...
     *
     * @private
     */
    _updateJSFClientArtifacts: function (context, value, identifier) {

        //elem not found for whatever reason
        //https://issues.apache.org/jira/browse/MYFACES-3544

        var prefix = this._getPrefix(context);

        //do we still need the issuing form update? I guess it is needed.
        //jsf spec 2.3 and earlier all issuing forms must update
        var sourceForm = (context._mfInternal._mfSourceFormId) ? this._Dom.byId(context._mfInternal._mfSourceFormId) : null;
        if (sourceForm) {
            sourceForm = this._Dom.byId(sourceForm);
            if (sourceForm) {
                //some cases where the source form cannot be updated
                //because it is gone
                this._applyJSFArtifactValueToForm(context, sourceForm, value, identifier);
            }
        }



        var viewRoot = this._getViewRoot(context);
        var forms = this._Dom.findByTagNames(viewRoot, {"form": 1}) || [];

        //since the spec thanks to the over intrusive portlet api still is broken
        //we need our old fallback hack for proper handling without having
        //to deal with multiple render targets.


        if(this._RT.getLocalOrGlobalConfig(context, "no_portlet_env", false)) {

            //We update all elements under viewroot
            //this clearly violates the jsf 2.3 jsdocs
            //however I think that the jsdocs were sloppily updated
            //because just updating the render targets under one viewroot and the issuing form
            //again would leave broken viewstates, in the end the portlet spec is at fault here
            //which came late to the game and expected all frameworks to adapt to their needs.
            //instead of properly adapting to the frameworks
            //now the viewroot mechanism per se would work, but people are dropping
            //jsf 2.3 into old portlet containers which then expose the legacy behavior
            //of having just one view root.
            this._Lang.arrForEach(forms, this._Lang.hitch(this, function (elem) {
                //update all forms which start with prefix (all render and execute targets
                this._applyJSFArtifactValueToForm(context, elem, value, identifier);
            }));
        } else {


            //check for a portlet condition a present viewroot

            var viewRootId = viewRoot.id || "";

            for(var cnt = 0; cnt < context._mfInternal._updateForms.length; cnt++) {
                var updateForm = context._mfInternal._updateForms[cnt];

                //follow the spec 2.3 path 1:1 we update the forms hosting the render targets which start
                //with the viewroot
                //if there is a viewroot present, however we seem to have a bug in myfaces
                //even if we have a naming container response we
                //cannot rely on the naming container being prefixed

                //This atm is not bad, because we safely can assume
                //that if no viewroot can be found we are under
                //one single viewroot and can omit the prefix check
                //(aka fallback into the old behavior)


                if(updateForm.indexOf(viewRootId) != 0) {
                    continue;
                } else { //either an empty viewroot, or a namespace match
                    this._applyJSFArtifactValueToForm(context, this._Dom.byId(updateForm), value, identifier);
                }
            }

        }

    },

    _getViewRoot: function (context) {
        var prefix = this._getPrefix(context);
        if (prefix == "") {
            return document.getElementsByTagName("body")[0];
        }
        prefix = prefix.substr(0, prefix.length - 1);
        var viewRoot = document.getElementById(prefix);
        if (viewRoot) {
            return viewRoot;
        }
        return document.getElementsByTagName("body")[0];
    },


    _getPrefix: function (context) {
        var mfInternal = context._mfInternal;
        var prefix = mfInternal.namingModeId;
        if (prefix != "") {
            prefix = prefix + jsf.separatorchar;
        }
        return prefix;
    },

    _getNamingContainerPrefix: function(context) {
        var mfInternal = context._mfInternal;
        var prefix = myfaces._impl.xhrCore._AjaxUtils._$ncRemap(context._mfInternal, "");
        return prefix;
    },

    /**
     * processes an incoming error from the response
     * which is hosted under the &lt;error&gt; tag
     * @param request the current request
     * @param context the contect object
     * @param node the node in the xml hosting the error message
     */
    processError: function (request, context, node) {
        /**
         * <error>
         *      <error-name>String</error-name>
         *      <error-message><![CDATA[message]]></error-message>
         * <error>
         */
        var errorName = node.firstChild.textContent || node.firstChild.text || "",
            errorMessage = node.childNodes[1].firstChild.data || "";

        this.attr("impl").sendError(request, context, this.attr("impl").SERVER_ERROR, errorName, errorMessage, "myfaces._impl.xhrCore._AjaxResponse", "processError");
    },

    /**
     * processes an incoming xml redirect directive from the ajax response
     * @param request the request object
     * @param context the context
     * @param node the node hosting the redirect data
     */
    processRedirect: function (request, context, node) {
        /**
         * <redirect url="url to redirect" />
         */
        var _Lang = this._Lang;
        var redirectUrl = node.getAttribute("url");
        if (!redirectUrl) {
            throw this._raiseError(new Error(), _Lang.getMessage("ERR_RED_URL", null, "_AjaxResponse.processRedirect"), "processRedirect");
        }
        redirectUrl = _Lang.trim(redirectUrl);
        if (redirectUrl == "") {
            return false;
        }
        window.location = redirectUrl;
        return true;
    }
    ,

    /**
     * main entry point for processing the changes
     * it deals with the &lt;changes&gt; node of the
     * response
     *
     * @param request the xhr request object
     * @param context the context map
     * @param node the changes node to be processed
     */
    processChanges: function (request, context, node) {
        var changes = node.childNodes;
        var _Lang = this._Lang;
        //note we need to trace the changes which could affect our insert update or delete
        //se that we can realign our ViewStates afterwards
        //the realignment must happen post change processing

        for (var i = 0; i < changes.length; i++) {

            switch (changes[i].tagName) {

                case this.CMD_UPDATE:
                    this.processUpdate(request, context, changes[i]);
                    break;
                //this one needs a csp spec extension for the global eval
                //for now we recycle the csp for this case from the jsf.js file
                case this.CMD_EVAL:
                    _Lang.globalEval(changes[i].firstChild.data);
                    break;
                case this.CMD_INSERT:
                    this.processInsert(request, context, changes[i]);
                    break;
                case this.CMD_DELETE:
                    this.processDelete(request, context, changes[i]);
                    break;
                case this.CMD_ATTRIBUTES:
                    this.processAttributes(request, context, changes[i]);
                    break;
                case this.CMD_EXTENSION:
                    break;
                case undefined:
                    // ignoring white spaces
                    break;
                default:
                    throw this._raiseError(new Error(), "_AjaxResponse.processChanges: Illegal Command Issued", "processChanges");
            }
        }

        return true;
    },

    /**
     * First sub-step process a pending update tag
     *
     * @param request the xhr request object
     * @param context the context map
     * @param node the changes node to be processed
     */
    processUpdate: function (request, context, node) {
        var mfInternal = context._mfInternal;
        if ((node.getAttribute('id').indexOf(this.P_VIEWSTATE) != -1) || (node.getAttribute('id').indexOf(this.P_CLIENTWINDOW) != -1)) {
            if (node.getAttribute('id').indexOf(this.P_VIEWSTATE) != -1) {
                mfInternal.appliedViewState = this._Dom.concatCDATABlocks(node);//node.firstChild.nodeValue;
            } else if (node.getAttribute('id').indexOf(this.P_CLIENTWINDOW) != -1) {
                mfInternal.appliedClientWindow = node.firstChild.nodeValue;
            }
        }
        else {
            // response may contain several blocks
            var cDataBlock = this._Dom.concatCDATABlocks(node),
                resultNode = null,
                pushOpRes = this._Lang.hitch(this, this._pushOperationResult);

            switch (node.getAttribute('id')) {
                case this.P_VIEWROOT:

                    cDataBlock = cDataBlock.substring(cDataBlock.indexOf("<html"));

                    var parsedData = this._replaceHead(request, context, cDataBlock);

                    ('undefined' != typeof parsedData && null != parsedData) ? this._replaceBody(request, context, cDataBlock, parsedData) : this._replaceBody(request, context, cDataBlock);

                    break;
                case this.P_VIEWHEAD:
                    //we cannot replace the head, almost no browser allows this, some of them throw errors
                    //others simply ignore it or replace it and destroy the dom that way!
                    this._replaceHead(request, context, cDataBlock);

                    break;
                case this.P_VIEWBODY:
                    //we assume the cdata block is our body including the tag
                    resultNode = this._replaceBody(request, context, cDataBlock);
                    if (resultNode) {
                        pushOpRes(context, resultNode);
                    }
                    break;
                case this.P_RESOURCE:

                    this._addResourceToHead(request, context, cDataBlock);
                    break;
                default:

                    resultNode = this.replaceHtmlItem(request, context, node.getAttribute('id'), cDataBlock);
                    if (resultNode) {
                        pushOpRes(context, resultNode);
                    }
                    break;
            }
        }

        return true;
    },

    _pushOperationResult: function(context, resultNode) {
        var mfInternal = context._mfInternal;
        var pushSubnode = this._Lang.hitch(this, function(currNode) {
            var parentForm = this._Dom.getParent(currNode, "form");
            //if possible we work over the ids
            //so that elements later replaced are referenced
            //at the latest possibility
            if (null != parentForm) {
                mfInternal._updateForms.push(parentForm.id || parentForm);
            }
            else {
                mfInternal._updateElems.push(currNode.id || currNode);
            }
        });

        var pushEmbedded = this._Lang.hitch(this, function(currNode) {
            if(currNode.tagName && this._Lang.equalsIgnoreCase(currNode.tagName, "form")) {
                if(currNode.id)  { //should not happen but just in case someone manipulates the html
                    mfInternal._updateForms.push(currNode.id);
                }
            } else {
                var childForms = this._Dom.findByTagName(currNode, "form");
                if(childForms && childForms.length) {
                    for(var cnt = 0; cnt < childForms.length; cnt++) {
                        if(childForms[cnt].id) {
                            mfInternal._updateForms.push(childForms[cnt].id);
                        }
                    }
                }
            }

        });


        var isArr = 'undefined' != typeof resultNode.length && 'undefined' == typeof resultNode.nodeType;
        if (isArr && resultNode.length) {
            for (var cnt = 0; cnt < resultNode.length; cnt++) {
                pushSubnode(resultNode[cnt]);
                pushEmbedded(resultNode[cnt]);
            }
        } else if (!isArr) {
            pushSubnode(resultNode);
            pushEmbedded(resultNode);
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
    _replaceHead: function (request, context, newData) {

        var _Lang = this._Lang,
            _Dom = this._Dom;

        var newDom = _Dom.fromMarkup(newData);
        var newHead = newDom.getElementsByTagName("head")[0];
        var oldTags = document.head.childNodes;

        _Dom.deleteItems(_Lang.objToArray(oldTags));
        _Dom.appendToHead(newHead);


        return document.head;
    },

    _addResourceToHead: function (request, context, newData) {
        this._Dom.appendToHead(newData);
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
    _replaceBody: function (request, context, newData /*varargs*/) {
        var _RT = this._RT,
            _Dom = this._Dom,

            oldBody = document.getElementsByTagName("body")[0],
            placeHolder = document.createElement("div");

        placeHolder.id = "myfaces_bodyplaceholder";

        var newDom = _Dom.fromMarkup(newData);
        var newBodyData = newDom.getElementsByTagName("body")[0];

        _Dom._removeChildNodes(oldBody);
        oldBody.innerHTML = "";
        oldBody.appendChild(placeHolder);


        //speedwise we serialize back into the code
        //for code reduction, speedwise we will take a small hit
        //there which we will clean up in the future, but for now
        //this is ok, I guess, since replace body only is a small subcase
        //bodyData = _Lang.serializeChilds(newBodyData);
        var browser = _RT.browser;
        if (!browser.isIEMobile || browser.isIEMobile >= 7) {
            //TODO check what is failing there
            for (var cnt = 0; cnt < newBodyData.attributes.length; cnt++) {
                var value = newBodyData.attributes[cnt].value;
                if (value)
                    _Dom.setAttribute(oldBody, newBodyData.attributes[cnt].name, value);
            }
        }

        var returnedElement = this.replaceHtmlItem(request, context, placeHolder, newBodyData.innerHTML);

        if (returnedElement) {
            this._pushOperationResult(context, returnedElement);
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
    replaceHtmlItem: function (request, context, itemIdToReplace, markup) {
        var _Lang = this._Lang, _Dom = this._Dom;

        var item = (!_Lang.isString(itemIdToReplace)) ? itemIdToReplace :
            _Dom.byIdOrName(itemIdToReplace);

        if (!item) {
            throw this._raiseError(new Error(), _Lang.getMessage("ERR_ITEM_ID_NOTFOUND", null, "_AjaxResponse.replaceHtmlItem", (itemIdToReplace) ? itemIdToReplace.toString() : "undefined"), "replaceHtmlItem");
        }
        return _Dom.outerHTML(item, markup, this._RT.getLocalOrGlobalConfig(context, "preserveFocus", false));
    },

    /**
     * xml insert command handler
     *
     * @param request the ajax request element
     * @param context the context element holding the data
     * @param node the xml node holding the insert data
     * @return true upon successful completion, false otherwise
     *
     **/
    processInsert: function (request, context, node) {
        /*remapping global namespaces for speed and readability reasons*/
        var _Dom = this._Dom,
            _Lang = this._Lang,
            //determine which path to go:
            insertData = this._parseInsertData(request, context, node);

        if (!insertData) return false;

        var opNode = _Dom.byIdOrName(insertData.opId);
        if (!opNode) {
            throw this._raiseError(new Error(), _Lang.getMessage("ERR_PPR_INSERTBEFID_1", null, "_AjaxResponse.processInsert", insertData.opId), "processInsert");
        }

        //call insertBefore or insertAfter in our dom routines
        var replacementFragment = _Dom[insertData.insertType](opNode, insertData.cDataBlock);
        if (replacementFragment) {
            this._pushOperationResult(context, replacementFragment);
        }
        return true;
    },

    /**
     * determines the corner data from the insert tag parsing process
     *
     *
     * @param request request
     * @param context context
     * @param node the current node pointing to the insert tag
     * @return false if the parsing failed, otherwise a map with follwing attributes
     * <ul>
     *     <li>inserType - a ponter to a constant which maps the direct function name for the insert operation </li>
     *     <li>opId - the before or after id </li>
     *     <li>cDataBlock - the html cdata block which needs replacement </li>
     * </ul>
     *
     * TODO we have to find a mechanism to replace the direct sendError calls with a javascript exception
     * which we then can use for cleaner error code handling
     */
    _parseInsertData: function (request, context, node) {
        var _Lang = this._Lang,
            _Dom = this._Dom,
            concatCDATA = _Dom.concatCDATABlocks,

            INSERT_TYPE_BEFORE = "insertBefore",
            INSERT_TYPE_AFTER = "insertAfter",

            id = node.getAttribute("id"),
            beforeId = node.getAttribute("before"),
            afterId = node.getAttribute("after"),
            ret = {};

        //now we have to make a distinction between two different parsing paths
        //due to a spec malalignment
        //a <insert id="... beforeId|AfterId ="...
        //b <insert><before id="..., <insert> <after id="....
        //see https://issues.apache.org/jira/browse/MYFACES-3318
        //simple id, case1
        if (id && beforeId && !afterId) {
            ret.insertType = INSERT_TYPE_BEFORE;
            ret.opId = beforeId;
            ret.cDataBlock = concatCDATA(node);

            //<insert id=".. afterId="..
        } else if (id && !beforeId && afterId) {
            ret.insertType = INSERT_TYPE_AFTER;
            ret.opId = afterId;
            ret.cDataBlock = concatCDATA(node);

            //<insert><before id="... <insert><after id="...
        } else if (!id) {
            var opType = node.childNodes[0].tagName;

            if (opType != "before" && opType != "after") {
                throw this._raiseError(new Error(), _Lang.getMessage("ERR_PPR_INSERTBEFID"), "_parseInsertData");
            }
            opType = opType.toLowerCase();
            var beforeAfterId = node.childNodes[0].getAttribute("id");
            ret.insertType = (opType == "before") ? INSERT_TYPE_BEFORE : INSERT_TYPE_AFTER;
            ret.opId = beforeAfterId;
            ret.cDataBlock = concatCDATA(node.childNodes[0]);
        } else {
            throw this._raiseError(new Error(), [_Lang.getMessage("ERR_PPR_IDREQ"),
                "\n ",
                _Lang.getMessage("ERR_PPR_INSERTBEFID")].join(""), "_parseInsertData");
        }
        ret.opId = _Lang.trim(ret.opId);
        return ret;
    },

    processDelete: function (request, context, node) {

        var _Lang = this._Lang,
            _Dom = this._Dom,
            deleteId = node.getAttribute('id');

        if (!deleteId) {
            throw this._raiseError(new Error(), _Lang.getMessage("ERR_PPR_UNKNOWNCID", null, "_AjaxResponse.processDelete", ""), "processDelete");
        }

        var item = _Dom.byIdOrName(deleteId);
        if (!item) {
            throw this._raiseError(new Error(), _Lang.getMessage("ERR_PPR_UNKNOWNCID", null, "_AjaxResponse.processDelete", deleteId), "processDelete");
        }

        var parentForm = this._Dom.getParent(item, "form");
        if (null != parentForm) {
            context._mfInternal._updateForms.push(parentForm);
        }
        _Dom.deleteItem(item);

        return true;
    },

    processAttributes: function (request, context, node) {
        //we now route into our attributes function to bypass
        //IE quirks mode incompatibilities to the biggest possible extent
        //most browsers just have to do a setAttributes but IE
        //behaves as usual not like the official standard
        //myfaces._impl._util.this._Dom.setAttribute(domNode, attribute, value;

        var _Lang = this._Lang,
            //<attributes id="id of element"> <attribute name="attribute name" value="attribute value" />* </attributes>
            elemId = node.getAttribute('id');

        if (!elemId) {
            throw this._raiseError(new Error(), "Error in attributes, id not in xml markup", "processAttributes");
        }
        var childNodes = node.childNodes;

        if (!childNodes) {
            return false;
        }
        for (var loop2 = 0; loop2 < childNodes.length; loop2++) {
            var attributesNode = childNodes[loop2],
                attrName = attributesNode.getAttribute("name"),
                attrValue = attributesNode.getAttribute("value");

            if (!attrName) {
                continue;
            }

            attrName = _Lang.trim(attrName);
            /*no value means reset*/
            //value can be of boolean value hence full check
            if ('undefined' == typeof attrValue || null == attrValue) {
                attrValue = "";
            }

            switch (elemId) {
                case this.P_VIEWROOT:
                    throw  this._raiseError(new Error(), _Lang.getMessage("ERR_NO_VIEWROOTATTR", null, "_AjaxResponse.processAttributes"), "processAttributes");

                case this.P_VIEWHEAD:
                    throw  this._raiseError(new Error(), _Lang.getMessage("ERR_NO_HEADATTR", null, "_AjaxResponse.processAttributes"), "processAttributes");

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

    /**
     * internal helper which raises an error in the
     * format we need for further processing
     *
     * @param message the message
     * @param title the title of the error (optional)
     * @param name the name of the error (optional)
     */
    _raiseError: function (error, message, caller, title, name) {
        var _Impl = this.attr("impl");
        var finalTitle = title || _Impl.MALFORMEDXML;
        var finalName = name || _Impl.MALFORMEDXML;
        var finalMessage = message || "";

        return this._Lang.makeException(error, finalTitle, finalName, this._nameSpace, caller || ( (arguments.caller) ? arguments.caller.toString() : "_raiseError"), finalMessage);
    }
});
