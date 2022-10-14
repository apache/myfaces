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

import {Config, Optional, ValueEmbedder} from "./Monad";
import {XMLQuery} from "./XmlQuery";
import {IStream, LazyStream, Stream} from "./Stream";
import {ArrayCollector, ICollector, IStreamDataSource, ITERATION_STATUS} from "./SourcesCollectors";
import {Lang} from "./Lang";
import trim = Lang.trim;
import objToArray = Lang.objToArray;
import isString = Lang.isString;
import equalsIgnoreCase = Lang.equalsIgnoreCase;

/**
 * in order to poss custom parameters we need to extend the mutation observer init
 */
export interface WAIT_OPTS extends MutationObserverInit {
    timeout ?: number;
    /**
     * interval on non legacy browsers
     */
    interval ?: number;
}


/**
 *
 *        // - submit checkboxes and radio inputs only if checked
 if ((tagName != "select" && elemType != "button"
 && elemType != "reset" && elemType != "submit" && elemType != "image")
 && ((elemType != "checkbox" && elemType != "radio"
 */

enum Submittables {
    SELECT = "select",
    BUTTON = "button",
    SUBMIT = "submit",
    RESET = "reset",
    IMAGE = "image",
    RADIO = "radio",
    CHECKBOX = "checkbox"

}

/**
 * helper to fix a common problem that a system has to wait until a certain condition is reached
 * depening on the browser this uses either the mutation observer or a semi compatible interval as fallback
 * @param root the root domquery element to start from
 * @param condition the condition lambda to be fullfilled
 * @param options options for the search
 */
function waitUntilDom(root: DomQuery, condition: (element: DomQuery) => boolean, options: WAIT_OPTS = { attributes: true, childList: true, subtree: true, timeout: 500, interval: 100 }): Promise<DomQuery> {
    return new Promise<DomQuery>((success, error) => {
        const MUT_ERROR = new Error("Mutation observer timeout");

        //we do the same but for now ignore the options on the dom query
        //we cannot use absent here, because the condition might search for an absent element
        function findElement(root: DomQuery, condition: (element: DomQuery) => boolean): DomQuery | null {
            let found = null;
            if (condition(root)) {
                return root;
            }
            if (options.childList) {
                found = (condition(root)) ? root : root.childNodes.first(condition).value.value;
            } else if (options.subtree) {
                found = (condition(root)) ? root : root.querySelectorAll(" * ").first(condition).value;
            } else {
                found = (condition(root)) ? root : null;
            }
            return found;
        }

        let foundElement = root;
        if ((foundElement = findElement(foundElement, condition)) != null) {
            success(new DomQuery(foundElement));
            return;
        }

        if ('undefined' != typeof MutationObserver) {
            const mutTimeout = setTimeout(() => {
                return error(MUT_ERROR);
            }, options.timeout);
            const callback: MutationCallback = (mutationList: MutationRecord[]) => {
                const found = new DomQuery(mutationList.map((mut: MutationRecord) => mut.target)).first(condition);
                if (found) {
                    clearTimeout(mutTimeout);
                    success(found);
                }
            }

            const observer = new window.MutationObserver(callback);
            // browsers might ignore it, but we cannot break the api in the case
            // hence no timeout is passed
            let observableOpts = {...options};
            delete observableOpts.timeout;
            root.eachElem(item => {
                observer.observe(item, observableOpts)
            })
        } else { //fallback for legacy browsers without mutation observer

            let interval = setInterval(() => {
                let found = findElement(root, condition);
                if (found) {
                    if (timeout) {
                        clearTimeout(timeout);
                        clearInterval(interval);
                        interval = null;
                        success(found);
                    }
                }
            }, options.interval);
            let timeout = setTimeout(() => {
                if (interval) {
                    clearInterval(interval);
                    error(MUT_ERROR);
                }
            }, options.timeout)

        }
    });
}


export class ElementAttribute extends ValueEmbedder<string> {

    constructor(private element: DomQuery, private name: string, private defaultVal: string = null) {
        super(element, name);
    }

    get value(): string {
        let val: Element[] = this.element.get(0).orElse(...[]).values;
        if (!val.length) {
            return this.defaultVal;
        }
        return val[0].getAttribute(this.name);
    }

    set value(value: string) {
        let val: Element[] = this.element.get(0).orElse(...[]).values;
        for (let cnt = 0; cnt < val.length; cnt++) {
            val[cnt].setAttribute(this.name, value);
        }
        val[0].setAttribute(this.name, value);
    }

    protected getClass(): any {
        return ElementAttribute;
    }

    static fromNullable<ElementAttribute,T>(value?: any, valueKey: string = "value"): ElementAttribute {
        return <any> new ElementAttribute(value, valueKey);
    }

}

export class Style extends ValueEmbedder<string> {

    constructor(private element: DomQuery, private name: string, private defaultVal: string = null) {
        super(element, name);
    }

    get value(): string {
        let val: Element[] = this.element.values;
        if (!val.length) {
            return this.defaultVal;
        }
        return (val[0] as HTMLElement).style[this.name];
    }

    set value(value: string) {
        let val: HTMLElement[] = this.element.values as HTMLElement[];
        for (let cnt = 0; cnt < val.length; cnt++) {
            val[cnt].style[this.name] = value;
        }
    }

    protected getClass(): any {
        return ElementAttribute;
    }

    static fromNullable<ElementAttribute,T>(value?: any, valueKey: string = "value"): ElementAttribute {
        return <any> new ElementAttribute(value, valueKey);
    }

}

/**
 * small helper for the specialized jsf case
 * @constructor
 */
const DEFAULT_WHITELIST = () => {
    return true;
};

interface IDomQuery {
    /**
     * reads the first element if it exists and returns an optional
     */
    readonly value: Optional<Element>;
    /**
     * All elements as array
     */
    readonly values: Element[];
    /**
     * returns the id as settable value (See also ValueEmbedder)
     */
    readonly id: ValueEmbedder<string>;
    /**
     * returns the length of embedded nodes (top level)
     */
    readonly length: number;
    /**
     * the tag name of the first element
     */
    readonly tagName: Optional<string>;
    /**
     * the node name of the first element
     */
    readonly nodeName: Optional<string>;
    /**
     * the type of the first element
     */
    readonly type: Optional<string>;
    /**
     * The name as changeable value
     */
    readonly name: ValueEmbedder<string>;
    /**
     * The the value in case of inputs as changeable value
     */
    readonly inputValue: ValueEmbedder<string | boolean>;

    /**
     * accumulated top element offsetWidth
     */
    readonly offsetWidth: number;
    /**
     * accumulated top element offsetHeight
     */
    readonly offsetHeight: number;
    /**
     * accumulated top element offsetLeft
     */
    readonly offsetLeft: number;
    /**
     * accumulated top element offsetTop
     */
    readonly offsetTop: number;


    /**
     * abbreviation for inputValue.value to make
     * the code terser
     */
    val: string | boolean;

    /**
     * the underlying form elements as domquery object
     */
    readonly elements: DomQuery;
    /**
     * settable flag for disabled
     */
    disabled: boolean;
    /**
     * The child nodes of this node collection as readonly attribute
     */
    readonly childNodes: DomQuery;
    /**
     * an early stream representation for this DomQuery
     */
    readonly stream: Stream<DomQuery>;
    /**
     * lazy stream representation for this DomQuery
     */
    readonly lazyStream: LazyStream<DomQuery>;
    /**
     * transform this node collection to an array
     */
    readonly asArray: Array<DomQuery>;

    /**
     * inner html property
     * setter and getter which works directly on strings
     */
    innerHTML: string;

    /**
     * same as innerHTML
     * will be removed once
     * my code is transitioned
     * @deprecated do not use anymore, user innerHTML instead
     */
    innerHtml: string;

    /**
     * returns true if the elements have the tag *tagName* as tag embedded (highest level)
     * @param tagName
     */
    isTag(tagName: string): boolean;

    /**
     * returns the nth element as domquery
     * from the internal elements
     * note if you try to reach a non existing element position
     * you will get back an absent entry
     *
     * @param index the nth index
     */
    get(index: number): DomQuery;

    /**
     * returns the nth element as optional of an Element object
     * @param index the number from the index
     * @param defaults the default value if the index is overrun default Optional.absent
     */
    getAsElem(index: number, defaults: Optional<any>): Optional<Element>;

    /**
     * returns the value array< of all elements
     */
    allElems(): Array<Element>;

    /**
     * absent no values reached?
     */
    isAbsent(): boolean;

    /**
     * should make the code clearer
     * note if you pass a function
     * this refers to the active dopmquery object
     */
    isPresent(presentRunnable ?: (elem ?: DomQuery) => void): boolean;

