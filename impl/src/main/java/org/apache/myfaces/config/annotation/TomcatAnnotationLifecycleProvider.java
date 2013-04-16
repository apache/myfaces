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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.ExternalContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.apache.myfaces.shared.util.ClassUtils;

public class TomcatAnnotationLifecycleProvider implements 
    DiscoverableLifecycleProvider, LifecycleProvider2
{
    //private static Log log = LogFactory.getLog(TomcatAnnotationLifecycleProvider.class);
    private static Logger log = Logger.getLogger(TomcatAnnotationLifecycleProvider.class.getName());

    private ExternalContext externalContext;
    private org.apache.AnnotationProcessor annotationProcessor;

    public TomcatAnnotationLifecycleProvider(ExternalContext externalContext)
    {
        this.externalContext = externalContext;
    }


    public Object newInstance(String className)
            throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NamingException, ClassNotFoundException
    {
        Class<?> clazz = ClassUtils.classForName(className);
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("Creating instance of " + className);
        }
        Object object = clazz.newInstance();
        annotationProcessor.processAnnotations(object);
        //annotationProcessor.postConstruct(object);
        return object;
    }


    public void destroyInstance(Object o) throws IllegalAccessException, InvocationTargetException
    {
        if (log.isLoggable(Level.FINEST))
        {
            log.info("Destroy instance of " + o.getClass().getName());
        }
        annotationProcessor.preDestroy(o);
    }

    public boolean isAvailable()
    {
        try
        {
            annotationProcessor =  (org.apache.AnnotationProcessor) ((ServletContext)
                     externalContext.getContext()).getAttribute(org.apache.AnnotationProcessor.class.getName());
            return annotationProcessor != null;
        }
        catch (Throwable e)
        {
            // ignore
        }
        return false;
    }


    public void postConstruct(Object o) throws IllegalAccessException,
            InvocationTargetException
    {
        annotationProcessor.postConstruct(o);
    }

}
