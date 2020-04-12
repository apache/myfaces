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
package org.apache.myfaces.view.facelets.tag;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.faces.view.facelets.MetadataTarget;
import org.apache.myfaces.util.lang.MethodHandleUtils;

public class MethodHandleMetadataTargetImpl extends MetadataTarget
{
    private final Map<String, MethodHandleUtils.LambdaPropertyDescriptor> propertyDescriptors;
    private final Class<?> type;

    public MethodHandleMetadataTargetImpl(Class<?> type) throws IntrospectionException
    {
        this.type = type;
        this.propertyDescriptors = MethodHandleUtils.getLambdaPropertyDescriptors(type);
    }

    @Override
    public PropertyDescriptor getProperty(String name)
    {
        MethodHandleUtils.LambdaPropertyDescriptor lpd = getLambdaProperty(name);
        if (lpd == null)
        {
            return null;
        }

        return lpd.getWrapped();
    }

    @Override
    public Class<?> getPropertyType(String name)
    {
        MethodHandleUtils.LambdaPropertyDescriptor lpd = getLambdaProperty(name);
        if (lpd == null)
        {
            return null;
        }

        return lpd.getPropertyType();
    }

    @Override
    public Method getReadMethod(String name)
    {
        throw new UnsupportedOperationException("Please use the getReadFunction for better performance!");
    }

    @Override
    public Class<?> getTargetClass()
    {
        return type;
    }

    @Override
    public Method getWriteMethod(String name)
    {
        throw new UnsupportedOperationException("Please use the getWriteFunction for better performance!");
    }

    @Override
    public boolean isTargetInstanceOf(Class type)
    {
        return type.isAssignableFrom(type);
    }
 
    public MethodHandleUtils.LambdaPropertyDescriptor getLambdaProperty(String name)
    {
        return propertyDescriptors.get(name);
    }

    public Function<Object, Object> getReadFunction(String name)
    {
        MethodHandleUtils.LambdaPropertyDescriptor lpd = getLambdaProperty(name);
        if (lpd == null)
        {
            return null;
        }
        
        return lpd.getReadFunction();
    }

    public BiConsumer<Object, Object> getWriteFunction(String name)
    {
        MethodHandleUtils.LambdaPropertyDescriptor lpd = getLambdaProperty(name);
        if (lpd == null)
        {
            return null;
        }
        
        return lpd.getWriteFunction();
    }
}
