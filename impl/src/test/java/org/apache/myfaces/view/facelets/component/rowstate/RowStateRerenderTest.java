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

import java.util.List;
import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.apache.myfaces.test.mock.MockPrintWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The per-row state of the child inputs of UIData and UIRepeat must be discarded on encodeBegin, unless it is
 * needed to rerender the page with errors (messages of severity ERROR or higher). This also holds for a later
 * request which rerenders the iterating component without executing it, so the per-row state must survive the
 * state saving at the end of the request which rendered it.
 */
public class RowStateRerenderTest extends AbstractMyFacesCDIRequestTestCase
{
    private static final List<String> ROWS = List.of(
            "top:0:in", "top:1:in",
            "outer:0:inner:0:in", "outer:0:inner:1:in", "outer:1:inner:0:in", "outer:1:inner:1:in",
            "rtop:0:in", "rtop:1:in",
            "router:0:rinner:0:in", "router:0:rinner:1:in", "router:1:rinner:0:in", "router:1:rinner:1:in");

    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES",
                "org.apache.myfaces.view.facelets.component.rowstate");
    }

    @Test
    public void testRefreshWithoutMessagesRendersModelValues() throws Exception
    {
        submitTypedValuesWithValidationError();
        client.ajax("mainForm:refresh", "action", "mainForm:refresh", "mainForm", true);
        processLifecycleExecuteAndRender();
        assertRenderedValues(false);
        endRequest();
    }

    @Test
    public void testRefreshWithInfoRendersModelValues() throws Exception
    {
        submitTypedValuesWithValidationError();
        client.ajax("mainForm:refreshWithInfo", "action", "mainForm:refreshWithInfo", "mainForm", true);
        processLifecycleExecuteAndRender();
        assertRenderedValues(false);
        endRequest();
    }

    @Test
    public void testRefreshWithErrorsRendersTypedValues() throws Exception
    {
        submitTypedValuesWithValidationError();
        client.getParameters().put("mainForm:required", "");
        client.ajax("mainForm:refreshWithErrors", "action", "mainForm:required", "mainForm", true);
        processLifecycleExecuteAndRender();
        assertRenderedValues(true);
        endRequest();
    }

    @Test
    public void testRefreshWithFatalRendersTypedValues() throws Exception
    {
        submitTypedValuesWithValidationError();
        client.ajax("mainForm:refreshWithFatal", "action", "mainForm:refreshWithFatal", "mainForm", true);
        processLifecycleExecuteAndRender();
        assertRenderedValues(true);
        endRequest();
    }

    @Test
    public void testRefreshWithErrorsTwiceThenWithoutMessages() throws Exception
    {
        submitTypedValuesWithValidationError();

        client.getParameters().put("mainForm:required", "");
        client.ajax("mainForm:refreshWithErrors", "action", "mainForm:required", "mainForm", true);
        processLifecycleExecuteAndRender();
        assertRenderedValues(true);

        client.getParameters().put("mainForm:required", "");
        client.ajax("mainForm:refreshWithErrors", "action", "mainForm:required", "mainForm", true);
        processLifecycleExecuteAndRender();
        assertRenderedValues(true);

        client.ajax("mainForm:refresh", "action", "mainForm:refresh", "mainForm", true);
        processLifecycleExecuteAndRender();
        assertRenderedValues(false);
        endRequest();
    }

    private void submitTypedValuesWithValidationError() throws Exception
    {
        startViewRequest("/rowState.xhtml");
        processLifecycleExecuteAndRender();
        assertRenderedValues(false);

        for (String row : ROWS)
        {
            client.getParameters().put("mainForm:" + row, typed(row));
        }
        client.getParameters().put("mainForm:required", "");
        client.submit("mainForm:submit");
        processLifecycleExecuteAndRender();

        Assertions.assertTrue(facesContext.isValidationFailed());
        assertRenderedValues(true);
    }

    private void assertRenderedValues(boolean typed) throws Exception
    {
        String content = new String(((MockPrintWriter) response.getWriter()).content());
        String[] model = {
            "model-top-0", "model-top-1",
            "model-nested-0-0", "model-nested-0-1", "model-nested-1-0", "model-nested-1-1",
            "model-top-0", "model-top-1",
            "model-nested-0-0", "model-nested-0-1", "model-nested-1-0", "model-nested-1-1"};
        for (int i = 0; i < ROWS.size(); i++)
        {
            String row = ROWS.get(i);
            String expected = typed ? typed(row) : model[i];
            String input = renderedInput(content, "mainForm:" + row);
            Assertions.assertTrue(input.contains("value=\"" + expected + "\""),
                    "mainForm:" + row + " should render " + expected + " but was: " + input);
        }
    }

    private static String renderedInput(String content, String clientId)
    {
        int start = content.indexOf("id=\"" + clientId + "\"");
        Assertions.assertTrue(start >= 0, clientId + " not rendered in: " + content);
        return content.substring(start, content.indexOf('>', start));
    }

    private static String typed(String row)
    {
        return "typed-" + row.replace(':', '-');
    }
}
