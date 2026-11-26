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

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringWriter;
import jakarta.faces.application.ViewHandler;

import jakarta.faces.component.UIViewRoot;

import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.Test;

public class AjaxBehaviorRenderTestCase extends AbstractFaceletTestCase {

    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter(ViewHandler.FACELETS_SKIP_COMMENTS_PARAM_NAME, "true");
    }

    @Test
    void ajax1() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ajax_1.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();

        String response = sw.toString();
        assertFalse(response.contains("faces.util.chain"));
        assertFalse(response.contains("myfaces.ab"));
    }

    @Test
    void ajax2() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ajax_2.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();

        String response = sw.toString();
        assertTrue(response.contains("myfaces.ab"));
        assertEquals(1, countMatches(response, "myfaces.ab"));
    }

    @Test
    void ajax3() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ajax_3.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();

        String response = sw.toString();
        assertTrue(response.contains("myfaces.ab"));
        assertEquals(1, countMatches(response, "myfaces.ab"));
    }

    @Test
    void ajax4() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ajax_4.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();

        String response = sw.toString();
        assertTrue(response.contains("myfaces.ab"));
        assertEquals(1, countMatches(response, "faces.util.chain"));
        assertEquals(2, countMatches(response, "myfaces.ab"));
    }

    @Test
    void ajax5() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ajax_5.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();

        String response = sw.toString();
        assertFalse(response.contains("myfaces.ab"));
        assertFalse(response.contains("faces.util.chain"));
    }

    @Test
    void ajax6() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ajax_6.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();

        String response = sw.toString();
        assertTrue(response.contains("myfaces.ab"));
    }

    @Test
    void ajax7() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "ajax_7.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();

        String response = sw.toString();
        assertTrue(response.contains("myfaces.ab"));
        assertTrue(response.contains("faces.util.chain"));
        assertEquals(1, countMatches(response, "faces.util.chain"));
        assertEquals(2, countMatches(response, "myfaces.ab"));
    }
    
    public int countMatches(String all, String search)
    {
        return all.split(search, -1).length - 1;
    }
}
