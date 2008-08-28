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

import org.apache.myfaces.Assert;
import org.apache.myfaces.TestRunner;
import org.apache.shale.test.base.AbstractJsfTestCase;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import javax.faces.render.Renderer;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UIDataTest extends AbstractJsfTestCase
{
    public UIDataTest(String name)
    {
        super(name);
    }

    private IMocksControl _mocksControl;
    private UIData _testImpl;

    protected void setUp() throws Exception
    {
        super.setUp();
        _mocksControl = EasyMock.createControl();
        _testImpl = new UIData();
    }

    /**
     * Test method for
     * {@link javax.faces.component.UIData#setValueExpression(String, javax.el.ValueExpression)}.
     */
    public void testValueExpression()
    {
        assertSetValueExpressionException(IllegalArgumentException.class, "rowIndex");
        assertSetValueExpressionException(NullPointerException.class, null);
    }

    private void assertSetValueExpressionException(Class<? extends Throwable> expected, final String name)
    {
        Assert.assertException(expected, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testImpl.setValueExpression(name, null);
            }
        });
    }

    /**
     * Test method for {@link javax.faces.component.UIData#getClientId(javax.faces.context.FacesContext)}.
     */
    public void testGetClientId()
    {
        _testImpl.setId("xxx");
        Renderer renderer = _mocksControl.createMock(Renderer.class);
        renderKit.addRenderer(UIData.COMPONENT_FAMILY, UIData.COMPONENT_TYPE, renderer );
        assertEquals("xxx", _testImpl.getClientId(facesContext));
        _testImpl.setRowIndex(99);
        assertEquals("xxx:99", _testImpl.getClientId(facesContext));
    }

    /**
     * Test method for
     * {@link javax.faces.component.UIData#invokeOnComponent(javax.faces.context.FacesContext, String, javax.faces.component.ContextCallback)}.
     */
    public void testInvokeOnComponentFacesContextStringContextCallback()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#broadcast(javax.faces.event.FacesEvent)}.
     */
    public void testBroadcastFacesEvent()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#encodeBegin(javax.faces.context.FacesContext)}.
     */
    public void testEncodeBeginFacesContext()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#encodeEnd(javax.faces.context.FacesContext)}.
     */
    public void testEncodeEndFacesContext()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#queueEvent(javax.faces.event.FacesEvent)}.
     */
    public void testQueueEventFacesEvent()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#processDecodes(javax.faces.context.FacesContext)}.
     */
    public void testProcessDecodesFacesContext()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#processValidators(javax.faces.context.FacesContext)}.
     */
    public void testProcessValidatorsFacesContext()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#processUpdates(javax.faces.context.FacesContext)}.
     */
    public void testProcessUpdatesFacesContext()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#saveState(javax.faces.context.FacesContext)}.
     */
    public void testSaveState()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link javax.faces.component.UIData#restoreState(javax.faces.context.FacesContext, Object)}.
     */
    public void testRestoreState()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#UIData()}.
     */
    public void testUIData()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#setFooter(javax.faces.component.UIComponent)}.
     */
    public void testSetFooter()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#getFooter()}.
     */
    public void testGetFooter()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#setHeader(javax.faces.component.UIComponent)}.
     */
    public void testSetHeader()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#getHeader()}.
     */
    public void testGetHeader()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#isRowAvailable()}.
     */
    public void testIsRowAvailable()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#getRowCount()}.
     */
    public void testGetRowCount()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#getRowData()}.
     */
    public void testGetRowData()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#getRowIndex()}.
     */
    public void testGetRowIndex()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#setRowIndex(int)}.
     */
    public void testSetRowIndex()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#getDataModel()}.
     */
    public void testGetDataModel()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#setDataModel(javax.faces.model.DataModel)}.
     */
    public void testSetDataModel()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#setValue(Object)}.
     */
    public void testSetValue()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#setRows(int)}.
     */
    public void testSetRows()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#setFirst(int)}.
     */
    public void testSetFirst()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#getValue()}.
     */
    public void testGetValue()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#getVar()}.
     */
    public void testGetVar()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#setVar(String)}.
     */
    public void testSetVar()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#getRows()}.
     */
    public void testGetRows()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#getFirst()}.
     */
    public void testGetFirst()
    {
        // TODO
    }

    /**
     * Test method for {@link javax.faces.component.UIData#getFamily()}.
     */
    public void testGetFamily()
    {
        // TODO
    }

}
