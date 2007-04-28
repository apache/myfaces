package org.apache.myfaces.renderkit.html;

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockRenderKitFactory;
import org.apache.shale.test.mock.MockResponseWriter;

import javax.el.ValueExpression;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import java.io.StringWriter;

/**
 * @author Bruno Aranda (latest modification by $Author: baranda $)
 * @version $Revision: 451814 $ $Date: 2006-10-01 22:28:42 +0100 (dom, 01 oct 2006) $
 */
public class HtmlTableRendererTest extends AbstractJsfTestCase
{
    private static final String LINE_SEPARATOR = System.getProperty(
            "line.separator", "\r\n");

    private MockResponseWriter writer ;
    private HtmlDataTable dataTable;
    private HtmlOutputText colText;

    public HtmlTableRendererTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        dataTable = new HtmlDataTable();
        colText = new HtmlOutputText();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                dataTable.getFamily(),
                dataTable.getRendererType(),
                new HtmlTableRenderer());
        facesContext.getRenderKit().addRenderer(
                colText.getFamily(),
                colText.getRendererType(),
                new HtmlTextRenderer());

    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        dataTable = null;
        writer = null;
    }

    public void testRenderTable() throws Exception
    {
        dataTable.setValue(new Integer[] {1,2,3});
        dataTable.setVar("var");

        UIColumn col = new UIColumn();

        ValueExpression varValueExpression = facesContext.getApplication().getExpressionFactory()
                .createValueExpression(facesContext.getELContext(), "#{var}", Integer.class);
        colText.setValueExpression("value", varValueExpression);

        col.getChildren().add(colText);
        dataTable.getChildren().add(col);



        dataTable.encodeAll(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();

        assertEquals(LINE_SEPARATOR +
                "<table>" + LINE_SEPARATOR +
                "<tbody id=\"j_id0:tbody_element\">" + LINE_SEPARATOR +
                "<tr><td>1</td></tr>" + LINE_SEPARATOR +
                "<tr><td>2</td></tr>" + LINE_SEPARATOR +
                "<tr><td>3</td></tr></tbody></table>"+LINE_SEPARATOR, output);
    }

    public void testRenderTableWithColumnHeader() throws Exception
    {
        dataTable.setValue(new String[] {"One","Two","Three"});
        dataTable.setVar("var");

        UIColumn col = new UIColumn();
        HtmlOutputText headerTest = new HtmlOutputText();
        headerTest.setValue("colHeader");
        col.getFacets().put("header", headerTest);

        ValueExpression varValueExpression = facesContext.getApplication().getExpressionFactory()
                .createValueExpression(facesContext.getELContext(), "#{var}", String.class);
        colText.setValueExpression("value", varValueExpression);

        col.getChildren().add(colText);
        dataTable.getChildren().add(col);



        dataTable.encodeAll(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();

        assertEquals(LINE_SEPARATOR +
                "<table>" + LINE_SEPARATOR +
                "<thead>" + LINE_SEPARATOR +
                "<tr><th scope=\"col\">colHeader</th></tr></thead>" + LINE_SEPARATOR +
                "<tbody id=\"j_id0:tbody_element\">" + LINE_SEPARATOR +
                "<tr><td>One</td></tr>" + LINE_SEPARATOR +
                "<tr><td>Two</td></tr>" + LINE_SEPARATOR +
                "<tr><td>Three</td></tr></tbody></table>"+LINE_SEPARATOR, output);
    }


    public void testRenderTableWithHeader() throws Exception
    {
        dataTable.setValue(new String[] {"One","Two","Three"});
        dataTable.setVar("var");

        HtmlOutputText headerTest = new HtmlOutputText();
        headerTest.setValue("colHeader");
        dataTable.getFacets().put("header", headerTest);

        UIColumn col = new UIColumn();

        ValueExpression varValueExpression = facesContext.getApplication().getExpressionFactory()
                .createValueExpression(facesContext.getELContext(), "#{var}", String.class);
        colText.setValueExpression("value", varValueExpression);

        col.getChildren().add(colText);
        dataTable.getChildren().add(col);

        dataTable.encodeAll(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();
        assertEquals(LINE_SEPARATOR +
                "<table>" + LINE_SEPARATOR +
                "<thead>" + LINE_SEPARATOR +
                "<tr><th scope=\"colgroup\" colspan=\"1\">colHeader</th></tr></thead>" + LINE_SEPARATOR +
                "<tbody id=\"j_id0:tbody_element\">" + LINE_SEPARATOR +
                "<tr><td>One</td></tr>" + LINE_SEPARATOR +
                "<tr><td>Two</td></tr>" + LINE_SEPARATOR +
                "<tr><td>Three</td></tr></tbody></table>"+LINE_SEPARATOR, output);
    }

}