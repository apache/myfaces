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
package org.apache.myfaces.renderkit;

import java.io.IOException;
import java.io.StringWriter;

import javax.el.ValueExpression;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlOutputText;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.application.viewstate.StateUtils;
import org.apache.myfaces.spi.impl.DefaultSerialFactory;
import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;

/**
 * @author Bruno Aranda (latest modification by $Author: struberg $)
 * @version $Revision: 1188235 $ $Date: 2011-10-24 13:09:33 -0400 (Mon, 24 Oct 2011) $
 */
public class ErrorPageWriterTest extends AbstractJsfTestCase
{
    public static Test suite()
    {
        return new TestSuite(ErrorPageWriterTest.class); // needed in maven
    }

    private MockResponseWriter writer ;
    private HtmlOutputText outputText;

    public ErrorPageWriterTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        outputText = new HtmlOutputText();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);
        // TODO remove these two lines once myfaces-test goes alpha, see MYFACES-1155
        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                outputText.getFamily(),
                outputText.getRendererType(),
                new HtmlTextRenderer());
        servletContext.setAttribute(StateUtils.SERIAL_FACTORY, new DefaultSerialFactory());

        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_JSF_JS", Boolean.TRUE);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
        outputText = null;
        writer = null;
    }

    public void testValueExpressionGetExpressionStringReturnsNull() throws IOException
    {
        //See MYFACES-3413 for details
        UIViewRoot root = facesContext.getViewRoot();
//        UIForm form = new UIForm();
//        form.setId("formId");
//        
//        form.getChildren().add(inputText);
        root.getChildren().add(outputText);

        ValueExpression ve = new NullReturningGetExpressionStringValueExpression();
        
        outputText.setValueExpression("rendered", ve);
        String id = "testValueExpressionGetExpressionStringReturnsNullOutputComponent";
        outputText.setId(id);

        StringWriter w = new StringWriter();
        Throwable t = new Throwable("Placeholder throwable");
        ErrorPageWriter.debugHtml(w, facesContext, t);
        String output = w.toString();
        int indexOfOutputComponentId = output.indexOf(id);
        String surroundingText = "output component not found.";
        if (-1 != indexOfOutputComponentId) {
            surroundingText = output.substring(Math.max(0, indexOfOutputComponentId - 20), Math.min(output.length(), indexOfOutputComponentId + 280));
        }
        int indexOfHasRenderedAttribute = output.indexOf("rendered=\"\"");
        boolean hasRenderedAttribute = (-1 != indexOfHasRenderedAttribute);
        assertTrue("rendered attribute wasn't written correctly: " + surroundingText, hasRenderedAttribute);
    }
    
}
