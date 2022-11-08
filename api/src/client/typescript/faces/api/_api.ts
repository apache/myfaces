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
import {Implementation} from "../impl/AjaxImpl";
import {PushImpl} from "../impl/PushImpl";
import {oam as _oam} from "../myfaces/OamSubmit";
import {$nsp, CTX_PARAM_EXECUTE, CTX_PARAM_RENDER, P_BEHAVIOR_EVENT} from "../impl/core/Const";
import {ErrorData} from "../impl/xhrCore/ErrorData";
import {EventData} from "../impl/xhrCore/EventData";

//we use modules to get a proper jsdoc and static/map structure in the calls
//as per spec requirement
export module faces {


    /**
     * Version of the implementation for the faces.ts.
     * <p />
     * as specified within the jsf specifications faces.html:
     * <ul>
     * <li>left two digits major release number</li>
     * <li>middle two digits minor spec release number</li>
     * <li>right two digits bug release number</li>
     * </ul>
     * @constant
     */
    export var specversion = 400000;
    /**
     * Implementation version as specified within the jsf specification.
     * <p />
     * A number increased with every implementation version
     * and reset by moving to a new spec release number
     *
     * @constant
     */
    export var implversion = 0;

    /**
     * SeparatorChar as defined by facesContext.getNamingContainerSeparatorChar()
     */
    export var separatorchar: string = getSeparatorChar();

    // noinspection JSUnusedGlobalSymbols
    /**
     * Context Path as defined externalContext.requestContextPath
     */
    export var contextpath: string = '#{facesContext.externalContext.requestContextPath}';
    // we do not have a fallback here, for now

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
    export function getProjectStage(): string {
        return Implementation.getProjectStage();
    }

    /**
     * collect and encode data for a given form element (must be of type form)
     * find the jakarta.faces.ViewState element and encode its value as well!
     * return a concatenated string of the encoded values!
     *
     * @throws an exception in case of the given element not being of type form!
     * https://issues.apache.org/jira/browse/MYFACES-2110
     */
    export function getViewState(formElement: Element | string): string {
        return Implementation.getViewState(formElement);
    }

    /**
     * returns the window identifier for the given node / window
     * @return the window identifier or null if none is found
     * @param rootNode
     */
    export function getClientWindow(rootNode?: Element | string): string {
        return Implementation.getClientWindow(rootNode);
    }

