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
 * Author: Ganesh Jung (latest modification by $Author: werpu $)
 * Version: $Revision: 1.9 $ $Date: 2009/04/16 12:41:32 $
 *
 */

_reserveMyfacesNamespaces();


/**
 * Constructor
 * @param {String} alarmThreshold
 */
myfaces._impl.xhrCore._AjaxResponse = function(alarmThreshold) {
    // Request object
    this.m_request = null;
    // Html-Form-Element from which the request comes from
    this.m_htmlFormElement = null;
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
myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_ATTRIBUTES  = "attributes";
myfaces._impl.xhrCore._AjaxResponse.prototype._PCMD_EXTENSION   = "extension";

/*various errors within the rendering stage*/
myfaces._impl.xhrCore._AjaxResponse.prototype._ERROR_EMPTY_RESPONSE = "emptyResponse";
myfaces._impl.xhrCore._AjaxResponse.prototype._ERROR_MALFORMEDXML   = "malformedXML";
myfaces._impl.xhrCore._AjaxResponse.prototype._ERROR_SERVER_ERROR   = "serverError";
myfaces._impl.xhrCore._AjaxResponse.prototype._MSG_SUCCESS          = "success";

/*various ajax message types*/
myfaces._impl.xhrCore._AjaxResponse.prototype._MSG_TYPE_ERROR       = "error";
myfaces._impl.xhrCore._AjaxResponse.prototype._MSG_TYPE_EVENT       = "event";
myfaces._impl.xhrCore._AjaxResponse.prototype._AJAX_STAGE_BEGIN     = "begin";
myfaces._impl.xhrCore._AjaxResponse.prototype._AJAX_STAGE_COMPLETE  = "complete";
myfaces._impl.xhrCore._AjaxResponse.prototype._AJAX_STAGE_HTTPERROR = "httpError";

/**
 * uses response to start Html element replacement
 * @param {XmlHttpRequest} request - request object
 * @param {HtmlElement} sourceForm - HTML Form element which contains the element that triggered the request
 * @param {Map} context - AJAX context
 *
 * A special handling has to be added to the update cycle
 * according to the JSDoc specs if the CDATA block contains html tags the outer rim must be stripped
 * if the CDATA block contains a head section the document head must be replaced
 * and if the CDATA block contains a body section the document body must be replaced!
 *
 */
myfaces._impl.xhrCore._AjaxResponse.prototype.processResponse = function(request, sourceForm, context) {
    this.m_request = request;
    this.m_htmlFormElement = sourceForm;
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
            jsf.ajax.sendError(request, context, this._ERROR_EMPTY_RESPONSE);
            return;
        }

        var xmlContent = request.responseXML;
        if (xmlContent.firstChild.tagName == "parsererror") {
            jsf.ajax.sendError(request, context, this._ERROR_MALFORMEDXML);
            return;
        }
        var partials = xmlContent.childNodes[0];
        if ('undefined' == typeof partials || partials == null
            || partials.tagName != this._RESPONSE_PARTIAL) {
            jsf.ajax.sendError(request, context, this._ERROR_MALFORMEDXML);
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

            if (tagName == this._PCMD_EVAL) {
                //eval is always in CDATA blocks
                eval(childNode.firstChild.nodeValue);

            //this ought to be enough for eval
            //however the run scripts still makes sense
            //in the update and insert area for components
            //which do not use the response writer properly
            //we might add this one as custom option in update and
            //insert!
            } else if (tagName == this._PCMD_ERROR) {
                
                /**
                 * <error>
                 *      <error-name>String</error-name>
                 *      <error-messafe><![CDATA[message]]></error-message>
                 * <error>
                 */
                var errorName = childNode.firstChild.nodeValue;
                var errorMessage = childNode.childNodes[1].firstChild.nodeValue;

                if('undefined' == typeof errorName || null == errorName) {
                  errorName = "";
                }
                if('undefined' == typeof errorMessage || null == errorMessage) {
                  errorMessage = "";
                }

                jsf.ajax.sendError(request, context,this._SERVER_ERROR, errorName, errorMessage);

                return;
                

            } else if (tagName == this._PCMD_REDIRECT) {
                /**
                 * <redirect url="url to redirect" />
                 */
                var redirectUrl = childNode.getAttribute("url");
                if('undefined' == typeof redirectUrl || null == redirectUrl) {
                    jsf.ajax.sendError(request, context,this._ERROR_MALFORMEDXML,this._ERROR_MALFORMEDXML,"Redirect without url");
                    return;
                }
                redirectUrl = myfaces._impl._util._LangUtils.trim(redirectUrl);
                if(redirectUrl == "") {
                    return;
                }
                window.location = redirectUrl;
                

            } else if (tagName == this._PCMD_CHANGES) {
                var changes = childNode.childNodes;

                for (var i = 0; i < changes.length; i++) {
                    if (changes[i].tagName == "update") {
                        if (changes[i].getAttribute('id') == "javax.faces.ViewState") {
                            document.getElementById("javax.faces.ViewState").value = changes[i].firstChild.nodeValue;

                            //TODO all forms for elements with the identifier (name?) javax.faces.ViewState
                            //if present then set them if the form has no element of type javax.faces.viewState
                            //append a hidden field and set it!



                        } else {
                            //TODO make a special head body and html handling here
                            var cDataBlock = changes[i].firstChild.data;
                            if( changes[i].getAttribute('id') == "javax.faces.ViewRoot") {
                                
                                //lets strip the internal html if given
                                var htmlContent = myfaces._impl._util._Utils.stripHtml(cDataBlock);
                                htmlContent = (htmlContent == null) ? cDataBlock: headContent;
                                var headContent =  myfaces._impl._util._Utils.stripHead(htmlContent);
                                var bodyContent =  myfaces._impl._util._Utils.stripBody(htmlContent);
                                var body = document.getElementsByTagName('body')[0];
                                var head = document.getElementsByTagName('head')[0];

                                if(headContent != null) {
                                   
                                    head.innerHTML = headContent;
                                    //fetch the scripts and do an eval on the scripts to bypass
                                    //browser inconsistencies in this area
                                    //lets have the browser itself deal with this issue, j4fry
                                    //is pretty well optimized in this area!
                                    if (myfaces._impl._util._Utils.isUserAgentInternetExplorer()) {
                                        myfaces._impl._util._Utils.runScripts(head);
                                    }
                                }

                                //if the body content is provided only the body content is applied, according
                                //to the jsDoc specs!
                                if(bodyContent != null) {
                                   
                                    body.innerHTML = bodyContent;
                                    //TODO fetch the scripts and do an eval on the scripts to bypass
                                    //browser inconsistencies in this area
                                    if (myfaces._impl._util._Utils.isUserAgentInternetExplorer()) {
                                        myfaces._impl._util._Utils.runScripts(head);
                                    }
                                //no body content is defined means we have to replace the body with the entire cdata content
                                } else {
                                    body = cDataBlock;
                                    if (myfaces._impl._util._Utils.isUserAgentInternetExplorer()) {
                                        myfaces._impl._util._Utils.runScripts(head);
                                    }
                                }
                            } else {
                                myfaces._impl._util._Utils.replaceHtmlItem(
                                    changes[i].getAttribute('id'), cDataBlock, this.m_htmlFormElement);
                            }
                        }
                    }
                    else if (tagName == this._PCMD_INSERT) {
                        //  this._responseHandler.doInsert(childNode);
                        /*insert, three attributes can be present
                         * id = insert id
                         * before = before id
                         * after = after  id
                         *
                         * the insert id is the id of the node to be inserted
                         * the before is the id if set which the component has to be inserted before
                         * the after is the id if set which the component has to be inserted after
                         **/
                        var insertId = changes[i].getAttribute('id');
                        var beforeId = changes[i].getAttribute('before');
                        var afterId = changes[i].getAttribute('after');

                        var insertSet = 'undefined' != typeof insertId && null != insertId && myfaces._impl._util._LangUtils.trim(insertId) != "";
                        var beforeSet = 'undefined' != typeof beforeId && null != beforeId && myfaces._impl._util._LangUtils.trim(beforeId) != "";
                        var afterSet = 'undefined' != typeof afterId && null != afterId && myfaces._impl._util._LangUtils.trim(afterId) != "";

                        if(!insertSet) {
                             jsf.ajax.sendError(request, context,this._ERROR_MALFORMEDXML,this._ERROR_MALFORMEDXML, "Error in PPR Insert, id must be present");
                             return;
                        }
                        if(!(beforeSet || afterSet)) {
                             jsf.ajax.sendError(request, context,this._ERROR_MALFORMEDXML, this._ERROR_MALFORMEDXML, "Error in PPR Insert, before id or after id must be present");
                             return;
                        }
                        //either before or after but not two at the same time
                        if(beforeSet) {
                            beforeId = myfaces._impl._util._LangUtils.trim(beforeId);
                            var beforeNode = document.getElementById(beforeId);
                            if('undefined' == typeof beforeNode || null == beforeNode) {
                                jsf.ajax.sendError(request, context,this._ERROR_MALFORMEDXML, this._ERROR_MALFORMEDXML, "Error in PPR Insert, before  node of id "+beforeId+" does not exist in document");
                                return;
                            }
                            /**
                             *we generate a temp holder
                             *so that we can use innerHTML for
                             *generating the content upfront
                             *before inserting it"
                             **/
                            var nodeHolder = document.createElement("div");
                            nodeHolder.innerHTML = changes[i].firstChild.data;
                            var parentNode = beforeNode.parentNode;
                            parentNode.insertBefore(nodeHolder.firstChild, beforeNode);
                            /**
                             * The ie needs a separate eval cycle to
                             * deal with embedded scripts
                             * (which should not happen if the responseWriter
                             * works as expected but there are component sets
                             * which bypass the response writer)
                             */
                            if (myfaces._impl._util._Utils.isUserAgentInternetExplorer()) {
                                myfaces._impl._util._Utils.runScripts(nodeHolder.firstChild);
                            }
                        } else {
                            afterId = myfaces._impl._util._LangUtils.trim(afterId);
                            var afterNode = document.getElementById(afterId);
                            if('undefined' == typeof afterNode || null == afterNode) {
                                jsf.ajax.sendError(request, context,this._ERROR_MALFORMEDXML,this._ERROR_MALFORMEDXML, "Error in PPR Insert, after  node of id "+after+" does not exist in document");
                                return;
                            }
                            var nodeHolder = document.createElement("div");
                            nodeHolder.innerHTML = changes[i].firstChild.data;
                            var parentNode = afterNode.parentNode;
                            parentNode.insertBefore(nodeHolder.firstChild, beforeNode.nextSibling);
                            if (myfaces._impl._util._Utils.isUserAgentInternetExplorer()) {
                                myfaces._impl._util._Utils.runScripts(nodeHolder.firstChild);
                            }
                        }

                    } else if (changes[i].tagName == this._PCMD_DELETE) {
                        // this._responseHandler.doDelete(childNode);
                        var deleteId = changes[i].getAttribute('id');
                        if('undefined' == typeof deleteId || null == deleteId) {
                            jsf.ajax.sendError(request, context,this._ERROR_MALFORMEDXML,this._ERROR_MALFORMEDXML,  "Error in delete, id not in xml markup");
                            return;
                        }

                        myfaces._impl._util._Utils.deleteItem(deleteId, "","");

                    } else if (changes[i].tagName == this._PCMD_ATTRIBUTES) {
                        //we now route into our attributes function to bypass
                        //IE quirks mode incompatibilities to the biggest possible extent
                        //most browsers just have to do a setAttributes but IE
                        //behaves as usual not like the official standard
                        //myfaces._impl._util._Utils.setAttribute(domNode, attribute, value;
                         
                        //<attributes id="id of element"> <attribute name="attribute name" value="attribute value" />* </attributes>
                        var attributesRoot = changes[i];
                        var elementId = attributesRoot.getAttribute('id');
                        if('undefined' == typeof elementId || null == elementId) {
                            jsf.ajax.sendError(request, context,this._ERROR_MALFORMEDXML,this._ERROR_MALFORMEDXML,  "Error in attributes, id not in xml markup");
                            return;
                        }
                        var childs = attributesRoot.childNodes;

                        if('undefined' == typeof childs || null == childs) {
                            return;
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

                    // this._responseHandler.doAtttributes(childNode);
                    //TODO check the spec if this part is obsolete!!!
                    //} else if (changes[i].tagName == this._PCMD_EXTENSION) {
                    //  this._responseHandler.doExtension(childNode);

                    } else {
                        jsf.ajax.sendError(request, context, this._ERROR_MALFORMEDXML);
                        return;
                    }

                }
            } 
        }
        jsf.ajax.sendEvent(request, context, this._MSG_SUCCESS);
    } catch (e) {
        this.m_exception.throwError("processResponse", e);
    }
};
