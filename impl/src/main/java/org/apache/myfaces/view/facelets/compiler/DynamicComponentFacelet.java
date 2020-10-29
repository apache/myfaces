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
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletHandler;
import org.apache.myfaces.view.facelets.FaceletCompositionContext;

/**
 *
 * @author lu4242
 */
public class DynamicComponentFacelet implements FaceletHandler
{
    private NamespaceHandler next;

    public DynamicComponentFacelet(NamespaceHandler next)
    {
        this.next = next;
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException
    {
        FaceletCompositionContext fcc = FaceletCompositionContext.getCurrentInstance(ctx);
        boolean nextHandlerCompositeComponent = isNextHandlerCompositeComponent();
        try
        {
            if (nextHandlerCompositeComponent)
            {
                fcc.setDynamicCompositeComponentHandler(true);
            }
            next.apply(ctx, parent);
        }
        finally
        {
            if (nextHandlerCompositeComponent)
            {
                fcc.setDynamicCompositeComponentHandler(false);
            }
        }
    }
    
    public boolean isNextHandlerComponent()
    {
        return next.isNextHandlerComponent();
    }
    
    public boolean isNextHandlerCompositeComponent()
    {
        return next.isNextHandlerCompositeComponent();
    }    
}
