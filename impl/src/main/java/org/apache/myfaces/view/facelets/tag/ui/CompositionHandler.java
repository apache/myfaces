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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.FaceletCompositionContext;
import org.apache.myfaces.view.facelets.TemplateClient;
import org.apache.myfaces.view.facelets.tag.TagHandlerUtils;

/**
 *
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
@JSFFaceletTag(name="ui:composition")
public final class CompositionHandler extends TagHandler implements TemplateClient
{
    private static final Logger log = Logger.getLogger(CompositionHandler.class.getName());

    public final static String NAME = "composition";

    /**
     * The resolvable URI of the template to use. The content within the composition tag will 
     * be used in populating the template specified.
     */
    @JSFFaceletAttribute(
            name="template",
            className="jakarta.el.ValueExpression",
            deferredValueType="java.lang.String")
    protected final TagAttribute _template;

    protected final Map<String, DefineHandler> _handlers;

    protected final ParamHandler[] _params;

    public CompositionHandler(TagConfig config)
    {
        super(config);
        _template = getAttribute("template");
        if (_template != null)
        {
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

            ArrayList<ParamHandler> params = TagHandlerUtils.findNextByType(nextHandler, ParamHandler.class);
            if (params.isEmpty())
            {
                _params = null;
            }
            else
            {
                _params = params.toArray(new ParamHandler[params.size()]);
            }
        }
        else
        {
            _params = null;
            _handlers = null;
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
        if (_template != null)
        {
            AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
            FaceletCompositionContext fcc = FaceletCompositionContext.getCurrentInstance(ctx);
            actx.extendClient(this);
            if (_params != null)
            {
                String uniqueId = fcc.generateUniqueComponentId();
                for (int i = 0; i < _params.length; i++)
                {
                    _params[i].apply(ctx, parent, _params[i].getName(ctx), _params[i].getValue(ctx), uniqueId);
                }
            }

            try
            {
                ctx.includeFacelet(parent, _template.getValue(ctx));
            }
            finally
            {
                actx.popExtendedClient(this);
            }
        }
        else
        {
            this.nextHandler.apply(ctx, parent);
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
