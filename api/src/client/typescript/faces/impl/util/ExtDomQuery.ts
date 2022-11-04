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
import {Config, IValueHolder, Optional, DomQuery, DQ, Stream, ArrayCollector} from "mona-dish";
import {$nsp, P_WINDOW_ID} from "../core/Const";


/**
 * detects whether a source is a faces.js request
 *
 * @param source the source string for the faces.js request
 * @return true if a faces.js loading pattern is detected
 * @constructor
 */
const IS_FACES_SOURCE = (source?: string): boolean => {
    //spec version smaller 4 we have to deal with the jsf namespace

    return source && !!(source?.search(/\/jakarta\.faces\.resource.*\/faces\.js.*/) != -1 ||
        source?.search(/\/faces-development\.js.*/) != -1 ||
        source?.search(/\/faces-uncompressed\.js.*/) != -1 ||
        source?.search(/\/faces[^.]*\.js.*ln=jakarta.faces.*/gi) != -1 ||
        //fallback without check for jsf, that way we allow both bookmarks
        source?.search(/\/javax\.faces\.resource.*\/jsf\.js.*/) != -1 ||
            source?.search(/\/jsf-development\.js.*/) != -1 ||
            source?.search(/\/jsf-uncompressed\.js.*/) != -1 ||
            source?.search(/\/jsf[^.]*\.js.*ln=javax.faces.*/gi) != -1);
}

/**
 * namespace myfaces.testscripts can be used as extension point for internal
 * tests, those will be handled similarly to faces.js - regarding
 * reload blocking on ajax requests
 *
 * @param source the source to check
 * @constructor
 */
const IS_INTERNAL_SOURCE = (source: string): boolean => {
    return source.search(/\/faces[^.]*\.js.*ln=myfaces.testscripts.*/gi) != -1 || source.search(/\/jsf[^.]*\.js.*ln=myfaces.testscripts.*/gi) != -1;
}


const ATTR_SRC = 'src';

/**
 * Extension which adds implementation specific
 * meta data to our dom query
 *
 * Usage
 * el = new ExtDQ(oldReference)
 * nonce = el.nonce
 * windowId = el.getWindowId
 */
export class ExtDomquery extends DQ {

    static get windowId() {
        return new ExtDomquery(document.body).windowId;
    }

    static get nonce(): string {
        return new ExtDomquery(document.body).nonce;
    }

    get windowId(): string | null {

        const fetchWindowIdFromURL = function (): string | null {
            let href = window.location.href;
            let windowId = "windowId";
            let regex = new RegExp("[\\?&]" + windowId + "=([^&#\\;]*)");
            let results = regex.exec(href);
            //initial trial over the url and a regexp
            if (results != null) return results[1];
            return null;
        };

        //byId ($)
        if (this.value.isPresent()) {
            let result = this.querySelectorAll("form input[name='" + P_WINDOW_ID + "']");
            if (result.length > 1) {
                throw Error("Multiple different windowIds found in document");
            }

            return (result.isPresent()) ? (<HTMLInputElement>result.getAsElem(0).value).value : fetchWindowIdFromURL();
        } else {
            return fetchWindowIdFromURL();
        }
    }

    /*
    * determines the faces.js nonce and adds them to the namespace
    * this is done once and only lazily
    */
    get nonce(): string | null {
        //already processed
        let myfacesConfig = new ExtConfig(window.myfaces);
        let nonce: IValueHolder<string> = myfacesConfig.getIf("config", "cspMeta", "nonce");
        if (nonce.value) {
            return <string>nonce.value;
        }

        let curScript = new DQ(document.currentScript);
        //since our baseline atm is ie11 we cannot use document.currentScript globally
        if (!!this.extractNonce(curScript)) {
            // fastpath for modern browsers
            return this.extractNonce(curScript);
        }
        // fallback if the currentScript method fails, we just search the jsf tags for nonce, this is
        // the last possibility
        let nonceScript = DQ
            .querySelectorAll("script[src], link[src]")
            .lazyStream
            .filter((item) => this.extractNonce(item)  && item.attr(ATTR_SRC) != null)
            .filter(item => IS_FACES_SOURCE(item.attr(ATTR_SRC).value))
            .first();

        if (nonceScript.isPresent()) {
            return this.extractNonce(nonceScript.value);
        }
        return null;
    }



    private extractNonce(curScript: DomQuery) {
        return (curScript.getAsElem(0).value as HTMLElement)?.nonce ?? curScript.attr("nonce").value;
    }

    static searchJsfJsFor(item: RegExp): Optional<String> {
        return new ExtDomquery(document).searchJsfJsFor(item);
    }

    /**
     * searches the embedded faces.js for items like separator char etc..
     * expects a match as variable under position 1 in the result match
     * @param rexp
     */
    searchJsfJsFor(rexp: RegExp): Optional<string> {
        //perfect application for lazy stream
        return DQ.querySelectorAll("script[src], link[src]").lazyStream
            .filter(item => IS_FACES_SOURCE(item.attr(ATTR_SRC).value))
            .map(item => item.attr(ATTR_SRC).value.match(rexp))
            .filter(item => item != null && item.length > 1)
            .map((result: string[]) => {
                return decodeURIComponent(result[1]);
            }).first();
    }

