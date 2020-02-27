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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.application.ProjectStage;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.impl.digester.elements.ManagedBeanImpl;
import org.apache.myfaces.test.mock.MockApplication;
import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.apache.myfaces.test.el.MockValueExpression;

/**
 * Class used to test ManagedBeanResolver
 * @author Jakob Korherr (latest modification by $Author$)
 */
public class ManagedBeanResolverTest extends AbstractJsfTestCase
{
    
    /**
     * A managed bean used in the test cases
     * @author Jakob Korherr
     */
    public static class TestBean {
        
        private Map<Object, Object> scope;

        public Map<Object, Object> getScope()
        {
            if (scope == null)
            {
                scope = new HashMap<Object, Object>();
            }
            return scope;
        }

        public void setScope(Map<Object, Object> scope)
        {
            this.scope = scope;
        }
        
    }
    
    private RuntimeConfig runtimeConfig;
    
    public ManagedBeanResolverTest(String name)
    {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        // override MockApplication20 to get a ProjectStage
        application = new MockApplication() {

            @Override
            public ProjectStage getProjectStage()
            {
                return ProjectStage.Development;
            }
            
        };
        // add the ManagedBeanResolver as a ELResolver
        ManagedBeanResolver resolver = new ManagedBeanResolver();
        application.addELResolver(resolver);
        facesContext.setApplication(application);
        
        runtimeConfig = RuntimeConfig.getCurrentInstance(externalContext);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        runtimeConfig = null;
        
        super.tearDown();
    }

    /**
     * Tests if the ManagedBeanResolver handles custom scopes correctly
     */
    @SuppressWarnings("unchecked")
    public void testCustomScope()
    {
        // create the custom scope
        ManagedBeanImpl scopeBean = new ManagedBeanImpl();
        scopeBean.setBeanClass(HashMap.class.getName());
        scopeBean.setName("scopeBean");
        scopeBean.setScope("application");
        runtimeConfig.addManagedBean("scopeBean", scopeBean);
        
        // create the managed bean
        ManagedBeanImpl beanInCustomScope = new ManagedBeanImpl();
        beanInCustomScope.setBeanClass(ArrayList.class.getName());
        beanInCustomScope.setName("beanInCustomScope");
        beanInCustomScope.setScope("#{scopeBean}");
        runtimeConfig.addManagedBean("beanInCustomScope", beanInCustomScope);
        
        // resolve the managed bean
        Object resolvedBeanInCustomScope = new MockValueExpression("#{beanInCustomScope}", List.class)
                                                   .getValue(facesContext.getELContext());
        
        // get the custom scope
        Map resolvedScopeBean = (Map) new MockValueExpression("#{scopeBean}", Map.class)
                                              .getValue(facesContext.getELContext());
        
        // the custom scope has to contain the resolved bean
        assertTrue(resolvedScopeBean.containsKey("beanInCustomScope"));
        assertTrue(resolvedScopeBean.get("beanInCustomScope").equals(resolvedBeanInCustomScope));
    }
    
    /**
     * Tests if the ManagedBeanResolver throws the right Exception, if a custom scope
     * does not evaluate to java.util.Map.
     * Spec says: If the ValueExpression does not evaluate to a Map, a FacesException 
     * must be thrown with a message that includes the expression string, the toString() 
     * of the value, and the type of the value.
     */
    public void testCustomScopeNoMap()
    {
        // create the custom scope
        ManagedBeanImpl scopeBean = new ManagedBeanImpl();
        // Scope is ArrayList instead of HashMap
        scopeBean.setBeanClass(ArrayList.class.getName()); 
        scopeBean.setName("scopeBean");
        scopeBean.setScope("application");
        runtimeConfig.addManagedBean("scopeBean", scopeBean);
        
        // create the managed bean
        ManagedBeanImpl beanInCustomScope = new ManagedBeanImpl();
        beanInCustomScope.setBeanClass(ArrayList.class.getName());
        beanInCustomScope.setName("beanInCustomScope");
        beanInCustomScope.setScope("#{scopeBean}");
        runtimeConfig.addManagedBean("beanInCustomScope", beanInCustomScope);
        
        // resolve the managed bean
        try
        {
            new MockValueExpression("#{beanInCustomScope}", List.class)
                    .getValue(facesContext.getELContext());
        }
        catch (FacesException fe)
        {
            // the message must contain ...
            final String message = fe.getMessage();
            // ... the expression string 
            assertTrue(message.contains(beanInCustomScope.getManagedBeanScope()));
            Object resolvedScopeBean = new MockValueExpression("#{scopeBean}", List.class)
                                               .getValue(facesContext.getELContext());
            // ... the toString() of the value
            assertTrue(message.contains(resolvedScopeBean.toString()));
            // ... and the type of the value
            assertTrue(message.contains(resolvedScopeBean.getClass().getName()));
            return;
        }
        // No FacesException was thrown
        fail();
    }
    
    /**
     * Tests if the ManagedBeanResolver detects cyclic references in custom scopes.
     * The ManagedBeanResolver only tries to detect cyclic references if ProjectStage != Production.
     */
    public void testCustomScopeCyclicReferences()
    {
        // create m1
        ManagedBeanImpl m1 = new ManagedBeanImpl();
        m1.setBeanClass(TestBean.class.getName());
        m1.setName("m1");
        m1.setScope("#{m2.scope}");
        runtimeConfig.addManagedBean("m1", m1);
        
        // create m2
        ManagedBeanImpl m2 = new ManagedBeanImpl();
        m2.setBeanClass(TestBean.class.getName());
        m2.setName("m2");
        m2.setScope("#{m1.scope}");
        runtimeConfig.addManagedBean("m2", m2);
        
        // try to resolve m1
        try
        {
            new MockValueExpression("#{m1}", TestBean.class).getValue(facesContext.getELContext());
        }
        catch (ELException e)
        {
            // success
            return;
        }
        fail();
    }
    
    /**
     * Tests the view scope, which was introduced in jsf 2.0
     */
    public void testViewScope()
    {
        // create the managed bean
        ManagedBeanImpl beanInViewScope = new ManagedBeanImpl();
        beanInViewScope.setBeanClass(ArrayList.class.getName());
        beanInViewScope.setName("beanInViewScope");
        beanInViewScope.setScope("view");
        runtimeConfig.addManagedBean("beanInViewScope", beanInViewScope);
        
        // resolve the managed bean
        Object resolvedBeanInCustomScope = new MockValueExpression("#{beanInViewScope}", List.class)
                                                   .getValue(facesContext.getELContext());
        
        // get the view map
        Map<String, Object> viewMap = facesContext.getViewRoot().getViewMap();
        
        // the custom scope has to contain the resolved bean
        assertTrue(viewMap.containsKey("beanInViewScope"));
        assertTrue(viewMap.get("beanInViewScope").equals(resolvedBeanInCustomScope));
    }

}
