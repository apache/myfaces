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
import {
    EMPTY_STR,
    ERROR_MESSAGE,
    ERROR_NAME,
    RESPONSE_TEXT,
    RESPONSE_XML,
    SOURCE,
    STATUS,
    UNKNOWN
} from "../core/Const";
import {Config} from "mona-dish";

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
export class ErrorData extends EventData {

    type: string = "error";
    source: string;
    errorName: string;
    errorMessage: string;

    responseText: string;
    responseXML: any;

    status: string;
    typeDetails: ErrorType;

    //TODO backwards compatible attributes
    serverErrorName: string;
    serverErrorMessage: string;
    message: string;

    constructor(source: string, errorName: string, errorMessage: string, responseText: string = null, responseXML: any = null, responseCode: string = "200", status: string = "UNKNOWN", type = ErrorType.CLIENT_ERROR) {
        super();
        this.source = source;
        this.type = "error";
        this.errorName = errorName;
        this.message = this.errorMessage = errorMessage;
        this.responseCode = responseCode;
        this.responseText = responseText;
        this.status = status;
        this.typeDetails = type;

        if (type == ErrorType.SERVER_ERROR) {
            this.serverErrorName = this.errorName;
            this.serverErrorMessage = this.errorMessage;
        }
    }

    static fromClient(e: Error): ErrorData {
        return new ErrorData("client", e?.name ?? '', e?.message ?? '', e?.stack ?? '');
    }

    static fromHttpConnection(source: any, name: string, message: string, responseText, responseCode: number, status: string = 'UNKNOWN'): ErrorData {
        return new ErrorData(source, name, message, responseText, responseCode, `${responseCode}`, status, ErrorType.HTTP_ERROR);
    }

    static fromGeneric(context: Config, errorCode: number, errorType: ErrorType = ErrorType.SERVER_ERROR): ErrorData {

        let getMsg = this.getMsg;

        let source = getMsg(context, SOURCE);
        let errorName = getMsg(context, ERROR_NAME);
        let errorMessage = getMsg(context, ERROR_MESSAGE);
        let status = getMsg(context, STATUS);
        let responseText = getMsg(context, RESPONSE_TEXT);
        let responseXML = getMsg(context, RESPONSE_XML);
        return new ErrorData(source, errorName, errorMessage, responseText, responseXML, errorCode + EMPTY_STR, status, errorType);
    }

    private static getMsg(context, param) {
        return getMessage(context.getIf(param).orElse(UNKNOWN).value);
    }

    static fromServerError(context: Config): ErrorData {
        return this.fromGeneric(context, -1);
    }

}