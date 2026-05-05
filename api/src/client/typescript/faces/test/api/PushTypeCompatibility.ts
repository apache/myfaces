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

/**
 * Compile-time type compatibility check for Push.init() overloads.
 *
 * This file is never executed. Its only purpose is to make the TypeScript
 * compiler verify that both supported call signatures of Push.init() are
 * accepted: the 4-callback form (onopen, onmessage, onerror, onclose) and
 * the legacy 3-callback form (onmessage, onerror, onclose). If either
 * overload is accidentally broken, tsc will reject this file.
 */
function verifyPushInitTypeCompatibility(push: Push): void {
    push.init("clientId1", "booga.ws", "mychannel",
        () => {},
        () => {},
        () => {},
        () => {},
        {},
        true
    );

    push.init("clientId1", "booga.ws", "mychannel",
        () => {},
        () => {},
        () => {},
        {},
        true
    );
}

verifyPushInitTypeCompatibility;
