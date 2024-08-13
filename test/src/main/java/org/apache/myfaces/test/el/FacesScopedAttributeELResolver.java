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

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotFoundException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

/**
 * <p><code>ELResolver</code> implementation that accesses scoped variables
 * in the current request context.  See the JSF 1.2 Specification, section
 * 5.6.2.7, for requirements implemented by this class.</p>
 *
 * @since 1.0.0
 */
public class FacesScopedAttributeELResolver extends ELResolver
{

    /**
     * <p>Return the most general type this resolver accepts for the
     * <code>property</code> argument.</p>
     */
    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base)
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
     * <p>Return the Java type of the specified property.</p>
     *
     * @param context <code>ELContext</code> for evaluating this value
     * @param base Base object against which this evaluation occurs
     *  (must be null because we are evaluating a top level variable)
     * @param property Property name to be accessed
     */
    @Override
    public Class<?> getType(ELContext context, Object base, Object property)
    {

        if (base != null)
        {
            return null;
        }
        if (property == null)
        {
            throw new PropertyNotFoundException("No property specified");
        }
        context.setPropertyResolved(true);
        return Object.class;

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
    @Override
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
        Object value = null;
        value = econtext.getRequestMap().get(property);
        if (value != null)
        {
            context.setPropertyResolved(true);
            return value;
        }
        value = econtext.getSessionMap().get(property);
        if (value != null)
        {
            context.setPropertyResolved(true);
            return value;
        }
        value = econtext.getApplicationMap().get(property);
        if (value != null)
        {
            context.setPropertyResolved(true);
            return value;
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
    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property)
    {

        if (base == null)
        {
            context.setPropertyResolved(true);
            return false;
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
    @Override
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

        context.setPropertyResolved(true);
        String key = property.toString();
        Object result = null;
        FacesContext fcontext = (FacesContext) context
                .getContext(FacesContext.class);
        ExternalContext econtext = fcontext.getExternalContext();

        if (econtext.getRequestMap().containsKey(property))
        {
            econtext.getRequestMap().put(key, value);
        }
        else if (econtext.getSessionMap().containsKey(property))
        {
            econtext.getSessionMap().put(key, value);
        }
        else if (econtext.getApplicationMap().containsKey(property))
        {
            econtext.getApplicationMap().put(key, value);
        }
        else
        {
            econtext.getRequestMap().put(key, value);
        }

    }

}
