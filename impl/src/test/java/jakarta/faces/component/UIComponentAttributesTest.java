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
package jakarta.faces.component;

import jakarta.faces.component.html.HtmlInputText;

import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UIComponentAttributesTest extends AbstractFacesTestCase
{
    private HtmlInputText input;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        input = new HtmlInputText();
        input.setId("testId");
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        super.tearDown();
        input = null;
    }

    @Test
    public void testSetNullAttributeOnValidProperty()
    {
        input.getAttributes().put("style", null);
    }
/*
    public void testSetNullAttributeOnInvalidProperty()
    {
        try
        {
            input.getAttributes().put("someBogus", null);
            Assertions.fail("Should have thrown NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // expected
        }
    }*/
}
