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

import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.apache.myfaces.test.mock.MockPrintWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * MYFACES-4777 (TCK Issue6128IT): with {@code rowStatePreserved="true"}, a row without saved state of its own, such
 * as a row added to the model after the last render, must not show the local value of the row before it. The input is
 * created in Java with only an id, so that nothing is in its state helper initially.
 */
public class RowStatePreservedNewRowTest extends AbstractMyFacesCDIRequestTestCase
{
    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES",
                "org.apache.myfaces.view.facelets.component.rowstate");
    }

    @Test
    public void testNewRowDoesNotShowLocalValueOfRowBefore() throws Exception
    {
        startViewRequest("/rowStatePreservedNewRow.xhtml");
        processLifecycleExecuteAndRender();

        client.getParameters().put("form:table:0:in", "");
        client.getParameters().put("form:table:1:in", "typed");
        client.getParameters().put("form:repeat:0:in", "");
        client.getParameters().put("form:repeat:1:in", "typed");
        client.submit("form:submit");
        processLifecycleExecuteAndRender();

        String content = content();
        Assertions.assertTrue(renderedInput(content, "form:table:1:in").contains("value=\"typed\""), content);
        Assertions.assertTrue(renderedInput(content, "form:repeat:1:in").contains("value=\"typed\""), content);

        client.ajax("form:addRow", "action", "form:addRow", "form", true);
        processLifecycleExecuteAndRender();

        content = content();
        Assertions.assertTrue(renderedInput(content, "form:table:1:in").contains("value=\"typed\""), content);
        Assertions.assertTrue(renderedInput(content, "form:repeat:1:in").contains("value=\"typed\""), content);
        String tableRow2 = renderedInput(content, "form:table:2:in");
        Assertions.assertFalse(tableRow2.contains("typed"), "new table row shows the value of the row before: "
                + tableRow2);
        String repeatRow2 = renderedInput(content, "form:repeat:2:in");
        Assertions.assertFalse(repeatRow2.contains("typed"), "new repeat row shows the value of the row before: "
                + repeatRow2);
        endRequest();
    }

    private String content() throws Exception
    {
        return new String(((MockPrintWriter) response.getWriter()).content());
    }

    private static String renderedInput(String content, String clientId)
    {
        int start = content.indexOf("id=\"" + clientId + "\"");
        Assertions.assertTrue(start >= 0, clientId + " not rendered in: " + content);
        return content.substring(start, content.indexOf('>', start));
    }
}
