package org.apache.myfaces.renderkit.html;

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockResponseWriter;
import org.apache.shale.test.mock.MockRenderKitFactory;

import javax.faces.component.html.HtmlSelectOneRadio;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.UISelectItems;
import javax.faces.model.SelectItem;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Bruno Aranda (latest modification by $Author: baranda $)
 * @version $Revision: 451814 $ $Date: 2006-10-01 22:28:42 +0100 (dom, 01 oct 2006) $
 */
public class HtmTextRendererTest extends AbstractJsfTestCase
{
    private MockResponseWriter writer ;
    private HtmlInputText inputText;

    public HtmTextRendererTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        inputText = new HtmlInputText();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                inputText.getFamily(),
                inputText.getRendererType(),
                new HtmlTextRenderer());

    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        inputText = null;
        writer = null;
    }

    public void testInputTextDefault() throws Exception
    {
        inputText.encodeEnd(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();

        assertEquals("<input id=\"j_id0\" name=\"j_id0\" type=\"text\" value=\"\"/>", output);
    }
}
