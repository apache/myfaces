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
import java.util.Map;

import jakarta.servlet.ServletRequest;

/**
 * ServletRequest attributes Map.
 * 
 * @author Anton Koinov (latest modification by $Author: slessard $)
 * @version $Revision: 698799 $ $Date: 2008-09-25 04:03:47 +0200 (Do, 25 Sep 2008) $
 * @since 1.0.0
 */
final class _RequestMap extends _AbstractAttributeMap<Object>
{
    final ServletRequest _servletRequest;

    _RequestMap(final ServletRequest servletRequest)
    {
        _servletRequest = servletRequest;
    }

    @Override
    protected Object getAttribute(final String key)
    {
        return _servletRequest.getAttribute(key);
    }

    @Override
    protected void setAttribute(final String key, final Object value)
    {
        _servletRequest.setAttribute(key, value);
    }

    @Override
    protected void removeAttribute(final String key)
    {
        _servletRequest.removeAttribute(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Enumeration<String> getAttributeNames()
    {
        return _servletRequest.getAttributeNames();
    }

    @Override
    public void putAll(final Map<? extends String, ? extends Object> t)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }
}
