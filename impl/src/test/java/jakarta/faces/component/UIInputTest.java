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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import jakarta.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.validator.LengthValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.myfaces.test.mock.MockRenderedValueExpression;
import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.apache.myfaces.test.el.MockValueExpression;
import org.apache.myfaces.test.mock.visit.MockVisitCallback;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UIInputTest extends AbstractFacesTestCase
{

    private Converter mockConverter;
    private Validator mockValidator;
    private UIInput input;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        input = new UIInput();
        input.setId("testId");
        mockConverter = createMock(Converter.class);
        mockValidator = createMock(Validator.class);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        super.tearDown();
        input = null;
        mockConverter = null;
        mockValidator = null;
    }
    
    public static class MyMockValueChangeBean 
    {
        public void changeValue(ValueChangeEvent evt)
        {
            throw new IllegalStateException("method should not be called");
        }
    }
    
    public static class MyMockValidatorBean implements Validator
    {
        public void validate(FacesContext context, UIComponent component,
                Object value) throws ValidatorException
        {
            ((UIInput)component).setValid(false);
        }
    }

    @Test
    void whenSpecifiedConverterMessageIsUsedInCaseConverterExceptionOccurs()
    {
        input.setConverterMessage("Cannot convert");

        input.setConverter(mockConverter);
        expect(mockConverter.getAsObject(facesContext, input, "xxx")).andThrow(new ConverterException());
        replay(mockConverter);

        input.setSubmittedValue("xxx");
        
        input.validate(facesContext);
        verify(mockConverter);

        assertFalse(input.isValid());
        assertNotNull(facesContext.getMessages("testId"));

        FacesMessage message = (FacesMessage) facesContext.getMessages("testId").next();
        assertEquals("Cannot convert", message.getDetail());
        assertEquals("Cannot convert", message.getSummary());
    }

    @Test
    void whenSpecifiedValidatorMessageIsUsedInCaseValidatorExceptionOccurs()
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
        assertEquals("Cannot validate", message.getDetail());
        assertEquals("Cannot validate", message.getSummary());
    }

    @Test
    void updateModelSetsTheLocalValueToModelValue()
    {
        input.setValue("testValue");

        ValueExpression expression = new MockValueExpression("#{requestScope.id}", String.class);
        input.setValueExpression("value", expression);

        input.updateModel(facesContext);

        String updatedValue = expression.getValue(facesContext.getELContext()).toString();
        assertEquals("testValue", updatedValue);
    }

    @Test
    void validateWithEmptyStringWithEmptyStringAsNullEnabled()
    {
        try
        {
            InitParameterMockExternalContext mockExtCtx =
                    new InitParameterMockExternalContext(servletContext, request, response);
            mockExtCtx.getInitParameterMap().put("jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL", "true");
            mockExtCtx.getInitParameterMap().put(UIInput.VALIDATE_EMPTY_FIELDS_PARAM_NAME, "true");
            facesContext.setExternalContext(mockExtCtx);
            
            input.addValidator(new Validator()
            {

                public void validate(FacesContext context,
                        UIComponent component, Object value)
                        throws ValidatorException
                {
                    // the value must be null
                    assertNull(value);
                }

                
            });
            
            input.setSubmittedValue("");
            input.validate(facesContext);

            assertNull(input.getSubmittedValue());
        }
        finally
        {
            facesContext.setExternalContext(externalContext);
        }
    }

    @Test
    void validateWithNonStringWithEmptyStringAsNullEnabled()
    {
        try
        {
            InitParameterMockExternalContext mockExtCtx =
                    new InitParameterMockExternalContext(servletContext, request, response);
            mockExtCtx.getInitParameterMap().put("jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL", "true");
            facesContext.setExternalContext(mockExtCtx);
            
            input.addValidator(new Validator()
            {

                public void validate(FacesContext context,
                        UIComponent component, Object value)
                        throws ValidatorException
                {
                    // the value must not be null
                    assertNotNull(value);
                    
                    // throw Exception to ensure this was called
                    throw new RuntimeException();
                }

                
            });
            
            // set Object with toString() returning "" as submittedValue
            input.setSubmittedValue(new Object()
            {

                @Override
                public String toString()
                {
                    return "";
                }
                
            });
            
            try
            {
                input.validate(facesContext);
                
                fail(); // validate() was not called --> fail!
            }
            catch (RuntimeException e)
            {
                // great - validate() was called!
            }
        }
        finally
        {
            facesContext.setExternalContext(externalContext);
        }
    }

    @Test
    void validateWithNonEmptyStringWithEmptyStringAsNullEnabled()
    {
        try
        {
            InitParameterMockExternalContext mockExtCtx =
                    new InitParameterMockExternalContext(servletContext, request, response);
            mockExtCtx.getInitParameterMap().put("jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL", "true");
            facesContext.setExternalContext(mockExtCtx);
            input.setValue("asd");
            input.setSubmittedValue("asd");
            input.validate(facesContext);

            assertNull(input.getSubmittedValue());
            assertEquals("asd", input.getValue());
        }
        finally
        {
            facesContext.setExternalContext(externalContext);
        }
    }

    @Test
    void validateWithEmptyStringWithEmptyStringAsNullDisabled()
    {
        try
        {
            InitParameterMockExternalContext mockExtCtx =
                    new InitParameterMockExternalContext(servletContext, request, response);
            mockExtCtx.getInitParameterMap().put("jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL", "false");
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

    @Test
    void validateWithEmptyStringWithEmptyStringAsNullNotSpecified()
    {
        try
        {
            InitParameterMockExternalContext mockExtCtx =
                    new InitParameterMockExternalContext(servletContext, request, response);
            //mockExtCtx.getInitParameterMap().put("jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL", "false");
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

    /**
     * Tests if UIInput.processValidators() correctly calls FacesContext.validationFailed()
     * if a validation error occurs.
     */
    @Test
    void validationErrorTriggersFacesContextValidationFailed()
    {
        LengthValidator validator = new LengthValidator();
        validator.setMinimum(5);
        input.addValidator(validator);
        
        input.setSubmittedValue("123");
        
        assertFalse(facesContext.isValidationFailed());
        input.processValidators(facesContext);
        assertTrue(facesContext.isValidationFailed());
    }

    static public class InitParameterMockExternalContext extends org.apache.myfaces.test.mock.MockExternalContext {

        private Map initParameters = new HashMap();

        public InitParameterMockExternalContext(ServletContext context, HttpServletRequest request, HttpServletResponse response) {
            super(context, request, response);
        }

        @Override
        public String encodePartialActionURL(String url) {
            return null;
        }

        @Override
        public String getInitParameter(String name) {
            return (String) initParameters.get(name);
        }

        @Override
        public Map getInitParameterMap() {
            return initParameters;
        }
    }

    @Test
    void processDecodesRenderedFalse() throws Exception {
        input = new VerifyNoLifecycleMethodComponent();
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, input, false);
        
        input.processDecodes(facesContext);
        
        assertEquals(parent, UIComponent.getCurrentComponent(facesContext), "processDecodes must not change currentComponent");
        
    }

    @Test
    void processDecodesRenderedTrue() throws Exception {
        
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, input, true);
        _setUpValueExpressionForImmediate();
        
        input.processDecodes(facesContext);
        
        assertEquals(parent, UIComponent.getCurrentComponent(facesContext), "processDecodes must not change currentComponent");
    }

    @Test
    void processValidatorsRenderedFalse() throws Exception {
        input = new VerifyNoLifecycleMethodComponent();
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, input, false);
        
        input.processValidators(facesContext);
        
        assertEquals(parent, UIComponent.getCurrentComponent(facesContext), "processValidators must not change currentComponent");
        
    }

    @Test
    void processValidatorsRenderedTrue() throws Exception {
        
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, input, true);
        _setUpValueExpressionForImmediate();
        
        input.processValidators(facesContext);
        
        assertEquals(parent, UIComponent.getCurrentComponent(facesContext), "processValidators must not change currentComponent");
    }

    @Test
    void processUpdatesRenderedFalse() throws Exception {
        input = new VerifyNoLifecycleMethodComponent();    
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext,input, false);
        
        input.processUpdates(facesContext);
        
        assertEquals(parent, UIComponent.getCurrentComponent(facesContext), "processValidators must not change currentComponent");
        
    }

    @Test
    void processUpdatesRenderedTrue() throws Exception {
        
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, input, true);
        _setUpValueExpressionForImmediate();
        
        input.processUpdates(facesContext);
        
        assertEquals(parent, UIComponent.getCurrentComponent(facesContext), "processValidators must not change currentComponent");
    }

    @Test
    void visitTree() throws Exception {
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, input, true);
        
        VisitContext visitContext = VisitContext.createVisitContext(facesContext, null, EnumSet.of(VisitHint.SKIP_UNRENDERED));
        MockVisitCallback mockVisitCallback = new MockVisitCallback();
        input.visitTree(visitContext, mockVisitCallback);
        
        assertEquals(parent, UIComponent.getCurrentComponent(facesContext), "visitTree must not change currentComponent");
    }
    
    /** Verifies no call to encode* and process* methods */
    public class VerifyNoLifecycleMethodComponent extends UIInput
    {
        @Override
        public void decode(FacesContext context) {
            fail();
        }
        @Override
        public void validate(FacesContext context) {
            fail();
        }
        @Override
        public void updateModel(FacesContext context) {
            fail();
        }
        @Override
        public void encodeBegin(FacesContext context) throws IOException {
            fail();
        }
        @Override
        public void encodeChildren(FacesContext context) throws IOException {
            fail();
        }
        @Override
        public void encodeEnd(FacesContext context) throws IOException {
            fail();
        }
    }
    
    private void _setUpValueExpressionForImmediate() {
        ValueExpression ve = new MockRenderedValueExpression("#{component.id eq 'testId'}", Boolean.class, input, true);
        input.setValueExpression("immediate", ve);
    }
}
