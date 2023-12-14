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
package org.apache.myfaces.view.facelets.tag.composite;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import jakarta.el.ValueExpression;
import jakarta.faces.view.facelets.MetadataTarget;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.util.lang.ClassUtils;


/**
 * Like MetadataTargetImpl but integrate composite component bean info
 * with it.
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
final class CompositeMetadataTargetImpl extends MetadataTarget
{
    private final Map<String, PropertyDescriptor> descriptors;
    private final MetadataTarget delegate;
    private final BeanInfo beanInfo;

    public CompositeMetadataTargetImpl(MetadataTarget delegate, BeanInfo beanInfo) throws IntrospectionException
    {
        this.delegate = delegate;
        this.beanInfo = beanInfo;
        this.descriptors = new HashMap<>();
        
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors())
        {
            this.descriptors.put(descriptor.getName(), descriptor);
        }
    }

    @Override
    public PropertyDescriptor getProperty(String name)
    {
        PropertyDescriptor pd = delegate.getProperty(name); 
        if (pd == null)
        {
            pd = descriptors.get(name);
        }
        return pd;
    }

    @Override
    public Class<?> getPropertyType(String name)
    {
        PropertyDescriptor pd = getProperty(name);
        if (pd != null)
        {
            Object type = pd.getValue("type");
            if (type != null)
            {
                type = ((ValueExpression) type).getValue(FacesContext.getCurrentInstance().getELContext());
                if (type instanceof String string)
                {
                    try
                    {
                        type = ClassUtils.javaDefaultTypeToClass(string);
                    }
                    catch (ClassNotFoundException e)
                    {
                        type = Object.class;
                    }
                }
                return (Class<?>) type;
            }
            return pd.getPropertyType();
        }
        
        return null;
    }

    @Override
    public Method getReadMethod(String name)
    {
        PropertyDescriptor pd = getProperty(name);
        if (pd != null)
        {
            return pd.getReadMethod();
        }
        
        return null;
    }

    @Override
    public Class<?> getTargetClass()
    {
        return delegate.getTargetClass();
    }

    @Override
    public Method getWriteMethod(String name)
    {
        PropertyDescriptor pd = getProperty(name);
        if (pd != null)
        {
            return pd.getWriteMethod();
        }
        
        return null;
    }

    @Override
    public boolean isTargetInstanceOf(Class type)
    {
        return delegate.isTargetInstanceOf(type);
    }
}
