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
package org.apache.myfaces.view.facelets.tag.ui;

import jakarta.el.ExpressionFactory;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.util.lang.FastWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestUserTags extends FaceletTestCase {

    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter(ViewHandler.FACELETS_SKIP_COMMENTS_PARAM_NAME, "true");
        servletContext.addInitParameter(ViewHandler.FACELETS_LIBRARIES_PARAM_NAME, "/user.taglib.xml");
    }

    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        // For this test we need the a real one, because the Mock does not
        // handle VariableMapper stuff properly and ui:param logic will not work
        return new org.apache.el.ExpressionFactoryImpl();
    }
    
    @Override
    protected void setupComponents() throws Exception
    {
        application.addComponent(UIViewRoot.COMPONENT_TYPE,
                UIViewRoot.class.getName());
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
        renderKit.addRenderer(UIOutput.COMPONENT_FAMILY,
                "jakarta.faces.Text", new HtmlTextRenderer());
    }
    
    @Test
    public void testClientClient() throws Exception {
        request.setAttribute("test", "foo");
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "test-tags.xml");
        
        FastWriter fw = new FastWriter();
        MockResponseWriter mrw = new MockResponseWriter(fw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        //System.out.println(fw);
    }

    /**
     * Simple attribute passing should only be available on target xhtml source
     * 
     * @throws Exception
     */
    @Test
    public void testUserTag1() throws Exception 
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "usertagtest1.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assertions.assertTrue(result.contains("value1"));
        Assertions.assertFalse(result.contains("value2"));
        Assertions.assertFalse(result.contains("value3"));
    }
    
    /**
     * Attributes should not pass to other user tags if they are not declared explicitly.
     * 
     * @throws Exception
     */
    @Test
    public void testUserTag2() throws Exception 
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "usertagtest2.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assertions.assertTrue(result.contains("value2"));
        Assertions.assertFalse(result.contains("value3"));
        Assertions.assertFalse(result.contains("value1"));
    } 
}
