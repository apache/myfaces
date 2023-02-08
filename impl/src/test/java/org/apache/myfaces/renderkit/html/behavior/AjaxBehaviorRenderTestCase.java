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
package org.apache.myfaces.renderkit.html.behavior;

import java.io.StringWriter;
import jakarta.faces.application.ViewHandler;

import jakarta.faces.component.UIViewRoot;

import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AjaxBehaviorRenderTestCase extends FaceletTestCase {

    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter(ViewHandler.FACELETS_SKIP_COMMENTS_PARAM_NAME, "true");
    }

    @Test
    public void testAjax1() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ajax_1.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();

        String response = sw.toString();
        Assertions.assertFalse(response.contains("faces.util.chain"));
        Assertions.assertFalse(response.contains("myfaces.ab"));
    }
 
    @Test
    public void testAjax2() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ajax_2.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();

        String response = sw.toString();
        Assertions.assertTrue(response.contains("myfaces.ab"));
        Assertions.assertEquals(countMatches(response, "myfaces.ab"), 1);
    }
    
    @Test
    public void testAjax3() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ajax_3.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();

        String response = sw.toString();
        Assertions.assertTrue(response.contains("myfaces.ab"));
        Assertions.assertEquals(countMatches(response, "myfaces.ab"), 1);
    }
    
    @Test
    public void testAjax4() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ajax_4.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();

        String response = sw.toString();
        Assertions.assertTrue(response.contains("myfaces.ab"));
        Assertions.assertEquals(countMatches(response, "faces.util.chain"), 1);
        Assertions.assertEquals(countMatches(response, "myfaces.ab"), 2);
    }
    
    @Test
    public void testAjax5() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ajax_5.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();

        String response = sw.toString();
        Assertions.assertFalse(response.contains("myfaces.ab"));
        Assertions.assertFalse(response.contains("faces.util.chain"));
    }
    
    @Test
    public void testAjax6() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ajax_6.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();

        String response = sw.toString();
        Assertions.assertTrue(response.contains("myfaces.ab"));
    }
    
    @Test
    public void testAjax7() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ajax_7.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();

        String response = sw.toString();
        Assertions.assertTrue(response.contains("myfaces.ab"));
        Assertions.assertTrue(response.contains("faces.util.chain"));
        Assertions.assertEquals(countMatches(response, "faces.util.chain"), 1);
        Assertions.assertEquals(countMatches(response, "myfaces.ab"), 2);
    }
    
    public int countMatches(String all, String search)
    {
        return all.split(search, -1).length - 1;
    }
}
