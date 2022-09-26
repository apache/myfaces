# JSF_JS next gen

this directory hosts the main build for the typescript based
jsf files.
Due to project requirements all necessary source files are hosted here.
Build related node files cannot be hosted here.

We use the npm packages as downstream to get the code from npm.
Any change should be upstreamed back to the corresponding 
projects from github. (aka mona-dish and jsf_ts)

usage:
One time usage, must be manually triggered:
* npm run copy:npm-sources - Downstreams the source code from the npm packages
* npm run build - runs the build, this script target will be regularily triggered automatically
* npm run test - runs the build, this script target will be regularily triggered automatically

to update to a new jsf_ts version 
* change the version in the package json (either manually or via *ncu*)
* *npm install* to install the new version
* *npm run copy:npm-sources* to move the sources out of their npm packages into the source tree
* run a build and test to see if the final build works


