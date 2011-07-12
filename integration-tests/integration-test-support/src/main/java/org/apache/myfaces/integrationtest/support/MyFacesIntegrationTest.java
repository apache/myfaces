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
package org.apache.myfaces.integrationtest.support;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import org.junit.After;
import org.junit.Before;

/**
 * Base class for all MyFaces integration tests, providing the fully configured WebClient instance
 * for running the HtmlUnit tests against the server provided by cargo/maven.
 *
 * @author Jakob Korherr
 */
public abstract class MyFacesIntegrationTest
{

    private static final String CARGO_CONTEXT_PROPERTY = "cargo.context";
    private static final String CARGO_PORT_PROPERTY = "cargo.port";

    private static final String DEFAULT_CONTEXT = "cargo-test";
    private static final String DEFAULT_PORT = "9090";

    protected WebClient webClient;

    private String baseUrl;

    protected String getBaseURL()
    {
        if (baseUrl == null)
        {
            // maven passes the port + context via system properties to us
            String port = System.getProperty(CARGO_PORT_PROPERTY);
            if (port == null)
            {
                port = DEFAULT_PORT;
            }

            String context = System.getProperty(CARGO_CONTEXT_PROPERTY);
            if (context == null)
            {
                context = DEFAULT_CONTEXT;
            }

            baseUrl = "http://localhost:" + port + "/" + context + "/";
        }

        return baseUrl;
    }

    @Before
    public void before()
    {
        webClient = new WebClient(getBrowserVersion());
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
    }

    @After
    public void after()
    {
        webClient.closeAllWindows();
        webClient = null;
    }

    /**
     * Returns the Browser to use.
     * Default is Firefox 3.6, override to change this.
     *
     * @return default browser
     */
    public BrowserVersion getBrowserVersion()
    {
        return BrowserVersion.FIREFOX_3_6;
    }

}
