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
let formatHref = (href) => {
    return href.substring(href.lastIndexOf('/'));
}

let resultData = JSON.parse(sessionStorage.getItem("_jasmine_log__") || "[]");

let failures = resultData.filter(item => 'undefined' != typeof item.status && item.status != 'passed');

let failureHolder = DQ$("#failures");
if (!resultData.length) {
    DomQuery.fromMarkup(`<h2> No test results found, please rerun the tests </h2>`).appendTo(failureHolder);
} else if (!failures.length) {
    DomQuery.fromMarkup(`<h2> All Tests have passed </h2>`).appendTo(failureHolder);
} else {
    let content = DomQuery.fromMarkup(`<h2> ${failures.length} Tests have failed </h2>`);
    let details = DomQuery.fromMarkup(`<ul></ul>`);
    content.appendTo(failureHolder);
    details.appendTo(failureHolder);

    failures.forEach(failure => {
        DomQuery.fromMarkup(`<li> <b style='color: darkred;'>${formatHref(failure.from)}</b>: ${failure.message}</li>`).appendTo(details);
    })
}





