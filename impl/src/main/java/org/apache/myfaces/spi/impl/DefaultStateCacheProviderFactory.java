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
import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import org.apache.myfaces.application.viewstate.StateCacheFactoryImpl;
import org.apache.myfaces.util.ClassUtils;
import org.apache.myfaces.spi.ServiceProviderFinderFactory;
import org.apache.myfaces.spi.StateCacheProvider;
import org.apache.myfaces.spi.StateCacheProviderFactory;

/**
 *
 */
public class DefaultStateCacheProviderFactory extends StateCacheProviderFactory
{
    
    public static final String STATE_CACHE_PROVIDER = StateCacheProvider.class.getName();
    
    public static final String STATE_CACHE_PROVIDER_LIST = StateCacheProvider.class.getName()+".LIST";
    
    public static final String STATE_CACHE_PROVIDER_INSTANCE = StateCacheProvider.class.getName()+".INSTANCE";
    
    private Logger getLogger()
    {
        return Logger.getLogger(DefaultStateCacheProviderFactory.class.getName());
    }
    
    @Override
    public StateCacheProvider getStateCacheProvider(ExternalContext externalContext)
    {
        StateCacheProvider stateCacheProvider
                = (StateCacheProvider) externalContext.getApplicationMap().get(STATE_CACHE_PROVIDER_INSTANCE);
        if (stateCacheProvider == null)
        {
            stateCacheProvider = createStateCacheProvider(externalContext);
            externalContext.getApplicationMap().put(STATE_CACHE_PROVIDER_INSTANCE, stateCacheProvider);
        }
        return stateCacheProvider;
    }
    
    @Override
    public StateCacheProvider createStateCacheProvider(
            ExternalContext externalContext)
    {
        StateCacheProvider returnValue = null;
        final ExternalContext extContext = externalContext;
        try
        {
            if (System.getSecurityManager() != null)
            {
                returnValue = AccessController.doPrivileged(new PrivilegedExceptionAction<StateCacheProvider>()
                        {
                            public StateCacheProvider run() throws ClassNotFoundException,
                                    NoClassDefFoundError,
                                    InstantiationException,
                                    IllegalAccessException,
                                    InvocationTargetException,
                                    PrivilegedActionException
                            {
                                return resolveStateCacheProviderFromService(extContext);
                            }
                        });
            }
            else
            {
                returnValue = resolveStateCacheProviderFromService(extContext);
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
            getLogger().log(Level.SEVERE, "", e);
        }
        catch (IllegalAccessException e)
        {
            getLogger().log(Level.SEVERE, "", e);
        }
        catch (InvocationTargetException e)
        {
            getLogger().log(Level.SEVERE, "", e);
        }
        catch (PrivilegedActionException e)
        {
            throw new FacesException(e);
        }
        return returnValue;
    }
    
    private StateCacheProvider resolveStateCacheProviderFromService(
            ExternalContext externalContext) throws ClassNotFoundException,
            NoClassDefFoundError,
            InstantiationException,
            IllegalAccessException,
            InvocationTargetException,
            PrivilegedActionException
    {
        List<String> classList = (List<String>) externalContext.getApplicationMap().get(STATE_CACHE_PROVIDER_LIST);
        if (classList == null)
        {
            classList = ServiceProviderFinderFactory.getServiceProviderFinder(externalContext).
                    getServiceProviderList(STATE_CACHE_PROVIDER);
            externalContext.getApplicationMap().put(STATE_CACHE_PROVIDER_LIST, classList);
        }
        return ClassUtils.buildApplicationObject(StateCacheProvider.class, classList, new StateCacheFactoryImpl());
    }    
    
}
