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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.webapp.FacesServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

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
    //private static final Log log = LogFactory.getLog(MyFacesServlet.class);
    private static final Logger log = Logger.getLogger(MyFacesServlet.class.getName());

    private final FacesServlet delegate = new FacesServlet();
    
    private FacesInitializer _facesInitializer;
    
    protected void initFaces(ServletContext context)
    {
        if (_facesInitializer == null)
        {
            if (ContainerUtils.isJsp21(context)) 
            {
                _facesInitializer = new Jsp21FacesInitializer();
            } 
            else 
            {
                _facesInitializer = new Jsp20FacesInitializer();
            }
        }
        
        _facesInitializer.initFaces(context);
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
            if(log.isLoggable(Level.WARNING))
                log.warning("ServletContextListener not yet called");
            initFaces(servletConfig.getServletContext());
        }
        delegate.init(servletConfig);
        log.info("MyFacesServlet for context '" + servletConfig.getServletContext().getRealPath("/") + "' initialized.");
    }

    public void service(ServletRequest request, ServletResponse response)
            throws IOException,
                   ServletException
    {
        if (log.isLoggable(Level.FINEST)) log.finest("MyFacesServlet service start");
        delegate.service(request, response);
        if (log.isLoggable(Level.FINEST)) log.finest("MyFacesServlet service finished");
    }

}
