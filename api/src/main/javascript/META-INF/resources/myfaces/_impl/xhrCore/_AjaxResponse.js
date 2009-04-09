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
 * Version: $Revision: 1.5 $ $Date: 2009/04/09 13:58:56 $
 *
 */

_reserveMyfacesNamespaces();


/**
 * Constructor
 * @param {String} alarmThreshold
 */
myfaces._impl.xhrCore_AjaxResponse = function(alarmThreshold) {
	// Request object
	this.m_request = null;
	// Html-Form-Element from which the request comes from
	this.m_htmlFormElement = null;	
	this.alarmThreshold = alarmThreshold;
	this.m_exception = new myfaces._impl.xhrCore_Exception("myfaces._impl.xhrCore_AjaxResponse", this.alarmThreshold);
};

/*partial response types*/
myfaces._impl.xhrCore_AjaxResponse.prototype._RESPONSE_PARTIAL = "partial-response";
myfaces._impl.xhrCore_AjaxResponse.prototype._RESPONSETYPE_ERROR = "error";
myfaces._impl.xhrCore_AjaxResponse.prototype._RESPONSETYPE_REDIRECT = "redirect";
myfaces._impl.xhrCore_AjaxResponse.prototype._RESPONSETYPE_REDIRECT = "changes";

/*partial commands*/
myfaces._impl.xhrCore_AjaxResponse.prototype._PCMD_CHANGES = "changes";
myfaces._impl.xhrCore_AjaxResponse.prototype._PCMD_DELETE = "delete";
myfaces._impl.xhrCore_AjaxResponse.prototype._PCMD_INSERT = "insert";
myfaces._impl.xhrCore_AjaxResponse.prototype._PCMD_EVAL = "eval";
myfaces._impl.xhrCore_AjaxResponse.prototype._PCMD_ATTRIBUTES = "attributes";
myfaces._impl.xhrCore_AjaxResponse.prototype._PCMD_EXTENSION = "extension";

/*various errors within the rendering stage*/
myfaces._impl.xhrCore_AjaxResponse.prototype._ERROR_EMPTY_RESPONSE = "emptyResponse";
myfaces._impl.xhrCore_AjaxResponse.prototype._ERROR_MALFORMEDXML = "malformedXML";
myfaces._impl.xhrCore_AjaxResponse.prototype._MSG_SUCCESS = "success";

/*various ajax message types*/
myfaces._impl.xhrCore_AjaxResponse.prototype._MSG_TYPE_ERROR = "error";
myfaces._impl.xhrCore_AjaxResponse.prototype._MSG_TYPE_EVENT = "event";
myfaces._impl.xhrCore_AjaxResponse.prototype._AJAX_STAGE_BEGIN = "begin";
myfaces._impl.xhrCore_AjaxResponse.prototype._AJAX_STAGE_COMPLETE = "complete";
myfaces._impl.xhrCore_AjaxResponse.prototype._AJAX_STAGE_HTTPERROR = "httpError";

/**
 * uses response to start Html element replacement
 * @param {XmlHttpRequest} request - request object
 * @param {HtmlElement} htmlFormElement - HTML Form element which contains the element that triggered the request
 */
myfaces._impl.xhrCore_AjaxResponse.prototype.processResponse = function(request, htmlFormElement) {
	this.m_request = request;
	this.m_htmlFormElement = htmlFormElement;
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

            if (tagName == this._PCMD_EVAL) {
               //eval is always in CDATA blocks
               eval(childNode.firstChild.nodeValue);

               //this ought to be enough for eval
               //however the run scripts still makes sense
               //in the update and insert area for components
               //which do not use the response writer properly
               //we might add this one as custom option in update and
               //insert!

            } else if (tagName == this._PCMD_CHANGES) {
                var changes = childNode.childNodes;

                for (var i = 0; i < changes.length; i++) {
                    if (changes[i].tagName == "update") {
                        if (changes[i].getAttribute('id') == "javax.faces.ViewState") {
                            document.getElementById("javax.faces.ViewState").value = changes[i].firstChild.data;
                        } else {
                            myfaces._impl._util._Utils.replaceHtmlItem(
                                changes[i].getAttribute('id'), changes[i].firstChild.data, this.m_htmlFormElement);
                        }
                    }
                }
            } else if (tagName == this._PCMD_INSERT) {
                //  this._responseHandler.doInsert(childNode);
            } else if (tagName == this._PCMD_DELETE) {
                // this._responseHandler.doDelete(childNode);

                myfaces._impl._util._Utils.deleteItem(childNode.getAttribute("id"), "","");

            } else if (tagName == this._PCMD_ATTRIBUTES) {
                // this._responseHandler.doAtttributes(childNode);
            } else if (tagName == this._PCMD_EXTENSION) {
                //  this._responseHandler.doExtension(childNode);
            } else {
                jsf.ajax.sendError(request, context, this._ERROR_MALFORMEDXML);
                return;
            }
        }
        jsf.ajax.sendEvent(request, context, this._MSG_SUCCESS);
	} catch (e) {
		this.m_exception.throwError("processResponse", e);
	}
};
