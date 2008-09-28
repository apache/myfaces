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

import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlSelectOneRadio;
import javax.faces.model.SelectItem;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlRadioRendererTest extends AbstractJsfTestCase
{
    private MockResponseWriter writer ;
    private HtmlSelectOneRadio selectOneRadio;

    public HtmlRadioRendererTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        selectOneRadio = new HtmlSelectOneRadio();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                selectOneRadio.getFamily(),
                selectOneRadio.getRendererType(),
                new HtmlRadioRenderer());

    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        selectOneRadio = null;
        writer = null;
    }

    public void testDefault() throws Exception
    {
        List<SelectItem> items = new ArrayList<SelectItem>();
        items.add(new SelectItem("mars"));
        items.add(new SelectItem("jupiter"));

        UISelectItems selectItems = new UISelectItems();
        selectItems.setValue(items);

        selectOneRadio.getChildren().add(selectItems);

        selectOneRadio.encodeEnd(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();

        assertEquals("<table><tr>\t\t" +
                "<td><input id=\"j_id0:0\" type=\"radio\" name=\"j_id0\" value=\"mars\"/><label for=\"j_id0:0\">&#160;mars</label></td>\t\t" +
                "<td><input id=\"j_id0:1\" type=\"radio\" name=\"j_id0\" value=\"jupiter\"/><label for=\"j_id0:1\">&#160;jupiter</label></td>" +
                "</tr></table>", output);
    }

    public void testDefaultStylePassthru() throws Exception
    {
        List<SelectItem> items = new ArrayList<SelectItem>();
        items.add(new SelectItem("mars"));
        items.add(new SelectItem("jupiter"));

        UISelectItems selectItems = new UISelectItems();
        selectItems.setValue(items);

        selectOneRadio.getChildren().add(selectItems);

        selectOneRadio.setStyle("color: red;");

        selectOneRadio.encodeEnd(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();
        assertEquals("<table style=\"color: red;\"><tr>\t\t" +
                "<td><input id=\"j_id0:0\" type=\"radio\" name=\"j_id0\" value=\"mars\"/><label for=\"j_id0:0\">&#160;mars</label></td>\t\t" +
                "<td><input id=\"j_id0:1\" type=\"radio\" name=\"j_id0\" value=\"jupiter\"/><label for=\"j_id0:1\">&#160;jupiter</label></td>" +
                "</tr></table>", output);
    }
}
