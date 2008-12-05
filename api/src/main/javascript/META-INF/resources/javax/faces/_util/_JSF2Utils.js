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
 *
 * reserve the root namespace for myfaces
 */
if('undefined' != typeof OpenAjax && ('undefined' == typeof org || 'undefined' == typeof org.apache || 'undefined' == typeof org.apache.myfaces ||
                                        null == org || null == org.apache || null == org.apache.myfaces) ) {
    OpenAjax.hub.registerLibrary("org.apache.myfaces", "myfaces.apache.org", "2.0", null);
}

if('undefined' == typeof(org.apache.myfaces._JSF2Utils) || null == org.apache.myfaces._JSF2Utils) {
    org.apache.myfaces._JSF2Utils = function() {
        var _underTest = false;

        isUnderTest = function() {
            return this._underTest;
        }
    }  
}

