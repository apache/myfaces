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
package org.apache.myfaces.view.facelets.tag.jsf.core;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;

/**
 * Defines the view metadata. It is expected that this tag contains only
 * one or many f:viewParam tags.
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFFaceletTag(name="f:metadata")
public final class ViewMetadataHandler extends TagHandler
{

    public ViewMetadataHandler(TagConfig config)
    {
        super(config);
    }

    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException
    {
        if (FaceletViewDeclarationLanguage.
                isBuildingViewMetadata(ctx.getFacesContext()))
        {
            if (parent == null)
            {
                throw new TagException(this.tag, "Parent UIComponent was null");
            }
            if (! (parent instanceof UIViewRoot) )
            {
                throw new TagException(this.tag, "Parent UIComponent "+parent.getId()+" should be instance of UIViewRoot");
            }
            UIComponent metadataFacet = parent.getFacet(UIViewRoot.METADATA_FACET_NAME);
            if (metadataFacet == null)
            {
                metadataFacet = ctx.getFacesContext().getApplication().createComponent(UIPanel.COMPONENT_TYPE);
                metadataFacet.setId(UIViewRoot.METADATA_FACET_NAME);
                metadataFacet.getAttributes().put(ComponentSupport.FACET_CREATED_UIPANEL_MARKER, true);
                parent.getFacets().put(UIViewRoot.METADATA_FACET_NAME, metadataFacet);
            }
            parent.getAttributes().put(FacetHandler.KEY, UIViewRoot.METADATA_FACET_NAME);
            try
            {
                this.nextHandler.apply(ctx, parent);
            }
            finally
            {
                parent.getAttributes().remove(FacetHandler.KEY);
            }
        }
    }
}
