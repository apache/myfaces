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
import {Config, DQ} from "mona-dish";
import {$nsp, EMPTY_STR, IDENT_NONE, P_VIEWSTATE} from "../core/Const";

import {
    encodeFormData,
    fixEmptyParameters, getFormInputsAsArr
} from "../util/FileUtils";
import {ExtLang} from "../util/Lang";
const ofAssoc = ExtLang.ofAssoc;
import {Es2019Array} from "mona-dish";


type ParamsMapper = (key: string, value: any) => [string, any];
const defaultParamsMapper: ParamsMapper = (key, item) => [key, item];

/**
 * A unified form data class
 * which builds upon our configuration.
 *
 * We cannot use standard html5 forms everywhere
 * due to api constraints on the HTML Form object in IE11
 * and due to the url encoding constraint given by the faces.js spec
 *
 *
 * internal storage format
 * every value is stored as an array
 * even scalar ones!
 */
export class XhrFormData extends Config {
    /**
     * Checks if the given datasource is a multipart request source
     * multipart is only needed if one of the executes is a file input
     * since file inputs are stateless, they fall out of the view state
     * and need special handling. With file submits we have to send a formData object
     * instead of an encoded string files cannot be sent that way
     */
    isMultipartRequest: boolean = false;

    /**
     * data collector from a given form
     *
     * @param dataSource either a form as DomQuery object or an encoded url string
     * @param paramsMapper a remapper for the params keys and values
     * @param executes the executes id list for the elements to being processed
     * @param partialIds partial ids to collect, to reduce the data sent down
     */
    constructor(dataSource: DQ, private paramsMapper: ParamsMapper = defaultParamsMapper, executes?: string[], partialIds?: string[]) {
        super({});
        /*
         * Spec. 13.3.1 — order matters: detect multipart first, encode fields second,
         * apply view state last (so it is not double-counted if already present in fields).
         */
        this.initFormData(dataSource, executes, partialIds);
    }

    /**
     * @returns a Form data representation, this is needed for file submits
     */
    toFormData(): FormData {
        return this.buildFormData();
    }

    /**
     * returns an encoded string representation of our xhr form data
     *
     * @param defaultStr optional default value if nothing is there to encode
     */
    toString(defaultStr = EMPTY_STR): string {
        return encodeFormData(this, this.paramsMapper, defaultStr);
    }

    /**
     * Drives the three-phase form data initialisation in the required order.
     */
    private initFormData(dataSource: DQ, executes?: string[], partialIds?: string[]): void {
        this.detectMultipartRequest(dataSource, executes);
        this.encodeSubmittableFields(dataSource, partialIds);
        this.applyViewState(dataSource);
    }

    /**
     * Sets isMultipartRequest when any of the executed elements is a file input.
     */
    private detectMultipartRequest(rootElement: DQ, executes?: Array<string>): void {
        if (!executes || executes.includes(IDENT_NONE)) {
            return;
        }
        this.isMultipartRequest = rootElement.isMultipartCandidate(true);
    }

    /**
     * special case view state handling
     *
     * @param form the form holding the view state value
     */
    private applyViewState(form: DQ): void {
        if (this.getIf($nsp(P_VIEWSTATE)).isPresent()) {
            return;
        }
        const viewStateElement = form.querySelectorAllDeep(`[name*='${$nsp(P_VIEWSTATE)}'`);
        const viewState = viewStateElement.inputValue;
        this.appendIf(viewState.isPresent(), this.remapKeyForNamingContainer(viewStateElement.name.value)).value = viewState.value;
    }

    /**
     * determines fields to submit
     * @param {Node} parentItem - form element item is nested in
     * @param {Array} partialIds - ids fo PPS
     */
    private encodeSubmittableFields(parentItem: DQ, partialIds: string[] = []): void {
        const mergeIntoThis = ([key, value]: [string, any]) => this.append(key).value = value;
        const namingContainerRemap = ([key, value]: [string, any]) => this.paramsMapper(key as string, value);

        const remappedPartialIds = partialIds.map(id => this.remapKeyForNamingContainer(id));

        getFormInputsAsArr(parentItem)
            .map(fixEmptyParameters)
            .map(namingContainerRemap)
            .filter(([key]) => this.isFieldIncluded(key, remappedPartialIds))
            .forEach(mergeIntoThis);
    }

    /**
     * Returns true when the field should be included in the submission.
     * Special "@"-prefixed keys (internal markers) are always included.
     * When no partial ids are specified, everything passes through.
     */
    private isFieldIncluded(key: string, remappedPartialIds: string[]): boolean {
        if (!remappedPartialIds.length || key.startsWith("@")) {
            return true;
        }
        return remappedPartialIds.includes(key);
    }

    /**
     * Builds the FormData object from the internal key→value[] map.
     * Arrays are expanded so each value becomes a separate FormData entry.
     */
    private buildFormData(): FormData {
        const expandValueArrays = ([key, item]: [string, any]) => {
            if (Array.isArray(item)) {
                return new Es2019Array(...item).map((value: any) => ({key, value}));
            }
            return [{key, value: item}];
        };

        const remapForNamingContainer = ({key, value}: {key: string, value: any}) => ({
            key: this.remapKeyForNamingContainer(key),
            value
        });

        return ofAssoc(this.value)
            .flatMap(expandValueArrays)
            .map(remapForNamingContainer)
            .reduce((formData: FormData, {key, value}: any) => {
                formData.append(key, value);
                return formData;
            }, new FormData()) as FormData;
    }

    /**
     * Applies paramsMapper to remap only the key, ignoring the value transformation.
     * EMPTY_STR is passed as a placeholder since only the remapped key is used.
     */
    private remapKeyForNamingContainer(key: string): string {
        const [remappedKey] = this.paramsMapper(key, EMPTY_STR); // value is a required arg but irrelevant here; only the remapped key is used
        return remappedKey;
    }
}