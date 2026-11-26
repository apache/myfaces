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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;

import jakarta.el.ExpressionFactory;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.view.facelets.ELExpressionCacheMode;
import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.Test;

class CompositeComponentNestedCCTestCase extends AbstractFaceletTestCase
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
    void compositeNestedCC1() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC1.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        assertTrue(sw.toString().contains("ALFA"));
        assertTrue(sw.toString().contains("BETA"));
        assertTrue(sw.toString().contains("GAMMA"));
    }

    /**
     * The same tests, but with caching enabled. In this case, ccLevel changes
     * all the time, so the caching logic must take that into account.
     * 
     * @throws Exception 
     */
    @Test
    void compositeNestedCC1Cache() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString());
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC1.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        assertTrue(sw.toString().contains("ALFA"));
        assertTrue(sw.toString().contains("BETA"));
        assertTrue(sw.toString().contains("GAMMA"));
    }

    /**
     * Try a nested reference inside the same composite component
     * 
     * @throws Exception 
     */
    @Test
    void compositeNestedCC2() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC2.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        assertTrue(sw.toString().contains("ALFA"));
        assertTrue(sw.toString().contains("BETA"));
        assertTrue(sw.toString().contains("GAMMA"));
        assertTrue(sw.toString().contains("OMEGA"));
    }

    /**
     * Try a nested reference inside the same composite component
     * 
     * @throws Exception 
     */
    @Test
    void compositeNestedCC2Cache() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString());
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC2.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        assertTrue(sw.toString().contains("ALFA"));
        assertTrue(sw.toString().contains("BETA"));
        assertTrue(sw.toString().contains("GAMMA"));
        assertTrue(sw.toString().contains("OMEGA"));
    }

    /**
     * Try what happen when a dynamic ui:include is used inside a composite
     * component and that include contains a reference to the same composite
     * component. 
     * 
     * @throws Exception 
     */
    @Test
    void compositeNestedCC3() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC3.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        assertTrue(sw.toString().contains("ALFA"));
        assertTrue(sw.toString().contains("BETA"));
        assertTrue(sw.toString().contains("GAMMA"));
        assertTrue(sw.toString().contains("OMEGA"));
    }

    /**
     * Try what happen when a dynamic ui:include is used inside a composite
     * component and that include contains a reference to the same composite
     * component. 
     * 
     * @throws Exception 
     */
    @Test
    void compositeNestedCC3Cache() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString()); //allowCset
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC3.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        assertTrue(sw.toString().contains("ALFA"));
        assertTrue(sw.toString().contains("BETA"));
        assertTrue(sw.toString().contains("GAMMA"));
        assertTrue(sw.toString().contains("OMEGA"));
    }

    /**
     * Try a nested insertChildren with some conditional inclusions.
     * 
     * @throws Exception 
     */
    @Test
    void compositeNestedCC4() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC4.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        assertTrue(sw.toString().contains("ALFA"));
        assertTrue(sw.toString().contains("BETA"));
        assertTrue(sw.toString().contains("GAMMA"));
        assertTrue(sw.toString().contains("OMEGA"));
    }

    /**
     * Try a nested insertChildren with some conditional inclusions.
     * 
     * @throws Exception 
     */
    @Test
    void compositeNestedCC4Cache() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString());

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC4.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        assertTrue(sw.toString().contains("ALFA"));
        assertTrue(sw.toString().contains("BETA"));
        assertTrue(sw.toString().contains("GAMMA"));
        assertTrue(sw.toString().contains("OMEGA"));
    }

    @Test
    void compositeNestedCC5() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC5.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        assertTrue(sw.toString().contains("ALFA"));
        assertTrue(sw.toString().contains("BETA"));
        assertTrue(sw.toString().contains("GAMMA"));
    }

    /**
     * The same tests, but with caching enabled. In this case, ccLevel changes
     * all the time, so the caching logic must take that into account.
     * 
     * @throws Exception 
     */
    @Test
    void compositeNestedCC5Cache() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString());
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC5.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        assertTrue(sw.toString().contains("ALFA"));
        assertTrue(sw.toString().contains("BETA"));
        assertTrue(sw.toString().contains("GAMMA"));
    }

    /**
     * Try a nested reference inside the same composite component
     * 
     * @throws Exception 
     */
    @Test
    void compositeNestedCC6() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC6.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        assertTrue(sw.toString().contains("ALFA"));
        assertTrue(sw.toString().contains("BETA"));
        assertTrue(sw.toString().contains("GAMMA"));
        assertTrue(sw.toString().contains("OMEGA"));
    }

    /**
     * Try a nested reference inside the same composite component
     * 
     * @throws Exception 
     */
    @Test
    void compositeNestedCC6Cache() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString());
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC6.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        assertTrue(sw.toString().contains("ALFA"));
        assertTrue(sw.toString().contains("BETA"));
        assertTrue(sw.toString().contains("GAMMA"));
        assertTrue(sw.toString().contains("OMEGA"));
    }

    /**
     * Try what happen when a dynamic ui:include is used inside a composite
     * component and that include contains a reference to the same composite
     * component. 
     * 
     * @throws Exception 
     */
    @Test
    void compositeNestedCC7() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC7.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        assertTrue(sw.toString().contains("ALFA"));
        assertTrue(sw.toString().contains("BETA"));
        assertTrue(sw.toString().contains("GAMMA"));
        assertTrue(sw.toString().contains("OMEGA"));
    }

    /**
     * Try what happen when a dynamic ui:include is used inside a composite
     * component and that include contains a reference to the same composite
     * component. 
     * 
     * @throws Exception 
     */
    @Test
    void compositeNestedCC7Cache() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString()); //allowCset
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC7.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        assertTrue(sw.toString().contains("ALFA"));
        assertTrue(sw.toString().contains("BETA"));
        assertTrue(sw.toString().contains("GAMMA"));
        assertTrue(sw.toString().contains("OMEGA"));
    }

    /**
     * Try a nested insertChildren with some conditional inclusions.
     * 
     * @throws Exception 
     */
    @Test
    void compositeNestedCC8() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC8.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        assertTrue(sw.toString().contains("ALFA"));
        assertTrue(sw.toString().contains("BETA"));
        assertTrue(sw.toString().contains("GAMMA"));
        assertTrue(sw.toString().contains("OMEGA"));
    }

    /**
     * Try a nested insertChildren with some conditional inclusions.
     * 
     * @throws Exception 
     */
    @Test
    void compositeNestedCC8Cache() throws Exception
    {
        servletContext.addInitParameter(MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString());

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeNestedCC8.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        assertTrue(sw.toString().contains("ALFA"));
        assertTrue(sw.toString().contains("BETA"));
        assertTrue(sw.toString().contains("GAMMA"));
        assertTrue(sw.toString().contains("OMEGA"));
    }
}