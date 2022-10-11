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

afterEach(function () {
    setTimeout(function () {
        myfaces.testcases.redirect("./test10-doubleeval.jsf");
    }, 1000);
});
describe("Spreadsheet test for the replacement of table elements", function () {
    it("Runs the spreadsheet test", function (done) {
        let start;
        let promises =  [];
        for (let cnt = 0; cnt < 100; cnt++) {
            //first we set 100 values
            let currentField1 = "#testTable2\\"+faces.separatorchar + cnt + "\\"+faces.separatorchar+"input1";
            let currentField2 = "#testTable2\\"+faces.separatorchar+ cnt + "\\"+faces.separatorchar+"input2";
            let currentOutput2 = "testTable2"+faces.separatorchar + cnt + faces.separatorchar+"field1";
            let currentOutput1 = "testTable2"+faces.separatorchar + cnt + faces.separatorchar+"field2";
            let origin = "testTable2"+faces.separatorchar + cnt + faces.separatorchar+"submitall";

            $(currentField1).val("value1:" + cnt);
            $(currentField2).val("value2:" + cnt);
            //secondly we issue 100 requests
            currentField2 = currentField2.replace("#", "").replace(/\\/g, "");
            currentField1 = currentField1.replace("#", "").replace(/\\/g, "");
            start = Date.now();
            promises.push(jsfAjaxRequestPromise(origin, null, {
                execute: currentField2 + " " + currentField1,
                render: currentOutput1 + " " + currentOutput2,
                'jakarta.faces.behavior.event': 'action'
            }));
        }
        Promise.all(promises).finally(function() {
            setTimeout(function () {

                for (let cnt = 0; cnt < 100; cnt++) {
                    let currentOutput1 = "#testTable2" + faces.separatorchar + cnt + faces.separatorchar + "field1";
                    let currentOutput2 = "#testTable2" + faces.separatorchar + cnt + faces.separatorchar + "field2";
                    let assert1 = $(currentOutput1.replace(/\:/g, "\\:")).html().indexOf("value1:" + cnt) != -1;
                    let assert2 = $(currentOutput2.replace(/\:/g, "\\:")).html().indexOf("value2:" + cnt) != -1;
                    expect( assert1 && assert2).toBeTruthy(); //field must have ajax content
                }
                console.log("Processing time last request:"+(Date.now() - 500 - start))
                done();
            }, 500);
        });
    });
});