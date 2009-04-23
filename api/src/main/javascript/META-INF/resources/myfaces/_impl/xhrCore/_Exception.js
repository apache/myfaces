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
 * Version: $Revision: 1.4 $ $Date: 2009/04/18 17:19:12 $
 *
 */

_reserveMyfacesNamespaces();


if (!myfaces._impl._util._LangUtils.exists(myfaces._impl.xhrCore, "_Exception")) {


    myfaces._impl.xhrCore._Exception = function(sourceClass, threshold) {
        this.m_class = sourceClass
        this.m_threshold = threshold
    };

    /**
 * [STATIC]
 * static method used by static methods that throw errors
 */
    myfaces._impl.xhrCore._Exception.throwNewError = function(request, context, sourceClass, func, exception) {
        newException = new myfaces._impl.xhrCore._Exception(request, context, sourceClass, "ERROR");
        newException.throwError(request, context, func, exception);
    };

    /**
 * [STATIC]
 * static method used by static methods that throw warnings
 */
    myfaces._impl.xhrCore._Exception.throwNewWarning = function(request, context, sourceClass, func, message){
        newException = new myfaces._impl.xhrCore._Exception(request, context, sourceClass, "WARNING");
        newException.throwWarning(request, context, func, message);
    };

    /**
 * throws errors
 */
    myfaces._impl.xhrCore._Exception.prototype.throwError = function(request, context, func, exception) {
        if (this.m_threshold == "ERROR") {
            jsf.ajax.sendError(request, context, myfaces._impl.core._jsfImpl._ERROR_CLIENT_ERROR, exception.name,
                "MyFaces ERROR\n"
                + "Affected Class: " + this.m_class + "\n"
                + "Affected Method: " + func + "\n"
                + "Error name: " + exception.name + "\n"
                + "Error message: " + exception.message + "\n"
                + "Error description: " + exception.description	+ "\n"
                + "Error number: " + exception.number + "\n"
                + "Error line number: " + exception.lineNumber);
        }
        this.destroy();
    };

    /**
 * throws warnings
 */
    myfaces._impl.xhrCore._Exception.prototype.throwWarning = function(request, context, func, message) {
        if (this.m_threshold == "WARNING" || this.m_threshold == "ERROR") {
            jsf.ajax.sendError(request, context, myfaces._impl.core._jsfImpl._ERROR_CLIENT_ERROR, exception.name,
                "MyFaces WARNING\n[" + this.m_class + "::" + func + "]\n\n"
                + message);
        }
        this.destroy();
    };

    /**
 * cleanup activities if an error occurs
 */
    myfaces._impl.xhrCore._Exception.prototype.destroy = function() {
        if (myfaces._impl.xhrCore._AjaxRequestQueue.queue &&
            myfaces._impl.xhrCore._AjaxRequestQueue.queue != null) {
            // clear RequestQueue when an exception occurs
            myfaces._impl.xhrCore._AjaxRequestQueue.queue.clearQueue();
        }
    };

}