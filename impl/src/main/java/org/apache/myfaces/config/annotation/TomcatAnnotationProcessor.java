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


import org.apache.myfaces.DiscoverableAnnotationProcessor;
import org.apache.myfaces.shared_impl.util.ClassUtils;

import javax.naming.NamingException;
import javax.faces.context.ExternalContext;
import javax.servlet.ServletContext;
import java.lang.reflect.InvocationTargetException;

public class TomcatAnnotationProcessor implements DiscoverableAnnotationProcessor
{
    private ExternalContext externalContext;
    private org.apache.AnnotationProcessor annotationProcessor;
    public TomcatAnnotationProcessor(ExternalContext externalContext)
    {
        this.externalContext = externalContext;
    }


    public Object newInstance(String className)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        Class clazz = ClassUtils.classForName(className);
        return clazz.newInstance();
    }

    public boolean isAvailable()
    {
        try
        {
            annotationProcessor =  (org.apache.AnnotationProcessor) ((ServletContext)
                     externalContext.getContext()).getAttribute(org.apache.AnnotationProcessor.class.getName());
            return annotationProcessor != null;
        } catch (Exception e) {
            // ignore
        }
        return false;
    }

    public void postConstruct(Object instance) throws IllegalAccessException, InvocationTargetException
    {
        annotationProcessor.postConstruct(instance);
    }

    public void preDestroy(Object instance) throws IllegalAccessException, InvocationTargetException
    {
        annotationProcessor.preDestroy(instance);
    }

    public void processAnnotations(Object instance) throws IllegalAccessException, InvocationTargetException, NamingException
    {
        annotationProcessor.processAnnotations(instance);
    }

}
