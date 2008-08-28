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

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.el.unified.resolver.ManagedBeanResolver;
import org.apache.myfaces.el.unified.resolver.ResourceBundleResolver;
import org.apache.myfaces.el.unified.resolver.ScopedAttributeResolver;
import org.apache.myfaces.el.unified.resolver.implicitobject.ImplicitObjectResolver;

/**
 * Create the el resolver for faces. see 1.2 spec section 5.6.2
 * 
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public class ResolverBuilderForFaces extends ResolverBuilderBase implements ELResolverBuilder
{
    public ResolverBuilderForFaces(RuntimeConfig config)
    {
        super(config);
    }

    public void build(CompositeELResolver elResolver)
    {
        elResolver.add(ImplicitObjectResolver.makeResolverForFaces());

        addFromRuntimeConfig(elResolver);

        elResolver.add(new ManagedBeanResolver());
        elResolver.add(new ResourceBundleELResolver());
        elResolver.add(new ResourceBundleResolver());
        elResolver.add(new MapELResolver());
        elResolver.add(new ListELResolver());
        elResolver.add(new ArrayELResolver());
        elResolver.add(new BeanELResolver());
        elResolver.add(new ScopedAttributeResolver());
    }

}
