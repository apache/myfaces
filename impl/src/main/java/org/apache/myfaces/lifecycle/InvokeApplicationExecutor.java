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
package org.apache.myfaces.lifecycle;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import org.apache.myfaces.view.facelets.ViewPoolProcessor;

/**
 * Implements the invoke application phase (Faces Spec 2.2.5)
 * 
 * @author Nikolay Petrov (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
class InvokeApplicationExecutor extends PhaseExecutor
{
    private ViewPoolProcessor viewPoolProcessor;
    private boolean initialized = false;
    
    @Override
    public boolean execute(FacesContext facesContext)
    {
        if (facesContext.getViewRoot() == null)
        {
            throw new ViewNotFoundException("A view is required to execute "+facesContext.getCurrentPhaseId());
        }
        facesContext.getViewRoot().processApplication(facesContext);
        
        ViewPoolProcessor processor = getViewPoolProcessor(facesContext);
        if (processor != null)
        {
            processor.processDeferredNavigation(facesContext);
        }
        
        return false;
    }

    @Override
    public PhaseId getPhase()
    {
        return PhaseId.INVOKE_APPLICATION;
    }
    
    private ViewPoolProcessor getViewPoolProcessor(FacesContext context)
    {
        if (!initialized)
        {
            viewPoolProcessor = ViewPoolProcessor.getInstance(context);
            initialized = true;
        }
        return viewPoolProcessor;
    }
}
