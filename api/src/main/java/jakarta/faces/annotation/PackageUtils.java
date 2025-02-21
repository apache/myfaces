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
package jakarta.faces.annotation;

import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.Set;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.context.FacesContext;

class PackageUtils
{
    private PackageUtils()
    {
    }

    /**
     * This does unfortunately not exist in cdi spec: https://stackoverflow.com/a/63653513
     *
     * This basically sorts descending by priority with fallback to FQN.
     * Highest priority first.
     * Priotityless bean last.
     * Same priorities ordered by FQN (for now?)
     */
    public static final Comparator<Object> BEAN_PRIORITY_COMPARATOR = (left, right) ->
    {
        Class<?> leftClass = left.getClass();
        Class<?> rightClass = right.getClass();
        Priority leftPriority = leftClass.getAnnotation(Priority.class);
        Priority rightPriority = rightClass.getAnnotation(Priority.class);

        int compare = leftPriority != null && rightPriority != null
                ? Integer.compare(leftPriority.value(), rightPriority.value())
                : leftPriority != null
                    ? -1
                    : rightPriority != null
                        ? 1
                        : 0;

        if (compare == 0)
        {
            return leftClass.getName().compareTo(rightClass.getName());
        }

        return compare;
    };

    public static Set<?> getBeanReferencesByQualifier(FacesContext context, Annotation... qualifiers)
    {
        BeanManager beanManager = CDI.current().getBeanManager();
        return beanManager.getBeans(Object.class, qualifiers).stream()
                .map(bean -> beanManager.getReference(bean,
                        bean.getBeanClass(),
                        beanManager.createCreationalContext(bean)))
                .collect(toSet());
    }

}
