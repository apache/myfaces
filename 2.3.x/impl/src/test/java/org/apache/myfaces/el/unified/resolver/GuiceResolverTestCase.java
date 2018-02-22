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

package org.apache.myfaces.el.unified.resolver;

import javax.el.ELResolver;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.impl.digester.elements.ManagedBeanImpl;
import org.apache.myfaces.test.base.AbstractJsfTestCase;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class GuiceResolverTestCase extends AbstractJsfTestCase {

    public GuiceResolverTestCase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        
        super.setUp();
        
        // simulate a ServletContextListener
        Injector injector = Guice.createInjector(new ShoppingModule());
        servletContext.setAttribute(GuiceResolver.KEY, injector);
        
        // simulate Myfaces starting up
        RuntimeConfig rc = RuntimeConfig.getCurrentInstance(externalContext);
        ManagedBeanImpl bean = new ManagedBeanImpl();
        bean.setBeanClass(ShoppingCart.class.getName());
        bean.setScope("request");
        rc.addManagedBean("shoppingCart", bean);
        
    }

    public void testResolve() {
        
        ELResolver resolver = new GuiceResolver();
        
        ShoppingCart cart = (ShoppingCart) resolver.getValue(facesContext.getELContext(), ((Object)null), ((Object)"shoppingCart"));
        
        assertNotNull(cart);
        
        assertEquals(new BulkOrder().toString(), cart.getOrder().toString());
        
        cart = (ShoppingCart) resolver.getValue(facesContext.getELContext(), ((Object)null), ((Object)"XXXshoppingCart"));
        
        assertNull(cart);
    }
    
}
