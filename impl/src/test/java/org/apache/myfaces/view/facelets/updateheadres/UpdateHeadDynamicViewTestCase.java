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
package org.apache.myfaces.view.facelets.updateheadres;

import jakarta.faces.application.StateManager;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;

import org.apache.myfaces.mc.test.core.AbstractMyFacesRequestTestCase;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.junit.Assert;
import org.junit.Test;

public class UpdateHeadDynamicViewTestCase extends AbstractMyFacesRequestTestCase
{

    @Override
    protected boolean isScanAnnotations()
    {
        return true;
    }

    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES","org.apache.myfaces.view.facelets.updateheadres.managed");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_CLIENT);
        servletContext.addInitParameter("jakarta.faces.PARTIAL_STATE_SAVING", "true");
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
        servletContext.addInitParameter("org.apache.myfaces.STRICT_JSF_2_REFRESH_TARGET_AJAX", "true");
    }
    
    @Test
    public void testNoUpdateScript1Head() throws Exception
    {
        startViewRequest("/ajaxContent.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent content = facesContext.getViewRoot().findComponent("content");
        UIComponent page1Button = facesContext.getViewRoot().findComponent("mainForm:page1");
        
        client.ajax((UICommand)page1Button, "action", page1Button.getClientId(facesContext), content.getClientId(facesContext), true);
        
        processLifecycleExecuteAndRender();
        String text = getRenderedContent(facesContext);
        // the inclusion should trigger update head
        Assert.assertFalse(text.contains("update id=\"jakarta.faces.ViewHead\""));
        //System.out.println(text);
        endRequest();
    }
    
    @Test
    public void testUpdateScript2Head() throws Exception
    {
        startViewRequest("/ajaxContent.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent content = facesContext.getViewRoot().findComponent("content");
        UIComponent page2Button = facesContext.getViewRoot().findComponent("mainForm:page2");
        
        client.ajax((UICommand)page2Button, "action", page2Button.getClientId(facesContext), content.getClientId(facesContext), true);
        
        processLifecycleExecuteAndRender();
        
        String text = getRenderedContent(facesContext);
        // the inclusion should trigger update head
        Assert.assertTrue(text.contains("update id=\"jakarta.faces.Resource\""));
        Assert.assertTrue(text.contains("alert(\"script2\");"));
        //System.out.println(text);
        endRequest();
    }
    
    @Test
    public void testUpdateScript3Head() throws Exception
    {
        startViewRequest("/ajaxContent.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent content = facesContext.getViewRoot().findComponent("content");
        UIComponent page3Button = facesContext.getViewRoot().findComponent("mainForm:page3");
        
        client.ajax((UICommand)page3Button, "action", page3Button.getClientId(facesContext), content.getClientId(facesContext), true);
        
        processLifecycleExecuteAndRender();
        
        String text = getRenderedContent(facesContext);
        // the inclusion should trigger update head
        Assert.assertTrue(text.contains("update id=\"jakarta.faces.Resource\""));
        Assert.assertTrue(text.contains("alert(\"script3\");"));
        Assert.assertTrue(text.contains("link rel=\"stylesheet\" type=\"text/css\" href=\"/test/faces/jakarta.faces.resource/style3.css\""));
        //System.out.println(text);
        endRequest();
    }
   
}
