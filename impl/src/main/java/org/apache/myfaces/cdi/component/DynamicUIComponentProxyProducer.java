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

package org.apache.myfaces.cdi.component;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;
import javax.faces.component.ContextCallback;
import javax.faces.component.UIComponent;
import javax.faces.component.search.SearchExpressionContext;
import javax.faces.component.search.SearchExpressionHandler;
import javax.faces.component.search.SearchExpressionHint;
import javax.faces.context.FacesContext;
import org.apache.myfaces.cdi.viewTransient.ViewTransientScoped;
import org.apache.myfaces.shared.util.ClassUtils;

/**
 *
 */
@Typed
public class DynamicUIComponentProxyProducer implements Bean<UIComponent>, Serializable, PassivationCapable
{
    private static final long serialVersionUID = 1L;

    private BeanManager beanManager;
    private ResolveComponentInfo typeInfo;
    private Set<Type> types;
    private Class<?> beanClass;

    public DynamicUIComponentProxyProducer(BeanManager beanManager, ResolveComponentInfo typeInfo)
    {
        this.beanManager = beanManager;
        this.typeInfo = typeInfo;
        types = new HashSet<Type>(asList(typeInfo.getType(), Object.class));
        beanClass = ClassUtils.simpleClassForName(typeInfo.getType().getTypeName());
    }    

    @Override
    public String getId()
    {
        return typeInfo.getType()+"_"+typeInfo.getExpression();
    }
    
    public static class DefaultAnnotationLiteral extends AnnotationLiteral<ResolveComponent> implements ResolveComponent
    {
        private static final long serialVersionUID = 1L;
        
        private String value;

        public DefaultAnnotationLiteral(String value)
        {
            this.value = value;
        }

        @Override
        public String value()
        {
            return value;
        }
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
        return Collections.singleton((Annotation) new DefaultAnnotationLiteral(typeInfo.getExpression()));
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return ViewTransientScoped.class;
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
    public UIComponent create(CreationalContext<UIComponent> creationalContext) 
    {        
        if ("".equals(typeInfo.getExpression()))
        {
            return UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        }
        else
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            UIComponent refComponent = UIComponent.getCurrentComponent(facesContext);
            refComponent = refComponent == null ? facesContext.getViewRoot() : refComponent;
            
            SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();
            Set<SearchExpressionHint> hints = new HashSet<SearchExpressionHint>();
            hints.add(SearchExpressionHint.SKIP_VIRTUAL_COMPONENTS);
            SearchExpressionContext searchContext = SearchExpressionContext.createSearchExpressionContext(
                    facesContext, refComponent, hints, null);
            FindComponentCallback callback = new FindComponentCallback();
            handler.resolveComponent(searchContext, typeInfo.getExpression(), callback);
            return callback.getComponent();
        }
    }
    
    private static class FindComponentCallback implements ContextCallback
    {
        private UIComponent component = null;

        @Override
        public void invokeContextCallback(FacesContext context, UIComponent target)
        {
            if (component == null)
            {
                component = target;
            }
        }

        public UIComponent getComponent()
        {
            return component;
        }
    }

    @Override
    public void destroy(UIComponent t, CreationalContext<UIComponent> cc)
    {
    }
    
    public static <A extends Annotation> A getQualifier(InjectionPoint injectionPoint, Class<A> qualifierClass)
    {
        for (Annotation annotation : injectionPoint.getQualifiers())
        {
            if (qualifierClass.isAssignableFrom(annotation.getClass()))
            {
                return qualifierClass.cast(annotation);
            }
        }

        return null;
    }
}
