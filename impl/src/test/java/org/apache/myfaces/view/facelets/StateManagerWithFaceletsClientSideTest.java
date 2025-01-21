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
package org.apache.myfaces.view.facelets;

import jakarta.faces.application.StateManager;

import org.apache.myfaces.application.StateManagerImpl;

public class StateManagerWithFaceletsClientSideTest extends StateManagerWithFaceletsTest
{

    @Override
    protected void setUpApplication() throws Exception
    {
        super.setUpApplication();
        
        application.setStateManager(new StateManagerImpl());
    }

    @Override
    protected void setUpServletContextAndSession() throws Exception
    {
        super.setUpServletContextAndSession();
        
        servletContext.addInitParameter(StateManager.PARTIAL_STATE_SAVING_PARAM_NAME, "true");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.StateSavingMethod.CLIENT.name());
    }

    @Override
    public void testWriteAndRestoreState() throws Exception
    {
        super.testWriteAndRestoreState();
    }

    @Override
    public void testWriteAndRestoreStateWithMyFacesRSM() throws Exception
    {
        super.testWriteAndRestoreStateWithMyFacesRSM();
    }


}
