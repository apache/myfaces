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

import {IListener} from "./util/IListener";
import {Response} from "./xhrCore/Response";
import {XhrRequest} from "./xhrCore/XhrRequest";
import {Config, DQ, DQ$, Lang, Optional} from "mona-dish";
import {Assertions} from "./util/Assertions";
import {ExtConfig, ExtDomQuery} from "./util/ExtDomQuery";
import {ErrorData} from "./xhrCore/ErrorData";
import {EventData} from "./xhrCore/EventData";
import {ExtLang} from "./util/Lang";

import {
    CTX_OPTIONS_EXECUTE,
    CTX_PARAM_REQ_PASS_THR,
    CTX_PARAM_SRC_CTL_ID,
    CTX_PARAM_SRC_FRM_ID,
    IDENT_ALL,
    IDENT_FORM,
    IDENT_NONE,
    IDENT_THIS,
    MYFACES,
    ON_ERROR,
    ON_EVENT,
    P_AJAX,
    P_CLIENT_WINDOW,
    P_EVT,
    P_EXECUTE,
    P_AJAX_SOURCE,
    P_RENDER,
    P_RESET_VALUES,
    P_WINDOW_ID,
    CTX_PARAM_RENDER,
    SOURCE,
    HTML_TAG_FORM,
    CTX_OPTIONS_PARAMS,
    VIEW_ID,
    $faces,
    EMPTY_STR,
    NAMED_VIEWROOT,
    NAMING_CONTAINER_ID,
    CTX_PARAM_PPS,
    MYFACES_OPTION_PPS,
    $nsp,
    CTX_PARAM_UPLOAD_ON_PROGRESS,
    CTX_PARAM_UPLOAD_PREINIT,
    CTX_PARAM_UPLOAD_LOADSTART,
    CTX_PARAM_UPLOAD_LOADEND,
    CTX_PARAM_UPLOAD_LOAD, CTX_PARAM_UPLOAD_ERROR, CTX_PARAM_UPLOAD_ABORT, CTX_PARAM_UPLOAD_TIMEOUT
} from "./core/Const";
import {
    resolveDefaults,
    resolveDelay,
    resolveForm,
    resolveTimeout, resolveViewId, resolveViewRootId, resoveNamingContainerMapper
} from "./xhrCore/RequestDataResolver";
import {encodeFormData} from "./util/FileUtils";
import {XhrQueueController} from "./util/XhrQueueController";

/*
 * allowed project stages
 */
enum ProjectStages {
    Production = "Production",
    Development = "Development",
    SystemTest = "SystemTest",
    UnitTest = "UnitTest"
}

/*
 *   Block-filter for the pass-through filtering; the attributes given here
 *   will not be transmitted from the options into the pass-through
 */
enum BlockFilter {
    onerror = "onerror",
    onevent = "onevent",
    render = "render",
    execute = "execute",
    myfaces = "myfaces",
    delay = "delay",
    timeout = "timeout",
    resetValues = "resetValues",
    windowId = "windowId",
    params = "params"
}



/**
 * Core Implementation
 * to distinct between api and impl
 *
 * The original idea was to make the implementation pluggable
 * but this is pointless, you always can overwrite the thin api layer
 * however a dedicated api makes sense for readability reasons
 */
