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

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlHead;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import org.apache.myfaces.test.base.junit4.AbstractJsfConfigurableMockTestCase;

import org.junit.Assert;
import org.junit.Test;

public class GlobalPostAddToViewEventTestCase extends AbstractJsfConfigurableMockTestCase
{
    public class HeadResourceListener implements SystemEventListener
    {
        @Override
        public boolean isListenerForSource(Object source)
        {
            return "javax.faces.Head".equals(((UIComponent) source).getRendererType());
        }

        @Override
        public void processEvent(SystemEvent event)
        {
            event.getFacesContext().getAttributes().put("SystemEventListenerInvokedForHead", Boolean.TRUE);
        }
    }

    @Test
    public void testPostAddToViewForHead() throws Exception
    {
        application.subscribeToEvent(PostAddToViewEvent.class, new HeadResourceListener());

        application.addComponent(HtmlHead.COMPONENT_TYPE,
                HtmlHead.class.getName());
        
        HtmlHead comp = (HtmlHead) application.createComponent(facesContext, 
                HtmlHead.COMPONENT_TYPE,
                "javax.faces.Head");
        
        // Invoke PostAddToViewEvent
        facesContext.getViewRoot().getChildren().add(comp);

        Assert.assertTrue(facesContext.getAttributes().containsKey("SystemEventListenerInvokedForHead"));
    }
}
