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
package com.sun.facelets.tag.jsf.core;

import java.io.IOException;
import java.io.Serializable;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.ActionSource;
import javax.faces.component.ActionSource2;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import javax.faces.webapp.pdl.facelets.FaceletContext;
import javax.faces.webapp.pdl.facelets.FaceletException;
import com.sun.facelets.el.LegacyValueBinding;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagException;
import com.sun.facelets.tag.TagHandler;
import com.sun.facelets.tag.jsf.ComponentSupport;
import com.sun.facelets.util.FacesAPI;

public class SetPropertyActionListenerHandler extends TagHandler
{

    private final TagAttribute value;

    private final TagAttribute target;

    public SetPropertyActionListenerHandler(TagConfig config)
    {
        super(config);
        this.value = this.getRequiredAttribute("value");
        this.target = this.getRequiredAttribute("target");
    }

    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        if (parent instanceof ActionSource)
        {
            ActionSource src = (ActionSource) parent;
            if (ComponentSupport.isNew(parent))
            {
                ValueExpression valueExpr = this.value.getValueExpression(ctx, Object.class);
                ValueExpression targetExpr = this.target.getValueExpression(ctx, Object.class);

                ActionListener listener;

                if (FacesAPI.getVersion() >= 12 && src instanceof ActionSource2)
                {
                    listener = new SetPropertyListener(valueExpr, targetExpr);
                }
                else
                {
                    listener = new LegacySetPropertyListener(new LegacyValueBinding(valueExpr),
                                                             new LegacyValueBinding(targetExpr));
                }

                src.addActionListener(listener);
            }
        }
        else
        {
            throw new TagException(this.tag, "Parent is not of type ActionSource, type is: " + parent);
        }
    }

    private static class LegacySetPropertyListener implements ActionListener, Serializable
    {

        private ValueBinding value;

        private ValueBinding target;

        public LegacySetPropertyListener()
        {
        };

        public LegacySetPropertyListener(ValueBinding value, ValueBinding target)
        {
            this.value = value;
            this.target = target;
        }

        public void processAction(ActionEvent evt) throws AbortProcessingException
        {
            FacesContext faces = FacesContext.getCurrentInstance();
            Object valueObj = this.value.getValue(faces);
            this.target.setValue(faces, valueObj);
        }

    }

    private static class SetPropertyListener implements ActionListener, Serializable
    {

        private ValueExpression value;

        private ValueExpression target;

        public SetPropertyListener()
        {
        };

        public SetPropertyListener(ValueExpression value, ValueExpression target)
        {
            this.value = value;
            this.target = target;
        }

        public void processAction(ActionEvent evt) throws AbortProcessingException
        {
            FacesContext faces = FacesContext.getCurrentInstance();
            ELContext el = faces.getELContext();
            Object valueObj = this.value.getValue(el);
            this.target.setValue(el, valueObj);
        }

    }

}
