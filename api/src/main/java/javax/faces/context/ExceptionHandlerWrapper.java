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
package javax.faces.context;

import javax.faces.FacesException;
import javax.faces.FacesWrapper;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ExceptionEvent;
import javax.faces.event.SystemEvent;

/**
*
* @since 2.0
* @author Leonardo Uribe (latest modification by $Author$)
* @version $Revision$ $Date$
*/
public abstract class ExceptionHandlerWrapper extends ExceptionHandler
        implements FacesWrapper<ExceptionHandler>
{

    public ExceptionEvent getHandledExceptionEvent()
    {
        return getWrapped().getHandledExceptionEvent();
    }


    public Iterable<ExceptionEvent> getHandledExceptionEvents()
    {
        return getWrapped().getHandledExceptionEvents();
    }


    public Throwable getRootCause(Throwable t)
    {
        return getWrapped().getRootCause(t);
    }


    public Iterable<ExceptionEvent> getUnhandledExceptionEvents()
    {
        return getWrapped().getUnhandledExceptionEvents();
    }


    public void handle() throws FacesException
    {
        getWrapped().handle();
    }


    public boolean isListenerForSource(Object source)
    {
        return getWrapped().isListenerForSource(source);
    }


    public void processEvent(SystemEvent exceptionEvent)
            throws AbortProcessingException
    {
        getWrapped().processEvent(exceptionEvent);
    }

    public abstract ExceptionHandler getWrapped();
}