export module Implementation {
    /*
     Small internal explanation, this code is optimized for readability
     and cuts off a ton of old legacy code.
     Aka older browsers are not supported anymore.
     We use a self written helper library to keep the number of external
     code dependencies down.
     The library is called mona-dish and started as a small sideproject of mine
     it provides following

     a) Monad like structures for querying because this keeps the code denser and adds abstractions
     that always was the strong point of jQuery, and it still is better in this regard than what ecmascript provides

     c) A neutral json like configuration which allows assignments of arbitrary values with reduce code which then can be
     transformed into different data representations

     examples:
     internalCtx.assign(MYPARAM, CTX_PARAM_SRC_FRM_ID).value = form.id.value;
     passes a value into context.MYPARAM.CTX_PARAM_SRC_FRM_ID

     basically an abbreviation for

     internalCtxt[MYPARAM] = internalCtxt?.[MYPARAM] ?  internalCtxt[MYPARAM] : {};
     internalCtxt[MYPARAM][CTX_PARAM_SRC_FRM_ID] = internalCtxt?.[MYPARAM][CTX_PARAM_SRC_FRM_ID] ?  internalCtxt[MYPARAM][CTX_PARAM_SRC_FRM_ID] : {};
     internalCtxt[MYPARAM][CTX_PARAM_SRC_FRM_ID] = form.id.value;


     internalCtx.assign(condition, MYPARAM, CTX_PARAM_SRC_FRM_ID).value = form.id.value;
     passes a value into context.MYPARAM.CTX_PARAM_SRC_FRM_ID if condition === true otherwise it is ignored

     abbreviates:
     if(condition) {
        internalCtxt[MYPARAM] = internalCtxt?.[MYPARAM] ?  internalCtxt[MYPARAM] : {};
        internalCtxt[MYPARAM][CTX_PARAM_SRC_FRM_ID] = internalCtxt?.[MYPARAM][CTX_PARAM_SRC_FRM_ID] ?  internalCtxt[MYPARAM][CTX_PARAM_SRC_FRM_ID] : {};
        internalCtxt[MYPARAM][CTX_PARAM_SRC_FRM_ID] = form.id.value;
     }


     d) Optional constructs, while under heavy debate we only use them lightly where the api requires it from mona-dish

     Note the inclusion of this library uses a reduced build which only includes the part of it, which we really use

     */

    const trim = Lang.trim;
    const getMessage = ExtLang.getMessage;
    const getGlobalConfig = ExtLang.getGlobalConfig;
    import assert = Assertions.assert;
    const ofAssoc = ExtLang.ofAssoc;
    const collectAssoc = ExtLang.collectAssoc;

    let projectStage: string = null;
    let separator: string = null;
    let eventQueue = [];
    let errorQueue = [];
    export let requestQueue: XhrQueueController<XhrRequest> = null;
    /*error reporting threshold*/
    let threshold = "ERROR";

    /**
     * fetches the separator char from the given script tags
     *
     * @return {string} the separator char for the given script tags
     */
    export function getSeparatorChar(): string {
        return resolveGlobalConfig()?.separator ??
            this?.separator ??
            (separator = ExtDomQuery.searchJsfJsFor(/separator=([^&;]*)/).orElse(":").value);
    }

    /**
     * this is for testing purposes only, since AjaxImpl is a module
     * we need to reset for every unit test its internal states
     */
    export function reset() {
        projectStage = null;
        separator = null;
        eventQueue = [];
        errorQueue = [];
        requestQueue = null;
    }

    /**
     * @return the project stage also emitted by the server:
     * it cannot be cached and must be delivered over the server
     * The value for it comes from the requestInternal parameter of the faces.js script called "stage".
     */
    export function getProjectStage(): string | null {
        return resolveGlobalConfig()?.projectStage ??
            this?.projectStage ??
            (projectStage = resolveProjectStateFromURL());
    }

    /**
     * resolves the project stage as url parameter
     * @return the project stage or null
     */
    export function resolveProjectStateFromURL(): string | null {

        /* run through all script tags and try to find the one that includes faces.js */
        const foundStage = ExtDomQuery.searchJsfJsFor(/stage=([^&;]*)/).value as string;
        return (foundStage in ProjectStages) ? foundStage : ProjectStages.Production; // MYFACES-4572: default is production
    }

    /**
     * implementation of the faces.util.chain functionality
     *
     * @param source
     * @param event
     * @param funcs
     */
    export function chain(source: any, event: Event, ...funcs: EvalFuncs): boolean {
        // we can use our lazy stream each functionality to run our chain here.
        // by passing a boolean as return value into the onElem call
        // we can stop early at the first false, just like the spec requests

        let ret = true;
        funcs.every(func => {
            let returnVal = resolveAndExecute(source, event, func);
            if(returnVal === false) {
                ret = false;
            }
            //we short circuit in case of false and break the every loop
            return ret;
        });
        return ret;

    }

