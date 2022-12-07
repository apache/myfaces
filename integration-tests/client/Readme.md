# MyFaces faces.ts client side tests

## General info

The new client side integration testsuite, derived
from: 

https://github.com/werpu/myfaces-js-integrationtests

(which serves as downstream project for this myfaces part)

## Details

This testsuite differs in a way that it does not use
Arqullian like the other test suites in this project.
It relies on a running faces server in conjunction
with a set of client side test apis (moch, a headless chrome triggered by node)

The suite can be triggered the following way
a) Automatically simply by running the build
b) Running the server via clean clean install -DskipTests exec:java -Pstandalone -f pom.xml
and then run *npm run tests*
c) By running directly in your browser, just go to 
http://localhost:8080/IntegrationJSTest and follow the instructions.

## Technology

The tests basically are jsf pages where a set of client operations
is performed which trigger the jsf mechanisms or fake them (depending on the test)

Technically every page is a set of Mocha tests triggering aspects
of the page and analyzing the outcome via expects
(include for the Mocha test script is always in the page)
The results are displayed in page and at the final page where
the end result for the entire testsuite is displayed.

For automated build tests a server and a headless Chrome is launched.
The headless Chrome basically triggers another set of Mocha tests
which point to the single test pages and wait for the test result
to be displayed. An expectation for this client test is fulfilled 
if none of the in page tests fail.

Everything then is bundled together via npm and the maven frontend plugin
(which is an integration shim for npm into maven)




