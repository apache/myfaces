"use strict";
var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (Object.prototype.hasOwnProperty.call(b, p)) d[p] = b[p]; };
        return extendStatics(d, b);
    };
    return function (d, b) {
        if (typeof b !== "function" && b !== null)
            throw new TypeError("Class extends value " + String(b) + " is not a constructor or null");
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
exports.__esModule = true;
exports.BrokerBuilder = exports.Broker = exports.BroadcastChannelBrokerBuilder = exports.BroadcastChannelBroker = exports.Message = exports.NoCrypto = void 0;
/**
 * a standardized message to be sent over the message bus
 */
var rxjs_1 = require("rxjs");
var Stream_1 = require("./Stream");
/**
 * Default implementation = no encryption
 */
var NoCrypto = /** @class */ (function () {
    function NoCrypto() {
    }
    NoCrypto.prototype.decode = function (data) {
        return data;
    };
    NoCrypto.prototype.encode = function (data) {
        return data;
    };
    return NoCrypto;
}());
exports.NoCrypto = NoCrypto;
//TODO dynamic encryptor which flushes the messages before changing the keys
//that way we can rotate and change internal crypto keys on the fly
var noEncryption = new NoCrypto();
var Message = /** @class */ (function () {
    function Message(message, targetOrigin) {
        if (message === void 0) { message = {}; }
        if (targetOrigin === void 0) { targetOrigin = "*"; }
        this.message = message;
        this.encoded = false;
        this.targetOrigin = targetOrigin;
        this.creationDate = new Date().getMilliseconds();
        this.identifier = new Date().getMilliseconds() + "_" + Math.random() + "_" + Math.random();
    }
    return Message;
}());
exports.Message = Message;
/**
 * custom dom event wrapping our messages
 */
var MessageWrapper = /** @class */ (function () {
    function MessageWrapper(channel, message) {
        this.detail = message;
        this.bubbles = true;
        this.cancelable = true;
        this.composed = true;
        this.channel = channel;
    }
    return MessageWrapper;
}());
var BaseBroker = /** @class */ (function () {
    function BaseBroker() {
        /**
         * we can split the listeners with the system
         * namespace... and type (aka identifier criteria)
         */
        this.messageListeners = {};
        this.subjects = {};
        this.processedMessages = {};
        this.cleanupCnt = 0;
        this.TIMEOUT_IN_MS = 1000;
        this.MSG_EVENT = "message";
        //must be public because we also must have the option
        //to set it outside of the constructor
        this.crypto = noEncryption;
    }
    /**
     * registers a listener on a channel
     * @param channel the channel to register the listeners for
     * @param listener the listener to register
     */
    BaseBroker.prototype.registerListener = function (channel, listener) {
        var _this = this;
        this.reserveListenerNS(channel);
        //we skip the processed messages, because they originated here
        //and already are processed
        this.messageListeners[channel].push(function (msg) {
            var _a;
            if (msg.identifier in _this.processedMessages) {
                return;
            }
            if ((msg === null || msg === void 0 ? void 0 : msg.encoded) || ((_a = msg === null || msg === void 0 ? void 0 : msg["detail"]) === null || _a === void 0 ? void 0 : _a.encoded)) {
                if (msg === null || msg === void 0 ? void 0 : msg["detail"]) {
                    msg["detail"].message = _this.crypto.decode(msg["detail"].message);
                    msg["detail"].encoded = false;
                }
                else {
                    msg.message = _this.crypto.decode(msg.message);
                    msg.encoded = false;
                }
            }
            listener(msg);
        });
        return this;
    };
    /**
     * binding into rxjs
     * produces a subject which can be used via next calls to send messages
     * on the other hand we
     * @param channel
     */
    BaseBroker.prototype.asSubject = function (channel) {
        var _this = this;
        this.reserveSubjectNS(channel);
        var subject = this.subjects[channel];
        var oldNext = subject.next;
        subject.next = function (msg) {
            //We use a recursive call to let the broadcaster handle
            //The wrapper conversion and then again call us here
            //that way both directions are handled.. next calls the broker
            //and a broadcast calls next
            if (msg === null || msg === void 0 ? void 0 : msg.detail) {
                oldNext.call(subject, msg === null || msg === void 0 ? void 0 : msg.detail);
            }
            else {
                _this.broadcast(channel, msg);
            }
        };
        return subject;
    };
    // noinspection JSUnusedGlobalSymbols
    /**
     * returns an observable on the baseBroker
     * @param channel
     */
    BaseBroker.prototype.asObservable = function (channel) {
        return this.asSubject(channel).asObservable();
    };
    /**
     * reserves the listener namespace and wildcard namespace for the given identifier
     * @param identifier
     * @private
     */
    BaseBroker.prototype.reserveListenerNS = function (identifier) {
        if (!this.messageListeners[identifier]) {
            this.messageListeners[identifier] = [];
        }
        if (!this.messageListeners["*"]) {
            this.messageListeners["*"] = [];
        }
    };
    BaseBroker.prototype.reserveSubjectNS = function (identifier) {
        if (!this.subjects[identifier]) {
            this.subjects[identifier] = new rxjs_1.Subject();
        }
        if (!this.subjects["*"]) {
            this.subjects["*"] = new rxjs_1.Subject();
        }
    };
    /**
     * unregisters a listener from this channel
     *
     * @param channel the channel to unregister from
     * @param listener the listener to unregister the channel from
     */
    BaseBroker.prototype.unregisterListener = function (channel, listener) {
        this.messageListeners[channel] = (this.messageListeners[channel] || []).filter(function (item) { return item !== listener; });
        return this;
    };
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
    BaseBroker.prototype.answer = function (channel, request, answer) {
        if ('string' == typeof request) {
            request = new Message(request);
        }
        if (BaseBroker.isAnswer(request)) {
            return;
        }
        answer.identifier = BaseBroker.getAnswerId(request);
        this.broadcast(channel, answer);
        return this;
    };
    BaseBroker.getAnswerId = function (request) {
        return "_r_" + request.identifier;
    };
    BaseBroker.isAnswer = function (request) {
        return request.identifier.indexOf("_r_") == 0;
    };
    /**
     * idea... a bidirectional broadcast
     * sends a message and waits for the first answer coming in from one of the receivers
     * sending the message back with a messageIdentifier_broadCastId answer
     *
     * @param channel
     * @param message
     */
    BaseBroker.prototype.request = function (channel, message) {
        var _this = this;
        if ('string' == typeof message) {
            message = new Message(message);
        }
        var messageId = message.identifier;
        var ret = new Promise(function (resolve, reject) {
            var timeout = null;
            var listener = function (message2) {
                if (message2.identifier == messageId) {
                    //broadcast from same source, we do not want
                    //to deal with it now
                    return;
                }
                if (message2.identifier == "_r_" + messageId) {
                    clearTimeout(timeout);
                    _this.unregisterListener(channel, listener);
                    resolve(message2);
                }
            };
            timeout = setTimeout(function () {
                _this.unregisterListener(channel, listener);
                reject("request message performed, timeout, no return value");
            }, 3000);
            _this.registerListener(channel, listener);
        });
        this.broadcast(channel, message);
        return ret;
    };
    /**
     * garbage collects the processed messages queue
     * usually after one second
     */
    BaseBroker.prototype.gcProcessedMessages = function () {
        var _this = this;
        if ((++this.cleanupCnt) % 10 != 0) {
            return;
        }
        var newProcessedMessages = {};
        Object.keys(this.processedMessages).forEach(function (key) {
            if (_this.messageStillActive(key))
                return;
            newProcessedMessages[key] = _this.processedMessages[key];
        });
        this.processedMessages = newProcessedMessages;
    };
    BaseBroker.prototype.messageStillActive = function (key) {
        return this.processedMessages[key] > ((new Date()).getMilliseconds() - this.TIMEOUT_IN_MS);
    };
    BaseBroker.prototype.markMessageAsProcessed = function (message) {
        this.processedMessages[message.identifier] = message.creationDate;
    };
    BaseBroker.EVENT_TYPE = "brokerEvent";
    return BaseBroker;
}());
var broadCastChannelBrokerGenerator = function (name) {
    if (window === null || window === void 0 ? void 0 : window.BroadcastChannel) {
        return new window.BroadcastChannel(name);
    }
    throw Error("No Broadcast channel in the system, use a shim or provide a factory function" +
        "in the constructor");
};
var DEFAULT_CHANNEL_GROUP = "brokr";
/**
 * a broker which hooks into the Broadcast Channel broker
 * either via shim or substitute lib
 */
var BroadcastChannelBroker = /** @class */ (function (_super) {
    __extends(BroadcastChannelBroker, _super);
    /**
     * @param brokerFactory a factory generating a broker
     * @param channelGroup a group to combine a set of channels
     * @param crypto a crypto class
     */
    function BroadcastChannelBroker(brokerFactory, channelGroup, crypto) {
        if (brokerFactory === void 0) { brokerFactory = broadCastChannelBrokerGenerator; }
        if (channelGroup === void 0) { channelGroup = DEFAULT_CHANNEL_GROUP; }
        if (crypto === void 0) { crypto = noEncryption; }
        var _this = _super.call(this) || this;
        _this.brokerFactory = brokerFactory;
        _this.channelGroup = channelGroup;
        _this.crypto = crypto;
        _this.openChannels = {};
        _this.msgListener = function (messageData) {
            var _a, _b;
            if (messageData.detail.encoded) {
                messageData.detail.message = _this.crypto.decode(messageData.detail.message);
                messageData.detail.encoded = false;
            }
            var coreMessage = messageData.detail;
            var channel = messageData.channel;
            if ((_a = _this.messageListeners) === null || _a === void 0 ? void 0 : _a[channel]) {
                (_b = _this.messageListeners) === null || _b === void 0 ? void 0 : _b[channel].forEach(function (listener) {
                    listener(coreMessage);
                });
            }
            _this.markMessageAsProcessed(coreMessage);
            return true;
        };
        _this.crypto = crypto;
        _this.register();
        return _this;
    }
    BroadcastChannelBroker.prototype.broadcast = function (channel, message, includeOrigin) {
        if (includeOrigin === void 0) { includeOrigin = true; }
        try {
            if ('string' == typeof message) {
                message = new Message(message);
            }
            //we now run a quick remapping to avoid
            //serialisation errors
            var msgString = JSON.stringify(message);
            message = JSON.parse(msgString);
            var messageWrapper = new MessageWrapper(channel, message);
            messageWrapper.detail.message = this.crypto.encode(messageWrapper.detail.message);
            messageWrapper.detail.encoded = true;
            if (this === null || this === void 0 ? void 0 : this.subjects[channel]) {
                this.subjects[channel].next(messageWrapper);
            }
            this.openChannels[this.channelGroup].postMessage(messageWrapper);
            if (includeOrigin) {
                this.msgListener(messageWrapper);
            }
        }
        finally {
            this.gcProcessedMessages();
        }
        return this;
    };
    BroadcastChannelBroker.prototype.registerListener = function (channel, listener) {
        _super.prototype.registerListener.call(this, channel, listener);
        return this;
    };
    BroadcastChannelBroker.prototype.register = function () {
        if (!this.openChannels[this.channelGroup]) {
            this.openChannels[this.channelGroup] = this.brokerFactory(this.channelGroup);
        }
        this.openChannels[this.channelGroup].addEventListener("message", this.msgListener);
        return this;
    };
    BroadcastChannelBroker.prototype.unregister = function () {
        this.openChannels[this.channelGroup].close();
        return this;
    };
    return BroadcastChannelBroker;
}(BaseBroker));
exports.BroadcastChannelBroker = BroadcastChannelBroker;
// noinspection JSUnusedGlobalSymbols
/**
 * Helper factory to create a broadcast channel broker
 */
var BroadcastChannelBrokerBuilder = /** @class */ (function () {
    function BroadcastChannelBrokerBuilder() {
        this.broadCastChannelGenerator = broadCastChannelBrokerGenerator;
        this.channelGroup = DEFAULT_CHANNEL_GROUP;
        this.crypto = noEncryption;
        this.listeners = [];
    }
    BroadcastChannelBrokerBuilder.prototype.withGeneratorFunc = function (generatorFunc) {
        this.broadCastChannelGenerator = generatorFunc;
        return this;
    };
    BroadcastChannelBrokerBuilder.prototype.withListener = function (channel) {
        var _this = this;
        var listeners = [];
        for (var _i = 1; _i < arguments.length; _i++) {
            listeners[_i - 1] = arguments[_i];
        }
        Stream_1.Stream.of.apply(Stream_1.Stream, listeners).each(function (listener) {
            _this.listeners.push({
                channel: channel,
                listener: listener
            });
        });
        return this;
    };
    BroadcastChannelBrokerBuilder.prototype.withChannelGroup = function (channelGroup) {
        this.channelGroup = channelGroup;
        return this;
    };
    BroadcastChannelBrokerBuilder.prototype.withCrypto = function (crypto) {
        this.crypto = crypto;
        return this;
    };
    BroadcastChannelBrokerBuilder.prototype.build = function () {
        var broker = new BroadcastChannelBroker(this.broadCastChannelGenerator, this.channelGroup, this.crypto);
        Stream_1.Stream.of.apply(Stream_1.Stream, this.listeners).each(function (listenerItem) {
            broker.registerListener(listenerItem.channel, listenerItem.listener);
        });
        return broker;
    };
    return BroadcastChannelBrokerBuilder;
}());
exports.BroadcastChannelBrokerBuilder = BroadcastChannelBrokerBuilder;
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
var Broker = /** @class */ (function (_super) {
    __extends(Broker, _super);
    /**
     * constructor has an optional root element
     * and an internal name
     *
     * @param scopeElement
     * @param brokerGroup
     * @param crypto
     */
    function Broker(scopeElement, brokerGroup, crypto) {
        if (scopeElement === void 0) { scopeElement = window; }
        if (brokerGroup === void 0) { brokerGroup = "brokr"; }
        if (crypto === void 0) { crypto = noEncryption; }
        var _this = _super.call(this) || this;
        _this.brokerGroup = brokerGroup;
        /**
         * message relay.. identifies message events and relays them to the listeners
         * @param event
         */
        var evtHandler = function (event) {
            var _a, _b, _c, _d;
            var details = (_a = event === null || event === void 0 ? void 0 : event.detail) !== null && _a !== void 0 ? _a : (_b = event === null || event === void 0 ? void 0 : event.data) === null || _b === void 0 ? void 0 : _b.detail;
            //TODO possible crypto hook, needs unit testing
            var channel = (_d = ((_c = event === null || event === void 0 ? void 0 : event.data) === null || _c === void 0 ? void 0 : _c.channel)) !== null && _d !== void 0 ? _d : (event === null || event === void 0 ? void 0 : event.channel);
            //javascript loses the type info in certain module types
            if ((details === null || details === void 0 ? void 0 : details.identifier) && (details === null || details === void 0 ? void 0 : details.message)) {
                var msg = details;
                if (msg.identifier in _this.processedMessages) {
                    return;
                }
                //coming in from up... we need to send it down
                //a relayed message always has to trigger the listeners as well
                if (event === null || event === void 0 ? void 0 : event.detail) {
                    _this.broadcast(channel, msg);
                }
                else {
                    _this.broadcast(channel, msg);
                }
            }
        };
        _this.msgHandler = function (evt) { return evtHandler(evt); };
        _this.crypto = crypto;
        _this.register(scopeElement);
        return _this;
    }
    /**
     * register the current broker into a scope defined by wnd
     * @param scopeElement
     */
    Broker.prototype.register = function (scopeElement) {
        this.rootElem = scopeElement.host ? scopeElement.host : scopeElement;
        if (scopeElement.host) {
            var host = scopeElement.host;
            host.setAttribute("data-broker", "1");
        }
        else {
            if (scopeElement === null || scopeElement === void 0 ? void 0 : scopeElement["setAttribute"])
                scopeElement.setAttribute("data-broker", "1");
        }
        this.rootElem.addEventListener(this.brokerGroup + "__||__" + Broker.EVENT_TYPE, this.msgHandler, { capture: true });
        /*dom message usable by iframes*/
        this.rootElem.addEventListener(this.brokerGroup + "__||__" + Broker.EVENT_TYPE + this.MSG_EVENT, this.msgHandler, { capture: true });
        return this;
    };
    /**
     * manual unregister function, to unregister as broker from the current
     * scope
     */
    Broker.prototype.unregister = function () {
        this.rootElem.removeEventListener(this.brokerGroup + "__||__" + Broker.EVENT_TYPE, this.msgHandler);
        this.rootElem.removeEventListener(this.brokerGroup + "__||__" + this.MSG_EVENT, this.msgHandler);
        return this;
    };
    /**
     * broadcast a message
     * the message contains the channel and the data and some internal bookkeeping data
     *
     * @param channel the channel to broadcast to
     * @param message the message dot send
     * (for instance 2 iframes within the same parent broker)
     */
    Broker.prototype.broadcast = function (channel, message) {
        if ('string' == typeof message) {
            message = new Message(message);
        }
        //message.message = this.crypto.encode(message);
        //message.encoded = true;
        if (this === null || this === void 0 ? void 0 : this.subjects[channel]) {
            var messageWrapper = new MessageWrapper(channel, message);
            if (!messageWrapper.detail.encoded) {
                messageWrapper.detail.message = this.crypto.encode(messageWrapper.detail.message);
                messageWrapper.detail.encoded = true;
            }
            this.subjects[channel].next(messageWrapper);
        }
        try {
            this.dispatchUp(channel, message, false, true);
            //listeners already called
            this.dispatchDown(channel, message, true, false);
        }
        finally {
            this.gcProcessedMessages();
        }
        return this;
    };
    Broker.prototype.dispatchUp = function (channel, message, ignoreListeners, callBrokerListeners) {
        if (ignoreListeners === void 0) { ignoreListeners = true; }
        if (callBrokerListeners === void 0) { callBrokerListeners = true; }
        if (!ignoreListeners) {
            this.msgCallListeners(channel, message);
        }
        this.markMessageAsProcessed(message);
        if (window.parent != null) {
            var messageWrapper = new MessageWrapper(channel, message);
            window.parent.postMessage(JSON.parse(JSON.stringify(messageWrapper)), message.targetOrigin);
        }
        if (callBrokerListeners) {
            this.dispatchSameLevel(channel, message);
        }
    };
    Broker.prototype.dispatchSameLevel = function (channel, message) {
        var event = this.transformToEvent(channel, message, true);
        //we also dispatch sideways
        window.dispatchEvent(event);
    };
    //a dispatch of our own should never trigger the listeners hence the default true
    Broker.prototype.dispatchDown = function (channel, message, ignoreListeners, callBrokerListeners) {
        if (ignoreListeners === void 0) { ignoreListeners = true; }
        if (callBrokerListeners === void 0) { callBrokerListeners = true; }
        if (!ignoreListeners) {
            this.msgCallListeners(channel, message);
        }
        this.processedMessages[message.identifier] = message.creationDate;
        var evt = this.transformToEvent(channel, message);
        /*we now notify all iframes lying underneath */
        Array.prototype.slice.call(document.querySelectorAll("iframe")).forEach(function (element) {
            var messageWrapper = new MessageWrapper(channel, message);
            element.contentWindow.postMessage(JSON.parse(JSON.stringify(messageWrapper)), message.targetOrigin);
        });
        Array.prototype.slice.call(document.querySelectorAll("[data-broker='1']")).forEach(function (element) { return element.dispatchEvent(evt); });
        if (callBrokerListeners) {
            this.dispatchSameLevel(channel, message);
        }
    };
    Broker.prototype.msgCallListeners = function (channel, message) {
        var listeners = this.messageListeners[channel];
        if (listeners === null || listeners === void 0 ? void 0 : listeners.length) {
            var callElement = function (element) {
                element(message);
            };
            listeners.forEach(callElement);
        }
    };
    Broker.prototype.transformToEvent = function (channel, message, bubbles) {
        if (bubbles === void 0) { bubbles = false; }
        var messageWrapper = new MessageWrapper(channel, message);
        messageWrapper.bubbles = bubbles;
        return Broker.createCustomEvent(this.brokerGroup + "__||__" + Broker.EVENT_TYPE, messageWrapper);
    };
    Broker.createCustomEvent = function (name, wrapper) {
        if ('function' != typeof window.CustomEvent) {
            var e = document.createEvent('HTMLEvents');
            e.detail = wrapper.detail;
            e.channel = wrapper.channel;
            e.initEvent(name, wrapper.bubbles, wrapper.cancelable);
            return e;
        }
        else {
            var customEvent = new window.CustomEvent(name, wrapper);
            customEvent.channel = wrapper.channel;
            return customEvent;
        }
    };
    return Broker;
}(BaseBroker));
exports.Broker = Broker;
// noinspection JSUnusedGlobalSymbols
/**
 * Helper factory to create a dom broker
 */
var BrokerBuilder = /** @class */ (function () {
    function BrokerBuilder() {
        this.scopeElement = window;
        this.channelGroup = DEFAULT_CHANNEL_GROUP;
        this.crypto = noEncryption;
        this.listeners = [];
    }
    BrokerBuilder.prototype.withScopeElement = function (scopeElement) {
        this.scopeElement = scopeElement;
        return this;
    };
    BrokerBuilder.prototype.withListener = function (channel) {
        var _this = this;
        var listeners = [];
        for (var _i = 1; _i < arguments.length; _i++) {
            listeners[_i - 1] = arguments[_i];
        }
        Stream_1.Stream.of.apply(Stream_1.Stream, listeners).each(function (listener) {
            _this.listeners.push({
                channel: channel,
                listener: listener
            });
        });
        return this;
    };
    BrokerBuilder.prototype.withChannelGroup = function (channelGroup) {
        this.channelGroup = channelGroup;
        return this;
    };
    BrokerBuilder.prototype.withCrypto = function (crypto) {
        this.crypto = crypto;
        return this;
    };
    BrokerBuilder.prototype.build = function () {
        var broker = new Broker(this.scopeElement, this.channelGroup, this.crypto);
        Stream_1.Stream.of.apply(Stream_1.Stream, this.listeners).each(function (listenerItem) {
            broker.registerListener(listenerItem.channel, listenerItem.listener);
        });
        return broker;
    };
    return BrokerBuilder;
}());
exports.BrokerBuilder = BrokerBuilder;
//# sourceMappingURL=Messaging.js.map