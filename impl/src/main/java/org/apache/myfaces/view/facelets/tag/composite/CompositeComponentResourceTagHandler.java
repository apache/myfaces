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
package org.apache.myfaces.view.facelets.tag.composite;

import java.io.IOException;

import javax.el.VariableMapper;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.MetaRuleset;

import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;
import org.apache.myfaces.view.facelets.el.VariableMapperWrapper;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentBuilderHandler;

/**
 * This handler is responsible for apply composite components. It
 * is created by CompositeResourceLibrary class when a composite component
 * is found.
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class CompositeComponentResourceTagHandler extends ComponentHandler
    implements ComponentBuilderHandler
{
    private final Resource _resource;

    public CompositeComponentResourceTagHandler(ComponentConfig config, Resource resource)
    {
        super(config);
        _resource = resource;
    }

    @Override
    public UIComponent createComponent(FaceletContext ctx)
    {
        FacesContext faceContext = ctx.getFacesContext();
        return faceContext.getApplication().createComponent(faceContext, _resource);
    }

    @Override
    public void applyNextHandler(FaceletContext ctx, UIComponent c)
            throws IOException
    {
        super.applyNextHandler(ctx, c);
        
        applyCompositeComponentFacelet(ctx,c);
        
        FacesContext facesContext = ctx.getFacesContext();
        
        ViewDeclarationLanguage vdl = facesContext.getApplication().getViewHandler().
            getViewDeclarationLanguage(facesContext, facesContext.getViewRoot().getViewId());
        
        // TODO: This method should be called from here, but we have to retrieve
        // its handlers from somewhere.
        //vdl.retargetAttachedObjects(facesContext, c, handlers)
        
        // TODO: Uncomment this code and test it.
        //vdl.retargetMethodExpressions(facesContext, c);
        
        if (ctx.getFacesContext().getAttributes().containsKey(
                FaceletViewDeclarationLanguage.MARK_INITIAL_STATE_KEY))
        {
            // Call it only if we are using partial state saving
            c.markInitialState();
            // Call it to other components created not bound by a tag handler
            c.getFacet(UIComponent.COMPOSITE_FACET_NAME).markInitialState();
        }
    }
    
    protected void applyCompositeComponentFacelet(FaceletContext faceletContext, UIComponent compositeComponentBase) 
        throws IOException
    {
        UIPanel compositeFacetPanel = (UIPanel)
            faceletContext.getFacesContext().getApplication().createComponent(UIPanel.COMPONENT_TYPE);
        compositeComponentBase.getFacets().put(UIComponent.COMPOSITE_FACET_NAME, compositeFacetPanel);
        
        VariableMapper orig = faceletContext.getVariableMapper();
        AbstractFaceletContext actx = (AbstractFaceletContext) faceletContext;
        try
        {
            faceletContext.setVariableMapper(new VariableMapperWrapper(orig));
            
            actx.applyCompositeComponent(compositeFacetPanel, _resource);
        }
        finally
        {
            faceletContext.setVariableMapper(orig);
        }
    }

    @Override
    protected MetaRuleset createMetaRuleset(Class<?> type)
    {
        // TODO: Which metadata rules should be applied for a composite component?
        // For now we apply the same as a default component (from delegate), but there
        // are properties like "binding" that does not apply for.
        return super.createMetaRuleset(type);
    }
}
