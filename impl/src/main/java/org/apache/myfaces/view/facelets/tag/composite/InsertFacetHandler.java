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

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;

/**
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFFaceletTag(name="composite:insertFacet")
public class InsertFacetHandler extends TagHandler
{
    @JSFFaceletAttribute(name="name",
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.String")
    protected final TagAttribute _name;
    
    @JSFFaceletAttribute(name="required",
            className="javax.el.ValueExpression",
            deferredValueType="boolean")
    protected final TagAttribute _required;
    
    public InsertFacetHandler(TagConfig config)
    {
        super(config);
        _name = getRequiredAttribute("name");
        _required = getAttribute("required");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException
    {
        // TODO: Add "required" behavior: "... If true, and there is no 
        // such facet present on the top level component, a 
        // TagException must be thrown, containing the Location, 
        // the facet name, and a localized descriptive error message...."
        
        String facetName = _name.getValue(ctx);
        
        UIComponent parentCompositeComponent = 
            UIComponent.getCurrentCompositeComponent(ctx.getFacesContext());
        
        UIComponent facetComponent = parentCompositeComponent.getFacet(facetName);
        
        if (facetComponent != null)
        {
            parent.getFacets().put(facetName, facetComponent);
        }
        
        //TODO: Reset clientId calling setId() when necessary            
        //for (UIComponent child : childList)
        //{
        //    child.setId(child.getId());
        //}
    }

}
