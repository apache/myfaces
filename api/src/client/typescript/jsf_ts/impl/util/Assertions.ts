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
import {Config, Lang, XMLQuery} from "mona-dish";

import {DQ} from "mona-dish";
import {ExtLang} from "./Lang";
import getMessage = ExtLang.getMessage;
import makeException = ExtLang.makeException;
import {
    ATTR_URL,
    EMPTY_RESPONSE,
    EMPTY_STR, ERR_NO_PARTIAL_RESPONSE, MALFORMEDXML,
    ON_ERROR,
    ON_EVENT,
    PHASE_PROCESS_RESPONSE,
    RESP_PARTIAL
} from "../core/Const";

/**
 * a set of internal code assertions
 * which raise an error
 *
 */
export module Assertions {

    export function assertRequestIntegrity(options: Config, elem: DQ): void | never {
        /*assert if the onerror is set and once if it is set it must be of type function*/
        assertFunction(options.getIf(ON_ERROR).value);
        /*assert if the onevent is set and once if it is set it must be of type function*/
        assertFunction(options.getIf(ON_EVENT).value);
        //improve the error messages if an empty elem is passed
        //Assertions.assertElementExists(elem);
        assert(elem.isPresent(), getMessage("ERR_MUST_BE_PROVIDED1", "{0}: source  must be provided or exist", "source element id"), "jsf.ajax.request", "ArgNotSet",  )
    }

    export function assertUrlExists(node: XMLQuery): void | never {
        if (node.attr(ATTR_URL).isAbsent()) {
            throw Assertions.raiseError(new Error(), getMessage("ERR_RED_URL", null, "_Ajaxthis.processRedirect"), "processRedirect");
        }
    }

    /**
     * checks the xml for various issues which can occur
     * and prevent a proper processing
     */
    export function assertValidXMLResponse(responseXML: XMLQuery) : void | never  {
        assert(!responseXML.isAbsent(), EMPTY_RESPONSE, PHASE_PROCESS_RESPONSE);
        assert(!responseXML.isXMLParserError(),  responseXML.parserErrorText(EMPTY_STR), PHASE_PROCESS_RESPONSE);
        assert(responseXML.querySelectorAll(RESP_PARTIAL).isPresent(), ERR_NO_PARTIAL_RESPONSE, PHASE_PROCESS_RESPONSE);
    }

    /**
     * internal helper which raises an error in the
     * format we need for further processing
     *
     * @param error
     * @param message the message
     * @param caller
     * @param title the title of the error (optional)
     * @param name the name of the error (optional)
     */
    export function raiseError(error: any, message: string, caller ?: string, title ?: string, name ?: string): Error {

        let finalTitle = title ?? MALFORMEDXML;
        let finalName = name ?? MALFORMEDXML;
        let finalMessage = message ?? EMPTY_STR;

        //TODO clean up the messy makeException, this is a perfect case for encapsulation and sane defaults
        return makeException(error, finalTitle, finalName, "Response", caller || (((<any>arguments).caller) ? (<any>arguments).caller.toString() : "_raiseError"), finalMessage);
    }

    /*
     * using the new typescript 3.7 compiler assertion functionality to improve compiler hinting
     * we are not fully there yet, but soon
     */

    export function assert(value: any, msg = EMPTY_STR, caller=EMPTY_STR, title="Assertion Error"): asserts value {
        if(!value) {
            throw Assertions.raiseError(new Error(), msg ,caller, title);
        }
    }


    export function assertType(value: any, theType: any, msg = EMPTY_STR, caller=EMPTY_STR, title="Type Assertion Error"): asserts value {
        if((!!value) && !Lang.assertType(value,theType)) {
            throw Assertions.raiseError(new Error(), msg ,caller, title);
        }
    }

    export function assertFunction(value: any, msg = EMPTY_STR, caller=EMPTY_STR, title="Assertion Error"): asserts value is Function {
        assertType(value, "function", msg, caller, title);
    }
}


