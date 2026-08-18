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
package org.apache.myfaces.view.facelets.tag.faces.html;

import java.io.StringWriter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlInputText;

import org.apache.myfaces.renderkit.html.HtmlAjaxBehaviorRenderer;
import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Spec issue 1507, end to end: an on* attribute without a matching component property has to survive the facelet
 * tag handling and end up in the rendered markup, on its own as well as chained with an f:ajax on the same event.
 */
public class AdditionalHtmlEventsTestCase extends AbstractFaceletTestCase
{

    @Override
    protected void setupComponents() throws Exception
    {
        application.addComponent(UIViewRoot.COMPONENT_TYPE, UIViewRoot.class.getName());
        application.addComponent(UIOutput.COMPONENT_TYPE, UIOutput.class.getName());
        application.addComponent(HtmlInputText.COMPONENT_TYPE, HtmlInputText.class.getName());
        application.addBehavior(AjaxBehavior.BEHAVIOR_ID, AjaxBehavior.class.getName());
    }

    @Override
    protected void setupConvertersAndValidators() throws Exception
    {
    }

    @Override
    protected void setupRenderers() throws Exception
    {
        renderKit.addRenderer(UIInput.COMPONENT_FAMILY, "jakarta.faces.Text", new HtmlTextRenderer());
        renderKit.addRenderer(UIOutput.COMPONENT_FAMILY, "jakarta.faces.Text", new HtmlTextRenderer());
        renderKit.addClientBehaviorRenderer(AjaxBehavior.BEHAVIOR_ID, new HtmlAjaxBehaviorRenderer());
    }

    @Test
    public void testAdditionalHtmlEvents() throws Exception
    {
        externalContext.getRequestMap().put("inputScript", "doEl()");

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "additionalHtmlEvents.xml");

        // a literal on* attribute without matching property
        String plain = encode(root, "plain");
        Assertions.assertTrue(plain.contains("oninput=\"doPlain()\""), plain);

        // the same, but bound to a ValueExpression
        String el = encode(root, "el");
        Assertions.assertTrue(el.contains("oninput=\"doEl()\""), el);

        // <f:ajax event="input"> on a component which has no oninput property
        Assertions.assertEquals("[input]",
                ((ClientBehaviorHolder) root.findComponent("ajax")).getClientBehaviors().keySet().toString());
        String ajax = encode(root, "ajax");
        Assertions.assertTrue(ajax.contains("oninput=\""), ajax);
        Assertions.assertTrue(ajax.contains("myfaces.ab("), ajax);

        // both of them on the same component must be chained
        String both = encode(root, "both");
        Assertions.assertTrue(both.contains("faces.util.chain("), both);
        Assertions.assertTrue(both.contains("doBoth()"), both);
        Assertions.assertTrue(both.contains("myfaces.ab("), both);
    }

    private String encode(UIViewRoot root, String id) throws Exception
    {
        UIComponent component = root.findComponent(id);
        Assertions.assertNotNull(component, "component " + id + " not found");

        StringWriter sw = new StringWriter();
        facesContext.setResponseWriter(new MockResponseWriter(sw));
        component.encodeAll(facesContext);
        sw.flush();
        return sw.toString();
    }
}
