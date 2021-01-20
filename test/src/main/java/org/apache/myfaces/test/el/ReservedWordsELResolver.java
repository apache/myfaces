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
package org.apache.myfaces.test.el;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.el.ELContext;
import jakarta.el.PropertyNotWritableException;

/**
 * {@code ELResolver} for reserved words.
 */
public class ReservedWordsELResolver extends AbstractELResolver
{

    private static final Map<String, Object> VALUES;

    static
    {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("true", Boolean.TRUE);
        values.put("false", Boolean.FALSE);
        values.put("null", null);
        VALUES = Collections.unmodifiableMap(values);
    }

    private List<FeatureDescriptor> featureDescriptors;

    @Override
    public Object getValue(ELContext context, Object base, Object property)
    {
        if (base == null && VALUES.containsKey(property))
        {
            context.setPropertyResolved(true);
            return VALUES.get(property);
        }
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property)
    {
        if (base == null && VALUES.containsKey(property))
        {
            context.setPropertyResolved(true);
            Object value = VALUES.get(property);
            return value == null ? null : value.getClass();
        }
        return null;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property,
        Object value)
    {
        if (base == null && VALUES.containsKey(property))
        {
            context.setPropertyResolved(true);
            throw new PropertyNotWritableException(property.toString());
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property)
    {
        if (base == null && VALUES.containsKey(property))
        {
            context.setPropertyResolved(true);
            return true;
        }
        return false;
    }

    @Override
    public synchronized Iterator<FeatureDescriptor> getFeatureDescriptors(
        ELContext context, Object base)
    {
        if (featureDescriptors == null)
        {
            featureDescriptors = new ArrayList<FeatureDescriptor>();
            for (Map.Entry<String, Object> e : VALUES.entrySet())
            {
                final Class<?> type
                    = e.getValue() == null ? null : e.getValue().getClass();
                featureDescriptors.add(descriptor(e.getKey(), e.getKey(),
                    e.getKey(), false, false, true, type, true));
            }
            featureDescriptors
                = Collections.unmodifiableList(featureDescriptors);
        }
        return featureDescriptors.iterator();
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base)
    {
        return base == null ? String.class : null;
    }

}
