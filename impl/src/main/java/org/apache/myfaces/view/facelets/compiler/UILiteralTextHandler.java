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
package org.apache.myfaces.view.facelets.compiler;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UniqueIdVendor;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;

import org.apache.myfaces.view.facelets.FaceletCompositionContext;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;

final class UILiteralTextHandler extends AbstractUIHandler
{

    protected final String txtString;

    public UILiteralTextHandler(String txtString)
    {
        this.txtString = txtString;
    }

    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        if (parent != null)
        {
            UIComponent c = new UILiteralText(this.txtString);
            UniqueIdVendor uniqueIdVendor
                    = FaceletCompositionContext.getCurrentInstance(ctx).getUniqueIdVendorFromStack();
            if (uniqueIdVendor == null)
            {
                uniqueIdVendor = ComponentSupport.getViewRoot(ctx, parent);
            }
            if (uniqueIdVendor != null)
            {
                // UIViewRoot implements UniqueIdVendor, so there is no need to cast to UIViewRoot
                // and call createUniqueId()
                String uid = uniqueIdVendor.createUniqueId(ctx.getFacesContext(), null);
                c.setId(uid);
            }
            this.addComponent(ctx, parent, c);
        }
    }

    public String getText()
    {
        return this.txtString;
    }

    public String getText(FaceletContext ctx)
    {
        return this.txtString;
    }
}
