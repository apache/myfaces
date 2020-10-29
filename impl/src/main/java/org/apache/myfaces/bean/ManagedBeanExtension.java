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

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.enterprise.util.AnnotationLiteral;
import org.apache.myfaces.bean.literal.ApplicationScopedLiteral;
import org.apache.myfaces.bean.literal.DependentScopeLiteral;
import org.apache.myfaces.bean.literal.RequestScopedLiteral;
import org.apache.myfaces.bean.literal.SessionScopedLiteral;
import org.apache.myfaces.cdi.view.ViewScopedLiteral;

public class ManagedBeanExtension implements Extension
{
    private static final Logger LOGGER = Logger.getLogger(ManagedBeanExtension.class.getName());

    private Map<Class<? extends Annotation>, Class<? extends Annotation>> mappings = new HashMap<>();
    private Map<Class<? extends Annotation>, AnnotationLiteral> literals = new HashMap<>();

    public ManagedBeanExtension()
    {
        mappings.put(jakarta.faces.bean.ApplicationScoped.class,
                javax.enterprise.context.ApplicationScoped.class);
        mappings.put(jakarta.faces.bean.SessionScoped.class,
                javax.enterprise.context.SessionScoped.class);
        mappings.put(jakarta.faces.bean.RequestScoped.class,
                javax.enterprise.context.RequestScoped.class);
        mappings.put(jakarta.faces.bean.NoneScoped.class,
                javax.enterprise.context.Dependent.class);
        mappings.put(jakarta.faces.bean.ViewScoped.class,
                jakarta.faces.view.ViewScoped.class);
        
        literals.put(javax.enterprise.context.ApplicationScoped.class,
                new ApplicationScopedLiteral());
        literals.put(javax.enterprise.context.SessionScoped.class,
                new SessionScopedLiteral());
        literals.put(javax.enterprise.context.RequestScoped.class,
                new RequestScopedLiteral());
        literals.put(javax.enterprise.context.Dependent.class,
                new DependentScopeLiteral());
        literals.put(jakarta.faces.view.ViewScoped.class,
                new ViewScopedLiteral());
    }

    protected void convertJsf2Scopes(@Observes ProcessAnnotatedType pat)
    {
        Class<?> clazz = pat.getAnnotatedType().getJavaClass();

        jakarta.faces.bean.ManagedBean managedBean = clazz.getAnnotation(jakarta.faces.bean.ManagedBean.class);
        if (managedBean != null)
        {
            Class<? extends Annotation> oldScope = resolveScope(pat);
            if (oldScope != null)
            {
                Class<? extends Annotation> newScope = mappings.get(oldScope);
                
                LOGGER.info("@ManagedBean (" + clazz.getName() + ") was converted to a CDI bean with scope: "
                        + oldScope.getName());

                pat.setAnnotatedType(
                        new ManagedBeanWrapper(pat.getAnnotatedType(), managedBean,
                                newScope, oldScope, literals.get(newScope)));
            }
        }
    }

    private Class<? extends Annotation> resolveScope(ProcessAnnotatedType pat)
    {
        for (Class<? extends Annotation> oldScope : mappings.keySet())
        {
            if (pat.getAnnotatedType().getJavaClass().isAnnotationPresent(oldScope))
            {
                return oldScope;
            }
        }
        return null;
    }
}
