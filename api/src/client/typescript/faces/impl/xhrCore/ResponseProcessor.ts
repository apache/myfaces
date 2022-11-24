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

import {Config, DomQueryCollector, DQ, DQ$, Lang, LazyStream, Stream, XMLQuery} from "mona-dish";
import {Implementation} from "../AjaxImpl";
import {Assertions} from "../util/Assertions";
import {IResponseProcessor} from "./IResponseProcessor";
import {ErrorData} from "./ErrorData";
import {StateHolder} from "../core/ImplTypes";
import {EventData} from "./EventData";

import {
    $nsp,
    APPLIED_CLIENT_WINDOW,
    APPLIED_VST,
    ATTR_ID,
    ATTR_NAME,
    ATTR_URL,
    ATTR_VALUE,
    EMPTY_FUNC,
    EMPTY_STR,
    ERROR_MESSAGE,
    ERROR_NAME,
    HTML_VIEWSTATE,
    ON_ERROR,
    ON_EVENT, P_CLIENT_WINDOW,
    P_PARTIAL_SOURCE,
    P_VIEWSTATE,
    RESPONSE_XML, SEL_CLIENT_WINDOW_ELEM,
    SEL_VIEWSTATE_ELEM,
    SOURCE,
    SUCCESS,
    TAG_AFTER,
    TAG_ATTR,
    TAG_BEFORE,
    TAG_BODY,
    TAG_FORM,
    TAG_HEAD,
    UPDATE_ELEMS,
    UPDATE_FORMS,
    DEFERRED_HEAD_INSERTS, PARTIAL_ID, P_EXECUTE, P_RENDER, HTML_CLIENT_WINDOW
} from "../core/Const";
import trim = Lang.trim;
import {ExtConfig, ExtDomQuery} from "../util/ExtDomQuery";


/**
 * Response processor
 *
 * Each  XML tag is either a node or a leaf
 * or both
 *
 * the processor provides a set of operations
 * which are executed on a single leaf node per operation
 * and present the core functionality of our response
 *
 * Note the response processor is stateful hence we bundle it in a class
 * to reduce code we keep references tot contexts in place
 */
export class ResponseProcessor implements IResponseProcessor {

    constructor(private request: Config, private externalContext: Config, private internalContext: Config) {
    }

    /**
     * head replacement
     * @param shadowDocument incoming shadow head data (aka cdata as xml reference or dom element)
     * the data incoming must represent the html representation of the head itself one way or the other
     */
    replaceHead(shadowDocument: XMLQuery | DQ) {
        let shadowHead = shadowDocument.querySelectorAll(TAG_HEAD);
        if (!shadowHead.isPresent()) {
            return;
        }
        let head = ExtDomQuery.querySelectorAll(TAG_HEAD);
        // full replace we delete everything
        head.childNodes.delete();
        this.addToHead(shadowHead);
    }

    addToHead(shadowHead: XMLQuery | DQ) {
        const mappedHeadData = new ExtDomQuery(shadowHead);
        const postProcessTags = ["STYLE", "LINK", "SCRIPT"];
        const nonExecutables = mappedHeadData.filter(item => postProcessTags.indexOf(item.tagName.orElse("").value) == -1);
        nonExecutables.runHeadInserts(true);

        //incoming either the outer head tag or its children
        const nodesToAdd = (shadowHead.tagName.value === "HEAD") ? shadowHead.childNodes : shadowHead;
        // this is stored for post processing
        // after the rest of the "pyhsical build up", head before body
        const evalElements = nodesToAdd.stream
            .filter(item => postProcessTags.indexOf(item.tagName.orElse("").value) != -1).collect(new DomQueryCollector());
        this.addToHeadDeferred(evalElements);
    }

    addToHeadDeferred(newElements: XMLQuery | DQ) {
        this.internalContext.assign(DEFERRED_HEAD_INSERTS).value.push(newElements);
    }

