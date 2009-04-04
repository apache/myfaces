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
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagAttributeException;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.view.facelets.TemplateClient;

/**
 * @author Jacob Hookom
 * @version $Id: InsertHandler.java,v 1.7 2008/07/13 19:01:42 rlubke Exp $
 */
public final class InsertHandler extends TagHandler implements TemplateClient
{

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
     * @see javax.faces.view.facelets.FaceletHandler#apply(javax.faces.view.facelets.FaceletContext, javax.faces.component.UIComponent)
     */
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        this.nextHandler.apply(ctx, parent);
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
