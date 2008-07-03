/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.myfaces.el.convert;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.faces.FactoryFinder;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyResolver;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.List;

/**
 * Wrapper that converts a VariableResolver into an ELResolver. See JSF 1.2 spec section 5.6.1.6
 * 
 * @author Stan Silvert (latest modification by $Author$)
 * @author Mathias Broekelmann
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public class PropertyResolverToELResolver extends ELResolver
{
    private PropertyResolver propertyResolver;

    private ExpressionFactory expressionFactory;

    /**
     * Creates a new instance of PropertyResolverToELResolver
     */
    public PropertyResolverToELResolver(PropertyResolver propertyResolver)
    {
        this.propertyResolver = propertyResolver;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, final Object value)
            throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException
    {
        invoke(context, base, property, new ResolverInvoker<Object>()
        {
            @Override
            public Object invoke(Object base, Object property)
            {
                if (needsCoersion(base))
                {
                    propertyResolver.setValue(base, coerceToInt(property), value);
                }
                else
                {
                    propertyResolver.setValue(base, property, value);
                }
                return null;
            }
        });
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) throws NullPointerException,
            PropertyNotFoundException, ELException
    {
        return invoke(context, base, property, new ResolverInvoker<Boolean>()
        {
            @Override
            public Boolean invoke(Object base, Object property)
            {
                if (needsCoersion(base))
                {
                    return propertyResolver.isReadOnly(base, coerceToInt(property));
                }
                return propertyResolver.isReadOnly(base, property);
            }

            @Override
            Boolean getValueIfBaseAndPropertyIsNull()
            {
                return true;
            }
        });
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) throws NullPointerException,
            PropertyNotFoundException, ELException
    {
        return invoke(context, base, property, new ResolverInvoker<Object>()
        {
            @Override
            Object invoke(Object base, Object property)
            {
                if (needsCoersion(base))
                {
                    return propertyResolver.getValue(base, coerceToInt(property));
                }
                return propertyResolver.getValue(base, property);
            }
        });
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) throws NullPointerException,
            PropertyNotFoundException, ELException
    {
        return invoke(context, base, property, new ResolverInvoker<Class<?>>()
        {
            @Override
            Class<?> invoke(Object base, Object property)
            {
                if (needsCoersion(base))
                {
                    return propertyResolver.getType(base, coerceToInt(property));
                }

                return propertyResolver.getType(base, property);
            }
        });
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base)
    {
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base)
    {

        if (base == null)
            return null;

        return Object.class;
    }

    private boolean needsCoersion(Object base)
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
        Integer coerced = (Integer) getExpressionFactory().coerceToType(property, Integer.class);
        return coerced.intValue();
    }

    private <T> T invoke(ELContext context, Object base, Object property, ResolverInvoker<T> invoker)
            throws PropertyNotFoundException, ELException
    {
        if (base == null || property == null)
        {
            return invoker.getValueIfBaseAndPropertyIsNull();
        }

        try
        {
            context.setPropertyResolved(true);
            T value = invoker.invoke(base, property);
            
            // see: https://issues.apache.org/jira/browse/MYFACES-1670
            context.setPropertyResolved(
                    FacesContext.getCurrentInstance().getELContext().isPropertyResolved());
            
            return value;
        }
        catch (javax.faces.el.PropertyNotFoundException e)
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

    private abstract class ResolverInvoker<T>
    {
        abstract T invoke(Object base, Object property) throws PropertyNotFoundException, EvaluationException,
                RuntimeException;

        T getValueIfBaseAndPropertyIsNull()
        {
            return null;
        }
    }

    public PropertyResolver getPropertyResolver()
    {
        return propertyResolver;
    }
}
