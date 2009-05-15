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
 * Version: $Revision: 1.7 $ $Date: 2009/04/23 11:03:09 $
 *
 */

_reserveMyfacesNamespaces();

if (!myfaces._impl._util._LangUtils.exists(myfaces._impl.xhrCore, "_AjaxUtils")) {

    /**
     * Constructor
     * @param {String} alarmThreshold - Error Level
     */
    myfaces._impl.xhrCore._AjaxUtils = function(alarmThreshold) {
        // Exception Objekt
        this.alarmThreshold = alarmThreshold;
        this.m_exception = new myfaces._impl.xhrCore._Exception("myfaces._impl.xhrCore._AjaxUtils", this.alarmThreshold);
    };

    /**
     * determines fields to submit
     * @param {HtmlElement} item - item that triggered the event
     * @param {HtmlElement} parentItem - form element item is nested in
     * @param {Array} partialIds - ids fo PPS
     */
    myfaces._impl.xhrCore._AjaxUtils.prototype.processUserEntries = function(request, context, item,
        parentItem,	partialIds) {
        try {
            var form = parentItem;

            if (form == null) {
                this.m_exception.throwWarning(request, context, "processUserEntries",
                    "Html-Component is not nested in a Form-Tag");
                return null;
            }

            var stringBuffer = new Array();

            if (partialIds != null && partialIds.length > 0) {
                // recursivly check items
                this.addNodes(form, false, partialIds, stringBuffer);
            } else {
                // add all nodes
                var eLen = form.elements.length;
                for ( var e = 0; e < eLen; e++) {
                    this.addField(form.elements[e], stringBuffer);
                } // end of for (formElements)
            }

            // if triggered by a Button send it along
            if (item.type != null && item.type.toLowerCase() == "submit") {
                stringBuffer[stringBuffer.length] = encodeURIComponent(item.name);
                stringBuffer[stringBuffer.length] = "=";
                stringBuffer[stringBuffer.length] = encodeURIComponent(item.value);
                stringBuffer[stringBuffer.length] = "&";
            }

            return stringBuffer.join("");
        } catch (e) {
            alert(e);
            this.m_exception.throwError(request, context, "processUserEntries", e);
        }
    };

    /**
     * checks recursively if contained in PPS
     * @param {} node -
     * @param {} insideSubmittedPart -
     * @param {} partialIds -
     * @param {} stringBuffer -
     */
    myfaces._impl.xhrCore._AjaxUtils.prototype.addNodes = function(node, insideSubmittedPart,
        partialIds, stringBuffer) {
        if (node != null && node.childNodes != null) {
            var nLen = node.childNodes.length;
            for ( var i = 0; i < nLen; i++) {
                var child = node.childNodes[i];
                var id = child.id;
                var elementName = child.name;
                if (child.nodeType == 1) {
                    var isPartialSubmitContainer = ((id != null)
                        && myfaces._impl._util._LangUtils.arrayContains(partialIds, id));
                    if (insideSubmittedPart
                        || isPartialSubmitContainer
                        || (elementName != null
                            && elementName == myfaces._impl.core._jsfImpl._PROP_VIEWSTATE)) {
                        // node required for PPS
                        this.addField(child, stringBuffer);
                        if (insideSubmittedPart || isPartialSubmitContainer) {
                            // check for further children
                            this.addNodes(child, true, partialIds, stringBuffer);
                        }
                    } else {
                        // check for further children
                        this.addNodes(child, false, partialIds, stringBuffer);
                    }
                }
            }
        }
    }

    /**
     * encapsulated xhr object which tracks down various implementations
     * of the xhr object in a browser independend fashion
     * (ie pre 7 used to have non standard implementations because
     * the xhr object standard came after IE had implemented it first
     * newer ie versions adhere to the standard and all other new browsers do anyway)
     */
    myfaces._impl.xhrCore._AjaxUtils.prototype.getXHRObject = function() {
        if('undefined' != typeof XMLHttpRequest && null != XMLHttpRequest) {
            return new XMLHttpRequest();
        }
        //IE
        try {
            return new ActiveXObject("Msxml2.XMLHTTP");
        } catch (e) {
            return new ActiveXObject('Microsoft.XMLHTTP');
        }
       
    }


    /**
     * loads a script and executes it under a global scope
     * @param {String} src the source to be loaded
     * @param {String} type the mime type of the script (currently ignored
     * but in the long run it will be used)
     */
    myfaces._impl.xhrCore._AjaxUtils.prototype.loadScript = function(src, type, defer, charSet) {
        var xhr = this.getXHRObject();
        xhr.open("GET", src, false);

        if('undefined' != typeof charSet && null != charSet) {
            xhr.setRequestHeader("Content-Type","application/x-javascript; charset:"+charSet);
        }

        xhr.send(null);

        //since we are synchronous we do it after not with onReadyStateChange
        if(xhr.readyState == 4) {
            if (xhr.status == 200) {
                //defer also means we have to process after the ajax response
                //has been processed
                //we can achieve that with a small timeout, the timeout
                //triggers after the processing is done!
                if(!defer) {
                    myfaces._impl._util._Utils.globalEval(xhr.responseText);
                } else {
                   setTimeout(function() {
                     myfaces._impl._util._Utils.globalEval(xhr.responseText);
                   },1);
                }
            } else {
                throw Error(xhr.responseText);
            }
        } else {
            throw Error("Loading of script "+src+" failed ");
        }
    }



    /**
     * add a single field to stringbuffer for param submission
     * @param {HtmlElement} element -
     * @param {} stringBuffer -
     */
    myfaces._impl.xhrCore._AjaxUtils.prototype.addField = function(element, stringBuffer) {
        var elementName = element.name;
        var elementTagName = element.tagName.toLowerCase();
        var elementType = element.type;
        if (elementType != null) {
            elementType = elementType.toLowerCase();
        }

        // routine for all elements
        // rules:
        // - process only inputs, textareas and selects
        // - elements muest have attribute "name"
        // - elements must not be disabled
        if (((elementTagName == "input" || elementTagName == "textarea" || elementTagName == "select") &&
            (elementName != null && elementName != "")) && element.disabled == false) {

            // routine for select elements
            // rules:
            // - if select-one and value-Attribute exist => "name=value"
            // (also if value empty => "name=")
            // - if select-one and value-Attribute don't exist =>
            // "name=DisplayValue"
            // - if select multi and multple selected => "name=value1&name=value2"
            // - if select and selectedIndex=-1 don't submit
            if (elementTagName == "select") {
                // selectedIndex must be >= 0 sein to be submittet
                if (element.selectedIndex >= 0) {
                    var uLen = element.options.length;
                    for ( var u = 0; u < uLen; u++) {
                        // find all selected options
                        if (element.options[u].selected == true) {
                            var elementOption = element.options[u];
                            stringBuffer[stringBuffer.length] = encodeURIComponent(elementName);
                            stringBuffer[stringBuffer.length] = "=";
                            if (elementOption.getAttribute("value") != null) {
                                stringBuffer[stringBuffer.length] = encodeURIComponent(elementOption.value);
                            } else {
                                stringBuffer[stringBuffer.length] = encodeURIComponent(elementOption.text);
                            }
                            stringBuffer[stringBuffer.length] = "&";
                        }
                    }
                }
            }

            // routine for remaining elements
            // rules:
            // - don't submit no selects (processed above), buttons, reset buttons, submit buttons,
            // - submit checkboxes and radio inputs only if checked
            if ((elementTagName != "select" && elementType != "button"
                && elementType != "reset" && elementType != "submit" && elementType != "image")
            && ((elementType != "checkbox" && elementType != "radio") || element.checked)) {
                stringBuffer[stringBuffer.length] = encodeURIComponent(elementName);
                stringBuffer[stringBuffer.length] = "=";
                stringBuffer[stringBuffer.length] = encodeURIComponent(element.value);
                stringBuffer[stringBuffer.length] = "&";
            }

        }
    }
}