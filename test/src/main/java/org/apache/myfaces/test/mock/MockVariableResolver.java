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

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.VariableResolver;

import java.util.Map;

/**
 * <p>Mock implementation of <code>VariableResolver</code>.</p>
 *
 * <p>This implementation recognizes the standard scope names
 * <code>applicationScope</code>, <code>facesContext</code>,
 * <code>RequestScope</code>, and
 * <code>sessionScope</code>, plus it knows how to search in ascending
 * scopes for non-reserved names.</p>
 *
 * $Id$
 * @since 1.0.0
 */

public class MockVariableResolver extends VariableResolver
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a default instance.</p>
     */
    public MockVariableResolver()
    {
    }

    // ----------------------------------------------------- Mock Object Methods

    // ------------------------------------------------------ Instance Variables

    // ------------------------------------------------ VariableResolver Methods

    /** {@inheritDoc} */
    public Object resolveVariable(FacesContext context, String name)
    {

        if ((context == null) || (name == null))
        {
            throw new NullPointerException();
        }

        // Check for magic names
        if ("application".equals(name))
        {
            return external().getContext();
        }
        else if ("applicationScope".equals(name))
        {
            return external().getApplicationMap();
        }
        else if ("cookie".equals(name))
        {
            return external().getRequestCookieMap();
        }
        else if ("facesContext".equals(name))
        {
            return FacesContext.getCurrentInstance();
        }
        else if ("header".equals(name))
        {
            return external().getRequestHeaderMap();
        }
        else if ("headerValues".equals(name))
        {
            return external().getRequestHeaderValuesMap();
        }
        else if ("param".equals(name))
        {
            return external().getRequestParameterMap();
        }
        else if ("paramValues".equals(name))
        {
            return external().getRequestParameterValuesMap();
        }
        else if ("request".equals(name))
        {
            return external().getRequest();
        }
        else if ("requestScope".equals(name))
        {
            return external().getRequestMap();
        }
        else if ("response".equals(name))
        {
            return external().getResponse();
        }
        else if ("session".equals(name))
        {
            return external().getSession(true);
        }
        else if ("sessionScope".equals(name))
        {
            return external().getSessionMap();
        }
        else if ("view".equals(name))
        {
            return FacesContext.getCurrentInstance().getViewRoot();
        }

        // Search ascending scopes for non-magic names
        Map map = null;
        map = external().getRequestMap();
        if (map.containsKey(name))
        {
            return map.get(name);
        }
        map = external().getSessionMap();
        if ((map != null) && (map.containsKey(name)))
        {
            return map.get(name);
        }
        map = external().getApplicationMap();
        if (map.containsKey(name))
        {
            return map.get(name);
        }

        // No such variable can be found
        return null;

    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>Return the <code>ExternalContext</code> for this request.</p>
     */
    private ExternalContext external()
    {

        return FacesContext.getCurrentInstance().getExternalContext();

    }

}
