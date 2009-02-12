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

import javax.el.ELException;
import javax.el.MethodExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.event.PhaseEvent;
import javax.faces.webapp.pdl.facelets.FaceletContext;
import javax.faces.webapp.pdl.facelets.FaceletException;

import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagHandler;
import com.sun.facelets.tag.jsf.ComponentSupport;

/**
 * Container for all JavaServer Faces core and custom component actions used on a page. <p/> See <a target="_new"
 * href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/view.html">tag documentation</a>.
 * 
 * @author Jacob Hookom
 * @version $Id: ViewHandler.java,v 1.5 2008/07/13 19:01:44 rlubke Exp $
 */
public final class ViewHandler extends TagHandler
{

    private final static Class[] LISTENER_SIG = new Class[] { PhaseEvent.class };

    private final TagAttribute locale;

    private final TagAttribute renderKitId;

    private final TagAttribute contentType;

    private final TagAttribute encoding;

    private final TagAttribute beforePhaseListener;

    private final TagAttribute afterPhaseListener;

    /**
     * @param config
     */
    public ViewHandler(TagConfig config)
    {
        super(config);
        this.locale = this.getAttribute("locale");
        this.renderKitId = this.getAttribute("renderKitId");
        this.contentType = this.getAttribute("contentType");
        this.encoding = this.getAttribute("encoding");
        this.beforePhaseListener = this.getAttribute("beforePhaseListener");
        this.afterPhaseListener = this.getAttribute("afterPhaseListener");
    }

    /**
     * See taglib documentation.
     * 
     * @see javax.faces.webapp.pdl.facelets.FaceletHandler#apply(javax.faces.webapp.pdl.facelets.FaceletContext, javax.faces.component.UIComponent)
     */
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        UIViewRoot root = ComponentSupport.getViewRoot(ctx, parent);
        if (root != null)
        {
            if (this.locale != null)
            {
                root.setLocale(ComponentSupport.getLocale(ctx, this.locale));
            }
            if (this.renderKitId != null)
            {
                String v = this.renderKitId.getValue(ctx);
                root.setRenderKitId(v);
            }
            if (this.contentType != null)
            {
                String v = this.contentType.getValue(ctx);
                ctx.getFacesContext().getExternalContext().getRequestMap().put("facelets.ContentType", v);
            }
            if (this.encoding != null)
            {
                String v = this.encoding.getValue(ctx);
                ctx.getFacesContext().getExternalContext().getRequestMap().put("facelets.Encoding", v);
            }
            if (this.beforePhaseListener != null)
            {
                MethodExpression m = this.beforePhaseListener.getMethodExpression(ctx, null, LISTENER_SIG);
                root.setBeforePhaseListener(m);
            }
            if (this.afterPhaseListener != null)
            {
                MethodExpression m = this.afterPhaseListener.getMethodExpression(ctx, null, LISTENER_SIG);
                root.setAfterPhaseListener(m);
            }
        }
        this.nextHandler.apply(ctx, parent);
    }

}
