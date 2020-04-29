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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jakarta.el.ExpressionFactory;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UIViewParameter;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.view.ViewMetadata;

import org.apache.myfaces.mc.test.core.AbstractMyFacesRequestTestCase;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.apache.myfaces.view.facelets.ViewPoolProcessor;
import org.apache.myfaces.view.facelets.pool.impl.ViewPoolImpl;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;
import org.apache.myfaces.view.facelets.tag.jsf.FaceletState;
import org.junit.Assert;
import org.junit.Test;

public class ViewPoolMyFacesRequestTestCase extends AbstractMyFacesRequestTestCase
{

    @Override
    protected boolean isScanAnnotations()
    {
        return true;
    }

    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }

    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES","org.apache.myfaces.view.facelets.pool");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_CLIENT);
        servletContext.addInitParameter(StateManager.PARTIAL_STATE_SAVING_PARAM_NAME, "true");
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
        
        //servletContext.addInitParameter(ViewPoolProcessor.INIT_PARAM_VIEW_POOL_ENABLED, "true");
        servletContext.addInitParameter(ViewPoolImpl.INIT_PARAM_VIEW_POOL_ENTRY_MODE, "soft");
        servletContext.addInitParameter(ViewPoolImpl.INIT_PARAM_VIEW_POOL_MAX_POOL_SIZE, "20");
        servletContext.addInitParameter("org.apache.myfaces.CACHE_EL_EXPRESSIONS", "alwaysRecompile");
        servletContext.addInitParameter("jakarta.faces.CONFIG_FILES", "/view-pool-faces-config.xml");
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, "Production");
    }

    /**
     * Test view pool in a page with static structure and no forms. 
     * This check that once the page is rendered, the view is stored in the pool
     * 
     * @throws Exception 
     */
    @Test
    public void testStaticPageNoForm1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageNoForm.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        renderResponse();
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageNoForm.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.COMPLETE, entry.getResult());
        endRequest();
    }

    /**
     * This check the view stored into the pool is used on another request,
     * and check if the restored view does not have state from previous requests
     * 
     * @throws Exception 
     */
    @Test
    public void testStaticPageNoForm1_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageNoForm.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        endRequest();

        startViewRequest("/staticPageNoForm.xhtml");
        processLifecycleExecute();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        Assert.assertTrue(facesContext.getViewRoot().getChildCount() > 0);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageNoForm.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        // Check it was used.
        Assert.assertNull(entry);
    }
    
    @Test
    public void testStaticPageNoForm2() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageNoForm2.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        UIOutput testjs1_1 = (UIOutput) facesContext.getViewRoot().findComponent("testjs");
        Assert.assertNotNull(testjs1_1);
        testjs1_1.getAttributes().put("param1", "value1");
        
        List<UIComponent> crlist = facesContext.getViewRoot().getComponentResources(facesContext, "head");
        Assert.assertEquals(2, crlist.size());
        for(UIComponent comp : crlist)
        {
            comp.getAttributes().put("param2", "value2");
        }
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        endRequest();

        startViewRequest("/staticPageNoForm2.xhtml");
        processLifecycleExecute();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        Assert.assertTrue(facesContext.getViewRoot().getChildCount() > 0);
        
        UIOutput testjs2_1 = (UIOutput) facesContext.getViewRoot().findComponent("testjs");
        Assert.assertNotNull(testjs2_1);
        // The state of the component should not be transferred.
        Assert.assertNull(testjs2_1.getAttributes().get("param1"));
        
        List<UIComponent> crlist2 = facesContext.getViewRoot().getComponentResources(facesContext, "head");
        Assert.assertEquals(2, crlist2.size());   
        for(UIComponent comp : crlist)
        {
            Assert.assertNull(comp.getAttributes().get("param2"));
        }

        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageNoForm2.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        // Check it was used.
        Assert.assertNull(entry);
    }    

    @Test
    public void testStaticPage1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPage.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        renderResponse();
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPage.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.COMPLETE, entry.getResult());
        endRequest();
    }
    
    @Test
    public void testStaticPage1_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPage.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        // Add a component in order to trigger a dynamic update
        UIOutput testComponent = (UIOutput) application.createComponent(UIOutput.COMPONENT_TYPE);
        testComponent.setId("testId");
        testComponent.setValue("Some Text");
        UIForm form = (UIForm) facesContext.getViewRoot().findComponent("mainForm");
        form.getChildren().add(testComponent);
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPage.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.COMPLETE, entry.getResult());
        
        //Check the component was removed
        UIForm form2 = (UIForm) entry.getViewRoot().findComponent("mainForm");
        Assert.assertNotNull(form2);
        UIOutput testComponent2 = (UIOutput) form2.findComponent("testId");
        Assert.assertNull(testComponent2);
        
        endRequest();
    }
    
    @Test
    public void testStaticPage1_2() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPage.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        // Add a component in order to trigger a dynamic update
        UIOutput testComponent = (UIOutput) application.createComponent(UIOutput.COMPONENT_TYPE);
        testComponent.setId("testId");
        testComponent.setValue("Some Text");
        UIForm form = (UIForm) facesContext.getViewRoot().findComponent("mainForm");
        form.getChildren().add(testComponent);
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPage.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.COMPLETE, entry.getResult());
        
        //Check the component was removed
        UIForm form2 = (UIForm) entry.getViewRoot().findComponent("mainForm");
        Assert.assertNotNull(form2);
        UIOutput testComponent2 = (UIOutput) form2.findComponent("testId");
        Assert.assertNull(testComponent2);
        
        endRequest();
    }
    
    @Test
    public void testStaticPage1_3() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPage.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        // Now let's try to remove some component programatically
        // that invalidates the view to be reused without a refresh,
        // so in the pool it should be marked as REFRESH_REQUIRED
        UIForm form = (UIForm) facesContext.getViewRoot().findComponent("mainForm");
        form.getChildren().remove(0);
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPage.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.REFRESH_REQUIRED, entry.getResult());
        endRequest();
    }
    
    @Test
    public void testStaticPage1_4() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPage.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        // Now let's try to remove some component programatically
        // that invalidates the view to be reused without a refresh,
        // so in the pool it should be marked as REFRESH_REQUIRED
        UIForm form = (UIForm) facesContext.getViewRoot().findComponent("mainForm");
        UIComponent panel = form.getChildren().remove(0);
        // Add it again
        form.getChildren().add(panel);
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPage.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.REFRESH_REQUIRED, entry.getResult());
        endRequest();
    }
    
    @Test
    public void testStaticPage1_5() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPage.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);

        // Use view scope
        facesContext.getViewRoot().getViewMap().put("someKey", "someValue");
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);

        processLifecycleExecute();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        //Check if the view scope value is preserved
        Assert.assertEquals("someValue", facesContext.getViewRoot().getViewMap().get("someKey"));
        
        facesContext.getViewRoot().getViewMap().put("someKey", "someValue2");
        
        submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);

        processLifecycleExecute();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        //Check if the view scope value is preserved
        Assert.assertEquals("someValue2", facesContext.getViewRoot().getViewMap().get("someKey"));
        
        Assert.assertTrue(facesContext.getViewRoot().getChildCount() > 0);
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPage.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.COMPLETE, entry.getResult());
        
        endRequest();
    }    
    
    @Test
    public void testStaticPage1_6() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPage3.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        facesContext.getViewRoot().getViewMap().put("keyBeforeView", "someBeforeValue");
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);

        // Use view scope
        Assert.assertEquals("someBeforeValue", facesContext.getViewRoot().getViewMap().get("keyBeforeView"));
        facesContext.getViewRoot().getViewMap().put("someKey", "someValue");
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);

        processLifecycleExecute();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        //Check if the view scope value is preserved
        Assert.assertEquals("someValue", facesContext.getViewRoot().getViewMap().get("someKey"));
        
        facesContext.getViewRoot().getViewMap().put("someKey", "someValue2");
        
        submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);

        processLifecycleExecute();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        //Check if the view scope value is preserved
        Assert.assertEquals("someValue2", facesContext.getViewRoot().getViewMap().get("someKey"));
        
        Assert.assertTrue(facesContext.getViewRoot().getChildCount() > 0);
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPage3.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.COMPLETE, entry.getResult());
        
        endRequest();
    }    
    
    @Test
    public void testStaticPage1_7() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPage3.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);

        // Use view scope
        facesContext.getViewRoot().getViewMap().put("someKey", "someValue");
        //Assert.assertEquals("viewValue", facesContext.getViewRoot().getViewMap().get("viewKey"));
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);

        startViewRequest("/staticPage3.xhtml");
        request.addParameter("id", "someId");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        facesContext.getViewRoot().getViewMap().put("keyBeforeView", "someBeforeValue");
        
        UIViewParameter paramComponent = ViewMetadata.getViewParameters(facesContext.getViewRoot()).iterator().next();
        Assert.assertNotNull(paramComponent);
        Assert.assertEquals("someId", paramComponent.getValue());

        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);

        paramComponent = ViewMetadata.getViewParameters(facesContext.getViewRoot()).iterator().next();
        Assert.assertNotNull(paramComponent);
        Assert.assertEquals("someId", paramComponent.getValue());
        
        // Use view scope
        Assert.assertEquals("someBeforeValue", facesContext.getViewRoot().getViewMap().get("keyBeforeView"));
        facesContext.getViewRoot().getViewMap().put("someKey", "someValue");
        //Assert.assertEquals("viewValue", facesContext.getViewRoot().getViewMap().get("viewKey"));
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        endRequest();
    }        
    
    @Test
    public void testStaticPage2() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPage2.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        UIOutput testjs1_1 = (UIOutput) facesContext.getViewRoot().findComponent("testjs");
        Assert.assertNotNull(testjs1_1);
        testjs1_1.getAttributes().put("param1", "value1");
        
        List<UIComponent> crlist = facesContext.getViewRoot().getComponentResources(facesContext, "head");
        Assert.assertEquals(2, crlist.size());
        for(UIComponent comp : crlist)
        {
            comp.getAttributes().put("param2", "value2");
        }
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);

        processLifecycleExecute();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        Assert.assertTrue(facesContext.getViewRoot().getChildCount() > 0);
        
        UIOutput testjs2_1 = (UIOutput) facesContext.getViewRoot().findComponent("testjs");
        Assert.assertNotNull(testjs2_1);
        // The state of the component should not be transferred.
        Assert.assertEquals("value1",testjs2_1.getAttributes().get("param1"));
        
        List<UIComponent> crlist2 = facesContext.getViewRoot().getComponentResources(facesContext, "head");
        Assert.assertEquals(2, crlist2.size());   
        for(UIComponent comp : crlist)
        {
            Assert.assertEquals("value2", comp.getAttributes().get("param2"));
        }

        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPage2.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        // Check it was used.
        Assert.assertNull(entry);
    }
    
    @Test
    public void testStaticPageWithViewScope1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPage.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBuildViewCycle(facesContext);
        facesContext.getViewRoot().getViewMap(true).put("viewItem", "someValue");
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        endRequest();
        
        startViewRequest("/staticPage.xhtml");
        processLifecycleExecute();

        executeBuildViewCycle(facesContext);
        
        // Check the ViewMap value is not passed to the view
        Assert.assertNull(facesContext.getViewRoot().getViewMap(false));
        
        // Check the view was used
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPage.xhtml");        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
    }
    
    @Test
    public void testStaticPageWithViewScope1_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPage.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBuildViewCycle(facesContext);
        facesContext.getViewRoot().getViewMap(true).put("viewItem", "someValue");
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPage.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.COMPLETE, entry.getResult());
        endRequest();
    }
    
    @Test
    public void testStaticPageWithViewScope2() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPage.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        facesContext.getViewRoot().getViewMap(true).put("viewItem", "someValue");

        executeBuildViewCycle(facesContext);
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");

        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPage.xhtml");
        
        client.submit(submitButton);
        
        processLifecycleExecute();

        // Check the ViewMap is passed to the view
        Assert.assertNotNull(facesContext.getViewRoot().getViewMap(false));
        Assert.assertEquals("someValue", facesContext.getViewRoot().getViewMap(false).get("viewItem"));
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        // Check the view was used
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
    }

    @Test
    public void testStaticUIParamPage1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticUIParamPage1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        renderResponse();
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticUIParamPage1.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.COMPLETE, entry.getResult());
        endRequest();
    }
    
    @Test
    public void testStaticUIParamPage1_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticUIParamPage1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBuildViewCycle(facesContext);
        
        UIOutput outParam1_1 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:paramOut_1");
        Assert.assertNotNull(outParam1_1);
        Assert.assertEquals("hello_1", outParam1_1.getValue());

        UIOutput outParam1_2 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:paramOut_2");
        Assert.assertNotNull(outParam1_2);
        Assert.assertEquals("hello_2", outParam1_2.getValue());
        
        executeViewHandlerRender(facesContext);
        
        //MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        //String content1 = new String(writer1.content());
        
        executeAfterRender(facesContext);
        
        endRequest();
        
        startViewRequest("/staticUIParamPage1.xhtml");
        processLifecycleExecute();

        executeBuildViewCycle(facesContext);
        
        UIOutput outParam2_1 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:paramOut_1");
        Assert.assertNotNull(outParam2_1);
        Assert.assertEquals("hello_1", outParam2_1.getValue());

        UIOutput outParam2_2 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:paramOut_2");
        Assert.assertNotNull(outParam2_2);
        Assert.assertEquals("hello_2", outParam2_2.getValue());        
        
        // Check the view was used
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPage.xhtml");        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
        
        executeViewHandlerRender(facesContext);
        
        //MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        //String content2 = new String(writer2.content());
        
        executeAfterRender(facesContext);
    }
        
    @Test
    public void testStaticViewParamPage1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticViewParamPage1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        renderResponse();
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticViewParamPage1.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.COMPLETE, entry.getResult());
        endRequest();
    }
    
    @Test
    public void testStaticViewParamPage1_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticViewParamPage1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBuildViewCycle(facesContext);
        
        UIOutput testId1_1 = (UIOutput) facesContext.getViewRoot().findComponent("testId");
        Assert.assertNotNull(testId1_1);
        testId1_1.getAttributes().put("param1", "value1");
        
        executeViewHandlerRender(facesContext);
        
        executeAfterRender(facesContext);
        
        endRequest();
        
        startViewRequest("/staticViewParamPage1.xhtml");
        processLifecycleExecute();

        executeBuildViewCycle(facesContext);
        
        UIOutput testId2_1 = (UIOutput) facesContext.getViewRoot().findComponent("testId");
        Assert.assertNotNull(testId2_1);
        Assert.assertNull(testId2_1.getAttributes().get("param1"));
        
        // Check the view was used
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticViewParamPage1.xhtml");        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
        
        executeViewHandlerRender(facesContext);
        
        executeAfterRender(facesContext);
    }

    @Test
    public void testStaticViewParamPage1_2() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticViewParamPage1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBuildViewCycle(facesContext);
        
        UIOutput testId1_1 = (UIOutput) facesContext.getViewRoot().findComponent("testId");
        Assert.assertNotNull(testId1_1);
        // With the line below, we put something into the state. The idea is everything inside
        // metadata facet should not be taken into account by the view pool
        testId1_1.getAttributes().put("param1", "value1");
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        
        executeViewHandlerRender(facesContext);
        
        executeAfterRender(facesContext);

        client.submit(submitButton);
        
        processLifecycleExecute();

        UIOutput testId2_1 = (UIOutput) facesContext.getViewRoot().findComponent("testId");
        Assert.assertNotNull(testId2_1);
        // Check the state is correctly restored. 
        Assert.assertEquals("value1", testId2_1.getAttributes().get("param1"));
        
        executeBuildViewCycle(facesContext);
        
        UIOutput testId3_1 = (UIOutput) facesContext.getViewRoot().findComponent("testId");
        Assert.assertNotNull(testId3_1);
        // Check the state is correctly restored. 
        Assert.assertEquals("value1", testId3_1.getAttributes().get("param1"));
        
        // Check the view was used
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticViewParamPage1.xhtml");        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
        
        executeViewHandlerRender(facesContext);
        
        executeAfterRender(facesContext);
    }
    
    @Test
    public void testDynamicPage1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/dynPage1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        FaceletState faceletState = (FaceletState) facesContext.getViewRoot().getAttributes().get(
            ComponentSupport.FACELET_STATE_INSTANCE);
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/dynPage1.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popDynamicStructureView(facesContext, root, faceletState);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.COMPLETE, entry.getResult());
        endRequest();
    }
    
    @Test
    public void testDynamicPage1_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/dynPage1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        // Add a component in order to trigger a dynamic update
        UIOutput testComponent = (UIOutput) application.createComponent(UIOutput.COMPONENT_TYPE);
        testComponent.setId("testId");
        testComponent.setValue("Some Text");
        UIForm form = (UIForm) facesContext.getViewRoot().findComponent("mainForm");
        form.getChildren().add(testComponent);
        
        FaceletState faceletState = (FaceletState) facesContext.getViewRoot().getAttributes().get(
            ComponentSupport.FACELET_STATE_INSTANCE);
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/dynPage1.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popDynamicStructureView(facesContext, root, faceletState);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.COMPLETE, entry.getResult());
        
        //Check the component was removed
        UIForm form2 = (UIForm) entry.getViewRoot().findComponent("mainForm");
        Assert.assertNotNull(form2);
        UIOutput testComponent2 = (UIOutput) form2.findComponent("testId");
        Assert.assertNull(testComponent2);
        
        endRequest();
    }

    @Test
    public void testDynamicPage1_2() throws Exception
    {
        Locale locale = null;
        startViewRequest("/dynPage1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        facesContext.getViewRoot().getViewMap(true).put("viewItem", "someValue");

        executeBuildViewCycle(facesContext);
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");

        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);
        
        processLifecycleExecute();
        
        // Check the ViewMap is passed to the view
        Assert.assertNotNull(facesContext.getViewRoot().getViewMap(false));
        Assert.assertEquals("someValue", facesContext.getViewRoot().getViewMap(false).get("viewItem"));
        
        FaceletState faceletState = (FaceletState) facesContext.getViewRoot().getAttributes().get(
            ComponentSupport.FACELET_STATE_INSTANCE);        
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/dynPage1.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        // Check the view was used
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
        ViewEntry entry3 = viewPool.popDynamicStructureView(facesContext, root, faceletState);
        Assert.assertNull(entry3);
    }
        
    @Test
    public void testStaticPageLocale1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageLocale1.xhtml");
        processLifecycleExecute();
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        locale = facesContext.getViewRoot().getLocale();
        Assert.assertEquals(Locale.US, locale);
        
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageLocale1.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.COMPLETE, entry.getResult());
        endRequest();
    }
    
    @Test
    public void testStaticPageLocale1_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageLocale1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        Assert.assertEquals(Locale.US, locale);
        
        executeBuildViewCycle(facesContext);
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        endRequest();
        
        startViewRequest("/staticPageLocale1.xhtml");
        processLifecycleExecute();

        executeBuildViewCycle(facesContext);
        
        // Check the view was used
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageLocale1.xhtml");        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
    }
    
    /**
     * Check if the locale changes, the view IS NOT reused.
     * 
     * @throws Exception 
     */
    @Test
    public void testStaticPageLocale1_2() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageLocale1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        Assert.assertEquals(Locale.US, locale);
        
        executeBuildViewCycle(facesContext);
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        DynamicBean bean = facesContext.getApplication().evaluateExpressionGet(
            facesContext, "#{dynamicBean}", DynamicBean.class);
        bean.setLocale(Locale.UK);
        
        endRequest();
        
        startViewRequest("/staticPageLocale1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        Assert.assertEquals(Locale.UK, locale);
        executeBuildViewCycle(facesContext);
        
        // Check the view with Locale.US was not used
        UIViewRoot root = new UIViewRoot();
        root.setLocale(Locale.US);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageLocale1.xhtml");        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry2);
    }

    @Test
    public void testStaticPageContract1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageContract1.xhtml");
        processLifecycleExecute();
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        locale = facesContext.getViewRoot().getLocale();
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageContract1.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.COMPLETE, entry.getResult());
        endRequest();
    }
    
    @Test
    public void testStaticPageContract1_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageContract1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBuildViewCycle(facesContext);
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        endRequest();
        
        startViewRequest("/staticPageContract1.xhtml");
        processLifecycleExecute();

        executeBuildViewCycle(facesContext);
        
        // Check the view was used
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageContract1.xhtml");        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
    }
    
    /**
     * Check if the locale changes, the view IS NOT reused.
     * 
     * @throws Exception 
     */
    @Test
    public void testStaticPageContract1_2() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageContract1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBuildViewCycle(facesContext);
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        DynamicBean bean = facesContext.getApplication().evaluateExpressionGet(
            facesContext, "#{dynamicBean}", DynamicBean.class);
        bean.setContract("yellow");
        
        endRequest();
        
        startViewRequest("/staticPageContract1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        executeBuildViewCycle(facesContext);
        
        // Check the view with blue contract was not used
        List<String> contracts = new ArrayList<String>();
        contracts.add("blue");
        facesContext.setResourceLibraryContracts(contracts);
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageContract1.xhtml");        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry2);
    }
    
    @Test
    public void testDynPageResource1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/dynPageResource.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        FaceletState faceletState = (FaceletState) facesContext.getViewRoot().getAttributes().get(
            ComponentSupport.FACELET_STATE_INSTANCE);

        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/dynPageResource.xhtml");        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry1 = viewPool.popDynamicStructureView(facesContext, root, faceletState);
        Assert.assertNotNull(entry1);
    }
    
    @Test
    public void testDynPageResource1_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/dynPageResource.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        UIOutput testjs1_1 = (UIOutput) facesContext.getViewRoot().findComponent("testjs");
        Assert.assertNotNull(testjs1_1);
        testjs1_1.getAttributes().put("param1", "value1");
        
        List<UIComponent> crlist = facesContext.getViewRoot().getComponentResources(facesContext, "head");
        Assert.assertEquals(2, crlist.size());
        for(UIComponent comp : crlist)
        {
            comp.getAttributes().put("param2", "value2");
        }
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);

        processLifecycleExecute();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        Assert.assertTrue(facesContext.getViewRoot().getChildCount() > 0);
        
        UIOutput testjs2_1 = (UIOutput) facesContext.getViewRoot().findComponent("testjs");
        Assert.assertNotNull(testjs2_1);
        // The state of the component should not be transferred.
        Assert.assertEquals("value1",testjs2_1.getAttributes().get("param1"));
        
        List<UIComponent> crlist2 = facesContext.getViewRoot().getComponentResources(facesContext, "head");
        Assert.assertEquals(2, crlist2.size());   
        for(UIComponent comp : crlist)
        {
            Assert.assertEquals("value2", comp.getAttributes().get("param2"));
        }

        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/dynPageResource.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        // Check it was used.
        Assert.assertNull(entry);
    }
    
    @Test
    public void testDynPageResource2() throws Exception
    {
        Locale locale = null;
        
        startViewRequest("/dynPageResource.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);

        DynamicBean bean = facesContext.getApplication().evaluateExpressionGet(
            facesContext, "#{dynamicBean}", DynamicBean.class);
        bean.setResource1(true);
        
        endRequest();
        
        startViewRequest("/dynPageResource.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        UIOutput testjs1_1 = (UIOutput) facesContext.getViewRoot().findComponent("testjs");
        Assert.assertNotNull(testjs1_1);
        testjs1_1.getAttributes().put("param1", "value1");
        
        List<UIComponent> crlist = facesContext.getViewRoot().getComponentResources(facesContext, "head");
        Assert.assertEquals(2, crlist.size());
        for(UIComponent comp : crlist)
        {
            comp.getAttributes().put("param2", "value2");
        }
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        
        bean.setResource1(false);
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);

        processLifecycleExecute();
        
        UIOutput testjs2_1 = (UIOutput) facesContext.getViewRoot().findComponent("testjs");
        Assert.assertNotNull(testjs2_1);
        // The state of the component should not be transferred.
        Assert.assertEquals("value1",testjs2_1.getAttributes().get("param1"));
        
        List<UIComponent> crlist2 = facesContext.getViewRoot().getComponentResources(facesContext, "head");
        Assert.assertEquals(2, crlist2.size());   
        
        /* Resource dependency components do not preserve state
        for(UIComponent comp : crlist)
        {
            Assert.assertEquals("value2", comp.getAttributes().get("param2"));
        } */       
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);

        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/dynPageResource.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        // Check it was used.
        Assert.assertNull(entry);
    }
    
    @Test
    public void testStaticPageBinding1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageBinding1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);

        UIPanel panel1 = (UIPanel) facesContext.getViewRoot().findComponent("mainForm:panel1");
        Assert.assertNotNull(panel1);
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageBinding1.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.COMPLETE, entry.getResult());
        endRequest();
    }
    
    @Test
    public void testStaticPageBinding1_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageBinding1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();

        executeBuildViewCycle(facesContext);
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");

        UIPanel panel1 = (UIPanel) facesContext.getViewRoot().findComponent("mainForm:panel1");
        Assert.assertNotNull(panel1);
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);
        
        processLifecycleExecute();
        
        UIPanel panel2_1 = (UIPanel) facesContext.getViewRoot().findComponent("mainForm:panel1");
        Assert.assertNotNull(panel2_1);
        
        FaceletState faceletState = (FaceletState) facesContext.getViewRoot().getAttributes().get(
            ComponentSupport.FACELET_STATE_INSTANCE);        
                
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageBinding1.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        // Check the view was used
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
        ViewEntry entry3 = viewPool.popDynamicStructureView(facesContext, root, faceletState);
        Assert.assertNull(entry3);
        
    }
    
    @Test
    public void testStaticPageBinding2() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageBinding2.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);

        UIPanel panel1_2 = (UIPanel) facesContext.getViewRoot().findComponent("mainForm:panel2");
        Assert.assertNotNull(panel1_2);
        Assert.assertEquals(1, panel1_2.getChildCount());
        Assert.assertEquals("added component through binding", 
            panel1_2.getChildren().get(0).getAttributes().get("value"));
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageBinding2.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.REFRESH_REQUIRED, entry.getResult());
        endRequest();
    }
    
    @Test
    public void testStaticPageBinding2_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageBinding2.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();

        executeBuildViewCycle(facesContext);
        
        UIPanel panel1_2 = (UIPanel) facesContext.getViewRoot().findComponent("mainForm:panel2");
        Assert.assertNotNull(panel1_2);
        Assert.assertEquals(1, panel1_2.getChildCount());
        Assert.assertEquals("added component through binding", 
            panel1_2.getChildren().get(0).getAttributes().get("value"));
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");

        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);
        
        processLifecycleExecute();
        
        UIPanel panel2_2 = (UIPanel) facesContext.getViewRoot().findComponent("mainForm:panel2");
        Assert.assertNotNull(panel2_2);
        Assert.assertEquals(1, panel2_2.getChildCount());
        Assert.assertEquals("added component through binding", 
            panel2_2.getChildren().get(0).getAttributes().get("value"));

        FaceletState faceletState = (FaceletState) facesContext.getViewRoot().getAttributes().get(
            ComponentSupport.FACELET_STATE_INSTANCE);        
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageBinding2.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        // Check the view was used
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
        ViewEntry entry3 = viewPool.popDynamicStructureView(facesContext, root, faceletState);
        Assert.assertNull(entry3);
    }
    
    @Test
    public void testStaticPageBinding3() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageBinding3.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);

        UIPanel panel1_3 = (UIPanel) facesContext.getViewRoot().findComponent("mainForm:panel3");
        Assert.assertNotNull(panel1_3);
        Assert.assertEquals(1, panel1_3.getChildCount());
        Assert.assertEquals("added component through binding", 
            panel1_3.getChildren().get(0).getAttributes().get("value"));
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageBinding3.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.REFRESH_REQUIRED, entry.getResult());
        endRequest();
    }

    @Test
    public void testStaticPageBinding3_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageBinding3.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();

        executeBuildViewCycle(facesContext);
        
        UIPanel panel1_3 = (UIPanel) facesContext.getViewRoot().findComponent("mainForm:panel3");
        Assert.assertNotNull(panel1_3);
        Assert.assertEquals(1, panel1_3.getChildCount());
        Assert.assertEquals("added component through binding", 
            panel1_3.getChildren().get(0).getAttributes().get("value"));
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");

        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);
        
        processLifecycleExecute();
        
        UIPanel panel2_3 = (UIPanel) facesContext.getViewRoot().findComponent("mainForm:panel3");
        Assert.assertNotNull(panel2_3);
        Assert.assertEquals(1, panel2_3.getChildCount());
        Assert.assertEquals("added component through binding", 
            panel2_3.getChildren().get(0).getAttributes().get("value"));

        FaceletState faceletState = (FaceletState) facesContext.getViewRoot().getAttributes().get(
            ComponentSupport.FACELET_STATE_INSTANCE);        
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageBinding3.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        // Check the view was used
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
        ViewEntry entry3 = viewPool.popDynamicStructureView(facesContext, root, faceletState);
        Assert.assertNull(entry3);
    }
    
    @Test
    public void testStaticPageBinding4() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageBinding4.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);

        UIPanel panel1_4 = (UIPanel) facesContext.getViewRoot().findComponent("mainForm:panel4");
        Assert.assertNotNull(panel1_4);
        Assert.assertEquals(1, panel1_4.getChildCount());
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageBinding4.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.REFRESH_REQUIRED, entry.getResult());
        endRequest();
    }
    
    @Test
    public void testStaticPageBinding4_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageBinding4.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();

        executeBuildViewCycle(facesContext);
        
        UIPanel panel1_4 = (UIPanel) facesContext.getViewRoot().findComponent("mainForm:panel4");
        Assert.assertNotNull(panel1_4);
        Assert.assertEquals(1, panel1_4.getChildCount());
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");

        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);
        
        processLifecycleExecute();
        
        UIPanel panel2_4 = (UIPanel) facesContext.getViewRoot().findComponent("mainForm:panel4");
        Assert.assertNotNull(panel2_4);
        Assert.assertEquals(1, panel2_4.getChildCount());

        FaceletState faceletState = (FaceletState) facesContext.getViewRoot().getAttributes().get(
            ComponentSupport.FACELET_STATE_INSTANCE);        
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageBinding4.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        // Check the view was used
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
        ViewEntry entry3 = viewPool.popDynamicStructureView(facesContext, root, faceletState);
        Assert.assertNull(entry3);
    }
    
    @Test
    public void testStaticPageBindingValidator1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageBindingValidator1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);

        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageBindingValidator1.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.COMPLETE, entry.getResult());
        endRequest();
    }
    
    @Test
    public void testStaticPageBindingValidator1_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageBindingValidator1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();

        executeBuildViewCycle(facesContext);
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");

        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);
        
        processLifecycleExecute();
        
        FaceletState faceletState = (FaceletState) facesContext.getViewRoot().getAttributes().get(
            ComponentSupport.FACELET_STATE_INSTANCE);        
                
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageBindingValidator1.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        // Check the view was used
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
        ViewEntry entry3 = viewPool.popDynamicStructureView(facesContext, root, faceletState);
        Assert.assertNull(entry3);
        
    }
    
    /**
     * The existence of a converter that implements StateHolder makes the view
     * not reseteable. This means the view is considered partial and force 
     * a refresh over the view when is taken from the pool.
     * 
     * @throws Exception T
     */
    @Test
    public void testStaticPageStateHolderConverter1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageStateHolderConverter1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);

        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageStateHolderConverter1.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.REFRESH_REQUIRED, entry.getResult());
        endRequest();
    }
    
    @Test
    public void testStaticPageStateHolderConverter1_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageStateHolderConverter1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();

        executeBuildViewCycle(facesContext);
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");

        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);
        
        processLifecycleExecute();
        
        FaceletState faceletState = (FaceletState) facesContext.getViewRoot().getAttributes().get(
            ComponentSupport.FACELET_STATE_INSTANCE);        
                
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageStateHolderConverter1.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        // Check the view was used
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
        ViewEntry entry3 = viewPool.popDynamicStructureView(facesContext, root, faceletState);
        Assert.assertNull(entry3);
        
    }
    
    /**
     * The existence of a converter that implements StateHolder makes the view
     * not reseteable. This means the view is considered partial and force 
     * a refresh over the view when is taken from the pool.
     * 
     * @throws Exception T
     */
    @Test
    public void testStaticPageStateHolderConverter2() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageStateHolderConverter2.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);

        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageStateHolderConverter2.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.REFRESH_REQUIRED, entry.getResult());
        endRequest();
    }
    
    @Test
    public void testStaticPageStateHolderConverter2_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/staticPageStateHolderConverter2.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();

        executeBuildViewCycle(facesContext);
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");

        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);
        
        processLifecycleExecute();
        
        FaceletState faceletState = (FaceletState) facesContext.getViewRoot().getAttributes().get(
            ComponentSupport.FACELET_STATE_INSTANCE);        
                
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/staticPageStateHolderConverter2.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        // Check the view was used
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
        ViewEntry entry3 = viewPool.popDynamicStructureView(facesContext, root, faceletState);
        Assert.assertNull(entry3);
        
    }
    
    @Test
    public void testPartialPage1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/partialPage1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        // Now let's try to remove some component programatically
        // that invalidates the view to be reused without a refresh,
        // so in the pool it should be marked as REFRESH_REQUIRED
        UIPanel panel = (UIPanel) facesContext.getViewRoot().findComponent("mainForm:panel1");
        
        panel.getParent().getChildren().remove(panel);
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/partialPage1.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        ViewEntry entry = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNotNull(entry);
        Assert.assertEquals(RestoreViewFromPoolResult.REFRESH_REQUIRED, entry.getResult());
        endRequest();
    }
    
    @Test
    public void testPartialPage1_1() throws Exception
    {
        Locale locale = null;
        startViewRequest("/partialPage1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();

        executeBuildViewCycle(facesContext);
        
        // Now let's try to remove some component programatically
        // that invalidates the view to be reused without a refresh,
        // so in the pool it should be marked as REFRESH_REQUIRED
        UIPanel panel = (UIPanel) facesContext.getViewRoot().findComponent("mainForm:panel1");
        panel.getParent().getChildren().remove(panel);

        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");

        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);
        
        processLifecycleExecute();
        
        FaceletState faceletState = (FaceletState) facesContext.getViewRoot().getAttributes().get(
            ComponentSupport.FACELET_STATE_INSTANCE);        
                
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/partialPage1.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        // Check the view was used
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
        ViewEntry entry3 = viewPool.popDynamicStructureView(facesContext, root, faceletState);
        Assert.assertNull(entry3);
        
    }
    
    @Test
    public void testPartialPage1_2() throws Exception
    {
        Locale locale = null;
        startViewRequest("/partialPage1.xhtml");
        processLifecycleExecute();
        locale = facesContext.getViewRoot().getLocale();

        executeBuildViewCycle(facesContext);
        
        // Now let's try to remove some component programatically
        // that invalidates the view to be reused without a refresh,
        // so in the pool it should be marked as REFRESH_REQUIRED
        UIPanel panel = (UIPanel) facesContext.getViewRoot().findComponent("mainForm:panel1");
        panel.getParent().getChildren().remove(panel);

        facesContext.getViewRoot().getViewMap().put("someKey", "someValue");
        
        UICommand submitButton = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");

        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        client.submit(submitButton);
        
        processLifecycleExecute();
        
        Assert.assertEquals("someValue", facesContext.getViewRoot().getViewMap().get("someKey"));
        
        FaceletState faceletState = (FaceletState) facesContext.getViewRoot().getAttributes().get(
            ComponentSupport.FACELET_STATE_INSTANCE);        
                
        UIViewRoot root = new UIViewRoot();
        root.setLocale(locale);
        root.setRenderKitId("HTML_BASIC");
        root.setViewId("/partialPage1.xhtml");
        
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool viewPool = processor.getViewPool(facesContext, root);
        // Check the view was used
        ViewEntry entry2 = viewPool.popStaticOrPartialStructureView(facesContext, root);
        Assert.assertNull(entry2);
        ViewEntry entry3 = viewPool.popDynamicStructureView(facesContext, root, faceletState);
        Assert.assertNull(entry3);
        
    }    
    
    
    //Pending tests:
    // - Partial
}
