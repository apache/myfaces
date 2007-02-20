/*
 * Copyright 2007 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.el.unified.resolver;

import javax.el.CompositeELResolver;
import javax.el.ELResolver;

import org.apache.myfaces.el.convert.PropertyResolverToELResolver;
import org.apache.myfaces.el.convert.VariableResolverToELResolver;

/**
 * Handles custom el resolvers
 * 
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class CustomResolverBase extends CompositeELResolver
{
    protected CompositeELResolver resolversFromAppConfig = new CompositeELResolver();

    protected CompositeELResolver resolversFromLegacyVariableResolvers = new CompositeELResolver();

    protected CompositeELResolver resolversFromLegacyPropertyResolvers = new CompositeELResolver();

    protected CompositeELResolver resolversFromApplicationAddResolver = new CompositeELResolver();

    protected void addCustomResolvers()
    {
        add(resolversFromAppConfig);
        add(resolversFromLegacyVariableResolvers);
        add(resolversFromLegacyPropertyResolvers);
        add(resolversFromApplicationAddResolver);
    }

    // The following methods allow resolvers to be added to the proper position in the
    // chain at any time
    public void addResolverFromAppConfig(ELResolver resolver)
    {
        resolversFromAppConfig.add(resolver);
    }

    // We allow more than one call, but it is expected that this will be 
    // called only once by passing in the fully-constructed VariableResolver chain
    @SuppressWarnings("deprecation")
    public void addResolverFromLegacyVariableResolver(
            javax.faces.el.VariableResolver resolver)
    {
        resolversFromLegacyVariableResolvers
                .add(new VariableResolverToELResolver(resolver));
    }

    @SuppressWarnings("deprecation")
    public void addResolverFromLegacyPropertyResolver(
            javax.faces.el.PropertyResolver resolver)
    {
        resolversFromLegacyPropertyResolvers
                .add(new PropertyResolverToELResolver(resolver));
    }

    public void addResolverFromApplicationAddResolver(ELResolver resolver)
    {
        this.resolversFromApplicationAddResolver.add(resolver);
    }
}
