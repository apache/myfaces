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

declare namespace faces {
    /**
     * Project stage values, mirroring jakarta.faces.application.ProjectStage.
     */
    export type ProjectStage = "Development" | "UnitTest" | "SystemTest" | "Production";
    /**
     * Status values sent to ajax event callbacks.
     */
    export type AjaxEventStatus = "begin" | "complete" | "success";
    /**
     * Status values sent to ajax error callbacks.
     */
    export type AjaxErrorStatus = "httpError" | "emptyResponse" | "malformedXML" | "serverError";
    /**
     * Common shape for the data passed to ajax callbacks.
     */
    export interface AjaxData {
        source?: Element;
        responseCode?: number;
        responseText?: string;
        responseXML?: XMLDocument;
    }
    /**
     * Data passed to ajax event callbacks.
     */
    export interface AjaxEvent extends AjaxData {
        type: "event";
        status: AjaxEventStatus;
    }
    /**
     * Data passed to ajax error callbacks.
     */
    export interface AjaxError extends AjaxData {
        type: "error";
        status: AjaxErrorStatus | "clientError" | "timeout";
        errorName?: string;
        errorMessage?: string;
        /** @deprecated MyFaces compatibility alias. */
        serverErrorName?: string;
        /** @deprecated MyFaces compatibility alias. */
        serverErrorMessage?: string;
        /** MyFaces compatibility detail. */
        description?: string;
        /** MyFaces compatibility detail. */
        typeDetails?: unknown;
    }
    /**
     * Version of the implementation for the faces.ts.
     * <p />
     * as specified within the jsf specifications faces.html:
     * <ul>
     * <li>left two digits major release number</li>
     * <li>middle two digits minor spec release number</li>
     * <li>right two digits bug release number</li>
     * </ul>
     */
    export const specversion: number;
    /**
     * Implementation version as specified within the jsf specification.
     * <p />
     * A number increased with every implementation version
     * and reset by moving to a new spec release number
     *
     */
    export const implversion: number;
    /**
     * SeparatorChar as defined by facesContext.getNamingContainerSeparatorChar()
     */
    export const separatorchar: string;
    /**
     * Context Path as defined externalContext.requestContextPath
     */
    export const contextpath: string;
    /**
     * This method is responsible for the return of a given project stage as defined
     * by the jsf specification.
     * <p/>
     * Valid return values are:
     * <ul>
     *     <li>&quot;Production&quot;</li>
     *     <li>&quot;Development&quot;</li>
     *     <li>&quot;SystemTest&quot;</li>
     *     <li>&quot;UnitTest&quot;</li>
     * </li>
     *
     * @return {String} the current project state emitted by the server side method:
     * <i>jakarta.faces.application.Application.getProjectStage()</i>
     */
    export function getProjectStage(): ProjectStage;
    /**
     * collect and encode data for a given form element (must be of type form)
     * find the jakarta.faces.ViewState element and encode its value as well!
     * return a concatenated string of the encoded values!
     *
     * @throws an exception in case of the given element not being of type form!
     * https://issues.apache.org/jira/browse/MYFACES-2110
     */
    export function getViewState(formElement: Element | string): string;
    /**
     * returns the window identifier for the given node / window
     * @return the window identifier or null if none is found
     * @param rootNode
     */
    export function getClientWindow(rootNode?: Element | string): string | null;
    export namespace ajax {
        /**
         * Callback signature for ajax lifecycle events.
         */
        export type OnEventCallback = (data: AjaxEvent) => void;
        /**
         * Callback signature for ajax errors.
         */
        export type OnErrorCallback = (data: AjaxError) => void;
        /**
         * Options object for faces.ajax.request.
         */
        export interface RequestOptions {
            execute?: string;
            render?: string;
            onevent?: OnEventCallback;
            onerror?: OnErrorCallback;
            params?: Record<string, string | number | boolean>;
            delay?: number | "none";
            resetValues?: boolean;
            /** MyFaces extension/pass-through compatibility. */
            [key: string]: any;
        }
        /**
         * Per-request context object passed to faces.ajax.response.
         */
        export interface RequestContext {
            sourceid?: string;
            onerror?: OnErrorCallback;
            onevent?: OnEventCallback;
            /** MyFaces extension/pass-through compatibility. */
            [key: string]: any;
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
         * @param {String|Node} element: any dom element no matter being it html or jsf, from which the event is emitted
         * @param {EVENT} event: any javascript event supported by that object
         * @param {Map} options : map of options being pushed into the ajax cycle
         */
        export function request(element: Element | string, event?: Event | null, options?: RequestOptions): void;
        /**
         * response handler
         * @param request the request object having triggered this response
         * @param context the request context
         *
         */
        export function response(request: XMLHttpRequest, context?: RequestContext): void;
        /**
         * Adds an error handler to our global error queue.
         * the error handler must be of the format <i>function errorListener(&lt;errorData&gt;)</i>
         * with errorData being of following format:
         * <ul>
         *     <li> errorData.type : &quot;error&quot;</li>
         *     <li> errorData.status : the error status message</li>
         *     <li> errorData.serverErrorName : the server error name in case of a server error</li>
         *     <li> errorData.serverErrorMessage : the server error message in case of a server error</li>
         *     <li> errorData.source  : the issuing source element which triggered the request </li>
         *     <li> eventData.responseCode: the response code (aka http request response code, 401 etc...) </li>
         *     <li> eventData.responseText: the request response text </li>
         *     <li> eventData.responseXML: the request response xml </li>
         * </ul>
         *
         * @param errorFunc error handler must be of the format <i>function errorListener(&lt;errorData&gt;)</i>
         */
        export function addOnError(errorFunc: OnErrorCallback): void;
        /**
         * Adds a global event listener to the ajax event queue. The event listener must be a function
         * of following format: <i>function eventListener(&lt;eventData&gt;)</i>
         *
         * @param eventFunc event must be of the format <i>function eventListener(&lt;eventData&gt;)</i>
         */
        export function addOnEvent(eventFunc: OnEventCallback): void;
    }
    export namespace util {
        /**
         * varargs function which executes a chain of code (functions or any other code)
         *
         * if any of the code returns false, the execution
         * is terminated prematurely skipping the rest of the code!
         *
         * @param {HTMLElement | String} source, the callee object
         * @param {Event} event, the event object of the callee event triggering this function
         * @param funcs ... arbitrary array of functions or strings
         * @returns true if the chain has succeeded false otherwise
         */
        export function chain(source: HTMLElement | string, event?: Event | null, ...funcs: Array<Function | string>): boolean;
    }
    export namespace push {
        /**
         * Invoked when the websocket is opened.
         */
        export type OnOpenHandler = (channel: string) => void;
        /**
         * Invoked when a message is received from the server.
         */
        export type OnMessageHandler = (message: unknown, channel: string, event: MessageEvent) => void;
        /**
         * Invoked when a connection error occurs and the websocket will attempt to reconnect.
         */
        export type OnErrorHandler = (code: number, channel: string, event: CloseEvent) => void;
        /**
         * Invoked when the websocket is closed and will not attempt to reconnect.
         */
        export type OnCloseHandler = (code: number, channel: string, event: CloseEvent) => void;
        /**
         * @param socketClientId the sockets client identifier
         * @param url the uri to reach the socket
         * @param channel the channel name/id
         * @param onopen The function to be invoked when the web socket is opened.
         * @param onmessage The function to be invoked when a message is received.
         * @param onerror The function to be invoked when an error occurs.
         * @param onclose The function to be invoked when the web socket is closed.
         * @param behaviors functions which are invoked whenever a message is received
         * @param autoConnect Whether or not to automatically open the socket. Defaults to <code>false</code>.
         */
        export function init(socketClientId: string, url: string, channel: string, onopen: OnOpenHandler | string | null, onmessage: OnMessageHandler | string | null, onerror: OnErrorHandler | string | null, onclose: OnCloseHandler | string | null, behaviors: Record<string, Array<() => void>>, autoConnect: boolean): void;
        /**
         * Open the web socket on the given channel.
         * @param  socketClientId The name of the web socket channel.
         * @throws  Error is thrown, if the channel is unknown.
         */
        export function open(socketClientId: string): void;
        /**
         * Close the web socket on the given channel.
         * @param  socketClientId The id of the web socket client.
         * @throws  Error is thrown, if the channel is unknown.
         */
        export function close(socketClientId: string): void;
    }
}

declare namespace myfaces {
    /**
     * AB function similar to mojarra and Primefaces
     * not part of the spec but a convenience accessor method
     * Code provided by Thomas Andraschko
     *
     * @param source the event source
     * @param event the event
     * @param eventName event name for java.jakarta.faces.behavior.event
     * @param execute execute list as passed down in faces.ajax.request
     * @param render the render list as string
     * @param options the options which need to be merged in
     * @param userParameters a set of user parameters which go into the final options under params, they can override whatever is passed via options
     */
    export function ab(source: Element, event: Event, eventName: string, execute: string, render: string, options?: faces.ajax.RequestOptions, userParameters?: faces.ajax.RequestOptions): void;
    /**
     * Helper function in the myfaces namespace to handle document ready properly for the load case
     * the ajax case, does not need proper treatment, since it is deferred anyway.
     * Used by command script as helper function!
     *
     * @param executionFunc the function to be executed upon ready
     */
    export function onDomReady(executionFunc: () => void): void;
    /**
     * reserve a namespace for the given string
     * @param namespace the namespace to reserve with '.' as separator
     */
    export function reserveNamespace(namespace: string): void;
    /**
     * legacy oam functions
     */
    export const oam: typeof oam;
}
