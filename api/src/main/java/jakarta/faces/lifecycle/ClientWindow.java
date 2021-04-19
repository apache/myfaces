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
package jakarta.faces.lifecycle;

import java.util.Map;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;

/**
 * @since 2.2
 */
public abstract class ClientWindow
{
    /**
     * Defines the ClientWindow mode to use.
     * url = like the defined in the specs
     * url-redirect = same like 'url' but with a initial redirect, so that the first request already contains
     *                a valid windowId in the URL. Similar to DeltaSpile LAZY mode.
     * client = like the DeltaSpike CLIENTWINDOW mode.
     */
    @JSFWebConfigParam(since = "2.2.0", expectedValues = "none, url, url-redirect, client", defaultValue = "none")
    public static final String CLIENT_WINDOW_MODE_PARAM_NAME = 
            "jakarta.faces.CLIENT_WINDOW_MODE";

    /**
     * Indicate the max number of ClientWindows, which is used by {@link ClientWindowScoped}.
     * It is only active when jakarta.faces.CLIENT_WINDOW_MODE is enabled.
     *
     * @since 4.0
     */
    @JSFWebConfigParam(since="4.0", group="state", tags="performance", defaultValue = "10")
    public static final String NUMBER_OF_CLIENT_WINDOWS_PARAM_NAME = 
            "jakarta.faces.NUMBER_OF_CLIENT_WINDOWS";

    private static final String CLIENT_WINDOW_RENDER_MODE_DISABLED = 
            "org.apache.myfaces.CLIENT_WINDOW_URL_QUERY_PARAMETER_DISABLED";
    
    public abstract void decode(FacesContext context);
    
    public abstract String getId();
    
    public abstract Map<String,String> getQueryURLParameters(FacesContext context);
    
    public boolean isClientWindowRenderModeEnabled(FacesContext context)
    {
        // By default is enabled, so it is easier to check the opposite.
        return !Boolean.TRUE.equals(
                context.getAttributes().get(CLIENT_WINDOW_RENDER_MODE_DISABLED));
    }
    
    public void disableClientWindowRenderMode(FacesContext context)
    {
        context.getAttributes().put(CLIENT_WINDOW_RENDER_MODE_DISABLED, Boolean.TRUE);
    }
    
    public void enableClientWindowRenderMode(FacesContext context)
    {
        context.getAttributes().put(CLIENT_WINDOW_RENDER_MODE_DISABLED, Boolean.FALSE);
    }
}
