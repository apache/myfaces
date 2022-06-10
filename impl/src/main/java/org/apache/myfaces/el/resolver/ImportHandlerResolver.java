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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.PropertyNotFoundException;

import org.apache.myfaces.util.lang.ClassUtils;

/**
 * See Faces 2.2 section 5.6.2.8
 */
public class ImportHandlerResolver extends ScopedAttributeResolver
{
    private static final Class EL_CLASS;
    private static final Constructor EL_CLASS_CONSTRUCTOR;
    private static final Method GET_IMPORT_HANDLER_METHOD;
    private static final Method IMPORT_HANDLER_RESOLVE_CLASS_METHOD;
    
    static
    {
        Class elClass = null;
        Class importHandlerClass = null;
        Constructor elClassConstructor = null;
        Method getImportHandlerMethod = null;
        Method importHandlerResolveClassMethod = null;
        try
        {
            // These classes will only be available with EL 3+
            elClass = ClassUtils.classForName("jakarta.el.ELClass");
            importHandlerClass = ClassUtils.classForName("jakarta.el.ImportHandler");
            getImportHandlerMethod = ELContext.class.getMethod("getImportHandler");
            if (elClass != null && importHandlerClass != null) 
            {
                importHandlerResolveClassMethod = 
                    importHandlerClass.getDeclaredMethod("resolveClass", new Class[] {String.class});
                elClassConstructor = elClass.getConstructor(Class.class);
            }
        }
        catch (SecurityException | ClassNotFoundException | NoSuchMethodException ex)
        {
            //No op
        }

        EL_CLASS = elClass;
        GET_IMPORT_HANDLER_METHOD = getImportHandlerMethod;
        IMPORT_HANDLER_RESOLVE_CLASS_METHOD = importHandlerResolveClassMethod;
        EL_CLASS_CONSTRUCTOR = elClassConstructor;
    }
    
    /**
     * Creates a new instance of ImportHandlerResolver
     */
    public ImportHandlerResolver()
    {
    }

    /*
     * Handle the EL case for the name of an import class
     */
    @Override
    public Object getValue(final ELContext context, final Object base, final Object property)
        throws NullPointerException, PropertyNotFoundException, ELException
    {
        if (EL_CLASS == null
                || EL_CLASS_CONSTRUCTOR == null 
                || GET_IMPORT_HANDLER_METHOD == null
                || IMPORT_HANDLER_RESOLVE_CLASS_METHOD == null)
        {
            return null;
        }

        try 
        {
            // In an EL 3+ environment, the ELContext will have a getImportHandler() method
            Object importHandler = GET_IMPORT_HANDLER_METHOD.invoke(context);
            if (importHandler != null) 
            {
                Class<?> clazz = (Class<?>) IMPORT_HANDLER_RESOLVE_CLASS_METHOD.invoke(
                        importHandler, property.toString());
                if (clazz != null) 
                {
                    context.setPropertyResolved(true);
                    return EL_CLASS_CONSTRUCTOR.newInstance(clazz);
                }
            }
        } 
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException
                | InstantiationException ex) 
        {
            //No op
        }            

        return null;
    }
}
