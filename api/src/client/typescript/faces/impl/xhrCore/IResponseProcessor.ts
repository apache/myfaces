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
import {XMLQuery} from "mona-dish";
// noinspection TypeScriptPreferShortImport
import {DQ} from "mona-dish";

/**
 * response processor interface
 * We expose an interface
 * to allow a clear contract for future
 * testing and implementation extension points
 */
export interface IResponseProcessor {

    /**
     * replace the head element
     *
     * @param shadowHead
     */
    replaceHead(shadowHead: XMLQuery | DQ): void;

    /**
     * adds new elements to the head
     *
     * @param newElements the elements which need addition
     */
    addToHead(newElements: XMLQuery | DQ): void;


    /**
     * adds new elements to the head in a deferred manner
     *
     * @param newElements the elements which need addition
     */
    addToHeadDeferred(newElements: XMLQuery | DQ): void;

    /**
     * replace the body
     *
     * @param shadowBody
     */
    replaceBody(shadowBody: XMLQuery | DQ): void;

    /**
     * Leaf Tag eval... process whatever is in the evaluated cdata block
     *
     * @param node
     */
    eval(node: XMLQuery): void;

    /**
     * processes an incoming error from the response
     * which is hosted under the &lt;error&gt; tag
     * @param node the node in the xml hosting the error message
     */
    error(node: XMLQuery): void;

    /**
     * process the redirect operation
     *
     * @param node
     */
    redirect(node: XMLQuery): void;

    /**
     * processes the update operation and updates the node with the cdata block
     * @param node
     * @param cdataBlock
     */
    update(node: XMLQuery, cdataBlock: string): void;

    /**
     * delete operation, deletes the data from
     * node from the dom
     *
     * @param node
     */
    delete(node: XMLQuery): void;

    /**
     * attributes leaf tag... process the attributes
     *
     * @param node
     */
    attributes(node: XMLQuery): void;

    /**
     * replace the entire viewRoot
     * with shadowResponse
     * @param shadowResponse
     */
    replaceViewRoot(shadowResponse: XMLQuery | DQ): void;

    /**
     * jsf insert resolution
     * which then has to handle the before and after situation
     *
     * @param node
     */
    insert(node: XMLQuery): void;

    /**
     * insert with before, after sub-tags
     * @param node
     */
    insertWithSubTags(node: XMLQuery);

    /**
     * process the viewState update, update the affected
     * forms with their respective new viewState values
     *
     */
    processViewState(node: XMLQuery): boolean;

    /**
     * process the viewState update, update the affected
     * forms with their respective new viewState values
     *
     */
    processClientWindow(node: XMLQuery): boolean;


    /**
     * Eval - all processed elements so far
     * and executes the embedded scripts
     */
    globalEval(): void;

    /**
     * fix the viewStates of all processed forms
     */
    fixViewStates(): void;

    /**
     * processing done
     * send last event
     */
    done(): void;
}