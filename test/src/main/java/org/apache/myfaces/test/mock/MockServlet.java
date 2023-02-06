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
package org.apache.myfaces.test.mock;

import java.io.IOException;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * <p>Mock implementation of <code>Servlet</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */

public class MockServlet implements Servlet
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Create a default Servlet instance.</p>
     */
    public MockServlet()
    {
    }

    /**
     * <p>Create a new Servlet with the specified ServletConfig.</p>
     *
     * @param config The new ServletConfig instance
     */
    public MockServlet(ServletConfig config) throws ServletException
    {
        init(config);
    }

    // ----------------------------------------------------- Mock Object Methods

    /**
     * <p>Set the <code>ServletConfig</code> instance for this servlet.</p>
     *
     * @param config The new ServletConfig instance
     */
    public void setServletConfig(ServletConfig config)
    {

        this.config = config;

    }

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>The <code>ServletConfig</code> instance for this servlet.</p>
     */
    private ServletConfig config;

    // --------------------------------------------------------- Servlet Methods

    @Override
    public void destroy()
    {
    }

    @Override
    public ServletConfig getServletConfig()
    {

        return this.config;

    }

    @Override
    public String getServletInfo()
    {

        return "MockServlet";

    }

    @Override
    public void init(ServletConfig config) throws ServletException
    {

        this.config = config;

    }

    @Override
    public void service(ServletRequest request, ServletResponse response)
            throws IOException, ServletException
    {

        // Do nothing by default

    }

}
