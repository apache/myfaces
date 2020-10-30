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

import java.util.Locale;

import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.event.PhaseId;
import org.apache.myfaces.application.ViewIdSupport;

import org.apache.myfaces.test.FacesTestCase;
import org.junit.Assert;
import org.mockito.Mockito;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class RestoreViewExecutorTest extends FacesTestCase
{
    private RestoreViewExecutor _testimpl;
    private ViewHandler _viewHandler;
    private RestoreViewSupport _restoreViewSupport;
    private ViewIdSupport _viewHandlerSupport;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        _viewHandler = Mockito.mock(ViewHandler.class);
        _restoreViewSupport = Mockito.mock(RestoreViewSupport.class);
        _viewHandlerSupport = Mockito.mock(ViewIdSupport.class);
        _testimpl = new RestoreViewExecutor();
        _testimpl.setRestoreViewSupport(_restoreViewSupport);
        _testimpl.setViewHandlerSupport(_viewHandlerSupport);
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.lifecycle.RestoreViewExecutor#execute(jakarta.faces.context.FacesContext)}.
     */
    public void testExecuteWithExistingViewRoot()
    {
        Mockito.when(_facesContext.getApplication()).thenReturn(_application);
        Mockito.when(_application.getViewHandler()).thenReturn(_viewHandler);
        _viewHandler.initView(_facesContext);
        UIViewRoot viewRoot = Mockito.mock(UIViewRoot.class);
        Mockito.when(_facesContext.getViewRoot()).thenReturn(viewRoot);
        Locale expectedLocale = new Locale("xxx");
        Mockito.when(_facesContext.getExternalContext()).thenReturn(_externalContext);
        Mockito.when(_externalContext.getRequestLocale()).thenReturn(expectedLocale);
        viewRoot.setLocale(expectedLocale);
        Mockito.when(viewRoot.getAfterPhaseListener()).thenReturn(null);
        _restoreViewSupport.processComponentBinding(_facesContext, viewRoot);

        _testimpl.doPrePhaseActions(_facesContext);
        _testimpl.execute(_facesContext);
    }

    /**
     * Test method for {@link org.apache.myfaces.lifecycle.RestoreViewExecutor#getPhase()}.
     */
    public void testGetPhase()
    {
        Assert.assertEquals(PhaseId.RESTORE_VIEW, _testimpl.getPhase());
    }

}
