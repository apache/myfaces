/**
 * initialize jasmine to perform the tests
 * once the testrunner is enabled we
 * basically just run the tests as includes
 * from the integration test pages
 * and collect the data on the server
 */
(function () {
    let jasmineEnv = jasmine.getEnv();
    jasmineEnv.configure({
        random: false,
        updateInterval: 250
    })
    /*some tests rely historically grown on the results of others*/

    jasmine.DEFAULT_TIMEOUT_INTERVAL = 20000;
    /**
     Create the `HTMLReporter`, which Jasmine calls to provide results of each spec and each suite. The Reporter is responsible for presenting results to the user.
     */
    //let consoleReporter = new jasmine.ConsoleReporter(jasmineEnv);
    //jasmineEnv.addReporter(consoleReporter);
    jasmineEnv.addReporter(jasmine.SessionReporter);
    /**
     Delegate filtering of specs to the reporter. Allows for clicking on single suites or specs in the results to only run a subset of the suite.
     */
    //jasmineEnv.specFilter = function (spec) {
    //    return htmlReporter.specFilter(spec);
    //};
    /**
     Run all of the tests when the page finishes loading - and make sure to run any previous `onload` handler
     ### Test Results
     Scroll down to see the results of all of these specs.
     */
    $('.version').html(jasmine.version || (jasmine.getEnv().versionString && jasmine.getEnv().versionString()));




})();