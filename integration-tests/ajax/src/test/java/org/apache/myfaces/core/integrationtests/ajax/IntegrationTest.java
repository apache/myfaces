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
        trigger("cmd_error", webDriver -> webDriver.findElement(new ByIdOrName("processedErrror")).isDisplayed() &&
                webDriver.findElement(new ByIdOrName("processedErrror")).getText().contains("serverError"));


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
    public void testBasicTable()
    {
        webDriver.get(contextPath + "test4-tablebasic.jsf");
        resetServerValues();

        trigger("replace_head", webDriver ->
        {
            final WebElement testTable = webDriver.findElement(new By.ById("testTable"));

            return testTable.getText().contains("column1 in line1 replaced") &&
                    testTable.getText().contains("script evaled0");
        });

        trigger("replace_body", webDriver ->
        {
            final WebElement tableSegment = webDriver.findElement(new By.ById("body_row1_col1"));
            return tableSegment.getText().contains("column1 in line1 replaced") &&
                    tableSegment.getText().contains("script evaled");
        });

        trigger("insert_row_head", webDriver ->
        {
            final WebElement headRow0 = webDriver.findElement(new By.ById("head_row1_0"));
            final WebElement headRow1 = webDriver.findElement(new By.ById("head_row1"));

            return headRow1.getLocation().y > headRow0.getLocation().y &&
                    headRow0.getText().contains("column1 in line1 inserted before") &&
                    headRow0.getText().contains("colum2 in line2 inserted before");
        });


        trigger("insert_row_body", webDriver ->
        {
            final WebElement bodyRowCol1 = webDriver.findElement(new By.ById("body_row1_col1"));
            final WebElement bodyRowCol2 = webDriver.findElement(new By.ById("body_row1_col2"));
            final WebElement bodyRowCol0 = webDriver.findElement(new By.ById("body_row1_3_col1"));
            final WebElement bodyRowCol4 = webDriver.findElement(new By.ById("body_row1_4_col1"));

            return bodyRowCol0.getLocation().y < bodyRowCol1.getLocation().y &&
                    bodyRowCol1.getLocation().y < bodyRowCol4.getLocation().y &&

                    bodyRowCol1.getText().contains("column1 in line1 inserted after") &&
                    bodyRowCol1.getText().contains("evaled") &&
                    bodyRowCol2.getText().contains("colum2 in line1 replaced");
        });

        trigger("insert_column_head", webDriver ->
        {
            final WebElement headCol0 = webDriver.findElement(new By.ById("head_col1_1_4"));
            final WebElement headCol1 = webDriver.findElement(new By.ById("head_col1_1_5"));
            final WebElement headCol2 = webDriver.findElement(new By.ById("head_col1"));
            final WebElement headCol3 = webDriver.findElement(new By.ById("head_col2"));
            final WebElement headCol4 = webDriver.findElement(new By.ById("head_col1_1_6"));
            final WebElement headCol5 = webDriver.findElement(new By.ById("head_col1_1_7"));

            return headCol0.getLocation().x < headCol1.getLocation().x &&
                   headCol1.getLocation().x < headCol2.getLocation().x &&
                   headCol3.getLocation().x < headCol4.getLocation().x &&
                   headCol4.getLocation().x < headCol5.getLocation().x &&
                   headCol1.getLocation().y == headCol2.getLocation().y &&
                   headCol2.getLocation().y == headCol3.getLocation().y &&
                   headCol3.getLocation().y == headCol4.getLocation().y &&
                   headCol4.getLocation().y == headCol5.getLocation().y;

        });


        trigger("insert_column_body", webDriver ->
        {
            final WebElement bodyCol0 = webDriver.findElement(new By.ById("body_row1_col1_1_8"));
            final WebElement bodyCol1 = webDriver.findElement(new By.ById("body_row1_col1_1_9"));
            final WebElement bodyCol2 = webDriver.findElement(new By.ById("body_row1_col1"));
            final WebElement bodyCol3 = webDriver.findElement(new By.ById("body_row1_col2"));
            final WebElement bodyCol4 = webDriver.findElement(new By.ById("body_row1_col1_1_10"));
            final WebElement bodyCol5 = webDriver.findElement(new By.ById("body_row1_col1_1_11"));

            return bodyCol0.getLocation().x < bodyCol1.getLocation().x &&
                    bodyCol1.getLocation().x < bodyCol2.getLocation().x &&
                    bodyCol3.getLocation().x < bodyCol4.getLocation().x &&
                    bodyCol4.getLocation().x < bodyCol5.getLocation().x &&
                    bodyCol1.getLocation().y == bodyCol2.getLocation().y &&
                    bodyCol2.getLocation().y == bodyCol3.getLocation().y &&
                    bodyCol3.getLocation().y == bodyCol4.getLocation().y &&
                    bodyCol4.getLocation().y == bodyCol5.getLocation().y;

        });

        trigger("insert_body", webDriver ->
        {
            return webDriver.getPageSource().contains("<tbody>") &&
                    webDriver.getPageSource().contains("second body added");
        });

    }

    @Test
    public void testViewRootBodyReplacement()
    {
        webDriver.get(contextPath + "test5-viewbody-full-response.jsf");
        resetServerValues();
        trigger("cmd_body1", webDriver1 -> webDriver1.getPageSource().contains("Test for body change done") &&
                webDriver1.findElement(new By.ById("scriptreceiver")).getText().contains("hello from embedded script & in the body"));
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
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofMillis(200));
        wait.until(condition);
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
