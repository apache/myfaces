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

import org.apache.myfaces.spi.impl.SpiUtils;

import jakarta.faces.context.ExternalContext;
import org.apache.myfaces.spi.impl.DefaultFacesFlowProviderFactory;

/**
 * SPI to provide a FacesFlowProviderFactory implementation and thus
 * a custom FacesFlowProvider instance.
 *
 * @author Leonardo Uribe
 * @since 2.2
 */
public abstract class FacesFlowProviderFactory
{
    private static final String FACTORY_KEY = FacesFlowProviderFactory.class.getName();

    public static FacesFlowProviderFactory getFacesFlowProviderFactory(ExternalContext ctx)
    {
        FacesFlowProviderFactory instance
                = (FacesFlowProviderFactory) ctx.getApplicationMap().get(FACTORY_KEY);

        if (instance != null)
        {
            // use cached instance
            return instance;
        }

        // create new instance from service entry
        instance = (FacesFlowProviderFactory) SpiUtils
                .build(ctx, FacesFlowProviderFactory.class, DefaultFacesFlowProviderFactory.class);

        if (instance != null)
        {
            // cache instance on ApplicationMap
            setFacesFlowProviderFactory(ctx, instance);
        }

        return instance;
    }

    public static void setFacesFlowProviderFactory(ExternalContext ctx, FacesFlowProviderFactory instance)
    {
        ctx.getApplicationMap().put(FACTORY_KEY, instance);
    }

    public abstract FacesFlowProvider getFacesFlowProvider(ExternalContext externalContext);

}
