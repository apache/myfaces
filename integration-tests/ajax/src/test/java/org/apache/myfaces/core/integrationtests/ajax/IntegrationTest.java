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
package org.apache.myfaces.core.integrationtests.ajax;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ByIdOrName;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(Arquillian.class)
@RunAsClient
public class IntegrationTest
{

    public static final String IB_1 = "insert before succeeded should display before test1";
    public static final String IB_2 = "insert2 before succeeded should display before test1";
    public static final String IA_2 = "insert2 after succeeded should display after test1";
    public static final String IA_1 = "insert after succeeded should display after test1";
    public static final String IEL = "update succeeded 1";

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

        return ShrinkWrap.create(ZipImporter.class, "ajax.war")
                .importFrom(new File("target/ajax.war"))
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

    @After
    public void after()
    {
        webDriver.manage().deleteAllCookies();
    }

    @Before
    public void before()
    {
        // The default is 0 which causes race conditions on findElement!
        webDriver.manage().timeouts().implicitlyWait(Duration.ofMillis(3000));
    }

    public void resetServerValues()
    {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofMillis(20));
        wait.until((ExpectedCondition<Boolean>) driver -> driver.findElement(By.id("_reset_all")).isDisplayed());
        webDriver.findElement(new By.ById("_reset_all")).click();
    }

    @Test
    public void testAjaxPresent()
    {

        webDriver.get(contextPath + "index.jsf");
        resetServerValues();

        webDriver.findElement(new ByIdOrName("mainForm:press")).click();
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofMillis(20));
        wait.until((ExpectedCondition<Boolean>) driver -> driver.getPageSource().contains("Action Performed"));
        assertTrue(webDriver.getPageSource().contains("ViewState"));
        assertTrue(webDriver.getPageSource().contains("_ajax_found"));
        assertTrue(webDriver.getPageSource().contains("Action Performed"));
    }


    /**
     * Second test, test various aspects of the xhr protocol
     * and the response handling
     */
    @Test
    public void testProtocol()
    {
        webDriver.get(contextPath + "test1-protocol.jsf");
        resetServerValues();
        //simple eval
        trigger("cmd_eval", webDriver -> webDriver.getPageSource().contains("eval test succeeded"));

        //simple update insert with embedded js
        trigger("cmd_update_insert", webDriver ->
        {
            String pageSource = webDriver.getPageSource();
            return pageSource.contains("embedded script at update succeed") &&
                    pageSource.contains("embedded script at insert succeed");
        });

        //update, insert with the correct order
        trigger("cmd_update_insert2", webDriver ->
        {
            String pageSource = webDriver.getPageSource();
            return updateInsertElementsPresent(pageSource) &&
                    correctInsertUpdatePos(pageSource);
        });

        //delete command
        trigger("cmd_delete", webDriver -> !webDriver.getPageSource().contains("deleteable"));


        //attributes change
        trigger("cmd_attributeschange", webDriver -> webDriver.getPageSource().contains("1px solid black"));

        //illegal response just triggers a normal error which goes into the log
        trigger("cmd_illegalresponse", webDriver -> webDriver.findElement(new ByIdOrName("logError")).isDisplayed() &&
                webDriver.findElement(new ByIdOrName("logError")).getText().contains("malformedXML"));

        //server error, should trigger our error chain, no log error
        trigger("cmd_error", webDriver -> webDriver.findElement(new ByIdOrName("processedError")).isDisplayed() &&
                webDriver.findElement(new ByIdOrName("processedError")).getText().contains("serverError"));


    }


    /**
     * third test, body replacement
     */
    @Test
    public void testViewBody()
    {
        webDriver.get(contextPath + "test2-viewbody.jsf");
        resetServerValues();
        trigger("cmd_body1", webDriver ->
                !webDriver.getPageSource().contains("toReplace") &&
                        !webDriver.getPageSource().contains("hello from embedded script & in the body")
        );
    }


    /**
     * third test, testing the chain function
     */
    @Test
    public void testChain()
    {
        webDriver.get(contextPath + "test3-chain.jsf");
        resetServerValues();
        webDriver.findElement(new ByIdOrName("chaincall")).click();
        String testSource = webDriver.findElement(new ByIdOrName("testResults")).getText();
        assertTrue(testSource.contains("test1 succeeded"));
        assertTrue(testSource.contains("test2 succeeded"));
        assertTrue(testSource.contains("test3 succeeded"));
        assertFalse(testSource.contains("test4 failed"));
    }

    @Test
    public void testViewRootBodyReplacement()
    {
        webDriver.get(contextPath + "test4-viewbody-full-response.jsf");
        resetServerValues();
        trigger("cmd_body1", webDriver1 -> webDriver1.getPageSource().contains("Test for body change done") &&
                webDriver1.getPageSource().contains("Body replacement test  successful"));
    }

    /**
     * recurring trigger, wait until ajax processing is done function
     *
     * @param id        the trigger element id
     * @param condition a condition resolver which should return true if the condition is met
     */
    void trigger(String id, ExpectedCondition<Boolean> condition)
    {
        webDriver.findElement(new ByIdOrName(id)).click();
        new WebDriverWait(webDriver, Duration.ofMillis(500))
                .ignoring(StaleElementReferenceException.class)
                .until(condition);
    }


    //some page state condition helpers
    private boolean updateInsertElementsPresent(String pageSource)
    {
        return pageSource.contains(IB_1) &&
                pageSource.contains(IB_2) &&
                pageSource.contains(IA_2) &&
                pageSource.contains(IA_1) &&
                pageSource.contains(IEL);
    }


    private boolean correctInsertUpdatePos(String pageSource)
    {
        return pageSource.indexOf(IB_1) < pageSource.indexOf(IB_2) &&
                pageSource.indexOf(IB_2) < pageSource.indexOf(IEL) &&
                pageSource.indexOf(IEL) < pageSource.indexOf(IA_2) &&
                pageSource.indexOf(IA_2) < pageSource.indexOf(IA_1);
    }
}
