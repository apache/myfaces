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

package org.apache.myfaces.cdi.managedproperty;

import java.lang.reflect.ParameterizedType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.faces.FacesException;
import jakarta.faces.annotation.ManagedProperty;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.cdi.util.AbstractDynamicProducer;
import org.apache.myfaces.util.lang.ClassUtils;

@ApplicationScoped
public class ManagedPropertyProducer extends AbstractDynamicProducer<Object>
{
    class ManagedPropertyAnnotationLiteral extends AnnotationLiteral<ManagedProperty> implements ManagedProperty
    {
        private static final long serialVersionUID = 1L;
        
        private String value;

        public ManagedPropertyAnnotationLiteral(String value)
        {
            this.value = value;
        }

        @Override
        public String value()
        {
            return value;
        }
    }
    
    public ManagedPropertyProducer(BeanManager beanManager, ManagedPropertyInfo typeInfo)
    {
        super(beanManager);
        
        Class<?> beanClass;
        // Need to check for ParameterizedType to support types such as List<String>
        if (typeInfo.getType() instanceof ParameterizedType) 
        {
            beanClass = ClassUtils.simpleClassForName(
                    ((ParameterizedType) typeInfo.getType()).getRawType().getTypeName());
        }
        else 
        {
            // need to use simpleJavaTypeToClass to support Arrays and primitive types
            beanClass = ClassUtils.simpleJavaTypeToClass(typeInfo.getType().getTypeName());
        }

        super.id(typeInfo.getType() + "_" + typeInfo.getExpression())
                .scope(Dependent.class)
                .qualifiers(new ManagedPropertyAnnotationLiteral(typeInfo.getExpression()))
                .types(typeInfo.getType(), Object.class)
                .beanClass(beanClass)
                .create(e -> createManagedProperty(e, typeInfo));
    }

    protected Object createManagedProperty(CreationalContext<Object> cc, ManagedPropertyInfo typeInfo)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null)
        {
            throw new FacesException("@ManagedProperty(\"" + typeInfo.getExpression()
                    + "\") can only be resolved in a Faces request!");
        }

        return facesContext.getApplication().evaluateExpressionGet(
                facesContext, typeInfo.getExpression(), getBeanClass());
    }
}