    /**
     * this function has to send the ajax requests
     *
     * following request conditions must be met:
     * <ul>
     *  <li> the request must be sent asynchronously! </li>
     *  <li> the request must be a POST!!! request </li>
     *  <li> the request url must be the form action attribute </li>
     *  <li> all requests must be queued with a client side request queue to ensure the request ordering!</li>
     * </ul>
     *
     * @param el any dom element no matter being it html or jsf, from which the event is emitted
     * @param event any javascript event supported by that object
     * @param opts  map of options being pushed into the ajax cycle
     *
     * a) transformArguments out of the function
     * b) passThrough handling with a map copy with a filter map block map
     */
    export function request(el: ElemDef, event?: Event, opts ?: Options) {

        const {
            options,
            elem,
            elementId,
            windowId,
            isResetValues
        } = resolveDefaults(event, opts, el);
        const requestCtx = new ExtConfig({});
        const internalCtx = new ExtConfig({});

        Assertions.assertRequestIntegrity(options, elem);

        /**
         * fetch the parent form
         *
         * note we also add an override possibility here
         * so that people can use dummy forms and work
         * with detached objects
         */
        const form: DQ = resolveForm(elem, event);
        const viewId: string = resolveViewId(form);
        const formId = form.id.value;
        const delay: number = resolveDelay(options);
        const timeout: number = resolveTimeout(options);

        requestCtx.assignIf(!!windowId, P_WINDOW_ID).value = windowId;

        // old non - spec behavior will be removed after it is clear whether the removal breaks any code
        requestCtx.assign(CTX_PARAM_REQ_PASS_THR).value = extractLegacyParams(options.value);

        // spec conform behavior, all passthrough params must be under "passthrough
        const params = remapArrayToAssocArr(options.getIf(CTX_OPTIONS_PARAMS).orElse({}).value);
        //we turn off the remapping for the param merge, because we do not want to have
        //any namespacing to be remapped

        let ctxPassthrough = requestCtx.getIf(CTX_PARAM_REQ_PASS_THR) as ExtConfig;
        ctxPassthrough.$nspEnabled = false;
        ctxPassthrough.shallowMerge(new Config(params), true);
        //now we turn it on again
        ctxPassthrough.$nspEnabled = true;
        requestCtx.assignIf(!!event, CTX_PARAM_REQ_PASS_THR, P_EVT).value = event?.type;

        /**
         * ajax pass through context with the source
         * onresolved Event and onerror Event
         */
        requestCtx.assign(SOURCE).value = elementId;

        requestCtx.assign(VIEW_ID).value = viewId;

        /**
         * on resolvedEvent and onError...
         * those values will be traversed later on
         * also into the response context
         */
        requestCtx.assign(ON_EVENT).value = options.value?.onevent;
        requestCtx.assign(ON_ERROR).value = options.value?.onerror;

        /**
         * Fetch the myfaces config params
         */
        requestCtx.assign(MYFACES).value = options.value?.myfaces;

        /**
         * binding contract the jakarta.faces.source must be set
         */
        requestCtx.assign(CTX_PARAM_REQ_PASS_THR, P_AJAX_SOURCE).value = elementId;

        /**
         * jakarta.faces.partial.ajax must be set to true
         */
        requestCtx.assign(CTX_PARAM_REQ_PASS_THR, P_AJAX).value = true;

        /**
         * if resetValues is set to true
         * then we have to set jakarta.faces.resetValues as well
         * as pass through parameter
         * the value has to be explicitly true, according to
         * the specs jsdoc
         */
        requestCtx.assignIf(isResetValues, CTX_PARAM_REQ_PASS_THR, P_RESET_VALUES).value = true;

        // additional meta information to speed things up, note internal non jsf
        // pass through options are stored under _mfInternal in the context
        internalCtx.assign(CTX_PARAM_SRC_FRM_ID).value = formId;

        /**
         * special myfaces only internal parameter for onProgress until we have an official api
         * that way we can track the progress of a xhr request (useful for file uploads)
         */
        internalCtx.assign(CTX_PARAM_UPLOAD_PREINIT).value = options.value?.myfaces?.upload?.preinit;
        internalCtx.assign(CTX_PARAM_UPLOAD_LOADSTART).value = options.value?.myfaces?.upload?.loadstart;
        internalCtx.assign(CTX_PARAM_UPLOAD_ON_PROGRESS).value = options.value?.myfaces?.upload?.progress;
        internalCtx.assign(CTX_PARAM_UPLOAD_LOADEND).value = options.value?.myfaces?.upload?.loadend;
        internalCtx.assign(CTX_PARAM_UPLOAD_LOAD).value = options.value?.myfaces?.upload?.load;
        internalCtx.assign(CTX_PARAM_UPLOAD_ERROR).value = options.value?.myfaces?.upload?.error;
        internalCtx.assign(CTX_PARAM_UPLOAD_ABORT).value = options.value?.myfaces?.upload?.abort;
        internalCtx.assign(CTX_PARAM_UPLOAD_TIMEOUT).value = options.value?.myfaces?.upload?.timeout;

        // mojarra compatibility, mojarra is sending the form id as well
        // this is not documented behavior but can be determined by running
        // mojarra under blackbox conditions.
        // I assume it does the same as our formId_submit=1 so leaving it out
        // won't hurt but for the sake of compatibility we are going to add it
        requestCtx.assign(CTX_PARAM_REQ_PASS_THR, formId).value = formId;
        internalCtx.assign(CTX_PARAM_SRC_CTL_ID).value = elementId;
        // reintroduction of PPS as per myfaces 2.3 (myfaces.pps = true, only the executes are submitted)
        internalCtx.assign(CTX_PARAM_PPS).value = extractMyFacesParams(options.value)?.[MYFACES_OPTION_PPS] ?? false;


        assignClientWindowId(form, requestCtx);
        assignExecute(options, requestCtx, form, elementId);
        assignRender(options, requestCtx, form, elementId);
        assignNamingContainerData(internalCtx, form);

        //now we enqueue the request as asynchronous runnable into our request
        //queue and let the queue take over the rest
        queueHandler.addRequestToQueue(elem, form, requestCtx, internalCtx, delay, timeout);
    }

