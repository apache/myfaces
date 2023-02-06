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
package jakarta.faces.convert;

import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.DoubleConverter;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.jupiter.api.Test;

import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test the {@link DoubleConverter}.
 */
public class DoubleConverterTest extends AbstractJsfTestCase {

    private DoubleConverter mock;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        mock = new DoubleConverter();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception
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
            Assertions.assertNotNull(d);
            Assertions.assertEquals(47.3443d, d.doubleValue(), 0);
        }

        {
            Double d = (Double) mock.getAsObject(FacesContext.getCurrentInstance(), input, "0,3443e3");
            Assertions.assertNotNull(d);
            Assertions.assertEquals(344.3d, d.doubleValue(), 0);
        }

        {
            // values with a dot as decimal seperator should still work...
            Double d = (Double) mock.getAsObject(FacesContext.getCurrentInstance(), input, "0.3443e3");
            Assertions.assertNotNull(d);
            Assertions.assertEquals(344.3d, d.doubleValue(), 0);
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
            Assertions.assertNotNull(d);
            Assertions.assertEquals(47.3443d, d.doubleValue(), 0);
        }

        {
            // german decimal separators are not expected to work here ;)
            try
            {
                Double d = (Double) mock.getAsObject(FacesContext.getCurrentInstance(), input, "47,3443");
                Assertions.fail();
            }
            catch (ConverterException cev)
            {
                // all is well, that was expected ;)
            }

        }
    }

}
