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
 *
 */

import {DomQuery, DQ, DQ$} from "mona-dish";
import {$faces, $nsp, HTML_CLIENT_WINDOW, HTML_VIEWSTATE, P_CLIENT_WINDOW, P_VIEWSTATE} from "../core/Const";

/**
 * Builder for hidden inputs.
 * ATM only ViewState and Client window
 * are supported (per spec)
 *
 * Improves readability in the response processor!
 */
export class HiddenInputBuilder {
    private namingContainerId?: string;
    private parent?: DomQuery;
    private namedViewRoot: boolean = false;
    private readonly name: string;
    private readonly template: string;

    constructor(private selector: string) {
        const isViewState = selector.indexOf($nsp(P_VIEWSTATE)) != -1;
        this.name = isViewState ? P_VIEWSTATE : P_CLIENT_WINDOW
        this.template = isViewState ? HTML_VIEWSTATE : HTML_CLIENT_WINDOW
    }

    withNamingContainerId(namingContainer: string): HiddenInputBuilder {
        this.namingContainerId = namingContainer;
        return this;
    }

    withParent(parent: DomQuery): HiddenInputBuilder {
        this.parent = parent;
        return this;
    }

    withNamedViewRoot(namedViewRoot: boolean): HiddenInputBuilder {
        this.namedViewRoot = namedViewRoot;
        return this;
    }


    build(): DomQuery {
        const SEP = $faces().separatorchar;

        let existingStates = DQ$(`[name*='${$nsp(this.name)}']`);
        let cnt = existingStates.asArray.map(state => {
            let ident: string = state.id.orElse("0").value;
            ident = ident.substring(ident.lastIndexOf(SEP)+1);
            return parseInt(ident);
        })
            .filter(item => {
                return !isNaN(item);
            })
            .reduce((item1, item2) => {
                return Math.max(item1, item2);
            }, 0); //we start with 1 (see cnt++)
        //the maximum  new ident is the current max + 1
        cnt++;


        const newElement = DQ.fromMarkup($nsp(this.template));
        newElement.id.value = ((this.namingContainerId?.length) ?
            [this.namingContainerId,  $nsp(this.name),  cnt]:
            [$nsp(this.name),  cnt]).join(SEP);

        //name must be prefixed with the naming container id as well according to the jsdocs
        if(this.namedViewRoot) {
            newElement.name.value = (this.namingContainerId?.length) ?
                [this.namingContainerId,  $nsp(this.name)].join(SEP): $nsp(this.name);
        } else {
            newElement.name.value = $nsp(this.name);
        }


        this?.parent?.append(newElement);
        return newElement;
    }
}
