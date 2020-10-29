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
package org.apache.myfaces.application.viewstate;

import jakarta.faces.application.StateManager;
import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.test.base.junit.AbstractJsfConfigurableMultipleRequestsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author lu4242
 */
public class ClientSideStateCacheTest extends AbstractJsfConfigurableMultipleRequestsTestCase
{
    
    private static final int TIMESTAMP_PARAM = 2;
    
    @Test
    public void testSaveRestoreState() throws Exception
    {
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_CLIENT);

        // Initialization
        setupRequest();
        StateCache stateCache = new StateCacheClientSide();
        tearDownRequest();
        
        Object savedToken;
        Object firstSavedToken;
        
        try
        {
            setupRequest();
           
            facesContext.getViewRoot().setViewId("view1.xhtml");
            savedToken = stateCache.saveSerializedView(facesContext, 1);
            firstSavedToken = savedToken;
        }
        finally
        {
            tearDownRequest();
        }
        
        try
        {
            setupRequest();
            
            Object value = stateCache.restoreSerializedView(facesContext, "view1.xhtml", savedToken);
            
            Assert.assertEquals(1, value);
            
            facesContext.getViewRoot().setViewId("view2.xhtml");
            savedToken = stateCache.saveSerializedView(facesContext, 2);
        }
        finally
        {
            tearDownRequest();
        }

        try
        {
            setupRequest();
            
            Object value = stateCache.restoreSerializedView(facesContext, "view2.xhtml", savedToken);
            
            Assert.assertEquals(2, value);
            
            facesContext.getViewRoot().setViewId("view2.xhtml");
            savedToken = stateCache.saveSerializedView(facesContext, 3);
        }
        finally
        {
            tearDownRequest();
        }
        
        try
        {
            setupRequest();
            
            Object value = stateCache.restoreSerializedView(facesContext, "view1.xhtml", firstSavedToken);
            
            Assert.assertEquals(1, value);
        }
        finally
        {
            tearDownRequest();
        }        
    }
    
    @Test
    public void testSaveRestoreStateWrongViewId() throws Exception
    {
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_CLIENT);

        // Initialization
        setupRequest();
        StateCache stateCache = new StateCacheClientSide();
        tearDownRequest();
        
        Object savedToken;
        Object firstSavedToken;
        
        try
        {
            setupRequest();
           
            facesContext.getViewRoot().setViewId("/view1.xhtml");
            savedToken = stateCache.saveSerializedView(facesContext, 1);
            firstSavedToken = savedToken;
        }
        finally
        {
            tearDownRequest();
        }
        
        try
        {
            setupRequest();
            
            // Note we are trying to restore restoring another different view with a token from the previous view.
            // It should return null and later throw ViewExpiredException
            // In client side state saving, the encoded viewId inside the state should be compared against the passed one.
            // as parameter.
            Object value = stateCache.restoreSerializedView(facesContext, "/view2.xhtml", firstSavedToken);
            
            Assert.assertNull(value);
        }
        finally
        {
            tearDownRequest();
        }
        
        try
        {
            setupRequest();
            
            // It should restore this:
            Object value = stateCache.restoreSerializedView(facesContext, "/view1.xhtml", firstSavedToken);
            
            Assert.assertEquals(1, value);
        }
        finally
        {
            tearDownRequest();
        }
        
    }

    @Test
    public void testSaveRestoreStateClientTimeout() throws Exception
    {
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_CLIENT);
        servletContext.addInitParameter(MyfacesConfig.CLIENT_VIEW_STATE_TIMEOUT, "2");

        // Initialization
        setupRequest();
        StateCache stateCache = new StateCacheClientSide();
        tearDownRequest();
        
        Object savedToken;
        Object firstSavedToken;
        
        try
        {
            setupRequest();
           
            facesContext.getViewRoot().setViewId("/view1.xhtml");
            savedToken = stateCache.saveSerializedView(facesContext, 1);
            firstSavedToken = savedToken;
        }
        finally
        {
            tearDownRequest();
        }

        //Change timestamp to a previous date
        Long timestamp = (Long)((Object[])firstSavedToken)[TIMESTAMP_PARAM];
        ((Object[])firstSavedToken)[TIMESTAMP_PARAM] = timestamp.longValue() - 60000*3;
        
        try
        {
            setupRequest();
            
            // It should return null, because the timeStamp was changed to a previous date
            Object value = stateCache.restoreSerializedView(facesContext, "/view1.xhtml", firstSavedToken);
            
            Assert.assertNull(value);
        }
        finally
        {
            tearDownRequest();
        }
    }

    
}
