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
 * Sets the specified name and attribute on the parent UIComponent. If the "value" specified is not a literal, it will
 * instead set the ValueExpression on the UIComponent. 
 * 
 * See jakarta.faces.component.UIComponent#getAttributes()
 * See jakarta.faces.component.UIComponent#setValueExpression(java.lang.String, jakarta.el.ValueExpression)
 * @author Jacob Hookom
 * @version $Id$
 */
@JSFFaceletTag(name = "f:attribute", bodyContent = "empty")
public final class AttributeHandler extends TagHandler
    implements jakarta.faces.view.facelets.AttributeHandler
{
    private final TagAttribute _name;
    private final TagAttribute _value;

    public AttributeHandler(TagConfig config)
    {
        super(config);
        _name = getRequiredAttribute("name");
        _value = getRequiredAttribute("value");
    }

    /*
     * (non-Javadoc)
     * 
     * See jakarta.faces.view.facelets.FaceletHandler#apply(
     *       jakarta.faces.view.facelets.FaceletContext, jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        if (parent == null)
        {
            throw new TagException(this.tag, "Parent UIComponent was null");
        }

        // only process if the parent is new to the tree
        if (parent.getParent() == null)
        {
            String n = _name.getValue(ctx);
            if (!parent.getAttributes().containsKey(n))
            {
                if (_value.isLiteral())
                {
                    parent.getAttributes().put(n, _value.getValue());
                }
                else
                {
                    parent.setValueExpression(n, _value.getValueExpression(ctx, Object.class));
                }
            }
        }
    }

    @Override
    public String getAttributeName(FaceletContext ctx)
    {
        return _name.getValue(ctx);
    }
}
