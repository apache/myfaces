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

import jakarta.faces.FactoryFinder;
import jakarta.faces.validator.BeanValidator;
import jakarta.faces.validator.LengthValidator;
import jakarta.faces.validator.RequiredValidator;
import jakarta.faces.webapp.FacesServlet;
import org.apache.myfaces.application.ApplicationFactoryImpl;

import org.apache.myfaces.test.base.junit.AbstractFacesConfigurableMockTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.util.ExternalSpecifications;
import org.apache.myfaces.util.lang.Lazy;
import org.apache.myfaces.view.ViewDeclarationLanguageFactoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for the installation of default validators (e.g. BeanValidator).
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @since 2.0
 */
public class FacesConfiguratorDefaultValidatorsTestCase extends AbstractFacesConfigurableMockTestCase
{
    private FacesConfigurator facesConfigurator;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        
        facesConfigurator = new FacesConfigurator(externalContext);
    }
    
    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        facesConfigurator = null;
        
        super.tearDown();
    }

    @Override
    public void setFactories() throws Exception
    {
        super.setFactories();
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY,
                ApplicationFactoryImpl.class.getName());
        FactoryFinder.setFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY,
                ViewDeclarationLanguageFactoryImpl.class.getName());
    }
    
    /**
     * We have to reset MockRenderKitFactory before, because the FacesConfigurator
     * will later add HTML_BASIC as a new RenderKit, but MockRenderKitFactory has 
     * this one already installed and so it would throw an Exception.
     */
    @SuppressWarnings("unchecked")
    private void _cleanRenderKits()
    {
        try
        {
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
    }
    
    /**
     * Sets the cached field in ExternalSpecifications to enable or 
     * disable bean validation for testing purposes.
     * 
     * @param available
     */
    private void _setBeanValidationAvailable(boolean available)
    {
        try
        {
            Field field = ExternalSpecifications.class.getDeclaredField("beanValidationAvailable");
            field.setAccessible(true);
            field.set(ExternalSpecifications.class, new Lazy<>(() ->
            {
                return available;
            }));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not configure BeanValidation for the test case.", e);
        }
    }

    /**
     * Tests the case that the default bean validator is disabled with the config parameter
     * jakarta.faces.validator.DISABLE_DEFAULT_BEAN_VALIDATOR, but it is defined in the faces-config.
     * In this case the bean validator should be installed as a default validator.
     */
    @Test
    public void testDefaultBeanValidatorDisabledButPresentInFacesConfig()
    {
        // remove existing RenderKit installations
        _cleanRenderKits();
        
        // set the document root for the config file
        File documentRoot = new File("src/test/resources/org/apache/myfaces/config/testfiles");
        servletContext.setDocumentRoot(documentRoot);
        
        // set the right faces-config file
        servletContext.addInitParameter(FacesServlet.CONFIG_FILES_ATTR, 
                "/default-bean-validator.xml");
        
        // disable the default bean validator via config parameter
        // thus it will not be added programmatically
        servletContext.addInitParameter(
                BeanValidator.DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME, "true");
        
        // configure BeanValidation to be available
        _setBeanValidationAvailable(true);
            
        // run the configuration procedure
        facesConfigurator.configure();
        
        // the bean validator has to be installed, because 
        // of the entry in default-bean-validator.xml
        Assertions.assertTrue(application.getDefaultValidatorInfo()
                .containsKey(BeanValidator.VALIDATOR_ID));
    }
    
    /**
     * Tests the case when bean validation is available, but adding the BeanValidator
     * as a default validator has been disabled via the config parameter and there
     * is no faces-config file that would install it manually.
     * In this case the BeanValidator mustn't be installed.
     */
    @Test
    public void testDefaultBeanValidatorDisabled()
    {
        // remove existing RenderKit installations
        _cleanRenderKits();
        
        // set the document root for the config file
        File documentRoot = new File("src/test/resources/org/apache/myfaces/config/testfiles");
        servletContext.setDocumentRoot(documentRoot);
        
        // disable the default bean validator via config parameter
        // thus it will not be added programmatically
        servletContext.addInitParameter(
                BeanValidator.DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME, "true");
        
        // configure BeanValidation to be available
        _setBeanValidationAvailable(true);
            
        // run the configuration procedure
        facesConfigurator.configure();
        
        // the bean validator must not be installed, because it
        // has been disabled and there is no entry in the faces-config
        // that would install it manually.
        Assertions.assertFalse(application.getDefaultValidatorInfo()
                .containsKey(BeanValidator.VALIDATOR_ID));
    }
    
    /**
     * Tests the case that bean validation is not available in the classpath.
     * In this case the BeanValidator must not be installed.
     */
    @Test
    public void testBeanValidationNotAvailable()
    {
        // remove existing RenderKit installations
        _cleanRenderKits();
        
        // set the document root for the config file
        File documentRoot = new File("src/test/resources/org/apache/myfaces/config/testfiles");
        servletContext.setDocumentRoot(documentRoot);
        
        // configure BeanValidation to be not available
        _setBeanValidationAvailable(false);
            
        // run the configuration procedure
        facesConfigurator.configure();
        
        // the bean validator mustn't be installed, because
        // bean validation is not available in the classpath
        Assertions.assertFalse(application.getDefaultValidatorInfo()
                .containsKey(BeanValidator.VALIDATOR_ID));
    }
    
    /**
     * Tests the case with two config files. The first one would install the a
     * default validator (in this case the RequiredValidator), but the second one 
     * specifies an empty default-validators element, which overrules the first one
     * and cleares all existing default-validators.
     * In this case the RequiredValidator must not be installed, however the BeanValidator
     * has to be installed (automatically) since bean validation is available.
     */
    @Test
    public void testDefaultValidatorsClearedByLatterConfigFileWithEmptyElement()
    {
        // remove existing RenderKit installations
        _cleanRenderKits();
        
        // set the document root for the config files
        File documentRoot = new File("src/test/resources/org/apache/myfaces/config/testfiles");
        servletContext.setDocumentRoot(documentRoot);
        
        // set the right faces-config files.
        // we want that default-required-validator.xml is feeded before
        // empty-default-validators.xml and since the FacesConfigurator
        // will change the order when no ordering information is present, 
        // we have to specify them the other way round!
        servletContext.addInitParameter(FacesServlet.CONFIG_FILES_ATTR, 
                "/empty-default-validators.xml,/default-required-validator.xml");
        
        // configure BeanValidation to be available
        _setBeanValidationAvailable(true);
        
        // run the configuration procedure
        facesConfigurator.configure();
        
        // the required validator must not be installed, because the latter config file
        // (empty-default-validators.xml) has an empty default validators element
        // and this cleares all existing default-validators.
        Assertions.assertFalse(application.getDefaultValidatorInfo().containsKey(RequiredValidator.VALIDATOR_ID));
        
        // and since bean validation is available, the BeanValidator has to be installed
        Assertions.assertTrue(application.getDefaultValidatorInfo().containsKey(BeanValidator.VALIDATOR_ID));
    }
    
    /**
     * Tests the case with two config files. The first one installs a
     * default validator (in this case the RequiredValidator), and the 
     * second one does not specify any default-validators element.
     * In this case the RequiredValidator must be installed, because the latter
     * config file does not specify and default-validator information. Furthermore the 
     * BeanValidator must also be installed since bean validation is available.
     */
    @Test
    public void testDefaultValidatorsNotClearedByLatterConfigFileWithNoElement()
    {
        // remove existing RenderKit installations
        _cleanRenderKits();
        
        // set the document root for the config files
        File documentRoot = new File("src/test/resources/org/apache/myfaces/config/testfiles");
        servletContext.setDocumentRoot(documentRoot);
        
        // set the right faces-config files.
        // we want that default-required-validator.xml is feeded before
        // no-default-validators.xml and since the FacesConfigurator
        // will change the order when no ordering information is present, 
        // we have to specify them the other way round!
        servletContext.addInitParameter(FacesServlet.CONFIG_FILES_ATTR, 
                "/no-default-validators.xml,/default-required-validator.xml");
        
        // configure BeanValidation to be available
        _setBeanValidationAvailable(true);
        
        // run the configuration procedure
        facesConfigurator.configure();
        
        // the required validator must be installed, because the latter config file
        // (no-default-validators.xml) has not got a default validators element.
        Assertions.assertTrue(application.getDefaultValidatorInfo().containsKey(RequiredValidator.VALIDATOR_ID));
        
        // and since bean validation is available, the BeanValidator has to be installed
        Assertions.assertTrue(application.getDefaultValidatorInfo().containsKey(BeanValidator.VALIDATOR_ID));
    }
    
    /**
     * Tests the case with two config files. The first one would install a
     * default validator (in this case the RequiredValidator), but the second one 
     * also specifies default-validators. This overrules the first config and thus 
     * cleares all existing default-validators and adds its default-validators.
     * In this case the RequiredValidator must not be installed, however the 
     * LengthValidator has to be installed (and the BeanValidator must not be installed
     * since bean validation is not available).
     */
    @Test
    public void testDefaultValidatorsOverwrittenByLatterConfigFile()
    {
        // remove existing RenderKit installations
        _cleanRenderKits();
        
        // set the document root for the config files
        File documentRoot = new File("src/test/resources/org/apache/myfaces/config/testfiles");
        servletContext.setDocumentRoot(documentRoot);
        
        // set the right faces-config files.
        // we want that default-required-validator.xml is feeded before
        // default-length-validator.xml and since the FacesConfigurator
        // will change the order when no ordering information is present, 
        // we have to specify them the other way round!
        servletContext.addInitParameter(FacesServlet.CONFIG_FILES_ATTR, 
                "/default-length-validator.xml,/default-required-validator.xml");
        
        // configure BeanValidation to be not available
        _setBeanValidationAvailable(false);
        
        // run the configuration procedure
        facesConfigurator.configure();
        
        // the required validator must not be installed, because the latter config file
        // (default-length-validator.xml) specifies a default validators element
        // and this cleares all existing default-validators.
        Assertions.assertFalse(application.getDefaultValidatorInfo().containsKey(RequiredValidator.VALIDATOR_ID));
        
        // the length validator has to be installed, because it was installed
        // by the latter config file
        Assertions.assertTrue(application.getDefaultValidatorInfo().containsKey(LengthValidator.VALIDATOR_ID));
        
        // and since bean validation is not available, the BeanValidator must not be installed
        Assertions.assertFalse(application.getDefaultValidatorInfo().containsKey(BeanValidator.VALIDATOR_ID));
    }

}
