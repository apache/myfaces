/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.renderkit.html;

import java.io.StringWriter;

import javax.faces.application.FacesMessage;
import javax.faces.component.html.HtmlMessages;

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockRenderKitFactory;
import org.apache.shale.test.mock.MockResponseWriter;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlMessagesRendererTest extends AbstractJsfTestCase
{
    private static final String ERROR_CLASS = "errorClass";
    private static final String WARN_CLASS = "warnClass";
    private static final String INFO_CLASS = "infoClass";

    private MockResponseWriter writer;
    private HtmlMessages messages;

    public HtmlMessagesRendererTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        messages = new HtmlMessages();
        messages.setErrorClass(ERROR_CLASS);
        messages.setWarnClass(WARN_CLASS);
        messages.setInfoClass(INFO_CLASS);

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                messages.getFamily(),
                messages.getRendererType(),
                new HtmlMessagesRenderer());

    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        writer = null;
    }

    public void testLayoutTable_Style() throws Exception
    {
        facesContext.addMessage("test1", new FacesMessage(FacesMessage.SEVERITY_WARN, "warnSumary", "detailWarnSummary"));
        facesContext.addMessage("test2", new FacesMessage(FacesMessage.SEVERITY_ERROR, "errorSumary", "detailErrorSummary"));

        messages.setLayout("table");
        messages.setStyle("left: 48px; top: 432px; position: absolute");

        HtmlMessagesRenderer renderer = new HtmlMessagesRenderer();
        renderer.encodeEnd(facesContext, messages);

        facesContext.renderResponse();

        //String output = writer.getWriter().toString();

        // TODO: test don't work on different jvms (messages are in different order)
        // assertEquals("<table style=\"left: 48px; top: 432px; position: absolute\"><tr><td><span class=\"warnClass\">warnSumary</span></td></tr>" +
        //              "<tr><td><span class=\"errorClass\">errorSumary</span></td></tr></table>", output);
    }
}
