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
package org.apache.myfaces.application;

import jakarta.faces.application.StateManager;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.ResponseStateManager;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StateManagerImpl extends StateManager
{
    private static final Logger log = Logger.getLogger(StateManagerImpl.class.getName());
    
    public static final String SERIALIZED_VIEW_REQUEST_ATTR = 
        StateManagerImpl.class.getName() + ".SERIALIZED_VIEW";

    public StateManagerImpl()
    {
    }

    @Override
    public void writeState(FacesContext facesContext, Object state) throws IOException
    {
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("Entering writeState");
        }

        //save state in response (client)
        RenderKit renderKit = facesContext.getRenderKit();
        ResponseStateManager responseStateManager = renderKit.getResponseStateManager();

        responseStateManager.writeState(facesContext, state);

        if (log.isLoggable(Level.FINEST))
        {
            log.finest("Exiting writeState");
        }
    }
}
