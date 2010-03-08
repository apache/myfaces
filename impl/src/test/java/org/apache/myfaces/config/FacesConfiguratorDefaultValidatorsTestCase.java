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

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;

import javax.faces.FactoryFinder;
import javax.faces.validator.BeanValidator;

import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.util.ExternalSpecifications;

/**
 * Test cases for the installation of default validators (e.g. BeanValidator).
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @since 2.0
 */
public class FacesConfiguratorDefaultValidatorsTestCase extends AbstractJsfTestCase
{
    
    private FacesConfigurator facesConfigurator;
    
    public FacesConfiguratorDefaultValidatorsTestCase(String name)
    {
        super(name);
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
        
        facesConfigurator = new FacesConfigurator(externalContext);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        facesConfigurator = null;
        
        super.tearDown();
    }

    /**
     * Tests the case that the default bean validator is disabled with the config parameter
     * javax.faces.validator.DISABLE_DEFAULT_BEAN_VALIDATOR, but it is defined in faces-config.xml.
     * In this case the bean validator should be installed as a default validator.
     * The related faces-config.xml is in the sub-folder test1.
     */
    @SuppressWarnings("unchecked")
    public void testDefaultBeanValidatorDisabledButPresentInFacesConfig()
    {
        try
        {
            // We have to reset MockRenderKitFactory before, because the FacesConfigurator
            // will later add HTML_BASIC as a new RenderKit, but MockRenderKitFactory has 
            // this one already installed and so it would throw an Exception.
            MockRenderKitFactory renderKitFactory 
                    = (MockRenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
            Field renderKitsField = MockRenderKitFactory.class.getDeclaredField("renderKits");
            renderKitsField.setAccessible(true);
            renderKitsField.set(renderKitFactory, new HashMap());
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not configure MockRenderKitFactory for test case.", e);
        }
        
        // set the document root for the faces-config.xml
        File documentRoot = new File("src/test/resources/org/apache/myfaces/config/test1");
        servletContext.setDocumentRoot(documentRoot);
        
        // disable the default bean validator via config parameter
        servletContext.setAttribute(BeanValidator.DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME, "true");
        
        // configure BeanValidation to be available
        try
        {
            Field field = ExternalSpecifications.class.getDeclaredField("beanValidationAvailable");
            field.setAccessible(true);
            field.set(ExternalSpecifications.class, true);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not configure BeanValidation for the test case.", e);
        }
            
        // run the configuration procedure
        facesConfigurator.configure();
        
        // the bean validator has to be installed, because of the entry in faces-config.xml
        assertTrue(application.getDefaultValidatorInfo().containsKey(BeanValidator.VALIDATOR_ID));
    }

}
