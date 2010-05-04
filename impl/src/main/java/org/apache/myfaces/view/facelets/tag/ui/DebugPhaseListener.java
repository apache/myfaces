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

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * PhaseListener to create extended debug information.
 * Installed in FacesConfigurator.configureLifecycle() if ProjectStage is Development.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class DebugPhaseListener implements PhaseListener
{
    
    private static final long serialVersionUID = -1517198431551012882L;

    /**
     * VisitCallback used for visitTree()  
     *  
     * @author Jakob Korherr
     */
    private class DebugVisitCallback implements VisitCallback
    {

        public VisitResult visit(VisitContext context, UIComponent target)
        {
            // TODO implement for components that do not extend UIInput
            
            return VisitResult.ACCEPT;
        }
        
    }
    
    private boolean _afterPhase = false;
    private PhaseId _currentPhase;
    private DebugVisitCallback _visitCallback = new DebugVisitCallback();

    public void afterPhase(PhaseEvent event)
    {
        _doTreeVisit(event, true);
    }

    public void beforePhase(PhaseEvent event)
    {
        _doTreeVisit(event, false);
    }

    public PhaseId getPhaseId()
    {
        return PhaseId.ANY_PHASE;
    }
    
    private void _doTreeVisit(PhaseEvent event, boolean afterPhase)
    {
        _afterPhase = afterPhase;
        _currentPhase = event.getPhaseId();
        
        // visitTree() on the UIViewRoot
        UIViewRoot viewroot = event.getFacesContext().getViewRoot();
        if (viewroot != null)
        {
            viewroot.visitTree(VisitContext.createVisitContext(
                    event.getFacesContext()), _visitCallback);
        }
    }

}
