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

package org.apache.myfaces.cdi.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.faces.model.DataModel;
import javax.faces.model.FacesDataModel;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.util.lang.ClassUtils;

public class FacesDataModelExtension implements Extension
{
    private Set<DataModelInfo> types = new HashSet<>();

    void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event, BeanManager beanManager)
    {
        AnnotatedType beanHolder = beanManager.createAnnotatedType(FacesDataModelClassBeanHolder.class);
        event.addAnnotatedType(beanHolder, beanHolder.getJavaClass().getName());
    }

    public <T> void collect(@Observes ProcessManagedBean<T> event)
    {
        Annotated annotated = event.getAnnotatedBeanClass();
        if (annotated.isAnnotationPresent(FacesDataModel.class))
        {
            FacesDataModel model = (FacesDataModel) annotated.getAnnotation(FacesDataModel.class);

            boolean hasValue = model.forClass() != null;
            if (hasValue)
            {
                types.add(new DataModelInfo(annotated.getBaseType(), model.forClass()));
            }
        }
    }
    
    public void afterBean(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        for (DataModelInfo typeInfo : types)
        {
            afterBeanDiscovery.addBean(new DynamicDataModelProducer(beanManager, typeInfo));
        }
    }
    
    public void afterDeploymentValidation(@Observes AfterDeploymentValidation adv, BeanManager beanManager)
    {
        FacesDataModelClassBeanHolder holder = CDIUtils.get(beanManager, FacesDataModelClassBeanHolder.class);
        for (DataModelInfo typeInfo : types)
        {
            holder.addFacesDataModel(typeInfo.getForClass(), 
                    ClassUtils.simpleClassForName(typeInfo.getType().getTypeName()));
        }
        // Initialize unmodifiable wrapper
        Map<Class<?>,Class<? extends DataModel>> map = holder.getClassInstanceToDataModelWrapperClassMap();
        
        types.clear();
    }
}
