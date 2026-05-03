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

import {AsyncRunnable, IAsyncRunnable} from "../util/AsyncRunnable";
import {Config, DQ, XMLQuery} from "mona-dish";
import {Implementation} from "../AjaxImpl";

import {XhrFormData} from "./XhrFormData";
import {ErrorData} from "./ErrorData";
import {EventData} from "./EventData";
import {ExtLang} from "../util/Lang";
import {
    $faces,
    BEGIN,
    COMPLETE,
    CONTENT_TYPE,
    CTX_PARAM_MF_INTERNAL,
    CTX_PARAM_REQ_PASS_THR,
    HEAD_FACES_REQ,
    MALFORMEDXML,
    NO_TIMEOUT,
    ON_ERROR,
    ON_EVENT,
    P_EXECUTE,
    REQ_ACCEPT,
    REQ_TYPE_GET,
    REQ_TYPE_POST,
    SOURCE,
    STATE_EVT_TIMEOUT,
    STD_ACCEPT,
    URL_ENCODED,
    VAL_AJAX,
    IDENT_NONE,
    CTX_PARAM_SRC_FRM_ID,
    CTX_PARAM_SRC_CTL_ID,
    CTX_PARAM_PPS,
    EMPTY_RESPONSE,
    HTTP_ERROR,
    EMPTY_STR,
    $nsp,
    P_BEHAVIOR_EVENT,
    CTX_PARAM_UPLOAD_ON_PROGRESS,
    CTX_PARAM_UPLOAD_LOAD,
    CTX_PARAM_UPLOAD_LOADSTART,
    CTX_PARAM_UPLOAD_LOADEND,
    CTX_PARAM_UPLOAD_ABORT,
    CTX_PARAM_UPLOAD_TIMEOUT,
    CTX_PARAM_UPLOAD_ERROR,
    CTX_PARAM_UPLOAD_PREINIT
} from "../core/Const";
import {
    resolveFinalUrl,
    resolveHandlerFunc,
    resoveNamingContainerMapper
} from "./RequestDataResolver";
const failSaveExecute = ExtLang.failSaveExecute;
import {ExtConfig} from "../util/ExtDomQuery";

/**
 * Faces XHR Request Wrapper
 * as AsyncRunnable for our Asynchronous queue
 * This means from the outside the
 * xhr request is similar to a Promise in a way
 * that you can add then and catch and finally callbacks.
 *
 *
 * The idea is that we basically just enqueue
 * a single ajax request into our queue
 * and let the queue do the processing.
 *
 *
 */

export class XhrRequest extends AsyncRunnable<XMLHttpRequest> {

    private readonly ERR_INVALID_RESPONSE = "Invalid Response";
    private readonly ERR_EMPTY_RESPONSE = "Empty Response";
    static readonly TYPE_CHECKBOX = "checkbox";
    static readonly TYPE_RADIO = "radio";

    private responseContext!: Config;
    private stopProgress = false;
    private xhrObject = new XMLHttpRequest();

    /**
     * Required Parameters
     *
     * @param requestContext the request context with all pass through values
     * @param internalContext internal context with internal info which is passed through, not used by the user
     * Optional Parameters
     * @param timeout optional xhr timeout
     * @param ajaxType optional request type, default "POST"
     * @param contentType optional content type, default "application/x-www-form-urlencoded"
     */
    constructor(
        private requestContext: ExtConfig,
        private internalContext: Config,
        private timeout = NO_TIMEOUT,
        private ajaxType = REQ_TYPE_POST,
        private contentType = URL_ENCODED
    ) {
        super();
        // we omit promises here because we have to deal with cancel functionality,
        // and promises to not provide that (yet) instead we have our async queue
        // which uses an api internally, which is very close to promises
        this.registerXhrCallbacks((data: any) => this.resolve(data), (data: any) => this.reject(data));
    }

