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

import org.apache.myfaces.Assert;
import org.apache.myfaces.FacesTestCase;
import org.apache.myfaces.TestRunner;
import org.apache.myfaces.application.DefaultViewHandlerSupport.FacesServletMapping;

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
        // extension mapping
        assertCalculateViewId("xx.jsp", "/xx.faces", null, ".jsp", "xx.faces");
        assertCalculateViewId("xx.jspx", "/xx.faces", null, ".jspx", "xx.faces");
        assertCalculateViewId("xx.jsp", "/xx.jsf", null, ".jsp", "xx.jsf");
        assertCalculateViewId("xx.jspx", "/xx.jsf", null, ".jspx", "xx.jsf");

        // path mapping
        assertCalculateViewId("xx.jsp", "/faces", "/xx.jsp", ".jsp", "xx.jsp");
        assertCalculateViewId("xx.xyz", "/faces", "/xx.xyz", ".jsp", "xx.xyz");
    }
    
    private void assertCalculateViewId(
    		String expectedViewId, String servletPath, String pathInfo, 
    			String contextSuffix, String viewId) throws Exception
    {
    	DefaultViewHandlerSupport support = createdMockedViewHandlerSupport();
    	
    	expect(support.getContextSuffix(same(_facesContext))).andReturn(contextSuffix);
    	expect(support.getFacesServletMapping(same(_facesContext))).andReturn(
    			DefaultViewHandlerSupport.calculateFacesServletMapping(servletPath, pathInfo));
    	expect(_facesContext.getExternalContext()).andReturn(_externalContext).anyTimes();
    	
    	_mocksControl.replay();
    	assertEquals(expectedViewId, support.calculateViewId(_facesContext, viewId));
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

        // extension mapping
        assertActionUrl("/context/faces/testViewId.faces", "/context", "/testViewId.faces", null, "/faces/testViewId.jsp");
        assertActionUrl("/ctx/faces/testViewId.jsf", "/ctx",  "/faces/testViewId.jsf", null, "/faces/testViewId.jsp");

        // path mapping
        assertActionUrl("/context/testViewId.jsx", "/context", "", "/testViewId.jsx", "/testViewId.jsx");
        assertActionUrl("/context/faces/testViewId.jsp", "/context", "/faces", "/testViewId.jsp", "/testViewId.jsp");
    }
    
    private void assertActionUrl(
    		String expectedActionUrl, String contextPath, String servletPath, 
    			String pathInfo, String viewId) throws Exception 
    {
    	DefaultViewHandlerSupport support = createdMockedViewHandlerSupport();
    	
    	expect(support.getContextSuffix(same(_facesContext))).andReturn(DEFAULT_SUFFIX);
    	expect(support.getFacesServletMapping(same(_facesContext))).andReturn(
    			DefaultViewHandlerSupport.calculateFacesServletMapping(servletPath, pathInfo));
    	expect(_facesContext.getExternalContext()).andReturn(_externalContext).anyTimes();
    	expect(_externalContext.getRequestContextPath()).andReturn(contextPath);
    	
    	_mocksControl.replay();
    	assertEquals(expectedActionUrl, support.calculateActionURL(_facesContext, viewId));
    	_mocksControl.reset();
    }

    private DefaultViewHandlerSupport createdMockedViewHandlerSupport() throws Exception
    {
        Class<DefaultViewHandlerSupport> supportClass = DefaultViewHandlerSupport.class;
        Class<?>[] parameterTypes = new Class[] { FacesContext.class };

        DefaultViewHandlerSupport support = _mocksControl.createMock(
            supportClass, new Method[] {
                // the methods "getFacesServletMapping" and "getContextSuffix" will be mocked
                supportClass.getDeclaredMethod("getFacesServletMapping", parameterTypes),
                supportClass.getDeclaredMethod("getContextSuffix", parameterTypes) 
            }
        );

        return support;
    }
    
    /**
     * Test method for
     * {@link org.apache.myfaces.application.DefaultViewHandlerSupport#calculateFacesServletMapping(String, String)}.
     */
	public void testCalculateFacesServletMapping() throws Exception
    {
        assertExtensionMapping(".jsf", "/index.jsf", null);
        assertExtensionMapping(".jsf", "/secure/login.jsf", null);

        assertPathBasedMapping("/faces", "/faces", null);
        assertPathBasedMapping("/faces", "/faces", "/index.jsp");
        assertPathBasedMapping("/faces", "/faces", "/secure/login.jsp");
    }

    /**
     * Convenience method that tests if the ViewHandlerSupport object knows that
     * the "given request" has been handled by a FacesServlet being postfix
     * mapped. Extract the path elements of a Request-URI according to the
     * Servlet specification and pass the servletPath and pathInfo element.
     * 
     * @param extension
     *            expected extension
     * @param servletPath
     *            servletPath of the "current request" (e.g. "/faces")
     * @param pathInfo
     *            <code>null</code>
     */
    private void assertExtensionMapping(
            String extension, String servletPath, String pathInfo)
    {
		FacesServletMapping mapping = 
		    DefaultViewHandlerSupport.calculateFacesServletMapping(servletPath, pathInfo);
        assertTrue(mapping.isExtensionMapping());
        assertEquals(extension, mapping.getExtension());
    }
	
	/**
     * Convenience method that tests if the ViewHandlerSupport object knows that
     * the "given request" has been handled by a FacesServlet being prefix
     * mapped. Extract the path elements of a Request-URI according to the
     * Servlet specification and pass the servletPath and pathInfo element.
     * 
     * @param prefix
     *            expected prefix
     * @param servletPath
     *            servletPath of the "current request" (e.g. "/faces")
     * @param pathInfo
     *            infoPath of the "current request" (e.g. "/login.jsp")
     */
    private void assertPathBasedMapping(
            String prefix, String servletPath, String pathInfo)
    {
		FacesServletMapping mapping = 
		    DefaultViewHandlerSupport.calculateFacesServletMapping(servletPath, pathInfo);
        assertFalse(mapping.isExtensionMapping());
        assertEquals(prefix, mapping.getPrefix());
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
