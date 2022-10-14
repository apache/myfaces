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


import {DomQuery} from "mona-dish";


declare let global;
declare let faces: any;
declare let jsf: any;
declare let myfaces: any;

/**
 * helpers with various init and html patterns
 *
 * We use jsdom global which builds up a
 * dom tree and emulates a full environment
 *
 * Note the buildup and loading is asynchronous so
 * we have to work with Promises and asyncs to get things
 * where we want to have them
 *
 * This is a pattern pretty much for every test which iterates over
 * multiple doms
 */
export module StandardInits {

    export const HTML_DEFAULT = `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<div id="id_1"></div>
<div id="id_2" booga="blarg"></div>
<div id="id_3"></div>
<div id="id_4"></div>
</body>
</html>`;


    export const HTML_SHADOW = `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<form id="blarg">
    <input type="text" id="blarg:input_1" name="blarg:input_1" value="input_1_val"></input>
    <input type="hidden" id="jakarta.faces.ViewState" name="jakarta.faces.ViewState" value="blubbblubblubb"></input>
    <input type="button" id="blarg:input_2" name="blarg:input_2" value="input_1_val"></input>
    <div id="shadowDomArea">
            <input type="button" id="blarg:input_3" name="blarg:input_3" value="input_3_val"></input>
    </div>
</form>
</body>
</html>`;

    /**
     * a page simulating basically a simple faces form
     */
    const HTML_FORM_DEFAULT = `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<form id="blarg">
    <input type="text" id="input_1" name="input_1" value="input_1_val"></input>
    <input type="hidden" id="jakarta.faces.ViewState" name="jakarta.faces.ViewState" value="blubbblubblubb"></input>
    <input type="button" id="input_2" name="input_2" value="input_1_val"></input>
</form>
</body>
</html>`;



    /**
     * a page simulating basically a simple faces form
     */
    const HTML_FILE_FORM_DEFAULT = `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<form id="blarg" enctype="multipart/form-data">
    <input type="file" id="fíleupload"></input>
    <input type="text" id="input_1" name="input_1" value="input_1_val"></input>
    <input type="hidden" id="jakarta.faces.ViewState" name="jakarta.faces.ViewState" value="blubbblubblubb"></input>
    <input type="button" id="input_2" name="input_2" value="input_1_val"></input>
</form>
</body>
</html>`;



    export const STD_XML = `<?xml version="1.0" encoding="utf-8"?><partial-response><changes><update id="value_1"><![CDATA[<span id="out1">2</span>]]></update><update id="jakarta.faces.ViewState"><![CDATA[j_id1:j_id3]]></update></changes></partial-response>`;

