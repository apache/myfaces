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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import jakarta.faces.component.UIViewRoot;

import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UITestCase extends FaceletTestCase
{

    @Override
    protected void setupComponents() throws Exception
    {
        application.addComponent(UIViewRoot.COMPONENT_TYPE, UIViewRoot.class
                .getName());
        application.addComponent(ComponentRef.COMPONENT_TYPE,
                ComponentRef.class.getName());
    }

    @Override
    protected void setupConvertersAndValidators() throws Exception
    {
    }

    @Override
    protected void setupRenderers() throws Exception
    {
    }

    @Test
    public void testRelativePaths() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "parent.xml");

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        
        Assertions.assertTrue(sw.toString().equals("Hello World!"));
        
        //System.out.println("************************");
        //System.out.println(sw.toString());
        //System.out.println("************************");
    }

    @Test
    public void testCompositionTemplate() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "composition-template.xml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();

        String response = sw.toString();
        
        Assertions.assertTrue(response.contains("New Title"));
        Assertions.assertTrue(response.contains("New Body"));
    }

    @Test
    public void testCompositionTemplateSimple() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "composition-template-simple.xml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();

        String response = sw.toString();
        
        Assertions.assertTrue(response.contains("New Body"));
    }

    @Test
    public void testComponent() throws Exception
    {
        Map map = new HashMap();
        facesContext.getExternalContext().getRequestMap().put("map", map);

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "component.xml");

        Assertions.assertEquals(1, root.getChildCount());
        Assertions.assertNotNull(map.get("c"));
    }

    /*
    public void testComponentClient() throws Exception {
        FacesContext faces = FacesContext.getCurrentInstance();
        Map map = new HashMap();
        faces.getExternalContext().getRequestMap().put("map", map);

        FaceletFactory f = FaceletFactory.getInstance();
        Facelet at = f.getFacelet("component-client.xml");

        UIViewRoot root = faces.getViewRoot();
        at.apply(faces, root);
        
        Assertions.assertEquals("4 children, the component", 4, root.getChildCount());
        Assertions.assertNotNull("bound to map", map.get("c"));
    }*/

}
