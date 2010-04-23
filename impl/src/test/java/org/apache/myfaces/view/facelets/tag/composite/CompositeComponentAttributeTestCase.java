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

package org.apache.myfaces.view.facelets.tag.composite;

import java.io.StringWriter;

import javax.el.MethodExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlOutputText;

import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.apache.myfaces.view.facelets.FaceletTestCase;

public class CompositeComponentAttributeTestCase extends FaceletTestCase
{

    @Override
    protected void setupComponents() throws Exception
    {
        super.setupComponents();
        application.addComponent(CompositeTestComponent.class.getName(), 
                CompositeTestComponent.class.getName());
    }

    /**
     * Test simple attribute resolution (not set, default, normal use case).
     * 
     * @throws Exception
     */
    public void testSimpleCompositeAttribute() throws Exception
    {
        TestAttributeBean bean = new TestAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeVE.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        CompositeTestComponent compositeComponent1 = (CompositeTestComponent) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        assertNotNull(facet1);
        HtmlOutputText text1 = (HtmlOutputText) facet1.findComponent("text");
        assertNotNull(text1);
        HtmlCommandButton button1 = (HtmlCommandButton) facet1.findComponent("button");
        assertNotNull(button1);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        facet1.pushComponentToEL(facesContext, facet1);
        text1.pushComponentToEL(facesContext, text1);
        //set on tag
        assertEquals(bean.getStyleClass(), text1.getStyleClass());
        //set as default
        assertEquals(bean.getStyle(), text1.getStyle());
        
        assertEquals(bean.getJavaProperty(), text1.getValue());
        
        text1.popComponentFromEL(facesContext);
        button1.pushComponentToEL(facesContext,  button1);
        MethodExpression method = button1.getActionExpression();
        assertEquals(bean.doSomethingFunny("xysj"), method.invoke(facesContext.getELContext(), new Object[]{"xysj"}));
        button1.popComponentFromEL(facesContext);
        facet1.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        compositeComponent1.encodeAll(facesContext);
        sw.flush();
        
        HtmlRenderedAttr[] attrs = new HtmlRenderedAttr[]{
                new HtmlRenderedAttr("style")
        };
            
        HtmlCheckAttributesUtil.checkRenderedAttributes(attrs, sw.toString());
    }
}
