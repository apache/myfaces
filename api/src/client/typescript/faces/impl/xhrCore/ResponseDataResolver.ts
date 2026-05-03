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

import {Config, Optional, XMLQuery} from "mona-dish";

import {Assertions} from "../util/Assertions";
import {DQ} from "mona-dish";
import {
    $nsp,
    CTX_PARAM_MF_INTERNAL,
    CTX_PARAM_SRC_CTL_ID,
    CTX_PARAM_SRC_FRM_ID,
    SEL_RESPONSE_XML,
    SOURCE,
    HTML_TAG_FORM,
    UPDATE_ELEMS,
    UPDATE_FORMS,
    DEFERRED_HEAD_INSERTS
} from "../core/Const";
import {ExtConfig} from "../util/ExtDomQuery";

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
 * Throws an error in case of non-existent or wrong xml data
 *
 */
export function resolveResponseXML(request: Config): XMLQuery {
    let ret = new XMLQuery($nsp(request.getIf(SEL_RESPONSE_XML).value));
    Assertions.assertValidXMLResponse(ret);

    return ret;
}

/**
 * Splits the incoming pass-through context apart
 * in an internal and an external normalized context
 * the internal one is just for our internal processing
 *
 * @param context the root context as associative array
 */
export function resolveContexts(context: { [p: string]: any }): any {
    /**
     * we split the context apart into the external one and
     * some internal values
     */
    let externalContext = ExtConfig.fromNullable(context);
    let internalContext = externalContext.getIf(CTX_PARAM_MF_INTERNAL);
    if (!internalContext.isPresent()) {
        internalContext = ExtConfig.fromNullable({});
    }

    /**
     * prepare storage for some deferred operations
     */
    internalContext.assign(DEFERRED_HEAD_INSERTS).value = [];
    internalContext.assign(UPDATE_FORMS).value = [];
    internalContext.assign(UPDATE_ELEMS).value = [];
    return {externalContext, internalContext};
}
