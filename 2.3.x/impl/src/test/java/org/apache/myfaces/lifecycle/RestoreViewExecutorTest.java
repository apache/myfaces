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

import static org.apache.myfaces.Assert.assertException;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.same;

import java.util.HashMap;
import java.util.Locale;

import javax.faces.application.ViewExpiredException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.event.PhaseId;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.view.ViewDeclarationLanguage;

import org.apache.myfaces.FacesTestCase;
import org.apache.myfaces.TestRunner;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class RestoreViewExecutorTest extends FacesTestCase
{
    private RestoreViewExecutor _testimpl;
    private ViewHandler _viewHandler;
    private RestoreViewSupport _restoreViewSupport;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        _viewHandler = _mocksControl.createMock(ViewHandler.class);
        _restoreViewSupport = _mocksControl.createMock(RestoreViewSupport.class);
        _testimpl = new RestoreViewExecutor();
        _testimpl.setRestoreViewSupport(_restoreViewSupport);
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.lifecycle.RestoreViewExecutor#execute(javax.faces.context.FacesContext)}.
     */
    public void testExecuteWithExistingViewRoot()
    {
        expect(_facesContext.getApplication()).andReturn(_application).anyTimes();
        expect(_application.getViewHandler()).andReturn(_viewHandler).anyTimes();
        _viewHandler.initView(eq(_facesContext));
        UIViewRoot viewRoot = _mocksControl.createMock(UIViewRoot.class);
        expect(_facesContext.getViewRoot()).andReturn(viewRoot).times(2);
        Locale expectedLocale = new Locale("xxx");
        expect(_facesContext.getExternalContext()).andReturn(_externalContext).anyTimes();
        expect(_externalContext.getRequestLocale()).andReturn(expectedLocale);
        viewRoot.setLocale(eq(expectedLocale));
        expect(viewRoot.getAfterPhaseListener()).andReturn(null);
        _restoreViewSupport.processComponentBinding(same(_facesContext), same(viewRoot));

        _mocksControl.replay();
        _testimpl.doPrePhaseActions(_facesContext);
        _testimpl.execute(_facesContext);
        _mocksControl.verify();
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.lifecycle.RestoreViewExecutor#execute(javax.faces.context.FacesContext)}.
     *//*
    public void testExecuteWOExistingViewRootNoPostBack()
    {
        setupWOExistingViewRoot();
        expect(_facesContext.getExternalContext()).andReturn(_externalContext).anyTimes();
        expect(_externalContext.getRequestMap()).andReturn(new HashMap());
        expect(_restoreViewSupport.isPostback(same(_facesContext))).andReturn(false);
        _facesContext.renderResponse();

        UIViewRoot viewRoot = _mocksControl.createMock(UIViewRoot.class);
        //viewRoot.subscribeToEvent(same(PostAddToViewEvent.class), same(viewRoot));

        ViewDeclarationLanguage vdl = _mocksControl.createMock(ViewDeclarationLanguage.class);
        //expect(_restoreViewSupport.deriveViewId(same(_facesContext), eq("calculatedViewId"))).andReturn("calculatedViewId");
        expect(_viewHandler.deriveLogicalViewId(same(_facesContext), eq("calculatedViewId"))).andReturn("calculatedViewId");
        expect(_facesContext.getResponseComplete()).andReturn(false);
        expect(_viewHandler.getViewDeclarationLanguage(same(_facesContext), eq("calculatedViewId")))
            .andReturn(vdl);
        expect(vdl.getViewMetadata(same(_facesContext), eq("calculatedViewId")))
            .andReturn(null);
        expect(_viewHandler.createView(same(_facesContext), eq("calculatedViewId"))).andReturn(viewRoot);
        expect(_application.getFlowHandler()).andReturn(null);
        _application.publishEvent(same(_facesContext), same(PostAddToViewEvent.class), same(viewRoot));
        _facesContext.setViewRoot(same(viewRoot));
        expect(_facesContext.getViewRoot()).andReturn(viewRoot);
        expect(viewRoot.getAfterPhaseListener()).andReturn(null);

        _mocksControl.replay();
        _testimpl.doPrePhaseActions(_facesContext);
        _testimpl.execute(_facesContext);
        _mocksControl.verify();
    }*/

    /**
     * Test method for
     * {@link org.apache.myfaces.lifecycle.RestoreViewExecutor#execute(javax.faces.context.FacesContext)}.
     *//*
    public void testExecuteWOExistingViewRootPostBack()
    {
        setupWOExistingViewRoot();
        expect(_facesContext.getExternalContext()).andReturn(_externalContext).anyTimes();
        expect(_externalContext.getRequestMap()).andReturn(new HashMap());
        expect(_restoreViewSupport.isPostback(same(_facesContext))).andReturn(true);
        _facesContext.setProcessingEvents(eq(true));
        UIViewRoot viewRoot = _mocksControl.createMock(UIViewRoot.class);
        expect(_viewHandler.restoreView(same(_facesContext), eq("calculatedViewId"))).andReturn(viewRoot);
        _restoreViewSupport.processComponentBinding(same(_facesContext), same(viewRoot));
        _facesContext.setViewRoot(same(viewRoot));
        _facesContext.setProcessingEvents(eq(false));
        expect(_facesContext.getViewRoot()).andReturn(viewRoot);
        expect(viewRoot.getAfterPhaseListener()).andReturn(null);

        _mocksControl.replay();
        _testimpl.doPrePhaseActions(_facesContext);
        _testimpl.execute(_facesContext);
        _mocksControl.verify();
    }*/

    /**
     * Test method for
     * {@link org.apache.myfaces.lifecycle.RestoreViewExecutor#execute(javax.faces.context.FacesContext)}.
     *//*
    public void testExecuteWOExistingViewRootPostBackAndViewExpired()
    {
        setupWOExistingViewRoot();
        expect(_facesContext.getExternalContext()).andReturn(_externalContext).anyTimes();
        expect(_externalContext.getRequestMap()).andReturn(new HashMap());
        expect(_restoreViewSupport.isPostback(same(_facesContext))).andReturn(true);
        _facesContext.setProcessingEvents(eq(true));
        expect(_viewHandler.restoreView(same(_facesContext), eq("calculatedViewId"))).andReturn(null);
        _facesContext.setProcessingEvents(eq(false));

        _mocksControl.replay();
        assertException(ViewExpiredException.class, new TestRunner()
        {
            public void run() throws Throwable
            {
                _testimpl.doPrePhaseActions(_facesContext);
                _testimpl.execute(_facesContext);
            };
        });
        _mocksControl.verify();
    }*/

    private void setupWOExistingViewRoot()
    {
        expect(_facesContext.getApplication()).andReturn(_application).anyTimes();
        expect(_application.getViewHandler()).andReturn(_viewHandler).anyTimes();
        _viewHandler.initView(eq(_facesContext));
        expect(_facesContext.getViewRoot()).andReturn(null);
        expect(_restoreViewSupport.calculateViewId(eq(_facesContext))).andReturn("calculatedViewId");
    }

    /**
     * Test method for {@link org.apache.myfaces.lifecycle.RestoreViewExecutor#getRestoreViewSupport()}.
     */
    public void testGetRestoreViewSupport() throws Exception
    {
        expect(_facesContext.getExternalContext()).andReturn(_externalContext).anyTimes();
        expect(_externalContext.getInitParameter("javax.faces.FACELETS_VIEW_MAPPINGS")).andReturn(null).anyTimes();
        expect(_externalContext.getInitParameter("facelets.VIEW_MAPPINGS")).andReturn(null).anyTimes();
        expect(_externalContext.getInitParameter("javax.faces.FACELETS_SUFFIX")).andReturn(null).anyTimes();
        expect(_externalContext.getInitParameter("javax.faces.DEFAULT_SUFFIX")).andReturn(null).anyTimes();
        _mocksControl.replay();
        assertTrue(DefaultRestoreViewSupport.class.equals(new RestoreViewExecutor().getRestoreViewSupport(_facesContext).getClass()));
        _mocksControl.verify();
    }

    /**
     * Test method for {@link org.apache.myfaces.lifecycle.RestoreViewExecutor#getPhase()}.
     */
    public void testGetPhase()
    {
        assertEquals(PhaseId.RESTORE_VIEW, _testimpl.getPhase());
    }

}
