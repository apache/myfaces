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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

/**
 * <p>This class contains utility methods to resolve contextual references
 * in situations where no injection is available because the
 * current class is not managed by the CDI Container. This can happen
 * in e.g. a JPA-2.0 EntityListener, a ServletFilter, a Spring managed
 * Bean, etc.</p>
 *
 * <p><b>Attention:</b> This method is intended for being used in user code at runtime.
 * If this method gets used during Container boot (in an Extension), non-portable
 * behaviour results. The CDI specification only allows injection of the
 * BeanManager during CDI-Container boot time.</p>
 *
 */
@Typed()
public final class BeanProvider
{

    private BeanProvider()
    {
        // this is a utility class which doesn't get instantiated.
    }

    /**
     *
     * @param beanManager the BeanManager to use
     * @param type the type of the bean in question
     * @param optional if <code>true</code> it will return <code>null</code> if no bean could be found or created.
     *                 Otherwise it will throw an {@code IllegalStateException}
     * @param qualifiers additional qualifiers which further distinct the resolved bean
     * @param <T> target type
     * @return the resolved Contextual Reference
     */
    public static <T> T getContextualReference(BeanManager beanManager,
                                               Class<T> type,
                                               boolean optional,
                                               Annotation... qualifiers)
    {
        Set<Bean<?>> beans = beanManager.getBeans(type, qualifiers);

        if (beans == null || beans.isEmpty())
        {
            if (optional)
            {
                return null;
            }

            throw new IllegalStateException("Could not find beans for Type=" + type
                    + " and qualifiers:" + Arrays.toString(qualifiers));
        }

        return getContextualReference(type, beanManager, beans);
    }

    /**
     * Internal helper method to resolve the right bean and resolve the contextual reference.
     *
     * @param <T> target type
     * @param type the type of the bean in question
     * @param beanManager current bean-manager
     * @param beans beans in question
     * @return the contextual reference
     */
    private static <T> T getContextualReference(Class<T> type, BeanManager beanManager, Set<Bean<?>> beans)
    {
        Bean<?> bean = beanManager.resolve(beans);

        //logWarningIfDependent(bean);

        CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);

        @SuppressWarnings({ "unchecked", "UnnecessaryLocalVariable" })
        T result = (T) beanManager.getReference(bean, type, creationalContext);
        return result;
    }

}
