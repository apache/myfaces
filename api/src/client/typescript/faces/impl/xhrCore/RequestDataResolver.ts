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

import {Config, DomQuery, DQ} from "mona-dish";
import {
    $faces,
    $nsp,
    CTX_OPTIONS_DELAY,
    CTX_OPTIONS_TIMEOUT,
    DELAY_NONE,
    EMPTY_FUNC,
    EMPTY_STR,
    ENCODED_URL, NAMED_VIEWROOT, NAMING_CONTAINER_ID,
    P_VIEWSTATE,
    REQ_TYPE_GET,
    REQ_TYPE_POST
} from "../core/Const";
import {XhrFormData} from "./XhrFormData";
import {ExtLang} from "../util/Lang";
import {ExtConfig, ExtDomQuery} from "../util/ExtDomQuery";
import {Assertions} from "../util/Assertions";


/**
 * Resolver functions for various aspects of the request data
 *
 * stateless because it might be called from various
 * parts of the response classes
 */

/**
 * resolves the event handlers lazily
 * so that if some decoration happens in between we can deal with it
 *
 * @param requestContext
 * @param responseContext
 * @param funcName
 */
export function resolveHandlerFunc(requestContext: Config, responseContext: Config, funcName: string) {
    responseContext = responseContext || new Config({});
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
 * @param elem
 * @param event
 */
export function resolveForm(elem: DQ, event: Event): DQ {
    return ExtLang.getForm(elem.getAsElem(0).value, event);
}

export function resolveViewId(form: DQ): string {
    const viewState = form.querySelectorAll(`input[type='hidden'][name*='${$nsp(P_VIEWSTATE)}']`).id.orElse("").value;
    const divider = $faces().separatorchar;
    const viewId = viewState.split(divider, 2)[0];
    const viewStateViewId = viewId.indexOf($nsp(P_VIEWSTATE)) === -1 ? viewId : "";
    // myfaces specific, we in non portlet environments prepend the viewId
    // even without being in a naming container, the other components ignore that
    return form.id.value.indexOf(viewStateViewId) === 0 ? viewStateViewId : "";
}

export function resolveViewRootId(form: DQ): string {
    const viewState = form.querySelectorAll(`input[type='hidden'][name*='${$nsp(P_VIEWSTATE)}']`).attr("name").orElse("").value;
    const divider = $faces().separatorchar;
    const viewId = viewState.split(divider, 2)[0];
    //different to the identifier the form id is never prepended to the viewstate
    return viewId.indexOf($nsp(P_VIEWSTATE)) === -1 ? viewId : "";
}

/**
 * as per jsdoc before the request it must be ensured that every post argument
 * is prefixed with the naming container id (there is an exception in mojarra with
 * the element=element param, which we have to follow here as well.
 * (inputs are prefixed by name anyway normally this only affects our standard parameters)
 * @private
 */
export function resoveNamingContainerMapper(internalContext: Config): (key: string, value: any) => [string, any] {
    const isNamedViewRoot = internalContext.getIf(NAMED_VIEWROOT).isPresent();
    if(!isNamedViewRoot) {
        return (key, value) => [key, value];
    }
    const partialId = internalContext.getIf(NAMING_CONTAINER_ID).value;
    const SEP = $faces().separatorchar;
    const prefix = partialId + SEP;
    return (key: string, value: any) => (key.indexOf(prefix) == 0) ? [key, value] : [prefix + key, value];
}

export function resolveTimeout(options: Config): number {
    let getCfg = ExtLang.getLocalOrGlobalConfig;
    return options.getIf(CTX_OPTIONS_TIMEOUT).value ?? getCfg(options.value, CTX_OPTIONS_TIMEOUT, 0);
}

/**
 * resolve the delay from the options and/or the request context and or the configuration
 *
 * @param options ... the options object, in most cases it will host the delay value
 */
export function resolveDelay(options: Config): number {
    // null, 'none', or undefined will automatically be mapped to 0 aka no delay
    // the config delay will be dropped not needed anymore, it does not really
    // make sense anymore now that it is part of a local spec
    let ret = options.getIf(CTX_OPTIONS_DELAY).orElse(0).value;
    // if delay === none, no delay must be used, aka delay 0
    ret = (DELAY_NONE === ret) ? 0 : ret;
    // negative, or invalid values will automatically get a js exception
    Assertions.assertDelay(ret);
    return ret;
}

/**
 * resolves the window-id from various sources
 *
 * @param options
 */
export function resolveWindowId(options: Config): string | null {
    return options?.value?.windowId ?? ExtDomQuery.windowId.value;
}

/**
 * cross port from the dojo lib
 * browser save event resolution
 * @param evt the event object
 * (with a fallback for ie events if none is present)
 * @deprecated soon will be removed
 */
export function getEventTarget(evt: Event): Element {
    // ie6 and 7 fallback
    let finalEvent = evt;
    /*
     * evt source is defined in the jsf events
     * seems like some component authors use our code,
     * so we add it here see also
     * https://issues.apache.org/jira/browse/MYFACES-2458
     * not entirely a bug but makes sense to add this
     * behavior. I don´t use it that way but nevertheless it
     * does not break anything so why not
     */
    let t = finalEvent?.srcElement ?? finalEvent?.target ?? (finalEvent as any)?.source;
    while ((t) && (t.nodeType != 1)) {
        t = t.parentNode;
    }
    return t;
}

/**
 * resolves a bunch of default values
 * which can be further processed from the given
 * call parameters of faces.ajax.request
 *
 * @param event
 * @param opts
 * @param el
 */
export function resolveDefaults(event: Event, opts: Options | [[string, any]] , el: Element | string = null): any {
    //deep copy the options, so that further transformations to not backfire into the callers
    const elem = DQ.byId(el || <Element>event.target, true);
    const options = new ExtConfig(opts).deepCopy as ExtConfig;
    return {
        options: options,
        elem: elem,
        elementId: elem.id.value,
        windowId: resolveWindowId(options),
        isResetValues: true === options.value?.resetValues
    };
}