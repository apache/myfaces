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

import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">Faces Specification</a>
 */
@MultipartConfig
public class FacesServletImpl implements Servlet
{
    private static final Logger LOG = Logger.getLogger(FacesServletImpl.class.getName());

    private ServletConfig servletConfig;
    private FacesContextFactory facesContextFactory;
    private Lifecycle lifecycle;

    public FacesServletImpl()
    {
        super();
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("init begin");
        }
        this.servletConfig = servletConfig;
        this.facesContextFactory = (FacesContextFactory)FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);

        // Javadoc says: Lifecycle instance is shared across multiple simultaneous requests, it must be implemented in a
        // thread-safe manner.
        // So we can acquire it here once:
        LifecycleFactory lifecycleFactory = (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        this.lifecycle = lifecycleFactory.getLifecycle(getLifecycleId());
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("init end");
        }
    }


    @Override
    public void destroy()
    {
        servletConfig = null;
        facesContextFactory = null;
        lifecycle = null;
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("destroy");
        }
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws IOException, ServletException
    {
        // If the request and response arguments to this method are not instances of HttpServletRequest and 
        // HttpServletResponse, respectively, the results of invoking this method are undefined.
        // In this case ClassCastException
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        String pathInfo = httpRequest.getPathInfo();

        // if it is a prefix mapping ...
        
        /*
         * This method must respond to requests that start with the following strings by invoking the sendError 
         * method on the response argument (cast to HttpServletResponse), passing the code 
         * HttpServletResponse.SC_NOT_FOUND as the argument.
         * 
         *       /WEB-INF/
         *       /WEB-INF
         *       /META-INF/
         *       /META-INF
         */
        if (pathInfo != null && (pathInfo.startsWith("/WEB-INF") || pathInfo.startsWith("/META-INF")))
        {
            StringBuilder buffer = new StringBuilder();

            buffer.append(" Someone is trying to access a secure resource : ").append(pathInfo);
            buffer.append("\n remote address is ").append(httpRequest.getRemoteAddr());
            buffer.append("\n remote host is ").append(httpRequest.getRemoteHost());
            buffer.append("\n remote user is ").append(httpRequest.getRemoteUser());
            buffer.append("\n request URI is ").append(httpRequest.getRequestURI());

            LOG.warning(buffer.toString());

            // Why does RI return a 404 and not a 403, SC_FORBIDDEN ?

            ((HttpServletResponse)response).sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // If none of the cases described above in the specification for this method apply to the servicing of this 
        // request, the following action must be taken to service the request:
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("service begin");
        }

        // Acquire a FacesContext instance for this request.
        FacesContext facesContext = prepareFacesContext(request, response);

        try
        {
            // jsf 2.0 : get the current ResourceHandler and
            // check if it is a resource request, if true
            // delegate to ResourceHandler, else continue with
            // the lifecycle.
            // Acquire the ResourceHandler for this request by calling Application.getResourceHandler(). 
            ResourceHandler resourceHandler = facesContext.getApplication().getResourceHandler();

            // Call ResourceHandler.isResourceRequest(jakarta.faces.context.FacesContext).
            if (resourceHandler.isResourceRequest(facesContext))
            {
                // If this returns true call ResourceHandler.handleResourceRequest(jakarta.faces.context.FacesContext).
                resourceHandler.handleResourceRequest(facesContext);
            }
            else
            {
                //Faces 2.2: attach window
                lifecycle.attachWindow(facesContext);
                // If this returns false, handle as follows:
                // call Lifecycle.execute(jakarta.faces.context.FacesContext)
                lifecycle.execute(facesContext);
                // followed by Lifecycle.render(jakarta.faces.context.FacesContext).
                lifecycle.render(facesContext);
            }
        }
        catch (FacesException e)
        {
            // If a FacesException is thrown in either case
            
            // extract the cause from the FacesException
            Throwable cause = e.getCause();
            if (cause == null)
            {
                // If the cause is null extract the message from the FacesException, put it inside of a new 
                // ServletException instance, and pass the FacesException instance as the root cause, then 
                // rethrow the ServletException instance.
                throw new ServletException(e.getLocalizedMessage(), e);
            }
            else if (cause instanceof ServletException)
            {
                // If the cause is an instance of ServletException, rethrow the cause.
                throw (ServletException)cause;
            }
            else if (cause instanceof IOException)
            {
                // If the cause is an instance of IOException, rethrow the cause.
                throw (IOException)cause;
            }
            else
            {
                // Otherwise, create a new ServletException instance, passing the message from the cause, 
                // as the first argument, and the cause itself as the second argument. 
                throw new ServletException(cause.getLocalizedMessage(), cause);
            }
        }
        finally
        {
            // In a finally block, FacesContext.release() must be called. 
            facesContext.release();
        }
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("service end");
        }
    }


    @Override
    public ServletConfig getServletConfig()
    {
        return servletConfig;
    }

    @Override
    public String getServletInfo()
    {
        return "FacesServlet of the MyFaces Implementation";
    }

    private String getLifecycleId()
    {
        // 1. check for Servlet's init-param
        // 2. check for global context parameter
        // 3. use default Lifecycle Id, if none of them was provided
        String serLifecycleId = servletConfig.getInitParameter(FacesServlet.LIFECYCLE_ID_ATTR);
        String appLifecycleId = servletConfig.getServletContext().getInitParameter(FacesServlet.LIFECYCLE_ID_ATTR);
        appLifecycleId = serLifecycleId == null ? appLifecycleId : serLifecycleId;
        return appLifecycleId != null ? appLifecycleId : LifecycleFactory.DEFAULT_LIFECYCLE;
    }

    private FacesContext prepareFacesContext(ServletRequest request, ServletResponse response)
    {
        FacesContext facesContext =
                facesContextFactory.getFacesContext(servletConfig.getServletContext(), request, response, lifecycle);
        return facesContext;
    }
}
