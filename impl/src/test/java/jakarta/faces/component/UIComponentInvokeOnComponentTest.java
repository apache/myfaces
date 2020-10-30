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
package jakarta.faces.component;

import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.UIPanel;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.test.TestRunner;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
     * Tests for
 * {@link UIComponent#invokeOnComponent(jakarta.faces.context.FacesContext, String, ContextCallback)}.
 */
public class UIComponentInvokeOnComponentTest extends AbstractJsfTestCase
{
    protected IMocksControl _mocksControl;
    private UIComponent _testimpl;
    private ContextCallback _contextCallback;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        _mocksControl = EasyMock.createNiceControl();
        Collection<Method> mockedMethods = new ArrayList<Method>();
        Class<UIComponent> clazz = UIComponent.class;
        mockedMethods.add(clazz.getDeclaredMethod("getClientId", new Class[] { FacesContext.class }));
        mockedMethods.add(clazz.getDeclaredMethod("getFacetsAndChildren", (Class<?>[])null));

        _testimpl = _mocksControl.createMock(clazz, mockedMethods.toArray(new Method[mockedMethods.size()]));
        _contextCallback = _mocksControl.createMock(ContextCallback.class);
        _mocksControl.checkOrder(true);
    }

    @Test
    public void testInvokeOnComponentWithSameClientId() throws Exception
    {
        UIComponent testimpl = new UIOutput();
        testimpl.setId("xxxId");

        //EasyMock.expect(_testimpl.getClientId(EasyMock.same(facesContext))).andReturn("xxxId");
        _contextCallback.invokeContextCallback(EasyMock.same(facesContext), EasyMock.same(testimpl));
        _mocksControl.replay();
        Assert.assertTrue(testimpl.invokeOnComponent(facesContext, "xxxId", _contextCallback));
        _mocksControl.verify();
    }

    @Test
    public void testInvokeOnComponentWithException() throws Exception
    {
        final UIComponent testimpl = new UIOutput();
        testimpl.setId("xxxId");

        //EasyMock.expect(_testimpl.getClientId(EasyMock.same(facesContext))).andReturn("xxxId");
        _contextCallback.invokeContextCallback(EasyMock.same(facesContext), EasyMock.same(testimpl));
        EasyMock.expectLastCall().andThrow(new RuntimeException());
        _mocksControl.replay();
        
        org.apache.myfaces.test.MyFacesAsserts.assertException(FacesException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                Assert.assertTrue(testimpl.invokeOnComponent(facesContext, "xxxId", _contextCallback));
            }
        });
    }

    @Test
    public void testInvokeOnComponentAndNotFindComponentWithClientId() throws Exception
    {
        //List<UIComponent> emptyList = Collections.emptyList();
        UIComponent testimpl = new UIPanel();
        testimpl.setId("xxxId");

        //EasyMock.expect(_testimpl.getClientId(EasyMock.same(facesContext))).andReturn("xxxId");
        //EasyMock.expect(_testimpl.getFacetsAndChildren()).andReturn(emptyList.iterator());
        //_mocksControl.replay();
        Assert.assertFalse(testimpl.invokeOnComponent(facesContext, "xxId", _contextCallback));
        //_mocksControl.verify();
    }

    @Test
    public void testInvokeOnComponentOnChild() throws Exception
    {
        UIComponent testimpl = new UIPanel();
        testimpl.setId("xxxId");
        //EasyMock.expect(_testimpl.getClientId(EasyMock.same(facesContext))).andReturn("xxxId");
        String childId = "childId";
        UIComponent child = new UIOutput();/*_mocksControl.createMock(UIComponent.class);*/
        child.setId(childId);
        testimpl.getChildren().add(child);
        //EasyMock.expect(testimpl.getFacetsAndChildren()).andReturn(Collections.singletonList(child).iterator());
        //EasyMock.expect(child.invokeOnComponent(EasyMock.same(facesContext), EasyMock.eq(childId), EasyMock.same(_contextCallback))).andReturn(true);
        _contextCallback.invokeContextCallback(EasyMock.same(facesContext), EasyMock.same(child));
        _mocksControl.replay();
        Assert.assertTrue(testimpl.invokeOnComponent(facesContext, "childId", _contextCallback));
        _mocksControl.verify();
    }

    @Test
    public void testInvokeOnComponentExceptions() throws Exception
    {
        org.apache.myfaces.test.MyFacesAsserts.assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testimpl.invokeOnComponent(null, "xxx", _contextCallback);
            }
        });
        org.apache.myfaces.test.MyFacesAsserts.assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testimpl.invokeOnComponent(facesContext, null, _contextCallback);
            }
        });
        org.apache.myfaces.test.MyFacesAsserts.assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testimpl.invokeOnComponent(facesContext, "xxx", null);
            }
        });
    }
}
