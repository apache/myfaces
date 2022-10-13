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
        myfaces.testcases.redirect("./test5-viewroot2.jsf");
    }, 1000);
});
describe("Chain function suite", function () {
    it("Should process faces.util.chain properly", function () {
        //testfunc1 til 4 are defined in the html page
        faces.util.chain(document.getElementById("chaincall"), null, testFunc1, testFunc2, testFunc3, testFunc4);
        expect(DQ$("body").innerHTML.indexOf("test1 succeeded test2 succeededtest3 succeeded")).not.toBe(-1);
    });
});