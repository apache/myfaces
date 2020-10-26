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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.myfaces.util.lang.AbstractAttributeMap;

/**
 * HttpServletRequest header values (multi-value headers) as Map of String[].
 * 
 * @author Anton Koinov (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public final class RequestHeaderValuesMap extends AbstractAttributeMap<String[]>
{
    private final HttpServletRequest httpServletRequest;
    private final Map<String, String[]> attributeValueCache = new HashMap<>();

    RequestHeaderValuesMap(final HttpServletRequest httpServletRequest)
    {
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    protected String[] getAttribute(final String key)
    {
        String[] attributes = attributeValueCache.get(key);
        if (attributes == null)
        {
            List<String> attributesList = Collections.list(httpServletRequest.getHeaders(key));

            attributes = attributesList.toArray(new String[attributesList.size()]);
            attributeValueCache.put(key, attributes);
        }

        return attributes;
    }

    @Override
    protected void setAttribute(final String key, final String[] value)
    {
        throw new UnsupportedOperationException("Cannot set HttpServletRequest HeaderValues");
    }

    @Override
    protected void removeAttribute(final String key)
    {
        throw new UnsupportedOperationException("Cannot remove HttpServletRequest HeaderValues");
    }

    @Override
    protected Enumeration<String> getAttributeNames()
    {
        return httpServletRequest.getHeaderNames();
    }
}
