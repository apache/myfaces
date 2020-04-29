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

import static org.apache.myfaces.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.Date;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;

import jakarta.faces.el.ValueBinding;
import junit.framework.TestCase;

import org.apache.myfaces.TestRunner;
import org.apache.myfaces.mock.ExceptionMockRunner;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

/**
 * Tests for {@link ValueExpressionToValueBinding}. TODO: tests for StateHolder implementation
 */
@SuppressWarnings("deprecation")
public class _ValueExpressionToValueBindingTest extends TestCase
{
    private _ValueExpressionToValueBinding testimpl;
    private ValueExpression expression;
    private IMocksControl mockControl;
    private FacesContext facesContext;
    private ELContext elContext;

    @Override
    protected void setUp() throws Exception
    {
        mockControl = EasyMock.createControl();
        expression = mockControl.createMock(ValueExpression.class);
        facesContext = mockControl.createMock(FacesContext.class);
        elContext = mockControl.createMock(ELContext.class);
        testimpl = new _ValueExpressionToValueBinding(expression);
    }

    public void testHashCode() throws Exception
    {
        assertEquals(testimpl.hashCode(), testimpl.hashCode());
        _ValueExpressionToValueBinding other = new _ValueExpressionToValueBinding(expression);
        assertEquals(testimpl.hashCode(), other.hashCode());
        other.setTransient(true);
        assertFalse(testimpl.hashCode() == other.hashCode());
        assertFalse(testimpl.hashCode() == mockControl.createMock(ValueExpression.class).hashCode());
    }

