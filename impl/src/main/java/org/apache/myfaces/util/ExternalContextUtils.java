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
package org.apache.myfaces.util;

import org.apache.myfaces.util.lang.ClassUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.ExternalContext;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This provides some functionality for determining some things about the
 * native request object that is not provided by JSF.  This class is useful
 * for use in places where Portlet API's may or may not be present and can
 * also provide access to some request-specific items which are not available on
 * the JSF ExternalContext.  If portlet API's are not present, this class simply 
 * handles the Servlet Request type.
 */
public final class ExternalContextUtils
{
    
    /**
     * Returns the requestType of this ExternalContext.
     * 
     * @param externalContext the current external context
     * @return the appropriate RequestType for this external context
     * @see RequestType
     */
    public static final RequestType getRequestType(ExternalContext externalContext)
    {
        //Stuff is laid out strangely in this class in order to optimize
        //performance.  We want to do as few instanceof's as possible so
        //things are laid out according to the expected frequency of the
        //various requests occurring.
        if(_PORTLET_10_SUPPORTED || _PORTLET_20_SUPPORTED)
        {
            if (_PORTLET_CONTEXT_CLASS.isInstance(externalContext.getContext()))
            {
                //We are inside of a portlet container
                Object request = externalContext.getRequest();
                
                if(_PORTLET_RENDER_REQUEST_CLASS.isInstance(request))
                {
                    return RequestType.RENDER;
                }
                
                if(_PORTLET_RESOURCE_REQUEST_CLASS != null)
                {
                    if(_PORTLET_ACTION_REQUEST_CLASS.isInstance(request))
                    {
                        return RequestType.ACTION;
                    }

                    //We are in a JSR-286 container
                    if(_PORTLET_RESOURCE_REQUEST_CLASS.isInstance(request))
                    {
                        return RequestType.RESOURCE;
                    }
                    
                    return RequestType.EVENT;
                }
                
                return RequestType.ACTION;
            }
        }
        
        return RequestType.SERVLET;
    }

    /**
     * This method is used when a ExternalContext object is not available,
     * like in TomahawkFacesContextFactory.
     * 
     * According to TOMAHAWK-1331, the object context could receive an
     * instance of javax.portlet.PortletContext or javax.portlet.PortletConfig,
     * so we check both cases.
     * 
     * @param context
     * @param request
     * @return
     */
    public static final RequestType getRequestType(Object context, Object request)
    {
        //Stuff is laid out strangely in this class in order to optimize
        //performance.  We want to do as few instanceof's as possible so
        //things are laid out according to the expected frequency of the
        //various requests occurring.

        if(_PORTLET_10_SUPPORTED || _PORTLET_20_SUPPORTED)
        {
            if (_PORTLET_CONFIG_CLASS.isInstance(context) ||
                _PORTLET_CONTEXT_CLASS.isInstance(context))
            {
                //We are inside of a portlet container
                
                if(_PORTLET_RENDER_REQUEST_CLASS.isInstance(request))
                {
                    return RequestType.RENDER;
                }
                
                if(_PORTLET_RESOURCE_REQUEST_CLASS != null)
                {
                    if(_PORTLET_ACTION_REQUEST_CLASS.isInstance(request))
                    {
                        return RequestType.ACTION;
                    }

                    //We are in a JSR-286 container
                    if(_PORTLET_RESOURCE_REQUEST_CLASS.isInstance(request))
                    {
                        return RequestType.RESOURCE;
                    }
                    
                    return RequestType.EVENT;
                }
                
                return RequestType.ACTION;
            }
        }
        
        return RequestType.SERVLET;
    }

    /**
     * Returns the value of {@link RequestType#isPortlet()} for the current
     * RequestType. This is a convenience function designed to perform a quick
     * check of the current request. If more capabilities need to be tested for
     * the given request, then it is more efficient to pull this information from
     * the RequestType itself.
     * 
     * @param ec the current external context
     * @return a boolean value of <code>true</code> if the current RequestType
     *         is a portlet request.
     * 
     * @see RequestType#isPortlet()
     * @see #getRequestType(ExternalContext)
     */
    public static boolean isPortlet(ExternalContext ec)
    {
        return getRequestType(ec).isPortlet();
    }

