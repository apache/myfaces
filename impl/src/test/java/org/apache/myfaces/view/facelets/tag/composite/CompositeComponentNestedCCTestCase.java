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

package org.apache.myfaces.view.facelets.tag.composite;

import java.io.StringWriter;

import jakarta.el.ExpressionFactory;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.view.facelets.ELExpressionCacheMode;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.Assert;
import org.junit.Test;

public class CompositeComponentNestedCCTestCase extends FaceletTestCase
{

    @Override
    protected void setupComponents() throws Exception
    {
        super.setupComponents();
    }
    
    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }
    
    @Test
    public void testCompositeNestedCC1() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC1.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
    }
    
    /**
     * The same tests, but with caching enabled. In this case, ccLevel changes
     * all the time, so the caching logic must take that into account.
     * 
     * @throws Exception 
     */
    @Test
    public void testCompositeNestedCC1Cache() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString());
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC1.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
    }
    
    /**
     * Try a nested reference inside the same composite component
     * 
     * @throws Exception 
     */
    @Test
    public void testCompositeNestedCC2() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC2.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
        Assert.assertTrue(sw.toString().contains("OMEGA"));
    }

    /**
     * Try a nested reference inside the same composite component
     * 
     * @throws Exception 
     */
    @Test
    public void testCompositeNestedCC2Cache() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString());
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC2.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
        Assert.assertTrue(sw.toString().contains("OMEGA"));
    }
    
    /**
     * Try what happen when a dynamic ui:include is used inside a composite
     * component and that include contains a reference to the same composite
     * component. 
     * 
     * @throws Exception 
     */
    @Test
    public void testCompositeNestedCC3() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC3.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
        Assert.assertTrue(sw.toString().contains("OMEGA"));
    }
    
    /**
     * Try what happen when a dynamic ui:include is used inside a composite
     * component and that include contains a reference to the same composite
     * component. 
     * 
     * @throws Exception 
     */
    @Test
    public void testCompositeNestedCC3Cache() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString()); //allowCset
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC3.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
        Assert.assertTrue(sw.toString().contains("OMEGA"));
    }
    
    /**
     * Try a nested insertChildren with some conditional inclusions.
     * 
     * @throws Exception 
     */
    @Test
    public void testCompositeNestedCC4() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC4.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
        Assert.assertTrue(sw.toString().contains("OMEGA"));
    }
    
    /**
     * Try a nested insertChildren with some conditional inclusions.
     * 
     * @throws Exception 
     */
    @Test
    public void testCompositeNestedCC4Cache() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString());

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC4.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
        Assert.assertTrue(sw.toString().contains("OMEGA"));
    }

    @Test
    public void testCompositeNestedCC5() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC5.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
    }
    
    /**
     * The same tests, but with caching enabled. In this case, ccLevel changes
     * all the time, so the caching logic must take that into account.
     * 
     * @throws Exception 
     */
    @Test
    public void testCompositeNestedCC5Cache() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString());
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC5.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
    }
    
    /**
     * Try a nested reference inside the same composite component
     * 
     * @throws Exception 
     */
    @Test
    public void testCompositeNestedCC6() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC6.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
        Assert.assertTrue(sw.toString().contains("OMEGA"));
    }

    /**
     * Try a nested reference inside the same composite component
     * 
     * @throws Exception 
     */
    @Test
    public void testCompositeNestedCC6Cache() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString());
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC6.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
        Assert.assertTrue(sw.toString().contains("OMEGA"));
    }
    
    /**
     * Try what happen when a dynamic ui:include is used inside a composite
     * component and that include contains a reference to the same composite
     * component. 
     * 
     * @throws Exception 
     */
    @Test
    public void testCompositeNestedCC7() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC7.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
        Assert.assertTrue(sw.toString().contains("OMEGA"));
    }
    
    /**
     * Try what happen when a dynamic ui:include is used inside a composite
     * component and that include contains a reference to the same composite
     * component. 
     * 
     * @throws Exception 
     */
    @Test
    public void testCompositeNestedCC7Cache() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString()); //allowCset
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC7.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
        Assert.assertTrue(sw.toString().contains("OMEGA"));
    }
    
    /**
     * Try a nested insertChildren with some conditional inclusions.
     * 
     * @throws Exception 
     */
    @Test
    public void testCompositeNestedCC8() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC8.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
        Assert.assertTrue(sw.toString().contains("OMEGA"));
    }
    
    /**
     * Try a nested insertChildren with some conditional inclusions.
     * 
     * @throws Exception 
     */
    @Test
    public void testCompositeNestedCC8Cache() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString());

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC8.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        Assert.assertTrue(sw.toString().contains("ALFA"));
        Assert.assertTrue(sw.toString().contains("BETA"));
        Assert.assertTrue(sw.toString().contains("GAMMA"));
        Assert.assertTrue(sw.toString().contains("OMEGA"));
    }
}