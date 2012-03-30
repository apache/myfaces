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
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.model.ListDataModel;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockRenderKitFactory;
import org.apache.shale.test.mock.MockResponseWriter;

public class HtmlTableRendererTest extends AbstractJsfTestCase
{
    private MockResponseWriter writer ;
    private HtmlDataTable dataTable;

    public HtmlTableRendererTest(String name)
    {
        super(name);
    }
    
    public static Test suite() {
        return new TestSuite(HtmlTableRendererTest.class);
    }

    public void setUp()
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
    }

    public void tearDown()
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
            fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    public void testHtmlPropertyPassTruNotRendered() throws Exception
    {
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateAttrsNotRenderedForReadOnly();
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                dataTable, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    /**
     * Check table renderer behavior when DataModel returns -1 from getRowCount(). It should
     * render the same as if that value is provided. Note t:dataTable newspaper mode requires
     * row count to calculate newspaperRows and newspaperColumns. 
     */
    public void testNoRowCountRender() throws Exception
    {
        List list = new ArrayList();
        list.add(new Person("John"  , "Smith"));
        list.add(new Person("Pepito", "Perez"));
        list.add(new Person("Kurt",   "Kobain"));
        
        dataTable.setId("data");
        dataTable.setRowClasses("class1, class2");
        dataTable.setVar("person");
        
        UIColumn column1 = new UIColumn();
        HtmlOutputText text = new HtmlOutputText();
        text.setValueBinding("value", 
                facesContext.getApplication().createValueBinding("#{person.firstName}"));
        column1.getChildren().add(text);
        
        dataTable.getChildren().add(column1);
        UIColumn column2 = new UIColumn();
        HtmlOutputText text2 = new HtmlOutputText();
        text2.setValueBinding("value", 
                facesContext.getApplication().createValueBinding("#{person.lastName}"));
        column2.getChildren().add(text2);
        dataTable.getChildren().add(column2);

        UnknownRowCountDemoDataModel urdm = new UnknownRowCountDemoDataModel();
        urdm.setWrappedData(list);
        dataTable.setValue(urdm);

        String output1 = null;
        try 
        {
            dataTable.encodeBegin(facesContext);
            dataTable.encodeChildren(facesContext);
            dataTable.encodeEnd(facesContext);
            output1 = ((StringWriter) writer.getWriter()).getBuffer().toString();
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        
        ListDataModel ldm = new ListDataModel();
        ldm.setWrappedData(list);
        dataTable.setValue(ldm);
        ((StringWriter) writer.getWriter()).getBuffer().setLength(0);
        
        String output2 = null;
        try 
        {
            dataTable.encodeBegin(facesContext);
            dataTable.encodeChildren(facesContext);
            dataTable.encodeEnd(facesContext);
            output2 = ((StringWriter) writer.getWriter()).getBuffer().toString();
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }

        assertTrue(output2.contains("John"));
        assertTrue(output2.contains("Smith"));
        assertTrue(output2.contains("class1"));
        assertTrue(output2.contains("class2"));
        
        assertTrue(output1.contains("John"));
        assertTrue(output1.contains("Smith"));
        assertTrue(output1.contains("class1"));
        assertTrue(output1.contains("class2"));
        
        assertEquals(output2, output1);
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
    
    public class UnknownRowCountDemoDataModel extends ListDataModel
    {
        public UnknownRowCountDemoDataModel()
        {
            super();
        }
        
        public int getRowCount()
        {
            return -1;
        }
    }
}
