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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import javax.faces.application.ProjectStage;
import javax.faces.application.StateManager;

import org.apache.myfaces.test.base.junit4.AbstractJsfConfigurableMultipleRequestsTestCase;
import org.junit.Assert;
import org.junit.Test;

public class ServerSideStateCacheTest extends AbstractJsfConfigurableMultipleRequestsTestCase
{

    @Test
    public void testNumberOfSequentialViewsInSession() throws Exception
    {
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_SERVER);
        servletContext.addInitParameter("org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION", "5");
        servletContext.addInitParameter("org.apache.myfaces.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION", "2");
        servletContext.addInitParameter(StateManager.SERIALIZE_SERVER_STATE_PARAM_NAME, "true");

        // Initialization
        setupRequest();
        StateCache stateCache = new StateCacheServerSide();
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
            
            // Since org.apache.myfaces.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION is 2, the first one was already discarded
            Assert.assertNull(value);
        }
        finally
        {
            tearDownRequest();
        }
    }
    
    @Test
    public void testSaveRestoreStateWrongViewId() throws Exception
    {
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_SERVER);

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
            // In server side state saving, the hashCode of the viewId should be part of the key used to restore
            // the state, along with a counter.
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
    public void testNonExistingViewId() throws Exception
    {
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_SERVER);
        servletContext.addInitParameter("org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION", "5");
        servletContext.addInitParameter("org.apache.myfaces.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION", "2");

        // this issue only happens in projectstage Production
        servletContext.addInitParameter("faces.PROJECT_STAGE", "Production");

        try
        {
            // Initialization
            setupRequest();

            // we need to take a viewId which is null -> not existing.
            facesContext.getViewRoot().setViewId(null);

            StateCache stateCache = new StateCacheServerSide();
            Object savedToken = stateCache.saveSerializedView(facesContext, 1);

        }
        finally
        {
            tearDownRequest();
        }

    }

    public void tryStateKeySerialization() throws Exception
    {
        // Initialization
        setupRequest();
        StateCache stateCache = new StateCacheServerSide();
        tearDownRequest();
        
        Object savedToken;
        Object firstSavedToken;
        
        try
        {
            setupRequest();
            facesContext.getViewRoot().setViewId("view1.xhtml");
            savedToken = stateCache.saveSerializedView(facesContext, 1);
            firstSavedToken = savedToken;
            
            for (Map.Entry<String, Object> entry : facesContext.getExternalContext().getSessionMap().entrySet())
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(entry.getValue());
                oos.flush();
                baos.flush();
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bais);
                Object blorg = ois.readObject();
            }
        }
        finally
        {
            tearDownRequest();
        }
    }

    @Test
    public void testStateKeySerialization1() throws Exception
    {
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_SERVER);
        servletContext.addInitParameter("org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION", "5");
        servletContext.addInitParameter("org.apache.myfaces.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION", "2");
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, ProjectStage.Production.toString());
        
        tryStateKeySerialization();
    }
    
    @Test
    public void testStateKeySerialization2() throws Exception
    {
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_SERVER);
        servletContext.addInitParameter("org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION", "5");
        servletContext.addInitParameter("org.apache.myfaces.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION", "2");
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, ProjectStage.Production.toString());
        servletContext.addInitParameter("org.apache.myfaces.RANDOM_KEY_IN_VIEW_STATE_SESSION_TOKEN", "random");
        
        tryStateKeySerialization();
    }
    
    @Test
    public void testStateKeySerialization3() throws Exception
    {
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_SERVER);
        servletContext.addInitParameter("org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION", "5");
        servletContext.addInitParameter("org.apache.myfaces.NUMBER_OF_SEQUENTIAL_VIEWS_IN_SESSION", "2");
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, ProjectStage.Development.toString());
        
        tryStateKeySerialization();
    }
    
}
