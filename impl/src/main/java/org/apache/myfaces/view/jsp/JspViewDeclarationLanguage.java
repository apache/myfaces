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
package org.apache.myfaces.view.jsp;

import java.beans.BeanInfo;
import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.application.Resource;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewMetadata;
import javax.servlet.http.HttpServletResponse;

import org.apache.myfaces.application.ViewHandlerSupport;
import org.apache.myfaces.view.ViewDeclarationLanguageBase;

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-03-22 13:55:12 -0400 (mer., 17 sept. 2008) $
 *
 * @since 2.0
 */
public class JspViewDeclarationLanguage extends ViewDeclarationLanguageBase
{
    private ViewHandlerSupport _cachedViewHandlerSupport;
    
    /**
     * 
     */
    public JspViewDeclarationLanguage()
    {
        // TODO: IMPLEMENT HERE
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildView(FacesContext context, UIViewRoot view) throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BeanInfo getComponentMetadata(FacesContext context, Resource componentResource)
    {
        // TODO: IMPLEMENT HERE
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getScriptComponentResource(FacesContext context, Resource componentResource)
    {
        // TODO: IMPLEMENT HERE
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewMetadata getViewMetadata(FacesContext context, String viewId)
    {
        // Not necessary given that this method always returns null, but staying true to
        // the spec.
        
        if (context == null)
        {
            throw new NullPointerException ("context must not be null");
        }
        
        if (viewId == null)
        {
            throw new NullPointerException ("viewId must not be null");
        }
        
        // JSP impl must return null.
        
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void renderView(FacesContext context, UIViewRoot view) throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId)
    {
        // TODO: IMPLEMENT HERE
        return null;
    }

    @Override
    protected String calculateViewId(FacesContext context, String viewId)
    {
        if (_cachedViewHandlerSupport == null)
        {
            _cachedViewHandlerSupport = null; // TODO: IMPLEMENT HERE
        }
        
        return _cachedViewHandlerSupport.calculateViewId(context, viewId);
    }


    @Override
    protected void sendSourceNotFound(FacesContext context, String message)
    {
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        try
        {
            context.responseComplete();
            response.sendError(HttpServletResponse.SC_NOT_FOUND, message);
        }
        catch (IOException ioe)
        {
            throw new FacesException(ioe);
        }
    }
}
