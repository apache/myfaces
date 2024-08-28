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
import {
    EMPTY_STR, ERROR,
    ERROR_MESSAGE,
    ERROR_NAME, HTTP_ERROR,
    RESPONSE_TEXT,
    RESPONSE_XML, SERVER_ERROR,
    SOURCE,
    STATUS,
    UNKNOWN
} from "../core/Const";
import {Config, Optional, XMLQuery} from "mona-dish";

import {EventData} from "./EventData";
import {ExtLang} from "../util/Lang";
import getMessage = ExtLang.getMessage;


export enum ErrorType {
    SERVER_ERROR = "serverError",
    HTTP_ERROR = "httpError",
    CLIENT_ERROR = "clientErrror",
    TIMEOUT = "timeout"
}

/**
 * the spec has a problem of having the error
 * object somewhat underspecified, there is no clear
 * description of the required contents.
 * I want to streamline it with mojarra here
 * hence we are going to move
 * everything into the same attributes,
 * I will add deprecated myfaces backwards compatibility attributes as well
 */
export class ErrorData extends EventData implements IErrorData {

    type: string = "error";
    source: string;

    errorName: string;
    errorMessage: string;

    responseText: string;
    responseXML: any;

    status: string;
    typeDetails: ErrorType;

    serverErrorName: string;
    serverErrorMessage: string;
    description: string;

    constructor(source: string, errorName: string, errorMessage: string, responseText: string = null, responseXML: Document = null, responseCode: number = -1, statusOverride: string = null,  type = ErrorType.CLIENT_ERROR) {
        super();
        this.source = source;
        this.type = ERROR;
        this.errorName = errorName;

        //tck requires that the type is prefixed to the message itself (jsdoc also) in case of a server error
        this.errorMessage = errorMessage;
        this.responseCode = `${responseCode}`;
        this.responseText = responseText;
        this.responseXML = responseXML;

        this.status = statusOverride;

        this.description = `Status: ${this.status}\nResponse Code: ${this.responseCode}\nError Message: ${this.errorMessage}`;

        this.typeDetails = type;

        if (type == ErrorType.SERVER_ERROR) {
            this.serverErrorName = this.errorName;
            this.serverErrorMessage = this.errorMessage;
        }
    }

    static fromClient(e: Error): ErrorData {
        return new ErrorData((e as any)?.source ?? "client", e?.name ?? EMPTY_STR, e?.message ?? EMPTY_STR, e?.stack ?? EMPTY_STR);
    }

    static fromHttpConnection(source: any, name: string, message: string, responseText: string, responseXML: Document, responseCode: number, status: string = EMPTY_STR): ErrorData {
        return new ErrorData(source, name, message, responseText, responseXML, responseCode, status, ErrorType.HTTP_ERROR);
    }

    static fromGeneric(context: Config, errorCode: number, errorType: ErrorType = ErrorType.SERVER_ERROR): ErrorData {

        let getMsg = this.getMsg;

        let source = getMsg(context, SOURCE);
        let errorName = getMsg(context, ERROR_NAME);
        let errorMessage = getMsg(context, ERROR_MESSAGE);
        let status = getMsg(context, STATUS);
        let responseText = getMsg(context, RESPONSE_TEXT);
        let responseXML: Document = context.getIf(RESPONSE_XML).value;


        return new ErrorData(source, errorName, errorMessage, responseText, responseXML, errorCode, status, errorType);
    }

    private static getMsg(context, param) {
        return getMessage(context.getIf(param).orElse(EMPTY_STR).value);
    }

    static fromServerError(context: Config): ErrorData {
        return this.fromGeneric(context, -1);
    }

}