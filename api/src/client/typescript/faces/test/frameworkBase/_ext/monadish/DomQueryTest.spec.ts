/* Licensed to the Apache Software Foundation (ASF) under one or more
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

import {expect} from 'chai';
import {describe, it} from 'mocha';
import {ArrayCollector, DomQuery, DomQueryCollector, Lang, LazyStream} from "mona-dish";
import trim = Lang.trim;
import {from} from "rxjs";


const jsdom = require("jsdom");
const {JSDOM} = jsdom;
(global as any).window = {}
let dom = null;
describe('DOMQuery tests', function () {

    beforeEach(function () {

        // language=HTML
        dom = new JSDOM(`
            <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <title>Title</title>
            </head>
            <body>
                <div id="id_1"></div>
                <div id="id_2"  booga="blarg" class="blarg2"></div>
                <div id="id_3" class="blarg1 blarg2"></div>
                <div id="id_4"></div>
            </body>
            </html>
    
    `, {
            contentType: "text/html",
            runScripts: "dangerously",
            resources: "usable",
            url: `file://${__dirname}/index.html`
        });

        let window = dom.window;

        (<any>global).dom = dom;
        (<any>global).window = window;
        (<any>global).body = window.document.body;
        (<any>global).document = window.document;
        (<any>global).navigator = {
            language: "en-En"
        };


    });

    this.afterEach(function () {

    });

    it('basic init', function () {
        let probe1 = new DomQuery(window.document.body);
        let probe2 = DomQuery.querySelectorAll("div");
        let probe3 = new DomQuery(probe1, probe2);
        let probe4 = new DomQuery(window.document.body, probe3);

        expect(probe1.length).to.be.eq(1);
        expect(probe2.length == 4).to.be.true;
        expect(probe3.length == 5).to.be.true;
        //still under discussion (we might index to avoid doubles)
        expect(probe4.length == 6).to.be.true;
    });

    it('proper iterator api and rxjs mapping', function () {
        let probe1 = new DomQuery(window.document.body);
        let probe2 = DomQuery.querySelectorAll("div");

        let o1 = from(probe1.stream);
        let o2 = from(probe2.stream);

        let cnt1 = 0;
        let isDQuery = false;
        let cnt2 = 0;

        o1.subscribe(() => {
            cnt1++;
        });

        o2.subscribe((item: any) => {
            cnt2++;
            isDQuery = (item.length == 1) && (item instanceof DomQuery)
        })

        expect(probe1.length).to.be.eq(1);
        expect(probe2.length == 4).to.be.true;
        expect(isDQuery).to.be.true;

    });

    it('proper iterator api and rxjs mapping with observable', function () {
        let probe1 = new DomQuery(window.document.body);
        let probe2 = DomQuery.querySelectorAll("div");

        let o1 = from(probe1.stream);
        let o2 = from(probe2.stream);

        let cnt1 = 0;
        let isDQuery = false;
        let cnt2 = 0;

        o1.subscribe(() => {
            cnt1++;
        });

        o2.subscribe((item: any) => {
            cnt2++;
            isDQuery = (item.length == 1) && (item instanceof DomQuery)
        })

        expect(probe1.length).to.be.eq(1);
        expect(probe2.length == 4).to.be.true;
        expect(isDQuery).to.be.true;

    });

    it('domquery ops test filter', function () {
        let probe2 = DomQuery.querySelectorAll("div");
        probe2 = probe2.filter((item: DomQuery) => item.id.match((id) => id != "id_1"));
        expect(probe2.length == 3);
    });

    it('global eval test', function () {
        let probe2 = DomQuery.querySelectorAll("div");
        probe2 = probe2.filter((item: DomQuery) => item.id.match((id) => id != "id_1"));
        expect(probe2.length == 3);
    });

    it('must detach', function () {
        let probe2 = DomQuery.querySelectorAll("div#id_1");
        probe2.detach();

        expect(DomQuery.querySelectorAll("div#id_1").isPresent()).to.be.false;
        probe2.appendTo(DomQuery.querySelectorAll("body"));
        expect(DomQuery.querySelectorAll("div#id_1").isPresent()).to.be.true;
    });

    it('domquery ops test2 each', () => {
        let probe2 = DomQuery.querySelectorAll("div#id_1");

        DomQuery.globalEval("document.getElementById('id_1').innerHTML = 'hello'");
        expect(probe2.html().value).to.eq("hello");
        expect(DomQuery.byId(document.head).innerHTML.indexOf("document.getElementById('id_1').innerHTML = 'hello'")).to.eq(-1);

        DomQuery.globalEval("document.getElementById('id_1').innerHTML = 'hello2'", "nonci");
        expect(probe2.html().value).to.eq("hello2");
    });


    it('domquery ops test2 with sticky eval code', () => {
        let probe2 = DomQuery.querySelectorAll("div#id_1");

        DomQuery.globalEvalSticky("document.getElementById('id_1').innerHTML = 'hello'");
        expect(probe2.html().value).to.eq("hello");
        expect(DomQuery.byId(document.head).innerHTML.indexOf("document.getElementById('id_1').innerHTML = 'hello'")).not.to.eq(-1);

        DomQuery.globalEvalSticky("document.getElementById('id_1').innerHTML = 'hello2'", "nonci");
        expect(probe2.html().value).to.eq("hello2");
        expect(DomQuery.byId(document.head).innerHTML.indexOf("document.getElementById('id_1').innerHTML = 'hello2'")).not.to.eq(-1);
    });


    it('domquery ops test2 eachNode', function () {
        let probe2 = DomQuery.querySelectorAll("div");
        let noIter = 0;
        probe2.each((item, cnt) => {
            expect(item instanceof DomQuery).to.be.true;
            expect(noIter == cnt).to.be.true;
            noIter++;
        });
        expect(noIter == 4).to.be.true;
    });

    it('domquery ops test2 byId', function () {
        let probe2 = DomQuery.byId("id_1");
        expect(probe2.length == 1).to.be.true;
        probe2 = DomQuery.byTagName("div");
        expect(probe2.length == 4).to.be.true;
    });

    it('outerhtml and eval tests', function () {
        let probe1 = new DomQuery(window.document.body);
        probe1.querySelectorAll("#id_1").outerHTML(`
            <div id='barg'>
            
            </div>
            <script type="text/javascript">
                document.getElementById('blarg').innerHTML = 'hello world';
            </script>
            `, true, true);
        expect(window.document.body.innerHTML.indexOf("hello world") != -1).to.be.true;
        expect(window.document.head.innerHTML.indexOf("hello world") == -1).to.be.true;
        expect(window.document.body.innerHTML.indexOf("id_1") == -1).to.be.true;
        expect(window.document.body.innerHTML.indexOf("blarg") != -1).to.be.true;
    });

    it('attr test and eval tests', function () {

        let probe1 = new DomQuery(document);
        probe1.querySelectorAll("div#id_2").attr("style").value = "border=1;";
        let blarg = probe1.querySelectorAll("div#id_2").attr("booga").value;
        let style = probe1.querySelectorAll("div#id_2").attr("style").value;
        let nonexistent = probe1.querySelectorAll("div#id_2").attr("buhaha").value;

        expect(blarg).to.be.eq("blarg");
        expect(style).to.be.eq("border=1;");
        expect(nonexistent).to.be.eq(null);


    });

    it('style must work ', function () {
        let probe1 = new DomQuery(document);
        let probe = probe1.querySelectorAll("div#id_2");
        probe.style("border").value = "10px solid red";
        probe.style("color").value = "blue";
        let styleNodeLevel = (probe.getAsElem(0).value as HTMLElement).style['color'];
        expect(probe.style("border").value).to.eq("10px solid red")
        expect(probe.style("color").value).to.eq("blue");
        expect(styleNodeLevel).to.eq('blue');

    });

    it('must perform addClass and hasClass correctly', function () {
        let probe1 = new DomQuery(document);
        let element = probe1.querySelectorAll("div#id_2");
        element.addClass("booga").addClass("Booga2");

        let classdef = element.attr("class").value;
        expect(classdef).to.eq("blarg2 booga Booga2");

        element.removeClass("booga2")
        expect(element.hasClass("booga2")).to.be.false;
        expect(element.hasClass("booga")).to.be.true;

    });

    it('must perform addClass and hasClass correctly 2', function () {
        let probe1 = new DomQuery(document);
        let element = probe1.querySelectorAll(".blarg2");


        element.addClass("booga").addClass("Booga2");

        let classdef = element.attr("class").value;
        expect(classdef).to.eq("blarg2 booga Booga2");

        element.removeClass("booga2")
        expect(element.hasClass("booga2")).to.be.false;
        expect(element.hasClass("booga")).to.be.true;
        expect(element.hasClass("blarg2")).to.be.true;

    });

    it('must perform addClass and hasClass correctly 2', function () {
        let probe1 = new DomQuery(document);
        let element = probe1.querySelectorAll(".blarg2");


        element.addClass("booga").addClass("Booga2");


        expect(probe1.querySelectorAll(".Booga2").length).eq(2);
    });

    it('must perform insert before and insert after correctly', function () {
        let probe1 = new DomQuery(document).querySelectorAll("#id_2");
        let insert = DomQuery.fromMarkup("<div id='insertedBefore'></div><div id='insertedBefore2'></div>")
        let insert2 = DomQuery.fromMarkup("<div id='insertedAfter'></div><div id='insertedAfter2'></div>")

        probe1.insertBefore(insert);
        probe1.insertAfter(insert2);

        expect(DomQuery.querySelectorAll("#insertedBefore").isPresent()).to.be.true;
        expect(DomQuery.querySelectorAll("#insertedBefore2").isPresent()).to.be.true;
        expect(DomQuery.querySelectorAll("#id_2").isPresent()).to.be.true;
        expect(DomQuery.querySelectorAll("#insertedAfter").isPresent()).to.be.true;
        expect(DomQuery.querySelectorAll("#insertedAfter2").isPresent()).to.be.true;
    });

    it('it must stream', function () {
        let probe1 = new DomQuery(document).querySelectorAll("div");
        let coll: Array<any> = probe1.stream.collect(new ArrayCollector());
        expect(coll.length == 4).to.be.true;

        coll = probe1.lazyStream.collect(new ArrayCollector());
        expect(coll.length == 4).to.be.true;

    });

    it('it must stream to a domquery', function () {
        let probe1 = new DomQuery(document).querySelectorAll("div");
        let coll: DomQuery = probe1.stream.collect(new DomQueryCollector());
        expect(coll.length == 4).to.be.true;

        coll = probe1.lazyStream.collect(new DomQueryCollector());
        expect(coll.length == 4).to.be.true;
    });

    it('it must have parents', function () {
        let probe1 = new DomQuery(document).querySelectorAll("div");
        let coll: Array<any> = probe1.firstParent("body").stream.collect(new ArrayCollector());
        expect(coll.length == 1).to.be.true;

    });

    it("must have a working insertBefore and insertAfter", function () {
        let probe1 = new DomQuery(document).byId("id_2");
        probe1.insertBefore(DomQuery.fromMarkup(` <div id="id_x_0"></div><div id="id_x_1"></div>`));
        probe1.insertAfter(DomQuery.fromMarkup(` <div id="id_x_0_1"></div><div id="id_x_1_1"></div>`));

        expect(DomQuery.querySelectorAll("div").length).to.eq(8);
        DomQuery.querySelectorAll("body").innerHTML = trim(DomQuery.querySelectorAll("body").innerHTML.replace(/>\s*</gi, "><"));
        expect(DomQuery.querySelectorAll("body").childNodes.length).to.eq(8);

        let innerHtml = DomQuery.querySelectorAll("body").innerHTML;
        expect(innerHtml.indexOf("id_x_0") < innerHtml.indexOf("id_x_1")).to.be.true;
        expect(innerHtml.indexOf("id_x_0") < innerHtml.indexOf("id_2")).to.be.true;
        expect(innerHtml.indexOf("id_x_0") > 0).to.be.true;

        expect(innerHtml.indexOf("id_x_0_1") > innerHtml.indexOf("id_2")).to.be.true;
        expect(innerHtml.indexOf("id_x_1_1") > innerHtml.indexOf("id_x_0_1")).to.be.true;
    })

    it("must have a working input handling", function () {
        DomQuery.querySelectorAll("body").innerHTML = `<form id="blarg">
    <div id="embed1">
        <input type="text" id="id_1" name="id_1" value="id_1_val">
        <input type="text" id="id_2" name="id_2" value="id_2_val" disabled="disabled">
        <textarea id="id_3" name="id_3">textareaVal</textarea>

        <fieldset>
            <input type="radio" id="mc" name="cc_1" value="Mastercard" checked="checked">
            <label for="mc"> Mastercard</label>
            <input type="radio" id="vi" name="cc_1" value="Visa">
            <label for="vi"> Visa</label>
            <input type="radio" id="ae" name="cc_1" value="AmericanExpress">
            <label for="ae"> American Express</label>
        </fieldset>
        <select id="val_5" name="val_5" size="5">
            <option>barg</option>
            <option>jjj</option>
            <option selected>akaka</option>
            <option>blon</option>
            <option>slashs</option>
        </select>
    </div>
</form>
       `;

        let length = DomQuery.querySelectorAll("form").elements.length;
        expect(length == 8).to.be.true;
        let length1 = DomQuery.querySelectorAll("body").elements.length;
        expect(length1 == 8).to.be.true;
        let length2 = DomQuery.byId("embed1").elements.length;
        expect(length2 == 8).to.be.true;

        let count = DomQuery.byId("embed1").elements
            .stream.map<number>(item => item.disabled ? 1 : 0)
            .reduce((val1, val2) => val1 + val2, 0);
        expect(count.value).to.eq(1);

        DomQuery.byId("embed1").elements
            .stream.filter(item => item.disabled)
            .each(item => item.disabled = false);

        count = DomQuery.byId("embed1").elements
            .stream.map<number>(item => item.disabled ? 1 : 0)
            .reduce((val1, val2) => val1 + val2, 0);
        expect(count.value).to.eq(0);

        count = DomQuery.byId("embed1").elements
            .stream.map<number>(item => item.attr("checked").isPresent() ? 1 : 0)
            .reduce((val1, val2) => val1 + val2, 0);
        expect(count.value).to.eq(1);

        expect(DomQuery.byId("id_1").inputValue.value == "id_1_val").to.be.true;
        DomQuery.byId("id_1").inputValue.value = "booga";
        expect(DomQuery.byId("id_1").inputValue.value == "booga").to.be.true;

        expect(DomQuery.byId("id_3").inputValue.value).to.eq("textareaVal");

        DomQuery.byId("id_3").inputValue.value = "hello world";
        expect(DomQuery.byId("id_3").inputValue.value).to.eq("hello world");

        let cfg = DomQuery.querySelectorAll("form").elements.encodeFormElement();
        expect(cfg.getIf("id_1").value[0]).to.eq("booga");
        expect(cfg.getIf("id_2").value[0]).to.eq("id_2_val");
        expect(cfg.getIf("id_3").value[0]).to.eq("hello world");
        expect(cfg.getIf("cc_1").value[0]).to.eq("Mastercard");
        expect(cfg.getIf("val_5").value[0]).to.eq("akaka");
    })

    it("must have a proper loadScriptEval execution", function (done) {

        DomQuery.byTagName("body").loadScriptEval("./fixtures/test.js");
        setTimeout(() => {
            expect(DomQuery.byId("id_1").innerHTML == "hello world").to.be.true;
            done();
        }, 100)

    });

    it("must have first etc working", function () {
        expect(DomQuery.querySelectorAll("div").first().id.value).to.eq("id_1");
    });

    it("runscript runcss", function (done) {
        DomQuery.byTagName("body").innerHTML = `
            <div id="first"></div>
            <div id="second"></div>
            <div id="third"></div>
            <div id="fourth"></div>
            
            <script type="text/javascript">
                document.getElementById("first").innerHTML = "hello world";
            </script>
            <script type="text/javascript">
            //<![CDATA[
                document.getElementById("second").innerHTML = "hello world";
            //]]>    
            </script>
            <script type="text/javascript">
            <!--
                document.getElementById("third").innerHTML = "hello world";
            //-->   
            </script>
              <script type="text/javascript">
            //<!--
                document.getElementById("fourth").innerHTML = "hello world";
            //-->   
            </script>
        
            <style>
                #first {
                    border: 1px solid black;
                }
            </style>
        `;
        let content = DomQuery.byTagName("body").runScripts().runCss();
        expect(content.byId("first").innerHTML).to.eq("hello world");
        expect(content.byId("second").innerHTML).to.eq("hello world");
        expect(content.byId("third").innerHTML).to.eq("hello world");
        expect(content.byId("fourth").innerHTML).to.eq("hello world");
        done();

    });

    //TODO defer does not work in jsdom
    it("must have a proper loadScriptEval deferred", function (done) {
        DomQuery.byId(document.body).loadScriptEval("./fixtures/test2.js", 200);
        setTimeout(() => {
            expect(DomQuery.byId("id_1").innerHTML == "hello world").to.be.false;
        }, 100)


        setTimeout(() => {
            expect(DomQuery.byId("id_1").innerHTML == "hello world").to.be.true;
            done();
        }, 1500)
    })

    it("it must handle events properly", function () {
        let clicked = 0;
        let listener = () => {
            clicked++;
        };
        let eventReceiver = DomQuery.byId("id_1");
        eventReceiver.addEventListener("click", listener);
        eventReceiver.click();

        expect(clicked).to.eq(1);

        eventReceiver.removeEventListener("click", listener);
        eventReceiver.click();

        expect(clicked).to.eq(1);

    });

    it("it must handle innerText properly", function () {

        //jsdom bug
        Object.defineProperty(Object.prototype, 'innerText', {
            get() {
                return this.textContent;
            },
        });

        let probe = DomQuery.byId("id_1");
        probe.innerHTML = "<div>hello</div><div>world</div>";
        expect(probe.innerText()).to.eq("helloworld");
    });
    it("it must handle textContent properly", function () {
        let probe = DomQuery.byId("id_1");
        probe.innerHTML = "<div>hello</div><div>world</div>";
        expect(probe.textContent()).to.eq("helloworld");
    });

    it("it must handle iterations properly", function () {
        let probe = DomQuery.byTagName("div");
        let resArr = probe.lazyStream.collect(new ArrayCollector());
        expect(resArr.length).to.eq(4);

        probe.reset();
        while (probe.hasNext()) {
            let el = probe.next();
            expect(el.tagName.value.toLowerCase()).to.eq("div");
        }
        expect(probe.next()).to.eq(null);
        let probe2 = DomQuery.byTagName("div").limits(2);
        resArr = LazyStream.ofStreamDataSource(<any>probe2).collect(new ArrayCollector());
        expect(resArr.length).to.eq(2);
    });

    it("it must handle subnodes properly", function () {
        let probe = DomQuery.byTagName("div");
        expect(probe.subNodes(1, 3).length).to.eq(2);
        probe = DomQuery.byTagName("body").childNodes.subNodes(0, 2);
        expect(probe.length).to.eq(2);

        probe = DomQuery.byTagName("div").subNodes(2);
        expect(probe.length).to.eq(2);
    })


    it("it must ensure shadow dom creation works properly", function () {
        let probe = DomQuery.byTagName("div");
        try {
            //probably not testable atm, mocha does not have shadow dom support
            //we might be able to shim it in one way or the other
            let element = probe.attachShadow();
            expect(element.length > 0).to.eq(true);
        } catch (e) {
            //not supported we still need to get an error here
            expect(e.message.indexOf("not supported") != -1).to.be.true;
        }
    })


    it('it must have a working wait for dom with mut observer and must detect condition after change', async function () {
        let probe = DomQuery.byId('id_1');
        probe.innerHTML = 'true';
        let ret = await probe.waitUntilDom((element) => element.innerHTML.indexOf('true') != -1);
        expect(ret.isPresent());
        probe = DomQuery.byId('bosushsdhs');
        ret = await probe.waitUntilDom((element) => element.isAbsent());
        expect(ret.isAbsent());

    });

    it('it must have a working wait for dom with mut observer', async function () {
        let probe = DomQuery.byId('id_1');
        setTimeout(() => probe.innerHTML = 'true', 300);
        let ret = await probe.waitUntilDom((element) => element.innerHTML.indexOf('true') != -1);
        delete window.MutationObserver;
        delete global.MutationObserver;
        probe.innerHTML = "";
        setTimeout(() => probe.innerHTML = 'true', 300);
        let ret2 = await probe.waitUntilDom((element) => element.innerHTML.indexOf('true') != -1);
        expect(ret.isPresent() && ret2.isPresent());
    });

    it('it must have a timeout', async function () {
        let probe = DomQuery.byId('booga');
        try {
            setTimeout(() => probe.innerHTML = 'true', 300);
            await probe.waitUntilDom((element) => element.innerHTML.indexOf('true') != -1);
            expect.fail("must have a timeout");
        } catch (ex) {
            expect(!!ex);
        }
        try {
            delete window.MutationObserver;
            delete global.MutationObserver;
            probe.innerHTML = "";
            setTimeout(() => probe.innerHTML = 'true', 300);
            await probe.waitUntilDom((element) => element.innerHTML.indexOf('true') != -1);
            expect.fail("must have a timeout");
        } catch (ex2) {
            expect(!!ex2);
        }
    });
    it('must handle null inputs correctly', function () {
        const dq = new DomQuery(null);
        expect(dq.isAbsent()).to.eq(true);
    })

    it('concat must work as expected resulting', function () {
        let probe = DomQuery.querySelectorAll("div");
        let probe2 = DomQuery.querySelectorAll("body");
        let result = probe.concat(probe2);
        expect(result.length).to.eq(probe.length + probe2.length);

        //lets now check for filter double
        probe2 = DomQuery.querySelectorAll('div');
        result = probe.concat(probe2);
        expect(result.length).to.eq(probe.length);
    })

    it('must handle match correctly', function () {
        let probe = DomQuery.querySelectorAll("div").first();
        let probe2 = DomQuery.querySelectorAll("body").first();

        expect(probe.matchesSelector("div")).to.eq(true);
        expect(probe2.matchesSelector("body")).to.eq(true);
        expect(probe2.matchesSelector("div")).to.eq(false);
    })

    it('must by recycleable', function () {
        let probe = DomQuery.querySelectorAll("div");

        let res1 = probe.filter(item => item.matchesSelector("div"));
        expect(res1.length).to.eq(4);
        let res2 = probe.filter(item => item.matchesSelector("div"));
        expect(res2.length).to.eq(4);
    })

    it('delete must work', function () {
        let probe = DomQuery.querySelectorAll("body");
        let probe2 = DomQuery.fromMarkup("<div id='deleteprobe1'>snafu</div>");

        probe2.appendTo(probe);

        expect(probe.querySelectorAll("#deleteprobe1").isPresent()).to.eq(true);
        probe2.delete();
        expect(probe.querySelectorAll("#deleteprobe1").isAbsent()).to.eq(true);
    })
    it('must work with rxjs and domquery', function () {
        let probe = DomQuery.querySelectorAll("div");
        let probe2 = DomQuery.querySelectorAll("div");
        let probeCnt = 0;
        let probe2Cnt = 0;
        from(probe).subscribe(() => probeCnt++);
        from(probe2.stream).subscribe(() => probe2Cnt++);
        expect(probeCnt).to.be.above(0);
        expect(probeCnt).to.eq(probe2Cnt);
    })
});
