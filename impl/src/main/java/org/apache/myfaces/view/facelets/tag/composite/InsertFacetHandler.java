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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;

/**
 * Insert or move the facet from the composite component body to the expected location.
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFFaceletTag(name="composite:insertFacet")
public class InsertFacetHandler extends TagHandler
{
    public static String USES_INSERT_FACET = "org.apache.myfaces.USES_INSERT_FACET";
    public static String INSERT_FACET_TARGET_ID = "org.apache.myfaces.INSERT_FACET_TARGET_ID.";
    public static String INSERT_FACET_ORDERING = "org.apache.myfaces.INSERT_FACET_ORDERING.";
    
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
        
        if (!ComponentHandler.isNew(parentCompositeComponent))
        {
            UIComponent facet = parent.getFacet(facetName);
            if (facet != null)
            {
                if (facet.getAttributes().containsKey(ComponentSupport.FACET_CREATED_UIPANEL_MARKER))
                {
                    ComponentSupport.markForDeletion(facet);
                    for (UIComponent child : facet.getChildren())
                    {
                        if (child.getAttributes().remove(USES_INSERT_FACET) != null)
                        {
                            ComponentSupport.finalizeForDeletion(child);
                        }
                    }
                }
                ComponentSupport.finalizeForDeletion(facet);
            }
            return;
        }
        
        parentCompositeComponent.subscribeToEvent(PostAddToViewEvent.class, 
                new RelocateFacetListener(parent, facetName));
        /*
        if (ctx.getFacesContext().getAttributes().containsKey(
                FaceletViewDeclarationLanguage.MARK_INITIAL_STATE_KEY))
        {
            parentCompositeComponent.subscribeToEvent(PostBuildComponentTreeOnRestoreViewEvent.class, 
                    new RelocateFacetListener(parent, facetName));
        }*/
    }

    public static final class RelocateFacetListener 
        implements ComponentSystemEventListener, StateHolder
    {
        private UIComponent _targetComponent;
        private String _targetClientId;
        private String _facetName;
    
        public RelocateFacetListener()
        {
        }
        
        public RelocateFacetListener(UIComponent targetComponent, String facetName)
        {
            _targetComponent = targetComponent;
            _facetName = facetName;
        }
        
        public void processEvent(ComponentSystemEvent event)
        {
            UIComponent parentCompositeComponent = event.getComponent();
            
            UIComponent facetComponent = parentCompositeComponent.getFacet(_facetName);
            
            if (_targetComponent == null)
            {
                //All composite components are NamingContainer and the target is inside it, so we can remove the prefix.
                _targetComponent = parentCompositeComponent.findComponent(_targetClientId.substring(parentCompositeComponent.getClientId().length()+1));
            }            
                        
            if (facetComponent != null)
            {
                Map<String, Object> ccAttributes = parentCompositeComponent.getAttributes();
                UIComponent oldFacet = _targetComponent.getFacets().get(_facetName);
                String insertFacetKey = INSERT_FACET_TARGET_ID+_facetName;
                
                if (!ccAttributes.containsKey(insertFacetKey))
                {
                    ccAttributes.put(insertFacetKey, _targetComponent.getClientId());
                }
                
                if (oldFacet != null && Boolean.TRUE.equals(oldFacet.getAttributes().get(ComponentSupport.FACET_CREATED_UIPANEL_MARKER)))
                {
                    List<UIComponent> childList = new ArrayList<UIComponent>(facetComponent.getChildren());
                    
                    List<UIComponent> targetChildrenList = oldFacet.getChildren(); 

                    List<String> ids = (List<String>) ccAttributes.get(INSERT_FACET_ORDERING+_facetName);
                    if (ids != null && targetChildrenList.size() > 0)
                    {
                        int i = 0;
                        int j = 0;
                        int k = 0;
                        while (i < ids.size() && j < targetChildrenList.size() && k < childList.size())
                        {
                            if (ids.get(i) != null)
                            {
                                if (ids.get(i).equals(childList.get(k).getAttributes().get(ComponentSupport.MARK_CREATED)))
                                {
                                    if (!ids.get(i).equals(targetChildrenList.get(j).getAttributes().get(ComponentSupport.MARK_CREATED)))
                                    {
                                        targetChildrenList.add(j, childList.get(k));
                                        k++;
                                    }
                                    j++;
                                }
                                else if (ids.get(i).equals(targetChildrenList.get(j).getAttributes().get(ComponentSupport.MARK_CREATED)))
                                {
                                    j++;
                                }
                            }
                            i++;
                        }
                        while (k < childList.size())
                        {
                            targetChildrenList.add(j, childList.get(k));
                            k++;
                            j++;
                        }
                    }
                    else
                    {
                        _targetComponent.getFacets().put(_facetName, facetComponent);
                    }
                }
                else
                {
                    _targetComponent.getFacets().put(_facetName, facetComponent);
                }

            }
        }

        public Object saveState(FacesContext context)
        {
            return new Object[]{_targetComponent != null ? _targetComponent.getClientId() : _targetClientId , _facetName};
        }

        public void restoreState(FacesContext context, Object state)
        {
            Object[] values = (Object[])state;
            _targetClientId = (String) values[0];
            _facetName = (String) values[1];
        }

        public boolean isTransient()
        {
            return false;
        }

        public void setTransient(boolean newTransientValue)
        {
            // no-op as listener is transient
        }
    }
}
