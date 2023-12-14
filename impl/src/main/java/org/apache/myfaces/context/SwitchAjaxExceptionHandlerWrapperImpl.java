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

import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.faces.event.SystemEvent;

/**
 * This wrapper is a switch to choose in a lazy way between ajax and
 * normal exceptionHandler wrapping, because FacesContext is initialized after
 * ExceptionHandler, so it is not safe to get it when
 * ExceptionHandlerFactory.getExceptionHandler() is called.
 */
public class SwitchAjaxExceptionHandlerWrapperImpl extends ExceptionHandlerWrapper
{
    private ExceptionHandler requestExceptionHandler;
    private ExceptionHandler ajaxExceptionHandler;
    private Boolean isAjaxRequest;
    
    public SwitchAjaxExceptionHandlerWrapperImpl(ExceptionHandler requestExceptionHandler,
            ExceptionHandler ajaxExceptionHandler)
    {
        this.requestExceptionHandler = requestExceptionHandler;
        this.ajaxExceptionHandler = ajaxExceptionHandler;
    }
    
    @Override
    public void processEvent(SystemEvent exceptionQueuedEvent)
            throws AbortProcessingException
    {
        //Check if this is an ajax request, but take advantage of exceptionQueuedEvent facesContext
        isAjaxRequest(exceptionQueuedEvent);
        super.processEvent(exceptionQueuedEvent);
    }

    protected boolean isAjaxRequest(SystemEvent exceptionQueuedEvent)
    {
        if (isAjaxRequest == null)
        {
            if (exceptionQueuedEvent instanceof ExceptionQueuedEvent eqe)
            {
                ExceptionQueuedEventContext eqec = eqe.getContext();
                if (eqec != null)
                {
                    FacesContext facesContext = eqec.getContext();
                    if (facesContext != null)
                    {
                        return isAjaxRequest(facesContext);
                    }
                }
            }
            return isAjaxRequest();
        }
        return isAjaxRequest;
    }
    
    protected boolean isAjaxRequest(FacesContext facesContext)
    {
        if (isAjaxRequest == null)
        {
            facesContext = (facesContext == null) ? FacesContext.getCurrentInstance() : facesContext;
            PartialViewContext pvc = facesContext.getPartialViewContext();
            if (pvc == null)
            {
                return false;
            }
            isAjaxRequest = pvc.isAjaxRequest();
        }
        return isAjaxRequest;
    }
    
    protected boolean isAjaxRequest()
    {
        if (isAjaxRequest == null)
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            PartialViewContext pvc = facesContext.getPartialViewContext();
            if (pvc == null)
            {
                return false;
            }
            isAjaxRequest = pvc.isAjaxRequest();
        }
        return isAjaxRequest;
    }
    
    @Override
    public ExceptionHandler getWrapped()
    {
        if (isAjaxRequest())
        {
            return ajaxExceptionHandler;
        }
        else
        {
            return requestExceptionHandler;
        }
    }
}
