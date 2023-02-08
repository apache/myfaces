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

import java.util.Enumeration;
import java.util.Hashtable;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

/**
 * <p>Mock implementation of <code>ServletConfig</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */

public class MockServletConfig implements ServletConfig
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a default instance.</p>
     */
    public MockServletConfig()
    {
    }

    /**
     * <p>Construct an instance associated with the specified
     * servlet context.</p>
     *
     * @param context The associated ServletContext
     */
    public MockServletConfig(ServletContext context)
    {
        setServletContext(context);
    }

    // ----------------------------------------------------- Mock Object Methods

    /**
     * <p>Add a servlet initialization parameter.</p>
     *
     * @param name Parameter name
     * @param value Parameter value
     */
    public void addInitParameter(String name, String value)
    {

        parameters.put(name, value);

    }

    /**
     * <p>Set the servlet context for this application.</p>
     *
     * @param context The new servlet context
     */
    public void setServletContext(ServletContext context)
    {

        this.context = context;

    }

    // ------------------------------------------------------ Instance Variables

    private ServletContext context;
    private Hashtable parameters = new Hashtable();

    // --------------------------------------------------- ServletConfig Methods

    @Override
    public String getInitParameter(String name)
    {

        return (String) parameters.get(name);

    }

    @Override
    public Enumeration getInitParameterNames()
    {

        return parameters.keys();

    }

    @Override
    public ServletContext getServletContext()
    {

        return this.context;

    }

    @Override
    public String getServletName()
    {

        return "MockServlet";

    }

}
