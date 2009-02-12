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
package com.sun.facelets.tag.jsf.core;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.webapp.pdl.facelets.FaceletContext;
import javax.faces.webapp.pdl.facelets.FaceletException;

import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagException;
import com.sun.facelets.tag.TagHandler;

/**
 * Sets the specified name and attribute on the parent UIComponent. If the "value" specified is not a literal, it will
 * instead set the ValueExpression on the UIComponent. <p /> See <a target="_new"
 * href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/attribute.html">tag documentation</a>.
 * 
 * @see javax.faces.component.UIComponent#getAttributes()
 * @see javax.faces.component.UIComponent#setValueExpression(java.lang.String, javax.el.ValueExpression)
 * @author Jacob Hookom
 * @version $Id: AttributeHandler.java,v 1.3 2008/07/13 19:01:44 rlubke Exp $
 */
public final class AttributeHandler extends TagHandler
{
    private final TagAttribute _name;

    private final TagAttribute _value;

    /**
     * @param config
     */
    public AttributeHandler(TagConfig config)
    {
        super(config);
        _name = getRequiredAttribute("name");
        _value = getRequiredAttribute("value");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.webapp.pdl.facelets.FaceletHandler#apply(javax.faces.webapp.pdl.facelets.FaceletContext, javax.faces.component.UIComponent)
     */
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
}
