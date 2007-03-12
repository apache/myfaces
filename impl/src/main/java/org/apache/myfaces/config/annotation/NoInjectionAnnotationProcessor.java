package org.apache.myfaces.config.annotation;

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

import org.apache.myfaces.AnnotationProcessor;
import org.apache.myfaces.shared_impl.util.ClassUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * See SRV.14.5 Servlet Specification Version 2.5 JSR 154
 * and Common Annotations for the Java Platform JSR 250

 */

public class NoInjectionAnnotationProcessor implements AnnotationProcessor {


    public Object newInstance(String className)
           throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException
    {
        Class clazz = ClassUtils.classForName(className);
        return clazz.newInstance();
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

    /**
     * Call preDestroy method on the specified instance.
     */
    public void preDestroy(Object instance)
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


    /**
     * Inject resources in specified instance.
     */
    public void processAnnotations(Object instance)
            throws IllegalAccessException, InvocationTargetException, NamingException
    {

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

}
