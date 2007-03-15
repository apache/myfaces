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

import static org.easymock.EasyMock.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.myfaces.Assert;
import org.apache.myfaces.TestRunner;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class UIComponentBaseTest extends TestCase
{
    private UIComponentBase _testImpl;

    private IMocksControl _mocksControl;
    private FacesContext _facesContext;
    private Renderer _renderer;

    @Override
    protected void setUp() throws Exception
    {
        _mocksControl = EasyMock.createControl();
        _facesContext = _mocksControl.createMock(FacesContext.class);
        _testImpl = _mocksControl.createMock(UIComponentBase.class, getMockedMethods());
        _renderer = _mocksControl.createMock(Renderer.class);
    }

    protected Method[] getMockedMethods() throws Exception
    {
        Collection<Method> methods = new ArrayList<Method>();
        methods.add(UIComponentBase.class.getDeclaredMethod("getRenderer", new Class[] { FacesContext.class }));
        methods.add(UIComponentBase.class.getDeclaredMethod("getFacesContext", null));
        methods.add(UIComponentBase.class.getDeclaredMethod("getFacesContext", null));
        methods.add(UIComponentBase.class.getDeclaredMethod("getParent", null));
        methods.add(UIComponentBase.class.getDeclaredMethod("getPathToComponent", new Class[] { UIComponent.class }));

        return methods.toArray(new Method[methods.size()]);
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
        return testSuite;
    }

    /*
     * Test method for 'javax.faces.component.UIComponentBase.getAttributes()'
     */
    public void testGetAttributes()
    {
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

    public static class AbstractUIComponentBaseTest extends TestCase
    {
        protected UIComponentBase _testImpl;
        protected IMocksControl _mocksControl;
        protected FacesContext _facesContext;
        protected Renderer _renderer;

        @Override
        protected void setUp() throws Exception
        {
            _mocksControl = EasyMock.createControl();
            _facesContext = _mocksControl.createMock(FacesContext.class);
            _testImpl = _mocksControl.createMock(UIComponentBase.class, getMockedMethods());
            _renderer = _mocksControl.createMock(Renderer.class);
        }

        protected Method[] getMockedMethods() throws Exception
        {
            Collection<Method> methods = new ArrayList<Method>();
            methods.add(UIComponentBase.class.getDeclaredMethod("getRenderer", new Class[] { FacesContext.class }));
            methods.add(UIComponentBase.class.getDeclaredMethod("getFacesContext", null));
            methods.add(UIComponentBase.class.getDeclaredMethod("getFacesContext", null));
            methods.add(UIComponentBase.class.getDeclaredMethod("getParent", null));
            methods.add(UIComponentBase.class
                    .getDeclaredMethod("getPathToComponent", new Class[] { UIComponent.class }));

            return methods.toArray(new Method[methods.size()]);
        }
    }

    public static class GetClientIdTest extends AbstractUIComponentBaseTest
    {
        public void testNullFacesContext() throws Exception
        {
            Assert.assertException(NullPointerException.class, new TestRunner()
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
    }

    public abstract static class TestNamingContainerComponent extends UIComponent implements NamingContainer
    {
    }
}
