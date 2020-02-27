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

package org.apache.myfaces.test.el;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;
import jakarta.faces.context.FacesContext;

/**
 * <p>Mock implementation of <code>ELContext</code>.</p>
 *
 * @since 1.0.0
 */

public class MockELContext extends ELContext
{

    // ------------------------------------------------------------ Constructors

    /** Creates a new instance of MockELContext */
    public MockELContext()
    {
    }

    // ------------------------------------------------------ Instance Variables

    private Map contexts = new HashMap();
    private FunctionMapper functionMapper = new MockFunctionMapper();
    private Locale locale = Locale.getDefault();
    private boolean propertyResolved;
    private VariableMapper variableMapper = new MockVariableMapper();

    // ----------------------------------------------------- Mock Object Methods

    // ------------------------------------------------------- ELContext Methods

    /** {@inheritDoc} */
    public Object getContext(Class key)
    {
        if (key == null)
        {
            throw new NullPointerException();
        }
        return contexts.get(key);
    }

    /** {@inheritDoc} */
    public ELResolver getELResolver()
    {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getApplication().getELResolver();
    }

    /** {@inheritDoc} */
    public FunctionMapper getFunctionMapper()
    {
        return this.functionMapper;
    }

    /** {@inheritDoc} */
    public Locale getLocale()
    {
        return this.locale;
    }

    /** {@inheritDoc} */
    public boolean isPropertyResolved()
    {
        return this.propertyResolved;
    }

    /** {@inheritDoc} */
    public void putContext(Class key, Object value)
    {
        if ((key == null) || (value == null))
        {
            throw new NullPointerException();
        }
        contexts.put(key, value);
    }

    /** {@inheritDoc} */
    public void setPropertyResolved(boolean propertyResolved)
    {
        this.propertyResolved = propertyResolved;
    }

    /** {@inheritDoc} */
    public VariableMapper getVariableMapper()
    {
        return this.variableMapper;
    }

    /** {@inheritDoc} */
    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

}
