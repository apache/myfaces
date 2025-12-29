/*!
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * a standardized message to be sent over the message bus
 */
import {Observable, Subject} from "rxjs";
import {Stream} from "./Stream";
import {_global$} from "./Global";


/**
 * generic crypto interface
 * to encrypt messages before they are sent
 * to the message bus oder the underlying bus system
 *
 * The idea is to make it as easy as possible, you can use for instance crypto js to
 * handle everything
 */
export interface Crypto {
    /**
     * note anything can be passed
     *
     * @param data the data to be encrypted
     * @returns the encrypted data in any format, important is decode must be able to handle it
     */
    encode(data: any): any;

    /**
     * @param data the encrypted data in the format you expect it to be
     * @returns the unencrypted data
     */
    decode(data: any): any;
}


/**
 * generic hash interface which provides
 * exactly one method a hash encode which returns a string hash value of encoded data
 */
export interface Hash {
    encode(encodedData: string): string;
}

/**
 * Default implementation = no encryption
 */
export class NoCrypto implements Crypto {
    decode(data: any): string {
        return data;
    }

    encode(data: any): any {
        return data;
    }
}

//TODO dynamic encryptor which flushes the messages before changing the keys
//that way we can rotate and change internal crypto keys on the fly

let noEncryption = new NoCrypto();

export class Message {

    creationDate?: number;
    identifier?: string;
    targetOrigin?: string;
    encoded: boolean = false;

    constructor(public message: any = {}, targetOrigin = "*") {
        this.targetOrigin = targetOrigin;
        this.creationDate = new Date().getMilliseconds();
        this.identifier = new Date().getMilliseconds() + "_" + Math.random() + "_" + Math.random();
    }
}

/**
 * custom dom event wrapping our messages
 */
class MessageWrapper implements CustomEventInit<Message> {

    detail?: Message;
    bubbles?: boolean;
    cancelable?: boolean;
    composed?: boolean;
    channel: string;

    constructor(channel: string, message: Message) {
        this.detail = message;
        this.bubbles = true;
        this.cancelable = true;
        this.composed = true;
        this.channel = channel;
    }
}

/**
 * abstract broker class
 * (The broker is the central distribution unit of messages)
 */
abstract class BaseBroker {

    static readonly EVENT_TYPE = "brokerEvent";
    /**
     * we can split the listeners with the system
     * namespace... and type (aka identifier criteria)
     */
    protected messageListeners: any = {};
    protected subjects: any = {};
    protected processedMessages: any = {};

    protected cleanupCnt = 0;
    protected rootElem;
    protected msgHandler;

    protected readonly TIMEOUT_IN_MS = 1000;
    protected readonly MSG_EVENT = "message";

    //must be public because we also must have the option
    //to set it outside of the constructor
    crypto = noEncryption;


    abstract register(scopeElement?: any): BaseBroker;

    abstract unregister(): BaseBroker;

    abstract broadcast(channel: string, message: Message | string): BaseBroker;


    /**
     * registers a listener on a channel
     * @param channel the channel to register the listeners for
     * @param listener the listener to register
     */
    registerListener(channel: string, listener: (msg: Message) => void): BaseBroker {
        this.reserveListenerNS(channel);

        //we skip the processed messages, because they originated here
        //and already are processed
        this.messageListeners[channel].push((msg: Message) => {
            if (msg.identifier in this.processedMessages) {
                return;
            }
            if (msg?.encoded || msg?.["detail"]?.encoded) {
                if (msg?.["detail"]) {
                    msg["detail"].message = this.crypto.decode(msg["detail"].message);
                    msg["detail"].encoded = false;
                } else {
                    msg.message = this.crypto.decode(msg.message);
                    msg.encoded = false;
                }

            }
            listener(msg);
        });
        return this;
    }

    /**
     * binding into rxjs
     * produces a subject which can be used via next calls to send messages
     * on the other hand we
     * @param channel
     */
    asSubject(channel: string): Subject<Message> {
        this.reserveSubjectNS(channel);
        let subject = this.subjects[channel];
        let oldNext = subject.next;

        subject.next = (msg: Message | MessageWrapper) => {
            //We use a recursive call to let the broadcaster handle
            //The wrapper conversion and then again call us here
            //that way both directions are handled.. next calls the broker
            //and a broadcast calls next
            if ((<MessageWrapper>msg)?.detail) {
                oldNext.call(subject, (<MessageWrapper>msg)?.detail);
            } else {
                this.broadcast(channel, <Message>msg);
            }
        }
        return subject;
    }

