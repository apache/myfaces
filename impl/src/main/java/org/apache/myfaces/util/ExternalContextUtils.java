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

import java.util.Map;
import org.apache.myfaces.util.lang.ClassUtils;


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
    private static final Class<?> PORTLET_CONTEXT_CLASS =
            ClassUtils.simpleClassForName("javax.portlet.PortletContext", false);

    /**
     * Constant defined on javax.portlet.faces.Bridge class that helps to 
     * define if the current request is a portlet request or not.
     */
    private static final String PORTLET_LIFECYCLE_PHASE = "javax.portlet.faces.phase";    
    
    /**
     * This is a convenience function designed to perform a quick check of the current {@link ExternalContext}.
     * 
     * @param ec the current external context
     * @return <code>true</code> if the current {@link ExternalContext} is a Porlet.
     */
    public static boolean isPortlet(ExternalContext ec)
    {
        if (PORTLET_CONTEXT_CLASS == null)
        {
            return false;
        }
        
        if (PORTLET_CONTEXT_CLASS.isInstance(ec.getContext()))
        {
            return true;
        }
        
        Map<String, Object> requestMap = ec.getRequestMap();
        if (requestMap.containsKey(PORTLET_LIFECYCLE_PHASE))
        {
            return true;
        }
        
        return false;
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

    // prevent this from being instantiated
    private ExternalContextUtils()
    {
    }
}
