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

import {Config, Optional, XMLQuery} from "mona-dish";

import {Assertions} from "../util/Assertions";
import {DQ} from "mona-dish";
import {
    CTX_PARAM_MF_INTERNAL,
    CTX_PARAM_SRC_CTL_ID,
    CTX_PARAM_SRC_FRM_ID,
    SEL_RESPONSE_XML,
    SOURCE,
    TAG_FORM,
    UPDATE_ELEMS,
    UPDATE_FORMS
} from "../core/Const";

/**
 * Resolver functions for various aspects of the response data
 *
 * stateless because it might be called from various
 * parts of the response classes
 */

/**
 * fetches the response XML
 * as XML Query object
 *
 * @param request the request hosting the responseXML
 *
 * Throws an error in case of non existent or wrong xml data
 *
 */
export function resolveResponseXML(request: Config): XMLQuery {
    let ret = new XMLQuery(request.getIf(SEL_RESPONSE_XML).value);
    Assertions.assertValidXMLResponse(ret);

    return ret;
}

/**
 * Splits the incoming passthrough context apart
 * in an internal and an external nomalized context
 * the internal one is just for our internal processing
 *
 * @param context the root context as associative array
 */
export function resolveContexts(context: { [p: string]: any }): any {
    /**
     * we split the context apart into the external one and
     * some internal values
     */
    let externalContext = Config.fromNullable(context);
    let internalContext = externalContext.getIf(CTX_PARAM_MF_INTERNAL);
    if (!internalContext.isPresent()) {
        internalContext = Config.fromNullable({});
    }

    /**
     * prepare storage for some deferred operations
     */
    internalContext.assign(UPDATE_FORMS).value = [];
    internalContext.assign(UPDATE_ELEMS).value = [];
    return {externalContext, internalContext};
}

/**
 * fetches the source element out of our conexts
 *
 * @param context the external context which shpuld host the source id
 * @param internalContext internal passthrough fall back
 *
 */
export function resolveSourceElement(context: Config, internalContext: Config): DQ {
    let elemId = resolveSourceElementId(context, internalContext);
    return DQ.byId(elemId.value, true);
}

/**
 * fetches the source form if it still exists
 * also embedded forms and parent forms are taken into consideration
 * as fallbacks
 *
 * @param internalContext
 * @param elem
 */
export function resolveSourceForm(internalContext: Config, elem: DQ): DQ {
    let sourceFormId = internalContext.getIf(CTX_PARAM_SRC_FRM_ID);
    let sourceForm = new DQ(sourceFormId.isPresent() ? document.forms[sourceFormId.value] : null);

    sourceForm = sourceForm.orElseLazy(() => elem.parents(TAG_FORM))
        .orElseLazy(() => elem.querySelectorAll(TAG_FORM))
        .orElseLazy(() => DQ.querySelectorAll(TAG_FORM));

    return sourceForm;
}

function resolveSourceElementId(context: Config, internalContext: Config): Optional<string> {
    //?internal context?? used to be external one
    return internalContext.getIf(CTX_PARAM_SRC_CTL_ID)
        .orElseLazy(() => context.getIf(SOURCE, "id").value);
}

