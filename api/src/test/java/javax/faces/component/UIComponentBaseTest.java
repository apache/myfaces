/*
 * Copyright 2004-2006 The Apache Software Foundation.
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
import static org.easymock.classextension.EasyMock.createControl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionListener;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.event.ValueChangeListener;
import javax.faces.render.Renderer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.myfaces.TestRunner;
import org.easymock.IAnswer;
import org.easymock.classextension.IMocksControl;

@SuppressWarnings("deprecation")
public class UIComponentBaseTest extends TestCase
{
    private UIComponentBase _testImpl;

    private IMocksControl _mocksControl;
    private FacesContext _facesContext;
    private Renderer _renderer;

    @Override
    protected void setUp() throws Exception
    {
        _mocksControl = createControl();
        _facesContext = _mocksControl.createMock(FacesContext.class);
        _testImpl = _mocksControl.createMock(UIComponentBase.class, getMockedMethodsArray());
        _renderer = _mocksControl.createMock(Renderer.class);
    }

    protected final Method[] getMockedMethodsArray() throws Exception
    {
        Collection<Method> mockedMethods = getMockedMethods();
        return mockedMethods.toArray(new Method[mockedMethods.size()]);
    }

    protected Collection<Method> getMockedMethods() throws Exception
    {
        Collection<Method> methods = new ArrayList<Method>();
        methods.add(UIComponentBase.class.getDeclaredMethod("getRenderer", new Class[] { FacesContext.class }));
        methods.add(UIComponentBase.class.getDeclaredMethod("getFacesContext", null));
        methods.add(UIComponentBase.class.getDeclaredMethod("getParent", null));
        methods.add(UIComponentBase.class.getDeclaredMethod("getPathToComponent", new Class[] { UIComponent.class }));

        return methods;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(UIComponentBaseTest.class);
    }

    public static Test suite()
    {
        TestSuite testSuite = new TestSuite(UIComponentBaseTest.class);
        testSuite.addTestSuite(RenderedPropertyTest.class);
        testSuite.addTestSuite(RendererTypePropertyTest.class);
        testSuite.addTestSuite(GetClientIdTest.class);
        testSuite.addTestSuite(ValueBindingTest.class);
        testSuite.addTestSuite(FindComponentTest.class);
        testSuite.addTestSuite(FacesListenerTest.class);
        testSuite.addTestSuite(ProcessSaveRestoreStateTest.class);
        return testSuite;
    }

    /*
     * Test method for 'javax.faces.component.UIComponentBase.getAttributes()'
     */
    public void testGetAttributes()
    {
        // TODO implement tests for _ComponentAttributesMap
        assertTrue(_testImpl.getAttributes() instanceof _ComponentAttributesMap);
    }

    public void testGetRendersChildren()
    {
        assertGetRendersChildren(false, null);
        assertGetRendersChildren(true, _renderer);
        assertGetRendersChildren(false, _renderer);
    }

    private void assertGetRendersChildren(boolean expectedValue, Renderer renderer)
    {
        expect(_testImpl.getFacesContext()).andReturn(_facesContext);
        expect(_testImpl.getRenderer(same(_facesContext))).andReturn(renderer);
        if (renderer != null)
            expect(renderer.getRendersChildren()).andReturn(expectedValue);
        _mocksControl.replay();
        assertEquals(expectedValue, _testImpl.getRendersChildren());
        _mocksControl.verify();
        _mocksControl.reset();
    }

    public void testGetChildCount() throws Exception
    {
        assertEquals(0, _testImpl.getChildCount());
        UIComponent child = _mocksControl.createMock(UIComponent.class);
        List<UIComponent> children = _testImpl.getChildren();
        expect(child.getParent()).andReturn(null);
        child.setParent(same(_testImpl));
        _mocksControl.replay();
        children.add(child);
        assertEquals(1, _testImpl.getChildCount());
        _mocksControl.reset();
        child.setParent((UIComponent) isNull());
        _mocksControl.replay();
        children.remove(child);
        assertEquals(0, _testImpl.getChildCount());
    }

    public void testBroadcast() throws Exception
    {
        assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testImpl.broadcast(null);
            }
        });

        FacesEvent event = _mocksControl.createMock(FacesEvent.class);
        _testImpl.broadcast(event);

        FacesListener listener1 = _mocksControl.createMock(FacesListener.class);
        FacesListener listener2 = _mocksControl.createMock(FacesListener.class);
        _testImpl.addFacesListener(listener1);
        _testImpl.addFacesListener(listener2);

        expect(event.isAppropriateListener(same(listener1))).andReturn(false);
        expect(event.isAppropriateListener(same(listener2))).andReturn(true);
        event.processListener(same(listener2));

        _mocksControl.replay();
        _testImpl.broadcast(event);
        _mocksControl.verify();
    }

    public void testDecode() throws Exception
    {
        assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testImpl.decode(null);
            }
        });

        expect(_testImpl.getRenderer(same(_facesContext))).andReturn(_renderer);
        _renderer.decode(same(_facesContext), same(_testImpl));
        _mocksControl.replay();
        _testImpl.decode(_facesContext);
        _mocksControl.verify();
    }

    public void testEncodeBegin() throws Exception
    {
        assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testImpl.encodeBegin(null);
            }
        });

        _testImpl.setRendered(false);
        _mocksControl.replay();
        _testImpl.encodeBegin(_facesContext);

        _mocksControl.reset();
        _testImpl.setRendered(true);
        expect(_testImpl.getRenderer(same(_facesContext))).andReturn(_renderer);
        _renderer.encodeBegin(same(_facesContext), same(_testImpl));
        _mocksControl.replay();
        _testImpl.encodeBegin(_facesContext);
        _mocksControl.verify();
    }

    public void testEncodeChildren() throws Exception
    {
        assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testImpl.encodeChildren(null);
            }
        });

        _testImpl.setRendered(false);
        _mocksControl.replay();
        _testImpl.encodeChildren(_facesContext);

        _mocksControl.reset();
        _testImpl.setRendered(true);
        expect(_testImpl.getRenderer(same(_facesContext))).andReturn(_renderer);
        _renderer.encodeChildren(same(_facesContext), same(_testImpl));
        _mocksControl.replay();
        _testImpl.encodeChildren(_facesContext);
        _mocksControl.verify();
    }

    public void testEncodeEnd() throws Exception
    {
        assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testImpl.encodeEnd(null);
            }
        });

        _testImpl.setRendered(false);
        _mocksControl.replay();
        _testImpl.encodeEnd(_facesContext);

        _mocksControl.reset();
        _testImpl.setRendered(true);
        expect(_testImpl.getRenderer(same(_facesContext))).andReturn(_renderer);
        _renderer.encodeEnd(same(_facesContext), same(_testImpl));
        _mocksControl.replay();
        _testImpl.encodeEnd(_facesContext);
        _mocksControl.verify();
    }

    public void testQueueEvent() throws Exception
    {
        assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testImpl.queueEvent(null);
            }

        });

        final FacesEvent event = _mocksControl.createMock(FacesEvent.class);

        expect(_testImpl.getParent()).andReturn(null);
        _mocksControl.replay();

        assertException(IllegalStateException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testImpl.queueEvent(event);
            }
        });

        _mocksControl.reset();
        UIComponent parent = _mocksControl.createMock(UIComponent.class);
        expect(_testImpl.getParent()).andReturn(parent);
        parent.queueEvent(same(event));
        _mocksControl.replay();
        _testImpl.queueEvent(event);
        _mocksControl.verify();
    }

    public void testProcessDecodes() throws Exception
    {
        assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testImpl.processDecodes(null);
            }
        });

        Collection<Method> methods = getMockedMethods();
        methods.add(UIComponentBase.class.getDeclaredMethod("decode", new Class[] { FacesContext.class }));
        methods.add(UIComponentBase.class.getDeclaredMethod("getFacetsAndChildren", null));
        _testImpl = _mocksControl.createMock(UIComponentBase.class, methods.toArray(new Method[methods.size()]));

        _testImpl.setRendered(false);
        _mocksControl.replay();
        _testImpl.processDecodes(_facesContext);

        _mocksControl.reset();
        _testImpl.setRendered(true);
        _mocksControl.checkOrder(true);
        UIComponent child = _mocksControl.createMock(UIComponent.class);
        expect(_testImpl.getFacetsAndChildren()).andReturn(Arrays.asList(new UIComponent[] { child }).iterator());
        child.processDecodes(same(_facesContext));
        _testImpl.decode(same(_facesContext));
        _mocksControl.replay();
        _testImpl.processDecodes(_facesContext);
        _mocksControl.verify();

        _mocksControl.reset();
        expect(_testImpl.getFacetsAndChildren()).andReturn(Collections.EMPTY_LIST.iterator());
        _testImpl.decode(same(_facesContext));
        expectLastCall().andThrow(new RuntimeException());
        _facesContext.renderResponse();
        _mocksControl.replay();
        assertException(RuntimeException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testImpl.processDecodes(_facesContext);
            }
        });
        _mocksControl.verify();
    }

    public void testProcessValidators() throws Exception
    {
        assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testImpl.processValidators(null);
            }
        });

        Collection<Method> methods = getMockedMethods();
        methods.add(UIComponentBase.class.getDeclaredMethod("getFacetsAndChildren", null));
        _testImpl = _mocksControl.createMock(UIComponentBase.class, methods.toArray(new Method[methods.size()]));

        _testImpl.setRendered(false);
        _mocksControl.replay();
        _testImpl.processValidators(_facesContext);

        _mocksControl.reset();
        _testImpl.setRendered(true);
        UIComponent child = _mocksControl.createMock(UIComponent.class);
        expect(_testImpl.getFacetsAndChildren()).andReturn(Arrays.asList(new UIComponent[] { child }).iterator());
        child.processValidators(same(_facesContext));
        _mocksControl.replay();
        _testImpl.processValidators(_facesContext);
        _mocksControl.verify();
    }

    public void testProcessUpdates() throws Exception
    {
        assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testImpl.processUpdates(null);
            }
        });

        Collection<Method> methods = getMockedMethods();
        methods.add(UIComponentBase.class.getDeclaredMethod("getFacetsAndChildren", null));
        _testImpl = _mocksControl.createMock(UIComponentBase.class, methods.toArray(new Method[methods.size()]));

        _testImpl.setRendered(false);
        _mocksControl.replay();
        _testImpl.processUpdates(_facesContext);

        _mocksControl.reset();
        _testImpl.setRendered(true);
        UIComponent child = _mocksControl.createMock(UIComponent.class);
        expect(_testImpl.getFacetsAndChildren()).andReturn(Arrays.asList(new UIComponent[] { child }).iterator());
        child.processUpdates(same(_facesContext));
        _mocksControl.replay();
        _testImpl.processUpdates(_facesContext);
        _mocksControl.verify();
    }

    public static class ProcessSaveRestoreStateTest extends AbstractUIComponentBaseTest
    {
        private static final String CHILD_STATE = "childState";
        private static final String TESTIMPL_STATE = "testimplState";
        private static final String FACET_STATE = "facetState";
        private UIComponent _facet;
        private UIComponent _child;
        
        @Override
        protected void setUp() throws Exception
        {
            super.setUp();
            _facet = _mocksControl.createMock(UIComponent.class);
            _child = _mocksControl.createMock(UIComponent.class);
        }

        @Override
        protected Collection<Method> getMockedMethods() throws Exception
        {
            Collection<Method> methods = super.getMockedMethods();
            methods.add(UIComponentBase.class.getDeclaredMethod("getFacets", null));
            methods.add(UIComponentBase.class.getDeclaredMethod("getChildren", null));
            methods.add(UIComponentBase.class.getDeclaredMethod("getFacetCount", null));
            methods.add(UIComponentBase.class.getDeclaredMethod("getChildCount", null));
            methods.add(UIComponentBase.class.getDeclaredMethod("saveState", new Class[] { FacesContext.class }));
            methods.add(UIComponentBase.class.getDeclaredMethod("restoreState", new Class[] { FacesContext.class,
                    Object.class }));
            return methods;
        }

        public void testSaveStateExpections() throws Exception
        {
            assertException(NullPointerException.class, new TestRunner()
            {
                public void run() throws Throwable
                {
                    _testImpl.processSaveState(null);
                }
            });
        }

        public void testRestoreStateExpections() throws Exception
        {
            assertException(NullPointerException.class, new TestRunner()
            {
                public void run() throws Throwable
                {
                    _testImpl.processRestoreState(null, null);
                }
            });
        }

        public void testSaveRestoreStateWithTransientChilds() throws Exception
        {
            _testImpl.setTransient(true);
            assertNull(_testImpl.processSaveState(_facesContext));

            _testImpl.setTransient(false);
            setUpChilds(true, true, true);
            _mocksControl.replay();
            Object state = _testImpl.processSaveState(_facesContext);
            assertNotNull(state);
            _mocksControl.verify();

            _mocksControl.reset();
            _testImpl.restoreState(same(_facesContext), eq(TESTIMPL_STATE));
            _mocksControl.replay();
            _testImpl.processRestoreState(_facesContext, state);
            _mocksControl.verify();
        }

        public void testSaveRestoreState() throws Exception
        {
            _testImpl.setTransient(true);
            assertNull(_testImpl.processSaveState(_facesContext));

            _testImpl.setTransient(false);
            setUpChilds(true, false, false);
            _mocksControl.replay();
            Object state = _testImpl.processSaveState(_facesContext);
            assertNotNull(state);
            _mocksControl.verify();
            
            _mocksControl.reset();
            setUpChilds(false, false, false);
            _mocksControl.replay();
            _testImpl.processRestoreState(_facesContext, state);
            _mocksControl.verify();
        }

        private void setUpChilds(boolean saveState, boolean facetTransient, boolean childTransient)
        {
            if (saveState || !facetTransient)
            {
                Map<String, UIComponent> facetMap = new HashMap<String, UIComponent>();
                facetMap.put("testFacet", _facet);
                expect(_testImpl.getFacetCount()).andReturn(1).anyTimes();
                expect(_testImpl.getFacets()).andReturn(facetMap).anyTimes();
                expect(_facet.isTransient()).andReturn(facetTransient).anyTimes();
            }
            if (!facetTransient)
            {
                if (saveState)
                    expect(_facet.processSaveState(same(_facesContext))).andReturn(FACET_STATE);
                else
                    _facet.processRestoreState(same(_facesContext), eq(FACET_STATE));
            }
            if (saveState || !childTransient)
            {
                List<UIComponent> childs = new ArrayList<UIComponent>();
                childs.add(_child);
                expect(_testImpl.getChildCount()).andReturn(1).anyTimes();
                expect(_testImpl.getChildren()).andReturn(childs).anyTimes();
                expect(_child.isTransient()).andReturn(childTransient).anyTimes();
            }
            if (!childTransient)
            {
                if (saveState)
                    expect(_child.processSaveState(same(_facesContext))).andReturn(CHILD_STATE);
                else
                    _child.processRestoreState(same(_facesContext), eq(CHILD_STATE));
            }
            if (saveState)
                expect(_testImpl.saveState(same(_facesContext))).andReturn(TESTIMPL_STATE);
            else
                _testImpl.restoreState(same(_facesContext), eq(TESTIMPL_STATE));
        }

        public void testProcessSaveState() throws Exception
        {
        }
    }

    public static class FacesListenerTest extends AbstractUIComponentBaseTest
    {
        private ActionListener _actionListener;
        private ValueChangeListener _valueChangeListener;

        @Override
        protected void setUp() throws Exception
        {
            super.setUp();
            _actionListener = _mocksControl.createMock(ActionListener.class);
            _valueChangeListener = _mocksControl.createMock(ValueChangeListener.class);
        }

        public void testExceptions() throws Exception
        {
            assertException(NullPointerException.class, new TestRunner()
            {
                public void run() throws Throwable
                {
                    _testImpl.getFacesListeners(null);
                }

            });
            assertException(IllegalArgumentException.class, new TestRunner()
            {
                public void run() throws Throwable
                {
                    _testImpl.getFacesListeners(Date.class);
                }
            });
        }

        public void testEmptyListener() throws Exception
        {
            FacesListener[] listener = _testImpl.getFacesListeners(ActionListener.class);
            assertNotNull(listener);
            assertEquals(0, listener.length);
        }

        public void testGetFacesListeners()
        {
            _testImpl.addFacesListener(_actionListener);

            FacesListener[] listener = _testImpl.getFacesListeners(ValueChangeListener.class);
            assertNotNull(listener);
            assertEquals(0, listener.length);
            assertTrue(ValueChangeListener.class.equals(listener.getClass().getComponentType()));

            _testImpl.addFacesListener(_valueChangeListener);

            listener = _testImpl.getFacesListeners(FacesListener.class);
            assertNotNull(listener);
            assertEquals(2, listener.length);
            Collection<FacesListener> col = Arrays.asList(listener);
            assertTrue(col.contains(_actionListener));
            assertTrue(col.contains(_valueChangeListener));
        }

        public void testRemoveFacesListener() throws Exception
        {
            assertException(NullPointerException.class, new TestRunner()
            {
                public void run() throws Throwable
                {
                    _testImpl.removeFacesListener(null);
                }
            });

            _testImpl.addFacesListener(_actionListener);
            assertEquals(_actionListener, _testImpl.getFacesListeners(FacesListener.class)[0]);
            _testImpl.removeFacesListener(_actionListener);
            assertEquals(0, _testImpl.getFacesListeners(FacesListener.class).length);
        }
    }

    public static class FindComponentTest extends AbstractUIComponentBaseTest
    {
        @Override
        protected void setUp() throws Exception
        {
            super.setUp();
        }

        public void testArguments() throws Exception
        {
            assertException(NullPointerException.class, new TestRunner()
            {
                public void run() throws Throwable
                {
                    _testImpl.findComponent(null);
                }
            });
            assertNull(_testImpl.findComponent(""));
        }

        public void testRootExpression() throws Exception
        {
            String expression = ":parent";
            UIComponent root = _mocksControl.createMock(UIComponent.class);
            UIComponent parent = _mocksControl.createMock(UIComponent.class);
            _testImpl.setId("testimpl");
            expect(_testImpl.getParent()).andReturn(parent).anyTimes();
            expect(parent.getParent()).andReturn(root).anyTimes();
            expect(root.getParent()).andReturn(null).anyTimes();
            expect(parent.getId()).andReturn("parent").anyTimes();
            expect(root.getId()).andReturn("root").anyTimes();
            expect(root.getFacetsAndChildren()).andReturn(Collections.singletonList(parent).iterator());

            _mocksControl.replay();

            assertEquals(parent, _testImpl.findComponent(expression));
        }

        public void testRelativeExpression() throws Exception
        {
            String expression = "testimpl";
            UIComponent namingContainer = _mocksControl.createMock(TestNamingContainerComponent.class);
            UIComponent parent = _mocksControl.createMock(UIComponent.class);
            _testImpl.setId("testimpl");
            expect(_testImpl.getParent()).andReturn(parent).anyTimes();
            expect(parent.getParent()).andReturn(namingContainer).anyTimes();
            expect(parent.getId()).andReturn("parent").anyTimes();
            expect(namingContainer.getId()).andReturn("namingContainer").anyTimes();
            expect(namingContainer.getFacetsAndChildren()).andReturn(Collections.singletonList(parent).iterator());
            expect(parent.getFacetsAndChildren()).andReturn(Arrays.asList(new UIComponent[] { _testImpl }).iterator());

            _mocksControl.replay();

            assertEquals(_testImpl, _testImpl.findComponent(expression));
        }

        public void testComplexRelativeExpression() throws Exception
        {
            String expression = "child1_1:testimpl";
            Collection<Method> mockedMethods = getMockedMethods();
            mockedMethods.add(UIComponentBase.class.getDeclaredMethod("getFacetsAndChildren", null));
            mockedMethods.add(UIComponentBase.class.getDeclaredMethod("getId", null));
            UIComponent namingContainer = _mocksControl.createMock(TestNamingContainerBaseComponent.class,
                    mockedMethods.toArray(new Method[mockedMethods.size()]));

            expect(namingContainer.getId()).andReturn("namingContainer").anyTimes();
            _testImpl.setId("testimpl");
            UIComponent child1_1 = _mocksControl.createMock(TestNamingContainerComponent.class);
            expect(child1_1.getId()).andReturn("child1_1").anyTimes();
            expect(namingContainer.getFacetsAndChildren()).andReturn(Collections.singletonList(child1_1).iterator());

            expect(child1_1.findComponent(eq("testimpl"))).andReturn(_testImpl);

            _mocksControl.replay();

            assertEquals(_testImpl, namingContainer.findComponent(expression));
        }

        public void testWithRelativeExpressionNamingContainer() throws Exception
        {
            String expression = "testimpl";
            Collection<Method> mockedMethods = getMockedMethods();
            mockedMethods.add(UIComponentBase.class.getDeclaredMethod("getFacetsAndChildren", null));
            mockedMethods.add(UIComponentBase.class.getDeclaredMethod("getId", null));
            UIComponent namingContainer = _mocksControl.createMock(TestNamingContainerBaseComponent.class,
                    mockedMethods.toArray(new Method[mockedMethods.size()]));
            UIComponent parent = _mocksControl.createMock(UIComponent.class);
            _testImpl.setId("testimpl");
            expect(_testImpl.getParent()).andReturn(parent).anyTimes();
            expect(parent.getParent()).andReturn(namingContainer).anyTimes();
            expect(parent.getId()).andReturn("parent").anyTimes();
            expect(namingContainer.getId()).andReturn("namingContainer").anyTimes();
            expect(namingContainer.getFacetsAndChildren()).andReturn(Collections.singletonList(parent).iterator());
            expect(parent.getFacetsAndChildren()).andReturn(Arrays.asList(new UIComponent[] { _testImpl }).iterator());

            _mocksControl.replay();

            assertEquals(_testImpl, namingContainer.findComponent(expression));
        }
    }

    public static class ValueBindingTest extends AbstractUIComponentBaseTest
    {
        private ValueBinding _valueBinding;

        @Override
        protected void setUp() throws Exception
        {
            super.setUp();
            _valueBinding = _mocksControl.createMock(ValueBinding.class);
        }

        @Override
        protected Collection<Method> getMockedMethods() throws Exception
        {
            Collection<Method> mockedMethods = super.getMockedMethods();
            mockedMethods.add(UIComponent.class.getDeclaredMethod("getValueExpression", new Class[] { String.class }));
            mockedMethods.add(UIComponent.class.getDeclaredMethod("setValueExpression", new Class[] { String.class,
                    ValueExpression.class }));
            return mockedMethods;
        }

        public void testGetValueBindingWOValueExpression() throws Exception
        {
            expect(_testImpl.getValueExpression(eq("xxx"))).andReturn(null);
            _mocksControl.replay();
            assertNull(_testImpl.getValueBinding("xxx"));
        }

        public void testSetValueBinding() throws Exception
        {
            _testImpl.setValueExpression(eq("xxx"), isA(_ValueBindingToValueExpression.class));
            expectLastCall().andAnswer(new IAnswer<Object>()
            {
                public Object answer() throws Throwable
                {
                    _ValueBindingToValueExpression ve = (_ValueBindingToValueExpression) getCurrentArguments()[1];
                    assertEquals(_valueBinding, ve.getValueBinding());
                    return null;
                }
            });
            _mocksControl.replay();
            _testImpl.setValueBinding("xxx", _valueBinding);
        }

        public void testSetValueBindingWNullValue() throws Exception
        {
            _testImpl.setValueExpression(eq("xxx"), (ValueExpression) isNull());
            _mocksControl.replay();
            _testImpl.setValueBinding("xxx", null);
        }

        public void testGetValueBindingWithVBToVE() throws Exception
        {
            ValueExpression valueExpression = new _ValueBindingToValueExpression(_valueBinding);
            expect(_testImpl.getValueExpression(eq("xxx"))).andReturn(valueExpression);
            _mocksControl.replay();
            assertEquals(_valueBinding, _testImpl.getValueBinding("xxx"));
        }

        public void testGetValueBindingFromVE() throws Exception
        {
            ValueExpression valueExpression = _mocksControl.createMock(ValueExpression.class);
            expect(_testImpl.getValueExpression(eq("xxx"))).andReturn(valueExpression);
            _mocksControl.replay();
            ValueBinding valueBinding = _testImpl.getValueBinding("xxx");
            assertNotNull(valueBinding);
            assertTrue(valueBinding instanceof _ValueExpressionToValueBinding);
            assertEquals(valueExpression, ((_ValueExpressionToValueBinding) valueBinding).getValueExpression());
        }
    }

    public static class RenderedPropertyTest extends UIComponentPropertyTest<Boolean>
    {
        @Override
        protected UIComponent createComponent()
        {
            return new UIComponentMock();
        }

        @Override
        protected Boolean getDefaultValue()
        {
            return true;
        }

        protected String getProperty()
        {
            return "rendered";
        }

        @Override
        protected Boolean[] getTestValues()
        {
            return new Boolean[] { false, true };
        }
    }

    public static class RendererTypePropertyTest extends UIComponentPropertyTest<String>
    {
        @Override
        protected UIComponent createComponent()
        {
            return new UIComponentMock();
        }

        @Override
        protected String getDefaultValue()
        {
            return null;
        }

        protected String getProperty()
        {
            return "rendererType";
        }

        @Override
        protected String[] getTestValues()
        {
            return new String[] { "xxx", "123" };
        }
    }

    public static abstract class AbstractUIComponentBaseTest extends TestCase
    {
        protected UIComponentBase _testImpl;
        protected IMocksControl _mocksControl;
        protected FacesContext _facesContext;
        protected Renderer _renderer;

        @Override
        protected void setUp() throws Exception
        {
            _mocksControl = createControl();
            _facesContext = _mocksControl.createMock(FacesContext.class);
            _testImpl = _mocksControl.createMock(UIComponentBase.class, getMockedMethodsArray());
            _renderer = _mocksControl.createMock(Renderer.class);
        }

        protected final Method[] getMockedMethodsArray() throws Exception
        {
            Collection<Method> mockedMethods = getMockedMethods();
            return mockedMethods.toArray(new Method[mockedMethods.size()]);
        }

        protected Collection<Method> getMockedMethods() throws Exception
        {
            Collection<Method> methods = new ArrayList<Method>();
            methods.add(UIComponentBase.class.getDeclaredMethod("getRenderer", new Class[] { FacesContext.class }));
            methods.add(UIComponentBase.class.getDeclaredMethod("getFacesContext", null));
            methods.add(UIComponentBase.class.getDeclaredMethod("getParent", null));
            methods.add(UIComponentBase.class
                    .getDeclaredMethod("getPathToComponent", new Class[] { UIComponent.class }));

            return methods;
        }
    }

    public static class GetClientIdTest extends AbstractUIComponentBaseTest
    {
        public void testNullFacesContext() throws Exception
        {
            assertException(NullPointerException.class, new TestRunner()
            {
                public void run() throws Throwable
                {
                    _testImpl.getClientId(null);
                }
            });
        }

        public void testWithoutParentAndNoRenderer() throws Exception
        {
            String expectedClientId = "testId";
            _testImpl.setId(expectedClientId);
            expect(_testImpl.getParent()).andReturn(null);
            expect(_testImpl.getRenderer(same(_facesContext))).andReturn(null);
            _mocksControl.replay();
            assertEquals(expectedClientId, _testImpl.getClientId(_facesContext));
            _mocksControl.verify();
            assertEquals(expectedClientId, _testImpl.getClientId(_facesContext));
        }

        public void testWithRenderer() throws Exception
        {
            String id = "testId";
            String expectedClientId = "convertedClientId";
            _testImpl.setId(id);
            expect(_testImpl.getParent()).andReturn(null);
            expect(_testImpl.getRenderer(same(_facesContext))).andReturn(_renderer);
            expect(_renderer.convertClientId(same(_facesContext), eq(id))).andReturn(expectedClientId);
            _mocksControl.replay();
            assertEquals(expectedClientId, _testImpl.getClientId(_facesContext));
            _mocksControl.verify();
            assertEquals(expectedClientId, _testImpl.getClientId(_facesContext));
        }

        public void testWithParentNamingContainer() throws Exception
        {
            String id = "testId";
            String containerClientId = "containerClientId";
            String expectedClientId = containerClientId + NamingContainer.SEPARATOR_CHAR + id;
            UIComponent parent = _mocksControl.createMock(UIComponent.class);
            UIComponent namingContainer = _mocksControl.createMock(TestNamingContainerComponent.class);
            _testImpl.setId(id);
            expect(_testImpl.getParent()).andReturn(parent);
            expect(parent.getParent()).andReturn(namingContainer);
            expect(namingContainer.getContainerClientId(same(_facesContext))).andReturn(containerClientId);

            expect(_testImpl.getRenderer(same(_facesContext))).andReturn(_renderer);
            expect(_renderer.convertClientId(same(_facesContext), eq(expectedClientId))).andReturn(expectedClientId);
            _mocksControl.replay();
            assertEquals(expectedClientId, _testImpl.getClientId(_facesContext));
            _mocksControl.verify();
            assertEquals(expectedClientId, _testImpl.getClientId(_facesContext));
        }

        public void testWithParentNamingContainerChanging() throws Exception
        {
            String id = "testId";
            String containerClientId = "containerClientId";
            UIComponent parent = _mocksControl.createMock(UIComponent.class);
            UIComponent namingContainer = _mocksControl.createMock(TestNamingContainerComponent.class);
            for (int i = 0; i < 10; i++)
            {
                _testImpl.setId(id);
                String expectedClientId = containerClientId + i + NamingContainer.SEPARATOR_CHAR + id;
                expect(_testImpl.getParent()).andReturn(parent);
                expect(parent.getParent()).andReturn(namingContainer);
                expect(namingContainer.getContainerClientId(same(_facesContext))).andReturn(containerClientId + i);

                expect(_testImpl.getRenderer(same(_facesContext))).andReturn(_renderer);
                expect(_renderer.convertClientId(same(_facesContext), eq(expectedClientId)))
                        .andReturn(expectedClientId);
                _mocksControl.replay();
                assertEquals(expectedClientId, _testImpl.getClientId(_facesContext));
                _mocksControl.verify();
                assertEquals(expectedClientId, _testImpl.getClientId(_facesContext));
                _mocksControl.reset();
            }
        }

        public void testWithoutId() throws Exception
        {
            UIViewRoot viewRoot = _mocksControl.createMock(UIViewRoot.class);
            expect(_facesContext.getViewRoot()).andReturn(viewRoot);
            String expectedId = "uniqueId";
            expect(viewRoot.createUniqueId()).andReturn(expectedId);
            expect(_testImpl.getParent()).andReturn(null).anyTimes();
            expect(_testImpl.getRenderer(same(_facesContext))).andReturn(null);
            _mocksControl.replay();
            assertEquals(expectedId, _testImpl.getClientId(_facesContext));
            assertEquals(expectedId, _testImpl.getId());
            _mocksControl.verify();
            assertEquals(expectedId, _testImpl.getClientId(_facesContext));
        }

        public void testWithoutIdAndNoUIViewRoot() throws Exception
        {
            expect(_testImpl.getParent()).andReturn(null).anyTimes();
            expect(_facesContext.getViewRoot()).andReturn(null);
            _mocksControl.replay();
            assertException(FacesException.class, new TestRunner()
            {
                public void run() throws Throwable
                {
                    _testImpl.getClientId(_facesContext);
                }
            });
        }
    }

    public abstract static class TestNamingContainerComponent extends UIComponent implements NamingContainer
    {
    }

    public abstract static class TestNamingContainerBaseComponent extends UIComponentBase implements NamingContainer
    {
    }
}
