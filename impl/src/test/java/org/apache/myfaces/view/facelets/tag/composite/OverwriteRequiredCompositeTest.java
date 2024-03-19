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

package org.apache.myfaces.view.facelets.tag.composite;

import jakarta.el.ExpressionFactory;
import jakarta.faces.component.UICommand;
import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// test for MYFACES-4656
public class OverwriteRequiredCompositeTest extends AbstractMyFacesCDIRequestTestCase
{
    @Override
    protected boolean isScanAnnotations()
    {
        return true;
    }
    
    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES","org.apache.myfaces.view.facelets.tag.composite");
        servletContext.addInitParameter("jakarta.faces.FACELETS_LIBRARIES", "/test-facelet.taglib.xml");
    }

    @Test
    public void testSimpleCompositeComponent() throws Exception
    {
        startViewRequest("/overwriteRequiredComposite.xhtml");
        processLifecycleExecuteAndRender();
        String content = getRenderedContent(facesContext);

        Assertions.assertTrue(content.contains("form:fifth REQUIRED: false")
                && content.contains("form:fifth VALID: true"));
        Assertions.assertTrue(content.contains("form:sixth REQUIRED: true")
                && content.contains("form:sixth VALID: true"));
        Assertions.assertTrue(content.contains("form:seventh REQUIRED: true")
                && content.contains("form:seventh VALID: true"));
        Assertions.assertTrue(content.contains("form:eight REQUIRED: true")
                && content.contains("form:eight VALID: true"));

        Assertions.assertTrue(content.contains("form:ninth REQUIRED: false")
            && content.contains("form:ninth VALID: true"));
        Assertions.assertTrue(content.contains("form:tenth REQUIRED: true")
            && content.contains("form:tenth VALID: true"));
        Assertions.assertTrue(content.contains("form:eleventh REQUIRED: true")
            && content.contains("form:eleventh VALID: true"));
        Assertions.assertTrue(content.contains("form:twelvth REQUIRED: true")
            && content.contains("form:twelvth VALID: true"));




        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("form:process");
        client.submit(button);
        processLifecycleExecuteAndRender();

        content = getRenderedContent(facesContext);

    }

    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }
}
