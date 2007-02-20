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

import org.apache.myfaces.el.unified.resolver.implicitobject.ImplicitObjectResolver;

/**
 * Provides an ELResolver for Faces.
 *
 * See JSF spec section 5.6.1
 *
 * @author Stan Silvert
 */
public class ResolverForJSP extends CustomResolverBase {
    
    /** Creates a new instance of ResolverForJSP */
    public ResolverForJSP() {
        add(ImplicitObjectResolver.makeResolverForJSP());
        add(new ManagedBeanResolver());
        add(new ResourceBundleResolver());
        addCustomResolvers();
    }
    
}
