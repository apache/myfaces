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

import java.util.Iterator;

import jakarta.el.ELContext;
import jakarta.el.PropertyNotFoundException;
import jakarta.faces.FacesException;

/**
 * <p><code>ELResolver</code> implementation that wraps the legacy (JSF 1.1)
 * <code>VariableResolver</code> chain.  See the JSF 1.2 Specification, section
 * 5.6.1.5, for requirements implemented by this class.</p>
 *
 * @since 1.0.0
 */
public class FacesVariableResolverChainWrapper extends AbstractELResolver
{

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

        return null;

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

        if ((base == null) && (property == null))
        {
            throw new PropertyNotFoundException("No property specified");
        }
        return null;

    }

    /**
     * <p>Evaluate with the legacy variable resolver chain and return
     * the value.</p>
     *
     * @param context <code>ELContext</code> for evaluating this value
     * @param base Base object against which this evaluation occurs
     *  (must be null because we are evaluating a top level variable)
     * @param property Property name to be accessed
     */
    public Object getValue(ELContext context, Object base, Object property)
    {
        throw new FacesException("not supported anymore");

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

        if ((base == null) && (property == null))
        {
            throw new PropertyNotFoundException("No property specified");
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

        if ((base == null) && (property == null))
        {
            throw new PropertyNotFoundException("No property specified");
        }

    }

}
