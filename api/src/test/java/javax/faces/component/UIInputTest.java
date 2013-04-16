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

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.el.MethodBinding;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.LengthValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.myfaces.mock.MockRenderedValueExpression;
import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.apache.myfaces.test.el.MockMethodExpression;
import org.apache.myfaces.test.el.MockValueExpression;
import org.apache.myfaces.test.mock.visit.MockVisitCallback;

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
    
    public void testValidateNotCallValueChangeListenerWhenCallValidateWithBinding()
    {
        MethodExpression itemValue = new MockMethodExpression("#{valueChangeBean.changeValue}", 
                new Class[]{ValueChangeEvent.class} , MyMockValueChangeBean.class);
        externalContext.getRequestMap().put("valueChangeBean", new MyMockValueChangeBean());
        input.setValueChangeListener(new _MethodExpressionToMethodBinding(itemValue));
        
        request.setAttribute("mockValidator",new MyMockValidatorBean());
        
        MethodBinding binding = application.createMethodBinding(
                "#{requestScope.mockValidator.validate}",
                new Class[] { FacesContext.class, UIComponent.class, Object.class }
            );
        input.setValidator(binding);
        
        UIViewRoot root = new UIViewRoot();
        
        root.getChildren().add(input);
        input.setSubmittedValue("xxx");
        input.processValidators(facesContext);
        input.setValid(true);
        input.processValidators(facesContext);
        root.broadcastEvents(facesContext, PhaseId.PROCESS_VALIDATIONS);
        assertNotNull(input.getSubmittedValue());
    }
    
    public void testValidateNotCallValueChangeListenerWhenCallValidateWithValidator()
    {
        MethodExpression itemValue = new MockMethodExpression("#{valueChangeBean.changeValue}", 
                new Class[]{ValueChangeEvent.class} , MyMockValueChangeBean.class);
        externalContext.getRequestMap().put("valueChangeBean", new MyMockValueChangeBean());
        input.setValueChangeListener(new _MethodExpressionToMethodBinding(itemValue));
        
        input.addValidator(new Validator(){

            public void validate(FacesContext context, UIComponent component,
                    Object value) throws ValidatorException
            {
                ((UIInput)component).setValid(false);
            }
        });
        
        UIViewRoot root = new UIViewRoot();
        
        root.getChildren().add(input);
        input.setSubmittedValue("xxx");
        input.processValidators(facesContext);
        root.broadcastEvents(facesContext, PhaseId.PROCESS_VALIDATIONS);
        assertNotNull(input.getSubmittedValue());
    }

    public void testWhenSpecifiedConverterMessageIsUsedInCaseConverterExceptionOccurs()
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

    public void testValidateWithEmptyStringWithEmptyStringAsNullEnabled()
    {
        try
        {
            InitParameterMockExternalContext mockExtCtx =
                    new InitParameterMockExternalContext(servletContext, request, response);
            mockExtCtx.getInitParameterMap().put("javax.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL", "true");
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

            assertEquals(null, input.getSubmittedValue());
        }
        finally
        {
            facesContext.setExternalContext(externalContext);
        }
    }
    
    public void testValidateWithNonStringWithEmptyStringAsNullEnabled()
    {
        try
        {
            InitParameterMockExternalContext mockExtCtx =
                    new InitParameterMockExternalContext(servletContext, request, response);
            mockExtCtx.getInitParameterMap().put("javax.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL", "true");
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
    
    /**
     * Tests if UIInput.processValidators() correctly calls FacesContext.validationFailed()
     * if a validation error occurs.
     */
    public void testValidationErrorTriggersFacesContextValidationFailed()
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
    
    public void testProcessDecodesRenderedFalse() throws Exception {
        input = new VerifyNoLifecycleMethodComponent();
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, input, false);
        
        input.processDecodes(facesContext);
        
        assertEquals("processDecodes must not change currentComponent", parent, UIComponent.getCurrentComponent(facesContext));
        
    }
    
    public void testProcessDecodesRenderedTrue() throws Exception {
        
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, input, true);
        _setUpValueExpressionForImmediate();
        
        input.processDecodes(facesContext);
        
        assertEquals("processDecodes must not change currentComponent", parent, UIComponent.getCurrentComponent(facesContext));
    }
    
    public void testProcessValidatorsRenderedFalse() throws Exception {
        input = new VerifyNoLifecycleMethodComponent();
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, input, false);
        
        input.processValidators(facesContext);
        
        assertEquals("processValidators must not change currentComponent", parent, UIComponent.getCurrentComponent(facesContext));
        
    }
    
    public void testProcessValidatorsRenderedTrue() throws Exception {
        
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, input, true);
        _setUpValueExpressionForImmediate();
        
        input.processValidators(facesContext);
        
        assertEquals("processValidators must not change currentComponent", parent, UIComponent.getCurrentComponent(facesContext));
    }
    
    
    public void testProcessUpdatesRenderedFalse() throws Exception {
        input = new VerifyNoLifecycleMethodComponent();    
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext,input, false);
        
        input.processUpdates(facesContext);
        
        assertEquals("processValidators must not change currentComponent", parent, UIComponent.getCurrentComponent(facesContext));
        
    }
    
    public void testProcessUpdatesRenderedTrue() throws Exception {
        
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, input, true);
        _setUpValueExpressionForImmediate();
        
        input.processUpdates(facesContext);
        
        assertEquals("processValidators must not change currentComponent", parent, UIComponent.getCurrentComponent(facesContext));
    }
    
    public void testVisitTree() throws Exception {
        UIComponent parent = MockRenderedValueExpression.setUpComponentStack(facesContext, input, true);
        
        VisitContext visitContext = VisitContext.createVisitContext(facesContext, null, EnumSet.of(VisitHint.SKIP_UNRENDERED));
        MockVisitCallback mockVisitCallback = new MockVisitCallback();
        input.visitTree(visitContext, mockVisitCallback);
        
        assertEquals("visitTree must not change currentComponent", parent, UIComponent.getCurrentComponent(facesContext));
    }
    
    /** Verifies no call to encode* and process* methods */
    public class VerifyNoLifecycleMethodComponent extends UIInput
    {
        public void decode(FacesContext context) {
            fail();
        }
        public void validate(FacesContext context) {
            fail();
        }
        public void updateModel(FacesContext context) {
            fail();
        }
        public void encodeBegin(FacesContext context) throws IOException {
            fail();
        }
        public void encodeChildren(FacesContext context) throws IOException {
            fail();
        }
        public void encodeEnd(FacesContext context) throws IOException {
            fail();
        }
    }
    
    private void _setUpValueExpressionForImmediate() {
        ValueExpression ve = new MockRenderedValueExpression("#{component.id eq 'testId'}", Boolean.class, input, true);
        input.setValueExpression("immediate", ve);
    }
}
