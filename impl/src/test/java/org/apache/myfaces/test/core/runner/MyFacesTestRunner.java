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
package org.apache.myfaces.test.core.runner;

import java.lang.reflect.Field;
import java.util.List;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.test.core.annotation.TestContainer;
import org.apache.myfaces.spi.InjectionProvider;
import org.apache.myfaces.spi.InjectionProviderFactory;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

/**
 *
 */
public class MyFacesTestRunner extends BlockJUnit4ClassRunner
{

    public MyFacesTestRunner(Class<?> klass) throws InitializationError
    {
        super(klass);
        
        // Annotation processing goes here.
    }

    @Override
    protected Statement withAfters(FrameworkMethod method, Object target, Statement statement)
    {
        return new ContainerAwareMethodInvoker(getTestClass(), method, 
            super.withAfters(method, target, statement), target);
    }

    @Override
    protected Statement withAfterClasses(Statement statement)
    {
        return new ClearSharedFacesConfiguration(statement, getTestClass());
    }

    private static class ContainerAwareMethodInvoker extends Statement
    {
        private final TestClass testClass;
        private final FrameworkMethod method;
        private final Object originalTarget;
        private final Statement defaultStatement;

        public ContainerAwareMethodInvoker(TestClass testClass,
            FrameworkMethod method, Statement defaultStatement, Object originalTarget)
        {
            this.testClass = testClass;
            this.method = method;
            this.defaultStatement = defaultStatement;
            this.originalTarget = originalTarget;
        }
        
        @Override
        public void evaluate() throws Throwable
        {
            MyFacesContainer currentTestContext = new MyFacesContainer(testClass);

            //Inject MyFacesContainer using @TestContainer
            List<FrameworkField> fields = testClass.getAnnotatedFields(TestContainer.class);
            if (fields != null && !fields.isEmpty())
            {
                for (FrameworkField field : fields)
                {
                    Field f = field.getField();
                    if (f.getType().equals(MyFacesContainer.class))
                    {
                        f.setAccessible(true);
                        f.set(originalTarget, currentTestContext);
                    }
                }
            }
            
            currentTestContext.setUp(this.originalTarget);
            
            FacesContext facesContext = null;
            InjectionProvider injectionProvider = null;
            Object testCaseCreationMetadata = null;
            try
            {
                facesContext = currentTestContext.getFacesInitializer().
                    initStartupFacesContext(currentTestContext.servletContext);
                
                InjectionProviderFactory ipf = InjectionProviderFactory.
                    getInjectionProviderFactory(facesContext.getExternalContext());
                injectionProvider = ipf.getInjectionProvider(
                    facesContext.getExternalContext());
                
                if (injectionProvider != null)
                {
                    testCaseCreationMetadata = injectionProvider.inject(originalTarget);
                    injectionProvider.postConstruct(originalTarget, testCaseCreationMetadata);
                }
            }
            finally
            {
                currentTestContext.getFacesInitializer().destroyStartupFacesContext(facesContext);
            }
            try
            {
                defaultStatement.evaluate();
            }
            finally
            {
                facesContext = currentTestContext.getFacesInitializer().
                    initShutdownFacesContext(currentTestContext.servletContext);

                if (injectionProvider != null)
                {
                    injectionProvider.preDestroy(originalTarget, testCaseCreationMetadata);
                }
                
                currentTestContext.getFacesInitializer().destroyShutdownFacesContext(facesContext);
                
                currentTestContext.tearDown();
            }
        }
    }
    
    private static class ClearSharedFacesConfiguration extends Statement
    {
        private final Statement defaultStatement;
        private final TestClass testClass;

        public ClearSharedFacesConfiguration(Statement defaultStatement, TestClass testClass)
        {
            this.defaultStatement = defaultStatement;
            this.testClass = testClass;
        }

        @Override
        public void evaluate() throws Throwable
        {
            defaultStatement.evaluate();
            
            AbstractJsfTestContainer.tearDownClass(testClass.getJavaClass());
        }
        
    }

}
