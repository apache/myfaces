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
 * A UIData or UIRepeat nested in a UIData or UIRepeat with {@code rowStatePreserved="true"}: the enclosing component
 * keeps the state of the nested one per row, which holds the per-row state map of the nested component. Restoring the
 * nested component for the next row must not change that map in place, otherwise the per-row state of the row before
 * is lost. This only shows from the second request with validation errors on.
 */
public class RowStatePreservedNestedTest extends AbstractMyFacesCDIRequestTestCase
{
    private static final List<String> ROWS = List.of(
            "outer:0:inner:0:in", "outer:0:inner:1:in", "outer:1:inner:0:in", "outer:1:inner:1:in",
            "router:0:rinner:0:in", "router:0:rinner:1:in", "router:1:rinner:0:in", "router:1:rinner:1:in");

    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES",
                "org.apache.myfaces.view.facelets.component.rowstate");
    }

    @Test
    public void testNestedTypedValuesSurviveRepeatedValidationFailures() throws Exception
    {
        startViewRequest("/rowStatePreservedNested.xhtml");
        processLifecycleExecuteAndRender();

        for (int round = 1; round <= 3; round++)
        {
            for (String row : ROWS)
            {
                client.getParameters().put("mainForm:" + row, typed(row, round));
            }
            client.getParameters().put("mainForm:required", "");
            client.submit("mainForm:submit");
            processLifecycleExecuteAndRender();
            Assertions.assertTrue(facesContext.isValidationFailed());

            String content = new String(((MockPrintWriter) response.getWriter()).content());
            for (String row : ROWS)
            {
                assertRenderedValue(content, "mainForm:" + row, typed(row, round), round);
            }
        }
        endRequest();
    }

    private static void assertRenderedValue(String content, String clientId, String expected, int round)
    {
        int start = content.indexOf("id=\"" + clientId + "\"");
        Assertions.assertTrue(start >= 0, clientId + " not rendered in: " + content);
        String input = content.substring(start, content.indexOf('>', start));
        Assertions.assertTrue(input.contains("value=\"" + expected + "\""),
                "request " + round + ": " + clientId + " should render " + expected + " but was: " + input);
    }

    private static String typed(String row, int round)
    {
        return "typed" + round + "-" + row.replace(':', '-');
    }
}
