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
import {BEGIN, CTX_PARAM_PASS_THR, EVENT, P_PARTIAL_SOURCE, SOURCE} from "../core/Const";

export class EventData implements IEventData{
    type: string;
    status: string;
    source: any;
    responseCode: string;
    responseText: string;
    responseXML: Document;

    static createFromRequest(request: XMLHttpRequest, context: Config, /*event name*/ name: string): EventData {

        let eventData = new EventData();

        eventData.type = EVENT;
        eventData.status = name;

        let sourceId: string = context.getIf(SOURCE)
            .orElseLazy(() => context.getIf(P_PARTIAL_SOURCE).value)
            .orElseLazy(() => context.getIf(CTX_PARAM_PASS_THR, P_PARTIAL_SOURCE).value)
            .value;
        if (sourceId) {
            eventData.source = DQ.byId(sourceId, true).first().value.value;
        }

        if (name !== BEGIN) {
            eventData.responseCode = request?.status?.toString();
            eventData.responseText = request?.responseText;
            eventData.responseXML = request?.responseXML;
        }
        return eventData;
    }
}
