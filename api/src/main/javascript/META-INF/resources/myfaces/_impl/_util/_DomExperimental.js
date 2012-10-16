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
if (_MF_SINGLTN) {
    _MF_SINGLTN(_PFX_UTIL + "_DomExperimental", myfaces._impl._util._Dom, /** @lends myfaces._impl._util._Dom.prototype */ {
        constructor_:function () {
            this._callSuper("constructor_");
            myfaces._impl._util._Dom = this;
        },

        /**
         * The function allows to fetch the windowid from any arbitrary elements
         * parent or child form.
         * If more than one unique windowid is found then an error is thrown.
         *
         * @param {optional String|DomNode} node, searches for the windowid from any given arbitrary element
         * a search  for embedded forms is performed. If the node parameter is omitted, a search on document.forms
         * will be performed and if that
         * does not bring any result a search within the url will be performed as last fallback.
         *
         * @throws an error in case of having more than one unique windowIds depending on the given
         * node element
         */
        getWindowId:function (/*optional String|DomNode*/node) {

            var FORM = "form";

            var fetchWindowIdFromForms = function (forms) {
                var result_idx = {};
                var result;
                var foundCnt = 0;
                for (var cnt = forms.length - 1; cnt >= 0; cnt--) {
                    var UDEF = 'undefined';
                    var currentForm = forms[cnt];
                    var windowId = currentForm["javax.faces.WindowId"] && currentForm["javax.faces.WindowId"].value;
                    if (UDEF != typeof windowId) {
                        if (foundCnt > 0 && UDEF == typeof result_idx[windowId]) throw Error("Multiple different windowIds found in document");
                        result = windowId;
                        result_idx[windowId] = true;
                        foundCnt++;
                    }
                }
                return result;
            }

            var getChildForms = function (currentElement) {
                //Special condition no element we return document forms
                //as search parameter, ideal would be to
                //have the viewroot here but the frameworks
                //can deal with that themselves by using
                //the viewroot as currentElement
                if (!currentElement) {
                    return document.forms;
                }

                var targetArr = [];
                if (!currentElement.tagName) return [];
                else if (currentElement.tagName.toLowerCase() == FORM) {
                    targetArr.push(currentElement);
                    return targetArr;
                }

                //if query selectors are supported we can take
                //a non recursive shortcut
                if(currentElement.querySelectorAll) {
                    return currentElement.querySelectorAll(FORM);
                }

                //old recursive way, due to flakeyness of querySelectorAll
                for (var cnt = currentElement.childNodes.length - 1; cnt >= 0; cnt--) {
                    var currentChild = currentElement.childNodes[cnt];
                    targetArr = targetArr.concat(getChildForms(currentChild, FORM));
                }
                return targetArr;
            }

            var fetchWindowIdFromURL = function () {
                var href = window.location.href;
                var windowId = "windowId";
                var regex = new RegExp("[\\?&]" + windowId + "=([^&#\\;]*)");
                var results = regex.exec(href);
                //initial trial over the url and a regexp
                if (results != null) return results[1];
                return null;
            }
            var finalNode = (node && (typeof node == "string" || node instanceof String)) ?
                    document.getElementById(node) : (node || null);

            var forms = getChildForms(finalNode);
            var result = fetchWindowIdFromForms(forms);
            return (null != result) ? result : fetchWindowIdFromURL();
        },

        html5FormDetection:function (item) {
            var browser = this._RT.browser;
            //ie shortcut, not really needed but speeds things up
            if (browser.isIEMobile && browser.isIEMobile <= 8) {
                return null;
            }
            var elemForm = this.getAttribute(item, "form");
            return (elemForm) ? this.byId(elemForm) : null;
        },

        isMultipartCandidate:function (executes) {
            if (this._Lang.isString(executes)) {
                executes = this._Lang.strToArray(executes, /\s+/);
            }

            for (var cnt = 0, len = executes.length; cnt < len ; cnt ++) {
                var element = this.byId(executes[cnt]);
                var inputs = this.findByTagName(element, "input", true);
                for (var cnt2 = 0, len2 = inputs.length; cnt2 < len2 ; cnt2++) {
                    if (this.getAttribute(inputs[cnt2], "type") == "file") return true;
                }
            }
            return false;
        }
    });
}