    /**
     * Spec. 13.3.3
     * Examining the response markup and updating the DOM tree
     * @param {XMLHttpRequest} request - the ajax request
     * @param {Object} context - the ajax context
     */
    export function response(request: XMLHttpRequest, context: Context) {
        Response.processResponse(request, context);
    }

    /**
     * adds an error handler to the error queue
     *
     * @param errorListener the error listener handler
     */
    export function addOnError(errorListener: IListener<ErrorData>) {
        errorQueue.push(errorListener);
    }

    /**
     * adds an event handler to the event queue
     *
     * @param eventListener the event listener handler
     */
    export function addOnEvent(eventListener: IListener<EventData>) {
        eventQueue.push(eventListener);
    }

    // noinspection JSUnusedLocalSymbols
    /**
     * sends an event to the event handlers
     *
     * @param data the event data object hosting the event data according to the spec @see EventData for what is reachable
     * @param localHandler an optional event handler, which is processed before the event handler chain
     */
    export function sendEvent(data: EventData, localHandler = function (data: EventData) {
    }) {
        /*now we serve the queue as well*/
        localHandler(data);
        eventQueue.forEach(fn => fn(data));
    }

    /**
     * error handler behavior called internally
     * and only into the impl it takes care of the
     * internal message transformation to a myfaces internal error
     * and then uses the standard send error mechanisms
     * also a double error logging prevention is done as well
     *
     * @param request the request currently being processed
     * @param context the context affected by this error
     * @param exception the exception being thrown
     * @param clearRequestQueue if set to true, clears the request queue of all pending requests
     */
    export function stdErrorHandler(request: XMLHttpRequest,
                                    context: Config,
                                    exception: Error,
                                    clearRequestQueue = false) {
        //newer browsers do not allow to hold additional values on native objects like exceptions
        //we hence capsule it into the request, which is gced automatically
        //on ie as well, since the stdErrorHandler usually is called between requests
        //this is a valid approach
        try {
            if (threshold == "ERROR") {
                let errorData = ErrorData.fromClient(exception);
                sendError(errorData);
            }
        } finally {
            if (clearRequestQueue) {
                requestQueue.clear();
            }
        }
    }

