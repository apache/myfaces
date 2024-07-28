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
package org.apache.myfaces.view.facelets.tag.faces.core.reset;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import org.junit.jupiter.api.Assertions;
import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.junit.jupiter.api.Test;

/**
 *
 * @author lu4242
 */
public class ResetValuesTestCase extends AbstractMyFacesCDIRequestTestCase
{
    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES","org.apache.myfaces.view.facelets.tag.faces.core.reset");
    }

    @Test
    public void testResetValuesActionListenerHandler2() throws Exception
    {
        startViewRequest("/resetValuesActionListener_2.xhtml");

        processLifecycleExecute();
        
        ResetValuesBean bean = facesContext.getApplication().evaluateExpressionGet(facesContext, 
            "#{bean}", ResetValuesBean.class);
        
        bean.setField1("Hello");
        bean.setField2(1);
        
        executeBuildViewCycle();
                
        UIComponent submitButton = facesContext.getViewRoot().findComponent("mainForm:submit");
        
        UIInput field1 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field1");
        UIInput field2 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field2");
        
        executeViewHandlerRender();
        executeAfterRender();
        
        // The lenght validator for field1 force a minimum of
        // 4 digits, but the entered text has only 3 charactes.
        // In the ajax request, that force a validation error, so
        // the submitted values are not updated into the model
        // but if the submitted value is set, it will render "xxx"
        // and "2". Reset values clear the submitted values and
        // let what's in the model.
        client.inputText(field1, "xxx");
        client.inputText(field2, "2");
        
        client.ajax(submitButton, "action", 
            submitButton.getClientId(facesContext) +" "+
            field1.getClientId(facesContext) + " "+ 
            field2.getClientId(facesContext), 
            field1.getClientId(facesContext) + " "+ 
            field2.getClientId(facesContext), true, true);
        
        processLifecycleExecute();
        renderResponse();

        UIComponent submitButton_2 = facesContext.getViewRoot().findComponent("mainForm:submit");
        UIInput field1_2 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field1");
        UIInput field2_2 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field2");
        
        Assertions.assertEquals("Hello", field1_2.getValue());
        Assertions.assertEquals(1, field2_2.getValue());
        Assertions.assertNull(field1_2.getSubmittedValue());
        Assertions.assertNull(field2_2.getSubmittedValue());
        
        Assertions.assertEquals("Hello", bean.getField1());
        Assertions.assertEquals(Integer.valueOf(1), bean.getField2());

        //Now let's try the normal way with no resetValues
        client.inputText(field1_2, "xxx");
        client.inputText(field2_2, "2");
        
        client.ajax(submitButton_2, "action", 
            submitButton_2.getClientId(facesContext) +" "+
            field1_2.getClientId(facesContext) + " "+ 
            field2_2.getClientId(facesContext), 
            field1_2.getClientId(facesContext) + " "+ 
            field2_2.getClientId(facesContext), true, false);
        
        processLifecycleExecute();
        renderResponse();

        UIComponent submitButton_3 = facesContext.getViewRoot().findComponent("mainForm:submit");
        UIInput field1_3 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field1");
        UIInput field2_3 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field2");
        
        // The values in the model are kept but the submitted values are there
        // and the renderer takes them.
        Assertions.assertEquals("Hello", field1_3.getValue());
        // the second field doesn't have validation error!, but the local value
        // is set with 2, but the model still is 1 because update model phase
        // was not executed.
        Assertions.assertEquals(2, field2_3.getValue());
        Assertions.assertTrue(field2_3.isLocalValueSet());
        Assertions.assertEquals("xxx", field1_3.getSubmittedValue());
        Assertions.assertNull(field2_3.getSubmittedValue());
        
        Assertions.assertEquals("Hello", bean.getField1());
        Assertions.assertEquals(Integer.valueOf(1), bean.getField2());
        
        // Now let's try a valid one, in this case the model is updated
        client.inputText(field1_3, "xxxx");
        client.inputText(field2_3, "3");
        
        client.ajax(submitButton_3, "action", 
            submitButton_3.getClientId(facesContext) +" "+
            field1_3.getClientId(facesContext) + " "+ 
            field2_3.getClientId(facesContext), 
            field1_3.getClientId(facesContext) + " "+ 
            field2_3.getClientId(facesContext), true, true);
        
        processLifecycleExecute();
        renderResponse();

        //UIComponent submitButton_4 = facesContext.getViewRoot().findComponent("mainForm:submit");
        UIInput field1_4 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field1");
        UIInput field2_4 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field2");
        
        Assertions.assertEquals("xxxx", field1_4.getValue());
        Assertions.assertEquals(3, field2_4.getValue());
        Assertions.assertNull(field1_4.getSubmittedValue());
        Assertions.assertNull(field2_4.getSubmittedValue());
        
        Assertions.assertEquals("xxxx", bean.getField1());
        Assertions.assertEquals(Integer.valueOf(3), bean.getField2());
    }

    @Test
    public void testResetValuesActionListenerHandler3() throws Exception
    {
        startViewRequest("/resetValuesActionListener_3.xhtml");

        processLifecycleExecute();
        
        ResetValuesBean bean = facesContext.getApplication().evaluateExpressionGet(facesContext, 
            "#{bean}", ResetValuesBean.class);
        
        bean.setField1("Hello");
        bean.setField2(1);
        
        executeBuildViewCycle();
                
        UIComponent submitButton = facesContext.getViewRoot().findComponent("mainForm:submit:button");
        
        UIInput field1 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field1");
        UIInput field2 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field2");
        
        executeViewHandlerRender();
        executeAfterRender();
        
        client.inputText(field1, "xxx");
        client.inputText(field2, "2");
        
        client.submit(submitButton);
        
        processLifecycleExecute();
        renderResponse();

        submitButton = facesContext.getViewRoot().findComponent("mainForm:submit:button");
        field1 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field1");
        field2 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field2");

        Assertions.assertEquals("Hello", field1.getValue());
        Assertions.assertEquals(1, field2.getValue());

    }

    @Test
    public void testResetValuesActionListenerHandler4() throws Exception
    {
        startViewRequest("/resetValuesActionListener_4.xhtml");

        processLifecycleExecute();

        ResetValuesBean bean = facesContext.getApplication().evaluateExpressionGet(facesContext,
                "#{bean}", ResetValuesBean.class);

        bean.setField1("Hello");
        bean.setField2(1);

        executeBuildViewCycle();

        UIComponent submitButton = facesContext.getViewRoot().findComponent("mainForm:submit");

        UIInput field1 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field1");
        UIInput field2 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field2");

        executeViewHandlerRender();
        executeAfterRender();

        // The lenght validator for field1 force a minimum of
        // 4 digits, but the entered text has only 3 charactes.
        // In the ajax request, that force a validation error, so
        // the submitted values are not updated into the model
        // but if the submitted value is set, it will render "xxx"
        // and "2". Reset values clear the submitted values and
        // let what's in the model.
        client.inputText(field1, "xxx");
        client.inputText(field2, "2");

        client.ajax(submitButton, "action",
                submitButton.getClientId(facesContext) +" "+
                        field1.getClientId(facesContext) + " "+
                        field2.getClientId(facesContext),
                "mainForm", true, true);

        processLifecycleExecute();
        renderResponse();

        UIComponent submitButton_2 = facesContext.getViewRoot().findComponent("mainForm:submit");
        UIInput field1_2 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field1");
        UIInput field2_2 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field2");

        Assertions.assertEquals("Hello", field1_2.getValue());
        Assertions.assertEquals(1, field2_2.getValue());
        Assertions.assertNull(field1_2.getSubmittedValue());
        Assertions.assertNull(field2_2.getSubmittedValue());

        Assertions.assertEquals("Hello", bean.getField1());
        Assertions.assertEquals(Integer.valueOf(1), bean.getField2());

        //Now let's try the normal way with no resetValues
        client.inputText(field1_2, "xxx");
        client.inputText(field2_2, "2");

        client.ajax(submitButton_2, "action",
                submitButton_2.getClientId(facesContext) +" "+
                        field1_2.getClientId(facesContext) + " "+
                        field2_2.getClientId(facesContext),
                "mainForm", true, false);

        processLifecycleExecute();
        renderResponse();

        UIComponent submitButton_3 = facesContext.getViewRoot().findComponent("mainForm:submit");
        UIInput field1_3 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field1");
        UIInput field2_3 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field2");

        // The values in the model are kept but the submitted values are there
        // and the renderer takes them.
        Assertions.assertEquals("Hello", field1_3.getValue());
        // the second field doesn't have validation error!, but the local value
        // is set with 2, but the model still is 1 because update model phase
        // was not executed.
        Assertions.assertEquals(2, field2_3.getValue());
        Assertions.assertTrue(field2_3.isLocalValueSet());
        Assertions.assertEquals("xxx", field1_3.getSubmittedValue());
        Assertions.assertNull(field2_3.getSubmittedValue());

        Assertions.assertEquals("Hello", bean.getField1());
        Assertions.assertEquals(Integer.valueOf(1), bean.getField2());

        // Now let's try a valid one, in this case the model is updated
        client.inputText(field1_3, "xxxx");
        client.inputText(field2_3, "3");

        client.ajax(submitButton_3, "action",
                submitButton_3.getClientId(facesContext) +" "+
                        field1_3.getClientId(facesContext) + " "+
                        field2_3.getClientId(facesContext),
                "mainForm", true, true);

        processLifecycleExecute();
        renderResponse();

        //UIComponent submitButton_4 = facesContext.getViewRoot().findComponent("mainForm:submit");
        UIInput field1_4 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field1");
        UIInput field2_4 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field2");

        Assertions.assertEquals("xxxx", field1_4.getValue());
        Assertions.assertEquals(3, field2_4.getValue());
        Assertions.assertNull(field1_4.getSubmittedValue());
        Assertions.assertNull(field2_4.getSubmittedValue());

        Assertions.assertEquals("xxxx", bean.getField1());
        Assertions.assertEquals(Integer.valueOf(3), bean.getField2());
    }

    @Test
    public void testResetValuesActionListenerHandler5() throws Exception
    {
        startViewRequest("/resetValuesActionListener_5.xhtml");

        processLifecycleExecute();

        ResetValuesBean bean = facesContext.getApplication().evaluateExpressionGet(facesContext,
                "#{bean}", ResetValuesBean.class);

        bean.setField1("Hello");
        bean.setField2(1);

        executeBuildViewCycle();

        UIComponent submitButton = facesContext.getViewRoot().findComponent("mainForm:submit");

        UIInput field1 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field1");
        UIInput field2 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field2");

        executeViewHandlerRender();
        executeAfterRender();

        // The lenght validator for field1 force a minimum of
        // 4 digits, but the entered text has only 3 charactes.
        // In the ajax request, that force a validation error, so
        // the submitted values are not updated into the model
        // but if the submitted value is set, it will render "xxx"
        // and "2". Reset values clear the submitted values and
        // let what's in the model.
        client.inputText(field1, "xxx");
        client.inputText(field2, "2");

        client.ajax(submitButton, "action",
                submitButton.getClientId(facesContext) +" "+
                        field1.getClientId(facesContext) + " "+
                        field2.getClientId(facesContext),
                "@all", true, true);

        processLifecycleExecute();
        renderResponse();

        UIComponent submitButton_2 = facesContext.getViewRoot().findComponent("mainForm:submit");
        UIInput field1_2 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field1");
        UIInput field2_2 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field2");

        Assertions.assertEquals("Hello", field1_2.getValue());
        Assertions.assertEquals(1, field2_2.getValue());
        Assertions.assertNull(field1_2.getSubmittedValue());
        Assertions.assertNull(field2_2.getSubmittedValue());

        Assertions.assertEquals("Hello", bean.getField1());
        Assertions.assertEquals(Integer.valueOf(1), bean.getField2());

        //Now let's try the normal way with no resetValues
        client.inputText(field1_2, "xxx");
        client.inputText(field2_2, "2");

        client.ajax(submitButton_2, "action",
                submitButton_2.getClientId(facesContext) +" "+
                        field1_2.getClientId(facesContext) + " "+
                        field2_2.getClientId(facesContext),
                "@all", true, false);

        processLifecycleExecute();
        renderResponse();

        UIComponent submitButton_3 = facesContext.getViewRoot().findComponent("mainForm:submit");
        UIInput field1_3 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field1");
        UIInput field2_3 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field2");

        // The values in the model are kept but the submitted values are there
        // and the renderer takes them.
        Assertions.assertEquals("Hello", field1_3.getValue());
        // the second field doesn't have validation error!, but the local value
        // is set with 2, but the model still is 1 because update model phase
        // was not executed.
        Assertions.assertEquals(2, field2_3.getValue());
        Assertions.assertTrue(field2_3.isLocalValueSet());
        Assertions.assertEquals("xxx", field1_3.getSubmittedValue());
        Assertions.assertNull(field2_3.getSubmittedValue());

        Assertions.assertEquals("Hello", bean.getField1());
        Assertions.assertEquals(Integer.valueOf(1), bean.getField2());

        // Now let's try a valid one, in this case the model is updated
        client.inputText(field1_3, "xxxx");
        client.inputText(field2_3, "3");

        client.ajax(submitButton_3, "action",
                submitButton_3.getClientId(facesContext) +" "+
                        field1_3.getClientId(facesContext) + " "+
                        field2_3.getClientId(facesContext),
                "@all", true, true);

        processLifecycleExecute();
        renderResponse();

        //UIComponent submitButton_4 = facesContext.getViewRoot().findComponent("mainForm:submit");
        UIInput field1_4 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field1");
        UIInput field2_4 = (UIInput) facesContext.getViewRoot().findComponent("mainForm:field2");

        Assertions.assertEquals("xxxx", field1_4.getValue());
        Assertions.assertEquals(3, field2_4.getValue());
        Assertions.assertNull(field1_4.getSubmittedValue());
        Assertions.assertNull(field2_4.getSubmittedValue());

        Assertions.assertEquals("xxxx", bean.getField1());
        Assertions.assertEquals(Integer.valueOf(3), bean.getField2());
    }
}
