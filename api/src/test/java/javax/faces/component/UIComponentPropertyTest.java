/*
 * Copyright 2007 The Apache Software Foundation.
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
package javax.faces.component;

import static org.easymock.EasyMock.*;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.el.ValueBinding;

import junit.framework.TestCase;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.shale.test.mock.MockFacesContext12;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class UIComponentPropertyTest<T extends Object> extends TestCase
{
    private IMocksControl _mocksControl;
    private UIComponent _testImpl;
    private MockFacesContext12 _facesContext;
    private ValueBinding _valueBinding;
    private ValueExpression _valueExpression;
    private ELContext _elContext;

    protected abstract UIComponent createComponent();

    protected abstract String getProperty();

    protected abstract T getDefaultValue();

    protected abstract T[] getTestValues();

    protected void setUp() throws Exception
    {
        _mocksControl = EasyMock.createControl();
        _facesContext = new MockFacesContext12();
        _elContext = _mocksControl.createMock(ELContext.class);
        _facesContext.setELContext(_elContext);
        _valueBinding = _mocksControl.createMock(ValueBinding.class);
        _valueExpression = _mocksControl.createMock(ValueExpression.class);
        _testImpl = createComponent();
    }

    public void testDefaultValue() throws Exception
    {
        assertEquals(getDefaultValue(), PropertyUtils.getProperty(_testImpl, getProperty()));
    }

    public void testExplicitValue() throws Exception
    {
        for (T testValue : getTestValues())
        {
            PropertyUtils.setProperty(_testImpl, getProperty(), testValue);
            assertEquals(testValue, PropertyUtils.getProperty(_testImpl, getProperty()));
        }
    }

    public void testExpressionWithLiteralTextValue() throws Exception
    {
        for (T testValue : getTestValues())
        {
            expect(_valueExpression.isLiteralText()).andReturn(true);
            expect(_valueExpression.getValue(eq(_facesContext.getELContext()))).andReturn(testValue);
            _mocksControl.replay();
            _testImpl.setValueExpression(getProperty(), _valueExpression);
            assertEquals(testValue, PropertyUtils.getProperty(_testImpl, getProperty()));
            _mocksControl.reset();
        }
    }

    public void testExpressionValue() throws Exception
    {
        for (T testValue : getTestValues())
        {
            expect(_valueExpression.isLiteralText()).andReturn(false);
            _mocksControl.replay();
            _testImpl.setValueExpression(getProperty(), _valueExpression);
            _mocksControl.reset();
            expect(_valueExpression.getValue(eq(_facesContext.getELContext()))).andReturn(testValue);
            _mocksControl.replay();
            assertEquals(testValue, PropertyUtils.getProperty(_testImpl, getProperty()));
            _mocksControl.reset();
        }
    }
}
