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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.faces.event.SystemEvent;

/**
 * DOCUMENT ME!
 * 
 * @author (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2008-09-24 18:31:37 -0400 (mer., 17 sept. 2008) $
 * 
 * @since 2.0
 */
public class ExceptionHandlerImpl extends ExceptionHandler
{
    private Queue<ExceptionQueuedEvent> handled;
    private Queue<ExceptionQueuedEvent> unhandled;

    public ExceptionHandlerImpl()
    {
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ExceptionQueuedEvent getHandledExceptionQueuedEvent()
    {
        return handled == null ? null : handled.poll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<ExceptionQueuedEvent> getHandledExceptionQueuedEvents()
    {
        return handled == null ? Collections.<ExceptionQueuedEvent>emptyList() : handled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Throwable getRootCause(Throwable t)
    {
        if (t == null)
        {
            throw new NullPointerException("t");
        }
        
        while (t != null)
        {
            Class<?> clazz = t.getClass();
            if (!clazz.equals(FacesException.class) && !clazz.equals(ELException.class))
            {
                return t;
            }
            
            t = t.getCause();
        }
        
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<ExceptionQueuedEvent> getUnhandledExceptionQueuedEvents()
    {
        return unhandled == null ? Collections.<ExceptionQueuedEvent>emptyList() : unhandled;
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
                handled = new ArrayDeque<ExceptionQueuedEvent>(1);
            }
            
            do
            {
                // For each ExceptionEvent in the list
                
                // The implementation must also ensure that subsequent calls to getUnhandledExceptionEvents() 
                // do not include that ExceptionEvent instance
                ExceptionQueuedEvent event = unhandled.remove();
                
                // call its getContext() method
                ExceptionQueuedEventContext context = event.getContext();
                
                // and call getException() on the returned result
                Throwable exception = context.getException();
                
                // Upon encountering the first such Exception that is not an instance of
                // javax.faces.event.AbortProcessingException
                if (!shouldSkip(exception))
                {
                    // the corresponding ExceptionEvent must be set so that a subsequent call to 
                    // getHandledExceptionEvent() or getHandledExceptionEvents() returns that 
                    // ExceptionEvent instance. 
                    // Should be ok to clear since this if never executed more than once per handle() calls
                    handled.clear();
                    handled.add(event);
                    
                    // Re-wrap toThrow in a ServletException or (PortletException, if in a portlet environment) 
                    // and throw it
                    // FIXME: The spec says to NOT use a FacesException to propagate the exception, but I see
                    //        no other way as ServletException is not a RuntimeException
                    throw new FacesException(wrap(getRethrownException(exception)));
                }
            } while (!unhandled.isEmpty());
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
            unhandled = new ArrayDeque<ExceptionQueuedEvent>(1);
        }
        
        unhandled.add((ExceptionQueuedEvent)exceptionQueuedEvent);
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
    
    protected Throwable wrap(Throwable exception)
    {
        // TODO: REPORT This method should be abstract and implemented by a Portlet or Servlet version 
        //       instance wrapping to either ServletException or PortletException
        return exception;
    }
    
    protected boolean shouldSkip(Throwable exception)
    {
        return exception instanceof AbortProcessingException;
    }
}
