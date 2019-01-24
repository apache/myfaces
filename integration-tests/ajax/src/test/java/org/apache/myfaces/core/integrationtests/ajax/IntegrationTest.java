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
import org.jboss.arquillian.graphene.javascript.JavaScript;
import org.jboss.arquillian.graphene.request.RequestGuard;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ByIdOrName;

import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.jboss.arquillian.graphene.Graphene.waitAjax;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(Arquillian.class)
@RunAsClient
public class IntegrationTest {

    public static final String IB_1 = "insert before succeeded should display before test1";
    public static final String IB_2 = "insert2 before succeeded should display before test1";
    public static final String IA_2 = "insert2 after succeeded should display after test1";
    public static final String IA_1 = "insert after succeeded should display after test1";
    public static final String IEL = "update succeeded 1";

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
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

    @ArquillianResource
    JavascriptExecutor executor;


    @JavaScript
    RequestGuard guard;




    @After
    public void after() {
        webDriver.manage().deleteAllCookies();
    }

    @Before
    public void before() {}

    public void resetServerValues() {
        waitAjax().withTimeout(10, TimeUnit.SECONDS).until(new Function<WebDriver, Object>() {

            public Object apply(WebDriver webDriver) {
                return webDriver.findElement(By.id("_reset_all")).isDisplayed();
            }
        });
        webDriver.findElement(new By.ById("_reset_all")).click();
    }

    @Test
    public void testAjaxPresent() {

        webDriver.get(contextPath + "index.jsf");
        resetServerValues();

        webDriver.findElement(new ByIdOrName("mainForm:press")).click();
        waitAjax().withTimeout(10, TimeUnit.SECONDS).until(new Function<WebDriver, Object>() {

            public Object apply(WebDriver webDriver) {
                return webDriver.getPageSource().contains("Action Performed");
            }
        });
        assertTrue(webDriver.getPageSource().contains("ViewState"));
        assertTrue(webDriver.getPageSource().contains("_ajax_found"));
        assertTrue(webDriver.getPageSource().contains("Action Performed"));
    }


    /**
     * Second test, test various aspects of the xhr protocol
     * and the response handling
     */
    @Test
    public void testProtocol() {
        webDriver.get(contextPath + "test1-protocol.jsf");
        resetServerValues();
        //simple eval
        trigger("cmd_eval", webDriver -> webDriver.getPageSource().contains("eval test succeeded"));

        //simple update insert with embedded js
        trigger("cmd_update_insert", webDriver -> {
            String pageSource = webDriver.getPageSource();
            return pageSource.contains("embedded script at update succeed") &&
                    pageSource.contains("embedded script at insert succeed");
        });

        //update, insert with the correct order
        trigger("cmd_update_insert2", webDriver -> {
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

        //component error, client side, only log error
        trigger("cmd_error_component", webDriver -> webDriver.findElement(new ByIdOrName("logError")).isDisplayed() &&
                webDriver.findElement(new ByIdOrName("logError")).getText().contains("ArgNotSet"));

    }


    /**
     * third test, body replacement
     */
    @Test
    public void testViewBody() {
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
    public void testChain() {
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
    public void testBasicTable() {
        webDriver.get(contextPath + "test4-tablebasic.jsf");
        resetServerValues();

        trigger("replace_head", webDriver -> {
            final WebElement testTable = webDriver.findElement(new By.ById("testTable"));

            return testTable.getText().contains("column1 in line1 replaced") &&
                    testTable.getText().contains("script evaled0");
        });

        trigger("replace_body", webDriver -> {
            final WebElement tableSegment = webDriver.findElement(new By.ById("body_row1_col1"));
            System.out.println(tableSegment.getText());
            return tableSegment.getText().contains("column1 in line1 replaced") &&
                    tableSegment.getText().contains("script evaled");
        });

        trigger("insert_row_head", webDriver -> {
            final WebElement headRow0 = webDriver.findElement(new By.ById("head_row1_0"));
            final WebElement headRow1 = webDriver.findElement(new By.ById("head_row1"));

            return  headRow1.getLocation().y > headRow0.getLocation().y &&
                    headRow0.getText().contains("column1 in line1 inserted before") &&
                    headRow0.getText().contains("colum2 in line2 inserted before");
        });


        //TODO the content is correct but the numbers are dynamic, so we need to find
        //a better way to identify the elements
       /* trigger("insert_row_body", webDriver -> {
            final WebElement headRow0 = webDriver.findElement(new By.ByClassName("body_row1_0"));
            final WebElement headRow1 = webDriver.findElement(new By.ById("head_row1"));

            return  headRow1.getLocation().y > headRow0.getLocation().y &&
                    headRow0.getText().contains("column1 in line1 inserted before") &&
                    headRow0.getText().contains("colum2 in line2 inserted before");
        });*/

    }


    /**
     * recurring trigger, wait until ajax processing is done function
     *
     * @param id        the trigger element id
     * @param condition a condition resolver which should return true if the condition is met
     */
    void trigger(String id, Function<WebDriver, Object> condition) {
        webDriver.findElement(new ByIdOrName(id)).click();
        waitAjax()
                .withTimeout(10, TimeUnit.SECONDS)
                .until(condition);
    }


    //some page state condition helpers
    private boolean updateInsertElementsPresent(String pageSource) {
        return pageSource.contains(IB_1) &&
                pageSource.contains(IB_2) &&
                pageSource.contains(IA_2) &&
                pageSource.contains(IA_1) &&
                pageSource.contains(IEL);
    }


    private boolean correctInsertUpdatePos(String pageSource) {
        return pageSource.indexOf(IB_1) < pageSource.indexOf(IB_2) &&
                pageSource.indexOf(IB_2) < pageSource.indexOf(IEL) &&
                pageSource.indexOf(IEL) < pageSource.indexOf(IA_2) &&
                pageSource.indexOf(IA_2) < pageSource.indexOf(IA_1);
    }
}
