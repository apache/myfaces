/**
 * jasmine reporter for the console
 */
(function() {
    if (! jasmine) {
        throw new Exception("jasmine library does not exist in global namespace!");
    }

    function logToSession(value, status) {
        let log = JSON.parse(sessionStorage.getItem("_jasmine_log__") || "[]");

        log.push({
            from: window.location.href,
            message: value,
            status: status
        });
        sessionStorage.setItem("_jasmine_log__", JSON.stringify(log));
    }

    const sessionReporter = {
        jasmineStarted: function(suiteInfo) {
            let log = JSON.parse(sessionStorage.getItem("_jasmine_log__") || "[]");
            if(window.location.href.indexOf("test1-protocol.jsf") != -1) {
                log.length = 0;
            }
            log = log.filter(item => item.from != window.location.href);
            sessionStorage.setItem("_jasmine_log__", JSON.stringify(log));
            logToSession('Running suite with ' + suiteInfo.totalSpecsDefined);
        },

        suiteStarted: function(result) {
            logToSession('Suite started: ' + result.description
                + ' whose full description is: ' + result.fullName);
        },

        specStarted: async function(result) {
            //await somethingAsync();
            logToSession('Spec started: ' + result.description
                + ' whose full description is: ' + result.fullName);
        },

        specDone: function(result) {
            logToSession('Spec: ' + result.description + ' was ' + result.status, result.status);

            for (const expectation of result.failedExpectations) {
                logToSession('Failure: ' + expectation.message);
                logToSession(expectation.stack);
            }

            logToSession(result.passedExpectations.length);
        },

        suiteDone: function(result) {
            logToSession('Suite: ' + result.description + ' was ' + result.status, result.status);
            for (const expectation of result.failedExpectations) {
                logToSession('Suite ' + expectation.message);
                logToSession(expectation.stack);
            }
        },

        jasmineDone: function(result) {
            logToSession('Finished suite: ' + result.overallStatus);
            for (const expectation of result.failedExpectations) {
                logToSession('Global ' + expectation.message);
                logToSession(expectation.stack);
            }
        }
    };


/**
* Basic reporter that outputs spec results to the browser console.
* Useful if you need to test an html page and don't want the TrivialReporter
* markup mucking things up.
*
* Usage:
*
* jasmine.getEnv().addReporter(new jasmine.ConsoleReporter());
* jasmine.getEnv().execute();
*/
    let ConsoleReporter = function() {
        this.started = false;
        this.finished = false;
    };

    ConsoleReporter.prototype = {
        reportRunnerResults: function(runner) {
            if (this.hasGroupedConsole()) {
                let suites = runner.suites();
                startGroup(runner.results(), 'tests');
                for (let i=0; i<suites.length; i++) {
                    if (!suites[i].parentSuite) {
                        suiteResults(suites[i]);
                    }
                }
                console.groupEnd();
            }
            else {
                let dur = (new Date()).getTime() - this.start_time;
                let failed = this.executed_specs - this.passed_specs;
                let spec_str = this.executed_specs + (this.executed_specs === 1 ? " spec, " : " specs, ");
                let fail_str = failed + (failed === 1 ? " failure in " : " failures in ");

                this.log("Runner Finished.");
                this.log(spec_str + fail_str + (dur/1000) + "s.");
            }
            this.finished = true;
        },

        hasGroupedConsole: function() {
            let console = jasmine.getGlobal().console;
            return console && console.info && console.warn && console.group && console.groupEnd && console.groupCollapsed;
        },

        reportRunnerStarting: function(runner) {
            this.started = true;
            if (!this.hasGroupedConsole()) {
                this.start_time = (new Date()).getTime();
                this.executed_specs = 0;
                this.passed_specs = 0;
                this.log("Runner Started.");
            }
        },

        reportSpecResults: function(spec) {
            if (!this.hasGroupedConsole()) {
                let resultText = "Failed.";

                if (spec.results().passed()) {
                    this.passed_specs++;
                    resultText = "Passed.";
                }

                this.log(resultText);
            }
        },

        reportSpecStarting: function(spec) {
            if (!this.hasGroupedConsole()) {
                this.executed_specs++;
                this.log(spec.suite.description + ' : ' + spec.description + ' ... ');
            }
        },

        reportSuiteResults: function(suite) {
            if (!this.hasGroupedConsole()) {
                let results = suite.results();
                this.log(suite.description + ": " + results.passedCount + " of " + results.totalCount + " passed.");
            }
        },

        log: function(str) {
            let console = jasmine.getGlobal().console;
            if (console && console.log) {
                console.log(str);
            }
        }
    };

    function suiteResults(suite) {
        let results = suite.results();
        startGroup(results, suite.description);
        let specs = suite.specs();
        for (let i in specs) {
            if (specs.hasOwnProperty(i)) {
                specResults(specs[i]);
            }
        }
        let suites = suite.suites();
        for (let j in suites) {
            if (suites.hasOwnProperty(j)) {
                suiteResults(suites[j]);
            }
        }
        console.groupEnd();
    }

    function specResults(spec) {
        let results = spec.results();
        startGroup(results, spec.description);
        let items = results.getItems();
        for (let k in items) {
            if (items.hasOwnProperty(k)) {
                itemResults(items[k]);
            }
        }
        console.groupEnd();
    }

    function itemResults(item) {
        if (item.passed && !item.passed()) {
            console.warn({actual:item.actual,expected: item.expected});
            item.trace.message = item.matcherName;
            console.error(item.trace);
        } else {
            console.info('Passed');
        }
    }

    function startGroup(results, description) {
        let consoleFunc = (results.passed() && console.groupCollapsed) ? 'groupCollapsed' : 'group';
        console[consoleFunc](description + ' (' + results.passedCount + '/' + results.totalCount + ' passed, ' + results.failedCount + ' failures)');
    }

    // export public
    jasmine.SessionReporter = sessionReporter;
    jasmine.ConsoleReporter = ConsoleReporter;
})();