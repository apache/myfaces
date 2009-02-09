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


/**
 Implementation of the JSF 2.0 transport layer as xml http request
 as for now only XmlHttpRequest is implemented as Transport,
 but in the long run we probably also can use iframes
 and web sockets as transport layers.

 The pluggable transport layer system is similar to what dojo has to offer
 but is no direct port due to the huge dependency chain dojo introduces in this
 area, we do not want to have a dojo dependency in our subsystem

 This transport layer allows an ajax command queuing as defined by the jsf 2 spec
 and also adds more flexibility in the ajax callbacks to ease the
 adding of trinidad compatible hooks in the implementation details

 The public api utilizes this transport class for calling the entire ajax part!
 */
_reserveMyfaces();


if (!myfaces._JSF2Utils.exists(myfaces, "_SimpleXHRTransport")) {
    myfaces._SimpleXHRTransport = function() {
        /*we need an instance of our utils for the hitch function*/
        this._utils = myfaces._JSF2Utils;
        /*const namespace remapping*/
        this.xhrConst = myfaces._XHRConst;

        /*currently only post is supported*/
        this._sendMethod = "POST";
        /*currently only async is supported*/
        this._async = true;
        /*calls for now must be uncached*/
        this._cached = false;

        this._eventListeners = new myfaces._ListenerQueue();
  
        /*
         * caching data queue forw cached
         * ajax post requests!
         */
        this._transportDataQueue = [];
    };


    myfaces._SimpleXHRTransport.prototype.addEventListener = function(/*function*/ eventListener) {
        this._eventListeners.add(eventListener);
    };
    myfaces._SimpleXHRTransport.prototype.removeEventListener = function(/*function*/ eventListener) {
        this._eventListeners.remove(eventListener);
    };

    myfaces._SimpleXHRTransport.prototype._handleXHREvent = function() {
        /*handle the event callbach*/
       
        var data = this._transportDataQueue[0];
        this._eventListeners.broadcastEvent(data);
  
        if(data.transport.readyState === this.xhrConst.READY_STATE_DONE) {
            this._transportDataQueue.shift();
            this._process(true);
            /*ie cleanup*/
            delete data.transport;
        }
        
        return true;
    };

    /**
     * central queue processing callback!
     * @param inProcess marks whether the process is called from the outside
     * or after terminating an xhr request from the inside
     * we have to cover this that way because of a callback error in mozilla
     */
    myfaces._SimpleXHRTransport.prototype._process = function( /*boolean*/ inProcess) {
        try {
            
            var size = this._transportDataQueue.length;
            if(size > 1 && !inProcess) return; /*still in queue no send can be issued*/
            if(size === 0) {
               
                return; /*empty queue process has to terminate*/
            }


            /*note this only works this way because javascript multitasks only premptively
             *if a real multithreading is added please put the outer send into a critical region
             *to prevent concurrency issues*/
            var data = this._transportDataQueue[0];

            data.transport = this._utils.getXHR();

            var transport = data.transport;
            var passThrough = data.passthroughArguments;

            if(!this._utils.isString(passThrough)) {
                passThrough = this._utils.getPostbackContentFromMap(passThrough);
            }
            if(!this._cached) { //we bypass any caching if needed!
                //set the pragmas here as well
                data.action = data.action + ((data.action.indexOf('?') == -1) ? "?" :"&" )+ "AjaxRequestUniqueId = "+(new Date().getTime());
            }
            /**
             * We set the on ready state change here
             */
            
            transport.onreadystatechange = this._utils.hitch(this, this._handleXHREvent);
            transport.open(this._sendMethod, data.action, this._async);
            if(this._utils.exists(transport, "setRequestHeader")) {
                transport.setRequestHeader(this.xhrConst.FACES_REQUEST, this.xhrConst.PARTIAL_AJAX);
                transport.setRequestHeader(this.xhrConst.CONTENT_TYPE, this.xhrConst.XFORM_ENCODED);
            }
       
            //THE RI does a notification here
            //be we rely on the official W3C codes
            //which should be sufficient for the callback
            //on "begin"
            transport.send(data.viewstate +"&"+ passThrough);
           

        } catch (e) {
            //Browser error...
            /*internal error we log it and then we splice the affected event away*/
            myfaces._Logger.getInstance().error("Error in  myfaces._SimpleXHRTransport.prototype._process",  e);
            if(this._transportDataQueue.length > 0) {
                this._transportDataQueue.shift();
            }
        }
    }

    /**
     * data = {
     *      context //jsf2 context
     *      action  //String
     *      viewstate //String
     *      passthroughArguments //Map|String
     *      //added by this method
     *      transport //XHRObject
     *  }
     * called indirectly from ajaxContext, sourceForm.action, viewState, passThroughArguments
     **/
    myfaces._SimpleXHRTransport.prototype.send = function(/*Object*/data) {

        var queueData = {};
        queueData.context = data.context;
        queueData.action = data.action;
        queueData.viewstate = data.viewstate;
        queueData.passthroughArguments = data.passthroughArguments;

        this._transportDataQueue.push(queueData);
        /*we initiate a send if none is in progress currently*/
        
        this._process(false);
    };
}