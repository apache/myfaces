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
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.el.ELException;
import javax.el.VariableMapper;
import javax.faces.FacesException;
import javax.faces.application.ProjectStage;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.el.VariableMapperWrapper;

/**
 * The include tag can point at any Facelet which might use the composition tag,
 * component tag, or simply be straight XHTML/XML. It should be noted that the 
 * src path does allow relative path names, but they will always be resolved 
 * against the original Facelet requested. 
 * 
 * The include tag can be used in conjunction with multiple &lt;ui:param/&gt; 
 * tags to pass EL expressions/values to the target page.
 * 
 * @author Jacob Hookom
 * @version $Id: IncludeHandler.java,v 1.5 2008/07/13 19:01:41 rlubke Exp $
 */
@JSFFaceletTag(name="ui:include", bodyContent="JSP")
public final class IncludeHandler extends TagHandler
{

    private static final String ERROR_PAGE_INCLUDE_PATH = "javax.faces.error.xhtml";
    private static final String ERROR_FACELET = "META-INF/rsc/myfaces-dev-error-include.xhtml";
    
    /**
     * A literal or EL expression that specifies the target Facelet that you 
     * would like to include into your document.
     */
    @JSFFaceletAttribute(
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.String",
            required=true)
    private final TagAttribute src;

    /**
     * @param config
     */
    public IncludeHandler(TagConfig config)
    {
        super(config);
        this.src = this.getRequiredAttribute("src");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.view.facelets.FaceletHandler#apply(javax.faces.view.facelets.FaceletContext, javax.faces.component.UIComponent)
     */
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        String path = this.src.getValue(ctx);
        if (path == null || path.length() == 0)
        {
            return;
        }
        VariableMapper orig = ctx.getVariableMapper();
        ctx.setVariableMapper(new VariableMapperWrapper(orig));
        try
        {
            this.nextHandler.apply(ctx, null);
            // if we are in ProjectStage Development and the path equals "javax.faces.error.xhtml"
            // we should include the default error page
            if (ctx.getFacesContext().isProjectStage(ProjectStage.Development) 
                    && ERROR_PAGE_INCLUDE_PATH.equals(path))
            {
                URL url;
                if (System.getSecurityManager()!=null)
                {
                    try
                    {
                        ClassLoader cl = AccessController.<ClassLoader>doPrivileged(new PrivilegedExceptionAction<ClassLoader>() {
                            public ClassLoader run() throws PrivilegedActionException
                            {
                                return Thread.currentThread().getContextClassLoader();
                            }
                        });
                        url = cl.getResource(ERROR_FACELET);
                    }
                    catch (PrivilegedActionException pae)
                    {
                        throw new FacesException(pae);
                    }
                }
                else
                {
                    url = Thread.currentThread().getContextClassLoader().getResource(ERROR_FACELET);
                }
                ctx.includeFacelet(parent, url);
            }
            else
            {
                ctx.includeFacelet(parent, path);
            }
        }
        finally
        {
            ctx.setVariableMapper(orig);
        }
    }
}
