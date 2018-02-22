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
package org.apache.myfaces.view.facelets.tag.jstl.core;

import java.util.Map;

import javax.faces.application.StateManager;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.event.PhaseId;

import org.apache.myfaces.shared.config.MyfacesConfig;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.bean.Employee;
import org.junit.Assert;
import org.junit.Test;

public class CifTestCase extends FaceletTestCase
{

    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        
        servletContext.addInitParameter(StateManager.PARTIAL_STATE_SAVING_PARAM_NAME,
                "true");
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS,"true");
    }
    
    @Test
    public void testIf1() throws Exception
    {
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        Map session = facesContext.getExternalContext().getSessionMap();
        Employee e = new Employee();
        session.put("employee", e);

        UIViewRoot root = facesContext.getViewRoot();

        // make sure the form is there
        e.setManagement(true);
        vdl.buildView(facesContext, root,"if2.xhtml");
        UIComponent c = root.findComponent("form");
        Assert.assertNotNull("form is null", c);
        c = root.findComponent("start");
        Assert.assertNotNull("start is null", c);
        c = root.findComponent("end");
        Assert.assertNotNull("end is null", c);
               
        
        // now make sure it isn't
        e.setManagement(false);
        
        //facesContext.setViewRoot(facesContext.getApplication().getViewHandler()
        //        .createView(facesContext, "/test"));
        root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"if2.xhtml");
        c = root.findComponent("form");
        Assert.assertNull("form is not null", c);
        // start and end are from if2.xhtml, so they shouldn't be here!
        c = root.findComponent("start");
        Assert.assertNotNull("start is null", c);
        c = root.findComponent("end");
        Assert.assertNotNull("end is null", c);
        facesContext.getAttributes().remove(root);

    }
    
    @Test
    public void testIf2() throws Exception
    {
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        Map session = facesContext.getExternalContext().getSessionMap();
        Employee e = new Employee();
        session.put("employee", e);

        UIViewRoot root = facesContext.getViewRoot();

        // make sure the form is there
        e.setManagement(false);
        vdl.buildView(facesContext, root,"if2.xhtml");
        UIComponent c = root.findComponent("form");
        Assert.assertNull("form is not null", c);
        c = root.findComponent("start");
        Assert.assertNotNull("start is null", c);
        c = root.findComponent("end");
        Assert.assertNotNull("end is null", c);
        //facesContext.getAttributes().remove(root);
        
        //rebuild if.xml but with form present now
        e.setManagement(true);
        root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"if2.xhtml");
        c = root.findComponent("form");
        Assert.assertNotNull("form is null", c);
        // start and end shouldn't be in the component tree
        c = root.findComponent("start");
        Assert.assertNotNull("start is null", c);
        c = root.findComponent("end");
        Assert.assertNotNull("start is null", c);
    }    
    
    @Test
    public void testIf3() throws Exception
    {
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        Map session = facesContext.getExternalContext().getSessionMap();
        Employee e = new Employee();
        session.put("employee", e);

        UIViewRoot root = facesContext.getViewRoot();

        // make sure the form is there
        e.setManagement(true);
        vdl.buildView(facesContext, root,"if3.xhtml");
        UIComponent c = root.findComponent("form");
        Assert.assertNotNull("form is null", c);
        Assert.assertNotNull(c.getFacet("header"));
        c = root.findComponent("start");
        Assert.assertNotNull("start is null", c);
        c = root.findComponent("end");
        Assert.assertNotNull("end is null", c);
        //facesContext.getAttributes().remove(root);
        
        //rebuild if.xml but with form present now
        e.setManagement(false);
        root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"if3.xhtml");
        c = root.findComponent("form");
        Assert.assertNotNull("form is null", c);
        Assert.assertNull(c.getFacet("header"));
        // start and end shouldn't be in the component tree
        c = root.findComponent("start");
        Assert.assertNotNull("start is null", c);
        c = root.findComponent("end");
        Assert.assertNotNull("start is null", c);
    }
}
