/*!
 * Licensed to the Apache Software Foundation (ASF) under one or more
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
 * this is a small script
 * which handles the recursive copy, it atm uses the fs-extra module which is under mit license
 * for build purposes.
 *
 * This module can be replaced with custom code if needed.
 *
 */
import * as fse from 'fs-extra';
import * as path from 'path';

const cwd = process.cwd();

/**
 * a small shim over the fsextra call
 * to recursively copy a directory
 * @param source
 * @param target
 */
function copyFilesRecursively(source: string, target: string)
{
    fse.copySync(path.resolve(cwd, source), path.resolve(cwd, target), {overwrite: true});
    console.log(`${source} successfully copied to ${target}`);
}

/**
 * the files which need to be copied in our case the generated sources and docs (for now we do not host
 * the files themselves)
 *
 * We are simply relocating to the old location of the files
 * standard jsf namespace and myfaces for the extended namespaces
 *
 * TODO we need to find a way to merge the jsdocs since we have to retire our existing
 * assembly plugin for jsdocs
 */
copyFilesRecursively('./node_modules/jsf.js_next_gen/src/main/typescript/api', './typescript/faces/api');
copyFilesRecursively('./node_modules/jsf.js_next_gen/src/main/typescript/impl', './typescript/faces/impl');
copyFilesRecursively('./node_modules/jsf.js_next_gen/src/main/typescript/myfaces', './typescript/faces/myfaces');
copyFilesRecursively('./node_modules/jsf.js_next_gen/src/main/typescript/test', './typescript/faces/test');
copyFilesRecursively('./node_modules/jsf.js_next_gen/src/main/typescript/@types', './typescript/faces/@types');
copyFilesRecursively('./node_modules/mona-dish/src/main/typescript', './typescript/mona_dish');
