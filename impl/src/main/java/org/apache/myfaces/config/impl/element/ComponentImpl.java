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
package org.apache.myfaces.config.impl.element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class ComponentImpl extends org.apache.myfaces.config.element.Component implements Serializable
{
    private String componentType;
    private String componentClass;
    private List<org.apache.myfaces.config.element.Property> properties = null;
    private List<org.apache.myfaces.config.element.Attribute> attributes = null;

    public ComponentImpl()
    {

    }

    public ComponentImpl(String componentType, String componentClass)
    {
        this.componentType = componentType;
        this.componentClass = componentClass;
    }

    public void setComponentType(String componentType)
    {
        this.componentType = componentType;
    }

    public void setComponentClass(String componentClass)
    {
        this.componentClass = componentClass;
    }

    @Override
    public String getComponentType()
    {
        return componentType;
    }

    @Override
    public String getComponentClass()
    {
        return componentClass;
    }

    public void addProperty(org.apache.myfaces.config.element.Property value)
    {
        if (properties == null)
        {
            properties = new ArrayList<>();
        }
        properties.add(value);
    }

    @Override
    public List<? extends org.apache.myfaces.config.element.Property> getProperties()
    {
        if (properties == null)
        {
            return Collections.emptyList();
        }
        return properties;
    }
    
    public void addAttribute(org.apache.myfaces.config.element.Attribute value)
    {
        if (attributes == null)
        {
            attributes = new ArrayList<>();
        }
        attributes.add(value);
    }

    @Override
    public List<? extends org.apache.myfaces.config.element.Attribute> getAttributes()
    {
        if (attributes == null)
        {
            return Collections.emptyList();
        }
        return attributes;
    }
}
