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
package javax.faces.component;

import javax.faces.component.html.HtmlInputText;

import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;

public class UIComponentAttributesTest extends AbstractJsfTestCase
{
    private HtmlInputText input;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        input = new HtmlInputText();
        input.setId("testId");
    }

    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
        input = null;
    }

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
            Assert.fail("Should have thrown NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // expected
        }
    }*/
}
