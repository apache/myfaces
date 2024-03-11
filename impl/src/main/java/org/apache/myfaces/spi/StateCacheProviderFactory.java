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

import jakarta.faces.FacesWrapper;
import jakarta.faces.context.ExternalContext;
import org.apache.myfaces.spi.impl.DefaultStateCacheProviderFactory;
import org.apache.myfaces.spi.impl.SpiUtils;

/**
 *
 */
public abstract class StateCacheProviderFactory implements FacesWrapper<StateCacheProviderFactory>
{
    private static final String FACTORY_KEY = StateCacheProviderFactory.class.getName();
    
    public static StateCacheProviderFactory getStateCacheProviderFactory(ExternalContext ctx)
    {
        StateCacheProviderFactory instance
                = (StateCacheProviderFactory) ctx.getApplicationMap().get(FACTORY_KEY);

        if (instance != null)
        {
            return instance;
        }

        instance = (StateCacheProviderFactory)
                SpiUtils.build(ctx, StateCacheProviderFactory.class,
                        DefaultStateCacheProviderFactory.class);

        if (instance != null)
        {
            setStateCacheProviderFactory(ctx, instance);
        }

        return instance;
    }

    public static void setStateCacheProviderFactory(ExternalContext ctx,
                                                             StateCacheProviderFactory instance)
    {
        ctx.getApplicationMap().put(FACTORY_KEY, instance);
    }    
    
    public StateCacheProvider getStateCacheProvider(ExternalContext ctx)
    {
        return createStateCacheProvider(ctx);
    }
    
    public abstract StateCacheProvider createStateCacheProvider(ExternalContext externalContext);
    
    @Override
    public StateCacheProviderFactory getWrapped()
    {
        return null;
    }
    
}
