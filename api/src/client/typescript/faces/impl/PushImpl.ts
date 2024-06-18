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

/**
 * Typescript port of the faces\.push part in the myfaces implementation
 */
import {MAX_RECONNECT_ATTEMPTS, REASON_EXPIRED, RECONNECT_INTERVAL} from "./core/Const";
import {DQ} from "mona-dish";

/**
 * Implementation class for the push functionality
 */
export module PushImpl {

    const URL_PROTOCOL = DQ.global().location.protocol.replace("http", "ws") + "//";


    // we expose the member variables for testing purposes
    // they are not directly touched outside of tests

    /* socket map by token */
    export let sockets = {};
    /* component attributes by clientId */
    export let components = {};
    /* client ids by token (share websocket connection) */
    export let clientIdsByTokens = {};


    // needed for testing
    export function reset() {
        sockets = {};
        components = {}
        clientIdsByTokens = {}
    }

    /*
     * Api implementations, exposed functions
     */

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
                         autoConnect: boolean) {
        onclose = resolveFunction(onclose);

        if (!DQ.global().WebSocket) { // IE6-9.
            onclose(-1, channel);
            return;
        }

        let channelToken = url.substr(url.indexOf('?') + 1);

        if (!components[socketClientId]) {
            components[socketClientId] = {
                'channelToken': channelToken,
                'onopen': resolveFunction(onopen),
                'onmessage' : resolveFunction(onmessage),
                'onerror' : resolveFunction(onerror),
                'onclose': onclose,
                'behaviors': behaviors,
                'autoconnect': autoConnect};
            if (!clientIdsByTokens[channelToken]) {
                clientIdsByTokens[channelToken] = [];
            }
            clientIdsByTokens[channelToken].push(socketClientId);
            if (!sockets[channelToken]){
                sockets[channelToken] = new Socket(channelToken,
                    getBaseURL(url), channel);
            }
        }

        if (autoConnect) {
            (DQ.global()?.faces ?? DQ.global()?.jsf).push.open(socketClientId);
        }
    }

    export function open(socketClientId: string) {
        getSocket(components[socketClientId]?.channelToken).open();
    }

    export function close(socketClientId: string) {
        getSocket(components[socketClientId].channelToken).close();
    }

    // Private helper classes
    // Private classes functions ----------------------------------------------------------------------------------
    /**
     * Creates a reconnecting web socket. When the web socket successfully connects on first attempt, then it will
     * automatically reconnect on timeout with cumulative intervals of 500ms with a maximum of 25 attempts (~3 minutes).
     * The <code>onclose</code> function will be called with the error code of the last attempt.
     * @constructor
     * @param {string} channelToken the channel token associated with this websocket connection
     * @param {string} url The URL of the web socket
     * @param {string} channel The name of the web socket channel.
     */

    class Socket {

        socket: WebSocket;
        reconnectAttempts = 0;

        constructor(private channelToken: string, private url: string, private channel: string) {
        }

        open() {
            if (this.socket && this.socket.readyState == 1) {
                return;
            }
            this.socket = new WebSocket(this.url);

            this.bindCallbacks();
        }

        // noinspection JSUnusedLocalSymbols
        onopen(event: any) {
            if (!this.reconnectAttempts) {
                let clientIds = clientIdsByTokens[this.channelToken];
                for (let i = clientIds.length - 1; i >= 0; i--) {
                    let socketClientId = clientIds[i];
                    components[socketClientId]?.['onopen']?.(this.channel);
                }
            }
            this.reconnectAttempts = 0;
        }

        onerror(event: any) {
            let message = JSON.parse(event.data === undefined ? null : event.data);
            //TODO replace this with a more readable Stream code
            for (let i = clientIdsByTokens[this.channelToken].length - 1; i >= 0; i--) {
                let socketClientId = clientIdsByTokens[this.channelToken][i];
                if (document.getElementById(socketClientId)) {
                    try {
                        components[socketClientId]?.['onerror']?.(message, this.channel, event);
                    } catch (e) {
                        //Ignore
                    }
                } else {
                    clientIdsByTokens[this.channelToken].splice(i, 1);
                }
            }
            if (clientIdsByTokens[this.channelToken].length == 0) {
                // tag disappeared
                this.close();
            }
        }

        onmmessage(event: any) {
            let message = JSON.parse(event.data);
            for (let i = clientIdsByTokens[this.channelToken].length - 1; i >= 0; i--) {
                let socketClientId = clientIdsByTokens[this.channelToken][i];
                if (document.getElementById(socketClientId)) {
                    try {
                        components[socketClientId]?.['onmessage']?.(message, this.channel, event);
                    } catch (e) {
                        //Ignore
                    }
                    let behaviors = components?.[socketClientId]?.['behaviors'];
                    let functions = behaviors?.[message];
                    if (functions && functions.length) {
                        for (let j = 0; j < functions.length; j++) {
                            try {
                                functions[j](null);
                            } catch (e) {
                                //Ignore
                            }
                        }
                    }
                } else {
                    clientIdsByTokens[this.channelToken].splice(i, 1);
                }
            }
            if (clientIdsByTokens[this.channelToken].length == 0) {
                // tag disappeared
                this.close();
            }
        }

        onclose(event: any) {
            if (!this.socket
                || (event.code == 1000 && event.reason == REASON_EXPIRED)
                || (event.code == 1008)
                || (!this.reconnectAttempts)
                || (this.reconnectAttempts >= MAX_RECONNECT_ATTEMPTS)) {
                let clientIds = clientIdsByTokens[this.channelToken];
                for (let i = clientIds.length - 1; i >= 0; i--) {
                    let socketClientId = clientIds[i];
                    components?.[socketClientId]?.['onclose']?.(event?.code, this?.channel, event);
                }
            } else {
                setTimeout(this.open, RECONNECT_INTERVAL * this.reconnectAttempts++);
            }
        };

        close() {
            if (this.socket) {
                let s = this.socket;
                this.socket = null;
                s.close();
            }
        }

        /**
         * bind the callbacks to the socket callbacks
         */
        private bindCallbacks() {
            this.socket.onopen = (event: Event) => this.onopen(event);
            this.socket.onmessage = (event: Event) => this.onmmessage(event);
            this.socket.onclose = (event: Event) => this.onclose(event);
            this.socket.onerror = (event: Event) => this.onerror(event);
        }
    }

    // Private static functions ---------------------------------------------------------------------------------------

    function getBaseURL(url: string) {
        if (url.indexOf("://") < 0) {
            let base = DQ.global().location.hostname + ":" + DQ.global().location.port;
            return URL_PROTOCOL + base + url;
        } else {
            return url;
        }
    }

    /**
     * Get socket associated with given channelToken.
     * @param channelToken The name of the web socket channelToken.
     * @return Socket associated with given channelToken.
     * @throws Error, when the channelToken is unknown, you may need to initialize
     *                 it first via <code>init()</code> function.
     */
    function getSocket(channelToken: string): Socket {
        let socket = sockets[channelToken];
        if (socket) {
            return socket;
        } else {
            throw new Error("Unknown channelToken: " + channelToken);
        }
    }

    function resolveFunction(fn: Function | string = () => {
    }): Function {
        return <Function>((typeof fn !== "function") && (fn = DQ.global()[fn]), fn);
    }

}