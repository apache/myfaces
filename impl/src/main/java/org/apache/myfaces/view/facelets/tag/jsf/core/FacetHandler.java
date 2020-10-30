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
package org.apache.myfaces.view.facelets.tag.jsf.core;

import java.io.IOException;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;
import jakarta.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;

/**
 * Register a named facet on the UIComponent associated with the closest parent UIComponent custom action. 
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
@JSFFaceletTag(
        name = "f:facet",
        bodyContent = "JSP", 
        tagClass="jakarta.faces.webapp.FacetTag")
public final class FacetHandler extends TagHandler 
    implements jakarta.faces.view.facelets.FacetHandler
{

    public static final String KEY = "facelets.FACET_NAME";

    protected final TagAttribute name;

    public FacetHandler(TagConfig config)
    {
        super(config);
        this.name = this.getRequiredAttribute("name");
    }

    /*
     * (non-Javadoc)
     * 
     * See jakarta.faces.view.facelets.FaceletHandler#apply(jakarta.faces.view.facelets.FaceletContext, 
     * jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        if (parent == null)
        {
            throw new TagException(this.tag, "Parent UIComponent was null");
        }
        parent.getAttributes().put(KEY, this.name.getValue(ctx));
        try
        {
            this.nextHandler.apply(ctx, parent);
        }
        finally
        {
            parent.getAttributes().remove(KEY);
        }
    }

    @Override
    public String getFacetName(FaceletContext ctx)
    {
        return this.name.getValue(ctx);
    }
}
