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

import org.apache.AnnotationProcessor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class NoInjectionAnnotationProcessor implements AnnotationProcessor
{

    /**
     * Call postConstruct method on the specified instance.
     */
    public void postConstruct(Object instance)
            throws IllegalAccessException, InvocationTargetException
    {

        Method[] methods = instance.getClass().getDeclaredMethods();
        Method postConstruct = null;
        for (Method method : methods)
        {
            if (method.isAnnotationPresent(PostConstruct.class))
            {
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

        // At the end the postconstruct annotated
        // method is invoked
        if (postConstruct != null)
        {
            boolean accessibility = postConstruct.isAccessible();
            postConstruct.setAccessible(true);
            postConstruct.invoke(instance);
            postConstruct.setAccessible(accessibility);
        }

    }


    /**
     * Call preDestroy method on the specified instance.
     */
    public void preDestroy(Object instance)
            throws IllegalAccessException, InvocationTargetException
    {

        Method[] methods = instance.getClass().getDeclaredMethods();
        Method preDestroy = null;
        for (Method method : methods)
        {
            if (method.isAnnotationPresent(PreDestroy.class))
            {
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

        // At the end the postconstruct annotated
        // method is invoked
        if (preDestroy != null)
        {
            boolean accessibility = preDestroy.isAccessible();
            preDestroy.setAccessible(true);
            preDestroy.invoke(instance);
            preDestroy.setAccessible(accessibility);
        }

    }


    /**
     * Inject resources in specified instance.
     */
    public void processAnnotations(Object instance)
            throws IllegalAccessException, InvocationTargetException, NamingException
    {

    }

}
