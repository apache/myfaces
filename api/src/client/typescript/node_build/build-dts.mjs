/*! Licensed to the Apache Software Foundation (ASF) under one or more
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
 * Programmatic api-extractor runner.
 * Suppresses the "bundled TypeScript is older than project" noise that appears
 * when the project uses a newer TypeScript than api-extractor bundles.
 *
 * Runs api-extractor against the pre-emitted declaration tree (produced by
 * `tsc -p tsconfig.dts.json` into ./dts-build) and rolls it up directly into
 * the maven resources target, then post-processes that file into a global
 * ambient declaration matching the runtime shape where faces/myfaces hang off
 * window.
 *
 * Usage (called by npm scripts):
 *   node typescript/node_build/build-dts.mjs
 */

import { Extractor, ExtractorConfig } from "@microsoft/api-extractor";
import { resolve, dirname } from "path";
import { fileURLToPath } from "url";
import { readFileSync, writeFileSync, mkdirSync } from "fs";

// Suppress the "bundled TypeScript / newer than bundled" noise from api-extractor.
function makeSuppressingWriter(original) {
  return function (chunk, ...args) {
    const text = typeof chunk === "string" ? chunk : chunk.toString();
    if (
      text.includes("bundled TypeScript version") ||
      text.includes("newer than the bundled compiler engine")
    ) {
      return true;
    }
    return original.call(this, chunk, ...args);
  };
}
process.stdout.write = makeSuppressingWriter(process.stdout.write.bind(process.stdout));
process.stderr.write = makeSuppressingWriter(process.stderr.write.bind(process.stderr));

const __dirname = dirname(fileURLToPath(import.meta.url));
// build-dts.mjs lives in typescript/node_build, project root is two levels up.
const projectRoot = resolve(__dirname, "..", "..");

// Resolve the local TypeScript compiler installed in the project.
const typescriptCompilerFolder = resolve(projectRoot, "node_modules", "typescript");

// The maven resources target the rollup is written into.
const targetDir = resolve(
  projectRoot,
  "..", "..",
  "target", "classes", "META-INF", "resources", "jakarta.faces"
);

// --- Step 1: run api-extractor for faces.d.ts ---

// Ensure the output directory exists (api-extractor does not create it, and on a
// fresh build webpack has not run yet so the maven target may be absent).
mkdirSync(targetDir, { recursive: true });

const configPath = resolve(projectRoot, "api-extractor.faces.json");
console.log(`\napi-extractor: processing api-extractor.faces.json`);

const extractorConfig = ExtractorConfig.loadFileAndPrepare(configPath);

const result = Extractor.invoke(extractorConfig, {
  localBuild: true,
  typescriptCompilerFolder,
  showVerboseMessages: false,
});

if (!result.succeeded) {
  console.error("  ✖  api-extractor.faces.json — FAILED");
  process.exit(1);
}
console.log("  ✔  api-extractor.faces.json — completed successfully");

// --- Step 2: post-process faces.d.ts into a global declaration file ---

/**
 * Walks forward from openIdx counting braces, returns the index of the
 * matching closing brace, or -1 if not found.
 */
function findClosingBrace(content, openIdx) {
  let depth = 0;
  for (let i = openIdx; i < content.length; i++) {
    if (content[i] === "{") depth++;
    else if (content[i] === "}") {
      if (--depth === 0) return i;
    }
  }
  return -1;
}

/**
 * Converts the api-extractor module output into a global declaration file:
 *
 *  A. Strips "export" from top-level namespace declarations so that
 *     "declare namespace faces { ... }" is a global ambient namespace,
 *     matching the runtime shape where faces/jsf hang off window.
 *
 *  B. Removes "export { }" which makes the file a TypeScript module
 *     (and therefore prevents global treatment).
 *
 *  C. Inlines the top-level "declare namespace oam { ... }" block as a
 *     nested "namespace oam { ... }" inside myfaces, fixing the
 *     self-referential "const oam: typeof oam" that api-extractor emits.
 */
