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
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.view.EditableValueHolderAttachedObjectHandler;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.MetaRuleset;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandlerDelegate;
import javax.faces.view.facelets.ValidatorHandler;

import org.apache.myfaces.view.facelets.tag.MetaRulesetImpl;
import org.apache.myfaces.view.facelets.tag.composite.CompositeComponentResourceTagHandler;

/**
 * Handles setting a Validator instance on a EditableValueHolder. Will wire all attributes set to the Validator instance
 * created/fetched. Uses the "binding" attribute for grabbing instances to apply attributes to. <p/> Will only
 * set/create Validator is the passed UIComponent's parent is null, signifying that it wasn't restored from an existing
 * tree.
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 *
 * @since 2.0
 */
public class ValidatorTagHandlerDelegate extends TagHandlerDelegate implements EditableValueHolderAttachedObjectHandler
{
    private ValidatorHandler _delegate;
    
    public ValidatorTagHandlerDelegate(ValidatorHandler delegate)
    {
        _delegate = delegate;
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException
    {
        if (!ComponentHandler.isNew(parent))
        {
            return;
        }
        if (parent instanceof EditableValueHolder)
        {
            applyAttachedObject(ctx.getFacesContext(), parent);
        }
        else if (UIComponent.isCompositeComponent(parent))
        {
            CompositeComponentResourceTagHandler.addAttachedObjectHandler(parent, _delegate);
        }
        else
        {
            throw new TagException(_delegate.getTag(), "Parent not composite component or an instance of EditableValueHolder: " + parent);
        }
    }

    /**
     * Template method for creating a Validator instance
     * 
     * @param ctx
     *            FaceletContext to use
     * @return a new Validator instance
     */
    protected Validator createValidator(FaceletContext ctx)
    {
        if (_delegate.getValidatorId(ctx) == null)
        {
            throw new TagException(
                                   _delegate.getTag(),
                                   "Default behavior invoked of requiring a validator-id passed in the constructor, must override ValidateHandler(ValidatorConfig)");
        }
        return ctx.getFacesContext().getApplication().createValidator(_delegate.getValidatorId(ctx));
    }

    @Override
    public MetaRuleset createMetaRuleset(Class type)
    {
        return new MetaRulesetImpl(_delegate.getTag(), type).ignore("binding");
    }

    public void applyAttachedObject(FacesContext context, UIComponent parent)
    {
        // Retrieve the current FaceletContext from FacesContext object
        FaceletContext faceletContext = (FaceletContext) context.getAttributes().get(
                FaceletContext.FACELET_CONTEXT_KEY);
        
        // cast to a ValueHolder
        EditableValueHolder evh = (EditableValueHolder) parent;
        ValueExpression ve = null;
        Validator v = null;
        if (_delegate.getBinding() != null)
        {
            ve = _delegate.getBinding().getValueExpression(faceletContext, Validator.class);
            v = (Validator) ve.getValue(faceletContext);
        }
        if (v == null)
        {
            v = this.createValidator(faceletContext);
            if (ve != null)
            {
                ve.setValue(faceletContext, v);
            }
        }
        if (v == null)
        {
            throw new TagException(_delegate.getTag(), "No Validator was created");
        }
        _delegate.setAttributes(faceletContext, v);
        evh.addValidator(v);
    }

    public String getFor()
    {
        TagAttribute forAttribute = _delegate.getTagAttribute("for");
        
        if (forAttribute == null)
        {
            return null;
        }
        else
        {
            return forAttribute.getValue();
        }
    }

}
