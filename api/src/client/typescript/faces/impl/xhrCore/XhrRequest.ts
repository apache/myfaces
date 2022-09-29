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

import {AsyncRunnable} from "../util/AsyncRunnable";
import {Config, DQ, Stream} from "mona-dish";
import {Implementation} from "../AjaxImpl";

import {XhrFormData} from "./XhrFormData";
import {ErrorData} from "./ErrorData";
import {EventData} from "./EventData";
import {ExtLang} from "../util/Lang";
import {
    BEGIN,
    COMPLETE,
    CONTENT_TYPE,
    CTX_PARAM_MF_INTERNAL,
    CTX_PARAM_PASS_THR,
    ERROR,
    HEAD_FACES_REQ,
    MALFORMEDXML,
    NO_TIMEOUT,
    ON_ERROR,
    ON_EVENT, P_EXECUTE,
    REQ_ACCEPT,
    REQ_TYPE_GET,
    REQ_TYPE_POST,
    STATE_EVT_TIMEOUT,
    STD_ACCEPT,
    URL_ENCODED,
    VAL_AJAX
} from "../core/Const";
import {resolveFinalUrl, resolveHandlerFunc} from "./RequestDataResolver";
import failSaveExecute = ExtLang.failSaveExecute;

/**
 * Faces XHR Request Wrapper
 * as Asyncrunnable for our Asynchronous queue
 *
 * The idea is that we basically just enqueue
 * a single ajax request into our queue
 * and let the queue do the processing.
 *
 */
declare const window: any;

export class XhrRequest implements AsyncRunnable<XMLHttpRequest> {

    private responseContext: Config;

    private stopProgress = false;

    /**
     * helper support so that we do not have to drag in Promise shims
     */
    private catchFuncs: Array<Function> = [];
    private thenFunc: Array<Function> = [];

    /**
     * Reqired Parameters
     *
     * @param source the issuing element
     * @param sourceForm the form which is related to the issuing element
     * @param requestContext the request context with allö pass through values
     *
     * Optional Parameters
     *
     * @param internalContext internal context with internal info which is passed through, not used by the user
     * @param partialIdsArray an optional restricting partial ids array for encoding
     * @param timeout optional xhr timeout
     * @param ajaxType optional request type, default "POST"
     * @param contentType optional content type, default "application/x-www-form-urlencoded"
     * @param xhrObject optional xhr object which must fullfill the XMLHTTPRequest api, default XMLHttpRequest
     */
    constructor(
        private source: DQ,
        private sourceForm: DQ,
        private requestContext: Config,
        private internalContext: Config,
        private partialIdsArray = [],
        private timeout = NO_TIMEOUT,
        private ajaxType = REQ_TYPE_POST,
        private contentType = URL_ENCODED,
        private xhrObject = new XMLHttpRequest()
    ) {
        /*
        * we omit promises here
        * some browsers do not support it and we do not need shim code
        */
        this.registerXhrCallbacks((data: any) => {
            this.resolve(data)
        }, (data: any) => {
            this.reject(data)
        });
    }

    start(): AsyncRunnable<XMLHttpRequest> {

        let ignoreErr = failSaveExecute;
        let xhrObject = this.xhrObject;

        let executesArr = () => {
            return this.requestContext.getIf(CTX_PARAM_PASS_THR, P_EXECUTE).get("none").value.split(/\s+/gi);
        };
        try {

            let formElement = this.sourceForm.getAsElem(0).value;
            let viewState = (window?.faces ?? window?.jsf).getViewState(formElement);
            //encoded we need to decode
            //We generated a base representation of the current form
            //in case someone has overloaded the viewstate with addtional decorators we merge
            //that in, there is no way around it, the spec allows it and getViewState
            //must be called, so whatever getViewState delivers has higher priority then
            //whatever the formData object delivers
            //the partialIdsArray arr is almost deprecated legacy code where we allowed to send a separate list of partial
            //ids for reduced load and server processing, this will be removed soon, we can handle the same via execute
            //anyway TODO remove the partial ids array
            let formData: XhrFormData = new XhrFormData(this.sourceForm, viewState, executesArr(), this.partialIdsArray);

            this.contentType = formData.isMultipartRequest ? "undefined" : this.contentType;

            //next step the pass through parameters are merged in for post params
            let requestContext = this.requestContext;
            let passThroughParams = requestContext.getIf(CTX_PARAM_PASS_THR);

            // this is an extension where we allow pass through parameters to be sent down additionally
            // this can be used and is used in the impl to enrich the post request parameters with additional
            // information
            formData.shallowMerge(passThroughParams, true, true);

            this.responseContext = passThroughParams.deepCopy;

            //we have to shift the internal passthroughs around to build up our response context
            let responseContext = this.responseContext;

            responseContext.assign(CTX_PARAM_MF_INTERNAL).value = this.internalContext.value;

            //per spec the onevent and onerrors must be passed through to the response
            responseContext.assign(ON_EVENT).value = requestContext.getIf(ON_EVENT).value;
            responseContext.assign(ON_ERROR).value = requestContext.getIf(ON_ERROR).value;

            xhrObject.open(this.ajaxType, resolveFinalUrl(this.sourceForm, formData, this.ajaxType), true);

            //adding timeout
            this.timeout ? xhrObject.timeout = this.timeout : null;

            //a bug in the xhr stub library prevents the setRequestHeader to be properly executed on fake xhr objects
            //normal browsers should resolve this
            //tests can quietly fail on this one
            if(this.contentType != "undefined") {
                ignoreErr(() => xhrObject.setRequestHeader(CONTENT_TYPE, `${this.contentType}; charset=utf-8`));
            }

            ignoreErr(() => xhrObject.setRequestHeader(HEAD_FACES_REQ, VAL_AJAX));

            //probably not needed anymore, will test this
            //some webkit based mobile browsers do not follow the w3c spec of
            // setting, they accept headers automatically
            ignoreErr(() => xhrObject.setRequestHeader(REQ_ACCEPT, STD_ACCEPT));

            this.sendEvent(BEGIN);

            this.sendRequest(formData);

        } catch (e) {
            //_onError//_onError
            this.handleError(e);
        }
        return this;
    }

