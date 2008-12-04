/* 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
/**
 * A test mockup to support
 * please do not use this file in production
 */



if('undefined' == typeof ( window) || null == window) {
    var window = new Object();
}

if('undefined' == typeof (window.document)  || null == window.document) {
    window.document = new Object();
}

if('undefined' == typeof document  || null == document) {
    var document = window.document;
    document._idCnt = 0;
    document.elements = [];
    /*for internal handling*/
    document._elementIndex = new Object();

    /**
     *Simulation of the create element
     *functionality
     */
    document.createElement = function(nodeType) {
        var element = new Object();
        element.nodeName = nodeType;
        element.children = [];
        element.id = document._idCnt++;
        return element;
    }
    /**
     *Simulation of the create textnode
     *functionality
     */
    document.createTextNode = function(text) {
        var element = new Object();
        element.innerHTML = text;
        element.nodeName = "text";
        element.children = [];
        element.id = document._idCnt++;
        return element;
    }
    document.getElementById = function(id) {
        //TODO implement this
    }

    document._getElementById = function(id,node) {
        //TODO implement this
    }
}



if('undefined' == typeof org  || null == org) {
    var org = new Object();
    document.org = org;
}

if('undefined' == typeof javax  || null == javax) {
    var javax = new Object();
    document.javax = javax;
}




/**
 * we have to emulate openajax as well since
 * it ties the namespaces to the windows explicitely
 * we tie them simply into an array
 * and get the same behavior
 */
var OpenAjax = new Object();

OpenAjax.hub = new Object();

OpenAjax.hub.registerLibrary = function(prefix, nsURL, version, extra) {
    var namespaces = prefix.split(".");

    var root = document;
    for(var cnt = 0; cnt < namespaces.length; cnt ++) {
        var curVal = namespaces[cnt];
        if('undefined' == root[curVal] || null == root[curVal]) {
            root[curVal] = new Object();
        }
        root = root[curVal];
    }
}