    /**
     * should make the code clearer
     * note if you pass a function
     * this refers to the active dopmquery object
     *
     *
     * @param presentRunnable
     */
    ifPresentLazy(presentRunnable: (elem ?: DomQuery) => void): DomQuery;

    /**
     * remove all affected nodes from this query object from the dom tree
     */
    delete(): void;

    /**
     * query selector all on the existing dom query object
     *
     * @param selector the standard selector
     * @return a DomQuery with the results
     */
    querySelectorAll(selector): DomQuery;

    /**
     * core byId method
     * @param id the id to search for
     * @param includeRoot also match the root element?
     */
    byId(id: string, includeRoot?: boolean): DomQuery;

    /**
     * same as byId just for the tag name
     * @param tagName
     * @param includeRoot
     */
    byTagName(tagName: string, includeRoot ?: boolean): DomQuery;

    /**
     * attr accessor, usage myQuery.attr("class").value = "bla"
     * or let value myQuery.attr("class").value
     * @param attr the attribute to set
     * @param defaultValue the default value in case nothing is presented (defaults to null)
     */
    attr(attr: string, defaultValue: string): ElementAttribute;

    /**
     * style accessor
     * @param defaultValue the default value in case nothing is presented (defaults to null)
     * @param cssProperty
     */
    style(cssProperty: string, defaultValue: string): Style;

    /**
     * hasclass, checks for an existing class in the class attributes
     *
     * @param clazz the class to search for
     */
    hasClass(clazz: string): boolean;

    /**
     * appends a class string if not already in the element(s)
     *
     * @param clazz the style class to append
     */
    addClass(clazz: string): DomQuery;

    /**
     * remove the style class if in the class definitions
     *
     * @param clazz
     */
    removeClass(clazz: string): DomQuery;

    /**
     * checks whether we have a multipart element in our children
     */
    isMultipartCandidate(): boolean;

    /**
     * innerHtml equivalent
     * equivalent to jQueries html
     * as setter the html is set and the
     * DomQuery is given back
     * as getter the html string is returned
     *
     * @param inval
     */
    html(inval?: string): DomQuery | Optional<string>;

    /**
     * dispatch event on all children
     * just a delegated dispatchevent from the standard
     * dom working on all queried elements in the monad level
     *
     * @param evt the event to be dispatched
     */
    dispatchEvent(evt: Event): DomQuery;

    /**
     * easy node traversal, you can pass
     * a set of node selectors which are joined as direct childs
     *
     * not the rootnodes are not in the getIf, those are always the child nodes
     *
     * @param nodeSelector
     */
    getIf(...nodeSelector: Array<string>): DomQuery;

    /**
     * iterate over each element and perform something on the element
     * (Dom element is passed instead of DomQuery)
     * @param func
     */
    eachElem(func: (item: Element, cnt?: number) => any): DomQuery;

    /**
     * perform an operation on the first element
     * returns a DomQuery on the first element only
     * @param func
     */
    firstElem(func: (item: Element, cnt?: number) => any): DomQuery;

    /**
     * perform an operation on the first element
     * returns a DomQuery on the first element only
     * @param func
     */
    lastElem(func: (item: Element, cnt?: number) => any): DomQuery;

    /**
     * same as eachElem, but a DomQuery object is passed down
     *
     * @param func
     */
    each(func: (item: DomQuery, cnt?: number) => any): DomQuery;

    /**
     * returns a new dom query containing only the first element max
     *
     * @param func a an optional callback function to perform an operation on the first element
     */
    first(func: (item: DomQuery, cnt?: number) => any): DomQuery;


    /**
     * returns a new dom query containing only the first element max
     *
     * @param func a an optional callback function to perform an operation on the first element
     */
    last(func: (item: DomQuery, cnt?: number) => any): DomQuery;

    /**
     * filter function which filters a subset
     *
     * @param func
     */
    filter(func: (item: DomQuery) => boolean): DomQuery;

    /**
     * global eval head appendix method
     * no other methods are supported anymore
     * @param code the code to be evaled
     * @param  nonce optional  nonce key for higher security
     */
    globalEval(code: string, nonce ?: string): DomQuery;

    /**
     * detaches a set of nodes from their parent elements
     * in a browser independend manner
     * @return {DomQuery} DomQuery of nodes with the detached dom nodes
     */
    detach(): DomQuery;

    /**
     * appends the current set of elements
     * to the element or first element passed via elem
     * @param elem
     */
    appendTo(elem: DomQuery | string): DomQuery;

    /**
     * appends the passed elements to our existing queries
     * note, double appends can happen if you are not careful
     *
     * @param elem to append
     */
    append(elem: DomQuery): DomQuery;

    /**
     * appends the passed elements to our existing queries
     * note, double appends can happen if you are not careful
     *
     * @param elem to append
     */
    prepend(elem: DomQuery): DomQuery;

    /**
     * prepend eqivalent to appendTo
     *
     * @param elem the element to prepend to
     */
    prependTo(elem: DomQuery): DomQuery;

    /**
     * loads and evals a script from a source uri
     *
     * @param src the source to be loaded and evaled
     * @param defer in miliseconds execution default (0 == no defer)
     * @param charSet
     */
    loadScriptEval(src: string, defer: number, charSet: string): void;

    /**
     * insert toInsert after the current element
     *
     * @param toInsert an array of DomQuery objects
     */
    insertAfter(...toInsert: Array<DomQuery>): DomQuery;

    /**
     * inserts the elements before the current element
     *
     * @param toInsert
     */
    insertBefore(...toInsert: Array<DomQuery>): DomQuery;

    /**
     * in case the domquery is pointing to nothing the else value is taken into consideration
     * als alternative
     *
     * @param elseValue the else value
     */
    orElse(...elseValue: any): DomQuery;

    /**
     * the same with lazy evaluation for cases where getting the else value
     * is a heavy operation
     *
     * @param func the else provider function
     */
    orElseLazy(func: () => any): DomQuery;

    /**
     * all parents with TagName
     * @param tagName
     */
    parents(tagName: string): DomQuery;

    /**
     * copy all attributes of sourceItem to this DomQuery items
     *
     * @param sourceItem the source item to copy over (can be another domquery or a parsed XML Query item)
     */
    copyAttrs(sourceItem: DomQuery | XMLQuery): DomQuery;

    /**
     * outerhtml convenience method
     * browsers only support innerHTML but
     * for instance for your jsf.js we have a full
     * replace pattern which needs outerHTML processing
     *
     * @param markup
     * @param runEmbeddedScripts
     * @param runEmbeddedCss
     */
    outerHTML(markup: string, runEmbeddedScripts ?: boolean, runEmbeddedCss ?: boolean): DomQuery;

    /**
     * Run through the given nodes in the DomQuery execute the inline scripts
     * @param whilteListed: optional whitelist function which can filter out script tags which are not processed
     * defaults to the standard jsf.js exclusion (we use this code for myfaces)
     */
    runScripts(whilteListed: (val: string) => boolean): DomQuery;

    /**
     * runs the embedded css
     */
    runCss(): DomQuery;

    /**
     * fires a click event on the underlying dom elements
     */
    click(): DomQuery;

    /**
     * adds an event listener
     *
     * @param type
     * @param listener
     * @param options
     */
    addEventListener(type: string, listener: (evt: Event) => void, options?: boolean | EventListenerOptions): DomQuery;

    /**
     * removes an event listener
     *
     * @param type
     * @param listener
     * @param options
     */
    removeEventListener(type: string, listener: (evt: Event) => void, options?: boolean | EventListenerOptions): DomQuery;

    /**
     * fires an event
     */
    fireEvent(eventName: string): void;

    /*
     * pushes  in optionally a new textContent, and/or returns the current text content
     */
    textContent(joinstr?: string): string;

    /*
     * pushes  in optionally a new innerText, and/or returns the current innerText
     */
    innerText(joinstr?: string): string;

    /**
     * encodes all input elements properly into respective
     * config entries, this can be used
     * for legacy systems, for newer usecases, use the
     * HTML5 Form class which all newer browsers provide
     *
     * @param toMerge optional config which can be merged in
     * @return a copy pf
     */
    encodeFormElement(toMerge): Config;

    /**
     * fetches the subnodes from ... to..
     * @param from
     * @param to
     */
    subNodes(from: number, to?: number): DomQuery;


    /**
     * attach shadow elements
     * 1:1 mapping from attach shadow
     *
     * @param modeParams
     */
    attachShadow(modeParams: { [key: string]: string }): DomQuery


    /**
     * wait until a condition on the dom is reached
     *
     * @return a promise on the affected elements where the condition
     * @throws an error in case of a timeout
     */
    waitUntilDom(condition: (element: DomQuery) => boolean, options: WAIT_OPTS): Promise<DomQuery>;

