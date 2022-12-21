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
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

@RunWith(Arquillian.class)
@RunAsClient
public class IntegrationTest
{
    @Deployment(testable = false)
    public static WebArchive createDeployment()
    {
        return ShrinkWrap.create(ZipImporter.class, "automaticExtensionlessMapping.war")
                .importFrom(new File("target/automaticExtensionlessMapping.war"))
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
    public void foo()
    {
        webDriver.get(contextPath + "foo");

        // check that URLs are rendered as exact mapping
        Assert.assertTrue(webDriver.getPageSource().contains("/foo\""));

        // check if served as non-exact-mapping
        Assert.assertTrue(webDriver.getCurrentUrl().endsWith("/foo"));
        
        Assert.assertTrue(webDriver.getPageSource().contains("/bar\""));
        
        // resources must NOT use exact mapping
        Assert.assertTrue(webDriver.getPageSource().contains("/jakarta.faces.resource/faces.js.xhtml?ln=jakarta.faces"));
    }

    @Test
    public void bar()
    {
        webDriver.get(contextPath + "bar");

        // check that URLs are rendered as exact mapping
        Assert.assertTrue(webDriver.getPageSource().contains("/bar\""));

        // check if served as non-exact-mapping
        Assert.assertTrue(webDriver.getCurrentUrl().endsWith("/bar"));
      
        Assert.assertTrue(webDriver.getPageSource().contains("/foo\""));
        
        // resources must NOT use exact mapping
        Assert.assertTrue(webDriver.getPageSource().contains("/jakarta.faces.resource/faces.js.xhtml?ln=jakarta.faces"));
    }
}
