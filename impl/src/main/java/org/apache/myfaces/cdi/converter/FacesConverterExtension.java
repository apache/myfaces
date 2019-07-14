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
package org.apache.myfaces.cdi.converter;

import java.util.HashSet;
import java.util.Set;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.faces.convert.FacesConverter;

public class FacesConverterExtension implements Extension
{
    private Set<FacesConverterInfo> types = new HashSet<>();

    public <T> void collect(@Observes ProcessManagedBean<T> event)
    {
        Annotated annotated = event.getAnnotatedBeanClass();
        if (annotated.isAnnotationPresent(FacesConverter.class))
        {
            FacesConverter converter = (FacesConverter) annotated.getAnnotation(FacesConverter.class);
            if (converter.managed())
            {
                boolean hasForClass = !Object.class.equals(converter.forClass());
                if (hasForClass)
                {
                    types.add(new FacesConverterInfo(annotated.getBaseType(), converter.forClass(), ""));
                }
                
                boolean hasValue = converter.value().length() > 0;
                if (hasValue)
                {
                    types.add(new FacesConverterInfo(annotated.getBaseType(), Object.class, converter.value()));
                }
            }
        }
    }
    
    public void afterBean(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        for (FacesConverterInfo typeInfo : types)
        {
            afterBeanDiscovery.addBean(new FacesConverterProducer(beanManager, typeInfo));
        }
    }

    public void cleanup(@Observes AfterDeploymentValidation afterDeploymentValidation)
    {
        types.clear();
    }
}
