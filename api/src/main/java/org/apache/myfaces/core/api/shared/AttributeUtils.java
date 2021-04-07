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
package org.apache.myfaces.core.api.shared;

import jakarta.faces.component.UIComponent;

public class AttributeUtils
{
    public static boolean getBooleanAttribute(UIComponent component,
            String attrName, boolean defaultValue)
    {
        Object value = component.getAttributes().get(attrName);
        if (value == null)
        {
            return defaultValue;
        }

        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }

        // If the value is a String, parse it.
        // This makes the following code work: <tag attribute="true" />,
        // otherwise you would have to write <tag attribute="#{true}" />.
        return Boolean.valueOf(value.toString());
    }

    public static int getIntegerAttribute(UIComponent component,
            String attrName, int defaultValue)
    {
        Object value = component.getAttributes().get(attrName);
        if (value == null)
        {
            return defaultValue;
        }

        if (value instanceof Integer)
        {
            return (Integer) value;
        }

        // If the value is a String, parse it.
        // This makes the following code work: <tag attribute="true" />,
        // otherwise you would have to write <tag attribute="#{true}" />.
        return Integer.valueOf(value.toString());
    }
}
