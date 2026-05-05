/*
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
 * Shim to provide a default export for mona-dish.
 *
 * index.ts has only named exports, which breaks the pattern:
 *   import pkg from 'mona-dish'; const { Lang } = pkg;
 *
 * Used via tsconfig paths for tests (tsx/ESM). Webpack keeps its own alias
 * to index_core.ts for the reduced-core production bundle.
 */
export * from "./mona_dish/index";
import * as _monaDish from "./mona_dish/index";
export default _monaDish;
