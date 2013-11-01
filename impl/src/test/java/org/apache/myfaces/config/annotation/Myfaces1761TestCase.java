/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.config.annotation;

import org.apache.myfaces.config.ManagedBeanBuilder;
import org.apache.myfaces.config.impl.digester.elements.ManagedBeanImpl;
import org.apache.myfaces.config.impl.digester.elements.ManagedPropertyImpl;
import org.apache.myfaces.test.base.AbstractJsfTestCase;

/**
 * Test MYFACES-1761 Handling PostConstruct annotations - wrong order 
 * 
 * @author Leonardo Uribe
 *
 */
public class Myfaces1761TestCase extends AbstractJsfTestCase
{

    protected ManagedBeanBuilder managedBeanBuilder;
    protected LifecycleProvider2 lifecycleProvider;
    protected ManagedBeanImpl beanConfiguration;
    
    private static final String TEST_LIFECYCLE_PROVIDER = MockLifecycleProvider2.class.getName();
    
    protected static final String INJECTED_VALUE = "tatiana";
    
    public Myfaces1761TestCase(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        managedBeanBuilder  = new ManagedBeanBuilder();
        
        beanConfiguration = new ManagedBeanImpl();        
        beanConfiguration.setBeanClass(AnnotatedManagedBean2.class.getName());
        beanConfiguration.setName("managed");
        beanConfiguration.setScope("request");
        
        ManagedPropertyImpl managedProperty = new ManagedPropertyImpl();
        managedProperty.setPropertyName("managedProperty");
        managedProperty.setValue(INJECTED_VALUE);
        beanConfiguration.addProperty(managedProperty);
        
        LifecycleProviderFactory.getLifecycleProviderFactory(externalContext).release();
        servletContext.addInitParameter(DefaultLifecycleProviderFactory.LIFECYCLE_PROVIDER, TEST_LIFECYCLE_PROVIDER);
    }

    public void tearDown() throws Exception
    {
        LifecycleProviderFactory.getLifecycleProviderFactory(externalContext).release();
        super.tearDown();
        managedBeanBuilder = null;
    }
    
    public void testPostConstruct() throws Exception
    {
        AnnotatedManagedBean2 bean = (AnnotatedManagedBean2) managedBeanBuilder.buildManagedBean(facesContext, beanConfiguration);
        assertEquals(INJECTED_VALUE, bean.getManagedProperty());
        assertTrue(bean.isPostConstructCalled());
    }
}
