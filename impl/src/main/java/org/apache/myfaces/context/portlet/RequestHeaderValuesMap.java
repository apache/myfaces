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
package org.apache.myfaces.context.portlet;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.portlet.PortletRequest;

import org.apache.myfaces.context.servlet.AbstractAttributeMap;

/**
 * PortletRequest header values (multi-value headers) as Map of String[].
 *
 * @author  Stan Silvert (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class RequestHeaderValuesMap extends AbstractAttributeMap
{
    private final PortletRequest _portletRequest;
    private final Map<String, Object>            _valueCache = new HashMap<String, Object>();

    RequestHeaderValuesMap(PortletRequest portletRequest)
    {
        _portletRequest = portletRequest;
    }

    protected Object getAttribute(String key)
    {
        Object ret = _valueCache.get(key);
        if (ret == null)
        {
            _valueCache.put(key, ret = toArray(_portletRequest
                .getProperties(key)));
        }

        return ret;
    }

    protected void setAttribute(String key, Object value)
    {
        throw new UnsupportedOperationException(
            "Cannot set PortletRequest Properties");
    }

    protected void removeAttribute(String key)
    {
        throw new UnsupportedOperationException(
            "Cannot remove PortletRequest Properties");
    }

    protected Enumeration getAttributeNames()
    {
        return _portletRequest.getPropertyNames();
    }

    private String[] toArray(Enumeration e)
    {
        List ret = new ArrayList();

        while (e.hasMoreElements())
        {
            ret.add(e.nextElement());
        }

        return (String[]) ret.toArray(new String[ret.size()]);
    }
}