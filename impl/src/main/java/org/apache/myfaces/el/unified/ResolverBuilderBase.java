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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELResolver;
import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.PropertyResolver;
import javax.faces.el.VariableResolver;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.el.convert.PropertyResolverToELResolver;
import org.apache.myfaces.el.convert.VariableResolverToELResolver;
import org.apache.myfaces.shared_impl.util.ClassUtils;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public class ResolverBuilderBase
{
    
    private static final Logger log = Logger.getLogger(ResolverBuilderBase.class.getName());
    
    @JSFWebConfigParam(since = "2.0.2",
            desc = "The Class of an Comparator<ELResolver> implementation.")
    public static final String EL_RESOLVER_COMPARATOR = "org.apache.myfaces.EL_RESOLVER_COMPARATOR";
    
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
     * @param resolvers
     */
    protected void addFromRuntimeConfig(List<ELResolver> resolvers)
    {
        if (_config.getFacesConfigElResolvers() != null)
        {
            for (ELResolver resolver : _config.getFacesConfigElResolvers())
            {
                resolvers.add(resolver);
            }
        }

        if (_config.getVariableResolver() != null)
        {
            resolvers.add(createELResolver(_config.getVariableResolver()));
        }
        else if (_config.getVariableResolverChainHead() != null)
        {
            resolvers.add(createELResolver(_config.getVariableResolverChainHead()));
        }

        if (_config.getPropertyResolver() != null)
        {
            resolvers.add(createELResolver(_config.getPropertyResolver()));
        }
        else if (_config.getPropertyResolverChainHead() != null)
        {
            resolvers.add(createELResolver(_config.getPropertyResolverChainHead()));
        }

        if (_config.getApplicationElResolvers() != null)
        {
            for (ELResolver resolver : _config.getApplicationElResolvers())
            {
                resolvers.add(resolver);
            }
        }
    }
    
    /**
     * Sort the ELResolvers with a custom Comparator provided by the user.
     * @param resolvers
     */
    @SuppressWarnings("unchecked")
    protected void sortELResolvers(List<ELResolver> resolvers)
    {
        ExternalContext externalContext 
                = FacesContext.getCurrentInstance().getExternalContext();
        
        String comparatorClass = externalContext
                .getInitParameter(EL_RESOLVER_COMPARATOR);
        
        if (comparatorClass != null && !"".equals(comparatorClass))
        {
            // the user provided the parameter.
            
            // if we already have a cached instance, use it
            Comparator<ELResolver> comparator 
                    = (Comparator<ELResolver>) externalContext.
                        getApplicationMap().get(EL_RESOLVER_COMPARATOR);
            try
            {
                if (comparator == null)
                {
                    // get the comparator class
                    Class<Comparator<ELResolver>> clazz 
                             = ClassUtils.classForName(comparatorClass);
                    
                    // create the instance
                    comparator = clazz.newInstance();
                    
                    // cache the instance, because it will be used at least two times
                    externalContext.getApplicationMap()
                            .put(EL_RESOLVER_COMPARATOR, comparator);
                }
                
                // sort the resolvers
                Collections.sort(resolvers, comparator);
            }
            catch (Exception e)
            {
                log.log(Level.WARNING, 
                        "Could not sort ELResolvers with custom Comparator", e);
            }
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
