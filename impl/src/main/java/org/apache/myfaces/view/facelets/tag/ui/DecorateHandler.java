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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELException;
import javax.el.VariableMapper;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.TemplateClient;
import org.apache.myfaces.view.facelets.el.VariableMapperWrapper;
import org.apache.myfaces.view.facelets.tag.TagHandlerUtils;

/**
 * TODO: REFACTOR - This class could easily use a common parent with CompositionHandler
 * 
 * @author Jacob Hookom
 * @version $Id: DecorateHandler.java,v 1.16 2008/07/13 19:01:41 rlubke Exp $
 */
public final class DecorateHandler extends TagHandler implements TemplateClient
{

    private static final Logger log = Logger.getLogger("facelets.tag.ui.decorate");

    private final TagAttribute _template;

    private final Map<String, DefineHandler> _handlers;

    private final ParamHandler[] _params;

    /**
     * @param config
     */
    public DecorateHandler(TagConfig config)
    {
        super(config);
        _template = getRequiredAttribute("template");
        _handlers = new HashMap<String, DefineHandler>();

        for (DefineHandler handler : TagHandlerUtils.findNextByType(nextHandler, DefineHandler.class))
        {
            _handlers.put(handler.getName(), handler);
            if (log.isLoggable(Level.FINE))
            {
                log.fine(tag + " found Define[" + handler.getName() + "]");
            }
        }

        Collection<ParamHandler> params = TagHandlerUtils.findNextByType(nextHandler, ParamHandler.class);
        if (!params.isEmpty())
        {
            int i = 0;
            _params = new ParamHandler[params.size()];
            for (ParamHandler handler : params)
            {
                _params[i++] = handler;
            }
        }
        else
        {
            _params = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.view.facelets.FaceletHandler#apply(javax.faces.view.facelets.FaceletContext,
     * javax.faces.component.UIComponent)
     */
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        VariableMapper orig = ctx.getVariableMapper();
        if (_params != null)
        {
            VariableMapper vm = new VariableMapperWrapper(orig);
            ctx.setVariableMapper(vm);
            for (int i = 0; i < _params.length; i++)
            {
                _params[i].apply(ctx, parent);
            }
        }

        AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
        actx.pushClient(this);
        try
        {
            ctx.includeFacelet(parent, _template.getValue(ctx));
        }
        finally
        {
            ctx.setVariableMapper(orig);
            actx.popClient(this);
        }
    }

    public boolean apply(FaceletContext ctx, UIComponent parent, String name) throws IOException, FacesException,
            FaceletException, ELException
    {
        if (name != null)
        {
            DefineHandler handler = _handlers.get(name);
            if (handler != null)
            {
                handler.applyDefinition(ctx, parent);
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            this.nextHandler.apply(ctx, parent);
            return true;
        }
    }
}
