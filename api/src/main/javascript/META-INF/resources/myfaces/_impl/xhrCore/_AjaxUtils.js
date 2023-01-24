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
_MF_SINGLTN(_PFX_XHR+"_AjaxUtils", _MF_OBJECT,
/** @lends myfaces._impl.xhrCore._AjaxUtils.prototype */
{

    NAMED_VIEWROOT: "namedViewRoot",
    NAMING_CONTAINER_ID: "myfaces.partialId",


    /**
     * determines fields to submit
     * @param {Object} targetBuf - the target form buffer receiving the data
     * @param {Node} parentItem - form element item is nested in
     * @param {Array} partialIds - ids fo PPS
     */
    encodeSubmittableFields : function(targetBuf,
                                       parentItem, partialIds) {
            if (!parentItem) throw "NO_PARITEM";
            if (partialIds ) {
                this.encodePartialSubmit(parentItem, false, partialIds, targetBuf);
            } else {
                // add all nodes
                var eLen = parentItem.elements.length;
                for (var e = 0; e < eLen; e++) {
                    this.encodeElement(parentItem.elements[e], targetBuf);
                } // end of for (formElements)
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

        var _RT = this._RT;
        var name = element.name;
        var tagName = element.tagName.toLowerCase();
        var elemType = element.type;
        var isMultiple = element.multiple;
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
                    for(var r = 0;r < element.files.length; r++){
                        targetBuf.append(name, element.files[r]);
                        // don't add any more files
                        if(!isMultiple){ 
                            break;
                        }
                    }
                   
                } else {
                    targetBuf.append(name, element.value);
                }
            }

        }
    },

    _$ncRemap: function(internalContext, containerId) {
        var namedVieRoot = internalContext[this.NAMED_VIEWROOT];
        var namingContainerId = internalContext[this.NAMING_CONTAINER_ID];
        if(!namedVieRoot || !namingContainerId) {
            return containerId
        }
        if(containerId.indexOf(namingContainerId) == 0) {
            return containerId;
        }
        return [namingContainerId, containerId].join("");
    },

    /**
     * determines the current naming container
     * and assigns it internally
     *
     * @param internalContext
     * @param formElement
     * @private
     */
    _assignNamingContainerData: function(internalContext, formElement, separatorChar) {
        const viewRootId = this._resolveViewRootId(formElement, separatorChar);

        if(!!viewRootId) {
            internalContext[this.NAMED_VIEWROOT] = true;
            internalContext[this.NAMING_CONTAINER_ID] = viewRootId;
        }
    },

    /**
     * resolve the viewRoot id in a naming container situation
     * (aka ViewState element name is prefixed)
     * @param form
     * @return a string (never null) which is either emtpy or contains the prefix for the ViewState
     * (including the separator)
     */
    _resolveViewRootId: function(form, separatorChar) /*string*/ {
        form = this._Dom.byId(form);
        var _t = this;
        var foundNames = this._Dom.findAll(form, function(node) {
            var name = node.getAttribute("name");
            if(!name || name.indexOf("jakarta.faces.ViewState") <= 0) {
                return false;
            }
            return node;
        }, true);
        if(!foundNames.length) {
            return "";
        }
        return foundNames[0].name.split(separatorChar, 2)[0] + separatorChar;
    },

    /**
     * as per jsdoc before the request it must be ensured that every post argument
     * is prefixed with the naming container id (there is an exception in mojarra with
     * the element=element param, which we have to follow here as well.
     * (inputs are prefixed by name anyway normally this only affects our standard parameters)
     * @private
     */
    _resoveConfigNamingContainerMapper: function(myfacesOptions, separatorChar) {
        var isNamedViewRoot = !!myfacesOptions[this.NAMED_VIEWROOT];
        if(!isNamedViewRoot) {
            return;
        }

        var partialId = myfacesOptions[this.NAMING_CONTAINER_ID];
        var prefix = partialId + this.getSeparatorChar();
        return function (data /*assoc array of key value pairs*/) {
            var ret = {};
            for(var key in data) {
                if(!data.hasOwnProperty(key)) {
                    continue;
                }
                ret[prefix + key] = data[key]
            }
            return ret;
        }
    }
});