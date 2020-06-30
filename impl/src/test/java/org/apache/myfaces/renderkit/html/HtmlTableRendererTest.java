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

import javax.faces.component.UIColumn;
import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.model.ListDataModel;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.junit.Assert;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlTableRendererTest extends AbstractJsfTestCase
{
    private MockResponseWriter writer ;
    private HtmlDataTable dataTable;

    public void setUp() throws Exception
    {
        super.setUp();

        dataTable = new HtmlDataTable();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                dataTable.getFamily(),
                dataTable.getRendererType(),
                new HtmlTableRenderer());

        HtmlOutputText text = new HtmlOutputText();
        facesContext.getRenderKit().addRenderer(
                text.getFamily(),
                text.getRendererType(),
                new HtmlTextRenderer());

        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_JSF_JS", Boolean.TRUE);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
        dataTable = null;
        writer = null;
    }

    public void testHtmlPropertyPassTru() throws Exception
    {
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateBasicReadOnlyAttrs();
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                dataTable, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    public void testHtmlPropertyPassTruNotRendered() throws Exception
    {
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateAttrsNotRenderedForReadOnly();
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                dataTable, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    public void testClientBehaviorHolderRendersIdAndName() 
    {
        dataTable.addClientBehavior("keypress", new AjaxBehavior());
        try 
        {
            dataTable.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            Assert.assertTrue(output.matches("(?s).+id=\".+\".+"));
            Assert.assertTrue(output.matches("(?s).+name=\".+\".+"));
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        
    }
    
    /**
     * Check table renderer behavior when DataModel returns -1 from getRowCount(). It should
     * render the same as if that value is provided. Note t:dataTable newspaper mode requires
     * row count to calculate newspaperRows and newspaperColumns. 
     */
    public void testNoRowCountRender()
    {
        List<Person> list = new ArrayList<Person>();
        list.add(new Person("John"  , "Smith"));
        list.add(new Person("Pepito", "Perez"));
        list.add(new Person("Kurt",   "Kobain"));
        
        dataTable.setId("data");
        dataTable.setRowClasses("class1, class2");
        dataTable.setVar("person");
        
        UIColumn column1 = new UIColumn();
        HtmlOutputText text = new HtmlOutputText();
        text.setValueExpression("value", 
                facesContext.getApplication().getExpressionFactory().createValueExpression(
                        facesContext.getELContext(), "#{person.firstName}", String.class));
        column1.getChildren().add(text);
        
        dataTable.getChildren().add(column1);
        UIColumn column2 = new UIColumn();
        HtmlOutputText text2 = new HtmlOutputText();
        text2.setValueExpression("value", 
                facesContext.getApplication().getExpressionFactory().createValueExpression(
                        facesContext.getELContext(), "#{person.lastName}", String.class));
        column2.getChildren().add(text2);
        dataTable.getChildren().add(column2);

        dataTable.setValue(new UnknownRowCountDemoDataModel<Person>(list));

        String output1 = null;
        try 
        {
            dataTable.encodeAll(facesContext);
            output1 = ((StringWriter) writer.getWriter()).getBuffer().toString();
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        
        dataTable.setValue(new ListDataModel<Person>(list));
        ((StringWriter) writer.getWriter()).getBuffer().setLength(0);
        
        String output2 = null;
        try 
        {
            dataTable.encodeAll(facesContext);
            output2 = ((StringWriter) writer.getWriter()).getBuffer().toString();
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        
        Assert.assertTrue(output2.contains("John"));
        Assert.assertTrue(output2.contains("Smith"));
        Assert.assertTrue(output2.contains("class1"));
        Assert.assertTrue(output2.contains("class2"));
        
        Assert.assertTrue(output1.contains("John"));
        Assert.assertTrue(output1.contains("Smith"));
        Assert.assertTrue(output1.contains("class1"));
        Assert.assertTrue(output1.contains("class2"));
        
        Assert.assertEquals(output2, output1);
    }

    public class Person
    {
        private String firstName;
        
        private String lastName;
        
        public Person(String firstName, String lastName)
        {
            this.firstName = firstName;
            this.lastName = lastName;
        }
        
        public String getFirstName()
        {
            return firstName;
        }
        
        public void setFirstName(String firstName)
        {
            this.firstName = firstName;
        }
        
        public String getLastName()
        {
            return lastName;
        }
        
        public void setLastName(String lastName)
        {
            this.lastName = lastName;
        }
    }
    
    public class UnknownRowCountDemoDataModel<E> extends ListDataModel<E>
    {
        public UnknownRowCountDemoDataModel()
        {
            super();
        }

        public UnknownRowCountDemoDataModel(List<E> list)
        {
            super(list);
        }
        
        @Override
        public int getRowCount()
        {
            return -1;
        }
    }
}
