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
package org.apache.myfaces.view.facelets.pss.dynamicchild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import jakarta.el.ExpressionFactory;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;

import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.apache.myfaces.view.facelets.PartialStateManagementStrategy;
import org.junit.jupiter.api.Test;

/**
 * A component added by a {@code postAddToView} listener is added while the view is built, so every build of the view
 * reproduces it. It must therefore not be tracked as a dynamically added component in the saved state, and the
 * postback must neither lose it nor duplicate it.
 */
class DynamicChildInPostAddToViewTestCase extends AbstractMyFacesCDIRequestTestCase
{
    private static final String SUBMITTED_VALUE = "submitted into a dynamically added input";

    // h:panelGroup is no NamingContainer, so the child is addressed relative to the form
    private static final String CHILD_CLIENT_ID = "form:" + DynamicChildInPostAddToViewBean.CHILD_ID;

    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }

    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES",
                "org.apache.myfaces.view.facelets.pss.dynamicchild");
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, "Production");
    }

    @Test
    void listenerOnViewRoot() throws Exception
    {
        assertBuildTimeAddIsNotSaved("/dynamicChildInViewRoot.xhtml");
    }

    @Test
    void listenerOnContainer() throws Exception
    {
        assertBuildTimeAddIsNotSaved("/dynamicChildInContainer.xhtml");
    }

    private void assertBuildTimeAddIsNotSaved(String viewId) throws Exception
    {
        startViewRequest(viewId);
        processLifecycleExecuteAndRender();

        assertEquals(1, countDynamicChildren(), "one added child on initial render");
        assertFalse(isMarkedAsAddedAfterBuild(), "added child marked as added after build on initial render");
        assertEquals(0, countClientIdsAdded(), "dynamically added components on initial render");

        client.inputText(CHILD_CLIENT_ID, SUBMITTED_VALUE);
        client.submit("form:submit");
        processLifecycleExecuteAndRender();

        assertEquals(1, countDynamicChildren(), "one added child after postback");
        assertEquals(SUBMITTED_VALUE, facesContext.getApplication().evaluateExpressionGet(facesContext,
                "#{dynamicChildInPostAddToViewBean.value}", String.class), "value decoded from the added child");
        assertEquals(SUBMITTED_VALUE,
                ((UIInput) facesContext.getViewRoot().findComponent(CHILD_CLIENT_ID)).getValue(),
                "value of the added child after postback");
        assertFalse(isMarkedAsAddedAfterBuild(), "added child marked as added after build after postback");
        assertEquals(0, countClientIdsAdded(), "dynamically added components after postback");

        endRequest();
    }

    private int countDynamicChildren()
    {
        UIComponent container = facesContext.getViewRoot().findComponent(DynamicChildInPostAddToViewBean.CONTAINER_ID);
        int count = 0;
        for (UIComponent child : container.getChildren())
        {
            if (DynamicChildInPostAddToViewBean.CHILD_ID.equals(child.getId()))
            {
                count++;
            }
        }
        return count;
    }

    /**
     * A component added outside the view build is marked on the component itself, see
     * PartialStateManagementStrategy.PostAddPreRemoveFromViewListener.
     */
    private boolean isMarkedAsAddedAfterBuild()
    {
        return facesContext.getViewRoot().findComponent(CHILD_CLIENT_ID).getAttributes()
                .containsKey(PartialStateManagementStrategy.COMPONENT_ADDED_AFTER_BUILD_VIEW);
    }

    /**
     * The client ids of the components added outside the view build, collected when the state is saved.
     */
    @SuppressWarnings("unchecked")
    private int countClientIdsAdded()
    {
        List<String> clientIdsAdded = (List<String>) facesContext.getViewRoot().getAttributes()
                .get(PartialStateManagementStrategy.CLIENTIDS_ADDED);
        return clientIdsAdded == null ? 0 : clientIdsAdded.size();
    }
}
