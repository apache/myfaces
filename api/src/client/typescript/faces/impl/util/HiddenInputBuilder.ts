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
    private namedViewRoot = false;
    private readonly name: string;
    private readonly template: string;

    constructor(selector: string) {
        const isViewState = selector.includes($nsp(P_VIEWSTATE));
        this.name = isViewState ? P_VIEWSTATE : P_CLIENT_WINDOW;
        this.template = isViewState ? HTML_VIEWSTATE : HTML_CLIENT_WINDOW;
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
        const newElement = DQ.fromMarkup($nsp(this.template));
        newElement.id.value = this.buildId();
        newElement.name.value = this.buildName();
        this.parent?.append(newElement);
        return newElement;
    }

    private buildId(): string {
        const separator = $faces().separatorchar;
        const parts = this.namingContainerId?.length ?
            [this.namingContainerId, $nsp(this.name), this.nextIndex()] :
            [$nsp(this.name), this.nextIndex()];

        return parts.join(separator);
    }

    private buildName(): string {
        if (!this.namedViewRoot) {
            return $nsp(this.name);
        }

        return this.namingContainerId?.length ?
            [this.namingContainerId, $nsp(this.name)].join($faces().separatorchar) :
            $nsp(this.name);
    }

    private nextIndex(): number {
        const separator = $faces().separatorchar;
        return DQ$(`[name*='${$nsp(this.name)}']`).asArray
            .map(state => state.id.orElse("0").value)
            .map(id => id.substring(id.lastIndexOf(separator) + 1))
            .map(idSuffix => parseInt(idSuffix))
            .filter(idSuffix => !isNaN(idSuffix))
            .reduce((max, idSuffix) => Math.max(max, idSuffix), 0) + 1;
    }
}
