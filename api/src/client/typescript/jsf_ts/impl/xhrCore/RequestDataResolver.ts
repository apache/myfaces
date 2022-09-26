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

import {Config, DomQuery, DQ} from "mona-dish";
import {
    CTX_PARAM_DELAY,
    CTX_PARAM_TIMEOUT,
    EMPTY_FUNC,
    EMPTY_STR,
    ENCODED_URL,
    MF_NONE,
    REQ_TYPE_GET,
    REQ_TYPE_POST
} from "../core/Const";
import {XhrFormData} from "./XhrFormData";
import {ExtLang} from "../util/Lang";
import {ExtDomquery} from "../util/ExtDomQuery";

/**
 * Resolver functions for various aspects of the request data
 *
 * stateless because it might be called from various
 * parts of the response classes
 */

/**
 * resolves the event handlers lazly
 * so that if some decoration happens in between we can deal with it
 *
 * @param requestContext
 * @param responseContext
 * @param funcName
 */
export function resolveHandlerFunc(requestContext: Config, responseContext: Config, funcName: string) {
    return responseContext.getIf(funcName)
        .orElseLazy(() =>requestContext.getIf(funcName).value)
        .orElse(EMPTY_FUNC).value;
}

export function resolveTargetUrl(srcFormElement: HTMLFormElement) {
    return (typeof srcFormElement.elements[ENCODED_URL] == 'undefined') ?
        srcFormElement.action :
        srcFormElement.elements[ENCODED_URL].value;
}

export function resolveFinalUrl(sourceForm: DomQuery, formData: XhrFormData, ajaxType = REQ_TYPE_POST) {
    let targetUrl = resolveTargetUrl(<HTMLFormElement>sourceForm.getAsElem(0).value);

    return targetUrl + (ajaxType == REQ_TYPE_GET ? "?" + formData.toString() : EMPTY_STR);
}

/**
 * form resolution the same way our old implementation did
 * it is either the id or the parent form of the element or an embedded form
 * of the element
 *
 * @param requestCtx
 * @param elem
 * @param event
 */
export function resolveForm(requestCtx: Config, elem: DQ, event: Event): DQ {
    const configId = requestCtx.value?.myfaces?.form ?? MF_NONE;
    return DQ
        .byId(configId, true)
        .orElseLazy(() => ExtLang.getForm(elem.getAsElem(0).value, event));
}

export function resolveTimeout(options: Config): number {
    let getCfg = ExtLang.getLocalOrGlobalConfig;
    return options.getIf(CTX_PARAM_TIMEOUT).value ?? getCfg(options.value, CTX_PARAM_TIMEOUT, 0);
}

/**
 * resolve the delay from the options and/or the request context and or the configuration
 *
 * @param options ... the options object, in most cases it will host the delay value
 */
export function resolveDelay(options: Config): number {
    let getCfg = ExtLang.getLocalOrGlobalConfig;

    return options.getIf(CTX_PARAM_DELAY).value ?? getCfg(options.value, CTX_PARAM_DELAY, 0);
}

/**
 * resolves the window Id from various sources
 *
 * @param options
 */
export function resolveWindowId(options: Config) {
    return options?.value?.windowId ?? ExtDomquery.windowId;
}

/**
 * cross port from the dojo lib
 * browser save event resolution
 * @param evt the event object
 * (with a fallback for ie events if none is present)
 */
export function getEventTarget(evt: Event): Element {
    //ie6 and 7 fallback
    let finalEvent = evt;
    /**
     * evt source is defined in the jsf events
     * seems like some component authors use our code
     * so we add it here see also
     * https://issues.apache.org/jira/browse/MYFACES-2458
     * not entirely a bug but makes sense to add this
     * behavior. I dont use it that way but nevertheless it
     * does not break anything so why not
     * */
    let t = finalEvent?.srcElement ?? finalEvent?.target ?? (<any>finalEvent)?.source;
    while ((t) && (t.nodeType != 1)) {
        t = t.parentNode;
    }
    return t;
}

/**
 * resolves a bunch of default values
 * which can be further processed from the given
 * call parameters of jsf.ajax.request
 *
 * @param event
 * @param opts
 * @param el
 */
export function resolveDefaults(event: Event, opts: any = {}, el: Element | string = null) {
    //deep copy the options, so that further transformations to not backfire into the callers
    const resolvedEvent = event,
        options = new Config(opts).deepCopy,
        elem = DQ.byId(el || <Element>resolvedEvent.target, true),
        elementId = elem.id.value, requestCtx = new Config({}),
        internalCtx = new Config({}), windowId = resolveWindowId(options),
        isResetValues = true === options.value?.resetValues;

    return {resolvedEvent, options, elem, elementId, requestCtx, internalCtx, windowId, isResetValues};
}