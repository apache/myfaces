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
package org.apache.myfaces.config.annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.naming.NamingException;

import org.apache.myfaces.shared.util.ClassUtils;

/**
 * See SRV.14.5 Servlet Specification Version 2.5 JSR 154
 * and Common Annotations for the Java Platform JSR 250

 */

public class NoInjectionAnnotationLifecycleProvider implements LifecycleProvider2
{


    public Object newInstance(String className)
           throws InstantiationException, IllegalAccessException, NamingException, InvocationTargetException, ClassNotFoundException
    {
        Class clazz = ClassUtils.classForName(className);
        Object object = clazz.newInstance();
        processAnnotations(object);
        //postConstruct(object);
        return object;
    }

    /**
     * Call postConstruct method on the specified instance.
     */
    public void postConstruct(Object instance)
            throws IllegalAccessException, InvocationTargetException
    {

        // TODO the servlet spec is not clear about searching in superclass??

        Method[] methods = instance.getClass().getDeclaredMethods();
        Method postConstruct = null;
        for (Method method : methods)
        {
            if (method.isAnnotationPresent(PostConstruct.class))
            {
                // a method that does not take any arguments
                // the method must not be static
                // must not throw any checked expections
                // the return value must be void
                // the method may be public, protected, package private or private

                if ((postConstruct != null)
                        || (method.getParameterTypes().length != 0)
                        || (Modifier.isStatic(method.getModifiers()))
                        || (method.getExceptionTypes().length > 0)
                        || (!method.getReturnType().getName().equals("void")))
                {
                    throw new IllegalArgumentException("Invalid PostConstruct annotation");
                }
                postConstruct = method;
            }
        }

        invokeAnnotatedMethod(postConstruct, instance);

    }

    public void destroyInstance(Object instance)
            throws IllegalAccessException, InvocationTargetException
    {

        // TODO the servlet spec is not clear about searching in superclass??
        // May be only check non private fields and methods
        Method[] methods = instance.getClass().getDeclaredMethods();
        Method preDestroy = null;
        for (Method method : methods)
        {
            if (method.isAnnotationPresent(PreDestroy.class))
            {
                // must not throw any checked expections
                // the method must not be static
                // must not throw any checked expections
                // the return value must be void
                // the method may be public, protected, package private or private

                if ((preDestroy != null)
                        || (method.getParameterTypes().length != 0)
                        || (Modifier.isStatic(method.getModifiers()))
                        || (method.getExceptionTypes().length > 0)
                        || (!method.getReturnType().getName().equals("void")))
                {
                    throw new IllegalArgumentException("Invalid PreDestroy annotation");
                }
                preDestroy = method;
            }
        }

        invokeAnnotatedMethod(preDestroy, instance);

    }

    private void invokeAnnotatedMethod(Method method, Object instance)
                throws IllegalAccessException, InvocationTargetException
    {
        // At the end the annotated
        // method is invoked
        if (method != null)
        {
            boolean accessibility = method.isAccessible();
            method.setAccessible(true);
            method.invoke(instance);
            method.setAccessible(accessibility);
        }
    }

     /**
     * Inject resources in specified instance.
     */
    protected void processAnnotations(Object instance)
            throws IllegalAccessException, InvocationTargetException, NamingException
    {

    }

}
