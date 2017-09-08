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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;

// Inspired by Mojarra's CdiProducer
public abstract class AbstractDynamicProducer<T> implements Bean<T>, PassivationCapable, Serializable
{
    private String id;
    private String name;
    private Class<?> beanClass;
    private Set<Type> types;
    private Set<Annotation> qualifiers;
    private Class<? extends Annotation> scope;
    private Function<CreationalContext<T>, T> create;

    public AbstractDynamicProducer()
    {
        this.id = this.getClass().getName();
        this.beanClass = Object.class;
        this.types = Collections.singleton(Object.class);
        this.qualifiers = Collections.unmodifiableSet(asSet(new DefaultLiteral(), new AnyLiteral()));
        this.scope = Dependent.class;
    }
    
    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Class<?> getBeanClass()
    {
        return beanClass;
    }

    @Override
    public Set<Type> getTypes()
    {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers()
    {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return scope;
    }

    @Override
    public T create(CreationalContext<T> creationalContext)
    {
        return create.apply(creationalContext);
    }

    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext)
    {
        // not required - we just push a JSF artifact into CDI
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints()
    {
        return Collections.emptySet();
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes()
    {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative()
    {
        return false;
    }

    @Override
    public boolean isNullable()
    {
        return false;
    }

    
    
    
    public AbstractDynamicProducer<T> name(String name)
    {
        this.name = name;
        return this;
    }

    public AbstractDynamicProducer<T> create(Function<CreationalContext<T>, T> create)
    {
        this.create = create;
        return this;
    }

    public AbstractDynamicProducer<T> beanClass(Class<?> beanClass)
    {
        this.beanClass = beanClass;
        return this;
    }

    public AbstractDynamicProducer<T> types(Type... types)
    {
        this.types = asSet(types);
        return this;
    }

    public AbstractDynamicProducer<T> beanClassAndType(Class<?> beanClass)
    {
        beanClass(beanClass);
        types(beanClass);
        return this;
    }

    public AbstractDynamicProducer<T> qualifiers(Annotation... qualifiers)
    {
        this.qualifiers = asSet(qualifiers);
        return this;
    }

    public AbstractDynamicProducer<T> scope(Class<? extends Annotation> scope)
    {
        this.scope = scope;
        return this;
    }

    public AbstractDynamicProducer<T> addToId(Object object)
    {
        id = id + " " + object.toString();
        return this;
    }

    @SafeVarargs
    public static <T> Set<T> asSet(T... a)
    {
        return new HashSet<>(Arrays.asList(a));
    }

}
