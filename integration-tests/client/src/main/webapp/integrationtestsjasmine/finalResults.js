let formatHref = (href) => {
    return href.substring(href.lastIndexOf('/'));
}

let resultData = JSON.parse(sessionStorage.getItem("_jasmine_log__") || "[]");

let failures = resultData.filter(item => 'undefined' != typeof item.status && item.status != 'passed');

let failureHolder = DomQuery.byId("failures");
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





