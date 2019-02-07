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
import java.util.Map;
import java.util.stream.Stream;
import javax.faces.FacesWrapper;
import javax.faces.application.Resource;
import javax.faces.application.ViewVisitOption;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

/**
 * @since 2.2
 */
public abstract class ViewDeclarationLanguageWrapper extends ViewDeclarationLanguage 
    implements FacesWrapper<ViewDeclarationLanguage>
{
    private ViewDeclarationLanguage delegate;

    @Deprecated
    public ViewDeclarationLanguageWrapper()
    {
    }

    public ViewDeclarationLanguageWrapper(ViewDeclarationLanguage delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void buildView(FacesContext context, UIViewRoot view) throws IOException
    {
        getWrapped().buildView(context, view);
    }

    @Override
    public UIViewRoot createView(FacesContext context, String viewId)
    {
        return getWrapped().createView(context, viewId);
    }

    @Override
    public BeanInfo getComponentMetadata(FacesContext context, Resource componentResource)
    {
        return getWrapped().getComponentMetadata(context, componentResource);
    }

    @Override
    public Resource getScriptComponentResource(FacesContext context, Resource componentResource)
    {
        return getWrapped().getScriptComponentResource(context, componentResource);
    }

    @Override
    public StateManagementStrategy getStateManagementStrategy(FacesContext context, String viewId)
    {
        return getWrapped().getStateManagementStrategy(context, viewId);
    }

    @Override
    public ViewMetadata getViewMetadata(FacesContext context, String viewId)
    {
        return getWrapped().getViewMetadata(context, viewId);
    }

    @Override
    public void renderView(FacesContext context, UIViewRoot view) throws IOException
    {
        getWrapped().renderView(context, view);
    }

    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId)
    {
        return getWrapped().restoreView(context, viewId);
    }

    @Override
    public void retargetAttachedObjects(FacesContext context, UIComponent topLevelComponent, 
        List<AttachedObjectHandler> handlers)
    {
        getWrapped().retargetAttachedObjects(context, topLevelComponent, handlers);
    }

    @Override
    public void retargetMethodExpressions(FacesContext context, UIComponent topLevelComponent)
    {
        getWrapped().retargetMethodExpressions(context, topLevelComponent);
    }

    @Override
    public String getId()
    {
        return getWrapped().getId();
    }

    @Override
    public boolean viewExists(FacesContext facesContext, String viewId)
    {
        return getWrapped().viewExists(facesContext, viewId);
    }

    @Override
    public UIComponent createComponent(FacesContext context, String taglibURI, String tagName,
        Map<String, Object> attributes)
    {
        return getWrapped().createComponent(context, taglibURI, tagName, attributes);
    }

    @Override
    public List<String> calculateResourceLibraryContracts(FacesContext context, String viewId)
    {
        return getWrapped().calculateResourceLibraryContracts(context, viewId);
    }
    
    @Override
    public ViewDeclarationLanguage getWrapped()
    {
        return delegate;
    }

    @Override
    public Stream<String> getViews(FacesContext facesContext, String path, int maxDepth, ViewVisitOption... options)
    {
        return getWrapped().getViews(facesContext, path, maxDepth, options);
    }

    @Override
    public Stream<String> getViews(FacesContext facesContext, String path, ViewVisitOption... options)
    {
        return getWrapped().getViews(facesContext, path, options);
    }
    
}
