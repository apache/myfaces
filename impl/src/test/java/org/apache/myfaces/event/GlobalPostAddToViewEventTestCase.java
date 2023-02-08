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
package org.apache.myfaces.event;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.html.HtmlHead;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import org.apache.myfaces.application.ApplicationImplEventManager;
import org.apache.myfaces.test.base.junit.AbstractJsfConfigurableMockTestCase;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GlobalPostAddToViewEventTestCase extends AbstractJsfConfigurableMockTestCase
{
    public class HeadResourceListener implements SystemEventListener
    {
        @Override
        public boolean isListenerForSource(Object source)
        {
            return "jakarta.faces.Head".equals(((UIComponent) source).getRendererType());
        }

        @Override
        public void processEvent(SystemEvent event)
        {
            event.getFacesContext().getAttributes().put("SystemEventListenerInvokedForHead", Boolean.TRUE);
        }
    }

    @Test
    public void testPostAddToViewSourceNull() throws Exception
    {
        ApplicationImplEventManager eventManager = new ApplicationImplEventManager();
        
        eventManager.subscribeToEvent(PostAddToViewEvent.class, new HeadResourceListener());

        eventManager.publishEvent(facesContext, PostAddToViewEvent.class, HtmlHead.class, new HtmlHead());

        Assertions.assertTrue(facesContext.getAttributes().containsKey("SystemEventListenerInvokedForHead"));
    }
    
    @Test
    public void testPostAddToViewSourceSuperClass() throws Exception
    {
        ApplicationImplEventManager eventManager = new ApplicationImplEventManager();
        
        eventManager.subscribeToEvent(PostAddToViewEvent.class, UIOutput.class, new HeadResourceListener());

        eventManager.publishEvent(facesContext, PostAddToViewEvent.class, HtmlHead.class, new HtmlHead());

        Assertions.assertTrue(facesContext.getAttributes().containsKey("SystemEventListenerInvokedForHead"));
    }
    
    @Test
    public void testPostAddToViewSourceDirect() throws Exception
    {
        ApplicationImplEventManager eventManager = new ApplicationImplEventManager();
        
        eventManager.subscribeToEvent(PostAddToViewEvent.class, HtmlHead.class, new HeadResourceListener());

        eventManager.publishEvent(facesContext, PostAddToViewEvent.class, HtmlHead.class, new HtmlHead());

        Assertions.assertTrue(facesContext.getAttributes().containsKey("SystemEventListenerInvokedForHead"));
    }
    
    @Test
    public void testPostAddToViewSourceUnmatching() throws Exception
    {
        ApplicationImplEventManager eventManager = new ApplicationImplEventManager();
        
        eventManager.subscribeToEvent(PostAddToViewEvent.class, HtmlInputText.class, new HeadResourceListener());

        eventManager.publishEvent(facesContext, PostAddToViewEvent.class, HtmlHead.class, new HtmlHead());

        Assertions.assertFalse(facesContext.getAttributes().containsKey("SystemEventListenerInvokedForHead"));
    }
}
