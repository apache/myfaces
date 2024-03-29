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
package org.apache.myfaces.renderkit.html;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.renderkit.html.base.HtmlRenderer;
import org.apache.myfaces.core.api.shared.lang.Assert;

/**
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFRenderer(renderKitId = "HTML_BASIC", family = "jakarta.faces.Output", type = "jakarta.faces.CompositeFacet")
public class HtmlCompositeFacetRenderer extends HtmlRenderer
{
    @Override
    public boolean getRendersChildren()
    {
        return true;
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException
    {
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException
    {
        Assert.notNull(context, "context");
        Assert.notNull(component, "component");

        String facetName = (String) component.getAttributes().get(UIComponent.FACETS_KEY);
        if (facetName == null)
        {
            throw new IOException("Composite facet name under key UIComponent.FACETS_KEY not found "+
                    component.getClientId(context));
        }
        
        UIComponent compositeComponent = UIComponent.getCurrentCompositeComponent(context);
        if (compositeComponent == null)
        {
            throw new IOException("parent Composite Component not found when rendering composite component facet "+
                    component.getClientId(context));
        }
        
        UIComponent compositeFacet = compositeComponent.getFacet(facetName);
        if (compositeFacet != null)
        {
            compositeFacet.encodeAll(context);
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException
    {
    }
}