    // noinspection JSUnusedLocalSymbols
    /**
     * implementation triggering the error chain
     *
     *
     *
     *  handles the errors, in case of an onError exists within the context the onError is called as local error handler
     *  the registered error handlers in the queue received an error message to be dealt with
     *  and if the projectStage is at development an alert box is displayed
     *
     *  note: we have additional functionality here, via the global config myfaces.config.defaultErrorOutput a function can be provided
     *  which changes the default output behavior from alert to something else
     *
     * @param errorData the error data to be displayed
     * @param localHandler an optional local error handler which has to be processed before the error handler queue
     */
    export function sendError(errorData: ErrorData, localHandler = function (data: ErrorData) {
    }) {

        localHandler(errorData);
        errorQueue.forEach((errorCallback: Function) => {
            errorCallback(errorData);
        });
        let displayError: (string) => void = getGlobalConfig("defaultErrorOutput", (console ? console.error : alert));
        displayError(errorData);
    }

    /**
     * @node optional element or id defining a rootnode where an element with the id "jakarta.faces.windowId" is hosted
     * @return the client window id of the current window, if one is given if none is found, null is returned
     */
    export function getClientWindow(node ?: Element | string): string | null {
        const ALTERED = "___mf_id_altered__";
        const INIT = "___init____";

        /*
         * the search root for the dom element search
         */


        let searchRoot = ((node) ? DQ.byId(node): DQ$("form"));
        let inputs = searchRoot
            .filterSelector(`input[name='${$nsp(P_CLIENT_WINDOW)}']`)
            .orElseLazy(() => searchRoot.querySelectorAll(`input[name='${$nsp(P_CLIENT_WINDOW)}']`))

        /*
         * lazy helper to fetch the window id from the included faces.js
         */
        let fetchWindowIdFromJSFJS = (): Optional<string> => ExtDomQuery.searchJsfJsFor(/jfwid=([^&;]*)/).orElse(null);

        /*
         * fetch window id from the url
         */
        let fetchWindowIdFromURL = function (): Optional<string> {
            const href = window.location.href, windowId = "jfwid";
            const regex = new RegExp("[\\?&]" + windowId + "=([^&#\\;]*)");
            const results = regex.exec(href);
            //initial trial over the url and a regexp
            if (results != null) return Optional.fromNullable(results[1]);
            return Optional.fromNullable(null);
        };

        /*
         * functional double check based on stream reduction
         * the values should be identical or on INIT value which is a premise to
         * skip the first check
         *
         * @param value1
         * @param value2
         */
        let differenceCheck = (value1: string, value2: string): string => {
            if(value1 == INIT) {
                return value2;
            } else if (value1 == ALTERED || value1 != value2) {
                return ALTERED;
            }
            return value2;
        };

        /*
         * helper for cleaner code, maps the value from an item
         *
         * @param item
         */
        let getValue = (item: DQ): string => item.val as string;
        /*
         * fetch the window id from the forms
         * window ids must be present in all forms
         * or non-existent. If they exist all of them must be the same
         */

        let formWindowId: string = inputs.asArray.map(getValue).reduce(differenceCheck, INIT);


        //if the resulting window id is set on altered then we have an unresolvable problem
        assert(ALTERED != formWindowId, "Multiple different windowIds found in document");

        /*
         * return the window id or null
         */
        return formWindowId != INIT ? formWindowId : (fetchWindowIdFromURL() || fetchWindowIdFromJSFJS()).value;
    }

    /**
     * collect and encode data for a given form element (must be of type form)
     * find the jakarta.faces.ViewState element and encode its value as well!
     * @return a concatenated string of the encoded values!
     *
     * @throws Error in case of the given element not being of type form!
     * https://issues.apache.org/jira/browse/MYFACES-2110
     */
    export function getViewState(form: Element | string): string {
        /**
         *  type-check assert!, we opt for strong typing here
         *  because it makes it easier to detect bugs
         */

        let element: DQ = DQ.byId(form, true);
        if (!element.isTag(HTML_TAG_FORM)) {
            throw new Error(getMessage("ERR_VIEWSTATE"));
        }

        // determine the naming container scenario
        const dummyContext = new Config({});
        assignNamingContainerData(dummyContext, DQ.byId(form))
        // fetch all non file input form elements
        let formElements = element.deepElements.encodeFormElement()

        // encode them! (file inputs are handled differently and are not part of the viewstate)
        return encodeFormData(new ExtConfig(formElements), resoveNamingContainerMapper(dummyContext));
    }

