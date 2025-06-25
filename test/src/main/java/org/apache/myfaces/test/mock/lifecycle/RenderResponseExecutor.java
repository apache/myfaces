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

package org.apache.myfaces.test.mock.lifecycle;

import java.io.IOException;

import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;

/**
 * Implements the lifecycle as described in Spec. 1.0 PFD Chapter 2
 *
 * render response phase (JSF Spec 2.2.6)
 * 
 * @author Nikolay Petrov
 * @since 1.0.0
 */
class RenderResponseExecutor implements PhaseExecutor
{
    @Override
    public boolean execute(FacesContext facesContext)
    {
        Application application = facesContext.getApplication();
        ViewHandler viewHandler = application.getViewHandler();

        try
        {
            viewHandler.renderView(facesContext, facesContext.getViewRoot());
        }
        catch (IOException e)
        {
            throw new FacesException(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public PhaseId getPhase()
    {
        return PhaseId.RENDER_RESPONSE;
    }
}
