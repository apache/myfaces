/*
 * Copyright 2004-2006 The Apache Software Foundation.
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

package javax.faces.convert;

import org.apache.myfaces.AbstractTestCase;
import org.apache.myfaces.mock.api.MockFacesContext;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

public class DateTimeConverterTest extends AbstractTestCase
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

    protected void setUp() throws Exception
    {
        super.setUp();

        mock = new DateTimeConverter();

        new MockFacesContext();
    }

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

        mock.setPattern("dd/MM/yyyy");

        // defaults to true
        try
        {
            mock.getAsObject(FacesContext.getCurrentInstance(),input,"15/15/15");

            assertTrue("this date should not be parsable - and it is, so this is wrong.",false);
        }
        catch (ConverterException e)
        {

        }
    }
}
