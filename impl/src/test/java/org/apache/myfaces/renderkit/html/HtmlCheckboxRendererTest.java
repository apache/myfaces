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
package org.apache.myfaces.renderkit.html;

import java.io.StringWriter;

import javax.faces.component.UISelectItem;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.component.html.HtmlSelectManyCheckbox;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockRenderKitFactory;
import org.apache.shale.test.mock.MockResponseWriter;


public class HtmlCheckboxRendererTest extends AbstractJsfTestCase
{
    private MockResponseWriter writer ;
    private HtmlSelectManyCheckbox selectManyCheckbox;
    private HtmlSelectBooleanCheckbox selectBooleanCheckbox;

    public HtmlCheckboxRendererTest(String name)
    {
        super(name);
    }
    
    public static Test suite() {
        return new TestSuite(HtmlCheckboxRendererTest.class);
    }

    public void setUp()
    {
        super.setUp();

        selectManyCheckbox = new HtmlSelectManyCheckbox();
        selectBooleanCheckbox = new HtmlSelectBooleanCheckbox();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                selectManyCheckbox.getFamily(),
                selectManyCheckbox.getRendererType(),
                new HtmlCheckboxRenderer());
        facesContext.getRenderKit().addRenderer(
                selectBooleanCheckbox.getFamily(),
                selectBooleanCheckbox.getRendererType(),
                new HtmlCheckboxRenderer());

    }

    public void tearDown()
    {
        super.tearDown();
        selectManyCheckbox = null;
        selectBooleanCheckbox = null;
        writer = null;
    }

    public void testSelectManyHtmlPropertyPassTru() throws Exception 
    {
        HtmlRenderedAttr[] attrs = {
            //_AccesskeyProperty
            new HtmlRenderedAttr("accesskey"),
            //_UniversalProperties
            new HtmlRenderedAttr("dir"), 
            new HtmlRenderedAttr("lang"), 
            new HtmlRenderedAttr("title"),
            //_FocusBlurProperties
            new HtmlRenderedAttr("onfocus"), 
            new HtmlRenderedAttr("onblur"),
            //_ChangeSelectProperties
            new HtmlRenderedAttr("onchange"), 
            new HtmlRenderedAttr("onselect"),
            //_EventProperties
            new HtmlRenderedAttr("onclick"), 
            new HtmlRenderedAttr("ondblclick"), 
            new HtmlRenderedAttr("onkeydown"), 
            new HtmlRenderedAttr("onkeypress"),
            new HtmlRenderedAttr("onkeyup"), 
            new HtmlRenderedAttr("onmousedown"), 
            new HtmlRenderedAttr("onmousemove"), 
            new HtmlRenderedAttr("onmouseout"),
            new HtmlRenderedAttr("onmouseover"), 
            new HtmlRenderedAttr("onmouseup"),
            //_StyleProperties
            new HtmlRenderedAttr("style", 2), 
            new HtmlRenderedAttr("styleClass", "styleClass", "class=\"styleClass\"", 2),
            //_TabindexProperty
            new HtmlRenderedAttr("tabindex")
        };
        
        UISelectItem item = new UISelectItem();
        item.setItemLabel("mars");
        item.setItemValue("mars");
        selectManyCheckbox.getChildren().add(item);

        HtmlCheckAttributesUtil.checkRenderedAttributes(
                selectManyCheckbox, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    public void testSelectBooleanHtmlPropertyPasstru() throws Exception 
    {
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateBasicAttrs();
        
        selectBooleanCheckbox.setSelected(true);

        HtmlCheckAttributesUtil.checkRenderedAttributes(
                selectBooleanCheckbox, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    
    }   
}
