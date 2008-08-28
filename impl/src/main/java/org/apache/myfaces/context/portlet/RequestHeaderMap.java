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

import javax.portlet.PortletRequest;

import org.apache.myfaces.util.AbstractAttributeMap;



/**
 * PortletRequest headers as Map.
 *
 * @author  Stan Silvert (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class RequestHeaderMap extends AbstractAttributeMap<String>
{
    private final PortletRequest _portletRequest;

    RequestHeaderMap(PortletRequest portletRequest)
    {
        _portletRequest = portletRequest;
    }

    @Override
    protected String getAttribute(String key)
    {
        return _portletRequest.getProperty(key);
    }

    @Override
    protected void setAttribute(String key, String value)
    {
        throw new UnsupportedOperationException(
            "Cannot set PortletRequest property");
    }

    @Override
    protected void removeAttribute(String key)
    {
        throw new UnsupportedOperationException(
            "Cannot remove PortletRequest property");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Enumeration<String> getAttributeNames()
    {
        return _portletRequest.getPropertyNames();
    }

    @Override
    public void putAll(Map t)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }    
}
