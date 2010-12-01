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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;

import org.apache.myfaces.config.DefaultFacesConfigurationProvider;
import org.apache.myfaces.spi.FacesConfigurationProvider;
import org.apache.myfaces.spi.FacesConfigurationProviderFactory;
import org.apache.myfaces.spi.ServiceLoaderFinderFactory;

/**
 * 
 * @author Leonardo Uribe
 * @since 2.0.3
 */
public class DefaultFacesConfigurationProviderFactory extends FacesConfigurationProviderFactory
{

    public static final String FACES_CONFIGURATION_PROVIDER = FacesConfigurationProvider.class.getName();
    
    public static final String FACES_CONFIGURATION_PROVIDER_LIST = FacesConfigurationProvider.class.getName()+".LIST";

    private Logger getLogger()
    {
        return Logger.getLogger(DefaultFacesConfigurationProviderFactory.class.getName());
    }

    @Override
    public FacesConfigurationProvider getFacesConfigurationProvider(
            ExternalContext externalContext)
    {
        FacesConfigurationProvider returnValue = null;
        final ExternalContext extContext = externalContext;
        try
        {
            if (System.getSecurityManager() != null)
            {
                returnValue = AccessController.doPrivileged(new java.security.PrivilegedExceptionAction<FacesConfigurationProvider>()
                        {
                            public FacesConfigurationProvider run() throws ClassNotFoundException,
                                    NoClassDefFoundError,
                                    InstantiationException,
                                    IllegalAccessException,
                                    InvocationTargetException,
                                    PrivilegedActionException
                            {
                                return resolveFacesConfigurationProviderFromService(extContext);
                            }
                        });
            }
            else
            {
                returnValue = resolveFacesConfigurationProviderFromService(extContext);
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

    private FacesConfigurationProvider resolveFacesConfigurationProviderFromService(
            ExternalContext externalContext) throws ClassNotFoundException,
            NoClassDefFoundError,
            InstantiationException,
            IllegalAccessException,
            InvocationTargetException,
            PrivilegedActionException
    {
        List<String> classList = (List<String>) externalContext.getApplicationMap().get(FACES_CONFIGURATION_PROVIDER_LIST);
        if (classList == null)
        {
            classList = ServiceLoaderFinderFactory.getServiceLoaderFinder(externalContext).getServiceProviderList(FACES_CONFIGURATION_PROVIDER);
            externalContext.getApplicationMap().put(FACES_CONFIGURATION_PROVIDER_LIST, classList);
        }

        return SpiUtils.buildApplicationObject(externalContext, FacesConfigurationProvider.class, classList, new DefaultFacesConfigurationProvider());
    }
}
