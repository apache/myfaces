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
package javax.faces.webapp;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.ResourceHandler;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public final class FacesServlet implements Servlet
{
    //private static final Log log = LogFactory.getLog(FacesServlet.class);
    private static final Logger log = Logger.getLogger(FacesServlet.class.getName());
    public static final String CONFIG_FILES_ATTR = "javax.faces.CONFIG_FILES";
    public static final String LIFECYCLE_ID_ATTR = "javax.faces.LIFECYCLE_ID";

    private static final String SERVLET_INFO = "FacesServlet of the MyFaces API implementation";
    private static final String ERROR_HANDLING_PARAMETER = "org.apache.myfaces.ERROR_HANDLING";
    private static final String ERROR_HANDLER_PARAMETER = "org.apache.myfaces.ERROR_HANDLER";
    private static final String ERROR_HANDLING_EXCEPTION_LIST = "org.apache.myfaces.errorHandling.exceptionList";

    private ServletConfig _servletConfig;
    private FacesContextFactory _facesContextFactory;
    private Lifecycle _lifecycle;

    public FacesServlet()
    {
        super();
    }

    public void destroy()
    {
        _servletConfig = null;
        _facesContextFactory = null;
        _lifecycle = null;
        if (log.isLoggable(Level.FINEST))
            log.finest("destroy");
    }

    public ServletConfig getServletConfig()
    {
        return _servletConfig;
    }

    public String getServletInfo()
    {
        return SERVLET_INFO;
    }

    private String getLifecycleId()
    {
        // 1. check for Servlet's init-param
        // 2. check for global context parameter
        // 3. use default Lifecycle Id, if none of them was provided
        String serLifecycleId = _servletConfig.getInitParameter(LIFECYCLE_ID_ATTR);
        String appLifecycleId = _servletConfig.getServletContext().getInitParameter(LIFECYCLE_ID_ATTR);
        appLifecycleId = serLifecycleId == null ? appLifecycleId : serLifecycleId;
        return appLifecycleId != null ? appLifecycleId : LifecycleFactory.DEFAULT_LIFECYCLE;
    }

    public void init(ServletConfig servletConfig) throws ServletException
    {
        if (log.isLoggable(Level.FINEST))
            log.finest("init begin");
        _servletConfig = servletConfig;
        _facesContextFactory = (FacesContextFactory)FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        // TODO: null-check for Weblogic, that tries to initialize Servlet before ContextListener

        // Javadoc says: Lifecycle instance is shared across multiple simultaneous requests, it must be implemented in a
        // thread-safe manner.
        // So we can acquire it here once:
        LifecycleFactory lifecycleFactory = (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        _lifecycle = lifecycleFactory.getLifecycle(getLifecycleId());
        if (log.isLoggable(Level.FINEST))
            log.finest("init end");
    }

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
            StringBuffer buffer = new StringBuffer();

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
        // request, the following action must be taken to service the request.
        if (log.isLoggable(Level.FINEST))
            log.finest("service begin");

        // Acquire a FacesContext instance for this request.
        FacesContext facesContext = prepareFacesContext(request, response);

        try
        {
            // jsf 2.0 : get the current ResourceHandler and
            // check if it is a resource request, if true
            // delegate to ResourceHandler, if continue with
            // the lifecycle.
            // Acquire the ResourceHandler for this request by calling Application.getResourceHandler(). 
            ResourceHandler resourceHandler = facesContext.getApplication().getResourceHandler();

            // Call ResourceHandler.isResourceRequest(javax.faces.context.FacesContext).
            if (resourceHandler.isResourceRequest(facesContext))
            {
                // If this returns true call ResourceHandler.handleResourceRequest(javax.faces.context.FacesContext).
                resourceHandler.handleResourceRequest(facesContext);
            }
            else
            {
                // If this returns false, handle as follow
                _handleStandardRequest(facesContext);
            }
        }
        catch (Exception e)
        {
            handleLifecycleException(facesContext, e);
        }
        catch (Throwable e)
        {
            handleLifecycleThrowable(facesContext, e);
        }
        finally
        {
            // In a finally block, FacesContext.release() must be called. 
            facesContext.release();
        }
        if (log.isLoggable(Level.FINEST))
            log.finest("service end");
    }

    /**
     * This method makes sure we see an exception page also if an exception has been thrown in UIInput.updateModel(); In
     * this method, according to the spec, we may not rethrow the exception, so we add it to a list and process it here.
     * 
     * Attention: if you use redirects, the exceptions will get lost - exactly like in the case of FacesMessages. If you
     * want them to be taken over to the next request, you should try the redirect-tracker of MyFaces.
     * 
     * @param facesContext
     * @throws FacesException
     */
    @SuppressWarnings("unchecked")
    private boolean handleQueuedExceptions(FacesContext facesContext) throws IOException, ServletException
    {
        Map<String, Object> requestScope = facesContext.getExternalContext().getRequestMap();
        List<Exception> li = (List<Exception>)requestScope.get(ERROR_HANDLING_EXCEPTION_LIST);

        if (li != null && li.size() >= 1)
        {
            // todo: for now, we only handle the first exception out of the list - we just rethrow this
            // first exception.
            // in the end, we should enable the error handler to show all the exceptions at once
            boolean errorHandling =
                    getBooleanValue(facesContext.getExternalContext().getInitParameter(ERROR_HANDLING_PARAMETER), true);

            if (errorHandling)
            {
                String errorHandlerClass = facesContext.getExternalContext().getInitParameter(ERROR_HANDLER_PARAMETER);
                if (errorHandlerClass != null)
                {
                    try
                    {
                        Class<?> clazz = Class.forName(errorHandlerClass);

                        Object errorHandler = clazz.newInstance();

                        Method m = clazz.getMethod("handleExceptionList", new Class[]{FacesContext.class,List.class});
                        m.invoke(errorHandler, new Object[]{facesContext, li});
                    }
                    catch (ClassNotFoundException ex)
                    {
                        throw new ServletException("Error-Handler : " + errorHandlerClass
                                + " was not found. Fix your web.xml-parameter : " + ERROR_HANDLER_PARAMETER, ex);
                    }
                    catch (IllegalAccessException ex)
                    {
                        throw new ServletException("Constructor of error-Handler : " + errorHandlerClass
                                + " is not accessible. Error-Handler is specified in web.xml-parameter : "
                                + ERROR_HANDLER_PARAMETER, ex);
                    }
                    catch (InstantiationException ex)
                    {
                        throw new ServletException("Error-Handler : " + errorHandlerClass
                                + " could not be instantiated. Error-Handler is specified in web.xml-parameter : "
                                + ERROR_HANDLER_PARAMETER, ex);
                    }
                    catch (NoSuchMethodException ex)
                    {
                        // Handle in the old way, since no custom method handleExceptionList found,
                        // throwing the first FacesException on the list.
                        throw (FacesException)li.get(0);
                    }
                    catch (InvocationTargetException ex)
                    {
                        throw new ServletException("Excecution of method handleException in Error-Handler : "
                                + errorHandlerClass
                                + " threw an exception. Error-Handler is specified in web.xml-parameter : "
                                + ERROR_HANDLER_PARAMETER, ex);
                    }
                }
                else
                {
                    _ErrorPageWriter.handleExceptionList(facesContext, li);
                }
            }
            else
            {
                _ErrorPageWriter.throwException(li.get(0));
            }
            return true;
        }
        return false;
    }

    private void handleLifecycleException(FacesContext facesContext, Exception e) throws IOException, ServletException
    {

        boolean errorHandling =
                getBooleanValue(facesContext.getExternalContext().getInitParameter(ERROR_HANDLING_PARAMETER), true);

        if (errorHandling)
        {
            String errorHandlerClass = facesContext.getExternalContext().getInitParameter(ERROR_HANDLER_PARAMETER);
            if (errorHandlerClass != null)
            {
                try
                {
                    Class<?> clazz = Class.forName(errorHandlerClass);

                    Object errorHandler = clazz.newInstance();

                    Method m = clazz.getMethod("handleException", new Class[] { FacesContext.class, Exception.class });
                    m.invoke(errorHandler, new Object[] { facesContext, e });
                }
                catch (ClassNotFoundException ex)
                {
                    throw new ServletException("Error-Handler : " + errorHandlerClass
                            + " was not found. Fix your web.xml-parameter : " + ERROR_HANDLER_PARAMETER, ex);
                }
                catch (IllegalAccessException ex)
                {
                    throw new ServletException("Constructor of error-Handler : " + errorHandlerClass
                            + " is not accessible. Error-Handler is specified in web.xml-parameter : "
                            + ERROR_HANDLER_PARAMETER, ex);
                }
                catch (InstantiationException ex)
                {
                    throw new ServletException("Error-Handler : " + errorHandlerClass
                            + " could not be instantiated. Error-Handler is specified in web.xml-parameter : "
                            + ERROR_HANDLER_PARAMETER, ex);
                }
                catch (NoSuchMethodException ex)
                {
                    log
                       .log(Level.SEVERE,
                           "Error-Handler : "
                                   + errorHandlerClass
                                   + " did not have a method with name : handleException and parameters : javax.faces.context.FacesContext, java.lang.Exception. Error-Handler is specified in web.xml-parameter : "
                                   + ERROR_HANDLER_PARAMETER, ex);
                    // Try to look if it is implemented more general method handleThrowable
                    handleLifecycleThrowable(facesContext, e);
                }
                catch (InvocationTargetException ex)
                {
                    throw new ServletException("Excecution of method handleException in Error-Handler : "
                            + errorHandlerClass
                            + " caused an exception. Error-Handler is specified in web.xml-parameter : "
                            + ERROR_HANDLER_PARAMETER, ex);
                }
            }
            else
            {
                _ErrorPageWriter.handleException(facesContext, e);
            }
        }
        else
        {
            _ErrorPageWriter.throwException(e);
        }
    }

    private void handleLifecycleThrowable(FacesContext facesContext, Throwable e) throws IOException, ServletException
    {

        boolean errorHandling =
                getBooleanValue(facesContext.getExternalContext().getInitParameter(ERROR_HANDLING_PARAMETER), true);

        if (errorHandling)
        {
            String errorHandlerClass = facesContext.getExternalContext().getInitParameter(ERROR_HANDLER_PARAMETER);
            if (errorHandlerClass != null)
            {
                try
                {
                    Class<?> clazz = Class.forName(errorHandlerClass);

                    Object errorHandler = clazz.newInstance();

                    Method m = clazz.getMethod("handleThrowable", new Class[] { FacesContext.class, Throwable.class });
                    m.invoke(errorHandler, new Object[] { facesContext, e });
                }
                catch (ClassNotFoundException ex)
                {
                    throw new ServletException("Error-Handler : " + errorHandlerClass
                            + " was not found. Fix your web.xml-parameter : " + ERROR_HANDLER_PARAMETER, ex);
                }
                catch (IllegalAccessException ex)
                {
                    throw new ServletException("Constructor of error-Handler : " + errorHandlerClass
                            + " is not accessible. Error-Handler is specified in web.xml-parameter : "
                            + ERROR_HANDLER_PARAMETER, ex);
                }
                catch (InstantiationException ex)
                {
                    throw new ServletException("Error-Handler : " + errorHandlerClass
                            + " could not be instantiated. Error-Handler is specified in web.xml-parameter : "
                            + ERROR_HANDLER_PARAMETER, ex);
                }
                catch (NoSuchMethodException ex)
                {
                    throw new ServletException(
                        "Error-Handler : "
                                + errorHandlerClass
                                + " did not have a method with name : handleException and parameters : javax.faces.context.FacesContext, java.lang.Exception. Error-Handler is specified in web.xml-parameter : "
                                + ERROR_HANDLER_PARAMETER, ex);
                }
                catch (InvocationTargetException ex)
                {
                    throw new ServletException("Excecution of method handleException in Error-Handler : "
                            + errorHandlerClass
                            + " threw an exception. Error-Handler is specified in web.xml-parameter : "
                            + ERROR_HANDLER_PARAMETER, ex);
                }
            }
            else
            {
                _ErrorPageWriter.handleThrowable(facesContext, e);
            }
        }
        else
        {
            _ErrorPageWriter.throwException(e);
        }
    }
    
    private void _handleStandardRequest(FacesContext context) throws IOException, ServletException
    {
        try
        {
            // call Lifecycle.execute(javax.faces.context.FacesContext)
            _lifecycle.execute(context);

            if (!handleQueuedExceptions(context))
            {
                // followed by Lifecycle.render(javax.faces.context.FacesContext).
                _lifecycle.render(context);
            }
        }
        catch (FacesException e)
        {
            // If a FacesException is thrown in either case
            
            // extract the cause from the FacesException
            Throwable cause = e.getCause();
            if (cause == null)
            {
                // If the cause is null extract the message from the FacesException put it inside of a new 
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
    }

    private static boolean getBooleanValue(String initParameter, boolean defaultVal)
    {

        if (initParameter == null || initParameter.trim().length() == 0)
            return defaultVal;

        return (initParameter.equalsIgnoreCase("on") || initParameter.equals("1") || initParameter
                                                                                                  .equalsIgnoreCase("true"));
    }

    private FacesContext prepareFacesContext(ServletRequest request, ServletResponse response)
    {
        FacesContext facesContext =
                _facesContextFactory.getFacesContext(_servletConfig.getServletContext(), request, response, _lifecycle);
        return facesContext;
    }
}
