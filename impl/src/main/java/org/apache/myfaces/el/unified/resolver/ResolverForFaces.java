/*
 * Copyright 2006 The Apache Software Foundation.
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

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELResolver;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.faces.el.PropertyResolver;
import javax.faces.el.VariableResolver;
import org.apache.myfaces.el.convert.PropertyResolverToELResolver;
import org.apache.myfaces.el.convert.VariableResolverToELResolver;
import org.apache.myfaces.el.unified.resolver.implicitobject.ImplicitObjectResolver;
import org.apache.myfaces.el.unified.resolver.ManagedBeanResolver;
import org.apache.myfaces.el.unified.resolver.ResourceBundleResolver;
import org.apache.myfaces.el.unified.resolver.ScopedAttributeResolver;

/**
 * Provides an ELResolver for Faces.
 *
 * See JSF spec section 5.6.2
 *
 * @author Stan Silvert
 */
public class ResolverForFaces extends CompositeELResolver {
    
    protected CompositeELResolver resolversFromAppConfig 
                                                    = new CompositeELResolver();
    
    protected CompositeELResolver resolversFromLegacyVariableResolvers
                                                    = new CompositeELResolver();
    
    protected CompositeELResolver resolversFromLegacyPropertyResolvers
                                                    = new CompositeELResolver();
    
    protected CompositeELResolver resolversFromApplicationAddResolver
                                                    = new CompositeELResolver();
    
    /** Creates a new instance of ResolverForFaces */
    public ResolverForFaces() {
        add(ImplicitObjectResolver.makeResolverForFaces());
        
        CompositeELResolver compResolver = new CompositeELResolver();
        compResolver.add(resolversFromAppConfig);
        compResolver.add(resolversFromLegacyVariableResolvers);
        compResolver.add(resolversFromLegacyPropertyResolvers);
        compResolver.add(resolversFromApplicationAddResolver);
        
        add(compResolver);
        add(new ManagedBeanResolver());
        add(new ResourceBundleELResolver());
        add(new ResourceBundleResolver());
        add(new MapELResolver());
        add(new ListELResolver());
        add(new ArrayELResolver());
        add(new BeanELResolver());
        add(new ScopedAttributeResolver());
        
    }
    
    // The following methods allow resolvers to be added to the proper position in the
    // chain at any time
    public void addResolverFromAppConfig(ELResolver resolver) {
        //TODO: implement
        throw new RuntimeException("Not implemented yet");
    }
    
    // We allow more than one call, but it is expected that this will be 
    // called only once by passing in the fully-constructed VariableResolver chain
    public void addResolverFromLegacyVariableResolver(VariableResolver resolver) {
        resolversFromLegacyPropertyResolvers.add(new VariableResolverToELResolver(resolver));
    }
    
    public void addResolverFromLegacyPropertyResolver(PropertyResolver resolver) {
        resolversFromLegacyPropertyResolvers.add(new PropertyResolverToELResolver(resolver));
    }
    
    public void addResolverFromApplicationAddResolver(ELResolver resolver) {
        this.resolversFromApplicationAddResolver.add(resolver);
    }
    
}
