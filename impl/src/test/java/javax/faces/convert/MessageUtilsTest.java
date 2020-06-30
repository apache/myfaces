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

import javax.el.ValueExpression;
import javax.faces.component.html.HtmlInputText;
import org.apache.myfaces.core.api.shared.MessageUtils;

import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.apache.myfaces.test.el.MockValueExpression;
import org.junit.Assert;

public class MessageUtilsTest extends AbstractJsfTestCase
{

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetLabelFromAttributesMap()
    {
        HtmlInputText inputText = new HtmlInputText();
        inputText.getAttributes().put("label", "testLabel");
        Object label = MessageUtils.getLabel(facesContext, inputText);
        Assert.assertEquals("testLabel", label);
    }

    public void testGetLabelFromValueExpression()
    {
        facesContext.getExternalContext().getRequestMap().put("lbl", "testLabel");
        HtmlInputText inputText = new HtmlInputText();
        ValueExpression expression = new MockValueExpression("#{requestScope.lbl}", String.class);
        inputText.setValueExpression("label", expression);

        Object label = MessageUtils.getLabel(facesContext, inputText);
        Assert.assertEquals("testLabel", label);
    }

    public void testGetLabelReturnsClientIdWhenLabelIsNotSpecified()
    {
        HtmlInputText inputText = new HtmlInputText();
        inputText.setId("testId");
        Object label = MessageUtils.getLabel(facesContext, inputText);
        Assert.assertEquals("testId", label);
    }
}