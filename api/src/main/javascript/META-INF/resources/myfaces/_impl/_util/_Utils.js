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
 * Version: $Revision: 1.3 $ $Date: 2009/04/09 13:58:56 $
 *
 */

if(!window.myfaces) window.myfaces = {};
if(!myfaces.xhrCore) myfaces.xhrCore = {};

/**
 * Constructor
 */
myfaces._impl._util._Utils = function() {
	
    }

/**
 * [STATIC]
 * Run through the given Html item and execute the inline scripts
 * (IE doesn't do this by itself)
 * @param {HtmlElement} item
 */
myfaces._impl._util._Utils.runScripts = function(item) {
    if (item.nodeType == 1) { // only if it's an element node
        if (item.tagName.toLowerCase() == 'script') {
            try {
                var test = item.text;
                var go = true;
                while (go) {
                    go = false;
                    if (test.substring(0, 1) == " ") {
                        test = test.substring(1);
                        go = true;
                    }
                    if (test.substring(0, 4) == "<!--") {
                        test = test.substring(4);
                        go = true;
                    }
                    if (test.substring(0, 11) == "//<![CDATA[") {
                        test = test.substring(11);
                        go = true;
                    }
                }
                window.execScript(test); // run the script
            } catch (e) {
                myfaces._impl.xhrCore_Exception.throwNewError ("Utils", "runScripts", e);
            }
        } else {
            var child = item.firstChild;
            while (child) {
                myfaces._impl._util._Utils.runScripts(child);
                child = child.nextSibling;
            }
        }
    }
}

/**
 * Simple delete on an existing item
 */
myfaces._impl._util._Utils.deleteItem = function(itemIdToReplace) {
    var item = document.getElementById(itemIdToReplace);
    if (item == null) {
        myfaces._impl.xhrCore_Exception.throwNewWarning
        ("Utils", "deleteItem", "Unknown Html-Component-ID: " + itemIdToReplace);
        return;
    }

    item.parentNode.removeChild(item);
}

/**
 * [STATIC]
 * Replaces HTML elements through others
 * @param {String} itemIdToReplace - ID of the element to replace
 * @param {String} newTag - the new tag
 * @param {HTML Element} form - form element that is parent of the element
 */
myfaces._impl._util._Utils.replaceHtmlItem = function(itemIdToReplace, newTag, form) {
    try {
        var item = myfaces._impl._util._Utils.getElementFromForm(itemIdToReplace, form);
        if (item == null) {
            myfaces._impl.xhrCore_Exception.throwNewWarning
            ("Utils", "replaceHTMLItem", "Unknown Html-Component-ID: " + itemIdToReplace);
            return;
        }

        if (newTag != "") {
            if (typeof window.Range != 'undefined'
                && typeof Range.prototype.createContextualFragment == 'function') {
                var range = document.createRange();
                range.setStartBefore(item);
                var fragment = range.createContextualFragment(newTag);
                item.parentNode.insertBefore(fragment, item);
            } else {
                item.insertAdjacentHTML('beforeBegin', newTag);
            }
            if (myfaces._impl._util._Utils.isUserAgentInternetExplorer()) {
                myfaces._impl._util._Utils.runScripts(item.previousSibling);
            }
        }

        // and remove the old item
        item.parentNode.removeChild(item);
    } catch (e) {
        myfaces._impl.xhrCore_Exception.throwNewError ("Utils", "replaceHTMLItem", e);
    }
};


myfaces._impl._util._Utils.ieQuircksEvents = {
    "onabort": true,
    "onload":true,
    "onunload":true,
    "onchange": true,
    "onsubmit": true,
    "onreset": true,
    "onselect": true,
    "onblur": true,
    "onfocus": true,
    "onkeydown": true,
    "onkeypress": true,
    "onkeyup": true,
    "onclick": true,
    "ondblclick": true,
    "onmousedown": true,
    "onmousemove": true,
    "onmouseout": true,
    "onmouseover": true,
    "onmouseup": true
};

/**
 * bugfixing for ie6 which does not cope properly with setAttribute
 */
