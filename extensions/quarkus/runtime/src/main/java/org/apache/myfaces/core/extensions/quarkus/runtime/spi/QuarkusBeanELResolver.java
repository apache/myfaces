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

import jakarta.el.PropertyNotFoundException;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.core.api.shared.lang.PropertyDescriptorUtils;
import org.apache.myfaces.core.api.shared.lang.PropertyDescriptorWrapper;
import org.apache.myfaces.el.resolver.LambdaBeanELResolver;

import java.util.Map;

/**
 * Custom LambdaBeanELResolver to unwrap Quarkus proxies as this leads to errors in newer versions. See MYFACES-4743.
 * Even if USE_LAMBDA_METAFACTORY is not activated, it still uses Java introspection as fallback.
 */
public class QuarkusBeanELResolver extends LambdaBeanELResolver
{
    @Override
    protected PropertyDescriptorWrapper getPropertyDescriptor(Object base, Object property)
    {
        Class<?> type = base.getClass();
        if (type.getName().endsWith("_ClientProxy"))
        {
            type = type.getSuperclass();
        }

        Map<String, ? extends PropertyDescriptorWrapper> beanCache = cache.get(type.getName());
        if (beanCache == null)
        {
            beanCache = PropertyDescriptorUtils.getCachedPropertyDescriptors(
                    FacesContext.getCurrentInstance().getExternalContext(),
                    type);
            cache.put(type.getName(), beanCache);
        }

        PropertyDescriptorWrapper pd = beanCache.get((String) property);
        if (pd == null)
        {
            throw new PropertyNotFoundException("Property [" + property
                    + "] not found on type [" + type.getName() + "]");
        }
        return pd;
    }
}
