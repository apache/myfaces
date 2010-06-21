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
import java.util.Locale;
import java.util.Map;

import javax.el.ELContext;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.event.PhaseId;
import javax.faces.render.RenderKit;
import javax.servlet.ServletContext;

/**
 * A FacesContext implementation which will be set as the current instance
 * during container startup and shutdown and which provides a basic set of
 * FacesContext functionality.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class StartupFacesContextImpl extends FacesContext
{
    
    public static final String EXCEPTION_TEXT = "This method is not supported during ";
    
    private boolean _startup;
    private boolean _released = false;
    private Application _application;
    private StartupServletExternalContextImpl _externalContext; // use real type for call to release()
    private UIViewRoot _viewRoot;
    
    public StartupFacesContextImpl(boolean startup, ServletContext servletContext)
    {
        _startup = startup;   
        _externalContext = new StartupServletExternalContextImpl(startup, servletContext);
        
        // this FacesContext impl is now the current instance
        // note that because this method is protected, it has to be called from here
        FacesContext.setCurrentInstance(this);
    }

    // ~ Methods which are valid to be called during startup and shutdown------
    
    /**
     * If called during application startup or shutdown, this method returns
     * the correct current Application instance.
     */
    @Override
    public Application getApplication()
    {
        assertNotReleased();
        
        if (_application == null)
        {
            _application = ((ApplicationFactory) FactoryFinder.getFactory(
                    FactoryFinder.APPLICATION_FACTORY)).getApplication();
        }
        
        return _application;
    }
    
    /**
     * If called during application startup or shutdown, this method returns an
     * ExternalContext instance with the special behaviors indicated in the 
     * javadoc for that class. Methods document as being valid to call during 
     * application startup or shutdown must be supported.
     */
    @Override
    public ExternalContext getExternalContext()
    {
        assertNotReleased();
        
        return _externalContext;
    }
    
    /**
     * If called during application startup or shutdown, this method returns a 
     * new UIViewRoot with its locale set to Locale.getDefault().
     */
    @Override
    public UIViewRoot getViewRoot()
    {
        assertNotReleased();
        
        if (_viewRoot == null)
        {
            _viewRoot = new UIViewRoot();
            _viewRoot.setLocale(Locale.getDefault());
        }
        
        return _viewRoot;
    }
    
    @Override
    public void release()
    {
        assertNotReleased();
        
        _externalContext.release();
        _externalContext = null;
        _application = null;
        _viewRoot = null;
        
        _released = true;
        FacesContext.setCurrentInstance(null);
    }
    
    // Note that isProjectStage() also is valid to be called, but this method
    // is already implemented in FacesContext class.
    
    // ~ Methods which can be called during startup and shutdown, but are not
    //   officially supported by the spec--------------------------------------
    
    @Override
    public boolean isProcessingEvents()
    {
        return true;
    }
    
    // ~ Methods which are unsupported during startup and shutdown-------------
    
    @Override
    public void addMessage(String clientId, FacesMessage message)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Iterator<String> getClientIdsWithMessages()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Severity getMaximumSeverity()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Iterator<FacesMessage> getMessages()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Iterator<FacesMessage> getMessages(String clientId)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public RenderKit getRenderKit()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public boolean getRenderResponse()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public boolean getResponseComplete()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public ResponseStream getResponseStream()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public ResponseWriter getResponseWriter()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void renderResponse()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void responseComplete()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setResponseStream(ResponseStream responseStream)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setResponseWriter(ResponseWriter responseWriter)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setViewRoot(UIViewRoot root)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Map<Object, Object> getAttributes()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public PhaseId getCurrentPhaseId()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public ELContext getELContext()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public ExceptionHandler getExceptionHandler()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public List<FacesMessage> getMessageList()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public List<FacesMessage> getMessageList(String clientId)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public PartialViewContext getPartialViewContext()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public boolean isPostback()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public boolean isValidationFailed()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setCurrentPhaseId(PhaseId currentPhaseId)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setExceptionHandler(ExceptionHandler exceptionHandler)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setProcessingEvents(boolean processingEvents)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void validationFailed()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }
    
    // ~ private Methods ------------------------------------------------------
    
    /**
     * Returns startup or shutdown as String according to the field _startup.
     * @return
     */
    private String _getTime()
    {
        return _startup ? "startup" : "shutdown";
    }
    
    /**
     * has to be thrown in many of the methods if the method is called after
     * the instance has been released!
     */
    private final void assertNotReleased()
    {
        if (_released)
        {
            throw new IllegalStateException(
                    "Error the FacesContext is already released!");
        }
    }
    
}
