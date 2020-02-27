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
package org.apache.myfaces.view.facelets.tag.jsf.core;

import java.io.IOException;
import java.util.Arrays;

import javax.el.ELException;
import javax.el.MethodExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.shared.util.StringUtils;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;

/**
 * Container for all JavaServer Faces core and custom component actions used on a page. 
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
@JSFFaceletTag(
        name = "f:view",
        bodyContent = "empty", 
        componentClass="jakarta.faces.component.UIViewRoot")
public final class ViewHandler extends TagHandler
{

    private final static Class<?>[] LISTENER_SIG = new Class<?>[] { PhaseEvent.class };

    @JSFFaceletAttribute
    private final TagAttribute locale;

    @JSFFaceletAttribute
    private final TagAttribute renderKitId;

    @JSFFaceletAttribute
    private final TagAttribute contentType;

    @JSFFaceletAttribute
    private final TagAttribute encoding;

    @JSFFaceletAttribute
    private final TagAttribute beforePhase;

    @JSFFaceletAttribute
    private final TagAttribute afterPhase;
    
    @JSFFaceletAttribute(name="transient")
    private final TagAttribute transientAttribute;
    
    private final TagAttribute contracts;
    
    private final TagAttribute oamEnableViewPool;

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
        this.beforePhase = this.getAttribute("beforePhase");
        this.afterPhase = this.getAttribute("afterPhase");
        this.transientAttribute = this.getAttribute("transient");
        this.contracts = this.getAttribute("contracts");
        this.oamEnableViewPool = this.getAttribute("oamEnableViewPool");
    }

    /**
     * See taglib documentation.
     * 
     * @see jakarta.faces.view.facelets.FaceletHandler#apply(FaceletContext, UIComponent)
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
            String encodingValue = null;
            if (this.contentType != null)
            {
                // This value is read as rfc2616 section 3.7 Media Types.
                // We should check and extract the param "charset" and assing
                // it as encoding for this page.
                String v = this.contentType.getValue(ctx);
                if (v != null)
                {
                    int j = v.indexOf(';');
                    if (j >= 0)
                    {
                        int i = v.indexOf("charset",j);
                        if (i >= 0)
                        {
                            i = v.indexOf('=',i)+1;
                            if (v.length() > i)
                            {
                                encodingValue = v.substring(i);
                            }
                            // Substract charset from encoding, it will be added 
                            // later on FaceletViewDeclarationLanguage.createResponseWriter
                            // by calling response.setContentType
                            v = v.substring(0 , j);
                        }
                    }
                }
                ctx.getFacesContext().getAttributes().put("facelets.ContentType", v);
            }
            if (this.encoding != null)
            {
                String v = this.encoding.getValue(ctx);
                ctx.getFacesContext().getAttributes().put("facelets.Encoding", v);
            }
            else if (encodingValue != null)
            {
                ctx.getFacesContext().getAttributes().put("facelets.Encoding", encodingValue);
            }
            if (this.beforePhase != null)
            {
                MethodExpression m = this.beforePhase.getMethodExpression(ctx, null, LISTENER_SIG);
                root.setBeforePhaseListener(m);
            }
            if (this.afterPhase != null)
            {
                MethodExpression m = this.afterPhase.getMethodExpression(ctx, null, LISTENER_SIG);
                root.setAfterPhaseListener(m);
            }
            if (this.transientAttribute != null)
            {
                root.setTransient(this.transientAttribute.getBoolean(ctx));
            }
            if (this.contracts != null)
            {
                String contractsValue = this.contracts.getValue(ctx);
                if (contractsValue != null)
                {
                    String[] values = StringUtils.trim(StringUtils.splitShortString(contractsValue, ','));
                    if (values != null)
                    {
                        ctx.getFacesContext().setResourceLibraryContracts(Arrays.asList(values));
                    }
                }
            }
            if (this.oamEnableViewPool != null)
            {
                root.getAttributes().put("oamEnableViewPool", this.oamEnableViewPool.getBoolean(ctx));
            }
        }
        this.nextHandler.apply(ctx, parent);
    }

}
