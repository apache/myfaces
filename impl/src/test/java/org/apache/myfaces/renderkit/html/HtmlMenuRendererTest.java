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
import java.util.ArrayList;
import java.util.List;

import jakarta.faces.component.UISelectItem;
import jakarta.faces.component.UISelectItems;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.html.HtmlSelectManyMenu;
import jakarta.faces.component.html.HtmlSelectOneMenu;
import jakarta.faces.model.SelectItem;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.junit.Assert;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlMenuRendererTest extends AbstractJsfTestCase
{
    private MockResponseWriter writer ;
    private HtmlSelectOneMenu selectOneMenu;
    private HtmlSelectManyMenu selectManyMenu;

    public void setUp() throws Exception
    {
        super.setUp();

        selectOneMenu = new HtmlSelectOneMenu();
        selectManyMenu = new HtmlSelectManyMenu();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                selectOneMenu.getFamily(),
                selectOneMenu.getRendererType(),
                new HtmlMenuRenderer());
        facesContext.getRenderKit().addRenderer(
                selectManyMenu.getFamily(),
                selectManyMenu.getRendererType(),
                new HtmlMenuRenderer());
        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_JSF_JS", Boolean.TRUE);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
        selectOneMenu = null;
        selectManyMenu = null;
        writer = null;
    }

    public void testSelectOneHtmlPropertyPassTru() throws Exception
    {
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateBasicAttrs(false);
        
        List items = new ArrayList();
        items.add(new SelectItem("mars"));

        UISelectItems selectItems = new UISelectItems();
        selectItems.setValue(items);

        selectOneMenu.getChildren().add(selectItems);
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                selectOneMenu, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    public void testSelectManyHtmlPropertyPassTru() throws Exception
    {
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateBasicAttrs(false);
        
        List items = new ArrayList();
        items.add(new SelectItem("mars"));

        UISelectItems selectItems = new UISelectItems();
        selectItems.setValue(items);

        selectManyMenu.getChildren().add(selectItems);
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                selectManyMenu, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    public void testClientBehaviorHolderRendersIdAndNameSelectOneMenu() 
    {
        UISelectItem item1 = new UISelectItem();
        item1.setItemLabel("#1");
        item1.setItemValue("#1");
        
        UISelectItem item2 = new UISelectItem();
        item2.setItemLabel("#2");
        item2.setItemValue("#2");
        
        selectOneMenu.addClientBehavior("keypress", new AjaxBehavior());
        try 
        {
            selectOneMenu.getChildren().add(item1);
            selectOneMenu.getChildren().add(item2);
            selectOneMenu.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            Assert.assertTrue(output.matches(".+id=\".+\".+"));
            Assert.assertTrue(output.matches(".+name=\".+\".+"));
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        
    }
    
    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    public void testClientBehaviorHolderRendersIdAndNameSelectManyMenu() 
    {
        UISelectItem item1 = new UISelectItem();
        item1.setItemLabel("#1");
        item1.setItemValue("#1");
        
        UISelectItem item2 = new UISelectItem();
        item2.setItemLabel("#2");
        item2.setItemValue("#2");
        
        selectManyMenu.addClientBehavior("keypress", new AjaxBehavior());
        try 
        {
            selectManyMenu.getChildren().add(item1);
            selectManyMenu.getChildren().add(item2);
            selectManyMenu.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            Assert.assertTrue(output.matches(".+id=\".+\".+"));
            Assert.assertTrue(output.matches(".+name=\".+\".+"));
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        
    }
}
