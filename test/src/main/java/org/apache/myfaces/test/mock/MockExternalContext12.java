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
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>Mock implementation of <code>ExternalContext</code> that includes the semantics
 * added by JavaServer Faces 1.2.</p>
 *
 * $Id$
 *
 * @since 1.0.0
 */
public abstract class MockExternalContext12 extends MockExternalContext10
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

    @Override
    public String getRequestCharacterEncoding()
    {
        return this.request.getCharacterEncoding();
    }

    @Override
    public String getRequestContentType()
    {
        return this.request.getContentType();
    }

    @Override
    public String getResponseCharacterEncoding()
    {
        return this.response.getCharacterEncoding();
    }

    @Override
    public String getResponseContentType()
    {
        return this.response.getContentType();
    }

    @Override
    public void setRequest(Object request)
    {
        this.request = (HttpServletRequest) request;
    }

    @Override
    public void setRequestCharacterEncoding(String encoding)
            throws UnsupportedEncodingException
    {
        this.request.setCharacterEncoding(encoding);
    }

    @Override
    public void setResponse(Object response)
    {
        this.response = (HttpServletResponse) response;
    }

    @Override
    public void setResponseCharacterEncoding(String encoding)
    {
        this.response.setCharacterEncoding(encoding);
    }

}
