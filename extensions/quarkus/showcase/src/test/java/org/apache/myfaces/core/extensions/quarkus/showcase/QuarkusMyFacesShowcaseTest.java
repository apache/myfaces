package org.apache.myfaces.core.extensions.quarkus.showcase;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@QuarkusTest
public class QuarkusMyFacesShowcaseTest {

    @TestHTTPResource
    URL url;

    @Test
    public void shouldOpenIndexPage() throws Exception {
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setUseInsecureSSL(true);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getCookieManager().setCookiesEnabled(true);
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());
            webClient.getCookieManager().setCookiesEnabled(true);

            final HtmlPage page = webClient.getPage(url + "/index.xhtml");

            final HtmlDivision datatable = (HtmlDivision) page.getElementById("form:carTable");

            assertThat(datatable).isNotNull();
            assertThat(datatable.getByXPath("//tr[contains(@role,'row') and contains(@class,'ui-datatable-selectable')]"))
                    .hasSize(10);

        }
    }
}
