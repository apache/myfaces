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
package org.apache.myfaces.cdi.view;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;
import java.util.HashMap;
import java.util.Map;
import jakarta.enterprise.inject.spi.Bean;
import org.apache.myfaces.cdi.util.ContextualStorage;

/**
 * This Storage holds all information needed for storing
 * View Scope instances in a context.
 * 
 * This scope requires passivation and is not concurrent.
 */
public class ViewScopeContextualStorage extends ContextualStorage
{
    private static final long serialVersionUID = 1L;
    
    private final Map<String, Object> nameBeanKeyMap;

    public ViewScopeContextualStorage(BeanManager beanManager)
    {
        super(beanManager, false);
        this.nameBeanKeyMap = new HashMap<>();
    }

    public Map<String, Object> getNameBeanKeyMap()
    {
        return nameBeanKeyMap;
    }

    @Override
    public <T> T createContextualInstance(Contextual<T> bean, CreationalContext<T> creationalContext)
    {
        T instance = super.createContextualInstance(bean, creationalContext);

        if (bean instanceof Bean)
        {
            String name = ((Bean<T>) bean).getName();
            if (name != null)
            {
                nameBeanKeyMap.put(name, getBeanKey(bean));
            }
        }

        return instance;
    }
}
