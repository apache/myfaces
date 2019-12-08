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
package org.apache.myfaces.bean;

import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;
import org.apache.myfaces.bean.literal.NamedLiteral;

class ManagedBeanWrapper implements AnnotatedType<Object>
{
    private final AnnotatedType wrapped;
    private Map<Class<? extends Annotation>, Annotation> annotations;
    private Set<Annotation> annotationSet;

    ManagedBeanWrapper(AnnotatedType wrapped,
                    javax.faces.bean.ManagedBean managedBean,
                    Class<? extends Annotation> newScope,
                    Class<? extends Annotation> oldScope,
                    AnnotationLiteral literal)
    {
        this.wrapped = wrapped;

        this.annotations = new HashMap<>(wrapped.getAnnotations().size());
        this.annotations.put(newScope, literal);
        this.annotations.put(Named.class, new NamedLiteral(managedBean.name()));

        for (Annotation originalAnnotation : wrapped.getAnnotations())
        {
            if (!originalAnnotation.annotationType().equals(oldScope)
                    && !originalAnnotation.annotationType().equals(javax.faces.bean.ManagedBean.class))
            {
                this.annotations.put(originalAnnotation.annotationType(), originalAnnotation);
            }
        }

        this.annotationSet = new HashSet<>(this.annotations.values());
    }

    @Override
    public Class getJavaClass()
    {
        return wrapped.getJavaClass();
    }

    @Override
    public Set getConstructors()
    {
        return wrapped.getConstructors();
    }

    @Override
    public Set getMethods()
    {
        return wrapped.getMethods();
    }

    @Override
    public Set getFields()
    {
        return wrapped.getFields();
    }

    @Override
    public Type getBaseType()
    {
        return wrapped.getBaseType();
    }

    @Override
    public Set<Type> getTypeClosure()
    {
        return wrapped.getTypeClosure();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> targetClass)
    {
        return (T) this.annotations.get(targetClass);
    }

    @Override
    public Set<Annotation> getAnnotations()
    {
        return this.annotationSet;
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> targetClass)
    {
        return this.annotations.containsKey(targetClass);
    }
}
