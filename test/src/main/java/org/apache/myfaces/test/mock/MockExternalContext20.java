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

package org.apache.myfaces.test.mock;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.Flash;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>Mock implementation of <code>ExternalContext</code> that includes the semantics
 * added by JavaServer Faces 2.0.</p>
 * 
 * @author Leonardo Uribe
 * @since 1.0.0
 *
 */
public abstract class MockExternalContext20 extends MockExternalContext12
{

    // ------------------------------------------------------------ Constructors

    public MockExternalContext20(ServletContext context,
            HttpServletRequest request, HttpServletResponse response)
    {
        super(context, request, response);
    }

    public String getMimeType(String file)
    {
        return context.getMimeType(file);
    }

    // ------------------------------------------------------ Instance Variables

    private static final String URL_PARAM_SEPERATOR = "&";
    private static final String URL_QUERY_SEPERATOR = "?";
    private static final String URL_FRAGMENT_SEPERATOR = "#";
    private static final String URL_NAME_VALUE_PAIR_SEPERATOR = "=";

    // ----------------------------------------------------- Mock Object Methods

    // ------------------------------------------------- ExternalContext Methods

    public String encodeBookmarkableURL(String baseUrl,
            Map<String, List<String>> parameters)
    {
        return response.encodeURL(encodeURL(baseUrl, parameters));
    }

    private String encodeURL(String baseUrl,
            Map<String, List<String>> parameters)
    {
        String fragment = null;
        String queryString = null;
        Map<String, List<String>> paramMap = new HashMap<String, List<String>>();

        //extract any URL fragment
        int index = baseUrl.indexOf(URL_FRAGMENT_SEPERATOR);
        if (index != -1)
        {
            fragment = baseUrl.substring(index + 1);
            baseUrl = baseUrl.substring(0, index);
        }

        //extract the current query string and add the params to the paramMap
        index = baseUrl.indexOf(URL_QUERY_SEPERATOR);
        if (index != -1)
        {
            queryString = baseUrl.substring(index + 1);
            baseUrl = baseUrl.substring(0, index);
            String[] nameValuePairs = queryString.split(URL_PARAM_SEPERATOR);
            for (int i = 0; i < nameValuePairs.length; i++)
            {
                String[] currentPair = nameValuePairs[i]
                        .split(URL_NAME_VALUE_PAIR_SEPERATOR);
                if (currentPair[1] != null)
                {
                    ArrayList<String> value = new ArrayList<String>(1);
                    value.add(currentPair[1]);
                    paramMap.put(currentPair[0], value);
                }
            }
        }

        //add/update with new params on the paramMap
        if (parameters != null && parameters.size() > 0)
        {
            for (Map.Entry<String, List<String>> pair : parameters.entrySet())
            {
                if (pair.getKey() != null && pair.getKey().trim().length() != 0)
                {
                    paramMap.put(pair.getKey(), pair.getValue());
                }
            }
        }

        // start building the new URL
        StringBuilder newUrl = new StringBuilder(baseUrl);

        //now add the updated param list onto the url
        if (paramMap.size() > 0)
        {
            boolean isFirstPair = true;
            for (Map.Entry<String, List<String>> pair : paramMap.entrySet())
            {
                for (String value : pair.getValue())
                {
                    if (!isFirstPair)
                    {
                        newUrl.append(URL_PARAM_SEPERATOR);
                    }
                    else
                    {
                        newUrl.append(URL_QUERY_SEPERATOR);
                        isFirstPair = false;
                    }

                    newUrl.append(pair.getKey());
                    newUrl.append(URL_NAME_VALUE_PAIR_SEPERATOR);
                    try
                    {
                        newUrl.append(URLEncoder.encode(value,
                                getResponseCharacterEncoding()));
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        //shouldn't ever get here
                        throw new UnsupportedOperationException(
                                "Encoding type="
                                        + getResponseCharacterEncoding()
                                        + " not supported", e);
                    }
                }
            }
        }

        //add the fragment back on (if any)
        if (fragment != null)
        {
            newUrl.append(URL_FRAGMENT_SEPERATOR + fragment);
        }

        return newUrl.toString();
    }

    public String encodeRedirectURL(String baseUrl,
            Map<String, List<String>> parameters)
    {
        return response.encodeRedirectURL(encodeURL(baseUrl, parameters));
    }

    @Override
    public String encodePartialActionURL(String url)
    {
        return ((HttpServletResponse) response).encodeURL(url);
    }

    public String getContextName()
    {
        return context.getServletContextName();
    }

    public String getRealPath(String path)
    {
        return context.getRealPath(path);
    }

    public void responseSendError(int statusCode, String message)
            throws IOException
    {
        if (message == null)
        {
            response.sendError(statusCode);
        }
        else
        {
            response.sendError(statusCode, message);
        }
    }

    public void setResponseHeader(String name, String value)
    {
        response.setHeader(name, value);
    }

    public String getRequestScheme()
    {
        return request.getScheme();
    }

    public String getRequestServerName()
    {
        return request.getServerName();
    }

    public int getRequestServerPort()
    {
        return request.getServerPort();
    }

    public OutputStream getResponseOutputStream() throws IOException
    {
        return response.getOutputStream();
    }

    public Writer getResponseOutputWriter() throws IOException
    {
        return response.getWriter();
    }

    @Override
    public void setResponseContentType(String contentType)
    {
        response.setContentType(contentType);
    }

    public Flash getFlash()
    {
        return MockFlash.getCurrentInstance(this);
    }

    @Override
    public void setResponseContentLength(int length)
    {
        response.setContentLength(length);
    }

    @Override
    public int getRequestContentLength()
    {
        return request.getContentLength();
    }

    @Override
    public int getResponseBufferSize()
    {
        return response.getBufferSize();
    }

    @Override
    public void setResponseBufferSize(int size)
    {
        response.setBufferSize(size);
    }

    @Override
    public void setResponseStatus(int statusCode)
    {
        response.setStatus(statusCode);
    }

    @Override
    public void invalidateSession()
    {
        HttpSession session = request.getSession(false);
        if (session != null)
        {
            session.invalidate();
        }
    }

    @Override
    public boolean isResponseCommitted()
    {
        return response.isCommitted();
    }

    @Override
    public void responseFlushBuffer() throws IOException
    {
        response.flushBuffer();
    }

    @Override
    public void responseReset()
    {
        response.reset();
    }

    @Override
    public void addResponseCookie(String name, String value,
            Map<String, Object> properties)
    {
        Cookie cookie = new Cookie(name, value);
        if (properties != null)
        {
            for (Map.Entry<String, Object> entry : properties.entrySet())
            {
                String propertyKey = entry.getKey();
                Object propertyValue = entry.getValue();
                if ("comment".equals(propertyKey))
                {
                    cookie.setComment((String) propertyValue);
                    continue;
                }
                else if ("domain".equals(propertyKey))
                {
                    cookie.setDomain((String)propertyValue);
                    continue;
                }
                else if ("maxAge".equals(propertyKey))
                {
                    cookie.setMaxAge((Integer) propertyValue);
                    continue;
                }
                else if ("secure".equals(propertyKey))
                {
                    cookie.setSecure((Boolean) propertyValue);
                    continue;
                }
                else if ("path".equals(propertyKey))
                {
                    cookie.setPath((String) propertyValue);
                    continue;
                }
                throw new IllegalArgumentException("Unused key when creating Cookie");
            }
        }
        response.addCookie(cookie);
    }

    @Override
    public void addResponseHeader(String name, String value)
    {
        response.addHeader(name, value);
    }
    
}
