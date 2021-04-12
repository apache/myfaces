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

import jakarta.el.ELException;
import jakarta.el.ValueExpression;
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
import org.apache.myfaces.view.facelets.ELExpressionCacheMode;
import org.apache.myfaces.view.facelets.el.FaceletStateValueExpression;
import org.apache.myfaces.view.facelets.tag.faces.ComponentSupport;
import org.apache.myfaces.view.facelets.tag.faces.FaceletState;

/**
 * @author Jacob Hookom
 * @version $Id$
 */
@JSFFaceletTag(name="ui:param")
public class ParamHandler extends TagHandler
{

    /**
     * The name of the variable to pass to the included Facelet.
     */
    @JSFFaceletAttribute(
            className="jakarta.el.ValueExpression",
            deferredValueType="java.lang.String",
            required=true)
    private final TagAttribute name;

    /**
     * The literal or EL expression value to assign to the named variable.
     */
    @JSFFaceletAttribute(
            className="jakarta.el.ValueExpression",
            deferredValueType="java.lang.String",
            required=true)
    private final TagAttribute value;

    /**
     * @param config
     */
    public ParamHandler(TagConfig config)
    {
        super(config);
        this.name = this.getRequiredAttribute("name");
        this.value = this.getRequiredAttribute("value");
    }

    /*
     * (non-Javadoc)
     * 
     * @see jakarta.faces.view.facelets.FaceletHandler#apply(jakarta.faces.view.facelets.FaceletContext,
     *        jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        String nameStr = getName(ctx);
        ValueExpression valueVE = getValue(ctx);
        apply(ctx, parent, nameStr, valueVE);
    }
    
    public void apply(FaceletContext ctx, UIComponent parent, String nameStr, ValueExpression valueVE)
            throws IOException, FacesException, FaceletException, ELException
    {
        AbstractFaceletContext actx = ((AbstractFaceletContext) ctx);
        actx.getTemplateContext().setParameter(nameStr, valueVE);
        
        if (actx.getTemplateContext().isAllowCacheELExpressions())
        {
            if (ELExpressionCacheMode.strict.equals(actx.getELExpressionCacheMode()) ||
                ELExpressionCacheMode.allowCset.equals(actx.getELExpressionCacheMode()))
            {
                actx.getTemplateContext().setAllowCacheELExpressions(false);
            }
        }
    }
    
    public void apply(FaceletContext ctx, UIComponent parent, String nameStr, ValueExpression valueVE, String uniqueId)
            throws IOException, FacesException, FaceletException, ELException
    {
        AbstractFaceletContext actx = ((AbstractFaceletContext) ctx);
        if (ELExpressionCacheMode.alwaysRecompile.equals(actx.getELExpressionCacheMode()))
        {
            FaceletState faceletState = ComponentSupport.getFaceletState(ctx, parent, true);
            faceletState.putBinding(uniqueId, nameStr, valueVE);
            ValueExpression ve = new FaceletStateValueExpression(uniqueId, nameStr);
            actx.getTemplateContext().setParameter(nameStr, ve);
        }
        else
        {
            actx.getTemplateContext().setParameter(nameStr, valueVE);
        }
        
        if (actx.getTemplateContext().isAllowCacheELExpressions())
        {
            if (ELExpressionCacheMode.strict.equals(actx.getELExpressionCacheMode()) ||
                ELExpressionCacheMode.allowCset.equals(actx.getELExpressionCacheMode()))
            {
                actx.getTemplateContext().setAllowCacheELExpressions(false);
            }
        }
    }
    
    public String getName(FaceletContext ctx)
    {
        return this.name.getValue(ctx);
    }
    
    public ValueExpression getValue(FaceletContext ctx)
    {
        return this.value.getValueExpression(ctx, Object.class);
    }
}
