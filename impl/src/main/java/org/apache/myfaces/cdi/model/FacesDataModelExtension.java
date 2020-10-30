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
import java.util.Set;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.faces.model.FacesDataModel;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.util.lang.ClassUtils;

public class FacesDataModelExtension implements Extension
{
    private Set<FacesDataModelInfo> types = new HashSet<>();

    void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event, BeanManager beanManager)
    {
        AnnotatedType beanHolder = beanManager.createAnnotatedType(FacesDataModelManager.class);
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
                types.add(new FacesDataModelInfo(annotated.getBaseType(), model.forClass()));
            }
        }
    }
    
    public void afterDeploymentValidation(@Observes AfterDeploymentValidation adv, BeanManager beanManager)
    {
        FacesDataModelManager facesDataModelManager = CDIUtils.get(beanManager, FacesDataModelManager.class);

        for (FacesDataModelInfo typeInfo : types)
        {
            facesDataModelManager.addFacesDataModel(typeInfo.getForClass(), 
                    ClassUtils.simpleClassForName(typeInfo.getType().getTypeName()));
        }

        facesDataModelManager.init();

        types.clear();
    }
}