    /**
     * a page containing a faces.js input with a new separator char
     * @param separatorChar
     * @constructor
     */
    function HTML_DEFAULT_SEPARATOR_CHAR(separatorChar: string, IS_40=true) {
        return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
    <script type="text/javascript"
            src="/wfmportal/${IS_40 ? 'jakarta' : 'javax'}.faces.resource/${IS_40 ? 'faces': 'jsf'}.js.jsf?ln=jakarta.faces&separator=${separatorChar}"></script>
</head>
<body>
<form id="blarg">
    <input type="text" id="input_1" name="input_1"/>
    <input type="button" id="input_2" name="input_2"/>
</form>
</body>
</html>
    
    `;
    }

    /**
     * This is a standardized small page mockup
     * testing the various aspects of the protocol
     * under pure html conditions
     *
     * We get the jsf out of the way and basically simulate what the browser sees
     */
    export const PROTOCOL_PAGE = `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<h2>protocol testcase1</h2>

<div id="centerDiv">

    <h1>Selenium Testprobe for insert update delete and attribute change</h1>

    <h2>This test tests all aspects of the protocol, under xhr and iframe conditions</h2>

    <div id="testResults">
        
        <h3>Test Results</h3>

        <div id="evalarea1">eval area 1 triggered by eval</div>
        
        <div id="evalarea2">eval area 2 triggered by update</div>
        
        <div id="evalarea3">eval area 3 triggered by insert</div>
        
        <div id="evalarea4">eval area 4 triggered by a click on the changed attributes area</div>

        <div id="changesArea">update insert area</div>
        
        <div id="deleteable">delete area will be deleted once you press the delete button</div>
        
        <div id="attributeChange">attributes changes area</div>
    
    
    
    </div>

    <h2>Call actions via normal ppr</h2>

    <form id="form1" action="boog.html">
    
        <input type="hidden" id="jakarta.faces.ViewState" name="jakarta.faces.ViewState" value="blubbblubblubb"></input>
    
        <input type="button" id="cmd_eval" value="eval"
               onclick="emitPPR(this, ('undefined' == typeof event)? null: event, 'eval1');"/>
               
        <input type="button" id="cmd_update_insert" value="update insert"
               onclick="emitPPR(this, ('undefined' == typeof event)? null: event, 'updateinsert1');"/>
               
        <input type="button" id="cmd_update_insert2" value="update insert second protocol path"
               onclick="emitPPR(this, ('undefined' == typeof event)? null: event, 'updateinsert2');"/>

        <input type="button" id="cmd_delete" value="delete"
               onclick="emitPPR(this, ('undefined' == typeof event)? null: event, 'delete1');"/>

        <input type="button" id="cmd_replace" value="Replace Body"
               onclick="emitPPR(this, ('undefined' == typeof event)? null: event, 'body_replace1');"/>

        <input type="button" id="cmd_attributeschange" value="change attributes"
               onclick="emitPPR(this, ('undefined' == typeof event)? null: event, 'attributes');"/>

        <input type="button" id="cmd_illegalresponse" value="illegal response, error trigger"
               onclick="emitPPR(this, ('undefined' == typeof event)? null: event, 'illegalResponse');"/>

        <input type="button" id="cmd_viewstate" value="Viewstate only update trigger"
               onclick="emitPPR(this, ('undefined' == typeof event)? null: event, 'viewstate');"/>

        <input type="button" id="cmd_error" value="Server error with error response"
               onclick="emitPPR(this, ('undefined' == typeof event)? null: event, 'errors');"/>

        <input type="button" id="cmd_error_component" value="Error: no component given"
               onclick="(window.faces || window.jsf).ajax.request(null, event, {}); return false"/>

    </form>

    <script type="text/javascript">
        document.getElementById("evalarea1").innerHTML = "booga";

        var target = "./test.mockup";

        function emitPPR(source, event, action, useIframe, formName) {
            document.getElementById(formName || "form1").action = target;

            (window?.faces ?? window.jsf).ajax.request(/*String|Dom Node*/ source, /*|EVENT|*/ (window.event) ? window.event : event, /*{|OPTIONS|}*/ {op: action});
        }
    </script>
</div>
</body>`;

    export function basicXML(): Document {
        return new window.DOMParser().parseFromString(STD_XML, "text/xml");
    }

    export function standardInit(scope: any, initFunc: (boolean) => Promise<() => void> = defaultHtml): Promise<any> {
        (<any>global).navigator = {
            language: "en-En"
        };
        return initFunc(false).then((closeFunc: Function) => {
            (<any>scope).currentTest.closeIt = () => {
                closeFunc();
                delete (<any>global).navigator;
            }
        });
    }

    export function standardClose(scope: any) {
        (<any>scope).currentTest.closeIt();
    }

    export function defaultHtml(withJsf = true): Promise<() => void> {
        return init(HTML_DEFAULT, withJsf);
    }
    export function defaultHtml_23(withJsf = true): Promise<() => void> {
        return init(HTML_DEFAULT.replace(/jakarta/gi, "javax"), withJsf, false);
    }

    export function defaultMyFaces(withJsf = true): Promise<() => void> {
        return init(HTML_FORM_DEFAULT, withJsf);
    }
    export function defaultMyFaces23(withJsf = true): Promise<() => void> {
        return init(HTML_FORM_DEFAULT.replace(/jakarta/gi, "javax"), withJsf, false);
    }
    export function defaultFileForm(withJsf = true): Promise<() => void> {
        return init(HTML_FILE_FORM_DEFAULT, withJsf);
    }
    export function defaultFileForm_23(withJsf = true): Promise<() => void> {
        return init(HTML_FILE_FORM_DEFAULT.replace(/jakarta/gi, "javax"), withJsf, false);
    }

    export function shadowDomMyFaces(withJsf = true): Promise<() => void> {
        return <Promise<() => void>>init(HTML_SHADOW, withJsf).then((close) => {
            let shadow = DomQuery.byId(<any>window.document).byId("shadowDomArea").attachShadow();
            shadow.innerHtml = `
                <input type="button" id="input_3" name="input_3" value="input_3_val" ></input>
                <div id="shadowContent">before update</div>
            `;
            return close;
        });
    }

    export function protocolPage(withJsf = true, IS_40 = true): Promise<() => void> {
        return <any>init((IS_40) ? PROTOCOL_PAGE : PROTOCOL_PAGE.replace(/jakarta/gi,"javax"), withJsf, IS_40);
    }

    export function defaultSeparatorChar(separatorChar: string, withJsf = true, IS_40 = true): Promise<() => void> {
        let template = HTML_DEFAULT_SEPARATOR_CHAR(separatorChar, IS_40);
        return init(template, withJsf);
    }

    /**
     * we need to manually apply the jsf top the global namespace
     *
     * the reason simply is that
     * we use our typescript dependency system on the tests
     * which forfeits the global namespace.
     *
     * The global namespace is used after the build automatically
     * by using the window build
     *
     * @param data
     * @param Implementation
     */
    let applyJsfToGlobals = function (data, Implementation, PushImpl) {
        (<any>global).faces = data.faces;
        (<any>global).myfaces = data.myfaces;
        (<any>global).window.faces = data.faces;
        (<any>global).window.myfaces = data.myfaces;
        (<any>global).Implementation = Implementation.Implementation;
        (<any>global).PushImpl = PushImpl.PushImpl;
        //bypass a bug on windows jsdom, domparser not an auto global but on window only
        (<any>global).DOMParser = (<any>global)?.DOMParser ?? window.DOMParser;
        (<any>global).document = (<any>global)?.document ?? window.document;
    };

    let applyJsfToGlobals23 = function (data, Implementation, PushImpl) {
        (<any>global).jsf = data.jsf;
        (<any>global).myfaces = data.myfaces;
        (<any>global).window.jsf = data.jsf;
        (<any>global).window.myfaces = data.myfaces;
        (<any>global).Implementation = Implementation.Implementation;
        (<any>global).window.Implementation = Implementation.Implementation;
        (<any>global).PushImpl = PushImpl.PushImpl;
        //bypass a bug on windows jsdom, domparser not an auto global but on window only
        (<any>global).DOMParser = (<any>global)?.DOMParser ?? window.DOMParser;
        (<any>global).document = (<any>global)?.document ?? window.document;
    };


    /**
     * init the jsdom global
     * @param clean
     * @param template
     */
    let initJSDOM = async function (template: string) {
        // @ts-ignore
        return import('jsdom-global').then((domIt) => {
            let params = {
                contentType: "text/html",
                runScripts: "dangerously"
            };
            //we have two different apis depending whether we allow module interop with sinon or not
            return (domIt?.default ?? domIt)?.(template, params) ;
        });
    };

    /**
     * init the jsf subsystem
     */
    let initJSF = async function (IS_40: boolean = true) {
        // @ts-ignore

        const facesImport = IS_40 ? import("../../../../api/faces") : import("../../../../api/jsf");

        return facesImport.then((data) => {
            let Implementation = require("../../../../impl/AjaxImpl");
            let PushImpl = require("../../../../impl/PushImpl");
            IS_40 ? applyJsfToGlobals(data, Implementation, PushImpl): applyJsfToGlobals23(data, Implementation, PushImpl);
        }).catch(err => {
            console.error(err);
        });
    };

    /**
     * lets clean up some old data which might interfere
     */
    let resetGlobals = function () {
        (<any>global)?.Implementation?.reset();
        (<any>global)?.PushImpl?.reset();

        ((<any>global).faces) ? delete (<any>global).faces : null;
        ((<any>global).jsf) ? delete (<any>global).jsf : null;
        delete (<any>global).myfaces;
        ((<any>global).Implementation) ? delete (<any>global).Implementation : null;
        delete (<any>global).PushImpl;
    };

    /**
     * entry point which initializes the test system with a template and with or without jsf
     *
     * @param template
     * @param withJsf
     */
    async function init(template: string, withJsf = true, IS_JSF_40 = true): Promise<() => void> {
        //let dom2 = new JSDOM(template)
        //return initMyFacesFromDom(dom2);
        let clean = null;
        //we use jsdom global to fullfill our requirements
        //we need to import dynamically and use awaits
        if (withJsf) {

            resetGlobals();
            // @ts-ignore
            await initJSDOM(template).then(data => clean = data);
            await initJSF(IS_JSF_40);
        } else {
            // @ts-ignore
            await import('jsdom-global').then((domIt) => {
                clean = (domIt?.default ?? domIt)?.(template);
            });
        }
        //the async is returning a promise on the caller level
        //which gets the return value on once done
        return clean;

    }
}