    //private helper functions
    function getSeparatorChar(): string {
        const sep = '#{facesContext.namingContainerSeparatorChar}';
        //We now enable standalone mode, the separator char was not mapped we make a fallback to 2.3 behavior
        //the idea is that the separator char is provided from the underlying container, but if not then we
        //will perform a fallback (aka 2.3 has the url fallback behavior)
        return (sep.match(/\#\{facesContext.namingContainerSeparatorChar\}/gi)) ? Implementation.getSeparatorChar() : sep;
    }




    export module ajax {
        "use strict";

        /**
         * this function has to send the ajax requests
         *
         * following requestInternal conditions must be met:
         * <ul>
         *  <li> the requestInternal must be sent asynchronously! </li>
         *  <li> the requestInternal must be a POST!!! requestInternal </li>
         *  <li> the requestInternal url must be the form action attribute </li>
         *  <li> all requests must be queued with a client side requestInternal queue to ensure the requestInternal ordering!</li>
         * </ul>
         *
         * @param {String|Node} element: any dom element no matter being it html or jsf, from which the event is emitted
         * @param {EVENT} event: any javascript event supported by that object
         * @param {Map} options : map of options being pushed into the ajax cycle
         */
        export function request(element: Element, event?: Event, options?: Context): void {
            Implementation.request(element, event, options)
        }

        /**
         * response handler
         * @param request the request object having triggered this response
         * @param context the request context
         *
         * TODO add info on what can be in the context
         */
        export function response(request: XMLHttpRequest, context?: Context): void {
            Implementation.response(request, context);
        }

        /**
         * Adds an error handler to our global error queue.
         * the error handler must be of the format <i>function errorListener(&lt;errorData&gt;)</i>
         * with errorData being of following format:
         * <ul>
         *     <li> errorData.type : &quot;error&quot;</li>
         *     <li> errorData.status : the error status message</li>
         *     <li> errorData.serverErrorName : the server error name in case of a server error</li>
         *     <li> errorData.serverErrorMessage : the server error message in case of a server error</li>
         *     <li> errorData.source  : the issuing source element which triggered the requestInternal </li>
         *     <li> eventData.responseCode: the response code (aka http requestInternal response code, 401 etc...) </li>
         *     <li> eventData.responseText: the requestInternal response text </li>
         *     <li> eventData.responseXML: the requestInternal response xml </li>
         * </ul>
         *
         * @param errorListener error handler must be of the format <i>function errorListener(&lt;errorData&gt;)</i>
         */
        export function addOnError(errorFunc: (data: ErrorData) => void): void {
            Implementation.addOnError(<any>errorFunc);
        }

        /**
         * Adds a global event listener to the ajax event queue. The event listener must be a function
         * of following format: <i>function eventListener(&lt;eventData&gt;)</i>
         *
         * @param eventListener event must be of the format <i>function eventListener(&lt;eventData&gt;)</i>
         */
        export function addOnEvent(eventFunc: (data: EventData) => void): void {
            Implementation.addOnEvent(<any>eventFunc);
        }
    }

    export module util {

        /**
         * varargs function which executes a chain of code (functions or any other code)
         *
         * if any of the code returns false, the execution
         * is terminated prematurely skipping the rest of the code!
         *
         * @param {DomNode} source, the callee object
         * @param {Event} event, the event object of the callee event triggering this function
         * @param funcs ... arbitrary array of functions or strings
         * @returns true if the chain has succeeded false otherwise
         */
        export function chain(source, event, ...funcs: Array<Function | string>): boolean {
            return Implementation.chain(source, event, ...(funcs as EvalFuncs));
        }
    }

    export module push {
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
        export function init(socketClientId: string,
                    url: string,
                    channel: string,
                    onopen: Function,
                    onmessage: Function,
                    onerror: Function,
                    onclose: Function,
                    behaviors: any,
                    autoConnect: boolean): void {
            PushImpl.init(socketClientId, url, channel, onopen, onmessage, onerror, onclose, behaviors, autoConnect);
        }

        /**
         * Open the web socket on the given channel.
         * @param  channel The name of the web socket channel.
         * @throws  Error is thrown, if the channel is unknown.
         */
        export function open(socketClientId: string): void {
            PushImpl.open(socketClientId);
        }

        /**
         * Close the web socket on the given channel.
         * @param  channel The name of the web socket channel.
         * @throws  Error is thrown, if the channel is unknown.
         */
        export function close(socketClientId: string): void {
            PushImpl.close(socketClientId);
        }

    }
}

export module myfaces {
    /**
     * AB function similar to mojarra and Primefaces
     * not part of the spec but a convenience accessor method
     * Code provided by Thomas Andraschko
     *
     * @param source the event source
     * @param event the event
     * @param eventName event name for java.jakarta.faces.behavior.evemnt
     * @param execute execute list as passed down in faces.ajax.request
     * @param render
     * @param options
     */
    export function ab(source: Element, event: Event, eventName: string, execute: string, render: string, options: Context = {}): void {
        if (eventName) {
           options[$nsp(P_BEHAVIOR_EVENT)] = eventName;
        }
        if (execute) {
            options[CTX_PARAM_EXECUTE] = execute;
        }
        if (render) {
            options[CTX_PARAM_RENDER] = render;
        }

        (window?.faces ?? window.jsf).ajax.request(source, event, options);
    }

    /**
     * legacy oam functions
     */
    export const oam = _oam;
}


