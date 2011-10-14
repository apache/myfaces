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
/*
 * Author: Werner Punz (latest modification by $Author: werpu $)
 * Version: $Revision: 1.4 $ $Date: 2009/05/31 09:16:44 $
 */
/**
 * @memberOf myfaces._impl
 * @namespace
 * @name xhrCore
 */


/**
 * @class
 * @name _FinalizeableObj
 * @memberOf myfaces._impl.xhrCore
 * @description  A helper class which supports IE with its non working garbage collection.
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb250448%28VS.85%29.aspx">http://msdn.microsoft.com/en-us/library/bb250448%28VS.85%29.aspx</a>
 * @see <a href="http://weblogs.java.net/blog/driscoll/archive/2009/11/13/ie-memory-management-and-you">http://weblogs.java.net/blog/driscoll/archive/2009/11/13/ie-memory-management-and-you</a>
 * @see <a href="http://home.orange.nl/jsrosman/">http://home.orange.nl/jsrosman/</a>
 * @see <a href="http://www.quirksmode.org/blog/archives/2005/10/memory_leaks_li.html">http://www.quirksmode.org/blog/archives/2005/10/memory_leaks_li.html</a>
 * @see <a href="http://www.josh-davis.org/node/7">http://www.josh-davis.org/node/7</a>
 */
_MF_CLS("myfaces._impl.xhrCore._FinalizeableObj", Object,
/** @lends myfaces._impl.xhrCore._FinalizeableObj.prototype */
{

    _resettableContent: null,

    constructor_: function() {
        this._resettableContent={};
        //we add our three util singletons
        this._RT =   myfaces._impl.core._Runtime;
        this._Lang = myfaces._impl._util._Lang;
        this._Dom = myfaces._impl._util._Dom;
    },

    _initDefaultFinalizableFields: function() {
         for(var key in this) {
            //per default we reset everything which is not preinitalized
            if(null == this[key] && key != "_resettableContent" && key.indexOf("_mf") != 0 && key.indexOf("_") == 0) {
                this._resettableContent[key]=true;
            }
        }
    },

    /**
     * ie6 cleanup
     * This method disposes all properties manually in case of ie6
     * hence reduces the chance of running into a gc problem tremendously
     * on other browsers this method does nothing
     */
    _finalize: function() {
        if (!myfaces._impl.core._Runtime.browser.isIE || !this._resettableContent) {
            //no ie, no broken garbage collector
            return;
        }
       
        for(var key in this._resettableContent) {
            if (myfaces._impl.core._Runtime.exists(this[key],"_finalize")) {
                this[key]._finalize();
            }
            delete this[key];
        }
    }
});