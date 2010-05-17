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
 * Version: $Revision: 1.1 $ $Date: 2009/05/26 21:24:42 $
 *
 */

myfaces._impl.core._Runtime.extendClass("myfaces._impl.xhrCore._Ajax"
        , Object, {
    /**
     * a singleton queue
     */
    _queue: myfaces._impl.xhrCore._RQInstance,
    _exception: new myfaces._impl.xhrCore._Exception("myfaces._impl.xhrCore._AjaxRequest", "ERROR"),

    /**
     * mapped options already have the exec and view properly in place
     * myfaces specifics can be found under mappedOptions.myFaces
     * @param {DomNode} source the source of this call
     * @param {HTMLForm} sourceForm the html form which is the source of this call
     * @param {Map} context the internal pass through context
     * @param {Map} passThroughValues values to be passed through
     **/
    xhrQueuedPost : function(source, sourceForm, context, passThroughValues) {
        this._queue.enqueue(
                new myfaces._impl.xhrCore._AjaxRequest(source, sourceForm, context, passThroughValues,this._queue));
    },
    /*
    _stdErrorHandler: function(request, context,sourceClass, func, exception) {
        this._exception.throwError(request, context, func, exception);
    },

    _stdWarningsHandler: function(request, context, sourceClass, func, exception) {
        this._exception.throwWarning(request, context, func, exception);
    },
      */

    /**
     * Spec. 13.3.3
     * Examining the response markup and updating the DOM tree
     * @param {XmlHttpRequest} request - the ajax request
     * @param {XmlHttpRequest} context - the ajax context
     */
    response : function(request, context) {
        this._queue._curReq.response.processResponse(request, context);
    }
});
