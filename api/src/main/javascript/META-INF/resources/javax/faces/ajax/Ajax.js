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
 *MyFaces core javascripting libraries
 */

/**
 *
 * reserve the root namespace
 */
if('undefined' != typeof OpenAjax && ('undefined' == typeof javax || null == typeof javax)) {
    OpenAjax.hub.registerLibrary("javax", "www.sun.com", "1.0", null);
}

if('undefined' == typeof javax.faces || null == javax.faces ) {
    javax.faces = new Object();
}

if('undefined' == typeof javax.faces.Ajax || null == javax.faces.Ajax ) {
    javax.faces.Ajax = new function() {};
}

/**
 * collect and encode data for a given form element (must be of type form)
 * find the javax.faces.ViewState element and encode its value as well!
 * return a concatenated string of the encoded values!
 */
javax.faces.Ajax.viewState = function(formElement) {
    //Do typechecking here!
    if(formElement.nodeValue != "form") {
        throw Exception("javax.faces.Ajax.viewState: param value not of type form!!!");
    }
    /*
     * TODO #60
     * https://issues.apache.org/jira/browse/MYFACES-2110
     */
}



