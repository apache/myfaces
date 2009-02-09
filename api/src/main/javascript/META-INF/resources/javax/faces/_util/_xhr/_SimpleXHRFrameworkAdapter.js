/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */


_reserveMyfaces();

/**
 * Adapter functionc call for the SimpleXHR Transport to be
 * compatible to jsf2!
 * For now no direct connection should exist between jsf2 and
 * the transports so that they stay interchangeable!
 *
 * Generally a factory pattern should be supported in the long
 * run to be able to switch adapters on the fly, probably an iframe
 * transport has to be added as well.
 *
 * But to keep the code lean it should be noted
 * that no transport code should be mixed with the other!
 */
if (!myfaces._JSF2Utils.exists(myfaces, "_SimpleXHRFrameworkAdapter")) {
    myfaces._SimpleXHRFrameworkAdapter = function() {
        this._delegate = new myfaces._SimpleXHRTransport();
        /*we fixate the scope of the event callback to the this object trinidad does a remapping 
         *to the scope object we currently do it here later*/
        this._delegate.addEventListener(this._eventCallback);
    };

    /**
     * unmapping via the event callback
     * which then triggers into
     * the jsf event and error subsystem
     */
    myfaces._SimpleXHRFrameworkAdapter.prototype._eventCallback = function(/**/event) {
        var xhrConst = myfaces._XHRConst;
        var xhrContext = event.context;
        var request = event.transport;

        var complete = false;
        /*here we have to do the event mapping back into the ri events*/

        //TODO check whether the scope changes on the sendEvent so that we have to bind it to our context!
        switch(request.readyState) {
            //TODO add mapping code here
            case xhrConst.READY_STATE_OPENED:
                jsf.ajax.sendEvent(null, xhrContext, jsf.ajax._AJAX_STAGE_BEGIN)
                break;
            case xhrConst.READY_STATE_DONE:
                /**
                  *here we can do our needed callbacks so
                  *that the specification is satisfied
                  **/
                complete = true;
                var responseStatusCode = event.status;

                if(200 <= responseStatusCode && 300 > responseStatusCode ) {
                    jsf.ajax.sendEvent(request, xhrContext, jsf.ajax._AJAX_STAGE_COMPLETE);

                    //TODO do the dom manipulation callback here
                    jsf.ajax.response(request, xhrContext);
                } else {
                    jsf.ajax.sendEvent(request, xhrContext, jsf.ajax._AJAX_STAGE_COMPLETE);
                    jsf.ajax.sendError(request, xhrContext, jsf.ajax._AJAX_STAGE_HTTPERROR);
                }
                break;
            default:
                break;
        }
        return complete;
    };

    /**
     * central request callback
     */
    myfaces._SimpleXHRFrameworkAdapter.prototype.sendRequest = function(ajaxContext,  action, viewState, passThroughArguments ) {
        var data = {};
        data.context = ajaxContext;
        data.action = action;
        data.viewstate = viewState;
        data.passthroughArguments = passThroughArguments;
        this._delegate.send(data);
    };

}





