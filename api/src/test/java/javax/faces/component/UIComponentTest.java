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

import static org.apache.myfaces.Assert.assertException;
import static org.easymock.EasyMock.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.myfaces.TestRunner;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UIComponentTest extends TestCase
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite(UIComponentTest.class.getName());
        suite.addTestSuite(BasicUIComponentTest.class);
        suite.addTestSuite(EncodeAllTest.class);
        suite.addTestSuite(ValueExpressionUIComponentTest.class);
        suite.addTestSuite(InvokeOnComponentTest.class);
        return suite;
    }

    /**
     * @author Mathias Broekelmann (latest modification by $Author$)
     * @version $Revision$ $Date$
     */
    public static class UIComponentTestBase extends TestCase
    {
        protected IMocksControl _mocksControl;
        protected FacesContext _facesContext;

        @Override
        protected void setUp() throws Exception
        {
            _mocksControl = EasyMock.createNiceControl();
            _facesContext = _mocksControl.createMock(FacesContext.class);
        }
    }

    /**
     * Tests for
     * {@link javax.faces.component.UIComponent#invokeOnComponent(javax.faces.context.FacesContext, java.lang.String, javax.faces.component.ContextCallback)}.
     */
    public static class InvokeOnComponentTest extends UIComponentTestBase
    {
        private UIComponent _testimpl;
        private ContextCallback _contextCallback;

        @Override
        protected void setUp() throws Exception
        {
            super.setUp();
            Collection<Method> mockedMethods = new ArrayList<Method>();
            Class<UIComponent> clazz = UIComponent.class;
            mockedMethods.add(clazz.getDeclaredMethod("getClientId", new Class[] { FacesContext.class }));
            mockedMethods.add(clazz.getDeclaredMethod("getFacetsAndChildren", null));

            _testimpl = _mocksControl.createMock(clazz, mockedMethods.toArray(new Method[mockedMethods.size()]));
            _contextCallback = _mocksControl.createMock(ContextCallback.class);
            _mocksControl.checkOrder(true);
        }

        public void testInvokeOnComponentWithSameClientId() throws Exception
        {
            expect(_testimpl.getClientId(same(_facesContext))).andReturn("xxxId");
            _contextCallback.invokeContextCallback(same(_facesContext), same(_testimpl));
            _mocksControl.replay();
            assertTrue(_testimpl.invokeOnComponent(_facesContext, "xxxId", _contextCallback));
            _mocksControl.verify();
        }

        public void testInvokeOnComponentAndNotFindComponentWithClientId() throws Exception
        {
            expect(_testimpl.getClientId(same(_facesContext))).andReturn("xxxId");
            expect(_testimpl.getFacetsAndChildren()).andReturn(Collections.EMPTY_LIST.iterator());
            _mocksControl.replay();
            assertFalse(_testimpl.invokeOnComponent(_facesContext, "xxId", _contextCallback));
            _mocksControl.verify();
        }

        public void testInvokeOnComponentOnChild() throws Exception
        {
            expect(_testimpl.getClientId(same(_facesContext))).andReturn("xxxId");
            String childId = "childId";
            UIComponent child = _mocksControl.createMock(UIComponent.class);
            expect(_testimpl.getFacetsAndChildren()).andReturn(Collections.singletonList(child).iterator());
            expect(child.invokeOnComponent(same(_facesContext), eq(childId), same(_contextCallback))).andReturn(true);
            _mocksControl.replay();
            assertTrue(_testimpl.invokeOnComponent(_facesContext, "childId", _contextCallback));
            _mocksControl.verify();
        }

        public void testInvokeOnComponentExceptions() throws Exception
        {
            assertException(NullPointerException.class, new TestRunner()
            {
                public void run() throws Throwable
                {
                    _testimpl.invokeOnComponent(null, "xxx", _contextCallback);
                }
            });
            assertException(NullPointerException.class, new TestRunner()
            {
                public void run() throws Throwable
                {
                    _testimpl.invokeOnComponent(_facesContext, null, _contextCallback);
                }
            });
            assertException(NullPointerException.class, new TestRunner()
            {
                public void run() throws Throwable
                {
                    _testimpl.invokeOnComponent(_facesContext, "xxx", null);
                }
            });
        }
    }

    /**
     * Tests for {@link javax.faces.component.UIComponent#encodeAll(javax.faces.context.FacesContext)}.
     */
    public static class EncodeAllTest extends UIComponentTestBase
    {
        private UIComponent _testimpl;

        @Override
        protected void setUp() throws Exception
        {
            super.setUp();
            Collection<Method> mockedMethods = new ArrayList<Method>();
            Class<UIComponent> clazz = UIComponent.class;
            mockedMethods.add(clazz.getDeclaredMethod("isRendered", null));
            mockedMethods.add(clazz.getDeclaredMethod("encodeBegin", new Class[] { FacesContext.class }));
            mockedMethods.add(clazz.getDeclaredMethod("getRendersChildren", null));
            mockedMethods.add(clazz.getDeclaredMethod("encodeChildren", new Class[] { FacesContext.class }));
            mockedMethods.add(clazz.getDeclaredMethod("getChildren", null));
            mockedMethods.add(clazz.getDeclaredMethod("encodeEnd", new Class[] { FacesContext.class }));

            _testimpl = _mocksControl.createMock(clazz, mockedMethods.toArray(new Method[mockedMethods.size()]));
            _mocksControl.checkOrder(true);
        }

        public void testEncodeAllNullContext() throws Exception
        {
            assertException(NullPointerException.class, new TestRunner()
            {
                public void run() throws Throwable
                {
                    _testimpl.encodeAll(null);
                }
            });
        }

        public void testEncodeAllNotRendered() throws Exception
        {
            expect(_testimpl.isRendered()).andReturn(false);
            _mocksControl.replay();
            _testimpl.encodeAll(_facesContext);
            _mocksControl.verify();
        }

        public void testEncodeAllRenderesChildren() throws Exception
        {
            expect(_testimpl.isRendered()).andReturn(true);
            _testimpl.encodeBegin(same(_facesContext));
            expect(_testimpl.getRendersChildren()).andReturn(true);
            _testimpl.encodeChildren(same(_facesContext));
            _testimpl.encodeEnd(same(_facesContext));
            _mocksControl.replay();
            _testimpl.encodeAll(_facesContext);
            _mocksControl.verify();
        }

        public void testEncodeAllNotRenderesChildren() throws Exception
        {
            expect(_testimpl.isRendered()).andReturn(true);
            _testimpl.encodeBegin(same(_facesContext));
            expect(_testimpl.getRendersChildren()).andReturn(false);

            List<UIComponent> childs = new ArrayList<UIComponent>();
            UIComponent testChild = _mocksControl.createMock(UIComponent.class);
            childs.add(testChild);
            expect(_testimpl.getChildren()).andReturn(childs);
            testChild.encodeAll(same(_facesContext));

            _testimpl.encodeEnd(same(_facesContext));
            _mocksControl.replay();
            _testimpl.encodeAll(_facesContext);
            _mocksControl.verify();
        }
    }

    /**
     * Test for {@link javax.faces.component.UIComponent#getValueExpression(java.lang.String)}. and
     * {@link javax.faces.component.UIComponent#setValueExpression(java.lang.String, javax.el.ValueExpression)}.
     */
    public static class ValueExpressionUIComponentTest extends UIComponentTestBase
    {
        private UIComponent _testimpl;
        private ValueExpression _expression;
        private ELContext _elContext;

        @Override
        protected void setUp() throws Exception
        {
            super.setUp();
            Collection<Method> mockedMethods = new ArrayList<Method>();
            Class<UIComponent> clazz = UIComponent.class;
            mockedMethods.add(clazz.getDeclaredMethod("getAttributes", null));
            mockedMethods.add(clazz.getDeclaredMethod("getFacesContext", null));
            mockedMethods.add(clazz.getDeclaredMethod("getValueBinding", new Class[] { String.class }));

            _testimpl = _mocksControl.createMock(clazz, mockedMethods.toArray(new Method[mockedMethods.size()]));
            _expression = _mocksControl.createMock(ValueExpression.class);
            _elContext = _mocksControl.createMock(ELContext.class);
            _mocksControl.checkOrder(true);
        }

        public void testValueExpressionArguments() throws Exception
        {
            assertException(NullPointerException.class, new SetValueExpressionTestRunner(_testimpl, null, _expression));
            assertException(IllegalArgumentException.class, new SetValueExpressionTestRunner(_testimpl, "id",
                    _expression));
            assertException(IllegalArgumentException.class, new SetValueExpressionTestRunner(_testimpl, "parent",
                    _expression));
        }

        public void testValueExpression() throws Exception
        {
            expect(_expression.isLiteralText()).andReturn(false);
            _mocksControl.replay();
            _testimpl.setValueExpression("xxx", _expression);
            _mocksControl.verify();
            assertEquals(_expression, _testimpl.getValueExpression("xxx"));
            _testimpl.setValueExpression("xxx", null);
            _mocksControl.verify();

            assertNull(_testimpl.getValueExpression("xxx"));
            assertNull(_testimpl.bindings);
        }

        public void testValueExpressionWithExceptionOnGetValue() throws Exception
        {
            assertValueExpressionWithExceptionOnGetValue(FacesException.class, new ELException());
        }

        private void assertValueExpressionWithExceptionOnGetValue(Class<? extends Throwable> expected, Throwable fired)
        {
            expect(_expression.isLiteralText()).andReturn(true);
            expect(_testimpl.getFacesContext()).andReturn(_facesContext);
            expect(_facesContext.getELContext()).andReturn(_elContext);
            expect(_expression.getValue(eq(_elContext))).andThrow(fired);
            Map map = new HashMap();
            expect(_testimpl.getAttributes()).andReturn(map);
            _mocksControl.replay();
            assertException(expected, new TestRunner()
            {
                public void run() throws Throwable
                {
                    _testimpl.setValueExpression("xxx", _expression);
                }
            });
            _mocksControl.reset();
        }

        public void testValueExpressionWithLiteralText() throws Exception
        {
            expect(_expression.isLiteralText()).andReturn(true);
            expect(_testimpl.getFacesContext()).andReturn(_facesContext);
            expect(_facesContext.getELContext()).andReturn(_elContext);
            expect(_expression.getValue(eq(_elContext))).andReturn("abc");
            Map map = new HashMap();
            expect(_testimpl.getAttributes()).andReturn(map);
            _mocksControl.replay();
            _testimpl.setValueExpression("xxx", _expression);
            assertEquals("abc", map.get("xxx"));
            _mocksControl.verify();
            assertNull(_testimpl.getValueExpression("xxx"));
        }

        public void testValueExpressionWithValueBindingFallback() throws Exception
        {
            ValueBinding valueBinding = _mocksControl.createMock(ValueBinding.class);
            expect(_testimpl.getValueBinding("xxx")).andReturn(valueBinding);
            _mocksControl.replay();
            ValueExpression valueExpression = _testimpl.getValueExpression("xxx");
            _mocksControl.verify();
            assertTrue(valueExpression instanceof _ValueBindingToValueExpression);
            _mocksControl.reset();
            expect(_elContext.getContext(eq(FacesContext.class))).andReturn(_facesContext);
            expect(valueBinding.getValue(eq(_facesContext))).andReturn("value");
            _mocksControl.replay();
            assertEquals("value", valueExpression.getValue(_elContext));
            _mocksControl.verify();
        }
    }

    public static class BasicUIComponentTest extends UIComponentTestBase
    {
        /**
         * Test method for {@link javax.faces.component.UIComponent#getFacetCount()}.
         */
        public void testGetFacetCount() throws Exception
        {
            UIComponent component = _mocksControl.createMock(UIComponent.class, new Method[] { UIComponent.class
                    .getDeclaredMethod("getFacets", null) });
            Map<String, UIComponent> map = new HashMap<String, UIComponent>();
            map.put("xxx1", new UIInput());
            map.put("xxx2", new UIInput());
            map.put("xxx3", new UIInput());
            expect(component.getFacets()).andReturn(map);
            _mocksControl.replay();
            assertEquals(3, component.getFacetCount());
            _mocksControl.verify();

            _mocksControl.reset();
            expect(component.getFacets()).andReturn(null);
            _mocksControl.replay();
            assertEquals(0, component.getFacetCount());
            _mocksControl.verify();
        }

        /**
         * Test method for
         * {@link javax.faces.component.UIComponent#getContainerClientId(javax.faces.context.FacesContext)}.
         * 
         * @throws Exception
         */
        public void testGetContainerClientId() throws Exception
        {
            Collection<Method> mockedMethods = new ArrayList<Method>();
            Class<UIComponent> clazz = UIComponent.class;
            mockedMethods.add(clazz.getDeclaredMethod("getClientId", new Class[] { FacesContext.class }));
            final UIComponent testimpl = _mocksControl.createMock(clazz, mockedMethods.toArray(new Method[mockedMethods
                    .size()]));
            _mocksControl.checkOrder(true);

            assertException(NullPointerException.class, new TestRunner()
            {
                public void run() throws Throwable
                {
                    testimpl.getContainerClientId(null);
                }
            });

            expect(testimpl.getClientId(same(_facesContext))).andReturn("xyz");
            _mocksControl.replay();
            assertEquals("xyz", testimpl.getContainerClientId(_facesContext));
            _mocksControl.verify();
        }
    }

    private static class SetValueExpressionTestRunner implements TestRunner
    {
        private final String _name;
        private final ValueExpression _expression;
        private final UIComponent _component;

        public SetValueExpressionTestRunner(UIComponent component, String name, ValueExpression expression)
        {
            _component = component;
            _name = name;
            _expression = expression;
        }

        public void run() throws Throwable
        {
            _component.setValueExpression(_name, _expression);
        }

    }
}
