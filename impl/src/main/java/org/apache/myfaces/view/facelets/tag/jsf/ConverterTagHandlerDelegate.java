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
package org.apache.myfaces.view.facelets.tag.jsf;

import java.io.IOException;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.view.AttachedObjectHandler;
import javax.faces.view.facelets.ConverterHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.MetaRuleset;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandlerDelegate;

import org.apache.myfaces.view.facelets.tag.MetaRulesetImpl;

/**
 * Handles setting a Converter instance on a ValueHolder. Will wire all attributes set to the Converter instance
 * created/fetched. Uses the "binding" attribute for grabbing instances to apply attributes to. <p/> Will only
 * set/create Converter is the passed UIComponent's parent is null, signifying that it wasn't restored from an existing
 * tree.
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 *
 * @since 2.0
 */
public class ConverterTagHandlerDelegate extends TagHandlerDelegate implements AttachedObjectHandler
{
    private ConverterHandler _delegate;
    
    private final TagAttribute _binding;

    private String _converterId;

    public ConverterTagHandlerDelegate(ConverterHandler delegate)
    {
        _delegate = delegate;

        //TODO: Is this the way?
        if (_delegate.getConverterId(null) != null)
        {
            this._binding = null;
            this._converterId = _delegate.getConverterId(null);
        }
        else
        {
            this._binding = delegate.getTagAttribute("binding");
            this._converterId = null;
        }
    }

    /**
     * Set Converter instance on parent ValueHolder if it's not being restored.
     * <ol>
     * <li>Cast to ValueHolder</li>
     * <li>If "binding" attribute was specified, fetch/create and re-bind to expression.</li>
     * <li>Otherwise, call {@link #createConverter(FaceletContext) createConverter}.</li>
     * <li>Call {@link ObjectHandler#setAttributes(FaceletContext, Object) setAttributes} on Converter instance.</li>
     * <li>Set the Converter on the ValueHolder</li>
     * <li>If the ValueHolder has a localValue, convert it and set the value</li>
     * </ol>
     * 
     * @see ValueHolder
     * @see Converter
     * @see #createConverter(FaceletContext)
     * @see javax.faces.view.facelets.FaceletHandler#apply(javax.faces.view.facelets.FaceletContext, javax.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException
    {
        if (parent == null || !(parent instanceof ValueHolder))
        {
            throw new TagException(_delegate.getTag(), "Parent not an instance of ValueHolder: " + parent);
        }

        // only process if it's been created
        if (parent.getParent() == null)
        {
            // cast to a ValueHolder
            ValueHolder vh = (ValueHolder) parent;
            ValueExpression ve = null;
            Converter c = null;
            if (this._binding != null)
            {
                ve = this._binding.getValueExpression(ctx, Converter.class);
                c = (Converter) ve.getValue(ctx);
            }
            if (c == null)
            {
                c = this.createConverter(ctx);
                if (ve != null)
                {
                    ve.setValue(ctx, c);
                }
            }
            if (c == null)
            {
                throw new TagException(_delegate.getTag(), "No Converter was created");
            }
            _delegate.setAttributes(ctx, c);
            vh.setConverter(c);
            Object lv = vh.getLocalValue();
            FacesContext faces = ctx.getFacesContext();
            if (lv instanceof String)
            {
                vh.setValue(c.getAsObject(faces, parent, (String) lv));
            }
        }        
    }

    /**
     * Create a Converter instance
     * 
     * @param ctx
     *            FaceletContext to use
     * @return Converter instance, cannot be null
     */
    protected Converter createConverter(FaceletContext ctx)
    {
        if (this._converterId == null)
        {
            throw new TagException(
                                   _delegate.getTag(),
                                   "Default behavior invoked of requiring a converter-id passed in the constructor, must override ConvertHandler(ConverterConfig)");
        }
        return ctx.getFacesContext().getApplication().createConverter(this._converterId);
    }

    @Override
    public MetaRuleset createMetaRuleset(Class<?> type)
    {
        return new MetaRulesetImpl(_delegate.getTag(), type).ignore("binding");
    }

    @Override
    public void applyAttachedObject(FacesContext context, UIComponent parent)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getFor()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
