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
package org.apache.myfaces.core.extensions.quarkus.runtime.exception;

import java.util.Iterator;

import jakarta.faces.FacesException;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;

/**
 * Custom {@link ExceptionHandler} to log exceptions in development mode.
 */
public class QuarkusExceptionHandler extends ExceptionHandlerWrapper
{
    public QuarkusExceptionHandler(ExceptionHandler wrapped)
    {
        super(wrapped);
    }

    @Override
    public void handle() throws FacesException
    {
        FacesContext context = FacesContext.getCurrentInstance();

        if (context.isProjectStage(ProjectStage.Development))
        {
            Iterator<ExceptionQueuedEvent> iterator = getUnhandledExceptionQueuedEvents().iterator();
            while (iterator.hasNext())
            {
                ExceptionQueuedEvent event = iterator.next();

                event.getContext().getException().printStackTrace();
            }
        }

        getWrapped().handle();
    }
}
