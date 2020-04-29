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
package org.apache.myfaces.el.convert;

import static org.apache.myfaces.Assert.assertException;
import static org.easymock.EasyMock.*;

import java.util.Date;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.ValueBinding;

import jakarta.faces.el.PropertyNotFoundException;
import junit.framework.TestCase;

import org.apache.myfaces.TestRunner;
import org.apache.myfaces.mock.ExceptionMockRunner;
import org.apache.myfaces.test.mock.MockFacesContext;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public class ValueBindingToValueExpressionTest extends TestCase
{

    private ValueBindingToValueExpression testimpl;
    private ValueBinding binding;
    private IMocksControl mockControl;
    private FacesContext facesContext;
    private ELContext elContext;

    @Override
    protected void setUp() throws Exception
    {
        mockControl = EasyMock.createControl();
        binding = mockControl.createMock(ValueBinding.class);
        facesContext = mockControl.createMock(FacesContext.class);
        elContext = mockControl.createMock(ELContext.class);
        testimpl = new ValueBindingToValueExpression(binding);
    }

    /**
     * Test method for {@link org.apache.myfaces.el.convert.ValueBindingToValueExpression#hashCode()}.
     */
    public void testHashCode()
    {
        assertEquals(testimpl.hashCode(), testimpl.hashCode());
        ValueBindingToValueExpression other = new ValueBindingToValueExpression(binding);
        assertEquals(testimpl.hashCode(), other.hashCode());
        other.setTransient(true);
        assertFalse(testimpl.hashCode() == other.hashCode());
        assertFalse(testimpl.hashCode() == mockControl.createMock(ValueBinding.class).hashCode());
    }

    /**
     * Test method for {@link org.apache.myfaces.el.convert.ValueBindingToValueExpression#equals(java.lang.Object)}.
     */
    public void testEqualsObject()
    {
        assertEquals(testimpl, testimpl);
        ValueBindingToValueExpression other = new ValueBindingToValueExpression(binding);
        assertEquals(testimpl, other);
        other.setTransient(true);
        assertFalse(testimpl.equals(other));
        assertFalse(testimpl.equals(mockControl.createMock(ValueBinding.class)));
    }

    /**
     * Test method for {@link org.apache.myfaces.el.convert.ValueBindingToValueExpression#isLiteralText()}.
     */
    public void testIsLiteralText()
    {
        mockControl.replay();
        assertFalse(testimpl.isLiteralText());
        mockControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.convert.ValueBindingToValueExpression#ValueBindingToValueExpression()}.
     */
    public void testValueBindingToValueExpression()
    {
        testimpl = new ValueBindingToValueExpression();
        assertNull(testimpl.getValueBinding());
        assertNull(testimpl.getExpectedType());
        assertException(IllegalStateException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                testimpl.getExpressionString();
                testimpl.getType(elContext);
                testimpl.getValue(elContext);
            }
        });
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.convert.ValueBindingToValueExpression#ValueBindingToValueExpression(ValueBinding)}.
     */
    public void testValueBindingToValueExpressionValueBinding()
    {
        assertEquals(binding, testimpl.getValueBinding());
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.convert.ValueBindingToValueExpression#isReadOnly(jakarta.el.ELContext)}.
     */
    public void testIsReadOnlyELContext()
    {
        expect(elContext.getContext(eq(FacesContext.class))).andReturn(facesContext);
        expect(binding.isReadOnly(eq(facesContext))).andReturn(false);
        mockControl.replay();
        assertEquals(false, testimpl.isReadOnly(elContext));
        mockControl.verify();
        mockControl.reset();
        expect(elContext.getContext(eq(FacesContext.class))).andReturn(facesContext);
        expect(binding.isReadOnly(eq(facesContext))).andReturn(true);
        mockControl.replay();
        assertEquals(true, testimpl.isReadOnly(elContext));
        mockControl.verify();
    }

    /**
     * Test method for {@link org.apache.myfaces.el.convert.ValueBindingToValueExpression#getValue(jakarta.el.ELContext)}.
     */
    public void testGetValueELContext()
    {
        expect(elContext.getContext(eq(FacesContext.class))).andReturn(facesContext);
        Object expectedValue = new Object();
        expect(binding.getValue(eq(facesContext))).andReturn(expectedValue);
        mockControl.replay();
        assertEquals(expectedValue, testimpl.getValue(elContext));
        mockControl.verify();
    }

    /**
     * Test method for {@link org.apache.myfaces.el.convert.ValueBindingToValueExpression#getType(jakarta.el.ELContext)}.
     */
    public void testGetTypeELContext()
    {
        expect(elContext.getContext(eq(FacesContext.class))).andReturn(facesContext);
        Class<Date> expectedType = Date.class;
        expect(binding.getType(eq(facesContext))).andReturn(expectedType);
        mockControl.replay();
        assertEquals(expectedType, testimpl.getType(elContext));
        mockControl.verify();
    }

    /**
     * Test method for {@link org.apache.myfaces.el.convert.ValueBindingToValueExpression#getType(jakarta.el.ELContext)}.
     */
    public void testGetTypeELContextExceptions() throws Exception
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
                expect(elContext.getContext(eq(FacesContext.class))).andReturn(facesContext);
                expect(binding.getType(eq(facesContext))).andThrow(exceptionToThrow);
            }

            @Override
            protected void run(IMocksControl mockControl) throws Exception
            {
                testimpl.getType(elContext);
            }
        }
        assertException(ELException.class, new GetTypeExceptionMockRunner(new EvaluationException()));
        mockControl.reset();
        assertException(jakarta.el.PropertyNotFoundException.class, new GetTypeExceptionMockRunner(
                new PropertyNotFoundException()));
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.convert.ValueBindingToValueExpression#setValue(jakarta.el.ELContext, java.lang.Object)}.
     */
    public void testSetValueELContextObject()
    {
        expect(elContext.getContext(eq(FacesContext.class))).andReturn(facesContext);
        Object expectedValue = new Object();
        binding.setValue(eq(facesContext), eq(expectedValue));
        mockControl.replay();
        testimpl.setValue(elContext, expectedValue);
        mockControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.convert.ValueBindingToValueExpression#setValue(jakarta.el.ELContext, java.lang.Object)}.
     */
    public void testSetValueELContextObjectExceptions() throws Exception
    {
        final Object expectedValue = new Object();
        class SetValueExceptionMockRunner extends ExceptionMockRunner
        {
            SetValueExceptionMockRunner(Throwable exception)
            {
                super(mockControl, exception);
            }

            @Override
            protected void setUp(IMocksControl mockControl, Throwable exceptionToThrow)
            {
                expect(elContext.getContext(eq(FacesContext.class))).andReturn(facesContext);
                binding.setValue(eq(facesContext), eq(expectedValue));
                expectLastCall().andThrow(exceptionToThrow);
            }

            @Override
            protected void run(IMocksControl mockControl) throws Exception
            {
                testimpl.setValue(elContext, expectedValue);
            }
        }
        assertException(ELException.class, new SetValueExceptionMockRunner(new EvaluationException()));
        mockControl.reset();
        assertException(jakarta.el.PropertyNotFoundException.class, new SetValueExceptionMockRunner(
                new PropertyNotFoundException()));
    }

    /**
     * Test method for {@link org.apache.myfaces.el.convert.ValueBindingToValueExpression#getExpressionString()}.
     */
    public void testGetExpressionString()
    {
        String expectedValue = "xxx";
        expect(binding.getExpressionString()).andReturn(expectedValue);
        mockControl.replay();
        assertEquals(expectedValue, testimpl.getExpressionString());
        mockControl.verify();
    }

    /**
     * Test method for {@link org.apache.myfaces.el.convert.ValueBindingToValueExpression#getExpectedType()}.
     */
    public void testGetExpectedType()
    {
        Object expectedValue = new Date();
        facesContext = new MockFacesContext();
        expect(binding.getValue(eq(facesContext))).andReturn(expectedValue);
        mockControl.replay();
        assertEquals(Date.class, testimpl.getExpectedType());
        mockControl.verify();
    }

}
