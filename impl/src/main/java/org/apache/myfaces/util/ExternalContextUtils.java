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

import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.myfaces.context.servlet.HttpServletResponseSwitch;
import org.apache.myfaces.context.servlet.ResponseSwitch;
import org.apache.myfaces.context.servlet.ServletResponseSwitch;

/**
 * Utility class for ExternalContext methods.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public final class ExternalContextUtils
{
    
    private ExternalContextUtils()
    {
    }
    
    /**
     * Trys to obtain a HttpServletResponse from the Response.
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
     * Trys to obtain a ResponseSwitch from the Response.
     * @param response
     * @return if found, the ResponseSwitch, null otherwise
     */
    public static ResponseSwitch getResponseSwitch(Object response)
    {
        // unwrap the response until we find a ResponseSwitch
        while (response != null)
        {
            if (response instanceof ResponseSwitch)
            {
                // found
                return (ResponseSwitch) response;
            }
            if (response instanceof ServletResponseWrapper)
            {
                // unwrap
                response = ((ServletResponseWrapper) response).getResponse();
            }
            // no more possibilities to find a ResponseSwitch
            break; 
        }
        return null; // not found
    }
    
    /**
     * Try to create a ResponseSwitch for this response.
     * @param response
     * @return the created ResponseSwitch, if there is a ResponseSwitch 
     *         implementation for the given response, null otherwise
     */
    public static ResponseSwitch createResponseSwitch(Object response)
    {
        if (response instanceof HttpServletResponse)
        {
            return new HttpServletResponseSwitch((HttpServletResponse) response);
        }
        else if (response instanceof ServletResponse)
        {
            return new ServletResponseSwitch((ServletResponse) response);
        }
        return null;
    }
    
}
