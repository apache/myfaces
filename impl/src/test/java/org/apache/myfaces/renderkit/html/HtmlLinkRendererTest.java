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

import javax.faces.component.UIForm;
import javax.faces.component.html.HtmlCommandLink;
import java.io.StringWriter;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlLinkRendererTest extends AbstractJsfTestCase
{

    private MockResponseWriter writer;
    private HtmlCommandLink link;

    public HtmlLinkRendererTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        UIForm form = new UIForm();

        link = new HtmlCommandLink();
        link.setValue("HelloLink");
        link.setStyleClass("linkClass");

        form.getChildren().add(link);

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                link.getFamily(),
                link.getRendererType(),
                new HtmlLinkRenderer());
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        writer = null;
    }

    public void testLinkDisabled() throws Exception
    {
        link.setDisabled(true);
        link.setTarget("testTarget");
        link.setId("foo1");

        HtmlLinkRenderer renderer = new HtmlLinkRenderer();
        renderer.encodeBegin(facesContext, link);
        renderer.encodeChildren(facesContext, link);
        renderer.encodeEnd(facesContext, link);

        facesContext.renderResponse();

        String output = writer.getWriter().toString();

        assertEquals("<span id=\"" + link.getClientId(facesContext) + "\" target=\"testTarget\" class=\"linkClass\">HelloLink</span>", output);
    }
}
