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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Martin Marinschek
 * @version $Revision$ $Date$
 *
 * The "property" element represents a JavaBean property of the Java class
 * represented by our parent element.
 *
 * Property names must be unique within the scope of the Java class
 * that is represented by the parent element, and must correspond to
 * property names that will be recognized when performing introspection
 * against that class via java.beans.Introspector.
 *
 */
public class PropertyImpl extends org.apache.myfaces.config.element.Property implements Serializable
{
    private List<String> description;
    private List<String> displayName;
    private List<String> icon;
    private String propertyName;
    private String propertyClass;
    private String defaultValue;
    private String suggestedValue;
    private List<String> propertyExtensions;

    public void addDescription(String value)
    {
        if (description == null)
        {
            description = new ArrayList<>(1);
        }
        description.add(value);
    }

    @Override
    public Collection<? extends String> getDescriptions()
    {
        if (description == null)
        {
            return Collections.emptyList();
        }

        return description;
    }

    public void addDisplayName(String value)
    {
        if (displayName == null)
        {
            displayName = new ArrayList<>(1);
        }

        displayName.add(value);
    }

    @Override
    public Collection<? extends String> getDisplayNames()
    {
        if (displayName == null)
        {
            return Collections.emptyList();
        }

        return displayName;
    }

    public void addIcon(String value)
    {
        if (icon == null)
        {
            icon = new ArrayList<>(1);
        }

        icon.add(value);
    }

    @Override
    public Collection<? extends String> getIcons()
    {
        if (icon == null)
        {
            return Collections.emptyList();
        }
        return icon;
    }

    public void setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;
    }

    @Override
    public String getPropertyName()
    {
        return propertyName;
    }

    public void setPropertyClass(String propertyClass)
    {
        this.propertyClass = propertyClass;
    }

    @Override
    public String getPropertyClass()
    {
        return propertyClass;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setSuggestedValue(String suggestedValue)
    {
        this.suggestedValue = suggestedValue;
    }

    @Override
    public String getSuggestedValue()
    {
        return suggestedValue;
    }

    public void addPropertyExtension(String propertyExtension)
    {
        if (propertyExtensions == null)
        {
            propertyExtensions = new ArrayList<>(1);
        }
        propertyExtensions.add(propertyExtension);
    }

    @Override
    public Collection<? extends String> getPropertyExtensions()
    {
        if (propertyExtensions == null)
        {
            return Collections.emptyList();
        }
        return propertyExtensions;
    }

}
