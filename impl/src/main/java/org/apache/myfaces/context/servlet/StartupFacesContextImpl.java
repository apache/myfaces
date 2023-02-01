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
    private boolean startup;
    
    public StartupFacesContextImpl(
            ExternalContext externalContext, 
            ExternalContext defaultExternalContext,
            ExceptionHandler exceptionHandler,
            boolean startup)
    {
        // setCurrentInstance is called in constructor of super class
        super(externalContext, defaultExternalContext);

        this.startup = startup;
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
        throw unsupportedOperation();
    }
    
    @Override
    public List<FacesMessage> getMessageList()
    {
        assertNotReleased();
        throw unsupportedOperation();
    }

    @Override
    public List<FacesMessage> getMessageList(String clientId)
    {
        assertNotReleased();
        throw unsupportedOperation();
    }

    @Override
    public Iterator<FacesMessage> getMessages()
    {
        assertNotReleased();
        throw unsupportedOperation();
    }
    
    
    @Override
    public Iterator<String> getClientIdsWithMessages()
    {
        assertNotReleased();
        throw unsupportedOperation();
    }

    @Override
    public Iterator<FacesMessage> getMessages(String clientId)
    {
        assertNotReleased();
        throw unsupportedOperation();
    }

    @Override
    public void addMessage(String clientId, FacesMessage message)
    {
        assertNotReleased();
        throw unsupportedOperation();
    }

    @Override
    public PartialViewContext getPartialViewContext()
    {
        assertNotReleased();
        throw unsupportedOperation();
    }
    
    @Override
    public boolean isPostback()
    {
        assertNotReleased();
        throw unsupportedOperation();
    }
    
    @Override
    public void validationFailed()
    {
        assertNotReleased();
        throw unsupportedOperation();
    }

    @Override
    public boolean isValidationFailed()
    {
        assertNotReleased();
        throw unsupportedOperation();
    }

    @Override
    public void renderResponse()
    {
        assertNotReleased();
        throw unsupportedOperation();
    }

    @Override
    public void responseComplete()
    {
        assertNotReleased();
        throw unsupportedOperation();
    }

    @Override
    public PhaseId getCurrentPhaseId()
    {
        assertNotReleased();
        throw unsupportedOperation();
    }
   
    @Override
    public void setCurrentPhaseId(PhaseId currentPhaseId)
    {
        assertNotReleased();
        throw unsupportedOperation();
    }
        
    @Override
    public boolean getRenderResponse()
    {
        assertNotReleased();
        throw unsupportedOperation();
    }

    @Override
    public boolean getResponseComplete()
    {
        assertNotReleased();
        throw unsupportedOperation();
    }

    @Override
    public void setResponseStream(ResponseStream responseStream)
    {
        assertNotReleased();
        throw unsupportedOperation();
    }

    @Override
    public ResponseStream getResponseStream()
    {
        assertNotReleased();
        throw unsupportedOperation();
    }

    @Override
    public void setResponseWriter(ResponseWriter responseWriter)
    {
        assertNotReleased();
        throw unsupportedOperation();
    }

    @Override
    public ResponseWriter getResponseWriter()
    {
        assertNotReleased();
        throw unsupportedOperation();
    }

    @Override
    public Lifecycle getLifecycle()
    {
        return null;
    }

    private UnsupportedOperationException unsupportedOperation()
    {
        return new UnsupportedOperationException("This method is not supported during "
                + (startup ? "startup" : "shutdown"));
    }
}
