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

import org.apache.commons.discovery.ResourceNameIterator;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.commons.discovery.resource.names.DiscoverServiceNames;
import org.apache.myfaces.config.DefaultFacesConfigResourceProvider;
import org.apache.myfaces.shared_impl.spi.impl.SpiUtils;
import org.apache.myfaces.spi.FacesConfigResourceProvider;
import org.apache.myfaces.spi.FacesConfigResourceProviderFactory;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @since 2.0.2
 * @author Leonardo Uribe
 */
public class DefaultFacesConfigResourceProviderFactory extends FacesConfigResourceProviderFactory
{
    public static final String ANNOTATION_PROVIDER = FacesConfigResourceProvider.class.getName();
    
    private Logger getLogger()
    {
        return Logger.getLogger(DefaultFacesConfigResourceProviderFactory.class.getName());
    }    
    
    @Override
    public FacesConfigResourceProvider createFacesConfigResourceProvider(
            ExternalContext externalContext)
    {
        FacesConfigResourceProvider returnValue = null;
        final ExternalContext extContext = externalContext;
        try
        {
            if (System.getSecurityManager() != null)
            {
                returnValue = AccessController.doPrivileged(new java.security.PrivilegedExceptionAction<FacesConfigResourceProvider>()
                        {
                            public FacesConfigResourceProvider run() throws ClassNotFoundException,
                                    NoClassDefFoundError,
                                    InstantiationException,
                                    IllegalAccessException,
                                    InvocationTargetException,
                                    PrivilegedActionException
                            {
                                return resolveFacesConfigResourceProviderFromService(extContext);
                            }
                        });
            }
            else
            {
                returnValue = resolveFacesConfigResourceProviderFromService(extContext);
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
    
    private FacesConfigResourceProvider resolveFacesConfigResourceProviderFromService(
            ExternalContext externalContext) throws ClassNotFoundException,
            NoClassDefFoundError,
            InstantiationException,
            IllegalAccessException,
            InvocationTargetException,
            PrivilegedActionException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null)
        {
            classLoader = this.getClass().getClassLoader();
        }
        ClassLoaders loaders = new ClassLoaders();
        loaders.put(classLoader);
        DiscoverServiceNames dsn = new DiscoverServiceNames(loaders);
        ResourceNameIterator iter = dsn.findResourceNames(ANNOTATION_PROVIDER);
        return SpiUtils.buildApplicationObject(FacesConfigResourceProvider.class, iter, new DefaultFacesConfigResourceProvider());
    }

}
