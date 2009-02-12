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
package com.sun.facelets.tag.ui;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.webapp.pdl.facelets.FaceletContext;
import javax.faces.webapp.pdl.facelets.FaceletException;

import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagAttributeException;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagHandler;

/**
 * @author Jacob Hookom
 * @version $Id: DefineHandler.java,v 1.5 2008/07/13 19:01:41 rlubke Exp $
 */
public final class DefineHandler extends TagHandler
{

    private final String name;

    /**
     * @param config
     */
    public DefineHandler(TagConfig config)
    {
        super(config);
        TagAttribute attr = this.getRequiredAttribute("name");
        if (!attr.isLiteral())
        {
            throw new TagAttributeException(this.tag, attr, "Must be Literal");
        }
        this.name = attr.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.webapp.pdl.facelets.FaceletHandler#apply(javax.faces.webapp.pdl.facelets.FaceletContext, javax.faces.component.UIComponent)
     */
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        // no-op
        // this.nextHandler.apply(ctx, parent);
    }

    public void applyDefinition(FaceletContext ctx, UIComponent parent) throws IOException, FacesException,
            FaceletException, ELException
    {
        this.nextHandler.apply(ctx, parent);
    }

    public String getName()
    {
        return this.name;
    }
}
