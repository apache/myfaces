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
package org.apache.myfaces.lifecycle.clientwindow;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import org.apache.myfaces.util.token.TokenGenerator;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.ResponseStateManager;
import org.apache.myfaces.util.lang.StringUtils;

public class UrlRedirectClientWindow extends UrlClientWindow
{
    
    public UrlRedirectClientWindow(TokenGenerator tokenGenerator)
    {
        super(tokenGenerator);
    }

     @Override
    public void decode(FacesContext context)
    {
        //1. If it comes as parameter, it takes precedence over any other choice, because
        //   no browser is capable to do a POST and create a new window at the same time.
        String requestWindowId = context.getExternalContext().getRequestParameterMap().get(
                ResponseStateManager.CLIENT_WINDOW_PARAM);
        
        if (requestWindowId == null)
        {
            requestWindowId = context.getExternalContext().getRequestParameterMap().get(
                    ResponseStateManager.CLIENT_WINDOW_URL_PARAM);
        }

        if (requestWindowId == null)
        {
            requestWindowId = tokenGenerator.getNextToken();
            setId(requestWindowId);
            
            try
            {
                // this will also include the new generated windowId
                String redirectUrl = constructInitialRedirectUrl(context.getExternalContext());
                context.getExternalContext().redirect(redirectUrl);
            }
            catch (IOException e)
            {
                throw new FacesException("Could not send initial redirect!", e);
            }

            context.responseComplete();
        }
        
        setId(requestWindowId);
    }

    protected String constructInitialRedirectUrl(ExternalContext externalContext)
    {
        String url = externalContext.getRequestContextPath() + externalContext.getRequestServletPath();
        if (externalContext.getRequestPathInfo() != null)
        {
            url += externalContext.getRequestPathInfo();
        }
        
        url = addRequestParameters(externalContext, url);
        url = externalContext.encodeRedirectURL(url, null);
        
        return url;
    }
    
    public static String addRequestParameters(ExternalContext externalContext, String url)
    {
        if (externalContext.getRequestParameterValuesMap().isEmpty())
        {
            return url;
        }

        StringBuilder finalUrl = new StringBuilder(url);
        boolean existingParameters = url.contains("?");

        for (Map.Entry<String, String[]> entry : externalContext.getRequestParameterValuesMap().entrySet())
        {
            for (String value : entry.getValue())
            {
                if (!url.contains(entry.getKey() + "=" + value) &&
                        !url.contains(entry.getKey() + "=" + encodeURLParameterValue(value, externalContext)))
                {
                    if (StringUtils.isEmpty(entry.getKey()) && StringUtils.isEmpty(value))
                    {
                        continue;
                    }

                    if (!existingParameters)
                    {
                        finalUrl.append("?");
                        existingParameters = true;
                    }
                    else
                    {
                        finalUrl.append("&");
                    }

                    finalUrl.append(encodeURLParameterValue(entry.getKey(), externalContext));
                    finalUrl.append("=");
                    finalUrl.append(encodeURLParameterValue(value, externalContext));
                }
            }
        }

        return finalUrl.toString();
    }
    
    /**
     * Encodes the given value using URLEncoder.encode() with the charset returned
     * from ExternalContext.getResponseCharacterEncoding().
     * This is exactly how the ExternalContext impl encodes URL parameter values.
     *
     * @param value           value which should be encoded
     * @param externalContext current external-context
     * @return encoded value
     */
    public static String encodeURLParameterValue(String value, ExternalContext externalContext)
    {
        try
        {
            return URLEncoder.encode(value, externalContext.getResponseCharacterEncoding());
        }
        catch (UnsupportedEncodingException e)
        {
            throw new UnsupportedOperationException("Encoding type="
                    + externalContext.getResponseCharacterEncoding() + " not supported", e);
        }
    }
}
