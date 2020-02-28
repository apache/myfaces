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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.faces.application.Application;
import jakarta.faces.component.StateHolder;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.PropertyNotFoundException;
import jakarta.faces.el.PropertyResolver;
import jakarta.faces.el.ReferenceSyntaxException;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.el.VariableResolver;

/**
 * <p>Mock implementation of <code>ValueBinding</code>.</p>
 * 
 * @since 1.0.0
 */
public class MockValueBinding extends ValueBinding implements StateHolder
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a default instance.</p>
     */
    public MockValueBinding()
    {

        this(null, null);

    }

    /**
     * <p>Construct a new value binding for the specified expression.</p>
     *
     * @param application Application instance to be wrapped
     * @param ref Expression to be wrapped
     */
    public MockValueBinding(Application application, String ref)
    {

        this.application = application;
        if (ref != null)
        {
            if (ref.startsWith("#{") && ref.endsWith("}"))
            {
                ref = ref.substring(2, ref.length() - 1);
            }
        }
        this.ref = ref;

    }

    // ----------------------------------------------------- Mock Object Methods

    /**
     * <p>Return the expression string for this value binding.</p>
     */
    public String ref()
    {

        return this.ref;

    }

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>The <code>Application</code> instance for this application.</p>
     */
    private transient Application application; // Restored as necessary

    /**
     * <p>The expression this value binding will evaluate.</p>
     */
    private String ref;

    // ---------------------------------------------------- ValueBinding Methods

    /** {@inheritDoc} */
    public Object getValue(FacesContext context) throws EvaluationException,
            PropertyNotFoundException
    {

        if (context == null)
        {
            throw new NullPointerException();
        }
        List names = parse(ref);

        // Resolve the variable name
        VariableResolver vr = application().getVariableResolver();
        String name = (String) names.get(0);
        Object base = vr.resolveVariable(context, name);
        if (names.size() < 2)
        {
            return (base);
        }

        // Resolve the property names
        PropertyResolver pr = application().getPropertyResolver();
        for (int i = 1; i < names.size(); i++)
        {
            base = pr.getValue(base, (String) names.get(i));
        }

        // Return the resolved value
        return (base);

    }

    /** {@inheritDoc} */
    public void setValue(FacesContext context, Object value)
            throws EvaluationException, PropertyNotFoundException
    {

        if (context == null)
        {
            throw new NullPointerException();
        }
        List names = parse(ref);

        // Resolve the variable name
        VariableResolver vr = application().getVariableResolver();
        String name = (String) names.get(0);
        Object base = vr.resolveVariable(context, name);
        if (names.size() < 2)
        {
            if ("applicationScope".equals(name) || "requestScope".equals(name)
                    || "sessionScope".equals(name))
            {
                throw new ReferenceSyntaxException("Cannot set '" + name + "'");
            }
            Map map = econtext().getRequestMap();
            if (map.containsKey(name))
            {
                map.put(name, value);
                return;
            }
            map = econtext().getSessionMap();
            if ((map != null) && (map.containsKey(name)))
            {
                map.put(name, value);
                return;
            }
            map = econtext().getApplicationMap();
            if (map.containsKey(name))
            {
                map.put(name, value);
                return;
            }
            econtext().getRequestMap().put(name, value);
            return;
        }

        // Resolve the property names
        PropertyResolver pr = application().getPropertyResolver();
        for (int i = 1; i < (names.size() - 1); i++)
        {
            // System.out.println("  property=" + names.get(i));
            base = pr.getValue(base, (String) names.get(i));
        }

        // Update the last property
        pr.setValue(base, (String) names.get(names.size() - 1), value);

    }

    /** {@inheritDoc} */
    public boolean isReadOnly(FacesContext context)
            throws PropertyNotFoundException
    {

        if (context == null)
        {
            throw new NullPointerException();
        }
        List names = parse(ref);

        // Resolve the variable name
        VariableResolver vr = application().getVariableResolver();
        String name = (String) names.get(0);
        Object base = vr.resolveVariable(context, name);
        if (names.size() < 2)
        {
            return true;
        }

        // Resolve the property names
        PropertyResolver pr = application().getPropertyResolver();
        for (int i = 1; i < names.size() - 1; i++)
        {
            base = pr.getValue(base, (String) names.get(i));
        }

        // Return the read only state of the final property
        return pr.isReadOnly(base, (String) names.get(names.size() - 1));

    }

    /** {@inheritDoc} */
    public Class getType(FacesContext context) throws PropertyNotFoundException
    {

        if (context == null)
        {
            throw new NullPointerException();
        }
        List names = parse(ref);

        // Resolve the variable name
        VariableResolver vr = application().getVariableResolver();
        String name = (String) names.get(0);
        Object base = vr.resolveVariable(context, name);
        if (names.size() < 2)
        {
            return base.getClass();
        }

        // Resolve the property names
        PropertyResolver pr = application().getPropertyResolver();
        for (int i = 1; i < names.size() - 1; i++)
        {
            base = pr.getValue(base, (String) names.get(i));
        }

        // Return the type of the final property
        return pr.getType(base, (String) names.get(names.size() - 1));

    }

    /** {@inheritDoc} */
    public String getExpressionString()
    {

        return "#{" + ref + "}";

    }

    // ----------------------------------------------------- StateHolder Methods

    /** {@inheritDoc} */
    public Object saveState(FacesContext context)
    {

        Object[] values = new Object[1];
        values[0] = ref;
        return values;

    }

    /** {@inheritDoc} */
    public void restoreState(FacesContext context, Object state)
    {

        Object[] values = (Object[]) state;
        ref = (String) values[0];

    }

    /**
     * <p>Flag indicating that this value is transient.</p>
     */
    private boolean transientFlag = false;

    /** {@inheritDoc} */
    public boolean isTransient()
    {

        return this.transientFlag;

    }

    /** {@inheritDoc} */
    public void setTransient(boolean transientFlag)
    {

        this.transientFlag = transientFlag;

    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>Return the relevant <code>Application</code> instance.</p>
     */
    private Application application()
    {

        if (application == null)
        {
            application = FacesContext.getCurrentInstance().getApplication();
        }
        return (application);

    }

    /**
     * <p>Return the relevant <code>ExternalContext</code> instance.</p>
     */
    private ExternalContext econtext()
    {

        return (FacesContext.getCurrentInstance().getExternalContext());

    }

    /**
     * <p>Return a list of the expression elements in this expression.</p>
     *
     * @param ref Expression to be parsed
     */
    private List parse(String ref)
    {

        List names = new ArrayList();
        StringBuffer expr = new StringBuffer(ref);
        boolean isBlockOn = false;
        for (int i = expr.length() - 1; i > -1; i--)
        {
            if (expr.charAt(i) == ' ')
            {
                expr.deleteCharAt(i);
            }
            else if (expr.charAt(i) == ']')
            {
                expr.deleteCharAt(i);
            }
            else if (expr.charAt(i) == '[')
            {
                expr.deleteCharAt(i);
            }
            else if (expr.charAt(i) == '\'')
            {
                if (!isBlockOn)
                {
                    expr.deleteCharAt(i);
                }
                else
                {
                    names.add(0, expr.substring(i + 1));
                    expr.delete(i, expr.length());
                }
                isBlockOn = !isBlockOn;
            }
            else if (expr.charAt(i) == '.' && !isBlockOn)
            {
                names.add(0, expr.substring(i + 1));
                expr.delete(i, expr.length());
            }
        }

        if (expr.length() > 0)
        {
            names.add(0, expr.toString());
        }

        if (names.size() < 1)
        {
            throw new ReferenceSyntaxException("No expression in '" + ref + "'");
        }
        for (int i = 0; i < names.size(); i++)
        {
            String name = (String) names.get(i);
            if (name.length() < 1)
            {
                throw new ReferenceSyntaxException("Invalid expression '" + ref
                        + "'");
            }
        }
        return (names);

    }

}
