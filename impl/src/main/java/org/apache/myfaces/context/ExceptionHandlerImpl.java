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

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.faces.event.SystemEvent;
import org.apache.myfaces.core.api.shared.lang.Assert;

/** 
 * @since 2.0
 */
public class ExceptionHandlerImpl extends ExceptionHandler
{
    private static final Logger log = Logger.getLogger(ExceptionHandlerImpl.class.getName());
    
    private Queue<ExceptionQueuedEvent> handled;
    private Queue<ExceptionQueuedEvent> unhandled;
    private ExceptionQueuedEvent handledAndThrown;

    public ExceptionHandlerImpl()
    {
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ExceptionQueuedEvent getHandledExceptionQueuedEvent()
    {
        return handledAndThrown;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<ExceptionQueuedEvent> getHandledExceptionQueuedEvents()
    {
        return handled == null ? Collections.emptyList() : handled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Throwable getRootCause(Throwable throwable)
    {
        Assert.notNull(throwable, "throwable");

        while (throwable != null)
        {
            Class<?> clazz = throwable.getClass();
            if (!clazz.equals(FacesException.class) && !clazz.equals(ELException.class))
            {
                return throwable;
            }
            
            throwable = throwable.getCause();
        }
        
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<ExceptionQueuedEvent> getUnhandledExceptionQueuedEvents()
    {
        return unhandled == null ? Collections.emptyList() : unhandled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle() throws FacesException
    {
        if (unhandled != null && !unhandled.isEmpty())
        {
            if (handled == null)
            {
                handled = new LinkedList<ExceptionQueuedEvent>();
            }
            
            FacesException toThrow = null;
            
            do
            {
                // For each ExceptionEvent in the list
                
                // get the event to handle
                ExceptionQueuedEvent event = unhandled.peek();
                try
                {
                    // call its getContext() method
                    ExceptionQueuedEventContext context = event.getContext();
                    
                    // and call getException() on the returned result
                    Throwable exception = context.getException();

                    // Upon encountering the first such Exception that is not an instance of
                    // jakarta.faces.event.AbortProcessingException
                    if (!shouldSkip(exception))
                    {
                        Throwable rootCause = getRootCause(exception);

                        // set handledAndThrown so that getHandledExceptionQueuedEvent() returns this event
                        handledAndThrown = event;

                        // Re-wrap toThrow in a ServletException or (PortletException, if in a portlet environment) 
                        // and throw it
                        // FIXME: The spec says to NOT use a FacesException to propagate the exception, but I see
                        //        no other way as ServletException is not a RuntimeException
                        toThrow = wrap(rootCause == null ? exception : rootCause);

                        ExceptionHandlerUtils.logException(rootCause == null ? exception : rootCause,
                                context.getComponent(), context.getContext(), log);

                        break;
                    }
                }
                catch (Throwable t)
                {
                    // A FacesException must be thrown if a problem occurs while performing
                    // the algorithm to handle the exception
                    throw new FacesException("Could not perform the algorithm to handle the Exception", t);
                }
                finally
                {
                    // if we will throw the Exception or if we just logged it,
                    // we handled it in either way --> add to handled
                    handled.add(event);
                    unhandled.remove(event);
                }
            } while (!unhandled.isEmpty());
            
            // do we have to throw an Exception?
            if (toThrow != null)
            {
                throw toThrow;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isListenerForSource(Object source)
    {
        return source instanceof ExceptionQueuedEventContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processEvent(SystemEvent exceptionQueuedEvent) throws AbortProcessingException
    {
        if (unhandled == null)
        {
            unhandled = new LinkedList<>();
        }
        
        unhandled.add((ExceptionQueuedEvent)exceptionQueuedEvent);
    }

    protected FacesException wrap(Throwable exception)
    {
        if (exception instanceof FacesException facesException)
        {
            return facesException;
        }
        return new FacesException(exception);
    }
    
    protected boolean shouldSkip(Throwable exception)
    {
        return exception instanceof AbortProcessingException;
    }
}
