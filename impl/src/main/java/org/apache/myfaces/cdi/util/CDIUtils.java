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
package org.apache.myfaces.cdi.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.spi.Context;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import org.apache.myfaces.webapp.FacesInitializerImpl;

/**
 * Lookup code for Contextual Instances.
 */
public class CDIUtils
{
    public static BeanManager getBeanManager(FacesContext facesContext)
    {
        return getBeanManager(facesContext.getExternalContext());
    }

    public static BeanManager getBeanManager(ExternalContext externalContext)
    {
        return (BeanManager) externalContext.getApplicationMap().get(FacesInitializerImpl.CDI_BEAN_MANAGER_INSTANCE);
    }

    public static <T> T get(BeanManager bm, Class<T> clazz)
    {
        Set<Bean<?>> beans = bm.getBeans(clazz);
        return resolveInstance(bm, beans, clazz);
    }

    public static <T> T getOptional(BeanManager bm, Class<T> clazz)
    {
        Set<Bean<?>> beans = bm.getBeans(clazz);
        if (beans == null || beans.isEmpty())
        {
            return null;
        }
        return resolveInstance(bm, beans, clazz);
    }
    
    private static <T> T resolveInstance(BeanManager bm, Set<Bean<?>> beans, Type type)
    {
        Bean<?> bean = bm.resolve(beans);
        CreationalContext<?> cc = bm.createCreationalContext(bean);
        T instance = (T) bm.getReference(bean, type, cc);
        return instance;

    }
    
    @SuppressWarnings("unchecked")
    public static <T> Bean<T> get(BeanManager beanManager, Class<T> beanClass, Annotation... qualifiers)
    {
        Set<Bean<?>> beans = beanManager.getBeans(beanClass, qualifiers);

        for (Bean<?> bean : beans)
        {
            if (bean.getBeanClass() == beanClass)
            {
                return (Bean<T>) beanManager.resolve(Collections.<Bean<?>>singleton(bean));
            }
        }

        return (Bean<T>) beanManager.resolve(beans);
    }

    public static <T> T get(BeanManager beanManager, Class<T> beanClass, 
            boolean create, Annotation... qualifiers)
    {
        try
        {
            Bean<T> bean = get(beanManager, beanClass, qualifiers);
            return bean == null ? null : get(beanManager, bean, beanClass, create);
        }
        catch (ContextNotActiveException e)
        {
            return null;
        }
    }

    public static <T> T get(BeanManager beanManager, Type type, boolean create, Annotation... qualifiers)
    {
        try
        {
            Set<Bean<?>> beans = beanManager.getBeans(type, qualifiers);
            Bean<T> bean = (Bean<T>) beanManager.resolve(beans);

            return bean == null ? null : get(beanManager, bean, type, create);
        }
        catch (ContextNotActiveException e)
        {
            return null;
        }
    }

    public static <T> T get(BeanManager beanManager, Bean<T> bean, Type type, boolean create)
    {
        if (create)
        {
            CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
            return (T) beanManager.getReference(bean, type, creationalContext);
        }
        else
        {
            Context context = beanManager.getContext(bean.getScope());
            return context.get(bean);
        }
    }
    
    public static boolean isSessionScopeActive(BeanManager beanManager)
    {
        try 
        {
            Context ctx = beanManager.getContext(SessionScoped.class);
            return ctx != null;
        }
        catch (ContextNotActiveException ex)
        {
            //No op
        }
        catch (Exception ex)
        {
            // Sometimes on startup time, since there is no active request context, trying to grab session scope
            // throws NullPointerException.
            //No op
        }
        return false;
    }
    
    public static boolean isRequestScopeActive(BeanManager beanManager)
    {
        try 
        {
            Context ctx = beanManager.getContext(RequestScoped.class);
            return ctx != null;
        }
        catch (ContextNotActiveException ex)
        {
            //No op
        }
        catch (Exception ex)
        {
            //No op
        }
        return false;
    }
    
    public static boolean isViewScopeActive(BeanManager beanManager)
    {
        try 
        {
            Context ctx = beanManager.getContext(ViewScoped.class);
            return ctx != null;
        }
        catch (ContextNotActiveException ex)
        {
            //No op
        }
        catch (Exception ex)
        {
            // Sometimes on startup time, since there is no active request context, trying to grab session scope
            // throws NullPointerException.
            //No op
        }
        return false;
    }
        
}