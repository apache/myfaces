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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

/**
 * <p><code>ELResolver</code> implementation that accesses implicit objects
 * in the current request context.  See the JSF 1.2 Specification, section
 * 5.6.2.1, for requirements implemented by this class.</p>
 *
 * @since 1.0.0
 */
public class FacesImplicitObjectELResolver extends AbstractELResolver
{

    /**
     * <p>The names of all implicit objects recognized by this resolver.</p>
     */
    private static final String[] NAMES = { "application", "applicationScope",
            "cookie", "facesContext", "header", "headerValues", "initParam",
            "param", "paramValues", "request", "requestScope", "session",
            "sessionScope", "view" };

    /**
     * <p>The property types corresponding to the implicit object names.</p>
     */
    private static final Class[] TYPES = { Object.class, Map.class, Map.class,
            FacesContext.class, Map.class, Map.class, Map.class, Map.class,
            Map.class, Object.class, Map.class, Object.class, Map.class,
            UIViewRoot.class };

    /**
     * <p>The settable value types corresponding to the implicit
     * object names.</p>
     */
    private static final Class[] VALUES = { null, Object.class, null, null,
            null, null, null, null, null, null, Object.class, null,
            Object.class, null };

    /**
     * <p>Return the most general type this resolver accepts for the
     * <code>property</code> argument.</p>
     */
    public Class getCommonPropertyType(ELContext context, Object base)
    {

        if (base != null)
        {
            return null;
        }
        else
        {
            return String.class;
        }

    }

    /**
     * <p>Return an <code>Iterator</code> over the attributes that this
     * resolver knows how to deal with.</p>
     *
     * @param context <code>ELContext</code> for evaluating this value
     * @param base Base object against which this evaluation occurs
     */
    public Iterator getFeatureDescriptors(ELContext context, Object base)
    {

        if (base != null)
        {
            return null;
        }

        // Create the variables we will need
        FacesContext fcontext = (FacesContext) context
                .getContext(FacesContext.class);
        List descriptors = new ArrayList();

        // Add feature descriptors for each implicit object
        for (int i = 0; i < NAMES.length; i++)
        {
            descriptors.add(descriptor(NAMES[i], NAMES[i], NAMES[i], false,
                    false, true, TYPES[i], true));
        }

        // Return the accumulated descriptors
        return descriptors.iterator();

    }

    /**
     * <p>Return the Java type of the specified property.</p>
     *
     * @param context <code>ELContext</code> for evaluating this value
     * @param base Base object against which this evaluation occurs
     *  (must be null because we are evaluating a top level variable)
     * @param property Property name to be accessed
     */
    public Class getType(ELContext context, Object base, Object property)
    {

        if (base != null)
        {
            return null;
        }
        if (property == null)
        {
            throw new PropertyNotFoundException("No property specified");
        }
        String name = property.toString();
        for (int i = 0; i < NAMES.length; i++)
        {
            if (name.equals(NAMES[i]))
            {
                context.setPropertyResolved(true);
                return VALUES[i];
            }
        }
        return null;

    }

    /**
     * <p>Return an existing scoped object for the specified name (if any);
     * otherwise, return <code>null</code>.</p>
     *
     * @param context <code>ELContext</code> for evaluating this value
     * @param base Base object against which this evaluation occurs
     *  (must be null because we are evaluating a top level variable)
     * @param property Property name to be accessed
     */
    public Object getValue(ELContext context, Object base, Object property)
    {

        if (base != null)
        {
            return null;
        }
        if (property == null)
        {
            throw new PropertyNotFoundException("No property specified");
        }

        FacesContext fcontext = (FacesContext) context
                .getContext(FacesContext.class);
        ExternalContext econtext = fcontext.getExternalContext();
        String name = property.toString();

        if (name.equals("application"))
        {
            context.setPropertyResolved(true);
            return econtext.getContext();
        }
        else if (name.equals("applicationScope"))
        {
            context.setPropertyResolved(true);
            return econtext.getApplicationMap();
        }
        else if (name.equals("cookie"))
        {
            context.setPropertyResolved(true);
            return econtext.getRequestCookieMap();
        }
        else if (name.equals("facesContext"))
        {
            context.setPropertyResolved(true);
            return fcontext;
        }
        else if (name.equals("header"))
        {
            context.setPropertyResolved(true);
            return econtext.getRequestHeaderMap();
        }
        else if (name.equals("headerValues"))
        {
            context.setPropertyResolved(true);
            return econtext.getRequestHeaderValuesMap();
        }
        else if (name.equals("initParam"))
        {
            context.setPropertyResolved(true);
            return econtext.getInitParameterMap();
        }
        else if (name.equals("param"))
        {
            context.setPropertyResolved(true);
            return econtext.getRequestParameterMap();
        }
        else if (name.equals("paramValues"))
        {
            context.setPropertyResolved(true);
            return econtext.getRequestParameterValuesMap();
        }
        else if (name.equals("request"))
        {
            context.setPropertyResolved(true);
            return econtext.getRequest();
        }
        else if (name.equals("requestScope"))
        {
            context.setPropertyResolved(true);
            return econtext.getRequestMap();
        }
        else if (name.equals("session"))
        {
            context.setPropertyResolved(true);
            return econtext.getSession(true);
        }
        else if (name.equals("sessionScope"))
        {
            context.setPropertyResolved(true);
            return econtext.getSessionMap();
        }
        else if (name.equals("view"))
        {
            context.setPropertyResolved(true);
            return fcontext.getViewRoot();
        }

        return null;

    }

    /**
     * <p>Return <code>true</code> if the specified property is read only.</p>
     *
     * @param context <code>ELContext</code> for evaluating this value
     * @param base Base object against which this evaluation occurs
     *  (must be null because we are evaluating a top level variable)
     * @param property Property name to be accessed
     */
    public boolean isReadOnly(ELContext context, Object base, Object property)
    {

        if (base != null)
        {
            return false;
        }
        if (property == null)
        {
            throw new PropertyNotFoundException("No property specified");
        }
        String name = property.toString();
        for (int i = 0; i < NAMES.length; i++)
        {
            if (name.equals(NAMES[i]))
            {
                context.setPropertyResolved(true);
                return true;
            }
        }
        return false;

    }

    /**
     * <p>Set the value of a scoped object for the specified name.</p>
     *
     * @param context <code>ELContext</code> for evaluating this value
     * @param base Base object against which this evaluation occurs
     *  (must be null because we are evaluating a top level variable)
     * @param property Property name to be accessed
     * @param value New value to be set
     */
    public void setValue(ELContext context, Object base, Object property,
            Object value)
    {

        if (base != null)
        {
            return;
        }
        if (property == null)
        {
            throw new PropertyNotFoundException("No property specified");
        }

        String name = property.toString();
        for (int i = 0; i < NAMES.length; i++)
        {
            if (name.equals(NAMES[i]))
            {
                context.setPropertyResolved(true);
                throw new PropertyNotWritableException(name);
            }
        }

    }

}
