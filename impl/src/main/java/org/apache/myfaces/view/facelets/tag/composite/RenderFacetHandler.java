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

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagException;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;

/**
 * Render the facet defined on the composite component body to the current location
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFFaceletTag(name="composite:renderFacet")
public class RenderFacetHandler extends ComponentHandler
{
    /**
     * The name that identify the current facet.
     */
    @JSFFaceletAttribute(name="name",
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.String")
    protected final TagAttribute _name;
    
    /**
     * Define if the facet to be inserted is required or not for every instance of
     * this composite component.
     */
    @JSFFaceletAttribute(name="required",
            className="javax.el.ValueExpression",
            deferredValueType="boolean")
    protected final TagAttribute _required;
    
    public RenderFacetHandler(ComponentConfig config)
    {
        super(config);
        _name = getRequiredAttribute("name");
        _required = getAttribute("required");
    }

    @Override
    public void onComponentPopulated(FaceletContext ctx, UIComponent c,
            UIComponent parent)
    {
        UIComponent parentCompositeComponent = UIComponent.getCurrentCompositeComponent(ctx.getFacesContext());
        
        String facetName = _name.getValue(ctx);

        if (_required != null && _required.getBoolean(ctx) && parentCompositeComponent.getFacet(facetName) == null)
        {
            throw new TagException(this.tag, "Cannot found facet with name "+facetName+" in composite component "
                    +parentCompositeComponent.getClientId(ctx.getFacesContext()));
        }
        
        c.getAttributes().put(UIComponent.FACETS_KEY, facetName);
    }
}
