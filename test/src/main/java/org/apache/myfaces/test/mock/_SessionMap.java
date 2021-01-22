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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * HttpSession attibutes as Map.
 *
 * @author Anton Koinov (latest modification by $Author: jakobk $)
 * @version $Revision: 979229 $ $Date: 2010-07-26 12:26:53 +0200 (Mo, 26 Jul 2010) $
 * @since 1.0.0
 */
final class _SessionMap extends _AbstractAttributeMap<Object>
{
    private final HttpServletRequest _httpRequest;

    _SessionMap(final HttpServletRequest httpRequest)
    {
        _httpRequest = httpRequest;
    }

    @Override
    protected Object getAttribute(final String key)
    {
        final HttpSession httpSession = _getSession();
        return (httpSession == null) ? null : httpSession.getAttribute(key);
    }

    @Override
    protected void setAttribute(final String key, final Object value)
    {
        _httpRequest.getSession(true).setAttribute(key, value);
    }

    @Override
    protected void removeAttribute(final String key)
    {
        final HttpSession httpSession = _getSession();
        if (httpSession != null)
        {
            httpSession.removeAttribute(key);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Enumeration<String> getAttributeNames()
    {
        final HttpSession httpSession = _getSession();
        return (httpSession == null) ? _NullEnumeration.instance()
                : httpSession.getAttributeNames();
    }

    @Override
    public void putAll(final Map<? extends String, ? extends Object> t)
    {
        throw new UnsupportedOperationException();
    }

    // we can use public void clear() from super-class

    private HttpSession _getSession()
    {
        return _httpRequest.getSession(false);
    }

}
