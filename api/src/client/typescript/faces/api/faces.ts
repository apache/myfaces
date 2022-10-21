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

"use strict";

/**
 * faces.js init layer which provides as per spec the proper
 * window namespace if it does not exist already
 *
 * The idea is that we use a small shim on top of
 * the implementation to provide the window namespace.
 * The implementation itself is in a protected namespace
 * which will be bound by the build system
 *
 * The documentation nevertheless targets the _api file, which
 * hosts the full api
 */
if(!window.faces) {
    //we lazily load the code to prevent ram bloat
    const faces = require("./_api").faces;
    window['faces'] = window?.faces ?? faces;
}
if(!window?.myfaces?.ab) {
    const myfaces = require("./_api").myfaces;

    //namespace might be extended is not exclusively reserved so we merge
    (window as any)["myfaces"] = window?.myfaces ?? {};
    Object.keys(myfaces).forEach(key => window.myfaces[key] = window.myfaces?.[key] ?? myfaces[key]);
}
export var faces = window.faces;
export var myfaces = window.myfaces;
