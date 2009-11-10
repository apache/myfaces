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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.faces.context.ExternalContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.el.MockValueExpression;
import org.apache.shale.test.mock.MockExternalContext12;

import java.util.Map;
import java.util.HashMap;

public class UIInputTest extends AbstractJsfTestCase
{

    private Converter mockConverter;
    private Validator mockValidator;
    private UIInput input;

    public UIInputTest(String name)
    {
        super(name);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        input = new UIInput();
        input.setId("testId");
        mockConverter = createMock(Converter.class);
        mockValidator = createMock(Validator.class);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        input = null;
        mockConverter = null;
        mockValidator = null;
    }

    public void testWhenSpecifiedConverterMessageIsUsedInCaseConverterExceptionOccurs()
    {
        input.setConverterMessage("Cannot convert");

        input.setConverter(mockConverter);
        expect(mockConverter.getAsObject(facesContext, input, "xxx")).andThrow(new ConverterException());
        replay(mockConverter);

        input.getConvertedValue(facesContext, "xxx");
        verify(mockConverter);

        assertFalse(input.isValid());
        assertNotNull(facesContext.getMessages("testId"));

        FacesMessage message = (FacesMessage) facesContext.getMessages("testId").next();
        assertEquals(message.getDetail(), "Cannot convert");
        assertEquals(message.getSummary(), "Cannot convert");
    }

    public void testWhenSpecifiedValidatorMessageIsUsedInCaseValidatorExceptionOccurs()
    {
        input.setValidatorMessage("Cannot validate");

        input.addValidator(mockValidator);
        mockValidator.validate(facesContext, input, "xxx");
        expectLastCall().andThrow(new ValidatorException(new FacesMessage()));
        replay(mockValidator);

        input.validateValue(facesContext, "xxx");
        verify(mockValidator);

        assertFalse(input.isValid());
        assertNotNull(facesContext.getMessages("testId"));

        FacesMessage message = (FacesMessage) facesContext.getMessages("testId").next();
        assertEquals(message.getDetail(), "Cannot validate");
        assertEquals(message.getSummary(), "Cannot validate");
    }

    public void testUpdateModelSetsTheLocalValueToModelValue()
    {
        input.setValue("testValue");

        ValueExpression expression = new MockValueExpression("#{requestScope.id}", String.class);
        input.setValueExpression("value", expression);

        input.updateModel(facesContext);

        String updatedValue = expression.getValue(facesContext.getELContext()).toString();
        assertEquals("testValue", updatedValue);
    }

    /*
    public void testValidateWithEmptyStringWithEmptyStringAsNullEnabled()
    {
        try
        {
            InitParameterMockExternalContext mockExtCtx =
                    new InitParameterMockExternalContext(servletContext, request, response);
            mockExtCtx.getInitParameterMap().put("javax.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL", "true");
            facesContext.setExternalContext(mockExtCtx);
            input.setSubmittedValue("");
            input.validate(facesContext);

            assertEquals(null, input.getSubmittedValue());
        }
        finally
        {
            facesContext.setExternalContext(externalContext);
        }
    }
    */

    public void testValidateWithNonEmptyStringWithEmptyStringAsNullEnabled()
    {
        try
        {
            InitParameterMockExternalContext mockExtCtx =
                    new InitParameterMockExternalContext(servletContext, request, response);
            mockExtCtx.getInitParameterMap().put("javax.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL", "true");
            facesContext.setExternalContext(mockExtCtx);
            input.setValue("asd");
            input.setSubmittedValue("asd");
            input.validate(facesContext);

            assertEquals(null, input.getSubmittedValue());
            assertEquals("asd", input.getValue());
        }
        finally
        {
            facesContext.setExternalContext(externalContext);
        }
    }

    public void testValidateWithEmptyStringWithEmptyStringAsNullDisabled()
    {
        try
        {
            InitParameterMockExternalContext mockExtCtx =
                    new InitParameterMockExternalContext(servletContext, request, response);
            mockExtCtx.getInitParameterMap().put("javax.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL", "false");
            facesContext.setExternalContext(mockExtCtx);
            input.setValue("");
            input.setSubmittedValue("");
            input.validate(facesContext);

            assertEquals("", input.getValue());
        }
        finally
        {
            facesContext.setExternalContext(externalContext);
        }
    }

    public void testValidateWithEmptyStringWithEmptyStringAsNullNotSpecified()
    {
        try
        {
            InitParameterMockExternalContext mockExtCtx =
                    new InitParameterMockExternalContext(servletContext, request, response);
            //mockExtCtx.getInitParameterMap().put("javax.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL", "false");
            facesContext.setExternalContext(mockExtCtx);
            input.setValue("");
            input.setSubmittedValue("");
            input.validate(facesContext);

            assertEquals("", input.getValue());
        }
        finally
        {
            facesContext.setExternalContext(externalContext);
        }
    }

    static public class InitParameterMockExternalContext extends org.apache.shale.test.mock.MockExternalContext {

        private Map initParameters = new HashMap();

        public InitParameterMockExternalContext(ServletContext context, HttpServletRequest request, HttpServletResponse response) {
            super(context, request, response);
        }

        public String encodePartialActionURL(String url) {
            return null;
        }

        public String getInitParameter(String name) {
            return (String) initParameters.get(name);
        }

        public Map getInitParameterMap() {
            return initParameters;
        }
    }

}
