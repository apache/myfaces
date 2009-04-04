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
import javax.el.VariableMapper;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.view.facelets.el.VariableMapperWrapper;

/**
 * @author Jacob Hookom
 * @version $Id: IncludeHandler.java,v 1.5 2008/07/13 19:01:41 rlubke Exp $
 */
public final class IncludeHandler extends TagHandler
{

    private final TagAttribute src;

    /**
     * @param config
     */
    public IncludeHandler(TagConfig config)
    {
        super(config);
        this.src = this.getRequiredAttribute("src");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.view.facelets.FaceletHandler#apply(javax.faces.view.facelets.FaceletContext, javax.faces.component.UIComponent)
     */
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        String path = this.src.getValue(ctx);
        if (path == null || path.length() == 0)
        {
            return;
        }
        VariableMapper orig = ctx.getVariableMapper();
        ctx.setVariableMapper(new VariableMapperWrapper(orig));
        try
        {
            this.nextHandler.apply(ctx, null);
            ctx.includeFacelet(parent, path);
        }
        finally
        {
            ctx.setVariableMapper(orig);
        }
    }
}
