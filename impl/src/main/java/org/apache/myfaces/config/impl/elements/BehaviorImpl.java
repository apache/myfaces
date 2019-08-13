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
package org.apache.myfaces.config.impl.elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of model for &lt;behavior&gt; element.
 */
public class BehaviorImpl extends org.apache.myfaces.config.element.Behavior implements Serializable
{
    private String behaviorClass;
    private String behaviorId;
    private List<AttributeImpl> attributes;
    private List<PropertyImpl> properties;

    @Override
    public String getBehaviorClass()
    {
        return behaviorClass;
    }

    @Override
    public String getBehaviorId()
    {
        return behaviorId;
    }
    
    public void setBehaviorClass(String behaviorClass)
    {
        this.behaviorClass = behaviorClass;
    }
    
    public void setBehaviorId(String behaviorId)
    {
        this.behaviorId = behaviorId;
    }
    
    public Collection<AttributeImpl> getAttributes()
    {
        if (attributes == null)
        {
            return Collections.emptyList();
        }
        return attributes;
    }

    public void addAttribute(AttributeImpl attribute)
    {
        if (attributes == null)
        {
            attributes = new ArrayList<>();
        }
        attributes.add(attribute);
    }

    public Collection<PropertyImpl> getProperties()
    {
        if (properties == null)
        {
            return Collections.emptyList();
        }
        return properties;
    }
    
    public void addProperty(PropertyImpl property)
    {
        if (properties == null)
        {
            properties = new ArrayList<>();
        }
        properties.add(property);
    }
}
