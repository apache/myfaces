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
package jakarta.faces;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.context.ExternalContext;
import org.apache.myfaces.core.api.shared.lang.ClassUtils;

/**
 * Provide utility methods used by FactoryFinder class to lookup for SPI interface FactoryFinderProvider.
 *
 * @since 2.0.5
 */
class _FactoryFinderProviderFactory
{

    public static final String FACTORY_FINDER_PROVIDER_FACTORY_CLASS_NAME = "org.apache.myfaces.spi" +
            ".FactoryFinderProviderFactory";

    public static final String FACTORY_FINDER_PROVIDER_CLASS_NAME = "org.apache.myfaces.spi.FactoryFinderProvider";

    public static final String INJECTION_PROVIDER_FACTORY_CLASS_NAME = 
        "org.apache.myfaces.spi.InjectionProviderFactory";

    public static final String INJECTION_PROVIDER_CLASS_NAME = "org.apache.myfaces.spi.InjectionProvider";
    
    public static final Class<?> FACTORY_FINDER_PROVIDER_FACTORY_CLASS;

    public static final Method FACTORY_FINDER_PROVIDER_GET_INSTANCE_METHOD;

    public static final Method FACTORY_FINDER_PROVIDER_FACTORY_GET_FACTORY_FINDER_METHOD;
    public static final Class<?> FACTORY_FINDER_PROVIDER_CLASS;
    public static final Method FACTORY_FINDER_PROVIDER_GET_FACTORY_METHOD;
    public static final Method FACTORY_FINDER_PROVIDER_SET_FACTORY_METHOD;
    public static final Method FACTORY_FINDER_PROVIDER_RELEASE_FACTORIES_METHOD;
    
    public static final Class<?> INJECTION_PROVIDER_FACTORY_CLASS;
    public static final Method INJECTION_PROVIDER_FACTORY_GET_INSTANCE_METHOD;
    public static final Method INJECTION_PROVIDER_FACTORY_GET_INJECTION_PROVIDER_METHOD;
    public static final Class<?> INJECTION_PROVIDER_CLASS;
    public static final Method INJECTION_PROVIDER_INJECT_METHOD;
    public static final Method INJECTION_PROVIDER_POST_CONSTRUCT_METHOD;
    public static final Method INJECTION_PROVIDER_PRE_DESTROY_METHOD;

