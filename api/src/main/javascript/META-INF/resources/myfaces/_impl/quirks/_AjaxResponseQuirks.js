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
 * Ajax Response overrides for IE quirks mode
 *
 */
_MF_SINGLTN(_PFX_XHR + "_AjaxResponseQuirks", myfaces._impl.xhrCore._AjaxResponse, /** @lends myfaces._impl.xhrCore._AjaxResponse.prototype */ {
    constructor_: function() {
        this._callSuper("constructor_");
        myfaces._impl.xhrCore._AjaxResponse = this;
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
            _Dom = this._Dom,
            _RT = this._RT,
            isWebkit = this._RT.browser.isWebKit,
            //we have to work around an xml parsing bug in Webkit
            //see https://issues.apache.org/jira/browse/MYFACES-3061
            doc = (!isWebkit) ? _Lang.parseXML(newData) : null,
            newHead = null;

        if (!isWebkit && _Lang.isXMLParseError(doc)) {
            doc = _Lang.parseXML(newData.replace(/<!\-\-[\s\n]*<!\-\-/g, "<!--").replace(/\/\/-->[\s\n]*\/\/-->/g, "//-->"));
        }

        if (isWebkit || _Lang.isXMLParseError(doc)) {
            //the standard xml parser failed we retry with the stripper
            var parser = new (this._RT.getGlobalConfig("updateParser", myfaces._impl._util._HtmlStripper))();
            var headData = parser.parse(newData, "head");
            //We cannot avoid it here, but we have reduced the parsing now down to the bare minimum
            //for further processing
            newHead = _Lang.parseXML("<head>" + headData + "</head>");
            //last and slowest option create a new head element and let the browser
            //do its slow job
            if (_Lang.isXMLParseError(newHead)) {
                try {
                    newHead = _Dom.createElement("head");
                    newHead.innerHTML = headData;
                } catch (e) {
                    //we give up no further fallbacks
                    throw this._raiseError(new Error(), "Error head replacement failed reason:" + e.toString(), "_replaceHead");
                }
            }
            newHead = newHead.childNodes[0];
        } else {
            //parser worked we go on
            newHead = doc.getElementsByTagName("head")[0];
        }

        var oldTags = _Lang.objToArray(document.head.childNodes);


        _Dom.deleteItems(_Lang.objToArray(oldTags));
        _Dom.appendToHead(newHead);


        return document.head;
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
            _Lang = this._Lang,

            oldBody = document.getElementsByTagName("body")[0],
            placeHolder = document.createElement("div"),
            isWebkit = _RT.browser.isWebKit;

        placeHolder.id = "myfaces_bodyplaceholder";

        _Dom._removeChildNodes(oldBody);
        oldBody.innerHTML = "";
        oldBody.appendChild(placeHolder);

        var bodyData, doc = null, parser;

        //we have to work around an xml parsing bug in Webkit
        //see https://issues.apache.org/jira/browse/MYFACES-3061
        if (!isWebkit) {
            doc =  _Lang.parseXML(newData);
        }

        if (!isWebkit && _Lang.isXMLParseError(doc)) {
            doc = _Lang.parseXML(newData.replace(/<!\-\-[\s\n]*<!\-\-/g, "<!--").replace(/\/\/-->[\s\n]*\/\/-->/g, "//-->"));
        }

        if (isWebkit || _Lang.isXMLParseError(doc)) {
            //the standard xml parser failed we retry with the stripper

            parser = new (_RT.getGlobalConfig("updateParser", myfaces._impl._util._HtmlStripper))();

            bodyData = parser.parse(newData, "body");
        } else {
            //parser worked we go on
            var newBodyData = doc.getElementsByTagName("body")[0];

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
        }
        //we cannot serialize here, due to escape problems
        //we must parse, this is somewhat unsafe but should be safe enough
        parser = new (_RT.getGlobalConfig("updateParser", myfaces._impl._util._HtmlStripper))();
        bodyData = parser.parse(newData, "body");

        var returnedElement = this.replaceHtmlItem(request, context, placeHolder, bodyData);

        if (returnedElement) {
            this._pushOperationResult(context, returnedElement);
        }
        return returnedElement;
    },
});
