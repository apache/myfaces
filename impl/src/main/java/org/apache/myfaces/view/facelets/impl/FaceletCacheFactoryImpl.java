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
package org.apache.myfaces.view.facelets.impl;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.FaceletCache;
import jakarta.faces.view.facelets.FaceletCacheFactory;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.util.WebConfigParamUtils;
import org.apache.myfaces.view.facelets.ELExpressionCacheMode;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;

/**
 * 
 * @author Leonardo Uribe
 * @since 2.1.0
 *
 */
public class FaceletCacheFactoryImpl extends FaceletCacheFactory
{

    @Override
    public FaceletCache getFaceletCache()
    {
        FacesContext context = FacesContext.getCurrentInstance();

        long refreshPeriod;
        if(context.isProjectStage(ProjectStage.Production))
        {
            refreshPeriod = WebConfigParamUtils.getLongInitParameter(context.getExternalContext(),
                    ViewHandler.FACELETS_REFRESH_PERIOD_PARAM_NAME,
                    FaceletViewDeclarationLanguage.DEFAULT_REFRESH_PERIOD_PRODUCTION);
        }
        else
        {
            refreshPeriod = WebConfigParamUtils.getLongInitParameter(context.getExternalContext(),
                    ViewHandler.FACELETS_REFRESH_PERIOD_PARAM_NAME,
                    FaceletViewDeclarationLanguage.DEFAULT_REFRESH_PERIOD);
        }
        
        MyfacesConfig myfacesConfig = MyfacesConfig.getCurrentInstance(context.getExternalContext());        
        if (ELExpressionCacheMode.alwaysRecompile == myfacesConfig.getELExpressionCacheMode())
        {
            return new CacheELFaceletCacheImpl(refreshPeriod);
        }
        else
        {
            return new FaceletCacheImpl(refreshPeriod);
        }
    }

}
