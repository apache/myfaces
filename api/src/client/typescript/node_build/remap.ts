/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
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

/**
 * we are remapping the mappings in our dist file, to meet the expected
 * jsf loading criteria...
 * luckily we can use more than one source map entry
 * so either one is found and the other is not
 */

//the replace in file plugin meets our system independent grep/awk criteria
//does pretty much what grep and awk do on unix systems
const replace = require('replace-in-file');

// we ned to fetch the proper argument
const args = process.argv.slice(2);
// and remap it into our proper option
const buildStage = (args[0] == "--development") ? "-development" : "";
console.log("fixing mapping file references for jsf");

let option = {
    //development
    files: '../../target/classes/META-INF/resources/jakarta.faces/**/*.js',
    from: (buildStage == "-development") ? /faces-development.js\.map/g : /faces.js\.map/g,
    to: `faces${buildStage}.js.map\n//# sourceMappingURL=faces${buildStage}.js.map.mf_map?ln=jakarta.faces`
}

try {
    const result = replace.sync(option);
    console.log("mapping file references fixed!");
    console.log('Replacement results:', result);
} catch (error) {
    console.error('Error occurred:', error);
}
