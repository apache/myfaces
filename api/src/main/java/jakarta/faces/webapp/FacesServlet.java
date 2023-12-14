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
package jakarta.faces.webapp;

import jakarta.faces.FactoryFinder;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.MultipartConfig;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;

import java.io.IOException;
/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">Faces Specification</a>
 */
@MultipartConfig
public final class FacesServlet implements Servlet
{
    /**
     * Comma separated list of URIs of (additional) faces config files.
     * (e.g. /WEB-INF/my-config.xml)See Faces 1.0 PRD2, 10.3.2
     * Attention: You do not need to put /WEB-INF/faces-config.xml in here.
     */
    @JSFWebConfigParam(since="1.1")
    public static final String CONFIG_FILES_ATTR = "jakarta.faces.CONFIG_FILES";

    /**
     * Identify the Lifecycle instance to be used.
     */
    @JSFWebConfigParam(since="1.1")
    public static final String LIFECYCLE_ID_ATTR = "jakarta.faces.LIFECYCLE_ID";
    
    /**
     * Disable automatic FacesServlet xhtml mapping.
     */
    @JSFWebConfigParam(since="2.3")
    public static final String DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME = "jakarta.faces.DISABLE_FACESSERVLET_TO_XHTML";
    
    /**
     * <p class="changed_added_4_0">
     * The <code>ServletContext</code> init parameter consulted by the runtime to tell if the automatic mapping of
     * the {@code FacesServlet} to the extensionless variant (without {@code *.xhtml}) should be enabled.
     * The implementation must enable this automatic mapping if and only if the value of this parameter is equal,
     * ignoring case, to {@code true}.
     * </p>
     *
     * <p>
     * If this parameter is not specified, this automatic mapping is not enabled.
     * </p>
     */
    @JSFWebConfigParam(since="4.0")
    public static final String AUTOMATIC_EXTENSIONLESS_MAPPING_PARAM_NAME
            = "jakarta.faces.AUTOMATIC_EXTENSIONLESS_MAPPING";

    private ServletConfig servletConfig;
    private Servlet facesServlet;

    public FacesServlet()
    {
        super();
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException
    {
        this.servletConfig = servletConfig;

        FacesServletFactory factory = (FacesServletFactory)
                FactoryFinder.getFactory(FactoryFinder.FACES_SERVLET_FACTORY);
        this.facesServlet = factory.getFacesServlet(servletConfig);
        this.facesServlet.init(servletConfig);
    }

    @Override
    public void destroy()
    {
        facesServlet.destroy();
        facesServlet = null;
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws IOException, ServletException
    {
        facesServlet.service(request, response);
    }

    @Override
    public String getServletInfo()
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

            log.warning(buffer.toString());

            // Why does RI return a 404 and not a 403, SC_FORBIDDEN ?

            ((HttpServletResponse)response).sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // If none of the cases described above in the specification for this method apply to the servicing of this 
        // request, the following action must be taken to service the request:
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("service begin");
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
                _lifecycle.attachWindow(facesContext);
                // If this returns false, handle as follows:
                // call Lifecycle.execute(jakarta.faces.context.FacesContext)
                _lifecycle.execute(facesContext);
                // followed by Lifecycle.render(jakarta.faces.context.FacesContext).
                _lifecycle.render(facesContext);
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
            else if (cause instanceof ServletException exception)
            {
                // If the cause is an instance of ServletException, rethrow the cause.
                throw exception;
            }
            else if (cause instanceof IOException exception)
            {
                // If the cause is an instance of IOException, rethrow the cause.
                throw exception;
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
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("service end");
        }
        
        return "FacesServlet of the MyFaces API";
    }

    @Override
    public ServletConfig getServletConfig()
    {
        return servletConfig;
    }
}
