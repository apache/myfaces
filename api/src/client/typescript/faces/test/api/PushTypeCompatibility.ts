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
// AI-generated: this file was created with assistance from Claude (Anthropic) — see AI_CONTRIBUTIONS.md

import type { faces } from '../../api/_api';

/**
 * Compatibility interface covering both the faces 9-param form (with onerror)
 * and the JSF 2.3 8-param form (without onerror). Both must be callable so
 * that the runtime implementation works correctly in both the faces and JSF realms.
 * This file is never executed — tsc rejecting it signals a broken signature.
 */
interface PushCompat {
    init(socketClientId: string, url: string, channel: string,
         onopen: faces.push.OnOpenHandler | string | null,
         onmessage: faces.push.OnMessageHandler | string | null,
         onerror: faces.push.OnErrorHandler | string | null,
         onclose: faces.push.OnCloseHandler | string | null,
         behaviors: Record<string, Array<() => void>>,
         autoConnect: boolean): void;
    init(socketClientId: string, url: string, channel: string,
         onopen: faces.push.OnOpenHandler | string | null,
         onmessage: faces.push.OnMessageHandler | string | null,
         onclose: faces.push.OnCloseHandler | string | null,
         behaviors: Record<string, Array<() => void>>,
         autoConnect: boolean): void;
    open(socketClientId: string): void;
    close(socketClientId: string): void;
}

function verifyPushInitTypeCompatibility(push: PushCompat): void {
    // faces 4.0 form — with onerror
    push.init("clientId1", "booga.ws", "mychannel",
        () => {},
        () => {},
        () => {},
        () => {},
        {},
        true
    );

    // JSF 2.3 legacy form — without onerror
    push.init("clientId1", "booga.ws", "mychannel",
        () => {},
        () => {},
        () => {},
        {},
        true
    );
}

verifyPushInitTypeCompatibility;
