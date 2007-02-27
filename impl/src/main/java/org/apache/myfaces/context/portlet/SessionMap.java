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

import org.apache.myfaces.shared_impl.util.NullEnumeration;
import org.apache.myfaces.util.AbstractAttributeMap;

import java.util.Enumeration;
import java.util.Map;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

/**
 * Portlet scope PortletSession attibutes as Map.
 *
 * @author  Stan Silvert (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class SessionMap extends AbstractAttributeMap<Object>
{
    private final PortletRequest _portletRequest;

    SessionMap(PortletRequest portletRequest)
    {
        _portletRequest = portletRequest;
    }

    @Override
    protected Object getAttribute(String key)
    {
        PortletSession portletSession = getSession();
        return (portletSession == null)
               ? null : portletSession.getAttribute(key.toString(), PortletSession.PORTLET_SCOPE);
    }

    @Override
    protected void setAttribute(String key, Object value)
    {
        _portletRequest.getPortletSession(true).setAttribute(key, value, PortletSession.PORTLET_SCOPE);
    }

    @Override
    protected void removeAttribute(String key)
    {
        PortletSession portletSession = getSession();
        if (portletSession != null)
        {
            portletSession.removeAttribute(key, PortletSession.PORTLET_SCOPE);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Enumeration<String> getAttributeNames()
    {
        PortletSession portletSession = getSession();
        return (portletSession == null)
               ? NullEnumeration.instance()
               : portletSession.getAttributeNames(PortletSession.PORTLET_SCOPE);
    }

    private PortletSession getSession()
    {
        return _portletRequest.getPortletSession(false);
    }

    @Override
    public void putAll(Map t)
    {
        throw new UnsupportedOperationException();
    }


    /**
     * This will clear the session without invalidation.  If no session has
     * been created, it will simply return.
     */
    @Override
    public void clear()
    {
        PortletSession session = getSession();
        if (session == null) return;
        for (Enumeration attributeNames = session.getAttributeNames(PortletSession.PORTLET_SCOPE); 
             attributeNames.hasMoreElements(); ) {
            String attributeName = (String)attributeNames.nextElement();
            session.removeAttribute(attributeName);
        }
    }
}