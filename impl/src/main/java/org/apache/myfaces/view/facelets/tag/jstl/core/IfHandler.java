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
package org.apache.myfaces.view.facelets.tag.jstl.core;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.FaceletCompositionContext;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;

/**
 * Simple conditional tag, which evalutes its body if the
 * supplied condition is true and optionally exposes a Boolean
 * scripting variable representing the evaluation of this condition
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
@JSFFaceletTag(name="c:if")
@JSFFaceletAttribute(
        name="scope",
        className="java.lang.String",
        longDescription="Scope for var.")
public final class IfHandler extends TagHandler
{

    /**
     * The test condition that determines whether or
     * not the body content should be processed.
     */
    @JSFFaceletAttribute(className="boolean", required=true)
    private final TagAttribute test;

    /**
     * Name of the exported scoped variable for the
     * resulting value of the test condition. The type
     * of the scoped variable is Boolean.  
     */
    @JSFFaceletAttribute(className="java.lang.String")
    private final TagAttribute var;

    /**
     * @param config
     */
    public IfHandler(TagConfig config)
    {
        super(config);
        this.test = this.getRequiredAttribute("test");
        this.var = this.getAttribute("var");
    }

    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, ELException
    {
        FaceletCompositionContext fcc = FaceletCompositionContext.getCurrentInstance(ctx);
        String uniqueId = fcc.startComponentUniqueIdSection();
        boolean b = getTestValue(ctx, fcc, parent, uniqueId);
        if (this.var != null)
        {
            ctx.setAttribute(var.getValue(ctx), new Boolean(b));
        }
        if (b)
        {
            this.nextHandler.apply(ctx, parent);
        }
        fcc.endComponentUniqueIdSection();
        //AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
        ComponentSupport.saveInitialTagState(ctx, fcc, parent, uniqueId, b);
        if (fcc.isUsingPSSOnThisView() && fcc.isRefreshTransientBuildOnPSS() && !fcc.isRefreshingTransientBuild())
        {
            //Mark the parent component to be saved and restored fully.
            ComponentSupport.markComponentToRestoreFully(ctx.getFacesContext(), parent);
        }
    }
    
    private boolean getTestValue(FaceletContext ctx, FaceletCompositionContext fcc, UIComponent parent, String uniqueId)
    {
        Boolean b = (Boolean) ComponentSupport.restoreInitialTagState(ctx, fcc, parent, uniqueId);
        if (b != null)
        {
            return b.booleanValue();
        }
        else
        {
            return this.test.getBoolean(ctx);
        }
    }
}
