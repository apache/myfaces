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
package org.apache.myfaces.integrationtest;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.apache.myfaces.integrationtest.support.MyFacesIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Basic form input-navigation-output test cases.
 *
 * @author Jakob Korherr
 */
@RunWith(JUnit4.class)
public class FormInputTest extends MyFacesIntegrationTest
{

    @Test
    public void validFormInput_shouldApplyValuesToBeanAndExecuteAction() throws Exception
    {
        HtmlPage page = webClient.getPage(getBaseURL() + "pages/formInput/index.xhtml");
        HtmlForm form = page.getFormByName("form");

        // values
        final String name = "John Doe";
        final String dateOfBirth = "1976-07-12";
        final String siblings = "2";

        // enter values
        HtmlInput nameInput = form.getInputByName("form:name");
        nameInput.setValueAttribute(name);
        HtmlInput dateOfBirthInput = form.getInputByName("form:dateOfBirth");
        dateOfBirthInput.setValueAttribute(dateOfBirth);
        HtmlInput siblingsInput = form.getInputByName("form:siblings");
        siblingsInput.setValueAttribute(siblings);

        // click commandButton
        HtmlSubmitInput submitButton = (HtmlSubmitInput) page.getElementById("form:submit");
        page = submitButton.click();

        // check output
        HtmlElement nameElement = page.getElementById("name");
        Assert.assertEquals(name, nameElement.getTextContent());
        HtmlElement dateOfBirthElement = page.getElementById("dateOfBirth");
        Assert.assertEquals(dateOfBirth, dateOfBirthElement.getTextContent());
        HtmlElement siblingsElement = page.getElementById("siblings");
        Assert.assertEquals(siblings, siblingsElement.getTextContent());
    }

}
