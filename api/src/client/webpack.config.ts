/*
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
import * as webpack from 'webpack';
import * as path from 'path'

let CompressionPlugin = require('compression-webpack-plugin');

/**
 * we need to define the export in a function
 * because the mode parameter is passed down via the argv
 *
 * @param env the environment
 * @param argv the arguments list
 */
function build(env: {[key:string]: string}, argv: {[key:string]: string}) {

    let libraryTarget = env.TARGET_TYPE ?? "window";

    const config: webpack.Configuration = {
        context: __dirname,
        entry: {
            faces: "./typescript/faces/api/faces.ts",
        },
        devtool: "source-map",

        output: {
            path: path.resolve(__dirname, '../../target/classes/META-INF/resources/jakarta.faces/'),
            libraryTarget: libraryTarget,
            filename: (argv.mode == "production") ? "faces.js" : "faces-development.js"
        },
        resolve: {
            extensions: [".tsx", ".ts", ".json"],
            alias: {
                /*we load the reduced core, because there are some parts we simply do not need*/
               "mona-dish": path.resolve(__dirname, "./typescript/mona_dish/index_core.ts")
            }
        },
        externals: {
            "rxjs": "RxJS"
        },

        module: {
            rules: [
                // all files with a '.ts' or '.tsx' extension will be handled by 'ts-loader'
                {
                    test: /\.tsx?$/, use: [{
                        loader: "ts-loader",
                        options: {
                            configFile: path.resolve(__dirname, "./tsconfig-myfaces.json")
                        }
                    }], exclude: /node_modules/
                }, {
                    test: /faces\.js$/,
                    loader: 'string-replace-loader',
                    options: {
                        search: 'sourceMappingURL=[name].js.map$',
                        replace: 'sourceMappingURL=[name].js.map\n//# sourceMappingURL=[name].js.map.jsf?ln=jakarta.faces',
                    }
                }
            ]
        },

        plugins: [
             new CompressionPlugin({
                algorithm: 'gzip',
                test: /\.js$|\.css$|\.html$|\.eot?.+$|\.ttf?.+$|\.woff?.+$|\.svg?.+$/,
                threshold: 10240,
                minRatio: 0.3
            }),
            new CompressionPlugin({
                algorithm: 'brotliCompress',
                test: /\.(js|css|html|svg)$/,
                threshold: 10240,
                minRatio: 0.8
            })
        ]
    }
    return config;
}

export default build;

