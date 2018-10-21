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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.ClientWindow;
import javax.faces.lifecycle.ClientWindowFactory;

/**
 *
 * @author lu4242
 */
public class MockClientWindowFactory extends ClientWindowFactory
{
    private static final String WINDOW_MODE_NONE = "none";
    private static final String WINDOW_MODE_MOCK = "mock";
    
    private String windowMode;

    @Override
    public ClientWindow getClientWindow(FacesContext facesContext)
    {
        if (WINDOW_MODE_NONE.equals(getWindowMode(facesContext)))
        {
            //No need to do anything
            return null;
        }
        else
        {
            if (WINDOW_MODE_MOCK.equals(getWindowMode(facesContext)))
            {
                return new MockClientWindow();
            }
            else
            {
                return null;
            }
        }
    }    
    
    private String getWindowMode(FacesContext context)
    {
        if (windowMode == null)
        {
            windowMode = getStringInitParameter(
                    context.getExternalContext(), 
                    ClientWindow.CLIENT_WINDOW_MODE_PARAM_NAME, WINDOW_MODE_NONE);
        }
        return windowMode;
    }
    
    private static String getStringInitParameter(ExternalContext context, String name, String defaultValue)
    {
        if (name == null)
        {
            throw new NullPointerException();
        }
        
        String param = context.getInitParameter(name);
        
        if (param == null)
        {
            return defaultValue;
        }

        param = param.trim();
        if (param.length() == 0)
        {
            return defaultValue;
        }

        return param;
    }    
}
