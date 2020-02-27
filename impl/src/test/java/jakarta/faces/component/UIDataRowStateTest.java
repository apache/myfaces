/*
 * Copyright 2011 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jakarta.faces.component;

import java.util.ArrayList;
import java.util.List;
import jakarta.faces.component.html.HtmlColumn;
import jakarta.faces.component.html.HtmlDataTable;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.model.ListDataModel;
import jakarta.faces.render.Renderer;
import junit.framework.Assert;
import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.junit.Test;

public class UIDataRowStateTest extends AbstractJsfTestCase
{
    
    public static class Item
    {
        
        private Integer id;
        private String name;
        private String lastName;

        public Item(Integer id, String name, String lastName)
        {
            this.id = id;
            this.name = name;
            this.lastName = lastName;
        }
        /**
         * @return the id
         */
        public Integer getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(Integer id) {
            this.id = id;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the lastName
         */
        public String getLastName() {
            return lastName;
        }

        /**
         * @param lastName the lastName to set
         */
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }

    @Override
    protected void setUpRenderKit() throws Exception {
        super.setUpRenderKit();
        renderKit.addRenderer(HtmlDataTable.COMPONENT_FAMILY, new HtmlDataTable().getRendererType(), new Renderer(){});
        renderKit.addRenderer(HtmlOutputText.COMPONENT_FAMILY, new HtmlOutputText().getRendererType(), new Renderer(){});
        renderKit.addRenderer(HtmlInputText.COMPONENT_FAMILY, new HtmlInputText().getRendererType(), new Renderer(){});
    }
    
    /**
     * 
     */
    @Test
    public void testChangeIdsAfterSetRowIndex()
    {
        List<Item> list = new ArrayList<Item>();
        int rowCount = 10;
        for (int i = 0; i < rowCount; i++)
        {
            list.add(new Item(i, "name"+i, "lastName"+i));
        }
        
        facesContext.getExternalContext().getRequestMap().put("items", list);
        
        UIViewRoot root = facesContext.getViewRoot();
        UIData data = new HtmlDataTable();
        data.setId("table");
        root.getChildren().add(data);
        data.setValue(new ListDataModel(list));
        data.setVar("item");
        data.setRows(rowCount);
        
        UIColumn col = new HtmlColumn();
        data.getChildren().add(col);

        UIOutput text = new HtmlOutputText();
        text.setId("text");
        text.setValue(facesContext.getApplication().
                getExpressionFactory().createValueExpression(
                facesContext.getELContext(), "#{item.name}", String.class));
        col.getChildren().add(text);
        
        for (int i = 0; i < rowCount ; i++)
        {
            data.setRowIndex(i);
            Assert.assertEquals(data.getId()+":"+i+":"+text.getId(), text.getClientId());
        }
        data.setRowIndex(-1);
        Assert.assertEquals(data.getId()+":"+text.getId(), text.getClientId());
    }
    
    @Test
    public void testChangeIdsAfterSetRowIndex2()
    {
        List<Item> list = new ArrayList<Item>();
        int rowCount = 10;
        for (int i = 0; i < rowCount; i++)
        {
            list.add(new Item(i, "name"+i, "lastName"+i));
        }
        
        facesContext.getExternalContext().getRequestMap().put("items", list);
        
        UIViewRoot root = facesContext.getViewRoot();
        UIData data = new HtmlDataTable();
        data.setId("table");
        root.getChildren().add(data);
        data.setValue(new ListDataModel(list));
        data.setVar("item");
        data.setRows(rowCount);
        
        UIColumn col = new HtmlColumn();
        data.getChildren().add(col);

        UIOutput text = new HtmlOutputText();
        text.setId("text");
        text.setValue(facesContext.getApplication().
                getExpressionFactory().createValueExpression(
                facesContext.getELContext(), "#{item.name}", String.class));
        col.getChildren().add(text);
        
        UIInput inputText = new HtmlInputText();
        inputText.setId("text");
        inputText.setValue(facesContext.getApplication().
                getExpressionFactory().createValueExpression(
                facesContext.getELContext(), "#{item.lastName}", String.class));
        col.getChildren().add(inputText);

        for (int i = 0; i < rowCount ; i++)
        {
            data.setRowIndex(i);
            Assert.assertEquals(data.getId()+":"+i+":"+text.getId(), text.getClientId());
            Assert.assertEquals(data.getId()+":"+i+":"+inputText.getId(), inputText.getClientId());
        }
        data.setRowIndex(-1);
        Assert.assertEquals(data.getId()+":"+text.getId(), text.getClientId());
        Assert.assertEquals(data.getId()+":"+inputText.getId(), inputText.getClientId());
    }
    
    @Test
    public void testAddRowAfterSetRowIndex()
    {
        List<Item> list = new ArrayList<Item>();
        int rowCount = 10;

        facesContext.getExternalContext().getRequestMap().put("items", list);
        
        UIViewRoot root = facesContext.getViewRoot();
        UIData data = new HtmlDataTable();
        data.setId("table");
        root.getChildren().add(data);
        data.setValue(new ListDataModel(list));
        data.setVar("item");
        data.setRows(rowCount);
        
        UIColumn col = new HtmlColumn();
        data.getChildren().add(col);

        UIOutput text = new HtmlOutputText();
        text.setId("text");
        text.setValue(facesContext.getApplication().
                getExpressionFactory().createValueExpression(
                facesContext.getELContext(), "#{item.name}", String.class));
        col.getChildren().add(text);
        
        data.setRowIndex(-1);
        
        data.processDecodes(facesContext);
        
        for (int i = 0; i < rowCount; i++)
        {
            list.add(new Item(i, "name"+i, "lastName"+i));
        }
        
        data.processDecodes(facesContext);
    }
    
    /**
     * Check if EditableValueHolder is being saved and restored.
     */
    @Test
    public void testEditableValueHolderState()
    {
        List<Item> list = new ArrayList<Item>();
        int rowCount = 10;
        for (int i = 0; i < rowCount; i++)
        {
            list.add(new Item(i, "name"+i, "lastName"+i));
        }
        
        facesContext.getExternalContext().getRequestMap().put("items", list);
        
        UIViewRoot root = facesContext.getViewRoot();
        UIData data = new HtmlDataTable();
        data.setId("table");
        root.getChildren().add(data);
        data.setValue(new ListDataModel(list));
        data.setVar("item");
        data.setRows(rowCount);
        
        UIColumn col = new HtmlColumn();
        data.getChildren().add(col);

        UIOutput text = new HtmlOutputText();
        text.setId("text");
        text.setValue(facesContext.getApplication().
                getExpressionFactory().createValueExpression(
                facesContext.getELContext(), "#{item.name}", String.class));
        col.getChildren().add(text);
        
        UIInput inputText = new HtmlInputText();
        inputText.setId("text");
        inputText.setValue(facesContext.getApplication().
                getExpressionFactory().createValueExpression(
                facesContext.getELContext(), "#{item.lastName}", String.class));
        col.getChildren().add(inputText);

        for (int i = 0; i < rowCount ; i++)
        {
            data.setRowIndex(i);
            inputText.setSubmittedValue("someString"+i);
        }
        data.setRowIndex(-1);
        
        for (int i = 0; i < rowCount ; i++)
        {
            data.setRowIndex(i);
            Assert.assertEquals("someString"+i, inputText.getSubmittedValue());
        }
    }
}
