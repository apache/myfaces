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
package org.apache.myfaces.view.facelets.component.rowstate;

import jakarta.faces.render.ResponseStateManager;
import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.apache.myfaces.test.core.mock.MockMyFacesClient;
import org.apache.myfaces.test.mock.MockPrintWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * MYFACES-4778, case 2 (TCK Issue6129IT.testSecondRestoreOfSavedStateDoesNotSeeChangesOfFirstRestore): a request
 * which restores a saved view state must not change the per-row state in that saved state, so that a later restore of
 * the same saved state, such as after the back button or in a second tab, sees the per-row state as it was saved.
 * With server side state saving, the saved state is not serialized by default, so each restore gets the same instance.
 */
public class RowStateSharedSavedStateTest extends AbstractMyFacesCDIRequestTestCase
{
    private String viewStateOverride;

    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES",
                "org.apache.myfaces.view.facelets.component.rowstate");
    }

    @Override
    protected MockMyFacesClient createClient()
    {
        return new MockMyFacesClient(facesContext, this)
        {
            @Override
            protected void applyStateFromPreviousRequest()
            {
                super.applyStateFromPreviousRequest();
                if (viewStateOverride != null)
                {
                    getParameters().put(ResponseStateManager.VIEW_STATE_PARAM, viewStateOverride);
                    viewStateOverride = null;
                }
            }
        };
    }

    @Test
    public void testSecondRestoreOfSavedStateDoesNotSeeChangesOfFirstRestore() throws Exception
    {
        startViewRequest("/rowStateSharedSavedState.xhtml");
        processLifecycleExecuteAndRender();

        // step 1: validation fails, row 0 keeps "first"
        putRows("first", "one");
        client.submit("form:submit");
        processLifecycleExecuteAndRender();
        Assertions.assertFalse(facesContext.getMessageList().isEmpty(), "required input should fail");
        assertRows("first", "one");
        String viewStateOfStep1 = renderedViewState();

        // step 2: validation fails again, row 1 keeps "second"
        putRows("first", "second");
        client.submit("form:submit");
        processLifecycleExecuteAndRender();
        Assertions.assertFalse(facesContext.getMessageList().isEmpty(), "required input should fail");
        assertRows("first", "second");

        // step 3: restore the view state of step 1 again, as the back button or a second tab would do
        viewStateOverride = viewStateOfStep1;
        client.getParameters().put("form:required", "");
        client.ajax("form:refresh", "action", "form:required", "form", true);
        processLifecycleExecuteAndRender();
        Assertions.assertFalse(facesContext.getMessageList().isEmpty(), "required input should fail");
        assertRows("first", "one");
        endRequest();
    }

    private void putRows(String row0, String row1)
    {
        client.getParameters().put("form:required", "");
        client.getParameters().put("form:table:0:in", row0);
        client.getParameters().put("form:table:1:in", row1);
        client.getParameters().put("form:repeat:0:in", row0);
        client.getParameters().put("form:repeat:1:in", row1);
    }

    private void assertRows(String row0, String row1) throws Exception
    {
        String content = new String(((MockPrintWriter) response.getWriter()).content());
        assertRenderedValue(content, "form:table:0:in", row0);
        assertRenderedValue(content, "form:table:1:in", row1);
        assertRenderedValue(content, "form:repeat:0:in", row0);
        assertRenderedValue(content, "form:repeat:1:in", row1);
    }

    /**
     * The value of the jakarta.faces.ViewState hidden field in the response, as the browser keeps it.
     */
    private String renderedViewState() throws Exception
    {
        String content = new String(((MockPrintWriter) response.getWriter()).content());
        int start = content.indexOf("name=\"" + ResponseStateManager.VIEW_STATE_PARAM + "\"");
        Assertions.assertTrue(start >= 0, "view state not rendered in: " + content);
        String field = content.substring(start, content.indexOf('>', start));
        int valueStart = field.indexOf("value=\"") + "value=\"".length();
        return field.substring(valueStart, field.indexOf('"', valueStart));
    }

    private static void assertRenderedValue(String content, String clientId, String expected)
    {
        int start = content.indexOf("id=\"" + clientId + "\"");
        Assertions.assertTrue(start >= 0, clientId + " not rendered in: " + content);
        String input = content.substring(start, content.indexOf('>', start));
        Assertions.assertTrue(input.contains("value=\"" + expected + "\""),
                clientId + " should render " + expected + " but was: " + input);
    }
}