    // noinspection JSUnusedGlobalSymbols
    /**
     * returns an observable on the baseBroker
     * @param channel
     */
    asObservable(channel: string): Observable<Message> {
        return this.asSubject(channel).asObservable();
    }

    /**
     * reserves the listener namespace and wildcard namespace for the given identifier
     * @param identifier
     * @private
     */
    private reserveListenerNS(identifier: string) {
        if (!this.messageListeners[identifier]) {
            this.messageListeners[identifier] = [];
        }
        if (!this.messageListeners["*"]) {
            this.messageListeners["*"] = [];
        }
    }

    private reserveSubjectNS(identifier: string) {
        if (!this.subjects[identifier]) {
            this.subjects[identifier] = new Subject();
        }
        if (!this.subjects["*"]) {
            this.subjects["*"] = new Subject();
        }
    }

    /**
     * unregisters a listener from this channel
     *
     * @param channel the channel to unregister from
     * @param listener the listener to unregister the channel from
     */
    unregisterListener(channel: string, listener: (msg: Message) => void): BaseBroker {
        this.messageListeners[channel] = (this.messageListeners[channel] || []).filter((item: any) => item !== listener);
        return this;
    }

    /**
     * answers a bidirectional message received
     * usage, the client can use this method, to answer an incoming message in a precise manner
     * so that the caller sending the bidirectional message knows how to deal with it
     * this mechanism can be used for global storages where we have one answering entity per channel delivering the
     * requested data, the request can be done asynchronously via promises waiting for answers
     *
     * @param channel the channel the originating message
     * @param request the requesting message
     * @param answer the answer to the request
     */
    answer(channel: string, request: Message | string, answer: Message): BaseBroker {
        if ('string' == typeof request) {
            request = new Message(request);
        }

        if (BaseBroker.isAnswer(request)) {
            return;
        }
        answer.identifier = BaseBroker.getAnswerId(request);
        this.broadcast(channel, answer);
        return this;
    }

    private static getAnswerId(request: Message) {
        return "_r_" + request.identifier;
    }

    private static isAnswer(request: Message) {
        return request.identifier.indexOf("_r_") == 0;
    }

    /**
     * idea... a bidirectional broadcast
     * sends a message and waits for the first answer coming in from one of the receivers
     * sending the message back with a messageIdentifier_broadCastId answer
     *
     * @param channel
     * @param message
     */
    request(channel: string, message: Message | string): Promise<Message> {
        if ('string' == typeof message) {
            message = new Message(message);
        }
        let messageId = message.identifier;

        let ret = new Promise<Message>((resolve, reject) => {
            let timeout = null;
            let listener = (message2: Message) => {
                if (message2.identifier == messageId) {
                    //broadcast from same source, we do not want
                    //to deal with it now
                    return;
                }

                if (message2.identifier == "_r_" + messageId) {
                    clearTimeout(timeout);
                    this.unregisterListener(channel, listener);
                    resolve(message2);
                }
            }
            timeout = setTimeout(() => {
                this.unregisterListener(channel, listener);
                reject("request message performed, timeout, no return value");
            }, 3000);
            this.registerListener(channel, listener);

        });
        this.broadcast(channel, message);
        return ret;
    }


    /**
     * garbage collects the processed messages queue
     * usually after one second
     */
    protected gcProcessedMessages() {
        if ((++this.cleanupCnt) % 10 != 0) {
            return;
        }
        let newProcessedMessages: any = {};
        Object.keys(this.processedMessages).forEach(key => {
            if (this.messageStillActive(key)) return;
            newProcessedMessages[key] = this.processedMessages[key];
        });
        this.processedMessages = newProcessedMessages;
    }

    private messageStillActive(key: string): boolean {
        return this.processedMessages[key] > ((new Date()).getMilliseconds() - this.TIMEOUT_IN_MS);
    }

    protected markMessageAsProcessed(message: Message) {
        this.processedMessages[message.identifier] = message.creationDate;
    }
}

let broadCastChannelBrokerGenerator = (name) => {
    if (_global$()?.BroadcastChannel) {
        return new (_global$()).BroadcastChannel(name);
    }
    throw Error("No Broadcast channel in the system, use a shim or provide a factory function" +
        "in the constructor");
};

const DEFAULT_CHANNEL_GROUP = "brokr";