    static
    {
        Class factoryFinderFactoryClass = null;
        Method factoryFinderproviderFactoryGetMethod = null;
        Method factoryFinderproviderFactoryGetFactoryFinderMethod = null;
        Class<?> factoryFinderProviderClass = null;

        Method factoryFinderProviderGetFactoryMethod = null;
        Method factoryFinderProviderSetFactoryMethod = null;
        Method factoryFinderProviderReleaseFactoriesMethod = null;
        
        Class injectionProviderFactoryClass = null;
        Method injectionProviderFactoryGetInstanceMethod = null;
        Method injectionProviderFactoryGetInjectionProviderMethod = null;
        Class injectionProviderClass = null;
        Method injectionProviderInjectMethod = null;
        Method injectionProviderPostConstructMethod = null;
        Method injectionProviderPreDestroyMethod = null;

        try
        {
            factoryFinderFactoryClass = ClassUtils.classForName(FACTORY_FINDER_PROVIDER_FACTORY_CLASS_NAME);
            if (factoryFinderFactoryClass != null)
            {
                factoryFinderproviderFactoryGetMethod = factoryFinderFactoryClass.getMethod
                        ("getInstance", null);
                factoryFinderproviderFactoryGetFactoryFinderMethod = factoryFinderFactoryClass
                        .getMethod("getFactoryFinderProvider", null);
            }

            factoryFinderProviderClass = ClassUtils.classForName(FACTORY_FINDER_PROVIDER_CLASS_NAME);
            if (factoryFinderProviderClass != null)
            {
                factoryFinderProviderGetFactoryMethod = factoryFinderProviderClass.getMethod("getFactory",
                        new Class[]{String.class});
                factoryFinderProviderSetFactoryMethod = factoryFinderProviderClass.getMethod("setFactory",
                        new Class[]{String.class, String.class});
                factoryFinderProviderReleaseFactoriesMethod = factoryFinderProviderClass.getMethod
                        ("releaseFactories", null);
            }
            
            injectionProviderFactoryClass = ClassUtils.classForName(INJECTION_PROVIDER_FACTORY_CLASS_NAME);
            
            if (injectionProviderFactoryClass != null)
            {
                injectionProviderFactoryGetInstanceMethod = injectionProviderFactoryClass.
                    getMethod("getInjectionProviderFactory", null);
                injectionProviderFactoryGetInjectionProviderMethod = injectionProviderFactoryClass.
                    getMethod("getInjectionProvider", ExternalContext.class);
            }
            
            injectionProviderClass = ClassUtils.classForName(INJECTION_PROVIDER_CLASS_NAME);
            
            if (injectionProviderClass != null)
            {
                injectionProviderInjectMethod = injectionProviderClass.
                    getMethod("inject", Object.class);
                injectionProviderPostConstructMethod = injectionProviderClass.
                    getMethod("postConstruct", Object.class, Object.class);
                injectionProviderPreDestroyMethod = injectionProviderClass.
                    getMethod("preDestroy", Object.class, Object.class);
            }
        }
        catch (Exception e)
        {
            // no op
        }

        FACTORY_FINDER_PROVIDER_FACTORY_CLASS = factoryFinderFactoryClass;
        FACTORY_FINDER_PROVIDER_GET_INSTANCE_METHOD = factoryFinderproviderFactoryGetMethod;
        FACTORY_FINDER_PROVIDER_FACTORY_GET_FACTORY_FINDER_METHOD = factoryFinderproviderFactoryGetFactoryFinderMethod;
        FACTORY_FINDER_PROVIDER_CLASS = factoryFinderProviderClass;

        FACTORY_FINDER_PROVIDER_GET_FACTORY_METHOD = factoryFinderProviderGetFactoryMethod;
        FACTORY_FINDER_PROVIDER_SET_FACTORY_METHOD = factoryFinderProviderSetFactoryMethod;
        FACTORY_FINDER_PROVIDER_RELEASE_FACTORIES_METHOD = factoryFinderProviderReleaseFactoriesMethod;
        
        INJECTION_PROVIDER_FACTORY_CLASS = injectionProviderFactoryClass;
        INJECTION_PROVIDER_FACTORY_GET_INSTANCE_METHOD = injectionProviderFactoryGetInstanceMethod;
        INJECTION_PROVIDER_FACTORY_GET_INJECTION_PROVIDER_METHOD = injectionProviderFactoryGetInjectionProviderMethod;
        INJECTION_PROVIDER_CLASS = injectionProviderClass;
        INJECTION_PROVIDER_INJECT_METHOD = injectionProviderInjectMethod;
        INJECTION_PROVIDER_POST_CONSTRUCT_METHOD = injectionProviderPostConstructMethod;
        INJECTION_PROVIDER_PRE_DESTROY_METHOD = injectionProviderPreDestroyMethod;
    }

    public static Object getInstance()
    {
        if (FACTORY_FINDER_PROVIDER_GET_INSTANCE_METHOD != null)
        {
            try
            {
                return FACTORY_FINDER_PROVIDER_GET_INSTANCE_METHOD.invoke(FACTORY_FINDER_PROVIDER_FACTORY_CLASS);
            }
            catch (Exception e)
            {
                //No op
                Logger log = Logger.getLogger(_FactoryFinderProviderFactory.class.getName());
                if (log.isLoggable(Level.WARNING))
                {
                    log.log(Level.WARNING, "Cannot retrieve current FactoryFinder instance from " +
                            "FactoryFinderProviderFactory." +
                            " Default strategy using thread context class loader will be used.", e);
                }
            }
        }
        return null;
    }

}
