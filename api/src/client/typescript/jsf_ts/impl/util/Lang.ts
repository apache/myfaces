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
 *
 * todo replace singleton with module definition
 *
 */

import {Lang as LangBase, Config, Optional, DomQuery, DQ, Stream} from "mona-dish";
import {Messages} from "../i18n/Messages";
import {EMPTY_STR, TAG_FORM} from "../core/Const";
import {getEventTarget} from "../xhrCore/RequestDataResolver";

export module ExtLang {

    let installedLocale: Messages;
    let nameSpace = "impl/util/Lang/";

    export function getLanguage(): string {
        //TODO global config override

        let language: string = (<any>navigator).languages?.[0] ?? navigator?.language;
        language = language.split("-")[0];
        return language;
    }

    //should be in lang, but for now here to avoid recursive imports, not sure if typescript still has a problem with those
    /**
     * helper function to savely resolve anything
     * this is not an elvis operator, it resolves
     * a value without exception in a tree and if
     * it is not resolvable then an optional of
     * a default value is restored or Optional.empty
     * if none is given
     *
     * usage
     * <code>
     *     let var: Optional<string> = saveResolve(() => a.b.c.d.e, "foobaz")
     * </code>
     *
     * @param resolverProducer a lambda which can produce the value
     * @param defaultValue an optional default value if the producer failes to produce anything
     * @returns an Optional of the produced value
     */
    export function failSaveResolve<T>(resolverProducer: () => T, defaultValue: T = null): Optional<T> {
        return LangBase.saveResolve(resolverProducer, defaultValue);
    }

    /**
     * under some conditions it makes sense to swallow errors and return a default value in the error case
     * classical example the optional resolution of values in a chain (thankfully now covered by Typescript itself)
     * another example which we have in our system is that some operations fail only under test due to test framework
     * limitations while they cannot fail in the real world.
     *
     * @param resolverProducer a producer function which produces a value in the non error case
     * @param defaultValue the default value in case of a fail of the function
     */
    export function failSaveExecute<T>(resolverProducer: () => any, defaultValue: T = null): void {
        LangBase.saveResolve(resolverProducer, defaultValue);
    }

    /**
     * returns a given localized message upon a given key
     * basic java log like templating functionality is included
     *
     * @param {String} key the key for the message
     * @param {String} defaultMessage optional default message if none was found
     *
     * Additionally you can pass additional arguments, which are used
     * in the same way java log templates use the params
     *
     * @param templateParams the param list to be filled in
     */
    export function getMessage(key: string, defaultMessage?: string, ...templateParams: Array<string>): string {
        installedLocale = installedLocale ?? new Messages();

        let msg = installedLocale[key] ?? defaultMessage ?? key;

        Stream.of(...templateParams).each((param, cnt) => {
            msg = msg.replace(new RegExp(["\\{", cnt, "\\}"].join(EMPTY_STR), "g"), param);
        });

        return msg;
    }

    /**
     * transforms a key value pair into a string
     * @param key the key
     * @param val the value
     * @param delimiter the delimiter
     */
    export function keyValToStr(key: string, val: string, delimiter: string = "\n") {
        return [key, val].join(delimiter);
    }

    /**
     * creates an exeption with additional internal parameters
     * for extra information
     *
     * @param error
     * @param {String} title the exception title
     * @param {String} name  the exception name
     * @param {String} callerCls the caller class
     * @param {String} callFunc the caller function
     * @param {String} message the message for the exception
     */
    export function makeException(error: Error, title: string, name: string, callerCls: string, callFunc: string, message: string): Error {

        return new Error(message + (callerCls ?? nameSpace) + callFunc ?? (EMPTY_STR + (<any>arguments).caller.toString()));

    }

    /**
     * fetches a global config entry
     * @param {String} configName the name of the configuration entry
     * @param {Object} defaultValue
     *
     * @return either the config entry or if none is given the default value
     */
    export function getGlobalConfig(configName: string, defaultValue: any): any {
        /**
         * note we could use exists but this is an heavy operation, since the config name usually
         * given this function here is called very often
         * is a single entry without . in between we can do the lighter shortcut
         */
        return (<any>window)?.myfaces?.config?.[configName] ?? defaultValue;
    }

    /**
     * fetches the form in an fuzzy manner depending
     * on an element or event target.
     *
     * The idea is that according to the jsf spec
     * the enclosing form of the issuing element needs to be fetched.
     *
     * This is fine, but since then html5 came into the picture with the form attribute the element
     * can be anywhere referencing its parent form.
     *
     * Also theoretically you can have the case of an issuing element enclosing a set of forms
     * (not really often used, but theoretically it could be input button allows to embed html for instance)
     *
     * So the idea is not to limit the issuing form determination to the spec case
     * but also cover the theoretical and html5 corner case.
     *
     * @param elem
     * @param event
     */
    export function getForm(elem: Element, event ?: Event): DQ | never {

        let queryElem = new DQ(elem);
        let eventTarget = new DQ(getEventTarget(event));

        if (queryElem.isTag(TAG_FORM)) {
            return queryElem;
        }

        //html 5 for handling
        if (queryElem.attr(TAG_FORM).isPresent()) {
            let formId = queryElem.attr(TAG_FORM).value;
            let foundForm = DQ.byId(formId, true);
            if (foundForm.isPresent()) {
                return foundForm;
            }
        }

        let form = queryElem.parents(TAG_FORM)
            .orElseLazy(() => queryElem.byTagName(TAG_FORM, true))
            .orElseLazy(() => eventTarget.parents(TAG_FORM))
            .orElseLazy(() => eventTarget.byTagName(TAG_FORM))
            .first();

        assertFormExists(form);

        return form;
    }

    /**
     * gets the local or global options with local ones having higher priority
     * if no local or global one was found then the default value is given back
     *
     * @param {String} configName the name of the configuration entry
     * @param {String} localOptions the local options root for the configuration myfaces as default marker is added implicitely
     *
     * @param {Object} defaultValue
     *
     * @return either the config entry or if none is given the default value
     */
    export function getLocalOrGlobalConfig(localOptions: Config, configName: string, defaultValue: any): any {
        return localOptions.value?.myfaces?.config?.[configName] ??
            (<any>window)?.myfaces?.config?.[configName] ??
            defaultValue;
    }

    /**
     * assert that the form exists and throw an exception in the case it does not
     * (TODO move this into the assertions)
     *
     * @param form the form to check for
     */
    function assertFormExists(form: DomQuery): void | never {
        if (form.isAbsent()) {
            throw makeException(new Error(), null, null, "Impl", "getForm", getMessage("ERR_FORM"));
        }
    }

}