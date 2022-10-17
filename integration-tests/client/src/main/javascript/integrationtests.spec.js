/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * Lang.work for additional information regarding copyright ownership.
 * The ASF licenses Lang.file to you under the Apache License, Version 2.0
 * (the "License"); you may not use Lang.file except in compliance with
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
 * This is the integration test client. We basically run a mocha/puppeteer layer against a
 * running faces server, which serves the test pages.
 * (puppeteer is basically an embedded chromium with a thin Ecmascript layer on top, mocha
 * is used to collect the test data and run the tests in a well specified testing framework
 * for post test analysis and build integration)
 */
const puppeteer = require('puppeteer');
const {expect} = require('chai');

const filteredGlobal =
    {
        browser: global.browser,
        expect: global.expect,
        window: global.window
    }

// puppeteer options
const TEST_TIMEOUT = 120000;
/*
maximum time a test can run until it hits timeout, we set it deliberately high
sometimes chromium takes a while to trigger (30 seconds on my machine every here and then)
*/
const BROWSER_OPTIONS = {
    headless: true,
    timeout: TEST_TIMEOUT
};

const RESULT_TIMEOUT = 10000; /*maximum time until the test result can ba analyzed*/

async function runStandardPage(pageIndex) {
    this.timeout(TEST_TIMEOUT);
    const page = await browser.newPage();
    console.log(`http://localhost:8080/IntegrationJSTest/integrationtestsjasmine/${pageIndex}.jsf`)
    await page.goto(`http://localhost:8080/IntegrationJSTest/integrationtestsjasmine/${pageIndex}.jsf`);
    await page.waitForFunction("document.body.querySelector('.jasmine-overall-result') != null && !!document.body.querySelector('.jasmine-overall-result').innerText.length", {
        timeout: RESULT_TIMEOUT
    });
    //note this is an isolated scope, the function is passed to the chrome process
    //we can only send serializable data back for analysis
    let pageEvalResult = await page.$eval('body', element => {
        let innerText = element.querySelector(".jasmine-overall-result").innerText;
        let failures = element.querySelectorAll(".jasmine-failures a");
        let results = element.querySelectorAll(".jasmine-summary a");
        return {
            innerText: innerText,
            errorLog: Array.from(failures).map(item => item.innerText),
            results: Array.from(results).map(item => item.innerText)
        };
    });
    if(pageEvalResult.results.length) {
        console.debug(`Page ${pageIndex} Successes:`);
        pageEvalResult.results.forEach(item => console.log("=> "+item));
    }
    if(pageEvalResult.errorLog.length) {
        console.debug(`Page ${pageIndex} Failures:`)
        pageEvalResult.errorLog.forEach(item => console.log("=> "+item));
    }
    return pageEvalResult;
}

// expose variables
before(async function () {
    this.timeout(TEST_TIMEOUT);
    global.expect = expect;
    global.browser = await puppeteer.launch(BROWSER_OPTIONS);
});


describe('Integration Testsuite MyFaces', function () {

    function loadPage() {
        let page_;
        return browser.newPage().then(page => {
            page_ = page;
            return page.goto('http://localhost:8080/IntegrationJSTest/integrationtestsjasmine/test1-protocol.jsf')
        }).then(resp => {
            return page_;
        })
    }

    it('testing protocol', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test1-protocol");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('testing viewRoot', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test2-viewroot");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('testing viewBody', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test3-viewbody");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('testing chain', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test4-chain");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('testing view root replacement 2', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test5-viewroot2");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('testing basic table replacement', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test6-tablebasic");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('testing event integration', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test7-eventtest");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('testing ajax navigation', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test8-navcase1");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('complex spreadsheet test', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test9-spreadsheet");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('double evaluation of embedded scripts testcase', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test10-doubleeval");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('various types of script blocks test', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test11-scriptblocks");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('api decoration tests', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test12-apidecoration");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('css replacement methods test', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test13-cssreplacementhead");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('css replacement methods test', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test13-cssreplacementhead");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('multiform test', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test14-multiform");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('delay test', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test15-jsf22delay");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    /* not ported yet
    it('file upload test', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test16-fileupload");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    */

    it('response with out source corner condition', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test17-responseonly");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('test for proper event handler location callbacks', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test18-eventlocations");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('execute parameter test', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test19-execute");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('form field combinations test', async function () {
        const pageEvalResult = await runStandardPage.call(this, "test20-formfields");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null).to.be.true;
    });
    it('must run all possible nonce conditions correctly', async function () {
        // first condition nonce fail on script src
        let pageEvalResult = await runStandardPage.call(this, "test21-nonce");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null, 'none fail on src').to.be.true;
        // nonce pass on script src
        pageEvalResult = await runStandardPage.call(this, "test22-nonce");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null, 'nonce pass on src').to.be.true;
        // nonce fail on embedded script
        pageEvalResult = await runStandardPage.call(this, "test23-nonce");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null, 'nonce fail on embedded script').to.be.true;
        // nonce pass on embedded script
        pageEvalResult = await runStandardPage.call(this, "test24-nonce");
        expect(pageEvalResult.innerText.match(/0\s*failures/gi) != null, 'nonce pass on embedded script').to.be.true;
    });

});

// close browser and reset global variables
after(function () {
    global.browser.close();
    global.browser = filteredGlobal.browser;
    global.expect = filteredGlobal.expect;
});

//booga