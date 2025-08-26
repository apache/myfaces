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
package org.apache.myfaces.core.extensions.quarkus.showcase;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Disabled
@QuarkusTest
public class ExtensionlessMappingTest
{

    @TestHTTPResource
    URL url;

    private static WebClient webClient;

    @BeforeAll
    public static void initWebClient()
    {
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getCookieManager().setCookiesEnabled(true);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
    }

    @AfterAll
    public static void closeWebClient()
    {
        if (webClient != null)
        {
            webClient.close();
        }
    }

    @Test
    public void foo() throws IOException {
        HtmlPage page = webClient.getPage(url + "/foo");

        // check that URLs are rendered as exact mapping
        Assertions.assertTrue(page.asXml().contains("/foo\""));

        // check if served as non-exact-mapping
        Assertions.assertTrue(page.getUrl().toExternalForm().endsWith("/foo"));

        Assertions.assertTrue(page.asXml().contains("/bar\""));
    }

    @Test
    public void bar() throws IOException {
        HtmlPage page = webClient.getPage(url + "/bar");

        // check that URLs are rendered as exact mapping
        Assertions.assertTrue(page.asXml().contains("/bar\""));

        // check if served as non-exact-mapping
        Assertions.assertTrue(page.getUrl().toExternalForm().endsWith("/bar"));

        Assertions.assertTrue(page.asXml().contains("/foo\""));
    }

    @Test
    public void subfolder() throws IOException {
        HtmlPage page = webClient.getPage(url + "/test/some-folder/bar");

        // check that URLs are rendered as exact mapping
        Assertions.assertTrue(page.asXml().contains("/bar\""));

        // check if served as non-exact-mapping
        Assertions.assertTrue(page.getUrl().toExternalForm().endsWith("some-folder/bar"));

        Assertions.assertTrue(page.asXml().contains("/foo\""));
        Assertions.assertTrue(page.asXml().contains("/someFolder/bar\""));
        Assertions.assertFalse(page.asXml().contains("/someFolder/bar.xhtml\""));
    }

    @Test
    public void subfolder2() throws IOException {
        HtmlPage page = webClient.getPage(url + "/test/someFolder/bar");

        // check that URLs are rendered as exact mapping
        Assertions.assertTrue(page.asXml().contains("/bar\""));

        // check if served as non-exact-mapping
        Assertions.assertTrue(page.getUrl().toExternalForm().endsWith("someFolder/bar"));

        Assertions.assertTrue(page.asXml().contains("/foo\""));
        Assertions.assertTrue(page.asXml().contains("/some-folder/bar\""));
        Assertions.assertFalse(page.asXml().contains("/some-folder/bar.xhtml\""));
    }
}
