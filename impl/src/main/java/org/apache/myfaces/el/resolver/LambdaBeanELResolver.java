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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.el.BeanELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.PropertyNotWritableException;
import javax.faces.context.FacesContext;
import org.apache.myfaces.core.api.shared.lang.LambdaPropertyDescriptor;
import org.apache.myfaces.core.api.shared.lang.PropertyDescriptorUtils;
import org.apache.myfaces.core.api.shared.lang.PropertyDescriptorWrapper;

public class LambdaBeanELResolver extends BeanELResolver
{
    private final ConcurrentHashMap<String, Map<String, ? extends PropertyDescriptorWrapper>> cache;

    public LambdaBeanELResolver()
    {
        this.cache = new ConcurrentHashMap<>(1000);
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property)
    {
        Objects.requireNonNull(context);
        if (base == null || property == null)
        {
            return null;
        }

        context.setPropertyResolved(base, property);

        return getPropertyDescriptor(base, property).getPropertyType();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getValue(ELContext context, Object base, Object property)
    {        
        Objects.requireNonNull(context);
        if (base == null || property == null)
        {
            return null;
        }

        context.setPropertyResolved(base, property);

        try
        {
            return getPropertyDescriptor(base, property).getReadFunction().apply(base);
        }
        catch (Exception e)
        {
            throw new ELException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValue(ELContext context, Object base, Object property, Object value)
    {
        Objects.requireNonNull(context);
        if (base == null || property == null)
        {
            return;
        }

        context.setPropertyResolved(base, property);

        LambdaPropertyDescriptor propertyDescriptor = getPropertyDescriptor(base, property);
        if (propertyDescriptor.getWriteFunction() == null)
        {
            throw new PropertyNotWritableException("Property \"" + (String) property
                    + "\" in \"" + base.getClass().getName() + "\" is not writable!");
        }

        try
        {
            propertyDescriptor.getWriteFunction().accept(base, value);
        }
        catch (Exception e)
        {
            throw new ELException(e);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property)
    {
        Objects.requireNonNull(context);
        if (base == null || property == null)
        {
            return false;
        }

        context.setPropertyResolved(base, property);

        return getPropertyDescriptor(base, property).getWriteFunction() == null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base)
    {
        if (base != null)
        {
            return Object.class;
        }

        return null;
    }

    protected LambdaPropertyDescriptor getPropertyDescriptor(Object base, Object property)
    {
        Map<String, ? extends PropertyDescriptorWrapper> beanCache = cache.get(base.getClass().getName());
        if (beanCache == null)
        {
            beanCache = PropertyDescriptorUtils.getCachedPropertyDescriptors(
                    FacesContext.getCurrentInstance().getExternalContext(),
                    base.getClass());
            cache.put(base.getClass().getName(), beanCache);
        }

        return (LambdaPropertyDescriptor) beanCache.get((String) property);
    }
}