    /**
     * replaces the body in the expected manner
     * which means the entire body content is refreshed
     * however also the body attributes must be transferred
     * keeping event handlers etc... in place
     *
     * @param shadowDocument .. an incoming shadow document hosting the new nodes
     */
    replaceBody(shadowDocument: XMLQuery | DQ) {

        let shadowBody = shadowDocument.querySelectorAll(TAG_BODY);
        if (!shadowBody.isPresent()) {
            return;
        }

        let shadowInnerHTML: string = <string>shadowBody.html().value;

        let resultingBody = <DQ>ExtDomQuery.querySelectorAll(TAG_BODY).html(shadowInnerHTML);
        let updateForms = resultingBody.querySelectorAll(TAG_FORM);

        // main difference, we cannot replace the body itself, but only its content
        // we need a separate step for post-processing the incoming
        // attributes, like classes, styles etc...
        resultingBody.copyAttrs(shadowBody);

        this.storeForPostProcessing(updateForms, resultingBody);
    }

    /**
     * Leaf Tag eval... process whatever is in the eval cdata block
     *
     * @param node the node to eval
     */
    eval(node: XMLQuery) {
        ExtDomQuery.globalEval(node.cDATAAsString);
    }

    /**
     * processes an incoming error from the response
     * which is hosted under the &lt;error&gt; tag
     * @param node the node hosting the error in our response xml
     * @param node the node in the xml hosting the error message
     */
    error(node: XMLQuery) {
        /**
         * <error>
         *      <error-name>String</error-name>
         *      <error-message><![CDATA[message]]></error-message>
         * <error>
         */

        let mergedErrorData = new ExtConfig({});
        mergedErrorData.assign(SOURCE).value = this.externalContext.getIf(P_PARTIAL_SOURCE).get(0).value;
        mergedErrorData.assign(ERROR_NAME).value = node.querySelectorAll(ERROR_NAME).textContent(EMPTY_STR);
        mergedErrorData.assign(ERROR_MESSAGE).value = node.querySelectorAll(ERROR_MESSAGE).cDATAAsString;

        let hasResponseXML = this.internalContext.get(RESPONSE_XML).isPresent();

        //we now store the response xml also in the error data for further details
        mergedErrorData.assignIf(hasResponseXML, RESPONSE_XML).value = this.internalContext.getIf(RESPONSE_XML).value.get(0).value;

        // error post-processing and enrichment (standard messages from keys)
        let errorData = ErrorData.fromServerError(mergedErrorData);

        // we now trigger an internally stored onError function which might be an attached to the context
        // either we do not have an internal on error, or an on error has been based via params from the outside.
        // In both cases they are attached to our contexts

        this.triggerOnError(errorData);
        Implementation.sendError(errorData);
    }

    /**
     * process the redirect operation
     *
     * @param node
     */
    redirect(node: XMLQuery) {
        Assertions.assertUrlExists(node);

        let redirectUrl = trim(node.attr(ATTR_URL).value);
        if (redirectUrl != EMPTY_STR) {
            window.location.href = redirectUrl;
        }
    }

    /**
     * processes the update operation and updates the node with the cdata block
     * @param node the xml response node hosting the update info
     * @param cdataBlock the cdata block with the new html code
     */
    update(node: XMLQuery, cdataBlock: string) {
        let result = ExtDomQuery.byId(node.id.value, true).outerHTML(cdataBlock, false, false);
        let sourceForm = result?.firstParent(TAG_FORM).orElseLazy(() => result.byTagName(TAG_FORM, true));
        if (sourceForm) {
            this.storeForPostProcessing(sourceForm, result);
        }
    }

    /**
     * Delete handler, simply deletes the node referenced by the xml data
     * @param node
     */
    delete(node: XMLQuery) {
        DQ.byId(node.id.value, true).delete();
    }

