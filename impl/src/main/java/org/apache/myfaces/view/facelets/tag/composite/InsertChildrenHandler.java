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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.FaceletCompositionContext;

/**
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFFaceletTag(name="composite:insertChildren")
public class InsertChildrenHandler extends TagHandler
{
    //public static String USES_INSERT_CHILDREN = "org.apache.myfaces.USES_INSERT_CHILDREN";
    //public static String INSERT_CHILDREN_TARGET_ID = "org.apache.myfaces.INSERT_CHILDREN_TARGET_ID";
    //public static String INSERT_CHILDREN_ORDERING = "org.apache.myfaces.INSERT_CHILDREN_ORDERING";
    
    public static final String INSERT_CHILDREN_USED = "org.apache.myfaces.INSERT_CHILDREN_USED";
    
    private static final Logger log = Logger.getLogger(InsertChildrenHandler.class.getName());

    public InsertChildrenHandler(TagConfig config)
    {
        super(config);
    }

    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException
    {
        UIComponent parentCompositeComponent = FaceletCompositionContext.getCurrentInstance(ctx).getCompositeComponentFromStack();
        
        AbstractFaceletContext actx = (AbstractFaceletContext) ctx;

        if (actx.isBuildingCompositeComponentMetadata())
        {
            CompositeComponentBeanInfo beanInfo = 
                (CompositeComponentBeanInfo) parentCompositeComponent.getAttributes()
                .get(UIComponent.BEANINFO_KEY);
            
            if (beanInfo == null)
            {
                if (log.isLoggable(Level.SEVERE))
                {
                    log.severe("Cannot find composite bean descriptor UIComponent.BEANINFO_KEY ");
                }
                return;
            }
            
            beanInfo.getBeanDescriptor().setValue(INSERT_CHILDREN_USED, Boolean.TRUE);
        }
        else
        {
            actx.includeCompositeComponentDefinition(parent, null);
            
            //parentCompositeComponent.getAttributes().put(INSERT_CHILDREN_USED, Boolean.TRUE);
        }
    }

    /*
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException
    {
        UIComponent parentCompositeComponent = FaceletCompositionContext.getCurrentInstance(ctx).getCompositeComponentFromStack();
        
        if (!ComponentHandler.isNew(parentCompositeComponent))
        {
            //Prevent deletion of components present on parentCompositeComponent. This components will not be changed.
            List<UIComponent> childList = new ArrayList<UIComponent>(parent.getChildren());
            
            boolean oldProcessingEvents = ctx.getFacesContext().isProcessingEvents();
            
            for (UIComponent tcChild : childList)
            {
                if (tcChild.getAttributes().remove(USES_INSERT_CHILDREN) != null)
                {
                    ComponentSupport.finalizeForDeletion(tcChild);
                    ctx.getFacesContext().setProcessingEvents(false);
                    try
                    {
                        parent.getChildren().remove(tcChild);
                        parent.getChildren().add(tcChild);
                    }
                    finally
                    {
                        ctx.getFacesContext().setProcessingEvents(oldProcessingEvents);
                    }
                }
            }
            return;
        }

        parentCompositeComponent.subscribeToEvent(PostAddToViewEvent.class,
                new RelocateAllChildrenListener(parent, parent.getChildCount()));
        parentCompositeComponent.subscribeToEvent(PostBuildComponentTreeOnRestoreViewEvent.class,
                new RelocateAllChildrenListener(parent, parent.getChildCount()));
        */
        /*
        if (ctx.getFacesContext().getAttributes().containsKey(
                FaceletViewDeclarationLanguage.MARK_INITIAL_STATE_KEY))
        {
            parentCompositeComponent.subscribeToEvent(PostBuildComponentTreeOnRestoreViewEvent.class, 
                    new RelocateAllChildrenListener(parent, parent.getChildCount()));
        }*/
    /*
    }
    */
    
    /*
    public static final class RelocateAllChildrenListener 
        implements ComponentSystemEventListener, StateHolder
    {
        private UIComponent _targetComponent;
        private String _targetClientId;
        private int _childIndex;

         
        public RelocateAllChildrenListener()
        {
        }
        
        public RelocateAllChildrenListener(UIComponent targetComponent, int childIndex)
        {
            _targetComponent = targetComponent;
            _childIndex = childIndex;
        }
        
        public void processEvent(ComponentSystemEvent event)
        {
            UIComponent parentCompositeComponent = event.getComponent();
            
            if (_targetComponent == null)
            {
                //All composite components are NamingContainer and the target is inside it, so we can remove the prefix.
                _targetComponent = parentCompositeComponent.findComponent(_targetClientId.substring(parentCompositeComponent.getClientId().length()+1));
                
                if (_targetComponent == null)
                {
                    //Could happen in no org.apache.myfaces.REFRESH_TRANSIENT_BUILD_ON_PSS_PRESERVE_STATE
                    //In this case we cannot relocate, just return;
                    return;
                }
            }

            if (parentCompositeComponent.getChildCount() <= 0)
            {
                return;
            }
            
            List<UIComponent> childList = new ArrayList<UIComponent>(parentCompositeComponent.getChildren());
            
            List<UIComponent> targetChildrenList = _targetComponent.getChildren(); 
            
            Map<String, Object> ccAttributes = parentCompositeComponent.getAttributes();
            if (!ccAttributes.containsKey(INSERT_CHILDREN_TARGET_ID))
            {
                //Save the target and the ordering of ids inserted
                ccAttributes.put(INSERT_CHILDREN_TARGET_ID, _targetComponent.getClientId());
                
                targetChildrenList.addAll(_childIndex, childList);
            }
            else
            {
                //Add one to one based on the ordering set at first time
                List<String> ids = (List<String>) ccAttributes.get(INSERT_CHILDREN_ORDERING);
                if (ids != null && _childIndex < targetChildrenList.size())
                {
                    int i = 0;
                    int j = _childIndex;
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
                    targetChildrenList.addAll(_childIndex, childList);
                }
            }*/
            
            // After check, the commented code is not necessary because at this 
            // point there is no any call to getClientId() yet. But it is better
            // let this code commented, because some day could be useful.
            //
            /*
            UIComponent uniqueIdVendor = (UIComponent) findParentUniqueIdVendor(_targetComponent);
            
            if (uniqueIdVendor != null && !uniqueIdVendor.getClientId().equals(parentCompositeComponent.getClientId()))
            {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                
                for (UIComponent child : childList)
                {
                    regenerateUniqueIds(facesContext, (UniqueIdVendor) uniqueIdVendor, child);
                }
            }*/
        /*
        }

        public Object saveState(FacesContext context)
        {
            return new Object[]{_targetComponent != null ? _targetComponent.getClientId() : _targetClientId , _childIndex};
        }

        public void restoreState(FacesContext context, Object state)
        {
            Object[] values = (Object[])state;
            _targetClientId = (String) values[0];
            _childIndex = (Integer) values[1];
        }

        public boolean isTransient()
        {
            return false;
        }

        public void setTransient(boolean newTransientValue)
        {
            // no-op as listener is transient
        }
        */
        /*
        private void regenerateUniqueIds(FacesContext facesContext, UniqueIdVendor uniqueIdVendor,  UIComponent comp)
        {
            if (comp.getId() != null && comp.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
            {
                //Assign a new generated id according to the new UniqueIdVendor parent 
                comp.setId(((UniqueIdVendor)uniqueIdVendor).createUniqueId(facesContext, null));
            }
            
            if (comp instanceof UniqueIdVendor)
            {
                uniqueIdVendor = (UniqueIdVendor) comp;
            }

            if (comp.getChildCount() > 0)
            {
                for (UIComponent child : comp.getChildren())
                {
                    regenerateUniqueIds(facesContext, uniqueIdVendor, child);
                }
            }
            if (comp.getFacetCount() > 0)
            {
                for (UIComponent child : comp.getFacets().values())
                {
                    regenerateUniqueIds(facesContext, uniqueIdVendor, child);
                }
            }
        }
        */
    /*
    }
    */

    /*
    private static UniqueIdVendor findParentUniqueIdVendor(UIComponent component)
    {
        UIComponent parent = component.getParent();

        while (parent != null)
        {
            if (parent instanceof UniqueIdVendor)
            {
                return (UniqueIdVendor) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }
    */
}
