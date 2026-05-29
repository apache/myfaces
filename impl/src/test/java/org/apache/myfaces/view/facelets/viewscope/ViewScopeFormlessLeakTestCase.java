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

import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Regression test for the @ViewScoped leak on formless views.
 *
 * <p>A view without any {@code UIForm} never has its state saved, so it can never be restored and
 * is never registered in the session {@code SerializedViewCollection}. Repeatedly requesting such a
 * view that references a {@code @ViewScoped} bean must not accumulate view scopes in the session:
 * the scope has to be destroyed at the end of each request. Before the fix the beans were never
 * destroyed and piled up in the session (a memory leak), so {@code DESTROYED} stayed at 0.</p>
 */
public class ViewScopeFormlessLeakTestCase extends AbstractMyFacesCDIRequestTestCase
{
    @Override
    protected boolean isScanAnnotations()
    {
        return true;
    }

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
    }

    @Test
    public void testFormlessViewDoesNotLeakViewScopedBeans() throws Exception
    {
        ViewScopeLeakProbeBean.reset();

        int requests = 5;
        for (int i = 0; i < requests; i++)
        {
            startViewRequest("/formlessViewScoped.xhtml");
            processLifecycleExecute();
            renderResponse();
            endRequest();
        }

        Assertions.assertEquals(requests, ViewScopeLeakProbeBean.CREATED.get(),
                "the @ViewScoped bean must be created once per request");
        Assertions.assertEquals(requests, ViewScopeLeakProbeBean.DESTROYED.get(),
                "the @ViewScoped beans of a formless (never-saved) view must be destroyed each "
                        + "request, not leaked into the session");
    }
}
