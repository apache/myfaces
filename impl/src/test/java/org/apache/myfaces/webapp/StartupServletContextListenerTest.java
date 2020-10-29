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
package org.apache.myfaces.webapp;

import java.lang.reflect.Field;
import java.util.Locale;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test cases for StartupServletContextListener
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@RunWith(JUnit4.class)
public class StartupServletContextListenerTest extends AbstractJsfTestCase
{
    
    private StartupServletContextListener _listener;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        _listener = new StartupServletContextListener();
    }

    @Override
    public void tearDown() throws Exception
    {
        _listener = null;
        
        super.tearDown();
    }

    /**
     * Verifies the calls to FacesInitializer on contextInitialized()
     */
    @Test
    public void testContextInitializedInitializerCalled()
    {
        FacesInitializer initializer = EasyMock.createMock(FacesInitializer.class);
        EasyMock.expect(initializer.initStartupFacesContext(servletContext)).andReturn(facesContext).once();
        initializer.initFaces(servletContext);
        EasyMock.expectLastCall().once();
        initializer.destroyStartupFacesContext(facesContext);
        EasyMock.expectLastCall().once();
        EasyMock.replay(initializer);
        _setFacesInitializer(initializer);
        
        _listener.contextInitialized(new ServletContextEvent(servletContext));
        EasyMock.verify(initializer);
    }
    
    /**
     * Verifies the calls to FacesInitializer on contextDestroyed()
     */
    @Test
    public void testContextDestroyedInitializerCalled()
    {
        _setServletContext(servletContext);
        
        FacesInitializer initializer = EasyMock.createMock(FacesInitializer.class);
        EasyMock.expect(initializer.initShutdownFacesContext(servletContext)).andReturn(facesContext).once();
        initializer.destroyFaces(servletContext);
        EasyMock.expectLastCall().once();
        initializer.destroyShutdownFacesContext(facesContext);
        EasyMock.expectLastCall().once();
        EasyMock.replay(initializer);
        _setFacesInitializer(initializer);
        
        _listener.contextDestroyed(new ServletContextEvent(servletContext));
        EasyMock.verify(initializer);
    }
    
    /**
     * Verifies the FacesContext handling at application startup.
     */
    @Test
    public void testContextInitializedFacesContextAvailable()
    {
        // release the current FacesContext to enforce setCurrentInstance(null)
        facesContext.release();
        
        final FacesInitializer realInitializer = FacesInitializerFactory.getFacesInitializer(servletContext);
        final AssertFacesContextAnswer assertAnswer = new AssertFacesContextAnswer();
        
        FacesInitializer mockInitializer = EasyMock.createMock(FacesInitializer.class);
        
        // initStartupFacesContext pass through to realInitializer
        EasyMock.expect(mockInitializer.initStartupFacesContext(servletContext))
                .andAnswer(new IAnswer<FacesContext>()
        {

            public FacesContext answer() throws Throwable
            {
                assertAnswer.facesContext = realInitializer.initStartupFacesContext(servletContext);
                return assertAnswer.facesContext;
            }
            
        });
        
        // initFaces with assert answer
        mockInitializer.initFaces(servletContext);
        EasyMock.expectLastCall().andStubAnswer(assertAnswer);
        
        // destroyStartupFacesContext pass through to realInitializer
        mockInitializer.destroyStartupFacesContext(EasyMock.isA(FacesContext.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>()
        {

            public Object answer() throws Throwable
            {
                FacesContext argCtx = (FacesContext) EasyMock.getCurrentArguments()[0];
                Assert.assertNotNull(argCtx);
                Assert.assertEquals(assertAnswer.facesContext, argCtx); // must be the same
                
                realInitializer.destroyStartupFacesContext(argCtx);
                
                return null;
            }
                    
                
        });
        
        EasyMock.replay(mockInitializer);
        _setFacesInitializer(mockInitializer);
        
        _listener.contextInitialized(new ServletContextEvent(servletContext));
        EasyMock.verify(mockInitializer);
        
        Assert.assertNull(FacesContext.getCurrentInstance()); // must be null by now
    }
    
    /**
     * Verifies the FacesContext handling at application shutdown.
     */
    @Test
    public void testContextDestroyedFacesContextAvailable()
    {
        _setServletContext(servletContext);
        
        // release the current FacesContext to enforce setCurrentInstance(null)
        facesContext.release();
        
        final FacesInitializer realInitializer = FacesInitializerFactory.getFacesInitializer(servletContext);
        final AssertFacesContextAnswer assertAnswer = new AssertFacesContextAnswer();
        
        FacesInitializer mockInitializer = EasyMock.createMock(FacesInitializer.class);
        
        // initShutdownFacesContext pass through to realInitializer
        EasyMock.expect(mockInitializer.initShutdownFacesContext(servletContext))
                .andAnswer(new IAnswer<FacesContext>()
        {

            public FacesContext answer() throws Throwable
            {
                assertAnswer.facesContext = realInitializer.initShutdownFacesContext(servletContext);
                return assertAnswer.facesContext;
            }
            
        });
        
        // destroyFaces with assert answer
        mockInitializer.destroyFaces(servletContext);
        EasyMock.expectLastCall().andStubAnswer(assertAnswer);
        
        // destroyShutdownFacesContext pass through to realInitializer
        mockInitializer.destroyShutdownFacesContext(EasyMock.isA(FacesContext.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>()
        {

            public Object answer() throws Throwable
            {
                FacesContext argCtx = (FacesContext) EasyMock.getCurrentArguments()[0];
                Assert.assertNotNull(argCtx);
                Assert.assertEquals(assertAnswer.facesContext, argCtx); // must be the same
                
                realInitializer.destroyShutdownFacesContext(argCtx);
                
                return null;
            }
                    
                
        });
        
        EasyMock.replay(mockInitializer);
        _setFacesInitializer(mockInitializer);
        
        _listener.contextDestroyed(new ServletContextEvent(servletContext));
        EasyMock.verify(mockInitializer);
        
        Assert.assertNull(FacesContext.getCurrentInstance());  // must be null by now
    }
    
    /**
     * Sets the ServletContext on the StartupServletContextListener
     * @param servletContext
     */
    private void _setServletContext(ServletContext servletContext)
    {
        try
        {
            Field field = StartupServletContextListener.class.getDeclaredField("_servletContext");
            field.setAccessible(true);
            field.set(_listener, servletContext);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not set ServletContext for test", e);
        }
        
    }
    
    /**
     * Sets the FacesInitializer on the StartupServletContextListener
     * @param facesInitializer
     */
    private void _setFacesInitializer(FacesInitializer facesInitializer)
    {
        try
        {
            Field field = StartupServletContextListener.class.getDeclaredField("_facesInitializer");
            field.setAccessible(true);
            field.set(_listener, facesInitializer);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not set FacesInitializer for test", e);
        }
        
    }
    
    /**
     * Helper class to do assertions on the StartupFacesContextImpl.
     * @author Jakob Korherr
     */
    private final class AssertFacesContextAnswer implements IAnswer<Object>
    {

        private FacesContext facesContext; // set by the first answer in the test case
        
        public Object answer() throws Throwable
        {
            Assert.assertEquals(facesContext, FacesContext.getCurrentInstance());
            Assert.assertNotNull(facesContext);
            Assert.assertNotNull(facesContext.getApplication());
            Assert.assertNotNull(facesContext.getExternalContext());
            Assert.assertNotNull(facesContext.getExceptionHandler());
            Assert.assertEquals(facesContext.getApplication()
                    .getProjectStage().equals(ProjectStage.Production),
                    facesContext.isProjectStage(ProjectStage.Production));
            UIViewRoot viewRoot = facesContext.getViewRoot();
            Assert.assertNotNull(viewRoot);
            Assert.assertEquals(Locale.getDefault(), viewRoot.getLocale());
            
            return null;
        }

    }
    
}
