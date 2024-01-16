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


import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.PassivationCapable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This Storage holds all information needed for storing Contextual Instances in a Context.
 *
 * It also addresses Serialisation in case of passivating scopes.
 */
public class ContextualStorage implements Serializable
{
    private static final long serialVersionUID = 1L;

    protected Map<Object, ContextualInstanceInfo<?>> contextualInstances;
    protected Map<String, Object> beanNameToKeyMapping;
    protected BeanManager beanManager;
    protected boolean concurrent;
    protected boolean passivating;
    protected transient volatile boolean activated;

    public ContextualStorage()
    {
        this.activated = true;
    }

    /**
     * @param beanManager is needed for serialisation
     * @param concurrent whether the ContextualStorage might get accessed concurrently by different threads
     * @param passivating whether the served scope is a passivating scope
     */
    public ContextualStorage(BeanManager beanManager, boolean concurrent, boolean passivating)
    {
        this.beanManager = beanManager;
        this.concurrent = concurrent;
        this.passivating = passivating;
        if (concurrent)
        {
            contextualInstances = new ConcurrentHashMap<>();
            beanNameToKeyMapping = new ConcurrentHashMap<>();
        }
        else
        {
            contextualInstances = new HashMap<>();
            beanNameToKeyMapping = new HashMap<>();
        }
        this.activated = true;
    }

    /**
     * @return the underlying storage map.
     */
    public Map<Object, ContextualInstanceInfo<?>> getStorage()
    {
        return contextualInstances;
    }

    public void clear()
    {
        contextualInstances.clear();
        beanNameToKeyMapping.clear();
    }

    /**
     * @return whether the ContextualStorage might get accessed concurrently by different threads.
     */
    public boolean isConcurrent()
    {
        return concurrent;
    }

    /**
     *
     * @param bean
     * @param creationalContext
     * @param <T>
     * @return
     */
    public <T> T createContextualInstance(Contextual<T> bean, CreationalContext<T> creationalContext)
    {
        Object beanKey = getBeanKey(bean);
        if (isConcurrent())
        {
            // locked approach
            ContextualInstanceInfo<T> instanceInfo = new ContextualInstanceInfo<>();

            ConcurrentMap<Object, ContextualInstanceInfo<?>> concurrentMap
                    = (ConcurrentHashMap<Object, ContextualInstanceInfo<?>>) contextualInstances;

            ContextualInstanceInfo<T> oldInstanceInfo
                    = (ContextualInstanceInfo<T>) concurrentMap.putIfAbsent(beanKey, instanceInfo);

            if (oldInstanceInfo != null)
            {
                instanceInfo = oldInstanceInfo;
            }

            synchronized (instanceInfo)
            {
                T instance = instanceInfo.getContextualInstance();
                if (instance == null)
                {
                    instance = bean.create(creationalContext);
                    instanceInfo.setContextualInstance(instance);
                    instanceInfo.setCreationalContext(creationalContext);

                    if (bean instanceof Bean)
                    {
                        String name = ((Bean<T>) bean).getName();
                        if (name != null)
                        {
                            beanNameToKeyMapping.put(name, beanKey);
                        }
                    }
                }

                return instance;
            }

        }
        else
        {
            // simply create the contextual instance
            ContextualInstanceInfo<T> instanceInfo = new ContextualInstanceInfo<>();
            instanceInfo.setCreationalContext(creationalContext);
            instanceInfo.setContextualInstance(bean.create(creationalContext));

            contextualInstances.put(beanKey, instanceInfo);

            if (bean instanceof Bean)
            {
                String name = ((Bean<T>) bean).getName();
                if (name != null)
                {
                    beanNameToKeyMapping.put(name, beanKey);
                }
            }

            return instanceInfo.getContextualInstance();
        }
    }

    /**
     * If the context is a passivating scope then we return the passivationId of the bean.
     * Otherwise we use the bean directly, this is mainly for Quarkus.
     *
     * @param bean
     *
     * @return the key to use in the context map
     */
    public <T> Object getBeanKey(Contextual<T> bean)
    {
        if (bean instanceof PassivationCapable && passivating)
        {
            // if the
            return ((PassivationCapable) bean).getId();
        }

        return bean;
    }

    /**
     * Restores the bean from its beanKey.
     * It returns null if the beanKey does not belong to a bean or is not Contextual.
     *
     * @see #getBeanKey(jakarta.enterprise.context.spi.Contextual)
     */
    public Contextual<?> getBean(Object beanKey)
    {
        if (beanKey instanceof String && passivating)
        {
            // If the beanKey is a String, it was generated by PassivationCapable#getId().
            return beanManager.getPassivationCapableBean((String) beanKey);
        }

        if (beanKey instanceof Contextual)
        {
            return (Contextual<?>) beanKey;
        }

        return null;
    }

    public boolean isActivated()
    {
        return activated;
    }

    public void activate()
    {
        activated = true;
    }

    public void deactivate()
    {
        activated = false;
    }

    public Map<String, Object> getBeanNameToKeyMapping()
    {
        return beanNameToKeyMapping;
    }
}
