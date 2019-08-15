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
package org.apache.myfaces.webapp;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.apache.myfaces.context.ExceptionHandlerImpl;
import org.apache.myfaces.context.servlet.StartupFacesContextImpl;
import org.apache.myfaces.context.servlet.StartupServletExternalContextImpl;
import org.apache.myfaces.spi.FacesFlowProvider;
import org.apache.myfaces.spi.ViewScopeProvider;

public class MyFacesHttpSessionListener implements HttpSessionListener
{
    public static final String APPLICATION_MAP_KEY = MyFacesHttpSessionListener.class.getName();

    private ViewScopeProvider viewScopeProvider = null;
    private FacesFlowProvider facesFlowProvider = null;
    
    public void setViewScopeProvider(ViewScopeProvider viewScopeProvider)
    {
        this.viewScopeProvider = viewScopeProvider;
    }
    
    public void setFacesFlowProvider(FacesFlowProvider facesFlowProvider)
    {
        this.facesFlowProvider = facesFlowProvider;
    }
    
    @Override
    public void sessionCreated(HttpSessionEvent event)
    {
    }
    
    @Override
    public void sessionDestroyed(HttpSessionEvent event)
    {
        // If we don't propagate this event, CDI will do for us but outside JSF control
        // so when @PreDestroy methods are called there will not be an active FacesContext.
        // The trick here is ensure clean the affected scopes to avoid duplicates.
        // Remember cdi session scope is different from jsf session scope, because in
        // jsf case the beans are stored under a session attribute, so it has the problem
        // with attributeRemoved, but on cdi a wrapper is used instead, avoiding the problem.
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null)
        {
            if (viewScopeProvider != null)
            {
                viewScopeProvider.onSessionDestroyed();
            }
            if (facesFlowProvider != null)
            {
                facesFlowProvider.onSessionDestroyed();
            }
        }
        else
        {
            // In case no FacesContext is available, we are on session invalidation
            // through timeout. In that case, create a dummy FacesContext for this one
            // like the one used in startup or shutdown and invoke the destroy method.
            try
            {
                ServletContext servletContext = event.getSession().getServletContext();
                ExternalContext externalContext = new StartupServletExternalContextImpl(servletContext, false);
                ExceptionHandler exceptionHandler = new ExceptionHandlerImpl();
                facesContext = new StartupFacesContextImpl(externalContext, externalContext, exceptionHandler, false);

                if (viewScopeProvider != null)
                {
                    viewScopeProvider.onSessionDestroyed();
                }
                if (facesFlowProvider != null)
                {
                    facesFlowProvider.onSessionDestroyed();
                }
            }
            finally
            {
                facesContext.release();
            }
        }
    }
}
