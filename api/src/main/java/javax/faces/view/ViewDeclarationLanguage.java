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
package javax.faces.view;

import java.beans.BeanInfo;
import java.io.IOException;
import java.util.List;

import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2008-09-24 19:54:04 -0400 (mer., 17 sept. 2008) $
 * 
 * @since 2.0
 */
public abstract class ViewDeclarationLanguage
{
    public abstract void buildView(FacesContext context, UIViewRoot view) throws IOException;

    public abstract UIViewRoot createView(FacesContext context, String viewId);

    public abstract BeanInfo getComponentMetadata(FacesContext context, Resource componentResource);

    public abstract Resource getScriptComponentResource(FacesContext context, Resource componentResource);
    
    public abstract StateManagementStrategy getStateManagementStrategy(FacesContext context, String viewId); 

    public abstract ViewMetadata getViewMetadata(FacesContext context, String viewId);

    public abstract void renderView(FacesContext context, UIViewRoot view) throws IOException;

    public abstract UIViewRoot restoreView(FacesContext context, String viewId);
    
    public void retargetAttachedObjects(FacesContext context, UIComponent topLevelComponent, List<AttachedObjectHandler> handlers)
    {
        //TODO: implement impl
    }

    public void retargetMethodExpressions(FacesContext context, UIComponent topLevelComponent)
    {
        //TODO: implement impl
    }
              
}
