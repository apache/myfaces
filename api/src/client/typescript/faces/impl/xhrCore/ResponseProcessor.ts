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

import {Config, DomQuery, DQ, Lang, Stream, XMLQuery} from "mona-dish";
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
    SEL_SCRIPTS_STYLES,
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
    UPDATE_FORMS
} from "../core/Const";
import trim = Lang.trim;
import {ExtConfig, ExtDomquery} from "../util/ExtDomQuery";


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

        let oldHead = DQ.querySelectorAll(TAG_HEAD);

        //delete all to avoid script and style overlays
        oldHead.querySelectorAll(SEL_SCRIPTS_STYLES).delete();

        // we cannot replace new elements in the head, but we can eval the elements
        // eval means the scripts will get attached (eval script attach method)
        // but this is done by DomQuery not in this code
        this.storeForEval(shadowHead);
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

        let resultingBody = <DQ>DQ.querySelectorAll(TAG_BODY).html(shadowInnerHTML);
        let updateForms = resultingBody.querySelectorAll(TAG_FORM);

        // main difference, we cannot replace the body itself, but only its content
        // we need a separate step for post processing the incoming attributes, like classes, styles etc...
        resultingBody.copyAttrs(shadowBody);

        this.storeForPostProcessing(updateForms, resultingBody);
    }

    /**
     * Leaf Tag eval... process whatever is in the evals cdata block
     *
     * @param node the node to eval
     */
    eval(node: XMLQuery) {
        DQ.globalEval(node.cDATAAsString);
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

        // error post processing and enrichment (standard messages from keys)
        let errorData = ErrorData.fromServerError(mergedErrorData);

        // we now trigger an internally stored onError function which might be a attached to the context
        // either we haven an internal on error, or an on error has been bassed via params from the outside
        // in both cases they are attached to our contexts

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
        let result = ExtDomquery.byId(node.id.value, true).outerHTML(cdataBlock, false, false);
        let sourceForm = result?.parents(TAG_FORM).orElseLazy(() => result.byTagName(TAG_FORM, true));
        if (sourceForm) {
            this.storeForPostProcessing(sourceForm, result);
        }
    }

    /**
     * Delete handler, simply deleetes the node referenced by the xml data
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
    insertWithSubtags(node: XMLQuery) {
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
     * forms with their respective new viewstate values
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
        let updateElems = new ExtDomquery(...this.internalContext.getIf(UPDATE_ELEMS).value);
        updateElems.runCss();
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
                let nameSpace = DQ.byId(value.nameSpace, true).orElse(document.body);
                let affectedForms = nameSpace.byTagName(TAG_FORM);
                let affectedForms2 = nameSpace.filter(item => item.tagName.orElse(EMPTY_STR).value.toLowerCase() == TAG_FORM);

                this.appendViewStateToForms(new DomQuery(affectedForms, affectedForms2), value.value);
            });
    }

    /**
     * same as with view states before applies the incoming client windows as last step after the rest of the processing
     * is done.
     */
    fixClientWindow() {
        Stream.ofAssoc<StateHolder>(this.internalContext.getIf(APPLIED_CLIENT_WINDOW).orElse({}).value)
            .each((item: Array<any>) => {
                let value: StateHolder = item[1];
                let nameSpace = DQ.byId(value.nameSpace, true).orElse(document.body);
                let affectedForms = nameSpace.byTagName(TAG_FORM);
                let affectedForms2 = nameSpace.filter(item => item.tagName.orElse(EMPTY_STR).value.toLowerCase() == TAG_FORM);

                this.appendClientWindowToForms(new DomQuery(affectedForms, affectedForms2), value.value);
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
     * proper viewstate -> form assignment
     *
     * @param forms the forms to append the viewstate to
     * @param viewState the final viewstate
     */
    private appendViewStateToForms(forms: DQ, viewState: string) {
        this.assignState(forms, $nsp(SEL_VIEWSTATE_ELEM), viewState);
    }


    /**
     * proper clientwindow -> form assignment
     *
     * @param forms the forms to append the viewstate to
     * @param clientWindow the final viewstate
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
                .orElseLazy(() => ResponseProcessor.newViewStateElement(form));

            stateHolders.attr("value").value = state;
        });
    }

    /**
     * Helper to Create a new JSF ViewState Element
     *
     * @param parent, the parent node to attach the viewstate element to
     * (usually a form node)
     */
    private static newViewStateElement(parent: DQ): DQ {
        let newViewState = DQ.fromMarkup($nsp(HTML_VIEWSTATE));
        newViewState.appendTo(parent);
        return newViewState;
    }

    /**
     * Stores certain aspects of the dom for later post processing
     *
     * @param updateForms the update forms which should receive standardized internal jsf data
     * @param toBeEvaled the resulting elements which should be evaled
     */
    private storeForPostProcessing(updateForms: DQ, toBeEvaled: DQ) {
        this.storeForUpdate(updateForms);
        this.storeForEval(toBeEvaled);
    }

    /**
     * helper to store a given form for the update post processing (viewstate)
     *
     * @param updateForms the dom query object pointing to the forms which need to be updated
     */
    private storeForUpdate(updateForms: DQ) {
        this.internalContext.assign(UPDATE_FORMS).value.push(updateForms);
    }

    /**
     * same for eval (js and css)
     *
     * @param toBeEvaled
     */
    private storeForEval(toBeEvaled: DQ) {
        this.internalContext.assign(UPDATE_ELEMS).value.push(toBeEvaled);
    }

    /**
     * check whether a given XMLQuery node is an explicit viewstate node
     *
     * @param node the node to check
     * @returns true of it ii
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

}