/**
 * a broker which hooks into the Broadcast Channel
 * either via shim or substitute lib
 *
 * The broadcast channels are a standardized messaging library
 * The broker mechanism sets a layer on top to make it more manageable!
 *
 */
export class BroadcastChannelBroker extends BaseBroker {
    private openChannels: [{ key: string }, BroadcastChannel] = <any>{};
    private readonly msgListener: Function;


    /**
     * @param brokerFactory a factory generating a broker
     * @param channelGroup a group to combine a set of channels
     * @param crypto a crypto class
     */
    constructor(private brokerFactory: Function = broadCastChannelBrokerGenerator, private channelGroup = DEFAULT_CHANNEL_GROUP, public crypto: Crypto = noEncryption) {
        super();
        this.msgListener = (messageData: MessageWrapper) => {
            if (messageData.detail.encoded) {
                messageData.detail.message = <any>this.crypto.decode(messageData.detail.message);
                messageData.detail.encoded = false;
            }
            let coreMessage = messageData.detail;
            let channel: string = messageData.channel;

            if (this.messageListeners?.[channel]) {
                this.messageListeners?.[channel].forEach(listener => {
                    listener(coreMessage);
                })
            }
            this.markMessageAsProcessed(coreMessage);
            return true;
        }
        this.crypto = crypto;
        this.register();
    }

    broadcast(channel: string, message: Message | string, includeOrigin = true): BaseBroker {
        try {
            if ('string' == typeof message) {
                message = new Message(message);
            }
            //we now run a quick remapping to avoid
            //serialisation errors
            let msgString = JSON.stringify(<Message>message);
            message = <Message>JSON.parse(msgString);

            let messageWrapper = new MessageWrapper(channel, message);
            messageWrapper.detail.message = this.crypto.encode(messageWrapper.detail.message);
            messageWrapper.detail.encoded = true;

            if (this?.subjects[channel]) {
                this.subjects[channel].next(messageWrapper);
            }

            this.openChannels[this.channelGroup].postMessage(messageWrapper);
            if (includeOrigin) {
                this.msgListener(messageWrapper);
            }
        } finally {
            this.gcProcessedMessages();
        }
        return this;
    }

    registerListener(channel: string, listener: (msg: Message) => void): BaseBroker {
        super.registerListener(channel, listener);
        return <BaseBroker>this;
    }

    register(): BaseBroker {
        if (!this.openChannels[this.channelGroup]) {
            this.openChannels[this.channelGroup] = this.brokerFactory(this.channelGroup);
        }
        this.openChannels[this.channelGroup].addEventListener("message", this.msgListener);
        return <BaseBroker>this;
    }

    unregister(): BaseBroker {
        this.openChannels[this.channelGroup].close();
        return <BaseBroker>this;
    }
}

// noinspection JSUnusedGlobalSymbols
/**
 * Helper factory to create a broadcast channel broker
 */
export class BroadcastChannelBrokerBuilder {
    private broadCastChannelGenerator: Function = broadCastChannelBrokerGenerator;
    private channelGroup = DEFAULT_CHANNEL_GROUP;
    private crypto = noEncryption;
    private listeners: Array<any> = [];

    withGeneratorFunc(generatorFunc: Function): BroadcastChannelBrokerBuilder {
        this.broadCastChannelGenerator = generatorFunc;
        return this;
    }

    withListener(channel: string, ...listeners: Function[]): BroadcastChannelBrokerBuilder {
        Stream.of(...listeners).each(listener => {
            this.listeners.push({
                channel: channel,
                listener: listener
            })
        });
        return this;
    }

    withChannelGroup(channelGroup: string): BroadcastChannelBrokerBuilder {
        this.channelGroup = channelGroup;
        return this;
    }

    withCrypto(crypto: Crypto): BroadcastChannelBrokerBuilder {
        this.crypto = crypto;
        return this;
    }

    build(): BroadcastChannelBroker {
        let broker = new BroadcastChannelBroker(this.broadCastChannelGenerator, this.channelGroup, this.crypto);
        Stream.of(...this.listeners).each(listenerItem => {
            broker.registerListener(listenerItem.channel, listenerItem.listener);
        });
        return broker;
    }
}


/**
 * implementation of a messaging based transport
 */

