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
package org.apache.myfaces.context.portlet;

import java.util.Enumeration;
import java.util.Map;
import javax.portlet.PortletContext;

import org.apache.myfaces.util.AbstractAttributeMap;


/**
 * PortletContext attributes as a Map.
 *
 * @author  Stan Silvert (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ApplicationMap extends AbstractAttributeMap<Object>
{
    final PortletContext _portletContext;

    ApplicationMap(PortletContext portletContext)
    {
        _portletContext = portletContext;
    }

    @Override
    protected Object getAttribute(String key)
    {
        return _portletContext.getAttribute(key);
    }

    @Override
    protected void setAttribute(String key, Object value)
    {
        _portletContext.setAttribute(key, value);
    }

    @Override
    protected void removeAttribute(String key)
    {
        _portletContext.removeAttribute(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Enumeration<String> getAttributeNames()
    {
        return _portletContext.getAttributeNames();
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> t)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }    
}
