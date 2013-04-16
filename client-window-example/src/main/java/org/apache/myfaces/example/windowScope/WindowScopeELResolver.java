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
package org.apache.myfaces.example.windowScope;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

/**
 *
 * @author lu4242
 */
public class WindowScopeELResolver extends ELResolver
{
    public static final String WINDOW = "window";

    @Override
    public void setValue(final ELContext context, final Object base, final Object property, final Object value)
    {
        if (property == null)
        {
            throw new PropertyNotFoundException();
        }
        if (!(property instanceof String))
        {
            return;
        }

        String strProperty = property.toString();

        if (WINDOW.equals(strProperty))
        {
            throw new PropertyNotWritableException();
        }
        else if (base instanceof WindowScope)
        {
            context.setPropertyResolved(true);
            try
            {
                ((WindowScope) base).put(strProperty, value);
            }
            catch (UnsupportedOperationException e)
            {
                throw new PropertyNotWritableException(e);
            }
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property)
            throws NullPointerException, PropertyNotFoundException, ELException
    {

        if (property == null)
        {
            throw new PropertyNotFoundException();
        }
        if (!(property instanceof String))
        {
            return false;
        }

        String strProperty = property.toString();

        if (WINDOW.equals(strProperty))
        {
            context.setPropertyResolved(true);
            return true;
        }
        else if (base instanceof WindowScope)
        {
            context.setPropertyResolved(true);
        }

        return false;
    }

    @Override
    public Object getValue(ELContext elContext, Object base, Object property)
            throws NullPointerException, PropertyNotFoundException, ELException
    {

        if (property == null)
        {
            throw new PropertyNotFoundException();
        }
        if (!(property instanceof String))
        {
            return null;
        }

        String strProperty = property.toString();

        if (base == null)
        {
            if (WINDOW.equals(strProperty))
            {
                FacesContext facesContext = facesContext(elContext);
                if (facesContext == null)
                {
                    return null;
                }
                ExternalContext externalContext = facesContext.getExternalContext();
                if (externalContext == null)
                {
                    return null;
                }

                //Access to window object
                elContext.setPropertyResolved(true);
                WindowScope window = getScope(facesContext);

                return window;
            }
        }
        else if (base instanceof WindowScope)
        {
            FacesContext facesContext = facesContext(elContext);
            if (facesContext == null)
            {
                return null;
            }
            ExternalContext externalContext = facesContext.getExternalContext();
            if (externalContext == null)
            {
                return null;
            }
            WindowScope window = (WindowScope) base;
            //Just get the value
            elContext.setPropertyResolved(true);
            return window.get(strProperty);
        }
        return null;
    }

    // get the FacesContext from the ELContext
    protected FacesContext facesContext(ELContext context)
    {
        return (FacesContext) context.getContext(FacesContext.class);
    }

    protected ExternalContext externalContext(ELContext context)
    {
        return facesContext(context).getExternalContext();
    }

    private WindowScope getScope(final FacesContext facesContext)
    {
        return WindowScopeImpl.getCurrentInstance(facesContext.getExternalContext());
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property)
            throws NullPointerException, PropertyNotFoundException, ELException
    {

        if (property == null)
        {
            throw new PropertyNotFoundException();
        }
        if (!(property instanceof String))
        {
            return null;
        }

        String strProperty = property.toString();

        if (WINDOW.equals(strProperty))
        {
            context.setPropertyResolved(true);
        }
        else if (base instanceof WindowScope)
        {
            context.setPropertyResolved(true);
            Object obj = ((WindowScope) base).get(property);
            return (obj != null) ? obj.getClass() : null;
        }

        return null;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context,
            Object base)
    {
        ArrayList<FeatureDescriptor> descriptors = new ArrayList<FeatureDescriptor>(1);

        descriptors.add(makeDescriptor(WINDOW,
                "Represents the current flash scope", Object.class));

        if (base instanceof WindowScope)
        {
            Iterator itr = ((WindowScope) base).keySet().iterator();
            Object key;
            FeatureDescriptor desc;
            while (itr.hasNext())
            {
                key = itr.next();
                desc = makeDescriptor(key.toString(), key.toString(), key.getClass());
                descriptors.add(desc);
            }
        }
        return descriptors.iterator();
    }

    protected FeatureDescriptor makeDescriptor(String name, String description,
            Class<?> elResolverType)
    {
        FeatureDescriptor fd = new FeatureDescriptor();
        fd.setValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME, Boolean.TRUE);
        fd.setValue(ELResolver.TYPE, elResolverType);
        fd.setName(name);
        fd.setDisplayName(name);
        fd.setShortDescription(description);
        fd.setExpert(false);
        fd.setHidden(false);
        fd.setPreferred(true);
        return fd;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base)
    {
        if (base == null)
        {
            return null;
        }

        if (base instanceof WindowScope)
        {
            return Object.class;
        }
        else if (WINDOW.equals(base.toString()))
        {
            return Object.class;
        }

        return null;
    }
}
