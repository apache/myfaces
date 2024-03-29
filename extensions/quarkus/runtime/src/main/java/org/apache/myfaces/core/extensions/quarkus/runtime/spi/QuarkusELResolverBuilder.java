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
package org.apache.myfaces.core.extensions.quarkus.runtime.spi;

import jakarta.el.ELResolver;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.el.DefaultELResolverBuilder;

/**
 * Custom {@link org.apache.myfaces.el.ELResolverBuilder} which only works with EL3.x+
 * and replaces the {@link jakarta.enterprise.inject.spi.BeanManager#getELResolver()}
 * with our own {@link QuarkusCdiELResolver}
 */
public class QuarkusELResolverBuilder extends DefaultELResolverBuilder
{

    public QuarkusELResolverBuilder(RuntimeConfig runtimeConfig, MyfacesConfig myfacesConfig)
    {
        super(runtimeConfig, myfacesConfig);
    }

    @Override
    protected ELResolver getCDIELResolver()
    {
        return new QuarkusCdiELResolver();
    }
}