function makeGlobalDeclaration(content) {
  content = content.replace(/\r\n/g, "\n");

  // A. Remove "export" from top-level namespace declarations.
  content = content.replace(/^export (declare namespace )/gm, "$1");

  // B. Remove the module-marker line.
  content = content.replace(/\nexport \{ \}\s*$/, "\n");

  // D. api-extractor drops "export" from const declarations inside namespaces
  //    (it preserves it for functions/types/interfaces but not consts).
  //    Add it back so namespace members like faces.specversion are accessible.
  content = content.replace(/^([ \t]+)const /gm, "$1export const ");

  // C. Inline the top-level oam namespace into myfaces.
  const oamNeedle = "\ndeclare namespace oam {";
  const nsIdx = content.indexOf(oamNeedle);
  if (nsIdx !== -1) {
    const openIdx = content.indexOf("{", nsIdx + 1);
    const closeIdx = findClosingBrace(content, openIdx);
    if (closeIdx !== -1) {
      // Extract inner content, add 4 extra spaces of indentation, and make
      // each const member explicitly exported so myfaces.oam.X is accessible.
      const inner = content
        .slice(openIdx + 1, closeIdx)
        .split("\n")
        .map((l) => {
          if (!l.trim().length) return "";
          const indented = "    " + l;
          // Top-level oam members had no 'export' (redundant at global scope);
          // nested inside myfaces they need it for myfaces.oam.X to be visible.
          return indented.replace(/^([ \t]+)const /, "$1export const ");
        })
        .join("\n")
        .trimEnd();

      // Find the cut point: the blank line immediately before the top-level
      // JSDoc that precedes "declare namespace oam {".
      // Top-level JSDoc lines start at column 0 ("/**"), while JSDoc inside
      // namespaces is indented ("    /**"), so lastIndexOf("\n/**") safely
      // targets the oam JSDoc without matching inner comments.
      const jsdocAnchor = content.lastIndexOf("\n/**", nsIdx);
      const cutFrom = jsdocAnchor !== -1 ? jsdocAnchor : nsIdx;

      // Remove the top-level oam block first (preserves index validity for
      // the const-oam replacement that follows).
      content = content.slice(0, cutFrom) + content.slice(closeIdx + 1);
      content = content.trimEnd() + "\n";

      // Replace 'const oam: typeof oam;' with the inlined nested namespace.
      // 'export namespace' so that myfaces.oam is visible to TypeScript.
      content = content.replace(
        /^([ \t]*)const oam: typeof oam;/m,
        `$1export namespace oam {\n${inner}\n$1}`
      );
    }
  }

  return content;
}

const APACHE_HEADER =
  "/*! Licensed to the Apache Software Foundation (ASF) under one or more\n" +
  " * contributor license agreements.  See the NOTICE file distributed with\n" +
  " * this work for additional information regarding copyright ownership.\n" +
  " * The ASF licenses this file to you under the Apache License, Version 2.0\n" +
  ' * (the "License"); you may not use this file except in compliance with\n' +
  " * the License.  You may obtain a copy of the License at\n" +
  " *\n" +
  " *      http://www.apache.org/licenses/LICENSE-2.0\n" +
  " *\n" +
  " * Unless required by applicable law or agreed to in writing, software\n" +
  ' * distributed under the License is distributed on an "AS IS" BASIS,\n' +
  " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
  " * See the License for the specific language governing permissions and\n" +
  " * limitations under the License.\n" +
  " */\n\n";

function postProcessFaces(facesPath) {
  let content = readFileSync(facesPath, "utf-8");
  content = makeGlobalDeclaration(content);
  content = APACHE_HEADER + content;
  writeFileSync(facesPath, content, "utf-8");
}

const facesPath = resolve(targetDir, "faces.d.ts");

postProcessFaces(facesPath);
console.log("  ✔  faces.d.ts — post-processed as global declaration");
