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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.same;

import java.util.Date;

import javax.el.ELException;
import javax.el.MethodInfo;
import javax.el.MethodNotFoundException;
import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.MethodBinding;

import jakarta.faces.el.EvaluationException;
import junit.framework.TestCase;

import org.apache.myfaces.Assert;
import org.apache.myfaces.TestRunner;
import org.apache.myfaces.test.el.MockELContext;
import org.apache.myfaces.test.mock.MockFacesContext;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

@SuppressWarnings("deprecation")
public class _MethodBindingToMethodExpressionTest extends TestCase
{

    private IMocksControl _mocksControl;
    private MethodBinding _methodBinding;
    private MockELContext _elContext;
    private MockFacesContext _facesContext;
    private Application _application;
    //private ExpressionFactory _expressionFactory;

    @Override
    protected void setUp() throws Exception
    {
        _mocksControl = EasyMock.createControl();
        _facesContext = new MockFacesContext();
        _application = _mocksControl.createMock(Application.class);
        _facesContext.setApplication(_application);
        //_expressionFactory = _mocksControl.createMock(ExpressionFactory.class);
        _elContext = new MockELContext();
        _elContext.putContext(FacesContext.class, _facesContext);
        _methodBinding = _mocksControl.createMock(MethodBinding.class);
    }

    /**
     * Test method for {@link _MethodBindingToMethodExpression#_MethodBindingToMethodExpression()}.
     */
    public void test_MethodBindingToMethodExpression()
    {
        _MethodBindingToMethodExpression testimpl = new _MethodBindingToMethodExpression();
        assertNull(testimpl.getMethodBinding());
    }

    /**
     * Test method for
     * {@link _MethodBindingToMethodExpression#_MethodBindingToMethodExpression(MethodBinding)}.
     */
    public void test_MethodBindingToMethodExpressionMethodBinding()
    {
        Assert.assertException(IllegalArgumentException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                new _MethodBindingToMethodExpression(null);
            }
        });
        _MethodBindingToMethodExpression testimpl = new _MethodBindingToMethodExpression(_methodBinding);
        assertEquals(_methodBinding, testimpl.getMethodBinding());
    }

    /**
     * Test method for {@link _MethodBindingToMethodExpression#isLiteralText()}.
     */
    public void testIsLiteralText()
    {
        assertIsLiteralText(true, "xxx");
        assertIsLiteralText(false, "#{xxx}");
    }

    private void assertIsLiteralText(boolean expected, String expressionString)
    {
        _MethodBindingToMethodExpression testimpl = new _MethodBindingToMethodExpression(_methodBinding);
        expect(_methodBinding.getExpressionString()).andReturn(expressionString);
        _mocksControl.replay();
        assertEquals(expected, testimpl.isLiteralText());
        _mocksControl.reset();
    }

    /**
     * Test method for {@link _MethodBindingToMethodExpression#getMethodInfo(javax.el.ELContext)}.
     */
    public void testGetMethodInfoELContext()
    {
        _MethodBindingToMethodExpression testimpl = new _MethodBindingToMethodExpression(_methodBinding);
        Class<Date> expectedReturnType = Date.class;
        expect(_methodBinding.getType(same(_facesContext))).andReturn(expectedReturnType);
        _mocksControl.replay();
        MethodInfo methodInfo = testimpl.getMethodInfo(_elContext);
        assertNotNull(methodInfo);
        // assertNull(methodInfo.getName());
        assertEquals(expectedReturnType, methodInfo.getReturnType());
        // assertNull(methodInfo.getParamTypes());
        _mocksControl.verify();
        _mocksControl.reset();

        assertGetMethodInfoException(MethodNotFoundException.class, new jakarta.faces.el.MethodNotFoundException());
        assertGetMethodInfoException(ELException.class, new EvaluationException());
    }

    private void assertGetMethodInfoException(Class<? extends Throwable> expected, final Throwable firedFromMBGetType)
    {
        final _MethodBindingToMethodExpression testimpl = new _MethodBindingToMethodExpression(_methodBinding);
        expect(_methodBinding.getType(same(_facesContext))).andThrow(firedFromMBGetType);
        _mocksControl.replay();
        Assert.assertException(expected, new TestRunner()
        {
            public void run() throws Throwable
            {
                testimpl.getMethodInfo(_elContext);
            }
        });
        _mocksControl.verify();
        _mocksControl.reset();
    }

    /**
     * Test method for
     * {@link _MethodBindingToMethodExpression#invoke(javax.el.ELContext, java.lang.Object[])}.
     */
    public void testInvoke()
    {
        _MethodBindingToMethodExpression testimpl = new _MethodBindingToMethodExpression(_methodBinding);
        Object[] testParams = new Object[] { "test" };
        Object expectedResult = new StringBuffer();
        expect(_methodBinding.invoke(same(_facesContext), same(testParams))).andReturn(expectedResult);
        _mocksControl.replay();
        assertEquals(expectedResult, testimpl.invoke(_elContext, testParams));
        _mocksControl.verify();
        _mocksControl.reset();

        assertInvokeException(MethodNotFoundException.class, new jakarta.faces.el.MethodNotFoundException());
        assertInvokeException(ELException.class, new EvaluationException());
    }

    private void assertInvokeException(Class<? extends Throwable> expected, final Throwable firedFromMBInvoke)
    {
        final _MethodBindingToMethodExpression testimpl = new _MethodBindingToMethodExpression(_methodBinding);
        expect(_methodBinding.invoke(same(_facesContext), (Object[]) isNull())).andThrow(firedFromMBInvoke);
        _mocksControl.replay();
        Assert.assertException(expected, new TestRunner()
        {
            public void run() throws Throwable
            {
                testimpl.invoke(_elContext, null);
            }
        });
        _mocksControl.verify();
        _mocksControl.reset();
    }

    /**
     * Test method for {@link _MethodBindingToMethodExpression#getExpressionString()}.
     */
    public void testGetExpressionString()
    {
        _MethodBindingToMethodExpression testimpl = new _MethodBindingToMethodExpression(_methodBinding);
        expect(_methodBinding.getExpressionString()).andReturn("xxx");
        _mocksControl.replay();
        assertEquals("xxx", testimpl.getExpressionString());
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link _MethodBindingToMethodExpression#restoreState(FacesContext, java.lang.Object)}.
     * 
     * @throws Exception
     */
    public void testStateHolder() throws Exception
    {
        _MethodBindingToMethodExpression testimpl = new _MethodBindingToMethodExpression(_methodBinding);
        assertFalse(testimpl.isTransient());
        testimpl.setTransient(true);        
        assertTrue(testimpl.isTransient());
        assertNull(testimpl.saveState(_facesContext));
    }
}