/**
 * central message broker which uses various dom constructs
 * to broadcast messages into subelements
 *
 * we use the dom event system as transport and iframe and shadow dom mechanisms in a transparent way to
 * pull this off
 *
 * usage
 *
 * broker = new Broker(optional rootElement)
 *
 * defines a message broker within a scope of rootElement (without it is window aka the current isolation level)
 *
 * broker.registerListener(channel, listener) registers a new listener to the current broker and channel
 * broker.unregisterListener(channel, listener) unregisters the given listener
 *
 * broker.broadcast(message, optional direction, optional callBrokerListeners)
 * sends a message (channel included in the message object) in a direction (up, down, both)
 * and also optionally calls the listeners on the same broker (default off)
 *
 * the flow is like
 * up messages are propagated upwards only until it reaches the outer top of the dom
 * downwards, the messages are propagated downwards only
 * both the message is propagated into both directions
 *
 * Usually messages sent from the same broker are not processed within... however by setting
 * callBrokerListeners to true the listeners on the same broker also are called
 * brokers on the same level will get the message and process it automatically no matter what.
 * That way you can exclude the source from message processing (and it is done that way automatically)
 *
 * Isolation levels. Usually every isolation level needs its own broker object registering
 * on the outer bounds
 *
 * aka documents will register on window
 * iframes on the iframe windowObject
 * isolated shadow doms... document
 *
 *
 *
 */
export class Broker extends BaseBroker {

    /**
     * constructor has an optional root element
     * and an internal name
     *
     * @param scopeElement
     * @param brokerGroup
     * @param crypto
     */
    constructor(scopeElement: HTMLElement | Window | ShadowRoot = window, public brokerGroup = "brokr", crypto: Crypto = noEncryption) {

        super();

        /**
         * message relay.. identifies message events and relays them to the listeners
         * @param event
         */
        let evtHandler = (event: MessageEvent | CustomEvent<Message>) => {
            let details = (event as any)?.detail ?? (<MessageEvent>event)?.data?.detail;
            //TODO possible crypto hook, needs unit testing
            let channel = ((event as any)?.data?.channel) ?? ((event as any)?.channel);

            //javascript loses the type info in certain module types
            if (details?.identifier && details?.message) {
                let msg: Message = details;
                if (msg.identifier in this.processedMessages) {
                    return;
                }
                //coming in from up... we need to send it down
                //a relayed message always has to trigger the listeners as well
                if ((event as any)?.detail) {
                    this.broadcast(channel, msg);
                } else {
                    this.broadcast(channel, msg);
                }

            }
        };
        this.msgHandler = (evt: MessageEvent) => evtHandler(evt);
        this.crypto = crypto;
        this.register(scopeElement);
    }

    /**
     * register the current broker into a scope defined by wnd
     * @param scopeElement
     */
    register(scopeElement: HTMLElement | Window | ShadowRoot): BaseBroker {
        this.rootElem = (scopeElement as any).host ? (scopeElement as any).host : scopeElement;
        if ((scopeElement as any).host) {
            let host = (<ShadowRoot>scopeElement).host;
            host.setAttribute("data-broker", "1");
        } else {
            if (scopeElement?.["setAttribute"])
                (scopeElement as any).setAttribute("data-broker", "1");
        }

        this.rootElem.addEventListener(this.brokerGroup + "__||__" + Broker.EVENT_TYPE, this.msgHandler, {capture: true});
        /*dom message usable by iframes*/
        this.rootElem.addEventListener(this.brokerGroup + "__||__" + Broker.EVENT_TYPE + this.MSG_EVENT, this.msgHandler, {capture: true});
        return <any>this;
    }

    /**
     * manual unregister function, to unregister as broker from the current
     * scope
     */
    unregister(): BaseBroker {
        this.rootElem.removeEventListener(this.brokerGroup + "__||__" + Broker.EVENT_TYPE, this.msgHandler)
        this.rootElem.removeEventListener(this.brokerGroup + "__||__" + this.MSG_EVENT, this.msgHandler)
        return <any>this;
    }


    /**
     * broadcast a message
     * the message contains the channel and the data and some internal bookkeeping data
     *
     * @param channel the channel to broadcast to
     * @param message the message dot send
     * (for instance 2 iframes within the same parent broker)
     */
    broadcast(channel: string, message: Message | string): BaseBroker {
        if ('string' == typeof message) {
            message = new Message(message);
        }
        //message.message = this.crypto.encode(message);
        //message.encoded = true;

        if (this?.subjects[channel]) {
            let messageWrapper = new MessageWrapper(channel, message);
            if (!messageWrapper.detail.encoded) {
                messageWrapper.detail.message = this.crypto.encode(messageWrapper.detail.message);
                messageWrapper.detail.encoded = true;
            }
            this.subjects[channel].next(messageWrapper);
        }

        try {
            this.dispatchUp(channel, message, false, true);
            //listeners already called
            this.dispatchDown(channel, message, true, false)
        } finally {
            this.gcProcessedMessages();
        }
        return this;
    }