    //observable: Observable<DomQuery>;

    //observableElem: Observable<Element>;
}

/**
 * Monadic DomNode representation, ala jquery
 * This is a thin wrapper over querySelectorAll
 * to get slim monadic support
 * to reduce implementation code on the users side.
 * This is vital for frameworks which want to rely on
 * plain dom but still do not want to lose
 * the reduced code footprint of querying dom trees and traversing
 * by using functional patterns.
 *
 * Also a few convenience methods are added to reduce
 * the code footprint of standard dom processing
 * operations like eval
 *
 * TODO add jquery fallback support, since it is supported
 * in most older systems
 * Note parts of this code still stem from the Dom.js I have written 10 years
 * ago, those parts look a little bit ancient and will be replaced over time.
 *
 */
export class DomQuery implements IDomQuery, IStreamDataSource<DomQuery>, Iterable<DomQuery> {

    static absent = new DomQuery();

    private rootNode: Array<Element> = [];

    pos = -1;

    constructor(...rootNode: Array<Element | ShadowRoot | DomQuery | Document | Array<any> | string>) {

        if (Optional.fromNullable(rootNode).isAbsent() || !rootNode.length ) {
            return;
        } else {
            //we need to flatten out the arrays

            for (let cnt = 0; cnt < rootNode.length; cnt++) {
                if(!rootNode[cnt]) {
                    //we skip possible null entries which can happen in
                    //certain corner conditions due to the constructor re-wrapping single elements into arrays.
                } else if (isString(rootNode[cnt])) {
                    let foundElement = DomQuery.querySelectorAll(<string>rootNode[cnt]);
                    if (!foundElement.isAbsent()) {
                        rootNode.push(...foundElement.values)
                    }
                } else if (rootNode[cnt] instanceof DomQuery) {
                    this.rootNode.push(...(<any>rootNode[cnt]).values);
                } else {
                    this.rootNode.push(<any>rootNode[cnt]);
                }
            }
        }
    }



    /**
     * returns the first element
     */
    get value(): Optional<Element> {
        return this.getAsElem(0);
    }

    get values(): Element[] {
        return this.allElems();
    }

    /**
     * returns the id of the first element
     */
    get id(): ValueEmbedder<string> {
        return new ElementAttribute(this.get(0), "id");
    }

    /**
     * length of the entire query set
     */
    get length(): number {
        return this.rootNode.length
    }

    /**
     * convenience method for tagName
     */
    get tagName(): Optional<string> {
        return <Optional<string>>this.getAsElem(0).getIf("tagName");
    }

    /**
     * convenience method for nodeName
     */
    get nodeName(): Optional<string> {
        return <Optional<string>>this.getAsElem(0).getIf("nodeName");
    }

    isTag(tagName: string): boolean {
        return !this.isAbsent()
            && (this.nodeName.orElse("__none___")
                    .value.toLowerCase() == tagName.toLowerCase()
                || this.tagName.orElse("__none___")
                    .value.toLowerCase() == tagName.toLowerCase()
            )
    }

    /**
     * convenience property for type
     *
     * returns null in case of no type existing otherwise
     * the type of the first element
     */
    get type(): Optional<string> {
        return this.getAsElem(0).getIf("type");
    }

    /**
     * convenience property for name
     *
     * returns null in case of no type existing otherwise
     * the name of the first element
     */
    get name(): ValueEmbedder<string> {
        return new ValueEmbedder(this.getAsElem(0).value, "name");
    }

    /**
     * convenience property for value
     *
     * returns null in case of no type existing otherwise
     * the value of the first element
     */
    get inputValue(): ValueEmbedder<string | boolean> {
        if (this.getAsElem(0).getIf("value").isPresent()) {
            return new ValueEmbedder<string>(this.getAsElem(0).value);
        } else {
            return <any>ValueEmbedder.absent;
        }
    }

    get val(): string | boolean {
        return this.inputValue.value;
    }

    set val(value: string | boolean) {
        this.inputValue.value = value;
    }

    get checked(): boolean {
        return Stream.of(...this.values).allMatch(el => !!(<any>el).checked);
    }

    set checked(newChecked: boolean) {
        this.eachElem(el => (<any>el).checked = newChecked);
    }

    get elements(): DomQuery {
        //a simple querySelectorAll should suffice
        return this.querySelectorAll("input, checkbox, select, textarea, fieldset");
    }

    get deepElements(): DomQuery {
        let elemStr = "input, select, textarea, checkbox, fieldset";
        return this.querySelectorAllDeep(elemStr);
    }

    /**
     * a deep search which treats the single isolated shadow doms
     * separately and runs the query on earch shadow dom
     * @param queryStr
     */
    querySelectorAllDeep(queryStr: string): DomQuery {
        let found: Array<DomQuery> = [];
        let queryRes = this.querySelectorAll(queryStr);
        if(queryRes.length) {
            found.push(queryRes);
        }
        let shadowRoots = this.querySelectorAll("*").shadowRoot;
        if(shadowRoots.length) {
            let shadowRes = shadowRoots.querySelectorAllDeep(queryStr);
            if(shadowRes.length) {
                found.push(shadowRes);
            }
        }
        return new DomQuery(...found);
    }



    /**
     * todo align this api with the rest of the apis
     */
    get disabled(): boolean {
        return this.attr("disabled").isPresent();
    }

    set disabled(disabled: boolean) {
        // this.attr("disabled").value = disabled + "";
        if (!disabled) {
            this.removeAttribute("disabled");
        } else {
            this.attr("disabled").value = "disabled";
        }

    }

    removeAttribute(name: string) {
        this.eachElem(item => item.removeAttribute(name));
    }

    get childNodes(): DomQuery {
        let childNodeArr: Array<Element> = [];
        this.eachElem((item: Element) => {
            childNodeArr = childNodeArr.concat(objToArray(item.childNodes));
        });
        return new DomQuery(...childNodeArr);
    }

    /**
     * binding into stream
     */
    get stream(): Stream<DomQuery> {
        return new Stream<DomQuery>(...this.asArray);
    }

    /**
     * fetches a lazy stream representation
     * lazy should be applied if you have some filters etc
     * in between, this can reduce the number of post filter operations
     * and ram usage
     * significantly because the operations are done lazily and stop
     * once they hit a dead end.
     */
    get lazyStream(): LazyStream<DomQuery> {
        return LazyStream.of(...this.asArray);
    }

    get asArray(): Array<DomQuery> {
        //filter not supported by IE11
        return [].concat(LazyStream.of(...this.rootNode).filter(item => {
            return item != null
        })
            .map(item => {
                return DomQuery.byId(item)
            }).collect(new ArrayCollector()));
    }

    get offsetWidth(): number {
        return LazyStream.of(...this.rootNode)
            .filter(item => item != null)
            .map(elem => (elem as HTMLElement).offsetWidth)
            .reduce((accumulate, incoming) => accumulate + incoming,0).value;
    }
    get offsetHeight(): number {
        return LazyStream.of(...this.rootNode)
            .filter(item => item != null)
            .map(elem => (elem as HTMLElement).offsetHeight)
            .reduce((accumulate, incoming) => accumulate + incoming,0).value;
    }

    get offsetLeft(): number {
        return LazyStream.of(...this.rootNode)
            .filter(item => item != null)
            .map(elem => (elem as HTMLElement).offsetLeft)
            .reduce((accumulate, incoming) => accumulate + incoming,0).value;
    }

    get offsetTop(): number {
        return LazyStream.of(...this.rootNode)
            .filter(item => item != null)
            .map(elem => (elem as HTMLElement).offsetTop)
            .reduce((accumulate, incoming) => accumulate + incoming,0).value;
    }

    get asNodeArray(): Array<DomQuery> {
        return [].concat(Stream.of(this.rootNode).filter(item => item != null).collect(new ArrayCollector()));
    }


    static querySelectorAllDeep(selector: string) {
        return new DomQuery(document).querySelectorAllDeep(selector);
    }
    /**
     * easy query selector all producer
     *
     * @param selector the selector
     * @returns a results dom query object
     */
    static querySelectorAll(selector: string): DomQuery {
        if (selector.indexOf("/shadow/") != -1) {
            return new DomQuery(document)._querySelectorAllDeep(selector);
        } else {
            return new DomQuery(document)._querySelectorAll(selector);
        }
    }

    /**
     * byId producer
     *
     * @param selector id
     * @param deep true if you want to go into shadow areas
     * @return a DomQuery containing the found elements
     */
    static byId(selector: string | DomQuery | Element, deep = false): DomQuery {
        if (isString(selector)) {
            return (!deep) ? new DomQuery(document).byId(<string>selector) : new DomQuery(document).byIdDeep(<string>selector);
        } else {
            return new DomQuery(<any>selector);
        }
    }

