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

import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import junit.framework.TestCase;

import org.apache.myfaces.MyFacesAsserts;
import org.apache.myfaces.TestRunner;
import org.apache.myfaces.test.mock.MockFacesContext12;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class ViewHandlerTest extends TestCase
{
    private MockFacesContext12 _facesContext;
    private IMocksControl _mocksControl;
    private ExternalContext _externalContext;
    private TestViewHandler _testimpl;

    @Override
    protected void setUp() throws Exception
    {
        _mocksControl = EasyMock.createControl();
        _externalContext = _mocksControl.createMock(ExternalContext.class);
        _facesContext = new MockFacesContext12(_externalContext);
        _testimpl = new TestViewHandler();
    }

    /**
     * Test method for
     * {@link javax.faces.application.ViewHandler#calculateCharacterEncoding(javax.faces.context.FacesContext)}.
     */
    @SuppressWarnings("unchecked")
    public void testCalculateCharacterEncodingWithRequestHeaderContentType()
    {
        Map<String, String> map = _mocksControl.createMock(Map.class);
        expect(_externalContext.getRequestHeaderMap()).andReturn(map);
        expect(map.get(eq("Content-Type"))).andReturn("text/html;charset=UTF-8");
        _mocksControl.replay();
        assertEquals("UTF-8", _testimpl.calculateCharacterEncoding(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link javax.faces.application.ViewHandler#calculateCharacterEncoding(javax.faces.context.FacesContext)}.
     */
    public void testCalculateCharacterEncodingWithNoRequestContentTypeAndNoSession()
    {
        Map<String, String> emptyMap = Collections.emptyMap();

        expect(_externalContext.getRequestHeaderMap()).andReturn(emptyMap);
        expect(_externalContext.getSession(eq(false))).andReturn(null);
        _mocksControl.replay();
        assertNull(_testimpl.calculateCharacterEncoding(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link javax.faces.application.ViewHandler#calculateCharacterEncoding(javax.faces.context.FacesContext)}.
     */
    @SuppressWarnings("unchecked")
    public void testCalculateCharacterEncodingWithNoRequestContentTypeAndWithSessionButNoSessionValue()
    {
        Map<String, String> emptyMap = Collections.emptyMap();

        expect(_externalContext.getRequestHeaderMap()).andReturn(emptyMap);
        expect(_externalContext.getSession(eq(false))).andReturn(new Object());
        Map<String, Object> map = _mocksControl.createMock(Map.class);
        expect(_externalContext.getSessionMap()).andReturn(map);
        expect(map.get(eq(ViewHandler.CHARACTER_ENCODING_KEY))).andReturn(null);
        _mocksControl.replay();
        assertNull(_testimpl.calculateCharacterEncoding(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link javax.faces.application.ViewHandler#calculateCharacterEncoding(javax.faces.context.FacesContext)}.
     */
    @SuppressWarnings("unchecked")
    public void testCalculateCharacterEncodingWithNoRequestContentTypeAndWithSessionAndNoSessionValue()
    {
        Map<String, String> emptyMap = Collections.emptyMap();

        expect(_externalContext.getRequestHeaderMap()).andReturn(emptyMap);
        expect(_externalContext.getSession(eq(false))).andReturn(new Object());
        Map<String, Object> map = _mocksControl.createMock(Map.class);
        expect(_externalContext.getSessionMap()).andReturn(map);
        expect(map.get(eq(ViewHandler.CHARACTER_ENCODING_KEY))).andReturn("UTF-8");
        _mocksControl.replay();
        assertEquals("UTF-8", _testimpl.calculateCharacterEncoding(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for {@link javax.faces.application.ViewHandler#initView(javax.faces.context.FacesContext)}.
     * 
     * @throws Exception
     */
    public void testInitView() throws Exception
    {
        ViewHandler handler = _mocksControl
                                           .createMock(
                                                       ViewHandler.class,
                                                       new Method[] { ViewHandler.class
                                                                                       .getMethod(
                                                                                                  "calculateCharacterEncoding",
                                                                                                  new Class[] { FacesContext.class }) });
        expect(handler.calculateCharacterEncoding(_facesContext)).andReturn("xxx");
        _externalContext.setRequestCharacterEncoding(eq("xxx"));
        _mocksControl.replay();
        handler.initView(_facesContext);
        _mocksControl.verify();
    }

    /**
     * Test method for {@link javax.faces.application.ViewHandler#initView(javax.faces.context.FacesContext)}.
     * 
     * @throws Exception
     */
    public void testInitViewWithUnsupportedEncodingException() throws Exception
    {
        final ViewHandler handler = _mocksControl
                                                 .createMock(
                                                             ViewHandler.class,
                                                             new Method[] { ViewHandler.class
                                                                                             .getMethod(
                                                                                                        "calculateCharacterEncoding",
                                                                                                        new Class[] { FacesContext.class }) });
        expect(handler.calculateCharacterEncoding(_facesContext)).andReturn("xxx");
        _externalContext.setRequestCharacterEncoding(eq("xxx"));
        expectLastCall().andThrow(new UnsupportedEncodingException());
        _mocksControl.replay();
        MyFacesAsserts.assertException(FacesException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                handler.initView(_facesContext);
            }
        });
        _mocksControl.verify();
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

        @Override
        public String getWebsocketURL(FacesContext context, String channelAndToken)
        {
            throw new UnsupportedOperationException();
        }

    }
}
