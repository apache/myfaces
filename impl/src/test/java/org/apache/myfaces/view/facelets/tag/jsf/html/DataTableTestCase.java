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

package org.apache.myfaces.view.facelets.tag.jsf.html;

import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UIData;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlColumn;
import jakarta.faces.component.html.HtmlDataTable;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.renderkit.html.HtmlTableRenderer;
import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.bean.Example;
import org.apache.myfaces.util.lang.FastWriter;
import org.junit.Test;

public class DataTableTestCase extends FaceletTestCase
{

    @Override
    protected void setupComponents() throws Exception
    {
        application.addComponent(UIViewRoot.COMPONENT_TYPE, UIViewRoot.class
                .getName());
        application.addComponent(UIPanel.COMPONENT_TYPE,
                UIPanel.class.getName());        
        application.addComponent(HtmlDataTable.COMPONENT_TYPE,
                HtmlDataTable.class.getName());
        application.addComponent(UIColumn.COMPONENT_TYPE,
                UIColumn.class.getName());        
        application.addComponent(HtmlColumn.COMPONENT_TYPE,
                HtmlColumn.class.getName());
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
                "javax.faces.Text", new HtmlTextRenderer());
        renderKit.addRenderer(UIData.COMPONENT_FAMILY,
                "javax.faces.Table", new HtmlTableRenderer());
    }

    @Test
    public void testDataTable() throws Exception
    {
        facesContext.getExternalContext().getRequestMap().put("company",
                Example.createCompany());

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "dataTable.xml");

        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        //System.out.println(fw);
    }

}
