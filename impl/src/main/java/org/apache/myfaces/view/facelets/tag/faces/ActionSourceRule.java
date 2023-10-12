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
package org.apache.myfaces.view.facelets.tag.faces;

import jakarta.el.MethodExpression;
import jakarta.faces.component.ActionSource;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.MethodExpressionActionListener;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRule;
import jakarta.faces.view.facelets.Metadata;
import jakarta.faces.view.facelets.MetadataTarget;
import jakarta.faces.view.facelets.TagAttribute;

import org.apache.myfaces.view.facelets.FaceletCompositionContext;

/**
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public final class ActionSourceRule extends MetaRule
{
    public final static Class<?>[] ACTION_SIG = new Class[0];

    public final static Class<?>[] ACTION_LISTENER_SIG = new Class<?>[] { ActionEvent.class };

    final static class ActionMapper extends Metadata
    {
        private final TagAttribute _attr;

        public ActionMapper(TagAttribute attr)
        {
            this._attr = attr;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            MethodExpression expr = _attr.getMethodExpression(ctx, null, ActionSourceRule.ACTION_SIG);
            ((ActionSource) instance).setActionExpression(expr);
        }
    }

    final static class ActionListenerMapper extends Metadata
    {
        private final TagAttribute _attr;

        public ActionListenerMapper(TagAttribute attr)
        {
            _attr = attr;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            // From Faces 2.0 it is possible to have actionListener method without ActionEvent parameter. 
            // It seems that MethodExpressionActionListener from API contains support for it but there is one big
            // problem - one-arg constructor will not preserve the current VariableMapper.
            // This is a problem when using facelets and <ui:decorate/> with EL params (see MYFACES-2541 for details).
            // So we must create two MethodExpressions here - both are created from the current 
            // facelets context and thus varibale mapping will work.
            final MethodExpression methodExpressionOneArg
                    = _attr.getMethodExpression(ctx, null, ActionSourceRule.ACTION_LISTENER_SIG);
            final MethodExpression methodExpressionZeroArg
                    = _attr.getMethodExpression(ctx, null, ActionSourceRule.ACTION_SIG);
            if (FaceletCompositionContext.getCurrentInstance(ctx).isUsingPSSOnThisView())
            {
                ((ActionSource) instance).addActionListener(
                        new PartialMethodExpressionActionListener(methodExpressionOneArg, methodExpressionZeroArg));
            }
            else
            {
                ((ActionSource) instance).addActionListener(
                        new MethodExpressionActionListener(methodExpressionOneArg, methodExpressionZeroArg));
            }
        }
    }

    public final static ActionSourceRule INSTANCE = new ActionSourceRule();

    public ActionSourceRule()
    {
        super();
    }

    @Override
    public Metadata applyRule(String name, TagAttribute attribute, MetadataTarget meta)
    {
        if (meta.isTargetInstanceOf(ActionSource.class))
        {
            if ("action".equals(name))
            {
                return new ActionMapper(attribute);
            }

            if ("actionListener".equals(name))
            {
                return new ActionListenerMapper(attribute);
            }
        }
        
        return null;
    }
}
