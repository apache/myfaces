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
import jakarta.faces.component.html.HtmlPanelGroup;

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
        // NOTE: since Faces 5.0 (spec issue 1507) a component representing the HTML <body> element exposes
        // HtmlEvents#getHtmlBodyEventNames(), which are all element level events plus the window level events which
        // only the <body> element supports. See testWindowEventsAreBehaviorEvents() below.
        attrs = (HtmlRenderedClientEventAttr[])
            org.apache.myfaces.util.lang.ArrayUtils.concat(
                    HtmlClientEventAttributesUtil.generateClientBehaviorEventAttrs(),
                new HtmlRenderedClientEventAttr[]{
                    new HtmlRenderedClientEventAttr(HTML.ONLOAD_ATTR, ClientBehaviorEvents.LOAD),
                    new HtmlRenderedClientEventAttr(HTML.ONUNLOAD_ATTR, ClientBehaviorEvents.UNLOAD)
                });
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
     * Spec issue 1507: the HTML &lt;body&gt; element is the only element which supports the window level events, so
     * only a component representing it exposes them as behavior events. &lt;f:ajax event="load"/&gt; on
     * &lt;h:body&gt; therefore keeps working, and so do e.g. "unload", "pagehide" and "popstate".
     */
    @Test
    public void testWindowEventsAreBehaviorEvents()
    {
        HtmlBody body = new HtmlBody();

        Assertions.assertTrue(body.getEventNames().contains(ClientBehaviorEvents.CLICK));
        Assertions.assertTrue(body.getEventNames().contains(ClientBehaviorEvents.LOAD));
        Assertions.assertTrue(body.getEventNames().contains(ClientBehaviorEvents.UNLOAD));
        Assertions.assertTrue(body.getEventNames().contains(HtmlEvents.HtmlWindowEvent.pagehide.name()));
        Assertions.assertTrue(body.getEventNames().contains(HtmlEvents.HtmlWindowEvent.popstate.name()));

        // but not on any other component
        HtmlPanelGroup group = new HtmlPanelGroup();
        Assertions.assertTrue(group.getEventNames().contains(ClientBehaviorEvents.LOAD));
        Assertions.assertFalse(group.getEventNames().contains(ClientBehaviorEvents.UNLOAD));
        Assertions.assertFalse(group.getEventNames().contains(HtmlEvents.HtmlWindowEvent.pagehide.name()));

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
     * The body renderer renders onload/onunload itself, so the generic behavior event pass must not render them a
     * second time.
     */
    @Test
    public void testRendererHandledWindowEventIsRenderedOnce()
    {
        HtmlBody body = new HtmlBody();
        body.addClientBehavior(ClientBehaviorEvents.LOAD, new AjaxBehavior());

        try
        {
            body.encodeAll(facesContext);
            String output = outputWriter.toString();
            Assertions.assertTrue(output.contains(HTML.ONLOAD_ATTR + "=\""), output);
            Assertions.assertEquals(output.indexOf(HTML.ONLOAD_ATTR + "=\""),
                    output.lastIndexOf(HTML.ONLOAD_ATTR + "=\""), output);
        }
        catch (Exception e)
        {
            Assertions.fail(e.getMessage());
        }
    }

    /**
     * The window level events which the renderer does not handle itself are rendered generically.
     */
    @Test
    public void testGenericWindowEventIsRendered()
    {
        HtmlBody body = new HtmlBody();
        body.addClientBehavior(HtmlEvents.HtmlWindowEvent.pagehide.name(), new AjaxBehavior());

        try
        {
            body.encodeAll(facesContext);
            Assertions.assertTrue(outputWriter.toString().contains(" onpagehide=\""), outputWriter.toString());
        }
        catch (Exception e)
        {
            Assertions.fail(e.getMessage());
        }
    }

}
