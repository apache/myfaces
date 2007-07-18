/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.config.annotation;

import javax.naming.NamingException;
import javax.naming.Context;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;

// TODO @Resources
public class ResourceAnnotationLifecycleProvider extends NoInjectionAnnotationLifecycleProvider
{

    protected Context context;
    private static final String JAVA_COMP_ENV = "java:comp/env/";

    public ResourceAnnotationLifecycleProvider(Context context)
    {
        this.context = context;
    }


    /**
     * Inject resources in specified instance.
     */
    protected void processAnnotations(Object instance)
            throws IllegalAccessException, InvocationTargetException, NamingException
    {

        if (context == null)
        {
            // No resource injection
            return;
        }

        checkAnnotation(instance.getClass(), instance);

        /* TODO the servlet spec is not clear about searching in superclass??
         * May be only check non private fields and methods
         * for @Resource (JSR 250), if used all superclasses MUST be examined
         * to discover all uses of this annotation.

        Class superclass = instance.getClass().getSuperclass();
        while (superclass != null && (!superclass.equals(Object.class)))
        {
            checkAnnotation(superclass, instance);
            superclass = superclass.getSuperclass();
        } */
    }

    private void checkAnnotation(Class clazz, Object instance)
            throws NamingException, IllegalAccessException, InvocationTargetException
    {
        // Initialize fields annotations
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields)
        {
            checkFieldAnnotation(field, instance);
        }

        // Initialize methods annotations
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods)
        {
            checkMethodAnnotation(method, instance);
        }
    }

    protected void checkMethodAnnotation(Method method, Object instance)
            throws NamingException, IllegalAccessException, InvocationTargetException
    {
        if (method.isAnnotationPresent(Resource.class))
        {
            Resource annotation = method.getAnnotation(Resource.class);
            lookupMethodResource(context, instance, method, annotation.name());
        }
    }

    protected void checkFieldAnnotation(Field field, Object instance)
            throws NamingException, IllegalAccessException
    {
        if (field.isAnnotationPresent(Resource.class))
        {
            Resource annotation = field.getAnnotation(Resource.class);
            lookupFieldResource(context, instance, field, annotation.name());
        }
    }

    /**
     * Inject resources in specified field.
     */
    protected static void lookupFieldResource(javax.naming.Context context,
            Object instance, Field field, String name)
            throws NamingException, IllegalAccessException
    {

        Object lookedupResource;

        if ((name != null) && (name.length() > 0))
        {
            // TODO local or global JNDI
            lookedupResource = context.lookup(JAVA_COMP_ENV + name);
        }
        else
        {
            // TODO local or global JNDI 
            lookedupResource = context.lookup(JAVA_COMP_ENV + instance.getClass().getName() + "/" + field.getName());
        }

        boolean accessibility = field.isAccessible();
        field.setAccessible(true);
        field.set(instance, lookedupResource);
        field.setAccessible(accessibility);
    }


    /**
     * Inject resources in specified method.
     */
    protected static void lookupMethodResource(javax.naming.Context context,
            Object instance, Method method, String name)
            throws NamingException, IllegalAccessException, InvocationTargetException
    {

        if (!method.getName().startsWith("set")
                || method.getParameterTypes().length != 1
                || !method.getReturnType().getName().equals("void"))
        {
            throw new IllegalArgumentException("Invalid method resource injection annotation");
        }

        Object lookedupResource;

        if ((name != null) && (name.length() > 0))
        {
            // TODO local or global JNDI
            lookedupResource = context.lookup(JAVA_COMP_ENV + name);
        }
        else
        {
            // TODO local or global JNDI
            lookedupResource =
                    context.lookup(JAVA_COMP_ENV + instance.getClass().getName() + "/" + method.getName().substring(3));
        }

        boolean accessibility = method.isAccessible();
        method.setAccessible(true);
        method.invoke(instance, lookedupResource);
        method.setAccessible(accessibility);
    }
}
