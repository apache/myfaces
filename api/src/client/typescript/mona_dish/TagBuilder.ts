/*!
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

//poliyfill from @webcomponents/webcomponentsjs
import {DomQuery} from "./DomQuery";

if ("undefined" != typeof window) {
    (function () {
        if (void 0 === window.Reflect || void 0 === window.customElements || (<any>window.customElements).polyfillWrapFlushCallback) return;
        const a = HTMLElement;
        (<any>window).HTMLElement = {
            HTMLElement: function HTMLElement() {
                return Reflect.construct(a, [], this.constructor)
            }
        }.HTMLElement, HTMLElement.prototype = a.prototype, HTMLElement.prototype.constructor = HTMLElement, Object.setPrototypeOf(HTMLElement, a);
    })();
}

/**
 * beginning custom tag support
 *
 * This api is still experimental
 * and might be interwoven with DomQuery
 * so it is bound to change
 *
 * it follows a builder pattern to allow easier creations
 * with less code of custom tags
 */
export class TagBuilder {
    tagName: string;
    connectedCallback?: Function;
    clazz?: CustomElementConstructor;
    extendsType: CustomElementConstructor = HTMLElement;
    theOptions: ElementDefinitionOptions | null;
    markup: string;
    disconnectedCallback?: Function;
    adoptedCallback ?: Function;
    attributeChangedCallback ?: Function;
    observedAttrs: string[] = [];

    // noinspection JSUnusedGlobalSymbols
    static withTagName(tagName): TagBuilder {
        return new TagBuilder(tagName);
    }

    // noinspection JSUnusedGlobalSymbols
    constructor(tagName: string) {
        this.tagName = tagName;
    }

    // noinspection JSUnusedGlobalSymbols
    withObservedAttributes(...oAttrs) {
        this.observedAttrs = oAttrs;
    }

    // noinspection JSUnusedGlobalSymbols
    withConnectedCallback(callback: Function) {
        this.connectedCallback = callback;
        return this;
    }

    // noinspection JSUnusedGlobalSymbols
    withDisconnectedCallback(callback: Function) {
        this.disconnectedCallback = callback;
        return this;
    }

    // noinspection JSUnusedGlobalSymbols
    withAdoptedCallback(callback: Function) {
        this.adoptedCallback = callback;
        return this;
    }

    // noinspection JSUnusedGlobalSymbols
    withAttributeChangedCallback(callback: Function) {
        this.attributeChangedCallback = callback;
        return this;
    }

    // noinspection JSUnusedGlobalSymbols
    withExtendsType(extendsType: CustomElementConstructor) {
        this.extendsType = extendsType;
        return this;
    }

    // noinspection JSUnusedGlobalSymbols
    withOptions(theOptions) {
        this.theOptions = theOptions;
        return this;
    }

    // noinspection JSUnusedGlobalSymbols
    withClass(clazz) {
        if (this.markup) {
            throw Error("Markup already defined, markup must be set in the class");
        }
        this.clazz = clazz;
        return this;
    }

    // noinspection JSUnusedGlobalSymbols
    withMarkup(markup) {
        if (this.clazz) {
            throw Error("Class already defined, markup must be set in the class");
        }
        this.markup = markup;
        return this;
    }

    // noinspection JSUnusedGlobalSymbols
    register() {
        if (!this.clazz && !this.markup) {
            throw Error("Class or markup must be defined")
        }
        if (this.clazz) {

            let applyCallback = (name: string) => {
                let outerCallback = this[name];
                let protoCallback = (<any>this.clazz.prototype)[name];
                let finalCallback = outerCallback || protoCallback;
                if (finalCallback) {
                    (<any>this.clazz.prototype)[name] = function () {
                        if(outerCallback) {
                            finalCallback.apply(DomQuery.byId(this));
                        } else {
                            protoCallback.apply(<any>this);
                        }
                    }
                }
            }

            applyCallback("connectedCallback");
            applyCallback("disconnectedCallback");
            applyCallback("adoptedCallback");
            applyCallback("attributeChangedCallback");

            //TODO how do we handle the oAttrs?
            if (this.observedAttrs.length) {
                Object.defineProperty(this.clazz.prototype, "observedAttributes", {
                    get(): any {
                        return this.observedAttrs;
                    }
                });
            }

            window.customElements.define(this.tagName, this.clazz, this.theOptions || null);
        } else {
            let _t_ = this;
            let applyCallback = (name: string, scope: any) => {
                if (_t_[name]) {
                    _t_[name].apply(DomQuery.byId(<any>scope));
                }
            };

            window.customElements.define(this.tagName, class extends this.extendsType {
                constructor() {
                    super();
                    this.innerHTML = _t_.markup;
                }

                // noinspection JSUnusedGlobalSymbols
                static get observedAttributes() {
                    return _t_.observedAttrs;
                }

                // noinspection JSUnusedGlobalSymbols
                connectedCallback() {
                    applyCallback("connectedCallback", this);
                }

                // noinspection JSUnusedGlobalSymbols
                disconnectedCallback() {
                    applyCallback("disconnectedCallback", this);
                }

                // noinspection JSUnusedGlobalSymbols
                adoptedCallback() {
                    applyCallback("adoptedCallback", this);
                }

                // noinspection JSUnusedGlobalSymbols
                attributeChangedCallback() {
                    applyCallback("attributeChangedCallback", this);
                }

            }, this.theOptions || null);
        }
    }
}