    start(): IAsyncRunnable<XMLHttpRequest> {

        const ignoreErr = failSaveExecute;
        const xhrObject = this.xhrObject;
        const sourceForm = DQ.byId(this.internalContext.getIf(CTX_PARAM_SRC_FRM_ID).value);

        try {
            // encoded we need to decode
            // We generated a base representation of the current form
            // in case someone has overloaded the viewState with additional decorators we merge
            // that in, there is no way around it, the spec allows it and getViewState
            // must be called, so whatever getViewState delivers has higher priority then
            // whatever the formData object delivers
            // the partialIdsArray arr is almost deprecated legacy code where we allowed to send a separate list of partial
            // ids for reduced load and server processing, this will be removed soon, we can handle the same via execute
            const executes = this.requestContext.getIf(CTX_PARAM_REQ_PASS_THR, P_EXECUTE).get(IDENT_NONE).value.split(/\s+/gi);
            const partialIdsArray = this.internalContext.getIf(CTX_PARAM_PPS).value === true ? executes : [];
            const formData: XhrFormData = new XhrFormData(
                sourceForm,
                resoveNamingContainerMapper(this.internalContext),
                executes, partialIdsArray
            );

            // next step the pass through parameters are merged in for post params
            this.requestContext.$nspEnabled = false;
            const requestContext = this.requestContext;
            const requestPassThroughParams = requestContext.getIf(CTX_PARAM_REQ_PASS_THR) as ExtConfig;

            // we are turning off here the jsf, faces remapping because we are now dealing with
            // pass-through parameters
            requestPassThroughParams.$nspEnabled = false;
            // this is an extension where we allow pass through parameters to be sent down additionally
            // this can be used and is used in the impl to enrich the post request parameters with additional
            // information
            try {
                formData.shallowMerge(requestPassThroughParams, true, true);
            } finally {
                // unfortunately as long as we support
                // both namespaces we have to keep manual control
                // on the key renaming before doing ops like deep copy
                this.requestContext.$nspEnabled = true;
                requestPassThroughParams.$nspEnabled = true;
            }

            this.appendIssuingItem(formData);

            this.responseContext = requestPassThroughParams.deepCopy;

            // we have to shift the internal passthroughs around to build up our response context
            const responseContext = this.responseContext;

            responseContext.assign(CTX_PARAM_MF_INTERNAL).value = this.internalContext.value;

            // per spec the onEvent and onError handlers must be passed through to the response
            responseContext.assign(ON_EVENT).value = requestContext.getIf(ON_EVENT).value;
            responseContext.assign(ON_ERROR).value = requestContext.getIf(ON_ERROR).value;

            xhrObject.open(this.ajaxType, resolveFinalUrl(sourceForm, formData, this.ajaxType), true);

            if (this.timeout) xhrObject.timeout = this.timeout;

            // a bug in the xhr stub library prevents the setRequestHeader to be properly executed on fake xhr objects
            // normal browsers should resolve this
            // tests can quietly fail on this one
            if (!formData.isMultipartRequest) {
                ignoreErr(() => xhrObject.setRequestHeader(CONTENT_TYPE, `${this.contentType}; charset=utf-8`));
            }

            ignoreErr(() => xhrObject.setRequestHeader(HEAD_FACES_REQ, VAL_AJAX));
            // some webkit based mobile browsers do not follow the w3c spec for Accept headers
            ignoreErr(() => xhrObject.setRequestHeader(REQ_ACCEPT, STD_ACCEPT));

            this.sendEvent(BEGIN);
            this.sendRequest(formData);
        } catch (e) {
            // this happens usually in a client side condition, hence we have to deal in with it in a client
            // side manner
            this.handleErrorAndClearQueue(e);
            throw e;
        }
        return this;
    }

    cancel() {
        try {
            // this causes onError to be called where the error
            // handling takes over
            this.xhrObject.abort();
        } catch (e) {
            this.handleError(e);
        }
    }


    /**
     * attaches the internal event and processing
     * callback within the promise to our xhr object
     *
     * @param resolve
     * @param reject
     */
    private registerXhrCallbacks(resolve: Consumer<any>, reject: Consumer<any>) {
        const xhrObject = this.xhrObject;

        xhrObject.onabort = () => {
            this.onAbort(resolve, reject);
        };
        xhrObject.ontimeout = () => {
            this.onTimeout(resolve, reject);
        };
        xhrObject.onload = () => {
            this.onResponseReceived(resolve)
        };
        xhrObject.onloadend = () => {
            this.onResponseProcessed(this.xhrObject, resolve);
        };

        this.registerUploadCallbacks(xhrObject);

        xhrObject.onerror = (errorData: any) => {
            // Older Safari/WebKit and Chrome/Chromium versions can cancel XHRs during
            // navigation or download handoff by triggering onerror with status=0/readyState=4.
            // Treat that as queue cleanup rather than reporting a user-facing Ajax error.
            if (this.isCancelledResponse(this.xhrObject)) {
                /*
                 * this triggers the catch chain and after that finally
                 */
                this.stopProgress = true;
                reject();
                return;
            }
            // error already processed somewhere else
            if (this.stopProgress) {
                return;
            }
            this.handleError(errorData);
        };
    }

