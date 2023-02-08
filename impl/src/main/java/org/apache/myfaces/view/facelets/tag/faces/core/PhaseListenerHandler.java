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
package org.apache.myfaces.view.facelets.tag.faces.core;

import java.io.IOException;
import java.io.Serializable;

import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;
import jakarta.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.view.facelets.tag.faces.ComponentSupport;

@JSFFaceletTag(name = "f:phaseListener", bodyContent = "empty")
public class PhaseListenerHandler extends TagHandler
{

    private final static class LazyPhaseListener implements PhaseListener, Serializable
    {
        private static final long serialVersionUID = -6496143057319213401L;

        private final String type;
        private final ValueExpression binding;

        public LazyPhaseListener(String type, ValueExpression binding)
        {
            this.type = type;
            this.binding = binding;
        }

        private PhaseListener getInstance()
        {
            PhaseListener instance = null;
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext == null)
            {
                return null;
            }
            
            if (this.binding != null)
            {
                instance = binding.getValue(facesContext.getELContext());
            }
            
            if (instance == null && type != null)
            {
                try
                {
                    instance = (PhaseListener) ClassUtils.forName(this.type).newInstance();
                }
                catch (Exception e)
                {
                    throw new AbortProcessingException("Couldn't Lazily instantiate PhaseListener", e);
                }
                if (this.binding != null)
                {
                    binding.setValue(facesContext.getELContext(), instance);
                }
            }
            return instance;
        }

        @Override
        public void afterPhase(PhaseEvent event)
        {
            PhaseListener pl = this.getInstance();
            if (pl != null)
            {
                pl.afterPhase(event);
            }
        }

        @Override
        public void beforePhase(PhaseEvent event)
        {
            PhaseListener pl = this.getInstance();
            if (pl != null)
            {
                pl.beforePhase(event);
            }
        }

        @Override
        public PhaseId getPhaseId()
        {
            PhaseListener pl = this.getInstance();
            return (pl != null) ? pl.getPhaseId() : PhaseId.ANY_PHASE;
        }

    }

    private final TagAttribute binding;
    private final String listenerType;

    public PhaseListenerHandler(TagConfig config)
    {
        super(config);
        TagAttribute type = this.getAttribute("type");
        this.binding = this.getAttribute("binding");
        if (type != null)
        {
            if (!type.isLiteral())
            {
                throw new TagAttributeException(type, "Must be a literal class name of type PhaseListener");
            }
            else
            {
                // test it out
                try
                {
                    ClassUtils.forName(type.getValue());
                }
                catch (ClassNotFoundException e)
                {
                    throw new TagAttributeException(type, "Couldn't qualify PhaseListener", e);
                }
            }
            this.listenerType = type.getValue();
        }
        else
        {
            this.listenerType = null;
        }
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        if (ComponentHandler.isNew(parent))
        {
            UIViewRoot root = ComponentSupport.getViewRoot(ctx, parent);
            if (root == null)
            {
                throw new TagException(this.tag, "UIViewRoot not available");
            }
            ValueExpression b = null;
            if (this.binding != null)
            {
                b = this.binding.getValueExpression(ctx, PhaseListener.class);
            }

            PhaseListener pl = new LazyPhaseListener(this.listenerType, b);

            root.addPhaseListener(pl);
        }
    }
}
