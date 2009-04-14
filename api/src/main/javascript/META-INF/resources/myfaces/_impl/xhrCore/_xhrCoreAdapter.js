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
 * Version: $Revision: 1.6 $ $Date: 2009/04/13 19:50:24 $
 *
 */

_reserveMyfacesNamespaces();

/**
 * Constructor
 */
myfaces._impl.xhrCore._Ajax = function() {}

/**
 * [STATIC PROPERTIES]
 */
// request, type myfaces._impl.xhrCore._AjaxRequest
myfaces._impl.xhrCore._Ajax.request = null;

/**
 * Spec. 13.3.1
 * Collect and encode input elements.
 * Additinoaly the hidden element javax.faces.ViewState
 * @param {String} FORM_ELEMENT - Client-Id of Form-Element
 * @return {String} - Concatenated String of the encoded input elements
 * 			and javax.faces.ViewState element
 */
myfaces._impl.xhrCore._Ajax.prototype.getViewState = function(FORM_ELEMENT) {
    return myfaces._impl.xhrCore._Ajax.request.getViewState(FORM_ELEMENT);
}


/**
 * mapped options already have the exec and view properly in place
 * myfaces specifics can be found under mappedOptions.myFaces
 * @param ajaxContext the ajax context which also has to be pushed into the messages and into the response

        var ajaxContext = {};
        ajaxContext.source = element;
        ajaxContext.onevent = options.onevent;
        ajaxContext.onerror = options.onerror;

       action the action to be used in the xhr request

 **/

myfaces._impl.xhrCore._Ajax.prototype._ajaxRequest = function(source, sourceForm, context, passThroughValues ) {
    myfaces._impl.xhrCore._Ajax.request = new myfaces._impl.xhrCore._AjaxRequest(source, sourceForm, context, passThroughValues);
   	myfaces._impl.xhrCore._AjaxRequestQueue.queue.queueRequest(myfaces._impl.xhrCore._Ajax.request.prepare());
}

/**
 *this has to be called by from various stages of the lifecycle
 *we probably can move this method one level up
 *
 *The idea of this method is following:
 *jsf needs event callback from various stages of the client side lifecycle
 *either errors or events
 *
 *when those events are dispatched in the lifecycle and when the response handling
 *is called is handled here!
 */
myfaces._impl.xhrCore._Ajax._xhrEventDispatcher = function(context, request) {

        var READY_STATE_UNSENT = 0;
        var READY_STATE_OPENED = 1;
        var READY_STATE_HEADERS_RECEIVED = 2;
        var READY_STATE_LOADING = 3;
        var READY_STATE_DONE = 4;

        
   
        var complete = false;
        /*here we have to do the event mapping back into the ri events*/

        //onEvent and onError are served on the JSF side as well!


        switch(request.readyState) {
            case xhrConst.READY_STATE_OPENED:
                jsf.ajax.sendEvent(null, context, "begin");
                break;
            case READY_STATE_DONE:
                /**
                  *here we can do our needed callbacks so
                  *that the specification is satisfied
                  **/
                complete = true;

                var responseStatusCode = request.status;

                if(200 <= responseStatusCode && 300 > responseStatusCode ) {
                    jsf.ajax.sendEvent(request, context, "complete");

                    jsf.ajax.response(request, context);
                } else {
                    jsf.ajax.sendEvent(request, context, "complete");
                    jsf.ajax.sendError(request, context, "httpError");
                }
                break;
            default:
                break;
        }
        return complete;
    };

/**
 * Spec. 13.3.3
 * Examining the response markup and updating the DOM tree
 * @param {XmlHttpRequest} request - The Ajax Request
 * @param {XmlHttpRequest} context - The Ajax context
 */
myfaces._impl.xhrCore._Ajax.prototype._ajaxResponse = function(request, context) {
	if(myfaces._impl.xhrCore._Ajax.request == null)
		alert("Cannot process ajax response because there's no ajax request.");
	myfaces._impl.xhrCore._Ajax.request.requestCallback();
   	myfaces._impl.xhrCore._AjaxRequestQueue.queue.processQueue();
}
