/*
 * Copyright 2004-2006 The Apache Software Foundation.
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


package org.apache.myfaces.config;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.List;

import org.apache.myfaces.config.element.NavigationRule;
import org.apache.myfaces.config.impl.FacesConfigDispenserImpl;
import org.apache.myfaces.config.impl.FacesConfigUnmarshallerImpl;
import org.apache.myfaces.config.impl.element.FacesConfigImpl;
import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.junit.Assert;

public class FacesConfigValidatorTestCase extends AbstractJsfTestCase
{
    private FacesConfigDispenser dispenser;
    private FacesConfigUnmarshaller<FacesConfigImpl> unmarshaller;

    public void setUp() throws Exception
    {

        super.setUp();
        
        dispenser = new FacesConfigDispenserImpl();
        unmarshaller = new FacesConfigUnmarshallerImpl(externalContext);
        try
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(testFacesConfig.getBytes());
            dispenser.feed(unmarshaller.getFacesConfig(bais, null));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
    }
    
    public void testVerifyExistence(){
        
        Collection<NavigationRule> navRules = dispenser.getNavigationRules();
        
        List<String> list = FacesConfigValidator.validate(navRules, externalContext);
        
        int expected = 2;
        
        Assert.assertTrue(list.size() + " should equal " + expected, list.size() == expected);
        
    }
    
    private static final String testFacesConfig =
        "<?xml version='1.0' encoding='UTF-8'?>" +
        "<!DOCTYPE faces-config PUBLIC " +
            "\"-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.1//EN\" " +
            "\"http://java.sun.com/dtd/web-facesconfig_1_1.dtd\">" +
            "<faces-config>" +
            "<navigation-rule>" +
            "    <from-view-id>/doesNotExist.jsp</from-view-id>" +
            "    <navigation-case>" +
            "        <from-outcome>doesNotMatter</from-outcome>" +
            "        <to-view-id>/doesNotExist2.jsp</to-view-id>" +
            "    </navigation-case>" +
            "</navigation-rule>" +
            "<managed-bean>" +
            "    <managed-bean-name>exist</managed-bean-name>" +
            "    <managed-bean-class>org.apache.myfaces.config.FacesConfigValidatorTestCase</managed-bean-class>" +
            "    <managed-bean-scope>request</managed-bean-scope>" +
            "</managed-bean>" +
            "<managed-bean>" +
            "    <managed-bean-name>nonExist</managed-bean-name>" +
            "    <managed-bean-class>org.apache.myfaces.config.NonExist</managed-bean-class>" +
            "    <managed-bean-scope>request</managed-bean-scope>" +
            "</managed-bean>" +
       "</faces-config>";
}
