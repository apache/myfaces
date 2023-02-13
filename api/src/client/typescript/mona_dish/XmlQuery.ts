/*!
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

import {Lang} from "./Lang";
import {DomQuery} from "./DomQuery";
import isString = Lang.isString;
import {_global$} from "./Global";

declare let ActiveXObject: any;

/**
 * xml query as specialized case for DomQuery
 */
export class XMLQuery extends DomQuery {

    constructor(rootNode: Document | string | DomQuery, docType: string = "text/xml") {

        let createIe11DomQueryShim = (): DOMParser => {
            //at the time if wroting ie11 is the only relevant browser
            //left withut any DomQuery support
            let parser = new ActiveXObject("Microsoft.XMLDOM");
            parser.async = false;
            //we shim th dom parser from ie in
            return <any>{
                parseFromString: (text: string, contentType: string): Document => {
                    return parser.loadXML(text);
                }
            }
        };

        let parseXML = (xml: string): Document => {
            if (xml == null) {
                return null;
            }
            let domParser: DOMParser = Lang.saveResolveLazy<DOMParser>(
                () => new (_global$()).DOMParser(),
                (): DOMParser => createIe11DomQueryShim()
            ).value;
            return domParser.parseFromString(xml, <any> docType);
        };

        if (isString(rootNode)) {
            super(parseXML(<string>rootNode))
        } else {
            super(rootNode);
        }
    }

    isXMLParserError(): boolean {
        return this.querySelectorAll("parsererror").isPresent();
    }

    toString(): string {
        let ret = [];
        this.eachElem((node: any) => {
            let serialized = (_global$())?.XMLSerializer?.constructor()?.serializeToString(node) ?? node?.xml;
            if (!!serialized) {
                ret.push(serialized);
            }
        });
        return ret.join("");
    }

    parserErrorText(joinstr: string): string {
        return this.querySelectorAll("parsererror").textContent(joinstr);
    }

    static parseXML(txt: string): XMLQuery {
        return new XMLQuery(txt);
    }

    static parseHTML(txt: string): XMLQuery {
        return new XMLQuery(txt, "text/html");
    }

    static fromString(txt: string, parseType: string = "text/xml"): XMLQuery {
        return new XMLQuery(txt, parseType);
    }
}

export const XQ = XMLQuery;
export type XQ = XMLQuery;