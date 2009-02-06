/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
_reserveMyfaces();
if ('undefined' == typeof myfaces._TrRequestQueue) {
    /**
     * The RequestQueue class is a service to serialize the XML HTTP
     * request communication from the client to the server.
     */
    myfaces._TrRequestQueue = function(domWindow)
    {
        this._state = myfaces._TrRequestQueue.STATE_READY;
        this._requestQueue = new Array();
        // listeners that are interested in the state change of this service object
        this._stateChangeListeners = null;
        //this._iframeLoadCallback = undefined;

        // Stash away the DOM window for later reference.
        this._window = domWindow;
    }
    // Class constants
    myfaces._TrRequestQueue.STATE_READY = 0;
    myfaces._TrRequestQueue.STATE_BUSY = 1;
    // frame used for multi-part form post
    myfaces._TrRequestQueue._MULTIPART_FRAME = "_trDTSFrame";
    myfaces._TrRequestQueue._XMLHTTP_TYPE = 0;
    myfaces._TrRequestQueue._MULTIPART_TYPE = 1;
    myfaces._TrRequestQueue.prototype.dispose = function()
    {
        // TODO aschwart
        // Check for outstanding requests?
        this._requestQueue = null;
        this._stateChangeListeners = null;
        this._window = null;
    }
    myfaces._TrRequestQueue._RequestItem = function(
            type,
            context,
            actionURL,
            headerParams,
            content,
            method
            )
    {
        this._type = type;
        this._context = context;
        this._actionURL = actionURL;
        this._headerParams = headerParams;
        this._content = content;
        this._method = method;
    }
    myfaces._TrRequestQueue.prototype._broadcastRequestStatusChanged = function(
            context, currMethod, event)
    {
        if (currMethod)
        {
            try
            {
                currMethod.call(context, event);
            }
            catch (e)
            {
                myfaces._TrRequestQueue._logError(
                        "Error ", e, " delivering XML request status changed to ",
                        currMethod);
            }
        }
    }
    myfaces._TrRequestQueue.prototype._addRequestToQueue = function(
            type,
            context,
            listener,
            actionURL,
            content,
            headerParams
            )
    {
        var newRequest = new myfaces._TrRequestQueue._RequestItem(
                type, context, actionURL, headerParams, content, listener);
        this._requestQueue.push(newRequest);
        try
        {
            var dtsRequestEvent = new myfaces._TrXMLRequestEvent(
                    myfaces._TrXMLRequestEvent.STATUS_QUEUED,
                    null); // no xmlhttp object at this time
            this._broadcastRequestStatusChanged(context, listener, dtsRequestEvent);
        }
        catch(e)
        {
            myfaces._TrRequestQueue._logError("Error on listener callback invocation - STATUS_QUEUED", e);
        }
        if (this._state == myfaces._TrRequestQueue.STATE_READY)
        {
            this._state = myfaces._TrRequestQueue.STATE_BUSY;
            this._broadcastStateChangeEvent(myfaces._TrRequestQueue.STATE_BUSY);
            this._doRequest();
        }
    }
    //
    // HTML-specific API: consider refactoring into a separate class
    //
    /**
     * Send a form post
     */
    myfaces._TrRequestQueue.prototype.sendFormPost = function(
            context,
            method,
            actionForm,
            params,
            headerParams
            )
    {
        if (this._isMultipartForm(actionForm))
        {
            // TODO: log a warning if we're dropping any headers?  Or
            // come up with a hack to send "headers" via a multipart request?
            this.sendMultipartRequest(context, method, actionForm.action, actionForm, params);
        }
        else
        {
            var content = this._getPostbackContent(actionForm, params);
            // IE BUG, see TRINIDAD-704
            if (_agent.isIE && window.external)
                window.external.AutoCompleteSaveForm(actionForm);
            this.sendRequest(context, method, actionForm.action, content, headerParams);
        }
    }
    /**
     * Returns true if the form has a "file" input that contains
     * anything to upload.
     */
    myfaces._TrRequestQueue.prototype._isMultipartForm = function(actionForm)
    {
        // If there not enough DOM support, namely getElementsByTagName() being
        // not supported, this function does not work. Return false for such case.
        if (!_agent.supportsDomDocument)
        {
            return false;
        }
        // Use enctype - supported on IE >= 6, Moz, and Safari.
        // encoding is not supported on Safari.
        if (actionForm.enctype.toLowerCase() != "multipart/form-data")
            return false;
        var inputs = actionForm.getElementsByTagName("input"),
                inputCount = inputs.length, multiPartForm = null;
        for (var i = 0; i < inputCount; ++i)
        {
            var inputElem = inputs[i];
            if (inputElem.type == "file" && inputElem.value)
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns the payload to include in the rich postback
     * @param actionForm {Element} Form to build up input elements for post in
     * @param params {Object} Name/value pairs to ensure that the form contains
     * @return Content encoded correctly in String form
     */
    myfaces._TrRequestQueue.prototype._getPostbackContent = function(actionForm, params)
    {
       return myfaces._JSF2Utils.getPostbackContent(actionForm, params)
    }

    //
    // Generic API
    //

    /**
     * Performs Asynchronous XML HTTP Request with the Server
     * @param context    any object that is sent back to the callback when the request
     *  is complete. This object can be null.
     * @param method   Javascript method
     * @param actionURL   the url to send the request to
     * @param headerParams  Option HTTP header parameters to attach to the request
     * @param content     the content of the Asynchronous XML HTTP Post
     */
    myfaces._TrRequestQueue.prototype.sendRequest = function(
            context,
            method,
            actionURL,
            content,
            headerParams
            )
    {
        this._addRequestToQueue(myfaces._TrRequestQueue._XMLHTTP_TYPE, context, method, actionURL, content, headerParams);
    }
    /**
     * Performs Asynchronous HTTP Request with the Server for multipart data
     * @param context    any object that is sent back to the callback when the request
     *  is complete. This object can be null.
     * @param actionURL  this is the appropriate action url
     * @param htmlForm    the form containing multi-part data. The action attribute
     *   of the form is used for send the request to the server
     * @param params     additional parameters that need to be sent to the server
     * @param method   Javascript method
     */
    myfaces._TrRequestQueue.prototype.sendMultipartRequest = function(
            context,
            method,
            actionURL,
            htmlForm,
            params
            )
    {
        var privateContext =
        {"htmlForm":htmlForm, "params": params, "context": context, "method": method};
        this._addRequestToQueue(myfaces._TrRequestQueue._MULTIPART_TYPE, privateContext, null, actionURL);
    }
    myfaces._TrRequestQueue.prototype._doRequest = function()
    {
        // currently we are posting only one request at a time. In future we may batch
        // mutiple requests in one post
        var requestItem = this._requestQueue.shift();
        switch (requestItem._type)
                {
            case myfaces._TrRequestQueue._XMLHTTP_TYPE:
                this._doXmlHttpRequest(requestItem);
                break;

            case myfaces._TrRequestQueue._MULTIPART_TYPE:
                this._doRequestThroughIframe(requestItem);
                break;
        }
    }
    myfaces._TrRequestQueue.prototype._doXmlHttpRequest = function(requestItem)
    {
        var xmlHttp = new myfaces._TrXMLRequest();
        xmlHttp.__dtsRequestContext = requestItem._context;
        xmlHttp.__dtsRequestMethod = requestItem._method;
        var callback = myfaces._JSF2Utils.hitch(this, this._handleRequestCallback);//TrUIUtils.createCallback(this, this._handleRequestCallback);
        xmlHttp.setCallback(callback);
        // xmlhttp request uses the same charset as its parent document's charset.
        // There is no need to set the charset.
        xmlHttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        var headerParams = requestItem._headerParams;
        if (headerParams != null)
        {
            for (var headerName in headerParams)
            {
                var currHeader = headerParams[headerName];
                // handle array parameters by joining them together with comma separators
                // Test if it's an array via the "join" method
                if (currHeader["join"])
                    currHeader = currHeader.join(',')
                xmlHttp.setRequestHeader(headerName, currHeader);
            }
        }
        xmlHttp.send(requestItem._actionURL, requestItem._content);
    }
    myfaces._TrRequestQueue.prototype._doRequestThroughIframe = function(requestItem)
    {
        var htmlForm = requestItem._context.htmlForm;
        var actionURL = requestItem._actionURL;
        var params = requestItem._context.params;
        // assert(htmlForm.action, "form action cannot be null for multiform post");
        var frameName = myfaces._TrRequestQueue._MULTIPART_FRAME;
        var domDocument = this._getDomDocument();
        var hiddenFrame = domDocument.getElementById(frameName), iframeDoc;
        var agentIsIE = _agent.isIE;
        if (!hiddenFrame)
        {
            hiddenFrame = domDocument.createElement('iframe');
            hiddenFrame.name = frameName;
            hiddenFrame.id = frameName;
            var frameStyle = hiddenFrame.style;
            frameStyle.top = frameStyle.left = '0px';
            frameStyle.width = frameStyle.height = '1px'
            frameStyle.position = 'absolute';
            frameStyle.visibility = "hidden";
            domDocument.body.appendChild(hiddenFrame);
        }
        if (agentIsIE)
        {
            // Why these lines happen to work, I can't say - but remove them,
            // and the postback actually goes to a new window
            hiddenFrame = domDocument.frames[frameName];
            hiddenFrame.name = frameName;
            iframeDoc = hiddenFrame.document;
        }
        else if (_agent.isSafari)
        {
            iframeDoc = hiddenFrame.document;
        }
        else
        {
            iframeDoc = hiddenFrame.contentDocument;
        }
        // We may not have a document yet for the IFRAME, since
        // nothing has been loaded (appears to work this way on Safari)
        if (iframeDoc && iframeDoc.firstChild)
            iframeDoc.removeChild(iframeDoc.firstChild);
        // store our context variables for later use
        this._dtsContext = requestItem._context.context;
        this._dtsRequestMethod = requestItem._context.method;
        this._htmlForm = htmlForm;
        this._savedActionUrl = htmlForm.action;
        this._savedTarget = htmlForm.target;
        // FIXME: why are the next two lines at all necessary?  The form
        // should already be set to post, and if the action has been
        // updated since this was queued
        htmlForm.method = "POST";
        htmlForm.action = actionURL;
        htmlForm.target = frameName;
        this._appendParamNode(domDocument, htmlForm, "Tr-XHR-Message", "true");
        // FIXME: the "partial" parameter is unnecessary
        this._appendParamNode(domDocument, htmlForm, "partial", "true");
        if (params)
        {
            for (var key in params)
            {
                this._appendParamNode(domDocument, htmlForm, key, params[key]);
            }
        }
        if (this._iframeLoadCallback == null)
            this._iframeLoadCallback = myfaces._JSF2Utils.hitch(this,this._handleIFrameLoad);//TrUIUtils.createCallback(this, this._handleIFrameLoad);
        // IE BUG, see TRINIDAD-704
        if (_agent.isIE && window.external)
            window.external.AutoCompleteSaveForm(htmlForm);
        htmlForm.submit();
        this._window.setTimeout(this._iframeLoadCallback, 50);
    }
    myfaces._TrRequestQueue.prototype._appendParamNode = function(domDocument, form, name, value)
    {
        // assert(form!=null);
        var nodes = this._paramNodes;
        if (!nodes)
        {
            nodes = new Array();
            this._paramNodes = nodes;
        }
        var node = domDocument.createElement("input");
        node.type = "hidden";
        node.name = name;
        node.value = value;
        nodes.push(node);
        form.appendChild(node);
    }
    myfaces._TrRequestQueue.prototype._clearParamNodes = function()
    {
        var nodes = this._paramNodes;
        if (nodes)
        {
            var form = nodes[0].parentNode;
            var count = nodes.length;
            for (var i = 0; i < count; i++)
            {
                form.removeChild(nodes[i]);
            }
            delete this._paramNodes;
        }
    }
    myfaces._TrRequestQueue.prototype._handleIFrameLoad = function()
    {
        var domDocument = this._getDomDocument();
        var agentIsIE = _agent.isIE;
        var frameName = myfaces._TrRequestQueue._MULTIPART_FRAME;
        var hiddenFrame, iframeDoc;
        if (agentIsIE)
        {
            hiddenFrame = domDocument.frames[frameName];
            var iframeDoc = hiddenFrame.document;
        }
        else
        {
            hiddenFrame = domDocument.getElementById(frameName);
            iframeDoc = hiddenFrame.contentDocument;
        }
        try
        {
            if (!iframeDoc.documentElement || !iframeDoc.documentElement.firstChild
                    || (agentIsIE && iframeDoc.readyState != "complete"))
            {
                this._window.setTimeout(this._iframeLoadCallback, 50);
            }
            else
            {
                this._onIFrameLoadComplete(iframeDoc, this._dtsContext,
                        this._dtsRequestMethod);
            }
        }
        catch(e)
        {
            myfaces._TrRequestQueue._alertError();
            myfaces._TrRequestQueue._logError("myfaces._TrRequestQueue.prototype._handleIFrameLoad ", "Error while performing request", e);
            this._htmlForm.action = this._savedActionUrl;
            this._htmlForm.target = this._savedTarget;
        }
    }
    myfaces._TrRequestQueue.prototype._onIFrameLoadComplete = function(
            iframeDoc,
            context,
            requestMethod)
    {
        try
        {
            var dtsRequestEvent = new TrIFrameXMLRequestEvent(
                    iframeDoc);
            this._broadcastRequestStatusChanged(context, requestMethod, dtsRequestEvent);
        }
        finally
        {
            //cleanup
            if (iframeDoc.firstChild)
                iframeDoc.removeChild(iframeDoc.firstChild);
            this._htmlForm.action = this._savedActionUrl;
            this._htmlForm.target = this._savedTarget;
            //clear the parameter nodes
            this._clearParamNodes();
            this._requestDone();
        }
    }
    myfaces._TrRequestQueue.prototype._handleRequestCallback = function(
            xmlHttp
            )
    {
        var httpState = xmlHttp.getCompletionState();
        if (httpState != myfaces._TrXMLRequest.COMPLETED)
            return;
        var statusCode = 0;
        var failedConnectionText = myfaces._TrRequestQueue._getFailedConnectionText();
        try
        {
            statusCode = xmlHttp.getStatus();
        }
        catch(e)
        {
            // Drop the exception without logging anything.
            // Firefox will throw an exception on attempting
            // to get the status of an XMLHttpRequest if
            // the Http connection  has been closed
        }
        if ((statusCode != 200) && (statusCode != 0))
        {
            //TODO check if the alert here is still needed?
            myfaces._TrRequestQueue._alertError();
            myfaces._TrRequestQueue._logError("myfaces._TrRequestQueue.prototype._handleRequestCallback","Error StatusCode(",
                    statusCode,
                    ") while performing request\n",
                    xmlHttp.getResponseText());
            //TODO check the request callback context if the onerror has to
            //be applied here

        }
        try
        {
            if (statusCode != 0)
            {
                var dtsRequestEvent = new myfaces._TrXMLRequestEvent(
                        myfaces._TrXMLRequestEvent.STATUS_COMPLETE,
                        xmlHttp);
                this._broadcastRequestStatusChanged(
                        xmlHttp.__dtsRequestContext,
                        xmlHttp.__dtsRequestMethod,
                        dtsRequestEvent);
            }
        }
        finally
        {
            //cleanup
            xmlHttp.cleanup();
            delete xmlHttp;
            this._requestDone();
        }
    }
    myfaces._TrRequestQueue.prototype._requestDone = function()
    {
        if (this._requestQueue.length > 0)
        {
            // send the next one in the queue
            this._doRequest();
        }
        else
        {
            // Reset our state to recieve more requests
            this._state = myfaces._TrRequestQueue.STATE_READY;
            this._broadcastStateChangeEvent(myfaces._TrRequestQueue.STATE_READY);
        }
    }
    /**
     * Adds a listener to the request queue that is interested in its state change.
     * The listners are notified in the order that they are added. A listener can cancel
     * notification to other listeners in the chain by returning false.
     *
     * @param {function} listener  listener function to remove
     * @param {object} instance to pass as this when calling function
     */
    myfaces._TrRequestQueue.prototype.addStateChangeListener = function(listener, instance)
    {
        // assertFunction(listener);
        // assertObjectOrNull(instance);
        var stateChangeListeners = this._stateChangeListeners;
        if (!stateChangeListeners)
        {
            stateChangeListeners = new Array();
            this._stateChangeListeners = stateChangeListeners;
        }
        stateChangeListeners.push(listener);
        stateChangeListeners.push(instance);
    }
    /**
     * Removes a listener from the request queue that is interested in its state change.
     * @param {function} listener  listener function to remove
     * @param {object} instance to pass as this when calling function
     */
    myfaces._TrRequestQueue.prototype.removeStateChangeListener = function(listener, instance)
    {
        // assertFunction(listener);
        // assertObjectOrNull(instance);

        // remove the listener/instance combination
        var stateChangeListeners = this._stateChangeListeners;
        // assert(stateChangeListeners, "stateChangeListeners must exist");
        var length = stateChangeListeners.length;
        for (var i = 0; i < length; i++)
        {
            var currListener = stateChangeListeners[i];
            i++;
            if (currListener == listener)
            {
                var currInstance = stateChangeListeners[i];
                if (currInstance === instance)
                {
                    stateChangeListeners.splice(i - 1, 2);
                }
            }
        }
        // remove array, if empty
        if (stateChangeListeners.length == 0)
        {
            this._stateChangeListeners = null;
        }
    }
    /**
     * Return the current DTS state.
     * return (int) _state
     */
    myfaces._TrRequestQueue.prototype.getDTSState = function()
    {
        return this._state;
    }
    /**
     * broadcast the state change of the request queue to its listeners
     */
    myfaces._TrRequestQueue.prototype._broadcastStateChangeEvent = function(state)
    {
        var stateChangeListeners = this._stateChangeListeners;
        // deliver the state change event to the listeners
        if (stateChangeListeners)
        {
            var listenerCount = stateChangeListeners.length;
            for (var i = 0; i < listenerCount; i++)
            {
                try
                {
                    var currListener = stateChangeListeners[i];
                    i++;
                    var currInstance = stateChangeListeners[i];
                    if (currInstance != null)
                        currListener.call(currInstance, state);
                    else
                        currListener(state);
                }
                catch (e)
                {
                    myfaces._TrRequestQueue._logError("myfaces._TrRequestQueue.prototype._broadcastStateChangeEvent","Error on DTS State Change Listener", e);
                }
            }
        }
    }
    myfaces._TrRequestQueue.prototype._getDomDocument = function()
    {
        return this._window.document;
    }
    myfaces._TrRequestQueue._getFailedConnectionText = function()
    {
        // TODO: get translated connection information
        return "Connection failed";
    }
    myfaces._TrRequestQueue._alertError = function()
    {
        // TODO: get translated connection information
        var failedConnectionText = myfaces._TrRequestQueue._getFailedConnectionText();
        if (failedConnectionText != null)
            alert(failedConnectionText);
    }
    // Logging helper for use in Firebug
    myfaces._TrRequestQueue._logWarning = function(varArgs)
    {
       myfaces._JSF2Utils.logWarning(arguments);
    }
    // Logging helper for use in Firebug
    myfaces._TrRequestQueue._logError = function(varArgs)
    {
         myfaces._JSF2Utils.logError(arguments);
    }
}