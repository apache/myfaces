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
package org.apache.myfaces.application;

import static org.easymock.EasyMock.*;

import java.lang.reflect.Method;

import javax.faces.application.ViewHandler;
import javax.faces.context.FacesContext;
import javax.faces.webapp.FacesServlet;

import org.apache.myfaces.Assert;
import org.apache.myfaces.FacesTestCase;
import org.apache.myfaces.TestRunner;
import org.apache.myfaces.shared_impl.webapp.webxml.ServletMapping;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class DefaultViewHandlerSupportTest extends FacesTestCase
{

    private static final String DEFAULT_SUFFIX = ".jsp";

    /**
     * Test method for
     * {@link org.apache.myfaces.application.DefaultViewHandlerSupport#calculateViewId(javax.faces.context.FacesContext, java.lang.String)}.
     * 
     * @throws Exception
     */
    public void testCalculateViewId() throws Exception
    {
        assertCalculateViewId("xx.jsp", "*.faces", ".jsp", "xx.faces");
        assertCalculateViewId("xx.jspx", "*.faces", ".jspx", "xx.faces");
        assertCalculateViewId("xx.jsp", "*.jsf", ".jsp", "xx.jsf");
        assertCalculateViewId("xx.jspx", "*.jsf", ".jspx", "xx.jsf");
        assertCalculateViewId("xx.jsp", "/faces/*", ".jsp", "xx.jsp");
        assertCalculateViewId("xx.xyz", "/faces/*", ".jsp", "xx.xyz");
        assertCalculateViewId(null, "/faces/*", ".jsp", null);
        assertCalculateViewId(null, "/xyz", ".jsp", null);
    }

    private void assertCalculateViewId(String expectedViewId, String urlPattern, String contextSuffix, String viewId)
            throws Exception
    {
        ServletMapping servletMapping = new ServletMapping("servletName", FacesServlet.class, urlPattern);
        DefaultViewHandlerSupport testImpl = _mocksControl.createMock(DefaultViewHandlerSupport.class, new Method[] {
                DefaultViewHandlerSupport.class.getDeclaredMethod("calculateServletMapping",
                        new Class[] { FacesContext.class }),
                DefaultViewHandlerSupport.class.getDeclaredMethod("getContextSuffix",
                        new Class[] { FacesContext.class }) });
        expect(testImpl.getContextSuffix(same(_facesContext))).andReturn(contextSuffix);
        expect(testImpl.calculateServletMapping(same(_facesContext))).andReturn(servletMapping);
        expect(_facesContext.getExternalContext()).andReturn(_externalContext).anyTimes();
        _mocksControl.replay();
        assertEquals(expectedViewId, testImpl.calculateViewId(_facesContext, viewId));
        _mocksControl.reset();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.DefaultViewHandlerSupport#applyDefaultSuffix(javax.faces.context.FacesContext, java.lang.String)}.
     */
    public void testApplyDefaultSuffix()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.DefaultViewHandlerSupport#calculateActionURL(javax.faces.context.FacesContext, java.lang.String)}.
     * 
     * @throws
     * @throws Exception
     */
    public void testCalculateActionURL() throws Exception
    {
        Assert.assertException(IllegalArgumentException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                new DefaultViewHandlerSupport().calculateActionURL(_facesContext, "xxx");
            }
        });
        assertActionUrl("/context/testViewId.jsx", "/*", "/context", "/testViewId.jsx");
        assertActionUrl("/context/faces/testViewId.jsp", "/faces/*", "/context", "/testViewId.jsp");
        assertActionUrl("/context/faces/testViewId.faces", "*.faces", "/context", "/faces/testViewId" + DEFAULT_SUFFIX);
        assertActionUrl("/ctx/faces/testViewId.jsf", "*.jsf", "/ctx", "/faces/testViewId" + DEFAULT_SUFFIX);
    }

    private void assertActionUrl(String expectedActionURL, String urlPattern, String contextPath, String viewId)
            throws Exception
    {
        ServletMapping servletMapping = new ServletMapping("servletName", FacesServlet.class, urlPattern);
        DefaultViewHandlerSupport testImpl = _mocksControl.createMock(DefaultViewHandlerSupport.class, new Method[] {
                DefaultViewHandlerSupport.class.getDeclaredMethod("calculateServletMapping",
                        new Class[] { FacesContext.class }),
                DefaultViewHandlerSupport.class.getDeclaredMethod("getContextSuffix",
                        new Class[] { FacesContext.class }) });
        expect(testImpl.getContextSuffix(same(_facesContext))).andReturn(DEFAULT_SUFFIX);
        expect(testImpl.calculateServletMapping(same(_facesContext))).andReturn(servletMapping);
        expect(_facesContext.getExternalContext()).andReturn(_externalContext).anyTimes();
        expect(_externalContext.getRequestContextPath()).andReturn(contextPath);
        _mocksControl.replay();
        assertEquals(expectedActionURL, testImpl.calculateActionURL(_facesContext, viewId));
        _mocksControl.reset();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.DefaultViewHandlerSupport#getContextSuffix(javax.faces.context.FacesContext)}.
     */
    public void testGetContextSuffix()
    {
        DefaultViewHandlerSupport testImpl = new DefaultViewHandlerSupport();
        expect(_facesContext.getExternalContext()).andReturn(_externalContext);
        expect(_externalContext.getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME)).andReturn(null);
        _mocksControl.replay();
        assertEquals(ViewHandler.DEFAULT_SUFFIX, testImpl.getContextSuffix(_facesContext));
        _mocksControl.reset();
        expect(_facesContext.getExternalContext()).andReturn(_externalContext);
        expect(_externalContext.getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME)).andReturn(".xxx");
        _mocksControl.replay();
        assertEquals(".xxx", testImpl.getContextSuffix(_facesContext));
    }
}
