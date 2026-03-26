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

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlForm;
import jakarta.faces.component.html.HtmlOutputText;

import org.apache.myfaces.renderkit.html.HtmlFormRenderer;
import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * MYFACES-4577: ui:insert nested under ui:include must resolve the enclosing
 * page's ui:define; components from the included fragment must be in the tree
 * (e.g. for Ajax update targets).
 */
public class IncludeHandler4577TestCase extends AbstractFaceletTestCase
{

    @Override
    protected void setupComponents() throws Exception
    {
        application.addComponent(UIViewRoot.COMPONENT_TYPE, UIViewRoot.class.getName());
        application.addComponent(HtmlForm.COMPONENT_TYPE, HtmlForm.class.getName());
        application.addComponent(HtmlOutputText.COMPONENT_TYPE, HtmlOutputText.class.getName());
    }

    @Override
    protected void setupConvertersAndValidators() throws Exception
    {
    }

    @Override
    protected void setupRenderers() throws Exception
    {
        renderKit.addRenderer(HtmlForm.COMPONENT_FAMILY, "jakarta.faces.Form", new HtmlFormRenderer());
        renderKit.addRenderer(HtmlOutputText.COMPONENT_FAMILY, "jakarta.faces.Text", new HtmlTextRenderer());
    }

    @Test
    public void testInsertInsideIncludeSeesOuterDefine() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "myfaces4577_main.xhtml");

        UIComponent toolbar = root.findComponent("form:toolbar");
        Assertions.assertNotNull(toolbar, "toolbar from ui:include inside ui:define must be built");
        Assertions.assertEquals("toolbar", ((HtmlOutputText) toolbar).getValue());
    }
}
