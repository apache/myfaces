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
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.apache.myfaces.util.lang.AbstractThreadSafeAttributeMap;

/**
 * HttpSession attributes as Map.
 *
 * @author Anton Koinov (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public final class SessionMap extends AbstractThreadSafeAttributeMap<Object>
{
    private final HttpServletRequest httpRequest;

    SessionMap(final HttpServletRequest httpRequest)
    {
        this.httpRequest = httpRequest;
    }

    @Override
    protected Object getAttribute(final String key)
    {
        HttpSession httpSession = httpRequest.getSession(false);
        return httpSession == null ? null : httpSession.getAttribute(key);
    }

    @Override
    protected void setAttribute(final String key, final Object value)
    {
        httpRequest.getSession(true).setAttribute(key, value);
    }

    @Override
    protected void removeAttribute(final String key)
    {
        HttpSession httpSession = httpRequest.getSession(false);
        if (httpSession != null)
        {
            httpSession.removeAttribute(key);
        }
    }

    @Override
    protected Enumeration<String> getAttributeNames()
    {
        HttpSession httpSession = httpRequest.getSession(false);
        return httpSession == null ? Collections.emptyEnumeration() : httpSession.getAttributeNames();
    }

    @Override
    public void putAll(final Map<? extends String, ? extends Object> t)
    {
        throw new UnsupportedOperationException();
    }
}
