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

import {DQ, XMLQuery} from "mona-dish";
import {ResponseProcessor} from "./ResponseProcessor";

import {IResponseProcessor} from "./IResponseProcessor";
import {
    $nsp,
    XML_TAG_ATTRIBUTES,
    XML_TAG_CHANGES,
    XML_TAG_DELETE,
    XML_TAG_ERROR,
    XML_TAG_EVAL,
    XML_TAG_EXTENSION,
    XML_TAG_INSERT,
    XML_TAG_REDIRECT,
    XML_TAG_UPDATE, P_RESOURCE,
    P_VIEWBODY,
    P_VIEWHEAD,
    P_VIEWROOT,
    NAMING_CONTAINER_ID,
    XML_TAG_PARTIAL_RESP,
    RESPONSE_XML,
    XML_TAG_AFTER,
    XML_TAG_BEFORE, NAMED_VIEWROOT, XML_ATTR_NAMED_VIEWROOT, P_VIEWSTATE, $faces
} from "../core/Const";
import {resolveContexts, resolveResponseXML} from "./ResponseDataResolver";
import {ExtConfig} from "../util/ExtDomQuery";



export module Response {


    /**
     * Standardized faces.ts response
     * this one is called straight from faces.ts.response
     *
     * The processing follows the spec by going for the responseXML
     * and processing its tags
     *
     * @param {XMLHttpRequest} request (xhrRequest) - xhr request object
     * @param context {Context} context (Map) - AJAX context
     *
     */
    export function processResponse(request: XMLHttpRequest, context: Context) {

        let req = ExtConfig.fromNullable(request);
        let {externalContext, internalContext} = resolveContexts(context);
        let responseXML: XMLQuery = resolveResponseXML(req);
        let responseProcessor = new ResponseProcessor(req, externalContext, internalContext);

        internalContext.assign(RESPONSE_XML).value = responseXML;

        // we now process the partial tags, or in none given raise an error
        responseXML.querySelectorAll(XML_TAG_PARTIAL_RESP)
            .each(item => processPartialTag(<XMLQuery>item, responseProcessor, internalContext));

        // We now process the viewStates, client windows and the elements to be evaluated are delayed.
        // The reason for this is that often it is better
        // to wait until the document has caught up before
        // doing any evaluations even on embedded scripts.
        // Usually this does not matter, the client window comes in almost last always anyway
        // we maybe drop this deferred assignment in the future, but myfaces did it until now.
        responseProcessor.updateNamedViewRootState();
        responseProcessor.fixViewStates();
        responseProcessor.fixClientWindow();
        responseProcessor.globalEval();

        responseProcessor.done();
    }

    /**
     * highest node partial-response from there the main operations are triggered
     */
    function processPartialTag(node: XMLQuery, responseProcessor: IResponseProcessor, internalContext) {


        /*
        https://javaee.github.io/javaserverfaces/docs/2.2/javadocs/web-partialresponse.html#ns_xsd
        The "partial-response" element is the root of the partial response information hierarchy,
        and contains nested elements for all possible elements that can exist in the response.
        This element must have an "id" attribute whose value is the return from calling getContainerClientId()
        on the UIViewRoot to which this response pertains.
         */
        // we can determine whether we are in a naming container scenario by checking whether the passed view id is present in the page
        // under or in body as identifier

        let partialId:string = node?.id?.value;
        internalContext.assignIf(!!partialId, NAMING_CONTAINER_ID).value = partialId; // second case mojarra

        // there must be at least one container viewstate element resembling the viewroot that we know
        // this is named
        responseProcessor.updateNamedViewRootState();

        const SEL_SUB_TAGS = [XML_TAG_ERROR, XML_TAG_REDIRECT, XML_TAG_CHANGES].join(",");

        // now we can process the main operations
        node.querySelectorAll(SEL_SUB_TAGS).each((node: XMLQuery) => {
            switch (node.tagName.value) {
                case XML_TAG_ERROR:
                    responseProcessor.error(node);
                    break;
                case XML_TAG_REDIRECT:
                    responseProcessor.redirect(node);
                    break;
                case XML_TAG_CHANGES:
                    processChangesTag(node, responseProcessor);
                    break;
            }
        });
    }

    let processInsert = function (responseProcessor: IResponseProcessor, node: XMLQuery) {
        // path1 insert after as child tags
        if(node.querySelectorAll([XML_TAG_BEFORE, XML_TAG_AFTER].join(",")).length) {
            responseProcessor.insertWithSubTags(node);
        } else { // insert before after with id
            responseProcessor.insert(node);
        }

    };

    /**
     * next level changes tag
     *
     * @param node
     * @param responseProcessor
     */
    function processChangesTag(node: XMLQuery, responseProcessor: IResponseProcessor): boolean {
        const ALLOWED_TAGS = [XML_TAG_UPDATE, XML_TAG_EVAL, XML_TAG_INSERT, XML_TAG_DELETE, XML_TAG_ATTRIBUTES, XML_TAG_EXTENSION].join(", ");
        node.querySelectorAll(ALLOWED_TAGS).each(
            (node: XMLQuery) => {
                switch (node.tagName.value) {
                    case XML_TAG_UPDATE:
                        processUpdateTag(node, responseProcessor);
                        break;

                    case XML_TAG_EVAL:
                        responseProcessor.eval(node);
                        break;

                    case XML_TAG_INSERT:
                        processInsert(responseProcessor, node);
                        break;

                    case XML_TAG_DELETE:
                        responseProcessor.delete(node);
                        break;

                    case XML_TAG_ATTRIBUTES:
                        responseProcessor.attributes(node);
                        break;

                    case XML_TAG_EXTENSION:
                        break;
                }
            }
        );
        return true;
    }

    /**
     * checks and stores a state update for delayed processing
     *
     * @param responseProcessor the response processor to perform the store operation
     * @param node the xml node to check for the state
     *
     * @private
     */
    function storeState(responseProcessor: IResponseProcessor, node: XMLQuery) {
        return responseProcessor.processViewState(node) || responseProcessor.processClientWindow(node);
    }

    /**
     * branch tag update. drill further down into the updates
     * special case viewState in that case it is a leaf
     * and the viewState must be processed
     *
     * @param node
     * @param responseProcessor
     */
    function processUpdateTag(node: XMLQuery, responseProcessor: IResponseProcessor) {
        // early state storing, if no state we perform a normal update cycle
        if (!storeState(responseProcessor, node)) {
            handleElementUpdate(node, responseProcessor);
        }
    }

    /**
     * element update
     *
     * @param node
     * @param responseProcessor
     */
    function handleElementUpdate(node: XMLQuery, responseProcessor: IResponseProcessor) {
        let cdataBlock = node.cDATAAsString;
        switch (node.id.value) {
            case $nsp(P_VIEWROOT) :
                responseProcessor.replaceViewRoot(DQ.fromMarkup(cdataBlock.substring(cdataBlock.indexOf("<html"))));
                break;

            case $nsp(P_VIEWHEAD):
                responseProcessor.replaceHead(DQ.fromMarkup(cdataBlock));
                break;

            case $nsp(P_VIEWBODY):
                responseProcessor.replaceBody(DQ.fromMarkup(cdataBlock));
                break;

            case $nsp(P_RESOURCE):
                responseProcessor.addToHead(DQ.fromMarkup(cdataBlock))
                break;

            default:// htmlItem replacement
                responseProcessor.update(node, cdataBlock);
                break;
        }
    }
}