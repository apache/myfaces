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

package org.apache.myfaces.test.mock;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.PropertyNotFoundException;
import jakarta.faces.el.PropertyResolver;

/**
 * <p>Mock implementation of <code>PropertyResolver</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */

public class MockPropertyResolver extends PropertyResolver
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a default instance.</p>
     */
    public MockPropertyResolver()
    {
    }

    // ----------------------------------------------------- Mock Object Methods

    // ------------------------------------------------------ Instance Variables

    // ------------------------------------------------ PropertyResolver Methods

    /** {@inheritDoc} */
    public Object getValue(Object base, Object property)
            throws EvaluationException, PropertyNotFoundException
    {

        if (base == null)
        {
            throw new NullPointerException();
        }
        if (base instanceof Map)
        {
            return ((Map) base).get(property);
        }
        String name = property.toString();
        PropertyDescriptor descriptor = descriptor(base.getClass(), name);
        try
        {
            return descriptor.getReadMethod().invoke(base, (Object[]) null);
        }
        catch (IllegalAccessException e)
        {
            throw new EvaluationException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new EvaluationException(e.getTargetException());
        }

    }

    /** {@inheritDoc} */
    public Object getValue(Object base, int index)
            throws PropertyNotFoundException
    {

        if (base instanceof List)
        {
            List l = (List) base;
            return index < l.size() ? l.get(index) : null;
        }
        if (base != null && base.getClass().isArray())
        {
            return index < Array.getLength(base) ? Array.get(base, index) : null;
        }
        return getValue(base, "" + index);

    }

    /** {@inheritDoc} */
    public void setValue(Object base, Object property, Object value)
            throws PropertyNotFoundException
    {

        if (base == null)
        {
            throw new NullPointerException();
        }
        if (base instanceof Map)
        {
            ((Map) base).put(property, value);
            return;
        }
        String name = property.toString();
        PropertyDescriptor descriptor = descriptor(base.getClass(), name);
        try
        {
            descriptor.getWriteMethod().invoke(base, new Object[] { value });
        }
        catch (IllegalAccessException e)
        {
            throw new EvaluationException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new EvaluationException(e.getTargetException());
        }

    }

    /** {@inheritDoc} */
    public void setValue(Object base, int index, Object value)
            throws PropertyNotFoundException
    {

        if (base instanceof List)
        {
            List l = (List) base;
            if (index > l.size())
            {
                throw new PropertyNotFoundException();
            }
            l.set(index, value);
            return;
        }
        if (base != null && base.getClass().isArray())
        {
            if (index < Array.getLength(base))
            {
                Array.set(base, index, value);
                return;
            }
            throw new PropertyNotFoundException();
        }
        setValue(base, "" + index, value);

    }

    /** {@inheritDoc} */
    public boolean isReadOnly(Object base, Object property)
            throws PropertyNotFoundException
    {

        if (base == null)
        {
            throw new NullPointerException();
        }
        if (base instanceof Map)
        {
            return false; // We have no way to know anything more specific
        }
        String name = property.toString();
        PropertyDescriptor descriptor = descriptor(base.getClass(), name);
        return (descriptor.getWriteMethod() == null);

    }

    /** {@inheritDoc} */
    public boolean isReadOnly(Object base, int index)
            throws PropertyNotFoundException
    {

        return isReadOnly(base, "" + index);

    }

    /** {@inheritDoc} */
    public Class getType(Object base, Object property)
            throws PropertyNotFoundException
    {

        if (base == null)
        {
            throw new NullPointerException();
        }
        if (base instanceof Map)
        {
            Object value = ((Map) base).get(property);
            if (value != null)
            {
                return value.getClass();
            }
            else
            {
                return Object.class;
            }
        }
        String name = property.toString();
        PropertyDescriptor descriptor = descriptor(base.getClass(), name);
        return descriptor.getPropertyType();

    }

    /** {@inheritDoc} */
    public Class getType(Object base, int index)
            throws PropertyNotFoundException
    {

        if (base instanceof List)
        {
            if (index < ((List) base).size())
            {
                Object element = getValue(base, index);
                return element == null ? null : element.getClass();
            }
            throw new PropertyNotFoundException();
        }
        if (base != null && base.getClass().isArray())
        {
            if (index < Array.getLength(base))
            {
                Object element = Array.get(base, index);
                return element != null ? element.getClass()
                        : base.getClass().getComponentType();
            }
            throw new PropertyNotFoundException();
        }
        return getType(base, "" + index);

    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>Return the <code>PropertyDescriptor</code> for the specified
     * property of the specified class.</p>
     *
     * @param clazz Class from which to extract a property descriptor
     * @param name Name of the desired property
     *
     * @exception EvaluationException if we cannot access the introspecition
     *  information for this class
     * @exception PropertyNotFoundException if the specified property does
     *  not exist on the specified class
     */
    private PropertyDescriptor descriptor(Class clazz, String name)
    {

        System.err.println("descriptor(class=" + clazz.getName() + ", name="
                + name);
        BeanInfo info = null;
        try
        {
            info = Introspector.getBeanInfo(clazz);
            System.err.println("  Found BeanInfo " + info);
        }
        catch (IntrospectionException e)
        {
            throw new EvaluationException(e);
        }
        PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
        for (int i = 0; i < descriptors.length; i++)
        {
            if (name.equals(descriptors[i].getName()))
            {
                System.err
                        .print("  Found PropertyDescriptor " + descriptors[i]);
                return descriptors[i];
            }
        }
        System.err.print("  No property descriptor for property " + name);
        throw new PropertyNotFoundException(name);

    }

}
