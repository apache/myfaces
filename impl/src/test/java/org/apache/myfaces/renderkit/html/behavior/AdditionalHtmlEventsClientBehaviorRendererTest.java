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

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlCommandButton;
import jakarta.faces.component.html.HtmlEvents;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.component.html.HtmlOutputLink;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.render.Renderer;

import org.apache.myfaces.view.facelets.component.JsfElement;

import org.apache.myfaces.test.el.MockValueExpression;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Spec issue 1507: every HTML event is a supported behavior event of the standard HTML components, even when the
 * component has no matching property. So both <code>oninput="..."</code> and
 * <code>&lt;f:ajax event="input"/&gt;</code> have to end up as an <code>oninput</code> attribute.
 * <p>
 * The inherited test cases cover exactly that for a set of events which have no property on any standard component,
 * this class adds the cases which are specific to this feature.
 */
public class AdditionalHtmlEventsClientBehaviorRendererTest extends AbstractClientBehaviorTestCase
{
    private HtmlRenderedClientEventAttr[] attrs = null;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        attrs = new HtmlRenderedClientEventAttr[]
        {
            new HtmlRenderedClientEventAttr("oninput", "input"),
            new HtmlRenderedClientEventAttr("onbeforeinput", "beforeinput"),
            new HtmlRenderedClientEventAttr("oncontextmenu", "contextmenu"),
            new HtmlRenderedClientEventAttr("oncopy", "copy"),
            new HtmlRenderedClientEventAttr("onpaste", "paste"),
            new HtmlRenderedClientEventAttr("ondrop", "drop"),
            new HtmlRenderedClientEventAttr("onwheel", "wheel"),
            new HtmlRenderedClientEventAttr("ontoggle", "toggle"),
            new HtmlRenderedClientEventAttr("oninvalid", "invalid")
        };
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
        return new HtmlInputText();
    }

    @Override
    protected HtmlRenderedClientEventAttr[] getClientBehaviorHtmlRenderedAttributes()
    {
        return attrs;
    }

    /**
     * A plain on* attribute without a matching property must be rendered as a pass through attribute.
     */
    @Test
    public void testPlainAttributeWithoutBehaviorIsRendered() throws Exception
    {
        HtmlInputText input = new HtmlInputText();
        input.getAttributes().put("oninput", "alert('input')");

        input.encodeAll(facesContext);

        Assertions.assertTrue(outputWriter.toString().contains(" oninput=\"alert('input')\""),
                outputWriter.toString());
    }

    /**
     * An on* attribute bound to a ValueExpression is neither a property nor part of the component attribute map key
     * set, so it needs the {@code EVENT_ATTRIBUTES_MARKED} marker to be found at render time.
     */
    @Test
    public void testValueExpressionAttributeWithoutBehaviorIsRendered() throws Exception
    {
        ValueExpression expression = new MockValueExpression("#{requestScope.oninputScript}", String.class);
        expression.setValue(facesContext.getELContext(), "alert(1)");

        HtmlInputText input = new HtmlInputText();
        input.setValueExpression("oninput", expression);

        input.encodeAll(facesContext);

        Assertions.assertTrue(outputWriter.toString().contains(" oninput=\"alert(1)\""), outputWriter.toString());
    }

    /**
     * An attribute which merely starts with "on" but is no known event must not be rendered, as required by the
     * standard HTML RenderKit: the substring after "on" has to be contained in getEventNames().
     */
    @Test
    public void testUnknownEventAttributeIsNotRendered() throws Exception
    {
        HtmlInputText input = new HtmlInputText();
        input.getAttributes().put("onsomethingweird", "alert('nope')");

        input.encodeAll(facesContext);

        Assertions.assertFalse(outputWriter.toString().contains("onsomethingweird"), outputWriter.toString());
    }

    /**
     * The very use case of the spec issue: an f:ajax on an event which no component declares as a property.
     */
    @Test
    public void testAjaxBehaviorOnAdditionalEventIsRendered() throws Exception
    {
        HtmlInputText input = new HtmlInputText();
        input.addClientBehavior("input", new AjaxBehavior());

        input.encodeAll(facesContext);

        String output = outputWriter.toString();
        Assertions.assertTrue(output.contains(" oninput=\""), output);
        Assertions.assertTrue(output.contains("myfaces.ab("), output);
    }

    /**
     * The renderer specific attributes must keep working exactly as before, next to the additional ones.
     */
    @Test
    public void testRendererSpecificAndAdditionalEventsAreCombined() throws Exception
    {
        HtmlInputText input = new HtmlInputText();
        input.setOnclick("alert('click')");
        input.getAttributes().put("oninput", "alert('input')");

        input.encodeAll(facesContext);

        String output = outputWriter.toString();
        Assertions.assertTrue(output.contains(" onclick=\"alert('click')\""), output);
        Assertions.assertTrue(output.contains(" oninput=\"alert('input')\""), output);
    }

    /**
     * Not only inputs; the feature applies to every standard HTML component, which are spread over quite a few
     * different renderers and rendering paths.
     */
    @Test
    public void testAdditionalEventsOnOtherComponents() throws Exception
    {
        assertAdditionalEventRendered(new HtmlCommandButton());
        assertAdditionalEventRendered(new HtmlOutputLink());

        HtmlPanelGroup group = new HtmlPanelGroup();
        group.setLayout("block");
        assertAdditionalEventRendered(group);
    }

    /**
     * An event which IS rendered by some renderers (here: focus by the field renderers) but NOT by the renderer at
     * hand (h:panelGroup only renders the mouse/key/click events itself) must be rendered generically as well.
     */
    @Test
    public void testFieldEventOnNonFieldComponent() throws Exception
    {
        HtmlPanelGroup group = new HtmlPanelGroup();
        group.setLayout("block");
        group.getAttributes().put("onfocus", "doFocus()");
        ((ClientBehaviorHolder) group).addClientBehavior("blur", new AjaxBehavior());

        group.encodeAll(facesContext);
        String output = outputWriter.toString();

        Assertions.assertTrue(output.contains(" onfocus=\"doFocus()\""), output);
        Assertions.assertTrue(output.contains(" onblur=\""), output);
    }

    /**
     * The opposite: an event which the renderer at hand DOES render itself must not be rendered a second time by
     * the generic pass.
     */
    @Test
    public void testRendererHandledEventIsNotRenderedTwice() throws Exception
    {
        HtmlInputText input = new HtmlInputText();
        input.setOnclick("doClick()");
        input.addClientBehavior("focus", new AjaxBehavior());

        input.encodeAll(facesContext);
        String output = outputWriter.toString();

        Assertions.assertEquals(output.indexOf(" onclick=\""), output.lastIndexOf(" onclick=\""), output);
        Assertions.assertEquals(output.indexOf(" onfocus=\""), output.lastIndexOf(" onfocus=\""), output);
    }

    private void assertAdditionalEventRendered(UIComponent component) throws Exception
    {
        component.getAttributes().put("oninput", "alert('input')");
        ((ClientBehaviorHolder) component).addClientBehavior("wheel", new AjaxBehavior());

        outputWriter.reset();
        component.encodeAll(facesContext);
        String output = outputWriter.toString();

        Assertions.assertTrue(output.contains(" oninput=\"alert('input')\""),
                component.getClass().getSimpleName() + ": " + output);
        Assertions.assertTrue(output.contains(" onwheel=\""),
                component.getClass().getSimpleName() + ": " + output);
    }

    /**
     * jsf:element mirrors the standard HTML components and must expose and render every HTML event as well.
     */
    @Test
    public void testAdditionalEventsOnJsfElement() throws Exception
    {
        JsfElement element = new JsfElement();
        element.getPassThroughAttributes().put(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY, "div");
        element.getAttributes().put("oninput", "doInput()");
        element.addClientBehavior("wheel", new AjaxBehavior());

        Assertions.assertTrue(element.getEventNames().contains("input"));
        Assertions.assertTrue(element.getEventNames().contains("scrollend"));

        element.encodeAll(facesContext);
        String output = outputWriter.toString();

        Assertions.assertTrue(output.contains(" oninput=\"doInput()\""), output);
        Assertions.assertTrue(output.contains(" onwheel=\""), output);
    }

    /**
     * The event names must reflect the component type, as the spec assigns a different set per component category.
     */
    @Test
    public void testEventNamesPerComponentCategory()
    {
        // EditableValueHolder
        Assertions.assertTrue(new HtmlInputText().getEventNames().contains("valueChange"));
        Assertions.assertFalse(new HtmlInputText().getEventNames().contains("action"));

        // ActionSource
        Assertions.assertTrue(new HtmlCommandButton().getEventNames().contains("action"));
        Assertions.assertFalse(new HtmlCommandButton().getEventNames().contains("valueChange"));

        // neither of both
        Assertions.assertFalse(new HtmlPanelGroup().getEventNames().contains("action"));
        Assertions.assertFalse(new HtmlPanelGroup().getEventNames().contains("valueChange"));

        // all of them expose the plain HTML events, including the window level ones
        for (UIComponent component : new UIComponent[]
                { new HtmlInputText(), new HtmlCommandButton(), new HtmlPanelGroup() })
        {
            ClientBehaviorHolder holder = (ClientBehaviorHolder) component;
            Assertions.assertTrue(holder.getEventNames().contains("input"));
            Assertions.assertTrue(holder.getEventNames().contains("click"));
            Assertions.assertTrue(holder.getEventNames().contains("scrollend"));
        }
    }

    /**
     * The default event names as mandated by the spec.
     */
    @Test
    public void testDefaultEventNames()
    {
        Assertions.assertEquals("valueChange", new HtmlInputText().getDefaultEventName());
        Assertions.assertEquals("action", new HtmlCommandButton().getDefaultEventName());
        Assertions.assertEquals("click", new HtmlOutputLink().getDefaultEventName());
        Assertions.assertNull(new HtmlPanelGroup().getDefaultEventName());
    }

    /**
     * Unknown event names can be added via context-param, and are then usable like any built in one.
     */
    @Test
    public void testAdditionalEventNamesContextParam() throws Exception
    {
        servletContext.addInitParameter(HtmlEvents.ADDITIONAL_HTML_EVENT_NAMES_PARAM_NAME, "  foo\t bar\nfoo ");

        Assertions.assertTrue(HtmlEvents.getAdditionalHtmlEventNames(facesContext).contains("foo"));
        Assertions.assertTrue(HtmlEvents.getAdditionalHtmlEventNames(facesContext).contains("bar"));

        HtmlInputText input = new HtmlInputText();
        Assertions.assertTrue(input.getEventNames().contains("foo"));

        input.getAttributes().put("onfoo", "alert('foo')");
        input.addClientBehavior("bar", new AjaxBehavior());
        input.encodeAll(facesContext);

        String output = outputWriter.toString();
        Assertions.assertTrue(output.contains(" onfoo=\"alert('foo')\""), output);
        Assertions.assertTrue(output.contains(" onbar=\""), output);
    }

    /**
     * Without the context-param there must be no stray empty event name, and the collections must be sorted,
     * distinct and layered as documented.
     */
    @Test
    public void testEventNameCollections()
    {
        Assertions.assertTrue(HtmlEvents.getAdditionalHtmlEventNames(facesContext).isEmpty());

        Assertions.assertTrue(HtmlEvents.getHtmlBodyElementEventNames(facesContext)
                .containsAll(HtmlEvents.getHtmlDocumentElementEventNames(facesContext)));
        Assertions.assertTrue(HtmlEvents.getFacesActionSourceEventNames(facesContext)
                .containsAll(HtmlEvents.getHtmlBodyElementEventNames(facesContext)));
        Assertions.assertTrue(HtmlEvents.getFacesEditableValueHolderEventNames(facesContext)
                .containsAll(HtmlEvents.getHtmlBodyElementEventNames(facesContext)));

        // the window level events are exclusive to the body element event names
        Assertions.assertFalse(HtmlEvents.getHtmlDocumentElementEventNames(facesContext).contains("load"));
        Assertions.assertTrue(HtmlEvents.getHtmlBodyElementEventNames(facesContext).contains("load"));

        String previous = null;
        for (String eventName : HtmlEvents.getFacesEditableValueHolderEventNames(facesContext))
        {
            Assertions.assertTrue(previous == null || previous.compareTo(eventName) < 0,
                    "not sorted and distinct at " + eventName);
            previous = eventName;
        }
    }
}
