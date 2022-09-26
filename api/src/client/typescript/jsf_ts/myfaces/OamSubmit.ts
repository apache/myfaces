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

import {DQ, Stream} from "mona-dish";

/**
 * legacy code to enable various aspects
 * of myfaces, used to be rendered inline
 * for jsf 2.0 we can externalize it into its own custom resource
 *
 * note this is a straight 1:1 port from the existing codebase
 * (not too much work has been spent here, the important thing is, that
 * the namespace and functions need to be kept intact for legacy code)
 *
 * we might move the code over in the future, but for now a straight 1:1 port suffices
 */
declare const window: any;
declare const myfaces: any;

export module oam {
    /**
     * sets a hidden input field
     * @param formName the formName
     * @param name the hidden field
     * @param value the value to be rendered
     */
    export const setHiddenInput = function (formName: string, name: string, value: string) {
        DQ.byId(document.forms[formName])
            .each(form => {
                const input = form.querySelectorAll(`input[type='hidden'][name='${name}']`);
                if (input.isPresent()) {
                    input.inputValue.value = value;
                } else {
                    const newInput = DQ.fromMarkup(`<input type='hidden' id='${name}' name='${name}'>`);
                    newInput.inputValue.value = value;
                    newInput.appendTo(form);
                }
            });
    };

    /**
     * clears a hidden input field
     *
     * @param formName formName for the input
     * @param name the name of the input field
     */
    export const clearHiddenInput = function (formName: string, name: string) {
        let element = document.forms?.[formName]?.elements?.[name];
        if(!element) {
            return;
        }
        DQ.byId(element).delete();
    };

    // noinspection JSUnusedGlobalSymbols
    /**
     * does special form submit remapping
     * re-maps the issuing command link into something,
     * a decode of the command link on the server can understand
     *
     * @param formName
     * @param linkId
     * @param target
     * @param params
     */
    export const submitForm = function (formName: string, linkId: string, target: string, params: { [key: string]: any }) {
        let clearFn = 'clearFormHiddenParams_' + formName.replace(/-/g, '\$:').replace(/:/g, '_');
        window?.[clearFn]?.(formName);

        //autoscroll code
        if (window?.myfaces?.core?.config?.autoScroll && window?.getScrolling) {
            myfaces.oam.setHiddenInput(formName, 'autoScroll', window?.getScrolling());
        }
        Stream.ofAssoc(params).each((param: [string, any]) => {
            myfaces.oam.setHiddenInput(formName, param[0], param[1]);
        });

        //we call the namespaced function, to allow decoration, via a direct call we would
        myfaces.oam.setHiddenInput(formName, `${formName}:_idcl`, linkId);

        DQ.byId(document.forms[formName]).each(form => {
            const ATTR_TARGET = "target";
            const formElement = form.getAsElem(0).value as HTMLFormElement;
            const oldTarget = form.attr(ATTR_TARGET).value;
            form.attr(ATTR_TARGET).value = target;

            const result = formElement?.onsubmit?.(null);

            try {
                if ((!!result) || 'undefined' == typeof result) {
                    formElement.submit();
                }
            } catch (e) {
                window?.console.error(e);
            }

            form.attr(ATTR_TARGET).value = oldTarget;
            Stream.ofAssoc(params).each((param: [string, any]) => {
                myfaces.oam.clearHiddenInput(formName, param[0]);
            });
            myfaces.oam.clearHiddenInput(formName, `${formName}:_idcl`);
        });
        return false;
    };
}