    /**
     * this at the first sight looks like a weird construct, but we need to do it this way
     * for testing, we cannot proxy addRequestToQueue from the testing frameworks directly,
     * but we need to keep it under unit tests.
     */
    export let queueHandler = {
        /**
         * public to make it accessible for tests
         *
         * adds a new request to our queue for further processing
         */
        addRequestToQueue: function (elem: DQ, form: DQ, reqCtx: ExtConfig, respPassThr: Config, delay = 0, timeout = 0) {
            requestQueue = requestQueue ?? new XhrQueueController<XhrRequest>();
            requestQueue.enqueue(new XhrRequest(reqCtx, respPassThr, timeout), delay);
        }
    };

    //----------------------------------------------- Methods ---------------------------------------------------------------------

    /**
     * the idea is to replace some placeholder parameters with their respective values
     * placeholder params like  @all, @none, @form, @this need to be replaced by
     * the values defined by the specification
     *
     * This function does it for the render parameters
     *
     * @param requestOptions the source options coming in as options object from faces.ajax.request (options parameter)
     * @param targetContext the receiving target context
     * @param issuingForm the issuing form
     * @param sourceElementId the executing element triggering the faces.ajax.request (id of it)
     */
    function assignRender(requestOptions: Config, targetContext: Config, issuingForm: DQ, sourceElementId: string) {
        if (requestOptions.getIf(CTX_PARAM_RENDER).isPresent()) {
            remapDefaultConstants(targetContext.getIf(CTX_PARAM_REQ_PASS_THR).get({}), P_RENDER, <string>requestOptions.getIf(CTX_PARAM_RENDER).value, issuingForm, <any>sourceElementId, targetContext.getIf(VIEW_ID).value);
        }
    }

    /**
     * the idea is to replace some placeholder parameters with their respective values
     * placeholder params like  @all, @none, @form, @this need to be replaced by
     * the values defined by the specification
     *
     * This function does it for the execute parameters
     *
     * @param requestOptions the source options coming in as options object from faces.ajax.request (options parameter)
     * @param targetContext the receiving target context
     * @param issuingForm the issuing form
     * @param sourceElementId the executing element triggering the faces.ajax.request (id of it)
     */
    function assignExecute(requestOptions: Config, targetContext: Config, issuingForm: DQ, sourceElementId: string) {

        if (requestOptions.getIf(CTX_OPTIONS_EXECUTE).isPresent()) {
            /*the options must be a blank delimited list of strings*/
            /*compliance with Mojarra which automatically adds @this to an execute
             * the spec rev 2.0a however states, if none is issued nothing at all should be sent down
             */
            requestOptions.assign(CTX_OPTIONS_EXECUTE).value = [requestOptions.getIf(CTX_OPTIONS_EXECUTE).value, IDENT_THIS].join(" ");
            remapDefaultConstants(targetContext.getIf(CTX_PARAM_REQ_PASS_THR).get({}), P_EXECUTE, <string>requestOptions.getIf(CTX_OPTIONS_EXECUTE).value, issuingForm, <any>sourceElementId, targetContext.getIf(VIEW_ID).value);
        } else {
            targetContext.assign(CTX_PARAM_REQ_PASS_THR, P_EXECUTE).value = sourceElementId;
        }
    }

    /**
     * apply the browser tab where the request was originating from
     *
     * @param form the form hosting the client window id
     * @param targetContext the target context receiving the value
     */
    function assignClientWindowId(form: DQ, targetContext: Config) {

        let clientWindow = $faces().getClientWindow(form.getAsElem(0).value);
        if (clientWindow) {
            targetContext.assign(CTX_PARAM_REQ_PASS_THR, P_CLIENT_WINDOW).value = clientWindow;
        }
    }

