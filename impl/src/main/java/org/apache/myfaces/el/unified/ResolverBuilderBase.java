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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.el.convert.PropertyResolverToELResolver;
import org.apache.myfaces.el.convert.VariableResolverToELResolver;
import org.apache.myfaces.el.unified.resolver.FacesCompositeELResolver.Scope;
import org.apache.myfaces.shared_impl.util.ClassUtils;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public class ResolverBuilderBase
{
    
    private static final Logger log = Logger.getLogger(ResolverBuilderBase.class.getName());
    
    @JSFWebConfigParam(since = "1.2.10, 2.0.2",
            desc = "The Class of an Comparator<ELResolver> implementation.")
    public static final String EL_RESOLVER_COMPARATOR = "org.apache.myfaces.EL_RESOLVER_COMPARATOR";
    
    @JSFWebConfigParam(since = "2.1.0",
            desc="The Class of an org.apache.commons.collections.Predicate<ELResolver> implementation." +
            "If used and returns true for a ELResolver instance, such resolver will not be installed in ELResolvers chain." +
            "Use with caution - can break functionality defined in JSF specification 'ELResolver Instances Provided by Faces'")
    public static final String EL_RESOLVER_PREDICATE = "org.apache.myfaces.EL_RESOLVER_PREDICATE";
    
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
     * @param scope scope of ELResolvers (Faces,JSP)  
     * @since 1.2.10, 2.0.2
     */
    @SuppressWarnings("unchecked")
    protected void sortELResolvers(List<ELResolver> resolvers, Scope scope)
    {
        Comparator<ELResolver> comparator = (Comparator<ELResolver>) getAplicationScopedObject(
                FacesContext.getCurrentInstance(), EL_RESOLVER_COMPARATOR);
        if (comparator != null)
        {
            try
            {
                // sort the resolvers
                Collections.sort(resolvers, comparator);
                
                if (log.isLoggable(Level.INFO)) {
                    log.log(Level.INFO, "Chain of EL resolvers for {0} sorted with: {1} and the result order is {2}", 
                            new Object [] {scope, comparator, resolvers});
                }
            }
            catch (Exception e)
            {
                log.log(Level.WARNING, 
                        "Could not sort ELResolvers with custom Comparator", e);
            }
        }
    }
    
    /**
     * Filters the ELResolvers  with a custom Predicate provided by the user.
     * @param resolvers list of ELResolvers
     * @param scope scope of ELResolvers (Faces,JSP)
     * @return Iterable instance of Iterable containing filtered ELResolvers 
     */
    protected Iterable<ELResolver> filterELResolvers(List<ELResolver> resolvers, Scope scope)
    {
        
        Predicate predicate = (Predicate) getAplicationScopedObject(
                FacesContext.getCurrentInstance(), EL_RESOLVER_PREDICATE);
        if (predicate != null) {
            try
            {
                // filter the resolvers
                CollectionUtils.filter(resolvers, predicate);
                
                if (log.isLoggable(Level.INFO)) {
                    log.log(Level.INFO, "Chain of EL resolvers for {0} filtered with: {1} and the result is {2}", 
                            new Object [] {scope, predicate, resolvers});
                }
            }
            catch (Exception e)
            {
                log.log(Level.WARNING, 
                        "Could not filter ELResolvers with custom Predicate", e);
            }
        }
        return resolvers;
    }
    
    // TODO this is very common logic, move to Utils? 
    protected Object getAplicationScopedObject(FacesContext facesContext, String initParameterName)
    {
        ExternalContext externalContext = facesContext.getExternalContext();
        String className = externalContext.getInitParameter(initParameterName);

        Object applicationScopedObject = null;
        if (className != null && !"".equals(className))
        {
            // if we already have a cached instance, use it
            applicationScopedObject = externalContext. getApplicationMap().get(initParameterName);
            try
            {
                if (applicationScopedObject == null)
                {
                    // get the  class
                    Class<?> clazz = ClassUtils.classForName(className);

                    // create the instance
                    applicationScopedObject = clazz.newInstance();

                    // cache the instance, because it will be used at least two times
                    externalContext.getApplicationMap()
                    .put(initParameterName, applicationScopedObject);
                }
            }
            catch (Exception e)
            {
                log.log(Level.WARNING, 
                        "Could not create instance of " + className + " for context-param " + initParameterName, e);
            }
        }    

        return applicationScopedObject;
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
