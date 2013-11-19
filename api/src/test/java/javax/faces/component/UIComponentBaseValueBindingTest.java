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

import java.lang.reflect.Method;
import java.util.Collection;

import javax.el.ValueExpression;
import javax.faces.el.ValueBinding;

import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: mathias
 * Date: 18.03.2007
 * Time: 01:22:43
 * To change this template use File | Settings | File Templates.
 */
public class UIComponentBaseValueBindingTest extends AbstractUIComponentBaseTest
{
    private ValueBinding _valueBinding;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        _valueBinding = _mocksControl.createMock(ValueBinding.class);
    }

    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
        _valueBinding = null;
    }

    @Override
    protected Collection<Method> getMockedMethods() throws Exception
    {
        Collection<Method> mockedMethods = super.getMockedMethods();
        mockedMethods.add(UIComponent.class.getDeclaredMethod("getValueExpression", new Class[]{String.class}));
        mockedMethods.add(UIComponent.class.getDeclaredMethod("setValueExpression", new Class[]{String.class,
                ValueExpression.class}));
        return mockedMethods;
    }

    @Test
    public void testGetValueBindingWOValueExpression() throws Exception
    {
        expect(_testImpl.getValueExpression(EasyMock.eq("xxx"))).andReturn(null);
        _mocksControl.replay();
        Assert.assertNull(_testImpl.getValueBinding("xxx"));
    }

    @Test
    public void testSetValueBinding() throws Exception
    {
        _testImpl.setValueExpression(EasyMock.eq("xxx"), EasyMock.isA(_ValueBindingToValueExpression.class));
        expectLastCall().andAnswer(new IAnswer<Object>()
        {
            public Object answer() throws Throwable
            {
                _ValueBindingToValueExpression ve = (_ValueBindingToValueExpression) EasyMock.getCurrentArguments()[1];
                Assert.assertEquals(_valueBinding, ve.getValueBinding());
                return null;
            }
        });
        _mocksControl.replay();
        _testImpl.setValueBinding("xxx", _valueBinding);
    }

    @Test
    public void testSetValueBindingWNullValue() throws Exception
    {
        _testImpl.setValueExpression(EasyMock.eq("xxx"), (ValueExpression) EasyMock.isNull());
        _mocksControl.replay();
        _testImpl.setValueBinding("xxx", null);
    }

    @Test
    public void testGetValueBindingWithVBToVE() throws Exception
    {
        ValueExpression valueExpression = new _ValueBindingToValueExpression(_valueBinding);
        expect(_testImpl.getValueExpression(EasyMock.eq("xxx"))).andReturn(valueExpression);
        _mocksControl.replay();
        Assert.assertEquals(_valueBinding, _testImpl.getValueBinding("xxx"));
    }

    @Test
    public void testGetValueBindingFromVE() throws Exception
    {
        ValueExpression valueExpression = _mocksControl.createMock(ValueExpression.class);
        expect(_testImpl.getValueExpression(EasyMock.eq("xxx"))).andReturn(valueExpression);
        _mocksControl.replay();
        ValueBinding valueBinding = _testImpl.getValueBinding("xxx");
        Assert.assertNotNull(valueBinding);
        Assert.assertTrue(valueBinding instanceof _ValueExpressionToValueBinding);
        Assert.assertEquals(valueExpression, ((_ValueExpressionToValueBinding) valueBinding).getValueExpression());
    }
}
