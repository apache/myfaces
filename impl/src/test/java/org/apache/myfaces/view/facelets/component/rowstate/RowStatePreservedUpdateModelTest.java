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
 * MYFACES-4778, case 1 (TCK Issue6129IT.testRowStatePreservedDoesNotKeepLocalValueAfterUpdateModel): with
 * {@code rowStatePreserved="true"}, the saved state of a row must not keep the local value of its inputs after Update
 * Model Values cleared it, otherwise that stale local value hides every later change of the model in that row.
 */
public class RowStatePreservedUpdateModelTest extends AbstractMyFacesCDIRequestTestCase
{
    private static final List<String> INPUTS = List.of(
            "form:preserved:0:in", "form:preserved:1:in",
            "form:repeated:0:in", "form:repeated:1:in",
            "form:plain:0:in", "form:plain:1:in");

    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES",
                "org.apache.myfaces.view.facelets.component.rowstate");
    }

    @Test
    public void testRowStatePreservedDoesNotKeepLocalValueAfterUpdateModel() throws Exception
    {
        startViewRequest("/rowStatePreservedUpdateModel.xhtml");
        processLifecycleExecuteAndRender();

        for (String input : INPUTS)
        {
            client.getParameters().put(input, "typed " + input);
        }
        client.submit("form:submit");
        processLifecycleExecuteAndRender();

        Assertions.assertFalse(facesContext.isValidationFailed());
        String content = content();
        for (String input : INPUTS)
        {
            assertRenderedValue(content, input, "typed " + input);
        }

        client.ajax("form:change", "action", "form:change", "form", true);
        processLifecycleExecuteAndRender();

        content = content();
        for (String input : INPUTS)
        {
            assertRenderedValue(content, input, "changed");
        }
        endRequest();
    }

    private String content() throws Exception
    {
        return new String(((MockPrintWriter) response.getWriter()).content());
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
