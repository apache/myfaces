/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_impl.webapp.webxml.DelegatedFacesServlet;

import javax.faces.webapp.FacesServlet;
import javax.servlet.*;
import java.io.IOException;

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

    private FacesServlet delegate = new FacesServlet();

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
        Boolean b = (Boolean)servletContext.getAttribute(org.apache.myfaces.webapp.StartupServletContextListener.FACES_INIT_DONE);
        if (b == null || b.booleanValue() == false)
        {
            log.warn("ServletContextListener not yet called");
            org.apache.myfaces.webapp.StartupServletContextListener.initFaces(servletConfig.getServletContext());
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
