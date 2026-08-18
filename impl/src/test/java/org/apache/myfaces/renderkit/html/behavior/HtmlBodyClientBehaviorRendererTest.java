/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.renderkit.html.behavior;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlBody;
import jakarta.faces.component.html.HtmlEvents;

import org.apache.myfaces.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlBodyClientBehaviorRendererTest extends AbstractClientBehaviorTestCase
{
    private HtmlRenderedClientEventAttr[] attrs = null;
    
    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        // NOTE: "load" and "unload" are deliberately absent here. Since Faces 5.0 (spec issue 1507) a component
        // representing the HTML <body> element exposes HtmlEvents#getHtmlDocumentElementEventNames(), which excludes
        // the events that the <body> element forwards to the Window object instead of firing on the element itself.
        // See testWindowEventsAreNoBehaviorEvents() and testWindowEventsCanBeReAddedViaContextParam() below.
        attrs = HtmlClientEventAttributesUtil.generateClientBehaviorEventAttrs();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        super.tearDown();
        attrs = null;
    }

    @Override
    protected UIComponent createComponentToTest()
    {
        return new HtmlBody();
    }

    @Override
    protected HtmlRenderedClientEventAttr[] getClientBehaviorHtmlRenderedAttributes()
    {
        return attrs;
    }
    
    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     * <body> does not have "name", so we just need to check "id"
     */
    @Test
    public void testClientBehaviorHolderRendersIdAndName() 
    {
        HtmlRenderedClientEventAttr[] attrs = getClientBehaviorHtmlRenderedAttributes();
        
        for (int i = 0; i < attrs.length; i++)
        {
            UIComponent component = createComponentToTest();
            ClientBehaviorHolder clientBehaviorHolder = (ClientBehaviorHolder) component;
            clientBehaviorHolder.addClientBehavior(attrs[i].getClientEvent(), new AjaxBehavior());
            try 
            {
                component.encodeAll(facesContext);
                String output = outputWriter.toString();
                Assertions.assertTrue(output.contains("id=\"j_id__"));
                outputWriter.reset();
            }
            catch (Exception e)
            {
                Assertions.fail(e.getMessage());
            }
        }
    }

    /**
     * Spec issue 1507: the events which the HTML &lt;body&gt; element forwards to the Window object must not be
     * exposed as behavior events of the component, so &lt;f:ajax event="load"/&gt; is no longer supported on
     * &lt;h:body&gt;. The plain onload attribute is unaffected, it is still a property of the component.
     */
    @Test
    public void testWindowEventsAreNoBehaviorEvents()
    {
        HtmlBody body = new HtmlBody();

        Assertions.assertFalse(body.getEventNames().contains(ClientBehaviorEvents.LOAD));
        Assertions.assertFalse(body.getEventNames().contains(ClientBehaviorEvents.UNLOAD));
        Assertions.assertTrue(body.getEventNames().contains(ClientBehaviorEvents.CLICK));

        // still supported as a plain attribute
        body.setOnload("alert('load')");
        try
        {
            body.encodeAll(facesContext);
            Assertions.assertTrue(outputWriter.toString().contains(HTML.ONLOAD_ATTR + "=\"alert('load')\""));
        }
        catch (Exception e)
        {
            Assertions.fail(e.getMessage());
        }
    }

    /**
     * Spec issue 1507: whatever HtmlEvents does not know about can be added via a context-param, which is also the
     * migration path for applications relying on &lt;f:ajax event="load"/&gt; on &lt;h:body&gt;.
     */
    @Test
    public void testWindowEventsCanBeReAddedViaContextParam()
    {
        servletContext.addInitParameter(HtmlEvents.ADDITIONAL_HTML_EVENT_NAMES_PARAM_NAME, "load unload");

        HtmlBody body = new HtmlBody();

        Assertions.assertTrue(body.getEventNames().contains(ClientBehaviorEvents.LOAD));
        Assertions.assertTrue(body.getEventNames().contains(ClientBehaviorEvents.UNLOAD));

        body.addClientBehavior(ClientBehaviorEvents.LOAD, new AjaxBehavior());
        try
        {
            body.encodeAll(facesContext);
            String output = outputWriter.toString();
            Assertions.assertTrue(output.contains(HTML.ONLOAD_ATTR + "=\""), output);
            // the body renderer renders onload itself; the generic pass must not render it a second time
            Assertions.assertEquals(output.indexOf(HTML.ONLOAD_ATTR + "=\""),
                    output.lastIndexOf(HTML.ONLOAD_ATTR + "=\""), output);
        }
        catch (Exception e)
        {
            Assertions.fail(e.getMessage());
        }
    }

}
