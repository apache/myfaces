/*
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.myfaces.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import jakarta.faces.application.FacesMessage;

import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.junit.jupiter.api.Test;

/**
 * TestCase for MessageUtils
 */
class MessageUtilsTest extends AbstractFacesTestCase
{
    private static final String DEFAULT_BUNDLE = "jakarta.faces.Messages";

    /**
     * Test method for 'org.apache.myfaces.shared.util.MessageUtils.getMessage(Severity, String, Object)'
     */
    @Test
    void getMessageSeverityStringObject()
    {
        facesContext.getViewRoot().setLocale(Locale.ENGLISH);

        FacesMessage msg = MessageUtils.getMessage(FacesMessage.Severity.ERROR,
                "jakarta.faces.component.UIInput.CONVERSION", null);
        assertEquals("{0}: Conversion error occurred.", msg.getSummary());

        facesContext.getViewRoot().setLocale(Locale.GERMAN);

        msg = MessageUtils.getMessage(FacesMessage.Severity.ERROR,
                "jakarta.faces.component.UIInput.CONVERSION",
                "blubb");
        assertEquals("blubb: Ein Konvertierungsfehler ist aufgetreten.", msg.getSummary());
    }

    /**
     * Test method for 'org.apache.myfaces.shared.util.MessageUtils.getMessage(Severity, String, Object[])'
     */
    @Test
    void getMessageSeverityStringObjectArray()
    {
        facesContext.getViewRoot().setLocale(Locale.ENGLISH);

        FacesMessage msg = MessageUtils.getMessage(FacesMessage.Severity.ERROR,
                "jakarta.faces.component.UIInput.CONVERSION", null);
        assertEquals("{0}: Conversion error occurred.", msg.getSummary());

        facesContext.getViewRoot().setLocale(Locale.GERMAN);

        msg = MessageUtils.getMessage(FacesMessage.Severity.ERROR,
                "jakarta.faces.component.UIInput.CONVERSION", null);
        assertEquals("{0}: Ein Konvertierungsfehler ist aufgetreten.", msg.getSummary());
    }

    /**
     * Test method for 'org.apache.myfaces.shared.util.MessageUtils.getMessage(Severity, String, Object[], FacesContext)'
     */
    @Test
    void getMessageSeverityStringObjectArrayFacesContext()
    {
        facesContext.getViewRoot().setLocale(Locale.ENGLISH);

        FacesMessage msg = MessageUtils.getMessage(FacesMessage.Severity.ERROR,
                "jakarta.faces.component.UIInput.CONVERSION", null, facesContext);
        assertEquals("{0}: Conversion error occurred.", msg.getSummary());

        facesContext.getViewRoot().setLocale(Locale.GERMAN);

        msg = MessageUtils.getMessage(FacesMessage.Severity.ERROR,
                "jakarta.faces.component.UIInput.CONVERSION", null,
                facesContext);
        assertEquals("{0}: Ein Konvertierungsfehler ist aufgetreten.", msg.getSummary());
    }

    /**
     * Test method for 'org.apache.myfaces.shared.util.MessageUtils.getMessage(Locale, String, Object[])'
     */
    @Test
    void getMessageLocaleStringObjectArray()
    {
        facesContext.getViewRoot().setLocale(Locale.ENGLISH);

        FacesMessage msg = org.apache.myfaces.util.MessageUtils.getMessage(Locale.ENGLISH,
                "jakarta.faces.component.UIInput.CONVERSION", null);
        assertEquals("{0}: Conversion error occurred.", msg.getSummary());

        msg = MessageUtils.getMessage(Locale.GERMAN,
                "jakarta.faces.component.UIInput.CONVERSION", null);
        assertEquals("{0}: Ein Konvertierungsfehler ist aufgetreten.", msg.getSummary());

    }

    /**
     * Test method for 'org.apache.myfaces.shared.util.MessageUtils.getMessage(FacesContext, String)'
     */
    @Test
    void getMessageFacesContextString()
    {
        facesContext.getViewRoot().setLocale(Locale.ENGLISH);

        FacesMessage msg = MessageUtils.getMessage(facesContext,
                "jakarta.faces.component.UIInput.CONVERSION");
        assertEquals("{0}: Conversion error occurred.", msg.getSummary());

        facesContext.getViewRoot().setLocale(Locale.GERMAN);

        msg = MessageUtils.getMessage(facesContext,
                "jakarta.faces.component.UIInput.CONVERSION");
        assertEquals("{0}: Ein Konvertierungsfehler ist aufgetreten.", msg.getSummary());
    }

    /**
     * Test method for 'org.apache.myfaces.shared.util.MessageUtils.getMessage(FacesContext, String, Object[])'
     */
    @Test
    void getMessageFacesContextStringObjectArray()
    {
        facesContext.getViewRoot().setLocale(Locale.ENGLISH);

        FacesMessage msg = MessageUtils.getMessage(facesContext,
                "jakarta.faces.component.UIInput.CONVERSION", null);
        assertEquals("{0}: Conversion error occurred.", msg.getSummary());

        facesContext.getViewRoot().setLocale(Locale.GERMAN);

        msg = MessageUtils.getMessage(facesContext,
                "jakarta.faces.component.UIInput.CONVERSION", null);
        assertEquals("{0}: Ein Konvertierungsfehler ist aufgetreten.", msg.getSummary());
    }

    /**
     * testGetMessageWithBundle
     */
    @Test
    void getMessageWithBundle()
    {
        facesContext.getViewRoot().setLocale(Locale.ENGLISH);

        ResourceBundle bundle = ResourceBundle.getBundle(DEFAULT_BUNDLE,
                Locale.ENGLISH);
        FacesMessage msg = MessageUtils.getMessage(bundle,
                "jakarta.faces.component.UIInput.CONVERSION", null);

        assertEquals("{0}: Conversion error occurred.", msg.getSummary());
    }

    /**
     * testGetMessageWithBundleName
     */
    @Test
    void getMessageWithBundleName()
    {
        facesContext.getViewRoot().setLocale(Locale.ENGLISH);

        FacesMessage msg = MessageUtils.getMessage(DEFAULT_BUNDLE,
                "jakarta.faces.component.UIInput.CONVERSION", null);

        assertEquals("{0}: Conversion error occurred.", msg.getSummary());
    }

    /**
     * testGetMessageWithBundleNameLocale
     */
    @Test
    void getMessageWithBundleNameLocale()
    {
        FacesMessage msg = MessageUtils.getMessage(DEFAULT_BUNDLE,
                Locale.GERMAN, "jakarta.faces.component.UIInput.CONVERSION", null);

        assertEquals("{0}: Ein Konvertierungsfehler ist aufgetreten.", msg.getSummary());
    }

    /**
     * testSubstituteParamsWithDELocale(
     */
    @Test
    void substituteParamsWithDELocale() {
        String paramString = MessageUtils.substituteParams(Locale.GERMANY, "currency {0,number,currency}", new Object[]{100});

        String expected = "currency " + NumberFormat.getCurrencyInstance(Locale.GERMANY).format(100);
        assertEquals(expected, paramString);
    }

    /**
     * testSubstituteParamsWithGBLocale(
     */
    @Test
    void substituteParamsWithGBLocale() {
        String paramString = MessageUtils.substituteParams(Locale.UK, "currency {0,number,currency}", new Object[]{100});

        //System.out.println(paramString);
        assertEquals("currency \u00a3100.00",paramString);
    }

}
