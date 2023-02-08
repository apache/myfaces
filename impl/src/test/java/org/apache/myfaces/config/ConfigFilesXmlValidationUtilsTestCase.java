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

import java.net.URL;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author lu4242
 */
public class ConfigFilesXmlValidationUtilsTestCase extends AbstractJsfTestCase
{

    @Test
    public void testCurrentStandardJSFFacesConfig() throws Exception
    {
        URL url = getClass().getResource("/META-INF/standard-faces-config.xml");
        ConfigFilesXmlValidationUtils.validateFacesConfigFile(
            url , externalContext, ConfigFilesXmlValidationUtils.getFacesConfigVersion(url));
    }

    @Test
    public void testJSF11Config1() throws Exception
    {
        URL url = getClass().getResource("a-config.xml");
        ConfigFilesXmlValidationUtils.validateFacesConfigFile(
            url , externalContext, ConfigFilesXmlValidationUtils.getFacesConfigVersion(url));
    }

    @Test
    public void testJSFInvalidConfig1() throws Exception
    {
        try
        {
            URL url = getClass().getResource("invalid-config_1.xml");
            ConfigFilesXmlValidationUtils.validateFacesConfigFile(
                url , externalContext, ConfigFilesXmlValidationUtils.getFacesConfigVersion(url));
            Assertions.fail();
        }
        catch (SAXException e)
        {
            // expected
        }
    }

    /*
     *  The four testJSFXXConfig tests below were added under MYFACES-4363
     */
     @Test
     public void testJSF22Config() throws Exception
     {
         URL url = getClass().getResource("jsf22-faces-config.xml");
         ConfigFilesXmlValidationUtils.validateFacesConfigFile(
             url , externalContext, ConfigFilesXmlValidationUtils.getFacesConfigVersion(url));
     }

     @Test
     public void testJSF23Config() throws Exception
     {
         URL url = getClass().getResource("jsf23-faces-config.xml");
         ConfigFilesXmlValidationUtils.validateFacesConfigFile(
             url , externalContext, ConfigFilesXmlValidationUtils.getFacesConfigVersion(url));
     }

     @Test
     public void testJSF30Config() throws Exception
     {
         URL url = getClass().getResource("jsf30-faces-config.xml");
         ConfigFilesXmlValidationUtils.validateFacesConfigFile(
             url , externalContext, ConfigFilesXmlValidationUtils.getFacesConfigVersion(url));
     }

     @Test
     public void testJSF40Config() throws Exception
     {
         URL url = getClass().getResource("jsf40-faces-config.xml");
         ConfigFilesXmlValidationUtils.validateFacesConfigFile(
             url , externalContext, ConfigFilesXmlValidationUtils.getFacesConfigVersion(url));
     }

	/*
    @Test(expected = SAXException.class)
    public void testJSFInvalidConfig2() throws Exception
    {
        URL url = getClass().getResource("invalid-config_2.xml");
        ConfigFilesXmlValidationUtils.validateFacesConfigFile(
            url , externalContext, ConfigFilesXmlValidationUtils.getFacesConfigVersion(url));
    }*/
}
