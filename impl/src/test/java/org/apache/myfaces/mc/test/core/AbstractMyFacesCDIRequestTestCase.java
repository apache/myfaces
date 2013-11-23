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
package org.apache.myfaces.mc.test.core;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpSessionEvent;
import org.apache.myfaces.spi.impl.CDIAnnotationDelegateInjectionProvider;
import org.apache.webbeans.servlet.WebBeansConfigurationListener;

/**
 *
 */
public class AbstractMyFacesCDIRequestTestCase extends AbstractMyFacesRequestTestCase
{
    
    private WebBeansConfigurationListener owbListener;

    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.spi.InjectionProvider", 
            CDIAnnotationDelegateInjectionProvider.class.getName());
   }
    
    @Override
    protected void setUpServletListeners() throws Exception
    {
        owbListener = new WebBeansConfigurationListener();
        owbListener.contextInitialized(new ServletContextEvent(servletContext));
        super.setUpServletListeners();
    }

    @Override
    protected void tearDownServletListeners() throws Exception
    {
        super.tearDownServletListeners();
        owbListener.contextDestroyed(new ServletContextEvent(servletContext));
    }

    @Override
    protected void setupRequest(String pathInfo, String query) throws Exception
    {
        owbListener.requestInitialized(new ServletRequestEvent(servletContext, request));
        super.setupRequest(pathInfo, query);
        owbListener.sessionCreated(new HttpSessionEvent(session));
    }

    @Override
    protected void tearDownRequest()
    {
        super.tearDownRequest();
        owbListener.requestDestroyed(new ServletRequestEvent(servletContext, request));
    }

}
