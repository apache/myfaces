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

package javax.faces.convert;

import java.math.BigInteger;
import java.util.Locale;
import javax.el.ValueExpression;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.junit.Test;

public class NumberConverterTest extends AbstractJsfTestCase
{
    private NumberConverter mock;

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(NumberConverterTest.class);
    }

    public NumberConverterTest(String name)
    {
        super(name);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        mock = new NumberConverter();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        mock = null;
    }

    /*
     * temporarily comment out tests that fail, until Matthias Wessendorf has time to investigate
     */
    @Test
    public void testFranceLocaleWithNonBreakingSpace()
    {
        mock.setLocale(Locale.FRANCE);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(Locale.GERMANY);
        UIInput input = new UIInput();
        mock.setType("currency");
        String stringValue = mock.getAsString(facesContext, input, new Double(12345.68d));
        Number number = (Number) mock.getAsObject(FacesContext.getCurrentInstance(), input, "12\u00a0345,68 \u20AC");
        assertNotNull(number);
    }
    
    @Test
    public void testFranceLocaleWithoutNonBreakingSpace()
    {
        mock.setLocale(Locale.FRANCE);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(Locale.GERMANY);
        UIInput input = new UIInput();
        mock.setType("currency");
        Number number = (Number) mock.getAsObject(FacesContext.getCurrentInstance(), input, "12 345,68 \u20AC");
        assertNotNull(number);
    }
    
    /**
     * EUR12,345.68 
     */
    @Test
    public void testUSLocaleUsingEURCurrencyCode()
    {
        facesContext.getViewRoot().setLocale(Locale.US);
        mock.setLocale(Locale.US);
        UIInput input = new UIInput();
        mock.setType("currency");
        mock.setCurrencyCode("EUR");
        Number testValue = 12345.68d;
        String stringValue = mock.getAsString(facesContext, input, testValue);
        Number number = (Number) mock.getAsObject(facesContext, input, stringValue);
        assertNotNull(number);
        assertEquals(testValue, number);
    }

    /**
     * 12,345.68
     */
    @Test
    public void testUKLocaleUsingEURCurrencyCode()
    {
        facesContext.getViewRoot().setLocale(Locale.US);
        mock.setLocale(Locale.UK);
        UIInput input = new UIInput();
        mock.setType("currency");
        mock.setCurrencyCode("EUR");
        Number testValue = 12345.68d;
        String stringValue = mock.getAsString(facesContext, input, testValue);
        Number number = (Number) mock.getAsObject(facesContext, input, stringValue);
        assertNotNull(number);
        assertEquals(testValue, number);
    }
    
    /**
     * 12.345,68 
     */
    @Test
    public void testGermanyLocaleUsingEURCurrencyCode()
    {
        facesContext.getViewRoot().setLocale(Locale.US);
        mock.setLocale(Locale.GERMANY);
        UIInput input = new UIInput();
        mock.setType("currency");
        mock.setCurrencyCode("EUR");
        Number testValue = 12345.68d;
        String stringValue = mock.getAsString(facesContext, input, testValue);
        Number number = (Number) mock.getAsObject(facesContext, input, stringValue);
        assertNotNull(number);
        assertEquals(testValue, number);
    }
    
    @Test
    public void testCurrencyPattern()
    {
        facesContext.getViewRoot().setLocale(Locale.US);
        mock.setLocale(Locale.US);
        UIInput input = new UIInput();
        mock.setPattern("\u00A4 ###,###.###");
        Number testValue = 12345.68d;
        String stringValue = mock.getAsString(facesContext, input, testValue);
        Number number = (Number) mock.getAsObject(facesContext, input, stringValue);
        assertNotNull(number);
        assertEquals(testValue, number);        
    }

    @Test
    public void testCurrencyPattern2()
    {
        facesContext.getViewRoot().setLocale(Locale.US);
        mock.setLocale(Locale.GERMANY);
        UIInput input = new UIInput();
        mock.setPattern("\u00A4 ###,###.###");
        mock.setCurrencyCode("USD"); //Since currency is EUR, but we are using USD currency code, the output is USD 12.345,68
        Number testValue = 12345.68d;
        String stringValue = mock.getAsString(facesContext, input, testValue);
        Number number = (Number) mock.getAsObject(facesContext, input, stringValue);
        assertNotNull(number);
        assertEquals(testValue, number);        
    }
    
    @Test
    public void testCzechLocaleWithNonBreakingSpace()
    {
        mock.setLocale(new Locale("cs"));
        mock.setIntegerOnly(true);
        mock.setGroupingUsed(true);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale("cs"));
        UIInput input = new UIInput();
        String stringValue = mock.getAsString(facesContext, input, new Long(7000));
        assertEquals("must return 7&NBSP000", "7\u00a0000", stringValue);
        
        Number number = (Number) mock.getAsObject(FacesContext.getCurrentInstance(), input, stringValue);
        assertNotNull(number);
        assertEquals(new Long(7000), number);
    }

    @Test
    public void testGetAsObjectWithBigInteger()
    {
        facesContext.getViewRoot().setLocale(Locale.US);
        mock.setLocale(Locale.GERMANY);
        mock.setIntegerOnly(true);
        mock.setGroupingUsed(false);
        UIInput input = new UIInput();
        facesContext.getELContext().getELResolver().setValue(facesContext.getELContext(), null,
            "bigInteger", BigInteger.ONE);
        ValueExpression valueExpression =
        application
            .getExpressionFactory()
            .createValueExpression(facesContext.getELContext(), "#{bigInteger}", BigInteger.class);
        input.setValueExpression("value", valueExpression);
        Number number = (Number) mock.getAsObject(FacesContext.getCurrentInstance(), input, "1");
        assertNotNull(number);
        assertTrue(number instanceof BigInteger);
        assertEquals(BigInteger.ONE, number);
    }
    
    @Test(expected = ConverterException.class)
    public void testGetAsObjectWithBigIntegerAndParsePosition()
    {
        facesContext.getViewRoot().setLocale(Locale.US);
        mock.setLocale(Locale.GERMANY);
        mock.setIntegerOnly(true);
        mock.setGroupingUsed(false);
        UIInput input = new UIInput();
        facesContext.getELContext().getELResolver().setValue(facesContext.getELContext(), null,
            "bigInteger", BigInteger.ONE);
        ValueExpression valueExpression =
        application
            .getExpressionFactory()
            .createValueExpression(facesContext.getELContext(), "#{bigInteger}", BigInteger.class);
        input.setValueExpression("value", valueExpression);
        Number number = (Number) mock.getAsObject(FacesContext.getCurrentInstance(), input, "1,0.0,00.00");
        assertNotNull(number);
        assertTrue(number instanceof BigInteger);
        assertEquals(BigInteger.ONE, number);
    }
}
