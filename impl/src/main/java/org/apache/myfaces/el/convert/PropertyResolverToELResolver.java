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
package org.apache.myfaces.el.convert;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.PropertyNotWritableException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.PropertyResolver;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.List;
import org.apache.myfaces.el.DefaultPropertyResolver;

/**
 * Wrapper that converts a VariableResolver into an ELResolver. See JSF 1.2 spec section 5.6.1.6
 *
 * @author Stan Silvert (latest modification by $Author$)
 * @author Mathias Broekelmann
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public final class PropertyResolverToELResolver extends ELResolver
{
    private PropertyResolver propertyResolver;

    private ExpressionFactory expressionFactory;
    
    private boolean isDefaultLegacyPropertyResolver;

    /**
     * Creates a new instance of PropertyResolverToELResolver
     */
    public PropertyResolverToELResolver(final PropertyResolver propertyResolver)
    {
        this.propertyResolver = propertyResolver;
        this.isDefaultLegacyPropertyResolver = (propertyResolver instanceof DefaultPropertyResolver);
    }

    @Override
    public void setValue(final ELContext context, final Object base, final Object property, final Object value)
        throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException
    {
        if (isDefaultLegacyPropertyResolver)
        {
            return;
        }
        if (base == null || property == null)
        {
            return;
        }

        try
        {
            context.setPropertyResolved(true);
            if (needsCoersion(base))
            {
                propertyResolver.setValue(base, coerceToInt(property), value);
            }
            else
            {
                propertyResolver.setValue(base, property, value);
            }
            // see: https://issues.apache.org/jira/browse/MYFACES-1670
            context.setPropertyResolved(
                FacesContext.getCurrentInstance().getELContext().isPropertyResolved());

        }
        catch (jakarta.faces.el.PropertyNotFoundException e)
        {
            context.setPropertyResolved(false);
            throw new PropertyNotFoundException(e.getMessage(), e);
        }
        catch (EvaluationException e)
        {
            context.setPropertyResolved(false);
            throw new ELException(e.getMessage(), e);
        }
        catch (RuntimeException e)
        {
            context.setPropertyResolved(false);
            throw e;
        }
    }

    @Override
    public boolean isReadOnly(final ELContext context, final Object base, final Object property)
            throws NullPointerException, PropertyNotFoundException, ELException
    {
        if (isDefaultLegacyPropertyResolver)
        {
            return false;
        }
        if (base == null || property == null)
        {
            return true;
        }

        try
        {
            boolean result;
            context.setPropertyResolved(true);
            if (needsCoersion(base))
            {
                result = propertyResolver.isReadOnly(base, coerceToInt(property));
            }
            else
            {
                result = propertyResolver.isReadOnly(base, property);
            }

            // see: https://issues.apache.org/jira/browse/MYFACES-1670
            context.setPropertyResolved(
                FacesContext.getCurrentInstance().getELContext().isPropertyResolved());
            return result;
        }
        catch (jakarta.faces.el.PropertyNotFoundException e)
        {
            context.setPropertyResolved(false);
            throw new PropertyNotFoundException(e.getMessage(), e);
        }
        catch (EvaluationException e)
        {
            context.setPropertyResolved(false);
            throw new ELException(e.getMessage(), e);
        }
        catch (RuntimeException e)
        {
            context.setPropertyResolved(false);
            throw e;
        }
    }

    @Override
    public Object getValue(final ELContext context, final Object base, final Object property)
            throws NullPointerException, PropertyNotFoundException, ELException
    {
        if (isDefaultLegacyPropertyResolver)
        {
            return null;
        }        
        if (base == null || property == null)
        {
            return null;
        }

        try
        {
            context.setPropertyResolved(true);
            Object value;
            if (needsCoersion(base))
            {
                value = propertyResolver.getValue(base, coerceToInt(property));
            }
            else
            {
                value = propertyResolver.getValue(base, property);
            }

            // see: https://issues.apache.org/jira/browse/MYFACES-1670
            context.setPropertyResolved(
                FacesContext.getCurrentInstance().getELContext().isPropertyResolved());

            return value;
        }
        catch (jakarta.faces.el.PropertyNotFoundException e)
        {
            context.setPropertyResolved(false);
            throw new PropertyNotFoundException(e.getMessage(), e);
        }
        catch (EvaluationException e)
        {
            context.setPropertyResolved(false);
            throw new ELException(e.getMessage(), e);
        }
        catch (RuntimeException e)
        {
            context.setPropertyResolved(false);
            throw e;
        }
    }

    @Override
    public Class<?> getType(final ELContext context, final Object base, final Object property)
            throws NullPointerException, PropertyNotFoundException, ELException
    {
        if (isDefaultLegacyPropertyResolver)
        {
            return null;
        }
        if (base == null || property == null)
        {
            return null;
        }

        try
        {
            context.setPropertyResolved(true);
            Class<?> value;
            if (needsCoersion(base))
            {
                value = propertyResolver.getType(base, coerceToInt(property));
            }
            else
            {
                value = propertyResolver.getType(base, property);
            }

            // see: https://issues.apache.org/jira/browse/MYFACES-1670
            context.setPropertyResolved(
                FacesContext.getCurrentInstance().getELContext().isPropertyResolved());

            return value;
        }
        catch (jakarta.faces.el.PropertyNotFoundException e)
        {
            context.setPropertyResolved(false);
            throw new PropertyNotFoundException(e.getMessage(), e);
        }
        catch (EvaluationException e)
        {
            context.setPropertyResolved(false);
            throw new ELException(e.getMessage(), e);
        }
        catch (RuntimeException e)
        {
            context.setPropertyResolved(false);
            throw e;
        }
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base)
    {
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base)
    {
        if (isDefaultLegacyPropertyResolver)
        {
            return null;
        }
        if (base == null)
        {
            return null;
        }

        return Object.class;
    }

    private static boolean needsCoersion(Object base)
    {
        return (base instanceof List) || base.getClass().isArray();
    }

    protected ExpressionFactory getExpressionFactory()
    {
        if (expressionFactory == null)
        {
            ApplicationFactory appFactory = (ApplicationFactory) FactoryFinder
                .getFactory(FactoryFinder.APPLICATION_FACTORY);
            expressionFactory = appFactory.getApplication().getExpressionFactory();
        }
        return expressionFactory;
    }

    public void setExpressionFactory(ExpressionFactory expressionFactory)
    {
        this.expressionFactory = expressionFactory;
    }

    private int coerceToInt(Object property)
    {
        return (Integer) getExpressionFactory().coerceToType(property, Integer.class);
    }


    public PropertyResolver getPropertyResolver()
    {
        return propertyResolver;
    }
}