    private registerUploadCallbacks(xhrObject: XMLHttpRequest): void {
        if (!xhrObject?.upload) {
            return;
        }
        // fire the pre-init hook so callers can inspect the upload object before any transfer starts
        this.internalContext.getIf(CTX_PARAM_UPLOAD_PREINIT).value?.(xhrObject.upload);
        xhrObject.upload.addEventListener("progress", (event: ProgressEvent) => {
            this.internalContext.getIf(CTX_PARAM_UPLOAD_ON_PROGRESS).value?.(xhrObject.upload, event);
        });
        xhrObject.upload.addEventListener("load", (event: ProgressEvent) => {
            this.internalContext.getIf(CTX_PARAM_UPLOAD_LOAD).value?.(xhrObject.upload, event);
        });
        xhrObject.upload.addEventListener("loadstart", (event: ProgressEvent) => {
            this.internalContext.getIf(CTX_PARAM_UPLOAD_LOADSTART).value?.(xhrObject.upload, event);
        });
        xhrObject.upload.addEventListener("loadend", (event: ProgressEvent) => {
            this.internalContext.getIf(CTX_PARAM_UPLOAD_LOADEND).value?.(xhrObject.upload, event);
        });
        xhrObject.upload.addEventListener("abort", (event: ProgressEvent) => {
            this.internalContext.getIf(CTX_PARAM_UPLOAD_ABORT).value?.(xhrObject.upload, event);
        });
        xhrObject.upload.addEventListener("timeout", (event: ProgressEvent) => {
            this.internalContext.getIf(CTX_PARAM_UPLOAD_TIMEOUT).value?.(xhrObject.upload, event);
        });
        xhrObject.upload.addEventListener("error", (event: ProgressEvent) => {
            this.internalContext.getIf(CTX_PARAM_UPLOAD_ERROR).value?.(xhrObject.upload, event);
        });
    }

    private isCancelledResponse(currentTarget: XMLHttpRequest): boolean {
        return currentTarget?.status === 0 && // cancelled internally by browser
            currentTarget?.readyState === 4 &&
            currentTarget?.responseText === '' &&
            currentTarget?.responseXML === null;
    }

    /*
     * xhr processing callbacks
     *
     * Those methods are the callbacks called by
     * the xhr object depending on its own state
     */
    private onAbort(_resolve: Consumer<any>, reject: Consumer<any>) {
        this.handleHttpError(reject);
    }

    private onTimeout(resolve: Consumer<any>, _reject: Consumer<any>) {
        this.sendEvent(STATE_EVT_TIMEOUT);
        this.handleHttpError(resolve);
    }

    /**
     * the response is received and normally is a normal response
     * but also can be some kind of error (http code >= 300)
     * In any case the response will be resolved either as error or response
     * and the next item in the queue will be processed
     * @param resolve
     * @private
     */
    private onResponseReceived(resolve: Consumer<any>) {

        this.sendEvent(COMPLETE);

        //request error resolution as per spec:
        if(!this.processRequestErrors(resolve)) {
            $faces().ajax.response(this.xhrObject, this.responseContext.value ?? {});
        }
    }

    private processRequestErrors(resolve: Consumer<any>): boolean {
        const responseXML = new XMLQuery(this.xhrObject?.responseXML as any);
        const responseText = this.xhrObject?.responseText ?? "";
        const responseCode = this.xhrObject?.status ?? -1;

        // HTTP status takes priority: a non-2xx response is always an HTTP error,
        // regardless of what the body contains (e.g. an HTML error page from a 404
        // must not be misreported as malformedXML).
        if (responseCode >= 300 || responseCode < 200) {
            this.handleHttpError(resolve);
            return true;
        }
        if(responseXML.isXMLParserError()) {
            // Firefox: malformed XML produces a Document with <parsererror>
            this.handleGenericResponseError(this.ERR_INVALID_RESPONSE, "The response xml is invalid", MALFORMEDXML, resolve);
            return true;
        } else if(responseXML.isAbsent() && responseText.trim().length > 0) {
            // Chrome: responseXML is null for unparseable XML, but responseText has content
            this.handleGenericResponseError(this.ERR_INVALID_RESPONSE, "The response xml is invalid", MALFORMEDXML, resolve);
            return true;
        } else if(responseXML.isAbsent()) {
            // Truly empty response
            this.handleGenericResponseError(this.ERR_EMPTY_RESPONSE, "The response has provided no data", EMPTY_RESPONSE, resolve);
            return true;
        }
        return false;
    }