    /**
     * attributes leaf tag... process the attributes
     *
     * @param node
     */
    attributes(node: XMLQuery) {
        let elem = DQ.byId(node.id.value, true);

        node.byTagName(TAG_ATTR).each((item: XMLQuery) => {
            elem.attr(item.attr(ATTR_NAME).value).value = item.attr(ATTR_VALUE).value;
        });
    }

    /**
     * @param shadowDocument a shadow document which is needed for further processing
     */
    replaceViewRoot(shadowDocument: XMLQuery) {
        this.replaceHead(shadowDocument);
        this.replaceBody(shadowDocument);
    }

    /**
     * Insert handling, either before or after
     *
     * @param node
     */
    insert(node: XMLQuery) {
        //let insertId = node.id; //not used atm

        let before = node.attr(TAG_BEFORE);
        let after = node.attr(TAG_AFTER);

        let insertNodes = DQ.fromMarkup(<any>node.cDATAAsString);

        if (before.isPresent()) {
            DQ.byId(before.value, true).insertBefore(insertNodes);
            this.internalContext.assign(UPDATE_ELEMS).value.push(insertNodes);
        }
        if (after.isPresent()) {
            let domQuery = DQ.byId(after.value, true);
            domQuery.insertAfter(insertNodes);

            this.internalContext.assign(UPDATE_ELEMS).value.push(insertNodes);
        }
    }

    /**
     * Handler for the case &lt;insert <&lt; before id="...
     *
     * @param node the node hosting the insert data
     */
    insertWithSubTags(node: XMLQuery) {
        let before = node.querySelectorAll(TAG_BEFORE);
        let after = node.querySelectorAll(TAG_AFTER);

        before.each(item => {
            let insertId = item.attr(ATTR_ID);
            let insertNodes = DQ.fromMarkup(<any>item.cDATAAsString);
            if (insertId.isPresent()) {
                DQ.byId(insertId.value, true).insertBefore(insertNodes);
                this.internalContext.assign(UPDATE_ELEMS).value.push(insertNodes);
            }
        });

        after.each(item => {
            let insertId = item.attr(ATTR_ID);
            let insertNodes = DQ.fromMarkup(<any>item.cDATAAsString);
            if (insertId.isPresent()) {
                DQ.byId(insertId.value, true).insertAfter(insertNodes);
                this.internalContext.assign(UPDATE_ELEMS).value.push(insertNodes);
            }
        });
    }

    /**
     * Process the viewState update, update the affected
     * forms with their respective new viewState values
     *
     */
    processViewState(node: XMLQuery): boolean {
        if (ResponseProcessor.isViewStateNode(node)) {
            let state = node.cDATAAsString;
            this.internalContext.assign(APPLIED_VST, node.id.value).value = new StateHolder($nsp(node.id.value), state);
            return true;
        }
        return false;
    }

    processClientWindow(node: XMLQuery): boolean {
        if (ResponseProcessor.isClientWindowNode(node)) {
            let state = node.cDATAAsString;
            this.internalContext.assign(APPLIED_CLIENT_WINDOW, node.id.value).value = new StateHolder($nsp(node.id.value), state);
            return true;
        }
    }

    /**
     * generic global eval which runs the embedded css and scripts
     */
    globalEval() {
        //  phase one, if we have head inserts, we build up those before going into the script eval phase
        let insertHeadElems = new ExtDomQuery(...this.internalContext.getIf(DEFERRED_HEAD_INSERTS).value);
        insertHeadElems.runHeadInserts(true);

        // phase 2 we run a script eval on all updated elements in the body
        let updateElems = new ExtDomQuery(...this.internalContext.getIf(UPDATE_ELEMS).value);
        updateElems.runCss();
        // phase 3, we do the same for the css
        updateElems.runScripts();
    }

