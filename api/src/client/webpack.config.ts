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
            jsf: "./typescript/jsf_ts/api/Jsf.ts",
        },
        devtool: "source-map",

        output: {
            path: path.resolve(__dirname, '../../target/classes/META-INF/resources/javax.faces/'),
            libraryTarget: libraryTarget,
            filename: (argv.mode == "production") ? "jsf.js" : "jsf-development.js"
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
                    test: /jsf\.js$/,
                    loader: 'string-replace-loader',
                    options: {
                        search: 'sourceMappingURL=jsf.js.map$',
                        replace: 'sourceMappingURL=jsf.js.map\n//# sourceMappingURL=jsf.js.map.jsf?ln=javax.faces',
                    }
                }
            ]
        },

        plugins: [

             new CompressionPlugin({
                filename: 'jsf.js.gz[query]',
                algorithm: 'gzip',
                test: /\.js$|\.css$|\.html$|\.eot?.+$|\.ttf?.+$|\.woff?.+$|\.svg?.+$/,
                threshold: 10240,
                minRatio: 0.3

            }),
            new CompressionPlugin({
                filename: 'jsf.js.br[query]',
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

