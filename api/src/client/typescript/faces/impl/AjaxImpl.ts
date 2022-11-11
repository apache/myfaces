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
import {AsynchronousQueue} from "./util/AsyncQueue";
import {AssocArrayCollector, Config, DQ, Lang, LazyStream, Optional, Stream} from "mona-dish";
import {Assertions} from "./util/Assertions";
import {XhrFormData} from "./xhrCore/XhrFormData";
import {ExtDomQuery} from "./util/ExtDomQuery";
import {ErrorData} from "./xhrCore/ErrorData";
import {EventData} from "./xhrCore/EventData";
import {ExtLang} from "./util/Lang";

import {
    CTX_PARAM_EXECUTE,
    CTX_PARAM_PASS_THR,
    CTX_PARAM_SRC_CTL_ID,
    CTX_PARAM_SRC_FRM_ID,
    CTX_PARAM_TR_TYPE,
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
    P_PARTIAL_SOURCE,
    P_RENDER,
    P_RESET_VALUES,
    P_WINDOW_ID,
    CTX_PARAM_RENDER,
    REQ_TYPE_POST,
    SOURCE,
    TAG_FORM, CTX_PARAM_SPEC_PARAMS
} from "./core/Const";
import {
    resolveDefaults,
    resolveDelay,
    resolveForm,
    resolveTimeout
} from "./xhrCore/RequestDataResolver";

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
 We use a self written helper library to keep the number of exernal
 code dependencies down.
 The library is called mona-dish and started as a small sideproject of mine
 it provides following

 a) Monad like structures for querying because this keeps the code denser and adds abstractions
 that always was the strong point of jquery and it still is better in this regard than what ecmascript provides

 b) Streams and lazystreams like java has, a pull like construct, ecmascript does not have anything like Lazystreams.
 Another option would have been rxjs but that would have introduced a code dependency and probably more code. We might
 move to RXJS if the need arises however. But for now I would rather stick with my small self grown library which works
 quite well and where I can patch quickly (I have used it in several industrial projects, so it works well
 and is heavily fortified by unit tests (140 testcases as time of writing this))

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

    import trim = Lang.trim;
    import getMessage = ExtLang.getMessage;
    import getGlobalConfig = ExtLang.getGlobalConfig;
    import assert = Assertions.assert;

    let projectStage: string = null;
    let separator: string = null;
    let eventQueue = [];
    let errorQueue = [];
    export let requestQueue: AsynchronousQueue<XhrRequest> = null;
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
        return (foundStage in ProjectStages) ? foundStage : null;
    }

    /**
     * implementation of the faces.util.chain functionality
     *
     * @param source
     * @param event
     * @param funcs
     */
    export function chain(source: any, event: Event, ...funcs: EvalFuncs): boolean {
        // we can use our lazy stream each functionality to run our chain here..
        // by passing a boolean as return value into the onElem call
        // we can stop early at the first false, just like the spec requests

        return LazyStream.of(...funcs)
            .map(func => resolveAndExecute(source, event, func))
            // we use the return false == stop as an early stop, onElem stops at the first false
            .onElem((opResult: boolean) => opResult)
            //last ensures we run until the first false is returned
            .last().value;
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
            resolvedEvent,
            options,
            elem,
            elementId,
            requestCtx,
            internalCtx,
            windowId,
            isResetValues
        } = resolveDefaults(event, opts, el);

        Assertions.assertRequestIntegrity(options, elem);

        /**
         * fetch the parent form
         *
         * note we also add an override possibility here
         * so that people can use dummy forms and work
         * with detached objects
         */
        const form: DQ = resolveForm(requestCtx, elem, resolvedEvent);
        const formId = form.id.value;
        const delay: number = resolveDelay(options);
        const timeout: number = resolveTimeout(options);

        requestCtx.assignIf(!!windowId, P_WINDOW_ID).value = windowId;

        // old non spec behavior will be removed after it is clear whether the removal breaks any code
        requestCtx.assign(CTX_PARAM_PASS_THR).value = filterPassThroughValues(options.value);

        // spec conform behavior, all passthrough params must be under "passthrough
        const params = remapArrayToAssocArr(options.getIf(CTX_PARAM_SPEC_PARAMS).orElse({}).value);
        requestCtx.getIf(CTX_PARAM_PASS_THR).shallowMerge(new Config(params), true);
        requestCtx.assignIf(!!resolvedEvent, CTX_PARAM_PASS_THR, P_EVT).value = resolvedEvent?.type;

        /**
         * ajax pass through context with the source
         * onresolved Event and onerror Event
         */
        requestCtx.assign(SOURCE).value = elementId;

        /**
         * on resolvedEvent and onError...
         * those values will be traversed later on
         * also into the response context
         */
        requestCtx.assign(ON_EVENT).value = options.value?.onevent;
        requestCtx.assign(ON_ERROR).value = options.value?.onerror;

        /**
         * lets drag the myfaces config params also in
         */
        requestCtx.assign(MYFACES).value = options.value?.myfaces;

        /**
         * binding contract the jakarta.faces.source must be set
         */
        requestCtx.assign(CTX_PARAM_PASS_THR, P_PARTIAL_SOURCE).value = elementId;

        /**
         * jakarta.faces.partial.ajax must be set to true
         */
        requestCtx.assign(CTX_PARAM_PASS_THR, P_AJAX).value = true;

        /**
         * if resetValues is set to true
         * then we have to set jakarta.faces.resetValues as well
         * as pass through parameter
         * the value has to be explicitly true, according to
         * the specs jsdoc
         */
        requestCtx.assignIf(isResetValues, CTX_PARAM_PASS_THR, P_RESET_VALUES).value = true;

        // additional meta information to speed things up, note internal non jsf
        // pass through options are stored under _mfInternal in the context
        internalCtx.assign(CTX_PARAM_SRC_FRM_ID).value = formId;

        // mojarra compatibility, mojarra is sending the form id as well
        // this is not documented behavior but can be determined by running
        // mojarra under blackbox conditions.
        // I assume it does the same as our formId_submit=1 so leaving it out
        // won't hurt but for the sake of compatibility we are going to add it
        requestCtx.assign(CTX_PARAM_PASS_THR, formId).value = formId;
        internalCtx.assign(CTX_PARAM_SRC_CTL_ID).value = elementId;
        internalCtx.assign(CTX_PARAM_TR_TYPE).value = REQ_TYPE_POST;

        assignClientWindowId(form, requestCtx);
        assignExecute(options, requestCtx, form, elementId);
        assignRender(options, requestCtx, form, elementId);

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
                requestQueue.cleanup();
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
        let searchRoot = new DQ(node || document.body).querySelectorAll(`form input [name='${P_CLIENT_WINDOW}']`);

        /*
         * lazy helper to fetch the window id from the window url
         */
        let fetchWindowIdFromUrl = () => ExtDomQuery.searchJsfJsFor(/jfwid=([^&;]*)/).orElse(null).value;

        /*
         * functional double check based on stream reduction
         * the values should be identical or on INIT value which is a premise to
         * skip the first check
         *
         * @param value1
         * @param value2
         */
        let differenceCheck = (value1: string, value2: string) => {
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
        let getValue = (item: DQ) => item.attr("value").value;
        /*
         * fetch the window id from the forms
         * window ids must be present in all forms
         * or non-existent. If they exist all of them must be the same
         */

        let formWindowId: Optional<string> = searchRoot.stream.map<string>(getValue).reduce(differenceCheck, INIT);


        //if the resulting window id is set on altered then we have an unresolvable problem
        assert(ALTERED != formWindowId.value, "Multiple different windowIds found in document");

        /*
         * return the window id or null
         */
        return formWindowId.value != INIT ? formWindowId.value : fetchWindowIdFromUrl();
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
        if (!element.isTag(TAG_FORM)) {
            throw new Error(getMessage("ERR_VIEWSTATE"));
        }

        let formData = new XhrFormData(element);
        return formData.toString();
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
        addRequestToQueue: function (elem: DQ, form: DQ, reqCtx: Config, respPassThr: Config, delay = 0, timeout = 0) {
            requestQueue = requestQueue ?? new AsynchronousQueue<XhrRequest>();
            requestQueue.enqueue(new XhrRequest(elem, form, reqCtx, respPassThr, [], timeout), delay);
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
            remapDefaultConstants(targetContext.getIf(CTX_PARAM_PASS_THR).get({}), P_RENDER, <string>requestOptions.getIf(CTX_PARAM_RENDER).value, issuingForm, <any>sourceElementId);
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

        if (requestOptions.getIf(CTX_PARAM_EXECUTE).isPresent()) {
            /*the options must be a blank delimited list of strings*/
            /*compliance with Mojarra which automatically adds @this to an execute
             * the spec rev 2.0a however states, if none is issued nothing at all should be sent down
             */
            requestOptions.assign(CTX_PARAM_EXECUTE).value = [requestOptions.getIf(CTX_PARAM_EXECUTE).value, IDENT_THIS].join(" ");
            remapDefaultConstants(targetContext.getIf(CTX_PARAM_PASS_THR).get({}), P_EXECUTE, <string>requestOptions.getIf(CTX_PARAM_EXECUTE).value, issuingForm, <any>sourceElementId);
        } else {
            targetContext.assign(CTX_PARAM_PASS_THR, P_EXECUTE).value = sourceElementId;
        }
    }

    /**
     * apply the browser tab where the request was originating from
     *
     * @param form the form hosting the client window id
     * @param targetContext the target context receiving the value
     */
    function assignClientWindowId(form: DQ, targetContext: Config) {

        let clientWindow = (window?.faces ?? window?.jsf).getClientWindow(form.getAsElem(0).value);
        if (clientWindow) {
            targetContext.assign(CTX_PARAM_PASS_THR, P_CLIENT_WINDOW).value = clientWindow;
        }
    }

    /**
     * transforms the user values to the expected one
     * with the proper none all form and this handling
     * (note we also could use a simple string replace but then
     * we would have had double entries under some circumstances)
     *
     * there are several standardized constants which need a special treatment
     * like @all, @none, @form, @this
     *
     * @param targetConfig the target configuration receiving the final values
     * @param targetKey the target key
     * @param userValues the passed user values (aka input string which needs to be transformed)
     * @param issuingForm the form where the issuing element originates
     * @param issuingElementId the issuing element
     */
    function remapDefaultConstants(targetConfig: Config, targetKey: string, userValues: string, issuingForm: DQ, issuingElementId: string): Config {
        //a cleaner implementation of the transform list method

        let iterValues: string[] = (userValues) ? trim(userValues).split(/\s+/gi) : [];
        let ret = [];
        let processed: {[key: string]: boolean} = {};

        // in this case we do not use lazy stream because it wont bring any code reduction
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
                    ret.push(issuingForm.id.value);
                    processed[issuingForm.id.value] = true;
                    break;
                //@this is replaced with the current issuing element id
                case IDENT_THIS:
                    if (!(issuingElementId in processed)) {
                        ret.push(issuingElementId);
                        processed[issuingElementId] = true;
                    }
                    break;
                default:
                    ret.push(iterValues[cnt]);
                    processed[iterValues[cnt]] = true;
            }
        }
        //We now add the target as joined list
        targetConfig.assign(targetKey).value = ret.join(" ");
        return targetConfig;
    }

    /**
     * Filter the options given with a blacklist, so that only
     * the values required for pass-through are processed in the ajax request
     *
     * Note this is a bug carried over from the old implementation
     * the spec conform behavior is to use params for passthrough values
     * this will be removed soon, after it is cleared up wheter removing
     * it breaks any legacy code
     *
     * @param {Context} mappedOpts the options to be filtered
     * @deprecated
     */
    function filterPassThroughValues(mappedOpts: Context): Context {
        //we now can use the full code reduction given by our stream api
        //to filter
        return Stream.ofAssoc(mappedOpts)
            .filter(item => !(item[0] in BlockFilter))
            .collect(new AssocArrayCollector());
    }

    function remapArrayToAssocArr(arrayedParams: [[string, any]] | {[key: string]: any}): {[key: string]: any} {
        if(Array.isArray(arrayedParams)) {
            return Stream.of(... arrayedParams).collect(new AssocArrayCollector());
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
