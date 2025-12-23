/*! Licensed to the Apache Software Foundation (ASF) under one or more
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

import {DQ} from "mona-dish";
import {ExtLang} from "../impl/util/Lang";

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
export module oam {
    const ofAssoc = ExtLang.ofAssoc;
    /**
     * sets a hidden input field
     * @param formName the formName
     * @param name the hidden field
     * @param value the value to be rendered
     */
    export const setHiddenInput = function (formName: string, name: string, value: string): void {
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
    export const clearHiddenInput = function (formName: string, name: string): void {
        let element = document.forms?.[formName]?.elements?.[name];
        if(!element) {
            return;
        }
        DQ.byId(element).delete();
    };

    // noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
    /**
     * does special form submit remapping
     * re-maps the issuing command link into something,
     * the "decode" of the command link on the server can understand
     *
     * @param formName
     * @param linkId
     * @param target
     * @param params
     */
    export const submitForm = function (formName: string, linkId: string | null = null, target: string |null = null, params: AssocArr<any> | Tuples<string, any> | null = {} ): boolean {


        //handle a possible incoming null, not sure if this is used that way anywhere, but we allow it
        params = (!params) ? {} : params;

        let clearFn = 'clearFormHiddenParams_' + formName.replace(/-/g, '\$:').replace(/:/g, '_');
        window?.[clearFn]?.(formName);

        //autoscroll code
        if (window?.myfaces?.core?.config?.autoScroll && (window as any)?.getScrolling) {
            myfaces.oam.setHiddenInput(formName, 'autoScroll', (window as any)?.getScrolling());
        }
        let paramsStream: Array<[string, any]> = Array.isArray(params) ? [...params] : ofAssoc(params);
        paramsStream.forEach(([key, data]) => myfaces.oam.setHiddenInput(formName, key, data));

        //we call the namespaced function, to allow decoration, via a direct call we would
        myfaces.oam.setHiddenInput(formName, `${formName}:_idcl`, linkId ?? '');


        DQ.byId(document.forms?.[formName] ?? document.getElementById(formName)).each(form => {
            const ATTR_TARGET = "target";
            const formElement = form.getAsElem(0).value as HTMLFormElement;
            const oldTarget = (form.getAsElem(0).value as HTMLFormElement).getAttribute("target");

            if(target != "null" && target) {
                (form.getAsElem(0).value as HTMLFormElement).setAttribute("target", target);
            }

            const result = formElement?.onsubmit?.(null);

            try {
                if ((!!result) || 'undefined' == typeof result) {
                    formElement.submit();
                }
            } catch (e) {
                window?.console.error(e);
            } finally {
                if(oldTarget == null || oldTarget == "null") {
                    (form.getAsElem(0).value as HTMLFormElement).removeAttribute("target");
                } else {
                    (form.getAsElem(0).value as HTMLFormElement).setAttribute("target", oldTarget);
                }

                // noinspection JSUnusedLocalSymbols
                paramsStream.forEach(([key, data]) => {
                    myfaces.oam.clearHiddenInput(formName, key);
                });
                myfaces.oam.clearHiddenInput(formName, `${formName}:_idcl`);
            }

        });
        return false;
    };
}