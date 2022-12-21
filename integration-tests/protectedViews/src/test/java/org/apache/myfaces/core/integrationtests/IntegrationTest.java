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
package org.apache.myfaces.core.integrationtests;

import java.io.File;
import java.net.URL;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@RunWith(Arquillian.class)
@RunAsClient
public class IntegrationTest
{
    @Deployment(testable = false)
    public static WebArchive createDeployment()
    {
        return ShrinkWrap.create(ZipImporter.class, "protectedViews.war")
                .importFrom(new File("target/protectedViews.war"))
                .as(WebArchive.class);
    }

    @Drone
    protected WebDriver webDriver;

    @ArquillianResource
    protected URL contextPath;

    @Before
    public void before()
    {
    }

    @After
    public void after()
    {
        webDriver.manage().deleteAllCookies();
    }

    @Test
    public void test()
    {
        webDriver.get(contextPath + "index.xhtml");

        Assert.assertTrue(webDriver.getPageSource().contains("Index View"));
        
        WebElement link = webDriver.findElement(By.id("link"));
        Assert.assertTrue(link.getAttribute("href").contains("jakarta.faces.Token"));
        
        Graphene.guardHttp(link).click();
        
        Assert.assertTrue(webDriver.getPageSource().contains("Protected View"));
    }
    
    @Test
    public void testIllegalAccess()
    {
        webDriver.get(contextPath + "myProtectedView.xhtml");
        
        Assert.assertTrue(webDriver.getPageSource().contains("HTTP ERROR 500")
            || webDriver.getPageSource().contains("HTTP Status 500"));
    }
}
