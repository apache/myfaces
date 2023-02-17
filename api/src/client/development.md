# Development Information


## One time usage, must be manually triggered
* npm run copy:npm-sources - Down-streams the source code from the npm packages
* npm run build - runs the build, this script target will be regularly triggered automatically
* npm run test - runs the build, this script target will be regularly triggered automatically

## To update to a new faces_ts version
* change the version in the package json (either manually or via *ncu*)
* *npm install* to install the new version
* *npm run copy:npm-sources* to move the sources out of their npm packages into the source tree
* run a build and test to see if the final build works
