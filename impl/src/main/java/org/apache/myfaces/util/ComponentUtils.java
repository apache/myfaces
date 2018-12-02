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

package org.apache.myfaces.util;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;

public class ComponentUtils
{
    public static String getPathToComponent(UIComponent component)
    {
        StringBuilder buf = new StringBuilder();

        if (component == null)
        {
            buf.append("{Component-Path : ");
            buf.append("[null]}");
            return buf.toString();
        }

        getPathToComponent(component, buf);

        buf.insert(0, "{Component-Path : ");
        Object location = component.getAttributes().get(
                UIComponent.VIEW_LOCATION_KEY);
        if (location != null)
        {
            buf.append(" Location: ").append(location);
        }
        buf.append('}');

        return buf.toString();
    }

    private static void getPathToComponent(UIComponent component, StringBuilder buf)
    {
        if (component == null)
        {
            return;
        }

        StringBuilder intBuf = new StringBuilder();

        intBuf.append("[Class: ");
        intBuf.append(component.getClass().getName());
        if (component instanceof UIViewRoot)
        {
            intBuf.append(",ViewId: ");
            intBuf.append(((UIViewRoot) component).getViewId());
        }
        else
        {
            intBuf.append(",Id: ");
            intBuf.append(component.getId());
        }
        intBuf.append(']');

        buf.insert(0, intBuf.toString());

        getPathToComponent(component.getParent(), buf);
    }
    
    public static <T> T closest(Class<T> type, UIComponent base) 
    {
        UIComponent parent = base.getParent();

        while (parent != null) 
        {
            if (type.isAssignableFrom(parent.getClass())) 
            {
                return (T) parent;
            }

            parent = parent.getParent();
        }

        return null;
    }
    
    public static UIComponent getRootComponent(UIComponent component)
    {
        UIComponent parent;
        for (;;)
        {
            parent = component.getParent();
            if (parent == null)
            {
                return component;
            }
            component = parent;
        }
    }
}
