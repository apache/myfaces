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
package javax.faces.application;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.shale.test.mock.MockExternalContext12;
import org.apache.shale.test.mock.MockFacesContext12;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ViewHandlerTest extends TestCase
{
    private MockFacesContext12 _facesContext;

    protected void setUp() throws Exception
    {
        _facesContext = new MockFacesContext12();
    }

    /**
     * Test method for
     * {@link javax.faces.application.ViewHandler#calculateCharacterEncoding(javax.faces.context.FacesContext)}.
     */
    public void testCalculateCharacterEncodingWithRequestHeaderContentType()
    {
        TestExternalContext context = new TestExternalContext(null, null, null)
        {
            @Override
            public Object getSession(boolean create)
            {
                throw new AssertionFailedError("session access not allowed");
            }

            @Override
            public Map getSessionMap()
            {
                throw new AssertionFailedError("session access not allowed");
            }
        };
        _facesContext.setExternalContext(context);
        TestViewHandler handler = new TestViewHandler();
        HashMap hashMap = new HashMap();
        hashMap.put("Content-Type", "text/html;charset=UTF-8");
        context.setRequestParameterMap(hashMap);
        assertEquals("UTF-8", handler.calculateCharacterEncoding(_facesContext));
    }

    /**
     * Test method for
     * {@link javax.faces.application.ViewHandler#calculateCharacterEncoding(javax.faces.context.FacesContext)}.
     */
    public void testCalculateCharacterEncodingWithSession()
    {
        TestExternalContext context = new TestExternalContext(null, null, null);
        _facesContext.setExternalContext(context);
        TestViewHandler handler = new TestViewHandler();
        context.sessionMap = new HashMap();
        context.sessionMap.put(ViewHandler.CHARACTER_ENCODING_KEY, "UTF-8");
        // no session
        context.allowSession = true;
        assertEquals(null, handler.calculateCharacterEncoding(_facesContext));
        assertEquals(true, context.getSessionCalled);
        assertEquals(false, context.getSessionMapCalled);
        context.getSessionCalled = false;
        
        //simulate session
        context.session = new Object();
        assertEquals("UTF-8", handler.calculateCharacterEncoding(_facesContext));
        assertEquals(true, context.getSessionCalled);
        assertEquals(true, context.getSessionMapCalled);
    }

    /**
     * Test method for {@link javax.faces.application.ViewHandler#initView(javax.faces.context.FacesContext)}.
     */
    public void testInitView()
    {
        TestExternalContext context = new TestExternalContext(null, null, null) {
        };
        _facesContext.setExternalContext(context);
        TestViewHandler handler = new TestViewHandler() {
            @Override
            public String calculateCharacterEncoding(FacesContext context)
            {
                return "UTF-8";
            }
        };
        handler.initView(_facesContext);
        assertEquals("UTF-8", context.getRequestCharacterEncoding());
    }

    private static class TestViewHandler extends ViewHandler
    {        
        @Override
        public Locale calculateLocale(FacesContext context)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String calculateRenderKitId(FacesContext context)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public UIViewRoot createView(FacesContext context, String viewId)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getActionURL(FacesContext context, String viewId)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getResourceURL(FacesContext context, String path)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void renderView(FacesContext context, UIViewRoot viewToRender) throws IOException, FacesException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public UIViewRoot restoreView(FacesContext context, String viewId)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeState(FacesContext context) throws IOException
        {
            throw new UnsupportedOperationException();
        }

    }

    private static class TestExternalContext extends MockExternalContext12
    {
        private Map requestHeaderMap = new HashMap();
        boolean getSessionCalled;
        boolean getSessionMapCalled;
        Map sessionMap;
        boolean allowSession;
        private Object session;
        private String requestCharacterEncoding;

        public TestExternalContext(ServletContext context, HttpServletRequest request, HttpServletResponse response)
        {
            super(context, request, response);
        }

        @Override
        public Object getSession(boolean create)
        {
            if (!allowSession)
            {
                fail("call not allowed");
            }
            assertEquals(false, create);
            getSessionCalled = true;
            return session;
        }

        @Override
        public Map getSessionMap()
        {
            if (!allowSession)
            {
                fail("call not allowed");
            }
            assertEquals(true, getSessionCalled);
            getSessionMapCalled = true;
            return sessionMap;
        }

        @Override
        public Map getRequestHeaderMap()
        {
            return requestHeaderMap;
        }

        @Override
        public void setRequestParameterMap(Map map)
        {
            requestHeaderMap = map;
        }

        @Override
        public String getRequestCharacterEncoding()
        {
            return requestCharacterEncoding;
        }

        @Override
        public void setRequestCharacterEncoding(String encoding) throws UnsupportedEncodingException
        {
            requestCharacterEncoding = encoding;
        }
        
        
        
    }
}
