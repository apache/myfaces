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

package org.apache.myfaces.test.mock;

import java.util.Locale;

import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.push.PushContext;
import jakarta.faces.render.RenderKitFactory;

/**
 * <p>Mock implementation of <code>ViewHandler</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */

public class MockViewHandler extends ViewHandler
{
    /**
     * <p>Construct a default instance.</p>
     */
    public MockViewHandler()
    {
    }

    @Override
    public Locale calculateLocale(FacesContext context)
    {
        Locale locale = context.getApplication().getDefaultLocale();
        if (locale == null)
        {
            locale = Locale.getDefault();
        }
        return locale;

    }

    @Override
    public String calculateRenderKitId(FacesContext context)
    {
        String renderKitId = context.getApplication().getDefaultRenderKitId();
        if (renderKitId == null)
        {
            renderKitId = RenderKitFactory.HTML_BASIC_RENDER_KIT;
        }
        return renderKitId;
    }

    @Override
    public UIViewRoot createView(FacesContext context, String viewId)
    {
        // Save locale and renderKitId from previous view (if any), per spec
        Locale locale = null;
        String renderKitId = null;
        if (context.getViewRoot() != null)
        {
            locale = context.getViewRoot().getLocale();
            renderKitId = context.getViewRoot().getRenderKitId();
        }

        // Configure a new UIViewRoot instance
        UIViewRoot view = new UIViewRoot();
        view.setViewId(viewId);
        if (locale != null)
        {
            view.setLocale(locale);
        }
        else
        {
            view.setLocale(context.getApplication().getViewHandler()
                    .calculateLocale(context));
        }
        if (renderKitId != null)
        {
            view.setRenderKitId(renderKitId);
        }
        else
        {
            view.setRenderKitId(context.getApplication().getViewHandler()
                    .calculateRenderKitId(context));
        }

        // Return the configured instance
        return view;
    }

    @Override
    public String getActionURL(FacesContext context, String viewId)
    {
        return FacesContext.getCurrentInstance().getExternalContext()
                .getRequestContextPath()
                + viewId;
    }

    @Override
    public String getResourceURL(FacesContext context, String path)
    {
        return FacesContext.getCurrentInstance().getExternalContext()
                .getRequestContextPath()
                + path;

    }

    @Override
    public void renderView(FacesContext context, UIViewRoot view)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeState(FacesContext context)
    {

    }
    
    @Override
    public String getWebsocketURL(FacesContext context, String channelAndToken)
    {
        String url = context.getExternalContext().getRequestContextPath() + 
                PushContext.URI_PREFIX + "/"+channelAndToken;
        return url;
    }
}
