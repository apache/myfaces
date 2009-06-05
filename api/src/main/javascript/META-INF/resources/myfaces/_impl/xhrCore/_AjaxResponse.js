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

_reserveMyfacesNamespaces();


if (!myfaces._impl._util._LangUtils.exists(myfaces._impl.xhrCore, "_AjaxResponse")) {


    /**
     * Constructor
     * @param {String} alarmThreshold
     */
    myfaces._impl.xhrCore._AjaxResponse = function(alarmThreshold) {
        this.alarmThreshold = alarmThreshold;
        this.m_exception = new myfaces._impl.xhrCore._Exception("myfaces._impl.xhrCore._AjaxResponse", this.alarmThreshold);
    };

    /*partial response types*/
    myfaces._impl.xhrCore._AjaxResponse.prototype._RESPONSE_PARTIAL         = "partial-response";
    myfaces._impl.xhrCore._AjaxResponse.prototype._RESPONSETYPE_ERROR       = "error";
    myfaces._impl.xhrCore._AjaxResponse.prototype._RESPONSETYPE_REDIRECT    = "redirect";
    myfaces._impl.xhrCore._AjaxResponse.prototype._RESPONSETYPE_REDIRECT    = "changes";

    /*partial commands*/
    myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_CHANGES     = "changes";
    myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_UPDATE      = "update";
    myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_DELETE      = "delete";
    myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_INSERT      = "insert";
    myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_EVAL        = "eval";
    myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_ERROR       = "error";
    myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_ATTRIBUTES  = "attributes";
    myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_EXTENSION   = "extension";

    /**
     * uses response to start Html element replacement
     *
     * @param {Object} request - xhr request object
     * @param {Map} context - AJAX context
     *
     * A special handling has to be added to the update cycle
     * according to the JSDoc specs if the CDATA block contains html tags the outer rim must be stripped
     * if the CDATA block contains a head section the document head must be replaced
     * and if the CDATA block contains a body section the document body must be replaced!
     *
     */
    myfaces._impl.xhrCore._AjaxResponse.prototype.processResponse = function(request, context) {
        try {
            // TODO:
            // Solution from
            // http://www.codingforums.com/archive/index.php/t-47018.html
            // to solve IE error 1072896658 when a Java server sends iso88591
            // istead of ISO-8859-1
            if ('undefined' == typeof(request) || null == request) {
                throw Exception("jsf.ajaxResponse: The response cannot be null or empty!");
            }

            if (!myfaces._impl._util._LangUtils.exists(request, "responseXML")) {
            	myfaces.ajax.sendError(request, context, myfaces._impl.core._jsfImpl._ERROR_EMPTY_RESPONSE);
                return;
            }

            var xmlContent = request.responseXML;
            if (xmlContent.firstChild.tagName == "parsererror") {
            	myfaces.ajax.sendError(request, context, myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML);
                return;
            }
            var partials = xmlContent.childNodes[0];
            if ('undefined' == typeof partials || partials == null
                || partials.tagName != this._RESPONSE_PARTIAL) {
            	myfaces.ajax.sendError(request, context, myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML);
                return;
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
        } catch (e) {
            this.m_exception.throwError(request, context, "processResponse", e);
        }
    };

    myfaces._impl.xhrCore._AjaxResponse.prototype.processError = function(request, context, node) {
        /**
     * <error>
     *      <error-name>String</error-name>
     *      <error-message><![CDATA[message]]></error-message>
     * <error>
     */
        var errorName = node.firstChild.textContent;
        var errorMessage = node.childNodes[1].firstChild.data;

        if('undefined' == typeof errorName || null == errorName) {
            errorName = "";
        }
        if('undefined' == typeof errorMessage || null == errorMessage) {
            errorMessage = "";
        }
        myfaces.ajax.sendError(request, context,myfaces._impl.core._jsfImpl._ERROR_SERVER_ERROR , errorName, errorMessage);
    }

    myfaces._impl.xhrCore._AjaxResponse.prototype.processRedirect = function(request, context, node) {
        /**
     * <redirect url="url to redirect" />
     */
        var redirectUrl = node.getAttribute("url");
        if('undefined' == typeof redirectUrl || null == redirectUrl) {
        	myfaces.ajax.sendError(request, context,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,"Redirect without url");
            return false;
        }
        redirectUrl = myfaces._impl._util._LangUtils.trim(redirectUrl);
        if(redirectUrl == "") {
            return false;
        }
        window.location = redirectUrl;
        return true;
    }

    myfaces._impl.xhrCore._AjaxResponse.prototype.processChanges = function(request, context, node) {
        var changes = node.childNodes;

        for (var i = 0; i < changes.length; i++) {
            if (changes[i].tagName == "update") {
                if (!this.processUpdate(request, context, changes[i])) return false;
            } else if (changes[i].tagName == this._PCMD_EVAL) {
                //eval is always in CDATA blocks
                myfaces._impl._util._Utils.globalEval(changes[i].firstChild.data);
            } else if (changes[i].tagName == this._PCMD_INSERT) {
                if (!this.processInsert(request, context, changes[i])) return false;
            } else if (changes[i].tagName == this._PCMD_DELETE) {
                if (!this.processDelete(request, context, changes[i])) return false;
            } else if (changes[i].tagName == this._PCMD_ATTRIBUTES) {
                if (!this.processAttributes(request, context, changes[i])) return false;
            // this._responseHandler.doAtttributes(childNode);
            //TODO check the spec if this part is obsolete!!!
            //} else if (changes[i].tagName == this._PCMD_EXTENSION) {
            //  this._responseHandler.doExtension(childNode);

            } else {
            	myfaces.ajax.sendError(request, context, myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML);
                return false;
            }
        }
        return true;
    }

    myfaces._impl.xhrCore._AjaxResponse.prototype.processUpdate = function(request, context, node) {
        if (node.getAttribute('id') == "javax.faces.ViewState") {
            /*if(null != document.getElementById("javax.faces.ViewState")) {
                document.getElementById("javax.faces.ViewState").value = node.firstChild.nodeValue;
            } else {

            }*/
            //update the submitting forms viewstate to the new value

            var sourceForm = myfaces._impl._util._Utils.getParent(null, context, context.source, "form");

            if ('undefined' == typeof sourceForm || null == sourceForm) {
                sourceForm = document.forms.length > 0 ? document.forms[0]:null;
            }
            if(null != sourceForm) {
                /*we check for an element and include a namesearch, but only within the bounds of the committing form*/
                var element = myfaces._impl._util._Utils.getElementFromForm(request, context, "javax.faces.ViewState", sourceForm,  true, true);
                if(null == element) {//no element found we have to append a hidden field
                    element = document.createElement("input");
                    myfaces._impl._util._Utils.setAttribute(element,"type", "hidden");
                    myfaces._impl._util._Utils.setAttribute(element,"name","javax.faces.ViewState");
                    sourceForm.appendChild(element);
                }
                myfaces._impl._util._Utils.setAttribute(element,"value", node.firstChild.nodeValue);

            }
            //else??? the latest spec has this omitted we have to wait for the RI which probably covers this

        } else {
            var cDataBlock = [];
            // response may contain sevaral blocks
            for (var i = 0; i < node.childNodes.length; i++) {
                cDataBlock.push( node.childNodes[i].data);
            }
            //a join is more efficient that normal string ops!
            cDataBlock = cDataBlock.join("");

            var body = document.getElementsByTagName('body')[0];
            var head = document.getElementsByTagName('head')[0];

            switch(node.getAttribute('id')) {
                case "javax.faces.ViewRoot":

                    var parser =  myfaces.ajax._impl = new (myfaces._impl._util._Utils.getGlobalConfig("updateParser",myfaces._impl._util._HtmlStripper))();
                
                    document.documentElement.innerHTML  =  parser.parse(cDataBlock);
                        // innerHTML doesn't execute scripts, so no browser switch here
                    myfaces._impl._util._Utils.runScripts(request, context, cDataBlock);
                   
                    break;
                case "javax.faces.ViewHead":
                    //we assume the cdata block is our head including the tag
                    this._replaceElement(request, context, head, cDataBlock );
                    

                    break;
                case "javax.faces.ViewBody":
                    //we assume the cdata block is our body including the tag
                    this._replaceElement(request, context, body, cDataBlock );
  
                    break;

                default:
                    this._replaceElement(request, context,node.getAttribute('id'), cDataBlock );
                   
                    break;
            }
        }
        return true;
    };

    /**
     * Helper method to avoid duplicate code
     * @param {Object}Êrequest our request object
     * @param {Map} context the response context
     * @param {DomNode}Â oldElement the element to be replaced
     * @param {String} newData the markup which replaces the old dom node!
     */
    myfaces._impl.xhrCore._AjaxResponse.prototype._replaceElement = function(request, context, oldElement, newData) {
        myfaces._impl._util._Utils.replaceHtmlItem(request, context,
            oldElement, newData, this.m_htmlFormElement);
        //fetch the scripts and do an eval on the scripts to bypass
        //browser inconsistencies in this area
        //lets have the browser itself deal with this issue, j4fry
        //is pretty well optimized in this area!
        if (myfaces._impl._util._Utils.isManualScriptEval()) {
            myfaces._impl._util._Utils.runScripts(request, context, newData);
        }
    };

    /*insert, three attributes can be present
     * id = insert id
     * before = before id
     * after = after  id
     *
     * the insert id is the id of the node to be inserted
     * the before is the id if set which the component has to be inserted before
     * the after is the id if set which the component has to be inserted after
     **/
    myfaces._impl.xhrCore._AjaxResponse.prototype.processInsert = function(request, context, node) {
        var insertId    = node.getAttribute('id');
        var beforeId    = node.getAttribute('before');
        var afterId     = node.getAttribute('after');

        var insertSet   = 'undefined' != typeof insertId && null != insertId && myfaces._impl._util._LangUtils.trim(insertId) != "";
        var beforeSet   = 'undefined' != typeof beforeId && null != beforeId && myfaces._impl._util._LangUtils.trim(beforeId) != "";
        var afterSet    = 'undefined' != typeof afterId && null != afterId && myfaces._impl._util._LangUtils.trim(afterId) != "";

        if(!insertSet) {
        	myfaces.ajax.sendError(request, context,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML, "Error in PPR Insert, id must be present");
            return false;
        }
        if(!(beforeSet || afterSet)) {
        	myfaces.ajax.sendError(request, context,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML, myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML, "Error in PPR Insert, before id or after id must be present");
            return false;
        }
        //either before or after but not two at the same time
        var nodeHolder = null;
        var parentNode = null;
        if(beforeSet) {
            beforeId = myfaces._impl._util._LangUtils.trim(beforeId);
            var beforeNode = document.getElementById(beforeId);
            if('undefined' == typeof beforeNode || null == beforeNode) {
            	myfaces.ajax.sendError(request, context,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML, myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML, "Error in PPR Insert, before  node of id "+beforeId+" does not exist in document");
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

            myfaces._impl._util._Utils.replaceHtmlItem(request, context,
                nodeHolder, node.firstChild.data, null);

        } else {
            afterId = myfaces._impl._util._LangUtils.trim(afterId);
            var afterNode = document.getElementById(afterId);
            if('undefined' == typeof afterNode || null == afterNode) {
            	myfaces.ajax.sendError(request, context, myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML, "Error in PPR Insert, after  node of id "+after+" does not exist in document");
                return false;
            }
            nodeHolder = document.createElement("div");
            parentNode = afterNode.parentNode;
            parentNode.insertBefore(nodeHolder, afterNode.nextSibling);
            myfaces._impl._util._Utils.replaceHtmlItem(request, context,
                nodeHolder, node.firstChild.data, null);
        }
        return true;
    }

    myfaces._impl.xhrCore._AjaxResponse.prototype.processDelete = function(request, context, node) {
        var deleteId = node.getAttribute('id');
        if('undefined' == typeof deleteId || null == deleteId) {
        	myfaces.ajax.sendError(request, context, myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,
                myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,  "Error in delete, id not in xml markup");
            return false;
        }

        myfaces._impl._util._Utils.deleteItem(request, context, deleteId, "","");
        return true;
    }

    myfaces._impl.xhrCore._AjaxResponse.prototype.processAttributes = function(request, context, node) {
        //we now route into our attributes function to bypass
        //IE quirks mode incompatibilities to the biggest possible extent
        //most browsers just have to do a setAttributes but IE
        //behaves as usual not like the official standard
        //myfaces._impl._util._Utils.setAttribute(domNode, attribute, value;

        //<attributes id="id of element"> <attribute name="attribute name" value="attribute value" />* </attributes>
        var attributesRoot = node;
        var elementId = attributesRoot.getAttribute('id');
        if('undefined' == typeof elementId || null == elementId) {
        	myfaces.ajax.sendError(request, context,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML
                ,myfaces._impl.core._jsfImpl._ERROR_MALFORMEDXML,  "Error in attributes, id not in xml markup");
            return false;
        }
        var childs = attributesRoot.childNodes;

        if('undefined' == typeof childs || null == childs) {
            return false;
        }
        for(var loop2 = 0; loop2 < childs.length; loop2++) {
            var attributesNode = childs[loop2];

            var attributeName = attributesNode.getAttribute("name");
            var attributeValue = attributesNode.getAttribute("value");

            if('undefined' == typeof attributeName || null == attributeName) {
                continue;
            }

            attributeName = myfaces._impl._util._LangUtils.trim(attributeName);
            /*no value means reset*/
            if('undefined' == typeof attributeValue || null == attributeValue) {
                attributeValue = "";
            }

            myfaces._impl._util._Utils.setAttribute(document.getElementById(elementId), attributeName, attributeValue);
        }
        return true;
    }

}