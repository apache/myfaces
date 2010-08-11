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
 *
 * Abstract base request encapuslating all methods and variables
 * shared over all different request objects
 *
 * Author: Werner Punz (latest modification by $Author: ganeshpuri $)
 * Version: $Revision: 1.4 $ $Date: 2009/05/31 09:16:44 $
 */
/** @namespace myfaces._impl.xhrCore._AjaxRequest */
myfaces._impl.core._Runtime.extendClass("myfaces._impl.xhrCore._BaseRequest", Object, {

    _Dom: myfaces._impl._util._Dom,
    _Lang: myfaces._impl._util._Lang,
    _RT: myfaces._impl.core._Runtime,

    _contentType: "application/x-www-form-urlencoded",
    _source: null,
    _xhr: null,
    _encoding:null ,

    _context:null,
    _ajaxUtil: null,
    _sourceForm: null,
    _passThrough: null,
    _requestParameters: null,
    _exception: null,
    _timeout: null,
    _delay:null,
    _queueSize:-1,

    _partialIdsArray : null,
    //callbacks for onDone... done issues
    //onSuccess everything has passed through
    //onError server side error

    //onException exception thrown by the client
    //onWarning warning issued by the client
    _onDone : null,
    _onSuccess: null,
    _onError: null,
    _onException: null,
    _onWarning: null,
    _onTimeout:null,

    /*response object which is exposed to the queue*/
    _response: null,

    _timeoutId: null,

    /*all instance vars can be set from the outside
     * via a parameter map*/
    _ajaxType: "POST",

    /*
     * constants used internally
     */
    _CONTENT_TYPE:"Content-Type",
    _HEAD_FACES_REQ:"Faces-Request",

    _READY_STATE_DONE: 4,
    _STATUS_OK_MINOR: 200,
    _STATUS_OK_MAJOR: 300,

    _VAL_AJAX: "partial/ajax",


    /**
     * ie6 cleanup
     */
    _finalize: function() {
        if(!this._RT.browser.isIE) {
            //no ie, no broken garbage collector
            return;
        }
        var resultArr = [];
        for(var key in this) {
            if(key != "_finalize" && key != "callback" && key.indexOf("_") == 0) {
                resultArr.push(key);
            }
        }
        //ie has a problem with its gc it cannot cleanup
        //circular references between javascript and com
        //to the worse every xhr object is a com object
        //and some but not all inpucd Tcd cdt elements as well
        //i cannot fix all mem leaks but at least I can
        //reduce them , I cannot help with event handlers set
        //on dom elements replaced, but ie7 gcs them at least
        //during the window unload phase
        for(var cnt = 0; cnt < resultArr.length; cnt++) {
            if(this[resultArr[cnt]])
            delete this[resultArr[cnt]];
        }

    },

    //abstract methods which have to be implemented
    //since we do not have abstract methods we simulate them
    //by using empty ones
    constructor_: function() {
        this._Lang = myfaces._impl._util._Lang;
        this._Dom = myfaces._impl._util._Dom;
    },

    /**
     * send the request
     */
    send: function() {
    },

    /**
     * the callback function after the request is done
     */
    callback: function() {

    },

    //non abstract ones
    /**
     * Spec. 13.3.1
     * Collect and encode input elements.
     * Additionally the hidden element javax.faces.ViewState
     *
     *
     * @return {FormDataWrapper} - an element of formDataWrapper
     * which keeps the final Send Representation of the
     */
    getViewState : function() {
        var ret = this._Lang.createFormDataDecorator(new Array());

        this._ajaxUtil.encodeSubmittableFields(ret, this._xhr, this._context, this._source,
                this._sourceForm, this._partialIdsArray);
       
        return ret;
    }
});