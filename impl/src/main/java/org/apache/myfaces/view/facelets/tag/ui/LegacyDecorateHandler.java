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
import jakarta.el.VariableMapper;
import jakarta.faces.FacesException;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.UIComponent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;
import org.apache.myfaces.util.lang.StringUtils;
import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.FaceletCompositionContext;
import org.apache.myfaces.view.facelets.TemplateClient;
import org.apache.myfaces.view.facelets.el.VariableMapperWrapper;
import org.apache.myfaces.view.facelets.tag.TagHandlerUtils;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;

/**
 * The decorate tag acts the same as a composition tag, but it will not trim 
 * everything outside of it. This is useful in cases where you have a list of 
 * items in a document, which you would like to be decorated or framed.
 *  
 * The sum of it all is that you can take any element in the document and decorate 
 * it with some external logic as provided by the template.
 * 
 * NOTE: This implementation is provided for compatibility reasons and
 * it is considered faulty. It is enabled using
 * org.apache.myfaces.STRICT_JSF_2_FACELETS_COMPATIBILITY web config param.
 * Don't use it if EL expression caching is enabled.
 * 
 * @author Jacob Hookom
 * @version $Id: DecorateHandler.java,v 1.16 2008/07/13 19:01:41 rlubke Exp $
 */
public final class LegacyDecorateHandler extends TagHandler implements TemplateClient
{

    private static final Logger log = Logger.getLogger(DecorateHandler.class.getName());

    /**
     * The resolvable URI of the template to use. The content within the decorate tag 
     * will be used in populating the template specified.
     */
    private final TagAttribute _template;

    private final Map<String, DefineHandler> _handlers;

    private final LegacyParamHandler[] _params;

    public LegacyDecorateHandler(TagConfig config)
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

        Collection<LegacyParamHandler> params = TagHandlerUtils.findNextByType(nextHandler, 
                LegacyParamHandler.class);
        if (params.isEmpty())
        {
            _params = null;
        }
        else
        {
            _params = (LegacyParamHandler[]) params.toArray(new LegacyParamHandler[params.size()]);
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
        VariableMapper orig = ctx.getVariableMapper();
        AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
        actx.pushClient(this);

        if (_params != null)
        {
            VariableMapper vm = new VariableMapperWrapper(orig);
            ctx.setVariableMapper(vm);
            for (int i = 0; i < _params.length; i++)
            {
                _params[i].apply(ctx, parent);
            }
        }

        FaceletCompositionContext fcc = FaceletCompositionContext.getCurrentInstance(ctx);
        String path;
        boolean markInitialState = false;
        if (!_template.isLiteral())
        {
            String uniqueId = actx.generateUniqueFaceletTagId(fcc.startComponentUniqueIdSection(), tagId);
            //path = getTemplateValue(actx, fcc, parent, uniqueId);
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
                ctx.setVariableMapper(orig);
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
