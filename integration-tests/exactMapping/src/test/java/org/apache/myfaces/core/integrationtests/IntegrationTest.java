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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
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
    public void test()
    {
        webDriver.get(contextPath + "foo");

        // check if we are on foo.xhtml
        Assert.assertTrue(webDriver.getPageSource().contains("foo-view"));
    }

    @Test
    public void testNonExactMapping()
    {
        webDriver.get(contextPath + "foo.jsf");

        // check if we are on foo.jsf
        Assert.assertTrue(webDriver.getPageSource().contains("foo-view"));
    }

    @Test
    public void testPostBack()
    {
        webDriver.get(contextPath + "foo");

        // remember url
        String url = webDriver.getCurrentUrl();

        // post to foo.xhtml
        Graphene.guardHttp(webDriver.findElement(By.id("form:commandButton"))).click();

        // check if method was invoked
        Assert.assertTrue(webDriver.getPageSource().contains("foo invoked"));
        
        // check that the exact mapping is still used after post
        Assert.assertTrue(webDriver.getCurrentUrl().equals(url));
    }

    @Test
    public void testLinkToNonExactMapping()
    {
        webDriver.get(contextPath + "foo");

        // check if we are on foo.xhtml
        Assert.assertTrue(webDriver.getPageSource().contains("foo-view"));

        // navigate to bar.xhtml
        Graphene.guardHttp(webDriver.findElement(By.id("form:button"))).click();

        // check if we are on bar.xhtml
        Assert.assertTrue(webDriver.getPageSource().contains("bar-view"));

        // check if it's a JSF view with ViewState
        Assert.assertTrue(webDriver.getPageSource().contains("ViewState"));

        // check if bar.xml is served as non-exact-mapping
        Assert.assertTrue(webDriver.getCurrentUrl().endsWith("/bar.jsf")
                || webDriver.getCurrentUrl().endsWith("/faces/bar")
                || webDriver.getCurrentUrl().endsWith("/faces/bar.xhtml"));
    }
    
    @Test
    public void testPostBackOnNonExactMapping()
    {
        webDriver.get(contextPath + "foo");

        // nagivate to non-exact-mapping (bar.xhtml)
        Graphene.guardHttp(webDriver.findElement(By.id("form:button"))).click();

        // post to bar.xhtml
        Graphene.guardHttp(webDriver.findElement(By.id("form:commandButton"))).click();

        // check if post was successful
        Assert.assertTrue(webDriver.getPageSource().contains("foo invoked"));
        
        // check if we are on bar.xhtml
        Assert.assertTrue(webDriver.getCurrentUrl().endsWith("/bar.jsf")
                || webDriver.getCurrentUrl().endsWith("/faces/bar")
                || webDriver.getCurrentUrl().endsWith("/faces/bar.xhtml"));
    }
    
    @Test
    public void testResourceReference()
    {
        webDriver.get(contextPath + "foo");

        // check if resources are loaded via non-exact-mapping
        Assert.assertTrue(webDriver.getPageSource().contains("jakarta.faces.resource/jsf.js.jsf")
                || webDriver.getPageSource().contains("jakarta.faces.resource/faces/jsf.js")
                || webDriver.getPageSource().contains("faces/jakarta.faces.resource/jsf.js"));
    }
    
    @Test
    public void testAjaxPostBack()
    {
        webDriver.get(contextPath + "foo");

        // call ajax button
        Graphene.guardAjax(webDriver.findElement(By.id("form:commandButtonAjax"))).click();

        // check if the button was invoked
        Assert.assertTrue(webDriver.getPageSource().contains("fooAjax invoked"));
    }
}
