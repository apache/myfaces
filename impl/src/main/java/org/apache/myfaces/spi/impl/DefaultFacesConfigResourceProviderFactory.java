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

import org.apache.myfaces.config.DefaultFacesConfigResourceProvider;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.spi.FacesConfigResourceProvider;
import org.apache.myfaces.spi.FacesConfigResourceProviderFactory;
import org.apache.myfaces.spi.ServiceProviderFinderFactory;

import jakarta.faces.context.ExternalContext;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @since 2.0.2
 * @author Leonardo Uribe
 */
public class DefaultFacesConfigResourceProviderFactory extends FacesConfigResourceProviderFactory
{
    public static final String FACES_CONFIG_PROVIDER = FacesConfigResourceProvider.class.getName();
    
    public static final String FACES_CONFIG_PROVIDER_LIST = FacesConfigResourceProvider.class.getName()+".LIST";
    
    private Logger getLogger()
    {
        return Logger.getLogger(DefaultFacesConfigResourceProviderFactory.class.getName());
    }    
    
    @Override
    public FacesConfigResourceProvider createFacesConfigResourceProvider(ExternalContext externalContext)
    {
        FacesConfigResourceProvider instance = null;

        try
        {
            instance = resolveFacesConfigResourceProviderFromService(externalContext);
        }
        catch (ClassNotFoundException | NoClassDefFoundError e)
        {
            // ignore
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
        {
            getLogger().log(Level.SEVERE, "", e);
        }

        return instance;
    }
    
    private FacesConfigResourceProvider resolveFacesConfigResourceProviderFromService(
            ExternalContext externalContext) throws ClassNotFoundException,
            NoClassDefFoundError,
            InstantiationException,
            IllegalAccessException,
            InvocationTargetException
    {
        List<String> classList = (List<String>) externalContext.getApplicationMap().get(FACES_CONFIG_PROVIDER_LIST);
        if (classList == null)
        {
            classList = ServiceProviderFinderFactory.getServiceProviderFinder(externalContext).
                    getServiceProviderList(FACES_CONFIG_PROVIDER);
            externalContext.getApplicationMap().put(FACES_CONFIG_PROVIDER_LIST, classList);
        }

        return ClassUtils.buildApplicationObject(FacesConfigResourceProvider.class, classList,
                                                 new DefaultFacesConfigResourceProvider());
    }

}
