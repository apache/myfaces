import {IValueHolder, Optional, ValueEmbedder} from "./Monad";
import {XMLQuery} from "./XmlQuery";
import {DomQuery, ElementAttribute, Style, WAIT_OPTS} from "./DomQuery";

export interface IDomQuery {
    /**
     * reference to the systems global object
     * (globalThis, window, global, depending on the environment)
     */
    readonly global: any;
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
     * The value in case of inputs as changeable value
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
     * nonce value if our first root element
     */
    readonly nonce: ValueEmbedder<string>;

    /**
     * abbreviation for inputValue\.value to make
     * the code terser
     */
    val: string | boolean;

    /**
     * the underlying form elements as DomQuery object
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
    readonly stream: any;
    /**
     * lazy stream representation for this DomQuery
     */
    readonly lazyStream: any;
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
     * convenience for dq.id.value to make the code a little tighter
     */
    nodeId: string;

    /**
     * returns true if the elements have the tag *tagName* as tag embedded ( highest level )
     * @param tagName
     */
    isTag(tagName: string): boolean;

    /**
     * returns the nth element as DomQuery
     * from the internal elements
     * note if you try to reach a non-existing element position
     * you will get back an absent entry
     *
     * @param index the nth index
     */
    get(index: number): DomQuery;

    /**
     * returns the nth element as optional of an Element object
     * @param index the number from the index
     * @param defaults the default value if the index is overrun default Optional\.absent
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
     * this refers to the active DomQuery object
     */
    isPresent(presentRunnable ?: (elem ?: DomQuery) => void): boolean;

    /**
     * should make the code clearer
     * note if you pass a function
     * this refers to the active DomQuery object
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
     * closest, walks up the dom tree to fid the closest element to match
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
     * Checks for an existing class in the class attributes
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
     * innerHtml
     * equivalent to jQueries html
     * as setter the html is set and the
     * DomQuery is given back
     * as getter the html string is returned
     *
     * @param newInnerHTML
     */
    html(newInnerHTML?: string): DomQuery | Optional<string>;

    /**
     * dispatch event on all children
     * just a delegated dispatchEvent from the standard
     * dom working on all queried elements in the monad level
     *
     * @param evt the event to be dispatched
     */
    dispatchEvent(evt: Event): DomQuery;

    /**
     * easy node traversal, you can pass
     * a set of node selectors which are joined as direct children
     *
     * Note! The root nodes are not in the getIf, those are always the child nodes
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
     * @param code the code to be evaluated
     * @param  nonce optional  nonce key for higher security
     */
    globalEval(code: string, nonce ?: string): DomQuery;

    /**
     * Runs an eval and keeps the evaluated code in the head
     * This is a corner condition, where we want to update the head with custom
     * code and leave the code in (instead of deleting ig)
     *
     * @param code the code to be evaluated
     * @param  nonce optional  nonce key for higher security
     */
    globalEvalSticky(code: string, nonce ?: string): DomQuery;

    /**
     * detaches a set of nodes from their parent elements
     * in a browser independent manner
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
     * replace convenience function replaces the domquery elements with the
     * elements passed as parameter
     * @param toReplace the replacement elements
     * @return a reference on the replacement elements
     */
    replace(toReplace: DomQuery): DomQuery;

    /**
     * appends the passed elements to our existing queries
     * note, double appends can happen if you are not careful
     *
     * @param elem to append
     */
    prepend(elem: DomQuery): DomQuery;

    /**
     * prepend equivalent to appendTo
     *
     * @param elem the element to prepend to
     */
    prependTo(elem: DomQuery): DomQuery;

    /**
     * loads and evaluates a script from a source uri
     *
     * @param src the source to be loaded and evaluated
     * @param delay in milliseconds execution default (0 == no delay)
     * @param charSet
     */
    loadScriptEval(src: string, delay: number, charSet: string): void;

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
     * in case the DomQuery is pointing to nothing the else value is taken into consideration
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
     * find all parents in the hierarchy for which the selector matches
     * @param selector
     */
    allParents(selector: string): DomQuery;

    /**
     * first parents with a matching selector
     * @param selector
     */
    firstParent(selector: string): DomQuery;

    /**
     * all parents until the selector match stops
     * @param selector
     */
    parentsWhileMatch(selector: string): DomQuery;


    /**
     * the parent of the elements
     */
    parent(): DomQuery;

    /**
     * copy all attributes of sourceItem to this DomQuery items
     *
     * @param sourceItem the source item to copy over (can be another DomQuery or a parsed XML Query item)
     */
    copyAttrs(sourceItem: DomQuery | XMLQuery): DomQuery;

    /**
     * outerHTML convenience method
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
     * @param sticky if set to true the element must be left in the head after eval default === false
     * @param whiteListed: optional whitelist function which can filter out script tags which are not processed
     * defaults to the standard jsf.js exclusion (we use this code for myfaces)
     */
    runScripts(sticky?: boolean, whiteListed?: (val: string) => boolean): DomQuery;

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
    textContent(joinString?: string): string;

    /*
     * pushes  in optionally a new innerText, and/or returns the current innerText
     */
    innerText(joinString?: string): string;

    /**
     * encodes all input elements properly into respective
     * config entries, this can be used
     * for legacy systems, for newer use-cases, use the
     * HTML5 Form class which all newer browsers provide
     *
     * @param toMerge optional config which can be merged in
     * @return a copy pf
     */
    encodeFormElement(toMerge): {[key: string]: any};

    /**
     * fetches the sub-nodes from ... to..
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

    // observable: Observable<DomQuery>;

    // observableElem: Observable<Element>;
}