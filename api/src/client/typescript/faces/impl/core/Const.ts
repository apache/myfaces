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

/*
 * [export const] constants
 */
export const P_PARTIAL_SOURCE = "jakarta.faces.source";
export const PARTIAL_ID = "partialId";
export const P_VIEWSTATE = "jakarta.faces.ViewState";
export const P_CLIENT_WINDOW = "jakarta.faces.ClientWindow";
export const P_VIEWROOT = "jakarta.faces.ViewRoot";
export const P_VIEWHEAD = "jakarta.faces.ViewHead";
export const P_VIEWBODY = "jakarta.faces.ViewBody";

export const P_RESOURCE = "jakarta.faces.Resource";

/*some useful definitions*/

export const EMPTY_FUNC = Object.freeze(() => {
});
export const EMPTY_STR = "";
export const EMPTY_MAP = Object.freeze({});

export const HTML_VIEWSTATE = ["<input type='hidden'", "name='", P_VIEWSTATE, "' value='' />"].join(EMPTY_STR);
export const HTML_CLIENT_WINDOW = ["<input type='hidden'", "' name='", P_CLIENT_WINDOW, "' value='' />"].join(EMPTY_STR);


/*internal identifiers for options*/
export const IDENT_ALL = "@all";
export const IDENT_NONE = "@none";
export const IDENT_THIS = "@this";
export const IDENT_FORM = "@form";


export const P_AJAX = "jakarta.faces.partial.ajax";
export const P_EXECUTE = "jakarta.faces.partial.execute";
export const P_RENDER = "jakarta.faces.partial.render";
export const P_EVT = "jakarta.faces.partial.event";

export const P_RESET_VALUES = "jakarta.faces.partial.resetValues";
export const P_WINDOW_ID = "jakarta.faces.windowId";

export const P_BEHAVIOR_EVENT = "jakarta.faces.behavior.event";

export const CTX_PARAM_RENDER = "render";
export const WINDOW_ID = "windowId";

/* message types */
export const ERROR = "error";
export const EVENT = "event";

export const ON_ERROR = "onerror";
export const ON_EVENT = "onevent";

/* event emitting stages */
export const BEGIN = "begin";
export const COMPLETE = "complete";
export const SUCCESS = "success";

export const SOURCE = "source";
export const STATUS = "status";


export const ERROR_NAME = "error-name";
export const ERROR_MESSAGE = "error-message";


export const RESPONSE_TEXT = "responseText";
export const RESPONSE_XML = "responseXML";

/*ajax errors spec 14.4.2*/
export const HTTPERROR = "httpError";
export const EMPTY_RESPONSE = "emptyResponse";
export const MALFORMEDXML = "malformedXML";
export const SERVER_ERROR = "serverError";
export const CLIENT_ERROR = "clientError";
export const TIMEOUT_EVENT = "timeout";

export const CTX_PARAM_MF_INTERNAL = "_mfInternal";

export const CTX_PARAM_SRC_FRM_ID = "_mfSourceFormId";
export const CTX_PARAM_SRC_CTL_ID = "_mfSourceControlId";
export const CTX_PARAM_TR_TYPE = "_mfTransportType";
export const CTX_PARAM_PASS_THR = "passThrgh";
export const CTX_PARAM_SPEC_PARAMS = "params";

export const CTX_PARAM_DELAY = "delay";
export const CTX_PARAM_TIMEOUT = "timeout";
export const CTX_PARAM_RST = "resetValues";
export const CTX_PARAM_EXECUTE = "execute";

export const STAGE_DEVELOPMENT = "Development";


export const CONTENT_TYPE = "Content-Type";
export const HEAD_FACES_REQ = "Faces-Request";
export const REQ_ACCEPT = "Accept";
export const VAL_AJAX = "partial/ajax";
export const ENCODED_URL = "jakarta.faces.encodedURL";
export const REQ_TYPE_GET = "GET";
export const REQ_TYPE_POST = "POST";
export const STATE_EVT_BEGIN = "begin"; //TODO remove this
export const STATE_EVT_TIMEOUT = "TIMEOUT_EVENT";
export const STATE_EVT_COMPLETE = "complete"; //TODO remove this
export const URL_ENCODED = "application/x-www-form-urlencoded";
export const MULTIPART = "multipart/form-data";
export const NO_TIMEOUT = 0;
export const STD_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

export const TAG_HEAD = "HEAD";
export const TAG_FORM = "FORM";
export const TAG_BODY = "BODY";
export const TAG_BEFORE = "before";
export const TAG_AFTER = "after";

export const TAG_ATTR = "attribute";


export const SEL_VIEWSTATE_ELEM = "[name='" + P_VIEWSTATE + "']";
export const SEL_CLIENT_WINDOW_ELEM = "[name='" + P_CLIENT_WINDOW + "']";
export const SEL_RESPONSE_XML = "responseXML";

export const PHASE_PROCESS_RESPONSE = "processResponse";


export const ERR_NO_PARTIAL_RESPONSE = "Partial response not set";

export const ATTR_URL = "url";
export const ATTR_NAME = "name";
export const ATTR_VALUE = "value";
export const ATTR_ID = "id";

/*partial response types*/
export const RESP_PARTIAL = "partial-response";

/*partial commands*/
export const CMD_CHANGES = "changes";
export const CMD_UPDATE = "update";
export const CMD_DELETE = "delete";
export const CMD_INSERT = "insert";
export const CMD_EVAL = "eval";
export const CMD_ERROR = "error";
export const CMD_ATTRIBUTES = "attributes";
export const CMD_EXTENSION = "extension";
export const CMD_REDIRECT = "redirect";

/*other constants*/

export const UPDATE_FORMS = "_updateForms";
export const UPDATE_ELEMS = "_updateElems";

//we want the head elements to be processed before we process the body
//but after the inner html is done
export const DEFERRED_HEAD_INSERTS = "_headElems";

export const MYFACES = "myfaces";

export const SEL_SCRIPTS_STYLES = "script, style, link";

export const MF_NONE = "__mf_none__";

export const REASON_EXPIRED = "Expired";

export const APPLIED_VST = "appliedViewState";
export const APPLIED_CLIENT_WINDOW = "appliedClientWindow";

export const RECONNECT_INTERVAL = 500;
export const MAX_RECONNECT_ATTEMPTS = 25;

export const UNKNOWN = "UNKNOWN";

/**
 * helper to remap the namespaces variables for 2.3
 * from 2.3 to 4.0 every javax namespace has been changed
 * to faces
 * To take the compatibility layer out this method just has to be
 * changed to a simple value passthrough
 */

export function $nsp(inputNamespace?: any): any {
     if((!inputNamespace) || !inputNamespace?.replace) {
          return inputNamespace;
     }
     return (!!window?.faces) ? inputNamespace.replace(/javax\.faces/gi,"jakarta.faces"): inputNamespace.replace(/jakarta\.faces/gi, "javax.faces");
}

