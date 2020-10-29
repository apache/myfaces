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
package org.apache.myfaces.core.extensions.quarkus.runtime.application;

import javax.el.CompositeELResolver;
import javax.el.ELResolver;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationWrapper;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.config.RuntimeConfig;

import org.apache.myfaces.core.extensions.quarkus.runtime.spi.QuarkusELResolverBuilder;

public class QuarkusApplication extends ApplicationWrapper
{
    private CompositeELResolver elResolver;

    private final RuntimeConfig runtimeConfig;
    private final MyfacesConfig myfacesConfig;

    public QuarkusApplication(Application delegate)
    {
        super(delegate);

        runtimeConfig = RuntimeConfig.getCurrentInstance(FacesContext.getCurrentInstance());
        myfacesConfig = MyfacesConfig.getCurrentInstance(FacesContext.getCurrentInstance());
    }

    @Override
    public final ELResolver getELResolver()
    {
        if (elResolver == null)
        {
            elResolver = new CompositeELResolver();
            new QuarkusELResolverBuilder(runtimeConfig, myfacesConfig).build(elResolver);
        }

        return elResolver;
    }

}
