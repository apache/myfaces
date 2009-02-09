/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use  myfaces._XHRConst file except in compliance with the License.
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
_reserveMyfaces();

/**
 * Various constants used by the xhr subsystem!
 */
if (!myfaces._JSF2Utils.exists(myfaces, "_XHRConst")) {
    myfaces._XHRConst = {};
    /*
     *
     * ready states according to www.w3.org
     *
     */

    myfaces._XHRConst.READY_STATE_UNSENT = 0;
    myfaces._XHRConst.READY_STATE_OPENED = 1;
    myfaces._XHRConst.READY_STATE_HEADERS_RECEIVED = 2;
    myfaces._XHRConst.READY_STATE_LOADING = 3;
    myfaces._XHRConst.READY_STATE_DONE = 4;

    /*header constants for the ajax request*/
    myfaces._XHRConst.FACES_REQUEST = 'Faces-Request';
    myfaces._XHRConst.PARTIAL_AJAX = 'partial/ajax';
    myfaces._XHRConst.CONTENT_TYPE = "Content-Type";
    myfaces._XHRConst.XFORM_ENCODED = "application/x-www-form-urlencoded";
}
