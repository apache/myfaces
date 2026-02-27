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

/**
 * Compatibility wrapper that provides a default export of the full namespace.
 * Used by the tsconfig paths alias so that `import pkg from 'mona-dish'`
 * works in tests and consumer projects that import from source, mirroring
 * the behaviour of the built UMD bundle with esModuleInterop enabled.
 */
import * as allExports from "./index";

export * from "./index";
export default allExports;