    cancel() {
        try {
            this.xhrObject.abort();
        } catch (e) {
            this.handleError(e);
        }
    }

    resolve(data: any) {
        Stream.of(...this.thenFunc).reduce((inputVal: any, thenFunc: any) => {
            return thenFunc(inputVal);
        }, data);
    }

    reject(data: any) {
        Stream.of(...this.catchFuncs).reduce((inputVal: any, catchFunc: any) => {
            return catchFunc(inputVal);
        }, data);
    }

    catch(func: (data: any) => any): AsyncRunnable<XMLHttpRequest> {
        //this.$promise.catch(func);
        this.catchFuncs.push(func);
        return this;
    }

    finally(func: () => void): AsyncRunnable<XMLHttpRequest> {
        //no ie11 support we probably are going to revert to shims for that one
        //(<any>this.$promise).then(func).catch(func);
        this.catchFuncs.push(func);
        this.thenFunc.push(func);
        return this;
    }

    then(func: (data: any) => any): AsyncRunnable<XMLHttpRequest> {
        //this.$promise.then(func);
        this.thenFunc.push(func);
        return this;
    }

    /**
     * attaches the internal event and processing
     * callback within the promise to our xhr object
     *
     * @param resolve
     * @param reject
     */
    private registerXhrCallbacks(resolve: Consumer<any>, reject: Consumer<any>) {
        let xhrObject = this.xhrObject;

        xhrObject.onabort = () => {
            this.onAbort(reject);
        };
        xhrObject.ontimeout = () => {
            this.onTimeout(reject);
        };
        xhrObject.onload = () => {
            this.onSuccess(resolve)
        };
        xhrObject.onloadend = () => {
            this.onDone(this.xhrObject, resolve);
        };
        xhrObject.onerror = (errorData: any) => {
            this.onError(errorData, reject);
        };
    }

    /*
     * xhr processing callbacks
     *
     * Those methods are the callbacks called by
     * the xhr object depending on its own state
     */

    private onAbort(reject: Consumer<any>) {
        reject();
    }

    private onTimeout(reject: Consumer<any>) {
        this.sendEvent(STATE_EVT_TIMEOUT);
        reject();
    }

    private onSuccess(resolve: Consumer<any>) {

        this.sendEvent(COMPLETE);

        //malforms always result in empty response xml
        if (!this?.xhrObject?.responseXML) {
            this.handleMalFormedXML(resolve);
            return;
        }

        (window?.faces ?? window.jsf).ajax.response(this.xhrObject, this.responseContext.value ?? {});
    }

    private handleMalFormedXML(resolve: Function) {
        this.stopProgress = true;
        let errorData = {
            type: ERROR,
            status: MALFORMEDXML,
            responseCode: 200,
            responseText: this.xhrObject?.responseText,
            source: {
                id: this.source.id.value
            }
        };
        try {
            this.handleError(errorData, true);
        } finally {
            //we issue a resolve in this case to allow the system to recover
            resolve(errorData);
            //reject();
        }
        //non blocking non clearing
    }

    private onDone(data: any, resolve: Consumer<any>) {
        if (this.stopProgress) {
            return;
        }
        resolve(data);
    }

    private onError(errorData: any,  reject: Consumer<any>) {
        this.handleError(errorData);
        reject();
    }

    private sendRequest(formData: XhrFormData) {
        let isPost = this.ajaxType != REQ_TYPE_GET;
        if (formData.isMultipartRequest) {
            //in case of a multipart request we send in a formData object as body
            this.xhrObject.send((isPost) ? formData.toFormData() : null);
        } else {
            //in case of a normal request we send it normally
            this.xhrObject.send((isPost) ? formData.toString() : null);
        }
    }

    /*
     * other helpers
     */
    private sendEvent(evtType: string) {
        let eventData = EventData.createFromRequest(this.xhrObject, this.requestContext, evtType);
        try {
            //user code error, we might cover
            //this in onError but also we cannot swallow it
            //we need to resolve the local handlers lazyly,
            //because some frameworks might decorate them over the context in the response
            let eventHandler = resolveHandlerFunc(this.requestContext, this.responseContext, ON_EVENT);

            Implementation.sendEvent(eventData, eventHandler);
        } catch (e) {
            this.handleError(e);
            throw e;
        }
    }

    private handleError(exception, responseFormatError: boolean = false) {
        let errorData = (responseFormatError) ? ErrorData.fromHttpConnection(exception.source, exception.type, exception.status, exception.responseText, exception.responseCode, exception.status) : ErrorData.fromClient(exception);

        let eventHandler = resolveHandlerFunc(this.requestContext, this.responseContext, ON_ERROR);
        Implementation.sendError(errorData, eventHandler);
    }

}