    /**
     * Postprocessing view state fixing
     * this appends basically the incoming view states to the forms.
     * It is called from outside after all forms have been processed basically
     * as last lifecycle step, before going into the next request.
     */
    fixViewStates() {
        Stream.ofAssoc<StateHolder>(this.internalContext.getIf(APPLIED_VST).orElse({}).value)
            .each((item: Array<any>) => {
                let value: StateHolder = item[1];
                let namingContainerId = this.internalContext.getIf(PARTIAL_ID);
                let affectedForms;

                affectedForms = this.getContainerForms(namingContainerId)
                    .filter(affectedForm => this.executeOrRenderFilter(affectedForm));

                this.appendViewStateToForms(affectedForms, value.value);
            });
    }



    /**
     * same as with view states before applies the incoming client windows as last step after the rest of the processing
     * is done.
     */
    fixClientWindow() {
        Stream.ofAssoc<StateHolder>(this.internalContext.getIf(APPLIED_CLIENT_WINDOW).orElse({}).value)
            .each((item: Array<any>) => {
                const value: StateHolder = item[1];
                const namingContainerId = this.internalContext.getIf(PARTIAL_ID);
                let affectedForms;

                affectedForms = this.getContainerForms(namingContainerId)
                    .filter(affectedForm => this.executeOrRenderFilter(affectedForm));

                this.appendClientWindowToForms(affectedForms, value.value);
            });
    }

    /**
     * all processing done we can close the request and send the appropriate events
     */
    done() {
        let eventData = EventData.createFromRequest(this.request.value, this.externalContext, SUCCESS);

        //because some frameworks might decorate them over the context in the response
        let eventHandler = this.externalContext.getIf(ON_EVENT).orElseLazy(() => this.internalContext.getIf(ON_EVENT).value).orElse(EMPTY_FUNC).value;
        Implementation.sendEvent(eventData, eventHandler);
    }

    /**
     * proper viewState -> form assignment
     *
     * @param forms the forms to append the viewState to
     * @param viewState the final viewState
     */
    private appendViewStateToForms(forms: DQ, viewState: string) {
        this.assignState(forms, $nsp(SEL_VIEWSTATE_ELEM), viewState);
    }


    /**
     * proper clientWindow -> form assignment
     *
     * @param forms the forms to append the viewState to
     * @param clientWindow the final viewState
     */
    private appendClientWindowToForms(forms: DQ, clientWindow: string) {
        this.assignState(forms, $nsp(SEL_CLIENT_WINDOW_ELEM), clientWindow);
    }

    /**
     * generic append state which appends a certain state as hidden element to an existing set of forms
     *
     * @param forms the forms to append or change to
     * @param selector the selector for the state
     * @param state the state itself which needs to be assigned
     *
     * @private
     */
    private assignState(forms: DQ, selector: string, state: string) {
        forms.each((form: DQ) => {
            let stateHolders = form.querySelectorAll(selector)
                .orElseLazy(() => {
                    return selector.indexOf("ViewState") != -1 ?
                        ResponseProcessor.newViewStateElement(form):
                        ResponseProcessor.newClientWindowElement(form);
                });

            stateHolders.attr("value").value = state;
        });
    }

    /**
     * Helper to Create a new JSF ViewState Element
     *
     * @param parent, the parent node to attach the viewState element to
     * (usually a form node)
     */
    private static newViewStateElement(parent: DQ): DQ {
        let newElement = DQ.fromMarkup($nsp(HTML_VIEWSTATE));
        newElement.appendTo(parent);
        return newElement;
    }

    /**
     * Helper to Create a new JSF ViewState Element
     *
     * @param parent, the parent node to attach the viewState element to
     * (usually a form node)
     */
    private static newClientWindowElement(parent: DQ): DQ {
        let newElement = DQ.fromMarkup($nsp(HTML_CLIENT_WINDOW));
        newElement.appendTo(parent);
        return newElement;
    }

    /**
     * Stores certain aspects of the dom for later post-processing
     *
     * @param updateForms the update forms which should receive standardized internal jsf data
     * @param toBeEvaluated the resulting elements which should be evaluated
     */
    private storeForPostProcessing(updateForms: DQ, toBeEvaluated: DQ) {
        this.storeForUpdate(updateForms);
        this.storeForEval(toBeEvaluated);
    }

