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
import javax.faces.validator.Validator;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.MetaRuleset;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandlerDelegate;
import javax.faces.view.facelets.ValidatorHandler;

import org.apache.myfaces.view.facelets.tag.MetaRulesetImpl;

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
public class ValidatorTagHandlerDelegate extends TagHandlerDelegate
{
    private ValidatorHandler _delegate;
    
    private final TagAttribute _binding;
    
    private String _validatorId;
    
    public ValidatorTagHandlerDelegate(ValidatorHandler delegate)
    {
        _delegate = delegate;

        //TODO: Is this the way?
        if (_delegate.getValidatorConfig().getValidatorId() != null)
        {
            this._binding = null;
            this._validatorId = _delegate.getValidatorConfig().getValidatorId();
        }
        else
        {
            this._binding = delegate.getTagAttribute("binding");
            this._validatorId = null;
        }
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException
    {

        if (parent == null || !(parent instanceof EditableValueHolder))
        {
            throw new TagException(_delegate.getTag(), "Parent not an instance of EditableValueHolder: " + parent);
        }

        // only process if it's been created
        if (parent.getParent() == null)
        {
            // cast to a ValueHolder
            EditableValueHolder evh = (EditableValueHolder) parent;
            ValueExpression ve = null;
            Validator v = null;
            if (this._binding != null)
            {
                ve = this._binding.getValueExpression(ctx, Validator.class);
                v = (Validator) ve.getValue(ctx);
            }
            if (v == null)
            {
                v = this.createValidator(ctx);
                if (ve != null)
                {
                    ve.setValue(ctx, v);
                }
            }
            if (v == null)
            {
                throw new TagException(_delegate.getTag(), "No Validator was created");
            }
            _delegate.setAttributes(ctx, v);
            evh.addValidator(v);
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
        if (this._validatorId == null)
        {
            throw new TagException(
                                   _delegate.getTag(),
                                   "Default behavior invoked of requiring a validator-id passed in the constructor, must override ValidateHandler(ValidatorConfig)");
        }
        return ctx.getFacesContext().getApplication().createValidator(this._validatorId);
    }

    @Override
    public MetaRuleset createMetaRuleset(Class<?> type)
    {
        return new MetaRulesetImpl(_delegate.getTag(), type).ignore("binding");
    }

}
