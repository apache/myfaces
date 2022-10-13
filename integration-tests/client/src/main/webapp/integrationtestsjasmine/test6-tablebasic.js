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
        myfaces.testcases.redirect("./test7-eventtest.jsf");
    }, 1000);
});
describe("Basic DOM Table Operation Tests utilizing the JSF protocol", function () {

    beforeEach(function () {
        myfaces.testcases.ajaxCnt = 0;
        //we reset the table to its original state
    });

    it("Replaces the head in the table", function (done) {

        emitPPR("replace_head", null, "table_replace_head", "form2").then(function () {
            setTimeout(function () {

                let headCol1 = DQ$("#head_col1").innerHTML;
                let headCol2 = DQ$("#head_col2").innerHTML;
                expect(headCol1.indexOf("replaced")).not.toBe(-1);  //headcol1 replaced
                expect(headCol1.indexOf("evaled")).not.toBe(-1);    //headcol1 auto evaled
                expect(headCol2.indexOf("replaced")).not.toBe(-1);  //headcol2 replaced
                done();
            }, 500)
        });


    });

    it("Replaces the body in the table", function (done) {

        emitPPR("replace_body", null, "table_replace_body", "form2").then(function () {
            setTimeout(function () {
                let col1 = DQ$("#body_row1_col1").innerHTML;
                let col2 = DQ$("#body_row1_col2").innerHTML;
                expect(col1.indexOf("replaced")).not.toBe(-1);//body col1 replaced
                expect(col1.indexOf("evaled")).not.toBe(-1);  //body col1 auto evaled
                expect(col2.indexOf("replaced")).not.toBe(-1);//body col2 replaced
                done();
            }, 500);
        });

    });

    it("Inserts rows in head", function (done) {

        emitPPR("insert_row_head", null, "table_insert_row_head", "form2").then(function () {
            setTimeout(function () {
                expect(DQ$("#table1 thead tr").length >= 3).toBeTruthy()    //three rows now in head
                expect(DQ$("#table1 thead tr").first().hasClass("insert_before")).toBeTruthy();   //first element must be insert before
                expect(DQ$("#table1 thead tr").last().hasClass("insert_after")).toBeTruthy();   //last element must be insert after"
                done();
            }, 500)
        });

    });

    it("Insert Rows in body", function (done) {

        emitPPR("insert_row_body", null, "table_insert_row_body", "form2").then(function () {
            setTimeout(function () {
                expect(DQ$("#table1 tbody tr").length >= 3).toBeTruthy();                       //three rows now in body
                expect(DQ$("#table1 tbody tr").first().hasClass("insert_before")).toBeTruthy(); //first element must be insert before
                expect(DQ$("#table1 tbody tr").last().hasClass("insert_after")).toBeTruthy();   //last element must be insert after
                done();
            }, 100);
        });

    });
    it("Insert Column in head", function (done) {
        emitPPR("insert_column_head", null, "table_insert_column_head", "form2").then(function () {
            setTimeout(function () {
                expect(DQ$("#table1 #head_row1 td").length >= 6).toBeTruthy(); //six columns in head or more
                expect(DQ$("#table1 #head_row1 td").first().innerHTML.indexOf("inserted before")).not.toBe(-1);//first element must be insert before
                expect(DQ$("#table1 #head_row1 td").last().innerHTML.indexOf("inserted after")).not.toBe(-1);//last element must be insert after
                done();
            }, 500);
        });
    });

    it("Insert Column in body", function (done) {

        emitPPR("insert_column_body", null, "table_insert_column_body", "form2").then(function () {
            setTimeout(function () {
                expect(DQ$("#table1 #body_row1 td").length >= 6).toBeTruthy(); //six columns in body or more
                expect(DQ$("#table1 #body_row1 td").first().innerHTML.indexOf("inserted before")).not.toBe(-1);//first element must be insert before
                expect(DQ$("#table1 #body_row1 td").last().innerHTML.indexOf("inserted after")).not.toBe(-1);//last element must be insert after
                done();
            }, 500);
        });
    });
    it("Inserts a second body", function (done) {

        emitPPR("insert_body", null, "table_insert_body", "form2").then(function () {
            setTimeout(function () {
                expect(DQ$("#table1 tbody").length >= 2).toBeTruthy();
                done();
            }, 500);
        });
    });
    //TODO footer test
});