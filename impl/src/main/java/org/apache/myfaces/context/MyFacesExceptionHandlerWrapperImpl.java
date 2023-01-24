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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.FacesException;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.faces.event.SystemEvent;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.myfaces.lifecycle.ViewNotFoundException;
import org.apache.myfaces.renderkit.ErrorPageWriter;
import org.apache.myfaces.util.WebConfigParamUtils;
import org.apache.myfaces.spi.WebConfigProvider;
import org.apache.myfaces.spi.WebConfigProviderFactory;

/**
 * Extended MyFaces-specific ExceptionHandler implementation. 
 * 
 * @author Leonardo Uribe
 *
 */
public class MyFacesExceptionHandlerWrapperImpl extends ExceptionHandlerWrapper
{
    private static final Logger log = Logger.getLogger(MyFacesExceptionHandlerWrapperImpl.class.getName());
    
    private Queue<ExceptionQueuedEvent> handled;
    private Queue<ExceptionQueuedEvent> unhandled;
    private ExceptionQueuedEvent handledAndThrown;

    private ExceptionHandler delegate;
    private boolean errorPagePresent;
    private boolean useMyFacesErrorHandling;
    private boolean initialized;

    public MyFacesExceptionHandlerWrapperImpl(ExceptionHandler delegate)
    {
        this.delegate = delegate;
        this.initialized = false;
    }
    
    protected void init()
    {
        if (!initialized)
        {
            init(FacesContext.getCurrentInstance());
        }
    }
    
    protected void init(FacesContext facesContext)
    {
        if (!initialized)
        {
            if (facesContext == null)
            {
                facesContext = FacesContext.getCurrentInstance();
            }
            WebConfigProvider webConfigProvider = WebConfigProviderFactory.getWebConfigProviderFactory(
                    facesContext.getExternalContext()).getWebConfigProvider(facesContext.getExternalContext());
    
            errorPagePresent = webConfigProvider.isErrorPagePresent(facesContext.getExternalContext());
            useMyFacesErrorHandling = WebConfigParamUtils.getBooleanInitParameter(facesContext.getExternalContext(),
                    ErrorPageWriter.ERROR_HANDLING_PARAMETER, facesContext.isProjectStage(ProjectStage.Development));
            initialized = true;
        }
    }
    
    protected void init(SystemEvent exceptionQueuedEvent)
    {
        if (!initialized)
        {
            if (exceptionQueuedEvent instanceof ExceptionQueuedEvent)
            {
                ExceptionQueuedEvent eqe = (ExceptionQueuedEvent)exceptionQueuedEvent;
                ExceptionQueuedEventContext eqec = eqe.getContext();
                if (eqec != null)
                {
                    FacesContext facesContext = eqec.getContext();
                    if (facesContext != null)
                    {
                        init(facesContext);
                        return;
                    }
                }
            }
            init(exceptionQueuedEvent.getFacesContext());
        }
    }
    
    protected boolean isUseMyFacesErrorHandling()
    {
        return useMyFacesErrorHandling;
    }
    
