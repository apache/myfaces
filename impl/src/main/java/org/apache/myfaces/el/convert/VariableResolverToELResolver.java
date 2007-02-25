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
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

/**
 * Wrapper that converts a VariableResolver into an ELResolver. See JSF 1.2 spec section 5.6.1.5
 * 
 * @author Stan Silvert (latest modification by $Author$)
 * @author Mathias Broekelmann
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public class VariableResolverToELResolver extends ELResolver
{

    private VariableResolver variableResolver;

    /**
     * Creates a new instance of VariableResolverToELResolver
     */
    public VariableResolverToELResolver(VariableResolver variableResolver)
    {
        this.variableResolver = variableResolver;
    }

    public Object getValue(ELContext context, Object base, Object property) throws NullPointerException,
            PropertyNotFoundException, ELException
    {

        if (base != null)
            return null;
        if (property == null)
            throw new PropertyNotFoundException();
        
        context.setPropertyResolved(true);

        if (!(property instanceof String))
            return null;

        String strProperty = (String) property;

        // This differs from the spec slightly. I'm wondering if the spec is
        // wrong because it is legal for the resolveVariable method to return
        // null. In that case you would not want to setPropertyResolved(true).
        try
        {
            Object value = variableResolver.resolveVariable(facesContext(context), strProperty);
            if (value != null)
            {
                return value;
            }
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

        return null;
    }

    // get the FacesContext from the ELContext
    private FacesContext facesContext(ELContext context)
    {
        return (FacesContext) context.getContext(FacesContext.class);
    }

    public Class<?> getCommonPropertyType(ELContext context, Object base)
    {
        if (base != null)
            return null;

        return String.class;
    }

    public void setValue(ELContext context, Object base, Object property, Object value) throws NullPointerException,
            PropertyNotFoundException, PropertyNotWritableException, ELException
    {

        if ((base == null) && (property == null))
            throw new PropertyNotFoundException();
    }

    public boolean isReadOnly(ELContext context, Object base, Object property) throws NullPointerException,
            PropertyNotFoundException, ELException
    {

        if ((base == null) && (property == null))
            throw new PropertyNotFoundException();

        return false;
    }

    public Class<?> getType(ELContext context, Object base, Object property) throws NullPointerException,
            PropertyNotFoundException, ELException
    {

        if ((base == null) && (property == null))
            throw new PropertyNotFoundException();

        return null;
    }

    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base)
    {
        return null;
    }

}
