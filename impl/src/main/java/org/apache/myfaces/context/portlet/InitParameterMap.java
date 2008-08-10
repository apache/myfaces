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
 * ServletContext init parameters as Map.
 *
 * @author  Stan Silvert (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class InitParameterMap extends AbstractAttributeMap<String>
{
    final PortletContext _portletContext;

    InitParameterMap(PortletContext portletContext)
    {
        _portletContext = portletContext;
    }

    @Override
    protected String getAttribute(String key)
    {
        return _portletContext.getInitParameter(key);
    }

    @Override
    protected void setAttribute(String key, String value)
    {
        throw new UnsupportedOperationException(
            "Cannot set PortletContext InitParameter");
    }

    @Override
    protected void removeAttribute(String key)
    {
        throw new UnsupportedOperationException(
            "Cannot remove PortletContext InitParameter");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Enumeration<String> getAttributeNames()
    {
        return _portletContext.getInitParameterNames();
    }

    @Override
    public boolean equals(Object o) {
        boolean retValue;

        retValue = super.equals(o);
        return retValue;
    }

    @Override
    public int hashCode() {
        int retValue;

        retValue = super.hashCode();
        return retValue;
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
