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

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlForm;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.ResponseStateManager;
import javax.faces.view.StateManagementStrategy;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;

import org.apache.myfaces.component.visit.VisitContextFactoryImpl;
import org.apache.myfaces.renderkit.html.HtmlButtonRenderer;
import org.apache.myfaces.renderkit.html.HtmlFormRenderer;
import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.view.ViewMetadataBase;
import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockRenderKit;

public class DefaultFaceletsStateManagementStrategyTest extends
        AbstractJsfTestCase
{

    public DefaultFaceletsStateManagementStrategyTest(String name)
    {
        super(name);
    }
    
    public Object stateToRestore;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        FactoryFinder.setFactory(FactoryFinder.VISIT_CONTEXT_FACTORY, VisitContextFactoryImpl.class.getName());
        
        RenderKitFactory renderKitFactory = (RenderKitFactory)FactoryFinder.getFactory("javax.faces.render.RenderKitFactory");
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
    protected void tearDown() throws Exception
    {
        stateToRestore = null;
        super.tearDown();
    }

    public void testSimpleSaveRestore() throws Exception
    {
        ViewDeclarationLanguage vdl = new MockViewDeclarationLanguage();
        DefaultFaceletsStateManagementStrategy stateManagement = new DefaultFaceletsStateManagementStrategy(vdl);
        
        servletContext.addInitParameter("javax.faces.STATE_SAVING_METHOD", "client");
        
        UIViewRoot viewRoot = vdl.createView(facesContext, "/root");
        vdl.buildView(facesContext, viewRoot);
        
        viewRoot.getAttributes().put("somekey", "somevalue");
        
        Object state1 = stateManagement.saveView(facesContext);
        stateToRestore = state1;
        stateManagement.restoreView(facesContext, "/root", viewRoot.getRenderKitId());
        
        viewRoot = facesContext.getViewRoot();
        
        assertEquals("somevalue", viewRoot.getAttributes().get("somekey"));
    }
    
    public void testSaveRestoreAddComponent() throws Exception
    {
        ViewDeclarationLanguage vdl = new MockViewDeclarationLanguage();
        DefaultFaceletsStateManagementStrategy stateManagement = new DefaultFaceletsStateManagementStrategy(vdl);
        
        servletContext.addInitParameter("javax.faces.STATE_SAVING_METHOD", "client");
        
        UIViewRoot viewRoot = vdl.createView(facesContext, "/root");
        vdl.buildView(facesContext, viewRoot);
        
        viewRoot.getAttributes().put("somekey", "somevalue");
        
        HtmlOutputText a = new HtmlOutputText();
        
        a.setId("output1");
        a.setValue("testOutput1");
        
        viewRoot.getChildren().add(a);
        
        //Simulate event
        PostAddToViewEvent evt = new PostAddToViewEvent(a);
        
        DefaultFaceletsStateManagementStrategy.PostAddPreRemoveFromViewListener listener =
            new DefaultFaceletsStateManagementStrategy.PostAddPreRemoveFromViewListener();
        
        listener.processEvent(evt);
        
        Object state1 = stateManagement.saveView(facesContext);
        stateToRestore = state1;
        stateManagement.restoreView(facesContext, "/root", viewRoot.getRenderKitId());
        
        viewRoot = facesContext.getViewRoot();
        
        assertEquals("somevalue", viewRoot.getAttributes().get("somekey"));
        
        a = (HtmlOutputText) viewRoot.findComponent("output1");
        assertNotNull(a);
        assertEquals("testOutput1",a.getValue());
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
