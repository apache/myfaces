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
    myfaces.testcases.redirect("./test7-eventtest.jsf");
});
describe("Basic DOM Table Operation Tests utilizing the JSF protocol", function () {

    beforeEach(function () {
        myfaces.testcases.ajaxCnt = 0;
        // we reset the table to its original state
    });

    it("Replaces the head in the table", function (done) {

        emitPPR("replace_head", null, "table_replace_head", "form2").then(function () {
            DQ$("#table1").waitUntilDom(() => {
                let headCol1 = DQ$("#head_col1").innerHTML;
                let headCol2 = DQ$("#head_col2").innerHTML;
                return headCol1.indexOf("replaced") !== -1 &&
                    headCol1.indexOf("evaled") !== -1 &&
                    headCol2.indexOf("replaced") !== -1;
            }).then(() => success(done)).catch(done);


        });


    });

    it("Replaces the body in the table", function (done) {

        emitPPR("replace_body", null, "table_replace_body", "form2").then(function () {
            DQ$("#table1").waitUntilDom(() => {
                let col1 = DQ$("#body_row1_col1").innerHTML;
                let col2 = DQ$("#body_row1_col2").innerHTML;
                return col1.indexOf("replaced") !== -1 &&
                    col1.indexOf("evaled") !== -1 &&
                    col2.indexOf("replaced") !== -1;
            }).then(() => success(done)).catch(done);
        });
    });

    it("Inserts rows in head", function (done) {
        emitPPR("insert_row_head", null, "table_insert_row_head", "form2").then(function () {
            DQ$("#table1").waitUntilDom(() => {
                let rows = DQ$("#table1 thead tr");
                return rows.length >= 3 &&
                    rows.first().hasClass("insert_before") &&
                    rows.last().hasClass("insert_after");
            }).then(() => success(done)).catch(done);
        });
    });

    it("Insert Rows in body", function (done) {
        emitPPR("insert_row_body", null, "table_insert_row_body", "form2").then(function () {
            DQ$("#table1").waitUntilDom(() => {
                let rows = DQ$("#table1 tbody tr");
                return rows.length >= 3 &&
                    rows.first().hasClass("insert_before") &&
                    rows.last().hasClass("insert_after");
            }).then(() => success(done)).catch(done);
        });
    });

    it("Insert Column in head", function (done) {
        emitPPR("insert_column_head", null, "table_insert_column_head", "form2").then(function () {

            DQ$("#table1").waitUntilDom(() => {
                let cols = DQ$("#table1  #head_row1 td");
                return cols.length >= 6 &&
                    cols.first().innerHTML.indexOf("inserted before") !== -1 &&
                    cols.last().innerHTML.indexOf("inserted after") !== -1;
            }).then(() => success(done)).catch(done);
        });
    });

    it("Insert Column in body", function (done) {

        emitPPR("insert_column_body", null, "table_insert_column_body", "form2").then(function () {
            DQ$("#table1").waitUntilDom(() => {
                let cols = DQ$("#table1  #body_row1 td");
                return cols.length >= 6 &&
                    cols.first().innerHTML.indexOf("inserted before") !== -1 &&
                    cols.last().innerHTML.indexOf("inserted after") !== -1;
            }).then(() => success(done)).catch(done);
        });
    });
    it("Inserts a second body", function (done) {
        emitPPR("insert_body", null, "table_insert_body", "form2").then(function () {
            DQ$("#table1").waitUntilDom(() => {
                return DQ$("#table1 tbody").length >= 2;
            }).then(() => success(done)).catch(done);
        });
    });
});