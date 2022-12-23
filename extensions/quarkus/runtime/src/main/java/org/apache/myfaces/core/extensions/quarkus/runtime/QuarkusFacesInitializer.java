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
package org.apache.myfaces.core.extensions.quarkus.runtime;

import java.util.Map;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.model.DataModel;
import jakarta.servlet.ServletContext;

import org.apache.myfaces.cdi.model.FacesDataModelManager;
import org.apache.myfaces.spi.FactoryFinderProviderFactory;
import org.apache.myfaces.webapp.FacesInitializerImpl;

import org.apache.myfaces.core.extensions.quarkus.runtime.spi.QuarkusFactoryFinderProviderFactory;

/**
 * Custom FacesInitializer to execute our integration code, always before MyFaces starts.
 * With ServletListeners or other ways, we would have order/priority problems.
 */
public class QuarkusFacesInitializer extends FacesInitializerImpl
{

    @Override
    public void initFaces(ServletContext servletContext)
    {
        FactoryFinderProviderFactory.setInstance(new QuarkusFactoryFinderProviderFactory());

        // see FacesDataModelExtension
        FacesDataModelManager facesDataModelManager = CDI.current().select(FacesDataModelManager.class).get();
        for (Map.Entry<Class<? extends DataModel>, Class<?>> typeInfo : MyFacesRecorder.FACES_DATA_MODELS.entrySet())
        {
            facesDataModelManager.addFacesDataModel(typeInfo.getValue(), typeInfo.getKey());
        }
        facesDataModelManager.init();

        super.initFaces(servletContext);
    }
}
