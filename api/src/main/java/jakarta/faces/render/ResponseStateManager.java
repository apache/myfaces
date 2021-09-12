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
package jakarta.faces.render;

import jakarta.faces.context.FacesContext;
import java.io.IOException;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 */
public abstract class ResponseStateManager
{
    public static final String RENDER_KIT_ID_PARAM = "jakarta.faces.RenderKitId";
    public static final String VIEW_STATE_PARAM = "jakarta.faces.ViewState";
    
    public static final String CLIENT_WINDOW_PARAM = "jakarta.faces.ClientWindow";
    public static final String CLIENT_WINDOW_URL_PARAM = "jfwid";
    
    public static final String NON_POSTBACK_VIEW_TOKEN_PARAM = "jakarta.faces.Token";

    public void writeState(FacesContext context, Object state) throws IOException
    {
    }

    /**
     * 
     * @since 2.0
     * @param context
     * @param state
     * @return
     */
    public String getViewState(FacesContext context, Object state)
    {
        return null;
    }

    /**
     * @since 1.2
     */
    public Object getState(FacesContext context, String viewId)
    {
        return null;
    }

    /**
     * Checks if the current request is a postback
     * 
     * @since 1.2
     */
    public boolean isPostback(FacesContext context)
    {
        return context.getExternalContext().getRequestParameterMap().containsKey(ResponseStateManager.VIEW_STATE_PARAM);
    }

    /**
     * @since 2.2
     * @param context
     * @return 
     */
    public String getCryptographicallyStrongTokenFromSession(FacesContext context)
    {
        return null;
    }
    
    /**
     * @since 2.2
     * @param context
     * @param viewId
     * @return 
     */
    public boolean isStateless(FacesContext context, String viewId)
    {
        return false;
    }
}
