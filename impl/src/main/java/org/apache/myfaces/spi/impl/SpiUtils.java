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
package org.apache.myfaces.spi.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;

import org.apache.commons.discovery.ResourceNameIterator;
import org.apache.myfaces.shared_impl.util.ClassUtils;

/**
 * 
 * @since 2.0.3
 * @author Leonardo Uribe
 */
public final class SpiUtils
{

    public static <T> T buildApplicationObject(Class<T> interfaceClass, ResourceNameIterator classNamesIterator, T defaultObject)
    {
        return buildApplicationObject(interfaceClass, null, null, classNamesIterator, defaultObject);
    }

    /**
     * Creates ApplicationObjects like NavigationHandler or StateManager and creates 
     * the right wrapping chain of the ApplicationObjects known as the decorator pattern. 
     * @param <T>
     * @param interfaceClass The class from which the implementation has to inherit from.
     * @param extendedInterfaceClass A subclass of interfaceClass which specifies a more
     *                               detailed implementation.
     * @param extendedInterfaceWrapperClass A wrapper class for the case that you have an ApplicationObject
     *                                      which only implements the interfaceClass but not the 
     *                                      extendedInterfaceClass.
     * @param classNamesIterator All the class names of the actual ApplicationObject implementations
     *                           from the faces-config.xml.
     * @param defaultObject The default implementation for the given ApplicationObject.
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T buildApplicationObject(Class<T> interfaceClass, Class<? extends T> extendedInterfaceClass,
            Class<? extends T> extendedInterfaceWrapperClass,
            ResourceNameIterator classNamesIterator, T defaultObject)
    {
        T current = defaultObject;

        while (classNamesIterator.hasNext())
        {
            String implClassName = classNamesIterator.nextResourceName();
            Class<? extends T> implClass = ClassUtils.simpleClassForName(implClassName);

            // check, if class is of expected interface type
            if (!interfaceClass.isAssignableFrom(implClass))
            {
                throw new IllegalArgumentException("Class " + implClassName + " is no " + interfaceClass.getName());
            }

            if (current == null)
            {
                // nothing to decorate
                current = (T) ClassUtils.newInstance(implClass);
            }
            else
            {
                // let's check if class supports the decorator pattern
                T newCurrent = null;
                try
                {
                    Constructor<? extends T> delegationConstructor = null;
                    
                    // first, if there is a extendedInterfaceClass,
                    // try to find a constructor that uses that
                    if (extendedInterfaceClass != null 
                            && extendedInterfaceClass.isAssignableFrom(current.getClass()))
                    {
                        try
                        {
                            delegationConstructor = 
                                    implClass.getConstructor(new Class[] {extendedInterfaceClass});
                        }
                        catch (NoSuchMethodException mnfe)
                        {
                            // just eat it
                        }
                    }
                    if (delegationConstructor == null)
                    {
                        // try to find the constructor with the "normal" interfaceClass
                        delegationConstructor = 
                                implClass.getConstructor(new Class[] {interfaceClass});
                    }
                    // impl class supports decorator pattern at this point
                    try
                    {
                        // create new decorator wrapping current
                        newCurrent = delegationConstructor.newInstance(new Object[] { current });
                    }
                    catch (InstantiationException e)
                    {
                        getLogger().log(Level.SEVERE, e.getMessage(), e);
                        throw new FacesException(e);
                    }
                    catch (IllegalAccessException e)
                    {
                        getLogger().log(Level.SEVERE, e.getMessage(), e);
                        throw new FacesException(e);
                    }
                    catch (InvocationTargetException e)
                    {
                        getLogger().log(Level.SEVERE, e.getMessage(), e);
                        throw new FacesException(e);
                    }
                }
                catch (NoSuchMethodException e)
                {
                    // no decorator pattern support
                    newCurrent = (T) ClassUtils.newInstance(implClass);
                }
                
                // now we have a new current object (newCurrent)
                // --> find out if it is assignable from extendedInterfaceClass
                // and if not, wrap it in a backwards compatible wrapper (if available)
                if (extendedInterfaceWrapperClass != null
                        && !extendedInterfaceClass.isAssignableFrom(newCurrent.getClass()))
                {
                    try
                    {
                        Constructor<? extends T> wrapperConstructor
                                = extendedInterfaceWrapperClass.getConstructor(
                                        new Class[] {interfaceClass, extendedInterfaceClass});
                        newCurrent = wrapperConstructor.newInstance(new Object[] {newCurrent, current});
                    }
                    catch (NoSuchMethodException e)
                    {
                        getLogger().log(Level.SEVERE, e.getMessage(), e);
                        throw new FacesException(e);
                    }
                    catch (InstantiationException e)
                    {
                        getLogger().log(Level.SEVERE, e.getMessage(), e);
                        throw new FacesException(e);
                    }
                    catch (IllegalAccessException e)
                    {
                        getLogger().log(Level.SEVERE, e.getMessage(), e);
                        throw new FacesException(e);
                    }
                    catch (InvocationTargetException e)
                    {
                        getLogger().log(Level.SEVERE, e.getMessage(), e);
                        throw new FacesException(e);
                    }
                }
                
                current = newCurrent;
            }
        }

        return current;
    }
    
    private static Logger getLogger()
    {
        return Logger.getLogger(SpiUtils.class.getName());
    }
}
