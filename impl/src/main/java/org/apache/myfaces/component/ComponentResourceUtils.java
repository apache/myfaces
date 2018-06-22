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
package org.apache.myfaces.component;

import javax.faces.application.StateManager;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.myfaces.context.RequestViewContext;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;

public class ComponentResourceUtils
{
    public static void addComponentResource(FacesContext context, UIComponent componentResource)
    {
        addComponentResource(context, componentResource, null);
    }

    public static void addComponentResource(FacesContext context, UIComponent componentResource, String target)
    {
        // this is required to make dynamic resource loading possible since JSF 2.3
        // also see {@link PartialViewContextImpl#processRenderResource}
        if (context.getPartialViewContext().isAjaxRequest())
        {
            boolean isBuildingInitialState = context.getAttributes().
                containsKey(StateManager.IS_BUILDING_INITIAL_STATE);

            // The next condition takes into account the current request is an ajax request. 
            boolean isPostAddToViewEventAfterBuildInitialState = !isBuildingInitialState ||
                (isBuildingInitialState && FaceletViewDeclarationLanguage.isRefreshingTransientBuild(context));

            if (isPostAddToViewEventAfterBuildInitialState)
            {
                RequestViewContext requestViewContext = RequestViewContext.getCurrentInstance(context);
                requestViewContext.setRenderTarget("head", true, componentResource);
            }
        }
        
        context.getViewRoot().addComponentResource(context, componentResource, target);
    }
}
