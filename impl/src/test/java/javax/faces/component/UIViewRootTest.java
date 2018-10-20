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

import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ProjectStage;
import javax.faces.application.ViewHandler;
import javax.faces.context.ExternalContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.webapp.FacesServlet;

import org.apache.myfaces.test.TestRunner;
import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockFacesContext12;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UIViewRootTest extends AbstractJsfTestCase
{
    private Map<PhaseId, Class<? extends PhaseListener>> phaseListenerClasses;
    private IMocksControl _mocksControl;
    private MockFacesContext12 _facesContext;
    private UIViewRoot _testimpl;
    private ExternalContext _externalContext;
    private Application _application;
    private Lifecycle _lifecycle;
    private LifecycleFactory _lifecycleFactory;
    private ViewHandler _viewHandler;
    private ELContext _elContext;

    private static ThreadLocal<LifecycleFactory> LIFECYCLEFACTORY = new ThreadLocal<LifecycleFactory>();

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        phaseListenerClasses = new HashMap<PhaseId, Class<? extends PhaseListener>>();
        phaseListenerClasses.put(PhaseId.APPLY_REQUEST_VALUES, ApplyRequesValuesPhaseListener.class);
        phaseListenerClasses.put(PhaseId.PROCESS_VALIDATIONS, ProcessValidationsPhaseListener.class);
        phaseListenerClasses.put(PhaseId.UPDATE_MODEL_VALUES, UpdateModelValuesPhaseListener.class);
        phaseListenerClasses.put(PhaseId.INVOKE_APPLICATION, InvokeApplicationPhaseListener.class);
        phaseListenerClasses.put(PhaseId.RENDER_RESPONSE, RenderResponsePhaseListener.class);

        _mocksControl = EasyMock.createControl();
        _externalContext = _mocksControl.createMock(ExternalContext.class);
        _facesContext = (MockFacesContext12) facesContext;
        _application = _mocksControl.createMock(Application.class);
        _lifecycleFactory = _mocksControl.createMock(LifecycleFactory.class);
        _testimpl = new UIViewRoot();
        _lifecycle = _mocksControl.createMock(Lifecycle.class);
        _elContext = _mocksControl.createMock(ELContext.class);
        _viewHandler = _mocksControl.createMock(ViewHandler.class);
        _facesContext.setELContext(_elContext);

        LIFECYCLEFACTORY.set(_lifecycleFactory);
        FactoryFinder.setFactory(FactoryFinder.LIFECYCLE_FACTORY, MockLifeCycleFactory.class.getName());
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, ProjectStage.UnitTest.name());
        
    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
        _mocksControl.reset();
    }

    @Test
    public void testSuperClass() throws Exception
    {
        assertEquals(UIComponentBase.class, UIViewRoot.class.getSuperclass());
    }

    @Test
    public void testComponentType() throws Exception
    {
        assertEquals("javax.faces.ViewRoot", UIViewRoot.COMPONENT_TYPE);
    }

    @Test
    public void testLocale() throws Exception
    {
        expect(_application.getViewHandler()).andReturn(_viewHandler).anyTimes();
        expect(_viewHandler.calculateLocale(_facesContext)).andReturn(null).anyTimes();
        _mocksControl.replay();

        _facesContext.setApplication(_application);
        assertNull(_testimpl.getLocale());
        _testimpl.setLocale(Locale.JAPANESE);
        assertEquals(Locale.JAPANESE, _testimpl.getLocale());
        _mocksControl.verify();
    }

    /**
     * Test method for {@link javax.faces.component.UIViewRoot#createUniqueId()}.
     */
    @Test
    public void testCreateUniqueId()
    {
        /*
        expect(_externalContext.encodeNamespace((String) anyObject())).andAnswer(new IAnswer<String>()
        {
            public String answer() throws Throwable
            {
                return (String) getCurrentArguments()[0];
            }
        }).anyTimes();*/
        _mocksControl.replay();
        Collection<String> createdIds = new HashSet<String>();
        for (int i = 0; i < 10000; i++)
        {
            if (!createdIds.add(_testimpl.createUniqueId()))
            {
                fail("duplicate id created");
            }
        }
        _mocksControl.verify();
    }

