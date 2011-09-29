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
 * @name _AjaxUtils
 * @memberOf myfaces._impl.xhrCore
 * @description
 *
 * A set of helper routines which are utilized within our Ajax subsystem and nowhere else
 *
 * TODO move this into a singleton, the current structure is
 * still a j4fry legacy we need to get rid of it in the long run
 */
myfaces._impl.core._Runtime.singletonExtendClass("myfaces._impl.xhrCore._AjaxUtils", Object,
/** @lends myfaces._impl.xhrCore._AjaxUtils.prototype */
{
    /**
     * Constructor
     */
    constructor_ : function() {
    },


    /**
     * determines fields to submit
     * @param {Object} targetBuf - the target form buffer receiving the data
     * @param {Node} parentItem - form element item is nested in
     * @param {Array} partialIds - ids fo PPS
     */
    encodeSubmittableFields : function(targetBuf,
                                       parentItem, partialIds) {

        //try {
            if (!parentItem) {
            //    context._mfInternal._onWarning(request, context, "myfaces._impl.xhrCore._AjaxUtils", "encodeSubmittableFields " + "Html-Component is not nested in a Form-Tag");
                throw "NO_PARITEM";
            }

            if (partialIds ) {
                this.encodePartialSubmit(parentItem, false, partialIds, targetBuf);
            } else {
                // add all nodes
                var eLen = parentItem.elements.length;
                for (var e = 0; e < eLen; e++) {
                    this.encodeElement(parentItem.elements[e], targetBuf);
                } // end of for (formElements)
            }

        //} catch (e) {
        //    context._mfInternal._onException(request, context, "myfaces._impl.xhrCore._AjaxUtils", "encodeSubmittableFields", e);
        //}
    },

    /**
     * checks recursively if contained in PPS
     * the algorithm is as follows we have an issuing item
     * the parent form of the issuing item and a set of child ids which do not
     * have to be inputs, we scan now for those ids and all inputs which are childs
     * of those ids
     *
     * Now this algorithm is up for discussion because it is relatively complex
     * but for now we will leave it as it is.
     *
     * @param {Node} node - the root node of the partial page submit  (usually the form)
     * @param {boolean} submitAll - if set to true, all elements within this node will
     * be added to the partial page submit
     * @param {Array} partialIds - an array of partial ids which should be used for the submit
     * @param {Object} targetBuf a target string buffer which receives the encoded elements
     */
    encodePartialSubmit : function(node,  submitAll,
                                   partialIds, targetBuf) {
        var _Lang = myfaces._impl._util._Lang;
        var _Impl = myfaces._impl.core.Impl;
        var _Dom = myfaces._impl._util._Dom;

        var partialIdsFilter = function(curNode) {
            if (curNode.nodeType != 1) return false;
            if (submitAll && node != curNode) return true;

            var id = curNode.id || curNode.name;

            return (id && _Lang.contains(partialIds, id)) || id == _Impl.P_VIEWSTATE;
        };

        //shallow scan because we have a second scanning step, to find the encodable childs of
        //the result nodes, that way we can reduce the number of nodes
        var nodes = _Dom.findAll(node, partialIdsFilter, false);

        var allowedTagNames = {"input":true, "select":true, "textarea":true};

        if (nodes && nodes.length) {
            for (var cnt = 0; cnt < nodes.length; cnt++) {
                //we can shortcut the form any other nodetype
                //must get a separate investigation
                var subNodes = (nodes[cnt].tagName.toLowerCase() == "form") ?
                        node.elements :
                        _Dom.findByTagNames(nodes[cnt], allowedTagNames, true);

                if (subNodes && subNodes.length) {
                    for (var cnt2 = 0; cnt2 < subNodes.length; cnt2++) {
                        this.encodeElement(subNodes[cnt2], targetBuf);
                    }
                } else {
                    this.encodeElement(nodes[cnt], targetBuf);
                }
            }
        }

        this.appendViewState(node, targetBuf);
    },

    /**
     * appends the viewstate element if not given already
     *
     * @param parentNode
     * @param targetBuf
     *
     * TODO dom level2 handling here, for dom level2 we can omit the check and readd the viewstate
     */
    appendViewState: function(parentNode, targetBuf) {
        var _Dom = myfaces._impl._util._Dom;
        var _Impl = myfaces._impl.core.Impl;

        //viewstate covered, do a preemptive check
        if (targetBuf.hasKey(_Impl.P_VIEWSTATE)) return;

        var viewStates = _Dom.findByName(parentNode, _Impl.P_VIEWSTATE, true);
        if (viewStates && viewStates.length) {
            for (var cnt2 = 0; cnt2 < viewStates.length; cnt2++) {
                this.encodeElement(viewStates[cnt2], targetBuf);
            }
        }
    },

    /**
     * appends the issuing item if not given already
     * @param item
     * @param targetBuf
     */
    appendIssuingItem: function (item, targetBuf) {
        // if triggered by a Button send it along
        if (item && item.type && item.type.toLowerCase() == "submit") {
            targetBuf.append(item.name, item.value);
        }
    },


    /**
     * encodes a single input element for submission
     *
     * @param {Node} element - to be encoded
     * @param {} targetBuf - a target array buffer receiving the encoded strings
     */
    encodeElement : function(element, targetBuf) {

        //browser behavior no element name no encoding (normal submit fails in that case)
        //https://issues.apache.org/jira/browse/MYFACES-2847
        if (!element.name) {
            return;
        }

        var _RT = myfaces._impl.core._Runtime;
        var name = element.name;
        var tagName = element.tagName.toLowerCase();
        var elemType = element.type;
        if (elemType != null) {
            elemType = elemType.toLowerCase();
        }

        // routine for all elements
        // rules:
        // - process only inputs, textareas and selects
        // - elements muest have attribute "name"
        // - elements must not be disabled
        if (((tagName == "input" || tagName == "textarea" || tagName == "select") &&
                (name != null && name != "")) && !element.disabled) {

            // routine for select elements
            // rules:
            // - if select-one and value-Attribute exist => "name=value"
            // (also if value empty => "name=")
            // - if select-one and value-Attribute don't exist =>
            // "name=DisplayValue"
            // - if select multi and multple selected => "name=value1&name=value2"
            // - if select and selectedIndex=-1 don't submit
            if (tagName == "select") {
                // selectedIndex must be >= 0 sein to be submittet
                if (element.selectedIndex >= 0) {
                    var uLen = element.options.length;
                    for (var u = 0; u < uLen; u++) {
                        // find all selected options
                        //var subBuf = [];
                        if (element.options[u].selected) {
                            var elementOption = element.options[u];
                            targetBuf.append(name, (elementOption.getAttribute("value") != null) ?
                                    elementOption.value : elementOption.text);
                        }
                    }
                }
            }

            // routine for remaining elements
            // rules:
            // - don't submit no selects (processed above), buttons, reset buttons, submit buttons,
            // - submit checkboxes and radio inputs only if checked
            if ((tagName != "select" && elemType != "button"
                    && elemType != "reset" && elemType != "submit" && elemType != "image")
                    && ((elemType != "checkbox" && elemType != "radio") || element.checked)) {
                if ('undefined' != typeof element.files && element.files != null && _RT.getXHRLvl() >= 2 && element.files.length) {
                    //xhr level2
                    targetBuf.append(name, element.files[0]);
                } else {
                    targetBuf.append(name, element.value);
                }
            }

        }
    }
});