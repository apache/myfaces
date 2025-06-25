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
import ofAssoc = ExtLang.ofAssoc;
import {Es2019Array} from "mona-dish";


type ParamsMapper<V, K> = (key: V, item: K) => [V, K];
const defaultParamsMapper: ParamsMapper<string, any> = (key, item) => [key, item];

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
    constructor(private dataSource: DQ, private paramsMapper: ParamsMapper<string, any> = defaultParamsMapper, executes?: string[], private partialIds?: string[]) {
        super({});
        //encode and append the issuing item if not a partial ids array of ids is passed
        /*
         * Spec. 13.3.1
         * Collect and encode input elements.
         * Additionally the hidden element jakarta.faces.ViewState
         * Enhancement partial page submit
         */
        this.resolveRequestType(this.dataSource, executes);
        this.encodeSubmittableFields(this.dataSource, this.partialIds);
        this.applyViewState(this.dataSource);
    }

    /**
     * @returns a Form data representation, this is needed for file submits
     */
    toFormData(): FormData {
        /*
           * expands key: [item1, item2]
           * to: [{key: key,  value: item1}, {key: key, value: item2}]
           */
        let expandValueArrays = ([key, item]) => {
            if (Array.isArray(item)) {
                return new Es2019Array(...item).map(value => {
                    return {key, value}
                })
            }
            return [{key, value: item}]
        }

        /*
         * remaps the incoming {key, value} tuples
         * to naming container prefixed keys and values
         */
        let remapForNamingContainer = ({key, value}) => {
            key = this.remapKeyForNamingContainer(key);
            return {key, value}
        };

        /*
         * collects everything into a FormData object
         */
        return ofAssoc(this.value)
            .flatMap(expandValueArrays)
            .map(remapForNamingContainer)
            .reduce((formData: FormData, {key, value}: any) => {
                formData.append(key, value);
                return formData;
            }, new FormData()) as FormData;
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
     * generic post init code, for now, this performs some post assign data post-processing
     * @param rootElement the root element which knows the request type (usually a form)
     * @param executes the executable dom nodes which need to be processed into the form data, which we can send
     * in our ajax request
     */
    private resolveRequestType(rootElement: DQ, executes?: Array<string>) {
        if (!executes || executes.indexOf(IDENT_NONE) != -1) {
            return;
        }
        this.isMultipartRequest = rootElement.isMultipartCandidate(true);
    }

    /**
     * special case view state handling
     *
     * @param form the form holding the view state value
     */
    private applyViewState(form: DQ) {
        if (this.getIf($nsp(P_VIEWSTATE)).isPresent()) {
            return;
        }
        let viewStateElement = form.querySelectorAllDeep(`[name*='${$nsp(P_VIEWSTATE)}'`);
        let viewState = viewStateElement.inputValue;
        this.appendIf(viewState.isPresent(), this.remapKeyForNamingContainer(viewStateElement.name.value)).value = viewState.value;
    }

    /**
     * determines fields to submit
     * @param {Node} parentItem - form element item is nested in
     * @param {Array} partialIds - ids fo PPS
     */
    private encodeSubmittableFields(parentItem: DQ, partialIds: string[] = []) {

        const mergeIntoThis = ([key, value]) => this.append(key).value = value;
        const namingContainerRemap = ([key, value]) => this.paramsMapper(key as string, value);

        const remappedPartialIds = partialIds.map(partialId => this.remapKeyForNamingContainer(partialId));
        const partialIdsFilter = ([key, value]) => (!remappedPartialIds.length || key.indexOf("@") == 0) ? true :
            remappedPartialIds.indexOf(key) != -1;

        let inputs = getFormInputsAsArr(parentItem);
        inputs
            .map(fixEmptyParameters)
            .map(namingContainerRemap)
            .filter(partialIdsFilter)
            .forEach(mergeIntoThis);
    }

    private remapKeyForNamingContainer(key: string): string {
        return this.paramsMapper(key, "")[0];
    }
}