    /**
     * determines the current naming container
     * and assigns it internally
     *
     * @param internalContext
     * @param formElement
     * @private
     */
    function assignNamingContainerData(internalContext: Config, formElement: DQ) {
        const viewRootId = resolveViewRootId(formElement);

        if(!!viewRootId) {
            internalContext.assign(NAMED_VIEWROOT).value = true;
            internalContext.assign(NAMING_CONTAINER_ID).value = viewRootId;
        }
    }

    /**
     * transforms the user values to the expected values
     *  handling '@none', '@all', '@form', and '@this' appropriately.
     * (Note: Although we could employ a simple string replacement method,
     * it could result in duplicate entries under certain conditions.)
     *
     * Specific standardized constants such as
     * '@all', '@none', '@form', and '@this'
     * require special treatment.
     *
     * @param targetConfig the target configuration receiving the final values
     * @param targetKey the target key
     * @param userValues the passed user values (aka input string which needs to be transformed)
     * @param issuingForm the form where the issuing element originates
     * @param issuingElementId the issuing element
     * @param rootNamingContainerId the naming container id ("" default if none is given)
     */
    function remapDefaultConstants(targetConfig: Config, targetKey: string, userValues: string, issuingForm: DQ, issuingElementId: string, rootNamingContainerId: string = ""): Config {
        //a cleaner implementation of the transform list method
        const SEP = $faces().separatorchar;
        let iterValues: string[] = (userValues) ? trim(userValues).split(/\s+/gi) : [];
        let ret = [];
        let processed: {[key: string]: boolean} = {};

        /**
         * remaps the client ids for the portlet case so that the server
         * can deal with them either prefixed ir not
         * also resolves the absolute id case (it was assumed the server does this, but
         * apparently the RI does not, so we have to follow the RI behavior here)
         * @param componentIdToTransform the componentId which needs post-processing
         */
        const remapNamingContainer = componentIdToTransform => {
            // pattern :<anything> must be prepended by viewRoot if there is one,
            // otherwise we are in a not namespaced then only the id has to match
            const rootNamingContainerPrefix = (rootNamingContainerId.length) ? rootNamingContainerId+SEP : EMPTY_STR;
            let formClientId = issuingForm.id.value;
            // nearest parent naming container relative to the form
            const nearestNamingContainer = formClientId.substring(0, formClientId.lastIndexOf(SEP));
            const nearestNamingContainerPrefix = (nearestNamingContainer.length) ? nearestNamingContainer + SEP : EMPTY_STR;
            // Absolute search expressions, always start with SEP or the name of the root naming container
            const hasLeadingSep = componentIdToTransform.indexOf(SEP) === 0;
            const isAbsolutSearchExpr = hasLeadingSep || (rootNamingContainerId.length
                && componentIdToTransform.indexOf(rootNamingContainerPrefix) == 0);
            let finalIdentifier: string;
            if (isAbsolutSearchExpr) {
                //we cut off the leading sep if there is one
                componentIdToTransform = hasLeadingSep ? componentIdToTransform.substring(1) : componentIdToTransform;
                componentIdToTransform = componentIdToTransform.indexOf(rootNamingContainerPrefix) == 0 ? componentIdToTransform.substring(rootNamingContainerPrefix.length) : componentIdToTransform;
                //now we prepend either the prefix or "" from the cut-off string to get the final result
                finalIdentifier = [rootNamingContainerPrefix, componentIdToTransform].join(EMPTY_STR);
            } else { //relative search according to the javadoc
                //we cut off the root naming container id from the form
                if (formClientId.indexOf(rootNamingContainerPrefix) == 0) {
                    formClientId = formClientId.substring(rootNamingContainerPrefix.length);
                }

                //If prependId = true, the outer form id must be present in the id if same form
                let hasPrependId = componentIdToTransform.indexOf(formClientId) == 0;
                finalIdentifier = hasPrependId ?
                    [rootNamingContainerPrefix, componentIdToTransform].join(EMPTY_STR) :
                    [nearestNamingContainerPrefix,  componentIdToTransform].join(EMPTY_STR);
            }
            // We need to double-check because we have scenarios where we have a naming container
            // and no prepend (aka tobago testcase "must handle ':' in IDs properly", scenario 3,
            // in this case we return the component id, and be happy
            // we can roll a dom check here
            return DQ.byId(finalIdentifier).isPresent() ? finalIdentifier : componentIdToTransform;
        };

        // in this case we do not use lazy stream because it won´t bring any code reduction
        // or speedup
        for (let cnt = 0; cnt < iterValues.length; cnt++) {
            //avoid doubles
            if (iterValues[cnt] in processed) {
                continue;
            }
            switch (iterValues[cnt]) {
                //@none no values should be sent
                case IDENT_NONE:
                    return targetConfig.delete(targetKey);
                //@all is a pass through case according to the spec
                case IDENT_ALL:
                    targetConfig.assign(targetKey).value = IDENT_ALL;
                    return targetConfig;
                //@form pushes the issuing form id into our list
                case IDENT_FORM:
                    ret.push(remapNamingContainer(issuingForm.id.value));
                    processed[issuingForm.id.value] = true;
                    break;
                //@this is replaced with the current issuing element id
                case IDENT_THIS:
                    if (!(issuingElementId in processed)) {
                        ret.push(remapNamingContainer(issuingElementId));
                        processed[issuingElementId] = true;
                    }
                    break;
                default:
                    ret.push(remapNamingContainer(iterValues[cnt]));
                    processed[iterValues[cnt]] = true;
            }
        }

        targetConfig.assign(targetKey).value = ret.join(" ");
        return targetConfig;
    }

