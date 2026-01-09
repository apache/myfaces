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
package org.apache.myfaces.view.facelets.tag.faces.core;

import java.io.IOException;
import java.util.Map;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIPanel;

import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;
import jakarta.faces.view.facelets.TagHandler;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.tag.faces.PassThroughLibrary;

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
    public static final String FACET_HAS_PASSTHROUGH_ATTRIBUTES = "facelets.FACET_HAS_PASSTHROUGH_ATTRIBUTES";

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

        String facetName = this.name.getValue(ctx);
        parent.getAttributes().put(KEY, facetName);

        try
        {
            this.nextHandler.apply(ctx, parent);

            UIComponent child = parent.getFacets().get(facetName);
            if (child == null && hasPassthroughAttributes(tag))
            {
                child = ctx.getFacesContext().getApplication().createComponent(UIPanel.COMPONENT_TYPE);
                parent.getFacets().put(facetName, child);
            }

            if (child != null)
            {
                copyPassthroughAttributes(ctx, child, tag);
                Map<String, Object> passThroughAttributes = child.getPassThroughAttributes(false);
                if (passThroughAttributes != null && !passThroughAttributes.isEmpty())
                {
                    child.getTransientStateHelper().putTransient(FACET_HAS_PASSTHROUGH_ATTRIBUTES, true);
                }
            }
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

    public static boolean hasFacetPassThroughAttributes(UIComponent possibleFacet)
    {
        return possibleFacet.getTransientStateHelper().getTransient(FACET_HAS_PASSTHROUGH_ATTRIBUTES) != null;
    }

    public static boolean hasPassthroughAttributes(Tag t)
    {
        for (String namespace : PassThroughLibrary.NAMESPACES)
        {
            TagAttribute[] passthroughAttrs = t.getAttributes().getAll(namespace);
            if (passthroughAttrs != null && passthroughAttrs.length > 0)
            {
                return true;
            }
        }
        return false;
    }

    public static void copyPassthroughAttributes(FaceletContext ctx, UIComponent c, Tag t)
    {
        if (null == c || null == t)
        {
            return;
        }

        for (String namespace : PassThroughLibrary.NAMESPACES)
        {
            TagAttribute[] passthroughAttrs = t.getAttributes().getAll(namespace);
            if (null != passthroughAttrs && 0 < passthroughAttrs.length)
            {
                Map<String, Object> componentPassthroughAttrs = c.getPassThroughAttributes(true);
                Object attrValue = null;
                for (TagAttribute cur : passthroughAttrs)
                {
                    attrValue = cur.isLiteral() ? cur.getValue(ctx) : cur.getValueExpression(ctx, Object.class);
                    componentPassthroughAttrs.put(cur.getLocalName(), attrValue);
                }
            }
        }
    }
}
