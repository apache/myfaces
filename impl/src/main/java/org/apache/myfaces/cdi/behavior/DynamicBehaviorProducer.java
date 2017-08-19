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

package org.apache.myfaces.cdi.behavior;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.faces.FacesException;
import javax.faces.component.behavior.Behavior;
import org.apache.myfaces.shared.util.ClassUtils;

/**
 *
 */
@Typed
public class DynamicBehaviorProducer implements Bean<Behavior>, Serializable, PassivationCapable
{
    private static final long serialVersionUID = 1L;

    private BeanManager beanManager;
    private BehaviorInfo typeInfo;
    private Set<Type> types;
    private Class<?> beanClass;

    public DynamicBehaviorProducer(BeanManager beanManager, BehaviorInfo typeInfo)
    {
        this.beanManager = beanManager;
        this.typeInfo = typeInfo;
        types = new HashSet<Type>(asList(typeInfo.getType(), Object.class));
        beanClass = ClassUtils.simpleClassForName(typeInfo.getType().getTypeName());
    }

    @Override
    public String getId()
    {
        String converterId = typeInfo.getBehaviorId() == null ? "" : typeInfo.getBehaviorId();
        return ""+typeInfo.getType()+"_"+converterId;
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
        return Collections.singleton(
                (Annotation) new FacesBehaviorAnnotationLiteral(
                        typeInfo.getBehaviorId() == null ? "" : typeInfo.getBehaviorId(), true));
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return Dependent.class;
    }

    @Override
    public String getName()
    {
        return null;
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
    public Set<InjectionPoint> getInjectionPoints()
    {
        return Collections.emptySet();
    }

    @Override
    public boolean isNullable()
    {
        return true;
    }
    
    @Override
    public Behavior create(CreationalContext<Behavior> cc)
    {
        Class<? extends Behavior> converterClass = (Class<? extends Behavior>) beanClass;        
        Behavior converter = null;
        try
        {
            converter = converterClass.newInstance();
        }
        catch (Exception ex)
        {
            Logger.getLogger(DynamicBehaviorProducer.class.getName()).log(
                    Level.SEVERE, "Could not instantiate converter " + beanClass.getName(), ex);
            throw new FacesException("Could not instantiate converter: " + beanClass.getName(), ex);
            
        }
        return converter;
    }

    @Override
    public void destroy(Behavior t, CreationalContext<Behavior> cc)
    {
    }
}
