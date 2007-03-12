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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.commons.discovery.resource.names.DiscoverServiceNames;
import org.apache.commons.discovery.ResourceNameIterator;
import org.apache.myfaces.AnnotationProcessor;
import org.apache.myfaces.shared_impl.util.ClassUtils;
import org.apache.myfaces.DiscoverableAnnotationProcessor;

import javax.faces.context.ExternalContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;

/*
 * Date: Mar 12, 2007
 * Time: 9:53:40 PM
 */
public class DefaultAnnotationProcessorFactory extends AnnotationProcessorFactory
{
    private static Log log = LogFactory.getLog(AnnotationProcessorFactory.class);
    private static AnnotationProcessor ANNOTATION_PROCESSOR_INSTANCE;
    public static final String ANNOTATION_PROCESSOR_PROVIDER = AnnotationProcessor.class.getName();


    public DefaultAnnotationProcessorFactory()
    {
    }

    public AnnotationProcessor getAnnotatonProcessor(ExternalContext externalContext)
    {
        if (ANNOTATION_PROCESSOR_INSTANCE == null)
        {
            if (externalContext == null)
            {
                log.info("No ExternalContext using fallback annotation processor.");
                resolveFallbackAnnotationProcessor();
            }
            else
            {
                if (!resolveAnnotationProcessorFromExternalContext(externalContext))
                {
                    if (!resolveAnnotationProcessorFromService(externalContext))
                    {
                        resolveFallbackAnnotationProcessor();
                    }
                }
            }
            log.info("Using AnnotationProcessor "+ ANNOTATION_PROCESSOR_INSTANCE.getClass().getName());
        }
        return ANNOTATION_PROCESSOR_INSTANCE;
    }

    public void release() {
        ANNOTATION_PROCESSOR_INSTANCE = null;
    }



    private boolean resolveAnnotationProcessorFromExternalContext(ExternalContext externalContext)
    {
        try
        {
            String annotationProcessorClassName = externalContext.getInitParameter(ANNOTATION_PROCESSOR_PROVIDER);
            if (annotationProcessorClassName != null)
            {

                Object obj = createClass(annotationProcessorClassName, externalContext);

                if (obj instanceof AnnotationProcessor) {
                    ANNOTATION_PROCESSOR_INSTANCE = (AnnotationProcessor) obj;
                    return true;
                }
            }
        }
        catch (ClassNotFoundException e)
        {
            log.error("", e);
        }
        catch (InstantiationException e)
        {
            log.error("", e);
        }
        catch (IllegalAccessException e)
        {
            log.error("", e);
        }
        catch (InvocationTargetException e)
        {
            log.error("", e);
        }
        return false;
    }


    private boolean resolveAnnotationProcessorFromService(ExternalContext externalContext) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClassLoaders loaders = new ClassLoaders();
        loaders.put(classLoader);
        DiscoverServiceNames dsn = new DiscoverServiceNames(loaders);
        ResourceNameIterator iter = dsn.findResourceNames(ANNOTATION_PROCESSOR_PROVIDER);
        while (iter.hasNext()) {
            String className = iter.nextResourceName();
            try
            {
                Object obj = createClass(className, externalContext);
                if (DiscoverableAnnotationProcessor.class.isAssignableFrom(obj.getClass())) {
                    DiscoverableAnnotationProcessor discoverableAnnotationProcessor =
                            (DiscoverableAnnotationProcessor) obj;
                    if (discoverableAnnotationProcessor.isAvailable()) {
                        ANNOTATION_PROCESSOR_INSTANCE = discoverableAnnotationProcessor;
                        return true;
                    }
                }
            }
            catch (ClassNotFoundException e)
            {
                log.error("", e);
            }
            catch (NoClassDefFoundError e)
            {
                log.error("", e);
            }
            catch (InstantiationException e)
            {
                log.error("", e);
            }
            catch (IllegalAccessException e)
            {
                log.error("", e);
            }
            catch (InvocationTargetException e)
            {
                log.error("", e);
            }
        }
        return false;
    }

    private Object createClass(String className, ExternalContext externalContext)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException
    {
        Class clazz = ClassUtils.classForName(className);

        Object obj;
        try
        {
            Constructor constructor = clazz.getConstructor(ExternalContext.class);
            obj = constructor.newInstance(externalContext);
        }
        catch (NoSuchMethodException e)
        {
            obj = clazz.newInstance();
        }
        return obj;
    }


    private void resolveFallbackAnnotationProcessor()
    {
        try
        {
                ClassUtils.classForName("javax.annotation.PreDestroy");
        }
        catch (ClassNotFoundException e)
        {
            // no annotation available don't process annotations
            ANNOTATION_PROCESSOR_INSTANCE = new NopAnnotationProcessor();
            return;
        }
        Context context;
        try
        {
            context = new InitialContext();
            try
            {
                ClassUtils.classForName("javax.ejb.EJB");
                // Asume full JEE 5 container
                ANNOTATION_PROCESSOR_INSTANCE = new AllAnnotationProcessor(context);
            }
            catch (ClassNotFoundException e)
            {
                // something else
                ANNOTATION_PROCESSOR_INSTANCE = new ResourceAnnotationProcessor(context);
            }
        }
        catch (NamingException e)
        {
            // no initial context available no injection
            ANNOTATION_PROCESSOR_INSTANCE = new NoInjectionAnnotationProcessor();
            log.error("No InitialContext found. Using NoInjectionAnnotationProcessor.", e);

        }
    }
}
