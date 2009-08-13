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

import javax.faces.component.UIViewRoot;

import org.apache.myfaces.view.facelets.util.FastWriter;
import org.apache.shale.test.mock.MockResponseWriter;

public class TemplateClientTestCase extends FaceletTestCase {

    @Override
    protected void setupComponents() throws Exception
    {
        application.addComponent(UIViewRoot.COMPONENT_TYPE,
                UIViewRoot.class.getName());
    }

    @Override
    protected void setupConvertersAndValidators() throws Exception
    {
    }

    @Override
    protected void setupRenderers() throws Exception
    {
    }
    
    public void testClientClient() throws Exception {
        this.servletRequest.setAttribute("name", "Mr. Hookom");
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"template-client-client.xhtml");
        FastWriter fw = new FastWriter();
        MockResponseWriter mrw = new MockResponseWriter(fw);
        facesContext.setResponseWriter(mrw);
        root.encodeAll(facesContext);
        //System.out.println(fw);
    }
    
    /*
    public void testLayoutClient() throws Exception {
        FaceletFactory ff = FaceletFactory.getInstance();
        FacesContext faces = FacesContext.getCurrentInstance();
        
        Facelet f = ff.getFacelet("layout-client.xhtml");
        
        this.servletRequest.setAttribute("name", "Mr. Hookom");
        
        UIViewRoot root = faces.getViewRoot();
        f.apply(faces, root);
        
        FastWriter fw = new FastWriter();
        MockResponseWriter mrw = new MockResponseWriter(fw);
        faces.setResponseWriter(mrw);
        root.encodeAll(faces);
        System.out.println(fw);
    }
    */
   

}