    private handleGenericResponseError(errorName: string, errorMessage: string, responseStatus: string, resolve: (s?: any) => void) {
        const errorData = new ErrorData(
            this.internalContext.getIf(CTX_PARAM_SRC_CTL_ID).value,
            errorName, errorMessage,
            this.xhrObject?.responseText ?? "",
            this.xhrObject?.responseXML ?? null,
            this.xhrObject.status,
            responseStatus
        );
        this.finalizeError(errorData, resolve);
    }

    private handleHttpError(resolveOrReject: Consumer<any>, errorMessage: string = "Generic HTTP Error") {
        this.stopProgress = true;

        const errorData = new ErrorData(
            this.internalContext.getIf(CTX_PARAM_SRC_CTL_ID).value,
            HTTP_ERROR, errorMessage,
            this.xhrObject?.responseText ?? "",
            this.xhrObject?.responseXML ?? null,
            this.xhrObject?.status ?? -1,
            HTTP_ERROR
        )
        this.finalizeError(errorData, resolveOrReject);
    }

    private finalizeError(errorData: ErrorData, resolveOrReject: Consumer<any>) {
        try {
            this.handleError(errorData, true);
        } finally {
            // we issue a resolveOrReject in this case to allow the system to recover
            // reject would clean up the queue
            // resolve would trigger the next element in the queue to be processed
            resolveOrReject(errorData);
            this.stopProgress = true;
        }
    }

    private onResponseProcessed(data: any, resolve: Consumer<any>) {
        if (!this.stopProgress) {
            resolve(data);
        }
    }

    private sendRequest(formData: XhrFormData) {
        if (this.ajaxType === REQ_TYPE_GET) {
            this.xhrObject.send(null);
            return;
        }
        this.xhrObject.send(formData.isMultipartRequest ? formData.toFormData() : formData.toString());
    }

    /*
     * other helpers
     */
    private sendEvent(evtType: string) {
        const eventData = EventData.createFromRequest(this.xhrObject, this.internalContext, this.requestContext, evtType);
        try {
            // User code error, we might cover
            // this in onError, but also we cannot swallow it.
            // We need to resolve the local handlers lazily,
            // because some frameworks might decorate them over the context in the response
            const eventHandler = resolveHandlerFunc(this.requestContext, this.responseContext, ON_EVENT);
            Implementation.sendEvent(eventData, eventHandler);
        } catch (e) {
            e.source = e?.source ?? this.requestContext.getIf(SOURCE).value;
            // this is a client error, no save state anymore for queue processing!
            this.handleErrorAndClearQueue(e);
            // we forward the error upward like all client side errors
            throw e;
        }
    }

    private handleErrorAndClearQueue(e: any, responseFormatError: boolean = false) {
        this.handleError(e, responseFormatError);
        this.reject(e);
    }

    private handleError(exception: any, responseFormatError: boolean = false) {
        const errorData = responseFormatError
            ? ErrorData.fromHttpConnection(exception.source, exception.type, exception.message ?? EMPTY_STR, exception.responseText, exception.responseXML, exception.responseCode, exception.status)
            : ErrorData.fromClient(exception);
        const eventHandler = resolveHandlerFunc(this.requestContext, this.responseContext, ON_ERROR);
        Implementation.sendError(errorData, eventHandler);
    }

    private appendIssuingItem(formData: XhrFormData) {
        const issuingItemId = this.internalContext.getIf(CTX_PARAM_SRC_CTL_ID).value;

        //to avoid sideffects with buttons we only can append the issuing item if no behavior event is set
        //MYFACES-4679!
        const eventType = formData.getIf($nsp(P_BEHAVIOR_EVENT)).value?.[0] ?? null;
        const isBehaviorEvent = !!eventType && eventType !== 'click';

        //not encoded
        if(issuingItemId && formData.getIf(issuingItemId).isAbsent() && !isBehaviorEvent) {
            const issuingItem = DQ.byId(issuingItemId);
            const itemValue = issuingItem.inputValue;
            const arr = new ExtConfig({});
            const type: string = issuingItem.type.orElse("").value.toLowerCase();

            //Checkbox and radio only value pass if checked is set, otherwise they should not show
            //up at all, and if checked is set, they either can have a value or simply being boolean
            const isCheckable = type === XhrRequest.TYPE_CHECKBOX || type === XhrRequest.TYPE_RADIO;
            if (isCheckable && !issuingItem.checked) {
                return;
            } else if (isCheckable) {
                arr.assign(issuingItemId).value = itemValue.orElse(true).value;
            } else if (itemValue.isPresent()) {
                arr.assign(issuingItemId).value = itemValue.value;
            }

            formData.shallowMerge(arr, true, true);
        }
    }
}
