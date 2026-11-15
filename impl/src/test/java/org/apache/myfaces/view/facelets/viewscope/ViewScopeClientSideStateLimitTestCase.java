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
package org.apache.myfaces.view.facelets.viewscope;

import jakarta.el.ExpressionFactory;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.StateManager;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * With client side state saving no view state is kept in the session, so view scopes were never
 * destroyed before the session expired. Repeatedly requesting a view with a form and a
 * {@code @ViewScoped} bean must not let the number of view scopes in the session grow without limit:
 * only the most recent {@code org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION} are kept.
 */
public class ViewScopeClientSideStateLimitTestCase extends AbstractMyFacesCDIRequestTestCase
{
    private static final int LIMIT = 3;

    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }

    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES",
                "org.apache.myfaces.view.facelets.viewscope");
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, "Production");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME,
                StateManager.STATE_SAVING_METHOD_CLIENT);
        servletContext.addInitParameter(MyfacesConfig.NUMBER_OF_VIEWS_IN_SESSION,
                Integer.toString(LIMIT));
    }

    @Test
    public void testViewScopesAreLimitedWithClientSideStateSaving() throws Exception
    {
        ViewScopeLimitProbeBean.reset();

        int requests = 10;
        for (int i = 0; i < requests; i++)
        {
            startViewRequest("/formViewScoped.xhtml");
            processLifecycleExecute();
            renderResponse();
            endRequest();
        }

        Assertions.assertEquals(requests, ViewScopeLimitProbeBean.CREATED.get(),
                "the @ViewScoped bean must be created once per request");
        Assertions.assertEquals(requests - LIMIT, ViewScopeLimitProbeBean.DESTROYED.get(),
                "only the most recent " + LIMIT + " view scopes must be kept in the session");
    }
}