    /**
     * Filters the provided options using a blacklist to ensure
     * only pass-through parameters are processed for the Ajax request.
     *
     * Note that this issue is leftover from a previous implementation.
     * The specification-conforming behavior is to use parameters for pass-through values.
     * This will be addressed soon, after confirming that removal won't break any legacy code.
     *
     * @param {Context} mappedOpts - The options to be filtered.
     */
    function extractLegacyParams(mappedOpts: Options): {[key: string]: any} {
        //we now can use the full code reduction given by our stream api
        //to filter
        return ofAssoc(mappedOpts)
            .filter((item => !(item[0] in BlockFilter)))
            .reduce(collectAssoc, {});
    }

    /**
     * Extracts the MyFaces configuration parameters
     * that augment JSF with additional functionality.
     *
     * @param mappedOpts
     * @private
     */
    function extractMyFacesParams(mappedOpts: Options): {[key: string]: any} {
        //we now can use the full code reduction given by our stream api
        //to filter
        return ofAssoc(mappedOpts)
            .filter((item => (item[0] == "myfaces")))
            .reduce(collectAssoc, {})?.[MYFACES];
    }


    function remapArrayToAssocArr(arrayedParams: [[string, any]] | {[key: string]: any}): {[key: string]: any} {
        if(Array.isArray(arrayedParams)) {
            return arrayedParams.reduce(collectAssoc, {} as any);
        }
        return arrayedParams;
    }

    function resolveGlobalConfig(): any {
        return  window?.[MYFACES]?.config ?? {};
    }

    /**
     * Private helper to execute a function or code fragment
     * @param source the source of the caller passed into the function as this
     * @param event an event which needs to be passed down into the function
     * @param func either a function or code fragment
     * @return a boolean value, if the passed function returns false, then the
     * caller is basically notified that the execution can now stop (JSF requirement for chain)
     * @private
     */
    function resolveAndExecute(source: any, event: Event, func: Function | string): boolean {
        if ("string" != typeof func) {
            //function is passed down as chain parameter, can be executed as is
            return (<Function>func).call(source, event) !== false;
        } else {
            //either a function or a string can be passed in case of a string we have to wrap it into another function
            //it is not a plain executable code but a definition
            let sourceCode = trim(<string>func);
            if (sourceCode.indexOf("function ") == 0) {
                sourceCode = `return ${sourceCode} (event)`;
            }
            return new Function("event", sourceCode).call(source, event) !== false;
        }
    }
}