    /**
     * byTagName producer
     *
     * @param selector name
     * @return a DomQuery containing the found elements
     */
    static byTagName(selector: string | DomQuery | Element): DomQuery {
        if (isString(selector)) {
            return new DomQuery(document).byTagName(<string>selector);
        } else {
            return new DomQuery(<any>selector);
        }
    }

    static globalEval(code: string, nonce?: string): DomQuery {
        return new DomQuery(document).globalEval(code, nonce);
    }

    /**
     * builds the ie nodes properly in a placeholder
     * and bypasses a non script insert bug that way
     * @param markup the marku code
     */
    static fromMarkup(markup: string): DomQuery {

        //https://developer.mozilla.org/de/docs/Web/API/DOMParser license creative commons
        const doc = document.implementation.createHTMLDocument("");
        markup = trim(markup);
        let lowerMarkup = markup.toLowerCase();
        if (lowerMarkup.indexOf('<!doctype') != -1 ||
            lowerMarkup.indexOf('<html') != -1 ||
            lowerMarkup.indexOf('<head') != -1 || //TODO proper regexps here to avoid embedded tags with same element names to be triggered
            lowerMarkup.indexOf('<body') != -1) {
            doc.documentElement.innerHTML = markup;
            return new DomQuery(doc.documentElement);
        } else {
            let startsWithTag = function (str: string, tagName: string) {
                let tag1 = ["<", tagName, ">"].join("");
                let tag2 = ["<", tagName, " "].join("");
                return (str.indexOf(tag1) == 0) || (str.indexOf(tag2) == 0);
            };

            let dummyPlaceHolder = new DomQuery(document.createElement("div"));

            //table needs special treatment due to the browsers auto creation
            if (startsWithTag(lowerMarkup, "thead") || startsWithTag(lowerMarkup, "tbody")) {
                dummyPlaceHolder.html(`<table>${markup}</table>`);
                return dummyPlaceHolder.querySelectorAll("table").get(0).childNodes.detach();
            } else if (startsWithTag(lowerMarkup, "tfoot")) {
                dummyPlaceHolder.html(`<table><thead></thead><tbody><tbody${markup}</table>`);
                return dummyPlaceHolder.querySelectorAll("table").get(2).childNodes.detach();
            } else if (startsWithTag(lowerMarkup, "tr")) {
                dummyPlaceHolder.html(`<table><tbody>${markup}</tbody></table>`);
                return dummyPlaceHolder.querySelectorAll("tbody").get(0).childNodes.detach();
            } else if (startsWithTag(lowerMarkup, "td")) {
                dummyPlaceHolder.html(`<table><tbody><tr>${markup}</tr></tbody></table>`);
                return dummyPlaceHolder.querySelectorAll("tr").get(0).childNodes.detach();
            }

            dummyPlaceHolder.html(markup);
            return dummyPlaceHolder.childNodes.detach();
        }

    }

    /**
     * returns the nth element as domquery
     * from the internal elements
     * note if you try to reach a non existing element position
     * you will get back an absent entry
     *
     * @param index the nth index
     */
    get(index: number): DomQuery {
        return (index < this.rootNode.length) ? new DomQuery(this.rootNode[index]) : DomQuery.absent;
    }



    /**
     * returns the nth element as optional of an Element object
     * @param index the number from the index
     * @param defaults the default value if the index is overrun default Optional.absent
     */
    getAsElem(index: number, defaults: Optional<any> = Optional.absent): Optional<Element> {
        return (index < this.rootNode.length) ? Optional.fromNullable(this.rootNode[index]) : defaults;
    }

    /**
     * returns the files from a given elmement
     * @param index
     */
    filesFromElem(index: number): Array<any> {
        return (index < this.rootNode.length) ? (<any>this.rootNode[index])?.files ?  (<any>this.rootNode[index]).files : [] : [];
    }

    /**
     * returns the value array< of all elements
     */
    allElems(): Array<Element> {
        return this.rootNode;
    }

    /**
     * absent no values reached?
     */
    isAbsent(): boolean {
        return this.length == 0;
    }

    /**
     * should make the code clearer
     * note if you pass a function
     * this refers to the active dopmquery object
     */
    isPresent(presentRunnable ?: (elem ?: DomQuery) => void): boolean {
        let absent = this.isAbsent();
        if (!absent && presentRunnable) {
            presentRunnable.call(this, this)
        }
        return !absent;
    }

    /**
     * should make the code clearer
     * note if you pass a function
     * this refers to the active dopmquery object
     *
     *
     * @param presentRunnable
     */
    ifPresentLazy(presentRunnable: (elem ?: DomQuery) => void = function () {
    }): DomQuery {
        this.isPresent.call(this, presentRunnable);
        return this;
    }

    /**
     * remove all affected nodes from this query object from the dom tree
     */
    delete() {
        this.eachElem((node: Element) => {
            if (node.parentNode) {
                node.parentNode.removeChild(node);
            }
        });
    }

    querySelectorAll(selector): DomQuery {
        //We could merge both methods, but for now this is more readable
        if (selector.indexOf("/shadow/") != -1) {
            return this._querySelectorAllDeep(selector);
        } else {
            return this._querySelectorAll(selector);
        }
    }

    /**
     * query selector all on the existing dom queryX object
     *
     * @param selector the standard selector
     * @return a DomQuery with the results
     */
    private _querySelectorAll(selector): DomQuery {
        if (!this?.rootNode?.length) {
            return this;
        }
        let nodes = [];
        for (let cnt = 0; cnt < this.rootNode.length; cnt++) {
            if (!this.rootNode[cnt]?.querySelectorAll) {
                continue;
            }
            let res = this.rootNode[cnt].querySelectorAll(selector);
            nodes = nodes.concat(objToArray(res));
        }

        return new DomQuery(...nodes);
    }


