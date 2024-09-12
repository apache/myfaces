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
 * this method is used only for pure multipart form parts
 * otherwise the normal method is used
 * IT is a specialized request which uses the form data
 * element for the handling of forms
 */
_MF_CLS(_PFX_XHR+"_AjaxRequestLevel2", myfaces._impl.xhrCore._AjaxRequest, {

    _sourceForm: null,

    constructor_: function(arguments) {
        this._callSuper("constructor_", arguments);
        //TODO xhr level2 can deal with real props

    },

    getFormData: function() {
        var ret;
        if (!this._partialIdsArray || this._partialIdsArray.length == 0) {
            ret = new FormData(this._sourceForm);
        } else {
            //for speed reasons we only need encodesubmittablefields
            //in the pps case
            ret = new FormData(this._sourceForm);
            this._AJAXUTIL.encodeSubmittableFields(ret, this._xhr, this._context, this._source,
                    this._sourceForm, this._partialIdsArray);
            if(this._source && !this._isBehaviorEvent()) {
                this._AJAXUTIL.appendIssuingItem(this._source, ret);
            }
        }
        return ret;
    },

    _formDataToURI: function() {
        //i assume in xhr level2 form data takes care of the http get parametrisation
        return "";
    },

    _getTransport: function() {
        return new XMLHttpRequest();
    }

});