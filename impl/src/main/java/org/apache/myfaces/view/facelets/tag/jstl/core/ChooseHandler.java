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
import java.util.ArrayList;
import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.UIComponent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;
import jakarta.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.FaceletCompositionContext;
import org.apache.myfaces.view.facelets.tag.ComponentContainerHandler;
import org.apache.myfaces.view.facelets.tag.TagHandlerUtils;
import org.apache.myfaces.view.facelets.tag.faces.ComponentSupport;

/**
 * Simple conditional tag that establishes a context for
 * mutually exclusive conditional operations, marked by
 * &lt;when&gt; and &lt;otherwise&gt;
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
@JSFFaceletTag(name = "c:choose")
public final class ChooseHandler extends TagHandler implements ComponentContainerHandler
{

    private final ChooseOtherwiseHandler otherwise;
    private final ChooseWhenHandler[] when;

    public ChooseHandler(TagConfig config)
    {
        super(config);

        ArrayList<ChooseWhenHandler> whenList =
                TagHandlerUtils.findNextByType(nextHandler, ChooseWhenHandler.class);
        if (whenList.isEmpty())
        {
            throw new TagException(this.tag, "Choose Tag must have one or more When Tags");
        }
        this.when = whenList.toArray(new ChooseWhenHandler[whenList.size()]);

        ArrayList<ChooseOtherwiseHandler> otherwiseList = 
            TagHandlerUtils.findNextByType(nextHandler, ChooseOtherwiseHandler.class);
        this.otherwise = otherwiseList.isEmpty() ? null : otherwiseList.get(0); 
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        FaceletCompositionContext fcc = FaceletCompositionContext.getCurrentInstance(ctx);
        boolean processed = false;
        //assign an unique id for this section
        AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
        String uniqueId = actx.generateUniqueFaceletTagId(fcc.startComponentUniqueIdSection(), tagId);
        Integer savedOption = null;
        try
        {
            Integer restoredSavedOption = getSavedOption(ctx, fcc, parent, uniqueId);

            if (restoredSavedOption != null)
            {
                if (!PhaseId.RESTORE_VIEW.equals(ctx.getFacesContext().getCurrentPhaseId()))
                {
                    for (int i = 0; i < this.when.length; i++)
                    {
                        //Ensure each option has its unique section
                        fcc.startComponentUniqueIdSection();
                        try
                        {
                            if (!processed)
                            {
                                if (this.when[i].isTestTrue(ctx))
                                {
                                    boolean markInitialState = !restoredSavedOption.equals(i);
                                    boolean oldMarkInitialState = false;
                                    Boolean isBuildingInitialState = null;
                                    try
                                    {
                                        if (markInitialState)
                                        {
                                            //set markInitialState flag
                                            oldMarkInitialState = fcc.isMarkInitialState();
                                            fcc.setMarkInitialState(true);
                                            isBuildingInitialState = (Boolean) ctx.getFacesContext().
                                                    getAttributes().put(
                                                    StateManager.IS_BUILDING_INITIAL_STATE, Boolean.TRUE);
                                        }
                                        this.when[i].apply(ctx, parent);
                                    }
                                    finally
                                    {
                                        if (markInitialState)
                                        {
                                            //unset markInitialState flag
                                            if (isBuildingInitialState == null)
                                            {
                                                ctx.getFacesContext().getAttributes().remove(
                                                        StateManager.IS_BUILDING_INITIAL_STATE);
                                            }
                                            else
                                            {
                                                ctx.getFacesContext().getAttributes().put(
                                                        StateManager.IS_BUILDING_INITIAL_STATE, isBuildingInitialState);
                                            }
                                            fcc.setMarkInitialState(oldMarkInitialState);
                                        }
                                    }
                                    processed = true;
                                    savedOption = i;
                                }
                            }
                        }
                        finally
                        {
                            fcc.endComponentUniqueIdSection();
                        }
                    }

                }
                else
                {
                    for (int i = 0; i < this.when.length; i++)
                    {
                        //Ensure each option has its unique section
                        fcc.startComponentUniqueIdSection();
                        try
                        {
                            if (!processed)
                            {
                                if (restoredSavedOption.equals(i))
                                {
                                    this.when[i].apply(ctx, parent);
                                    processed = true;
                                    savedOption = i;
                                }
                            }
                        }
                        finally
                        {
                            fcc.endComponentUniqueIdSection();
                        }
                    }
                }
            }
            else
            {
                for (int i = 0; i < this.when.length; i++)
                {
                    //Ensure each option has its unique section
                    fcc.startComponentUniqueIdSection();
                    try
                    {
                        if (!processed)
                        {
                            if (this.when[i].isTestTrue(ctx))
                            {
                                this.when[i].apply(ctx, parent);
                                processed = true;
                                savedOption = i;
                            }
                        }
                    }
                    finally
                    {
                        fcc.endComponentUniqueIdSection();
                    }
                }
            }
            if (this.otherwise != null)
            {
                fcc.startComponentUniqueIdSection();
                try
                {
                    if (!processed)
                    {
                        this.otherwise.apply(ctx, parent);
                        savedOption = -1;
                    }
                }
                finally
                {
                    fcc.endComponentUniqueIdSection();
                }
            }
        }
        finally
        {
            fcc.endComponentUniqueIdSection();
        }

        ComponentSupport.saveInitialTagState(ctx, fcc, parent, uniqueId, savedOption);
        if (fcc.isUsingPSSOnThisView() && fcc.isRefreshTransientBuildOnPSS() && !fcc.isRefreshingTransientBuild())
        {
            //Mark the parent component to be saved and restored fully.
            ComponentSupport.markComponentToRestoreFully(ctx.getFacesContext(), parent);
        }
        if (fcc.isDynamicComponentSection())
        {
            ComponentSupport.markComponentToRefreshDynamically(ctx.getFacesContext(), parent);
        }
    }
    
    private Integer getSavedOption(FaceletContext ctx, FaceletCompositionContext fcc,
                                   UIComponent parent, String uniqueId)
    {
        return (Integer) ComponentSupport.restoreInitialTagState(ctx, fcc, parent, uniqueId);
    }
}
