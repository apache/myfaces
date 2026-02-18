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
        // Fix for 'Failed to scan serializer.jar' error message
        String key = "tomcat.util.scan.StandardJarScanFilter.jarsToSkip";
        String value = "bootstrap.jar,commons-daemon.jar,tomcat-juli.jar,annotations-api.jar,el-api.jar,jsp-api.jar," +
                "servlet-api.jar,websocket-api.jar,jaspic-api.jar,catalina.jar,catalina-ant.jar,catalina-ha.jar," +
                "catalina-storeconfig.jar,catalina-tribes.jar,jasper.jar,jasper-el.jar,ecj-*.jar,tomcat-api.jar," +
                "tomcat-util.jar,tomcat-util-scan.jar,tomcat-coyote.jar,tomcat-dbcp.jar,tomcat-jni.jar," +
                "tomcat-websocket.jar,tomcat-i18n-en.jar,tomcat-i18n-es.jar,tomcat-i18n-fr.jar,tomcat-i18n-ja.jar," +
                "tomcat-juli-adapters.jar,catalina-jmx-remote.jar,catalina-ws.jar,tomcat-jdbc.jar,tools.jar," +
                "commons-beanutils*.jar,commons-codec*.jar,commons-collections*.jar,commons-dbcp*.jar," +
                "commons-digester*.jar,commons-fileupload*.jar,commons-httpclient*.jar,commons-io*.jar," +
                "commons-lang*.jar,commons-logging*.jar,commons-math*.jar,commons-pool*.jar,jstl.jar," +
                "taglibs-standard-spec-*.jar,geronimo-spec-jaxrpc*.jar,wsdl4j*.jar,ant.jar,ant-junit*.jar," +
                "aspectj*.jar,jmx.jar,h2*.jar,hibernate*.jar,httpclient*.jar,jmx-tools.jar,jta*.jar,log4j*.jar," +
                "mail*.jar,slf4j*.jar,xercesImpl.jar,xmlParserAPIs.jar,xml-apis.jar,junit.jar,junit-*.jar," +
                "ant-launcher.jar,cobertura-*.jar,asm-*.jar,dom4j-*.jar,icu4j-*.jar,jaxen-*.jar,jdom-*.jar," +
                "jetty-*.jar,oro-*.jar,servlet-api-*.jar,tagsoup-*.jar,xmlParserAPIs-*.jar,xom-*.jar,serializer.jar";
        System.setProperty(key, value);

        return ShrinkWrap.create(ZipImporter.class, "automaticExtensionlessMapping.war")
                .importFrom(new File("target/automaticExtensionlessMapping.war"))
                .as(WebArchive.class);
    }

    @Drone
    protected WebDriver webDriver;

    @ArquillianResource
    protected URL contextPath;

    @org.junit.BeforeClass
    public static void setupDriver()
    {
        io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
    }

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

    @Test
    public void subfolder()
    {
        webDriver.get(contextPath + "some-folder/bar");

        // check that URLs are rendered as exact mapping
        Assert.assertTrue(webDriver.getPageSource().contains("/bar\""));

        // check if served as non-exact-mapping
        Assert.assertTrue(webDriver.getCurrentUrl().endsWith("some-folder/bar"));

        Assert.assertTrue(webDriver.getPageSource().contains("/foo\""));
        Assert.assertTrue(webDriver.getPageSource().contains("/someFolder/bar\""));
        Assert.assertFalse(webDriver.getPageSource().contains("/someFolder/bar.xhtml\""));

        // resources must NOT use exact mapping
        Assert.assertTrue(webDriver.getPageSource().contains("/jakarta.faces.resource/faces.js.xhtml?ln=jakarta.faces"));
    }

    @Test
    public void subfolder2()
    {
        webDriver.get(contextPath + "someFolder/bar");

        // check that URLs are rendered as exact mapping
        Assert.assertTrue(webDriver.getPageSource().contains("/bar\""));

        // check if served as non-exact-mapping
        Assert.assertTrue(webDriver.getCurrentUrl().endsWith("someFolder/bar"));

        Assert.assertTrue(webDriver.getPageSource().contains("/foo\""));
        Assert.assertTrue(webDriver.getPageSource().contains("/some-folder/bar\""));
        Assert.assertFalse(webDriver.getPageSource().contains("/some-folder/bar.xhtml\""));

        // resources must NOT use exact mapping
        Assert.assertTrue(webDriver.getPageSource().contains("/jakarta.faces.resource/faces.js.xhtml?ln=jakarta.faces"));
    }

    /*
     * MYFACES-4644: Ensure Programmatic Views can be accessed without extensions
     */
    @Test
    public void testExtensionlessProgrammaticView()
    {
        webDriver.get(contextPath + "test");

        Assert.assertTrue(webDriver.getPageSource().contains("Success!"));

    }
}
