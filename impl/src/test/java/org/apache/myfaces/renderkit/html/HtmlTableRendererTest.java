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

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import jakarta.faces.component.UIColumn;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.html.HtmlDataTable;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.model.ListDataModel;

import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
class HtmlTableRendererTest extends AbstractFacesTestCase
{
    private MockResponseWriter writer ;
    private HtmlDataTable dataTable;

    @Override
    @BeforeEach
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

        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_FACES_JS", Boolean.TRUE);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        super.tearDown();
        dataTable = null;
        writer = null;
    }

    @Test
    void htmlPropertyPassTru() throws Exception
    {
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateBasicReadOnlyAttrs();
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                dataTable, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }

    @Test
    void htmlPropertyPassTruNotRendered() throws Exception
    {
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateAttrsNotRenderedForReadOnlyButBehaviorEvents();
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                dataTable, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }

    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    @Test
    void clientBehaviorHolderRendersIdAndName() 
    {
        dataTable.addClientBehavior("keypress", new AjaxBehavior());
        Assertions.assertDoesNotThrow(() -> {
            dataTable.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            assertTrue(output.matches("(?s).+id=\".+\".+"));
            assertTrue(output.matches("(?s).+name=\".+\".+"));
        });
        
    }

    /**
     * Check table renderer behavior when DataModel returns -1 from getRowCount(). It should
     * render the same as if that value is provided. Note t:dataTable newspaper mode requires
     * row count to calculate newspaperRows and newspaperColumns. 
     */
    @Test
    void noRowCountRender()
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

        String output1 = Assertions.assertDoesNotThrow(() -> {
            dataTable.encodeAll(facesContext);
            return ((StringWriter) writer.getWriter()).getBuffer().toString();
        });
        
        dataTable.setValue(new ListDataModel<Person>(list));
        ((StringWriter) writer.getWriter()).getBuffer().setLength(0);
        
        String output2 = Assertions.assertDoesNotThrow(() -> {
            dataTable.encodeAll(facesContext);
            return ((StringWriter) writer.getWriter()).getBuffer().toString();
        });
        
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
