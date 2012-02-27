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
         * @param {optional} element, searches for the windowid from any given arbitrary element
         * a search first for embedded forms is performed and then for the parent form. If no form is found at
         * all, or the element is not given then a search on document.forms is performed and if that
         * does not bring any result a search within the url is performed as last fallback.
         * @throws an error in case of having more than one unique windowIds depending on the given
         * node element
         */
        getWindowId:function (node) {

            var FORM = "form";

            var fetchWindowIdFromForms = function (forms) {
                var result_idx = {};
                var result;
                var foundCnt = 0;
                for (var cnt = forms.length - 1; cnt >= 0; cnt--) {
                    var currentForm = forms[cnt];
                    var windowId = currentForm["javax.faces.WindowId"] && currentForm["javax.faces.WindowId"].value;
                    if ('undefined' != typeof windowId) {
                        if (foundCnt > 0 && 'undefined' == typeof result_idx[windowId]) throw Error("Multiple different windowIds found in document");
                        result = windowId;
                        result_idx[windowId] = true;
                        foundCnt++;
                    }
                }
                return result;
            }

            var getChildForms = function (currentElement) {

                var targetArr = [];
                if(!currentElement.tagName) return [];
                else if (currentElement.tagName.toLowerCase() == FORM) {
                    targetArr.push(currentElement);
                    return targetArr;
                }
                //old recursive way, due to flakeyness of querySelectorAll
                for (var cnt = currentElement.childNodes.length - 1; cnt >= 0; cnt--) {
                    var currentChild = currentElement.childNodes[cnt];
                    targetArr = targetArr.concat(getChildForms(currentChild, FORM));
                }
                return targetArr;
            }

            var findParentForms = function(element) {
                while(element != null) {
                    if(element.tagName.toLowerCase() == FORM) return [element];
                    element = element.parentNode;
                }
                return document.forms;
            }

            var fetchWindowIdFromURL = function() {
                var href = window.location.href;
                var windowId = "windowId";
                var regex = new RegExp("[\\?&]" + windowId + "=([^&#\\;]*)");
                var results = regex.exec(href);
                //initial trial over the url and a regexp
                if (results != null) return results[1];
                return null;
            }
            var forms = [];
            if(node) {
                var forms = getChildForms(node);
                if(!forms || forms.length == 0) {
                    //We walk up the parent until we hit a form or document.body
                    forms = findParentForms(node);
                }
            }
            var result = fetchWindowIdFromForms(forms);
            return (null != result)? result:  fetchWindowIdFromURL();
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

            for (var executable in executes) {
                if (!executes.hasOwnProperty(executable)) continue;
                var element = this.byId(executes[executable]);
                var inputs = this.findByTagName(element, "input", true);
                for (var key in inputs) {
                    if (!inputs.hasOwnProperty(key)) continue;
                    if (this.getAttribute(inputs[key], "type") == "file") return true;
                }
            }
            return false;
        }
    });
}
