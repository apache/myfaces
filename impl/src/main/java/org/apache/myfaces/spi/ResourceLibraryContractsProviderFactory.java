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
package org.apache.myfaces.spi;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;

import org.apache.myfaces.spi.impl.DefaultResourceLibraryContractsProviderFactory;
import org.apache.myfaces.spi.impl.SpiUtils;

/**
 * Factory that provide ResourceLibraryContractsProvider instances
 * 
 * @since 2.0.2
 * @author Leonardo Uribe
 */
public abstract class ResourceLibraryContractsProviderFactory
{
    private static final String FACTORY_KEY = ResourceLibraryContractsProviderFactory.class.getName();

    public static ResourceLibraryContractsProviderFactory getFacesConfigResourceProviderFactory(ExternalContext ctx)
    {
        ResourceLibraryContractsProviderFactory instance
                = (ResourceLibraryContractsProviderFactory) ctx.getApplicationMap().get(FACTORY_KEY);
        if (instance != null)
        {
            return instance;
        }
        ResourceLibraryContractsProviderFactory lpf = null;
        try
        {

            if (System.getSecurityManager() != null)
            {
                final ExternalContext ectx = ctx;
                lpf = (ResourceLibraryContractsProviderFactory)
                        AccessController.doPrivileged((PrivilegedExceptionAction) () -> SpiUtils.build(ectx, 
                                ResourceLibraryContractsProviderFactory.class,
                                DefaultResourceLibraryContractsProviderFactory.class));
            }
            else
            {
                lpf = (ResourceLibraryContractsProviderFactory)
                        SpiUtils.build(ctx, ResourceLibraryContractsProviderFactory.class,
                                DefaultResourceLibraryContractsProviderFactory.class);
            }
        }
        catch (PrivilegedActionException pae)
        {
            throw new FacesException(pae);
        }
        if (lpf != null)
        {
            setResourceLibraryContractsProviderFactory(ctx, lpf);
        }
        return lpf;
    }

    public static void setResourceLibraryContractsProviderFactory(ExternalContext ctx,
                                                               ResourceLibraryContractsProviderFactory instance)
    {
        ctx.getApplicationMap().put(FACTORY_KEY, instance);
    }

    public abstract ResourceLibraryContractsProvider 
        createResourceLibraryContractsProvider(ExternalContext externalContext);
}
