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
 * @namespace
 * @name window
 * @description Eval routines, depending on the browser.
 * <p/>
 * The problem solved in this class is the problem on how to perform
 * a global eval on multiple browsers. Some browsers auto eval themselves
 * they do not need to be called
 * <li>Some work with a window.eval.call(window,... </li>
 * <li>Others use simply execScript <li>
 * <li>Some others work only with the head appendix method
 * head.appendChild(&lt;script...., head.removeChild(&lt;script </li>
 * <p/>
 * Note: The code here already is precompressed because the compressor
 * fails on it, the deficits in readability will be covered by more comments
 *
 */


if (!window.myfaces) {
    /**
     * @namespace
     * @name myfaces
     */
    var myfaces = new function() {
    };
    window.myfaces = myfaces;
}

/**
 * @memberOf myfaces
 * @namespace
 * @name _impl
 */
myfaces._impl = (myfaces._impl) ? myfaces._impl : {};
/**
 * @memberOf myfaces._impl
 * @namespace
 * @name core
 */
myfaces._impl.core = (myfaces._impl.core) ? myfaces._impl.core :{};

if (!myfaces._impl.core._EvalHandlers) {
    /**
     * @memberOf myfaces._impl.core
     * @namespace
     * @name _EvalHandlers
     */
    myfaces._impl.core._EvalHandlers = new function() {
        //the rest of the namespaces can be handled by our namespace feature
        //helper to avoid unneeded hitches
        /**
         * @borrows myfaces._impl.core._Runtime as _T
         */
        var _T = this;

        // note it is safe to remove the cascaded global eval
        // the head appendix method is the standard method and has been
        // for over a decade even on ie6 (see file history)
        /**
         * an implementation of eval which drops legacy support
         * and allows nonce
         * @param code
         * @param cspMeta optional csp metadata, only allowed key atm nonce
         */
        _T.globalEval = function(code, cspMeta) {
            //check for jsf nonce
            var nonce = cspMeta ? cspMeta.nonce : this._currentScriptNonce();

            var element = document.createElement("script");
            element.setAttribute("type", "text/javascript");
            element.innerHTML = code;
            if(nonce) {
                element.setAttribute("nonce", nonce);
            }
            //head appendix method, modern browsers use this method savely to eval scripts
            //we did not use it up until now because there were really old legacy browsers where
            //it did not work
            var htmlScriptElement = document.head.appendChild(element);
            document.head.removeChild(htmlScriptElement);
        };

        _T.resolveNonce = function(item) {
            var nonce = null;
            if(!!(item && item.nonce)) {
                nonce = item.nonce;
            } else if(!!item && item.getAttribute) {
                nonce = item.getAttribute("nonce");
            }
            //empty nonce means no nonce, the rest
            //of the code treats it like null
            return (!nonce) ? null : nonce;
        }
        /*
        * determines the jsfjs nonce and adds them to the namespace
        * this is done once and only lazily
        */
        _T._currentScriptNonce = function() {
            //already processed
            if(myfaces.config && myfaces.config.cspMeta) {
                return myfaces.config.cspMeta.nonce;
            }

            //since our baseline atm is ie11 we cannot use document.currentScript globally
            if(_T.resolveNonce(document.currentScript)) {
                // fastpath for modern browsers
                return _T.resolveNonce(document.currentScript);
            }

            var _Lang = myfaces._impl._util._Lang;
            var scripts = _Lang.objToArray(document.getElementsByTagName("script"))
                .concat(_Lang.objToArray(document.getElementsByTagName("link")));

            var jsf_js = null;

            //we search all scripts
            for(var cnt = 0; scripts && cnt < scripts.length; cnt++) {
                var scriptNode = scripts[cnt];
                if(!_T.resolveNonce(scriptNode)) {
                    continue;
                }
                var src = scriptNode.getAttribute("src") || "";
                if(src && !src.match(/jsf\.js\?ln\=javax\.faces/gi)) {
                    jsf_js = scriptNode;
                    //the first one is the one we have our code in
                    //subsequent ones do not overwrite our code
                    break;
                }
            }
            //found
            myfaces.config = myfaces.config || {};
            myfaces.config.cspMeta = myfaces.config.cspMeta || {
                nonce: null
            };
            if(jsf_js) {
                myfaces.config.cspMeta.nonce = _T.resolveNonce(jsf_js);
            }
            return myfaces.config.cspMeta.nonce;
        };

    };
}