    public void testEquals() throws Exception
    {
        assertEquals(testimpl, testimpl);
        _ValueExpressionToValueBinding other = new _ValueExpressionToValueBinding(expression);
        assertEquals(testimpl, other);
        other.setTransient(true);
        assertFalse(testimpl.equals(other));
        assertFalse(testimpl.equals(mockControl.createMock(ValueExpression.class)));
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.convert._ValueExpressionToValueBinding#getType(FacesContext)}.
     */
    public void testGetType()
    {
        expect(facesContext.getELContext()).andReturn(elContext);
        expect(expression.getType(eq(elContext))).andStubReturn(Date.class);
        mockControl.replay();
        assertEquals(Date.class, testimpl.getType(facesContext));
        mockControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.convert._ValueExpressionToValueBinding#getType(FacesContext)}.
     */
    public void testGetTypeExceptions() throws Exception
    {
        class GetTypeExceptionMockRunner extends ExceptionMockRunner
        {
            GetTypeExceptionMockRunner(Throwable exception)
            {
                super(mockControl, exception);
            }

            @Override
            protected void setUp(IMocksControl mockControl, Throwable exceptionToThrow)
            {
                expect(facesContext.getELContext()).andReturn(elContext);
                expect(expression.getType(eq(elContext))).andThrow(exceptionToThrow);
            }

            @Override
            protected void run(IMocksControl mockControl) throws Exception
            {
                testimpl.getType(facesContext);
            }
        }
        assertException(EvaluationException.class, new GetTypeExceptionMockRunner(new ELException()));
        mockControl.reset();
        assertException(jakarta.faces.el.PropertyNotFoundException.class, new GetTypeExceptionMockRunner(
                new PropertyNotFoundException()));
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.convert._ValueExpressionToValueBinding#getValue(FacesContext)}.
     */
    public void testGetValue()
    {
        expect(facesContext.getELContext()).andReturn(elContext);
        Object expectedValue = new StringBuffer();
        expect(expression.getValue(eq(elContext))).andReturn(expectedValue);
        mockControl.replay();
        assertEquals(expectedValue, testimpl.getValue(facesContext));
        mockControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.convert._ValueExpressionToValueBinding#getValue(FacesContext)}.
     */
    public void testGetValueExceptions()
    {
        class GetValueExceptionMockRunner extends ExceptionMockRunner
        {
            GetValueExceptionMockRunner(Throwable exception)
            {
                super(mockControl, exception);
            }

            @Override
            protected void setUp(IMocksControl mockControl, Throwable exceptionToThrow)
            {
                expect(facesContext.getELContext()).andReturn(elContext);
                expect(expression.getValue(eq(elContext))).andThrow(exceptionToThrow);
            }

            @Override
            protected void run(IMocksControl mockControl) throws Exception
            {
                testimpl.getValue(facesContext);
            }
        }
        assertException(EvaluationException.class, new GetValueExceptionMockRunner(new ELException()));
        mockControl.reset();
        assertException(jakarta.faces.el.PropertyNotFoundException.class, new GetValueExceptionMockRunner(
                new PropertyNotFoundException()));
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.convert._ValueExpressionToValueBinding#isReadOnly(FacesContext)}.
     */
    public void testIsReadOnly()
    {
        expect(facesContext.getELContext()).andReturn(elContext);
        expect(expression.isReadOnly(eq(elContext))).andReturn(true);
        mockControl.replay();
        assertEquals(true, testimpl.isReadOnly(facesContext));
        mockControl.verify();
        mockControl.reset();
        expect(facesContext.getELContext()).andReturn(elContext);
        expect(expression.isReadOnly(eq(elContext))).andReturn(false);
        mockControl.replay();
        assertEquals(false, testimpl.isReadOnly(facesContext));
        mockControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.convert._ValueExpressionToValueBinding#isReadOnly(FacesContext)}.
     */
    public void testIsReadOnlyExceptions()
    {
        class IsReadOnlyExceptionMockRunner extends ExceptionMockRunner
        {
            IsReadOnlyExceptionMockRunner(Throwable exception)
            {
                super(mockControl, exception);
            }

            @Override
            protected void setUp(IMocksControl mockControl, Throwable exceptionToThrow)
            {
                expect(facesContext.getELContext()).andReturn(elContext);
                expect(expression.isReadOnly(eq(elContext))).andThrow(exceptionToThrow);
            }

            @Override
            protected void run(IMocksControl mockControl) throws Exception
            {
                testimpl.isReadOnly(facesContext);
            }
        }
        assertException(EvaluationException.class, new IsReadOnlyExceptionMockRunner(new ELException()));
        mockControl.reset();
        assertException(jakarta.faces.el.PropertyNotFoundException.class, new IsReadOnlyExceptionMockRunner(
                new PropertyNotFoundException()));
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.convert._ValueExpressionToValueBinding#setValue(FacesContext, java.lang.Object)}.
     */
    public void testSetValue()
    {
        expect(facesContext.getELContext()).andReturn(elContext);
        Object valueToSet = new StringBuffer();
        expression.setValue(eq(elContext), eq(valueToSet));
        mockControl.replay();
        testimpl.setValue(facesContext, valueToSet);
        mockControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.convert._ValueExpressionToValueBinding#setValue(FacesContext, java.lang.Object)}.
     */
    public void testSetValueExceptions()
    {
        final Object valueToSet = new StringBuffer();
        class SetValueExceptionMockRunner extends ExceptionMockRunner
        {
            SetValueExceptionMockRunner(Throwable exception)
            {
                super(mockControl, exception);
            }

            @Override
            protected void setUp(IMocksControl mockControl, Throwable exceptionToThrow)
            {
                expect(facesContext.getELContext()).andReturn(elContext);
                expression.setValue(eq(elContext), eq(valueToSet));
                expectLastCall().andThrow(exceptionToThrow);
            }

            @Override
            protected void run(IMocksControl mockControl) throws Exception
            {
                testimpl.setValue(facesContext, valueToSet);
            }
        }
        assertException(EvaluationException.class, new SetValueExceptionMockRunner(new ELException()));
        mockControl.reset();
        assertException(jakarta.faces.el.PropertyNotFoundException.class, new SetValueExceptionMockRunner(
                new PropertyNotFoundException()));
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.convert._ValueExpressionToValueBinding#_ValueExpressionToValueBinding(jakarta.el.ValueExpression)}.
     */
    public void testValueExpressionToValueBindingValueExpression()
    {
        assertException(IllegalArgumentException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                new _ValueExpressionToValueBinding(null);
            }
        });
    }

    /**
     * Test method for {@link ValueBinding#getExpressionString()}.
     */
    public void testGetExpressionString()
    {
        expect(expression.getExpressionString()).andReturn("expressionString");
        mockControl.replay();
        assertEquals("expressionString", testimpl.getExpressionString());
        mockControl.verify();
    }
}
