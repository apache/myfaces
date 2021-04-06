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
package org.apache.myfaces.context.servlet;

import java.util.Iterator;
import java.util.List;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.PhaseId;
import jakarta.faces.lifecycle.Lifecycle;

/**
 * A FacesContext implementation which will be set as the current instance
 * during container startup and shutdown and which provides a basic set of
 * FacesContext functionality.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class StartupFacesContextImpl extends FacesContextImplBase
{
    private boolean _startup;
    
    public StartupFacesContextImpl(
            ExternalContext externalContext, 
            ExternalContext defaultExternalContext,
            ExceptionHandler exceptionHandler,
            boolean startup)
    {
        // setCurrentInstance is called in constructor of super class
        super(externalContext, defaultExternalContext);
        
        _startup = startup;
        setExceptionHandler(exceptionHandler);
    }

    // ~ Methods which are valid by spec to be called during startup and shutdown------
    
    // public UIViewRoot getViewRoot() implemented in super-class
    // public void release() implemented in super-class
    // public ExternalContext getExternalContext() implemented in super-class
    // public Application getApplication() implemented in super-class
    // public boolean isProjectStage(ProjectStage stage) implemented in super-class
    
    // ~ Methods which can be called during startup and shutdown, but are not
    //   officially supported by the spec--------------------------------------
    
    // all other methods on FacesContextImplBase
    
    // ~ Methods which are unsupported during startup and shutdown-------------

    @Override
    public FacesMessage.Severity getMaximumSeverity()
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }
    
    @Override
    public List<FacesMessage> getMessageList()
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }

    @Override
    public List<FacesMessage> getMessageList(String clientId)
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }

    @Override
    public Iterator<FacesMessage> getMessages()
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }
    
    
    @Override
    public Iterator<String> getClientIdsWithMessages()
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }

    @Override
    public Iterator<FacesMessage> getMessages(String clientId)
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }

    @Override
    public void addMessage(String clientId, FacesMessage message)
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }

    @Override
    public PartialViewContext getPartialViewContext()
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }
    
    @Override
    public boolean isPostback()
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }
    
    @Override
    public void validationFailed()
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }

    @Override
    public boolean isValidationFailed()
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }

    @Override
    public void renderResponse()
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }

    @Override
    public void responseComplete()
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }

    @Override
    public PhaseId getCurrentPhaseId()
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }
   
    @Override
    public void setCurrentPhaseId(PhaseId currentPhaseId)
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }
        
    @Override
    public boolean getRenderResponse()
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }

    @Override
    public boolean getResponseComplete()
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }

    @Override
    public void setResponseStream(ResponseStream responseStream)
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }

    @Override
    public ResponseStream getResponseStream()
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }

    @Override
    public void setResponseWriter(ResponseWriter responseWriter)
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }

    @Override
    public ResponseWriter getResponseWriter()
    {
        assertNotReleased();
        throw constructUnsupportedOperationException();
    }

    @Override
    public Lifecycle getLifecycle()
    {
        return null;
    }

    private UnsupportedOperationException constructUnsupportedOperationException()
    {
        throw new UnsupportedOperationException("This method is not supported during "
                + (_startup ? "startup" : "shutdown"));
    }
}
