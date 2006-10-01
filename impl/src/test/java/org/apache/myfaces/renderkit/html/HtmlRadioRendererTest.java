/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
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
        List items = new ArrayList();
        items.add(new SelectItem("mars"));
        items.add(new SelectItem("jupiter"));

        UISelectItems selectItems = new UISelectItems();
        selectItems.setValue(items);

        selectOneRadio.getChildren().add(selectItems);

        selectOneRadio.encodeEnd(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();
        
        assertEquals("<table><tr>\t\t" +
                "<td><input id=\"_id0:0\" type=\"radio\" name=\"_id0\" value=\"mars\"/><label for=\"_id0:0\">&#160;mars</label></td>\t\t" +
                "<td><input id=\"_id0:1\" type=\"radio\" name=\"_id0\" value=\"jupiter\"/><label for=\"_id0:1\">&#160;jupiter</label></td>" +
                "</tr></table>", output);
    }
}
