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
package org.apache.myfaces.test.htmlunit.junit4;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlBody;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlHead;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

/**
 * <p>Abstract base class for system integration tests based on HtmlUnit.
 * These tests will expect a system property named <code>url</code> to be
 * present, which will define the URL (including the context path, but
 * without a trailing slash) of the application to be tested.</p>
 * 
 * @since 1.0.0
 */
public abstract class AbstractHtmlUnitTestCase
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a new instance of this test case.</p>
     *
     * @param name Name of the new test case
     */
    public AbstractHtmlUnitTestCase()
    {
    }

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>The most recently retrieved page from the server.</p>
     */
    protected HtmlPage page = null;

    /**
     * <p>The calculated URL for the installed "systest" web application.
     * This value is based on a system property named <code>url</code>,
     * which must be defined as part of the command line that executes
     * each test case.</p>
     */
    protected URL url = null;

    /**
     * <p>The web client for this test case.</p>
     */
    protected WebClient webClient = null;

    // ------------------------------------------------------ Test Setup Methods

    /**
     * <p>Set up the instance variables required for this test case.</p>
     *
     * @exception Exception if an error occurs
     */
    @Before
    protected void setUp() throws Exception
    {

        // Calculate the URL for the installed "systest" web application
        String url = System.getProperty("url");
        this.url = new URL(url + "/");

        // Initialize HtmlUnit constructs for this test case
        webClient = new WebClient();

    }

    /**
     * <p>Tear down instance variables required by this test case.</p>
     */
    @After
    protected void tearDown() throws Exception
    {

        page = null;
        url = null;
        webClient = null;

    }

    // ------------------------------------------------------- Protected Methods

    /**
     * <p>Return the body element for the most recently retrieved page.
     * If there is no such element, return <code>null</code>.</p>
     *
     * @exception Exception if an error occurs
     */
    protected HtmlBody body() throws Exception
    {
        Iterable<HtmlElement> elements = page.getHtmlElementDescendants();
        for (HtmlElement element : elements)
        {
            if (element instanceof HtmlBody)
            {
                return ((HtmlBody) element);
            }
        }
        return (null);

    }

    /**
     * <p>Return the HTML element with the specified <code>id</code> from the
     * most recently retrieved page.  If there is no such element, return
     * <code>null</code>.</p>
     *
     * @param id Identifier of the requested element.
     *
     * @exception Exception if an error occurs
     */
    protected HtmlElement element(String id) throws Exception
    {

        try
        {
            return (page.getHtmlElementById(id));
        }
        catch (ElementNotFoundException e)
        {
            return (null);
        }

    }

    /**
     * <p>Return the form with the specified <code>id</code> from the most
     * recently retrieved page.  If there is no such form, return
     * <code>null</code>.<p>
     *
     * @param id Identifier of the requested form.
     *
     * @exception Exception if an error occurs
     */
    protected HtmlForm form(String id) throws Exception
    {

        Iterator forms = page.getForms().iterator();
        while (forms.hasNext())
        {
            HtmlForm form = (HtmlForm) forms.next();
            if (id.equals(form.getAttribute("id")))
            {
                return (form);
            }
        }
        return (null);

    }

    /**
     * <p>Return the head element for the most recently retrieved page.
     * If there is no such element, return <code>null</code>.</p>
     *
     * @exception Exception if an error occurs
     */
    protected HtmlHead head() throws Exception
    {
        Iterable<HtmlElement> elements = page.getHtmlElementDescendants();
        for (HtmlElement element : elements)
        {
            if (element instanceof HtmlHead)
            {
                return ((HtmlHead) element);
            }
        }
        return (null);

    }

    /**
     * <p>Click the specified hyperlink, and retrieve the subsequent page,
     * saving a reference so that other utility methods may be used to
     * retrieve information from it.</p>
     *
     * @param anchor Anchor component to click
     *
     * @exception IOException if an input/output error occurs
     */
    protected HtmlPage link(HtmlAnchor anchor) throws IOException
    {

        HtmlPage page = (HtmlPage) anchor.click();
        this.page = page;
        return page;

    }

    /**
     * <p>Return the currently stored page reference.</p>
     */
    protected HtmlPage page()
    {

        return this.page;

    }

    /**
     * <p>Retrieve and return the page at the specified context relative path.
     * Save a reference to this page so that other utility methods may be used
     * to retrieve information from it.</p>
     *
     * @param path Context relative path
     *
     * @exception IllegalArgumentException if the context relative path
     *  does not begin with a '/' character
     * @exception Exception if a different error occurs
     */
    protected HtmlPage page(String path) throws Exception
    {

        HtmlPage page = (HtmlPage) webClient.getPage(url(path));
        this.page = page;
        return (page);

    }

    /**
     * <p>Reset the stored page reference to the specified value.  This is
     * useful for scenarios testing resubmit of the same page (simulating the
     * user pressing the back button and then submitting again).</p>
     *
     * @param page Previously saved page to which to reset
     */
    protected void reset(HtmlPage page)
    {

        this.page = page;

    }

    /**
     * <p>Submit the current page, using the specified component, and retrieve
     * the subsequent page, saving a reference so that other utility methods
     * may be used to retrieve information from it.</p>
     *
     * @param submit Submit button component to click
     *
     * @exception IOException if an input/output error occurs
     */
    protected HtmlPage submit(HtmlSubmitInput submit) throws IOException
    {

        HtmlPage page = (HtmlPage) submit.click();
        this.page = page;
        return page;

    }

    /**
     * <p>Return the page title from the most recently retrieved page.
     * Any leading and trailing whitespace will be trimmed.</p>
     *
     * @exception Exception if an error occurs
     */
    protected String title() throws Exception
    {

        return (page.getTitleText().trim());

    }

    /**
     * <p>Calculate and return an absolute URL for the specified context
     * relative path, which must begin with a '/' character.</p>
     *
     * @param path Context relative path
     *
     * @exception IllegalArgumentException if the context relative path
     *  does not begin with a '/' character
     * @exception Exception if a different error ocurs
     */
    protected URL url(String path) throws Exception
    {

        if (path.charAt(0) != '/')
        {
            throw new IllegalArgumentException("Context path '" + path
                    + "' does not start with '/'");
        }
        return new URL(url, path.substring(1));

    }

}
