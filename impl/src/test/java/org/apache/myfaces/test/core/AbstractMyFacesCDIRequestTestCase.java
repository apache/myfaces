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
package org.apache.myfaces.test.core;

import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import javax.servlet.ServletContext;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.spi.InjectionProvider;
import org.apache.myfaces.spi.InjectionProviderException;
import org.apache.myfaces.spi.InjectionProviderFactory;
import org.apache.myfaces.spi.impl.CDIAnnotationDelegateInjectionProvider;
import org.apache.myfaces.webapp.AbstractFacesInitializer;

public class AbstractMyFacesCDIRequestTestCase extends AbstractMyFacesRequestTestCase
{

    private Object owbListener;
    protected InjectionProvider injectionProvider;
    
    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.spi.InjectionProvider", 
            CDIAnnotationDelegateInjectionProvider.class.getName());
   }
    
    @Override
    protected void setUpServletListeners() throws Exception
    {
        Class listenerClass = ClassUtils.classForName("org.apache.webbeans.servlet.WebBeansConfigurationListener");
        if (listenerClass == null)
        {
            listenerClass = ClassUtils.classForName("org.jboss.weld.environment.servlet.Listener");
        }
        if (listenerClass != null)
        {
            owbListener = ClassUtils.newInstance(listenerClass);
            webContainer.subscribeListener(owbListener);
        }
        super.setUpServletListeners();
    }

    @Override
    protected void tearDownServletListeners() throws Exception
    {
        super.tearDownServletListeners();
        owbListener = null;
    }

    @Override
    protected AbstractFacesInitializer createFacesInitializer()
    {
        return new CDIJUnitFacesInitializer(this);
    }
    
    protected class CDIJUnitFacesInitializer extends AbstractMyFacesTestCase.JUnitFacesInitializer
    {
        private Object testCaseCreationMetadata;

        public CDIJUnitFacesInitializer(AbstractMyFacesTestCase testCase)
        {
            super(testCase);
        }

        @Override
        protected void initContainerIntegration(ServletContext servletContext, ExternalContext externalContext)
        {
            super.initContainerIntegration(servletContext, externalContext);
            
            InjectionProviderFactory ipf = InjectionProviderFactory.getInjectionProviderFactory();
            injectionProvider = ipf.getInjectionProvider(externalContext);
            AbstractMyFacesTestCase testCase = getTestCase();
            try
            {
                testCaseCreationMetadata = injectionProvider.inject(testCase);
                injectionProvider.postConstruct(testCase, testCaseCreationMetadata);
            }
            catch (InjectionProviderException ex)
            {
                throw new FacesException("Cannot inject JUnit Test case", ex);
            }
        }

        @Override
        public void destroyFaces(ServletContext servletContext)
        {
            try
            {
                injectionProvider.preDestroy(getTestCase(), testCaseCreationMetadata);
            }
            catch (InjectionProviderException ex)
            {
                throw new FacesException("Cannot call @PreDestroy over inject JUnit Test case", ex);
            }
            super.destroyFaces(servletContext);
        }
        
        
    }
}
