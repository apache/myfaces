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

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.apache.myfaces.view.facelets.Facelet;
import org.apache.myfaces.view.facelets.FaceletFactory;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.shale.test.mock.MockResponseWriter;

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

    public void testRelativePaths() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "parent.xml");

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        sw.flush();
        //System.out.println("************************");
        //System.out.println(sw.toString());
        //System.out.println("************************");
    }

    public void testCompositionTemplate() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "composition-template.xml");
    }

    public void testCompositionTemplateSimple() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "composition-template-simple.xml");
    }

    public void testComponent() throws Exception
    {
        Map map = new HashMap();
        facesContext.getExternalContext().getRequestMap().put("map", map);

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "component.xml");

        assertEquals("only one child, the component", 1, root.getChildCount());
        assertNotNull("bound to map", map.get("c"));
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
        
        assertEquals("4 children, the component", 4, root.getChildCount());
        assertNotNull("bound to map", map.get("c"));
    }*/

}
