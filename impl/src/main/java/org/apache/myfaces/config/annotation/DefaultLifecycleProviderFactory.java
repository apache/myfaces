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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.commons.discovery.resource.names.DiscoverServiceNames;
import org.apache.commons.discovery.ResourceNameIterator;
import org.apache.myfaces.shared_impl.util.ClassUtils;

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
public class DefaultLifecycleProviderFactory extends LifecycleProviderFactory {
    private static Log log = LogFactory.getLog(DefaultLifecycleProviderFactory.class);
    private static LifecycleProvider LIFECYCLE_PROVIDER_INSTANCE;
    public static final String LIFECYCLE_PROVIDER = LifecycleProvider.class.getName();


    public DefaultLifecycleProviderFactory()
    {
    }

    public LifecycleProvider getLifecycleProvider(ExternalContext externalContext)
    {
        if (LIFECYCLE_PROVIDER_INSTANCE == null)
        {
            if (externalContext == null)
            {
                log.info("No ExternalContext using fallback LifecycleProvider.");
                resolveFallbackLifecycleProvider();
            }
            else
            {
                if (!resolveLifecycleProviderFromExternalContext(externalContext))
                {
                    if (!resolveLifecycleProviderFromService(externalContext))
                    {
                        resolveFallbackLifecycleProvider();
                    }
                }
            }
            log.info("Using LifecycleProvider "+ LIFECYCLE_PROVIDER_INSTANCE.getClass().getName());
        }
        return LIFECYCLE_PROVIDER_INSTANCE;
    }

    public void release() {
        LIFECYCLE_PROVIDER_INSTANCE = null;
    }



    private boolean resolveLifecycleProviderFromExternalContext(ExternalContext externalContext)
    {
        try
        {
            String lifecycleProvider = externalContext.getInitParameter(LIFECYCLE_PROVIDER);
            if (lifecycleProvider != null)
            {

                Object obj = createClass(lifecycleProvider, externalContext);

                if (obj instanceof LifecycleProvider) {
                    LIFECYCLE_PROVIDER_INSTANCE = (LifecycleProvider) obj;
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


    private boolean resolveLifecycleProviderFromService(ExternalContext externalContext) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClassLoaders loaders = new ClassLoaders();
        loaders.put(classLoader);
        DiscoverServiceNames dsn = new DiscoverServiceNames(loaders);
        ResourceNameIterator iter = dsn.findResourceNames(LIFECYCLE_PROVIDER);
        while (iter.hasNext()) {
            String className = iter.nextResourceName();
            try
            {
                Object obj = createClass(className, externalContext);
                if (DiscoverableLifecycleProvider.class.isAssignableFrom(obj.getClass())) {
                    DiscoverableLifecycleProvider discoverableLifecycleProvider =
                            (DiscoverableLifecycleProvider) obj;
                    if (discoverableLifecycleProvider.isAvailable()) {
                        LIFECYCLE_PROVIDER_INSTANCE = discoverableLifecycleProvider;
                        return true;
                    }
                }
            }
            catch (ClassNotFoundException e)
            {
                // ignore
            }
            catch (NoClassDefFoundError e)
            {
                // ignore
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
        Class<?> clazz = ClassUtils.classForName(className);

        Object obj;
        try
        {
            Constructor<?> constructor = clazz.getConstructor(ExternalContext.class);
            obj = constructor.newInstance(externalContext);
        }
        catch (NoSuchMethodException e)
        {
            obj = clazz.newInstance();
        }
        return obj;
    }


    private void resolveFallbackLifecycleProvider()
    {
        try
        {
                ClassUtils.classForName("javax.annotation.PreDestroy");
        }
        catch (ClassNotFoundException e)
        {
            // no annotation available don't process annotations
            LIFECYCLE_PROVIDER_INSTANCE = new NoAnnotationLifecyleProvider();
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
                LIFECYCLE_PROVIDER_INSTANCE = new AllAnnotationLifecycleProvider(context);
            }
            catch (ClassNotFoundException e)
            {
                // something else
                LIFECYCLE_PROVIDER_INSTANCE = new ResourceAnnotationLifecycleProvider(context);
            }
        }
        catch (NamingException e)
        {
            // no initial context available no injection
            LIFECYCLE_PROVIDER_INSTANCE = new NoInjectionAnnotationLifecycleProvider();
            log.error("No InitialContext found. Using NoInjectionAnnotationProcessor.", e);

        }
    }
}
