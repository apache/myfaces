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
package jakarta.faces.context;

import jakarta.faces.FacesException;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;

/**
 * @since 2.0
 */
public abstract class ExceptionHandler implements SystemEventListener
{
    /**
     * <p class="changed_added_5_0">
     * The name of the context init parameter that specifies exception types to be ignored in logging by
     * <code>ExceptionHandler</code> implementations.
     * The parameter value must be a comma-separated list of fully qualified exception class names.
     * Implementations and component libraries should consult this
     * parameter when determining whether to log a given exception, and should ignore exceptions that
     * are instances of the specified types.
     * </p>
     *
     * @since 5.0
     */
    public static final String EXCEPTION_TYPES_TO_IGNORE_IN_LOGGING_PARAM_NAME
            = "jakarta.faces.EXCEPTION_TYPES_TO_IGNORE_IN_LOGGING";

    public ExceptionHandler()
    {

    }

    public abstract ExceptionQueuedEvent getHandledExceptionQueuedEvent();

    public abstract Iterable<ExceptionQueuedEvent> getHandledExceptionQueuedEvents();

    public abstract Throwable getRootCause(Throwable t);

    public abstract Iterable<ExceptionQueuedEvent> getUnhandledExceptionQueuedEvents();

    public abstract void handle() throws FacesException;

    @Override
    public abstract boolean isListenerForSource(Object source);

    @Override
    public abstract void processEvent(SystemEvent exceptionQueuedEvent) throws AbortProcessingException;
}