    globalEval(code: string, nonce ?: string): DQ {
        return new ExtDomquery(super.globalEval(code, nonce ?? this.nonce));
    }

    globalEvalSticky(code: string, nonce ?: string): DQ {
        return new ExtDomquery(super.globalEvalSticky(code, nonce ?? this.nonce));
    }

    /**
     * decorated run scripts which takes our jsf extensions into consideration
     * (standard DomQuery will let you pass anything)
     * @param sticky if set to true the internally generated element for the script is left in the dom
     * @param whilteListed
     */
    runScripts(sticky = false, whilteListed?: (src: string) => boolean): DomQuery {
        const whitelistFunc = (src: string): boolean => {
            return (whilteListed?.(src) ?? true) && !IS_FACES_SOURCE(src) && !IS_INTERNAL_SOURCE(src);
        };
        return super.runScripts(sticky, whitelistFunc);
    }

    /**
     * adds the elements in this ExtDomQuery to the head
     *
     * @param suppressDoubleIncludes checks for existing elements in the head before running the insert
     */
    runHeadInserts(suppressDoubleIncludes = true): void {
        let head = ExtDomquery.byId(document.head);
        //automated nonce handling
        let processedScripts = [];

        // the idea is only to run head inserts on resources
        // which do not exist already, that way
        // we can avoid double includes on subsequent resource
        // requests.
        function resourceIsNew(element: DomQuery) {
            if(!suppressDoubleIncludes) {
                return true;
            }
            const tagName = element.tagName.value;
            if(!tagName) {
                // textnode
                return true;
            }
            let reference = element.attr("href")
                .orElse(element.attr("src").value)
                .orElse(element.attr("rel").value);

            if (!reference.isPresent()) {
                return true;
            }
            return !head.querySelectorAll(`${tagName}[href='${reference.value}']`).length &&
                !head.querySelectorAll(`${tagName}[src='${reference.value}']`).length &&
                !head.querySelectorAll(`${tagName}[rel='${reference.value}']`).length;
        }

        this
            .filter(resourceIsNew)
            .each(element => {
                if(element.tagName.value != "SCRIPT") {
                    //we need to run runScripts properly to deal with the rest
                    new ExtDomquery(...processedScripts).runScripts(true);
                    processedScripts = [];
                    head.append(element);
                } else {
                    processedScripts.push(element);
                }
            });
        new ExtDomquery(...processedScripts).runScripts(true);
    }


    /**
     * byId producer
     *
     * @param selector id
     * @param deep whether the search should go into embedded shadow dom elements
     * @return a DomQuery containing the found elements
     */
    static byId(selector: string | DomQuery | Element, deep = false): DomQuery {
        const ret = DomQuery.byId(selector, deep);
        return new ExtDomquery(ret);
    }


}

export const ExtDQ = ExtDomquery;

/**
 * in order to reduce the number of interception points for the fallbacks we add
 * the namespace remapping straight to our config accessors
 */
export class ExtConfig extends  Config {

    constructor(root: any) {
        super(root);
    }

    assignIf(condition: boolean, ...accessPath): IValueHolder<any> {
        const acessPathMapped = this.remap(accessPath);
        return super.assignIf(condition, ...acessPathMapped);
    }

    assign(...accessPath): IValueHolder<any> {
        const acessPathMapped = this.remap(accessPath);
        return super.assign(...acessPathMapped);
    }

    append(...accessPath): IValueHolder<any> {
        return super.append(...accessPath);
    }

    appendIf(condition: boolean, ...accessPath): IValueHolder<any> {
        const acessPathMapped = this.remap(accessPath);
        return super.appendIf(condition, ...acessPathMapped);
    }

    getIf(...accessPath): Config {
        const acessPathMapped = this.remap(accessPath);
        return super.getIf(...acessPathMapped);
    }

    get(defaultVal: any): Config {
        return super.get($nsp(defaultVal));
    }

    delete(key: string): Config {
        return super.delete($nsp(key));
    }

    /**
     * creates a config from an initial value or null
     * @param value
     */
    static fromNullable<T>(value?: T | null): Config {
        return new ExtConfig(value);
    }

    protected getClass(): any {
        return ExtConfig;
    }

    /**
     * shallow copy getter, copies only the first level, references the deeper nodes
     * in a shared manner
     */
    protected shallowCopy$(): Config {
        const ret = super.shallowCopy$();
        return new ExtConfig(ret);
    }

    /**
     * deep copy, copies all config nodes
     */
    get deepCopy(): Config {
        return new ExtConfig(super.deepCopy$());
    }


    private remap(accessPath: any[]) {
        return Stream.of(...accessPath).map(key => $nsp(key)).collect(new ArrayCollector());
    }


}