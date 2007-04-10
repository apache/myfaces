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
package org.apache.myfaces.el.unified.resolver;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.servlet.jsp.JspApplicationContext;

import org.apache.myfaces.el.VariableResolverImpl;

/**
 * <p>
 * This composite el resolver will be used at the top level resolver for faces ({@link Application#getELResolver()})
 * and jsp (the one we add with {@link JspApplicationContext#addELResolver(javax.el.ELResolver)}. It keeps track of its
 * scope to let the variable resolver {@link VariableResolverImpl} know in which scope it is executed. This is
 * necessarry to call either the faces or the jsp resolver head.
 * </p>
 * <p>
 * This implementation does nothing if there is no actual faces context. This is necessarry since we registered our
 * resolvers into the jsp engine. Therefore we have to make sure that jsp only pages where no faces context is available
 * are still working
 * </p>
 * 
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class FacesCompositeELResolver extends org.apache.myfaces.el.CompositeELResolver
{
    private final Scope _scope;

    public enum Scope
    {
        Faces, JSP
    }

    public FacesCompositeELResolver(Scope scope)
    {
        if (scope == null)
        {
            throw new IllegalArgumentException("scope must not be one of " + Scope.values());
        }
        _scope = scope;
    }

    @Override
    public Class<?> getCommonPropertyType(final ELContext context, final Object base)
    {
        return invoke(new ResolverInvoker<Class<?>>()
        {
            public Class<?> invoke()
            {
                return FacesCompositeELResolver.super.getCommonPropertyType(context, base);
            }
        });
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(final ELContext context, final Object base)
    {
        return invoke(new ResolverInvoker<Iterator<FeatureDescriptor>>()
        {
            public Iterator<FeatureDescriptor> invoke()
            {
                return FacesCompositeELResolver.super.getFeatureDescriptors(context, base);
            }
        });
    }

    @Override
    public Class<?> getType(final ELContext context, final Object base, final Object property)
    {
        return invoke(new ResolverInvoker<Class<?>>()
        {
            public Class<?> invoke()
            {
                return FacesCompositeELResolver.super.getType(context, base, property);
            }
        });
    }

    @Override
    public Object getValue(final ELContext context, final Object base, final Object property)
    {
        return invoke(new ResolverInvoker<Object>()
        {
            public Object invoke()
            {
                return FacesCompositeELResolver.super.getValue(context, base, property);
            }
        });
    }

    @Override
    public boolean isReadOnly(final ELContext context, final Object base, final Object property)
    {
        return invoke(new ResolverInvoker<Boolean>()
        {
            public Boolean invoke()
            {
                return FacesCompositeELResolver.super.isReadOnly(context, base, property);
            }
        });
    }

    @Override
    public void setValue(final ELContext context, final Object base, final Object property, final Object val)
    {
        invoke(new ResolverInvoker<Object>()
        {
            public Object invoke()
            {
                FacesCompositeELResolver.super.setValue(context, base, property, val);
                return null;
            }
        });
    }

    <T> T invoke(ResolverInvoker<T> invoker)
    {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null)
        {
            return null;
        }
        try
        {
            setScope(context);
            return invoker.invoke();
        }
        finally
        {
            unsetScope(context);
        }
    }

    private void setScope(FacesContext context)
    {
        context.getExternalContext().getRequestMap().put(Scope.class.getName(), _scope);
    }

    private void unsetScope(FacesContext context)
    {
        context.getExternalContext().getRequestMap().remove(Scope.class.getName());
    }

    interface ResolverInvoker<T>
    {
        T invoke();
    }

}
