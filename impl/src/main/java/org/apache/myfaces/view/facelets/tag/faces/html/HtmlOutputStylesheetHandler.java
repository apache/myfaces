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
package org.apache.myfaces.view.facelets.tag.faces.html;

import java.util.Iterator;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.FaceletContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.FaceletCompositionContext;
import org.apache.myfaces.view.facelets.el.CompositeComponentELUtils;
import org.apache.myfaces.view.facelets.tag.faces.ComponentSupport;
import org.apache.myfaces.view.facelets.tag.faces.RelocatableResourceHandler;

/**
 * 
 * @since 2.0
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFFaceletTag(
        name = "h:outputStylesheet",
        componentClass = "org.apache.myfaces.view.facelets.tag.faces.html._HtmlOutputStylesheet")
public class HtmlOutputStylesheetHandler extends HtmlComponentHandler implements RelocatableResourceHandler
{

    public HtmlOutputStylesheetHandler(ComponentConfig config)
    {
        super(config);
    }

    @Override
    public UIComponent findChildByTagId(FaceletContext ctx, UIComponent parent, String id)
    {
        UIComponent c = null;
        UIViewRoot root = ComponentSupport.getViewRoot(ctx, parent);
        if (root.getFacetCount() > 0)
        {
            Iterator<UIComponent> itr = root.getFacets().values().iterator();
            while (itr.hasNext() && c == null)
            {
                UIComponent facet = itr.next();
                c = ComponentSupport.findChildByTagId(facet, id);
            }
        }
        return c;
    }

    @Override
    public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent)
    {
        UIComponent parentCompositeComponent
                = FaceletCompositionContext.getCurrentInstance(ctx).getCompositeComponentFromStack();
        if (parentCompositeComponent != null)
        {
            c.getAttributes().put(CompositeComponentELUtils.LOCATION_KEY,
                    parentCompositeComponent.getAttributes().get(CompositeComponentELUtils.LOCATION_KEY));
        }
    }

}
