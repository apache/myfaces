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

import jakarta.faces.component.UIComponent;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;

import org.apache.myfaces.test.mock.MockFacesContext;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import  org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import  org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractUIComponentPropertyTest<T>
{
    private final String _property;
    private final T _defaultValue;
    private final T[] _testValues;

    private IMocksControl _mocksControl;
    private MockFacesContext _facesContext;
    private ValueExpression _valueExpression;
    private ELContext _elContext;
    private UIComponent _component;

    public AbstractUIComponentPropertyTest(String property, T defaultValue, T... testValues)
    {
        _property = property;
        _defaultValue = defaultValue;
        _testValues = testValues;
    }

    @BeforeEach
    public void setUp() throws Exception
    {
        _mocksControl = EasyMock.createControl();
        _facesContext = new MockFacesContext();
        _elContext = _mocksControl.createMock(ELContext.class);
        _facesContext.setELContext(_elContext);
        _valueExpression = _mocksControl.createMock(ValueExpression.class);
        _component = createComponent();
    }
    
    @AfterEach
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

    protected PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String property) throws IntrospectionException
    {
        for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors())
        {
            if (pd.getName().equals(property))
            {
                return pd;
            }
        }
        return null;
    }
    
    @Test
    public void testDefaultValue() throws Exception
    {
        Object val = getPropertyDescriptor(_component.getClass(), _property).getReadMethod()
                .invoke(_component);
        Assertions.assertEquals(_defaultValue, val);
    }

    @Test
    public void testExplicitValue() throws Exception
    {
        for (T testValue : _testValues)
        {
            getPropertyDescriptor(_component.getClass(), _property).getWriteMethod()
                    .invoke(_component, testValue);
            
            Object val = getPropertyDescriptor(_component.getClass(), _property).getReadMethod()
                    .invoke(_component);
            Assertions.assertEquals(testValue, val);
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
            
            Object val = getPropertyDescriptor(_component.getClass(), _property).getReadMethod()
                    .invoke(_component);
            Assertions.assertEquals(testValue, val);
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
            
            Object val = getPropertyDescriptor(_component.getClass(), _property).getReadMethod()
                    .invoke(_component);
            Assertions.assertEquals(testValue, val);
            _mocksControl.reset();
        }
    }
}
