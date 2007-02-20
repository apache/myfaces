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
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;

import org.apache.myfaces.el.unified.resolver.implicitobject.ImplicitObjectResolver;

/**
 * Provides an ELResolver for Faces.
 *
 * See JSF spec section 5.6.2
 *
 * @author Stan Silvert
 */
public class ResolverForFaces extends CustomResolverBase {
    
    /** Creates a new instance of ResolverForFaces */
    public ResolverForFaces() {
        add(ImplicitObjectResolver.makeResolverForFaces());
        addCustomResolvers();
        add(new ManagedBeanResolver());
        add(new ResourceBundleELResolver());
        add(new ResourceBundleResolver());
        add(new MapELResolver());
        add(new ListELResolver());
        add(new ArrayELResolver());
        add(new BeanELResolver());
        add(new ScopedAttributeResolver());        
    }
}