// Disabled until Myfaces test issues are resolved..
//    /**
//     * Test method for {@link javax.faces.component.UIViewRoot#processDecodes(javax.faces.context.FacesContext)}.
//     * 
//     * @throws Throwable
//     */
//    @Test
//    public void testProcessDecodes() throws Throwable
//    {
//        testProcessXXX(new TestRunner()
//        {
//            public void run() throws Throwable
//            {
//                _testimpl.processDecodes(_facesContext);
//            }
//        }, PhaseId.APPLY_REQUEST_VALUES, false, true, true);
//    }
//
//    /**
//     * Test method for {@link javax.faces.component.UIViewRoot#processValidators(javax.faces.context.FacesContext)}.
//     * 
//     * @throws Throwable
//     */
//    @Test
//    public void testProcessValidators() throws Throwable
//    {
//        testProcessXXX(new TestRunner()
//        {
//            public void run() throws Throwable
//            {
//                _testimpl.processValidators(_facesContext);
//            }
//        }, PhaseId.PROCESS_VALIDATIONS, false, true, true);
//    }
//
//    /**
//     * Test method for {@link javax.faces.component.UIViewRoot#processUpdates(javax.faces.context.FacesContext)}.
//     * 
//     * @throws Throwable
//     */
//    @Test
//    public void testProcessUpdates() throws Throwable
//    {
//        testProcessXXX(new TestRunner()
//        {
//            public void run() throws Throwable
//            {
//                _testimpl.processUpdates(_facesContext);
//            }
//        }, PhaseId.UPDATE_MODEL_VALUES, false, true, true);
//    }
//
//    /**
//     * Test method for {@link javax.faces.component.UIViewRoot#processApplication(javax.faces.context.FacesContext)}.
//     * 
//     * @throws Throwable
//     */
//    @Test
//    public void testProcessApplication() throws Throwable
//    {
//        testProcessXXX(new TestRunner()
//        {
//            public void run() throws Throwable
//            {
//                _testimpl.processApplication(_facesContext);
//            }
//        }, PhaseId.INVOKE_APPLICATION, false, true, true);
//    }
//
//    /**
//     * Test method for {@link javax.faces.component.UIViewRoot#encodeBegin(javax.faces.context.FacesContext)}.
//     * 
//     * @throws Throwable
//     */
//    @Test
//    public void testEncodeBegin() throws Throwable
//    {
//        testProcessXXX(new TestRunner()
//        {
//            public void run() throws Throwable
//            {
//                _testimpl.encodeBegin(_facesContext);
//            }
//        }, PhaseId.RENDER_RESPONSE, false, true, false);
//    }
//
//    /**
//     * Test method for {@link javax.faces.component.UIViewRoot#encodeEnd(javax.faces.context.FacesContext)}.
//     * 
//     * @throws Throwable
//     */
//    @Test
//    public void testEncodeEnd() throws Throwable
//    {
//        testProcessXXX(new TestRunner()
//        {
//            public void run() throws Throwable
//            {
//                _testimpl.encodeEnd(_facesContext);
//            }
//        }, PhaseId.RENDER_RESPONSE, false, false, true);
//    }
//
//    @Test
//    public void testEventQueue() throws Exception
//    {
//        FacesEvent event = _mocksControl.createMock(FacesEvent.class);
//        expect(event.getPhaseId()).andReturn(PhaseId.APPLY_REQUEST_VALUES).anyTimes();
//        UIComponent component = _mocksControl.createMock(UIComponent.class);
//        expect(event.getComponent()).andReturn(component).anyTimes();
//        component.broadcast(same(event));
//        _testimpl.queueEvent(event);
//
//        event = _mocksControl.createMock(FacesEvent.class);
//        expect(event.getPhaseId()).andReturn(PhaseId.PROCESS_VALIDATIONS).anyTimes();
//        _testimpl.queueEvent(event);
//
//        _mocksControl.replay();
//        _testimpl.processDecodes(_facesContext);
//        _mocksControl.verify();
//    }
//
//    @Test
//    public void testEventQueueWithAbortExcpetion() throws Exception
//    {
//        FacesEvent event = _mocksControl.createMock(FacesEvent.class);
//        expect(event.getPhaseId()).andReturn(PhaseId.INVOKE_APPLICATION).anyTimes();
//        UIComponent component = _mocksControl.createMock(UIComponent.class);
//        expect(event.getComponent()).andReturn(component).anyTimes();
//        component.broadcast(same(event));
//        expectLastCall().andThrow(new AbortProcessingException());
//        _testimpl.queueEvent(event);
//
//        event = _mocksControl.createMock(FacesEvent.class);
//        expect(event.getPhaseId()).andReturn(PhaseId.INVOKE_APPLICATION).anyTimes();
//        _testimpl.queueEvent(event);
//
//        _mocksControl.replay();
//        _testimpl.processApplication(_facesContext);
//        _mocksControl.verify();
//    }

    //
    //
    //
    // /**
    // * Test method for {@link javax.faces.component.UIViewRoot#saveState(javax.faces.context.FacesContext)}.
    // */
    // public void testSaveState()
    // {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // /**
    // * Test method for
    // * {@link javax.faces.component.UIViewRoot#restoreState(javax.faces.context.FacesContext, java.lang.Object)}.
    // */
    // public void testRestoreState()
    // {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // /**
    // * Test method for {@link javax.faces.component.UIViewRoot#UIViewRoot()}.
    // */
    // public void testUIViewRoot()
    // {
    // fail("Not yet implemented"); // TODO
    // }
    //
    //
    // /**
    // * Test method for {@link javax.faces.component.UIViewRoot#setLocale(java.util.Locale)}.
    // */
    // public void testSetLocale()
    // {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // /**
    // * Test method for {@link javax.faces.component.UIViewRoot#getRenderKitId()}.
    // */
    // public void testGetRenderKitId()
    // {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // /**
    // * Test method for {@link javax.faces.component.UIViewRoot#setRenderKitId(java.lang.String)}.
    // */
    // public void testSetRenderKitId()
    // {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // /**
    // * Test method for {@link javax.faces.component.UIViewRoot#getViewId()}.
    // */
    // public void testGetViewId()
    // {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // /**
    // * Test method for {@link javax.faces.component.UIViewRoot#setViewId(java.lang.String)}.
    // */
    // public void testSetViewId()
    // {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // /**
    // * Test method for {@link javax.faces.component.UIViewRoot#addPhaseListener(javax.faces.event.PhaseListener)}.
    // */
    // public void testAddPhaseListener()
    // {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // /**
    // * Test method for {@link javax.faces.component.UIViewRoot#removePhaseListener(javax.faces.event.PhaseListener)}.
    // */
    // public void testRemovePhaseListener()
    // {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // /**
    // * Test method for {@link javax.faces.component.UIViewRoot#getBeforePhaseListener()}.
    // */
    // public void testGetBeforePhaseListener()
    // {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // /**
    // * Test method for {@link javax.faces.component.UIViewRoot#setBeforePhaseListener(javax.el.MethodExpression)}.
    // */
    // public void testSetBeforePhaseListener()
    // {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // /**
    // * Test method for {@link javax.faces.component.UIViewRoot#getAfterPhaseListener()}.
    // */
    // public void testGetAfterPhaseListener()
    // {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // /**
    // * Test method for {@link javax.faces.component.UIViewRoot#setAfterPhaseListener(javax.el.MethodExpression)}.
    // */
    // public void testSetAfterPhaseListener()
    // {
    // fail("Not yet implemented"); // TODO
    // }
    //
    // /**
    // * Test method for {@link javax.faces.component.UIViewRoot#getFamily()}.
    // */
    // public void testGetFamily()
    // {
    // fail("Not yet implemented"); // TODO
    // }
    //

    private void testProcessXXX(TestRunner runner, PhaseId phaseId, boolean expectSuperCall, boolean checkBefore,
            boolean checkAfter) throws Throwable
    {
        expect(_lifecycleFactory.getLifecycle(eq(LifecycleFactory.DEFAULT_LIFECYCLE))).andReturn(_lifecycle);
        expect(_externalContext.getInitParameter(eq(FacesServlet.LIFECYCLE_ID_ATTR))).andReturn(null).anyTimes();

        PhaseEvent event = new PhaseEvent(_facesContext, phaseId, _lifecycle);

        if (expectSuperCall)
        {
            _testimpl = _mocksControl.createMock(UIViewRoot.class, new Method[]{UIViewRoot.class.getMethod(
                    "isRendered", new Class[0])});
        }

        MethodExpression beforeListener = _mocksControl.createMock(MethodExpression.class);
        _testimpl.setBeforePhaseListener(beforeListener);

        MethodExpression afterListener = _mocksControl.createMock(MethodExpression.class);
        _testimpl.setAfterPhaseListener(afterListener);

        Method[] mockedMethods = new Method[] {
                PhaseListener.class.getMethod("beforePhase", new Class[] { PhaseEvent.class }),
                PhaseListener.class.getMethod("afterPhase", new Class[] { PhaseEvent.class }) };
        PhaseListener phaseListener = _mocksControl.createMock(phaseListenerClasses.get(phaseId), mockedMethods);
        _testimpl.addPhaseListener(phaseListener);

        PhaseListener anyPhaseListener = _mocksControl.createMock(AnyPhasePhaseListener.class, mockedMethods);
        _testimpl.addPhaseListener(anyPhaseListener);

        PhaseListener restoreViewPhaseListener = _mocksControl.createMock(RestoreViewPhasePhaseListener.class,
                mockedMethods);
        _testimpl.addPhaseListener(restoreViewPhaseListener);

        _mocksControl.checkOrder(true);

        if (checkBefore)
        {
            expect(beforeListener.invoke(eq(_facesContext.getELContext()), aryEq(new Object[] { event }))).andReturn(
                    null);
            phaseListener.beforePhase(eq(event));
            anyPhaseListener.beforePhase(eq(event));
        }

        if (expectSuperCall)
        {
            expect(_testimpl.isRendered()).andReturn(false);
        }

        if (checkAfter)
        {
            expect(afterListener.invoke(eq(_facesContext.getELContext()), aryEq(new Object[] { event }))).andReturn(
                    null);
            phaseListener.afterPhase(eq(event));
            anyPhaseListener.afterPhase(eq(event));
        }

        _mocksControl.replay();
        runner.run();
        _mocksControl.verify();
    }

    private final class ActionListenerImplementation implements ActionListener
    {
        public int invocationCount = 0;
        
        public ActionEvent newActionEventFromListener;

        public ActionListenerImplementation(UICommand otherUiCommand)
        {
            // from spec: Queue one or more additional events, from the same source component
            // or a DIFFERENT one
            newActionEventFromListener = new ActionEvent(otherUiCommand);
        }

        public void processAction(ActionEvent actionEvent)
                throws AbortProcessingException
        {
            invocationCount++;
              
            newActionEventFromListener.queue();
            
            // Simulate infinite recursion,most likely coding error:
            actionEvent.queue();
        }
    }

    public static class MockLifeCycleFactory extends LifecycleFactory
    {

        @Override
        public void addLifecycle(String lifecycleId, Lifecycle lifecycle)
        {
            LIFECYCLEFACTORY.get().addLifecycle(lifecycleId, lifecycle);
        }

        @Override
        public Lifecycle getLifecycle(String lifecycleId)
        {
            return LIFECYCLEFACTORY.get().getLifecycle(lifecycleId);
        }

        @Override
        public Iterator<String> getLifecycleIds()
        {
            return LIFECYCLEFACTORY.get().getLifecycleIds();
        }

    }

    public static abstract class ApplyRequesValuesPhaseListener implements PhaseListener
    {
        public PhaseId getPhaseId()
        {
            return PhaseId.APPLY_REQUEST_VALUES;
        }
    }

    public static abstract class ProcessValidationsPhaseListener implements PhaseListener
    {
        public PhaseId getPhaseId()
        {
            return PhaseId.PROCESS_VALIDATIONS;
        }
    }

    public static abstract class UpdateModelValuesPhaseListener implements PhaseListener
    {
        public PhaseId getPhaseId()
        {
            return PhaseId.UPDATE_MODEL_VALUES;
        }
    }

    public static abstract class InvokeApplicationPhaseListener implements PhaseListener
    {
        public PhaseId getPhaseId()
        {
            return PhaseId.INVOKE_APPLICATION;
        }
    }

    public static abstract class AnyPhasePhaseListener implements PhaseListener
    {
        public PhaseId getPhaseId()
        {
            return PhaseId.ANY_PHASE;
        }
    }

    public static abstract class RestoreViewPhasePhaseListener implements PhaseListener
    {
        public PhaseId getPhaseId()
        {
            return PhaseId.RESTORE_VIEW;
        }
    }

    public static abstract class RenderResponsePhaseListener implements PhaseListener
    {
        public PhaseId getPhaseId()
        {
            return PhaseId.RENDER_RESPONSE;
        }
    }

    @Test
    public void testBroadcastEvents()
    {
        
        UICommand uiCommand = new UICommand();
        uiCommand.setId("idOfCommandOne");
        facesContext.getViewRoot().getChildren().add(uiCommand);
        
        // Spec 3.4.2.6 Event Broadcasting: During event broadcasting, a listener processing an event may
        // Queue one or more additional events from the same source component or a different one:
        // and the DIFFERENT ONE is the next UICommand instance
        UICommand differentUiCommand = new UICommand();
        uiCommand.setId("idOfdifferentUiCommand");
        facesContext.getViewRoot().getChildren().add(differentUiCommand);
        
        
        ActionListenerImplementation actionListener = new ActionListenerImplementation(differentUiCommand);
        uiCommand.addActionListener(actionListener);
        
        ActionListener differentActionListener = org.easymock.EasyMock.createNiceMock(ActionListener.class);
        differentActionListener.processAction(actionListener.newActionEventFromListener);
        org.easymock.EasyMock.expectLastCall().times(1);
        org.easymock.EasyMock.replay(differentActionListener);
        differentUiCommand.addActionListener(differentActionListener);
        
        // Simulates first event, in most cases click in GUI
        ActionEvent invokeApplicationEvent = new ActionEvent(uiCommand);
        invokeApplicationEvent.queue();
        
        // tested method: In this method is actionListener called and that
        // listener itself queues new event
        facesContext.getViewRoot().broadcastEvents(facesContext, PhaseId.INVOKE_APPLICATION);
        
        assertEquals(15, actionListener.invocationCount);
        org.easymock.EasyMock.verify(differentActionListener);
    }

}
