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
package org.apache.myfaces.renderkit;

import javax.faces.application.StateManager;

import org.apache.myfaces.application.StateCache;
import org.apache.myfaces.test.base.junit4.AbstractJsfConfigurableMultipleRequestsTestCase;
import org.junit.Test;
import org.testng.Assert;

public class ServerSideStateCacheTest extends AbstractJsfConfigurableMultipleRequestsTestCase
{

    @Test
    public void testNumberOfSequentialViewsInSession() throws Exception
    {
        
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_SERVER);
        servletContext.addInitParameter("org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION", "5");
        servletContext.addInitParameter("org.apache.myfaces.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION", "2");
        
        StateCache stateCache = new ServerSideStateCacheImpl();
        
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
            
            // Since org.apache.myfaces.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION is 2, the first one was already discarded
            Assert.assertNull(value);
        }
        finally
        {
            tearDownRequest();
        }
        
        
    }
}
