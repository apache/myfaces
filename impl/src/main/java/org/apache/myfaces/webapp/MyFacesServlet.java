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

import java.io.IOException;

import javax.faces.webapp.FacesServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_impl.webapp.webxml.DelegatedFacesServlet;
import org.apache.myfaces.util.ContainerUtils;

/**
 * Derived FacesServlet that can be used for debugging purpose
 * and to fix the Weblogic startup issue (FacesServlet is initialized before ServletContextListener).
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class MyFacesServlet implements Servlet, DelegatedFacesServlet
{
    private static final Log log = LogFactory.getLog(MyFacesServlet.class);

    private final FacesServlet delegate = new FacesServlet();
    
    private FacesInitializer _facesInitializer;
    
    protected FacesInitializer getFacesInitializer()
    {
        if (_facesInitializer == null)
        {
            if (ContainerUtils.isJsp21()) 
            {
                _facesInitializer = new Jsp21FacesInitializer();
            } 
            else 
            {
                _facesInitializer = new Jsp20FacesInitializer();
            }
        }
        
        return _facesInitializer;
    }
    
    public void setFacesInitializer(FacesInitializer facesInitializer)
    {
        _facesInitializer = facesInitializer;
    }

    public void destroy()
    {
        delegate.destroy();
    }

    public ServletConfig getServletConfig()
    {
        return delegate.getServletConfig();
    }

    public String getServletInfo()
    {
        return delegate.getServletInfo();
    }

    public void init(ServletConfig servletConfig)
        throws ServletException
    {
        //Check, if ServletContextListener already called
        ServletContext servletContext = servletConfig.getServletContext();
        Boolean b = (Boolean)servletContext.getAttribute(StartupServletContextListener.FACES_INIT_DONE);
        if (b == null || b.booleanValue() == false)
        {
            if(log.isWarnEnabled())
                log.warn("ServletContextListener not yet called");
            getFacesInitializer().initFaces(servletConfig.getServletContext());
        }
        delegate.init(servletConfig);
        log.info("MyFacesServlet for context '" + servletConfig.getServletContext().getRealPath("/") + "' initialized.");
    }

    public void service(ServletRequest request, ServletResponse response)
            throws IOException,
                   ServletException
    {
        if (log.isTraceEnabled()) log.trace("MyFacesServlet service start");
        delegate.service(request, response);
        if (log.isTraceEnabled()) log.trace("MyFacesServlet service finished");
    }

}
