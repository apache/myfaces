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

import javax.faces.component.UIComponent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.AbstractFaceletContext;

/**
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFFaceletTag(name="composite:insertChildren")
public class InsertChildrenHandler extends TagHandler
{

    public InsertChildrenHandler(TagConfig config)
    {
        super(config);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException
    {
        UIComponent parentCompositeComponent = ((AbstractFaceletContext)ctx).getCompositeComponentFromStack();
        
        parentCompositeComponent.subscribeToEvent(PostAddToViewEvent.class, 
                new RelocateAllChildrenListener(parent, parentCompositeComponent.getChildCount()));
    }
    
    public static final class RelocateAllChildrenListener 
        implements ComponentSystemEventListener
    {
        private final UIComponent _targetComponent;
        
        private final int _childIndex;

        public RelocateAllChildrenListener(UIComponent targetComponent, int childIndex)
        {
            _targetComponent = targetComponent;
            _childIndex = childIndex;
        }
        
        @Override
        public void processEvent(ComponentSystemEvent event)
        {
            UIComponent parentCompositeComponent = event.getComponent();

            List<UIComponent> childList = new ArrayList<UIComponent>(parentCompositeComponent.getChildren());
            
            List<UIComponent> targetChildrenList = _targetComponent.getChildren(); 
            
            targetChildrenList.addAll(_childIndex, childList);
            
            // After check, the commented code is not necessary because at this 
            // point there is no any call to getClientId() yet. But it is better
            // let this code commented, because some day could be useful.
            //
            //UIComponent uniqueIdVendor = (UIComponent) ComponentSupport.getClosestUniqueIdVendor(_targetComponent);
            //
            //if (uniqueIdVendor != null && !uniqueIdVendor.getClientId().equals(parentCompositeComponent.getClientId()))
            //{
                //Reset clientId calling setId()
                //for (UIComponent child : childList)
                //{
                //    child.setId(child.getId());
                //}
            //}
        }
    }
}
