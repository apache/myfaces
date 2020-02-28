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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.faces.application.Application;
import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.MethodBinding;
import jakarta.faces.el.MethodNotFoundException;
import jakarta.faces.el.ReferenceSyntaxException;
import jakarta.faces.el.ValueBinding;

/**
 * <p>Mock implementation of <code>MethodBinding</code>.</p>
 *
 * <p>This implementation is subject to the following restrictions:</p>
 * <ul>
 * <li>The portion of the method reference expression before the final
 *     "." must conform to the limitations of {@link MockValueBinding}.</li>
 * <li>The name of the method to be executed cannot be delimited by "[]".</li>
 * </ul>
 * 
 * @since 1.0.0
 */

public class MockMethodBinding extends MethodBinding implements StateHolder
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a default instance.</p>
     */
    public MockMethodBinding()
    {
    }

    /**
     * <p>Construct a configured instance.</p>
     *
     * @param application Application instance for this application
     * @param ref Method binding expression to be parsed
     * @param args Signature of this method
     */
    public MockMethodBinding(Application application, String ref, Class[] args)
    {

        this.application = application;
        this.args = args;
        if (ref.startsWith("#{") && ref.endsWith("}"))
        {
            ref = ref.substring(2, ref.length() - 1);
        }
        this.ref = ref;
        int period = ref.lastIndexOf(".");
        if (period < 0)
        {
            throw new ReferenceSyntaxException(ref);
        }
        vb = application.createValueBinding(ref.substring(0, period));
        name = ref.substring(period + 1);
        if (name.length() < 1)
        {
            throw new ReferenceSyntaxException(ref);
        }

    }

    // ------------------------------------------------------ Instance Variables

    private Application application;
    private Class args[];
    private String name;
    private String ref;
    private ValueBinding vb;

    // --------------------------------------------------- MethodBinding Methods

    /** {@inheritDoc} */
    public Object invoke(FacesContext context, Object[] params)
            throws EvaluationException, MethodNotFoundException
    {

        if (context == null)
        {
            throw new NullPointerException();
        }
        Object base = vb.getValue(context);
        if (base == null)
        {
            throw new EvaluationException(
                    "Cannot find object via expression \""
                            + vb.getExpressionString() + "\"");
        }
        Method method = method(base);
        try
        {
            return (method.invoke(base, params));
        }
        catch (IllegalAccessException e)
        {
            throw new EvaluationException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new EvaluationException(e.getTargetException());
        }

    }

    /** {@inheritDoc} */
    public Class getType(FacesContext context)
    {

        Object base = vb.getValue(context);
        Method method = method(base);
        Class returnType = method.getReturnType();
        if ("void".equals(returnType.getName()))
        {
            return (null);
        }
        else
        {
            return (returnType);
        }

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
        Object values[] = new Object[4];
        values[0] = name;
        values[1] = ref;
        values[2] = UIComponentBase.saveAttachedState(context, vb);
        values[3] = args;
        return (values);
    }

    /** {@inheritDoc} */
    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[]) state;
        name = (String) values[0];
        ref = (String) values[1];
        vb = (ValueBinding) UIComponentBase.restoreAttachedState(context,
                values[2]);
        args = (Class[]) values[3];
    }

    /**
     * <p>Flag indicating this is a transient instance.</p>
     */
    private boolean transientFlag = false;

    /** {@inheritDoc} */
    public boolean isTransient()
    {
        return (this.transientFlag);
    }

    /** {@inheritDoc} */
    public void setTransient(boolean transientFlag)
    {
        this.transientFlag = transientFlag;
    }

    /** {@inheritDoc} */
    public int hashCode()
    {
        if (ref == null)
        {
            return 0;
        }
        else
        {
            return ref.hashCode();
        }
    }

    /** {@inheritDoc} */
    public boolean equals(Object otherObj)
    {
        MockMethodBinding other = null;

        if (!(otherObj instanceof MockMethodBinding))
        {
            return false;
        }
        other = (MockMethodBinding) otherObj;
        // test object reference equality
        if (this.ref != other.ref)
        {
            // test object equality
            if (null != this.ref && null != other.ref)
            {
                if (!this.ref.equals(other.ref))
                {
                    return false;
                }
            }
            return false;
        }
        // no need to test name, since it flows from ref.
        // test our args array
        if (this.args != other.args)
        {
            if (this.args.length != other.args.length)
            {
                return false;
            }
            for (int i = 0, len = this.args.length; i < len; i++)
            {
                if (this.args[i] != other.args[i])
                {
                    if (!this.ref.equals(other.ref))
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>Return the <code>Method</code> to be called.</p>
     *
     * @param base Base object from which to extract the method reference
     */
    Method method(Object base)
    {

        Class clazz = base.getClass();
        try
        {
            return (clazz.getMethod(name, args));
        }
        catch (NoSuchMethodException e)
        {
            throw new MethodNotFoundException(ref + ": " + e.getMessage());
        }

    }

}
