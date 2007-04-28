package org.apache.myfaces.renderkit.html;

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockRenderKitFactory;
import org.apache.shale.test.mock.MockResponseWriter;

import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import java.io.StringWriter;

/**
 * @author Bruno Aranda (latest modification by $Author: baranda $)
 * @version $Revision: 451814 $ $Date: 2006-10-01 22:28:42 +0100 (dom, 01 oct 2006) $
 */
public class HtmlGridRendererTest extends AbstractJsfTestCase
{
    private static final String LINE_SEPARATOR = System.getProperty(
            "line.separator", "\r\n");

    private MockResponseWriter writer ;
    private HtmlPanelGrid panelGrid;
    private HtmlOutputText colText;

    public HtmlGridRendererTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        panelGrid = new HtmlPanelGrid();
        colText = new HtmlOutputText();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                panelGrid.getFamily(),
                panelGrid.getRendererType(),
                new HtmlGridRenderer());
        facesContext.getRenderKit().addRenderer(
                colText.getFamily(),
                colText.getRendererType(),
                new HtmlTextRenderer());

    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        panelGrid = null;
        writer = null;
    }

    public void testRenderTable() throws Exception
    {
        UIColumn col1 = new UIColumn();
        HtmlOutputText col1Text = new HtmlOutputText();
        col1Text.setValue("col1Text");

        UIColumn col2 = new UIColumn();
        HtmlOutputText col2Text = new HtmlOutputText();
        col2Text.setValue("col2Text");

        col1.getChildren().add(col1Text);
        col2.getChildren().add(col2Text);
        panelGrid.getChildren().add(col1);
        panelGrid.getChildren().add(col2);

        panelGrid.encodeAll(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();
        assertEquals("<table><tbody><tr><td>col1Text</td></tr>" + LINE_SEPARATOR +
                "<tr><td>col2Text</td></tr>" + LINE_SEPARATOR +
                "</tbody></table>", output);
    }

    public void testRenderTableWithCaption() throws Exception
    {
        HtmlOutputText captionText = new HtmlOutputText();
        captionText.setValue("captionText");

        panelGrid.getFacets().put("caption", captionText);
        panelGrid.setCaptionClass("captionClass");
        panelGrid.setCaptionStyle("captionStyle");

        UIColumn col1 = new UIColumn();
        HtmlOutputText col1Text = new HtmlOutputText();
        col1Text.setValue("col1Text");

        UIColumn col2 = new UIColumn();
        HtmlOutputText col2Text = new HtmlOutputText();
        col2Text.setValue("col2Text");

        col1.getChildren().add(col1Text);
        col2.getChildren().add(col2Text);
        panelGrid.getChildren().add(col1);
        panelGrid.getChildren().add(col2);

        panelGrid.encodeAll(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();
        assertEquals("<table>" + LINE_SEPARATOR +
                "<caption class=\"captionClass\" style=\"captionStyle\">captionText</caption><tbody><tr><td>col1Text</td></tr>" + LINE_SEPARATOR +
                "<tr><td>col2Text</td></tr>" + LINE_SEPARATOR +
                "</tbody></table>", output);
    }


}