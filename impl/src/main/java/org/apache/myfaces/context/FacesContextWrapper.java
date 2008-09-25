/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.context;

import javax.el.ELContext;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import java.util.Iterator;


/**
 * Convenient class to wrap the current FacesContext.
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Anton Koinov
 * @version $Revision$ $Date$
 */
public class FacesContextWrapper
    extends FacesContext
{
    //~ Instance fields ----------------------------------------------------------------------------

    private FacesContext _facesContext;

    //~ Constructors -------------------------------------------------------------------------------

    public FacesContextWrapper(FacesContext facesContext)
    {
        _facesContext = facesContext;
    }

    //~ Methods ------------------------------------------------------------------------------------

    public Application getApplication()
    {
        return _facesContext.getApplication();
    }

    public Iterator<String> getClientIdsWithMessages()
    {
        return _facesContext.getClientIdsWithMessages();
    }

    public ExternalContext getExternalContext()
    {
        return _facesContext.getExternalContext();
    }

    public Severity getMaximumSeverity()
    {
        return _facesContext.getMaximumSeverity();
    }

    public Iterator<FacesMessage> getMessages()
    {
        return _facesContext.getMessages();
    }

    public Iterator<FacesMessage> getMessages(String clientId)
    {
        return _facesContext.getMessages(clientId);
    }

    public RenderKit getRenderKit()
    {
        return _facesContext.getRenderKit();
    }

    public boolean getRenderResponse()
    {
        return _facesContext.getRenderResponse();
    }

    public boolean getResponseComplete()
    {
        return _facesContext.getResponseComplete();
    }

    public void setResponseStream(ResponseStream responsestream)
    {
        _facesContext.setResponseStream(responsestream);
    }

    public ResponseStream getResponseStream()
    {
        return _facesContext.getResponseStream();
    }

    public void setResponseWriter(ResponseWriter responsewriter)
    {
        _facesContext.setResponseWriter(responsewriter);
    }

    public ResponseWriter getResponseWriter()
    {
        return _facesContext.getResponseWriter();
    }

    public void setViewRoot(UIViewRoot viewRoot)
    {
        _facesContext.setViewRoot(viewRoot);
    }

    public UIViewRoot getViewRoot()
    {
        return _facesContext.getViewRoot();
    }

    public void addMessage(String clientId, FacesMessage message)
    {
        _facesContext.addMessage(clientId, message);
    }

    public void release()
    {
        _facesContext.release();
    }

    public void renderResponse()
    {
        _facesContext.renderResponse();
    }

    public void responseComplete()
    {
        _facesContext.responseComplete();
    }
    
    public ELContext getELContext()
    {
        return _facesContext.getELContext();
    }
}
