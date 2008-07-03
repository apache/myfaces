package org.apache.myfaces.renderkit.html;

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockRenderKitFactory;
import org.apache.shale.test.mock.MockResponseWriter;

import javax.faces.component.html.HtmlGraphicImage;
import java.io.StringWriter;

/**
 * @author Bruno Aranda (latest modification by $Author: baranda $)
 * @version $Revision: 451814 $ $Date: 2006-10-01 22:28:42 +0100 (dom, 01 oct 2006) $
 */
public class HtmlImageRendererTest extends AbstractJsfTestCase
{
    private MockResponseWriter writer ;
    private HtmlGraphicImage graphicImage;

    public HtmlImageRendererTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        graphicImage = new HtmlGraphicImage();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                graphicImage.getFamily(),
                graphicImage.getRendererType(),
                new HtmlImageRenderer());

    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        graphicImage = null;
        writer = null;
    }

    public void testRenderDefault() throws Exception
    {
        graphicImage.setId("img1");
        graphicImage.setValue("http://myfaces.apache.org");
        graphicImage.encodeAll(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();
        assertEquals("<img id=\"img1\" src=\"nullhttp://myfaces.apache.org\"/>", output);
    }

    public void testRenderNoValue() throws Exception
    {
        graphicImage.setId("img1");

        graphicImage.encodeAll(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();
        assertEquals("<img id=\"img1\"/>", output);
    }

}