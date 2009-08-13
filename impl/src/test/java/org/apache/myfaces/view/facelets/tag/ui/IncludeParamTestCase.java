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

import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.view.facelets.Facelet;
import org.apache.myfaces.view.facelets.FaceletFactory;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;
import org.apache.myfaces.view.facelets.util.FastWriter;

public class IncludeParamTestCase extends FaceletTestCase
{

    @Override
    protected void setupComponents() throws Exception
    {
        application.addComponent(UIViewRoot.COMPONENT_TYPE, UIViewRoot.class
                .getName());
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
        renderKit.addRenderer(UIOutput.COMPONENT_FAMILY, "javax.faces.Text",
                new HtmlTextRenderer());
    }

    public void testCaching() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();

        this.servletRequest.setAttribute("test", "test2.xml");
        vdl.buildView(facesContext, root, "test1.xml");

        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        //System.out.println(fw);

        ComponentSupport.removeTransient(root);

        this.servletRequest.setAttribute("test", "test3.xml");

        facesContext.setViewRoot(facesContext.getApplication().getViewHandler()
                .createView(facesContext, "/test"));
        root = facesContext.getViewRoot();

        vdl.buildView(facesContext, root, "test1.xml");

        fw = new FastWriter();
        rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        //System.out.println(fw);
    }

}
