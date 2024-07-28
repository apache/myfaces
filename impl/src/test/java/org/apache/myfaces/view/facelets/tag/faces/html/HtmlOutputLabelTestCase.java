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

package org.apache.myfaces.view.facelets.tag.faces.html;

import java.io.StringWriter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;

import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HtmlOutputLabelTestCase extends AbstractFaceletTestCase
{

    @Test
    public void testOutputLabelEscape() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testOutputLabelEscape.xhtml");

        UIComponent panelGroup = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup);
        UIOutput label = (UIOutput) panelGroup.findComponent("testLabel");
        Assertions.assertNotNull(label);
        UIOutput text = (UIOutput) panelGroup.findComponent("testOut");
        Assertions.assertNotNull(text);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        panelGroup.encodeAll(facesContext);
        sw.flush();

        /*
        HtmlRenderedAttr[] attrs = new HtmlRenderedAttr[]{
                new HtmlRenderedAttr("value")
        };
            
        HtmlCheckAttributesUtil.checkRenderedAttributes(attrs, sw.toString());
        */
    }
    
}
