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
if (!document.querySelectorAll) {

    //initial browser detection, we encapsule it in a closure
    //to drop all temporary variables from ram as soon as possible
    (function() {
        var _T  = myfaces._impl.core._Runtime;





        _T.getXHRObject = function() {
            //since this is a global object ie hates it if we do not check for undefined
            if (window.XMLHttpRequest) {
                var _ret = new XMLHttpRequest();
                //we now check the xhr level
                //sendAsBinary = 1.5 which means mozilla only
                //upload attribute present == level2

                if (!_T.XHR_LEVEL) {
                    var _e = _T.exists;
                    _T.XHR_LEVEL = (_e(_ret, "sendAsBinary")) ? 1.5 : 1;
                    _T.XHR_LEVEL = (_e(_ret, "upload") && 'undefined' != typeof FormData) ? 2 : _T.XHR_LEVEL;
                }
                return _ret;
            }
            //IE
            try {
                _T.XHR_LEVEL = 1;
                return new ActiveXObject("Msxml2.XMLHTTP");
            } catch (e) {

            }
            return new ActiveXObject('Microsoft.XMLHTTP');
        };

        /**
         * browser detection code
         * cross ported from dojo 1.2
         *
         * dojos browser detection code is very sophisticated
         * hence we port it over it allows a very fine grained detection of
         * browsers including the version number
         * this however only can work out if the user
         * does not alter the user agent, which they normally dont!
         *
         * the exception is the ie detection which relies on specific quirks in ie
         */
        var n = navigator;
        var dua = n.userAgent,
                dav = n.appVersion,
                tv = parseFloat(dav);
        var _T = myfaces._impl.core._Runtime;
        _T.browser = {};
        myfaces._impl.core._EvalHandlers.browser = _T.browser;
        var d = _T.browser;

        if (dua.indexOf("Opera") >= 0) {
            _T.isOpera = tv;
        }
        if (dua.indexOf("AdobeAIR") >= 0) {
            d.isAIR = 1;
        }
        if (dua.indexOf("BlackBerry") >= 0) {
            d.isBlackBerry = tv;
        }
        d.isKhtml = (dav.indexOf("Konqueror") >= 0) ? tv : 0;
        d.isWebKit = parseFloat(dua.split("WebKit/")[1]) || undefined;
        d.isChrome = parseFloat(dua.split("Chrome/")[1]) || undefined;

        // safari detection derived from:
        //		http://developer.apple.com/internet/safari/faq.html#anchor2
        //		http://developer.apple.com/internet/safari/uamatrix.html
        var index = Math.max(dav.indexOf("WebKit"), dav.indexOf("Safari"), 0);
        if (index && !d.isChrome) {
            // try to grab the explicit Safari version first. If we don't get
            // one, look for less than 419.3 as the indication that we're on something
            // "Safari 2-ish".
            d.isSafari = parseFloat(dav.split("Version/")[1]);
            if (!d.isSafari || parseFloat(dav.substr(index + 7)) <= 419.3) {
                d.isSafari = 2;
            }
        }

        //>>excludeStart("webkitMobile", kwArgs.webkitMobile);

        if (dua.indexOf("Gecko") >= 0 && !d.isKhtml && !d.isWebKit) {
            d.isMozilla = d.isMoz = tv;
        }
        if (d.isMoz) {
            //We really need to get away from _T. Consider a sane isGecko approach for the future.
            d.isFF = parseFloat(dua.split("Firefox/")[1] || dua.split("Minefield/")[1] || dua.split("Shiretoko/")[1]) || undefined;
        }

        if (document.all && !d.isOpera && !d.isBlackBerry) {
            d.isIE = parseFloat(dav.split("MSIE ")[1]) || undefined;
            d.isIEMobile = parseFloat(dua.split("IEMobile")[1]);
            //In cases where the page has an HTTP header or META tag with
            //X-UA-Compatible, then it is in emulation mode, for a previous
            //version. Make sure isIE reflects the desired version.
            //document.documentMode of 5 means quirks mode.

            /** @namespace document.documentMode */
            if (d.isIE >= 8 && document.documentMode != 5) {
                d.isIE = document.documentMode;
            }
        }
    })();
}