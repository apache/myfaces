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

import {describe} from "mocha";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import * as sinon from "sinon";


import {XmlResponses} from "../frameworkBase/_ext/shared/XmlResponses";
import {expect} from "chai";
import {DomQuery, DQ, DQ$} from "mona-dish";
import protocolPage = StandardInits.protocolPage;


declare var faces: any;
declare var Implementation: any;

/**
 * response test
 * the idea is simply to pass in a dom
 * the context and a response xml and then check what happens
 * we do not need to go through the entire ajax cycle for that.
 */
describe('Tests of the various aspects of the response protocol functionality', function () {

    beforeEach(async function () {
        let waitForResult = protocolPage();
        return waitForResult.then((close) => {

            this.xhr = sinon.useFakeXMLHttpRequest();
            this.requests = [];

            this.respond = (response: string): XMLHttpRequest => {
                let xhrReq = this.requests.shift();
                xhrReq.responsetype = "text/xml";
                xhrReq.respond(200, {'Content-Type': 'text/xml'}, response);
                return xhrReq;
            };

            this.xhr.onCreate = (xhr) => {
                this.requests.push(xhr);
            };
            (<any>global).XMLHttpRequest = this.xhr;
            window.XMLHttpRequest = this.xhr;

            this.closeIt = () => {
                (<any>global).XMLHttpRequest = window.XMLHttpRequest = this.xhr.restore();
                Implementation.reset();
                close();
            }
        });
    });

    afterEach(function () {
        this.closeIt();
    });

    it("must have a simple field updated as well as the viewstate", function (done) {
        //DQ.byId("cmd_update_insert").click();
        DQ.byId("cmd_update_insert").click();
        this.respond(XmlResponses.UPDATE_INSERT_1);

        expect(DQ.byId("changesArea")
            .html()
            .orElse("fail")
            .value.indexOf("update succeeded 1") != -1)
            .to.be.true;

        let pos1 = (<string>DQ.byId(document.body).html()
            .value).indexOf("insert before succeeded should display before test1");
        let pos3 = (<string>DQ.byId(document.body).html()
            .value).indexOf("insert after succeeded should display after test1");
        let pos2 = (<string>DQ.byId(document.body).html()
            .value).indexOf("update succeeded 1");

        expect(pos1 != -1).to.be.true;

        expect(pos1 < pos2 && pos2 < pos3).to.be.true;

        let pos4 = (<string>DQ.byId(document.body).html()
            .value).indexOf("embedded script at update succeed");

        expect(pos4 != -1).to.be.true;

        done();
    });


    it("must have a simple field updated  with the second before update rendering path", function (done) {
        //DQ.byId("cmd_update_insert").click();
        DQ.byId("cmd_update_insert").click();
        this.respond(XmlResponses.UPDATE_INSERT_2);

        expect(DQ.byId("changesArea")
            .html()
            .orElse("fail")
            .value.indexOf("update succeeded 2") != -1)
            .to.be.true;

        let pos1 = (<string>DQ.byId(document.body).html()
            .value).indexOf("insert before succeeded should display before test1");
        let pos3 = (<string>DQ.byId(document.body).html()
            .value).indexOf("insert after succeeded should display after test1");
        let pos2 = (<string>DQ.byId(document.body).html()
            .value).indexOf("update succeeded 2");

        expect(pos1 != -1).to.be.true;

        expect(pos1 < pos2 && pos2 < pos3).to.be.true;

        let pos4 = (<string>DQ.byId(document.body).html()
            .value).indexOf("embedded script at update succeed");

        expect(pos4 != -1).to.be.true;

        done();
    });

    it("must have a full body update", function () {
        DQ.byId("cmd_replace").click();
        this.respond(XmlResponses.BODY_REPLACEMENT);

        //basic replacement
        let newBody = DQ.byId(document.body);
        let newContent = <string>newBody.html().value;
        //standard replacement successful
        expect(newContent.indexOf("<h3>Body replacement test successful</h3>") != -1,
            "elements must be updated").to.be.true;
        //script eval
        expect(newContent.indexOf(">hello from embedded script in replacement body<") != -1,
            "embedded scripts must be executed").to.be.true;

        //body attributes
        expect(newBody.hasClass("tundra"),
            "attributes must be updated").to.be.true;
        expect(newBody.id.value == "the_id",
            "id must be updated").to.be.true;
    });


    it("must have a proper working head replace", function () {
        DQ.byId("cmd_replace").click();

        this.respond(XmlResponses.HEAD_REPLACEMENT);

        //basic replacement
        let newBody = DQ.byId(document.body);
        let newContent = <string>newBody.html().value;

        //standard replacement successful
        //script eval
        //modern browsers block head replacement but you still can eval
        expect(newContent.indexOf(">hello from embedded script in replacement head") != -1,
            "embedded scripts must be executed").to.be.true;
    });


    it("must have a proper workiung  root replace", function () {
        DQ.byId("cmd_replace").click();
        this.respond(XmlResponses.VIEW_ROOT_REPLACEMENT);

        //basic replacement
        let newHead = DQ.byId(document.head);
        let newBody = DQ.byId(document.body);
        let newContent = <string>newBody.html().value;

        expect(newHead.isPresent(), " head must exist ").to.be.true;

        //standard replacement successful
        expect(newContent.indexOf("<h3>Body replacement test successful</h3>") != -1,
            "elements must be updated").to.be.true;
        //script eval
        expect(newContent.indexOf(">hello from embedded script in replacement body<") != -1,
            "embedded scripts must be executed").to.be.true;

        //body attributes
        expect(newBody.hasClass("tundra"),
            "attributes must be updated").to.be.true;
        expect(newBody.id.value == "the_id",
            "id must be updated").to.be.true;

        //standard replacement successful
        //script eval
        //modern browsers block head replacement but you still can eval
        expect(newContent.indexOf(">hello from embedded script in replacement head") != -1,
            "embedded scripts must be executed").to.be.true;
    });

    it("must have a viewState update to be performed", function () {
        DQ.byId("cmd_viewstate").click();

        this.respond(XmlResponses.VIEWSTATE_1);
        let viewStateElem = DQ.byId('jakarta.faces.ViewState');
        expect(viewStateElem.inputValue.value == "hello world").to.be.true;
    });

    it("must have processed a proper delete", function () {
        DQ.byId("cmd_delete").click();

        this.respond(XmlResponses.DELETE_1);

        expect(DQ.byId("deletable").isAbsent()).to.be.true;

    });

    it("must have processed a proper eval of a script given in the eval tag", function () {
        DQ.byId("cmd_eval").click();
        this.respond(XmlResponses.EVAL_1);

        let resultHTML: string = <string>DQ.byId(document.body).html().value;
        expect(resultHTML.indexOf('eval test succeeded') != -1).to.be.true;

    });

    it("must have updated the viewstates properly", function () {
        DQ.byId("cmd_eval").click();
        /*js full submit form, coming from the integration tests*/
        window.document.body.innerHTML = `<form id="j_id__v_0" name="j_id__v_0" method="post" action="/IntegrationJSTest/integrationtestsjasmine/test7-eventtest.jsf"
      ><span id="updatePanel">hello world</span><a href="#"
                                                                                              onclick="return faces.util.chain(this, event,'return false;', 'return myfaces.ab(\'j_id_1l\',\'updateTrigger\');');"
                                                                                              id="updateTrigger"
                                                                                              name="updateTrigger"
                                                                                              class="updateTrigger">[Press
    me for Update]</a><input type="hidden" name="j_id_1l_SUBMIT" value="1">
</form>`;


        faces.ajax.request(window.document.getElementById("updateTrigger"), null, {
            render: "updatePanel",
            execute: "updatePanel updateTrigger"
        });

        // language=XML
        this.respond(`<?xml version="1.0" encoding="UTF-8"?>
        <partial-response id="j_id__v_0">
            <changes>
                <update id="updatePanel"><![CDATA[<span id="updatePanel">hello world</span>]]></update>
                <update id="j_id__v_0:jakarta.faces.ViewState:1"><![CDATA[RTUyRDI0NzE4QzAxM0E5RDAwMDAwMDVD]]></update>
            </changes>
        </partial-response>`);


        expect(DQ$("[name*='jakarta.faces.ViewState']").isPresent()).to.be.true;

        expect(DQ$("[name*='jakarta.faces.ViewState']").val == "RTUyRDI0NzE4QzAxM0E5RDAwMDAwMDVD").to.be.true;
    });


    it("must have updated the viewstates properly with lenient update block", function () {
        DQ.byId("cmd_eval").click();
        /*js full submit form, coming from the integration tests*/
        window.document.body.innerHTML = `<form id="j_id__v_0" name="j_id__v_0" method="post" action="/IntegrationJSTest/integrationtestsjasmine/test7-eventtest.jsf"
      ><span id="updatePanel">hello world</span><a href="#"
                                                                                              onclick="return faces.util.chain(this, event,'return false;', 'return myfaces.ab(\'j_id_1l\',\'updateTrigger\');');"
                                                                                              id="updateTrigger"
                                                                                              name="updateTrigger"
                                                                                              class="updateTrigger">[Press
    me for Update]</a><input type="hidden" name="j_id_1l_SUBMIT" value="1">
</form>`;


        faces.ajax.request(window.document.getElementById("updateTrigger"), null, {
            render: "updatePanel",
            execute: "updatePanel updateTrigger"
        });

        // language=XML
        this.respond(`<?xml version="1.0" encoding="UTF-8"?>
        <partial-response id="j_id__v_0">
            <changes>
                <update id="updatePanel"><![CDATA[<span id="updatePanel">hello world</span>]]></update>
                <update id="j_id__v_0:jakarta.faces.ViewState:1"><![CDATA[RTUyRDI0NzE4QzAxM0E5RDAwMDAwMDVD]]><!-- 
                        Some random junk which is sent by the server
                    --></update>
            </changes>
        </partial-response>`);


        expect(DQ$("[name*='jakarta.faces.ViewState']").isAbsent()).to.be.false;

        // expect((<HTMLInputElement>document.getElementsByName("jakarta.faces.ViewState")[0]).value == "RTUyRDI0NzE4QzAxM0E5RDAwMDAwMDVD").to.be.true;
        expect(DQ$("[name*='jakarta.faces.ViewState']").inputValue.value == "RTUyRDI0NzE4QzAxM0E5RDAwMDAwMDVD").to.be.true;
    });


    /**
     * The body innerHTML is based on a Tobago page. View state and client window id is rendered within a
     * jsf-state-container.
     * Beside the tobago-out tag, the view state and the client window id must be updated properly.
     */
    it("must have updated the client window tag properly", function () {
        window.document.body.innerHTML = `<tobago-page locale='en' class='container-fluid' id='page'>
   <form action='/IntegrationJSTest/integrationtestsjasmine/tobago-jfwid-test.jsf' id='page::form' method='post' accept-charset='UTF-8' data-tobago-context-path=''>
    <input type='hidden' name='jakarta.faces.source' id='jakarta.faces.source' disabled='disabled'>
    <tobago-focus id='page::lastFocusId'>
     <input type='hidden' name='page::lastFocusId' id='page::lastFocusId::field'>
    </tobago-focus>
    <input type='hidden' name='org.apache.myfaces.tobago.webapp.Secret' id='org.apache.myfaces.tobago.webapp.Secret' value='SLrPlxqLEaR/oYFLSu4wgg=='>
    <tobago-in id='page:input' class='tobago-margin-bottom'>
     <input type='text' name='page:input' id='page:input::field' class='tobago-in form-control' value='Alice'>
     <tobago-behavior event='change' client-id='page:input' field-id='page:input::field' execute='page:input' render='page:output'></tobago-behavior>
    </tobago-in>
    <div id='page:output' class='tobago-margin-bottom'>
     <tobago-out class='form-control-plaintext'></tobago-out>
    </div>
    <div class='tobago-page-menuStore'></div>
    <span id='page::jsf-state-container'>
      <input type='hidden' name='jakarta.faces.ViewState' id='j_id__v_0:jakarta.faces.ViewState:1' value='RkExQ0Q1NTYzOTNCNzg0RjAwMDAwMDE4' autocomplete='off'>
      <input type='hidden' name='jakarta.faces.RenderKitId' value='tobago'>
      <input type='hidden' id='j_id__v_0:jakarta.faces.ClientWindow:1' name='jakarta.faces.ClientWindow' value='5m10kooxi'>
    </span>
   </form>
  </tobago-page>`;

        expect(DQ.querySelectorAll("#page\\:output tobago-out").textContent() === "").to.be.true;
        expect(DQ.byId("j_id__v_0:jakarta.faces.ViewState:1").isAbsent()).to.be.false;
        expect(DQ.byId("j_id__v_0:jakarta.faces.ClientWindow:1").isAbsent()).to.be.false;

        faces.ajax.request(window.document.getElementById("page:input"), "change", {
            "jakarta.faces.behavior.event": "change",
            execute: "page:input",
            render: "page:output"
        });

        this.respond(`<?xml version="1.0" encoding="UTF-8"?>
<partial-response id='j_id__v_0'>
<changes>
<update id='page:output'><![CDATA[
<div id='page:output' class='tobago-margin-bottom'>
<tobago-out class='form-control-plaintext'>Alice</tobago-out>
</div>]]>
</update>
<update id='j_id__v_0:jakarta.faces.ViewState:1'><![CDATA[MDQwQzkxNkU0MTg0RTQxRjAwMDAwMDE3]]>
</update>
<update id='j_id__v_0:jakarta.faces.ClientWindow:1'><![CDATA[5m10kooxg]]>
</update>
</changes>
</partial-response>`);

        expect(DQ.querySelectorAll("#page\\:output tobago-out").textContent() === "Alice").to.be.true;
        expect(DQ.byId("j_id__v_0:jakarta.faces.ViewState:1").isAbsent()).to.be.false;
        expect(DQ.byId("j_id__v_0:jakarta.faces.ClientWindow:1").isAbsent()).to.be.false;
    });


    it("must handle simple resource responses properly", function (done) {

        // we need to fake the response as well to see whether the server has loaded the addedViewHead code and has interpreted it
        //(window as any)["test"] = "booga";

        DQ.byId("cmd_simple_resource").click();
        this.respond(XmlResponses.SIMPLE_RESOURCE_RESPONSE);

        expect(document.head.innerHTML.indexOf("../../../xhrCore/fixtures/addedViewHead1.js") != -1).to.be.true;
        DQ.byId(document.body).waitUntilDom(() => DQ.byId('resource_area_1').innerHTML === "true")
            .then(() => done())
            .catch(done);
    })


    it("only single resources are allowed", function (done) {
        // we need to fake the response as well to see whether the server has loaded the addedViewHead code and has interpreted it
        //(window as any)["test"] = "booga";
        for (let cnt = 0; cnt < 10; cnt++) {
            DQ.byId("cmd_simple_resource").click();
            this.respond(XmlResponses.MULTIPLE_RESOURCE_RESPONSE);
        }

        expect(document.head.innerHTML.indexOf("../../../xhrCore/fixtures/addedViewHead2.js") != -1).to.be.true;
        let addedScriptsCnt = DomQuery.byId(document.head).querySelectorAll("script[src='../../../xhrCore/fixtures/addedViewHead2.js']").length;
        expect(addedScriptsCnt).to.eq(1);
        addedScriptsCnt = DomQuery.byId(document.head).querySelectorAll("style[rel='../../../xhrCore/fixtures/addedViewHead2.css']").length;
        expect(addedScriptsCnt).to.eq(1);
        done();
    })

    //TODO implement secondary response mockup
    it("must handle complex resource responses properly", function (done) {
        DQ.byId("cmd_complex_resource").click();
        this.respond(XmlResponses.MULTIPLE_RESOURCE_RESPONSE);

        let headHTML = document.head.innerHTML;
        expect(headHTML.indexOf("../../../xhrCore/fixtures/addedViewHead2.js")).not.eq(-1);
        expect(headHTML.indexOf("rel=\"../../../xhrCore/fixtures/addedViewHead2.css\"")).not.eq(-1);

        DQ.byId(document.body).waitUntilDom(() => DQ.byId('resource_area_2').innerHTML === "true2")
            .then(() => done())
            .catch(done);
    })

    it("embedded scripts must be evaled", function (done) {

        DQ.byId("cmd_complex_resource2").click();
        this.respond(XmlResponses.EMBEDDED_SCRIPTS_RESOURCE_RESPONSE);
        // this.respond("debugger; document.getElementById('resource_area_1').innerHTML = 'true3'",  {'Content-Type': 'text/javascript'});
        let headHTML = document.head.innerHTML;
        expect(headHTML.indexOf("../../../xhrCore/fixtures/addedViewHead3.js")).not.eq(-1);
        expect(headHTML.indexOf("href=\"../../../xhrCore/fixtures/addedViewHead2.css\"")).not.eq(-1);
        expect(DQ$("head link[rel='stylesheet'][href='../../../xhrCore/fixtures/addedViewHead2.css']").length).to.eq(1);
        setTimeout(() => {
            let evalAreaHtml = DQ.byId('resource_area_1').innerHTML;
            //last one must be the last item, order must be preserved
            expect(evalAreaHtml).to.eq("booga");
            done();
        }, 800)

    })


    it("head replacement must work (https://issues.apache.org/jira/browse/MYFACES-4498 and TCK Issue 4345IT)", function (done) {

        DQ.byId("cmd_complex_resource2").click();
        this.respond(XmlResponses.HEAD_REPLACE);
        let headHTML = document.head.innerHTML;

        //failing now, no elements in the html head after respond!!!
        expect(headHTML.indexOf("href=\"../../../xhrCore/fixtures/addedViewHead2.css\"")).not.eq(-1);
        expect(DQ$("head link[rel='stylesheet'][href='../../../xhrCore/fixtures/addedViewHead2.css']").length).to.eq(1);

        expect(headHTML.indexOf("../../../xhrCore/fixtures/addedViewHead3.js")).not.eq(-1);
        setTimeout(() => {
            let evalAreaHtml = DQ.byId('resource_area_1').innerHTML;
            //last one must be the last item, order must be preserved
            expect(evalAreaHtml).to.eq("booga");
            done();
        }, 800)

    })


    it("complex head replacement must work", function (done) {

        DQ.byId("cmd_complex_resource2").click();
        this.respond(XmlResponses.HEAD_REPLACE2);
        let headHTML = document.head.innerHTML;

        //failing now, no elements in the html head after respond!!!
        expect(headHTML.indexOf("href=\"../../../xhrCore/fixtures/addedViewHead2.css\"")).not.eq(-1);
        expect(DQ$("head link[rel='stylesheet'][href='../../../xhrCore/fixtures/addedViewHead2.css']").length).to.eq(1);

        let metas = DQ$("head meta");
        expect(metas.length).to.eq(5);
        expect(metas.get(0).attr("charSet").value == "UTF-8");
        expect(metas.get(4).attr("author").value == "Whoever");

        expect(headHTML.indexOf("../../../xhrCore/fixtures/addedViewHead3.js")).not.eq(-1);
        setTimeout(() => {
            let evalAreaHtml = DQ.byId('resource_area_1').innerHTML;
            //last one must be the last item, order must be preserved
            expect(evalAreaHtml).to.eq("booga");
            done();
        }, 800)

    })
    const INNER_HTML_MULIT_VIEW = `
        <div id="viewroot_1">
            <form id="viewroot_1:form1">
                <button type="submit" id="viewroot_1:submit_1"></button>
                <input type="hidden" id="viewroot_1:form1:jakarta.faces.ViewState:1" name="jakarta.faces.ViewState" value="booga"></input>
            </form>
            <form id="viewroot_1:form2">
                <button type="submit" id="viewroot_1:submit_2"></button>
            </form>
        </div>
        
        <div id="viewroot_2">
            <form id="viewroot_2:form1">
                <button type="submit" id="viewroot_1:submit_2"></button>
            </form>
        </div>
        `;
    it("must handle multiple view roots", function (done) {


        const RESPONSE_1 = `<?xml version="1.0" encoding="UTF-8"?>
        <partial-response id='viewroot_1'>
        <changes>
        <update id='viewroot_1:jakarta.faces.ViewState:1'><![CDATA[updatedVST]]></update>
        </changes>
        </partial-response>`

        window.document.body.innerHTML = INNER_HTML_MULIT_VIEW;

        faces.ajax.request(window.document.getElementById("viewroot_1:submit_1"), null, {
            "javax.faces.behavior.event": "change",
            execute: "submit_1",
            render: "form1"
        });
        this.respond(RESPONSE_1);
        expect(DQ$("#viewroot_1\\:form2 [name='jakarta.faces.ViewState']").isAbsent()).to.be.true;
        expect(DQ$("#viewroot_1\\:form1 [name='jakarta.faces.ViewState']").isPresent()).to.be.true;
        expect(DQ$("#viewroot_1\\:form1 [name='jakarta.faces.ViewState']").val).to.be.eq("updatedVST");

        done();
    })

    it("must handle multiple view roots multi forms", function (done) {
        const RESPONSE_1 = `<?xml version="1.0" encoding="UTF-8"?>
        <partial-response id='viewroot_1'>
        <changes>
        <update id='viewroot_1:jakarta.faces.ViewState:1'><![CDATA[updatedVST]]></update>
        </changes>
        </partial-response>`

        window.document.body.innerHTML = INNER_HTML_MULIT_VIEW;
        global["debug4"] = true;
        faces.ajax.request(window.document.getElementById("viewroot_1:submit_1"), null, {
            "javax.faces.behavior.event": "change",
            execute: "submit_1",
            render: "viewroot_1:form1 submit_2"
        });
        this.respond(RESPONSE_1);
        expect(DQ$("#viewroot_1\\:form2 [name*='jakarta.faces.ViewState']").isPresent()).to.be.true;
        expect(DQ$("#viewroot_1\\:form1 [name*='jakarta.faces.ViewState']").isPresent()).to.be.true;
        expect(DQ$("#viewroot_2\\:form1\\:form1 [name*='jakarta.faces.ViewState']").isAbsent()).to.be.true;

        expect(faces.getViewState(DQ$("#viewroot_1\\:form2").getAsElem(0).value).indexOf("jakarta.faces.ViewState=updatedVST") != -1).to.be.true;
        expect(faces.getViewState("viewroot_1:form1").indexOf("jakarta.faces.ViewState=updatedVST") != -1).to.be.true;

        done();
    })


    it("must handle multiple view roots with ClientWindow ids", function (done) {


        const RESPONSE_1 = `<?xml version="1.0" encoding="UTF-8"?>
        <partial-response id='viewroot_1'>
        <changes>
        <update id='viewroot_1:jakarta.faces.ClientWindow:1'><![CDATA[updatedViewId]]></update>
        </changes>
        </partial-response>`

        window.document.body.innerHTML = INNER_HTML_MULIT_VIEW;

        faces.ajax.request(window.document.getElementById("viewroot_1:submit_1"), null, {
            "javax.faces.behavior.event": "change",
            execute: "submit_1",
            render: "viewroot_1:form1"
        });
        this.respond(RESPONSE_1);
        expect(DQ$("#viewroot_1\\:form2 [name*='jakarta.faces.ClientWindow']").isAbsent()).to.be.true;
        expect(DQ$("#viewroot_1\\:form1 [name*='jakarta.faces.ClientWindow']").isPresent()).to.be.true;
        expect(DQ$("#viewroot_1\\:form1 [name*='jakarta.faces.ClientWindow']").val).to.be.eq("updatedViewId");

        done();
    })

    it("must handle multiple view roots multi forms with ClientWindow ids", function (done) {
        const RESPONSE_1 = `<?xml version="1.0" encoding="UTF-8"?>
        <partial-response id='viewroot_1'>
        <changes>
        <update id='viewroot_1:jakarta.faces.ClientWindow:1'><![CDATA[updatedViewId]]></update>
        </changes>
        </partial-response>`

        window.document.body.innerHTML = INNER_HTML_MULIT_VIEW;

        faces.ajax.request(window.document.getElementById("viewroot_1:submit_1"), null, {
            "javax.faces.behavior.event": "change",
            execute: "submit_1",
            render: "viewroot_1:form1 :submit_2"
        });
        this.respond(RESPONSE_1);
        expect(DQ$("#viewroot_1\\:form2 [name*='jakarta.faces.ClientWindow']").isPresent()).to.be.true;
        expect(DQ$("#viewroot_1\\:form1 [name*='jakarta.faces.ClientWindow']").isPresent()).to.be.true;
        expect(DQ$("#viewroot_2\\:form1\\:form1 [name*='jakarta.faces.ClientWindow']").isAbsent()).to.be.true;
        expect(DQ$("#viewroot_1\\:form2 [name*='jakarta.faces.ClientWindow']").val).to.be.eq("updatedViewId");
        expect(DQ$("#viewroot_1\\:form1 [name*='jakarta.faces.ClientWindow']").val).to.be.eq("updatedViewId");

        done();
    })

    const TCK_790_MARKUP = `
        <div id="panel1">
            <form id="form1" name="form1" method="post"
                  action="booga"
                  ><input id="form1:button" name="form1:button" type="submit"
                                                                     value="submit form1 via ajax">
                   <input type="hidden" name="jakarta.faces.ViewState"
                                        id="viewroot_1:jakarta.faces.ViewState:1"
                                        value="beforeUpdate">
            </form>
        </div>
        <div id="panel2">
            <form id="form2" name="form2" method="post" action="booga2"
                  ><a href="#" id="form2:link" name="form2:link"></a>
           </form>
        </div>
        <div id="panel3">
            <form id="form3" name="form3" method="post" action="booga3"
                  ><a href="#" id="form3:link" name="form3:link"></a>
            </form>
        </div>
        `;


    const TCK_790_NAV_MARKUP = `
            <form id="form1x" name="form1" method="post"
                  action="booga"
                  ><input id="form1x:button" name="form1x:button" type="submit"
                                                                     value="submit form1 via ajax">
                   <input type="hidden" name="jakarta.faces.ViewState"
                                        id="viewroot_1:jakarta.faces.ViewState:1"
                                        value="beforeUpdate">
            </form>
    `;

    /**
     * Similar to TCK 790
     */
    it("must handle a more complex replace with several forms and one issuing form and a viewstate and a viewroot id in response but viewroot is not present in page", function (done) {        //special case, viewid given but no viewid in page special result all render and executes must be updated

        document.body.innerHTML = TCK_790_MARKUP;

        const RESPONSE_1 = `<partial-response id="viewroot_1">
    <changes>
        <update id="panel2"><![CDATA[
            <div id="panel2">
            after update
                <form id="form2" name="form2" method="post" action="booga2"
                      ><a href="#" id="form2:link" name="form2:link"></a>
                      <input type="hidden" name="form2_SUBMIT" value="1"/></form>
            </div>
            ]]>
        </update>
        <update id="panel3"><![CDATA[
            <div id="panel3">
            after update
                <form id="form3" name="form3" method="post" action="booga3"
                      ><a href="#"  id="form3:link" name="form3:link"></a>
                </form>
            </div>
            ]]>
        </update>
        <update id="viewroot_1:jakarta.faces.ViewState:1"><![CDATA[booga_after_update]]></update>
    </changes>
</partial-response>`;
        faces.ajax.request(window.document.getElementById("form1:button"), null, {
            "javax.faces.behavior.event": "click",
            execute: "form1",
            render: "form2 form3"
        });

        this.respond(RESPONSE_1);
        // all forms in execute and render must receive the latest viewstate
        expect(DQ$("#form1 [name*='jakarta.faces.ViewState']").val).to.eq("booga_after_update");
        expect(DQ$("#form2 [name*='jakarta.faces.ViewState']").val).to.eq("booga_after_update");
        expect(DQ$("#form2 [name*='jakarta.faces.ViewState']").val).to.eq("booga_after_update");
        done();
    })

    it("must handle a complex navigation response (TCK Spec790)", function (done) {
        /*we start from a simple form which triggers a an internal navigation*/
        document.body.innerHTML = TCK_790_NAV_MARKUP;

        faces.ajax.request(window.document.getElementById("form1x:button"), null, {
            "javax.faces.behavior.event": "click",
            execute: "@form",
            render: ":form1x:button"
        });

        //TODO xhr stubbing, to check if the viewId is prepended in render!

        this.respond(`<?xml version="1.0" encoding="UTF-8"?>
<partial-response id="viewroot_1">
    <changes>
        <update id="jakarta.faces.ViewRoot"><![CDATA[<!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <title>Spec 790</title>
                <script src="/jakarta.faces.resource/faces.js.xhtml?ln=jakarta.faces;stage=Development"></script>
            </head>
            <body>
                <div id="panel1">
                 <form id="form1" name="form1" method="post"
                  action="booga"
                  ><input id="form1:button" name="form1:button" type="submit"
                                                                     value="submit form1 via ajax">
                </form>
                </div>
                <div id="panel2">
                after update
                    <form id="form2" name="form2" method="post" action="booga2"
                          ><a href="#" id="form2:link" name="form2:link"></a>
                         <input type="hidden" name="form2_SUBMIT" value="1"/></form>
                </div>
                  <div id="panel3">
                    after update
                        <form id="form3" name="form3" method="post" action="booga3"
                              ><a href="#"  id="form3:link" name="form3:link"></a>
                        </form>
                    </div>
            </body>
             </html>
          ]]>
        </update>
        <update id="viewroot_1:jakarta.faces.ViewState:1"><![CDATA[booga_after_update]]></update>
    </changes>
</partial-response>
`)
        expect(DQ$("#form1 [name*='jakarta.faces.ViewState']").val).to.eq("booga_after_update");
        expect(DQ$("#form2 [name*='jakarta.faces.ViewState']").val).to.eq("booga_after_update");
        expect(DQ$("#form3 [name*='jakarta.faces.ViewState']").val).to.eq("booga_after_update");

        expect(DQ$("#form1 [name*='jakarta.faces.ViewState']").id.value.indexOf("viewroot_1:jakarta.faces.ViewState:0") === 0).to.be.true;
        expect(DQ$("#form2 [name*='jakarta.faces.ViewState']").id.value.indexOf("viewroot_1:jakarta.faces.ViewState:1") === 0).to.be.true;
        expect(DQ$("#form3 [name*='jakarta.faces.ViewState']").id.value.indexOf("viewroot_1:jakarta.faces.ViewState:2") === 0).to.be.true;

        done();
    });

    it('must handle a ViewExpired Error correctly, and only once in a listener', function (done) {

        document.body.innerHTML = TCK_790_NAV_MARKUP;

        let errorCalled = 0;
        faces.ajax.addOnError((error)=> {
            expect(error.errorName).to.eq("jakarta.faces.application.ViewExpiredException");
            expect(error.errorMessage).to.eq("serverError: View \"/testhmtl.xhtml\" could not be restored.");
            expect(error.source.id).to.eq("form1x:button");
            errorCalled++;
        });

        faces.ajax.request(window.document.getElementById("form1x:button"), null, {
            "javax.faces.behavior.event": "click",
            execute: "@form",
            render: ":form1x:button"
        });

        this.respond(`<?xml version="1.0" encoding="UTF-8"?>
        <partial-response><error>
        <error-name>jakarta.faces.application.ViewExpiredException</error-name>
        <error-message><![CDATA[View "/testhmtl.xhtml" could not be restored.]]></error-message>
        </error>
        </partial-response>`)

        expect(errorCalled).to.eq(1);

        done();

    });




});