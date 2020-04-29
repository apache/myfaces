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

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.myfaces.test.mock.MockFacesContext;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractUIComponentPropertyTest<T>
{
    private final String _property;
    private final T _defaultValue;
    private final T[] _testValues;

    private IMocksControl _mocksControl;
    private MockFacesContext _facesContext;
    //private ValueBinding _valueBinding;
    private ValueExpression _valueExpression;
    private ELContext _elContext;
    private UIComponent _component;

    public AbstractUIComponentPropertyTest(String property, T defaultValue, T... testValues)
    {
        _property = property;
        _defaultValue = defaultValue;
        _testValues = testValues;
    }

    @Before
    public void setUp() throws Exception
    {
        _mocksControl = EasyMock.createControl();
        _facesContext = new MockFacesContext();
        _elContext = _mocksControl.createMock(ELContext.class);
        _facesContext.setELContext(_elContext);
        //_valueBinding = _mocksControl.createMock(ValueBinding.class);
        _valueExpression = _mocksControl.createMock(ValueExpression.class);
        _component = createComponent();
    }
    
    @After
    public void tearDown() throws Exception
    {
        _mocksControl = null;
        _facesContext = null;
        _elContext = null;
        _valueExpression = null;
        _component = null;
    }
    
    protected IMocksControl getMocksControl()
    {
        return _mocksControl;
    }

    protected abstract UIComponent createComponent();

    @Test
    public void testDefaultValue() throws Exception
    {
        Assert.assertEquals(_defaultValue, PropertyUtils.getProperty(_component, _property));
    }

    @Test
    public void testExplicitValue() throws Exception
    {
        for (T testValue : _testValues)
        {
            PropertyUtils.setProperty(_component, _property, testValue);
            Assert.assertEquals(testValue, PropertyUtils.getProperty(_component, _property));
        }
    }

    @Test
    public void testExpressionWithLiteralTextValue() throws Exception
    {
        for (T testValue : _testValues)
        {
            expect(_valueExpression.isLiteralText()).andReturn(true);
            expect(_valueExpression.getValue(eq(_facesContext.getELContext()))).andReturn(testValue);
            _mocksControl.replay();
            _component.setValueExpression(_property, _valueExpression);
            Assert.assertEquals(testValue, PropertyUtils.getProperty(_component, _property));
            _mocksControl.reset();
        }
    }

    @Test
    public void testExpressionValue() throws Exception
    {
        for (T testValue : _testValues)
        {
            expect(_valueExpression.isLiteralText()).andReturn(false);
            _mocksControl.replay();
            _component.setValueExpression(_property, _valueExpression);
            _mocksControl.reset();
            expect(_valueExpression.getValue(eq(_facesContext.getELContext()))).andReturn(testValue);
            _mocksControl.replay();
            Assert.assertEquals(testValue, PropertyUtils.getProperty(_component, _property));
            _mocksControl.reset();
        }
    }
}
