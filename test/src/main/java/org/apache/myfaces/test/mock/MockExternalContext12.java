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

import java.io.UnsupportedEncodingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>Mock implementation of <code>ExternalContext</code> that includes the semantics
 * added by JavaServer Faces 1.2.</p>
 *
 * $Id$
 *
 * @since 1.0.0
 */
public class MockExternalContext12 extends MockExternalContext
{

    // ------------------------------------------------------------ Constructors

    public MockExternalContext12(ServletContext context,
            HttpServletRequest request, HttpServletResponse response)
    {
        super(context, request, response);
    }

    // ------------------------------------------------------ Instance Variables

    // ----------------------------------------------------- Mock Object Methods

    // ------------------------------------------------- ExternalContext Methods

    /** {@inheritDoc} */
    public String getRequestCharacterEncoding()
    {

        return this.request.getCharacterEncoding();

    }

    /** {@inheritDoc} */
    public String getRequestContentType()
    {

        return this.request.getContentType();

    }

    /** {@inheritDoc} */
    public String getResponseCharacterEncoding()
    {

        return this.response.getCharacterEncoding();

    }

    /** {@inheritDoc} */
    public String getResponseContentType()
    {

        return this.response.getContentType();

    }

    /** {@inheritDoc} */
    public void setRequest(Object request)
    {

        this.request = (HttpServletRequest) request;

    }

    /** {@inheritDoc} */
    public void setRequestCharacterEncoding(String encoding)
            throws UnsupportedEncodingException
    {

        this.request.setCharacterEncoding(encoding);

    }

    /** {@inheritDoc} */
    public void setResponse(Object response)
    {

        this.response = (HttpServletResponse) response;

    }

    /** {@inheritDoc} */
    public void setResponseCharacterEncoding(String encoding)
    {

        this.response.setCharacterEncoding(encoding);

    }

}
