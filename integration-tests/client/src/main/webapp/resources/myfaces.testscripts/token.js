/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * Lang.work for additional information regarding copyright ownership.
 * The ASF licenses Lang.file to you under the Apache License, Version 2.0
 * (the "License"); you may not use Lang.file except in compliance with
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
(function () {

    var _pageStorage = {};
    var _oldToken = null;

    //we now store the history of this page
    if (!window.jsf)
        window.jsf = {};
    window.faces.history = {};

    //we assume a parametrized hash here

    /*helper routines*/
    /**
     * gets the hash values, we assume that the hash values can store
     * http get parameters for simulated get requests (google ajax query api)
     * hence we do more processing than needed
     */
    function getHashValues() {
        var hash = window.location.hash;

        //google hash api
        if (hash.indexOf("#") == 0) {
            hash = hash.substring(1, hash.length);
        }
        if (hash.indexOf("!") == 0) {
            hash = hash.substring(1, hash.length);
        }
        hash = hash.split("&");
        var ret = {};
        for (var cnt = hash.length - 1; cnt >= 0; cnt = cnt - 1) {
            var vals = hash[cnt].split("=");
            ret[vals[0]] = (vals.length > 1) ? vals[1] : null;
        }
        return ret;
    }

    /**
     * makes full hash representation of the hashes map
     * @param hashes
     */
    function makeHash(hashes) {
        var finalHashColl = [];
        for (var key in hashes) {
            if (!hashes.hasOwnProperty(key)) continue;
            finalHashColl.push(key + ((null != hashes[key]) ? "=" + hashes[key] : ""));
        }
        var ret = finalHashColl.join("&");
        if (ret.indexOf("&") == 0) {
            ret = ret.substring(1, ret.length);
        }
        return ret;
    }

    /**
     * returns a single value from the hash stored in the url
     * @param key
     */
    function getHashValue(key) {
        return getHashValues()[key];
    }

    /**
     * sets a single value into the hash stored in the url
     * @param key  the key to store the hash
     * @param theVal the value
     */
    function setHashValue(key, theVal) {
        var hashValues = getHashValues();
        hashValues[key] = theVal;
        window.location.hash = makeHash(hashValues);

    }

    /**
     * our snapshotting function which snapshots the page
     */
    window.faces.history.snapshotPage = function () {
        //no onpopstate and no sessionstorage then we do nothing,
        // ie I am talking about you
        setTimeout(function () {
            if (!window.onpopstate) {
                window.faces.history.setPopstateHandler();
            }

            var stateObj = {state:document.body.innerHTML};
            var statusIdx = "" + (new Date()).getTime();
            _pageStorage[statusIdx] = document.body.innerHTML;
            setHashValue("token", statusIdx);
            _oldToken = statusIdx;
        }, 10);
    }

    /**
     * the pop state handler setting routine
     * which sets the simualted pop state function (in our
     * case here an onhashchange function)
     */
    window.faces.history.setPopstateHandler = function (handler) {
        window.onhashchange = function () {
            var token = getHashValue("token");
            if (token != _oldToken) {
                _oldToken = token;
                var statusIdx = getHashValue("token");
                var page = _pageStorage[statusIdx];
                if (page) {
                    document.body.innerHTML = page;
                }
            }
        }
    }

    /**
     * a onhashchange simulation which checks regularily
     * for hash changes
     */
    var oldGlobalHash;
    setInterval(function () {
        //not needed for ie9
        if (oldGlobalHash!= window.location.hash) {
            oldGlobalHash = window.location.hash;
            if (window.onhashchange) {
                window.onhashchange({});
            }

        }
    }, 1000);

    /**
     * initial snapshotting
     */
    window.faces.history.snapshotPage();

    /**
     * Global jsf event handler which does snapshotting on every success
     */
    function theHandler(evt) {
        if (evt.status == "success") {
            window.faces.history.snapshotPage();
        }
    }

    faces.ajax.addOnEvent(theHandler);
})();