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
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.model.SelectItem;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlMenuRendererTest extends AbstractJsfTestCase
{
    private static final String LINE_SEPARATOR = System.getProperty(
            "line.separator", "\r\n");

    private MockResponseWriter writer ;
    private HtmlSelectOneMenu selectOneMenu;

    public HtmlMenuRendererTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        selectOneMenu = new HtmlSelectOneMenu();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                selectOneMenu.getFamily(),
                selectOneMenu.getRendererType(),
                new HtmlMenuRenderer());

    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        selectOneMenu = null;
        writer = null;
    }

    public void testRenderDefault() throws Exception
    {
        List items = new ArrayList();
        items.add(new SelectItem("mars"));
        items.add(new SelectItem("jupiter"));

        UISelectItems selectItems = new UISelectItems();
        selectItems.setValue(items);

        selectOneMenu.getChildren().add(selectItems);

        selectOneMenu.encodeAll(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();
        assertEquals("<select name=\"j_id0\" size=\"1\">" +
                "\t<option value=\"mars\">mars</option>" +
                "\t<option value=\"jupiter\">jupiter</option></select>", output);
    }

    public void testRenderReadonly() throws Exception
    {
        List items = new ArrayList();
        items.add(new SelectItem("mars"));
        items.add(new SelectItem("jupiter"));

        UISelectItems selectItems = new UISelectItems();
        selectItems.setValue(items);

        selectOneMenu.getChildren().add(selectItems);

        selectOneMenu.setReadonly(true);

        selectOneMenu.encodeAll(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();
        assertEquals("<select name=\"j_id0\" size=\"1\" readonly=\"readonly\">" +
                "\t<option value=\"mars\">mars</option>" +
                "\t<option value=\"jupiter\">jupiter</option></select>", output);
    }


}