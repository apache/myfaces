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
package org.apache.myfaces.config;

import java.util.HashMap;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.ProjectStage;

import org.apache.myfaces.config.impl.digester.elements.ManagedBean;
import org.apache.myfaces.config.impl.digester.elements.ManagedProperty;
import org.apache.myfaces.el.unified.resolver.ManagedBeanResolver;
import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.el.MockValueExpression;
import org.apache.shale.test.mock.MockApplication20;

/**
 * Class used to test ManagedBeanBuilder
 * @author Jakob Korherr (latest modification by $Author$)
 */
public class ManagedBeanBuilderTest extends AbstractJsfTestCase
{

    /**
     * A managed bean used in the test cases
     * @author Jakob Korherr
     */
    public static class TestBean {
        
        private Map<Object, Object> scope;
        private TestBean anotherBean;

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

        public TestBean getAnotherBean()
        {
            return anotherBean;
        }

        public void setAnotherBean(TestBean anotherBean)
        {
            this.anotherBean = anotherBean;
        }
        
    }
    
    private RuntimeConfig runtimeConfig;
    
    public ManagedBeanBuilderTest(String name)
    {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        // override MockApplication20 to get a ProjectStage
        application = new MockApplication20() {

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
     * Tests, if the ManagedBeanBuilder checks that no property of the managed bean
     * references to a scope with a potentially shorter lifetime.
     * E.g. a managed bean of scope session is only allowed to reference an object in
     * the session, the application and the none scope.
     */
    public void testIsInValidScope()
    {
        // create sessionBean referencing requestBean
        ManagedBean sessionBean = new ManagedBean();
        sessionBean.setBeanClass(TestBean.class.getName());
        sessionBean.setName("sessionBean");
        sessionBean.setScope("session");
        ManagedProperty anotherBeanProperty = new ManagedProperty();
        anotherBeanProperty.setPropertyName("anotherBean");
        anotherBeanProperty.setValue("#{requestBean}");
        sessionBean.addProperty(anotherBeanProperty);
        runtimeConfig.addManagedBean("sessionBean", sessionBean);
        
        // create requestBean
        ManagedBean requestBean = new ManagedBean();
        requestBean.setBeanClass(TestBean.class.getName());
        requestBean.setName("requestBean");
        requestBean.setScope("request");
        runtimeConfig.addManagedBean("requestBean", requestBean);
        
        try
        {
            new MockValueExpression("#{sessionBean}", TestBean.class).getValue(facesContext.getELContext());
        }
        catch (FacesException e)
        {
            // success --> the ManagedBeanBuilder discovered the reference to a shorter lifetime
            return;
        }
        fail();
    }
    
    /**
     * Tests the same as testIsInValidScope, but this time the managed bean
     * has a custom scope.
     * The spec says, that if a managed bean has a custom scope, the runtime 
     * is not required to check the references. However, MyFaces checks the
     * references if the ProjectStage is not Production. 
     */
    public void testIsInValidScopeWithCustomScopes()
    {
        // create scopeBean
        ManagedBean scopeBean = new ManagedBean();
        scopeBean.setBeanClass(TestBean.class.getName());
        scopeBean.setName("scopeBean");
        scopeBean.setScope("session");
        runtimeConfig.addManagedBean("scopeBean", scopeBean);
        
        // create sessionBean referencing requestBean
        ManagedBean sessionBean = new ManagedBean();
        sessionBean.setBeanClass(TestBean.class.getName());
        sessionBean.setName("sessionBean");
        sessionBean.setScope("#{scopeBean.scope}");
        ManagedProperty anotherBeanProperty = new ManagedProperty();
        anotherBeanProperty.setPropertyName("anotherBean");
        anotherBeanProperty.setValue("#{requestBean}");
        sessionBean.addProperty(anotherBeanProperty);
        runtimeConfig.addManagedBean("sessionBean", sessionBean);
        
        // create requestBean
        ManagedBean requestBean = new ManagedBean();
        requestBean.setBeanClass(TestBean.class.getName());
        requestBean.setName("requestBean");
        requestBean.setScope("request");
        runtimeConfig.addManagedBean("requestBean", requestBean);
        
        try
        {
            new MockValueExpression("#{sessionBean}", TestBean.class).getValue(facesContext.getELContext());
        }
        catch (FacesException e)
        {
            // success --> the ManagedBeanBuilder discovered the reference to a shorter lifetime
            return;
        }
        fail();
    }

}
