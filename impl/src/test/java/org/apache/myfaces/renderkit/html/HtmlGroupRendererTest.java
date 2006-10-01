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

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockRenderKitFactory;
import org.apache.shale.test.mock.MockResponseWriter;

import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import java.io.StringWriter;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlGroupRendererTest extends AbstractJsfTestCase
{
    private static String PANEL_CHILD_TEXT = "PANEL";
    private static String STYLE_CLASS = "myStyleClass";

    private MockResponseWriter writer ;
    private HtmlPanelGroup panelGroup;

    public HtmlGroupRendererTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        panelGroup = new HtmlPanelGroup();

        HtmlOutputText panelChildOutputText = new HtmlOutputText();
        panelChildOutputText.setValue(PANEL_CHILD_TEXT);
        panelGroup.getChildren().add(panelChildOutputText);

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                panelGroup.getFamily(),
                panelGroup.getRendererType(),
                new HtmlGroupRenderer());
        facesContext.getRenderKit().addRenderer(
                panelChildOutputText.getFamily(),
                panelChildOutputText.getRendererType(),
                new HtmlTextRenderer());

    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        writer = null;
    }

    public void testLayout_Default() throws Exception
    {
        assertNull(panelGroup.getLayout());

        panelGroup.setStyleClass(STYLE_CLASS);

        panelGroup.encodeEnd(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();

        assertEquals("<span class=\""+STYLE_CLASS+"\">PANEL</span>", output);
    }

    public void testLayout_Block() throws Exception
    {
        panelGroup.setLayout("block");

        panelGroup.setStyleClass(STYLE_CLASS);

        panelGroup.encodeEnd(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();

        assertEquals("<div class=\""+STYLE_CLASS+"\">PANEL</div>", output);
    }
}
