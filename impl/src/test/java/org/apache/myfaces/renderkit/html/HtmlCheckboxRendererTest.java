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

import jakarta.faces.component.UISelectItem;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.html.HtmlSelectBooleanCheckbox;
import jakarta.faces.component.html.HtmlSelectManyCheckbox;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
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

    public void setUp() throws Exception
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

        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_JSF_JS", Boolean.TRUE);
    }

    public void tearDown() throws Exception
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
            new HtmlRenderedAttr("role"),
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
            new HtmlRenderedAttr("style", 1), 
            new HtmlRenderedAttr("styleClass", "styleClass", "class=\"styleClass\"", 1),
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
    
    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    public void testClientBehaviorHolderRendersIdAndNameSelectBooleanCheckbox() 
    {
        selectBooleanCheckbox.addClientBehavior("focus", new AjaxBehavior());
        try 
        {
            selectBooleanCheckbox.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            assertTrue(output.matches(".+id=\".+\".+"));
            assertTrue(output.matches(".+name=\".+\".+"));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        
    }
    
    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    public void testClientBehaviorHolderRendersIdAndNameSelectManyCheckbox() 
    {
        UISelectItem item1 = new UISelectItem();
        item1.setItemLabel("#1");
        item1.setItemValue("#1");
        
        UISelectItem item2 = new UISelectItem();
        item2.setItemLabel("#2");
        item2.setItemValue("#2");
        
        selectManyCheckbox.addClientBehavior("focus", new AjaxBehavior());
        try 
        {
            selectManyCheckbox.getChildren().add(item1);
            selectManyCheckbox.getChildren().add(item2);
            selectManyCheckbox.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            assertTrue(output.matches(".+id=\".+\".+"));
            assertTrue(output.matches(".+name=\".+\".+"));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        
    }
}
