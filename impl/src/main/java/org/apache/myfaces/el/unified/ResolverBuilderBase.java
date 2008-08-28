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
package org.apache.myfaces.el.unified;

import javax.el.CompositeELResolver;
import javax.el.ELResolver;
import javax.faces.application.Application;
import javax.faces.el.PropertyResolver;
import javax.faces.el.VariableResolver;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.el.convert.PropertyResolverToELResolver;
import org.apache.myfaces.el.convert.VariableResolverToELResolver;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public class ResolverBuilderBase
{
    private final RuntimeConfig _config;

    public ResolverBuilderBase(RuntimeConfig config)
    {
        _config = config;
    }

    /**
     * add the el resolvers from the faces config, the el resolver wrapper for variable resolver, the el resolver
     * wrapper for the property resolver and the el resolvers added by {@link Application#addELResolver(ELResolver)}.
     * The resolvers where only added if they are not null
     * 
     * @param elResolver
     *            the composite el resolver to which the resolvers where added
     */
    protected void addFromRuntimeConfig(CompositeELResolver elResolver)
    {
        if (_config.getFacesConfigElResolvers() != null)
        {
            elResolver.add(_config.getFacesConfigElResolvers());
        }

        if (_config.getVariableResolver() != null)
        {
            elResolver.add(createELResolver(_config.getVariableResolver()));
        }
        else if (_config.getVariableResolverChainHead() != null)
        {
            elResolver.add(createELResolver(_config.getVariableResolverChainHead()));
        }

        if (_config.getPropertyResolver() != null)
        {
            elResolver.add(createELResolver(_config.getPropertyResolver()));
        }
        else if (_config.getPropertyResolverChainHead() != null)
        {
            elResolver.add(createELResolver(_config.getPropertyResolverChainHead()));
        }

        if (_config.getApplicationElResolvers() != null)
        {
            elResolver.add(_config.getApplicationElResolvers());
        }
    }

    protected ELResolver createELResolver(VariableResolver resolver)
    {
        return new VariableResolverToELResolver(resolver);
    }

    protected ELResolver createELResolver(PropertyResolver resolver)
    {
        return new PropertyResolverToELResolver(resolver);
    }

}