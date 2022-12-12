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
package org.apache.myfaces.el;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.el.CompositeELResolver;
import jakarta.el.ELResolver;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.FacesContext;
import java.util.stream.Collectors;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.util.ExternalSpecifications;

/**
 * The ELResolverBuilder is responsible to build the el resolver which is used by the application through
 * {@link jakarta.faces.application.Application#getELResolver()} according to 1.2 spec
 * section 5.6.2
 * 
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ELResolverBuilder
{
    private static final Logger log = Logger.getLogger(ELResolverBuilder.class.getName());
    
    protected final RuntimeConfig runtimeConfig;
    protected final MyfacesConfig myfacesConfig;

    public ELResolverBuilder(RuntimeConfig runtimeConfig, MyfacesConfig myfacesConfig)
    {
        this.runtimeConfig = runtimeConfig;
        this.myfacesConfig = myfacesConfig;
    }

    /**
     * add the el resolvers from the faces config, the el resolver wrapper for variable resolver, the el resolver
     * wrapper for the property resolver and the el resolvers added by
     * {@link jakarta.faces.application.Application#addELResolver(ELResolver)}.
     * The resolvers where only added if they are not null
     * 
     * @param resolvers
     */
    protected void addFromRuntimeConfig(List<ELResolver> resolvers)
    {
        if (runtimeConfig.getFacesConfigElResolvers() != null)
        {
            resolvers.addAll(runtimeConfig.getFacesConfigElResolvers());
        }

        if (runtimeConfig.getApplicationElResolvers() != null)
        {
            resolvers.addAll(runtimeConfig.getApplicationElResolvers());
        }
    }
    
    protected List<ELResolver> wrapELResolvers(List<ELResolver> resolvers)
    {
        if (!myfacesConfig.isElResolverTracing() || myfacesConfig.getProjectStage() != ProjectStage.Development)
        {
            return resolvers;
        }

        return resolvers.stream()
                .map(r -> new TracingELResolverWrapper(r))
                .collect(Collectors.toList()); 
    }
    
    /**
     * Sort the ELResolvers with a custom Comparator provided by the user.
     * @param resolvers
     * @since 1.2.10, 2.0.2
     */
    protected void sortELResolvers(List<ELResolver> resolvers)
    {
        if (runtimeConfig.getELResolverComparator() != null)
        {
            try
            {
                // sort the resolvers
                Collections.sort(resolvers, runtimeConfig.getELResolverComparator());
                
                if (log.isLoggable(Level.INFO))
                {
                    log.log(Level.INFO, "Chain of EL resolvers sorted with: {1} and the result order is {2}", 
                            new Object [] { runtimeConfig.getELResolverComparator(), resolvers});
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
     * @return Iterable instance of Iterable containing filtered ELResolvers 
     */
    protected Iterable<ELResolver> filterELResolvers(List<ELResolver> resolvers)
    {
        
        Predicate<ELResolver> predicate = runtimeConfig.getELResolverPredicate();
        if (predicate != null)
        {
            try
            {
                // filter the resolvers
                resolvers.removeIf(elResolver -> !predicate.test(elResolver));

                if (log.isLoggable(Level.INFO))
                {
                    log.log(Level.INFO, "Chain of EL resolvers filtered with: {1} and the result is {2}", 
                            new Object [] {predicate, resolvers});
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
    
    protected boolean isReplaceImplicitObjectResolverWithCDIResolver(FacesContext facesContext)
    {
        if (!ExternalSpecifications.isCDIAvailable(facesContext.getExternalContext()))
        {
            return false;
        }

        BeanManager beanManager = CDIUtils.getBeanManager(facesContext);
        if (beanManager == null)
        {
            return false;
        }

        return true;
    }
    
    
    public void build(CompositeELResolver elResolver)
    {
        build(FacesContext.getCurrentInstance(), elResolver);
    }
    
    public void build(FacesContext facesContext, CompositeELResolver elResolver)
    {
        
    }
}
