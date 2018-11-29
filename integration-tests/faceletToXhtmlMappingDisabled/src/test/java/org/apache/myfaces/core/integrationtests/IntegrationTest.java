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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
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
        WebArchive webArchive = (WebArchive) EmbeddedMaven.forProject(new File("pom.xml"))
                .useMaven3Version("3.3.9")
                .setGoals("package")
                .setQuiet()
                .skipTests(true)
                .ignoreFailure()
                .build().getDefaultBuiltArchive();

        return webArchive;
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
    public void testDefaultMapping()
    {
        webDriver.get(contextPath + "index.xhtml");

        // check if the javax.faces.DISABLE_FACESSERVLET_TO_XHTML works as expected
        Assert.assertTrue(!webDriver.getPageSource().contains("ViewState"));
    }
    
    @Test
    public void testPrefixMapping()
    {
        webDriver.get(contextPath + "/faces/index.xhtml");

        // checks if a prefix mapping still works
        Assert.assertTrue(webDriver.getPageSource().contains("ViewState"));
    }

    @Test
    public void testSuffixMapping()
    {
        webDriver.get(contextPath + "/index.jsf");

        // checks if a prefix mapping still works
        Assert.assertTrue(webDriver.getPageSource().contains("ViewState"));
    }
}
