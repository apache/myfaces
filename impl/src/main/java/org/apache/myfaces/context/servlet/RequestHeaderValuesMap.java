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
package org.apache.myfaces.context.servlet;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.myfaces.util.AbstractAttributeMap;

/**
 * HttpServletRequest header values (multi-value headers) as Map of String[].
 * 
 * @author Anton Koinov (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class RequestHeaderValuesMap extends AbstractAttributeMap<String[]>
{
    private final HttpServletRequest _httpServletRequest;
    private final Map<String, String[]> _valueCache = new HashMap<String, String[]>();

    RequestHeaderValuesMap(HttpServletRequest httpServletRequest)
    {
        _httpServletRequest = httpServletRequest;
    }

    protected String[] getAttribute(String key)
    {
        String[] ret = _valueCache.get(key);
        if (ret == null)
        {
            _valueCache.put(key, ret = toArray(_httpServletRequest.getHeaders(key)));
        }

        return ret;
    }

    protected void setAttribute(String key, String[] value)
    {
        throw new UnsupportedOperationException("Cannot set HttpServletRequest HeaderValues");
    }

    protected void removeAttribute(String key)
    {
        throw new UnsupportedOperationException("Cannot remove HttpServletRequest HeaderValues");
    }

    @SuppressWarnings("unchecked")
    protected Enumeration<String> getAttributeNames()
    {
        return _httpServletRequest.getHeaderNames();
    }

    private String[] toArray(Enumeration<String> e)
    {
        List<String> ret = new ArrayList<String>();

        while (e.hasMoreElements())
        {
            ret.add(e.nextElement());
        }

        return (String[]) ret.toArray(new String[ret.size()]);
    }
}