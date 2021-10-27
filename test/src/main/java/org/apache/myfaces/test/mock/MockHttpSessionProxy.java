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

import java.util.Enumeration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionEvent;

/**
 * Proxy used to trigger session creation when it is accessed
 */
public class MockHttpSessionProxy extends MockHttpSession
{
    private MockServletContext servletContext;
    private MockHttpServletRequest request;
    private MockHttpSession delegate;

    public MockHttpSessionProxy(MockServletContext servletContext,
        MockHttpServletRequest request)
    {
        super(servletContext);
        this.servletContext = servletContext;
    }
    
    @Override
    public void addAttributeListener(HttpSessionAttributeListener listener)
    {
        getWrapped().addAttributeListener(listener);
    }

    @Override
    public void setServletContext(ServletContext servletContext)
    {
        if (servletContext instanceof MockServletContext)
        {
            this.servletContext = (MockServletContext) servletContext;
        }
        getWrapped().setServletContext(servletContext);
    }

    @Override
    public void setId(String id)
    {
        getWrapped().setId(id);
    }

    @Override
    public Object getAttribute(String name)
    {
        return getWrapped().getAttribute(name);
    }

    @Override
    public Enumeration getAttributeNames()
    {
        return getWrapped().getAttributeNames();
    }

    @Override
    public long getCreationTime()
    {
        return getWrapped().getCreationTime();
    }

    @Override
    public String getId()
    {
        return getWrapped().getId();
    }

    @Override
    public long getLastAccessedTime()
    {
        return getWrapped().getLastAccessedTime();
    }

    @Override
    public int getMaxInactiveInterval()
    {
        return getWrapped().getMaxInactiveInterval();
    }

    @Override
    public ServletContext getServletContext()
    {
        return servletContext == null ? getWrapped().getServletContext() : servletContext;
    }

    public Object getValue(String name)
    {
        return getWrapped().getValue(name);
    }

    @Override
    public String[] getValueNames()
    {
        return getWrapped().getValueNames();
    }

    @Override
    public void invalidate()
    {
        getWrapped().invalidate();
    }

    @Override
    public boolean isNew()
    {
        return getWrapped().isNew();
    }

    @Override
    public void putValue(String name, Object value)
    {
        getWrapped().putValue(name, value);
    }

    @Override
    public void removeAttribute(String name)
    {
        getWrapped().removeAttribute(name);
    }

    @Override
    public void removeValue(String name)
    {
        getWrapped().removeValue(name);
    }

    @Override
    public void setAttribute(String name, Object value)
    {
        getWrapped().setAttribute(name, value);
    }

    @Override
    public void setMaxInactiveInterval(int interval)
    {
        getWrapped().setMaxInactiveInterval(interval);
    }
    
    public MockHttpSession getWrapped()
    {
        if (delegate == null)
        {
            if (request != null)
            {
                delegate = (MockHttpSession) request.getSession(true);
            }
            else
            {
                delegate = new MockHttpSession(this.servletContext);
                MockWebContainer container = getWebContainer();
                if (container != null)
                {
                    HttpSessionEvent se = new HttpSessionEvent(delegate);
                    container.sessionCreated(se);
                }
            }
        }
        return delegate;
    }

    /**
     * @return the request
     */
    public MockHttpServletRequest getRequest()
    {
        return request;
    }

    /**
     * @param request the request to set
     */
    public void setRequest(MockHttpServletRequest request)
    {
        this.request = request;
    }
}
