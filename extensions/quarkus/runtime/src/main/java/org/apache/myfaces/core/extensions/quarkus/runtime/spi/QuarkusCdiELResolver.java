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
package org.apache.myfaces.core.extensions.quarkus.runtime.spi;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import jakarta.faces.FacesException;

/**
 * Custom {@link ELResolver} for CDI as {@link BeanManager#getELResolver} is not supported on Quarkus.
 * Currently @Dependent is not supported.
 */
public class QuarkusCdiELResolver extends ELResolver
{
    private BeanManager beanManager;
    private Map<String, Optional<Object>> cachedProxies;

    public QuarkusCdiELResolver()
    {
        beanManager = CDI.current().getBeanManager();
        cachedProxies = new ConcurrentHashMap<>();
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext arg0, Object arg1)
    {
        return null;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext arg0, Object arg1)
    {
        return null;
    }

    @Override
    public Class<?> getType(ELContext arg0, Object arg1, Object arg2) throws ELException
    {
        return null;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) throws ELException
    {
        //we only check root beans
        if (base != null)
        {
            return null;
        }

        String beanName = (String) property;

        Optional<Object> contextualInstance = cachedProxies.get(beanName);
        if (contextualInstance == null)
        {
            contextualInstance = resolveProxy(beanName);
            cachedProxies.put(beanName, contextualInstance);
        }

        if (contextualInstance.isPresent())
        {
            context.setPropertyResolved(true);
            return contextualInstance.get();
        }

        return null;
    }

    protected Optional<Object> resolveProxy(String beanName)
    {
        Object contextualInstance = null;

        Set<Bean<?>> beans = beanManager.getBeans(beanName);
        if (beans != null && !beans.isEmpty())
        {
            Bean<?> bean = beanManager.resolve(beans);

            if (bean.getScope().equals(Dependent.class))
            {
                throw new FacesException("@Dependent on beans used in EL are currently not supported! "
                        + " Class: " + bean.getBeanClass().toString());
            }

            CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
            contextualInstance = beanManager.getReference(bean, Object.class, creationalContext);
        }

        return contextualInstance == null ? Optional.empty() : Optional.of(contextualInstance);
    }

    @Override
    public boolean isReadOnly(ELContext arg0, Object arg1, Object arg2) throws ELException
    {
        return false;
    }

    @Override
    public void setValue(ELContext arg0, Object arg1, Object arg2, Object arg3) throws ELException
    {

    }
}
