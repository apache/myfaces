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
import org.apache.myfaces.view.facelets.TemplateClient;
import org.apache.myfaces.view.facelets.el.VariableMapperWrapper;
import org.apache.myfaces.view.facelets.tag.ui.DefineHandler;

/**
 * A Tag that is specified in a FaceletFile. Takes all attributes specified and sets them on the FaceletContext before
 * including the targeted Facelet file.
 * 
 * @author Jacob Hookom
 * @version $Id: UserTagHandler.java,v 1.12 2008/07/13 19:01:35 rlubke Exp $
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
        
        Collection<DefineHandler> handlers = TagHandlerUtils.findNextByType(nextHandler, DefineHandler.class);
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
            }
        }
    }

    /**
     * Iterate over all TagAttributes and set them on the FaceletContext's VariableMapper, then include the target
     * Facelet. Finally, replace the old VariableMapper.
     * 
     * @see TagAttribute#getValueExpression(FaceletContext, Class)
     * @see VariableMapper
     * @see jakarta.faces.view.facelets.FaceletHandler#apply(jakarta.faces.view.facelets.FaceletContext, jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        VariableMapper orig = ctx.getVariableMapper();

        AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
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

            actx.pushClient(this);
            // setup a variable map
            if (this._vars.length > 0)
            {
                VariableMapper varMapper = new VariableMapperWrapper(orig);
                for (int i = 0; i < this._vars.length; i++)
                {
                    varMapper.setVariable(names[i], values[i]);
                }
                ctx.setVariableMapper(varMapper);
            }
            actx.getTemplateContext().setAllowCacheELExpressions(false);

            ctx.includeFacelet(parent, this._location);
        }
        catch (FileNotFoundException e)
        {
            throw new TagException(this.tag, e.getMessage());
        }
        finally
        {
            // make sure we undo our changes
            actx.popClient(this);
            ctx.setVariableMapper(orig);
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
