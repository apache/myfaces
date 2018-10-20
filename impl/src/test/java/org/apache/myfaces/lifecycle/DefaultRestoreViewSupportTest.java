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
package org.apache.myfaces.lifecycle;

import static org.easymock.EasyMock.expect;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.faces.FacesException;

import org.apache.myfaces.test.MyFacesAsserts;
import org.apache.myfaces.test.FacesTestCase;
import org.apache.myfaces.test.TestRunner;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class DefaultRestoreViewSupportTest extends FacesTestCase
{

    private DefaultRestoreViewSupport _testimpl;

    protected void setUp() throws Exception
    {
        super.setUp();
        _testimpl = new DefaultRestoreViewSupport();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.lifecycle.DefaultRestoreViewSupport#processComponentBinding(javax.faces.context.FacesContext, javax.faces.component.UIComponent)}.
     */
    /*
    public void testProcessComponentBinding()
    {
        UIComponent root = _mocksControl.createMock(UIComponent.class);
        UIComponent testcomponent = _mocksControl.createMock(UIComponent.class);
        ValueExpression rootExpression = _mocksControl.createMock(ValueExpression.class);
        ValueExpression testExpression = _mocksControl.createMock(ValueExpression.class);
        
        _mocksControl.checkOrder(true);
        expect(root.getValueExpression(eq("binding"))).andReturn(rootExpression);
        expect(_facesContext.getELContext()).andReturn(_elContext);
        rootExpression.setValue(same(_elContext), same(root));
        expect(root.getFacetsAndChildren()).andReturn(Arrays.asList(new UIComponent[] { testcomponent }).iterator());
        expect(testcomponent.getValueExpression(eq("binding"))).andReturn(testExpression);
        expect(_facesContext.getELContext()).andReturn(_elContext);
        testExpression.setValue(same(_elContext), same(testcomponent));
        
        List<UIComponent> emptyList = Collections.emptyList();
        expect(testcomponent.getFacetsAndChildren()).andReturn(emptyList.iterator());

        _mocksControl.replay();
        _testimpl.processComponentBinding(_facesContext, root);
        _mocksControl.verify();
    }
    */

    /**
     * Test method for
     * {@link org.apache.myfaces.lifecycle.DefaultRestoreViewSupport#calculateViewId(javax.faces.context.FacesContext)}.
     */
    public void testCalculateViewIdFromRequestAttributeIncludePathInfo()
    {
        _mocksControl.checkOrder(true);
        expect(_facesContext.getExternalContext()).andReturn(_externalContext);
        Map<String, Object> map = new HashMap<String, Object>();
        String expectedValue = "javax.servlet.include.path_info_VIEWID";
        map.put("javax.servlet.include.path_info", expectedValue);
        expect(_externalContext.getRequestMap()).andReturn(map);
        //expect(_facesContext.getApplication()).andReturn(_application);
        //ViewHandler viewHandler = _mocksControl.createMock(ViewHandler.class);
        //expect(_application.getViewHandler()).andReturn(viewHandler);
        //expect(viewHandler.deriveViewId(
        //        same(_facesContext), eq(expectedValue))).andReturn(expectedValue);
        _mocksControl.replay();
        assertEquals(expectedValue, _testimpl.calculateViewId(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.lifecycle.DefaultRestoreViewSupport#calculateViewId(javax.faces.context.FacesContext)}.
     */
    public void testCalculateViewIdFromRequestPathInfo()
    {
        _mocksControl.checkOrder(true);
        expect(_facesContext.getExternalContext()).andReturn(_externalContext);
        
        Map<String, Object> emptyMap = Collections.emptyMap();
        expect(_externalContext.getRequestMap()).andReturn(emptyMap);
        String expectedValue = "requestPathInfo_VIEWID";
        expect(_externalContext.getRequestPathInfo()).andReturn(expectedValue);
        //expect(_facesContext.getApplication()).andReturn(_application);
        //ViewHandler viewHandler = _mocksControl.createMock(ViewHandler.class);
        //expect(_application.getViewHandler()).andReturn(viewHandler);
        //expect(viewHandler.deriveViewId(
        //        same(_facesContext), eq(expectedValue))).andReturn(expectedValue);

        _mocksControl.replay();
        assertEquals(expectedValue, _testimpl.calculateViewId(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.lifecycle.DefaultRestoreViewSupport#calculateViewId(javax.faces.context.FacesContext)}.
     */
    public void testCalculateViewIdFromRequestAttributeIncludeServletPath()
    {
        _mocksControl.checkOrder(true);
        expect(_facesContext.getExternalContext()).andReturn(_externalContext);
        Map<String, Object> map = new HashMap<String, Object>();
        String expectedValue = "javax.servlet.include.servlet_path_VIEWID";
        map.put("javax.servlet.include.servlet_path", expectedValue);
        expect(_externalContext.getRequestMap()).andReturn(map);
        expect(_externalContext.getRequestPathInfo()).andReturn(null);
        //expect(_facesContext.getApplication()).andReturn(_application);
        //ViewHandler viewHandler = _mocksControl.createMock(ViewHandler.class);
        //expect(_application.getViewHandler()).andReturn(viewHandler);
        //expect(viewHandler.deriveViewId(
        //        same(_facesContext), eq(expectedValue))).andReturn(expectedValue);

        _mocksControl.replay();
        assertEquals(expectedValue, _testimpl.calculateViewId(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.lifecycle.DefaultRestoreViewSupport#calculateViewId(javax.faces.context.FacesContext)}.
     */
    public void testCalculateViewIdFromRequestServletPath()
    {
        _mocksControl.checkOrder(true);
        expect(_facesContext.getExternalContext()).andReturn(_externalContext);
        
        Map<String, Object> emptyMap = Collections.emptyMap();
        
        expect(_externalContext.getRequestMap()).andReturn(emptyMap);
        expect(_externalContext.getRequestPathInfo()).andReturn(null);
        String expectedValue = "RequestServletPath_VIEWID";
        expect(_externalContext.getRequestServletPath()).andReturn(expectedValue);
        //expect(_facesContext.getApplication()).andReturn(_application);
        //ViewHandler viewHandler = _mocksControl.createMock(ViewHandler.class);
        //expect(_application.getViewHandler()).andReturn(viewHandler);
        //expect(viewHandler.deriveViewId(
        //        same(_facesContext), eq(expectedValue))).andReturn(expectedValue);

        _mocksControl.replay();
        assertEquals(expectedValue, _testimpl.calculateViewId(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.lifecycle.DefaultRestoreViewSupport#calculateViewId(javax.faces.context.FacesContext)}.
     */
    public void testCalculateViewIdFacesException()
    {
        _mocksControl.checkOrder(true);
        expect(_facesContext.getExternalContext()).andReturn(_externalContext);
        
        Map<String, Object> emptyMap = Collections.emptyMap();
        
        expect(_externalContext.getRequestMap()).andReturn(emptyMap);
        expect(_externalContext.getRequestPathInfo()).andReturn(null);
        expect(_externalContext.getRequestServletPath()).andReturn(null);

        _mocksControl.replay();
        MyFacesAsserts.assertException(FacesException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testimpl.calculateViewId(_facesContext);
            }
        });
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.lifecycle.DefaultRestoreViewSupport#isPostback(javax.faces.context.FacesContext)}.
     */
    public void testIsPostback()
    {
        // TODO: not testable unless static call to RendererUtils is removed
    }

}
