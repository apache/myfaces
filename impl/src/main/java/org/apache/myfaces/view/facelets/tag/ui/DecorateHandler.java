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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.UIComponent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.util.lang.StringUtils;
import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.FaceletCompositionContext;
import org.apache.myfaces.view.facelets.TemplateClient;
import org.apache.myfaces.view.facelets.tag.ComponentContainerHandler;
import org.apache.myfaces.view.facelets.tag.TagHandlerUtils;
import org.apache.myfaces.view.facelets.tag.faces.ComponentSupport;

/**
 * The decorate tag acts the same as a composition tag, but it will not trim 
 * everything outside of it. This is useful in cases where you have a list of 
 * items in a document, which you would like to be decorated or framed.
 *  
 * The sum of it all is that you can take any element in the document and decorate 
 * it with some external logic as provided by the template.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
@JSFFaceletTag(name="ui:decorate")
public final class DecorateHandler extends TagHandler implements TemplateClient, ComponentContainerHandler
{

    private static final Logger log = Logger.getLogger(DecorateHandler.class.getName());

    /**
     * The resolvable URI of the template to use. The content within the decorate tag 
     * will be used in populating the template specified.
     */
    @JSFFaceletAttribute(
            name="template",
            className="jakarta.el.ValueExpression",
            deferredValueType="java.lang.String")
    private final TagAttribute _template;

    private final Map<String, DefineHandler> _handlers;
    private final ParamHandler[] _params;

    public DecorateHandler(TagConfig config)
    {
        super(config);
        _template = getRequiredAttribute("template");
        
        ArrayList<DefineHandler> handlers = TagHandlerUtils.findNextByType(nextHandler, DefineHandler.class);
        if (handlers.isEmpty())
        {
            _handlers = null;
        }
        else
        {
            _handlers = new HashMap<>(handlers.size());
            for (DefineHandler handler : handlers)
            {
                _handlers.put(handler.getName(), handler);
                if (log.isLoggable(Level.FINE))
                {
                    log.fine(tag + " found Define[" + handler.getName() + ']');
                }
            }
        }

        Collection<ParamHandler> params = TagHandlerUtils.findNextByType(nextHandler, ParamHandler.class);
        if (params.isEmpty())
        {
            _params = null;
        }
        else
        {
            _params = params.toArray(new ParamHandler[params.size()]);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see jakarta.faces.view.facelets.FaceletHandler#apply(jakarta.faces.view.facelets.FaceletContext,
     * jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
        actx.pushClient(this);
        FaceletCompositionContext fcc = FaceletCompositionContext.getCurrentInstance(ctx);
        String uniqueId = null;
        try
        {
            if (!_template.isLiteral())
            {
                uniqueId = actx.generateUniqueFaceletTagId(
                    fcc.startComponentUniqueIdSection(), tagId);
            }
            else if (_params != null)
            {
                uniqueId = actx.generateUniqueFaceletTagId(
                    fcc.generateUniqueComponentId(), tagId);
            }
            if (_params != null)
            {
                for (int i = 0; i < _params.length; i++)
                {
                    _params[i].apply(ctx, parent, _params[i].getName(ctx), _params[i].getValue(ctx), uniqueId);
                }
            }

            String path;
            boolean markInitialState = false;
            if (!_template.isLiteral())
            {
                String restoredPath = (String) ComponentSupport.restoreInitialTagState(ctx, fcc, parent, uniqueId);
                if (restoredPath != null)
                {
                    // If is not restore view phase, the path value should be
                    // evaluated and if is not equals, trigger markInitialState stuff.
                    if (!PhaseId.RESTORE_VIEW.equals(ctx.getFacesContext().getCurrentPhaseId()))
                    {
                        path = this._template.getValue(ctx);
                        if (StringUtils.isBlank(path))
                        {
                            return;
                        }
                        if (!path.equals(restoredPath))
                        {
                            markInitialState = true;
                        }
                    }
                    else
                    {
                        path = restoredPath;
                    }
                }
                else
                {
                    //No state restored, calculate path
                    path = this._template.getValue(ctx);
                }
                ComponentSupport.saveInitialTagState(ctx, fcc, parent, uniqueId, path);
            }
            else
            {
                path = _template.getValue(ctx);
            }
            try
            {
                boolean oldMarkInitialState = false;
                Boolean isBuildingInitialState = null;
                if (markInitialState)
                {
                    //set markInitialState flag
                    oldMarkInitialState = fcc.isMarkInitialState();
                    fcc.setMarkInitialState(true);
                    isBuildingInitialState = (Boolean) ctx.getFacesContext().getAttributes().put(
                            StateManager.IS_BUILDING_INITIAL_STATE, Boolean.TRUE);
                }
                try
                {
                    ctx.includeFacelet(parent, path);
                }
                finally
                {
                    if (markInitialState)
                    {
                        //unset markInitialState flag
                        if (isBuildingInitialState == null)
                        {
                            ctx.getFacesContext().getAttributes().remove(StateManager.IS_BUILDING_INITIAL_STATE);
                        }
                        else
                        {
                            ctx.getFacesContext().getAttributes().put(
                                    StateManager.IS_BUILDING_INITIAL_STATE, isBuildingInitialState);
                        }
                        fcc.setMarkInitialState(oldMarkInitialState);
                    }
                }
            }
            finally
            {
                actx.popClient(this);
            }
        }
        finally
        {
            if (!_template.isLiteral())
            {
                fcc.endComponentUniqueIdSection();
                
                if (fcc.isUsingPSSOnThisView() && fcc.isRefreshTransientBuildOnPSS()
                        && !fcc.isRefreshingTransientBuild())
                {
                    //Mark the parent component to be saved and restored fully.
                    ComponentSupport.markComponentToRestoreFully(ctx.getFacesContext(), parent);
                }
                if (fcc.isDynamicComponentSection())
                {
                    ComponentSupport.markComponentToRefreshDynamically(ctx.getFacesContext(), parent);
                }
            }
        }

    }

    @Override
    public boolean apply(FaceletContext ctx, UIComponent parent, String name) throws IOException, FacesException,
            FaceletException, ELException
    {
        if (name != null)
        {
            DefineHandler handler = _handlers == null ? null : _handlers.get(name);
            if (handler != null)
            {
                handler.applyDefinition(ctx, parent);
                return true;
            }

            return false;
        }
        else
        {
            this.nextHandler.apply(ctx, parent);
            return true;
        }
    }
}