    /*deep with a selector and a peudo /shadow/ marker to break into the next level*/
    private _querySelectorAllDeep(selector): DomQuery {
        if (!this?.rootNode?.length) {
            return this;
        }

        let foundNodes: DomQuery = new DomQuery(...this.rootNode);
        let selectors = selector.split(/\/shadow\//);

        for (let cnt2 = 0; cnt2 < selectors.length; cnt2++) {
            if (selectors[cnt2] == "") {
                continue;
            }
            let levelSelector = selectors[cnt2];
            foundNodes = foundNodes.querySelectorAll(levelSelector);
            if (cnt2 < selectors.length - 1) {
                foundNodes = foundNodes.shadowRoot;
            }
        }

        return foundNodes;
    }

    /**
     * core byId method
     * @param id the id to search for
     * @param includeRoot also match the root element?
     */
    byId(id: string, includeRoot?: boolean): DomQuery {
        let res: Array<DomQuery> = [];
        if (includeRoot) {
            res = res.concat(
                LazyStream.of(...(this?.rootNode || []))
                    .filter(item => id == item.id)
                    .map(item => new DomQuery(item))
                    .collect(new ArrayCollector())
            );
        }

        //for some strange kind of reason the # selector fails
        //on hidden elements we use the attributes match selector
        //that works
        res = res.concat(this.querySelectorAll(`[id="${id}"]`));
        return new DomQuery(...res);
    }


    byIdDeep(id: string, includeRoot?: boolean): DomQuery {
        let res: Array<DomQuery> = [];
        if (includeRoot) {
            res = res.concat(
                LazyStream.of(...(this?.rootNode || []))
                    .filter(item => id == item.id)
                    .map(item => new DomQuery(item))
                    .collect(new ArrayCollector())
            );
        }

        let subItems = this.querySelectorAllDeep(`[id="${id}"]`);
        if(subItems.length) {
            res.push(subItems);
        }

        return new DomQuery(...res);
    }

    /**
     * same as byId just for the tag name
     * @param tagName the tagname to search for
     * @param includeRoot shall the root element be part of this search
     * @param deep do we also want to go into shadow dom areas
     */
    byTagName(tagName: string, includeRoot ?: boolean, deep ?: boolean): DomQuery {
        let res: Array<Element | DomQuery> = [];
        if (includeRoot) {
            res = <any> LazyStream.of(...(this?.rootNode ?? []))
                .filter(element => element?.tagName == tagName)
                .reduce<Array<Element | DomQuery>>((reduction: any, item: Element) => reduction.concat([item]), res)
                .orElse(res).value;
        }

        (deep) ? res.push(this.querySelectorAllDeep(tagName)) : res.push(this.querySelectorAll(tagName));
        return new DomQuery(...res);
    }

    /**
     * attr accessor, usage myQuery.attr("class").value = "bla"
     * or let value myQuery.attr("class").value
     * @param attr the attribute to set
     * @param defaultValue the default value in case nothing is presented (defaults to null)
     */
    attr(attr: string, defaultValue: string = null): ElementAttribute {
        return new ElementAttribute(this, attr, defaultValue);
    }

    style(cssProperty: string, defaultValue: string = null): Style {
        return new Style(this, cssProperty, defaultValue);
    }


    /**
     * hasclass, checks for an existing class in the class attributes
     *
     * @param clazz the class to search for
     */
    hasClass(clazz: string) {
        let hasIt = false;
        this.eachElem(node => {
            hasIt = node.classList.contains(clazz);
            if (hasIt) {
                return false;
            }
        });
        return hasIt;
    }

    /**
     * appends a class string if not already in the element(s)
     *
     * @param clazz the style class to append
     */
    addClass(clazz: string): DomQuery {
        this.eachElem(item => item.classList.add(clazz))
        return this;
    }

    /**
     * remove the style class if in the class definitions
     *
     * @param clazz
     */
    removeClass(clazz: string): DomQuery {
        this.eachElem(item => item.classList.remove(clazz));
        return this;
    }

    /**
     * checks whether we have a multipart element in our children
     * or are one
     */
    isMultipartCandidate(deep = false): boolean {
        const FILE_INPUT = "input[type='file']";
        return this.matchesSelector(FILE_INPUT) ||
            ((!deep) ? this.querySelectorAll(FILE_INPUT) :
                this.querySelectorAllDeep(FILE_INPUT)).first().isPresent();
    }

    /**
     * innerHtml equivalkent
     * equivalent to jqueries html
     * as setter the html is set and the
     * DomQuery is given back
     * as getter the html string is returned
     *
     * @param inval
     */
    html(inval?: string): DomQuery | Optional<string> {
        if (Optional.fromNullable(inval).isAbsent()) {
            return this.isPresent() ? Optional.fromNullable(this.innerHTML) : Optional.absent;
        }
        this.innerHTML = inval;

        return this;
    }

    /**
     * Standard dispatch event method, delegated from node
     */
    dispatchEvent(evt: Event): DomQuery {
        this.eachElem(elem => elem.dispatchEvent(evt));
        return this;
    }

    set innerHTML(inVal: string) {
        this.eachElem(elem => elem.innerHTML = inVal);
    }

    get innerHTML(): string {
        let retArr = [];
        this.eachElem(elem => retArr.push(elem.innerHTML));
        return retArr.join("");
    }

    set innerHtml(inval: string) {
        this.innerHTML = inval;
    }

    get innerHtml(): string {
        return this.innerHTML;
    }

    //source: https://developer.mozilla.org/en-US/docs/Web/API/Element/matches
    //code snippet license: https://creativecommons.org/licenses/by-sa/2.5/
    private _mozMatchesSelector(toMatch: Element, selector: string): boolean {
        let prot: { [key: string]: Function } = (<any>toMatch);
        let matchesSelector: Function = prot.matches ||
            prot.matchesSelector ||
            prot.mozMatchesSelector ||
            prot.msMatchesSelector ||
            prot.oMatchesSelector ||
            prot.webkitMatchesSelector ||
            function (s: string) {
                let matches: NodeListOf<HTMLElement> = (document || (<any>window).ownerDocument).querySelectorAll(s),
                    i = matches.length;
                while (--i >= 0 && matches.item(i) !== toMatch) {
                }
                return i > -1;
            };
        return matchesSelector.call(toMatch, selector);
    }

    /**
     * filters the current dom query elements
     * upon a given selector
     *
     * @param selector
     */
    filterSelector(selector: string): DomQuery {
        let matched = [];

        this.eachElem(item => {
            if (this._mozMatchesSelector(item, selector)) {
                matched.push(item)
            }
        });
        return new DomQuery(...matched);
    }

    /**
     * checks whether any item in this domQuery level matches the selector
     * if there is one element only attached, as root the match is only
     * performed on this element.
     * @param selector
     */
    matchesSelector(selector: string): boolean {
        const ret = this.lazyStream
            .map(item => this._mozMatchesSelector(item.getAsElem(0).value, selector))
            .filter(match => match)
            .first();
        return ret.isPresent();
    }

    /**
     * easy node traversal, you can pass
     * a set of node selectors which are joined as direct childs
     *
     * not the rootnodes are not in the getIf, those are always the child nodes
     *
     * @param nodeSelector
     */
    getIf(...nodeSelector: Array<string>): DomQuery {

        let selectorStage: DomQuery = this.childNodes;
        for (let cnt = 0; cnt < nodeSelector.length; cnt++) {
            selectorStage = selectorStage.filterSelector(nodeSelector[cnt]);
            if (selectorStage.isAbsent()) {
                return selectorStage;
            }
        }
        return selectorStage;
    }

    eachElem(func: (item: Element, cnt?: number) => any): DomQuery {

        for (let cnt = 0, len = this.rootNode.length; cnt < len; cnt++) {
            if (func(this.rootNode[cnt], cnt) === false) {
                break;
            }
        }
        return this;
    }

    firstElem(func: (item: Element, cnt?: number) => any = item => item): DomQuery {
        if (this.rootNode.length > 1) {
            func(this.rootNode[0], 0);
        }
        return this;
    }

    lastElem(func: (item: Element, cnt?: number) => any = item => item): DomQuery {
        if (this.rootNode.length > 1) {
            func(this.rootNode[this.rootNode.length - 1], 0);
        }
        return this;
    }

    each(func: (item: DomQuery, cnt?: number) => any): DomQuery {
        Stream.of(...this.rootNode)
            .each((item, cnt) => {
                //we could use a filter, but for the best performance we dont
                if (item == null) {
                    return;
                }
                return func(DomQuery.byId(item), cnt);
            });

        return this;
    }

    /**
     * returns a new dom query containing only the first element max
     *
     * @param func a an optional callback function to perform an operation on the first element
     */
    first(func: (item: DomQuery, cnt?: number) => any = (item) => item): DomQuery {
        if (this.rootNode.length >= 1) {
            func(this.get(0), 0);
            return this.get(0);
        }
        return this;
    }

    /**
     * returns a new dom query containing only the first element max
     *
     * @param func a an optional callback function to perform an operation on the first element
     */
    last(func: (item: DomQuery, cnt?: number) => any = (item) => item): DomQuery {
        if (this.rootNode.length >= 1) {
            let lastNode = this.get(this.rootNode.length - 1);
            func(lastNode, 0);
            return lastNode;
        }
        return this;
    }

    /**
     * filter function which filters a subset
     *
     * @param func
     */
    filter(func: (item: DomQuery) => boolean): DomQuery {
        let reArr: Array<DomQuery> = [];
        this.each((item: DomQuery) => {
            func(item) ? reArr.push(item) : null;
        });
        return new DomQuery(...<any>reArr);
    }

    //TODO append prepend

    /**
     * global eval head appendix method
     * no other methods are supported anymore
     * @param code the code to be evaled
     * @param  nonce optional  nonce key for higher security
     */
    globalEval(code: string, nonce ?: string): DomQuery {
        let head = document.getElementsByTagName("head")[0] || document.documentElement;
        let script = document.createElement("script");
        if (nonce) {
            if('undefined' != typeof script?.nonce) {
                script.nonce = nonce;
            } else {
                script.setAttribute("nonce", nonce);
            }
        }
        script.type = "text/javascript";
        script.innerHTML = code;
        let newScriptElement = head.appendChild(script);
        head.removeChild(newScriptElement);
        return this;
    }

    /**
     * detaches a set of nodes from their parent elements
     * in a browser independend manner
     * @return {Array} an array of nodes with the detached dom nodes
     */
    detach(): DomQuery {
        this.eachElem((item: Element) => {
            item.parentNode.removeChild(item);
        });
        return this;
    }

    /**
     * appends the current set of elements
     * to the element or first element passed via elem
     * @param elem
     */
    appendTo(elem: DomQuery | string): DomQuery {
        if(Lang.isString(elem)) {
            this.appendTo(DomQuery.querySelectorAll(elem as string));
            return this;
        }
        this.eachElem((item) => {
            let value1: Element = <Element>(elem as DomQuery).getAsElem(0).orElseLazy(() => {
                return {
                    appendChild: () => {
                    }
                }
            }).value;
            value1.appendChild(item);
        });
        return this;
    }

    /**
     * loads and evals a script from a source uri
     *
     * @param src the source to be loaded and evaled
     * @param defer in miliseconds execution default (0 == no defer)
     * @param charSet
     */
    loadScriptEval(src: string, defer: number = 0, charSet: string = "utf-8", nonce?:string) {
        let xhr = new XMLHttpRequest();
        xhr.open("GET", src, false);

        if (charSet) {
            xhr.setRequestHeader("Content-Type", "application/x-javascript; charset:" + charSet);
        }

        xhr.onload = () => {
            //defer also means we have to process after the ajax response
            //has been processed
            //we can achieve that with a small timeout, the timeout
            //triggers after the processing is done!
            if (!defer) {
                this.globalEval(xhr.responseText.replace(/\n/g, "\r\n") + "\r\n//@ sourceURL=" + src, nonce);
            } else {
                //TODO not ideal we maybe ought to move to something else here
                //but since it is not in use yet, it is ok
                setTimeout(() => {
                    this.globalEval(xhr.responseText + "\r\n//@ sourceURL=" + src, nonce);
                }, defer);
            }
        };

        xhr.onerror = (data: any) => {
            throw Error(data);
        };
        //since we are synchronous we do it after not with onReadyStateChange
        xhr.send(null);

        return this;
    }

    insertAfter(...toInsertParams: Array<DomQuery>): DomQuery {

        this.each(existingItem => {
            let existingElement = existingItem.getAsElem(0).value;
            let rootNode = existingElement.parentNode;
            for (let cnt = 0; cnt < toInsertParams.length; cnt++) {
                let nextSibling: Element = <any>existingElement.nextSibling;
                toInsertParams[cnt].eachElem(insertElem => {
                    if (nextSibling) {
                        rootNode.insertBefore(insertElem, nextSibling);
                        existingElement = nextSibling;
                    } else {
                        rootNode.appendChild(insertElem);
                    }
                });

            }
        });

        let res = [];
        res.push(this);
        res = res.concat(toInsertParams);
        return new DomQuery(...res);
    }

    insertBefore(...toInsertParams: Array<DomQuery>): DomQuery {
        this.each(existingItem => {
            let existingElement = existingItem.getAsElem(0).value;
            let rootNode = existingElement.parentNode;
            for (let cnt = 0; cnt < toInsertParams.length; cnt++) {
                toInsertParams[cnt].eachElem(insertElem => {
                    rootNode.insertBefore(insertElem, existingElement);
                });
            }
        });
        let res = [];
        res.push(this);
        res = res.concat(toInsertParams);
        return new DomQuery(...res);
    }

    orElse(...elseValue: any): DomQuery {
        if (this.isPresent()) {
            return this;
        } else {
            return new DomQuery(...elseValue);
        }
    }

    orElseLazy(func: () => any): DomQuery {
        if (this.isPresent()) {
            return this;
        } else {
            return new DomQuery(func());
        }
    }

    parents(tagName: string): DomQuery {
        const retSet: Set<Element> = new Set();
        const retArr: Array<Element> = [];
        const lowerTagName = tagName.toLowerCase();

        let resolveItem = (item: Element) => {
            if ((item.tagName || "").toLowerCase() == lowerTagName && !retSet.has(item)) {
                retSet.add(item);
                retArr.push(item);
            }
        };

        this.eachElem((item: Element) => {
            while (item.parentNode || (<any> item).host) {
                item = <Element>item?.parentNode ?? (<any>item)?.host;

                resolveItem(item);
                //nested forms not possible, performance shortcut
                if (tagName == "form" && retArr.length) {
                    return false;
                }
            }
        });

        return new DomQuery(...retArr);
    }

    copyAttrs(sourceItem: DomQuery | XMLQuery): DomQuery {
        sourceItem.eachElem((sourceNode: Element) => {
            let attrs: Array<Attr> = objToArray(sourceNode.attributes);
            for (let item of attrs) {
                let value: string = item.value;
                let name: string = item.name;

                switch (name) {
                    case "id":
                        this.id.value = value;
                        break;
                    case "disabled":
                        this.resolveAttributeHolder("disabled").disabled = value;
                        break;
                    case "checked":
                        this.resolveAttributeHolder("checked").checked = value;
                        break;
                    default:
                        this.attr(name).value = value;
                }
            }
        });
        return this;
    }

    /**
     * resolves an attribute holder compared
     * @param attrName the attribute name
     */
    private resolveAttributeHolder(attrName: string = "value"): HTMLFormElement | any {
        let ret = [];
        ret[attrName] = null;
        return (attrName in this.getAsElem(0).value) ?
            this.getAsElem(0).value :
            ret;
    }

    /**
     * outerhtml convenience method
     * browsers only support innerHTML but
     * for instance for your jsf.js we have a full
     * replace pattern which needs outerHTML processing
     *
     * @param markup the markup which should replace the root element
     * @param runEmbeddedScripts if true the embedded scripts are executed
     * @param runEmbeddedCss if true the embeddec css are executed
     * @param deep should this also work for shadow dom (run scripts etc...)
     */
    outerHTML(markup: string, runEmbeddedScripts ?: boolean, runEmbeddedCss ?: boolean, deep = false): DomQuery {
        if (this.isAbsent()) {
            return;
        }

        let focusElementId = document?.activeElement?.id;
        let caretPosition = (focusElementId) ? DomQuery.getCaretPosition(document.activeElement) : null;
        let nodes = DomQuery.fromMarkup(markup);
        let res = [];
        let toReplace = this.getAsElem(0).value;
        let firstInsert = nodes.get(0);
        let parentNode = toReplace.parentNode;
        let replaced = firstInsert.getAsElem(0).value;
        parentNode.replaceChild(replaced, toReplace);
        res.push(new DomQuery(replaced));
        //no replacement possible
        if (this.isAbsent()) {
            return this;
        }

        let insertAdditionalItems = [];

        if (nodes.length > 1) {
            insertAdditionalItems = insertAdditionalItems.concat(...nodes.values.slice(1));
            res.push(DomQuery.byId(replaced).insertAfter(new DomQuery(...insertAdditionalItems)));
        }

        if (runEmbeddedScripts) {
            this.runScripts();
        }
        if (runEmbeddedCss) {
            this.runCss();
        }

        let focusElement = DomQuery.byId(focusElementId);
        if (focusElementId && focusElement.isPresent() &&
            caretPosition != null && "undefined" != typeof caretPosition) {
            focusElement.eachElem(item => DomQuery.setCaretPosition(item, caretPosition));
        }

        return nodes;
    }

    /**
     * Run through the given nodes in the DomQuery execute the inline scripts
     * @param whilteListed: optional whitelist function which can filter out script tags which are not processed
     * defaults to the standard jsf.js exclusion (we use this code for myfaces)
     */
    runScripts(whilteListed: (val: string) => boolean = DEFAULT_WHITELIST): DomQuery {
        const evalCollectedScripts = (finalScripts: {evalText: string, nonce: string}[]) => {
            if (finalScripts.length) {
                //script source means we have to eval the existing
                //scripts before running the include
                //this.globalEval(finalScripts.join("\n"));
                let joinedScripts = [];
                Stream.of(...finalScripts).each(item => {
                    if (item.nonce == 'evalText') {
                        joinedScripts.push(item.evalText)
                    } else {
                        if (joinedScripts.length) {
                            this.globalEval(joinedScripts.join("\n"));
                            joinedScripts.length = 0;
                        }
                        this.globalEval(item.evalText, item.nonce);
                    }
                });
                if (joinedScripts.length) {
                    this.globalEval(joinedScripts.join("\n"));
                    joinedScripts.length = 0;
                }

                finalScripts = [];
            }
        }

        let finalScripts = [],
            equi = equalsIgnoreCase,
            execScrpt = (item) => {
                let tagName = item.tagName;
                let itemType = item.type || "";
                if (tagName && equi(tagName, "script") &&
                    (itemType === "" || equi(itemType, "text/javascript") ||
                        equi(itemType, "javascript") ||
                        equi(itemType, "text/ecmascript") ||
                        equi(itemType, "ecmascript"))) {
                    let src = item.getAttribute('src');
                    if ('undefined' != typeof src
                        && null != src
                        && src.length > 0
                    ) {
                        let nonce =  item?.nonce ?? item.getAttribute('nonce').value;
                        //we have to move this into an inner if because chrome otherwise chokes
                        //due to changing the and order instead of relying on left to right
                        //if jsf.js is already registered we do not replace it anymore
                        if (whilteListed(src)) {
                            evalCollectedScripts(finalScripts);
                            nonce != '' ? this.loadScriptEval(src, 0, "UTF-8", nonce):
                                //if no nonce is set we do not pass any once
                                this.loadScriptEval(src, 0, "UTF-8");
                        }

                    } else {
                        // embedded script auto eval
                        //TODO this probably needs to be changed due to our new parsing structures
                        //probably not needed anymore
                        let evalText = trim(item.text || item.innerText || item.innerHTML);
                        let go = true;

                        while (go) {
                            go = false;
                            if (evalText.substring(0, 4) == "<!--") {
                                evalText = evalText.substring(4);
                                go = true;
                            }
                            if (evalText.substring(0, 4) == "//<!--") {
                                evalText = evalText.substring(6);
                                go = true;
                            }
                            if (evalText.substring(0, 11) == "//<![CDATA[") {
                                evalText = evalText.substring(11);
                                go = true;
                            }
                        }
                        let nonce =  item?.nonce ?? item.getAttribute('nonce').value ?? '';
                        // we have to run the script under a global context
                        //we store the script for less calls to eval
                        finalScripts.push({
                            nonce,
                            evalText
                        });
                    }
                }
            };
        try {
            let scriptElements = new DomQuery(this.filterSelector("script"), this.querySelectorAll("script"));
            //script execution order by relative pos in their dom tree
            scriptElements.stream
                .flatMap(item => Stream.of(item.values))
                .sort((node1, node2) => node1.compareDocumentPosition(node2) - 3) //preceding 2, following == 4)
                .each(item => execScrpt(item));

             evalCollectedScripts(finalScripts);
        } catch (e) {
            if (console && console.error) {
                //not sure if we
                //should use our standard
                //error mechanisms here
                //because in the head appendix
                //method only a console
                //error would be raised as well
                console.error(e.message || e.description);
            }
        } finally {
            //the usual ie6 fix code
            //the IE6 garbage collector is broken
            //nulling closures helps somewhat to reduce
            //mem leaks, which are impossible to avoid
            //at this browser
            execScrpt = null;
        }
        return this;
    }

    runCss(): DomQuery {

        const applyStyle = (item: Element, style: string) => {
                let newSS: HTMLStyleElement = document.createElement("style");
                document.getElementsByTagName("head")[0].appendChild(newSS);

                let styleSheet = newSS.sheet ?? (<any>newSS).styleSheet;

                newSS.setAttribute("rel", item.getAttribute("rel") ?? "stylesheet");
                newSS.setAttribute("type", item.getAttribute("type") ?? "text/css");

                if (styleSheet?.cssText ?? false) {
                    styleSheet.cssText = style;
                } else {
                    newSS.appendChild(document.createTextNode(style));
                }
            },

            execCss = (item: Element) => {
                const tagName = item.tagName;
                if (tagName && equalsIgnoreCase(tagName, "link") && equalsIgnoreCase(item.getAttribute("type"), "text/css")) {
                    applyStyle(item, "@import url('" + item.getAttribute("href") + "');");
                } else if (tagName && equalsIgnoreCase(tagName, "style") && equalsIgnoreCase(item.getAttribute("type"), "text/css")) {
                    let innerText = [];
                    //compliant browsers know child nodes
                    let childNodes: Array<Node> = Array.prototype.slice.call(item.childNodes);
                    if (childNodes) {
                        childNodes.forEach(child => innerText.push((<Element>child).innerHTML || (<CharacterData>child).data));
                        //non compliant ones innerHTML
                    } else if (item.innerHTML) {
                        innerText.push(item.innerHTML);
                    }

                    applyStyle(item, innerText.join(""));
                }
            };

        const scriptElements: DomQuery = new DomQuery(this.filterSelector("link, style"), this.querySelectorAll("link, style"));

        scriptElements.stream
            .flatMap(item => Stream.of(item.values))
            .sort((node1, node2) => node1.compareDocumentPosition(node2) - 3)
            .each(item => execCss(item));

        return this;
    }

    /**
     * fires a click event on the underlying dom elements
     */
    click(): DomQuery {
        this.fireEvent("click");
        return this;
    }

    addEventListener(type: string, listener: (evt: Event) => void, options?: boolean | EventListenerOptions): DomQuery {
        this.eachElem((node: Element) => node.addEventListener(type, listener, options));
        return this;
    }

    removeEventListener(type: string, listener: (evt: Event) => void, options?: boolean | EventListenerOptions): DomQuery {
        this.eachElem((node: Element) => node.removeEventListener(type, listener, options));
        return this;
    }

    /**
     * fires an event
     */
    fireEvent(eventName: string) {
        this.eachElem((node: Element) => {
            let doc;
            if (node.ownerDocument) {
                doc = node.ownerDocument;
            } else if (node.nodeType == 9) {
                // the node may be the document itself, nodeType 9 = DOCUMENT_NODE
                doc = node;
            } else {
                throw new Error("Invalid node passed to fireEvent: " + node.id);
            }

            if (node.dispatchEvent) {
                // Gecko-style approach (now the standard) takes more work
                let eventClass = "";

                // Different events have different event classes.
                // If this switch statement can't map an eventName to an eventClass,
                // the event firing is going to fail.
                switch (eventName) {
                    case "click": // Dispatching of 'click' appears to not work correctly in Safari. Use 'mousedown' or 'mouseup' instead.
                    case "mousedown":
                    case "mouseup":
                        eventClass = "MouseEvents";
                        break;

                    case "focus":
                    case "change":
                    case "blur":
                    case "select":
                        eventClass = "HTMLEvents";
                        break;

                    default:
                        throw "fireEvent: Couldn't find an event class for event '" + eventName + "'.";
                }
                let event = doc.createEvent(eventClass);
                event.initEvent(eventName, true, true); // All events created as bubbling and cancelable.

                event.synthetic = true; // allow detection of synthetic events
                // The second parameter says go ahead with the default action
                node.dispatchEvent(event);
            } else if ((<any>node).fireEvent) {
                // IE-old school style, you can drop this if you don't need to support IE8 and lower
                let event = doc.createEventObject();
                event.synthetic = true; // allow detection of synthetic events
                (<any>node).fireEvent("on" + eventName, event);
            }
        })
    }

    textContent(joinstr: string = ""): string {
        return this.stream
            .map((value: DomQuery) => {
                let item = value.getAsElem(0).orElseLazy(() => {
                    return <any>{
                        textContent: ""
                    };
                }).value;
                return (<any>item).textContent || "";
            })
            .reduce((text1, text2) => text1 + joinstr + text2, "").value;
    }

    innerText(joinstr: string = ""): string {
        return this.stream
            .map((value: DomQuery) => {
                let item = value.getAsElem(0).orElseLazy(() => {
                    return <any>{
                        innerText: ""
                    };
                }).value;
                return (<any>item).innerText || "";
            })
            .reduce((text1, text2) => [text1, text2].join(joinstr), "").value;

    }

    /**
     * encodes all input elements properly into respective
     * config entries, this can be used
     * for legacy systems, for newer usecases, use the
     * HTML5 Form class which all newer browsers provide
     *
     * @param toMerge optional config which can be merged in
     * @return a copy pf
     */
    encodeFormElement(toMerge = new Config({})): Config {

        //browser behavior no element name no encoding (normal submit fails in that case)
        //https://issues.apache.org/jira/browse/MYFACES-2847
        if (this.name.isAbsent()) {
            return;
        }

        //lets keep it sideffects free
        let target = toMerge.shallowCopy;

        this.each((element: DomQuery) => {
            if (element.name.isAbsent()) {//no name, no encoding
                return;
            }
            let name = element.name.value;
            let tagName = element.tagName.orElse("__none__").value.toLowerCase();
            let elemType = element.type.orElse("__none__").value.toLowerCase();

            elemType = elemType.toLowerCase();

            // routine for all elements
            // rules:
            // - process only inputs, textareas and selects
            // - elements muest have attribute "name"
            // - elements must not be disabled
            if (((tagName == "input" || tagName == "textarea" || tagName == "select") &&
                (name != null && name != "")) && !element.disabled) {

                // routine for select elements
                // rules:
                // - if select-one and value-Attribute exist => "name=value"
                // (also if value empty => "name=")
                // - if select-one and value-Attribute don't exist =>
                // "name=DisplayValue"
                // - if select multi and multple selected => "name=value1&name=value2"
                // - if select and selectedIndex=-1 don't submit
                if (tagName == "select") {
                    // selectedIndex must be >= 0 sein to be submittet
                    let selectElem: HTMLSelectElement = <HTMLSelectElement>element.getAsElem(0).value;
                    if (selectElem.selectedIndex >= 0) {
                        let uLen = selectElem.options.length;
                        for (let u = 0; u < uLen; u++) {
                            // find all selected options
                            //let subBuf = [];
                            if (selectElem.options[u].selected) {
                                let elementOption = selectElem.options[u];
                                target.append(name).value = (elementOption.getAttribute("value") != null) ?
                                    elementOption.value : elementOption.text;
                            }
                        }
                    }
                }

                // routine for remaining elements
                // rules:
                // - don't submit no selects (processed above), buttons, reset buttons, submit buttons,
                // - submit checkboxes and radio inputs only if checked
                if (
                    (
                        tagName != Submittables.SELECT &&
                        elemType != Submittables.BUTTON &&
                        elemType != Submittables.RESET &&
                        elemType != Submittables.SUBMIT &&
                        elemType != Submittables.IMAGE
                    ) && (
                        (
                            elemType != Submittables.CHECKBOX && elemType != Submittables.RADIO) ||
                        element.checked
                    )
                ) {
                    let files: any = (<any>element.value).value?.files ?? [];
                    if (files?.length) {
                        //xhr level2
                        target.append(name).value = files[0];
                    } else {
                        target.append(name).value = element.inputValue.value;
                    }
                }

            }
        });

        return target;
    }

    get cDATAAsString(): string {
        let TYPE_CDATA_BLOCK = 4;

        let res: any = this.lazyStream.flatMap(item => {
            return item.childNodes.stream
        }).filter(item => {
            return item?.value?.value?.nodeType == TYPE_CDATA_BLOCK;
        }).reduce((reduced: Array<any>, item: DomQuery) => {
            reduced.push((<any>item?.value?.value)?.data ?? "");
            return reduced;
        }, []).value;

        // response may contain several blocks
        return res.join("");
    }

    subNodes(from: number, to?: number): DomQuery {
        if (Optional.fromNullable(to).isAbsent()) {
            to = this.length;
        }
        return new DomQuery(...this.rootNode.slice(from, Math.min(to, this.length)));
    }

    //TODO this part probably will be removed
    //because we can stream from an array stream directly into the dom query
    _limits = -1;

    limits(end: number): IStream<DomQuery> {
        this._limits = end;
        return <any>this;
    }

    //-- internally exposed methods needed for the interconnectivity
    hasNext() {
        let isLimitsReached = this._limits != -1 && this.pos >= this._limits - 1;
        let isEndOfArray = this.pos >= this.values.length - 1;
        return !(isLimitsReached ||
            isEndOfArray);
    }

    next(): DomQuery {
        if (!this.hasNext()) {
            return null;
        }
        this.pos++;
        return new DomQuery(this.values[this.pos]);
    }


    lookAhead(cnt = 1): ITERATION_STATUS | DomQuery {
        if((this.values.length - 1) < (this.pos + cnt)) {
            return ITERATION_STATUS.EO_STRM;
        }
        return new DomQuery(this.values[this.pos + cnt]);
    }



    current(): DomQuery | ITERATION_STATUS {
        if(this.pos == -1) {
            return ITERATION_STATUS.BEF_STRM;
        }
        return new DomQuery(this.values[this.pos]);
    }


    reset() {
        this.pos = -1;
    }

    attachShadow(params: { [key: string]: string } = {mode: "open"}): DomQuery {
        let shadowRoots: DomQuery[] = [];
        this.eachElem((item: Element) => {
            let shadowElement: DomQuery;
            if ((<any>item)?.attachShadow) {
                shadowElement = DomQuery.byId((<any>item).attachShadow(params));
                shadowRoots.push(shadowElement);
            } else {
                throw new Error("Shadow dom creation not supported by the browser, please use a shim, to gain this functionality");
            }
        });
        return new DomQuery(...shadowRoots);
    }

    /**
     * helper to fix a common dom problem
     * we have to wait until a certain condition is met, in most of the cases we just want to know whether an element is present in the subdome before being able to proceed
     * @param condition
     * @param options
     */
    async waitUntilDom(condition: (element: DomQuery) => boolean, options: WAIT_OPTS = { attributes: true, childList: true, subtree: true, timeout: 500, interval: 100 }): Promise<DomQuery> {
        return waitUntilDom(this, condition, options);
    }

    /**
     * returns the embedded shadow elements
     */
    get shadowElements(): DomQuery {
        let shadowElements = this.querySelectorAll("*")
            .filter(item => item.hasShadow);


        let mapped: Array<ShadowRoot> = (shadowElements.allElems() || []).map(element => element.shadowRoot);
        return new DomQuery(...mapped);
    }

    get shadowRoot(): DomQuery {
        let shadowRoots = [];
        for (let cnt = 0; cnt < this.rootNode.length; cnt++) {
            if (this.rootNode[cnt].shadowRoot) {
                shadowRoots.push(this.rootNode[cnt].shadowRoot);
            }
        }
        return new DomQuery(...shadowRoots);
    }

    get hasShadow(): boolean {
        for (let cnt = 0; cnt < this.rootNode.length; cnt++) {
            if (this.rootNode[cnt].shadowRoot) {
                return true;
            }
        }
        return false;
    }

    //from
    // http://blog.vishalon.net/index.php/javascript-getting-and-setting-caret-position-in-textarea/
    static getCaretPosition(ctrl: any) {
        let caretPos = 0;

        try {
            if ((<any>document)?.selection) {
                ctrl.focus();
                let selection = (<any>document).selection.createRange();
                //the selection now is start zero
                selection.moveStart('character', -ctrl.value.length);
                //the caretposition is the selection start
                caretPos = selection.text.length;
            }
        } catch (e) {
            //now this is ugly, but not supported input types throw errors for selectionStart
            //just in case someone dumps this code onto unsupported browsers
        }
        return caretPos;
    }

    /**
     * sets the caret position
     *
     * @param ctrl the control to set the caret position to
     * @param pos the position to set
     *
     * note if the control does not have any selectable and focusable behavior
     * calling this method does nothing (silent fail)
     *
     */
    static setCaretPosition(ctrl: any, pos: number) {
        ctrl?.focus ? ctrl?.focus() : null;
        //the selection range is our caret position

        ctrl?.setSelectiongRange ? ctrl?.setSelectiongRange(pos, pos) : null;
    }

    /**
     * Implementation of an iterator
     * to allow loops over dom query collections
     */
    [Symbol.iterator](): Iterator<DomQuery, any, undefined> {
        return {
            next: () => {
                let done = !this.hasNext();
                let val = this.next();
                return {
                    done: done,
                    value: <DomQuery>val
                }
            }
        }
    }

    /**
     * concats the elements of two Dom Queries into a single one
     * @param toAttach the elements to attach
     * @param filterDoubles filter out possible double elements (aka same markup)
     */
    concat(toAttach: DomQuery, filterDoubles = true): any {
        const ret = this.lazyStream.concat(toAttach.lazyStream).collect(new DomQueryCollector());
        //we now filter the doubles out
        if(!filterDoubles) {
            return ret;
        }
        let idx = {}; //ie11 does not support sets, we have to fake it
        return ret.lazyStream.filter(node => {
            const notFound = !(idx?.[node.value.value.outerHTML as any]);
            idx[node.value.value.outerHTML as any] = true;
            return notFound;
        }).collect(new DomQueryCollector());
    }

    append(elem: DomQuery): DomQuery {
        this.each(item => elem.appendTo(item));
        return this;
    }

    prependTo(elem: DomQuery): DomQuery {
        elem.eachElem(item => {
            item.prepend(...this.allElems());
        });
        return this;
    }

    prepend(elem: DomQuery): DomQuery {
        this.eachElem(item => {
            item.prepend(...elem.allElems());
        })
        return this;
    }


    /*[observable](): Observable<DomQuery> {
        return this.observable;
    }

    get observable(): Observable<DomQuery> {
        let observerFunc = (observer:Subscriber<DomQuery>) => {
            try {
                this.each(dqNode => {
                    observer.next(dqNode);
                });
            } catch (e) {
                observer.error(e);
            }
        };
        return new Observable(observerFunc);
    }

    get observableElem(): Observable<Element> {
        let observerFunc = (observer:Subscriber<Element>) => {
            try {
                this.eachElem(node => {
                    observer.next(node);
                });
            } catch (e) {
                observer.error(e);
            }
        };
        return new Observable(observerFunc);
    }*/

}



/**
 * Various collectors
 * which can be used in conjunction with Streams
 */

/**
 * A collector which bundles a full dom query stream into a single dom query element
 *
 * This connects basically our stream back into DomQuery
 */
export class DomQueryCollector implements ICollector<DomQuery, DomQuery> {

    data: DomQuery[] = [];

    collect(element: DomQuery) {
        this.data.push(element);
    }

    get finalValue(): DomQuery {
        return new DomQuery(...this.data);
    }
}

/**
 * abbreviation for DomQuery
 */
export const DQ = DomQuery;
export type DQ = DomQuery;
// noinspection JSUnusedGlobalSymbols
/**
 * replacement for the jquery $
 */
export const DQ$ = DomQuery.querySelectorAll;
