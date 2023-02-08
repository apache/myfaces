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
import jakarta.el.VariableMapper;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;
import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.TemplateClient;
import org.apache.myfaces.view.facelets.el.VariableMapperWrapper;
import org.apache.myfaces.view.facelets.tag.TagHandlerUtils;

/**
 * NOTE: This implementation is provided for compatibility reasons and
 * it is considered faulty. It is enabled using
 * org.apache.myfaces.STRICT_JSF_2_FACELETS_COMPATIBILITY web config param.
 * Don't use it if EL expression caching is enabled.
 * 
 * @author Jacob Hookom
 * @version $Id: CompositionHandler.java,v 1.14 2008/07/13 19:01:42 rlubke Exp $
 */
public final class LegacyCompositionHandler extends TagHandler implements TemplateClient
{
    private static final Logger log = Logger.getLogger(CompositionHandler.class.getName());

    public final static String NAME = "composition";

    /**
     * The resolvable URI of the template to use. The content within the composition tag will 
     * be used in populating the template specified.
     */
    protected final TagAttribute _template;

    protected final Map<String, DefineHandler> _handlers;
    protected final LegacyParamHandler[] _params;

    public LegacyCompositionHandler(TagConfig config)
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

            ArrayList<LegacyParamHandler> params = TagHandlerUtils.findNextByType(nextHandler,
                    LegacyParamHandler.class);
            if (params.isEmpty())
            {
                _params = null;
            }
            else
            {
                _params = params.toArray(new LegacyParamHandler[params.size()]);
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
            VariableMapper orig = ctx.getVariableMapper();
            AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
            actx.extendClient(this);
            if (_params != null)
            {
                VariableMapper vm = new VariableMapperWrapper(orig);
                ctx.setVariableMapper(vm);
                for (int i = 0; i < _params.length; i++)
                {
                    _params[i].apply(ctx, parent);
                }
            }

            try
            {
                ctx.includeFacelet(parent, _template.getValue(ctx));
            }
            finally
            {
                actx.popExtendedClient(this);
                ctx.setVariableMapper(orig);
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
