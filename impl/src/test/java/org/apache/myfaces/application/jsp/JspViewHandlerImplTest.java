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
package org.apache.myfaces.application.jsp;

import static org.apache.myfaces.Assert.assertException;
import org.apache.myfaces.FacesTestCase;
import org.apache.myfaces.TestRunner;
import org.apache.myfaces.shared.application.InvalidViewIdException;
import org.apache.myfaces.shared.application.ViewHandlerSupport;

import static org.easymock.EasyMock.*;
import org.easymock.IAnswer;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.ResponseStateManager;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class JspViewHandlerImplTest extends FacesTestCase
{
    private JspViewHandlerImpl _testimpl;

    protected void setUp() throws Exception
    {
        super.setUp();
        _testimpl = new JspViewHandlerImpl();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#calculateLocale(javax.faces.context.FacesContext)}.
     */
    public void testCalculateLocaleNPE()
    {
        assertException(NullPointerException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testimpl.calculateLocale(null);
            }
        });

        // Iterator<Locale> requstLocales = Arrays.asList(new Locale[] { Locale.GERMANY, Locale.US }).iterator();
        // expect(_externalContext.getRequestLocales()).andReturn(requstLocales);
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#calculateLocale(javax.faces.context.FacesContext)}.
     */
    public void testCalculateLocaleWithNoRequestLocaleAndNoDefaultLocale()
    {
        List<Locale> emptyList = Collections.emptyList();
        
        expectApplicationAndExternalContextGet();
        expect(_externalContext.getRequestLocales()).andReturn(emptyList.iterator());
        expect(_application.getDefaultLocale()).andReturn(null);
        _mocksControl.replay();
        assertEquals(Locale.getDefault(), _testimpl.calculateLocale(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#calculateLocale(javax.faces.context.FacesContext)}.
     */
    public void testCalculateLocaleWithNoRequestLocaleAndDefaultLocale()
    {
        List<Locale> emptyList = Collections.emptyList();
        
        expectApplicationAndExternalContextGet();
        expect(_externalContext.getRequestLocales()).andReturn(emptyList.iterator());
        expect(_application.getDefaultLocale()).andReturn(Locale.KOREAN);
        _mocksControl.replay();
        assertEquals(Locale.KOREAN, _testimpl.calculateLocale(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#calculateLocale(javax.faces.context.FacesContext)}.
     */
    public void testCalculateLocaleWithNoMatchingRequestLocaleAndNoSupportedLocaleAndDefaultLocale()
    {
        List<Locale> emptyList = Collections.emptyList();
        
        expectApplicationAndExternalContextGet();
        Iterator<Locale> requestLocales = Arrays.asList(new Locale[] { Locale.KOREAN }).iterator();
        expect(_externalContext.getRequestLocales()).andReturn(requestLocales);
        expect(_application.getSupportedLocales()).andReturn(emptyList.iterator());
        expect(_application.getDefaultLocale()).andReturn(Locale.GERMAN);
        _mocksControl.replay();
        assertEquals(Locale.GERMAN, _testimpl.calculateLocale(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#calculateLocale(javax.faces.context.FacesContext)}.
     */
    public void testCalculateLocaleWithNoMatchingRequestLocaleWithSupportedLocaleAndDefaultLocale()
    {
        expectApplicationAndExternalContextGet();
        Iterator<Locale> requestLocales = Arrays.asList(new Locale[] { Locale.KOREAN }).iterator();
        expect(_externalContext.getRequestLocales()).andReturn(requestLocales);
        Iterator<Locale> supportedLocales = Arrays.asList(new Locale[] { Locale.CHINESE }).iterator();
        expect(_application.getSupportedLocales()).andReturn(supportedLocales);
        expect(_application.getDefaultLocale()).andReturn(Locale.GERMAN);
        _mocksControl.replay();
        assertEquals(Locale.GERMAN, _testimpl.calculateLocale(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#calculateLocale(javax.faces.context.FacesContext)}.
     */
    public void testCalculateLocaleWithMatchingRequestLocaleWithSupportedLocale()
    {
        expectApplicationAndExternalContextGet();
        Iterator<Locale> requestLocales = Arrays.asList(new Locale[] { Locale.KOREAN, Locale.GERMANY }).iterator();
        expect(_externalContext.getRequestLocales()).andReturn(requestLocales);
        final Collection<Locale> supportedLocales = Arrays.asList(new Locale[] { Locale.GERMANY });
        expect(_application.getSupportedLocales()).andAnswer(new IAnswer<Iterator<Locale>>()
        {
            public Iterator<Locale> answer() throws Throwable
            {
                return supportedLocales.iterator();
            }
        }).times(2);
        _mocksControl.replay();
        assertEquals(Locale.GERMANY, _testimpl.calculateLocale(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#calculateLocale(javax.faces.context.FacesContext)}.
     */
    public void testCalculateLocaleIfRequestLocaleLanguageMatchesSupportedLocaleWOCountry()
    {
        expectApplicationAndExternalContextGet();
        Iterator<Locale> requestLocales = Arrays.asList(new Locale[] { Locale.GERMANY, Locale.KOREAN }).iterator();
        expect(_externalContext.getRequestLocales()).andReturn(requestLocales);
        Iterator<Locale> supportedLocales = Arrays.asList(new Locale[] { Locale.GERMAN, Locale.KOREAN }).iterator();
        expect(_application.getSupportedLocales()).andReturn(supportedLocales);
        _mocksControl.replay();
        assertEquals(Locale.GERMAN, _testimpl.calculateLocale(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#calculateRenderKitId(javax.faces.context.FacesContext)}.
     */
    public void testCalculateRenderKitIdFromRequest()
    {
        expect(_facesContext.getExternalContext()).andReturn(_externalContext).anyTimes();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ResponseStateManager.RENDER_KIT_ID_PARAM, "xxx");
        expect(_externalContext.getRequestMap()).andReturn(map);
        _mocksControl.replay();
        assertEquals("xxx", _testimpl.calculateRenderKitId(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#calculateLocale(javax.faces.context.FacesContext)}.
     */
    public void testCalculateLocaleWithNoMatchingRequestLocaleWithSupportedLocaleAndNoDefaultLocale()
    {
        expectApplicationAndExternalContextGet();
        Iterator<Locale> requestLocales = Arrays.asList(new Locale[] { Locale.ENGLISH }).iterator();
        expect(_externalContext.getRequestLocales()).andReturn(requestLocales);
        expect(_application.getDefaultLocale()).andReturn(null);
        Iterator<Locale> supportedLocales = Arrays.asList(new Locale[] { Locale.KOREAN }).iterator();
        expect(_application.getSupportedLocales()).andReturn(supportedLocales);
        _mocksControl.replay();
        assertEquals(Locale.getDefault(), _testimpl.calculateLocale(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#calculateRenderKitId(javax.faces.context.FacesContext)}.
     */
    public void testCalculateRenderKitIdFromApplicationDefault()
    {
        Map<String, Object> emptyMap = Collections.emptyMap();
        
        expectApplicationAndExternalContextGet();
        expect(_externalContext.getRequestMap()).andReturn(emptyMap);
        expect(_application.getDefaultRenderKitId()).andReturn("xxx");
        _mocksControl.replay();
        assertEquals("xxx", _testimpl.calculateRenderKitId(_facesContext));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#calculateRenderKitId(javax.faces.context.FacesContext)}.
     */
    public void testCalculateRenderKitIdFinalFallBack()
    {
        Map<String, Object> emptyMap = Collections.emptyMap();
        
        expectApplicationAndExternalContextGet();
        expect(_externalContext.getRequestMap()).andReturn(emptyMap);
        expect(_application.getDefaultRenderKitId()).andReturn(null);
        _mocksControl.replay();
        assertEquals(RenderKitFactory.HTML_BASIC_RENDER_KIT, _testimpl.calculateRenderKitId(_facesContext));
        _mocksControl.verify();
    }

    private void expectApplicationAndExternalContextGet()
    {
        expect(_facesContext.getExternalContext()).andReturn(_externalContext).anyTimes();
        expect(_facesContext.getApplication()).andReturn(_application).anyTimes();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#createView(javax.faces.context.FacesContext, java.lang.String)}.
     */
    public void testCreateView()
    {
        ViewHandlerSupport viewHandlerSupport = _mocksControl.createMock(ViewHandlerSupport.class);
        _testimpl.setViewHandlerSupport(viewHandlerSupport);
        expect(viewHandlerSupport.calculateViewId(same(_facesContext), eq("viewidxxx"))).andReturn("calculatedviewId");
        expect(_facesContext.getApplication()).andReturn(_application);
        ViewHandler viewHandler = _mocksControl.createMock(ViewHandler.class);
        expect(_application.getViewHandler()).andReturn(viewHandler);
        expect(_facesContext.getViewRoot()).andReturn(null);
        UIViewRoot viewRoot = _mocksControl.createMock(UIViewRoot.class);
        expect(_application.createComponent(eq(UIViewRoot.COMPONENT_TYPE))).andReturn(viewRoot);

        expect(viewHandler.calculateRenderKitId(same(_facesContext))).andReturn("renderkitid");
        Locale locale = new Locale("xxx");
        expect(viewHandler.calculateLocale(same(_facesContext))).andReturn(locale);

        viewRoot.setLocale(locale);
        viewRoot.setRenderKitId("renderkitid");
        viewRoot.setViewId("calculatedviewId");

        _mocksControl.replay();
        assertEquals(viewRoot, _testimpl.createView(_facesContext, "viewidxxx"));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#createView(javax.faces.context.FacesContext, java.lang.String)}.
     * 
     * @throws Exception
     */
    public void testCreateViewWithInvalidViewId() throws Exception
    {
        ViewHandlerSupport viewHandlerSupport = _mocksControl.createMock(ViewHandlerSupport.class);
        _testimpl.setViewHandlerSupport(viewHandlerSupport);
        expect(viewHandlerSupport.calculateViewId(same(_facesContext), eq("viewidxxx"))).andThrow(
                new InvalidViewIdException("xxx"));

        expect(_facesContext.getExternalContext()).andReturn(_externalContext);
        HttpServletResponse httpServletResponse = _mocksControl.createMock(HttpServletResponse.class);
        expect(_externalContext.getResponse()).andReturn(httpServletResponse);
        httpServletResponse.sendError(eq(HttpServletResponse.SC_NOT_FOUND), (String) anyObject());
        _facesContext.responseComplete();

        expect(_facesContext.getApplication()).andReturn(_application);
        ViewHandler viewHandler = _mocksControl.createMock(ViewHandler.class);
        expect(_application.getViewHandler()).andReturn(viewHandler);
        expect(_facesContext.getViewRoot()).andReturn(null);
        UIViewRoot viewRoot = _mocksControl.createMock(UIViewRoot.class);
        expect(_application.createComponent(eq(UIViewRoot.COMPONENT_TYPE))).andReturn(viewRoot);

        expect(viewHandler.calculateRenderKitId(same(_facesContext))).andReturn("renderkitid");
        Locale locale = new Locale("xxx");
        expect(viewHandler.calculateLocale(same(_facesContext))).andReturn(locale);

        viewRoot.setLocale(locale);
        viewRoot.setRenderKitId("renderkitid");
        viewRoot.setViewId("viewidxxx");

        _mocksControl.replay();
        assertEquals(viewRoot, _testimpl.createView(_facesContext, "viewidxxx"));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#createView(javax.faces.context.FacesContext, java.lang.String)}.
     */
    public void testCreateViewWithExistingViewRoot()
    {
        ViewHandlerSupport viewHandlerSupport = _mocksControl.createMock(ViewHandlerSupport.class);
        _testimpl.setViewHandlerSupport(viewHandlerSupport);
        expect(viewHandlerSupport.calculateViewId(same(_facesContext), eq("viewidxxx"))).andReturn("calculatedviewId");
        expect(_facesContext.getApplication()).andReturn(_application);
        ViewHandler viewHandler = _mocksControl.createMock(ViewHandler.class);
        expect(_application.getViewHandler()).andReturn(viewHandler);

        UIViewRoot existingViewRoot = _mocksControl.createMock(UIViewRoot.class);
        Locale locale = new Locale("xxx");
        expect(existingViewRoot.getLocale()).andReturn(locale);
        expect(existingViewRoot.getRenderKitId()).andReturn("renderkitid");

        expect(_facesContext.getViewRoot()).andReturn(existingViewRoot);
        UIViewRoot newViewRoot = _mocksControl.createMock(UIViewRoot.class);
        expect(_application.createComponent(eq(UIViewRoot.COMPONENT_TYPE))).andReturn(newViewRoot);

        newViewRoot.setLocale(locale);
        newViewRoot.setRenderKitId("renderkitid");
        newViewRoot.setViewId("calculatedviewId");

        _mocksControl.replay();
        assertEquals(newViewRoot, _testimpl.createView(_facesContext, "viewidxxx"));
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#getActionURL(javax.faces.context.FacesContext, java.lang.String)}.
     */
    public void testGetActionURL()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#getResourceURL(javax.faces.context.FacesContext, java.lang.String)}.
     */
    public void testGetResourceURL()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#renderView(javax.faces.context.FacesContext, javax.faces.component.UIViewRoot)}.
     */
    public void testRenderView()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#restoreView(javax.faces.context.FacesContext, java.lang.String)}.
     */
    public void testRestoreView()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.application.jsp.JspViewHandlerImpl#writeState(javax.faces.context.FacesContext)}.
     */
    public void testWriteState()
    {
        // TODO
    }
}
