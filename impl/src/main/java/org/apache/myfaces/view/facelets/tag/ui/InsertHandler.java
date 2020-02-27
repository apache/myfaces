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
package org.apache.myfaces.view.facelets.tag.ui;

import java.io.IOException;

import javax.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.TemplateClient;
import org.apache.myfaces.view.facelets.tag.ComponentContainerHandler;

/**
 * The insert tag is used within your templates to declare spots of replicable data.
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
@JSFFaceletTag(name="ui:insert")
public final class InsertHandler extends TagHandler implements TemplateClient, ComponentContainerHandler
{

    /**
     * The optional name attribute matches the associated &lt;ui:define/&gt; 
     * tag in this template's client. If no name is specified, it's expected 
     * that the whole template client will be inserted.
     */
    @JSFFaceletAttribute(
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.String",
            required=true)
    private final String name;

    /**
     * @param config
     */
    public InsertHandler(TagConfig config)
    {
        super(config);
        TagAttribute attr = this.getAttribute("name");
        if (attr != null)
        {
            if (!attr.isLiteral())
            {
                throw new TagAttributeException(this.tag, attr, "Must be Literal");
            }
            this.name = attr.getValue();
        }
        else
        {
            this.name = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see jakarta.faces.view.facelets.FaceletHandler#apply(jakarta.faces.view.facelets.FaceletContext, jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
        actx.extendClient(this);
        boolean found = false;
        try
        {
            found = actx.includeDefinition(parent, this.name);
        }
        finally
        {
            actx.popExtendedClient(this);
        }
        if (!found)
        {
            this.nextHandler.apply(ctx, parent);
        }
    }

    public boolean apply(FaceletContext ctx, UIComponent parent, String name) throws IOException, FacesException,
            FaceletException, ELException
    {
        if (this.name == name || this.name != null && this.name.equals(name))
        {
            this.nextHandler.apply(ctx, parent);
            return true;
        }
        return false;
    }
}
