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
package com.sun.facelets.tag;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Jacob Hookom
 * @version $Id: MetadataTargetImpl.java,v 1.3 2008/07/13 19:01:35 rlubke Exp $
 */
final class MetadataTargetImpl extends MetadataTarget
{

    private final Map pd;
    private final Class type;

    public MetadataTargetImpl(Class type) throws IntrospectionException
    {
        this.type = type;
        this.pd = new HashMap();
        BeanInfo info = Introspector.getBeanInfo(type);
        PropertyDescriptor[] pda = info.getPropertyDescriptors();
        for (int i = 0; i < pda.length; i++)
        {
            this.pd.put(pda[i].getName(), pda[i]);
        }
    }

    public PropertyDescriptor getProperty(String name)
    {
        return (PropertyDescriptor) this.pd.get(name);
    }

    public boolean isTargetInstanceOf(Class type)
    {
        return type.isAssignableFrom(this.type);
    }

    public Class getTargetClass()
    {
        return this.type;
    }

    public Class getPropertyType(String name)
    {
        PropertyDescriptor pd = this.getProperty(name);
        if (pd != null)
        {
            return pd.getPropertyType();
        }
        return null;
    }

    public Method getWriteMethod(String name)
    {
        PropertyDescriptor pd = this.getProperty(name);
        if (pd != null)
        {
            return pd.getWriteMethod();
        }
        return null;
    }

    public Method getReadMethod(String name)
    {
        PropertyDescriptor pd = this.getProperty(name);
        if (pd != null)
        {
            return pd.getReadMethod();
        }
        return null;
    }

}
