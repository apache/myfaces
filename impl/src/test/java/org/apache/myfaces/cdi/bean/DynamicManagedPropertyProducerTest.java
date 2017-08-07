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

import javax.el.ExpressionFactory;
import javax.faces.component.UIOutput;
import org.apache.myfaces.mc.test.core.AbstractMyFacesCDIRequestTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *  A unit test to test the CDI @ManagedProperty
 */
public class DynamicManagedPropertyProducerTest extends AbstractMyFacesCDIRequestTestCase
{

    @Test
    public void testCDIManagedProperty() throws Exception {
        String expectedValue = "numberManagedProperty=0textManagedProperty=zerolistManagedProperty" +
                                   "=zerostringArrayManagedProperty=zerobean=org.apache.myfaces.cdi.bean.TestBean";
        String result;

        startViewRequest("/CDIManagedProperty.xhtml");
        processLifecycleExecuteAndRender();

        UIOutput out = (UIOutput) facesContext.getViewRoot().findComponent("form1:out1");
        result = out.getValue().toString();

        result = result.substring(0, result.indexOf("@"));
        result = result.replaceAll("\\s","");

        Assert.assertTrue("The value output should have matched: " + expectedValue + " but was : " + result, result.equals(expectedValue));
    }

    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        // For this test we need the a real one so EL method invocation works.
        return new org.apache.el.ExpressionFactoryImpl();
    }

    
}
