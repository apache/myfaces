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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import jakarta.el.ExpressionFactory;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlCommandButton;
import jakarta.faces.component.html.HtmlForm;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.renderkit.html.HtmlButtonRenderer;
import org.apache.myfaces.renderkit.html.HtmlFormRenderer;
import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.view.facelets.ELExpressionCacheMode;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.bean.Employee;
import org.apache.myfaces.view.facelets.impl.FaceletCompositionContextImpl;
import org.junit.Assert;
import org.junit.Test;

public final class JstlCoreTestCase extends FaceletTestCase {

    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }
    
    @Override
    protected void setupComponents() throws Exception
    {
        application.addComponent(UIViewRoot.COMPONENT_TYPE,
                UIViewRoot.class.getName());
        application.addComponent(HtmlForm.COMPONENT_TYPE,
                HtmlForm.class.getName());
        application.addComponent(HtmlCommandButton.COMPONENT_TYPE,
                HtmlCommandButton.class.getName());
        application.addComponent(HtmlInputText.COMPONENT_TYPE,
                HtmlInputText.class.getName());
        application.addComponent(HtmlOutputText.COMPONENT_TYPE,
                HtmlOutputText.class.getName());
        
    }

    @Override
    protected void setupConvertersAndValidators() throws Exception
    {
    }

    @Override
    protected void setupRenderers() throws Exception
    {
        renderKit.addRenderer(UIForm.COMPONENT_FAMILY,
                "jakarta.faces.Form", new HtmlFormRenderer());
        renderKit.addRenderer(HtmlCommandButton.COMPONENT_FAMILY,
                "jakarta.faces.Button", new HtmlButtonRenderer());
        renderKit.addRenderer(HtmlInputText.COMPONENT_FAMILY,
                "jakarta.faces.Text", new HtmlTextRenderer());
        renderKit.addRenderer(HtmlOutputText.COMPONENT_FAMILY,
                "jakarta.faces.Text", new HtmlTextRenderer());
    }
    
    @Test
    public void testIf() throws Exception {
        Map session = facesContext.getExternalContext().getSessionMap();
        Employee e = new Employee();
        session.put("employee", e);

        UIViewRoot root = facesContext.getViewRoot();

        // make sure the form is there
        e.setManagement(true);
        vdl.buildView(facesContext, root,"if.xml");
        UIComponent c = root.findComponent("form");
        Assert.assertNotNull("form is null", c);
        
        // now make sure it isn't
        e.setManagement(false);
        
        facesContext.setViewRoot(facesContext.getApplication().getViewHandler()
                .createView(facesContext, "/test"));
        root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"if.xml");
        c = root.findComponent("form");
        Assert.assertNull("form is not null", c);
    }
    
    @Test
    public void testForEach() throws Exception {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map session = facesContext.getExternalContext().getSessionMap();
        Collection c = new ArrayList();
        for (int i = 0; i < 10; i++) {
            c.add(new Character((char)('A' + i)));
        }
        session.put("list", c);
        Map m = new HashMap();
        for (int i = 0; i < 10; i++) {
            m.put("" + i, "" + i);
        }
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"forEach.xml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
    }
    
    @Test
    public void testForEach1() throws Exception {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map session = facesContext.getExternalContext().getSessionMap();
        Collection c = new ArrayList();
        for (int i = 0; i < 10; i++) {
            c.add(new Character((char)('A' + i)));
        }
        session.put("list", c);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"forEach1.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        
        Assert.assertTrue(sw.toString().contains("A = true/false"));
        Assert.assertTrue(sw.toString().contains("B = false/false"));
        Assert.assertTrue(sw.toString().contains("C = false/false"));
        Assert.assertTrue(sw.toString().contains("D = false/false"));
        Assert.assertTrue(sw.toString().contains("E = false/false"));
        Assert.assertTrue(sw.toString().contains("F = false/false"));
        Assert.assertTrue(sw.toString().contains("G = false/false"));
        Assert.assertTrue(sw.toString().contains("H = false/false"));
        Assert.assertTrue(sw.toString().contains("I = false/false"));
        Assert.assertTrue(sw.toString().contains("J = false/true"));
    }

    /**
     * Verify an outer c:set declaration does not affect the block
     * after c:forEach
     * 
     * @throws Exception 
     */
    @Test
    public void testForEach2() throws Exception {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map session = facesContext.getExternalContext().getSessionMap();
        Collection c = new ArrayList();
        for (int i = 0; i < 10; i++) {
            c.add(new Character((char)('A' + i)));
        }
        session.put("list", c);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"forEach2.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        
        Assert.assertTrue(sw.toString().contains("value1 = /"));
    }
    
    @Test
    public void testForEach2CacheAlways() throws Exception
    {
        servletContext.addInitParameter(FaceletCompositionContextImpl.INIT_PARAM_CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString());
        testForEach2();
    }


    /**
     * Verify an outer c:set declaration does not affect the inner block
     * 
     * @throws Exception 
     */
    @Test
    public void testForEach3() throws Exception {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map session = facesContext.getExternalContext().getSessionMap();
        Collection c = new ArrayList();
        for (int i = 0; i < 10; i++) {
            c.add(new Character((char)('A' + i)));
        }
        session.put("list", c);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"forEach3.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        
        Assert.assertTrue(sw.toString().contains("A = true/false"));
        Assert.assertTrue(sw.toString().contains("B = false/false"));
        Assert.assertTrue(sw.toString().contains("C = false/false"));
        Assert.assertTrue(sw.toString().contains("D = false/false"));
        Assert.assertTrue(sw.toString().contains("E = false/false"));
        Assert.assertTrue(sw.toString().contains("F = false/false"));
        Assert.assertTrue(sw.toString().contains("G = false/false"));
        Assert.assertTrue(sw.toString().contains("H = false/false"));
        Assert.assertTrue(sw.toString().contains("I = false/false"));
        Assert.assertTrue(sw.toString().contains("J = false/true"));
        Assert.assertFalse(sw.toString().contains("value1 ="));
    }
    
    @Test
    public void testForEach3CacheAlways() throws Exception
    {
        servletContext.addInitParameter(FaceletCompositionContextImpl.INIT_PARAM_CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString());
        testForEach3();
    }


    /**
     * Verify encapsulation principle for definitions of c:forEach and ui:param
     * 
     * @throws Exception 
     */
    @Test
    public void testForEach4() throws Exception {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map session = facesContext.getExternalContext().getSessionMap();
        Collection c = new ArrayList();
        for (int i = 0; i < 10; i++) {
            c.add(new Character((char)('A' + i)));
        }
        session.put("list", c);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"forEach4.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        
        Assert.assertTrue(sw.toString().contains("value = A"));
        Assert.assertTrue(sw.toString().contains("value = B"));
        Assert.assertTrue(sw.toString().contains("value = C"));
        Assert.assertTrue(sw.toString().contains("value = D"));
        Assert.assertTrue(sw.toString().contains("value = E"));
        Assert.assertTrue(sw.toString().contains("value = F"));
        Assert.assertTrue(sw.toString().contains("value = G"));
        Assert.assertTrue(sw.toString().contains("value = H"));
        Assert.assertTrue(sw.toString().contains("value = I"));
        Assert.assertTrue(sw.toString().contains("value = J"));
        Assert.assertTrue(!sw.toString().contains("value = value"));
        Assert.assertTrue(!sw.toString().contains("A = A"));
    }

    @Test
    public void testForEach4CacheAlways() throws Exception
    {
        servletContext.addInitParameter(FaceletCompositionContextImpl.INIT_PARAM_CACHE_EL_EXPRESSIONS, 
                ELExpressionCacheMode.always.toString());
        testForEach4();
    }

}