myfaces._impl._util._Utils.setAttribute = function(domNode, attribute, value) {

    if(!myfaces._impl._util._Utils.isUserAgentInternetExplorer()) {
       domNode.setAttribute(attribute,value);
       return;
    }
    //Now to the broken browsers IE6+.... ie7 and ie8 quirks mode


    //now ie has the behavior of not wanting events to be set directly and also
    //class must be renamed to classname
    //according to http://www.quirksmode.org/dom/w3c_core.html it does not set styles
    //also class == className we have to rechange that
    //additionally events are not triggered as well

    //what we do is following for now:
    //1. remap the class
    attribute = attribute.toLowerCase();

    if(attribute === "class") {
        domNode["className"] = value;
    } else if(attribute === style) {
        //We have to split the styles here and assign them one by one
        var styleEntries = value.split(";");
        for(var loop = 0; loop < styleEntries.length; loop++) {
            var keyVal = styleEntries[loop].split(":");
            domNode["style"][keyVal[0]] = keyVal[1];
        }
    } else {
        //check if the attribute is an event, since this applies only
        //to quirks mode of ie anyway we can live with the standard html4/xhtml
        //ie supported events
        if(myfaces._impl._util._Utils.ieQuircksEvents[attribute]) {
            if(myfaces._impl._util._LangUtils.isString(attribute)) {
                domNode[attribute] = function(event) {
                    eval(attribute);
                };
            }
        }
        domNode[attribute] = value;
    }
    //TODO this needs further testing I will leave it for now...
};

/**
 * [STATIC]
 * Determines whether the user agent is IE or not
 * @return {boolean} - true if it is IE
 */
myfaces._impl._util._Utils.isUserAgentInternetExplorer = function() {
    return window.ActiveXObject;
};

/**
 * [STATIC]
 * gets an element from a form with its id -> sometimes two elements have got
 * the same id but are located in different forms -> MyFaces 1.1.4 two forms ->
 * 2 inputHidden fields with ID jsf_tree_64 & jsf_state_64 ->
 * http://www.arcknowledge.com/gmane.comp.jakarta.myfaces.devel/2005-09/msg01269.html
 * @param {String} itemId - ID of the HTML element located inside the form
 * @param {Html-Element} form - form element containing the element
 * @return {Html-Element} - return the element if found else null
 */
myfaces._impl._util._Utils.getElementFromForm = function(itemId, form) {
    try {
        if('undefined' == typeof form || form == null) {
            return document.getElementById(itemId);
        }

        var fLen = form.elements.length;
        for ( var f = 0; f < fLen; f++) {
            var element = form.elements[f];
            if (element.id != null && element.id == itemId) {
                return element;
            }
        }
        // element not found inside the form -> try document.getElementById
        // (kann be null if element doesn't exist)
        return document.getElementById(itemId);
    } catch (e) {
        myfaces._impl.xhrCore_Exception.throwNewError ("Utils", "getElementFromForm", e);
    }
};

/**
 * [STATIC]
 * gets a parent of an item with a given tagname
 * @param {HtmlElement} item - child element
 * @param {String} parentName - TagName of parent element
 */
myfaces._impl._util._Utils.getParent = function(item, parentName) {
    try {
        // parent tag parentName suchen
        var parentItem = item.parentNode;
        while (parentItem != null
            && parentItem.tagName.toLowerCase() != parentName) {
            parentItem = parentItem.parentNode;
        }
        if (parentItem != null) {
            return parentItem;
        } else {
            myfaces._impl.xhrCore_Exception.throwNewWarning
            ("Utils", "getParent", "The item has no parent with type <" + parentName + ">");
            return null;
        }
    } catch (e) {
        myfaces._impl.xhrCore_Exception.throwNewError ("Utils", "getParent", e);
    }
};

/**
 * [STATIC]
 * gets the child of an item with a given tagname
 * @param {HtmlElement} item - parent element
 * @param {String} childName - TagName of child element
 * @param {String} itemName - name-Attribut the child can have (can be null)
 */
myfaces._impl._util._Utils.getChild = function(item, childName, itemName) {
    var childItems = item.childNodes;
    for ( var c = 0, cLen = childItems.length; c < cLen; c++) {
        if (childItems[c].tagName != null
            && childItems[c].tagName.toLowerCase() == childName
            && (itemName == null || (itemName != null && itemName == childItems[c]
                .getAttribute("name")))) {
            return childItems[c];
        }
    }
    return null;
}

/**
 * helper regexp and funtion to strip the body content from full html
 * by providing checks for " and ' we force the regexp
 * to check only for the first body element which also must
 * be outside of a string definition (hence
 * body tags inside of javascript strings cannot interfere into
 * well formed html)
 */
myfaces._impl._util._Utils.getChild._bodyStripper = /[^\"\']*<\s*body[^>]*> (.*) <\/\s*body[^>]*> [^\"\']*/i;
myfaces._impl._util._Utils.getChild.getBodyContent = function(content) {

    if('undefined' == typeof content || null == typeof content) return null;
    myfaces._impl._util._Utils.getChild._bodyStripper.exec(content);

    var result = RegExp.$1;
    if('undefined' == typeof result || result == "") {
        return content;
    }
    return result;
};

 

