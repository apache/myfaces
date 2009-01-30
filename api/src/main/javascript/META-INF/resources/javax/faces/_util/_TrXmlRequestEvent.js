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

/**
 * AJAX Request Event class. This object is passed back to the listeners
 * of a AJAX Service Request. It support Imyfaces._TrXMLRequestEvent pseudo-interface
 * with the following methods: getStatus, getResponseXML, getResponseText,
 * isPprResponse, getResponseContentType
 */
_reserveMyfaces();
if ('undefined' == typeof(myfaces._TrXMLRequestEvent)) {
    myfaces._TrXMLRequestEvent = function(
        status,
        request
        )
        {
        this._status = status;
        this._request = request;
    };
    myfaces._TrXMLRequestEvent.STATUS_QUEUED = 1;
    myfaces._TrXMLRequestEvent.STATUS_SEND_BEFORE = 2;
    myfaces._TrXMLRequestEvent.STATUS_SEND_AFTER = 3;
    myfaces._TrXMLRequestEvent.STATUS_COMPLETE = 4;
    myfaces._TrXMLRequestEvent.prototype.getStatus = function()
    {
        return this._status;
    };
    /**
     *
     */
    /**
     * getter for the request, because jsf 2.0 wants to work directly on the object not the facade
     */
    myfaces._TrXMLRequestEvent.prototype.getRequest = function()
    {
        return this._request;
    };
    /**
     * Returns the response of the AJAX Request as an XML document
     * NOTE: this method is valid only for myfaces._TrXMLRequestEvent.STATUS_COMPLETE
     **/
    myfaces._TrXMLRequestEvent.prototype.getResponseXML = function()
    {
        return this._request.getResponseXML();
    };
    /**
     * Returns true if the response XML of the AJAX Request is valid.
     * NOTE: this method is valid only for myfaces._TrXMLRequestEvent.STATUS_COMPLETE
     **/
    myfaces._TrXMLRequestEvent.prototype._isResponseValidXML = function()
    {
        // Note: Mozilla applies default XSLT to XML parse error
        var responseDocument = this._request.getResponseXML();
        if (!responseDocument)
            return false;
        var docElement = responseDocument.documentElement;
        if (!docElement)
            return false;
        var nodeName = docElement.nodeName;
        if (!nodeName)
            nodeName = docElement.tagName;
        if (nodeName == "parsererror")
            return false;
        return true;
    };
    /**
     * Returns the response of the AJAX Request as text.
     * NOTE: this method is valid only for myfaces._TrXMLRequestEvent.STATUS_COMPLETE
     **/
    myfaces._TrXMLRequestEvent.prototype.getResponseText = function()
    {
        return this._request.getResponseText();
    };
    /**
     * Returns the status code of the xml http AJAX Request.
     * NOTE: this method is valid only for myfaces._TrXMLRequestEvent.STATUS_COMPLETE
     **/
    myfaces._TrXMLRequestEvent.prototype.getResponseStatusCode = function()
    {
        return this._request.getStatus();
    };
    /**
     * Returns all the response headers for xml http AJAX Request.
     * NOTE: this method is valid only for myfaces._TrXMLRequestEvent.STATUS_COMPLETE
     **/
    myfaces._TrXMLRequestEvent.prototype._getAllResponseHeaders = function()
    {
        return this._request.getAllResponseHeaders();
    };
    /**
     * Returns a particular response header for xml http Request.
     * NOTE: this method is valid only for myfaces._TrXMLRequestEvent.STATUS_COMPLETE
     **/
    myfaces._TrXMLRequestEvent.prototype.getResponseHeader = function(
        name
        )
        {
        // Note: Mozilla chokes when we ask for a response header that does not exist
        var allHeaders = this._request.getAllResponseHeaders();
        return (allHeaders.indexOf(name) != -1) ?
        this._request.getResponseHeader(name)
        : null;
    };
    /**
     * Returns if whether if is a rich response
     * NOTE: this method is valid only for myfaces._TrXMLRequestEvent.STATUS_COMPLETE
     **/
    // TODO: this should likely be deleted or renamed, as it is
    // not PPR-specific here
    myfaces._TrXMLRequestEvent.prototype.isPprResponse = function()
    {
        // todo: do we need to mark rich responses?
        // var responseType = this.getResponseHeader("Tr-XHR-Response-Type");
        var isrich = true;
        if (isrich && (!this._isResponseValidXML()))
        {
            myfaces._TrRequestQueue._logError("Invalid PPR response." +
                " The response-headers were:\n" +
                this._getAllResponseHeaders() +
                "\n The invalid response was:\n" +
                this.getResponseText());
        }
        return isrich;
    };
    /**
     * Returns if whether if is a rich response
     * NOTE: this method is valid only for myfaces._TrXMLRequestEvent.STATUS_COMPLETE
     **/
    myfaces._TrXMLRequestEvent.prototype.getResponseContentType = function()
    {
        this.getResponseHeader("Content-Type");
    };
}    