    private dispatchUp(channel: string, message: Message, ignoreListeners = true, callBrokerListeners = true) {
        if (!ignoreListeners) {
            this.msgCallListeners(channel, message);
        }
        this.markMessageAsProcessed(message);
        if (_global$().parent != null) {

            let messageWrapper = new MessageWrapper(channel, message);
            _global$().parent.postMessage(JSON.parse(JSON.stringify(messageWrapper)), message.targetOrigin);
        }
        if (callBrokerListeners) {
            this.dispatchSameLevel(channel, message);
        }
    }

    private dispatchSameLevel(channel: string, message: Message) {
        let event = this.transformToEvent(channel, message, true);
        //we also dispatch sideways
        _global$().dispatchEvent(event);
    }

    //a dispatch of our own should never trigger the listeners hence the default true
    private dispatchDown(channel: string, message: Message, ignoreListeners = true, callBrokerListeners = true) {
        if (!ignoreListeners) {
            this.msgCallListeners(channel, message);
        }
        this.processedMessages[message.identifier] = message.creationDate;
        let evt = this.transformToEvent(channel, message);

        /*we now notify all iframes lying underneath */
        Array.prototype.slice.call(document.querySelectorAll("iframe")).forEach((element: HTMLIFrameElement) => {
            let messageWrapper = new MessageWrapper(channel, message);
            element.contentWindow.postMessage(JSON.parse(JSON.stringify(messageWrapper)), message.targetOrigin);
        });

        Array.prototype.slice.call(document.querySelectorAll("[data-broker='1']")).forEach((element: HTMLElement) => element.dispatchEvent(evt))

        if (callBrokerListeners) {
            this.dispatchSameLevel(channel, message);
        }
    }


    private msgCallListeners(channel: string, message: Message) {
        let listeners = this.messageListeners[channel];
        if (listeners?.length) {
            let callElement = (element: (msg: Message) => void) => {
                element(message);
            }

            listeners.forEach(callElement);
        }
    }

    private transformToEvent(channel: string, message: Message, bubbles = false): CustomEvent {
        let messageWrapper = new MessageWrapper(channel, message);
        messageWrapper.bubbles = bubbles;
        return Broker.createCustomEvent(this.brokerGroup + "__||__" + Broker.EVENT_TYPE, messageWrapper);
    }

    private static createCustomEvent(name: string, wrapper: MessageWrapper): any {
        if ('function' != typeof _global$().CustomEvent) {
            let e: any = document.createEvent('HTMLEvents');
            e.detail = wrapper.detail;
            e.channel = wrapper.channel;
            e.initEvent(name, wrapper.bubbles, wrapper.cancelable);
            return e;

        } else {
            let customEvent = new (_global$()).CustomEvent(name, wrapper);
            (customEvent as any).channel = wrapper.channel;
            return customEvent;
        }

    }
}

// noinspection JSUnusedGlobalSymbols
/**
 * Helper factory to create a dom broker
 */
export class BrokerBuilder {
    private scopeElement: HTMLElement | Window | ShadowRoot = window;
    private channelGroup = DEFAULT_CHANNEL_GROUP;
    private crypto = noEncryption;
    private listeners: Array<any> = [];

    withScopeElement(scopeElement: HTMLElement | Window | ShadowRoot): BrokerBuilder {
        this.scopeElement = scopeElement;
        return this;
    }

    withListener(channel: string, ...listeners: Function[]): BrokerBuilder {
        Stream.of(...listeners).each(listener => {
            this.listeners.push({
                channel: channel,
                listener: listener
            })
        });
        return this;
    }


    withChannelGroup(channelGroup: string): BrokerBuilder {
        this.channelGroup = channelGroup;
        return this;
    }

    withCrypto(crypto: Crypto): BrokerBuilder {
        this.crypto = crypto;
        return this;
    }

    build(): Broker {
        let broker = new Broker(this.scopeElement, this.channelGroup, this.crypto);
        Stream.of(...this.listeners).each(listenerItem => {
            broker.registerListener(listenerItem.channel, listenerItem.listener);
        });
        return broker;
    }
}