    /**
     * helper to store a given form for the update post-processing (viewState)
     *
     * @param updateForms the dom query object pointing to the forms which need to be updated
     */
    private storeForUpdate(updateForms: DQ) {
        this.internalContext.assign(UPDATE_FORMS).value.push(updateForms);
    }

    /**
     * same for eval (js and css)
     *
     * @param toBeEvaluated
     */
    private storeForEval(toBeEvaluated: DQ) {
        this.internalContext.assign(UPDATE_ELEMS).value.push(toBeEvaluated);
    }

    /**
     * check whether a given XMLQuery node is an explicit viewState node
     *
     * @param node the node to check
     * @returns if it is a viewState node
     */
    private static isViewStateNode(node: XMLQuery): boolean {
        let separatorChar = (window?.faces ?? window?.jsf).separatorchar;
        return "undefined" != typeof node?.id?.value && (node?.id?.value == $nsp(P_VIEWSTATE) ||
            node?.id?.value?.indexOf([separatorChar, $nsp(P_VIEWSTATE)].join(EMPTY_STR)) != -1 ||
            node?.id?.value?.indexOf([$nsp(P_VIEWSTATE), separatorChar].join(EMPTY_STR)) != -1);
    }

    /**
     * incoming client window node also needs special processing
     *
     * @param node the node to check
     * @returns true of it ii
     */
    private static isClientWindowNode(node: XMLQuery): boolean {
        let separatorChar = (window?.faces ?? window?.jsf).separatorchar;
        return "undefined" != typeof node?.id?.value && (node?.id?.value == $nsp(P_CLIENT_WINDOW) ||
            node?.id?.value?.indexOf([separatorChar, $nsp(P_CLIENT_WINDOW)].join(EMPTY_STR)) != -1 ||
            node?.id?.value?.indexOf([$nsp(P_CLIENT_WINDOW), separatorChar].join(EMPTY_STR)) != -1);
    }

    private triggerOnError(errorData: ErrorData) {
        this.externalContext.getIf(ON_ERROR).orElse(this.internalContext.getIf(ON_ERROR).value).orElse(EMPTY_FUNC).value(errorData);
    }

    /**
     * filters the forms according to being in the execute or render cycle
     * @param affectedForm
     * @private
     */
    private executeOrRenderFilter(affectedForm) {
        let executes = this.externalContext.getIf($nsp(P_EXECUTE)).orElse("@none").value.split(/\s+/gi);
        let renders = this.externalContext.getIf($nsp(P_RENDER)).orElse("@none").value.split(/\s+/gi);
        let executeAndRenders = executes.concat(...renders);
        return LazyStream.of(...executeAndRenders).filter(nameOrId => {
            if (nameOrId == "@all") {
                return true;
            }
            if (nameOrId == "@none") {
                return true;
            }

            const nameOrIdSelector = `[id='${nameOrId}'], [name='#${nameOrId}']`;
            //either the form directly is in execute or render or one of its children or one of its parents
            return affectedForm.matchesSelector(nameOrIdSelector) ||
                affectedForm.querySelectorAll(nameOrIdSelector).isPresent() ||
                affectedForm.firstParent(nameOrIdSelector).isPresent();
        }).first().isPresent();
    }

    /**
     * gets all forms under a single naming container id
     * @param namingContainerId
     * @private
     */
    private getContainerForms(namingContainerId: Config) {
        if (namingContainerId.isPresent()) {
            //naming container mode, all forms under naming container id must be processed
            return DQ.byId(namingContainerId.value).orElse(DQ$(`form[name='${namingContainerId.value}']`)).byTagName(TAG_FORM, true);
        } else {
            return DQ.byTagName(TAG_FORM);
        }
    }
}