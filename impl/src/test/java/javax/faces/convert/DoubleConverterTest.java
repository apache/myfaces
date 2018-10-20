/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package javax.faces.convert;

import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.junit.Test;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import java.util.Locale;

/**
 * Test the {@link DoubleConverter}.
 */
public class DoubleConverterTest extends AbstractJsfTestCase {

    private DoubleConverter mock;

    public DoubleConverterTest(String name)
    {
        super(name);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        mock = new DoubleConverter();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        mock = null;
    }

    /**
     * the focus here is on the comma separator ',' in germany.
     */
    @Test
    public void testDoubleParsingGermany()
    {
        FacesContext.getCurrentInstance().getViewRoot().setLocale(Locale.GERMANY);
        UIInput input = new UIInput();

        {
            Double d = (Double) mock.getAsObject(FacesContext.getCurrentInstance(), input, "47,3443");
            assertNotNull(d);
            assertEquals(47.3443d, d.doubleValue());
        }

        {
            Double d = (Double) mock.getAsObject(FacesContext.getCurrentInstance(), input, "0,3443e3");
            assertNotNull(d);
            assertEquals(344.3d, d.doubleValue());
        }

        {
            // values with a dot as decimal seperator should still work...
            Double d = (Double) mock.getAsObject(FacesContext.getCurrentInstance(), input, "0.3443e3");
            assertNotNull(d);
            assertEquals(344.3d, d.doubleValue());
        }

    }

    /**
     * the focus here is on the comma separator '.' in the US.
     */
    @Test
    public void testDoubleParsingUS()
    {
        FacesContext.getCurrentInstance().getViewRoot().setLocale(Locale.US);
        UIInput input = new UIInput();

        {
            Double d = (Double) mock.getAsObject(FacesContext.getCurrentInstance(), input, "47.3443");
            assertNotNull(d);
            assertEquals(47.3443d, d.doubleValue());
        }

        {
            // german decimal separators are not expected to work here ;)
            try
            {
                Double d = (Double) mock.getAsObject(FacesContext.getCurrentInstance(), input, "47,3443");
                fail();
            }
            catch (ConverterException cev)
            {
                // all is well, that was expected ;)
            }

        }
    }


    /**
     * This tests a workaround which got introduced for the jvm bug
     * described in MYFACES-3024. This is necessary as long as the jvm
     * contains this bug resulting in the whole thread basically stalling
     * at 100% CPU conumption and never return from the
     * @link http://www.exploringbinary.com/java-hangs-when-converting-2-2250738585072012e-308/
     *
     *
     */
    @Test
    public void testDoubleParsingJvmBugWorkaround()
    {
        String[] baaadValues = new String[] {
                "0.00022250738585072012e-304",
                "2.225073858507201200000e-308",
                "2.225073858507201200000e-308",
                "2.2250738585072012e-00308",
                "2.2250738585072012997800001e-308"
        };

        FacesContext.getCurrentInstance().getViewRoot().setLocale(Locale.US);
        UIInput input = new UIInput();
        Double d;

        for (String badVal : baaadValues)
        {
            try
            {
                d = (Double) mock.getAsObject(FacesContext.getCurrentInstance(), input, badVal);
                fail();
            }
            catch(ConverterException cex)
            {
                // all is well, we expect the Converter to detect the baaad values...
            }
        }
    }



}
