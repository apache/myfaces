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

package org.apache.myfaces.view.facelets.tag.faces.core;

import org.apache.myfaces.view.facelets.tag.faces.core.reset.ResetValuesBean;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIGraphic;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlCommandButton;
import jakarta.faces.component.html.HtmlCommandLink;
import jakarta.faces.component.html.HtmlDataTable;
import jakarta.faces.component.html.HtmlForm;
import jakarta.faces.component.html.HtmlGraphicImage;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.convert.DateTimeConverter;
import jakarta.faces.convert.NumberConverter;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import jakarta.faces.validator.DoubleRangeValidator;
import jakarta.faces.validator.LengthValidator;
import jakarta.faces.validator.LongRangeValidator;
import jakarta.faces.validator.Validator;
import org.apache.myfaces.renderkit.html.HtmlButtonRenderer;

import org.apache.myfaces.renderkit.html.HtmlFormRenderer;
import org.apache.myfaces.renderkit.html.HtmlImageRenderer;
import org.apache.myfaces.renderkit.html.HtmlLinkRenderer;
import org.apache.myfaces.renderkit.html.HtmlTableRenderer;
import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CoreTestCase extends AbstractFaceletTestCase
{

    @Override
    protected void setupComponents() throws Exception
    {
        application.addComponent(UIViewRoot.COMPONENT_TYPE, UIViewRoot.class
                .getName());
        application.addComponent(UIPanel.COMPONENT_TYPE,
                UIPanel.class.getName());
        application.addComponent(HtmlCommandLink.COMPONENT_TYPE,
                HtmlCommandLink.class.getName());
        application.addComponent(HtmlCommandButton.COMPONENT_TYPE,
                HtmlCommandButton.class.getName());
        application.addComponent(HtmlGraphicImage.COMPONENT_TYPE,
                HtmlGraphicImage.class.getName());
        application.addComponent(HtmlForm.COMPONENT_TYPE, HtmlForm.class
                .getName());
        application.addComponent(HtmlOutputText.COMPONENT_TYPE,
                HtmlOutputText.class.getName());
        application.addComponent(HtmlInputText.COMPONENT_TYPE,
                HtmlInputText.class.getName());
        application.addComponent(HtmlDataTable.COMPONENT_TYPE,
                HtmlDataTable.class.getName());
    }

    @Override
    protected void setupConvertersAndValidators() throws Exception
    {
        application.addConverter(DateTimeConverter.CONVERTER_ID,
                DateTimeConverter.class.getName());
        application.addConverter(NumberConverter.CONVERTER_ID,
                NumberConverter.class.getName());
        application.addValidator(LengthValidator.VALIDATOR_ID,
                LengthValidator.class.getName());
        application.addValidator(DoubleRangeValidator.VALIDATOR_ID,
                DoubleRangeValidator.class.getName());
        application.addValidator(LongRangeValidator.VALIDATOR_ID,
                LongRangeValidator.class.getName());
    }

    @Override
    protected void setupRenderers() throws Exception
    {
        renderKit.addRenderer(UIOutput.COMPONENT_FAMILY, "jakarta.faces.Text",
                new HtmlTextRenderer());
        renderKit.addRenderer(UIInput.COMPONENT_FAMILY, "jakarta.faces.Text",
                new HtmlTextRenderer());
        renderKit.addRenderer(UIGraphic.COMPONENT_FAMILY, "jakarta.faces.Image",
                new HtmlImageRenderer());
        renderKit.addRenderer(UICommand.COMPONENT_FAMILY, "jakarta.faces.Link",
                new HtmlLinkRenderer());
        renderKit.addRenderer(UIForm.COMPONENT_FAMILY, "jakarta.faces.Form",
                new HtmlFormRenderer());
        renderKit.addRenderer(UIData.COMPONENT_FAMILY, "jakarta.faces.Table",
                new HtmlTableRenderer());
        renderKit.addRenderer(UICommand.COMPONENT_FAMILY, "jakarta.faces.Button",
                new HtmlButtonRenderer());
        
    }

    @Test
    public void testActionListenerHandler() throws Exception
    {
        ActionListener listener = new ActionListenerImpl();

        facesContext.getExternalContext().getRequestMap().put("actionListener",
                listener);

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "actionListener.xml");

        UICommand action1 = (UICommand) root.findComponent("action1");
        UICommand action2 = (UICommand) root.findComponent("action2");

        Assertions.assertNotNull(action1);
        Assertions.assertNotNull(action2);

        Assertions.assertEquals(1,action1.getActionListeners().length);
        Assertions.assertEquals(2,action2.getActionListeners().length);
    }

    @Test
    public void testAttributeHandler() throws Exception
    {
        String title = "Dog in a Funny Hat";
        facesContext.getExternalContext().getRequestMap().put("title", title);

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "attribute.xml");

        HtmlGraphicImage graphic1 = (HtmlGraphicImage) root
                .findComponent("graphic1");
        HtmlGraphicImage graphic2 = (HtmlGraphicImage) root
                .findComponent("graphic2");

        Assertions.assertNotNull(graphic1);
        Assertions.assertNotNull(graphic2);

        Assertions.assertEquals("literal", graphic1.getTitle());
        Assertions.assertEquals(title, graphic2.getTitle());
    }

    @Test
    public void testConvertDateTimeHandler() throws Exception
    {
        Date now = new Date(1000 * 360 * 60 * 24 * 7);
        facesContext.getExternalContext().getRequestMap().put("now", now);
        UIViewRoot root = facesContext.getViewRoot();
        root.setLocale(Locale.US);
        vdl.buildView(facesContext, root, "convertDateTime.xml");

        UIOutput out1 = (UIOutput) root.findComponent("form:out1");
        UIOutput out2 = (UIOutput) root.findComponent("form:out2");
        UIOutput out3 = (UIOutput) root.findComponent("form:out3");
        UIOutput out4 = (UIOutput) root.findComponent("form:out4");
        UIOutput out5 = (UIOutput) root.findComponent("form:out5");
        UIOutput out6 = (UIOutput) root.findComponent("form:out6");

        Assertions.assertNotNull(out1);
        Assertions.assertNotNull(out2);
        Assertions.assertNotNull(out3);
        Assertions.assertNotNull(out4);
        Assertions.assertNotNull(out5);
        Assertions.assertNotNull(out6);

        Assertions.assertNotNull(out1.getConverter());
        Assertions.assertNotNull(out2.getConverter());
        Assertions.assertNotNull(out3.getConverter());
        Assertions.assertNotNull(out4.getConverter());
        Assertions.assertNotNull(out5.getConverter());
        DateTimeConverter converter6 = (DateTimeConverter) out6.getConverter();

        Assertions.assertEquals("12/24/69", out1.getConverter().getAsString(
                facesContext, out1, now));
        Assertions.assertEquals("12/24/69 6:57:12 AM", out2.getConverter()
                .getAsString(facesContext, out2, now));
        Assertions.assertEquals("Dec 24, 1969", out3.getConverter()
                .getAsString(facesContext, out3, now));
        Assertions.assertEquals("6:57:12 AM", out4.getConverter()
                .getAsString(facesContext, out4, now));
        Assertions.assertEquals("0:57 AM, CST", out5.getConverter()
                .getAsString(facesContext, out5, now));
        Assertions.assertEquals(TimeZone.getTimeZone("GMT"), converter6.getTimeZone());
    }

    @Test
    public void testConvertDelegateHandler() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        root.setLocale(Locale.US);
        vdl.buildView(facesContext, root, "converter.xml");

        UIOutput out1 = (UIOutput) root.findComponent("out1");

        Assertions.assertNotNull( out1);

        Assertions.assertNotNull(out1.getConverter());

        Assertions.assertEquals(new Double(42.5), out1.getConverter()
                .getAsObject(facesContext, out1, out1.getLocalValue().toString()));
    }

    @Test
    public void testConvertNumberHandler() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        root.setLocale(Locale.US);
        vdl.buildView(facesContext, root, "convertNumber.xml");

        UIOutput out1 = (UIOutput) root.findComponent("out1");
        UIOutput out2 = (UIOutput) root.findComponent("out2");
        UIOutput out3 = (UIOutput) root.findComponent("out3");
        UIOutput out4 = (UIOutput) root.findComponent("out4");
        UIOutput out5 = (UIOutput) root.findComponent("out5");

        Assertions.assertNotNull(out1);
        Assertions.assertNotNull(out2);
        Assertions.assertNotNull(out3);
        Assertions.assertNotNull(out4);
        Assertions.assertNotNull(out5);

        Assertions.assertNotNull(out1.getConverter());
        Assertions.assertNotNull(out2.getConverter());
        Assertions.assertNotNull(out3.getConverter());
        Assertions.assertNotNull(out4.getConverter());
        Assertions.assertNotNull(out5.getConverter());

        Assertions.assertEquals("12", out1.getConverter().getAsString(facesContext,
                out1, new Double(12.001)));
        Assertions.assertEquals("$12.00", out2.getConverter().getAsString(
                facesContext, out2, new Double(12.00)));
        Assertions.assertEquals("00,032", out3.getConverter().getAsString(
                facesContext, out3, new Double(32)));
        Assertions.assertEquals("0.67", out4.getConverter().getAsString(
                facesContext, out4, new Double(2.0 / 3.0)));
        Assertions.assertEquals("67%", out5.getConverter().getAsString(
                facesContext, out5, new Double(0.67)));
    }

    @Test
    public void testFacetHandler() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "facet.xml");

        UIData data = (UIData) root.findComponent("table");

        Assertions.assertNotNull(data);

        UIComponent footer = data.getFooter();

        Assertions.assertNotNull(footer);
    }

    @Test
    public void testLoadBundleHandler() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "loadBundle.xml");

        Object value = facesContext.getExternalContext().getRequestMap().get("foo");

        Assertions.assertNotNull(value);
        Assertions.assertTrue(value instanceof Map);
        String result = (String) ((Map) value).get("some.not.found.key");
        Assertions.assertTrue(result.contains("???"));
    }

    @Test
    public void testValidateDelegateHandler() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "validator.xml");

        UIInput input = (UIInput) root.findComponent("form:input");

        Assertions.assertNotNull(input);

        Assertions.assertEquals(1, input.getValidators().length);

        Validator v = input.getValidators()[0];

        v.validate(facesContext, input, "4333");
    }

    @Test
    public void testValidateDoubleRangeHandler() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "validateDoubleRange.xml");

        UIInput input = (UIInput) root.findComponent("form:input");

        Assertions.assertNotNull(input);

        Assertions.assertEquals(1, input.getValidators().length);

        Validator v = input.getValidators()[0];

        v.validate(facesContext, input, new Double(1.8));
    }

    @Test
    public void testValidateLengthHandler() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "validateLength.xml");

        UIInput input = (UIInput) root.findComponent("form:input");

        Assertions.assertNotNull(input);

        Assertions.assertEquals(1, input.getValidators().length);

        Validator v = input.getValidators()[0];

        v.validate(facesContext, input, "beans");
    }

    @Test
    public void testValidateLongRangeHandler() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "validateLongRange.xml");

        UIInput input = (UIInput) root.findComponent("form:input");

        Assertions.assertNotNull(input);

        Assertions.assertEquals(1, input.getValidators().length);

        Validator v = input.getValidators()[0];

        v.validate(facesContext, input, new Long(2000));
    }

    @Test
    public void testValueChangeListenerHandler() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "valueChangeListener.xml");

        UIInput input = (UIInput) root.findComponent("form:input");

        Assertions.assertNotNull(input);

        Assertions.assertEquals(1,
                input.getValueChangeListeners().length);
    }

    @Test
    public void testViewHandler() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "view.xml");

        Assertions.assertEquals(Locale.GERMAN, root.getLocale());
    }
    
    @Test
    public void testResetValuesActionListenerHandler() throws Exception
    {
        ResetValuesBean bean = new ResetValuesBean();

        facesContext.getExternalContext().getRequestMap().put("bean", bean);
        bean.setField1("Hello");
        bean.setField2(2);

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "resetValuesActionListener_1.xhtml");

        UICommand action1 = (UICommand) root.findComponent("mainForm:submit");

        Assertions.assertNotNull(action1);

        Assertions.assertEquals(1, action1.getActionListeners().length);

        UIInput field1 = (UIInput) root.findComponent("mainForm:field1");
        field1.setValue("xxx");
        Assertions.assertEquals("xxx", field1.getValue());
        Assertions.assertTrue(field1.isLocalValueSet());
        
        UIInput field2 = (UIInput) root.findComponent("mainForm:field2");
        field2.setSubmittedValue("1");
        field2.setValid(false);
        
        action1.getActionListeners()[0].processAction(new ActionEvent(action1));
        
        // If resetValues() was activated, 
        Assertions.assertEquals("Hello",field1.getValue());
        Assertions.assertFalse(field1.isLocalValueSet());
        Assertions.assertNull(field2.getSubmittedValue());
        Assertions.assertTrue(field2.isValid());
    }


}
