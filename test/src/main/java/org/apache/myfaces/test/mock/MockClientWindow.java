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

import java.util.HashMap;
import java.util.Map;
import jakarta.faces.context.FacesContext;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.faces.render.ResponseStateManager;

/**
 * Mock ClientWindow implementation that by default set the id with "1".
 * 
 * @author Leonardo Uribe
 */
public class MockClientWindow extends ClientWindow
{
    private String windowId;
    
    private Map<String,String> queryParamsMap;

    public MockClientWindow()
    {
    }
    
    @Override
    public void decode(FacesContext context)
    {
        String windowId = calculateWindowId(context);

        if (windowId != null)
        {
            // Store the current windowId.
            setId(windowId);
        }
        else
        {
            //Generate a new windowId
            setId("1");
        }
    }
    
    
    protected String calculateWindowId(FacesContext context)
    {
        //1. If it comes as parameter, it takes precedence over any other choice, because
        //   no browser is capable to do a POST and create a new window at the same time.
        String windowId = context.getExternalContext().getRequestParameterMap().get(
                ResponseStateManager.CLIENT_WINDOW_PARAM);
        if (windowId != null)
        {
            return windowId;
        }
        windowId = context.getExternalContext().getRequestParameterMap().get(
                ResponseStateManager.CLIENT_WINDOW_URL_PARAM);
        if (windowId != null)
        {
            return windowId;
        }
        return null;
    }

    @Override
    public String getId()
    {
        return this.windowId;
    }
    
    public void setId(String id)
    {
        this.windowId = id;
    }

    @Override
    public Map<String, String> getQueryURLParameters(FacesContext context)
    {
        if (queryParamsMap == null)
        {
            String id = context.getExternalContext().getClientWindow().getId();
            if (id != null)
            {
                queryParamsMap = new HashMap<String, String>(2,1);
                queryParamsMap.put(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, id);
            }
        }
        return queryParamsMap;
    }
}
