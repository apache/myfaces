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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@QuarkusTest
public class QuarkusMyFacesShowcaseTest
{

    @TestHTTPResource
    URL url;

    private static WebClient webClient;

    @BeforeEachAll
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

    @AfterEachAll
    public static void closeWebClient()
    {
        if (webClient != null)
        {
            webClient.close();
        }
    }


    @Test
    public void shouldOpenIndexPage() throws Exception
    {
        final HtmlPage page = webClient.getPage(url + "/index.xhtml");
        final HtmlDivision datatable = (HtmlDivision) page.getElementById("form:carTable");
        assertThat(datatable).isNotNull();
        assertThat(datatable.getByXPath("//tr[contains(@class,'ui-datatable-selectable')]"))
                .hasSize(10);
    }

    @Test
    @Disabled("Check HtmlUnit websocket support, for now this test is not working")
    public void shouldCallWebSocket() throws IOException
    {
        HtmlPage page = webClient.getPage(url + "/socket.xhtml");
        final HtmlSubmitInput sendMessageBtn = (HtmlSubmitInput) page.getElementById("form:sendMessage");
        page = sendMessageBtn.click();
        webClient.waitForBackgroundJavaScript(5000);
        DomElement message = page.getElementById("message");
        assertThat(message.asXml())
                .contains("hello at");

    }


}
