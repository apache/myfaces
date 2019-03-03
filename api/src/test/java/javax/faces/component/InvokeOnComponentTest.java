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
package javax.faces.component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import static junit.framework.TestCase.assertTrue;

import org.apache.myfaces.dummy.data.Data;
import org.jmock.Mock;

public class InvokeOnComponentTest extends AbstractComponentTest
{

    Mock mock = null;
    ContextCallback cc = null;

    public InvokeOnComponentTest(String arg0)
    {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mock = mock(ContextCallback.class);
        cc = (ContextCallback) mock.proxy();
    }

    @Override
    protected void tearDown() throws Exception
    {
        // mock.verify();
        cc = null;
        mock = null;
        super.tearDown();
    }


    public void testInvokeOnFormPrependId() throws Exception
    {
        UIForm form = new UIForm();
        form.setId("form");
        form.setPrependId(true);
        facesContext.getViewRoot().getChildren().add(form);
        
        UIInput child1 = new UIInput();
        child1.setId("child1");
        form.getChildren().add(child1);

        
        AtomicBoolean val = new AtomicBoolean(false);
        
        this.facesContext.getViewRoot().invokeOnComponent(facesContext, "form:child1", (context, target) -> {
            val.set(true);
        });
        
        assertTrue(val.get());
    }
    
    public void testInvokeOnFormPrependIdFalse() throws Exception
    {
        UIForm form = new UIForm();
        form.setId("form");
        form.setPrependId(false);
        facesContext.getViewRoot().getChildren().add(form);
        
        UIInput child1 = new UIInput();
        child1.setId("child1");
        form.getChildren().add(child1);

        
        AtomicBoolean val = new AtomicBoolean(false);
        
        this.facesContext.getViewRoot().invokeOnComponent(facesContext, "child1", (context, target) -> {
            val.set(true);
        });
        
        assertTrue(val.get());
    }
    
    public void atestInvokeOnComp() throws Exception
    {
        UIForm form = new UIForm();
        UIInput i1 = new UIInput();
        i1.setId("_id1");
        UIInput i2 = new UIInput();
        i2.setId("_id2");
        UIInput i3 = new UIInput();
        i3.setId("_id3");
        UIInput i4 = new UIInput();
        i4.setId("_id4");
        form.getChildren().add(i1);
        form.getChildren().add(i4);
        form.getChildren().add(i2);
        form.getChildren().add(i3);
        this.facesContext.getViewRoot().getChildren().add(form);

        mock.expects(once()).method("invokeContextCallback").with(eq(facesContext), eq(i2));
        mock.expects(never()).method("invokeContextCallback").with(eq(facesContext), eq(i1));
        mock.expects(never()).method("invokeContextCallback").with(eq(facesContext), eq(i3));
        mock.expects(never()).method("invokeContextCallback").with(eq(facesContext), eq(i4));

        this.facesContext.getViewRoot().invokeOnComponent(facesContext, i2.getClientId(facesContext), cc);

    }

    public void btestInvokeOnCompOnUIData() throws Exception
    {
        // column1
        UIColumn c1 = new UIColumn();
        c1.setId("col1");

        UIOutput headerFacet = new UIOutput();
        headerFacet.setValue("HEADER");
        headerFacet.setId("header");
        c1.setHeader(headerFacet);

        UIOutput name = new UIOutput();
        name.setValue("#{data.username}");
        c1.getChildren().add(name);

        // column2
        UIColumn c2 = new UIColumn();
        c2.setId("col2");

        UIOutput secondheaderFacet = new UIOutput();
        secondheaderFacet.setValue("New HEADER");
        secondheaderFacet.setId("header2");
        c2.setHeader(secondheaderFacet);

        UIOutput passwd = new UIOutput();
        passwd.setValue("#{data.password}");
        c2.getChildren().add(passwd);

        // main table
        UIData table = new UIData();
        table.setId("table");

        table.setVar("data");

        table.getChildren().add(c1);
        table.getChildren().add(c2);

        DataModel model = new ListDataModel(createTestData());
        table.setValue(model);
        this.facesContext.getViewRoot().getChildren().add(table);

        // there should be no call on passwd yet, b/c for UIData the invokeOnComp isn't implemented yet...
        mock.expects(once()).method("invokeContextCallback").with(eq(facesContext), eq(table));
        mock.expects(never()).method("invokeContextCallback").with(eq(facesContext), eq(passwd));
        mock.expects(never()).method("invokeContextCallback").with(eq(facesContext), eq(c1));
        mock.expects(never()).method("invokeContextCallback").with(eq(facesContext), eq(name));

        this.facesContext.getViewRoot().invokeOnComponent(facesContext, table.getClientId(facesContext), cc);

    }

    public void testInvokeOnCompOnUIDataChildren() throws Exception
    {
        // column1
        UIColumn c1 = new UIColumn();
        c1.setId("col1");

        UIOutput headerFacet = new UIOutput();
        headerFacet.setValue("HEADER");
        headerFacet.setId("header");
        c1.setHeader(headerFacet);

        UIOutput name = new UIOutput();
        name.setValue("#{data.username}");
        c1.getChildren().add(name);

        // column2
        UIColumn c2 = new UIColumn();
        c2.setId("col2");

        UIOutput secondheaderFacet = new UIOutput();
        secondheaderFacet.setValue("New HEADER");
        secondheaderFacet.setId("header2");
        c2.setHeader(secondheaderFacet);

        UIOutput passwd = new UIOutput();
        passwd.setValue("#{data.password}");
        c2.getChildren().add(passwd);

        // main table
        UIData table = new UIData();
        table.setId("table");

        table.setVar("data");

        table.getChildren().add(c1);
        table.getChildren().add(c2);

        DataModel model = new ListDataModel(createTestData());
        table.setValue(model);
        this.facesContext.getViewRoot().getChildren().add(table);

        System.out.println("RC; " + table.getRowCount());
        table.encodeBegin(facesContext);
        System.out.println("RC; " + table.getRowCount());
        // there should be no call on passwd yet, b/c for UIData the invokeOnComp isn't implemented yet...
        mock.expects(never()).method("invokeContextCallback").with(eq(facesContext), eq(table));
        mock.expects(never()).method("invokeContextCallback").with(eq(facesContext), eq(passwd));
        mock.expects(never()).method("invokeContextCallback").with(eq(facesContext), eq(c1));
        mock.expects(never()).method("invokeContextCallback").with(eq(facesContext), eq(name));

        this.facesContext.getViewRoot().invokeOnComponent(facesContext, passwd.getClientId(facesContext), cc);

    }

    protected List<Data> createTestData()
    {
        List<Data> data = new ArrayList<Data>();

        Data d1 = new Data();
        d1.setPassword("secret");
        d1.setUsername("mr fumakilla");
        Data d2 = new Data();
        d2.setPassword("top secret");
        d2.setUsername("mr funk");

        data.add(d1);
        data.add(d2);

        return data;
    }

}