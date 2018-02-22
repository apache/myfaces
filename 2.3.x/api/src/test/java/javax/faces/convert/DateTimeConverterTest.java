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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import org.apache.myfaces.test.base.AbstractJsfTestCase;

public class DateTimeConverterTest extends AbstractJsfTestCase
{
    private DateTimeConverter mock;

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(DateTimeConverterTest.class);
    }

    public DateTimeConverterTest(String name)
    {
        super(name);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        mock = new DateTimeConverter();
        mock.setTimeZone(TimeZone.getDefault());
        FacesContext.getCurrentInstance().getViewRoot().setLocale(Locale.GERMANY);

    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        mock = null;
    }

    /*
     * Test method for 'javax.faces.component.UIComponentBase.getAsObject()'
     */
    public void testGetAsObject()
    {

        UIInput input = new UIInput();

        mock.setPattern("MM/dd/yyyy");

        // should trow a ConverterException
        try
        {
            mock.getAsObject(FacesContext.getCurrentInstance(), input, "15/15/15");

            assertTrue("this date should not be parsable - and it is, so this is wrong.", false);
        }
        catch (ConverterException e)
        {

        }

        // should not trow a ConverterException
        try
        {
            Date date = (Date) mock.getAsObject(FacesContext.getCurrentInstance(), input, "12/01/01");

            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");
            format.setTimeZone(TimeZone.getDefault());

            String str = format.format(date);

            assertEquals("12/01/01", str);

            format = new SimpleDateFormat("MM/dd/yyyy");
            format.setTimeZone(TimeZone.getDefault());

            str = format.format(date);

            assertEquals("12/01/0001", str);
        }
        catch (ConverterException e)
        {
            assertTrue("this date should not be parsable - and it is, so this is wrong.", false);
        }
    }
}
