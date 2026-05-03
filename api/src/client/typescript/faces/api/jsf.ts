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
 * jsf.js init layer which provides as per spec the proper
 * window namespace if it does not exist already
 * if this file is included then the code falls back with its namespaces
 * on jsf2.3 or earlier level, for 4.0+ please include faces.js
 */
if(!window?.jsf) {
    const myfacesApi = require("./_api").faces;
    (window as any)['jsf'] = window?.jsf ?? {...myfacesApi, push: {...myfacesApi.push}};
    window.jsf.specversion = 230000;
    delete (window.jsf as Partial<typeof window.jsf>).contextpath;

    /*
     * we shim back the breaking api change from 3.0 to 4.0
     * JSF 2.3 did not expose a websocket onerror callback.
     */
    window.jsf.push.init = (socketClientId: string,
                            url: string,
                            channel: string,
                            onopen: Function,
                            onmessage: Function,
                            onclose: Function,
                            behaviors: any,
                            autoConnect: boolean) => {
        myfacesApi.push.init(socketClientId, url, channel, onopen, onmessage, null, onclose, behaviors, autoConnect);
    }
}
if(!window?.myfaces?.ab) {
    const myfaces = require("./_api").myfaces;
    //namespace might be extended is not exclusively reserved so we merge
    (window as any)["myfaces"] = window?.myfaces ?? {};
    Object.keys(myfaces).forEach(key => window.myfaces[key] = window.myfaces?.[key] ?? myfaces[key]);
}

export var jsf = window.jsf;
export var myfaces = window.myfaces;
