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
 * legacy code to enable various aspects
 * of myfaces, used to be rendered inline
 * for jsf 2.0 we can externalized it into its own custom resource
 */



/**
 * sets a hidden input field
 * @param formname the formName
 * @param name the hidden field
 * @param value the value to be rendered
 */
function oamSetHiddenInput(formname, name, value) {
    var form = document.forms[formname];
    if (typeof form == 'undefined') {
        form = document.getElementById(formname);
    }

    if (typeof form.elements[name] != 'undefined' && (form.elements[name].nodeName == 'INPUT' || form.elements[name].nodeName == 'input')) {
        form.elements[name].value = value;
    }
    else {
        var newInput = document.createElement('input');
        newInput.setAttribute('type', 'hidden');
        newInput.setAttribute('id', name);
        newInput.setAttribute('name', name);
        newInput.setAttribute('value', value);
        form.appendChild(newInput);
    }
}

/**
 * clears a hidden input field
 *
 * @param formname formName for the input
 * @param name the name of the input field
 * @param value the value to be cleared
 */
function oamClearHiddenInput(formname, name, value) {
    var form = document.forms[formname];

    if (typeof form == 'undefined') {
        form = document.getElementById(formname);
    }

    var hInput = form.elements[name];
    if (typeof hInput != 'undefined') {
        form.removeChild(hInput);
    }
}

/**
 * does special form submit remapping
 * remaps the issuing command link into something
 * the decode of the command link on the server can understand
 *
 * @param formName
 * @param linkId
 * @param target
 * @param params
 */
function oamSubmitForm(formName, linkId, target, params) {

    var clearFn = 'clearFormHiddenParams_' + formName.replace(/-/g, '\$:').replace(/:/g, '_');
    if (typeof window[clearFn] == 'function') {
        window[clearFn](formName);
    }

    var form = document.forms[formName];
    if (typeof form == 'undefined') {
        form = document.getElementById(formName);
    }

    //autoscroll code
    if (myfaces.core.config.autoScroll && typeof window.getScrolling != 'undefined') {
        oamSetHiddenInput(formName, 'autoScroll', getScrolling());
    }




    if (myfaces.core.config.ieAutoSave) {
        var agentString = navigator.userAgent.toLowerCase();
        var version = navigator.appVersion;
        if (agentString.indexOf('msie') != -1) {
            if (!(agentString.indexOf('ppc') != -1 && agentString.indexOf('windows ce') != -1 && version >= 4.0)) {
                window.external.AutoCompleteSaveForm(form);
            }
        }
    }

    var oldTarget = form.target;
    if (target != null) {
        form.target = target;
    }
    if ((typeof params != 'undefined') && params != null) {
        for (var i = 0, param; (param = params[i]); i++) {
            oamSetHiddenInput(formName, param[0], param[1]);
        }

    }

    oamSetHiddenInput(formName, formName + ':' + '_idcl', linkId);

    if (form.onsubmit) {
        var result = form.onsubmit();
        if ((typeof result == 'undefined') || result) {
            try {
                form.submit();
            }
            catch(e) {
            }
        }

    }
    else {
        try {
            form.submit();
        }
        catch(e) {
        }
    }

    form.target = oldTarget;
    if ((typeof params != 'undefined') && params != null) {

        for (var i = 0, param; (param = params[i]); i++) {
            oamClearHiddenInput(formName, param[0], param[1]);
        }

    }

    oamClearHiddenInput(formName, formName + ':' + '_idcl', linkId);
    return false;
}

//reserve a cofig namespace for impl related stuff
(!window.myfaces) ? window.myfaces = {} : null;
(!myfaces.core) ? myfaces.core = {} : null;
(!myfaces.core.config) ? myfaces.core.config = {} : null;

