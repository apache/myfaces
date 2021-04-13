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
 * A test mockup to support
 * please do not use this file in production
 */



if('undefined' == typeof ( window) || null == window) {
    var window = new Object();
}

if('undefined' == typeof (window.document)  || null == window.document) {
    window.document = new Object();
}




if('undefined' == typeof(node)) {
    var node = function(tagName, id) {
        this.nodeType = "tag";
        this.attributes = new Object(); /*associative array keeping the attributes list*/
        this.id = id;
        this.tagName = tagName;
        this.childNodes = [];
        this.nodeValue = null;
        this.parentNode = null;
        this._data = [];
        this.data = null;
        this.firstChild = null;
        this.lastChild = null;
        this.nextSibling = null;
        this.previousSibling = null;
        //TODO add supported node functions here

        /**
         * append child which does all the needed dom concatenation
         * on the appended child elements
         * its parents and siblings!
         */
        this.appendChild = function(/*Node*/ child) {
            this.childNodes.push(child);
            if(this.childNodes.length == 1) {
                this.firstChild = child;
            } else {
                this.childNodes[this.childNodes.length - 2].nextSibling = child;
                child.previousSibling = this.childNodes[this.childNodes.length - 2];
            }
            this.lastChild = child;
            child.parentNode = this;
        };

        /**
         * remove child function
         * which does all the internally defined cross concatenation
         * of a dom element!
         */
        this.removeChild = function(/*Node*/ child) {
            var childPos = -1;
            for(var cnt = 0; cnt < this.childNodes.length && childPos == -1; cnt ++) {
                if(this.childNodes[cnt] == child) {
                    childPos = cnt;
                }
            }
            if(childPos > -1) {
                if(childPos < this.childNodes.length - 1 && childPos > 0) {
                    this.childNodes[childPos-1].nextSibling = this.childNodes[childPos+1];
                    this.childNodes[childPos+1].previousSibling = this.childNodes[childPos-1];

                }
                else if(childPos == 0 && this.childNodes.length > 1) {
                    this.childNodes[1].previousSibling = null;
                } else if(childPos == this.childNodes.length - 1 &  this.childNodes.length > 1) {
                    this.childNodes[this.childNodes.length - 2].nextSibling = null;
                }
                this.childNodes.splice(childPos, 1);
            }
        };

        /**
         * implementation of our getElementsByTagName function
         */
        this.getElementsByTagName = function(/*String*/tagName) {
            return this._getElementsByTagName(this, tagName);
        };

        /**
         * implementation of our getElementsByTagName function
         * recursive helper to walk the subtree
         * in a proper manner!
         */
        this._getElementsByTagName = function(/*node*/ rootNode, /*String*/ tagName) {
            if('undefined' == typeof(rootNode) || null == rootNode || rootNode.childNodes.length == 0) {
                return [];
            }
            var resultArr = [];
            for(var cnt = 0; cnt < rootNode.childNodes.length; cnt ++) {
                var node = this.childNodes[cnt];
                if('undefined' != typeof (node.tagName) && null !=  node.tagName && node.tagName == tagName) {
                    resultArr.push(node);
                }
            }
            for(cnt = 0; cnt < rootNode.childNodes.length; cnt ++) {
                var foundResults = this._getElementsByTagName(rootNode.childNodes[cnt],tagName);
                if(foundResults.length > 0) {
                    resultArr = resultArr.concat(foundResults);
                }
            }

            return resultArr;
        };

        this._getElementById = function(/*String*/ id) {
            return this.__getElementById(this, id);
        };

        /**
         * helper for our document object
         */
        this.__getElementById = function(/*node*/ rootNode, /*String*/ id) {
            if('undefined' != typeof(rootNode) && null != rootNode && 'undefined' != typeof rootNode.id && null != rootNode.id
                && rootNode.id == id) {
                return rootNode;
            }
            if('undefined' == typeof(rootNode) || null == rootNode
                || 'undefined' == typeof(rootNode.childNodes) || null == rootNode.childNodes
                || rootNode.childNodes.length == 0) {
                return null;
            }
            for(var cnt = 0; cnt < this.childNodes.length; cnt ++) {
                var foundResult = rootNode.childNodes[cnt]._getElementById(id);
                if(foundResult != null) {
                    return foundResult;
                }
            }
            return null;
        };


        this.appendData = function(/*Object*/ dataVar) {
            this._data = dataVar;
            this.data = this._data.join("");
        };


        /**
         *getAttribute('attributeName') Returns the value of the specified attribute [not class, style or event handler]
         */
        this.getAttribute = function(/*String*/ attributeName) {
            if ('undefined' != typeof(this.attributes.attributeName) && null != this.attributes.attributeName) {
                return this.attributes.attributeName;
            }
            return null;
        };

        this.removeAttribute = function(/*String*/ attributeName) {
            var attr = this.getAttribute(attributeName);
            if ('undefined' != attr && null != attr) {
                var attrIndex = -1;
                /**/
                for(var cnt = 0; cnt < this.attributes.length && attrIndex == -1; cnt ++) {
                    if(attr == this.attributes[cnt]) {

                }
                }
            }
            return attr;
        };

        this.setAttribute = function(/*String*/name,/*String*/ value) {
            this.attributes.name = value;
        };
    };
}

if('undefined' == typeof document  || null == document) {
    var document = window.document;
    document._idCnt = 0;
    document.elements = [];
    /*for internal handling*/
    document._elementIndex = new Object();
    document.node = new node("document","document");
    /**
     *Simulation of the create element
     *functionality
     */
    document.createElement = function(nodeType) {
        return new node(nodeType, document._idCnt++);
    };
    /**
     *Simulation of the create textnode
     *functionality
     */
    document.createTextNode = function(text) {
        var element = new node("text",  document._idCnt++);
        element.nodeType="text";
        element.appendData(text);
        return element;
    };

    document.getElementById = function(id) {
        return this.node._getElementById(id);
    };

    /**
     *internal method only to be used
     *by testing environments!
     */
    document._reset = function() {
        this._idCnt = 0;
        this.elements = [];
        /*for internal handling*/
        this._elementIndex = new Object();
        this.node = new node("document","document");
    };

};

if('undefined' == typeof jakarta  || null == jakarta) {
    var jakarta = new Object();
    document.jakarta = jakarta;
}
