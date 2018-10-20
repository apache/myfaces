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
package org.apache.myfaces.view.facelets.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIData;
import javax.faces.component.UIDataTest.RowData;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.event.PhaseId;

import org.apache.myfaces.test.MyFacesAsserts;
import org.apache.myfaces.test.TestRunner;
import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Assert;
import org.junit.Test;

public class UIRepeatTest extends AbstractJsfTestCase
{

    public UIRepeatTest()
    {
        super();
    }

    private IMocksControl _mocksControl;
    private UIData _testImpl;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        _mocksControl = EasyMock.createControl();
        _testImpl = new UIData();
    }

    /**
     * Test method for
     * {@link javax.faces.component.UIData#setValueExpression(java.lang.String, javax.el.ValueExpression)}.
     */
    public void testValueExpression()
    {
        assertSetValueExpressionException(IllegalArgumentException.class, "rowIndex");
        assertSetValueExpressionException(NullPointerException.class, null);
    }

    private void assertSetValueExpressionException(Class<? extends Throwable> expected, final String name)
    {
        MyFacesAsserts.assertException(expected, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testImpl.setValueExpression(name, null);
            }
        });
    }

    /**
     * Test state save and restore cycle taking in consideration portlet case.
     * 
     * In portlets, saveState() could be called on INVOKE_APPLICATION phase and
     * restoreState() could be called in RENDER_RESPONSE phase.
     * 
     * This test is active when PSS is disabled.
     * 
     * @throws Exception 
     */
    @Test
    public void testSaveAndRestorePortletLifecycleWithoutPss1() throws Exception
    {
        List<RowData> model = new ArrayList<RowData>();
        model.add(new RowData("text1","style1"));
        model.add(new RowData("text2","style2"));
        model.add(new RowData("text3","style3"));
        model.add(new RowData("text4","style4"));
        
        //Put on request map to be resolved later
        request.setAttribute("list", model);
        
        UIViewRoot root = facesContext.getViewRoot();
        createSimpleTable(root);
        UIRepeat table = (UIRepeat) root.getChildren().get(0);
        //UIColumn column = (UIColumn) table.getChildren().get(0);
        UIInput text = (UIInput) table.getChildren().get(0);
        
        facesContext.setCurrentPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        
        //Check the value expressions are working and change the component state 
        for (int i = 0; i < model.size(); i++)
        {
            RowData rowData = model.get(i); 
            table.setRowIndex(i);
            Assert.assertEquals(rowData.getText(), text.getValue());
            text.setSubmittedValue("value"+(i+1));
            //text.getAttributes().put("style", rowData.getStyle());
        }
        
        //Reset row index
        table.setRowIndex(-1);
        
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
        
        Object state = table.saveState(facesContext);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(state);
        oos.flush();
        baos.flush();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object restoredState = (Object) ois.readObject();
        oos.close();
        ois.close();
        
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        facesContext.setViewRoot(new UIViewRoot());
        root = facesContext.getViewRoot();
        createSimpleTable(root);
        table = (UIRepeat) root.getChildren().get(0);
        //column = (UIColumn) table.getChildren().get(0);
        text = (UIInput) table.getChildren().get(0);
        
        table.restoreState(facesContext, restoredState);

        //Check the values were not lost
        for (int i = 0; i < model.size(); i++)
        {
            RowData rowData = model.get(i); 
            table.setRowIndex(i);
            Assert.assertEquals("value"+(i+1), text.getSubmittedValue());
            //assertEquals(model.get(i).getStyle(), text.getAttributes().get("style"));
        }
    }
    
    private void createSimpleTable(UIViewRoot root)
    {
        createSimpleTable(root, false);
    }
    
    private void createSimpleTable(UIViewRoot root, boolean rowStatePreserved)
    {
        UIRepeat table = new UIRepeat();
        //UIColumn column = new UIColumn();
        UIInput text = new UIInput();
        
        //This is only required if markInitiaState fix is not used 
        root.setId(root.createUniqueId());
        table.setId(root.createUniqueId());
        //column.setId(root.createUniqueId());
        text.setId(root.createUniqueId());
        
        table.setVar("row");
        /*
        if (rowStatePreserved)
        {
            table.setRowStatePreserved(true);
        }*/
        table.setValueExpression("value", application.
                getExpressionFactory().createValueExpression(
                        facesContext.getELContext(),"#{list}",List.class));
        
        text.setValueExpression("value", application.
                getExpressionFactory().createValueExpression(
                        facesContext.getELContext(),"#{row.text}",String.class));
        
        root.getChildren().add(table);
        //table.getChildren().add(column);
        //column.getChildren().add(text);
        table.getChildren().add(text);
    }
}
