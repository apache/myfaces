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
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.AbstractFaceletContext;

/**
 * Insert or move the facet from the composite component body to the expected location.
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFFaceletTag(name="composite:insertFacet")
public class InsertFacetHandler extends TagHandler
{
    /**
     * The name that identify the current facet.
     */
    @JSFFaceletAttribute(name="name",
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.String",
            required=true)
    protected final TagAttribute _name;
    
    /**
     * Define if the facet to be inserted is required or not for every instance of
     * this composite component.
     */
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
        String facetName = _name.getValue(ctx);
        
        UIComponent parentCompositeComponent = ((AbstractFaceletContext)ctx).getCompositeComponentFromStack();
        
        if (_required != null && _required.getBoolean(ctx) && parentCompositeComponent.getFacet(facetName) == null)
        {
            throw new TagException(this.tag, "Cannot found facet with name "+facetName+" in composite component "
                    +parentCompositeComponent.getClientId(ctx.getFacesContext()));
        }
        
        parentCompositeComponent.subscribeToEvent(PostAddToViewEvent.class, 
                new RelocateFacetListener(parent, facetName));
    }

    public static final class RelocateFacetListener 
        implements ComponentSystemEventListener
    {
        private final UIComponent _targetComponent;
        
        private final String _facetName;
    
        public RelocateFacetListener(UIComponent targetComponent, String facetName)
        {
            _targetComponent = targetComponent;
            _facetName = facetName;
        }
        
        @Override
        public void processEvent(ComponentSystemEvent event)
        {
            UIComponent parentCompositeComponent = event.getComponent();
            
            UIComponent facetComponent = parentCompositeComponent.getFacet(_facetName);
            
            if (facetComponent != null)
            {
                _targetComponent.getFacets().put(_facetName, facetComponent);
            }
        }
    }
}
