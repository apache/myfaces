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

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.PropertyNotFoundException;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.config.element.ManagedBean;

import com.google.inject.Injector;

/**
 * <p>
 * Register this ELResolver in faces-config.xml.
 * </p>
 * <code>
 * &lt;application&gt; 
 * &lt;el-resolver&gt;org.apache.myfaces.el.unified.resolver.GuiceResolver&lt;/el-resolver&gt;
 * &lt;/application&gt;
 * </code>
 * <p>
 * Implement and configure a ServletContextListener in web.xml .
 * </p>
 * <code>
 * &lt;listener&gt; 
 * &lt;listener-class&gt;com.your_company.GuiceServletContextListener&lt;/listener-class&gt; 
 * &lt;/listener&gt;
 * </code>
 * <p>
 * Configure Guice in your ServletContextListener implementation, and place the Injector in application scope.
 * </p>
 * <code>
 * public class GuiceServletContextListener implements ServletContextListener {
 * 
 * public void contextInitialized(ServletContextEvent event) { ServletContext ctx = event.getServletContext(); //when on
 * Java6, use ServiceLoader.load(com.google.inject.Module.class); Injector injector = Guice.createInjector(new
 * YourModule()); ctx.setAttribute(GuiceResolver.KEY, injector); }
 * 
 * public void contextDestroyed(ServletContextEvent event) { ServletContext ctx = event.getServletContext();
 * ctx.removeAttribute(GuiceResolver.KEY); }
 * 
 * }
 * </code>
 * 
 * @author Dennis Byrne
 */

public class GuiceResolver extends ManagedBeanResolver
{

    public static final String KEY = "oam." + Injector.class.getName();

    @Override
    public Object getValue(ELContext ctx, Object base, Object property) throws NullPointerException,
        PropertyNotFoundException, ELException
    {

        if (base != null || !(property instanceof String))
        {
            return null;
        }

        FacesContext fctx = (FacesContext)ctx.getContext(FacesContext.class);

        if (fctx == null)
        {
            return null;
        }

        ExternalContext ectx = fctx.getExternalContext();

        if (ectx == null || ectx.getRequestMap().containsKey(property) || ectx.getSessionMap().containsKey(property)
                || ectx.getApplicationMap().containsKey(property))
        {
            return null;
        }

        ManagedBean managedBean = runtimeConfig(ctx).getManagedBean((String)property);

        return managedBean == null ? null : getValue(ctx, ectx, managedBean.getManagedBeanClass());
    }

    private Object getValue(ELContext ctx, ExternalContext ectx, Class<?> managedBeanClass)
    {

        Injector injector = (Injector)ectx.getApplicationMap().get(KEY);

        if (injector == null)
        {
            throw new FacesException("Could not find an instance of " + Injector.class.getName()
                    + " in application scope using key '" + KEY + "'");
        }

        Object value = injector.getInstance(managedBeanClass);
        ctx.setPropertyResolved(true);
        return value;
    }

}
