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
package org.apache.myfaces.view.facelets.tag;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;
import jakarta.faces.view.facelets.TagHandler;

import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.ELExpressionCacheMode;
import org.apache.myfaces.view.facelets.FaceletCompositionContext;
import org.apache.myfaces.view.facelets.TemplateClient;
import org.apache.myfaces.view.facelets.TemplateContext;
import org.apache.myfaces.view.facelets.el.FaceletStateValueExpression;
import org.apache.myfaces.view.facelets.impl.TemplateContextImpl;
import org.apache.myfaces.view.facelets.tag.faces.ComponentSupport;
import org.apache.myfaces.view.facelets.tag.faces.FaceletState;
import org.apache.myfaces.view.facelets.tag.ui.DefineHandler;

/**
 * Strict Facelets compatibility handler for Facelets tag files ({@code source} taglibs). Matches
 * {@link UserTagHandler}: taglib attributes use the Facelets
 * {@linkplain org.apache.myfaces.view.facelets.TemplateContext template context}. Restores
 * {@linkplain jakarta.el.VariableMapper VariableMapper} after include so third-party overlays do not leak
 * (MYFACES-4589).
 *
 * @author Jacob Hookom
 * @version $Id$
 */
final class LegacyUserTagHandler extends TagHandler implements TemplateClient, ComponentContainerHandler
{

    protected final TagAttribute[] _vars;
    protected final URL _location;
    protected final Map<String, DefineHandler> _handlers;

    public LegacyUserTagHandler(TagConfig config, URL location)
    {
        super(config);
        this._vars = this.tag.getAttributes().getAll();
        this._location = location;
        
        Collection<DefineHandler> defines = TagHandlerUtils.findNextByType(nextHandler, DefineHandler.class);
        if (defines.isEmpty())
        {
            _handlers = null;
        }
        else
        {
            _handlers = new HashMap<>(defines.size());
            for (DefineHandler handler : defines)
            {
                _handlers.put(handler.getName(), handler);
            }
        }
    }

    /**
     * Binds taglib attributes on the current template context, then includes the target Facelet.
     *
     * @see TagAttribute#getValueExpression(FaceletContext, Class)
     * @see jakarta.faces.view.facelets.FaceletHandler#apply(jakarta.faces.view.facelets.FaceletContext,
     *        jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
        VariableMapper userTagVariableMapperOrig = ctx.getVariableMapper();
        // eval include
        try
        {
            String[] names = null;
            ValueExpression[] values = null;
            if (this._vars.length > 0)
            {
                names = new String[_vars.length];
                values = new ValueExpression[_vars.length];
                for (int i = 0; i < _vars.length; i++)
                {
                    names[i] = _vars[i].getLocalName();
                    values[i] = _vars[i].getValueExpression(ctx, Object.class);
                }
            }
            actx.pushTemplateContext(new TemplateContextImpl());
            actx.pushClient(this);

            FaceletCompositionContext fcc = FaceletCompositionContext.getCurrentInstance(ctx);
            String uniqueId = fcc.startComponentUniqueIdSection();
            try
            {
                if (this._vars.length > 0)
                {
                    if (ELExpressionCacheMode.alwaysRecompile.equals(actx.getELExpressionCacheMode()))
                    {
                        FaceletState faceletState = ComponentSupport.getFaceletState(ctx, parent, true);
                        for (int i = 0; i < this._vars.length; i++)
                        {
                            faceletState.putBinding(uniqueId, names[i], values[i]);
                            ValueExpression ve = new FaceletStateValueExpression(uniqueId, names[i]);
                            actx.getTemplateContext().setParameter(names[i], ve);
                        }
                    }
                    else
                    {
                        for (int i = 0; i < this._vars.length; i++)
                        {
                            ((AbstractFaceletContext) ctx).getTemplateContext().setParameter(names[i], values[i]);
                        }
                    }
                }

                // Disable caching always, even in 'always' mode
                // The only mode that can support EL caching in this condition is alwaysRedirect.
                if (!ELExpressionCacheMode.alwaysRecompile.equals(actx.getELExpressionCacheMode()))
                {
                    actx.getTemplateContext().setAllowCacheELExpressions(false);
                }
                ctx.includeFacelet(parent, this._location);
            }
            finally
            {
                fcc.endComponentUniqueIdSection();
            }
        }
        catch (FileNotFoundException e)
        {
            throw new TagException(this.tag, e.getMessage());
        }
        finally
        {
            // make sure we undo our changes
            actx.popClient(this);
            actx.popTemplateContext();
            // Restore mapper so taglib overlays (e.g. o:tagAttribute) do not leak past this tag (MYFACES-4589).
            ctx.setVariableMapper(userTagVariableMapperOrig);
        }
    }

    @Override
    public boolean apply(FaceletContext ctx, UIComponent parent, String name) throws IOException, FacesException,
            FaceletException, ELException
    {
        if (name != null)
        {
            if (this._handlers == null)
            {
                return false;
            }
            DefineHandler handler = this._handlers.get(name);
            if (handler != null)
            {
                AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
                TemplateContext itc = actx.popTemplateContext();
                try
                {
                    handler.applyDefinition(ctx, parent);
                }
                finally
                {
                    actx.pushTemplateContext(itc);
                }
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
            TemplateContext itc = actx.popTemplateContext();
            try
            {
                this.nextHandler.apply(ctx, parent);
            }
            finally
            {
                actx.pushTemplateContext(itc);
            }
            return true;
        }
    }

}
