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
        link.setValue("HelloLink");
        link.setStyleClass("linkClass");
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

     public void testLinkPassthrough() throws Exception
    {
        link.setAccesskey("accesskey");
        link.setCharset("charset");
        link.setCoords("coords");
        link.setDir("dir");
        link.setHreflang("hreflang");
        link.setLang("lang");
        link.setOnblur("onblur");
        link.setOndblclick("ondblclick");
        link.setOnfocus("onfocus");
        link.setOnkeydown("onkeydown");
        link.setOnkeypress("onkeypress");
        link.setOnkeyup("onkeyup");
        link.setOnmousedown("onmousedown");
        link.setOnmousemove("onmousemove");
        link.setOnmouseout("onmouseout");
        link.setOnmouseover("onmouseover");
        link.setOnmouseup("onmouseup");
        link.setRel("rel");
        link.setRev("rev");
        link.setShape("shape");
        link.setStyle("style");
        link.setTabindex("tabindex");
        link.setTitle("title");
        link.setType("type");

        HtmlLinkRenderer renderer = new HtmlLinkRenderer();
        renderer.encodeBegin(facesContext, link);
        renderer.encodeChildren(facesContext, link);
        renderer.encodeEnd(facesContext, link);

        facesContext.renderResponse();

        String output = writer.getWriter().toString();

        assertEquals("<a href=\"#\" onclick=\"clear_j_5Fid1();document.forms[&apos;j_id1&apos;].elements[&apos;j_id1:_link_hidden_&apos;]" +
                ".value=&apos;j_id1:j_id0&apos;;if(document.forms[&apos;j_id1&apos;].onsubmit){var result=document.forms[&apos;j_id1&apos;]" +
                ".onsubmit();  if( (typeof result == &apos;undefined&apos;) || result ) {document.forms[&apos;j_id1&apos;].submit();}}else" +
                "{document.forms[&apos;j_id1&apos;].submit();}return false;\" " +
                "accesskey=\"accesskey\" charset=\"charset\" coords=\"coords\" hreflang=\"hreflang\" " +
                "rel=\"rel\" rev=\"rev\" shape=\"shape\" tabindex=\"tabindex\" type=\"type\" " +
                "ondblclick=\"ondblclick\" onmousedown=\"onmousedown\" onmouseup=\"onmouseup\" " +
                "onmouseover=\"onmouseover\" onmousemove=\"onmousemove\" onmouseout=\"onmouseout\" " +
                "onkeypress=\"onkeypress\" onkeydown=\"onkeydown\" onkeyup=\"onkeyup\" onblur=\"onblur\" " +
                "onfocus=\"onfocus\" dir=\"dir\" lang=\"lang\" title=\"title\" style=\"style\"></a>", output);

    }
}
