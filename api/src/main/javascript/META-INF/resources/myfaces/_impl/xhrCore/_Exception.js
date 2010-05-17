/*
 * Copyright 2009 Ganesh Jung
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Ganesh Jung (latest modification by $Author: ganeshpuri $)
 * Version: $Revision: 1.2 $ $Date: 2009/05/30 17:54:57 $
 *
 */

myfaces._impl.core._Runtime.extendClass("myfaces._impl.xhrCore._Exception", Object, {

    constructor_: function(sourceClass, threshold) {
        this.m_class = sourceClass;
        this.m_threshold = threshold;
    },
    /**
     * throws errors
     */
    throwError : function(request, context, func, exception) {
        if (this.m_threshold == "ERROR") {
            var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
            _Impl.sendError(request, context, myfaces._impl.core.Impl._ERROR_CLIENT_ERROR, exception.name,
                    "MyFaces ERROR\n"
                            + "Affected Class: " + this.m_class + "\n"
                            + "Affected Method: " + func + "\n"
                            + "Error name: " + exception.name + "\n"
                            + "Error message: " + exception.message + "\n"
                            + "Error description: " + exception.description + "\n"
                            + "Error number: " + exception.number + "\n"
                            + "Error line number: " + exception.lineNumber);
        }
        this.destroy();
        //client exceptions should not be swallowed they have to be delegated to the client itself!
        //TODO check if we are even allowed to issue client errors via the ajax API!
        throw exception;
    },

    /**
     * throws warnings
     */
    throwWarning : function(request, context, func, message) {
        if (this.m_threshold == "WARNING" || this.m_threshold == "ERROR") {
            var _Impl = myfaces._impl.core._Runtime.getGlobalConfig("jsfAjaxImpl", myfaces._impl.core.Impl);
            _Impl.sendError(request, context, myfaces._impl.core.Impl._ERROR_CLIENT_ERROR, exception.name,
                    "MyFaces WARNING\n[" + this.m_class + "::" + func + "]\n\n"
                            + message);
        }
        this.destroy();
    },

    /**
     * cleanup activities if an error occurs
     */
    destroy : function() {
        if (myfaces._impl.xhrCore._RQInstance &&
                myfaces._impl.xhrCore._RQInstance != null) {
            // clear RequestQueue when an exception occurs
           myfaces._impl.xhrCore._RQInstance.cleanup();
        }
    }

});