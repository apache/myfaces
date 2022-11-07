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
import {ArrayCollector, Config, DQ, Lang, LazyStream, Stream} from "mona-dish";
import {EMPTY_STR, IDENT_ALL, IDENT_FORM, P_VIEWSTATE} from "../core/Const";
import isString = Lang.isString;
import {ExtConfig} from "../util/ExtDomQuery";


/**
 * A unified form data class
 * which builds upon our configuration.
 *
 * We cannot use standard html5 forms everywhere
 * due to api constraints on the HTML Form object in IE11
 * and due to the url encoding constraint given by the faces.js spec
 *
 * probably only one needed and one overlay!
 * the entire file input storing probably is redundant now
 * that dom query has been fixed //TODO check this
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
     * @param viewState the form view state or an external viewState coming in as string
     * @param executes the executes id list for the elements to being processed
     * @param partialIds partial ids to collect, to reduce the data sent down
     */
    constructor(private dataSource: DQ | string, viewState?: string, executes?: string[], private partialIds?: string[]) {
        super({});
        //a call to getViewState before must pass the encoded line
        //a call from getViewState passes the form element as datasource,
        //so we have two call points
        if (isString(dataSource)) {
            this.assignEncodedString(<string>this.dataSource);
        } else {
            this.applyFormDataToConfig();
        }
        if('undefined' != typeof viewState) {
            this.assignEncodedString(viewState)
        }
        if(executes) {
            this.postInit(...executes);
        }
    }

    /**
     * generic post init code, for now, this performs some post assign data post-processing
     * @param executes the executable dom nodes which need to be processed into the form data, which we can send
     * in our ajax request
     */
    postInit(...executes: Array<string>) {
        let fetchInput = (id: string): DQ => {
            if (id == IDENT_ALL) {
                return DQ.querySelectorAllDeep("input[type='file']");
            } else if (id == IDENT_FORM) {
                return (<DQ>this.dataSource).querySelectorAllDeep("input[type='file']");
            } else {
                let element = DQ.byId(id, true);
                return this.getFileInputs(element);
            }
        };

        let inputExists = (item: DQ) => {
            return item.isPresent();
        };


        this.isMultipartRequest = LazyStream.of(...executes)
            .map(fetchInput)
            .filter(inputExists)
            .first().isPresent();
    }

    /**
     * special case view state handling
     *
     * @param form the form holding the view state value
     */
    private applyViewState(form: DQ) {
        let viewState = form.byId(P_VIEWSTATE, true).inputValue;
        this.appendIf(viewState.isPresent(), P_VIEWSTATE).value = viewState.value;
    }

    /**
     * assigns an url encoded string to this xhrFormData object
     * as key value entry
     * @param encoded
     */
    assignEncodedString(encoded: string) {
        // this code filters out empty strings as key value pairs
        let keyValueEntries = decodeURIComponent(encoded).split(/&/gi)
                .filter(item => !!(item || '')
                .replace(/\s+/g,''));
        this.assignString(keyValueEntries);
    }

    /**
     * assign a set of key value pairs passed as array ['key=val1', 'key2=val2']
     * @param keyValueEntries
     */
    assignString(keyValueEntries: string[]) {
        let toMerge = new ExtConfig({});

        function splitToKeyVal(line: string) {
            return line.split(/=(.*)/gi);
        }

        function fixKeyWithoutVal(keyVal: string[]) {
            return keyVal.length < 3 ? [keyVal?.[0] ?? [], keyVal?.[1] ?? []] : keyVal;
        }

        Stream.of(...keyValueEntries)
            //split only the first =
            .map(line => splitToKeyVal(line))
            //special case of having keys without values
            .map(keyVal => fixKeyWithoutVal(keyVal))
            .each(keyVal => {
                toMerge.append(keyVal[0] as string).value = keyVal?.splice(1)?.join("") ?? "";
            });
        //merge with overwrite but no append! (aka no double entries are allowed)
        this.shallowMerge(toMerge);
    }

    /**
     * @returns a Form data representation, this is needed for file submits
     */
    toFormData(): FormData {
        let ret: any = new FormData();
        this.appendInputs(ret);
        return ret;
    }

    resolveSubmitIdentifier(elem: HTMLInputElement) {
        let identifier = elem.name;
        identifier = ((elem?.name ?? "").replace(/s+/gi, "") == "") ? elem.id : identifier;
        return identifier;
    }

    /**
     * returns an encoded string representation of our xhr form data
     *
     * @param defaultStr optional default value if nothing is there to encode
     */
    toString(defaultStr = EMPTY_STR): string {
        if (this.isAbsent()) {
            return defaultStr;
        }
        let entries = LazyStream.of(...Object.keys(this.value))
            .filter(key => this.value.hasOwnProperty(key))
            .flatMap(key => Stream.of(...this.value[key]).map(val => [key, val]).collect(new ArrayCollector()))
            .map(keyVal => {
                return `${encodeURIComponent(keyVal[0])}=${encodeURIComponent(keyVal[1])}`;
            })
            .collect(new ArrayCollector());

        return entries.join("&")
    }

    /**
     * helper to fetch all file inputs from as given root element
     * @param rootElement
     * @private
     */
    private getFileInputs(rootElement: DQ): DQ {
        const rootFileInputs = rootElement
            .filter(elem => elem.matchesSelector("input[type='file']"))
        const childFileInputs = rootElement
            .querySelectorAll("input[type='file']");

        return rootFileInputs.concat(childFileInputs);
    }

    /**
     * encode the given fields and apply the view state
     * @private
     */
    private applyFormDataToConfig() {
        //encode and append the issuing item if not a partial ids array of ids is passed
        /*
         * Spec. 13.3.1
         * Collect and encode input elements.
         * Additionally the hidden element jakarta.faces.ViewState
         * Enhancement partial page submit
         *
         */
        this.encodeSubmittableFields(this, <DQ>this.dataSource, this.partialIds);

        if (this.getIf(P_VIEWSTATE).isPresent()) {
            return;
        }

        this.applyViewState(<DQ>this.dataSource);
    }

    /**
     * determines fields to submit
     * @param {Object} targetBuf - the target form buffer receiving the data
     * @param {Node} parentItem - form element item is nested in
     * @param {Array} partialIds - ids fo PPS
     */
    private encodeSubmittableFields(targetBuf: Config,
                                    parentItem: DQ, partialIds ?: string[]) {
        let toEncode = null;
        if (this.partialIds && this.partialIds.length) {
            // in case of our myfaces reduced ppr we
            // only submit the partials
            this._value = {};
            toEncode = new DQ(...this.partialIds);

        } else {
            if (parentItem.isAbsent()) throw 'NO_PAR_ITEM';
            toEncode = parentItem;
        }

        //lets encode the form elements
        this.shallowMerge(toEncode.deepElements.encodeFormElement());
    }

    private appendInputs(ret: any) {
        Stream.of(...Object.keys(this.value))
            .each(key => {
                Stream.of(...this.value[key]).each(item => ret.append(key, item));
            });
    }
}