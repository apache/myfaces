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
package org.apache.myfaces.test.mock;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.faces.event.SystemEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

/**
 * <p>Mock implementation of <code>ExceptionHandler</code>.</p>
 * <p/>
 * $Id$
 *
 * @since 1.0.0
 */
public class MockExceptionHandler extends ExceptionHandler
{

    private Queue<ExceptionQueuedEvent> handled;
    private Queue<ExceptionQueuedEvent> unhandled;
    private ExceptionQueuedEvent handledAndThrown;

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
        return handled == null ? Collections.emptyList()
                : handled;
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
            if (!clazz.equals(FacesException.class)
                    && !clazz.equals(ELException.class))
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
        return unhandled == null ? Collections
                .emptyList() : unhandled;
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
                        // set handledAndThrown so that getHandledExceptionQueuedEvent() returns this event
                        handledAndThrown = event;

                        // Re-wrap toThrow in a ServletException or (PortletException, if in a portlet environment)
                        // and throw it
                        // FIXME: The spec says to NOT use a FacesException to propagate the exception, but I see
                        //        no other way as ServletException is not a RuntimeException
                        toThrow = wrap(getRethrownException(exception));
                        break;
                    }
                }
                catch (Throwable t)
                {
                    // A FacesException must be thrown if a problem occurs while performing
                    // the algorithm to handle the exception
                    throw new FacesException(
                            "Could not perform the algorithm to handle the Exception",
                            t);
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
    public void processEvent(SystemEvent exceptionQueuedEvent)
            throws AbortProcessingException
    {
        if (unhandled == null)
        {
            unhandled = new LinkedList<ExceptionQueuedEvent>();
        }

        unhandled.add((ExceptionQueuedEvent) exceptionQueuedEvent);
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
}