    protected boolean isErrorPagePresent()
    {
        return errorPagePresent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExceptionQueuedEvent getHandledExceptionQueuedEvent()
    {
        init();
        if (!isUseMyFacesErrorHandling())
        {
            return super.getHandledExceptionQueuedEvent();
        }
        else
        {
            return handledAndThrown;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<ExceptionQueuedEvent> getHandledExceptionQueuedEvents()
    {
        init();
        if (!isUseMyFacesErrorHandling())
        {
            return super.getHandledExceptionQueuedEvents();
        }
        else
        {
            return handled == null ? Collections.<ExceptionQueuedEvent>emptyList() : handled;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<ExceptionQueuedEvent> getUnhandledExceptionQueuedEvents()
    {
        init();
        if (!isUseMyFacesErrorHandling())
        {
            return super.getUnhandledExceptionQueuedEvents();
        }
        else
        {
            return unhandled == null ? Collections.<ExceptionQueuedEvent>emptyList() : unhandled;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle() throws FacesException
    {
        init();
        if (!isUseMyFacesErrorHandling())
        {
            if (isErrorPagePresent())
            {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                // save current view in the request map to access it on the error page
                facesContext.getExternalContext().getRequestMap().put(ErrorPageWriter.VIEW_KEY,
                                                                      facesContext.getViewRoot());
            }
            try
            {
                super.handle();
            }
            catch (FacesException e)
            {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                if (e.getCause() instanceof ViewNotFoundException)
                {
                    facesContext.getExternalContext().setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                else
                {
                    facesContext.getExternalContext().setResponseStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                throw e;
            }
            return;
        }
        else
        {
            if (unhandled != null && !unhandled.isEmpty())
            {
                if (handled == null)
                {
                    handled = new LinkedList<>();
                }

                List<Throwable> throwableList = new ArrayList<>();
                List<UIComponent> components = new ArrayList<>();
                FacesContext facesContext = null;

                do
                {
                    // For each ExceptionEvent in the list
                    
                    // get the event to handle
                    ExceptionQueuedEvent event = unhandled.peek();
                    try
                    {
                        // call its getContext() method
                        ExceptionQueuedEventContext context = event.getContext();
    
                        if (facesContext == null)
                        {
                            facesContext = event.getContext().getContext();
                        }
                        
                        // and call getException() on the returned result
                        Throwable exception = context.getException();
                        
                        // Upon encountering the first such Exception that is not an instance of
                        // jakarta.faces.event.AbortProcessingException
                        if (!shouldSkip(exception))
                        {
                            // set handledAndThrown so that getHandledExceptionQueuedEvent() returns this event
                            handledAndThrown = event;
                            
                            Throwable rootCause = getRootCause(exception);
                            
                            throwableList.add(rootCause == null ? exception : rootCause);
                            components.add(event.getContext().getComponent());
                            
                            //break;
                        }
                        else
                        {
                            // Testing mojarra it logs a message and the exception
                            // however, this behaviour is not mentioned in the spec
                            log.log(Level.SEVERE, exception.getClass().getName() + " occured while processing " +
                                    (context.inBeforePhase() ? "beforePhase() of " : 
                                            (context.inAfterPhase() ? "afterPhase() of " : "")) + 
                                    "phase " + context.getPhaseId() + ": " +
                                    "UIComponent-ClientId=" + 
                                    (context.getComponent() != null ? 
                                            context.getComponent().getClientId(context.getContext()) : "") + ", " +
                                    "Message=" + exception.getMessage());
                            
                            log.log(Level.SEVERE, exception.getMessage(), exception);
                        }
                    }
                    finally
                    {
                        // if we will throw the Exception or if we just logged it,
                        // we handled it in either way --> add to handled
                        handled.add(event);
                        unhandled.remove(event);
                    }
                } while (!unhandled.isEmpty());

                if (facesContext == null)
                {
                    facesContext = FacesContext.getCurrentInstance();
                }
                if (throwableList.size() == 1)
                {
                    ErrorPageWriter.handle(facesContext, components, throwableList.get(0));
                }
                else if (throwableList.size() > 1)
                {
                    ErrorPageWriter.handle(facesContext, components,
                                           throwableList.toArray(new Throwable[throwableList.size()]));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processEvent(SystemEvent exceptionQueuedEvent) throws AbortProcessingException
    {
        init(exceptionQueuedEvent);
        
        if (!isUseMyFacesErrorHandling())
        {
            super.processEvent(exceptionQueuedEvent);
        }
        else
        {
            if (unhandled == null)
            {
                unhandled = new LinkedList<>();
            }
            
            unhandled.add((ExceptionQueuedEvent)exceptionQueuedEvent);
        }
    }

    protected Throwable getRethrownException(Throwable exception)
    {
        // Let toRethrow be either the result of calling getRootCause() on the Exception, 
        // or the Exception itself, whichever is non-null
        Throwable toRethrow = getRootCause(exception);
        if (toRethrow == null)
        {
            toRethrow = exception;
        }
        
        return toRethrow;
    }
    
    protected FacesException wrap(Throwable exception)
    {
        if (exception instanceof FacesException)
        {
            return (FacesException) exception;
        }
        return new FacesException(exception);
    }
    
    protected boolean shouldSkip(Throwable exception)
    {
        return exception instanceof AbortProcessingException;
    }
    
    @Override
    public ExceptionHandler getWrapped()
    {
        return delegate;
    }
}
