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
package org.apache.myfaces.spi.impl;

import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.spi.InjectionProvider;
import org.apache.myfaces.spi.InjectionProviderFactory;
import org.apache.myfaces.spi.ServiceProviderFinderFactory;

/**
 * 
 * 
 */
public class DefaultInjectionProviderFactory extends InjectionProviderFactory
{
    private static Logger log = Logger.getLogger(DefaultInjectionProviderFactory.class.getName());


    public static final String INJECTION_PROVIDER_INSTANCE_KEY
            = InjectionProvider.class.getName() + ".INJECTION_PROVIDER_INSTANCE";

    public DefaultInjectionProviderFactory()
    {
    }

    @Override
    public InjectionProvider getInjectionProvider(ExternalContext externalContext)
    {
        InjectionProvider injectionProvider = null;
        if (externalContext == null)
        {
            // Really in jsf 2.0, this will not happen, because a Startup/Shutdown
            // FacesContext and ExternalContext are provided on initialization and shutdown,
            // and in other scenarios the real FacesContext/ExternalContext is provided.
            log.info("No ExternalContext using fallback InjectionProvider.");
            injectionProvider = resolveFallbackInjectionProvider();
        }
        else
        {
            injectionProvider = (InjectionProvider)
                    externalContext.getApplicationMap().get(INJECTION_PROVIDER_INSTANCE_KEY);
        }
        if (injectionProvider == null)
        {
            if (!resolveInjectionProviderFromExternalContext(externalContext))
            {
                if (!resolveInjectionProviderFromService(externalContext))
                {
                    injectionProvider = resolveFallbackInjectionProvider();
                    externalContext.getApplicationMap().put(INJECTION_PROVIDER_INSTANCE_KEY, injectionProvider);
                }
                else
                {
                    //Retrieve it because it was resolved
                    injectionProvider = (InjectionProvider)
                            externalContext.getApplicationMap().get(INJECTION_PROVIDER_INSTANCE_KEY);
                }
            }
            else
            {
                //Retrieve it because it was resolved
                injectionProvider = (InjectionProvider)
                        externalContext.getApplicationMap().get(INJECTION_PROVIDER_INSTANCE_KEY);
            }
            log.fine("Using InjectionProvider " + injectionProvider.getClass().getName());
        }
        return injectionProvider;
    }

    @Override
    public void release()
    {
    }



    private boolean resolveInjectionProviderFromExternalContext(ExternalContext externalContext)
    {
        try
        {
            String injectionProvider = externalContext.getInitParameter(MyfacesConfig.INJECTION_PROVIDER);
            if (injectionProvider != null)
            {

                Object obj = createClass(injectionProvider, externalContext);

                if (obj instanceof InjectionProvider)
                {
                    externalContext.getApplicationMap().put(INJECTION_PROVIDER_INSTANCE_KEY, obj);
                    return true;
                }
            }
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e)
        {
            log.log(Level.SEVERE, "", e);
        }
        return false;
    }


    private boolean resolveInjectionProviderFromService(ExternalContext externalContext)
    {
        boolean returnValue = false;
        final ExternalContext extContext = externalContext;
        try
        {
            if (System.getSecurityManager() != null)
            {
                returnValue = AccessController.doPrivileged((PrivilegedExceptionAction<Boolean>) () ->
                {
                    List<String> classList = ServiceProviderFinderFactory.getServiceProviderFinder(extContext).
                            getServiceProviderList(MyfacesConfig.INJECTION_PROVIDER);
                    for (String className : classList)
                    {
                        Object obj = createClass(className,extContext);
                        if (InjectionProvider.class.isAssignableFrom(obj.getClass()))
                        {
                            InjectionProvider discoverableInjectionProvider = (InjectionProvider) obj;
                            if (discoverableInjectionProvider.isAvailable())
                            {
                                extContext.getApplicationMap().put(INJECTION_PROVIDER_INSTANCE_KEY,
                                        discoverableInjectionProvider);
                                return true;
                            }
                        }
                    }
                    return false;
                });
            }
            else
            {
                List<String> classList = ServiceProviderFinderFactory.getServiceProviderFinder(extContext).
                        getServiceProviderList(MyfacesConfig.INJECTION_PROVIDER);
                for (String className : classList)
                {
                    Object obj = createClass(className,extContext);
                    if (InjectionProvider.class.isAssignableFrom(obj.getClass()))
                    {
                        InjectionProvider discoverableInjectionProvider = (InjectionProvider) obj;
                        if (discoverableInjectionProvider.isAvailable())
                        {
                            extContext.getApplicationMap().put(INJECTION_PROVIDER_INSTANCE_KEY,
                                                               discoverableInjectionProvider);
                            return true;
                        }
                    }
                }
            }
        }
        catch (ClassNotFoundException | NoClassDefFoundError e)
        {
            // ignore
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
        {
            log.log(Level.SEVERE, "", e);
        }
        catch (PrivilegedActionException e)
        {
            throw new FacesException(e);
        }
        return returnValue;
    }

    private Object createClass(String className, ExternalContext externalContext)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException
    {
        Class<?> clazz = ClassUtils.classForName(className);

        try
        {
            return ClassUtils.newInstance(clazz, new Class<?>[]{ ExternalContext.class }, externalContext);
        }
        catch (NoSuchMethodException e)
        {
            return ClassUtils.newInstance(clazz);
        }
    }

    private InjectionProvider resolveFallbackInjectionProvider()
    {
        return resolveFallbackInjectionProvider(FacesContext.getCurrentInstance().getExternalContext());
    }

    private InjectionProvider resolveFallbackInjectionProvider(ExternalContext externalContext)
    {
        try
        {
            ClassUtils.classForName("jakarta.annotation.PreDestroy");
        }
        catch (ClassNotFoundException e)
        {
            // no annotation available don't process annotations
            return new NoAnnotationInjectionProvider(); 
        }
        Context context;
        try
        {
            context = new InitialContext();
            try
            {
                ClassUtils.classForName("jakarta.ejb.EJB");
                // Asume full JEE 5 container
                return new AllAnnotationInjectionProvider(context);
            }
            catch (ClassNotFoundException e)
            {
                // something else
                return new ResourceAnnotationInjectionProvider(context);
            }
        }
        catch (NamingException e)
        {
            // no initial context available no injection
            log.log(Level.SEVERE, "No InitialContext found. Using NoInjectionAnnotationProcessor.", e);
            return new NoInjectionAnnotationInjectionProvider();
        }
        catch (NoClassDefFoundError e)
        {
            //On Google App Engine, javax.naming.Context is a restricted class.
            //In that case, NoClassDefFoundError is thrown. stageName needs to be configured
            //below by context parameter.
            log.log(Level.SEVERE, "No InitialContext class definition found. Using NoInjectionAnnotationProcessor.");
            return new NoInjectionAnnotationInjectionProvider();
        }
    }
}
