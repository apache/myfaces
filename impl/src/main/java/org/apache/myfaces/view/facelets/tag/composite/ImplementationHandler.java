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
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;

/**
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ImplementationHandler extends TagHandler
{
    public final static String NAME = "implementation";
    
    public ImplementationHandler(TagConfig config)
    {
        super(config);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException
    {
        if (!FaceletViewDeclarationLanguage.
                isBuildingCompositeComponentMetadata(ctx.getFacesContext()))
        {
            // If this tag is found in a facelet, the compiler has trimmed all
            // tags outside this one excluding composite:interface, so "parent"
            // is a component used as value for the facet key
            // UIComponent.COMPOSITE_FACET_NAME in a composite component. 
            nextHandler.apply(ctx, parent);
        }        
    }
}
