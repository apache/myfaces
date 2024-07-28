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

import java.beans.BeanInfo;
import java.io.IOException;
import java.util.Iterator;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Resource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlCommandButton;
import jakarta.faces.component.html.HtmlForm;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.ResponseStateManager;
import jakarta.faces.view.StateManagementStrategy;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.faces.view.ViewDeclarationLanguageFactory;
import jakarta.faces.view.ViewMetadata;

import org.apache.myfaces.application.ApplicationFactoryImpl;
import org.apache.myfaces.application.StateManagerImpl;
import org.apache.myfaces.component.visit.VisitContextFactoryImpl;
import org.apache.myfaces.renderkit.html.HtmlButtonRenderer;
import org.apache.myfaces.renderkit.html.HtmlFormRenderer;
import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.test.base.junit.AbstractFacesConfigurableMockTestCase;
import org.apache.myfaces.test.mock.MockRenderKit;
import org.apache.myfaces.view.ViewMetadataBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PartialStateManagementStrategyTest extends
        AbstractFacesConfigurableMockTestCase
{

    public PartialStateManagementStrategyTest()
    {
        super();
    }
    
    public Object stateToRestore;
    
    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        
        FactoryFinder.setFactory(FactoryFinder.VISIT_CONTEXT_FACTORY, VisitContextFactoryImpl.class.getName());
        
        RenderKitFactory renderKitFactory = (RenderKitFactory)FactoryFinder.getFactory("jakarta.faces.render.RenderKitFactory");
        renderKit = new MockRenderKit()
        {
            ResponseStateManager stateManager = new ResponseStateManager(){
                public Object getState(FacesContext context, String viewId)
                {
                    return stateToRestore;
                }
            };

            @Override
            public ResponseStateManager getResponseStateManager()
            {
                return stateManager;
            }
        };
        renderKitFactory.addRenderKit("HTML_BASIC2", renderKit);
        
        //FactoryFinder.setFactory(FactoryFinder.VISIT_CONTEXT_FACTORY,
        //        VisitContextFactoryImpl.class.getName());

        renderKit.addRenderer(
                new HtmlCommandButton().getFamily(),
                new HtmlCommandButton().getRendererType(),
                new HtmlButtonRenderer());
        renderKit.addRenderer(
                new HtmlForm().getFamily(),
                new HtmlForm().getRendererType(),
                new HtmlFormRenderer());
        renderKit.addRenderer(
                new HtmlOutputText().getFamily(),
                new HtmlOutputText().getRendererType(),
                new HtmlTextRenderer());
        renderKit.addRenderer(
                new HtmlInputText().getFamily(),
                new HtmlInputText().getRendererType(),
                new HtmlTextRenderer());           
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        stateToRestore = null;
        super.tearDown();
    }

    public static class MockViewDeclarationLanguageFactory extends ViewDeclarationLanguageFactory
    {

        public ViewDeclarationLanguage vdl = new MockViewDeclarationLanguage();
        
        @Override
        public ViewDeclarationLanguage getViewDeclarationLanguage(String viewId)
        {
            return vdl;
        }
        
    }
    
    @Override
    protected void setFactories() throws Exception
    {
        super.setFactories();
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY,
                ApplicationFactoryImpl.class.getName());
        FactoryFinder.setFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY, MockViewDeclarationLanguageFactory.class.getName());
    }

    @Test
    public void testSimpleSaveRestore() throws Exception
    {
        ViewDeclarationLanguage vdl =((MockViewDeclarationLanguageFactory)FactoryFinder.getFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY)).vdl;
        PartialStateManagementStrategy stateManagement = new PartialStateManagementStrategy();
        
        servletContext.addInitParameter("jakarta.faces.STATE_SAVING_METHOD", "client");
        
        UIViewRoot viewRoot = vdl.createView(facesContext, "/root");
        vdl.buildView(facesContext, viewRoot);
        stateManagement.suscribeListeners(viewRoot);
        
        viewRoot.getAttributes().put("somekey", "somevalue");
        
        Object state1 = stateManagement.saveView(facesContext);
        stateToRestore = state1;
        facesContext.setViewRoot(null);
        stateManagement.restoreView(facesContext, "/root", viewRoot.getRenderKitId());
        
        viewRoot = facesContext.getViewRoot();
        
        Assertions.assertEquals("somevalue", viewRoot.getAttributes().get("somekey"));
        Assertions.assertNotNull(viewRoot.findComponent("form1"));
        Assertions.assertNotNull(viewRoot.findComponent("form1:foo1"));
        Assertions.assertNotNull(viewRoot.findComponent("form1:var1"));
        Assertions.assertNotNull(viewRoot.findComponent("form1:button1"));
    }
    
    @Test
    public void testSaveRestoreAddComponent() throws Exception
    {
        ViewDeclarationLanguage vdl =((MockViewDeclarationLanguageFactory)FactoryFinder.getFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY)).vdl;
        PartialStateManagementStrategy stateManagement = new PartialStateManagementStrategy();
        
        servletContext.addInitParameter("jakarta.faces.STATE_SAVING_METHOD", "client");
        
        UIViewRoot viewRoot = vdl.createView(facesContext, "/root");
        vdl.buildView(facesContext, viewRoot);
        stateManagement.suscribeListeners(viewRoot);

        viewRoot.getAttributes().put("somekey", "somevalue");
        
        HtmlOutputText a = new HtmlOutputText();
        
        a.setId("output1");
        a.setValue("testOutput1");
        
        viewRoot.getChildren().add(a);
        
        Object state1 = stateManagement.saveView(facesContext);
        stateToRestore = state1;
        facesContext.setViewRoot(null);
        stateManagement.restoreView(facesContext, "/root", viewRoot.getRenderKitId());
        
        viewRoot = facesContext.getViewRoot();
        
        Assertions.assertEquals("somevalue", viewRoot.getAttributes().get("somekey"));
        Assertions.assertNotNull(viewRoot.findComponent("form1"));
        Assertions.assertNotNull(viewRoot.findComponent("form1:foo1"));
        Assertions.assertNotNull(viewRoot.findComponent("form1:var1"));
        Assertions.assertNotNull(viewRoot.findComponent("form1:button1"));
        
        a = (HtmlOutputText) viewRoot.findComponent("output1");
        Assertions.assertNotNull(a);
        Assertions.assertEquals("testOutput1",a.getValue());
    }
    
    @Test
    public void testSaveRestoreAddComponentMultiplePostback() throws Exception
    {
        ViewDeclarationLanguage vdl =((MockViewDeclarationLanguageFactory)FactoryFinder.getFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY)).vdl;
        PartialStateManagementStrategy stateManagement = new PartialStateManagementStrategy();
        
        servletContext.addInitParameter("jakarta.faces.STATE_SAVING_METHOD", "client");
        
        UIViewRoot viewRoot = vdl.createView(facesContext, "/root");
        
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        
        vdl.buildView(facesContext, viewRoot);
        
        stateManagement.suscribeListeners(viewRoot);
        
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        viewRoot.getAttributes().put("somekey", "somevalue");
        
        HtmlOutputText a = new HtmlOutputText();
        a.setId("output1");
        a.setValue("testOutput1");
        viewRoot.getChildren().add(a);
        
        viewRoot = saveAndRestore(stateManagement, viewRoot);

        HtmlOutputText b = new HtmlOutputText();
        b.setId("output2");
        b.setValue("testOutput2");
        viewRoot.getChildren().add(b);

        viewRoot = saveAndRestore(stateManagement, viewRoot);
        
        HtmlOutputText c = new HtmlOutputText();
        c.setId("output3");
        c.setValue("testOutput3");
        viewRoot.getChildren().add(c);

        viewRoot = saveAndRestore(stateManagement, viewRoot);
        
        HtmlOutputText d = new HtmlOutputText();
        d.setId("output4");
        d.setValue("testOutput4");
        viewRoot.getChildren().add(d);
        
        viewRoot = saveAndRestore(stateManagement, viewRoot);
        
        Assertions.assertEquals("somevalue", viewRoot.getAttributes().get("somekey"));
        Assertions.assertNotNull(viewRoot.findComponent("form1"));
        Assertions.assertNotNull(viewRoot.findComponent("form1:foo1"));
        Assertions.assertNotNull(viewRoot.findComponent("form1:var1"));
        Assertions.assertNotNull(viewRoot.findComponent("form1:button1"));
        
        a = (HtmlOutputText) viewRoot.findComponent("output1");
        Assertions.assertNotNull(a);
        Assertions.assertEquals("testOutput1",a.getValue());
        b = (HtmlOutputText) viewRoot.findComponent("output2");
        Assertions.assertNotNull(b);
        Assertions.assertEquals("testOutput2",b.getValue());
        c = (HtmlOutputText) viewRoot.findComponent("output3");
        Assertions.assertNotNull(c);
        Assertions.assertEquals("testOutput3",c.getValue());
        d = (HtmlOutputText) viewRoot.findComponent("output4");
        Assertions.assertNotNull(d);
        Assertions.assertEquals("testOutput4",d.getValue());
    }
    
    @Test
    public void testSaveRestoreToogleComponent() throws Exception
    {
        ViewDeclarationLanguage vdl =((MockViewDeclarationLanguageFactory)FactoryFinder.getFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY)).vdl;
        PartialStateManagementStrategy stateManagement = new PartialStateManagementStrategy();
        
        servletContext.addInitParameter("jakarta.faces.STATE_SAVING_METHOD", "client");
        
        UIViewRoot viewRoot = vdl.createView(facesContext, "/root");
        vdl.buildView(facesContext, viewRoot);
        stateManagement.suscribeListeners(viewRoot);

        viewRoot.getAttributes().put("somekey", "somevalue");
        
        HtmlOutputText a = new HtmlOutputText();
        a.setId("output1");
        a.setValue("testOutput1");
        
        viewRoot.getChildren().add(a);
        
        viewRoot = saveAndRestore(stateManagement, viewRoot);
        
        Assertions.assertEquals("form1",viewRoot.getChildren().get(0).getId());
        Assertions.assertEquals("output1",viewRoot.getChildren().get(1).getId());
        
        UIComponent component = viewRoot.getChildren().remove( 0 );
        viewRoot.getChildren().add( component );

        viewRoot = saveAndRestore(stateManagement, viewRoot);

        Assertions.assertEquals("output1",viewRoot.getChildren().get(0).getId());
        Assertions.assertEquals("form1",viewRoot.getChildren().get(1).getId());
        
        component = viewRoot.getChildren().remove( 0 );
        viewRoot.getChildren().add( component );

        viewRoot = saveAndRestore(stateManagement, viewRoot);

        Assertions.assertEquals("form1",viewRoot.getChildren().get(0).getId());
        Assertions.assertEquals("output1",viewRoot.getChildren().get(1).getId());
        
        component = viewRoot.getChildren().remove( 0 );
        viewRoot.getChildren().add( component );

        viewRoot = saveAndRestore(stateManagement, viewRoot);

        Assertions.assertEquals("output1",viewRoot.getChildren().get(0).getId());
        Assertions.assertEquals("form1",viewRoot.getChildren().get(1).getId());
        
        component = viewRoot.getChildren().remove( 0 );
        viewRoot.getChildren().add( component );

        viewRoot = saveAndRestore(stateManagement, viewRoot);

        Assertions.assertEquals("form1",viewRoot.getChildren().get(0).getId());
        Assertions.assertEquals("output1",viewRoot.getChildren().get(1).getId());
        
        Assertions.assertEquals("somevalue", viewRoot.getAttributes().get("somekey"));
        Assertions.assertNotNull(viewRoot.findComponent("form1"));
        Assertions.assertNotNull(viewRoot.findComponent("form1:foo1"));
        Assertions.assertNotNull(viewRoot.findComponent("form1:var1"));
        Assertions.assertNotNull(viewRoot.findComponent("form1:button1"));
        
        a = (HtmlOutputText) viewRoot.findComponent("output1");
        Assertions.assertNotNull(a);
        Assertions.assertEquals("testOutput1",a.getValue());
    }
    
    @Test
    public void testSaveRestoreToogleComponent2() throws Exception
    {
        ViewDeclarationLanguage vdl =((MockViewDeclarationLanguageFactory)FactoryFinder.getFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY)).vdl;
        PartialStateManagementStrategy stateManagement = new PartialStateManagementStrategy();
        
        servletContext.addInitParameter("jakarta.faces.STATE_SAVING_METHOD", "client");
        
        UIViewRoot viewRoot = vdl.createView(facesContext, "/root");
        vdl.buildView(facesContext, viewRoot);
        stateManagement.suscribeListeners(viewRoot);

        viewRoot.getAttributes().put("somekey", "somevalue");
        
        HtmlOutputText a = new HtmlOutputText();
        a.setId("output1");
        a.setValue("testOutput1");        
        viewRoot.getChildren().add(a);

        HtmlOutputText b = new HtmlOutputText();
        b.setId("output2");
        b.setValue("testOutput2");        
        viewRoot.getChildren().add(b);
        
        viewRoot = saveAndRestore(stateManagement, viewRoot);
        
        Assertions.assertEquals("form1",viewRoot.getChildren().get(0).getId());
        Assertions.assertEquals("output1",viewRoot.getChildren().get(1).getId());
        Assertions.assertEquals("output2",viewRoot.getChildren().get(2).getId());
        
        UIComponent component = viewRoot.getChildren().remove( 0 );
        viewRoot.getChildren().add( component );

        viewRoot = saveAndRestore(stateManagement, viewRoot);

        Assertions.assertEquals("output1",viewRoot.getChildren().get(0).getId());
        Assertions.assertEquals("output2",viewRoot.getChildren().get(1).getId());
        Assertions.assertEquals("form1",viewRoot.getChildren().get(2).getId());
        
        component = viewRoot.getChildren().remove( 0 );
        viewRoot.getChildren().add( component );

        viewRoot = saveAndRestore(stateManagement, viewRoot);

        Assertions.assertEquals("output2",viewRoot.getChildren().get(0).getId());
        Assertions.assertEquals("form1",viewRoot.getChildren().get(1).getId());
        Assertions.assertEquals("output1",viewRoot.getChildren().get(2).getId());

        component = viewRoot.getChildren().remove( 0 );
        viewRoot.getChildren().add( component );

        viewRoot = saveAndRestore(stateManagement, viewRoot);

        Assertions.assertEquals("form1",viewRoot.getChildren().get(0).getId());
        Assertions.assertEquals("output1",viewRoot.getChildren().get(1).getId());
        Assertions.assertEquals("output2",viewRoot.getChildren().get(2).getId());
        
        component = viewRoot.getChildren().remove( 0 );
        viewRoot.getChildren().add( component );

        viewRoot = saveAndRestore(stateManagement, viewRoot);

        Assertions.assertEquals("output1",viewRoot.getChildren().get(0).getId());
        Assertions.assertEquals("output2",viewRoot.getChildren().get(1).getId());
        Assertions.assertEquals("form1",viewRoot.getChildren().get(2).getId());

        Assertions.assertEquals("somevalue", viewRoot.getAttributes().get("somekey"));
        Assertions.assertNotNull(viewRoot.findComponent("form1"));
        Assertions.assertNotNull(viewRoot.findComponent("form1:foo1"));
        Assertions.assertNotNull(viewRoot.findComponent("form1:var1"));
        Assertions.assertNotNull(viewRoot.findComponent("form1:button1"));
        
        a = (HtmlOutputText) viewRoot.findComponent("output1");
        Assertions.assertNotNull(a);
        Assertions.assertEquals("testOutput1",a.getValue());
    }
    
    public UIViewRoot saveAndRestore(StateManagementStrategy stateManagement, UIViewRoot viewRoot)
    {
        facesContext.getAttributes().remove(StateManagerImpl.SERIALIZED_VIEW_REQUEST_ATTR);        
        Object state1 = stateManagement.saveView(facesContext);
        stateToRestore = state1;
        facesContext.setViewRoot(null);
        stateManagement.restoreView(facesContext, "/root", viewRoot.getRenderKitId());
        return facesContext.getViewRoot();
    }
    
    public static class MockViewDeclarationLanguage extends ViewDeclarationLanguage
    {

        @Override
        public void buildView(FacesContext context, UIViewRoot viewRoot)
                throws IOException
        {            
            HtmlOutputText output = new HtmlOutputText();
            output.setValue("foo");
            output.setId("foo1");

            HtmlInputText input = new HtmlInputText();
            input.setValue("var");
            input.setId("var1");
            
            HtmlCommandButton button = new HtmlCommandButton();
            button.setId("button1");
            
            UIForm form = new HtmlForm();
            form.setId("form1");
            
            form.getChildren().add(output);
            form.getChildren().add(input);
            form.getChildren().add(button);
            viewRoot.getChildren().add(form);
            
            markInitialState(viewRoot);
        }
        
        public void markInitialState(UIComponent component)
        {
            component.markInitialState();
            
            Iterator<UIComponent> it = component.getFacetsAndChildren();
            
            while(it.hasNext())
            {
                markInitialState(it.next());
            }
        }

        @Override
        public UIViewRoot createView(FacesContext context, String viewId)
        {
            UIViewRoot viewRoot = new UIViewRoot();
            viewRoot.setViewId(viewId);
            viewRoot.setRenderKitId("HTML_BASIC2");
            context.setViewRoot(viewRoot);
            return viewRoot;
        }

        @Override
        public BeanInfo getComponentMetadata(FacesContext context,
                Resource componentResource)
        {
            return null;
        }

        @Override
        public Resource getScriptComponentResource(FacesContext context,
                Resource componentResource)
        {
            return null;
        }

        @Override
        public StateManagementStrategy getStateManagementStrategy(
                FacesContext context, String viewId)
        {
            return null;
        }

        @Override
        public ViewMetadata getViewMetadata(FacesContext context, String viewId)
        {
            return new ViewMetadataBase(viewId)
            {

                @Override
                public UIViewRoot createMetadataView(FacesContext context)
                {
                    try
                    {
                        context.setProcessingEvents(false);
                        String viewId = getViewId();
                        UIViewRoot view = createView(context, viewId);
                        return view;
                    }
                    finally
                    {
                        context.setProcessingEvents(true);
                    }
                }                
            };
        }

        @Override
        public void renderView(FacesContext context, UIViewRoot view)
                throws IOException
        {

        }

        @Override
        public UIViewRoot restoreView(FacesContext context, String viewId)
        {
            return null;
        }
        
    }    
}
