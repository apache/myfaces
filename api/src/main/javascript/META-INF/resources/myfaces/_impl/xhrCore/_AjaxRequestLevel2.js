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
 * an implementation of an xhr level2 object
 * doing all needed operations (aka FormElement, input type submit handling,
 * progress notifications)
 *
 * NOTE this is alpha code since the browsers emit currently insufficient
 * multipart data, this will be enabled as soon as it is possible
 * 
 * Author: Werner Punz (latest modification by $Author: werpu $)
 * Version: $Revision: 1.4 $ $Date: 2009/05/31 09:16:44 $
 */

myfaces._impl.core._Runtime.extendClass("myfaces._impl.xhrCore._AjaxRequestLevel2", myfaces._impl.xhrCore._AjaxRequest, {

    constructor_: function(arguments) {
        this._callSuper("constructor", arguments);
        //this._contentType = "multipart/form-data; boundary=---------------------------168072824752491622650073 ";
    },

    /**
     * internal callback from AjaxRequest._startXHR
     * at this stage the xhr object has been initialized last
     * post finish works like additional listeners can be registered
     */
    _preSend: function() {
        this._callSuper("_preSend");
    },

    /**
     * xhr level2 optimized getViewState element
     */
    getViewState: function() {
        var _Lang = myfaces._impl._util._Lang;
        var _ret;
        if (!this._partialIdsArray || this._partialIdsArray.length == 0) {
            //Standard case we have only the normal form issued
            //var getFormDataPresent = this._sourceForm.getFormData;
            _ret = _Lang.createFormDataDecorator(new FormData()); 
            //_ret = _Lang.createFormDataDecorator((getFormDataPresent) ? this._sourceForm.getFormData() : new FormData());
            if (true) {
                this._ajaxUtil.encodeSubmittableFields(_ret, this._xhr, this._context, this._source,
                        this._sourceForm, this._partialIdsArray);
            }
            this._ajaxUtil.appendIssuingItem(this._source, _ret);
        } else {
            _ret = _Lang.createFormDataDecorator(new FormData());
            this._ajaxUtil.encodeSubmittableFields(_ret, this._xhr, this._context, this._source,
                    this._sourceForm, this._partialIdsArray);
        }
        return _ret;
    }
});

