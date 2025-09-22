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
package org.apache.myfaces.el.resolver;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;

public class EmptyStringToNullELResolver extends ELResolver
{    
    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base)
    {
        return String.class;
    }

    @Override
    public <T extends Object> T convertToType(ELContext context, Object value, Class<T> targetType)
    {
        if (value == null && targetType == String.class)
        {
            context.setPropertyResolved(true);
        }

        return (T) value;
    }

    // @Override -- This method was removed in EL 6.0, but still supporting EL 5.0
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base)
    {
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property)
    {
        return null;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property)
    {
        return null;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property)
    {
        return true;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value)
    {
        // NOOP.
    }
}
