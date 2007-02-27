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
package org.apache.myfaces.context.servlet;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletContext;


/**
 * ServletContext attributes as a Map.
 *
 * @author Anton Koinov (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ApplicationMap extends AbstractAttributeMap<Object>
{
    final ServletContext _servletContext;

    ApplicationMap(ServletContext servletContext)
    {
        _servletContext = servletContext;
    }

    protected Object getAttribute(String key)
    {
        return _servletContext.getAttribute(key);
    }

    protected void setAttribute(String key, Object value)
    {
        _servletContext.setAttribute(key, value);
    }

    protected void removeAttribute(String key)
    {
        _servletContext.removeAttribute(key);
    }

    @SuppressWarnings("unchecked")
    protected Enumeration<String> getAttributeNames()
    {
        return _servletContext.getAttributeNames();
    }


    public void putAll(Map t)
    {
        throw new UnsupportedOperationException();
    }


    public void clear()
    {
        throw new UnsupportedOperationException();
    }
}
