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
 * Version: $Revision: 1.1 $ $Date: 2009/05/26 21:24:42 $
 *
 */

myfaces._impl.core._Runtime.extendClass("myfaces._impl.xhrCore._AjaxUtils", Object, {
    /**
     * Constructor
     * @param {String} alarmThreshold - Error Level
     */
    constructor_ : function(onException, onWarning) {

        this._onException = onException;
        this._onWarning = onWarning;
    },

    /**
     * determines fields to submit
     * @param {Object} request the xhr request object
     * @param {Object} context (Map)
     * @param {Node} item - item that triggered the event
     * @param {Node} parentItem - form element item is nested in
     * @param {Array} partialIds - ids fo PPS
     */
    encodeSubmittableFields : function(request, context, item,
                                       parentItem, partialIds) {
        try {
            if (parentItem == null) {
                this._onWarning(request, context,"myfaces._impl.xhrCore._AjaxUtils" ,"encodeSubmittableFields "+"Html-Component is not nested in a Form-Tag");
                return null;
            }

            var strBuf = [];

            if (partialIds != null && partialIds.length > 0) {
                // recursivly check items
                this.encodePartialSubmit(parentItem, false, partialIds, strBuf);
            } else {
                // add all nodes
                var eLen = parentItem.elements.length;
                for (var e = 0; e < eLen; e++) {
                    this.encodeElement(parentItem.elements[e], strBuf);
                } // end of for (formElements)
            }

            // if triggered by a Button send it along
            if ('undefined' != typeof item && null != item && item.type != null && item.type.toLowerCase() == "submit") {
                strBuf.push(encodeURIComponent(item.name));
                strBuf.push("=");
                strBuf.push(encodeURIComponent(item.value));
                strBuf.push("&");
            }

            return strBuf.join("");
        } catch (e) {
            this._onException(request, context,"myfaces._impl.xhrCore._AjaxUtils" ,"encodeSubmittableFields", e);
        }
    },

    /**
     * checks recursively if contained in PPS
     * @param {Node} node - the root node of the partial page submit
     * @param {boolean} submitAll - if set to true, all elements within this node will
     * be added to the partial page submit
     * @param {Array} partialIds - an array of partial ids which should be used for the submit
     * @param {Array} strBuf a target string buffer which receives the encoded elements
     */
    encodePartialSubmit : function(node, submitAll,
                                   partialIds, strBuf) {
        var _Lang = myfaces._impl._util._Lang;
        var _Impl = myfaces._impl.core.Impl;
        var _Dom = myfaces._impl._util._Dom;

        var nodeFilter = function(curNode) {
            //TODO bomb out if the element is not one of the input types
            //((elementTagName == "input" || elementTagName == "textarea" || elementTagName == "select") &&
            //    (elementName != null && elementName != "")) && !element.disabled
            //
            if (child.nodeType != 1) return false;
            if (submitAll && node != curNode) return true;

            var id = curNode.id;
            var name = curNode.name;

            var ppsElement = id && _Lang.arrayContains(partialIds, id);
            return  ppsElement || (name != null && name == _Impl._PROP_VIEWSTATE);
        };

        var nodes = _Dom.findAll(node, nodeFilter, true);

        if (nodes) {
            for (cnt = 0; cnt < nodes.length; cnt++) {
                this.encodeElement(nodes[cnt], strBuf);
            }
        }
    },

    /**
     * checks recursively if contained in PPS
     * @param {} node -
     * @param {} insideSubmittedPart -
     * @param {} partialIds -
     * @param {} stringBuffer -
     */
    /*addNodes : function(node, insideSubmittedPart,
     partialIds, stringBuffer) {
     if (node != null && node.childNodes != null) {
     var nLen = node.childNodes.length;
     for (var i = 0; i < nLen; i++) {
     var child = node.childNodes[i];
     var id = child.id;
     var elementName = child.name;
     if (child.nodeType == 1) {
     var isPartialSubmitContainer = ((id != null)
     && myfaces._impl._util._Lang.arrayContains(partialIds, id));
     if (insideSubmittedPart
     || isPartialSubmitContainer
     || (elementName != null
     && elementName == myfaces._impl.core.Impl._PROP_VIEWSTATE)) {
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
     },*/

    /**
     * add a single field to stringbuffer for param submission
     * @param {Node} element -
     * @param {} strBuf -
     */
    encodeElement : function(element, strBuf) {
        var elementName = (null != element.name || 'undefined' != typeof element.name) ? element.name : element.id;
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
                (elementName != null && elementName != "")) && !element.disabled) {

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
                    for (var u = 0; u < uLen; u++) {
                        // find all selected options
                        if (element.options[u].selected) {
                            var elementOption = element.options[u];
                            strBuf.push(encodeURIComponent(elementName));
                            strBuf.push("=");
                            if (elementOption.getAttribute("value") != null) {
                                strBuf.push(encodeURIComponent(elementOption.value));
                            } else {
                                strBuf.push(encodeURIComponent(elementOption.text));
                            }
                            strBuf.push("&");
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
                strBuf.push(encodeURIComponent(elementName));
                strBuf.push("=");
                strBuf.push(encodeURIComponent(element.value));
                strBuf.push("&");
            }

        }
    }
});