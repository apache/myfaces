/**
 * Generic Trinidad Transport framework adapter so that we
 * can plug other adapters in
 *
 * Every adapter has to follow
 * a certain interface can can call back into jsf and jsf.ajax as long
 * as no recursive calls are done!
 *
 * every framework adapter has to implement .sendRequest = function(ajaxContext,  action, viewState, passThroughArguments )
 * and can trigger into sendEvent and ajaxResponse of the jsf.ajax function
 *
 */

_reserveMyfaces();
if (!myfaces._JSF2Utils.exists(myfaces, "_TrinidadFrameworkAdapter")) {
    myfaces._TrinidadFrameworkAdapter = function() {
        this._delegate = new myfaces._TrRequestQueue();
    };

    /**
     * central request callback
     */
    myfaces._TrinidadFrameworkAdapter.prototype.sendRequest = function(ajaxContext,  action, viewState, passThroughArguments ) {
        this._delegate.sendRequest(ajaxContext, this._trXhrCallback, action, viewState+"&"+myfaces._JSF2Utils.getPostbackContentFromMap(passThroughArguments));
    };


    /**
     * Maps an internal Trinidad xmlhttprequest event
     * into one compatible with the events of the ri
     * this is done for compatibility purposes
     * since trinidad follows similar rules regarding
     * the events we just have to map the eventing accordingly
     *
     * note I am implementing this after reading the ri code
     *
     */
    myfaces._TrinidadFrameworkAdapter.prototype._mapTrinidadToRIEvents = function(/*object*/ xhrContext,/*_TrXMLRequestEvent*/ event) {
        var complete = false;

        switch(event.getStatus()) {
            //TODO add mapping code here
            case myfaces._TrXMLRequestEvent.STATUS_QUEUED:
                break; /*we have to wait*/
            case myfaces._TrXMLRequestEvent.STATUS_SEND_BEFORE:
                jsf.ajax.sendEvent(null, xhrContext, jsf.ajax._AJAX_STAGE_BEGIN)
                break;;
            case myfaces._TrXMLRequestEvent.STATUS_SEND_AFTER:
                /*still waiting, we can add listeners later if it is allowed*/

                break;
            case myfaces._TrXMLRequestEvent.STATUS_COMPLETE:
                /**
            *here we can do our needed callbacks so
            *that the specification is satisfied
            **/
                complete = true;
                var responseStatusCode = event.getResponseStatusCode();
                if(200 <= responseStatusCode && 300 > responseStatusCode ) {
                    jsf.ajax.sendEvent(event.getRequest(), xhrContext, jsf.ajax._AJAX_STAGE_COMPLETE);
                } else {
                    jsf.ajax.sendEvent(event.getRequest(), xhrContext, jsf.ajax._AJAX_STAGE_COMPLETE);
                    jsf.ajax.sendError(event.getRequest(), xhrContext, jsf.ajax._AJAX_STAGE_HTTPERROR);
                }
                break;
            default:
                break;
        }
        return complete;
    };

    /**
 * Internal Trinidad
 * JSF 2.0 callback compatibility handler
 * since we use trinidad as our transport we have to do it that way
 *
 * this function switches the context via the
 * xhr bindings
 * it is now under the context of
 * given by the calling function
 */
    myfaces._TrinidadFrameworkAdapter.prototype._trXhrCallback = function(/*_TrXMLRequestEvent*/ event) {
        var context = this;//remapping is done by the trinidad engine
        /**
         *generally every callback into this method must issue an event
         *we do a static call because we have lost our scope
         *another way would be to reassign the function to the context
         *which might be possible but I have to check the ri for that
         */
        var complete = myfaces._TrinidadFrameworkAdapter.prototype._mapTrinidadToRIEvents(context, event);

        /**
         * the standard incoming events are handled appropriately we now can deal with the response
         */
        if(complete) {
            jsf.ajax.response(event.getRequest(), context);
        }
    }
}