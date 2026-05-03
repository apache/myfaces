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
import {MAX_RECONNECT_ATTEMPTS, RECONNECT_INTERVAL} from "./core/Const";
import {DQ} from "mona-dish";

/**
 * Implementation class for the push functionality
 */
export namespace PushImpl {

    // @deprecated because we can assume at least for the newer versions
    // that the protocol is properly set!
    const URL_PROTOCOL = DQ.global().location.protocol.replace("http", "ws") + "//";


    // we expose the member variables for testing purposes
    // they are not directly touched outside of tests

    /* socket map by token */
    export type OpenCallback = (channel: string) => void;
    export type MessageCallback = (message: any, channel: string, event: MessageEvent) => void;
    export type ErrorCallback = (code: number, channel: string, event: CloseEvent) => void;
    export type CloseCallback = (code: number, channel: string, event?: CloseEvent) => void;

    type ComponentData = {
        channelToken: string;
        onopen: OpenCallback;
        onmessage: MessageCallback;
        onerror: ErrorCallback;
        onclose: CloseCallback;
        behaviors: any;
        autoconnect: boolean;
    };

    export let sockets: {[key: string]: Socket} = {};
    /* component attributes by clientId */
    export let components: {[key: string]: ComponentData} = {};
    /* client ids by token (share websocket connection) */
    export let clientIdsByTokens: {[key: string]: string[]} = {};

    // needed for testing
    export function reset() {
        Object.values(sockets).forEach(s => { try { s.close(); } catch(e) { /* ignore */ } });
        sockets = {};
        components = {};
        clientIdsByTokens = {};
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
                         onopen: OpenCallback | string,
                         onmessage: MessageCallback | string,
                         onerror: ErrorCallback | string,
                         onclose: CloseCallback | string,
                         behaviors: any,
                         autoConnect: boolean) {
        onclose = resolveFunction(onclose) as CloseCallback;

        if (!DQ.global().WebSocket) { // IE6-9.
            onclose(-1, channel);
            return;
        }

        let channelToken = url.substr(url.indexOf('?') + 1);

        if (!components[socketClientId]) {
            components[socketClientId] = {
                'channelToken': channelToken,
                'onopen': resolveFunction(onopen) as OpenCallback,
                'onmessage' : resolveFunction(onmessage) as MessageCallback,
                'onerror' : resolveFunction(onerror) as ErrorCallback,
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

        private socket: WebSocket | null = null;
        private reconnectAttempts = 0;
        private hasEverConnected = false;
        private hasNotifiedInitialOpenAttempt = false;

        constructor(private channelToken: string, private url: string, private channel: string) {
        }

        open() {
            if (this.socket && this.socket.readyState === 1) {
                return;
            }
            this.socket = new WebSocket(this.url);
            this.bindCallbacks();
            this.notifyInitialOpenAttempt();
        }

        // noinspection JSUnusedLocalSymbols
        onopen(event: any) {
            this.hasEverConnected = true;
            this.reconnectAttempts = 0;
        }

        onerror(event: any) {
            // Native WebSocket error events do not expose the close reason code.
            // Faces onerror is fired from onclose only when a reconnect is attempted.
        }

        onmessage(event: any) {
            let message = JSON.parse(event.data);
            let clientIds = clientIdsByTokens[this.channelToken];
            if (!clientIds) return; // socket was torn down (reset()) while message was pending
            for (let i = clientIds.length - 1; i >= 0; i--) {
                let socketClientId = clientIds[i];
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
                    clientIds.splice(i, 1);
                }
            }
            if (clientIds.length === 0) {
                // tag disappeared
                this.close();
            }
        }

        onclose(event: any) {
            if (this.isTerminalClose(event)) {
                this.notifyClose(event);
                this.resetConnectionState();
                return;
            }

            if (!this.notifyErrorAndPruneMissingComponents(event)) return;
            if (this.closeIfChannelHasNoComponents()) return;

            this.scheduleReconnect();
        };

        close() {
            if (this.socket) {
                let s = this.socket;
                this.socket = null;
                s.close();
            }
        }

        private notifyInitialOpenAttempt() {
            if (this.reconnectAttempts || this.hasNotifiedInitialOpenAttempt) return;

            this.hasNotifiedInitialOpenAttempt = true;
            let clientIds = clientIdsByTokens[this.channelToken];
            if (!clientIds) return; // socket was torn down (reset()) while timer was pending
            for (let i = clientIds.length - 1; i >= 0; i--) {
                let socketClientId = clientIds[i];
                components[socketClientId]?.['onopen']?.(this.channel);
            }
        }

        private isTerminalClose(event: any): boolean {
            return !this.socket
                // Spec: no reconnect when the very first connection attempt fails.
                // onerror must also not be invoked in this case, only onclose.
                || !this.hasEverConnected
                // Spec: code 1000 (normal closure) is always terminal, regardless of reason.
                || event.code === 1000
                // 1008 = Policy Violation. Reconnecting would hit the same rejection again.
                || event.code === 1008
                || this.reconnectAttempts >= MAX_RECONNECT_ATTEMPTS;
        }

        private notifyClose(event: any) {
            let clientIds = clientIdsByTokens[this.channelToken];
            if (!clientIds) return; // already torn down (reset() called while socket was open)
            for (let i = clientIds.length - 1; i >= 0; i--) {
                let socketClientId = clientIds[i];
                components?.[socketClientId]?.['onclose']?.(event?.code, this?.channel, event);
            }
        }

        private resetConnectionState() {
            this.reconnectAttempts = 0;
            this.hasEverConnected = false;
            this.hasNotifiedInitialOpenAttempt = false;
        }

        private notifyErrorAndPruneMissingComponents(event: any): boolean {
            let clientIds = clientIdsByTokens[this.channelToken];
            if (!clientIds) return false; // already torn down (reset() called while socket was open)
            for (let i = clientIds.length - 1; i >= 0; i--) {
                let socketClientId = clientIds[i];
                if (document.getElementById(socketClientId)) {
                    try {
                        components?.[socketClientId]?.['onerror']?.(event?.code, this?.channel, event);
                    } catch (e) {
                        //Ignore
                    }
                } else {
                    clientIds.splice(i, 1);
                }
            }
            return true;
        }

        private closeIfChannelHasNoComponents(): boolean {
            if (clientIdsByTokens[this.channelToken]?.length !== 0) return false;

            // tag disappeared
            this.close();
            return true;
        }

        private scheduleReconnect() {
            const reconnectAttempt = ++this.reconnectAttempts;
            this.socket = null;
            setTimeout(() => this.open(), RECONNECT_INTERVAL * reconnectAttempt);
        }

        /**
         * bind the callbacks to the socket callbacks
         */
        private bindCallbacks() {
            this.socket!.onopen = (event: Event) => this.onopen(event);
            this.socket!.onmessage = (event: Event) => this.onmessage(event);
            this.socket!.onclose = (event: Event) => this.onclose(event);
            this.socket!.onerror = (event: Event) => this.onerror(event);
        }
    }

    // Private static functions ---------------------------------------------------------------------------------------
    // @deprecated because we can assume at least for the newer versions
    // that the protocol is properly set!
    // https://issues.apache.org/jira/browse/MYFACES-4718
    // This needs further investigation
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

    function resolveFunction<T extends Function>(fn?: T | string | null): T {
        if (typeof fn === "function") return fn as T;
        if (typeof fn === "string" && typeof DQ.global()[fn] === "function") return DQ.global()[fn] as T;
        return (() => {}) as unknown as T;
    }

}
