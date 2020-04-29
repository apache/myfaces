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
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;

import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test cases for MyFacesServlet
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@RunWith(JUnit4.class)
public class MyFacesServletTest extends AbstractJsfTestCase
{
    
    private MyFacesServlet _servlet;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        _servlet = new MyFacesServlet();
    }

    @Override
    public void tearDown() throws Exception
    {
        _servlet = null;
        
        super.tearDown();
    }

    /**
     * Verifies the calls to FacesInitializer on init()
     * @throws ServletException 
     */
    @Test
    public void testInitInitializerCalled() throws ServletException
    {
        FacesInitializer initializer = EasyMock.createMock(FacesInitializer.class);
        EasyMock.expect(initializer.initStartupFacesContext(servletContext)).andReturn(facesContext).once();
        initializer.initFaces(servletContext);
        EasyMock.expectLastCall().once();
        initializer.destroyStartupFacesContext(facesContext);
        EasyMock.expectLastCall().once();
        
        // create Mock for ServletConfig
        ServletConfig servletConfig = EasyMock.createMock(ServletConfig.class);
        EasyMock.expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
        EasyMock.expect(servletConfig.getInitParameter(EasyMock.isA(String.class))).andReturn(null).anyTimes();
        
        EasyMock.replay(initializer, servletConfig);
        _setFacesInitializer(initializer);
        
        _servlet.init(servletConfig);
        EasyMock.verify(initializer);
    }
    
    /**
     * Verifies the FacesContext handling at application startup.
     * @throws ServletException 
     */
    @Test
    public void testInitFacesContextAvailable() throws ServletException
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
        
        // create Mock for ServletConfig
        ServletConfig servletConfig = EasyMock.createMock(ServletConfig.class);
        EasyMock.expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
        EasyMock.expect(servletConfig.getInitParameter(EasyMock.isA(String.class))).andReturn(null).anyTimes();
        
        EasyMock.replay(mockInitializer, servletConfig);
        _setFacesInitializer(mockInitializer);
        
        _servlet.init(servletConfig);
        EasyMock.verify(mockInitializer);
        
        Assert.assertNull(FacesContext.getCurrentInstance()); // must be null by now
    }
    
    /**
     * Sets the FacesInitializer on the MyFacesServlet
     * @param facesInitializer
     */
    private void _setFacesInitializer(FacesInitializer facesInitializer)
    {
        try
        {
            Field field = MyFacesServlet.class.getDeclaredField("_facesInitializer");
            field.setAccessible(true);
            field.set(_servlet, facesInitializer);
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
