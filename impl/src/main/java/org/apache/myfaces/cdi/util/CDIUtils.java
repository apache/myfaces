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

import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.ExternalContext;
import org.apache.myfaces.webapp.AbstractFacesInitializer;

/**
 * Lookup code for Contextual Instances.
 */
public class CDIUtils
{
    public static BeanManager getBeanManager(ExternalContext externalContext)
    {
        return (BeanManager) externalContext.getApplicationMap().get(
            AbstractFacesInitializer.CDI_BEAN_MANAGER_INSTANCE);
    }



    public static <T> T lookup(BeanManager bm, Class<T> clazz)
    {
        Set<Bean<?>> beans = bm.getBeans(clazz);
        return resolveInstance(bm, beans, clazz);
    }

    public static Object lookup(BeanManager bm, String name)
    {
        Set<Bean<?>> beans = bm.getBeans(name);
        return resolveInstance(bm, beans, Object.class);
    }

    private static <T> T resolveInstance(BeanManager bm, Set<Bean<?>> beans, Type type)
    {
        Bean<?> bean = bm.resolve(beans);
        CreationalContext<?> cc = bm.createCreationalContext(bean);
        T dao = (T) bm.getReference(bean, type, cc);
        return dao;

    }
}