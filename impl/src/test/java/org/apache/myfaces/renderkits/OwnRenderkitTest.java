package org.apache.myfaces.renderkits;

import java.io.StringWriter;

import javax.faces.FactoryFinder;
import javax.faces.component.html.HtmlInputText;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;

import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.shared_impl.util.ClassUtils;
import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockFacesContext12;
import org.apache.shale.test.mock.MockResponseWriter;

/**
 * @author martin.haimberger
 */
public class OwnRenderkitTest extends AbstractJsfTestCase {
    private MockResponseWriter writer;
    private HtmlInputText inputText;

    private static boolean isOwnRenderKit = false;

    public static void SetIsOwnRenderKit() {
        isOwnRenderKit = true;
    }


    public OwnRenderkitTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        addRenderKit();
        inputText = new HtmlInputText();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);
        MockFacesContext12.getCurrentInstance();

        facesContext.getViewRoot().setRenderKitId("OWN_BASIC");
        facesContext.getRenderKit().addRenderer(
                inputText.getFamily(),
                inputText.getRendererType(),
                new HtmlTextRenderer());

    }

    protected void tearDown() throws Exception {
        super.tearDown();
        inputText = null;
        writer = null;
        isOwnRenderKit = false;
    }

    public void testOwnRenderKit() throws Exception {

        inputText.encodeEnd(facesContext);
        facesContext.renderResponse();

        assertTrue(isOwnRenderKit);
    }


    private void addRenderKit() {
        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);

        String renderKitId = "OWN_BASIC";
        String renderKitClass = "org.apache.myfaces.renderkits.OwnRenderKitImpl";

        RenderKit renderKit = (RenderKit) ClassUtils.newInstance(renderKitClass);

        renderKitFactory.addRenderKit(renderKitId, renderKit);

    }

}
