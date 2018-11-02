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

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.faces.component.behavior.FacesBehavior;

public class FacesBehaviorExtension implements Extension
{
    private Set<FacesBehaviorInfo> types = new HashSet<FacesBehaviorInfo>();

    public <T> void collect(@Observes ProcessManagedBean<T> event)
    {
        if (event.getAnnotatedBeanClass().isAnnotationPresent(FacesBehavior.class))
        {
            Annotated annotated = event.getAnnotatedBeanClass();
            
            Type type = annotated.getBaseType();

            FacesBehavior conv = (FacesBehavior) annotated.getAnnotation(FacesBehavior.class);
            
            if (conv.managed())
            {
                boolean hasValue = conv.value().length() > 0;
                if (hasValue)
                {
                    types.add(new FacesBehaviorInfo(type, conv.value()));
                }
            }
        }
    }
    
    public void afterBean(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        for (FacesBehaviorInfo typeInfo : types)
        {
            afterBeanDiscovery.addBean(new FacesBehaviorProducer(beanManager, typeInfo));
        }
    }

    public void cleanup(@Observes AfterDeploymentValidation afterDeploymentValidation)
    {
        types.clear();
    }
}
