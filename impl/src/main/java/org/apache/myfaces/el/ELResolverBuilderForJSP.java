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

import org.apache.myfaces.el.resolver.FlashELResolver;
import java.util.ArrayList;
import java.util.List;

import javax.el.CompositeELResolver;
import javax.el.ELResolver;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.config.MyfacesConfig;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.el.resolver.FacesCompositeELResolver.Scope;
import org.apache.myfaces.el.resolver.ResourceBundleResolver;
import org.apache.myfaces.el.resolver.ResourceResolver;
import org.apache.myfaces.el.resolver.implicitobject.ImplicitObjectResolver;

/**
 * build the el resolver for jsp. see 1.2 spec section 5.6.1
 * 
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ELResolverBuilderForJSP extends ELResolverBuilder
{
    public ELResolverBuilderForJSP(RuntimeConfig runtimeConfig, MyfacesConfig myfacesConfig)
    {
        super(runtimeConfig, myfacesConfig);
    }

    @Override
    public void build(FacesContext facesContext, CompositeELResolver compositeElResolver)
    {
        // add the ELResolvers to a List first to be able to sort them
        List<ELResolver> list = new ArrayList<ELResolver>();
        
        if (isReplaceImplicitObjectResolverWithCDIResolver(facesContext))
        {
            //Add CDI ELResolver instead.
            BeanManager beanManager = CDIUtils.getBeanManager(
                    FacesContext.getCurrentInstance().getExternalContext());
            list.add(beanManager.getELResolver());
        }
        else
        {        
            list.add(ImplicitObjectResolver.makeResolverForJSP());
        }
        
        //Flash object is instanceof Map, so it is necessary to resolve
        //before MapELResolver. Better to put this one before
        list.add(new FlashELResolver());
        list.add(new ResourceBundleResolver());
        list.add(new ResourceResolver());

        addFromRuntimeConfig(list);
        
        // give the user a chance to sort the resolvers
        sortELResolvers(list, Scope.JSP);
        
        // give the user a chance to filter the resolvers
        Iterable<ELResolver> filteredELResolvers = filterELResolvers(list, Scope.JSP);
        
        // add the resolvers from the list to the CompositeELResolver
        for (ELResolver resolver : filteredELResolvers)
        {
            compositeElResolver.add(resolver);
        }
    }

}