    /**
     * Returns wherther of not this external context represents a true HttpServletRequest or
     * not.  Some portal containers implement the PortletRequest/Response objects as 
     * HttpServletRequestWrappers, and those objects should not be treated as an
     * HttpServlerRequest.  As such, this method first tests to see if the request is
     * a portlet request and, if not, then tests to see if the request is an instanceof
     * HttpServletRequest.
     * 
     * @param ec the current external context
     * @return a boolean value of <code>true</code> if the current request is an
     *         HttpServletRequest
     * @since 1.1
     */
    public static boolean isHttpServletRequest(ExternalContext ec)
    {
        return (!isPortlet(ec) && (ec.getRequest() instanceof HttpServletRequest));
    }

    /**
     * Returns an HttpServletResponse if one exists on the externalContext or null
     * if it does not.  Please note that some portal environments implement the
     * PortletRequest and Response objects as HttpServletRequest/Response objects.
     * This method handles these types of requests properly and will therefore
     * return null in portal environments.
     * 
     * @param ec
     * @return an HttpServletResponse if we have one or null if we do not
     */
    public static HttpServletResponse getHttpServletResponse(ExternalContext ec)
    {
        if (isHttpServletRequest(ec))
        {
            return (HttpServletResponse) ec.getResponse();
        }

        return null;
    }

    // prevent this from being instantiated
    private ExternalContextUtils()
    {
    }

    private static final Logger _LOG = Logger
            .getLogger(ExternalContextUtils.class.getName());

    // =-= Scott O'Bryan =-=
    // Performance enhancement. These will be needed anyway, let's not get them every time.
    private static final Class<?> _PORTLET_ACTION_REQUEST_CLASS;
    private static final Class<?> _PORTLET_RENDER_REQUEST_CLASS;
    private static final Class<?> _PORTLET_RESOURCE_REQUEST_CLASS;
    private static final Class<?> _PORTLET_CONTEXT_CLASS;
    private static final boolean _PORTLET_10_SUPPORTED;
    private static final boolean _PORTLET_20_SUPPORTED;
    private static final Class<?> _PORTLET_CONFIG_CLASS;

    static
    {
        Class<?> context;
        Class<?> config;
        Class<?> actionRequest;
        Class<?> renderRequest;
        Class<?> resourceRequest;
        boolean portlet20Supported = false;
        boolean portlet10Supported = false;

        try
        {
            context = ClassUtils.forName("javax.portlet.PortletContext");
            config = ClassUtils.forName("javax.portlet.PortletConfig");
            actionRequest = ClassUtils.forName("javax.portlet.ActionRequest");
            renderRequest = ClassUtils.forName("javax.portlet.RenderRequest");

            try
            {
                resourceRequest = ClassUtils.forName("javax.portlet.ResourceRequest");
            }
            catch (ClassNotFoundException e)
            {
                _LOG.fine("Portlet 2.0 API is not available on classpath.  Portlet 2.0 functionality is disabled");
                resourceRequest = null;
            }
        }
        catch (final ClassNotFoundException e)
        {
            _LOG.fine("Portlet API is not available on the classpath.  Portlet configurations are disabled.");
            context = null;
            config = null;
            actionRequest = null;
            renderRequest = null;
            resourceRequest = null;
        }

        //Find bridge to tell if portal is supported
        if (context != null)
        {
            // Portlet 1.0 API found. In this case we have to consider that exists alternate
            // bridge implementations like in WebSphere and others.
            portlet10Supported = true;

            try
            {
                Class<?> bridge = ClassUtils.forName("javax.portlet.faces.Bridge");

                if (bridge != null)
                {
                    //Standard bridge defines a spec name which can be used to 
                    //determine Portlet 2.0 Support.
                    String specName = bridge.getPackage()
                            .getSpecificationTitle();
                    _LOG.fine("Found Bridge: " + specName);
                    if (specName != null && specName.startsWith("Portlet 2"))
                    {
                        portlet20Supported = true;
                    }

                    if (_LOG.isLoggable(Level.INFO))
                    {
                        String ver = (portlet20Supported) ? "2.0" : "1.0";
                        _LOG.info("Portlet Environment Detected: " + ver);
                    }
                }
            }
            catch (ClassNotFoundException e)
            {
                _LOG.fine("Portlet API is present but Standard Apache Portlet Bridge is not. "
                        + " This could happen if you are using an alternate Portlet Bridge solution.");

                if (resourceRequest != null)
                {
                    portlet20Supported = true;
                }
            }
        }

        _PORTLET_CONTEXT_CLASS = context;
        _PORTLET_CONFIG_CLASS = config;
        _PORTLET_ACTION_REQUEST_CLASS = actionRequest;
        _PORTLET_RENDER_REQUEST_CLASS = renderRequest;
        _PORTLET_RESOURCE_REQUEST_CLASS = resourceRequest;
        _PORTLET_10_SUPPORTED = portlet10Supported;
        _PORTLET_20_SUPPORTED = portlet20Supported;
    }

