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

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import org.apache.myfaces.test.base.AbstractJsfTestCase;

/**
 * This testcase test <code>javax.faces.convert.EnumConverter</code>.
 */
public class EnumConverterTest extends AbstractJsfTestCase
{
    private enum testEnum
    {
        ITEM1, ITEM2;
        
        @Override
        public String toString()
        {
            // overriding toString() to check if converter uses
            // name() instead of toString() to create the String value.
            return "enum value";
        }
        
    };

    private EnumConverter converter;

    public EnumConverterTest(String name)
    {
        super(name);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        converter = new EnumConverter(testEnum.class);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        converter = null;
    }

    /**
     * Test method for
     * {@link javax.faces.convert.EnumConverter#getAsObject(FacesContext, javax.faces.component.UIComponent, String)}.
     */
    public void testGetAsObject()
    {
        UIInput input = new UIInput();
        Object convertedObj = converter.getAsObject(FacesContext.getCurrentInstance(), input, "ITEM2");
        assertEquals(convertedObj, testEnum.ITEM2);
    }

    /**
     * Test method for
     * {@link javax.faces.convert.EnumConverter#getAsObject(FacesContext, javax.faces.component.UIComponent, String)}.
     */
    public void testGetAsObjectNull()
    {
        UIInput input = new UIInput();
        Object convertedObj = converter.getAsObject(FacesContext.getCurrentInstance(), input, null);
        assertNull(convertedObj);
    }

    /**
     * Test method for
     * {@link javax.faces.convert.EnumConverter#getAsObject(FacesContext, javax.faces.component.UIComponent, String)}.
     */
    public void testGetAsObjectNoEnum()
    {
        UIInput input = new UIInput();
        try
        {
            converter.getAsObject(FacesContext.getCurrentInstance(), input, "NO_ENUM_CONST");
            fail("Converter exception should be thrown");
        }
        catch (ConverterException e)
        {
            // should be thrown
        }
    }

    /**
     * Test method for
     * {@link javax.faces.convert.EnumConverter#getAsObject(FacesContext, javax.faces.component.UIComponent, String)}.
     */
    public void testGetAsObjectNoClassSet()
    {
        Converter testConverter = new EnumConverter();
        UIInput input = new UIInput();
        try
        {
            testConverter.getAsObject(FacesContext.getCurrentInstance(), input, "ITEM2");
            fail("Converter exception should be thrown");
        }
        catch (ConverterException e)
        {
            // should be thrown
        }
    }

    /**
     * Test method for
     * {@link javax.faces.convert.EnumConverter#getAsString(FacesContext, javax.faces.component.UIComponent, Object)}.
     */
    public void testGetAsString()
    {
        UIInput input = new UIInput();
        String convertedStr = converter.getAsString(FacesContext.getCurrentInstance(), input, testEnum.ITEM1);
        assertEquals(convertedStr, testEnum.ITEM1.name());
    }

    /**
     * Test method for
     * {@link javax.faces.convert.EnumConverter#getAsString(FacesContext, javax.faces.component.UIComponent, Object)}.
     */
    public void testGetAsStringNull()
    {
        UIInput input = new UIInput();
        String convertedStr = converter.getAsString(FacesContext.getCurrentInstance(), input, null);
        assertEquals(convertedStr, "");
    }

    /**
     * Test method for
     * {@link javax.faces.convert.EnumConverter#getAsString(FacesContext, javax.faces.component.UIComponent, Object)}.
     */
    public void testGetAsStringNoEnum()
    {
        UIInput input = new UIInput();
        try
        {
            String convertedStr = converter.getAsString(FacesContext.getCurrentInstance(), input, "HALLO");
            fail("Converter exception should be thrown");
        }
        catch (ConverterException e)
        {
            // should be thrown
        }
    }

    /**
     * Test method for
     * {@link javax.faces.convert.EnumConverter#getAsString(FacesContext, javax.faces.component.UIComponent, Object)}.
     */
    public void testGetAsStringNoClassSet()
    {
        Converter testConverter = new EnumConverter();
        UIInput input = new UIInput();
        try
        {
            testConverter.getAsString(FacesContext.getCurrentInstance(), input, testEnum.ITEM1);
            fail("Converter exception should be thrown");
        }
        catch (ConverterException e)
        {
            // should be thrown
        }
    }
}
