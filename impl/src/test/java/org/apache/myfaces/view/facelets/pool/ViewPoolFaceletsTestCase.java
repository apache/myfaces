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
package org.apache.myfaces.view.facelets.pool;

import javax.faces.application.ProjectStage;
import javax.faces.application.StateManager;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.event.PhaseId;
import javax.faces.render.RenderKitFactory;
import org.junit.Assert;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.ViewPoolProcessor;
import org.junit.Test;

/**
 *
 * @author lu4242
 */
public class ViewPoolFaceletsTestCase extends FaceletTestCase
{
    
    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter(StateManager.PARTIAL_STATE_SAVING_PARAM_NAME, "true");
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
        servletContext.addInitParameter("javax.faces.FACELETS_LIBRARIES", "/test-facelet.taglib.xml");
        //servletContext.addInitParameter(ViewPoolProcessor.INIT_PARAM_VIEW_POOL_ENABLED, "true");
        servletContext.addInitParameter("org.apache.myfaces.CACHE_EL_EXPRESSIONS", "alwaysRecompile");
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, "Production");
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        ViewPoolProcessor.initialize(facesContext);
    }

    @Override
    protected void setupComponents() throws Exception
    {
        super.setupComponents();
        application.addComponent(UISimpleComponentA.COMPONENT_TYPE, UISimpleComponentA.class.getName());
    }

    /**
     * Check remove component resource added using h:outputScript
     * 
     * @throws Exception 
     */
    @Test
    public void testDynPageResourceCleanup1() throws Exception
    {
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        
        // Store structure for state checkA=false
        facesContext.getExternalContext().getRequestMap().put("checkA", Boolean.FALSE);
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "dynPageResourceCleanup1.xhtml");
        processor.storeViewStructureMetadata(facesContext, root);

        // Store structure for state checkA=true
        facesContext.getExternalContext().getRequestMap().put("checkA", Boolean.TRUE);
        root = new UIViewRoot();
        root.setViewId("/test");
        root.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.setViewRoot(root);
        vdl.buildView(facesContext, root, "dynPageResourceCleanup1.xhtml");
        
        processor.storeViewStructureMetadata(facesContext, root);
        
        // Try change a view with state checkA=true to checkA=false
        facesContext.getAttributes().remove(root); //change set filled view
        facesContext.getExternalContext().getRequestMap().put("checkA", Boolean.FALSE);
        vdl.buildView(facesContext, root, "dynPageResourceCleanup1.xhtml");
        
        UIComponent headerFacet = root.getFacet("head");
        int oldCount = headerFacet.getChildCount();
        // the resource should still be there
        //Assert.assertEquals(1, oldCount);
        // The code in MYFACES-3659 reset the component after refresh
        Assert.assertEquals(0, oldCount);

        // Clear the view and synchronize resources
        ViewStructureMetadata metadata = processor.retrieveViewStructureMetadata(facesContext, root);
        Assert.assertNotNull(metadata);
        processor.clearTransientAndNonFaceletComponentsForDynamicView(facesContext, root, metadata);
        
        // the resource added by state checkA=true should not be there
        Assert.assertEquals(0, headerFacet.getChildCount());
    }
    
    /**
     * Check remove component resource added using @ResourceDependency
     * 
     * @throws Exception 
     */
    @Test
    public void testDynPageResourceCleanup2() throws Exception
    {
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        
        // Store structure for state checkA=false
        facesContext.getExternalContext().getRequestMap().put("checkA", Boolean.FALSE);
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "dynPageResourceCleanup2.xhtml");
        processor.storeViewStructureMetadata(facesContext, root);

        // Store structure for state checkA=true
        facesContext.getExternalContext().getRequestMap().put("checkA", Boolean.TRUE);
        root = new UIViewRoot();
        root.setViewId("/test");
        root.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.setViewRoot(root);
        vdl.buildView(facesContext, root, "dynPageResourceCleanup2.xhtml");
        
        processor.storeViewStructureMetadata(facesContext, root);
        
        // Try change a view with state checkA=true to checkA=false
        facesContext.getAttributes().remove(root); //change set filled view
        facesContext.getExternalContext().getRequestMap().put("checkA", Boolean.FALSE);
        vdl.buildView(facesContext, root, "dynPageResourceCleanup2.xhtml");
        
        // the resource should still be there
        UIComponent headerFacet = root.getFacet("head");
        int oldCount = headerFacet.getChildCount();
        Assert.assertEquals(1, oldCount);

        // Clear the view and synchronize resources
        ViewStructureMetadata metadata = processor.retrieveViewStructureMetadata(facesContext, root);
        Assert.assertNotNull(metadata);
        processor.clearTransientAndNonFaceletComponentsForDynamicView(facesContext, root, metadata);
        
        // the resource added by state checkA=true should not be there
        Assert.assertEquals(0, headerFacet.getChildCount());
    }
    
}
