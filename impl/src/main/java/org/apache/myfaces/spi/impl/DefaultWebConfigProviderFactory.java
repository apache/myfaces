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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.context.ExternalContext;

import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.spi.ServiceProviderFinderFactory;
import org.apache.myfaces.spi.WebConfigProvider;
import org.apache.myfaces.spi.WebConfigProviderFactory;

/**
 * The default implementation of WebXmlProviderFactory. Looks for META-INF/service
 * entries of org.apache.myfaces.shared.spi.WebXmlProvider.
 *
 * Returns a new DefaultWebXmlProvider if no custom impl can be found.
 *
 * @author Jakob Korherr
 * @since 2.0.3
 */
public class DefaultWebConfigProviderFactory extends WebConfigProviderFactory
{

    public static final String WEB_CONFIG_PROVIDER = WebConfigProvider.class.getName();
    
    public static final String WEB_CONFIG_PROVIDER_LIST = WebConfigProvider.class.getName()+".LIST";

    private Logger getLogger()
    {
        return Logger.getLogger(DefaultWebConfigProviderFactory.class.getName());
    }

    @Override
    public WebConfigProvider getWebConfigProvider(ExternalContext externalContext)
    {
        WebConfigProvider instance = null;

        try
        {
            instance = resolveWebXmlProviderFromService(externalContext);
        }
        catch (ClassNotFoundException | NoClassDefFoundError | InstantiationException | IllegalAccessException
                | InvocationTargetException e)
        {
            getLogger().log(Level.SEVERE, "", e);
        }

        if (instance == null)
        {
            instance = new DefaultWebConfigProvider();
        }
 
        return instance;
    }

    private WebConfigProvider resolveWebXmlProviderFromService(
            ExternalContext externalContext) throws ClassNotFoundException,
            NoClassDefFoundError,
            InstantiationException,
            IllegalAccessException,
            InvocationTargetException
    {
        List<String> classList = (List<String>) externalContext.getApplicationMap().get(WEB_CONFIG_PROVIDER_LIST);
        if (classList == null)
        {
            classList = ServiceProviderFinderFactory.getServiceProviderFinder(externalContext).
                    getServiceProviderList(WEB_CONFIG_PROVIDER);
            externalContext.getApplicationMap().put(WEB_CONFIG_PROVIDER_LIST, classList);
        }

        return ClassUtils.buildApplicationObject(WebConfigProvider.class, classList, new DefaultWebConfigProvider());
    }

}
