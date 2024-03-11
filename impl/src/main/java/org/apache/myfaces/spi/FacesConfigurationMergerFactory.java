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

import org.apache.myfaces.spi.impl.DefaultFacesConfigurationMergerFactory;
import org.apache.myfaces.spi.impl.SpiUtils;

import jakarta.faces.context.ExternalContext;

/**
 * SPI to provide a FacesConfigurationMergerFactory implementation and thus
 * a custom FacesConfigurationMerger instance.
 *
 * @author Jakob Korherr
 * @since 2.0.3
 */
public abstract class FacesConfigurationMergerFactory
{
    private static final String FACTORY_KEY = FacesConfigurationMergerFactory.class.getName();

    public static FacesConfigurationMergerFactory getFacesConfigurationMergerFactory(ExternalContext ctx)
    {
        FacesConfigurationMergerFactory instance
                = (FacesConfigurationMergerFactory) ctx.getApplicationMap().get(FACTORY_KEY);

        if (instance != null)
        {
            // use cached instance
            return instance;
        }

        // create new instance from service entry
        instance = (FacesConfigurationMergerFactory) SpiUtils
                .build(ctx, FacesConfigurationMergerFactory.class,
                        DefaultFacesConfigurationMergerFactory.class);

        if (instance != null)
        {
            // cache instance on ApplicationMap
            setFacesConfigurationMergerFactory(ctx, instance);
        }

        return instance;
    }

    public static void setFacesConfigurationMergerFactory(ExternalContext ctx,
                                                          FacesConfigurationMergerFactory instance)
    {
        ctx.getApplicationMap().put(FACTORY_KEY, instance);
    }

    public abstract FacesConfigurationMerger getFacesConfigurationMerger(ExternalContext externalContext);

}