    /**
     * Trys to obtain a HttpServletResponse from the Response.
     * Note that this method also trys to unwrap any ServletResponseWrapper
     * in order to retrieve a valid HttpServletResponse.
     * @param response
     * @return if found, the HttpServletResponse, null otherwise
     */
    public static HttpServletResponse getHttpServletResponse(Object response)
    {
        // unwrap the response until we find a HttpServletResponse
        while (response != null)
        {
            if (response instanceof HttpServletResponse)
            {
                // found
                return (HttpServletResponse) response;
            }
            if (response instanceof ServletResponseWrapper)
            {
                // unwrap
                response = ((ServletResponseWrapper) response).getResponse();
            }
            // no more possibilities to find a HttpServletResponse
            break;
        }
        return null; // not found
    }
    
    
    
    
    /**
     * Represents the type of request currently in the ExternalContext.
     * All servlet requests will be of the SERVLET requestType whereas
     * all of the other RequestTypes will be portlet type requests.  There
     * are a number of convenience methods on the RequestType enumeration
     * which can be used to determine the capabilities of the current request.
     * 
     * @version $Revision$ $Date$
     */
    public enum RequestType
    {
        /**
         * The type for all servlet requests.  SERVLET request types are
         * both client requests and response writable.
         */
        SERVLET(true, true, false),

        /**
         * The type for a portlet RenderRequest.  RENDER request types are
         * for portlets and are response writable but are NOT client
         * requests.
         */
        RENDER(false, true, true),

        /**
         * The type for a portlet ActionRequest.  ACTION request types are
         * for portlets and are client requests but are NOT response 
         * writable.
         */
        ACTION(true, false, true),

        /**
         * The type for a portlet ResourceRequest.  RESOURCE request types
         * are for portlets and are both client requests and response 
         * writable.  RESOURCE request types will only be returned in a
         * Portlet 2.0 portlet container.
         */
        RESOURCE(true, true, true),

        /**
         * The type for a portlet EventRequest.  EVENT request types
         * are for portlets and are neither client requests nor response 
         * writable.  EVENT request types will only be returned in a
         * Portlet 2.0 portlet container.
         */        
        EVENT(false, false, true);

        private boolean _client;
        private boolean _writable;
        private boolean _portlet;

        RequestType(boolean client, boolean writable, boolean portlet)
        {
            _client = client;
            _writable  = writable;
            _portlet    = portlet;
        }

        /**
         * Returns <code>true</code> if this request was a direct
         * result of a call from the client.  This implies that
         * the current application is the "owner" of the current
         * request and that it has access to the inputStream, can
         * get and set character encodings, etc.  Currently all
         * SERVLET, ACTION, and RESOURCE RequestTypes are client
         * requests.
         * 
         * @return <code>true</code> if the current request is a
         *         client data type request and <code>false</code>
         *         if it is not.
         */
        public boolean isRequestFromClient()
        {
            return _client;
        }

        /**
         * Returns <code>true</code> if the response for this
         * RequestType is intended to produce output to the client.
         * Currently the SERVLET, RENDER, and RESOURCE request are
         * response writable.
         *  
         * @return <code>true</code> if the current request is 
         *         intended to produce output and <code>false</code>
         *         if it is not.
         */
        public boolean isResponseWritable()
        {
            return _writable;
        }

        /**
         * Returns <code>true</code> if the response for this
         * RequestType originated from a JSR-168 or JSR-286 
         * portlet container.  Currently RENDER, ACTION,
         * RESOURCE, and EVENT RequestTypes are all portlet
         * requests.
         * 
         * @return <code>true</code> if the current request
         *         originated inside of a JSR-168 or JSR-286
         *         Portlet Container or <code>false</code> if
         *         it did not.
         */
        public boolean isPortlet()
        {
            return _portlet;
        }
    }
}
