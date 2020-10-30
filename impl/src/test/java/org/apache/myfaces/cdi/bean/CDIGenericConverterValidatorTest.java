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

package org.apache.myfaces.cdi.bean;

import jakarta.el.ExpressionFactory;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIOutput;
import jakarta.faces.convert.Converter;
import jakarta.faces.validator.ValidatorException;

import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests to ensure that Generics work with CDI Converters and Validators
 */
public class CDIGenericConverterValidatorTest extends AbstractMyFacesCDIRequestTestCase {

    @Test
    public void testConverter() throws Exception {

        String expectedValue = "zero";
        String result = "";

        startViewRequest("/CDIGenericConverterTest.xhtml");
        application.addConverter("customConverter", "org.apache.myfaces.cdi.bean.CustomConverter");
        processLifecycleExecuteAndRender();

        UIOutput out = (UIOutput) facesContext.getViewRoot().findComponent("form1:out");
        TestBean bean = (TestBean) out.getValue();
        Converter converter = out.getConverter();

        result = converter.getAsString(facesContext, out, bean);

        Assert.assertTrue("The value output should have matched: " + expectedValue + " but was : " + result,
                result.equals(expectedValue));

    }

    @Test
    public void testValidator() throws Exception {

        startViewRequest("/CDIGenericValidatorTest.xhtml");
        application.addValidator("customValidator", "org.apache.myfaces.cdi.bean.CustomValidator");
        processLifecycleExecuteAndRender();

        UIInput out = (UIInput) facesContext.getViewRoot().findComponent("form1:out");
        String r = (String) out.getValue();

        //Expects a ValidatorException 
        try {
            out.getValidators()[0].validate(facesContext, out, r);
            Assert.fail("ValidatorException was not thrown. Custom Generic validator failed.");
        } catch (ValidatorException e) {
            //Ignored
        }

    }

    @Override
    protected ExpressionFactory createExpressionFactory() {
        // For this test we need the a real one so EL method invocation works.
        return new org.apache.el.ExpressionFactoryImpl();
    }

}
